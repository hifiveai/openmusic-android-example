package com.ss.ugc.android.editor.core.api.sticker

import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.core.event.SelectedTrackSlotEvent

/**
 * 贴纸编辑器
 */
interface IStickerEditor: ITextTemplateEditor{

    /**
     * 应用信息贴纸
     */
    fun applyInfoSticker(stickerIcon: String, stickerPath: String, stickerId: String? = null, param: InfoStickerParam? = null): NLETrackSlot

    /**
     * 应用图片贴纸
     */
    fun applyImageSticker(param: ImageStickerParam): NLETrackSlot?

    /**
     * 应用文字贴纸
     */
    fun applyTextSticker(
        info: TextStyleInfo = TextStyleInfo.emptySticker(),
        param: InfoStickerParam? = null,
        defaultFontPaths: List<String>? = null
    ): SelectedTrackSlotEvent

    /**
     * 应用Emoji表情贴纸
     */
    fun applyEmojiSticker(utf8Code: String): NLETrackSlot?

    /**
     * 更新文字贴纸
     */
    fun updateTextSticker(isUpdateWithKeyframe: Boolean = false): NLETrackSlot?

    /**
     * 更新信息化或图片贴纸
     */
    fun updateInfoOrImageSticker(): NLETrackSlot?

    /**
     * 删除贴纸
     */
    fun removeSticker(): Boolean

    /**
     * 更新贴纸起始时间
     */
    fun updateStickerTimeRange(
        startTime: Long? = null,
        endTime: Long? = null,
        adjustKeyFrame: Boolean = false
    ): Boolean

    /**
     * 更新贴纸位置
     */
    fun updateStickPosition(
        offsetX: Float? = null,
        offsetY: Float? = null
    ): Boolean

    /**
     * 更新贴纸缩放比例
     */
    fun updateStickerScale(scale: Float? = null): Boolean

    /**
     * 更新贴纸旋转角度
     */
    fun updateStickerRotation(rotation: Float? = null): Boolean

    /**
     * 更新贴纸缩放和旋转
     */
    fun updateStickerScaleAndRotation(
        scale: Float? = null,
        rotation: Float? = null
    ): Boolean

    /**
     * 更新贴纸Flip
     */
    fun updateStickerFlip(flipX: Boolean? = null, flipY: Boolean = false): Boolean

}