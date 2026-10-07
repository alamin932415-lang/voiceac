package com.example.voiceassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val info = TextView(this).apply {
            text = "Voice Assistant - মডিউল ২\n\nআইকনে ট্যাপ = কথা শোনা\nআইকনে চেপে ধরে রাখা = ভাষা বদল (বাংলা/ইংরেজি)"
            textSize = 17f
            gravity = Gravity.CENTER
        }

        val startBtn = Button(this).apply {
            text = "ফ্লোটিং মাইক চালু করুন"
            setOnClickListener { startFloating() }
        }

        val stopBtn = Button(this).apply {
            text = "ফ্লোটিং মাইক বন্ধ করুন"
            setOnClickListener {
                stopService(Intent(this@MainActivity, FloatingService::class.java))
            }
        }

        layout.addView(info)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    private fun startFloating() {
        // ১. মাইক্রোফোন (ও নোটিফিকেশন) অনুমতি
        val needed = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) {
            requestPermissions(needed.toTypedArray(), 1)
            Toast.makeText(this, "অনুমতি দিয়ে আবার বাটন চাপুন", Toast.LENGTH_LONG).show()
            return
        }

        // ২. অন্য অ্যাপের ওপর দেখানোর অনুমতি
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "অনুমতি দিন: অন্য অ্যাপের ওপর দেখানো", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }

        // ৩. সার্ভিস চালু
        ContextCompat.startForegroundService(this, Intent(this, FloatingService::class.java))
    }
}
