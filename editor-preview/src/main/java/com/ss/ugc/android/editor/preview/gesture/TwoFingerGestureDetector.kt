package com.ss.ugc.android.editor.preview.gesture

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration

abstract class TwoFingerGestureDetector(context: Context) : BaseGestureDetector(context) {

    private val mEdgeSlop: Float
    private var mRightSlopEdge: Float = 0.toFloat()
    private var mBottomSlopEdge: Float = 0.toFloat()

    protected var mPrevFingerDiffX: Float = 0.toFloat()
    protected var mPrevFingerDiffY: Float = 0.toFloat()
    protected var mCurrFingerDiffX: Float = 0.toFloat()
    protected var mCurrFingerDiffY: Float = 0.toFloat()

    private var mCurrLen: Float = 0.toFloat()
    private var mPrevLen: Float = 0.toFloat()

    /**
     * Return the current distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Distance between pointers in pixels.
     */
    val currentSpan: Float
        get() {
            if (mCurrLen == -1f) {
                val cvx = mCurrFingerDiffX
                val cvy = mCurrFingerDiffY
                mCurrLen = Math.sqrt((cvx * cvx + cvy * cvy).toDouble()).toFloat()
            }
            return mCurrLen
        }

    /**
     * Return the previous distance between the two pointers forming the
     * gesture in progress.
     *
     * @return Previous distance between pointers in pixels.
     */
    val previousSpan: Float
        get() {
            if (mPrevLen == -1f) {
                val pvx = mPrevFingerDiffX
                val pvy = mPrevFingerDiffY
                mPrevLen = Math.sqrt((pvx * pvx + pvy * pvy).toDouble()).toFloat()
            }
            return mPrevLen
        }

    init {

        val config = ViewConfiguration.get(context)
        mEdgeSlop = config.scaledEdgeSlop.toFloat()
    }

    abstract override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent)

    abstract override fun handleInProgressEvent(actionCode: Int, event: MotionEvent)

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = mPrevEvent

        mCurrLen = -1f
        mPrevLen = -1f

        // Previous
        val px0 = prev!!.getX(0)
        val py0 = prev.getY(0)
        val px1: Float
        val py1: Float
        if (prev.pointerCount >= 2) {
            px1 = prev.getX(1)
            py1 = prev.getY(1)
        } else {
            px1 = 0f
            py1 = 0f
        }

        val pvx = px1 - px0
        val pvy = py1 - py0
        mPrevFingerDiffX = pvx
        mPrevFingerDiffY = pvy

        // Current
        val cx0 = curr.getX(0)
        val cy0 = curr.getY(0)
        val cx1: Float
        val cy1: Float
        // 加一个判断，处理java.lang.IllegalArgumentException pointerIndex out of range
        if (curr.pointerCount >= 2) {
            cx1 = curr.getX(1)
            cy1 = curr.getY(1)
        } else {
            cx1 = px1
            cy1 = py1
        }
        val cvx = cx1 - cx0
        val cvy = cy1 - cy0
        mCurrFingerDiffX = cvx
        mCurrFingerDiffY = cvy
    }

    /**
     * Check if we have a sloppy gesture. Sloppy gestures can happen if the edge
     * of the user's hand is touching the screen, for example.
     *
     * @param event
     * @return
     */
    protected open fun isSloppyGesture(event: MotionEvent): Boolean {
        // As orientation can change, query the metrics in touch down
        val metrics = mContext.resources.displayMetrics
        mRightSlopEdge = metrics.widthPixels - mEdgeSlop
        mBottomSlopEdge = metrics.heightPixels - mEdgeSlop

        val edgeSlop = mEdgeSlop
        val rightSlop = mRightSlopEdge
        val bottomSlop = mBottomSlopEdge

        val x0 = event.rawX
        val y0 = event.rawY
        val x1 = getRawX(event, 1)
        val y1 = getRawY(event, 1)

        val p0sloppy = (x0 < edgeSlop || y0 < edgeSlop ||
                x0 > rightSlop || y0 > bottomSlop)
        val p1sloppy = (x1 < edgeSlop || y1 < edgeSlop ||
                x1 > rightSlop || y1 > bottomSlop)

        if (p0sloppy && p1sloppy) {
            return true
        } else if (p0sloppy) {
            return true
        } else if (p1sloppy) {
            return true
        }
        return false
    }

    companion object {

        /**
         * MotionEvent has no getRawX(int) method; simulate it pending future API approval.
         * @param event
         * @param pointerIndex
         * @return
         */
        protected fun getRawX(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.x - event.rawX
            return if (pointerIndex < event.pointerCount) {
                event.getX(pointerIndex) + offset
            } else 0f
        }

        /**
         * MotionEvent has no getRawY(int) method; simulate it pending future API approval.
         * @param event
         * @param pointerIndex
         * @return
         */
        protected fun getRawY(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.y - event.rawY
            return if (pointerIndex < event.pointerCount) {
                event.getY(pointerIndex) + offset
            } else 0f
        }
    }
}
