package com.ss.ugc.android.editor.bottom.panel.mask

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.core.event.SelectSlotEvent
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.theme.resource.DownloadIconConfig
import com.ss.ugc.android.editor.base.theme.resource.FirstNullItemConfig
import com.ss.ugc.android.editor.base.theme.resource.ResourceImageConfig
import com.ss.ugc.android.editor.base.theme.resource.ItemSelectorConfig
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.Logger
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.view.ProgressBar
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.base.viewmodel.VideoMaskViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.R.drawable
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListView
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import kotlinx.android.synthetic.main.btm_fragment_video_mask.*

/**
 * 视频面板的底部栏
 */
class VideoMaskFragment : BaseUndoRedoFragment<VideoMaskViewModel>() {

    companion object {
        const val TAG = "VideoMaskFragment"
    }

    var rcyView: ResourceListView? = null

    // 羽化进度条
    private var featherPb: ProgressBar? = null
    private var featherTitle: TextView? = null
    private var tvMaskInvert: TextView? = null
    private var ivConfirm: ImageView? = null

    private val keyframeObserver: Observer<Long> = Observer {
        val feather = viewModel.getCurrentMaskFeather()
        featherPb?.progress = feather
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_fragment_video_mask
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val optPanelConfigure = ThemeStore.getOptPanelViewConfig()
        optPanelConfigure?.apply {
            if (closeIconDrawableRes != 0) {
                confirm.setImageResource(closeIconDrawableRes)
            }
        }
        viewModel.maskSegmentState.value = SelectSlotEvent(viewModel.getSelectedSlot()!!)
        viewModel.show()
        initView(view)

        viewModel.keyframeUpdateState.observe(this, keyframeObserver)
    }

    private fun initView(view: View) {
        rcyView = view.findViewById(R.id.rcv_mask)
        setPanelName(getString(R.string.ck_video_mask))
        ivConfirm = view.findViewById(R.id.confirm)
        tvMaskInvert = view.findViewById(R.id.tv_mask_invert)
        ivConfirm?.setOnClickListener {
            closeFragment()
        }
        tvMaskInvert?.setOnClickListener {
            val current = viewModel.getCurrentMaskInvert()
            viewModel.updateInvert(!current)
            // updateInvertUI(!current)
        }
        hideBottomBar()
        featherTitle = view.findViewById(R.id.tv_feature) as TextView
        featherPb = view.findViewById(R.id.pb_feather) as ProgressBar
        optPanelConfigure?.apply {
            if (slidingBarColor != 0) {
                featherPb?.setActiveLineColor(slidingBarColor)
            }
        }
        featherPb?.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
            if (isFormUser) {
                Logger.d(TAG, "setOnProgressChangedListener = $progress")
                viewModel.updateFeather(progress, eventAction == MotionEvent.ACTION_UP)
            }
        }

        rcyView?.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(
                    resourceConfig?.maskPanel
                        ?: DefaultResConfig.MASK_PANEL
                )
                .layoutManager(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        addNullItemInFirst = true,
                        nullItemResource = ThemeStore.bottomUIConfig.videoMaskPanelViewConfig.nullItemResource,
                        enableSelector = true
                    )
                )
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        enableSelector = true,
                        selectorWidth = 60,
                        selectorHeight = 60,
                        selectorBorderRes = ThemeStore.bottomUIConfig.videoMaskPanelViewConfig.selectorBorderRes
                    )
                )
                .resourceImageConfig(
                    ResourceImageConfig(
                        imageWidth = 60,
                        imageHeight= 60,
                        roundRadius = 8,
                        backgroundResource = ThemeStore.bottomUIConfig.videoMaskPanelViewConfig.backgroundResource,
                        resourcePlaceHolder = ThemeStore.bottomUIConfig.videoMaskPanelViewConfig.resourcePlaceHolder
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    onUpdateUI()
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    if (TextUtils.equals(item?.path ?: "", viewModel.getCurrentMask())) {
                        return
                    }

                    updateInvertVisible(position)
                    item?.let {
                        viewModel.updateMask(it)
                        selectItem(viewModel.getCurrentMask())
                        showProgressBar(position > 0)
                    }
                }
            })
        }
    }

    private fun updateInvertVisible(position: Int) {
        if (position <= 0) {
            if (tvMaskInvert?.visibility != View.INVISIBLE) {
                tvMaskInvert?.visibility = View.INVISIBLE
            }
        } else {
            if (tvMaskInvert?.visibility == View.INVISIBLE) {
                tvMaskInvert?.visibility = View.VISIBLE
            }
        }
    }

    private fun showProgressBar(showPb: Boolean) {
        featherPb?.visibility = if (showPb) View.VISIBLE else View.INVISIBLE
        featherTitle?.visibility = if (showPb) View.VISIBLE else View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        viewModel.show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.hide()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.hide()

    }

    override fun provideEditorViewModel(): VideoMaskViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(VideoMaskViewModel::class.java)

    }

    override fun onUpdateUI() {
        viewModel.getSelectedSlot()?.apply {
//            var curPos = viewModel.getCurrentMaskIndex()
            rcyView?.getResourceListAdapter()?.resourceList?.let { list ->
                val curPos = viewModel.getFetchResIndex(list)
                val feather = viewModel.getCurrentMaskFeather()
                val invert = viewModel.getCurrentMaskInvert()
                showProgressBar(curPos > 0)
                featherPb?.progress = feather
                rcyView?.selectItem(viewModel.getCurrentMask())?.let { index->
                    updateInvertVisible(index)
                }
                rcyView?.getRecyclerView()?.scrollToPosition(curPos)
                // updateInvertUI(invert)
            }
        } ?: closeFragment()
    }


    private fun updateInvertUI(invert: Boolean) {
        val drawableID = when (invert) {
            true -> getSelectDrawable()
            else -> getNormalDrawable()
        }
        tvMaskInvert?.setCompoundDrawablesWithIntrinsicBounds(drawableID, null, null, null)
    }

    private fun getNormalDrawable(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(Color.parseColor("#00000000"))
        gradientDrawable.setStroke(5, Color.WHITE)
        val widthPixels = UIUtils.dp2px(requireContext(), 13)
        gradientDrawable.setSize(widthPixels, widthPixels)
        return gradientDrawable
    }

    private fun getSelectDrawable(): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        val themeColor = ContextCompat.getColor(requireContext(), ThemeStore.globalUIConfig.themeColorRes)
        gradientDrawable.setColor(themeColor)
        gradientDrawable.setStroke(5, Color.WHITE)
        val widthPixels = UIUtils.dp2px(requireContext(), 13)
        gradientDrawable.setSize(widthPixels, widthPixels)
        return gradientDrawable
    }

}
