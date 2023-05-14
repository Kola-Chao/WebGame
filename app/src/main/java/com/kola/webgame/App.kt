package com.kola.webgame

import android.app.Application
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.DebugLogger
import com.blankj.utilcode.util.Utils
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
        com.ks.vny.lqh.e.a(this)
    }

    override fun newImageLoader(): ImageLoader = imageLoader.get()
}