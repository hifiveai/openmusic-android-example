package com.ss.ugc.android.editor.base.theme

import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceTextConfig
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.base.theme.resource.*

object ThemeStore {

    private val editorUIConfig = EditorSDK.instance.editorUIConfig()

    val globalUIConfig by lazy {
        editorUIConfig?.customizeGlobalUI() ?: GlobalUIConfig()
    }

    val bottomUIConfig by lazy {
        editorUIConfig?.customizeBottomUI() ?: BottomUIConfig()
    }

    val previewUIConfig by lazy {
        editorUIConfig?.customizePreviewUI() ?: PreviewUIConfig()
    }

    val trackUIConfig by lazy {
        editorUIConfig?.customizeTrackUI() ?: TrackUIConfig()
    }

    fun setCommonBackgroundRes(target: View) {
        globalUIConfig.apply {
            if (btnBgDrawableRes > 0) {
                target.setBackgroundResource(btnBgDrawableRes)
            }
        }
    }

    fun setLightBackgroundRes(target: View) {
        globalUIConfig.apply {
            if (lightBtnBgDrawableRes > 0) {
                target.setBackgroundResource(lightBtnBgDrawableRes)
            }
        }
    }

    fun setCommonTextColor(target: TextView) {
        globalUIConfig.apply {
            if (themeColorRes > 0) {
                target.setTextColor(target.resources.getColor(themeColorRes))
            }
        }
    }

    fun setSelectedTabIndicatorColor(target: TabLayout) {
        globalUIConfig.apply {
            if (themeColorRes > 0) {
                target.setSelectedTabIndicatorColor(target.resources.getColor(themeColorRes))
            }
        }
    }

    fun getResourceLoadingView(): ((ViewGroup) -> View)? {
        return bottomUIConfig.resourceListViewConfig.loadingView
    }

    fun getResourceEmptyView(): ((ViewGroup) -> View)? {
        return bottomUIConfig.resourceListViewConfig.emptyView
    }

    fun getResourceEmptyRetryView(): ((parent: ViewGroup) -> Pair<View, View>)? {
        return bottomUIConfig.resourceListViewConfig.errorRetryView
    }

    fun getFunctionBarViewConfig(): FuncBarViewConfig {
        return bottomUIConfig.funcBarViewConfig
    }

    fun getOptPanelViewConfig(): OptPanelViewConfig {
        return bottomUIConfig.optPanelViewConfig
    }

    fun getResourceListViewConfig(): ResourceListViewConfig {
        return bottomUIConfig.resourceListViewConfig
    }

    fun getDownloadIconConfig(): DownloadIconConfig {
        return getResourceListViewConfig().downloadIconConfig
    }

    fun getResourceImageConfig(): ResourceImageConfig {
        return getResourceListViewConfig().resourceImageConfig
    }

    fun getResourceTextConfig(): ResourceTextConfig {
        return getResourceListViewConfig().resourceTextConfig
    }

    fun getItemSelectorConfig(): ItemSelectorConfig {
        return getResourceListViewConfig().itemSelectorConfig
    }

    fun getFirstNullItemConfig(): FirstNullItemConfig {
        return getResourceListViewConfig().firstNullItemConfig
    }

    fun getCustomItemConfig(): CustomItemConfig {
        return getResourceListViewConfig().customItemConfig
    }

    fun getSelectBorderRes(): Int {
        return getItemSelectorConfig().selectorBorderRes
    }

    fun getFirstNullItemRes(): Int {
        return getFirstNullItemConfig().nullItemResource
    }

    fun getResourceItemRoundRadius(): Int {
        return getResourceImageConfig().roundRadius
    }

    fun getUndoIconRes(): Int? {
        return globalUIConfig.undoIconRes
    }

    fun getRedoIconRes(): Int? {
        return globalUIConfig.redoIconRes
    }


    fun getPlayIconRes(): Int? {
        return globalUIConfig.playIconRes
    }


