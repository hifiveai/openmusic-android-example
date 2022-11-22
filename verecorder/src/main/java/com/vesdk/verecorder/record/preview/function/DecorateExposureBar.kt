package com.vesdk.verecorder.record.preview.function

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.vesdk.vebase.util.dp
import com.vesdk.verecorder.R

class DecorateExposureBar @JvmOverloads constructor(var mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(mContext, attrs, defStyleAttr) {
    var mViewWidth = 0
    var mViewHeight = 0
    var MAX_INDEX = 100
    var MIN_INDEX = 0
    var BAR_WIDTH: Float = 1f.dp()
    var DEFAULT_PICTURE_SIZE: Float = 29f.dp()
    var BAR_PADDING = DEFAULT_PICTURE_SIZE / 2
    var TRIANGLE_PADDING: Float = 19f.dp()
    var TRIANGEL_BTP_HEIGT: Float = 10f.dp()
    val XIFU_PADDING = 10
    var mColorWhite = 0
    var mColorGreen = 0
    var mCenterX = 0
    var mEveryIndexLength = 0f
    var mPaintBar: Paint? = null
    var mPaintCircle: Paint? = null
    var mCurIndex = 0 // 当前位置 = 0
    var mTempIndex = 0 // 开始位移时的位置 = 0
    var mActionDownY = 0f
    var mActionDownX = 0f
    var mIsDoubleFinger = false // 是否是两个手指 = false
    var mTouchAble = true // 是否可拖拽（在执行动画过程中不允许拖拽）
    var mHaveInit = false
    private var mOnLevelChangeListener: OnLevelChangeListener? = null
    private var mVelocityTracker //生命变量
            : VelocityTracker? = null
    var mIsTouched = false

    /**
     * 当用户开始触摸后，监听是否要进入调节
     */
    var mStartDetectChange = false
    var mHasChanged = false
    private var mIsWhite = false
    private val mShadowWidth: Int = 1.5f.dp().toInt()
    private val mShadowColor = -0x67000000
    private val mIndicatorTiranglePath = Path()
    private var enableDrawBar = true
    private var mCurrentRotation = 0
    fun enableDrawBar(enable: Boolean) {
        enableDrawBar = enable
        postInvalidate()
    }

    /**
     * reset bar to index of zero, means 50
     */
    fun resetBarIndex() {
        mCurIndex = DEFAULT_PROGRESS_BAR_VALUE
        postInvalidate()
    }

    /**
     * while in rotation mode, we need convert the progress for formula
     *
     * @param degree 0，90，180，270
     */
    fun setOnTouchDegree(degree: Int) {
        mCurrentRotation = degree
    }

    interface OnLevelChangeListener {
        fun onChanged(level: Int)
        fun changeFinish(index: Int)
        fun onFirstChange()
    }

    fun setUpParams() {
//        mCenterX = mViewWidth / 2 + SizeUtils.dp2px(28);
        mCenterX = mViewWidth / 2
        mEveryIndexLength = (mViewHeight - BAR_PADDING * 2) / MAX_INDEX.toFloat()
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        mColorWhite = ContextCompat.getColor(mContext, R.color.white)
        mColorGreen = ContextCompat.getColor(mContext, R.color.app_green)
        mPaintBar = Paint()
        mPaintBar!!.style = Paint.Style.FILL
        mPaintBar!!.strokeWidth = BAR_WIDTH
        mPaintBar!!.setShadowLayer(mShadowWidth.toFloat(), 0f, 0f, mShadowColor)
        mPaintBar!!.isAntiAlias = true
        mPaintCircle = Paint()
        mPaintCircle!!.isAntiAlias = true
        mPaintCircle!!.setShadowLayer(mShadowWidth.toFloat(), 0f, 0f, mShadowColor)
        mCurIndex = 50
        mHaveInit = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!mHaveInit) {
            return
        }
        mPaintBar!!.color = mColorWhite
        if (mCurIndex == DEFAULT_PROGRESS_BAR_VALUE && mIsWhite) {
            mPaintBar!!.color = mColorWhite
            mPaintBar!!.setShadowLayer(mShadowWidth.toFloat(), 0f, 0f, mShadowColor)
        }
        val circleCenter = mCurIndex * mEveryIndexLength
        if (enableDrawBar) {
            // 画滑动栏上半身
            if (circleCenter >= BAR_PADDING) {
                canvas.drawLine(mCenterX.toFloat(), BAR_PADDING,
                        mCenterX.toFloat(), circleCenter, mPaintBar!!)
            }
            // 画滑动栏的下半身
            if (mViewHeight - BAR_PADDING >= BAR_PADDING + circleCenter + DEFAULT_PICTURE_SIZE / 2) {
                canvas.drawLine(mCenterX.toFloat(), BAR_PADDING + circleCenter + DEFAULT_PICTURE_SIZE / 2,
                        mCenterX.toFloat(), mViewHeight - BAR_PADDING, mPaintBar!!)
            }
        }

        // 画可拖拽的太阳
        val circleCenterY = circleCenter + DEFAULT_PICTURE_SIZE / 2
        canvas.drawCircle(mCenterX.toFloat(), circleCenterY, SUN_CIRCLE_RADIUS.toFloat(), mPaintBar!!)
        for (i in 0..7) {
            canvas.save()
            canvas.rotate(360f / 8 * i, mCenterX.toFloat(), circleCenterY)
            canvas.drawLine(mCenterX.toFloat(), circleCenterY + SUN_CIRCLE_RADIUS + SUN_LINE_LENGTH,
                    mCenterX.toFloat(), circleCenterY + SUN_CIRCLE_RADIUS + SUN_LINE_LENGTH + SUN_LINE_LENGTH,
                    mPaintBar!!)
            canvas.restore()
        }
        canvas.drawPath(mIndicatorTiranglePath, mPaintBar!!)
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return true
        }
        if (!mTouchAble) {
            return true
        }

//        Log.d(TAG, "touch " + event.getAction() + "//" + event.getX() + "// " + event.getY());
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain() //获得VelocityTracker类实例
        }
        mVelocityTracker!!.addMovement(event)
        mVelocityTracker!!.computeCurrentVelocity(1000)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "ACTION_DOWN")
                mTempIndex = mCurIndex
                mIsDoubleFinger = false
                mActionDownY = event.y
                mActionDownX = event.x
                mIsTouched = true
                mStartDetectChange = true
                mHasChanged = false
                mIsWhite = false
            }
            MotionEvent.ACTION_POINTER_DOWN, 261 -> {
                // 开始二指了
                Log.d(TAG, "ACTION_POINTER_DOWN")
                mIsDoubleFinger = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsDoubleFinger) {
                    //双指不响应亮度调节
                    return true
                }
                val y = event.y
                val x = event.x
                val diffX = x - mActionDownX
                val diffY = y - mActionDownY
                //                Log.d(TAG, "diff X is " + diffX);

