package com.ss.ugc.android.editor.base.data

/**
 * time : 2020/12/11
 *
 * description :
 *
 */
class FilterType {
    companion object {
        // 视频大小,等调节
        const val CANVAS_BLEND = "canvas blend"
        const val ANIM = "ANIM"

        // 蒙版
        const val MASK_FILTER = "mask_filter"

        // 抠图
        const val CHROMA = "chroma"

        // 滤镜
        const val COLOR_FILTER = "color_filter"

        // 亮度
        const val BRIGHTNESS = "brightness"

        // 对比度
        const val CONTRAST = "contrast"

        // 饱和度
        const val SATURATION = "saturation"

        // 锐化
        const val SHARPEN = "sharpen"

        // 高光
        const val HIGHLIGHT = "highlight"

        // 阴影
        const val SHADOW = "shadow"

        // 色温
        const val TEMPERATURE = "temperature"

        // 色调
        const val TONE = "tone"

        // 褪色
        const val FADE = "fade"

        // 光感
        const val LIGHT_SENSATION = "light_sensation"

        // 暗角
        const val VIGNETTING = "vignetting"

        // 颗粒
        const val PARTICLE = "particle"

        // 这2个 type 是客户端自行增加的,贴纸类型的 filterType
        const val TYPE_STICKER = "info_sticker"
        const val TYPE_TEXT = "text_sticker"

        const val AUDIO_VOLUME = "audio volume filter"

        // 滤镜和调节一期先不做 ,这里先注释
        val KEYFRAME_TYPES = arrayOf(
            CANVAS_BLEND,
            MASK_FILTER,
            CHROMA,
            TYPE_STICKER,
            TYPE_TEXT,
            AUDIO_VOLUME,
            COLOR_FILTER,
            BRIGHTNESS,
            CONTRAST,
            SATURATION,
            SHARPEN,
            HIGHLIGHT,
            SHADOW,
            TEMPERATURE,
            TONE,
            FADE,
            LIGHT_SENSATION,
            VIGNETTING,
            PARTICLE
        )

        const val CROP = "crop filter"
        const val BEAUTY = "beauty_filter"
        const val RESHAPE = "reshape"
        const val AUDIO = "audio effect"
        const val COLOR_CANVAS = "color_canvas"
        const val AUDIO_FADING = "audio fading"
        const val FILTER_GLOBAL_FILTER = "global_color_filter"
        const val VIDEO_EFFECT = "video_effect"
    }
}