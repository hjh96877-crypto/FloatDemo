package com.example.floatdemo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            )
        }
    }

    // 授权后回到这里,自动启动悬浮窗
    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            startForegroundService(Intent(this, FloatService::class.java))
            finish()
        }
    }
}