    fun getVoiceRecognizeViewConfig(): VoiceRecognizeViewConfig {
        return bottomUIConfig.voiceRecognizeViewConfig
    }

    /**
     *  trackpanel UI 定制化
     */
    fun setTransitionIconRes(target: View) {
        trackUIConfig.apply {
            if (trackTransitionCustomIcon != null) {
                (target as ImageView).setImageResource(trackTransitionCustomIcon!!)
            }
        }
    }

    fun setMainTrackLeftMoveIconRes(target: View) {
        trackUIConfig.apply {
            if (mainTrackLeftMoveCustomIcon != null) {
                target.setBackgroundResource(mainTrackLeftMoveCustomIcon!!)
            }
        }
    }

    fun setMainTrackRightMoveIconRes(target: View) {
        trackUIConfig.apply {
            if (mainTrackRightMoveCustomIcon != null) {
                target.setBackgroundResource(mainTrackRightMoveCustomIcon!!)
            }
        }
    }

    fun getViceTrackLeftMoveIconRes(): Int?{
        trackUIConfig.apply {
            if(viceTrackLeftMoveCustomIcon != null){
                return viceTrackLeftMoveCustomIcon
            } else {
                return null
            }
        }
    }

    fun getViceTrackRightMoveIconRes(): Int?{
        trackUIConfig.apply {
            if(viceTrackRightMoveCustomIcon != null){
                return viceTrackRightMoveCustomIcon
            } else {
                return null
            }
        }
    }

    fun setCustomAddMediaIconRes(target: View) {
        trackUIConfig.apply {
            if (addMediaCustomIcon != null) {
                target.background = ContextCompat.getDrawable(target.context, addMediaCustomIcon!!)
            }
        }
    }

    fun getCustomAddMediaIconRes(): Int? {
        trackUIConfig.apply {
            if (addMediaCustomIcon != null) {
                return addMediaCustomIcon!!
            } else {
                return null
            }
        }
    }

    fun getCustomImageSlotColor(): Int? {
        trackUIConfig.apply {
            if (imageTrackColor != null) {
                return imageTrackColor!!
            } else {
                return null
            }
        }
    }

    fun getCustomStickerSlotColor(): Int? {
        trackUIConfig.apply {
            if (stickerTrackColor != null) {
                return stickerTrackColor!!
            } else {
                return null
            }
        }
    }

    fun getCustomSubtitleSlotColor(): Int? {
        trackUIConfig.apply {
            if (subtitleTrackColor != null) {
                return subtitleTrackColor!!
            } else {
                return null
            }
        }
    }

    fun getCustomEffectSlotColor(): Int? {
        trackUIConfig.apply {
            if (effectTrackColor != null) {
                return effectTrackColor!!
            } else {
                return null
            }
        }
    }

    fun getCustomTextSlotColor(): Int? {
        trackUIConfig.apply {
            if (textTrackColor != null) {
                return textTrackColor!!
            } else {
                return null
            }
        }
    }

    fun getCustomRecordWaveColor(): Int? {
        trackUIConfig.apply {
            if (recordWaveColor != null) {
                return recordWaveColor
            } else {
                return null
            }
        }
    }

    fun getCustomAfterRecordWaveColor(): Int? {
        trackUIConfig.apply {
            if (afterRecordWaveColor != null) {
                return afterRecordWaveColor
            } else {
                return null
            }
        }
    }

    fun getCustomAudioBgColor(): Int? {
        trackUIConfig.apply {
            if (audioTrackBgColor != null) {
                return audioTrackBgColor
            } else {
                return null
            }
        }
    }

    fun getCustomRecordBgColor(): Int? {
        trackUIConfig.apply {
            if (recordTrackBgColor != null) {
                return recordTrackBgColor
            } else {
                return null
            }
        }
    }

