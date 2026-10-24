package com.example.floatdemo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class MatrixRainView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    private val chars = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ{}<>/;=+*".toCharArray()
    private val colors = intArrayOf(
        0xFF00FF41.toInt(), // 绿
        0xFF00E5FF.toInt(), // 青
        0xFFFF1744.toInt(), // 红
        0xFFFFD600.toInt()  // 黄
    )
    private var colorIndex = 0
    private val fontSize = 36f   // 字号
    private val trail = 14       // 尾巴长度
    private var heads = IntArray(0)
    private val paint = Paint().apply {
        textSize = fontSize
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }

    fun setColorIndex(i: Int) { colorIndex = i.coerceIn(0, colors.size - 1) }
    fun nextColor() { colorIndex = (colorIndex + 1) % colors.size }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        val cols = (w / fontSize).toInt().coerceAtLeast(1)
        val rows = (h / fontSize).toInt().coerceAtLeast(1)
        heads = IntArray(cols) { -Random.nextInt(rows) }
    }

    override fun onDraw(c: Canvas) {
        c.drawColor(0xCC000000.toInt())  // 背景透明度,CC 改小更透明
        val rows = (height / fontSize).toInt()
        for (i in heads.indices) {
            for (t in 0 until trail) {
                val row = heads[i] - t
                if (row < 0 || row > rows) continue
                paint.color = if (t == 0) Color.WHITE else colors[colorIndex]
                paint.alpha = (255 * (1 - t / trail.toFloat())).toInt()
                c.drawText(chars.random().toString(), i * fontSize, (row + 1) * fontSize, paint)
            }
            heads[i]++
            if (heads[i] - trail > rows && Random.nextFloat() > 0.95f) heads[i] = 0
        }
        postInvalidateDelayed(50)  // 速度,越小越快
    }
}