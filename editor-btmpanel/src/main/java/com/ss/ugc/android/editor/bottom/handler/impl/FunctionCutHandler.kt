package com.ss.ugc.android.editor.bottom.handler.impl

import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.IFunctionManager
import com.ss.ugc.android.editor.base.utils.runOnUiThread
import com.ss.ugc.android.editor.base.view.export.WaitingDialog
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.videoeffect.BlendModeFragment
import com.ss.ugc.android.editor.bottom.viewmodel.CutViewModel
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.api.video.IReverseListener
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @date: 2021/3/29
 */
class FunctionCutHandler(
    activity: FragmentActivity,
    containerId: Int,
    private val functionManager: IFunctionManager
) : BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.TYPE_CUT_SPLIT,
        FunctionType.TYPE_CUT_DELETE,
        FunctionType.TYPE_CUT_REPLACE,
        FunctionType.TYPE_CUT_COPY,
        FunctionType.TYPE_CUT_CROP,
        FunctionType.TYPE_CUT_ROTATE,
        FunctionType.TYPE_CUT_MIRROR,
        FunctionType.TYPE_CUT_REVERSE,
        FunctionType.TYPE_CUT_BLENDMODE,
        FunctionType.TYPE_CUT_FREEZE
    )

    private val viewModel by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(CutViewModel::class.java)
    }

    private val editorContext by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(NLEEditorContext::class.java)
    }

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.TYPE_CUT_BLENDMODE -> { //混合模式
                editorContext.videoPlayer.pause()
                showFragment(BlendModeFragment())
            }
            FunctionType.TYPE_CUT_SPLIT -> {
                viewModel.splitClip()
            }
            FunctionType.TYPE_CUT_DELETE -> {
                viewModel.deleteClip()
            }
            FunctionType.TYPE_CUT_REPLACE -> {
                viewModel.slotReplace()
            }
            FunctionType.TYPE_CUT_COPY -> {
                viewModel.slotCopy()
            }
            FunctionType.TYPE_CUT_ROTATE -> {
                viewModel.rotate()
            }
            FunctionType.TYPE_CUT_MIRROR -> {
                viewModel.mirror()
            }
            FunctionType.TYPE_CUT_CROP -> {
                viewModel.crop()
            }

            FunctionType.TYPE_CUT_FREEZE -> {
                GlobalScope.launch(Dispatchers.Main) {
                    showProgress(true)
                    withContext(Dispatchers.IO) {
                        viewModel.freezeFrame()
                    }
                    showProgress(false)
                }
            }

            FunctionType.TYPE_CUT_REVERSE -> {
                //倒放
                viewModel.reversePlay(object : IReverseListener {

                    var dialog: WaitingDialog? = null

                    override fun onReverseStart() {
                        if (!activity.isFinishing) {
//                            runOnUiThread {
                            dialog = WaitingDialog(activity).also {
                                // 设置ProgressDialog 标题
                                it.setTitle(activity.getString(R.string.ck_reversing_video))
                                // 设置ProgressDialog 提示信息
                                // 设置ProgressDialog 是否可以按退回按键取消
                                it.setCancelable(true)
                                it.setProgress1(activity.getString(R.string.ck_reversing), 0f)
                                it.setOnCancelListener { viewModel.cancelReverse() }
                                it.show()
                            }
                        }
                    }

                    override fun onReverseDone(ret: Int) {
                        runOnUiThread {
                            if (dialog != null) {
                                dialog!!.dismiss()
                                functionManager.disableFuncItem(FunctionType.TYPE_CUT_VOLUME) //置灰音量键
                                functionManager.disableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE)
                                Toaster.show(activity.getString(R.string.ck_reverse_play_success))
                            }
                        }
                    }

                    override fun onReverseCancel(isRewind: Boolean) {
                        if (isRewind) {
                            functionManager.disableFuncItem(FunctionType.TYPE_CUT_VOLUME) //置灰音量键
                            functionManager.disableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE)
                            Toaster.show(activity.getString(R.string.ck_reverse_play_success))
                        } else {
                            functionManager.enableFuncItem(FunctionType.TYPE_CUT_VOLUME) //置灰音量键
                            functionManager.enableFuncItem(FunctionType.TYPE_CUT_CHANGE_VOICE)
                            Toaster.show(activity.getString(R.string.ck_reverse_play_cancel))
                        }
                    }

                    override fun onReverseProgress(progress: Double) {
                        runOnUiThread {
                            if (dialog != null) {
                                dialog!!.setProgress1(
                                    activity.getString(R.string.ck_reversing),
                                    progress.toFloat()
                                )
                            }
                        }
                    }

                    override fun onReverseFailed(errorCode: Int, errorMsg: String) {
                        runOnUiThread {
                            Toaster.show(activity.getString(R.string.ck_reverse_video_failed))
                            if (dialog != null) {
                                dialog!!.dismiss()
                            }
                        }
                    }
                })
            }
        }
    }

}