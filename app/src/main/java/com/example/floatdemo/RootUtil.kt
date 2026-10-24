package com.example.floatdemo

import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtil {
    @Volatile private var granted = false

    // 必须在子线程调用。执行 su -c id,返回 uid=0 才算有 root
    fun hasRoot(): Boolean {
        if (granted) return true
        granted = try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val out = BufferedReader(InputStreamReader(p.inputStream)).readText()
            p.waitFor()
            out.contains("uid=0")
        } catch (e: Exception) { false }
        return granted
    }

    fun exec(cmd: String): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        p.waitFor() == 0
    } catch (e: Exception) { false }
}
