package com.ss.ugc.android.editor.base.view.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Date: 2019/1/20
 * 等待加载的View
 */
class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : View(context, attrs, defStyle) {

    var progress: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val rectF: RectF by lazy { RectF(0f, 0f, width.toFloat(), height.toFloat()) }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(rectF, 270f, progress * 360, false, paint)
    }

}