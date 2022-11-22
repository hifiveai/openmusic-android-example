package com.vesdk.veeditor.edit.mask.presenter

import android.graphics.Canvas
import com.ss.ugc.android.editor.preview.mask.AbstractMaskPresenter
import com.ss.ugc.android.editor.preview.mask.IMaskGestureCallback
import com.ss.ugc.android.editor.preview.mask.MaskPresenterInfo
import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout

/**
 * none型蒙板，do nothing
 * on 2020-02-11.
 */
class NoneMaskPresenter(
    view: VideoGestureLayout,
    maskGestureCallback: IMaskGestureCallback
) : AbstractMaskPresenter(view, maskGestureCallback) {

    override fun dispatchDraw(canvas: Canvas) {
        //无模板状态下要绘制选择矩形
        maskPresenterInfo?.let { info ->
            drawRect(canvas, info.videoWidth, info.videoHeight
                , info.videoCenterX, info.videoCenterY
                , info.videoRotate)
        }
    }

    override fun updateMaskInfo(maskPresenterInfo: MaskPresenterInfo?) {
        //无模板状态下要绘制选择矩形 需要maskPresenterInfo数据获取绘制信息
        if (this.maskPresenterInfo == maskPresenterInfo) return
        this.maskPresenterInfo = maskPresenterInfo
        view.invalidate()
    }

    override fun onMoveBegin(downX: Float, downY: Float) {
    }

    override fun onMove(deltaX: Float, deltaY: Float) {
    }

    override fun onScaleBegin() {
    }

    override fun onScale(scaleFactor: Float) {
    }

    override fun onRotateBegin() {
    }

    override fun onRotation(degrees: Float) {
    }
}
