package com.ss.ugc.android.editor.bottom.panel.sticker.image

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.ies.nle.editor_jni.NLEResType
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.NLEStyStickerAnim
import com.ss.ugc.android.editor.base.event.InfoStickerOperationType
import com.ss.ugc.android.editor.base.fragment.BasePanelFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.ResourceType
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.base.view.MutexSeekBar
import com.ss.ugc.android.editor.base.view.OnSliderChangeListener
import com.ss.ugc.android.editor.base.viewmodel.BaseEditorViewModel
import com.ss.ugc.android.editor.base.viewmodel.StickerViewModel
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.bottom.panel.sticker.text.*
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_panel_sticker_anim.*
import kotlin.math.min
import kotlin.math.roundToInt

class StickerAnimFragment : BasePanelFragment<BaseEditorViewModel>() {

    companion object {
        const val DEFAULT_IN_DURATION = 500   // 毫秒
        const val DEFAULT_OUT_DURATION = 500  // 毫秒
        const val DEFAULT_LOOP_DURATION = 600 // 毫秒

//        const val DEFAULT_MAX_LOOP_DURATION = 5000 // 毫秒

        const val MODE_ANIM_IN = 1
        const val MODE_ANIM_OUT = 2
        const val MODE_ANIM_LOOP = 3
    }

