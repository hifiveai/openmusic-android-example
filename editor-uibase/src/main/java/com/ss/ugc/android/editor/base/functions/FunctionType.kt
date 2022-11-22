package com.ss.ugc.android.editor.base.functions


/**
 * @date: 2021/3/28
 */
class FunctionType {

    companion object {
        const val TYPE_FUNCTION_CUT = "cut"
        const val TYPE_FUNCTION_RATIO = "ratio"
        const val TYPE_FUNCTION_AUDIO = "audio"
        const val TYPE_FUNCTION_ADD_AUDIO = "add_audio"
        const val TYPE_FUNCTION_STICKER = "sticker"
        const val TYPE_FUNCTION_IMAGE_STICKER = "image_sticker"
        const val TYPE_FUNCTION_TEXT = "text_sticker"
        const val TYPE_FUNCTION_ADD_TEXT = "add_text_sticker"
        const val TYPE_FUNCTION_FILTER = "filter"
        const val TYPE_FUNCTION_FILTER_STATE = "filter_state"
        const val TYPE_FUNCTION_ADD_FILTER = "global_filter"
        const val TYPE_FUNCTION_ADJUST = "global_adjust"        //全局调节
        const val TYPE_FUNCTION_VIDEO_EFFECT = "video_effect"   //特效
        const val TYPE_FUNCTION_PIP = "pip"
        const val TYPE_FUNCTION_SPEED = "speed"
        const val TYPE_FUNCTION_CURVE_SPEED = "curve_speed"
        const val TYPE_FUNCTION_VOLUME = "volume"
        const val TYPE_FUNCTION_TRANSACTION = "transaction"
        const val TYPE_FUNCTION_CANVAS = "canvas"


        //裁剪类型
        const val TYPE_CUT_SPLIT = "type_cut_split"     //拆分
        const val TYPE_CUT_DELETE = "type_cut_delete"   //删除
        const val TYPE_CUT_COPY = "type_cut_copy"       //复制
        const val TYPE_CUT_REPLACE = "type_cut_replace" //替换
        const val TYPE_CUT_SPEED = "type_cut_speed"     //速度
        const val TYPE_CUT_ROTATE = "type_cut_rotate"   //旋转
        const val TYPE_CUT_MIRROR = "type_cut_mirror"   //翻转
        const val TYPE_CUT_CROP = "type_cut_crop"       //裁剪
        const val TYPE_CUT_REVERSE = "type_cut_reverse" //倒放
        const val TYPE_CUT_VOLUME = "type_cut_volume"   //音量
        const val TYPE_CUT_FILTER = "cut_filter"        //局部滤镜
        const val TYPE_CUT_ADJUST = "cut_adjust"   //调节
        const val TYPE_VIDEO_MASK = "type_video_mask"   // 视频蒙版
        const val TYPE_CUT_CHANGE_VOICE = "type_cut_change_voice"   // 变声-剪辑
        const val TYPE_RECORDING_CHANGE_VOICE = "type_recording_change_voice"   // 变声-录音
        const val TYPE_CUT_ANIMATION = "type_cut_anim"       //动画
        const val TYPE_CUT_BLENDMODE = "type_cut_blendmode"  //混合模式
        const val TYPE_CUT_PIP = "type_cut_pip"         //画中画
        const val TYPE_CUT_FREEZE = "type_cut_freeze"

        //音轨类型
        const val TYPE_AUDIO_VOLUME = "audio_volume"    //音量
        const val TYPE_AUDIO_DELETE = "audio_delete"    //删除
        const val TYPE_AUDIO_RECORD = "type_audio_record"    //删除

        const val TYPE_AUDIO_IN_OUT = "audio_in_out"    //淡化
        const val TYPE_AUDIO_SPLIT = "audio_split"      //音频拆分
        const val TYPE_AUDIO_COPY = "audio_copy"        //音频拆分

        const val TYPE_ADD_AUDIO= "add_audio"   //添加音频
        const val TYPE_ADD_AUDIO_EFFECT = "add_audio_effect"   //添加音效



        const val ANIM_IN = "anim_in"
        const val ANIM_OUT = "anim_out"
        const val ANIM_ALL = "anim_all"

        // 特效
        const val VIDEO_EFFECT_ADD = "type_video_effect_add"         //添加特效
        const val FUNCTION_EFFECT_SELECTED = "type_video_effect_select"         //选中特效

        const val VIDEO_EFFECT_REPLACE = "video_effect_replace"         //替换特效
        const val VIDEO_EFFECT_COPY = "video_effect_copy"              //复制
        const val VIDEO_EFFECT_APPLY = "video_effect_apply"         //作用对象
        const val VIDEO_EFFECT_DELETE= "video_effect_delete"         //删除

        // 调节
        const val FUNCTION_ADJUST_SELECTED = "type_adjust_select"         //选中调节
        const val FUNCTION_ADJUST_DELETE = "type_adjust_delete"         //删除调节

        const val FUNCTION_FILTER_SELECTED = "type_filter_select"         //选中调节
        const val FUNCTION_FILTER_DELETE = "type_filter_delete"         //删除调节


        // 文字选中
        const val FUNCTION_TEXT_SELECTED = "text_sticker_selected"
        const val FUNCTION_TEXT_RECOGNIZE = "text_sticker_recognize"
        const val FUNCTION_TEXT_RECOGNIZE_AUDIO = "text_recognize_audio"
        const val FUNCTION_TEXT_EDITOR = "text_sticker_editor"
        const val FUNCTION_TEXT_DELETE = "text_sticker_delete"
        const val FUNCTION_TEXT_COPY = "text_sticker_copy"
        const val FUNCTION_TEXT_SPLIT = "text_sticker_split"

        const val TYPE_FUNCTION_TEXT_TEMPLATE = "text_template"
        const val FUNCTION_TEXT_TEMPLATE_SELECTED = "text_template_selected"
        const val FUNCTION_TEXT_TEMPLATE_SPLIT = "text_template_split"
        const val FUNCTION_TEXT_TEMPLATE_COPY = "text_template_copy"
        const val FUNCTION_TEXT_TEMPLATE_EDIT = "text_template_edit"
        const val FUNCTION_TEXT_TEMPLATE_DELETE = "text_template_delete"

        //贴纸选中
        const val FUNCTION_STICKER_SELECTED = "sticker_selected"
        const val FUNCTION_STICKER_DELETE = "sticker_delete"
        const val FUNCTION_STICKER_COPY = "sticker_copy"
        const val FUNCTION_STICKER_SPLIT = "sticker_split"
        const val FUNCTION_STICKER_ANIM = "sticker_anim"

        // 音轨选中
        const val FUNCTION_AUDIO_SELECTED = "audio_selected"

        //画布分类
        const val TYPE_CANVAS_COLOR = "canvas_color"
        const val TYPE_CANVAS_STYLE = "canvas_style"
        const val TYPE_CANVAS_BLUR = "canvas_blur"


    }
}