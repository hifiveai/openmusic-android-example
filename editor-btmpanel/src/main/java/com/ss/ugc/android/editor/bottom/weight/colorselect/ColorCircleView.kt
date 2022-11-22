package com.ss.ugc.android.editor.bottom.weight.colorselect

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt

/**
 * 圆形色块View
 */
class ColorCircleView : View {

    private var fillColor = Color.WHITE
    private lateinit var fillPaint: Paint

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        fillPaint = Paint()

        fillPaint.color = fillColor
        fillPaint.isAntiAlias = true
        fillPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        val width = width
        val radius = width / 2
        canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), fillPaint)
    }

    fun setColor(@ColorInt color: Int) {
        fillColor = color
        fillPaint.color = fillColor
    }

    fun getColor() = fillColor
}