package com.ss.ugc.android.editor.preview.subvideo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.annotation.ColorInt
import com.ss.ugc.android.editor.base.extensions.dp
import com.ss.ugc.android.editor.base.extensions.safelyPerformHapticFeedback

/**
 * on 2020-01-17.
 */
class VideoFramePainter(
        private val view: VideoEditorGestureLayout
) {
    companion object{
        @JvmStatic
        var FRAME_WIDTH = 2F.dp()
        @JvmStatic
        var FRAME_CORNER = 1F.dp()
        @JvmStatic
        var ADSORPTION_LINE_LENGTH = 40F.dp()
        @JvmStatic
        var ADSORPTION_LINE_WIDTH = 1.5F.dp()
        @JvmStatic
        var ADSORPTION_LINE_COLOR = Color.parseColor("#00E5F6")
        @JvmStatic
        var FRAME_GRAY_COLOR = Color.parseColor("#626262")
        @JvmStatic
        var FRAME_COLOR = Color.parseColor("#99EC3A5C")
        @JvmStatic
        var FRAME_WIDTH_ERROR_DECREASE = 1F.dp()
    }



    private var paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = FRAME_WIDTH.toFloat()
        color = FRAME_COLOR
        style = Paint.Style.STROKE
    }
    private var adsorptionLinePaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = ADSORPTION_LINE_WIDTH
        color = ADSORPTION_LINE_COLOR
    }

    private var transState: TransAdsorptionState =
            TransAdsorptionState.NONE
    private var rotationState: RotationAdsorptionState =
            RotationAdsorptionState.NONE
    private var frameInfo: FrameInfo? = null

    fun updateTransAdsorptionState(state: TransAdsorptionState, performFeedback: Boolean = true) {
        if (transState != state) {
            // 如果之前是没有吸附的，但是现在吸附了，就抖动一下
            if (performFeedback && transState.value <= state.value) {
                view.safelyPerformHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
            transState = state
            view.invalidate()
        }
    }

    fun updateRotationAdsorptionState(
            state: RotationAdsorptionState,
            degree: Int,
            performFeedback: Boolean = true
    ) {
        if (rotationState != state) {
            if (performFeedback && rotationState.value < state.value) {
                view.safelyPerformHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
            rotationState = state
        } else if (state == RotationAdsorptionState.NONE) { // 非吸附状态下，进入90度（etc）也
            if (performFeedback && degree % 90 == 0) {
                view.safelyPerformHapticFeedback(
                        HapticFeedbackConstants.LONG_PRESS,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
        }
    }
    // 画矩形框的方法 提供 FrameInfo
    fun updateFrameInfo(info: FrameInfo?) {
        if (frameInfo == info) return
        frameInfo = info
        view.invalidate()
    }

    fun dispatchDraw(canvas: Canvas?) {
        canvas ?: return

        drawAdsorptionLine(canvas)
        frameInfo?.let { (width, height, centerX, centerY, rotate) ->
            drawFrame(canvas, width, height, centerX, centerY, rotate)
        }
    }

    private fun drawAdsorptionLine(canvas: Canvas) {
        if (transState == TransAdsorptionState.ALL || transState == TransAdsorptionState.X) {
            val x = view.measuredWidth / 2F
            canvas.drawLine(x, 0F, x,
                    ADSORPTION_LINE_LENGTH, adsorptionLinePaint)
            val endY = view.measuredHeight.toFloat()
            canvas.drawLine(x, endY - ADSORPTION_LINE_LENGTH, x, endY, adsorptionLinePaint)
        }
        if (transState == TransAdsorptionState.ALL || transState == TransAdsorptionState.Y) {
            val y = view.measuredHeight / 2F
            canvas.drawLine(0F, y,
                    ADSORPTION_LINE_LENGTH, y, adsorptionLinePaint)
            val endX = view.measuredWidth.toFloat()
            canvas.drawLine(endX - ADSORPTION_LINE_LENGTH, y, endX, y, adsorptionLinePaint)
        }
    }

    private fun drawFrame(
            canvas: Canvas,
            width: Float,
            height: Float,
            centerX: Float,
            centerY: Float,
            rotate: Int
    ) {
        canvas.save()
        canvas.rotate(rotate.toFloat(), centerX, centerY)
        Log.e("dfdfgfg--RectColor",paint.color.toString())
        canvas.drawRoundRect(
                centerX - width / 2F - FRAME_WIDTH_ERROR_DECREASE,
                centerY - height / 2F - FRAME_WIDTH_ERROR_DECREASE,
                centerX + width / 2F + FRAME_WIDTH_ERROR_DECREASE,
                centerY + height / 2F + FRAME_WIDTH_ERROR_DECREASE,
                FRAME_CORNER,
                FRAME_CORNER,
                paint
        )
        canvas.restore()
    }

    fun updateRectColor(@ColorInt colorInt: Int) {
        Log.e("dfdfgfg--RectColor",colorInt.toString())
        FRAME_COLOR = colorInt
        paint.color = colorInt
    }

    fun updateRectCorner(corner :Float){
        FRAME_CORNER = corner.dp()
    }

    fun updateRectStrokeWidth(strokeWidth : Float){
        paint.strokeWidth = strokeWidth.dp()
    }

    fun updateAdsorptionLineWidth(width : Float){
        ADSORPTION_LINE_WIDTH = width.dp()
        adsorptionLinePaint.strokeWidth =ADSORPTION_LINE_WIDTH
    }

    fun updateAdsorptionLineLength(length : Float){
        ADSORPTION_LINE_LENGTH = length.dp()
    }

    fun updateAdsorptionLineColor(@ColorInt colorInt: Int){
        Log.e("dfdfgfg--Ads",colorInt.toString())
        ADSORPTION_LINE_COLOR = colorInt
        adsorptionLinePaint.color = colorInt
    }


    data class FrameInfo(
            val width: Float,
            val height: Float,
            val centerX: Float,
            val centerY: Float,
            val rotate: Int
    )

    enum class TransAdsorptionState(val value: Int) {
        NONE(0),
        X(1),
        Y(1),
        ALL(2)
    }

    enum class RotationAdsorptionState(val value: Int) {
        NONE(0),
        ADSORBED(1)
    }
}
