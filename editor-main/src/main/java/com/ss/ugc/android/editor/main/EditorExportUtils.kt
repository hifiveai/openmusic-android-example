package com.ss.ugc.android.editor.main

import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils.doReport
import com.ss.ugc.android.editor.base.monitior.ReportUtils.frameRate
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getCanvasScale
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getCutReverse
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getCutSpeed
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getEffectIds
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getFilterIds
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getMainVideoCnt
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getMainVideoDuration
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getMusicCnt
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getMusicName
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getRotate
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getStickerCnt
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getStickerIds
import com.ss.ugc.android.editor.base.monitior.ReportUtils.getTextCnt
import com.ss.ugc.android.editor.base.monitior.ReportUtils.resolutionRate
import java.util.*

object EditorExportUtils {

    fun reportExportVideoFinishedEvent(success: Boolean, failCode: Int, failMsg: String) {
        val params: MutableMap<String, String> = HashMap()
        params["video_duration"] = getMainVideoDuration().toString() + ""
        params["video_cnt"] = getMainVideoCnt().toString() + ""
        params["cut_speed"] = getCutSpeed()
        params["cut_reverse"] = getCutReverse()
        params["rotate"] = getRotate()
        params["text_cnt"] = getTextCnt().toString() + ""
        params["sticker_cnt"] = getStickerCnt().toString() + ""
        params["sticker_id"] = getStickerIds() + ""
        params["effect_id"] = getEffectIds() + ""
        params["filter_id"] = getFilterIds() + ""
        params["music_cnt"] = getMusicCnt().toString() + ""
        params["music_name"] = getMusicName()
        params["resolution_rate"] = resolutionRate.toString() + ""
        params["frame_rate"] = frameRate.toString() + ""
        params["canvas_scale"] = getCanvasScale()
        params["screen"] = "vertical_screen"
        if (success) {
            params["result"] = "success"
        } else {
            params["result"] = "fail"
            params["fail_code"] = failCode.toString() + ""
            params["fail_msg"] = failMsg
        }
        doReport(ReportConstants.VIDEO_EDIT_PUBLISH_RESULT_EVENT, params)
    }

    fun reportExportClickEvent() {
        val params: MutableMap<String, String> = HashMap()
        params["video_duration"] = getMainVideoDuration().toString() + ""
        params["video_cnt"] = getMainVideoCnt().toString() + ""
        params["cut_speed"] = getCutSpeed()
        params["cut_reverse"] = getCutReverse()
        params["rotate"] = getRotate()
        params["text_cnt"] = getTextCnt().toString() + ""
        params["sticker_cnt"] = getStickerCnt().toString() + ""
        params["sticker_id"] = getStickerIds() + ""
        params["effect_id"] = getEffectIds() + ""
        params["filter_id"] = getFilterIds() + ""
        params["music_cnt"] = getMusicCnt().toString() + ""
        params["music_name"] = getMusicName()
        params["resolution_rate"] = resolutionRate.toString() + ""
        params["frame_rate"] = frameRate.toString() + ""
        params["canvas_scale"] = getCanvasScale()
        params["screen"] = "vertical_screen"
        doReport(ReportConstants.VIDEO_EDIT_PUBLISH_CLICK_EVENT, params)
    }

}