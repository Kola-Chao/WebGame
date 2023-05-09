package com.kola.webgame

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout.TabGravity
import com.kola.webgame.databinding.ActivityMainBinding
import com.kola.webgame.webview.MyWebViewClient
import com.spin.ok.gp.OkSpin
import com.spin.ok.gp.utils.Error


class MainActivity : AppCompatActivity(), OkSpin.SpinListener {
    private val TAG = "Kola"
    private val OkSpin_Key = "nZiTgPX3eXDXyIgBflNO49GO6gOTjxOF"
    private val OkSpin_Placement = "10772"

    //通过ViewBind创建View
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initSDK()
        initView()
    }

    private fun initSDK() {
        OkSpin.setListener(this)
        if (!OkSpin.isInit()) {
            OkSpin.initSDK(OkSpin_Key)
            OkSpin.loadIcon(OkSpin_Placement)
            OkSpin.setUserId(OkSpin.getUserId())
        }
    }

    private fun initView() {
//        binding.web.setJsBridge(JsBri)
        binding.web.webViewClient = MyWebViewClient(this)
        binding.web.settings.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.web, true);
        binding.web.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
//        MobileAds.registerWebView(binding.web);
        binding.web.loadUrl("https://cashbird.minigame.vip/game/pop-stone3/play?from=home")
        binding.icon.setOnClickListener {
            if (OkSpin.isGSpaceReady(OkSpin_Placement)) {
                OkSpin.openGSpace(OkSpin_Placement)
            }
        }
    }

    private fun showIcon(placementId: String?) {
        val iconView = OkSpin.showIcon(placementId)
        if (iconView != null) {
            if (iconView.parent != null) {
                (iconView.parent as ViewGroup).removeView(iconView)
            }
        }
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.icon.addView(iconView, layoutParams)
    }

    override fun onInitSuccess() {
        // 初始化成功
        Log.w(TAG, "onInitSuccess: ")
    }

    override fun onInitFailed(error: Error?) {
        // 初始化失败
        Log.w(TAG, "onInitFailed: $error")
    }

    override fun onIconReady(placement: String?) {
        // Placement 加载成功
        showIcon(placement)
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
        binding.web.close()
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