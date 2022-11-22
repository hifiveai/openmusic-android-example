package com.ss.ugc.android.editor.core

class Constants {
    companion object {
        const val TRACK_TYPE = "track_type"
        const val TRACK_INDEX = "track_index"
        const val PIP_TRACK_INDEX = "pip_track_index"
        const val MUSIC_NAME = "music_name" //保存所添加转场选项的position
        const val STICKER_INDEX = "sticker_index"

        const val TRACK_VIDEO = "video"
        const val TRACK_AUDIO = "audio"
        const val TRACK_STICKER = "sticker"

        const val TRACK_FILTER = "type_filter"
        const val TRACK_FILTER_ADJUST = "type_filter_adjust"
        const val TRACK_FILTER_FILTER = "type_filter_filter"
        const val TRACK_VIDEO_EFFECT = "video_effect" //视频特效
        const val TRACK_LAYER = "track_layer"  //记录model中track的数量
        const val KEY_MAIN = "key_mainViewModel"
        const val KEY_TEMPLATE = "key_template"

        const val CLIP_INDEX = "clip_index"
        const val CLIP_REVERSE_VIDEO_PATH = "clip_reverse_video_path"

        const val FILTER_POSITION = "filter_position" //保存所添加滤镜的position
        const val AUDIO_FILTER_POSITION = "audio_filter_position" //保存所添加变声滤镜的position
        const val FILTER_INTENSITY = "filter_intensity" //保存所添加滤镜的值大小

        const val MixMode_POSITION = "mixmode_position" //混合模式position
        const val MixMode_INTENSITY = "mixmode_intensity" //混合模式值大小

        const val ADJUST_POSITION = "adjust_position" //保存所添加调节选项的position
        const val Transition_MAX_DURATION = "Transition_Max_Duration" //保存所添加转场选项的position

        const val KEY_FUNCTION = "key_function"
        const val KEY_FUNCTION_BACK = "key_function_back"
        const val KEY_ADD_AUDIO = "key_add_audio"
        const val KEY_ADD_AUDIO_FRAGMENT = "key_add_audio_fragment"
        const val KEY_ADD_VIDEO_EFFECT = "key_add_video_effect"

        const val KEY_ADD_FILTER = "key_add_filter"

        const val KEY_AUDIO_LISTEN = "key_audio_listen"
        const val KEY_STICKER_ENABLE = "key_sticker_enable"
        const val KEY_COMPRESS_SUB_TRACK = "compress_sub_track_group"

        const val CUSTOM_CANVAS_PATH = "custom_canvas_path"

        /**
         * 文字模板，依赖资源的urs_id
         */
        const val DEP_RES_URS_ID = "dependency_res_id"
        const val ORIGIN_RES_ID = "origin_res_id"

        var currentEnv = Env.PRODUCT_CN

        /**
         * 是否支持外网访问
         */
        var EXTRANET_ENVIRONMENT = true

        const val MAX_SUB_VIDEO_LIMIT = 19

        enum class Env {
            PRODUCT_CN,
            BOE_CN,
            PRODUCT_OVER_SEA,
            BOE_OVER_SEA
        }
    }
}