    fun getCustomAudioWaveColor(): Int? {
        trackUIConfig.apply {
            if (audioWaveColor != null) {
                return audioWaveColor
            } else {
                return null
            }
        }
    }

    fun getCustomItemFrameWidth(): Int? {
        trackUIConfig.apply {
            if (mainTrackItemFrameWidth != null) {
                return SizeUtil.dp2px( mainTrackItemFrameWidth!!.toFloat())
            } else {
                return null
            }
        }
    }

    fun getCustomMainTrackHeight(): Int? {
        trackUIConfig.apply {
            if (mainTrackHeight != null) {
                return mainTrackHeight!!
            } else {
                return null
            }
        }
    }

    fun setCustomMainTrackHeight(target: View) {
        trackUIConfig.apply {
            if (mainTrackHeight != null) {
                val lp: ViewGroup.LayoutParams = target.getLayoutParams()
                lp.height = SizeUtil.dp2px(mainTrackHeight!!.toFloat())
                target.layoutParams = lp
            }
        }
    }

    fun getCustomItemFrameHeight(): Int? {
        trackUIConfig.apply {
            if (itemFrameHeight != null) {
                return SizeUtil.dp2px(itemFrameHeight!!.toFloat())
            } else {
                return null
            }
        }
    }

    fun getCustomViceTrackHeight(): Int? {
        trackUIConfig.apply {
            if (viceTrackHeight != null) {
                return SizeUtil.dp2px(viceTrackHeight!!.toFloat())
            } else {
                return null
            }
        }
    }

    fun getCustomViceTrackItemFrameWidth(): Int? {
        trackUIConfig.apply {
            if (viceTrackItemFrameWidth != null) {
                return SizeUtil.dp2px(viceTrackItemFrameWidth!!.toFloat())
            } else {
                return null
            }
        }
    }

    fun getCustomViceTrackItemFrameHeight(): Int? {
        trackUIConfig.apply {
            if (viceTrackItemFrameHeight != null) {
                return SizeUtil.dp2px(viceTrackItemFrameHeight!!.toFloat())
            } else {
                return null
            }
        }
    }

    fun getCustomMainTrackNormalChangeSpeedIcon(): Int? {
        trackUIConfig.apply {
            if (mainTrackNormalChangeSpeedIcon != null) {
                return mainTrackNormalChangeSpeedIcon
            } else {
                return null
            }
        }
    }

    fun getCustomMainTrackCurveChangeSpeedIcon(): Int? {
        trackUIConfig.apply {
            if (mainTrackCurveChangeSpeedIcon != null) {
                return mainTrackCurveChangeSpeedIcon
            } else {
                return null
            }
        }
    }

    fun getCustomViceTrackNormalChangeSpeedIcon(): Int? {
        trackUIConfig.apply {
            if (viceTrackNormalChangeSpeedIcon != null) {
                return viceTrackNormalChangeSpeedIcon
            } else {
                return null
            }
        }
    }

    fun getCustomViceTrackCurveChangeSpeedIcon(): Int? {
        trackUIConfig.apply {
            if (viceTrackCurveChangeSpeedIcon != null) {
                return viceTrackCurveChangeSpeedIcon
            } else {
                return null
            }
        }
    }

    fun getCustomEnableOriginalVoiceIcon(): Int? {
        trackUIConfig.apply {
            if (enableOriginalVoiceIcon != null) {
                return enableOriginalVoiceIcon
            } else {
                return null
            }
        }
    }

    fun getCustomDisableOriginalVoiceIcon(): Int? {
        trackUIConfig.apply {
            if (disableOriginalVoiceIcon != null) {
                return disableOriginalVoiceIcon
            } else {
                return null
            }
        }
    }

    fun getCustomDisableOriginalVoiceTip(): Int? {
        trackUIConfig.apply {
            if (disableOriginalVoiceTip != null) {
                return disableOriginalVoiceTip
            } else {
                return null
            }
        }
    }

    /**
     *
     */
}