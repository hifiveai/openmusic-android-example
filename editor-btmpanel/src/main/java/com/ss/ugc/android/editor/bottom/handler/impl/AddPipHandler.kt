package com.ss.ugc.android.editor.bottom.handler.impl

import android.content.Intent
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.EditorConfig
import com.ss.ugc.android.editor.base.EditorSDK.Companion.instance
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.getSubTrackSize
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.picker.mediapicker.PickType
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig

/**
 * @date: 2021/3/30
 * @desc: 添加画中画
 */
class AddPipHandler(activity: FragmentActivity, @IdRes containerId: Int) :
    BaseFunctionHandler(activity, containerId) {

    private val MAX_SELECT_SIZE = 188743680L
    private val MAX_SELECT_COUNT = 1

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return funcItem.type == FunctionType.TYPE_CUT_PIP
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        if (nleEditor.nleModel.getSubTrackSize() >= Constants.MAX_SUB_VIDEO_LIMIT) {
            Toaster.show(activity.getString(R.string.ck_tips_too_many_pip_limit))
            return
        }
        val intent = if (instance.config.videoSelector != null) {
            instance.config.videoSelector?.obtainAlbumIntent(
                activity,
                EditorConfig.AlbumFunctionType.SUBVIDEOTRACK
            )
        } else {
            Intent(activity, PickerActivity::class.java)
        }
        intent?.apply {
            putExtra(PickerConfig.MAX_SELECT_SIZE, MAX_SELECT_SIZE) //default 180MB (Optional)
            putExtra(PickerConfig.MAX_SELECT_COUNT, MAX_SELECT_COUNT) //default 40 (Optional)
            putExtra(PickerConfig.PICK_TYPE, PickType.ADD.type)
            startActivityForResult(intent, ActivityForResultCode.PIP_VIDEO_REQUEST_CODE)

            ReportUtils.doReport(
                ReportConstants.VIDEO_IMPORT_CLICK_EVENT,
                mutableMapOf(
                    "type" to "pip"
                )
            )
        }
    }
}