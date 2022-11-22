package com.ss.ugc.android.editor.base.monitior


class ReportConstants {
    companion object {
        //视频剪辑页展现
        const val VIDEO_EDIT_PAGE_SHOW_EVENT = "video_edit_page_show"

        //视频剪辑工具点击
        const val ROOT_FUNCTION_ITEM_CLICKED_EVENT = "video_edit_tools_click"

        //视频剪辑工具剪辑功能点击
        const val CUT_FUNCTION_ITEM_CLICKED_EVENT = "video_edit_tools_cut_click"

        //视频剪辑导入点击
        const val VIDEO_IMPORT_CLICK_EVENT = "video_edit_import_click"

        //视频剪辑导入完成点击
        const val VIDEO_IMPORT_COMPLETE_CLICK_EVENT = "video_edit_import_complete_click"

        //视频剪辑音频展现
        const val AUDIO_SHOW_EVENT = "video_edit_audio_show"

        //视频剪辑音频点击
        const val AUDIO_CLICK_EVENT = "video_edit_audio_click"

        //视频剪辑音频tab进入
        const val AUDIO_TAB_ENTER_EVENT = "video_edit_audio_tab_enter"

        //视频发布点击
        const val VIDEO_EDIT_PUBLISH_CLICK_EVENT = "video_edit_publish_click"

        //视频编辑发布结果
        const val VIDEO_EDIT_PUBLISH_RESULT_EVENT = "video_edit_publish_result"

        //返回弹窗草稿保存提示（取消、确定）点击
        const val VIDEO_EDIT_BACK_CLICK_EVENT = "video_edit_back_click"

        //转场面板展示（视频间转场点击）
        const val VIDEO_EDIT_TRANS_SHOW_EVENT = "video_edit_trans_show"

        //录音开始、录音完成点击
        const val VIDEO_EDIT_DUB_CLICK_EVENT = "video_edit_dub_click"

        //本地贴纸入口点击
        const val VIDEO_EDIT_LOCAL_STICKER_CLICK_EVENT = "video_edit_local_sticker_click"

        //语音识别（弹窗）上功能点击
        const val VIDEO_EDIT_AI_CAPTION_CLICK_EVENT = "video_edit_ai_caption_click"

        //文字样式面板展示
        const val VIDEO_EDIT_TEXT_STYLE_SHOW_EVENT = "video_edit_text_style_show"

        //视频：亮度、对比度、色温、饱和度点击
        const val VIDEO_EDIT_CONFIG_CLICK_EVENT = "video_edit_config_click"

    }
}
