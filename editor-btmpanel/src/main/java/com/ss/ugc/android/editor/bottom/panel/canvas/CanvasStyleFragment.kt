package com.ss.ugc.android.editor.bottom.panel.canvas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.EditorConfig
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListView
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.utils.Toaster
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.picker.mediapicker.PickType
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import kotlinx.android.synthetic.main.btm_panel_canvas_style.*
import java.io.File

class CanvasStyleFragment : BaseUndoRedoFragment<CanvasStyleViewModel>() {

    companion object {
        private const val MAX_SELECT_SIZE = 188743680L
        private const val MAX_SELECT_COUNT = 1
        private const val BLANK_CANVAS = ""
        private const val OTHER_CANVAS = "other"
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_canvas_style
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_canvas_style))
        initView()
        initListener()
    }

    private fun initView() {
        canvas_style_list?.apply {
            val config = ResourceViewConfig.Builder()
                .itemDecoration(FuncItemDecoration(0))
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        addNullItemInFirst = true,
                        nullItemResource = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterBgRes,
                        enableSelector = true,
                        nullItemIcon = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterIconRes
                    )
                )
                .panelKey(resourceConfig?.canvasPanel ?: DefaultResConfig.CANVAS_STYLE_PANEL)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        true,
                        56,
                        56,
                        selectorBorderRes = ThemeStore.getSelectBorderRes()
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        50,
                        50,
                        backgroundResource = R.drawable.transparent_image
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(false)
                )
                .customItemConfig(
                    CustomItemConfig(
                        addCustomItemInFirst = true,
                        customItemResource = ThemeStore.bottomUIConfig.filterPanelViewConfig.nullFilterBgRes,
                        onClearButtonClick = {
                            canvas_style_list.updateCustomItemIcon(BLANK_CANVAS)
                            viewModel.saveSlotCustomCanvas(BLANK_CANVAS)
                            viewModel.setCanvasStyle(BLANK_CANVAS)
                            selectItem(BLANK_CANVAS)
                        }
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    updateCustomItemIcon(viewModel.getSlotCustomCanvas())
                    selectCurrentCanvasStyle()
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    scrollToPosition(position)
                    if (position == 1) {
                        activity?.let {
                            val intent = if (EditorSDK.instance.config.videoSelector != null) {
                                EditorSDK.instance.config.videoSelector?.obtainAlbumIntent(
                                    it,
                                    EditorConfig.AlbumFunctionType.CANVAS
                                )
                            } else {
                                Intent(it, PickerActivity::class.java)
                            }
                            intent?.apply {
                                putExtra(
                                    PickerConfig.MAX_SELECT_SIZE,
                                    MAX_SELECT_SIZE
                                )
                                putExtra(
                                    PickerConfig.MAX_SELECT_COUNT,
                                    MAX_SELECT_COUNT
                                )
                                putExtra(PickerConfig.PICK_TYPE, PickType.ADD.type)
                                putExtra(
                                    PickerConfig.SELECT_MODE,
                                    PickerConfig.PICKER_IMAGE_EXCLUDE_GIF
                                )
                            }
                            it.startActivityForResult(
                                intent,
                                ActivityForResultCode.CUSTOM_CANVAS_REQUEST_CODE
                            )
                        }
                    } else {
                        item?.let {
                            val isBuildInResource =
                                EditorSDK.instance.config.resourceProvider?.isUseBuildInResource()
                                    ?: true
                            if (isBuildInResource) {
                                viewModel.setCanvasStyle(item.path)
                            } else {
                                val resPath = FileUtil.findImageFilePath(item.path) ?: return
                                viewModel.setCanvasStyle(resPath)
                            }
                            selectItem(item.path)
                        }
                    }
                }
            })
        }
    }

    private fun initListener() {
        viewModel.nleEditorContext.customCanvasImage.observe(this, Observer { customCanvas ->
            if (this.lifecycle.currentState == Lifecycle.State.RESUMED && !customCanvas.isNullOrBlank()) {
                canvas_style_list.updateCustomItemIcon(customCanvas)
                canvas_style_list.selectItem(customCanvas)
                viewModel.saveSlotCustomCanvas(customCanvas)
            }
        })
        viewModel.nleEditorContext.slotSelectChangeEvent.observe(this, Observer { selectedSlot ->
            if (this.lifecycle.currentState == Lifecycle.State.RESUMED && selectedSlot != null && selectedSlot.slot != null) {
                if (viewModel.selectSlotFromMainTrack(selectedSlot.slot!!)) {
                    canvas_style_list?.apply {
                        updateCustomItemIcon(viewModel.getSlotCustomCanvas())
                        selectCurrentCanvasStyle()
                    }
                }
            }
        })
        viewModel.nleEditorContext.closeCanvasPanelEvent.observe(this, Observer { close ->
            close?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED && close) {
                    pop()
                }
            }
        })
        apply_all_group.setOnClickListener {
            viewModel.applyCanvasToAllSlot()
            Toaster.show(this.getString(R.string.ck_has_apply_all))
        }
    }

    override fun provideEditorViewModel(): CanvasStyleViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(CanvasStyleViewModel::class.java)
    }

    override fun onUpdateUI() {
        canvas_style_list?.selectCurrentCanvasStyle()
    }

    private fun ResourceListView.selectCurrentCanvasStyle() {
        val isBuildInResource =
            EditorSDK.instance.config.resourceProvider?.isUseBuildInResource()
                ?: true
        val selectedPath = if (isBuildInResource) {
            viewModel.getSelectedSlotCanvasStyle()
        } else {
            viewModel.getSelectedSlotCanvasStyle()?.let {
                val canvasFile = File(it)
                canvasFile.parent ?: ""
            }
        }
        val isImageCanvas = selectedPath != null
        val isOriginCanvas = viewModel.isOriginCanvas()
        if (isImageCanvas && !isOriginCanvas) {
            selectedPath?.let {
                selectItem(it)
            }
        } else if (isOriginCanvas) {
            selectItem(BLANK_CANVAS)
        } else {
            selectItem(OTHER_CANVAS)
        }
    }
}