    private var isStickerViewModelInit: Boolean = false
    private val stickerViewModel: StickerViewModel by lazy {
        EditViewModelFactory.viewModelProvider(this).get(StickerViewModel::class.java)
    }
    private lateinit var stickerUI: StickerUIViewModel
    private var currLoopPosition = DEFAULT_LOOP_DURATION
    private var currInPosition = DEFAULT_IN_DURATION
    private var currOutPosition = DEFAULT_OUT_DURATION

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_panel_sticker_anim
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setPanelName(getString(R.string.ck_sticker_animation))
        stickerUI = EditViewModelFactory.viewModelProvider(this).get(StickerUIViewModel::class.java)
        isStickerViewModelInit = true
        init()
        initTabByNLETrackSlot()
        updateUIByNLETrackSlot()
    }

    override fun onPause() {
        super.onPause()
        stopStickerAnimationPreview()
    }

    private fun init() {
        //监听preview里边点击x删除贴纸
        stickerUI.infoStickerOperation.observe(this, Observer {
            it?.apply {
                when (operation) {
                    InfoStickerOperationType.DELETE -> {
                        closeFragment()
                    }
                }
            }
        })

        animGroup.setOnCheckedChangeListener { _, checkedId ->
            resetRecyclerView()
            var mode = MODE_ANIM_IN
            when (checkedId) {
                R.id.rb_in -> {
                    recycler_animation_in.visibility = View.VISIBLE
                    mode = MODE_ANIM_IN
                }

                R.id.rb_out -> {
                    recycler_animation_out.visibility = View.VISIBLE
                    mode = MODE_ANIM_OUT
                }

                R.id.rb_loop -> {
                    recycler_animation_loop.visibility = View.VISIBLE
                    mode = MODE_ANIM_LOOP
                }
            }
            // 切换到对应的Tab之后会判断当前Tab是否有动画
            // 如果有就启动预览，如果没有就停止动画的预览
            startStickerAnimationPreview(mode)
            updateUIByNLETrackSlot()
        }

        bar_inout_duration.apply {
            setOnIndicatorChangedListener(object :
                MutexSeekBar.OnIndicatorChangedListener {
                override fun onLeftIndicatorChange(leftIndicator: Int) {
                }

                override fun onRightIndicatorChange(rightIndicator: Int) {
                }

                override fun getLeftIndicatorText(leftIndicator: Int): String {
                    val s = leftIndicator / 1000
                    val ms = ((leftIndicator % 1000) / 100f).roundToInt()
                    return "$s.${ms}s"
                }

                override fun getRightIndicatorText(rightIndicator: Int): String {
                    val s = rightIndicator / 1000
                    val ms = ((rightIndicator % 1000) / 100f).roundToInt()
                    return "$s.${ms}s"
                }

                override fun onActionUp(touchArea: MutexSeekBar.TouchArea, indicatorValue: Int) {
                    when (touchArea) {
                        MutexSeekBar.TouchArea.LEFT_INDICATOR -> {
                            currInPosition = indicatorValue
                            stickerViewModel.curInfoOrImageSticker()?.animation?.let {
                                it.inDuration = indicatorValue * 1000
                            }
                            stickerViewModel.updateInfoOrImageSticker()
                            startStickerAnimationPreview(1)
                        }
                        MutexSeekBar.TouchArea.RIGHT_INDICATOR -> {
                            currOutPosition = indicatorValue
                            stickerViewModel.curInfoOrImageSticker()?.animation?.let {
                                it.outDuration = indicatorValue * 1000
                            }
                            stickerViewModel.updateInfoOrImageSticker()
                            startStickerAnimationPreview(2)
                        }
                        else -> return
                    }
                }
            })
            setEnableLeftIndicator(false)
            setEnableRightIndicator(false)
        }

        bar_loop_duration.setOnSliderChangeListener(object : OnSliderChangeListener() {
            override fun getShowText(value: Int): String? {
                val s = value / 1000
                val ms = (value % 1000) / 100
                return "$s.${ms}s"
            }

            override fun onChange(value: Int) {
            }

            override fun onFreeze(value: Int) {
                currLoopPosition = value
                stickerViewModel.curInfoOrImageSticker()?.animation?.let {
                    it.inDuration = value * 1000
                }
                stickerViewModel.updateInfoOrImageSticker()
                startStickerAnimationPreview(MODE_ANIM_LOOP)
            }
        })


        recycler_animation_in.apply {
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    if (!isAdded || isDetached) {
                        return
                    }
                    val animation = stickerViewModel.curInfoOrImageSticker()?.animation
                    val inDuration = animation?.takeIf { !it.loop && it.inAnim != null }?.inDuration
                    currInPosition =
                        if (inDuration != null) inDuration / 1000 else DEFAULT_IN_DURATION
                    selectItem(animation?.takeIf { !it.loop }?.inAnim?.resourceFile ?: "")
                }
            })
            init(
                getResourceViewConfig(
                    category = resourceConfig?.stickerAnimationResConfig?.categoryIn
                        ?: DefaultResConfig.STICKER_ANIM_CATEGORY_IN
                )
            )
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    selectInAnim(item)
                    selectItem(item?.path ?: "")
                    recycler_animation_loop.selectItem("")
                }
            })
        }

        recycler_animation_out.apply {
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val animation = stickerViewModel.curInfoOrImageSticker()?.animation
                    val outDuration = animation?.takeIf { !it.loop }?.outDuration
                    currOutPosition =
                        if (outDuration != null) outDuration / 1000 else DEFAULT_OUT_DURATION
                    selectItem(animation?.takeIf { !it.loop }?.outAnim?.resourceFile ?: "")
                }
            })
            init(
                getResourceViewConfig(
                    category = resourceConfig?.stickerAnimationResConfig?.categoryOut
                        ?: DefaultResConfig.STICKER_ANIM_CATEGORY_OUT
                )
            )
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    selectOutAnim(item)
                    selectItem(item?.path ?: "")
                    recycler_animation_loop.selectItem("")
                }
            })
        }

        recycler_animation_loop.apply {
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    val animation = stickerViewModel.curInfoOrImageSticker()?.animation
                    val loopDuration = animation?.takeIf { it.loop }?.inDuration
                    currLoopPosition =
                        if (loopDuration != null) loopDuration / 1000 else DEFAULT_LOOP_DURATION
                    selectItem(animation?.takeIf { it.loop }?.inAnim?.resourceFile ?: "")
                }
            })
            init(
                getResourceViewConfig(
                    category = resourceConfig?.stickerAnimationResConfig?.categoryLoop
                        ?: DefaultResConfig.STICKER_ANIM_CATEGORY_LOOP
                )
            )
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    selectLoopAnim(item)
                    selectItem(item?.path ?: "")
                    recycler_animation_in.selectItem("")
                    recycler_animation_out.selectItem("")
                }
            })
        }
    }

    private fun getResourceViewConfig(category: String) = ResourceViewConfig.Builder()
        .panelKey(DefaultResConfig.STICKER_ANIMATION)
        .hasCategory(true)
        .categoryKey(category)
        .layoutManager(LinearLayoutManager(context, RecyclerView.HORIZONTAL, false))
        .nullItemInFirstConfig(
            FirstNullItemConfig(
                nullItemResource = R.drawable.bg_text_anim_null_item,
                addNullItemInFirst = true,
                enableSelector = true
            )
        )
        .resourceTextConfig(
            ResourceTextConfig(
                enableText = true,
                textPosition = TextPosition.DOWN, textSize = 12
            )
        )
        .selectorConfig(
            ItemSelectorConfig(
                selectorWidth = 56,
                selectorHeight = 56, selectorBorderRes = R.drawable.text_anim_item_bg_selected
            )
        )
        .resourceImageConfig(
            ResourceImageConfig(
                imageWidth = 50,
                imageHeight = 50,
                roundRadius = 25,
                backgroundResource = R.drawable.transparent_holder
            )
        )
        .build()

    private fun selectInAnim(item: ResourceItem?) {
        currLoopPosition = DEFAULT_LOOP_DURATION
        stickerViewModel.curTrackSlot()?.let { currSlot ->
            val slotDuration = (currSlot.duration / 1000f).toInt() // 微秒
            val initLoopDuration = min(slotDuration, currInPosition)

            stickerViewModel.curInfoOrImageSticker()

            stickerViewModel.curInfoOrImageSticker()?.apply {

                item?.path?.takeIf { it.isNotEmpty() }?.let {
                    animation = animation?.apply {
                        loop = false
                        inAnim = NLEResourceNode().apply {
                            resourceFile = it
                            resourceId = item.resourceId
                            resourceType = NLEResType.ANIMATION_STICKER
                        }
                        inDuration = initLoopDuration * 1000
                    } ?: NLEStyStickerAnim().apply {
                        loop = false
                        inAnim = NLEResourceNode().apply {
                            resourceFile = it
                            resourceId = item.resourceId
                            resourceType = NLEResType.ANIMATION_STICKER
                        }
                        inDuration = initLoopDuration * 1000
                    }
                } ?: let {
                    animation = animation?.apply {
                        loop = false
                        inAnim = null
                        inDuration = 0
                    }
                    bar_inout_duration.setEnableLeftIndicator(false)
                    currInPosition = DEFAULT_IN_DURATION
                }

                stickerViewModel.updateInfoOrImageSticker()
                stickerViewModel.startStickerAnimationPreview(currSlot, 1)
            }

            updateUIByNLETrackSlot()
        }
    }

    private fun selectOutAnim(item: ResourceItem?) {
        currLoopPosition = DEFAULT_LOOP_DURATION
        stickerViewModel.curTrackSlot()?.let { currSlot ->
            val slotDuration = (currSlot.duration / 1000f).toInt() // 微秒
            val initLoopDuration = min(slotDuration, currOutPosition)

            stickerViewModel.curInfoOrImageSticker()?.apply {

                val oriLoop = animation?.loop ?: false
                item?.path?.takeIf { it.isNotEmpty() }?.let {
                    animation = animation?.apply {
                        loop = false
                        outAnim = NLEResourceNode().apply {
                            resourceFile = it
                            resourceId = item.resourceId
                            resourceType = NLEResType.ANIMATION_STICKER
                        }
                        outDuration = initLoopDuration * 1000
                        if (oriLoop) {
                            inAnim = null
                            inDuration = 0
                        }
                    } ?: NLEStyStickerAnim().apply {
                        loop = false
                        outAnim = NLEResourceNode().apply {
                            resourceFile = it
                            resourceId = item.resourceId
                            resourceType = NLEResType.ANIMATION_STICKER
                        }
                        outDuration = initLoopDuration * 1000
                    }
                } ?: let {
                    animation = animation?.apply {
                        loop = false
                        outAnim = null
                        outDuration = 0
                        if (oriLoop) {
                            inAnim = null
                            inDuration = 0
                        }
                    }
                    bar_inout_duration.setEnableRightIndicator(false)
                    currOutPosition = DEFAULT_OUT_DURATION
                }

                stickerViewModel.updateInfoOrImageSticker()
                stickerViewModel.startStickerAnimationPreview(currSlot, 2)
            }

            updateUIByNLETrackSlot()
        }
    }

    private fun selectLoopAnim(item: ResourceItem?) {
        currInPosition = DEFAULT_IN_DURATION
        currOutPosition = DEFAULT_OUT_DURATION
        stickerViewModel.curTrackSlot()?.let { currSlot ->
            val slotDuration = (currSlot.duration / 1000f).toInt() // 微秒
            val loopMaxDuration = slotDuration - 200
            val initLoopDuration = min(loopMaxDuration, currLoopPosition)

            stickerViewModel.curInfoOrImageSticker()?.apply {

                // 这里设置了循环动画之后 outAnim也被清空了
                item?.path?.takeIf { it.isNotEmpty() }?.let {
                    animation
                    animation = NLEStyStickerAnim().apply {
                        loop = true
                        inAnim = NLEResourceNode().apply {
                            resourceFile = it
                            resourceId = item.resourceId
                            resourceType = NLEResType.ANIMATION_STICKER
                        }
                        inDuration = initLoopDuration * 1000
                    }
                } ?: let {
                    animation = animation?.apply {
                        loop = true
                        inAnim = null
                        inDuration = 0
                    }
                    currLoopPosition = DEFAULT_LOOP_DURATION
                }

                stickerViewModel.updateInfoOrImageSticker()
                stickerViewModel.startStickerAnimationPreview(currSlot, 3)
            }

            updateUIByNLETrackSlot()
        }
    }

    /**
     * 确定初始化哪个TAB
     */
    private fun initTabByNLETrackSlot() {
        when (stickerViewModel.curInfoOrImageSticker()?.animation.getAnimationType()) {
            // 如果只有出场动画 或者 入场动画&出场动画都有 则默认定位到出场动画Tab
            ANIMATION_TYPE_OUT, ANIMATION_TYPE_INANDOUT -> rb_out.isChecked = true

            // 如果只有循环动画 则默认定位到入场动画Tab
            ANIMATION_TYPE_LOOP -> rb_loop.isChecked = true

            // 其他情况下默认定位到入场动画Tab
            else -> rb_in.isChecked = true
        }
    }

    private fun resetRecyclerView() {
        recycler_animation_in.visibility = View.GONE
        recycler_animation_out.visibility = View.GONE
        recycler_animation_loop.visibility = View.GONE
    }

    private fun resetSeekBar() {
        ll_inout_layout.visibility = View.INVISIBLE
        ll_loop_layout.visibility = View.INVISIBLE
    }

    /**
     * 根据NLEStyStickerAnim 的信息更新UI页面
     */
    private fun updateUIByNLETrackSlot() {
        stickerViewModel.curTrackSlot()?.let {
            /**
             * 设置循环动画的range
             */
            // 获取片段的时长，单位是微秒，需要先转成毫秒
            val slotDuration = (it.duration / 1000f).toInt()
            // 调整的时长最长不超过贴纸总时长-0.2s
            val loopMaxDuration = slotDuration - 200
            // 设置可调整的时长范围
            bar_loop_duration.setRange(100, loopMaxDuration)

            // 设置入场&出场动画的range
            bar_inout_duration.setTotalRange(slotDuration, 1)
        }

        val inDuration = stickerViewModel.curInfoOrImageSticker()?.animation?.inDuration ?: 0
        val outDuration = stickerViewModel.curInfoOrImageSticker()?.animation?.outDuration ?: 0

        val animationType = stickerViewModel.curInfoOrImageSticker()?.animation.getAnimationType()
        val currentMode = when {
            rb_out.isChecked -> MODE_ANIM_OUT
            rb_loop.isChecked -> MODE_ANIM_LOOP
            else -> MODE_ANIM_IN
        }

        resetSeekBar()

        when {
            animationType == ANIMATION_TYPE_LOOP && currentMode == MODE_ANIM_LOOP -> {
                // 如果是循环动画，显示
                ll_loop_layout.visibility = View.VISIBLE
                bar_loop_duration.currPosition = (inDuration / 1000f).toInt()
            }

            (animationType == ANIMATION_TYPE_IN
                    || animationType == ANIMATION_TYPE_OUT
                    || animationType == ANIMATION_TYPE_INANDOUT) && (currentMode == MODE_ANIM_IN
                    || currentMode == MODE_ANIM_OUT) -> {
                ll_inout_layout.visibility = View.VISIBLE
                if (animationType == ANIMATION_TYPE_INANDOUT || animationType == ANIMATION_TYPE_IN) {
                    bar_inout_duration.setEnableLeftIndicator(true)
                } else {
                    bar_inout_duration.setEnableLeftIndicator(false)
                }
                if (animationType == ANIMATION_TYPE_INANDOUT || animationType == ANIMATION_TYPE_OUT) {
                    bar_inout_duration.setEnableRightIndicator(true)
                } else {
                    bar_inout_duration.setEnableRightIndicator(false)
                }
                bar_inout_duration.setBothIndicator(
                    (inDuration / 1000f).toInt(),
                    (outDuration / 1000f).toInt()
                )
            }
        }
    }

    /**
     * 开启预览动画
     */
    private fun startStickerAnimationPreview(mode: Int) {
        stickerViewModel.curTrackSlot()?.let {
            stickerViewModel.startStickerAnimationPreview(it, mode)
        }
    }

    /**
     * 停止预览动画
     */
    private fun stopStickerAnimationPreview() {
        stickerViewModel.curTrackSlot()?.let {
            stickerViewModel.stopStickerAnimationPreview(it)
        }
    }

    override fun provideEditorViewModel(): BaseEditorViewModel {
        return stickerViewModel
    }
}