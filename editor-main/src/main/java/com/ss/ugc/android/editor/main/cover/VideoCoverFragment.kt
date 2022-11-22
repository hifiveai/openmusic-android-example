package com.ss.ugc.android.editor.main.cover

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.ss.ugc.android.editor.base.constants.ActivityForResultCode
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.viewmodel.PreviewStickerViewModel
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment
import com.ss.ugc.android.editor.core.event.PanelEvent
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_FROM_MULTI_SELECT
import com.ss.ugc.android.editor.main.EditorHelper.EXTRA_KEY_FROM_TYPE
import com.ss.ugc.android.editor.main.R
import com.ss.ugc.android.editor.picker.mediapicker.PickerActivity
import com.ss.ugc.android.editor.picker.mediapicker.PickerConfig
import kotlinx.android.synthetic.main.btm_panel_cover.*
import kotlinx.android.synthetic.main.include_editor_top_bar.*

class VideoCoverFragment : BaseUndoRedoFragment<VideoCoverViewModel>() {

    companion object {
        private const val COVER_TYPE = "cover_type"
        private const val CONTAINER_ID = "container_id"
        private const val VIDEO_FRAME = "VIDEO_FRAME"
        private const val IMAGE = "IMAGE"
        private const val COVER_MODE = "cover_mode"
        const val DRAFT_RESTORE = 2
        const val TEMPLATE_COVER = 4

        @JvmStatic
        fun newInstance(coverType: String, @IdRes containerId: Int, type: Int): VideoCoverFragment {
            val videoCoverFragment = VideoCoverFragment()
            val args = Bundle().apply {
                putString(COVER_TYPE, coverType)
                putInt(CONTAINER_ID, containerId)
                putInt(EXTRA_KEY_FROM_TYPE, type)
            }
            videoCoverFragment.arguments = args
            return videoCoverFragment
        }
    }

    private var isEditFromTemplate: Boolean = false

    private val frameCoverFragment by lazy {
        Fragment.instantiate(requireContext(), FrameCoverFragment::class.java.canonicalName!!)
    }

    private val imageCoverFragment by lazy {
        Fragment.instantiate(requireContext(), ImageCoverFragment::class.java.canonicalName!!)
    }

    private var fragmentHelper: FragmentHelper? = null

    private var previewStickerViewModel: PreviewStickerViewModel? = null

    override fun getContentViewLayoutId() = R.layout.btm_panel_cover

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        enable_template_setting_tv?.visibility = View.GONE

