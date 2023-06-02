package com.kola.webgame.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class KUtils {
    companion object {
        private var instance: KUtils? = null
        fun getInstance(): KUtils {
            if (instance == null) {
                synchronized(KUtils::class.java) {
                    if (instance == null) {
                        instance = KUtils()
                    }
                }
            }
            return instance!!
        }
    }

    //线程同步
    @Volatile
    var GAID: String? = null

    /**
     * 获取设备的 GAID
     * @param context 上下文
     * @return 设备的 GAID
     */
    fun getDeviceGaid(context: Context) {
        try {
            //子线程执行
            Thread {
                // 线程执行的代码
                GAID = AdvertisingIdClient.getAdvertisingIdInfo(context).id
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getGAID(context: Context): String? = suspendCancellableCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                continuation.resumeWith(Result.success(adInfo.id))
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    fun replaceGaid(context: Context, url: String): String {
        return url.replace("{did}", GAID ?: "00000000-0000-0000-0000-000000000000")
    }

    //获取AppName，并去除所有空格
    fun getAppName(context: Context): String {
        val packageManager = context.packageManager
        val applicationInfo: ApplicationInfo =
            packageManager.getApplicationInfo(context.packageName, 0)
        return applicationInfo.loadLabel(packageManager).toString().replace(" ", "")
    }
}