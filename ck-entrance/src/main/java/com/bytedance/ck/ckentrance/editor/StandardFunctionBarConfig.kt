package com.bytedance.ckdemo.editor

import android.annotation.SuppressLint
import com.ss.ugc.android.editor.base.EditorConfig
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.main.config.CKTobFunctionBarConfig

/**
 *  author : lijunguan
 *  date : 2022/6/7 4:39 下午
 *  description :
 */
class StandardFunctionBarConfig : EditorConfig.IFunctionBarConfig {
    @SuppressLint("ResourceType")
    override fun createFunctionItemList(): ArrayList<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_edit_clip)
                .icon(R.drawable.ic_func_cut)
                .type(FunctionType.TYPE_FUNCTION_CUT)
                .children(cutFunctionItemList())
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_size)
                .icon(R.drawable.ic_func_ratio)
                .type(FunctionType.TYPE_FUNCTION_RATIO)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_canvas)
                .icon(R.drawable.ic_func_canvas)
                .type(FunctionType.TYPE_FUNCTION_CANVAS)
                .children(genCanvasChildren())
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_audio)
                .icon(R.drawable.ic_func_music)
                .type(FunctionType.TYPE_FUNCTION_AUDIO)
                .children(musicChildren())
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_image_sticker)
                .icon(R.drawable.ic_func_sticker)
                .type(FunctionType.TYPE_FUNCTION_STICKER)
                .children(genStickerChildren())
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_text)
                .icon(R.drawable.ic_func_text)
                .type(FunctionType.TYPE_FUNCTION_TEXT)
                .children(genTextChildren())
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_filter)
                .icon(R.drawable.ic_func_filter)
                .children(genFilterChildren())
                .type(FunctionType.TYPE_FUNCTION_FILTER_STATE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_effect)
                .icon(R.drawable.ic_videoeffect_replace)
                .children(genEffectChildren())
                .type(FunctionType.TYPE_FUNCTION_VIDEO_EFFECT)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_adjust)
                .children(genAdjustChildren())
                .icon(R.drawable.ic_func_adjust)
                .type(FunctionType.TYPE_FUNCTION_ADJUST)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_pip)
                .icon(R.drawable.ic_func_pip)
                .type(FunctionType.TYPE_FUNCTION_PIP)
                .children(genPipChildren())
                .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun cutFunctionItemList(): ArrayList<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_split)
                .icon(R.drawable.ic_cut_cf)
                .type(FunctionType.TYPE_CUT_SPLIT)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_delete)
                .icon(R.drawable.ic_cut_delete)
                .type(FunctionType.TYPE_CUT_DELETE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_speed)
                .icon(R.drawable.ic_cut_speed)
                .type(FunctionType.TYPE_CUT_SPEED)
                .children(
                    arrayListOf(
                        FunctionItem.Builder()
                            .title(R.string.ck_normal_speed)
                            .icon(R.drawable.ic_cut_speed_normal)
                            .type(FunctionType.TYPE_FUNCTION_SPEED)
                            .build(),
                        FunctionItem.Builder()
                            .title(R.string.ck_curve_speed)
                            .icon(R.drawable.ic_cut_curves_speed)
                            .type(FunctionType.TYPE_FUNCTION_CURVE_SPEED)
                            .build()
                    )
                )
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_rotate)
                .icon(R.drawable.ic_cut_xz)
                .type(FunctionType.TYPE_CUT_ROTATE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_flip)
                .icon(R.drawable.ic_cut_fz)
                .type(FunctionType.TYPE_CUT_MIRROR)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_crop)
                .icon(R.drawable.ic_func_crop)
                .type(FunctionType.TYPE_CUT_CROP)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_reverse)
                .icon(R.drawable.ic_cut_df)
                .type(FunctionType.TYPE_CUT_REVERSE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_volume)
                .icon(R.drawable.ic_cut_voice)
                .type(FunctionType.TYPE_CUT_VOLUME)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_filter)
                .icon(R.drawable.ic_func_filter)
                .type(FunctionType.TYPE_CUT_FILTER)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_adjust)
                .icon(R.drawable.ic_func_adjust)
                .type(FunctionType.TYPE_CUT_ADJUST)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_video_mask)
                .icon(R.drawable.ic_func_videomask)
                .type(FunctionType.TYPE_VIDEO_MASK)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_change_voice)
                .icon(R.drawable.ic_cut_change_voice)
                .type(FunctionType.TYPE_CUT_CHANGE_VOICE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_animation)
                .icon(R.drawable.ic_cut_animation)
                .type(FunctionType.TYPE_CUT_ANIMATION)
                .children(
                    arrayListOf(
                        FunctionItem.Builder()
                            .title(R.string.ck_animation_in)
                            .icon(R.drawable.ic_anim_in)
                            .type(FunctionType.ANIM_IN)
                            .build(),
                        FunctionItem.Builder()
                            .title(R.string.ck_animation_out)
                            .icon(R.drawable.ic_anim_out)
                            .type(FunctionType.ANIM_OUT)
                            .build(),
                        FunctionItem.Builder()
                            .title(R.string.ck_animation_group)
                            .icon(R.drawable.ic_anim_all)
                            .type(FunctionType.ANIM_ALL)
                            .build()
                    )
                )
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_mix_mode)
                .icon(R.drawable.ic_cut_blendmode)
                .type(FunctionType.TYPE_CUT_BLENDMODE)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_freeze)
                .icon(R.drawable.ic_freeze_frame)
                .type(FunctionType.TYPE_CUT_FREEZE)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_replace)
                .icon(R.drawable.ic_replace_slot)
                .type(FunctionType.TYPE_CUT_REPLACE)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_copy)
                .icon(R.drawable.ic_copy_slot)
                .type(FunctionType.TYPE_CUT_COPY)
                .build(),

            )
    }

    override fun expendFuncItemOnTrackSelected(selectType: String): FunctionItem? {
        return when (selectType) {
            FunctionType.FUNCTION_TEXT_SELECTED -> genTextSelectItem()
            FunctionType.FUNCTION_STICKER_SELECTED -> genStickerSelectItem()
            FunctionType.FUNCTION_EFFECT_SELECTED -> genEffectSelectItem()
            FunctionType.FUNCTION_AUDIO_SELECTED -> genEditAudioItem()
            FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED -> genTextTemplateSelectItem()
            FunctionType.FUNCTION_ADJUST_SELECTED -> genAdjustSelectItem()
            FunctionType.FUNCTION_FILTER_SELECTED -> genFilterSelectItem()
            else -> null
        }
    }

    @SuppressLint("ResourceType")
    private fun genTextSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_TEXT_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_split)
                        .icon(R.drawable.ic_cut_cf)
                        .type(FunctionType.FUNCTION_TEXT_SPLIT)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_copy)
                        .icon(R.drawable.ic_func_copy)
                        .type(FunctionType.FUNCTION_TEXT_COPY)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_edit)
                        .icon(R.drawable.ic_text_editor)
                        .type(FunctionType.FUNCTION_TEXT_EDITOR)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_cut_delete)
                        .type(FunctionType.FUNCTION_TEXT_DELETE)
                        .build()
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genStickerSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_TEXT_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_split)
                        .icon(R.drawable.ic_cut_cf)
                        .type(FunctionType.FUNCTION_STICKER_SPLIT)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_copy)
                        .icon(R.drawable.ic_func_copy)
                        .type(FunctionType.FUNCTION_STICKER_COPY)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_animation)
                        .icon(R.drawable.ic_cut_animation)
                        .type(FunctionType.FUNCTION_STICKER_ANIM)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_cut_delete)
                        .type(FunctionType.FUNCTION_STICKER_DELETE)
                        .build(),
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genEffectSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_EFFECT_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_replace_effect)
                        .icon(R.drawable.ic_videoeffect_replace)
                        .type(FunctionType.VIDEO_EFFECT_REPLACE)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_copy)
                        .icon(R.drawable.ic_copy_slot)
                        .type(FunctionType.VIDEO_EFFECT_COPY)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_applied_range)
                        .icon(R.drawable.ic_videoeffect_apply)
                        .type(FunctionType.VIDEO_EFFECT_APPLY)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_videoeffect_delete)
                        .type(FunctionType.VIDEO_EFFECT_DELETE)
                        .build()
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genAdjustSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_ADJUST_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_adjust)
                        .icon(R.drawable.ic_func_adjust)
                        .type(FunctionType.TYPE_FUNCTION_ADJUST)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_cut_delete)
                        .type(FunctionType.FUNCTION_ADJUST_DELETE)
                        .build()
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genFilterSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_FILTER_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_change_filter)
                        .icon(R.drawable.ic_func_filter)
                        .type(FunctionType.TYPE_FUNCTION_ADD_FILTER)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_cut_delete)
                        .type(FunctionType.FUNCTION_FILTER_DELETE)
                        .build()
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genEditAudioItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_AUDIO_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_split)
                        .icon(R.drawable.ic_cut_cf)
                        .type(FunctionType.TYPE_AUDIO_SPLIT)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_copy)
                        .icon(R.drawable.ic_func_copy)
                        .type(FunctionType.TYPE_AUDIO_COPY)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_volume)
                        .icon(R.drawable.ic_volume_set)
                        .type(FunctionType.TYPE_AUDIO_VOLUME)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_volume_delete)
                        .type(FunctionType.TYPE_AUDIO_DELETE)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_fade)
                        .icon(R.drawable.ic_audio_fade)
                        .type(FunctionType.TYPE_AUDIO_IN_OUT)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_change_voice)
                        .icon(R.drawable.ic_cut_change_voice)
                        .type(FunctionType.TYPE_RECORDING_CHANGE_VOICE)
                        .build(),

                    FunctionItem.Builder()
                        .title(R.string.ck_speed)
                        .icon(R.drawable.ic_cut_speed)
                        .type(FunctionType.TYPE_FUNCTION_SPEED)
                        .build()
                )
            )
            .build()
    }


    @SuppressLint("ResourceType")
    private fun speedChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_normal_speed)
                .icon(R.drawable.ic_cut_normal_speed)
                .type(FunctionType.TYPE_FUNCTION_SPEED)
                .build()
        )
    }

    @SuppressLint("ResourceType")
    override fun createTransactionItem(): FunctionItem? {
        return FunctionItem.Builder()
            .title(R.string.ck_transition)
            .type(FunctionType.TYPE_FUNCTION_TRANSACTION)
            .build()
    }

    @SuppressLint("ResourceType")
    private fun musicChildren(): ArrayList<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_add_audio)
                .icon(R.drawable.ic_func_music)
                .type(FunctionType.TYPE_ADD_AUDIO)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_add_sound_effect)
                .icon(R.drawable.ic_icon_sound_effect)
                .type(FunctionType.TYPE_ADD_AUDIO_EFFECT)
                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_record_audio)
                .icon(R.drawable.ic_recording_n)
                .type(FunctionType.TYPE_AUDIO_RECORD)
                .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun genStickerChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_add_image_sticker)
                .icon(R.drawable.ic_image_sticker_new)
                .type(FunctionType.TYPE_FUNCTION_IMAGE_STICKER)
                .build()
        )
    }


    @SuppressLint("ResourceType")
    private fun genTextChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_text_template)
                .icon(R.drawable.ic_text_template)
                .type(FunctionType.TYPE_FUNCTION_TEXT_TEMPLATE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_apply_text_sticker_cover)
                .icon(R.drawable.ic_text_sticker)
                .type(FunctionType.TYPE_FUNCTION_TEXT)
                .build()
        )
    }


    @SuppressLint("ResourceType")
    private fun genFilterChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_add_filter)
                .icon(R.drawable.ic_func_filter)
                .type(FunctionType.TYPE_FUNCTION_ADD_FILTER)
                .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun genEffectChildren(): List<FunctionItem> {
        return arrayListOf(
//                FunctionItem.Builder()
//                    .title("选中特效")
//                    .icon(drawable.ic_func_videoeffect)
//                    .type(FunctionType.VIDEO_EFFECT_SELECT)
//                    .children(genEffectSelectChildren())
//                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_add_effect)
                .icon(R.drawable.ic_videoeffect_replace)
                .type(FunctionType.VIDEO_EFFECT_ADD)
                .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun genAdjustChildren(): List<FunctionItem> {
        return arrayListOf(
//                FunctionItem.Builder()
//                    .title("选中特效")
//                    .icon(drawable.ic_func_videoeffect)
//                    .type(FunctionType.VIDEO_EFFECT_SELECT)
//                    .children(genEffectSelectChildren())
//                .build(),
            FunctionItem.Builder()
                .title(R.string.ck_add_adjust)
                .icon(R.drawable.ic_func_adjust)
                .type(FunctionType.TYPE_FUNCTION_ADJUST)
                .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun genPipChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_add_pip)
                .icon(R.drawable.ic_func_pip)
                .type(FunctionType.TYPE_CUT_PIP)
                .build()
        )
    }


    @SuppressLint("ResourceType")
    private fun genTextTemplateSelectItem(): FunctionItem {
        return FunctionItem.Builder()
            .type(FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED)
            .children(
                arrayListOf(
                    FunctionItem.Builder()
                        .title(R.string.ck_replace)
                        .icon(R.drawable.ic_func_replace)
                        .type(FunctionType.TYPE_FUNCTION_TEXT_TEMPLATE)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_split)
                        .icon(R.drawable.ic_cut_cf)
                        .type(FunctionType.FUNCTION_TEXT_TEMPLATE_SPLIT)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_copy)
                        .icon(R.drawable.ic_func_copy)
                        .type(FunctionType.FUNCTION_TEXT_COPY)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_edit)
                        .icon(R.drawable.ic_text_editor)
                        .type(FunctionType.FUNCTION_TEXT_TEMPLATE_EDIT)
                        .build(),
                    FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(R.drawable.ic_cut_delete)
                        .type(FunctionType.FUNCTION_TEXT_DELETE)
                        .build()
                )
            )
            .build()
    }

    @SuppressLint("ResourceType")
    private fun genCanvasChildren(): List<FunctionItem> {
        return arrayListOf(
            FunctionItem.Builder()
                .title(R.string.ck_canvas_color)
                .icon(R.drawable.ic_canvas_color)
                .type(FunctionType.TYPE_CANVAS_COLOR)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_canvas_style)
                .icon(R.drawable.ic_canvas_style)
                .type(FunctionType.TYPE_CANVAS_STYLE)
                .build(),

            FunctionItem.Builder()
                .title(R.string.ck_canvas_blur)
                .icon(R.drawable.ic_canvas_blur)
                .type(FunctionType.TYPE_CANVAS_BLUR)
                .build(),
        )
    }
}