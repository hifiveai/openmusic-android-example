package com.ss.ugc.android.editor.main.cover.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.main.R
import kotlin.math.max
import kotlin.math.min

/**
 * @since 2021-07-09
 */
class CenterMaskView : View {

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        initAttrs(context!!, attrs!!)
        initPaint()
    }

    companion object {
        private const val DEFAULT_MASK_ALPHA = 45 // 0 - 255 
        private const val DEFAULT_LINE_COLOR = 0xFF00FFFF
        private const val DEFAULT_LINE_WIDTH = 1f // dp 
    }

    private var maskPathAll = Path()
    private var boxPath = Path()
    private var screenWidth = 0
    private var screenHeight = 0
    private var boxWidth = 0
    private var boxHeight = 0

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val boxLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mLineColor = DEFAULT_LINE_COLOR.toInt() // 选区线的颜色 
    private var mMaskAlpha =
        DEFAULT_MASK_ALPHA // 0 - 255, 蒙版透明度 
    private var mLineWidth =
        DEFAULT_LINE_WIDTH // 选区线的宽度 

    private var inited = true

    private fun initAttrs(context: Context, attrs: AttributeSet) {
        context.obtainStyledAttributes(attrs, R.styleable.CenterMaskView).apply {
            mMaskAlpha = min(
                max(0, getInt(R.styleable.CenterMaskView_civMaskAlpha,
                    DEFAULT_MASK_ALPHA
                )),
                255
            )
            mLineColor = getColor(
                R.styleable.CenterMaskView_civLineColor,
                DEFAULT_LINE_COLOR.toInt()
            )
            mLineWidth = getDimension(
                R.styleable.CenterMaskView_civLineWidth,
                SizeUtil.px2dp(
                    DEFAULT_LINE_WIDTH.toInt()
                )
            )
            recycle()
        }
    }

    /**
     * 设置蒙版的size
     */
    fun setMaskSize(width: Int, height: Int) {
        inited = false
        boxWidth = width
        boxHeight = height
        invalidate()
    }

    /**
     * 用于选择缩放的因子
     * 1. 选择相对于屏幕的缩放比例
     * 2. 添加padding，根据padding，调整新的缩放比例
     * 3. 计算出所位于的显示位置
     * 4. 将计算出的值设置到 boxWidth、boxHeight
     * 调用时机：在监听到界面初始化完成，view的宽高计算完成之后
     * @return Triple<计算出最终所选择的缩放比例、长、宽>
     */
    fun selectBoxScaleFactor(srcX: Int, srcY: Int): Triple<Float, Float, Float> {
        val sx = (width.toFloat() - SizeUtil.dp2px(
            12f * 2
        )) / srcX.toFloat()
        val sy = (height.toFloat() - SizeUtil.dp2px(
            56f * 2
        )) / srcY.toFloat()
        val scaleFactor = min(sx, sy)
        val distW = srcX * scaleFactor
        val distH = srcY * scaleFactor
        setMaskSize(distW.toInt(), distH.toInt())
        return Triple(distH / srcY.toFloat(), distW, distH)
    }

    private fun initDrawAttrs(canvas: Canvas?) {
        if (inited.not()) {
            canvas?.apply {
                screenWidth = width
                screenHeight = height
            }

            val drawWeight = boxWidth + SizeUtil.dp2px(
                mLineWidth / 2
            )
            val drawHeight = boxHeight + SizeUtil.dp2px(
                mLineWidth / 2
            )

            maskPathAll.apply {
                reset()
                moveTo(0f, 0f)
                lineTo(0f, screenHeight.toFloat())
                lineTo(screenWidth.toFloat(), screenHeight.toFloat())
                lineTo(screenWidth.toFloat(), 0f)

                val wl = ((screenWidth - drawWeight) shr 1).toFloat()
                val wr = wl + drawWeight

                val ht = ((screenHeight - drawHeight) shr 1).toFloat()
                val hb = ht + drawHeight
                fillType = Path.FillType.EVEN_ODD
                moveTo(wl, ht)
                lineTo(wr, ht)
                lineTo(wr, hb)
                lineTo(wl, hb)
                close()
            }

            boxPath.apply {
                val wl = ((screenWidth - drawWeight) shr 1).toFloat() + 3
                val wr = wl + drawWeight - 6

                val ht = ((screenHeight - drawHeight) shr 1).toFloat() + 3
                val hb = ht + drawHeight - 6
                fillType = Path.FillType.EVEN_ODD
                moveTo(wl, ht)
                lineTo(wr, ht)
                lineTo(wr, hb)
                lineTo(wl, hb)
                close()
            }

            inited = true
        }
    }

    private fun initPaint() {
        maskPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = mMaskAlpha
        }

        boxLinePaint.apply {
            color = mLineColor
            strokeWidth = mLineWidth
            style = Paint.Style.STROKE
        }
    }

    override fun onDraw(canvas: Canvas?) {
        initDrawAttrs(canvas)
        if (inited) {
            canvas?.apply {
                drawPath(maskPathAll, maskPaint)
                drawPath(boxPath, boxLinePaint)
            }
        }
    }
}
