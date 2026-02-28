package com.cast.tv

import android.app.Application
import com.cast.tv.service.CastServerService
import com.cast.tv.service.DiscoveryService
import timber.log.Timber

/**
 * 应用入口
 * 
 * 管理应用级别的服务
 */
class CastApplication : Application() {
    
    // 服务运行状态
    var isServicesRunning = false
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("应用已启动")
    }
    
    /**
     * 启动所有服务
     */
    fun startAllServices() {
        if (isServicesRunning) {
            Timber.d("服务已在运行，无需重复启动")
            return
        }
        
        DiscoveryService.start(this)
        CastServerService.start(this)
        isServicesRunning = true
        Timber.d("所有服务已启动")
    }
    
    /**
     * 停止所有服务
     */
    fun stopAllServices() {
        if (!isServicesRunning) {
            Timber.d("服务未运行，无需停止")
            return
        }
        
        CastServerService.stop(this)
        DiscoveryService.stop(this)
        isServicesRunning = false
        Timber.d("所有服务已停止")
    }
}
