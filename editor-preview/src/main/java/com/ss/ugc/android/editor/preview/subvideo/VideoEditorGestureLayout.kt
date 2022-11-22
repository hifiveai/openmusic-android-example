package com.ss.ugc.android.editor.preview.subvideo

import android.content.Context
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.ss.ugc.android.editor.preview.gesture.MoveGestureDetector
import com.ss.ugc.android.editor.preview.gesture.OnGestureListener
import com.ss.ugc.android.editor.preview.gesture.RotateGestureDetector
import com.ss.ugc.android.editor.preview.gesture.ScaleGestureDetector

/**
 * 处理单击, 双击, 手指缩放, 上下左右滑动事件, 抛出回调.
 */
open class VideoEditorGestureLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {
    /**
     * 给外部的统一回调
     */
    protected var onGestureListener: OnGestureListener? = null
        private set

    /**
     * 内部监听
     */
    private lateinit var mGestureDetector: GestureDetectorCompat
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private lateinit var mRotationGestureDetector: RotateGestureDetector
    private lateinit var mMoveGestureDetector: MoveGestureDetector

    private var mScaleFactor = 1f
    private var mDoubleTapSlopSquare: Float = 0.toFloat()
    private var mFirstUpX: Float = 0.toFloat()
    private var mFirstUpY: Float = 0.toFloat()
    private var isTwoPoint = false
    var enableEdit: Boolean = false

    var onTouch = false
    var isInFullScreen = false
    var isInTemplateSettingView = false // 是否在模版设置页面


    val view by lazy {
        this
    }
    private val mSimpleOnRotateGestureListener =
            object : RotateGestureDetector.SimpleOnRotateGestureListener() {
                override fun onRotate(detector: RotateGestureDetector): Boolean {
                    val curRotate = detector.rotationDegreesDelta
                    if (onGestureListener != null) {
                        onGestureListener!!.onRotation(view,curRotate)
                    }
                    return true
                }

                override fun onRotateEnd(detector: RotateGestureDetector) {
                    val curRotate = detector.rotationDegreesDelta
                    if (onGestureListener != null) {
                        onGestureListener!!.onRotationEnd(view,curRotate)
                    }
                }

                override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
                    if (onGestureListener != null) {
                        onGestureListener!!.onRotationBegin(view,detector)
                    }
                    return true
                }
            }

    private val mSimpleOnMoveGestureListener = object : MoveGestureDetector.OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            if (onGestureListener != null) {
                onGestureListener!!.onMove(view,detector)
            }
            return true
        }

        override fun onMoveBegin(
                detector: MoveGestureDetector,
                downX: Float,
                downY: Float
        ): Boolean {
            if (onGestureListener != null) {
                onGestureListener!!.onMoveBegin(view,detector, downX, downY)
            }
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            if (onGestureListener != null) {
                onGestureListener!!.onMoveEnd(view,detector)
            }
        }
    }

    init {
        initLayout(context)
    }

    private fun initLayout(context: Context) {
        mRotationGestureDetector =
                RotateGestureDetector(context, mSimpleOnRotateGestureListener)
        mMoveGestureDetector =
                MoveGestureDetector(context, mSimpleOnMoveGestureListener)
        mGestureDetector = GestureDetectorCompat(context, this)
        mGestureDetector.setOnDoubleTapListener(this)
        mScaleGestureDetector = ScaleGestureDetector(this)
        val doubleTapSlop = ViewConfiguration.get(getContext()).scaledDoubleTapSlop.toFloat()
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop
        setOnTouchListener(this)
    }

    /***
     * 需要处理两个手指的时候不触发一个手指的所有事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enableEdit || isInFullScreen || isInTemplateSettingView) return false
        // 两个手指的事件永远传递
        mScaleGestureDetector.onTouchEvent(this, event)
        mRotationGestureDetector.onTouchEvent(event)

        val action = event.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_DOWN) {
            isTwoPoint = false
        }
        if (event.pointerCount > 1) {
            isTwoPoint = true
        }
        mMoveGestureDetector.onTouchEvent(event)

        if (!isTwoPoint) {
            mGestureDetector.onTouchEvent(event)
        }

        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        mFirstUpX = e.x
        mFirstUpY = e.y
        return false
    }

    override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
    ): Boolean {
        if (e1 == null || e2 == null) {
            return false
        }

        if (onGestureListener != null) {
            onGestureListener!!.onScroll(e1, e2, distanceX, distanceY)
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        onGestureListener?.onLongPress(e)
    }

    override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        return onGestureListener != null && onGestureListener!!.onFling(
                e1,
                e2,
                velocityX,
                velocityY
        )
    }

    open fun setOnGestureListener(onGestureListener: OnGestureListener?) {
        this.onGestureListener = onGestureListener
    }

    fun getCurrVideoGestureListener(): OnGestureListener? = onGestureListener

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return onGestureListener != null && onGestureListener!!.onSingleTapConfirmed(view,e)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP) {
            val width = measuredWidth
            val height = measuredHeight
            val widthEdge = width / 10
            val heightEdge = height / 10
            if (onGestureListener == null) {
                return false
            }

            if (!isConsideredDoubleTap2(e)) {
                return false
            }
            return if (isConsideredEdge(e, width, height, widthEdge, heightEdge)) {
                false
            } else onGestureListener!!.onDoubleClick(view,e)
        }
        return false
    }

    private fun isConsideredEdge(
            e: MotionEvent,
            width: Int,
            height: Int,
            widthEdge: Int,
            heightEdge: Int
    ): Boolean {
        // 边缘不响应双击事件, 防止拍摄过程中由于拇指误触屏幕边缘导致切换前后摄像头.
        return e.x < widthEdge || width - e.x < widthEdge ||
                e.y < heightEdge || height - e.y < heightEdge
    }

    // Similar to GestureDetector.isConsideredDoubleTap(),
    // we should guarantee that the distance between first up and second up is within touch slop.
    private fun isConsideredDoubleTap2(secondUp: MotionEvent): Boolean {
        val deltaX = secondUp.x - mFirstUpX
        val deltaY = secondUp.y - mFirstUpY
        return deltaX * deltaX + deltaY * deltaY < mDoubleTapSlopSquare
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (onGestureListener == null) {
            return false
        }
        val action = motionEvent.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                onGestureListener!!.onDown(this,motionEvent)
            }
            MotionEvent.ACTION_POINTER_DOWN -> onGestureListener!!.onPointerDown()
            MotionEvent.ACTION_POINTER_UP -> onGestureListener!!.onPointerUp()
            MotionEvent.ACTION_UP -> {
                onGestureListener!!.onUp(this,motionEvent)
            }
            MotionEvent.ACTION_CANCEL -> {
                // mOnGestureListener!!.onUp(motionEvent)
            }
        }
        return false
    }

    override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
        if (onGestureListener != null) {
            val needContinue = onGestureListener!!.onScale(this,detector)
            if (needContinue) {
                mScaleFactor = detector.scaleFactor
            }
            return needContinue
        }
        return false
    }

    override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
        return onGestureListener?.onScaleBegin(this,detector) ?: false
    }

    override fun onScaleEnd(view: View, detector: ScaleGestureDetector) {
        onGestureListener?.onScaleEnd(this,mScaleFactor)
    }


}
