package com.kola.webgame

import android.graphics.drawable.Icon
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import coil.imageLoader
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ObjectUtils
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.kola.webgame.bean.IconConfig
import com.kola.webgame.databinding.ActivityMainBinding
import com.kola.webgame.utils.KUtils
import com.kola.webgame.webview.MyWebViewClient
import com.mn.n.m
import com.spin.ok.gp.OkSpin
import com.spin.ok.gp.OkSpin.initSDK
import com.spin.ok.gp.utils.Error
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OkSpin.SpinListener {
    private val TAG = "Kola"
    private val OkSpin_Key = "nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF"
    private val OkSpin_Placement = "10772"
    private var Default_Url = "https://cart.minigame.vip/game/happypuppy-crush/play"

    //通过ViewBind创建View
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mIconConfig: IconConfig = IconConfig()
    private val userId = OkSpin.getUserId()
    private val oksSDKReady = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initFireBase()
        initOkSpinSDK()
    }

    private fun initFireBase() {
        FirebaseApp.initializeApp(this)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        refreshConfig(remoteConfig.getString("config"))
        try {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (com.kola.webgame.BuildConfig.DEBUG) 10 else 3600
            }
            remoteConfig.fetchAndActivate().addOnCompleteListener(this) {
                refreshConfig(remoteConfig.getString("config"))
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun refreshConfig(config: String) {
        Log.w(TAG, "refreshConfig: $config")
        if (ObjectUtils.isEmpty(config)) return
        mIconConfig = GsonUtils.fromJson(config, IconConfig::class.java)
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

    private fun initOkSpinSDK() {
        OkSpin.setListener(this)
        if (!OkSpin.isInit()) {
            OkSpin.initSDK(OkSpin_Key)
            OkSpin.loadIcon(OkSpin_Placement)
            OkSpin.setUserId(userId)
        }
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
    }


    override fun onInitSuccess() {
        refershOKSIcon()
    }

    private fun refershOKSIcon() {
        //使用Coil加载图片到binding.ivIcon上
        if (!mIconConfig.isOpen()) return
        if (ObjectUtils.isNotEmpty(mIconConfig.icon)) {
            binding.ivIcon.load(mIconConfig.icon)
        } else {
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
            binding.ivIcon.setOnClickListener {
                if (OkSpin.isInteractiveReady(OkSpin_Placement)) {
                    OkSpin.openInteractive(OkSpin_Placement)
                    if (!mIconConfig.alwaysShow()) {
                        binding.ivIcon.visibility = View.GONE
                    }
                }
            }
        } else {
            handleView()
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
            if (OkSpin.isInteractiveReady(OkSpin_Placement)) {
                OkSpin.openInteractive(OkSpin_Placement)
            }
            myView.visibility = View.GONE
            // 使用协程再次显示视图
            GlobalScope.launch(Dispatchers.Main) {
                delay(hideDuration)
                handleView(myView = myView, delayShow = 0)
            }
        }
    }

    override fun onInitFailed(error: Error?) {
        // 初始化失败
        Log.w(TAG, "onInitFailed: $error")
    }

    override fun onIconReady(placement: String?) {
        // Placement 加载成功
//        showIcon(placement)
        Log.w(TAG, "onIconReady: $placement")
    }

    override fun onIconLoadFailed(placement: String?, error: Error?) {
        // Placement 加载失败
        Log.w(TAG, "onIconLoadFailed: $placement error: $error")
    }

    override fun onIconShowFailed(placementId: String?, error: Error?) {
        // Placement 素材展示失败
        Log.w(TAG, "onIconShowFailed: $placementId error: $error")
    }

    override fun onIconClick(placement: String?) {
        // Placement 被点击
        Log.w(TAG, "onIconClick: $placement")
    }

    override fun onInteractiveOpen(placement: String?) {
        // GSpace - Interactive Ads 页面被打开
        Log.w(TAG, "onInteractiveOpen: $placement")
    }

    override fun onInteractiveOpenFailed(placementId: String?, error: Error?) {
        // GSpace - Interactive Ads 页面打开失败
        Log.w(TAG, "onInteractiveOpenFailed: $placementId error: $error")
    }

    override fun onInteractiveClose(placement: String?) {
        // GSpace - Interactive Ads 页面被关闭
        Log.w(TAG, "onInteractiveClose: $placement")
    }

    override fun onOfferWallOpen(placementId: String?) {
        // GSpace - Interactive Wall 页面被打开
        Log.w(TAG, "onOfferWallOpen: $placementId")
    }

    override fun onOfferWallOpenFailed(placementId: String?, error: Error?) {
        // GSpace - Interactive Wall 页面打开失败
        Log.w(TAG, "onOfferWallOpenFailed: $placementId error: $error")
    }

    override fun onOfferWallClose(placementId: String?) {
        // GSpace - Interactive Wall 页面关闭
        Log.w(TAG, "onOfferWallClose: $placementId")
    }

    override fun onGSpaceOpen(placementId: String?) {
        // GSpace 页面打开
        Log.w(TAG, "onGSpaceOpen: $placementId")
    }

    override fun onGSpaceOpenFailed(placementId: String?, error: Error?) {
        // GSpace 页面打开失败
        Log.w(TAG, "onGSpaceOpenFailed: $placementId error: $error")
    }

    override fun onGSpaceClose(placementId: String?) {
        // GSpace 页面关闭
        Log.w(TAG, "onGSpaceClose: $placementId")
//        binding.web.close()
    }

    /**
     * 用户交互行为回调
     * INTERACTIVE_PLAY         GSpace - Interactive Ads 页面互动
     * INTERACTIVE_CLICK        GSpace - Interactive Ads 页面点击广告
     * OFFER_WALL_SHOW_DETAIL   GSpace - Interactive Wall 页面展示Offer详情
     * OFFER_WALL_GET_TASK      GSpace - Interactive Wall 页面领取Offer
     */
    override fun onUserInteraction(placementId: String?, interaction: String?) {
        Log.w(TAG, "onUserInteraction: $placementId interaction: $interaction")
    }


}