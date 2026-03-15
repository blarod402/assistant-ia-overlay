package com.assistantia.overlay

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")), 1234)
        }

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val apiInput = findViewById<EditText>(R.id.apiKeyInput)
        apiInput.setText(prefs.getString("gemini_key", ""))

        findViewById<Button>(R.id.btnSaveKey).setOnClickListener {
            val key = apiInput.text.toString().trim()
            if (key.isEmpty()) { Toast.makeText(this, "Clé vide !", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            prefs.edit().putString("gemini_key", key).apply()
            Toast.makeText(this, "✅ Clé sauvegardée !", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startService(Intent(this, OverlayService::class.java).apply { action = "START" })
            Toast.makeText(this, "✅ Assistant IA activé !", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            startService(Intent(this, OverlayService::class.java).apply { action = "STOP" })
        }
    }
}
