package com.example.floatdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast

class MainActivity : Activity() {
    private val ui = Handler(Looper.getMainLooper())
    private var rootOk = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Toast.makeText(this, "正在检测 root 权限…", Toast.LENGTH_SHORT).show()
        Thread {
            val ok = RootUtil.hasRoot()
            ui.post { if (ok) onRootOk() else denyAndExit() }
        }.start()
    }

    private fun denyAndExit() {
        AlertDialog.Builder(this)
            .setTitle("无法使用")
            .setMessage("未检测到 root 权限,应用无法打开。请在提权管理器中授权后重试。")
            .setCancelable(false)
            .setPositiveButton("退出") { _, _ -> finishAndRemoveTask() }
            .show()
    }

    private fun onRootOk() {
        rootOk = true
        Thread {
            // 用 root 静默授予悬浮窗权限
            RootUtil.exec("appops set $packageName SYSTEM_ALERT_WINDOW allow")
            ui.post {
                if (Settings.canDrawOverlays(this)) {
                    startFloat()
                } else {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (rootOk && Settings.canDrawOverlays(this)) startFloat()
    }

    private fun startFloat() {
        startForegroundService(Intent(this, FloatService::class.java))
        finish()
    }
}
