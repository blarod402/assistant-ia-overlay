package com.assistantia.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        if (text.isEmpty()) { finish(); return }
        val apiKey = getSharedPreferences("prefs", MODE_PRIVATE).getString("gemini_key", "") ?: ""
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "⚠️ Configure ta clé Gemini dans l'app !", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish(); return
        }
        Toast.makeText(this, "⏳ IA en train de répondre...", Toast.LENGTH_SHORT).show()
        finish()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "Tu es un assistant francophone. Réponds clairement en français à cette question ou texte :\n\n$text")
                                })
                            })
                        })
                    })
                }
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.outputStream.write(body.toString().toByteArray())
                val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()
                val json = JSONObject(response)
                val reply = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                withContext(Dispatchers.Main) {
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Réponse IA", reply))
                    showNotification(reply)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showNotification("❌ Erreur: ${e.message}")
                }
            }
        }
    }

    private fun showNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("ai_reply", "Réponse IA", NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannel(channel)
        val notif = NotificationCompat.Builder(this, "ai_reply")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ Réponse copiée !")
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        nm.notify(1, notif)
    }
}
