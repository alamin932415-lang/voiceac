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

        // নোটিফিকেশন পারমিশন (Android 13+)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val info = TextView(this).apply {
            text = "Voice Assistant - মডিউল ১\nফ্লোটিং মাইক আইকন টেস্ট"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val startBtn = Button(this).apply {
            text = "ফ্লোটিং মাইক চালু করুন"
            setOnClickListener { startFloating() }
        }

        val stopBtn = Button(this).apply {
            text = "ফ্লোটিং মাইক বন্ধ করুন"
            setOnClickListener { stopService(Intent(this@MainActivity, FloatingService::class.java)) }
        }

        layout.addView(info)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    private fun startFloating() {
        // অন্য অ্যাপের ওপর দেখানোর পারমিশন আছে কিনা দেখা
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
        ContextCompat.startForegroundService(this, Intent(this, FloatingService::class.java))
    }
}
