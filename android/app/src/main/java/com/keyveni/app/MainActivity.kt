package com.keyveni.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null
    private val pickFileRequest = 4107
    private val siteHost = "hexspasium.github.io"
    private val siteUrl = "https://hexspasium.github.io/keyveni/"

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.statusBarColor = android.graphics.Color.rgb(7, 23, 17)
        window.navigationBarColor = android.graphics.Color.rgb(7, 23, 17)
        webView = WebView(this)
        webView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        setContentView(webView)
        webView.settings.apply {
            javaScriptEnabled = true; domStorageEnabled = true; databaseEnabled = true
            loadsImagesAutomatically = true; mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            javaScriptCanOpenWindowsAutomatically = true; setSupportMultipleWindows(true)
            cacheMode = WebSettings.LOAD_DEFAULT; userAgentString = userAgentString + " KeyveniAndroid/1.0"
        }
        webView.isVerticalScrollBarEnabled = false; webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = routeUrl(request.url.toString())
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(v: WebView?, callback: ValueCallback<Array<Uri>>?, params: FileChooserParams?): Boolean {
                fileCallback?.onReceiveValue(null); fileCallback = callback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "image/*" }
                return try { startActivityForResult(Intent.createChooser(intent, "Ürün görseli seç"), pickFileRequest); true }
                catch (_: Exception) { fileCallback = null; false }
            }
            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                val popup = WebView(this@MainActivity)
                popup.settings.javaScriptEnabled = true
                popup.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean { routeUrl(request.url.toString()); v.destroy(); return true }
                    override fun onPageStarted(v: WebView, url: String, favicon: android.graphics.Bitmap?) { routeUrl(url); v.stopLoading(); v.destroy() }
                }
                transport.webView = popup; resultMsg.sendToTarget(); return true
            }
        }
        if (savedInstanceState == null) webView.loadUrl(siteUrl) else webView.restoreState(savedInstanceState)
    }

    private fun routeUrl(raw: String): Boolean {
        val uri = Uri.parse(raw); val scheme = uri.scheme ?: return true
        if (scheme == "https" && uri.host == siteHost) return false
        if (scheme == "about") return false
        return try { startActivity(Intent(Intent.ACTION_VIEW, uri)); true }
        catch (_: Exception) { Toast.makeText(this, "Bağlantı açılamadı.", Toast.LENGTH_SHORT).show(); true }
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickFileRequest) {
            val result = if (resultCode == RESULT_OK && data?.data != null) arrayOf(data.data!!) else null
            fileCallback?.onReceiveValue(result); fileCallback = null
        }
    }
    override fun onSaveInstanceState(outState: Bundle) { webView.saveState(outState); super.onSaveInstanceState(outState) }
    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() { if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed() }
    override fun onDestroy() { if (::webView.isInitialized) webView.destroy(); fileCallback?.onReceiveValue(null); super.onDestroy() }
}
