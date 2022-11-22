package com.ss.ugc.android.editor.preview.infosticker

import androidx.core.content.ContextCompat
import com.ss.ugc.android.editor.base.theme.StickerEditViewConfig
import com.ss.ugc.android.editor.preview.OnViewPrepareListener
import com.ss.ugc.android.editor.preview.PreviewPanel

class InfoStickerGestureManager private constructor(private val previewPanel: PreviewPanel) {
    private var mConfig : StickerEditViewConfig? = null // 配置预存
    var onViewPrepareListener : OnViewPrepareListener =object : OnViewPrepareListener {
        override fun onInfoStikerViewPrepare(infoStickerGestureView: InfoStickerGestureView) {
            update(infoStickerGestureView)
        }
    }
    companion object {
        @Volatile
        private var instance: InfoStickerGestureManager? = null
        fun getInstance(previewPanel: PreviewPanel) =
                instance ?: synchronized(this) {
                    instance ?: InfoStickerGestureManager(previewPanel).also {
                        instance = it
                        previewPanel.setOnViewPrepareListener(instance!!.onViewPrepareListener)
                    }
                }
    }


    fun setInfoStickerViewConfig(config: StickerEditViewConfig) {
        mConfig =config
    }

    private fun update(stickerGestureView: InfoStickerGestureView){
        mConfig?.apply {
            val context = stickerGestureView.context
            if (rectColor != 0) {
                stickerGestureView.selectFrame.setPaintColor(ContextCompat.getColor(context, rectColor))
            }
            if (rectStrokeWidth > 0) {
                stickerGestureView.selectFrame.setRectStrokeWidth(rectStrokeWidth.toFloat())
            }
            if (adsorptionLineColor != 0) {
                stickerGestureView.updateAdsorbColor(ContextCompat.getColor(context, adsorptionLineColor))
            }
            if (adsorptionLineWidth > 0) {
                stickerGestureView.updateAdsorptionLineWidth(adsorptionLineWidth.toFloat())
            }
            if (adsorptionLineLength > 0) {
                stickerGestureView.updateAdsorptionLineLength(adsorptionLineLength.toFloat())
            }
            stickerGestureView.setEditIcon(editIconConfig)
            stickerGestureView.setFlipIcon(flipIconConfig)
            stickerGestureView.setCopyIcon(copyIconConfig)
            stickerGestureView.setRotateIcon(rotateIconConfig)
            stickerGestureView.setDeleteIcon(deleteIconConfig)
        }
    }
}