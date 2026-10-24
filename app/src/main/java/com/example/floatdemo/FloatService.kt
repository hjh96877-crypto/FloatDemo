package com.example.floatdemo

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast

class FloatService : Service() {
    private val ui = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var view: MatrixRainView? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        startForeground(1, buildNotification())
        val cmd = i?.getStringExtra("cmd") ?: "show"
        val color = i?.getStringExtra("color")
        val camera = cmd == "camera" || i?.getBooleanExtra("camera", false) == true

        if (cmd == "hide") {
            stopSelf()
            return START_NOT_STICKY
        }

        Thread {
            val ok = AccessUtil.hasAccess()
            ui.post {
                if (!ok) {
                    Toast.makeText(this, "未检测到 root 或 Shizuku 授权,无法使用", Toast.LENGTH_LONG).show()
                    stopSelf()
                } else {
                    showFloat(color)
                    if (camera) ui.postDelayed({ openFrontCamera() }, 300)
                }
            }
        }.start()
        return START_STICKY
    }

    private fun showFloat(color: String?) {
        if (view == null) {
            wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val v = MatrixRainView(this)
            params = WindowManager.LayoutParams(
                600, 800,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply { x = 100; y = 300 }
            enableDragAndClick(v)
            wm!!.addView(v, params)
            view = v
        }
        when (color) {
            "green"  -> view?.setColorIndex(0)
            "cyan"   -> view?.setColorIndex(1)
            "red"    -> view?.setColorIndex(2)
            "yellow" -> view?.setColorIndex(3)
        }
    }

    private fun openFrontCamera() {
        val base = Intent("android.media.action.STILL_IMAGE_CAMERA").apply {
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        for (pkg in listOf("com.oplus.camera", "com.oneplus.camera")) {
            try { startActivity(Intent(base).setPackage(pkg)); return } catch (e: Exception) { }
        }
        try { startActivity(base) } catch (e: Exception) { }
    }

    private fun enableDragAndClick(v: MatrixRainView) {
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        v.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    sx = e.rawX; sy = e.rawY
                    ox = params.x; oy = params.y; moved = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - sx).toInt()
                    val dy = (e.rawY - sy).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                    params.x = ox + dx; params.y = oy + dy
                    wm?.updateViewLayout(v, params)
                }
                MotionEvent.ACTION_UP -> if (!moved) v.nextColor()
            }
            true
        }
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel("float", "悬浮窗", NotificationManager.IMPORTANCE_LOW))
        return Notification.Builder(this, "float")
            .setContentTitle("悬浮窗运行中")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onDestroy() {
        try { view?.let { wm?.removeView(it) } } catch (e: Exception) { }
        view = null
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null
}
