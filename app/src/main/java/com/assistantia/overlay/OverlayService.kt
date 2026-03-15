package com.assistantia.overlay

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OverlayService : Service() {
    private var wm: WindowManager? = null
    private var view: android.view.View? = null
    private var webView: WebView? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, f: Int, id: Int): Int {
        when (intent?.action) { "START" -> show(); "STOP" -> hide() }
        return START_NOT_STICKY
    }

    inner class Bridge {
        @JavascriptInterface
        fun analyzeRegion(x: Int, y: Int, w: Int, h: Int) {
            takeScreenshotAndOCR(x, y, w, h)
        }
    }

    private fun show() {
        if (view != null) return
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSPARENT)
        p.gravity = Gravity.TOP or Gravity.START
        view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        webView = view!!.findViewById<WebView>(R.id.webView)
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView!!.addJavascriptInterface(Bridge(), "Android")
        webView!!.webViewClient = WebViewClient()
        webView!!.setBackgroundColor(0x00000000)
        webView!!.loadUrl("file:///android_asset/index.html")
        wm!!.addView(view, p)
    }

    private fun takeScreenshotAndOCR(x: Int, y: Int, w: Int, h: Int) {
        try {
            val rootView = view!!.rootView
            rootView.isDrawingCacheEnabled = true
            val full = Bitmap.createBitmap(rootView.drawingCache)
            rootView.isDrawingCacheEnabled = false
            val cropped = Bitmap.createBitmap(full, x, y, w, h)
            val image = InputImage.fromBitmap(cropped, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text.replace("\"", "\\\"").replace("\n", " ")
                    webView?.post {
                        webView?.evaluateJavascript("receiveText(\"$text\")", null)
                    }
                }
                .addOnFailureListener {
                    webView?.post {
                        webView?.evaluateJavascript("receiveText(\"Erreur OCR\")", null)
                    }
                }
        } catch (e: Exception) {
            webView?.post {
                webView?.evaluateJavascript("receiveText(\"Erreur: ${e.message}\")", null)
            }
        }
    }

    private fun hide() { view?.let { wm?.removeView(it); view = null }; stopSelf() }
}