//                Log.d(TAG, "trackSpeed " + mVelocityTracker.getXVelocity() + "// " + mVelocityTracker.getYVelocity());
                // X 方向位移大于y的两倍，而且速度也更快，那么认为是滤镜
                if (mCurrentRotation == 0 && Math.abs(diffX) > 0.9 * Math.abs(diffY) && Math.abs(mVelocityTracker!!.xVelocity) > 0.9 * Math.abs(mVelocityTracker!!.yVelocity)) {
//                     is slide
                    return true
                }
                var diffIndex = y - mActionDownY
                if (mCurrentRotation == 90) {
                    diffIndex = mActionDownX - x
                } else if (mCurrentRotation == 270) {
                    diffIndex = x - mActionDownX
                } else if (mCurrentRotation == 180) {
                    diffIndex = mActionDownY - y
                }
                var index = getFitIndex(mTempIndex + (diffIndex / mEveryIndexLength).toInt())
                if (index <= 50 + XIFU_PADDING && index >= 50 - XIFU_PADDING) {
                    index = 50
                }
                if (mCurIndex != index) {
                    mCurIndex = index
                    if (null != mOnLevelChangeListener) {
                        if (mStartDetectChange) {
                            mOnLevelChangeListener!!.onFirstChange()
                            mStartDetectChange = false
                            mHasChanged = true
                        }
                        mOnLevelChangeListener!!.onChanged(mCurIndex)
                        finalValue = mCurIndex
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // 按下位置不在可拖拽的圆范围，且抬手位置和落手位置相差不超过3dp,则当做点击处理
                val actionUpY = event.y
                mIsTouched = false
                if (mHasChanged && null != mOnLevelChangeListener) {
                    // has changed
                    mOnLevelChangeListener!!.changeFinish(mCurIndex)
                }
                invalidate()
            }
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mViewHeight == 0 && mViewWidth == 0) {
            mViewWidth = measuredWidth
            mViewHeight = measuredHeight
            setUpParams()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
        }
    }

    fun startClickAnim(startIndex: Int, endIndex: Int) {
        mTouchAble = false
        val animator = ValueAnimator.ofFloat(0f, 1.0f)
        animator.setTarget(this)
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            mCurIndex = getFitIndex((startIndex + (endIndex - startIndex) * value).toInt())
            finalValue = mCurIndex
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mTouchAble = true
                super.onAnimationEnd(animation)
            }
        })
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    fun getFitIndex(index: Int): Int {
        if (index > MAX_INDEX) {
            return MAX_INDEX
        }
        return if (index < MIN_INDEX) {
            MIN_INDEX
        } else index
    }

    fun setFaceModelLevel(level: Int) {
        mCurIndex = level
        finalValue = level
        startClickAnim(mCurIndex, mCurIndex)
    }

    fun setOnLevelChangeListener(onLevelChangeListener: OnLevelChangeListener?) {
        mOnLevelChangeListener = onLevelChangeListener
    }

    fun setIsWhite(isWhite: Boolean) {
        mIsWhite = isWhite
    }

    override fun buildDrawingCache(autoScale: Boolean) {
        super.buildDrawingCache(autoScale)
    }

    override fun buildDrawingCache() {
        super.buildDrawingCache()
    }

    companion object {
        private const val TAG = "DecorateExposureBar"
        const val MIN_EXPOSURE = -1.325f // -1.325
        const val MAX_EXPOSURE = 0.85f // 0.85
        private val SUN_LINE_LENGTH: Int = 3.dp()
        private val SUN_CIRCLE_RADIUS: Int = 4.dp()
        const val DEFAULT_PROGRESS_BAR_VALUE = 50
        var finalValue = DEFAULT_PROGRESS_BAR_VALUE //最终输出给外部的位置
            private set

        fun changeValue(level: Int): Float {
            var level = level
            level = 100 - level
            return if (level > 50) {
                val avg = MAX_EXPOSURE / 50.0f
                (level - 50) * avg
            } else {
                val avg = MIN_EXPOSURE / 50.0f
                (50 - level) * avg
            }
        }
    }

}