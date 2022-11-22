package com.ss.ugc.android.editor.picker.selector.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.picker.selector.config.SelectorViewConfig
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectModel

/**
 * 选择View
 */
class MaterialSelectorView(
    private val root: ViewGroup,
    private val lifecycleOwner: LifecycleOwner,
    private val materialSelectModel: MaterialSelectModel,
    private val selectorViewConfig: SelectorViewConfig? = null,
    private val selectorMeasureListener: ((height: Int) -> Unit),
    private val confirmClickListener: (() -> Unit),
    private val addClickListener: (() -> Unit)? = null,
) {
    private lateinit var contentView: ViewGroup
    private lateinit var confirmTV: TextView
    private lateinit var addTV: TextView
    private lateinit var selectCountTv: TextView

    private var viewType: SelectViewType = SelectViewType.CONFIRM //默认为提交功能类型

    fun init() {
        initContentView(root)
        initObserver()
    }

    private fun initObserver() {
        materialSelectModel.apply {
            selectCount.observe(lifecycleOwner, { count ->
                count?.let {
                    if (count == 0) {
                        selectCountTv.visibility = View.GONE
                    } else {
                        selectCountTv.visibility = View.VISIBLE
                        selectCountTv.text = String.format(
                            root.context.getString(R.string.ck_selector_select_count),
                            count
                        )
                    }
                }
            })

            enableConfirm.observe(lifecycleOwner, { enableConfirm ->
                //控制相册的「开始创作」按钮的样式
                if (enableConfirm == false) {
                    confirmTV.isEnabled = false
                    confirmTV.background =
                        root.resources.getDrawable(R.drawable.bg_finish_select_disable_btn, null)
                } else {
                    confirmTV.isEnabled = true
                    confirmTV.background =
                        root.resources.getDrawable(R.drawable.bg_finish_select_enable_btn, null)
                }
            })
        }
    }

    private fun initContentView(root: ViewGroup) {
        contentView = LayoutInflater.from(root.context)
            .inflate(R.layout.layout_material_selector, root, true) as ViewGroup
        confirmTV = contentView.findViewById(R.id.confirmTV)
        addTV = contentView.findViewById(R.id.addTV)
        selectCountTv = contentView.findViewById(R.id.selectCountTv)

        selectorViewConfig?.finishBtnText?.apply {
            confirmTV.text = this
        }
        confirmTV.setOnClickListener {
            confirmClickListener()
        }
        addTV.setOnClickListener {
            addClickListener?.invoke()
        }
        contentView.post {
            selectorMeasureListener(contentView.height)
        }
    }

    fun changeViewType(type: SelectViewType) {
        if (viewType == type) {
            return
        }

        viewType = type
        when (type) {
            SelectViewType.CONFIRM -> {
                confirmTV.visibility = View.VISIBLE
                addTV.visibility = View.GONE
            }
            SelectViewType.ADD -> {
                confirmTV.visibility = View.GONE
                addTV.visibility = View.VISIBLE
            }
        }
    }
}