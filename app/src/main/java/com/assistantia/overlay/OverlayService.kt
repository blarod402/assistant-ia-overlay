package com.assistantia.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.webkit.JavascriptInterface
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

    inner class Bridge {
        @JavascriptInterface
        fun openApp(pkg: String) {
            val i = packageManager.getLaunchIntentForPackage(pkg)
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
            }
        }

        @JavascriptInterface
        fun goBack() {
            val i = Intent(Intent.ACTION_MAIN)
            i.addCategory(Intent.CATEGORY_HOME)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }

        @JavascriptInterface
        fun getInstalledApps(): String {
            val apps = packageManager.getInstalledApplications(0)
            val result = StringBuilder("[")
            var first = true
            for (app in apps) {
                val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launch != null) {
                    val name = packageManager.getApplicationLabel(app).toString()
                        .replace("\"", "\\\"")
                    if (!first) result.append(",")
                    result.append("{\"name\":\"$name\",\"pkg\":\"${app.packageName}\"}")
                    first = false
                }
            }
            result.append("]")
            return result.toString()
        }
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
        wb.addJavascriptInterface(Bridge(), "Android")
        wb.webViewClient = WebViewClient()
        wb.loadUrl("file:///android_asset/index.html")
        wm!!.addView(view, p)
    }

    private fun hide() {
        view?.let { wm?.removeView(it); view = null }
        stopSelf()
    }
}
