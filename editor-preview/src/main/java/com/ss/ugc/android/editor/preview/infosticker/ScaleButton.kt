package com.ss.ugc.android.editor.preview.infosticker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 *
 * @version 1.0
 * @since 2019/4/15 10:51 PM
 */
// 旋转吸附角度阈值
private const val THRESHOLD_DEGREE = 5

class ScaleButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var listener: OnOptionListener? = null

    private var lastX: Float = 0F
    private var lastY: Float = 0F

    private val rect = Rect()
    private val center = PointF()
    private val first = PointF()
    private val second = PointF()

    private var currentViewRotate = 0F
    private var isAdsorbing = false
    private var adsorbOffset = 0F
    private var angleSum = 0F
    private var totalRotate = 0F

    init {
        isHapticFeedbackEnabled = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent ?: return super.onTouchEvent(event)
        val parentView = parent as ViewGroup
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastY = event.rawY
                    lastX = event.rawX

                    listener?.onScaleRotateBegin()
                    currentViewRotate = parentView.rotation % 360
                    isAdsorbing =
                        (currentViewRotate % (90 - THRESHOLD_DEGREE)) < 2 * THRESHOLD_DEGREE
                    angleSum = 0F
                    totalRotate = currentViewRotate
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    listener?.onScaleRotateEnd()
                }
                MotionEvent.ACTION_MOVE -> {
                    parentView.getGlobalVisibleRect(rect)
                    center.set(rect.centerX().toFloat(), rect.centerY().toFloat())
                    first.set(lastX, lastY)
                    second.set(event.rawX, event.rawY)

                    val scale = center.distance(second) / center.distance(first)
                    val angle = angle(center, first, second)
                    val touchAngle = currentViewRotate + angle + angleSum
                    adsorbOffset = touchAngle % 90
                    val targetAngle = when {
                        abs(adsorbOffset) < THRESHOLD_DEGREE -> {
                            if (isAdsorbing) {
                                angleSum += angle
                            }
                            adsorbFeedback()
                            touchAngle - adsorbOffset
                        }
                        abs(adsorbOffset) > 90 - THRESHOLD_DEGREE -> {
                            if (isAdsorbing) {
                                angleSum += angle
                            }
                            adsorbFeedback()
                            touchAngle + ((if (adsorbOffset < 0) -90 else 90) - adsorbOffset)
                        }
                        else -> {
                            if (isAdsorbing) {
                                isAdsorbing = false
                                angleSum = 0F
                            }
                            touchAngle
                        }
                    }

                    val rotate = targetAngle - currentViewRotate

                    totalRotate += rotate

                    currentViewRotate = targetAngle % 360
                    if (currentViewRotate >= 360000000) {
                        currentViewRotate = 0f
                    }
                    listener?.onScaleRotate(scale, rotate)

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }
        }
        return true
    }

    private fun adsorbFeedback() {
        if (!isAdsorbing) {
            isAdsorbing = true
            angleSum = 0F
        }
    }

    private fun PointF.distance(other: PointF) = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

    fun setOnOptionListener(optionListener: OnOptionListener) {
        listener = optionListener
    }

    private fun angle(center: PointF, first: PointF, second: PointF): Float {
        // 三边的平方
        val oa = center.distance(first)
        val ob = center.distance(second)
        val ab = first.distance(second)

        // 向量叉乘结果, 如果结果为负数， 表示为逆时针， 结果为正数表示顺时针
        val isClockwise =
            (first.x - center.x) * (second.y - center.y) - (first.y - center.y) * (second.x - center.x) > 0

        // 余弦定理
        var cosDegree = (oa * oa + ob * ob - ab * ab) / (2.0 * oa * ob)

        if (cosDegree > 1) {
            cosDegree = 1.0
        } else if (cosDegree < -1) {
            cosDegree = -1.0
        }

        // 计算弧度
        val angle = Math.toDegrees(acos(cosDegree)).toFloat()

        // 计算旋转过的角度，顺时针为正，逆时针为负
        return if (isClockwise) angle else -angle
    }

    interface OnOptionListener {
        fun onScaleRotateBegin()
        fun onScaleRotate(scale: Float, rotate: Float)
        fun onScaleRotateEnd()
    }
}
