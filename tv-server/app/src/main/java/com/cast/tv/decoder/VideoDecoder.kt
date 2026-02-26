package com.cast.tv.decoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import kotlinx.coroutines.*
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * 视频解码器
 * 
 * 使用Android MediaCodec硬件解码H.264视频流
 */
class VideoDecoder(private val surface: Surface) {
    
    companion object {
        const val MIME_TYPE = "video/avc"  // H.264
        const val DEFAULT_WIDTH = 1920
        const val DEFAULT_HEIGHT = 1080
    }
    
    private var decoder: MediaCodec? = null
    private var width = DEFAULT_WIDTH
    private var height = DEFAULT_HEIGHT
    
    private val decoderScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var isRunning = false
    
    // 帧队列
    private val frameQueue = ArrayDeque<VideoFrame>()
    private val queueLock = Object()
    private val MAX_QUEUE_SIZE = 5  // 最大队列大小，防止延迟过大
    
    data class VideoFrame(
        val data: ByteArray,
        val timestamp: Long,
        val isKeyFrame: Boolean
    )
    
    /**
     * 初始化解码器
     */
    fun initialize(videoWidth: Int = DEFAULT_WIDTH, videoHeight: Int = DEFAULT_HEIGHT) {
        width = videoWidth
        height = videoHeight
        
        try {
            // 创建解码器
            decoder = MediaCodec.createDecoderByType(MIME_TYPE)
            
            // 配置格式
            val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, 
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            
            // 配置解码器
            decoder?.configure(format, surface, null, 0)
            decoder?.start()
            
            isRunning = true
            
            // 启动解码循环
            decoderScope.launch {
                decodeLoop()
            }
            
            Timber.d("视频解码器已初始化: ${width}x$height")
            
        } catch (e: Exception) {
            Timber.e(e, "初始化解码器失败")
            release()
        }
    }
    
    /**
     * 释放解码器
     */
    fun release() {
        isRunning = false
        
        decoderScope.cancel()
        
        synchronized(queueLock) {
            frameQueue.clear()
            queueLock.notifyAll()
        }
        
        try {
            decoder?.stop()
            decoder?.release()
            decoder = null
        } catch (e: Exception) {
            Timber.e(e, "释放解码器失败")
        }
        
        Timber.d("视频解码器已释放")
    }
    
    /**
     * 解码视频帧
     */
    fun decodeFrame(data: ByteArray, timestamp: Long) {
        if (!isRunning) return
        
        // 判断是否为关键帧 (H.264 NAL单元类型5)
        val isKeyFrame = data.size > 4 && 
            ((data[4].toInt() and 0x1F) == 5)
        
        synchronized(queueLock) {
            // 如果队列已满，移除最旧的帧（如果是关键帧则保留）
            while (frameQueue.size >= MAX_QUEUE_SIZE) {
                val removed = frameQueue.removeFirst()
                if (removed.isKeyFrame && !isKeyFrame) {
                    // 如果移除的是关键帧而新来的不是，保留关键帧
                    frameQueue.addFirst(removed)
                    return  // 丢弃新帧
                }
            }
            
            frameQueue.addLast(VideoFrame(data, timestamp, isKeyFrame))
            queueLock.notify()
        }
    }
    
    /**
     * 解码循环
     */
    private suspend fun decodeLoop() {
        val decoder = this.decoder ?: return
        
        val bufferInfo = MediaCodec.BufferInfo()
        
        while (isRunning) {
            try {
                // 获取输入缓冲区索引
                val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                
                if (inputBufferIndex >= 0) {
                    val frame = getFrameFromQueue()
                    
                    if (frame != null) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                        inputBuffer?.clear()
                        inputBuffer?.put(frame.data)
                        
                        decoder.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            frame.data.size,
                            frame.timestamp,
                            0
                        )
                    } else {
                        // 没有数据，提交空缓冲区
                        decoder.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            0,
                            0
                        )
                    }
                }
                
                // 获取输出缓冲区
                val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                
                when {
                    outputBufferIndex >= 0 -> {
                        // 渲染到Surface
                        decoder.releaseOutputBuffer(outputBufferIndex, true)
                    }
                    outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val format = decoder.outputFormat
                        Timber.d("输出格式改变: $format")
                    }
                }
                
            } catch (e: Exception) {
                if (isRunning) {
                    Timber.e(e, "解码出错")
                }
            }
            
            yield()  // 让出时间片
        }
    }
    
    /**
     * 从队列获取帧
     */
    private fun getFrameFromQueue(): VideoFrame? {
        synchronized(queueLock) {
            while (frameQueue.isEmpty() && isRunning) {
                queueLock.wait(100)
            }
            return if (frameQueue.isNotEmpty()) frameQueue.removeFirst() else null
        }
    }
}
