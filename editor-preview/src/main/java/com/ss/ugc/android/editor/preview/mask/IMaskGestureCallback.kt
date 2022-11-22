package com.ss.ugc.android.editor.preview.mask

import android.graphics.PointF

interface IMaskGestureCallback {

    fun onMaskMove(centerPointX: Float, centerPointY: Float, maskCenter: PointF)

//    fun onMaskFeather(feather: Float)

    fun onSizeChange(width: Float, height: Float)

    fun onRotationChange(degrees: Float)

    fun onRoundCornerChange(corner: Float)
}
