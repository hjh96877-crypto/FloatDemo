package com.example.floatdemo

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object AccessUtil {
    enum class State { ROOT, SHIZUKU, NEED_PERMISSION, NONE }

    @Volatile private var mode = State.NONE

    // 必须在子线程调用
    fun check(): State {
        if (rootOk()) { mode = State.ROOT; return mode }
        try {
            if (Shizuku.pingBinder()) {
                mode = when {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> State.SHIZUKU
                    Shizuku.shouldShowRequestPermissionRationale() -> State.NONE
                    else -> State.NEED_PERMISSION
                }
                return mode
            }
        } catch (e: Throwable) { }
        mode = State.NONE
        return mode
    }

    fun hasAccess(): Boolean {
        val s = check()
        return s == State.ROOT || s == State.SHIZUKU
    }

    private fun rootOk(): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val out = BufferedReader(InputStreamReader(p.inputStream)).readText()
        p.waitFor()
        out.contains("uid=0")
    } catch (e: Exception) { false }

    // 用当前可用的权限执行命令
    fun exec(cmd: String): Boolean = try {
        val p: Process = if (mode == State.ROOT) {
            Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        } else {
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            m.isAccessible = true
            m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        }
        p.waitFor() == 0
    } catch (e: Throwable) { false }
}
