package com.ss.ugc.android.editor.preview.gesture

import android.content.Context
import android.view.MotionEvent

class RotateGestureDetector(context: Context, private val mListener: OnRotateGestureListener) :
    TwoFingerGestureDetector(context) {
    private var mSloppyGesture: Boolean = false
    var focusX: Float = 0.toFloat()
        private set
    var focusY: Float = 0.toFloat()
        private set

    /**
     * Return the rotation difference from the previous rotate event to the current
     * event.
     *
     * @return The current rotation //difference in degrees.
     */
    val rotationDegreesDelta: Float
        get() {
            val diffRadians =
                Math.atan2(mPrevFingerDiffY.toDouble(), mPrevFingerDiffX.toDouble()) - Math.atan2(
                    mCurrFingerDiffY.toDouble(),
                    mCurrFingerDiffX.toDouble()
                )

            return diffRadians.toFloat()
        }

    /**
     * @see SimpleOnRotateGestureListener
     */
    interface OnRotateGestureListener {
        fun onRotate(detector: RotateGestureDetector): Boolean

        fun onRotateBegin(detector: RotateGestureDetector): Boolean

        fun onRotateEnd(detector: RotateGestureDetector)
    }

    open class SimpleOnRotateGestureListener :
            OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            return false
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector) {
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
                    isInProgress = mListener.onRotateBegin(this)
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
                    isInProgress = mListener.onRotateBegin(this)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> if (!mSloppyGesture) {
                return
            }
        }
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)
        calculateFocus(curr)
    }

    private fun calculateFocus(event: MotionEvent) {
        val count = event.pointerCount
        val skipIndex =
            if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP) event.actionIndex else -1
        var sumX = 0f
        var sumY = 0f
        for (i in 0 until count) {
            if (skipIndex == i) continue
            sumX += event.getX(i)
            sumY += event.getY(i)
        }

        focusX = sumX / count
        focusY = sumY / count
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                // Gesture ended but
                updateStateByEvent(event)

                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }

                resetState()
            }

            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)

                // Only accept the event if our relative pressure is within
                // a certain limit. This can help filter shaky data as a
                // finger is lifted.
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    val updatePrevious = mListener.onRotate(this)
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
    }
}
