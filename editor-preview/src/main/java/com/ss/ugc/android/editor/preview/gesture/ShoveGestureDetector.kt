package com.ss.ugc.android.editor.preview.gesture

import android.content.Context
import android.view.MotionEvent

class ShoveGestureDetector(context: Context, private val mListener: OnShoveGestureListener) :
    TwoFingerGestureDetector(context) {

    private var mPrevAverageY: Float = 0.toFloat()
    private var mCurrAverageY: Float = 0.toFloat()
    private var mSloppyGesture: Boolean = false

    /**
     * Return the distance in pixels from the previous shove event to the current
     * event.
     *
     * @return The current distance in pixels.
     */
    val shovePixelsDelta: Float
        get() = mCurrAverageY - mPrevAverageY

    /**
     * @see SimpleOnShoveGestureListener
     */
    interface OnShoveGestureListener {
        fun onShove(detector: ShoveGestureDetector): Boolean
        fun onShoveBegin(detector: ShoveGestureDetector): Boolean
        fun onShoveEnd(detector: ShoveGestureDetector)
    }

    class SimpleOnShoveGestureListener :
            OnShoveGestureListener {
        override fun onShove(detector: ShoveGestureDetector): Boolean {
            return false
        }

        override fun onShoveBegin(detector: ShoveGestureDetector): Boolean {
            return true
        }

        override fun onShoveEnd(detector: ShoveGestureDetector) {
            // Do nothing, overridden implementation may be used
        }
    }

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                // At least the second finger is on screen now

                resetState() // In case we missed an UP/CANCEL event
                mPrevEvent = MotionEvent.obtain(event)
                timeDelta = 0

                updateStateByEvent(event)

                // See if we have a sloppy gesture
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    // No, start gesture now
                    isInProgress = mListener.onShoveBegin(this)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mSloppyGesture) {
                    return
                }

                // See if we still have a sloppy gesture
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    // No, start normal gesture now
                    isInProgress = mListener.onShoveBegin(this)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> if (!mSloppyGesture) {
                return
            }
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                // Gesture ended but
                updateStateByEvent(event)

                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted. Also check that shove is meaningful.
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD && Math.abs(
                        shovePixelsDelta
                    ) > 0.5f
                ) {
                    val updatePrevious = mListener.onShove(this)
                    if (updatePrevious) {
                        mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
        mPrevAverageY = 0.0f
        mCurrAverageY = 0.0f
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = mPrevEvent
        val py0 = prev!!.getY(0)
        val py1 = prev.getY(1)
        mPrevAverageY = (py0 + py1) / 2.0f

        val cy0 = curr.getY(0)
        val cy1 = curr.getY(1)
        mCurrAverageY = (cy0 + cy1) / 2.0f
    }

    override fun isSloppyGesture(event: MotionEvent): Boolean {
        val sloppy = super.isSloppyGesture(event)
        if (sloppy)
            return true

        // If it's not traditionally sloppy, we check if the angle between fingers
        // is acceptable.
        val angle = Math.abs(Math.atan2(mCurrFingerDiffY.toDouble(), mCurrFingerDiffX.toDouble()))
        // about 20 degrees, left or right
        return !(0.0f < angle && angle < 0.35f || 2.79f < angle && angle < Math.PI)
    }
}
