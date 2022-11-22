package com.ss.ugc.android.editor.base.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.animation.PathInterpolatorCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.extensions.dp
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.base.utils.UIUtils
import java.util.*


/**
 * time : 2020/12/14
 *
 * description :
 * 变速控件
 */
class ChangeSpeedView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    @Suppress("MagicNumber")
    private val easeOut: Interpolator
        get() = PathInterpolatorCompat.create(0.54F, 0F, 0.94F, 0.74F)

    private val handleBitmap = getSelectBitmap()
    private val handleSrcRect = Rect(0, 0, handleBitmap.width, handleBitmap.height
            ?: 10.dp())
    private val handleDestRect = RectF()

    private val floatingMarginBottom = SizeUtil.dp2px(12.5F)
    private var floatingFadeOutAnim: Animator? = null

    private val shadowColor = 0x40000000

    @ColorInt
    private val lineHintColor = Color.parseColor("#D8D8D8")

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textSplitPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private val textColor = Color.parseColor("#CCFFFFFF")
    private val textSize = SizeUtil.dp2px(12F).toFloat()
    private val textBounds = Rect()

    private var boundSlop = SizeUtil.dp2px(10F)

    private var lineStart: Float = 0F
    private var lineEnd: Float = 0F
    private var lineCenterY: Float = 0F
    private var precision: Float = 1F

    private var currPosition: Int = 10
    private var changePosition: Int = 10
    private var isHighSpeed = true

    private var lineWidth = SizeUtil.dp2px(1F)
    private var handleRadius = (handleBitmap.width + handleBitmap.height - 2) / 4f

    private var isSliding = false
    private var drawFloating = false
    var slideAble: Boolean? = true
    private var slideAbleOnMove = false

    private var dividerSmallHeight = SizeUtil.dp2px(4.6F)
    private var dividerBigHeight = SizeUtil.dp2px(11F)
    private var dividerWidth = SizeUtil.dp2px(1F)
    private var dividerTextMargin = SizeUtil.dp2px(25F)

    private var listener: OnSpeedSliderChangeListener? = null

    init {
        linePaint.style = Paint.Style.FILL
        linePaint.strokeWidth = lineWidth.toFloat()
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.isAntiAlias = true
        linePaint.setShadowLayer(SizeUtil.dp2px(1F).toFloat(), 0F, 0F, shadowColor)

        handlePaint.style = Paint.Style.FILL
        handlePaint.color = Color.WHITE
        handlePaint.isAntiAlias = true
        handlePaint.setShadowLayer(SizeUtil.dp2px(3F).toFloat(), 0F, 0F, shadowColor)

        textPaint.color = textColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize

        textSplitPaint.color = textColor
        textSplitPaint.isAntiAlias = true
        textSplitPaint.textSize = textSize
    }

    fun getSelectBitmap(): Bitmap {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        val themeColor = ContextCompat.getColor(context, ThemeStore.globalUIConfig.themeColorRes)
        gradientDrawable.setColor(ContextCompat.getColor(context, R.color.bg_fragment))
        gradientDrawable.setStroke(7, themeColor)
        val widthPixels = UIUtils.dp2px(context, 15)
        gradientDrawable.setSize(widthPixels, widthPixels)
        val mutableBitmap = Bitmap.createBitmap(widthPixels, widthPixels, ARGB_8888)
        val canvas = Canvas(mutableBitmap)
        gradientDrawable.setBounds(0, 0, widthPixels, widthPixels)
        gradientDrawable.draw(canvas)

        return mutableBitmap
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.UNSPECIFIED -> SizeUtil.getScreenWidth(context)
            else -> 0
        }

        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)

        // 100是最长的情况，计算最大宽度
        val text = "100.0x"
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val desireHeight: Int = ((handleRadius + textBounds.height() +
                floatingMarginBottom) * 2).toInt() + paddingTop + paddingBottom
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> maxHeight
            MeasureSpec.AT_MOST -> Math.min(desireHeight, maxHeight)
            MeasureSpec.UNSPECIFIED -> desireHeight
            else -> desireHeight
        }

        setMeasuredDimension(width, height)

        val decorateSize: Float = Math.max(handleRadius, (textBounds.right - textBounds.left) / 2F)
        lineStart = paddingLeft + decorateSize
        lineEnd = measuredWidth - paddingRight - decorateSize
        lineCenterY = measuredHeight / 2F
        precision = (lineEnd - lineStart) / (if (isHighSpeed) 500 else 400)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        val handleCenterX: Float = lineStart + precision * currPosition

        linePaint.color = lineHintColor

        val text = String.format(Locale.ENGLISH, "%.1fx", changePosition / 10 / 10f)
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val x = handleCenterX - textBounds.width() / 2F - SizeUtil.dp2px(1F)
        val y = lineCenterY - handleRadius - floatingMarginBottom
        if (drawFloating) canvas.drawText(text, x, y, textPaint)
        if (isHighSpeed) {
            drawHighSpeed(canvas)
        } else {
            drawLowSpeed(canvas, handleCenterX)
        }
        handleDestRect.set(handleCenterX - handleRadius, lineCenterY - handleRadius,
                handleCenterX + handleRadius, lineCenterY + handleRadius)
        canvas.drawBitmap(handleBitmap, handleSrcRect, handleDestRect, null)
    }

    private fun drawLowSpeed(canvas: Canvas?, handleCenterX: Float) {
        // draw divider
        for (i in 0 until 5) {
            var start = lineStart + i * (lineEnd - lineStart) / 4
            val smallStep = (lineEnd - lineStart) / 40
            var lineNum = 10
            if (i == 0) {
                lineNum = 8
                start += 20 * precision
            } else if (i == 4) {
                lineNum = 1
            }
            var curr = start
            var yHeight = dividerBigHeight
            for (j in 0 until lineNum) {
                if (j == 1) yHeight = dividerSmallHeight
                canvas?.drawLine(
                        curr - dividerWidth / 2, lineCenterY - yHeight / 2,
                        curr - dividerWidth / 2, lineCenterY + yHeight / 2, linePaint
                )
                curr += smallStep
            }
            var dividerText = "0.2x"
            if (i != 0) {
                dividerText = i.toString() + "x"
            }
            textSplitPaint.getTextBounds(dividerText, 0, dividerText.length, textBounds)
            val dividerX = start - textBounds.width() / 2F - SizeUtil.dp2px(1F)
            val dividerY = lineCenterY + dividerTextMargin + dividerBigHeight / 2
            canvas?.drawText(dividerText, dividerX, dividerY, textSplitPaint)
        }
        handleDestRect.set(
                handleCenterX - handleRadius, lineCenterY - handleRadius,
                handleCenterX + handleRadius, lineCenterY + handleRadius
        )
        canvas?.drawBitmap(handleBitmap, handleSrcRect, handleDestRect, null)
    }

    private fun drawHighSpeed(canvas: Canvas?) {
        // draw divider
        for (i in 0 until 6) {
            var start = lineStart + i * (lineEnd - lineStart) / 5
            val smallStep = (lineEnd - lineStart) / 50
            var lineNum = 10
            if (i == 0) {
                lineNum = 9
                start += 10 * precision
            } else if (i == 5) {
                lineNum = 1
            }
            var curr = start
            var yHeight = dividerBigHeight
            for (j in 0 until lineNum) {
                if (j == 1) yHeight = dividerSmallHeight
                canvas?.drawLine(
                        curr - dividerWidth / 2, lineCenterY - yHeight / 2,
                        curr - dividerWidth / 2, lineCenterY + yHeight / 2, linePaint
                )
                curr += smallStep
            }
            var dividerText = "0.1x"
            when (i) {
                0 -> dividerText = "0.1x"
                1 -> dividerText = "1x"
                2 -> dividerText = "2x"
                3 -> dividerText = "5x"
                4 -> dividerText = "10x"
                5 -> dividerText = "100x"
            }
            textSplitPaint.getTextBounds(dividerText, 0, dividerText.length, textBounds)
            val dividerX = start - textBounds.width() / 2F - SizeUtil.dp2px(1F)
            val dividerY = lineCenterY + dividerTextMargin + dividerBigHeight / 2
            canvas?.drawText(dividerText, dividerX, dividerY, textSplitPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            ACTION_DOWN -> {
                slideAbleOnMove = false
                textPaint.alpha = 255
                floatingFadeOutAnim?.cancel()
                var position = calcCurrPosition(event.x)
                if (isHighSpeed) {
                    position = absoulteToRelativePosition(position)
                }
                listener?.onDown(position)
                if (touchLine(event.x, event.y)) {
                    if (onChange(event.x)) {
                        isSliding = true
                        drawFloating = true
                    } else {
                        return false
                    }
                }
            }

            ACTION_MOVE -> {
                if (isSliding) {
                    textPaint.alpha = 255
                    onChange(event.x)
                    if (slideAble == true) {
                        // up时可能会丢掉move时可播放状态
                        slideAbleOnMove = true
                    }
                }
            }

            ACTION_UP -> {
                if (isSliding) {
                    isSliding = false
                    if (slideAble == true || slideAbleOnMove) {
                        listener?.onFreeze(changePosition)
                    }
                    val animator = ValueAnimator.ofFloat(1.0F, 0F)
                    animator.addUpdateListener {
                        textPaint.alpha = ((it.animatedValue as Float) * 255).toInt()
                        invalidate()
                    }
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            drawFloating = false
                            textPaint.alpha = 0
                            invalidate()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            textPaint.alpha = 255
                            invalidate()
                        }
                    })
                    animator.interpolator = easeOut
                    animator.duration = 2000
                    animator.start()
                    floatingFadeOutAnim = animator
                }
            }

            ACTION_CANCEL -> {
                if (isSliding) {
                    isSliding = false
                    if (slideAble == true || slideAbleOnMove) {
                        listener?.onFreeze(changePosition)
                    }
                }
                drawFloating = false
            }
        }

        invalidate()
        return true
    }

    private fun onChange(x: Float): Boolean {
        val position = calcCurrPosition(x)
        changePosition = if (isHighSpeed) {
            absoulteToRelativePosition(position)
        } else {
            position
        }
        if (currPosition != position) {
            if (listener != null && listener?.onPreChange(changePosition) == false) {
                slideAble = false
            } else {
                currPosition = position
                listener?.onChange(changePosition)
                slideAble = true
            }
        }
        return slideAble ?: true
    }

    private fun touchLine(x: Float, y: Float): Boolean {
        val centerY = lineCenterY
        return x in lineStart..lineEnd &&
                y in (centerY - handleRadius - boundSlop)..(centerY + handleRadius + boundSlop)
    }

    private fun calcCurrPosition(x: Float): Int {
        val start = if (isHighSpeed) 10 else 20
        return when {
            x in (lineStart + start * precision)..lineEnd -> {
                val delta = x - lineStart
                Math.ceil((delta / precision).toDouble()).toInt()
            }
            x < lineStart + start * precision -> start
            else -> if (isHighSpeed) 500 else 400
        }
    }

    /**
     *  ui 滑杆值转换成输出值
     */
    private fun absoulteToRelativePosition(position: Int): Int {
        return when {
            position in 10..200 -> {
                position
            }
            position in 200..300 -> {
                200 + (position - 200) * 3
            }
            position in 300..400 -> {
                500 + (position - 300) * 5
            }
            position in 400..500 -> {
                1000 + (position - 400) * 90
            }
            position < 10 -> 10
            else -> 100 * 100
        }
    }

    /**
     *  输出值转换成 ui 滑杆值
     */
    private fun relativeToabsoultePosition(position: Int): Int {
        return when {
            position in 10..200 -> {
                position
            }
            position in 200..5 * 100 -> {
                200 + (position - 200) / 3
            }
            position in 5 * 100..10 * 100 -> {
                300 + (position - 500) / 5
            }
            position in 1000..10000 -> {
                400 + (position - 1000) / 90
            }
            position < 10 -> 10
            else -> 500
        }
    }

    fun setOnSliderChangeListener(listener: OnSpeedSliderChangeListener) {
        this.listener = listener
    }

    fun setCurrPosition(position: Int, isHighSpeedMode: Boolean = true) {
        isHighSpeed = isHighSpeedMode
        if (isHighSpeedMode) {
            changePosition = position
            currPosition = relativeToabsoultePosition(changePosition)
            precision = (lineEnd - lineStart) / 500F
        } else {
            changePosition = position
            currPosition = position
            precision = (lineEnd - lineStart) / 400F
        }
        invalidate()
    }
}

interface OnSpeedSliderChangeListener {
    /**
     * ACTION_MOVE时调用
     *
     * @param value 收到ACTION_MOVE事件时的滑杆值
     */
    fun onChange(value: Int)

    /**
     * 抬手或ACTION_CANCEL时调用
     *
     * @param value 滑杆值，这个值跟最近一次onChange回调出去的值应该是一样的
     */
    fun onFreeze(value: Int)

    fun onDown(value: Int)

    /**
     * change之前计算是否可滑动
     *
     * @return 是否可滑动
     *
     */
    fun onPreChange(value: Int): Boolean = true
}
