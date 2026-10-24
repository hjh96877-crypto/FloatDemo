package com.example.floatdemo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {
    private val ui = Handler(Looper.getMainLooper())
    private var ready = false

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, result ->
        if (result == PackageManager.PERMISSION_GRANTED) {
            Thread { AccessUtil.check(); ui.post { onAccessOk() } }.start()
        } else {
            denyAndExit("你拒绝了 Shizuku 授权,应用无法打开。")
        }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Shizuku.addRequestPermissionResultListener(permListener)
        Toast.makeText(this, "正在检测 root / Shizuku…", Toast.LENGTH_SHORT).show()
        Thread {
            val s = AccessUtil.check()
            ui.post {
                when (s) {
                    AccessUtil.State.ROOT, AccessUtil.State.SHIZUKU -> onAccessOk()
                    AccessUtil.State.NEED_PERMISSION -> Shizuku.requestPermission(100)
                    AccessUtil.State.NONE -> denyAndExit(
                        "未检测到 root 权限,也没有可用的 Shizuku 授权。\n" +
                        "请先授予 root,或启动 Shizuku 并允许本应用。")
                }
            }
        }.start()
    }

    private fun denyAndExit(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("无法使用")
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("退出") { _, _ -> finishAndRemoveTask() }
            .show()
    }

    private fun onAccessOk() {
        ready = true
        Thread {
            AccessUtil.exec("appops set $packageName SYSTEM_ALERT_WINDOW allow")
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
        if (ready && Settings.canDrawOverlays(this)) startFloat()
    }

    private fun startFloat() {
        startForegroundService(Intent(this, FloatService::class.java))
        finish()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }
}
