package com.ss.ugc.android.editor.bottom

import android.annotation.SuppressLint
import com.ss.ugc.android.editor.base.EditorConfig.IFunctionBarConfig
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.base.functions.FunctionType


/**
 * 默认底部栏（CK UI风格）
 */
class DefaultFunctionBarConfig : IFunctionBarConfig {

    @SuppressLint("ResourceType")
    override fun createFunctionItemList(): ArrayList<FunctionItem> {
        return arrayListOf(
                FunctionItem.Builder()
                        .title(R.string.ck_edit_clip)
                        .icon(drawable.ic_func_cut)
                        .type(FunctionType.TYPE_FUNCTION_CUT)
                        .children(cutFunctionItemList())
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_canvas)
                        .icon(drawable.ic_func_ratio)
                        .type(FunctionType.TYPE_FUNCTION_RATIO)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_audio)
                        .icon(drawable.ic_func_music)
                        .type(FunctionType.TYPE_FUNCTION_AUDIO)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add_audio)
                                                .icon(drawable.ic_func_music)
                                                .type(FunctionType.TYPE_FUNCTION_ADD_AUDIO)
                                                .build()
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_sticker)
                        .icon(drawable.ic_func_sticker)
                        .type(FunctionType.TYPE_FUNCTION_STICKER)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add_sticker)
                                                .icon(drawable.ic_image_sticker_new)
                                                .type(FunctionType.TYPE_FUNCTION_IMAGE_STICKER)
                                                .build()
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_text)
                        .icon(drawable.ic_func_text)
                        .type(FunctionType.TYPE_FUNCTION_TEXT)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add_text)
                                                .icon(drawable.ic_text_sticker)
                                                .type(FunctionType.TYPE_FUNCTION_ADD_TEXT)
                                                .build(),
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_effect)
                        .icon(drawable.ic_func_filter)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add)
                                                .icon(drawable.ic_func_filter)
                                                .type(FunctionType.TYPE_FUNCTION_ADD_FILTER)
                                                .build()
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_effect)
                        .icon(drawable.ic_func_videoeffect)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add_effect)
                                                .icon(drawable.ic_func_videoeffect)
                                                .type(FunctionType.VIDEO_EFFECT_ADD)
                                                .build(),
                                )
                        )
                        .type(FunctionType.TYPE_FUNCTION_VIDEO_EFFECT)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_adjust)
                        .icon(drawable.ic_func_adjust)
                        .type(FunctionType.TYPE_FUNCTION_ADJUST)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_pip)
                        .icon(drawable.ic_func_pip)
                        .type(FunctionType.TYPE_FUNCTION_PIP)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_add_pip)
                                                .icon(drawable.ic_func_pip)
                                                .type(FunctionType.TYPE_CUT_PIP)
                                                .build()
                                )
                        )
                        .build()
        )
    }

    @SuppressLint("ResourceType")
    private fun cutFunctionItemList(): ArrayList<FunctionItem> {
        return arrayListOf(
                FunctionItem.Builder()
                        .title(R.string.ck_split)
                        .icon(drawable.ic_cut_cf)
                        .type(FunctionType.TYPE_CUT_SPLIT)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_delete)
                        .icon(drawable.ic_cut_delete)
                        .type(FunctionType.TYPE_CUT_DELETE)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_speed)
                        .icon(drawable.ic_cut_speed)
                        .type(FunctionType.TYPE_CUT_SPEED)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_normal_speed)
                                                .icon(drawable.ic_cut_speed_normal)
                                                .type(FunctionType.TYPE_FUNCTION_SPEED)
                                                .build()
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_rotate)
                        .icon(drawable.ic_cut_xz)
                        .type(FunctionType.TYPE_CUT_ROTATE)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_flip)
                        .icon(drawable.ic_cut_fz)
                        .type(FunctionType.TYPE_CUT_MIRROR)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_crop)
                        .icon(drawable.ic_func_crop)
                        .type(FunctionType.TYPE_CUT_CROP)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_reverse)
                        .icon(drawable.ic_cut_df)
                        .type(FunctionType.TYPE_CUT_REVERSE)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_volume)
                        .icon(drawable.ic_cut_voice)
                        .type(FunctionType.TYPE_CUT_VOLUME)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_filter)
                        .icon(drawable.ic_func_filter)
                        .type(FunctionType.TYPE_CUT_FILTER)
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_adjust)
                        .icon(drawable.ic_func_adjust)
                        .type(FunctionType.TYPE_CUT_ADJUST)
                        .build(),
                FunctionItem.Builder()
                        .title(R.string.ck_video_mask)
                        .icon(drawable.ic_func_videomask)
                        .type(FunctionType.TYPE_VIDEO_MASK)
                        .build(),
                FunctionItem.Builder()
                        .title(R.string.ck_animation)
                        .icon(drawable.ic_cut_animation)
                        .type(FunctionType.TYPE_CUT_ANIMATION)
                        .children(
                                arrayListOf(
                                        FunctionItem.Builder()
                                                .title(R.string.ck_animation_in)
                                                .icon(drawable.ic_anim_in)
                                                .type(FunctionType.ANIM_IN)
                                                .build(),
                                        FunctionItem.Builder()
                                                .title(R.string.ck_animation_out)
                                                .icon(drawable.ic_anim_out)
                                                .type(FunctionType.ANIM_OUT)
                                                .build(),
                                        FunctionItem.Builder()
                                                .title(R.string.ck_animation_group)
                                                .icon(drawable.ic_anim_all)
                                                .type(FunctionType.ANIM_ALL)
                                                .build()
                                )
                        )
                        .build(),

                FunctionItem.Builder()
                        .title(R.string.ck_mix_mode)
                        .icon(drawable.ic_cut_blendmode)
                        .type(FunctionType.TYPE_CUT_BLENDMODE)
                        .build(),
        )
    }

    override fun expendFuncItemOnTrackSelected(selectType: String): FunctionItem? {
        return when (selectType) {
            FunctionType.FUNCTION_TEXT_SELECTED -> genTextSelectItem()
            FunctionType.FUNCTION_EFFECT_SELECTED -> genEffectSelectItem()
            FunctionType.FUNCTION_AUDIO_SELECTED -> genEditAudioItem()
            FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED -> genTextTemplateSelectItem()
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
                                        .title(R.string.ck_edit)
                                        .icon(drawable.ic_text_editor)
                                        .type(FunctionType.FUNCTION_TEXT_EDITOR)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_delete)
                                        .icon(drawable.ic_cut_delete)
                                        .type(FunctionType.FUNCTION_TEXT_DELETE)
                                        .build()
                        )
                )
                .build()
    }

    @SuppressLint("ResourceType")
    private fun genTextTemplateSelectItem(): FunctionItem {
        return FunctionItem.Builder()
                .type(FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED)
                .children(
                        arrayListOf(
                                FunctionItem.Builder()
                                        .title(R.string.ck_edit)
                                        .icon(drawable.ic_text_editor)
                                        .type(FunctionType.FUNCTION_TEXT_EDITOR)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_delete)
                                        .icon(drawable.ic_cut_delete)
                                        .type(FunctionType.FUNCTION_TEXT_DELETE)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_text_reading)
                                        .icon(drawable.ic_text_editor)
                                        .type(FunctionType.FUNCTION_TEXT_RECOGNIZE_AUDIO)
                                        .build()
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
                                        .icon(drawable.ic_anim_in)
                                        .type(FunctionType.VIDEO_EFFECT_REPLACE)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_copy)
                                        .icon(drawable.ic_anim_out)
                                        .type(FunctionType.VIDEO_EFFECT_COPY)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_applied_range)
                                        .icon(drawable.ic_anim_all)
                                        .type(FunctionType.VIDEO_EFFECT_APPLY)
                                        .build(),
                                FunctionItem.Builder()
                                        .title(R.string.ck_delete)
                                        .icon(drawable.ic_anim_all)
                                        .type(FunctionType.VIDEO_EFFECT_DELETE)
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
                                        .title(R.string.ck_volume)
                                        .icon(drawable.ic_volume_set)
                                        .type(FunctionType.TYPE_AUDIO_VOLUME)
                                        .build(),

                                FunctionItem.Builder()
                                        .title(R.string.ck_delete)
                                        .icon(drawable.ic_volume_delete)
                                        .type(FunctionType.TYPE_AUDIO_DELETE)
                                        .build()
                        )
                )
                .build()
    }


    @SuppressLint("ResourceType")
    override fun createTransactionItem(): FunctionItem? {
        return FunctionItem.Builder()
                .title(R.string.ck_transition)
                .type(FunctionType.TYPE_FUNCTION_TRANSACTION)
                .build()
    }

}