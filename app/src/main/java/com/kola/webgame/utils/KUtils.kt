package com.kola.webgame.utils

import android.content.Context
import android.os.Build.VERSION_CODES.P
import com.blankj.utilcode.util.ThreadUtils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

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

    /**
     * 获取设备的 GAID
     * @param context 上下文
     * @return 设备的 GAID
     */
    private fun getDeviceGaid(context: Context): String? {
        var gaid: String? = null
        try {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
            gaid = info.id
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        }
        return gaid
    }

    suspend fun replaceUrl(context: Context, url: String, userUUid: String): String? {
        return withContext(Dispatchers.IO) {
            getDeviceGaid(context)?.let {
                url.replace("{did}", it)
            }
        }
    }
}