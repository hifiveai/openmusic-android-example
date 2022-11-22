package com.ss.ugc.android.editor.bottom.panel.sticker.image

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.StickerGestureViewModel
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.resourceview.*
import com.ss.ugc.android.editor.core.api.sticker.InfoStickerParam
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig.*
import kotlinx.android.synthetic.main.btm_panel_image_sticker.*
import kotlin.random.Random

/**
 * time : 2020/12/20
 *
 * description :
 * 贴图贴纸
 */
class ImageStickerFragment : BasePanelFragment<StickerViewModel>() {
    private val stickerViewModel by lazy {
        EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
    }
    private lateinit var stickerGesture: StickerGestureViewModel

    companion object {
        private const val MAX_SELECT_SIZE = 188743680L
        private const val MAX_SELECT_COUNT = 1
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_image_sticker
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPanelName(getString(R.string.ck_sticker))
        stickerGesture =
            EditViewModelFactory.viewModelProvider(this).get(StickerGestureViewModel::class.java)
        stickerGesture.stickerPanelVisibility.value = true
        initView()
    }

    private fun initView() {
        rcv.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(
                    resourceConfig?.stickerPanel ?: DefaultResConfig.STICKER_PANEL
                )
                .layoutManager(GridLayoutManager(context, 4))
                .itemDecoration(ItemSpaceDecoration(4, UIUtils.dp2px(context, 20.0f), false))
                .nullItemInFirstConfig(
                    if (EditorSDK.instance.config.enableLocalSticker) {
                        FirstNullItemConfig(
                            true,
                            R.drawable.bg_image_sticker,
                            nullItemIcon = R.drawable.ic_custom_sticker
                        )
                    } else {
                        FirstNullItemConfig(false)
                    }
                )
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorWidth = 74,
                        selectorHeight = 74,
                        iconPadding = 1,
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 70,
                        imageHeight = 70,
                        roundRadius = ThemeStore.getResourceItemRoundRadius(),
                        backgroundResource = R.drawable.bg_image_sticker,
                        resourcePlaceHolder = R.drawable.filter_place_holder
                    )
                )
                .resourceTextConfig(ResourceTextConfig(false))
                .build()
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    if (activity == null) {
                        // 资源下载成功后，回调界面可能已经关闭，导致crash
                        DLog.e("ImageStickerFragment not attached to an activity")
                        return
                    }
                    getRecyclerView()?.findViewHolderForAdapterPosition(position)?.let {
                        if (EditorSDK.instance.config.enableLocalSticker && position == 0) {
                            activity?.let {
                                if (EditorSDK.instance.config.videoSelector != null) {
                                    EditorSDK.instance.config.videoSelector?.startLocalStickerSelector(
                                        it
                                    )
                                } else {
                                    val intent = Intent(it, PickerActivity::class.java).apply {
                                        putExtra(PickerConfig.MAX_SELECT_SIZE, MAX_SELECT_SIZE)
                                        putExtra(
                                            PickerConfig.MAX_SELECT_COUNT,
                                            MAX_SELECT_COUNT
                                        )
                                        putExtra(SELECT_MODE, PICKER_IMAGE_EXCLUDE_GIF)
                                    }
                                    it.startActivityForResult(
                                        intent,
                                        ActivityForResultCode.LOCAL_STICKER_REQUEST_CODE
                                    )
                                }
                                ReportUtils.doReport(
                                    ReportConstants.VIDEO_EDIT_LOCAL_STICKER_CLICK_EVENT,
                                    mutableMapOf()
                                )
                            }
                        } else {
                            item?.let {

                                //  随机生产初始坐标，防止连续添加贴纸堆积
                                // 这里要转换NLE 坐标， 转换后等价中心点  x ：[-0.3,0.3] y：[-0.20,0.20]
                             val transformX = Random.nextDouble(0.2, 0.8)
                                val transformY = Random.nextDouble(0.30, 0.7)
                                stickerViewModel.applyInfoSticker(
                                    item,
                                    InfoStickerParam(pos_x = transformX.toFloat(), pos_y = transformY.toFloat())
                                )
                            }
                            it.itemView.post {
                                stickerViewModel.setStickerDefaultTime()
                            }
//                        }
                        }
                    }

                    if (position != 0) {
                        selectItem(item!!.path)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stickerGesture.stickerPanelVisibility.value = false
    }

    override fun provideEditorViewModel(): StickerViewModel {
        return stickerViewModel
    }
}