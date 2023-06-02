package com.kola.webgame

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.kola.webgame.bean.IconConfig
import com.kola.webgame.databinding.ActivityMainBinding
import com.kola.webgame.utils.KUtils
import com.kola.webgame.webview.MyWebViewClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = "Kola"
    private val OkSpin_Key = "nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF"
    private val OkSpin_Placement = "10868"
    private var Default_Url = "https://cart.minigame.vip/game/popstone2/play"
    private var OKS_API =
        "https://s.oksp.in/v1/spin/tml?pid=10772&appk=nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF&did={did}"

    //通过ViewBind创建View
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mIconConfig: IconConfig = IconConfig()
    private var oksReady = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w(TAG, "onCreate: ")
        setContentView(binding.root)
        initView()
        initFireBase()
        refershOKSIcon()
    }

    private fun initFireBase() {
        FirebaseApp.initializeApp(this)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (com.kola.webgame.BuildConfig.DEBUG) 10 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        try {
            refreshConfig(remoteConfig.getString("config"))
            remoteConfig.fetchAndActivate().addOnCompleteListener(this) {
                refreshConfig(remoteConfig.getString("config"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun refreshConfig(config: String) {
        Log.w(TAG, "refreshConfig: $config")
        if (ObjectUtils.isNotEmpty(config)) {
            mIconConfig = GsonUtils.fromJson(config, IconConfig::class.java)
        }
        refreshScreenOrientation(mIconConfig.isLock)
//        if (ObjectUtils.isNotEmpty(mIconConfig.url)) {
//            Default_Url = mIconConfig.url
//        }
//        initView()
//        lifecycleScope.launch {
//            mIconConfig.url.let {
//                mIconConfig.url =
//                    KUtils.getInstance().replaceUrl(this@MainActivity, it, userId).toString()
//
//            }
//        }
    }

    private fun initView() {
//        binding.web.setJsBridge(JsBri)
        WebView.setWebContentsDebuggingEnabled(com.kola.webgame.BuildConfig.DEBUG)
        binding.web.webViewClient =
            MyWebViewClient(this, object : MyWebViewClient.OnPageFinishedListener {
                override fun onPageFinished(url: String) {
                    binding.splash.visibility = View.GONE
                }
            })
        binding.web.settings.javaScriptEnabled = true
        binding.web.settings.allowFileAccess = true
        binding.web.settings.domStorageEnabled = true
        binding.web.settings.allowContentAccess = true
        binding.web.settings.javaScriptCanOpenWindowsAutomatically = true
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.web, true);
        binding.web.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
//        MobileAds.registerWebView(binding.web);
        Log.w(TAG, "initView: url:$Default_Url")
        binding.web.loadUrl(Default_Url)


        binding.myWeb.webViewClient =
            MyWebViewClient(this, null)
        binding.myWeb.settings.javaScriptEnabled = true
        binding.myWeb.settings.allowFileAccess = true
        binding.myWeb.settings.domStorageEnabled = true
        binding.myWeb.settings.allowContentAccess = true
        binding.myWeb.settings.javaScriptCanOpenWindowsAutomatically = true
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.myWeb, true);
        binding.myWeb.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
    }

    private fun refershOKSIcon() {
        Log.w(TAG, "refershOKSIcon")
        //使用Coil加载图片到binding.ivIcon上
        if (!mIconConfig.isOpen()) return
        if (ObjectUtils.isNotEmpty(mIconConfig.icon)) {
            Log.w(TAG, "refershOKSIcon: online icon" + mIconConfig.icon)
            binding.ivIcon.load(mIconConfig.icon)
        } else {
            Log.w(TAG, "refershOKSIcon: local icon")
            binding.ivIcon.load(R.drawable.icon)
        }
        //动态修改ivIcon的位置
        val set = ConstraintSet()
        set.clone(binding.root)
        set.setVerticalBias(binding.ivIcon.id, mIconConfig.verticalBias)
        set.setHorizontalBias(binding.ivIcon.id, mIconConfig.horizontalBias)
        set.applyTo(binding.root)
        if (mIconConfig.alwaysShow()) {
            binding.ivIcon.visibility = View.VISIBLE
        } else {
            handleView()
        }

        binding.ivIcon.setOnClickListener {
            binding.myWeb.loadUrl(KUtils.getInstance().replaceGaid(this, OKS_API))
            binding.myWeb.visibility = View.VISIBLE
            if (!mIconConfig.alwaysShow()) {
                binding.ivIcon.visibility = View.GONE
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun handleView(
        myView: View = binding.ivIcon,
        delayShow: Long = mIconConfig.showTimeX * 1000L,
        showDuration: Long = mIconConfig.showTimeZ * 1000L,
        hideDuration: Long = mIconConfig.showTimeY * 1000L,
    ) {
        var isViewShown = false // 使用标志跟踪视图是否已经显示

        // 使用协程检查是否需要隐藏视图
        fun checkHideView() {
            GlobalScope.launch(Dispatchers.Main) {
                delay(showDuration)
                myView.visibility = View.GONE
                // 使用协程再次显示视图
                delay(hideDuration)
                if (isViewShown) {
                    handleView(myView = myView, delayShow = 0)
                }
            }
        }

        // 使用协程在指定延迟后显示视图
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayShow)
            myView.visibility = View.VISIBLE
            isViewShown = true // 当视图第一次显示时设置标志
            checkHideView() // 在视图第一次显示后调用 checkHideView() 函数
        }

        // 在视图附加到窗口时重新计时以再次显示视图
        myView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(p0: View) {
            }

            override fun onViewDetachedFromWindow(p0: View) {
                // 取消协程作业
                GlobalScope.coroutineContext.cancelChildren()
            }
        })

        // 点击视图时隐藏它
        myView.setOnClickListener {
            myView.visibility = View.GONE
            // 使用协程再次显示视图
            GlobalScope.launch(Dispatchers.Main) {
                delay(hideDuration)
                handleView(myView = myView, delayShow = 0)
            }
        }
    }

    /**
     * 根据当前是否竖屏来设置屏幕方向
     */
    private fun refreshScreenOrientation(isLock: Int = 1) {
        if (isLock == 0) return
        requestedOrientation = if (isLock == 1) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    override fun onBackPressed() {
        //如果当前myWebView可见
        if (binding.myWeb.visibility == View.VISIBLE) {
            binding.myWeb.close()
        } else {
            super.onBackPressed()
        }
    }
}