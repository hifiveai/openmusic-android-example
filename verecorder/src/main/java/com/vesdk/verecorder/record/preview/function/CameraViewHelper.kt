package com.vesdk.verecorder.record.preview.function

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.ss.android.ttve.utils.UIUtils
import com.vesdk.vebase.util.dp
import com.vesdk.verecorder.R

/**
 * 处理曝光
 */
class CameraViewHelper(
        private val context: Context,
        private val rootView: FeatureView
) {

    private var isLockFocus: Boolean = false
    private var currentDegree: Int = 0
    private val TAG = "CameraViewHelper"

    private var isFocusing = false
    private var focusIcon: ImageView? = null
    private var exposureBar: DecorateExposureBar? = null
    private var focusAnimationSet: AnimatorSet? = AnimatorSet()
    val width = UIUtils.dip2Px(context, 126F).toInt()
    val margin = UIUtils.dip2Px(context, 86F).toInt()
    private val barWidth = UIUtils.dip2Px(context, 80F).toInt()
    private val barHeight = UIUtils.dip2Px(context, 126F).toInt()

    private var exposureBarAlignLeft = false // 在竖屏状态下是否展示在左边，默认展示在右边（注意：这里上下左右都是相对于正常竖屏而言的）
    private var exposureBarAlignTop = false // 在横屏状态下是否展示在上面，默认展示在下边（注意：这里上下左右都是相对于正常竖屏而言的）
    var hasChangedExposure = false

    private var exposureListener: DecorateExposureBar.OnLevelChangeListener? = null

    @Synchronized
    fun showFocusIcon(eventX: Int, eventY: Int, lockFocus: Boolean = false) {
        isLockFocus = lockFocus
        hasChangedExposure = false
        if (focusIcon == null) {
            focusIcon = ImageView(context).apply {
                setImageResource(R.drawable.focus_icon)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            exposureBar = DecorateExposureBar(context)

            val barParams = FrameLayout.LayoutParams(barWidth, barHeight)
            val layout = FrameLayout.LayoutParams(width, width)
            adjustLayoutParams(layout, barParams, eventX, margin, eventY)
            focusIcon?.layoutParams = layout
            exposureBar?.layoutParams = barParams
            if (currentDegree != 0) {
                // not 0 degree need change ,post 10
                updateDegree(currentDegree, true)
            }
            rootView.addView(focusIcon)
            rootView.addView(exposureBar)

            exposureBar?.setOnLevelChangeListener(object :
                    DecorateExposureBar.OnLevelChangeListener {
                override fun onChanged(level: Int) {
                    Log.d(TAG, "onChanged $level")
                    exposureBar?.enableDrawBar(true)
                    exposureListener?.onChanged(level)
                }

                override fun onFirstChange() {
                    Log.d(TAG, "onFirstChange")
                    focusAnimationSet?.removeAllListeners()
                    focusAnimationSet?.cancel()
                    hasChangedExposure = true
                    resetUI()
                    exposureListener?.onFirstChange()
                }

                override fun changeFinish(index: Int) {
                    Log.d(TAG, "changeFinish ")
                    startFocusAnimationAfterExposeChangeFinish()
                    val fpsMap = mutableMapOf<String, String>()
                    fpsMap["exposure"] = (100 - index).toString()
                    exposureListener?.changeFinish(index)
                }
            })
        } else {
            // 已经添加过
            val layoutParams = focusIcon!!.layoutParams as FrameLayout.LayoutParams
            val barLayoutParams = exposureBar!!.layoutParams as FrameLayout.LayoutParams
            adjustLayoutParams(layoutParams, barLayoutParams, eventX, margin, eventY)
            focusIcon!!.layoutParams = layoutParams
            exposureBar!!.layoutParams = barLayoutParams
            updateDegree(currentDegree, true)
        }
        if (!lockFocus) {
            resetUI()
            startFocusAnimation()
        } else {
            startFocusLockAnimation()
        }
    }

    private fun resetUI() {
        focusIcon?.alpha = 1.0f
        focusIcon?.visibility = View.VISIBLE
        exposureBar?.alpha = 1.0f
        exposureBar?.visibility = View.VISIBLE
    }

    private fun startFocusAnimationAfterExposeChangeFinish() {
        // from 1f to 1f only to keep time
        if (focusAnimationSet != null) {
//            focusAnimationSet?.end()
            focusAnimationSet?.removeAllListeners()
            focusAnimationSet?.cancel()
        }
        val alphaSet = prepareAlphaAnimation(1f, 1f, 1800)
        val changeAlpha = prepareAlphaAnimation(1f, 0.3f, 200)
        val keepAlpha = prepareAlphaAnimation(0.3f, 0.3f, 1800)
        keepAlpha.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                exposureBar?.enableDrawBar(false)
            }
        })
        focusAnimationSet = AnimatorSet()
        focusAnimationSet?.playSequentially(alphaSet, changeAlpha, keepAlpha)
        focusAnimationSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isLockFocus) {
                    focusIcon?.visibility = View.INVISIBLE
                    exposureBar?.visibility = View.INVISIBLE
                }
            }
        })
        focusAnimationSet?.start()
    }

    private fun startFocusLockAnimation() {
        val alpha = ObjectAnimator.ofFloat(focusIcon, View.ALPHA, 1f, 0.3f, 1.0f, 0.3f, 1.0f)
        alpha.duration = 500
        alpha.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                exposureBar?.visibility = View.VISIBLE
            }
        })
        val animatorX =
                ObjectAnimator.ofFloat(focusIcon, View.SCALE_X, 1F, 1.5f, 1f, 1.5f, 0.67f)
        val animatorY =
                ObjectAnimator.ofFloat(focusIcon, View.SCALE_Y, 1F, 1.5f, 1f, 1.5f, 0.67f)
        val scaleAnimator = AnimatorSet()
        scaleAnimator.playTogether(animatorX, animatorY)
        scaleAnimator.duration = 800

        val alphaSet = prepareAlphaAnimation(1f, 1f, 1800)
        val changeAlpha = prepareAlphaAnimation(1f, 0.3f, 200)
        val keepAlpha = prepareAlphaAnimation(0.3f, 0.3f, 1800)
        keepAlpha.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                exposureBar?.enableDrawBar(false)
            }
        })
        if (focusAnimationSet?.isRunning == true) {
            focusAnimationSet?.removeAllListeners()
            focusAnimationSet?.cancel()
        }
        focusAnimationSet = AnimatorSet()
        focusAnimationSet?.playSequentially(scaleAnimator, alpha, alphaSet, changeAlpha, keepAlpha)
        focusIcon?.alpha = 1.0f
        focusIcon?.visibility = View.VISIBLE
        exposureBar?.alpha = 1.0f
        exposureBar?.visibility = View.INVISIBLE
        focusAnimationSet?.start()
    }

    private fun startFocusAnimation() {
        val animatorX = ObjectAnimator.ofFloat(focusIcon, View.SCALE_X, 1F, 0.67f)
        val animatorY = ObjectAnimator.ofFloat(focusIcon, View.SCALE_Y, 1F, 0.67f)
        // from 1f to 1f only to keep time
        val alphaSet = prepareAlphaAnimation(1f, 1f, 1800)
        val scaleAnimator = AnimatorSet()
        scaleAnimator.playTogether(animatorX, animatorY)
        scaleAnimator.duration = 200

        val changeAlpha = prepareAlphaAnimation(1f, 0.3f, 200)
        val keepAlpha = prepareAlphaAnimation(0.3f, 0.3f, 1800)
        if (focusAnimationSet?.isRunning == true) {
            focusAnimationSet?.removeAllListeners()
            focusAnimationSet?.cancel()
        }
        focusAnimationSet = AnimatorSet()
        focusAnimationSet?.playSequentially(scaleAnimator, alphaSet, changeAlpha, keepAlpha)
        focusAnimationSet?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                focusIcon?.visibility = View.INVISIBLE
                exposureBar?.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                focusIcon?.alpha = 1f
                exposureBar?.alpha = 1f
                focusIcon?.visibility = View.VISIBLE
                exposureBar?.visibility = View.VISIBLE
            }
        })
        focusAnimationSet?.start()
    }

    private fun prepareAlphaAnimation(start: Float, end: Float, duration: Long): AnimatorSet {
        val alpha = ObjectAnimator.ofFloat(focusIcon, View.ALPHA, start, end)
        val alphaBar = ObjectAnimator.ofFloat(exposureBar, View.ALPHA, start, end)
        val alphaSet = AnimatorSet()
        alphaSet.playTogether(alpha, alphaBar)
        alphaSet.duration = duration
        return alphaSet
    }

    private fun adjustLayoutParams(
            layout: FrameLayout.LayoutParams,
            barLayout: FrameLayout.LayoutParams,
            eventX: Int,
            width: Int,
            eventY: Int
    ) {
        layout.leftMargin = (eventX - width / 2)
        if (LayoutUtil.isRTL(context)) {
            layout.rightMargin = UIUtils.getScreenWidth(context) - layout.leftMargin - width
        }
        layout.topMargin = eventY - width / 2
        if (layout.leftMargin > UIUtils.getScreenWidth(context) - width - 20.dp()) {
            layout.leftMargin = UIUtils.getScreenWidth(context) - width - 20.dp()
        }
        if (LayoutUtil.isRTL(context)) {
            if (layout.rightMargin > UIUtils.getScreenWidth(context) - width) {
                layout.rightMargin = UIUtils.getScreenWidth(context) - width
            }
        }
        if (layout.leftMargin < (-20).dp()) {
            layout.leftMargin = (-20).dp()
        }
        if (LayoutUtil.isRTL(context)) {
            if (layout.rightMargin < 0) {
                layout.rightMargin = 0
            }
        }
        if (layout.topMargin > UIUtils.getScreenHeight(context) - width) {
            layout.topMargin = UIUtils.getScreenHeight(context) - width
        }
        if (layout.topMargin < 0) {
            layout.topMargin = 0
        }
        // 这里的适配规则是，在竖屏状态，如果对焦框左边位置不够就放在右边，默认右边
        //                在横屏状态，如果对焦框上边位置不够就放在下边，默认下边
        val enoughSpace = 50.dp() // 小太阳要展示，需要足够的空间
        exposureBarAlignLeft =
                (layout.leftMargin + width) > (UIUtils.getScreenWidth(context) - enoughSpace)
        exposureBarAlignTop = (layout.topMargin + width) > (UIUtils.getScreenHeight(context) - enoughSpace)

        barLayout.leftMargin =
                if (exposureBarAlignLeft) (layout.leftMargin - 40.dp()) else (layout.leftMargin + margin)
//        barLayout.rightMargin = layout.rightMargin
        barLayout.topMargin = layout.topMargin
        exposureBar?.resetBarIndex()

        exposureBar?.enableDrawBar(false)
    }

    fun hideFocusIcon() {
        focusIcon?.visibility = View.INVISIBLE
        exposureBar?.visibility = View.INVISIBLE
        exposureBar?.resetBarIndex()
    }

    fun onTouchEvent(event: MotionEvent) {
        if (exposureBar?.visibility != View.VISIBLE) {
            return
        }
        exposureBar?.handleTouchEvent(event)
    }

    private fun updateDegree(degree: Int, forceChange: Boolean = false) {
        Log.d(TAG, "updateDegree $degree")

        if (!forceChange && ((currentDegree == degree) || exposureBar?.visibility == View.INVISIBLE || exposureBar == null)) {
            currentDegree = degree
            return
        }
        currentDegree = degree
        exposureBar?.setOnTouchDegree(degree)
        focusAnimationSet?.removeAllListeners()
        focusAnimationSet?.cancel()
        resetUI()
        when (degree) {
            90, 270 -> {
                exposureBar?.rotation = degree.toFloat()
                exposureBar?.translationX =
                        if (!exposureBarAlignLeft) -(margin + barWidth / 2f - width / 2f) else width / 2f
                exposureBar?.translationY = if (exposureBarAlignTop) -barHeight / 2f else barHeight / 2f
            }
            0, 180 -> {
                exposureBar?.rotation = degree.toFloat()
                exposureBar?.translationX = 0f
                exposureBar?.translationY = 0f
            }
        }

        if (hasChangedExposure) {
            exposureBar?.enableDrawBar(true)
        }
        if (!forceChange) {
            startFocusAnimationAfterExposeChangeFinish()
        }
    }

    fun isShowFocusIcon(): Boolean {
        return (currentDegree != 0 && currentDegree != 180) && exposureBar?.visibility == View.VISIBLE
    }

    fun setExposureListener(exposureListener: SimpleChangeListener) {
        this.exposureListener = exposureListener
    }

    open class SimpleChangeListener : DecorateExposureBar.OnLevelChangeListener {
        override fun onChanged(level: Int) {
        }

        override fun changeFinish(index: Int) {
        }

        override fun onFirstChange() {
        }
    }
}
