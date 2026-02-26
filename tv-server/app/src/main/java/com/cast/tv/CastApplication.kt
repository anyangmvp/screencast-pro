package com.cast.tv

import android.app.Application
import com.cast.tv.service.DiscoveryService
import timber.log.Timber

/**
 * 应用入口
 */
class CastApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("应用已启动")
        
        // 启动设备发现服务
        DiscoveryService.start(this)
    }
}
