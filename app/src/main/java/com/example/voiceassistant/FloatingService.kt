package com.example.voiceassistant

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: ImageView? = null
    private var pulseAnimator: ObjectAnimator? = null
    private var listening = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        addFloatingIcon()
    }

    private fun startForegroundWithNotification() {
        val channelId = "voice_assistant_channel"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Voice Assistant", NotificationManager.IMPORTANCE_LOW)
        )

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Voice Assistant চালু আছে")
            .setContentText("ফ্লোটিং মাইক আইকন সক্রিয়")
            .setSmallIcon(R.drawable.ic_mic)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun addFloatingIcon() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        val size = (56 * density).toInt()
        val pad = (14 * density).toInt()

        val icon = ImageView(this).apply {
            setImageResource(R.drawable.ic_mic)
            setBackgroundResource(R.drawable.bg_floating)
            setColorFilter(Color.WHITE)
            setPadding(pad, pad, pad, pad)
        }

        val params = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 300
        }

        // ড্র্যাগ ও ট্যাপ হ্যান্ডলিং
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var moved = false

        icon.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    params.x = startX + dx
                    params.y = startY + dy
                    windowManager.updateViewLayout(icon, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) onIconTapped(icon)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(icon, params)
        floatingView = icon
    }

    // আইকনে ট্যাপ করলে (মডিউল ২-এ এখানে আসল মাইক চালু হবে)
    private fun onIconTapped(icon: ImageView) {
        listening = !listening
        if (listening) {
            icon.backgroundTintList = ColorStateList.valueOf(Color.RED)
            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                icon,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 1.25f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 1.25f)
            ).apply {
                duration = 600
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
            Toast.makeText(this, "শুনছি...", Toast.LENGTH_SHORT).show()
        } else {
            pulseAnimator?.cancel()
            icon.scaleX = 1f
            icon.scaleY = 1f
            icon.backgroundTintList = null
            Toast.makeText(this, "মাইক বন্ধ", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        pulseAnimator?.cancel()
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
        super.onDestroy()
    }
}
