package com.ss.ugc.android.editor.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.ies.nle.editor_jni.NLEModel
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.core.utils.clickWithTrigger

/**
 * @date: 2021/3/30
 */
open abstract class BasePanelFragment<VM : BaseEditorViewModel> : BaseFragment() {

    private var panelTextView: TextView? = null
    protected var bottomBarLayout: View? = null
    protected val optPanelConfigure = ThemeStore.getOptPanelViewConfig()
    protected val resourceProvider = EditorSDK.instance.config.resourceProvider
    protected val resourceConfig = EditorSDK.instance.config.resourceProvider?.resourceConfig
    private var initNLEModel: NLEModel? = null

    protected val viewModel: VM by lazy {
        provideEditorViewModel()
    }

    override fun getContentView(): Int {
        return R.layout.btm_panel_container
    }

    abstract fun provideEditorViewModel(): VM

    override fun initView(view: View, inflater: LayoutInflater) {
        val panelContainer = view.findViewById<FrameLayout>(R.id.opt_panel_container)
        if (getContentViewLayoutId() <= 0) {
            throw IllegalStateException("contentViewLayoutId is invalid.")
        }
        if (context == null) {
            return
        }
        inflater.inflate(getContentViewLayoutId(), panelContainer, true)
        panelTextView = view.findViewById(R.id.tv_panel_name)
        bottomBarLayout = view.findViewById(R.id.rl_show_container)
        val ivPanelClose = view.findViewById<ImageView>(R.id.iv_panel_close)
        optPanelConfigure?.apply {
            if (closeIconDrawableRes != 0) {
//                ivPanelClose.setImageDrawable(context!!.getDrawable(closeIconDrawableRes))
                ivPanelClose.setImageResource(closeIconDrawableRes)
            }
            if (panelNameTextViewColor != 0) {
                panelTextView!!.setTextColor(context!!.resources.getColor(panelNameTextViewColor))
            }
            if (panelNameTextViewSize != 0) {
                panelTextView!!.textSize = panelNameTextViewSize.toFloat()
            }
        }
        ivPanelClose.clickWithTrigger {
            closeFragment()
        }
    }

    fun closeFragment() {
        pop()
    }

    fun setPanelName(name: String) {
        panelTextView?.text = name
    }

    fun hideBottomBar() {
        bottomBarLayout?.visibility = View.GONE
    }

    abstract fun getContentViewLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (configAutoCommitDone()) {
            initNLEModel = viewModel.nleEditorContext.nleEditor.stageModel
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (configAutoCommitDone()) {
            checkDoneOnDestroy()
        }
    }


    open fun checkDoneOnDestroy() {
        if (viewModel.isDirty(initNLEModel)) {
            viewModel.nleEditorContext.done()
        }
    }

    /**
     * 是否需要在退出时自动commitDone生成撤销重做操作
     * 理论上一个面板所有操作都是commit，只有最终退出时调用Done使之仅生成一个撤销重做记录
     * node:如果有关键帧可以中间自行调用done
     */
    open fun configAutoCommitDone(): Boolean {
        return false
    }
}