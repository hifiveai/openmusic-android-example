package com.ss.ugc.android.editor.base.viewmodel.adapter

/**
 * time : 2020/12/28
 *
 * description :
 *
 */
interface IStickerGestureViewModel {
    fun onGestureEnd()

    fun changePosition(x: Float, y: Float)

    fun scale(scaleDiff: Float)

    fun rotate(rotation: Float)

    fun scaleRotate(scaleDiff: Float, rotation: Float)

    fun onScaleRotateEnd()

    fun remove(done: Boolean = true)

    fun copy(
    )

    fun flip(
    )
}