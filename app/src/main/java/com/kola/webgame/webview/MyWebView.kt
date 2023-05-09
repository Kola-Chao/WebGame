package com.kola.webgame.webview

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.blankj.utilcode.util.Utils


class MyWebView : WebView {
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setJsBridge()
    }

    //向webview注入自定义JavascriptInterface
    //请将您传入的自定义NAMESPACE发送给OkSpin进行配置
    private fun setJsBridge() {
        this.addJavascriptInterface(this, "YOUR_NAMESPACE")
    }

    @JavascriptInterface
    fun openBrowser(url: String) {
        try {
            var intent: Intent? = null
            intent = if (url.startsWith("intent")) {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } else {
                Intent("android.intent.action.VIEW", Uri.parse(url))
            }
            if (intent != null) {
                if (isHw()) {
                    intent.setPackage(getDefaultBrowser())
                }
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.component = null
                intent.flags = FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    //点击互动广告关闭按钮时调用该方法。
    @JavascriptInterface
    fun close() {
        //TODO  close the ad page or activity.
    }

    private fun isHw(): Boolean {
        return "huawei".equals(Build.MANUFACTURER, ignoreCase = true)
    }

    private fun getDefaultBrowser(): String? {
        var packageName: String? = null
        var systemApp: String? = null
        var userApp: String? = null
        val userAppList: MutableList<String?> = ArrayList()
        val context: Context = Utils.getApp()
        val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("https://"))
        val resolveInfo =
            context.packageManager.resolveActivity(
                browserIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        if (resolveInfo?.activityInfo != null) {
            packageName = resolveInfo.activityInfo.packageName
        }
        if (packageName == null || packageName == "android") {
            val lists = context.packageManager.queryIntentActivities(browserIntent, 0)
            for (app in lists) {
                if (app.activityInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                    systemApp = app.activityInfo.packageName
                } else {
                    userApp = app.activityInfo.packageName
                    userAppList.add(userApp)
                }
            }
            if (userAppList.contains("com.android.chrome")) {
                packageName = "com.android.chrome"
            } else {
                if (systemApp != null) {
                    packageName = systemApp
                }
                if (userApp != null) {
                    packageName = userApp
                }
            }
        }
        return packageName
    }
}