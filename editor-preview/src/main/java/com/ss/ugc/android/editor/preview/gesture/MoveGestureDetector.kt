package com.ss.ugc.android.editor.preview.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent

class MoveGestureDetector(context: Context, private val mListener: OnMoveGestureListener) :
    BaseGestureDetector(context) {

    private var mCurrFocusInternal: PointF? = null
    private var mPrevFocusInternal: PointF? = null
    private val mFocusExternal = PointF()
    var focusDelta = PointF()
        private set
    private val mRawPointF = PointF()

    /**
     * @see SimpleOnMoveGestureListener
     */
    interface OnMoveGestureListener {
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector, downX: Float, downY: Float): Boolean
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    /**
     * Helper class which may be extended and where the methods may be
     * implemented. This way it is not necessary to implement all methods
     * of OnMoveGestureListener.
     */
    class SimpleOnMoveGestureListener :
            OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(
                detector: MoveGestureDetector,
                downX: Float,
                downY: Float
        ): Boolean {
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            // Do nothing, overridden implementation may be used
        }
    }

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent) {
        if (event.pointerCount > 1) return
        when (actionCode) {
            MotionEvent.ACTION_DOWN -> {
                fireMove(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isInProgress) {
                    fireMove(event)
                    return
                }
            }
        }
    }

    private fun fireMove(event: MotionEvent) {
        resetState() // In case we missed an UP/CANCEL event
        mFocusExternal.x = 0f
        mFocusExternal.y = 0f
        mDownEvent = MotionEvent.obtain(event)
        mPrevEvent = MotionEvent.obtain(event)
        timeDelta = 0

        updateStateByEvent(event)
        isInProgress = mListener.onMoveBegin(
                this,
                if (mPrevEvent == null) -1f else mPrevEvent!!.rawX,
                if (mPrevEvent == null) -1f else mPrevEvent!!.rawY
        )
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                resetState()
                isInProgress = false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mListener.onMoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                // updateStateByEvent looks for information within mPrevEvent
                // However, if the gesture started before the detector was attached,
                // ACTION_MOVE happens but mPrevEvent is null, and code will crash
                // @BaseGestureDetector.updateStateByEvent() line 102.
                // This check will simply ignore the current movement if it didn't track it from
                // the start.
                updateStateByEvent(event)

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted.
                val currEvent = mCurrEvent ?: return
                val downEvent = mDownEvent ?: return
                val deltaX = currEvent.x - downEvent.x
                val deltaY = currEvent.y - downEvent.y
                if (deltaX * deltaX + deltaY * deltaY > touchSlop * touchSlop &&
                    mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD
                ) {
                    val updatePrevious = mListener.onMove(this)
                    if (updatePrevious) {
                        mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }

            // 手指1，2依次触摸屏幕，抬起手指1，onMove会发生一次跳到手指2的情况
            // 正确的应该是在手指1抬起后，依然在原地
            MotionEvent.ACTION_POINTER_UP -> {
                mPrevEvent?.recycle()
                mPrevEvent = MotionEvent.obtain(event)
            }
        }
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = mPrevEvent

        // Focus intenal
        mCurrFocusInternal = determineFocalPoint(curr)
        mPrevFocusInternal = determineFocalPoint(prev!!)

        // Focus external
        // - Prevent skipping of focus delta when a finger is added or removed
        val mSkipNextMoveEvent = prev.pointerCount != curr.pointerCount
        focusDelta = if (mSkipNextMoveEvent) FOCUS_DELTA_ZERO else PointF(
            mCurrFocusInternal!!.x - mPrevFocusInternal!!.x,
            mCurrFocusInternal!!.y - mPrevFocusInternal!!.y
        )

        // - Don't directly use mFocusInternal (or skipping will occur). Add
        // 	 unskipped delta values to mFocusExternal instead.
        mFocusExternal.x += focusDelta.x
        mFocusExternal.y += focusDelta.y
        mRawPointF.x = curr.rawX
        mRawPointF.y = curr.rawY
    }

    private fun determineFocalPoint(e: MotionEvent): PointF {
        // Number of fingers on screen
        val pCount = e.pointerCount
        var x = 0f
        var y = 0f

        for (i in 0 until pCount) {
            x += e.getX(i)
            y += e.getY(i)
        }

        return PointF(x / pCount, y / pCount)
    }

    companion object {

        private val FOCUS_DELTA_ZERO = PointF()
    }
}
