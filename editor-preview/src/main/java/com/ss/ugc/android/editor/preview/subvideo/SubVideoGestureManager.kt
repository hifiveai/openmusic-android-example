package com.ss.ugc.android.editor.preview.subvideo

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import com.ss.ugc.android.editor.base.theme.VideoEditViewConfig
import com.ss.ugc.android.editor.preview.OnViewPrepareListener
import com.ss.ugc.android.editor.preview.PreviewPanel

class SubVideoGestureManager private constructor(private val previewPanel: PreviewPanel){
    private var mConfig : VideoEditViewConfig? =null
    private var onViewPrepareListener :OnViewPrepareListener =object :OnViewPrepareListener{
        override fun onVideoGestureViewPrepare(videoGestureLayout: SubVideoViewHolder) {
            update(videoGestureLayout)
        }
    }
    companion object {
        @Volatile
        private var instance: SubVideoGestureManager? = null
        fun getInstance(previewPanel: PreviewPanel) =
                instance ?: synchronized(this) {
                    instance ?: SubVideoGestureManager(previewPanel).also {
                        instance = it
                        previewPanel.setOnViewPrepareListener(instance!!.onViewPrepareListener)
                    }

                }
    }


    fun setGestureViewConfig(config: VideoEditViewConfig) : SubVideoGestureManager{
        mConfig = config;
        return this
    }
    @SuppressLint("ResourceType")
    private fun update(videoGestureLayout: SubVideoViewHolder){
        mConfig?.apply {
            val context = videoGestureLayout.videoGestureLayout.context
            if (rectColor != 0) {
                videoGestureLayout.videoFramePainter.updateRectColor(ContextCompat.getColor(context, rectColor))
            }
            if (rectCornerRadius > 0) {
                videoGestureLayout.videoFramePainter.updateRectCorner(rectCornerRadius.toFloat())
            }
            if (rectStrokeWidth > 0) {
                videoGestureLayout.videoFramePainter.updateRectStrokeWidth(rectStrokeWidth.toFloat())
            }
            if (adsorptionLineColor != 0) {
                videoGestureLayout.videoFramePainter.updateAdsorptionLineColor(ContextCompat.getColor(context, adsorptionLineColor))
            }
            if (adsorptionLineWidth > 0) {
                videoGestureLayout.videoFramePainter.updateAdsorptionLineWidth(adsorptionLineWidth.toFloat())
            }
            if (adsorptionLineLength > 0) {
                videoGestureLayout.videoFramePainter.updateAdsorptionLineLength(adsorptionLineLength.toFloat())
            }
        }
    }
}