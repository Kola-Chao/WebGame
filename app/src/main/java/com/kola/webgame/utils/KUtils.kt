package com.kola.webgame.utils

import android.content.Context
import android.os.Build.VERSION_CODES.P
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException

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
    fun getDeviceGaid(context: Context): String? {
        var gaid: String? = null
        try {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
            gaid = info?.id
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        }
        return gaid
    }

    fun replaceUrl(context: Context, url: String, userUUid: String): String? {
        return getDeviceGaid(context)?.let {
            url.replace("{did}", it)
            url.replace("{user_uuid}", userUUid)
        }
    }
}