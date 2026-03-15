package com.assistantia.overlay
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")), 1234)
        }
        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startService(Intent(this, OverlayService::class.java).apply { action = "START" })
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            startService(Intent(this, OverlayService::class.java).apply { action = "STOP" })
        }
    }
}
