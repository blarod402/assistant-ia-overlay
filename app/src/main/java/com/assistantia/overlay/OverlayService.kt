package com.assistantia.overlay
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
class OverlayService : Service() {
    private var wm: WindowManager? = null
    private var view: android.view.View? = null
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, f: Int, id: Int): Int {
        when (intent?.action) { "START" -> show(); "STOP" -> hide() }
        return START_NOT_STICKY
    }
    private fun show() {
        if (view != null) return
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSPARENT)
        p.gravity = Gravity.TOP or Gravity.START
        view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        val wb = view!!.findViewById<WebView>(R.id.webView)
        wb.settings.javaScriptEnabled = true
        wb.settings.domStorageEnabled = true
        wb.settings.cacheMode = WebSettings.LOAD_DEFAULT
        wb.webViewClient = WebViewClient()
        wb.loadUrl("file:///android_asset/index.html")
        wm!!.addView(view, p)
    }
    private fun hide() { view?.let { wm?.removeView(it); view = null }; stopSelf() }
}