        super.onViewCreated(view, savedInstanceState)
        hideBottomBar()
        previewStickerViewModel = activity?.let {
            EditViewModelFactory.viewModelProvider(it).get(PreviewStickerViewModel::class.java)
        }
        arguments?.getInt(CONTAINER_ID)?.let {
            fragmentHelper = FragmentHelper(it).bind(activity)
        }
        viewModel.updateTextStickersDuration()
        initView()
        initListener()
        initData()
    }

    private fun initData() {
        val type = arguments?.getInt(EXTRA_KEY_FROM_TYPE, EXTRA_FROM_MULTI_SELECT)
            ?: EXTRA_FROM_MULTI_SELECT
        if (type == DRAFT_RESTORE && !viewModel.isStickerLoaded) {
            previewStickerViewModel?.restoreInfoSticker()
            viewModel.isStickerLoaded = true
        }
        if (type == TEMPLATE_COVER) {
            isEditFromTemplate = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        val fragmentList = arrayListOf(frameCoverFragment, imageCoverFragment)
        val tabList = arrayListOf(getString(R.string.ck_video_frame), getString(R.string.ck_import_from_album))

        cover_vp.apply {
            setDisableSwiping(true)
            setSmoothScroll(false)
            adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(p0: Int) = fragmentList[p0]

                override fun getCount() = 2

                override fun getPageTitle(position: Int) = tabList[position]
            }
            cover_tab.setupWithViewPager(this)
            val tab = cover_tab.getTabAt(1)
            tab?.let {
                val view = it.view as? LinearLayout
                view?.let { tabView ->
                    tabView.setOnTouchListener { v, event ->
                        if (viewModel.hasImageCover()) {
                            return@setOnTouchListener false
                        } else {
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                startSelectImageCover()
                            }
                            return@setOnTouchListener true
                        }
                    }
                }
            }

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(p0: Int) {
                }

                override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                }

                override fun onPageSelected(p0: Int) {
                    if (p0 == 0) {
                        viewModel.updateCoverType(VIDEO_FRAME)
                    } else {
                        viewModel.updateCoverType(IMAGE)
                    }
                }
            })
        }

        val coverType = arguments?.getString(COVER_TYPE)
        coverType?.let {
            if (it == VIDEO_FRAME) {
                cover_vp.setCurrentItem(0, false)
            } else {
                cover_vp.setCurrentItem(1, false)
            }
        }
    }

    private fun startSelectImageCover() {
        val intent = Intent(activity, PickerActivity::class.java)
        val maxSize = 188743680L //long long long long类型
        intent.putExtra(
            PickerConfig.MAX_SELECT_SIZE,
            maxSize
        ) //default 180MB (Optional)
        intent.putExtra(
            PickerConfig.MAX_SELECT_COUNT,
            1
        ) //default 40 (Optional)
        intent.putExtra(
            PickerConfig.SELECT_MODE,
            PickerConfig.PICKER_IMAGE_EXCLUDE_GIF
        )
        activity?.startActivityForResult(
            intent,
            ActivityForResultCode.COVER_IMAGE_REQUEST_CODE
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        video_cover.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        viewModel.nleEditorContext.panelEvent.observe(
            viewLifecycleOwner,
            Observer<PanelEvent> { panelEvent ->
                panelEvent?.let {
                    val isCloseNow =
                        this.lifecycle.currentState == Lifecycle.State.RESUMED &&
                            ((it.panel == PanelEvent.Panel.COVER && it.state == PanelEvent.State.CLOSE) ||
                                (it.panel == PanelEvent.Panel.TEMPLATE_COVER && it.state == PanelEvent.State.CLOSE))
                    if (isCloseNow) {
                        viewModel.nleEditorContext.nleEditor.resetHead()
                        viewModel.nleEditorContext.nleModel.cover.enable = false
                        viewModel.nleEditorContext.commit()
                        closeFragment()
                        viewModel.nleEditorContext.closeCoverTextPanelEvent.postValue(true)
                    }
                }
            })

        viewModel.nleEditorContext.resetCoverEvent.observe(viewLifecycleOwner) { reset ->
            reset?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED && it) {
                    viewModel.resetCoverModel()
                    (frameCoverFragment as? FrameCoverFragment)?.seekToOriginalPosition()
                    (imageCoverFragment as? ImageCoverFragment)?.resetImageCover()
                    cover_vp.setCurrentItem(0, false)
                }
            }
        }

        viewModel.nleEditorContext.saveCoverEvent.observe(viewLifecycleOwner) { save ->
            save?.let {
                if (this.lifecycle.currentState == Lifecycle.State.RESUMED &&
                    ((it.panel == PanelEvent.Panel.COVER && it.state == PanelEvent.State.SAVE) ||
                        (it.panel == PanelEvent.Panel.TEMPLATE_COVER && it.state == PanelEvent.State.SAVE))
                ) {
                    viewModel.saveCoverModel()
                    closeFragment()
                }
            }
        }

        viewModel.nleEditorContext.imageCoverInfo.observe(viewLifecycleOwner) { imageCover ->
            imageCover?.let {
                activity?.let { hostActivity ->
                    val selectImageTab =
                        (imageCoverFragment as? ImageCoverFragment)?.updateImageCover(
                            hostActivity,
                            imageCover
                        ) ?: false
                    if (selectImageTab) {
                        cover_vp.currentItem = 1
                    }
                }
            }
        }

        cover_add_text.setOnClickListener {
            val textStickerFragment = TextStickerFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(COVER_MODE, true)
                }
            }
            viewModel.unSelectCurrentTextSticker()
            fragmentHelper?.startFragment(textStickerFragment)
        }
    }

    override fun provideEditorViewModel() = EditViewModelFactory.viewModelProvider(this)
        .get(VideoCoverViewModel::class.java)

    override fun onUpdateUI() {
    }

    override fun onDestroy() {
        super.onDestroy()
        enable_template_setting_tv?.visibility = View.VISIBLE
    }
}
