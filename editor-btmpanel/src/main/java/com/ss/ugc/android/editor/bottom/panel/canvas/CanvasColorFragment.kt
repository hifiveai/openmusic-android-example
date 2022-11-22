package com.ss.ugc.android.editor.bottom.panel.canvas

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.SimpleResourceListener
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.weight.colorselect.OnColorSelectedListener
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_canvas_color.*

class CanvasColorFragment : BaseUndoRedoFragment<CanvasColorViewModel>() {

    override fun getContentViewLayoutId() = R.layout.btm_panel_canvas_color

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_canvas_color))

        fetchColorList()

        initListener()
    }

    private fun initListener() {
        canvas_color_list?.setColorSelectedListener(object : OnColorSelectedListener {
            override fun onColorSelected(colorItem: ResourceItem?) {
                colorItem?.let {
                    viewModel.setCanvasColor(it.color)
                }
            }
        })

        apply_all_color_group.setOnClickListener {
            viewModel.applyCanvasToAllSlot()
            Toaster.show(this.getString(R.string.ck_has_apply_all))
        }

        viewModel.nleEditorContext.closeCanvasPanelEvent.observe(this, Observer { close ->
            close?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED && close) {
                    pop()
                }
            }
        })
    }

    private fun fetchColorList() {
        resourceProvider?.fetchTextList("color", object : SimpleResourceListener<ResourceItem> {
            override fun onSuccess(dataList: MutableList<ResourceItem>) {
                canvas_color_list?.setColorList(dataList, withNoneAtFirst = false)
            }
        })
    }

    override fun provideEditorViewModel(): CanvasColorViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(CanvasColorViewModel::class.java)
    }

    override fun onUpdateUI() {
    }
}