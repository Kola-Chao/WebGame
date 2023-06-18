package com.kola.webgame

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kola.webgame.utils.KUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    @Inject
    lateinit var imageLoader: Provider<ImageLoader>

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        com.mn.sdk.init(this, "fc5ka7dm2ladincj")
//        com.ks.vny.lqh.e.a(this)
        KUtils.getInstance().getDeviceGaid(this)
        //如果发生了全局未捕获的异常，可以在这里捕获
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            //上报Firebase异常
            FirebaseCrashlytics.getInstance().recordException(e)
            //重启app
            AppUtils.relaunchApp()
        }
    }

    override fun newImageLoader(): ImageLoader = imageLoader.get()
}