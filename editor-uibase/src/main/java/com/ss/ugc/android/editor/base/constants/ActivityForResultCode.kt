package com.ss.ugc.android.editor.base.constants

/**
 * @date: 2021/3/30
 */
class ActivityForResultCode {

    companion object {
        const val CHOSE_VIDEO_REQUEST_CODE = 0
        const val ADD_VIDEO_REQUEST_CODE = 1 // 选择视频
        const val PIP_VIDEO_REQUEST_CODE = 2 // 画中画
        const val ADD_AUDIO_REQUEST_CODE = 3 // 音乐
        const val ADD_CROP_REQUEST_CODE = 4 // 裁剪
        const val LOCAL_STICKER_REQUEST_CODE = 5 //本地贴纸
        const val LOCAL_STICKER_CROP_REQUEST_CODE = 6 //本地贴纸裁剪
        const val COVER_IMAGE_REQUEST_CODE = 7 // 选择图片封面
        const val REPLACE_VIDEO_REQUEST_CODE = 8 //为了替换素材而从相册选择素材
        const val CUSTOM_CANVAS_REQUEST_CODE = 9 //选择自定义画布
    }
}