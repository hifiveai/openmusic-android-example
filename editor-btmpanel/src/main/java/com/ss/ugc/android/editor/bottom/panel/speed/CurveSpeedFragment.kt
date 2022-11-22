package com.ss.ugc.android.editor.bottom.panel.speed

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import com.ss.ugc.android.editor.base.extensions.gone
import com.ss.ugc.android.editor.base.extensions.hide
import com.ss.ugc.android.editor.base.extensions.show
import com.ss.ugc.android.editor.base.extensions.visible
import com.ss.ugc.android.editor.base.fragment.BaseUndoRedoFragment
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resourceview.ResourceItemClickListener
import com.ss.ugc.android.editor.base.resourceview.ResourceListInitListener
import com.ss.ugc.android.editor.base.resourceview.ResourceViewConfig
import com.ss.ugc.android.editor.base.theme.resource.*
import com.ss.ugc.android.editor.bottom.R
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import kotlinx.android.synthetic.main.btm_fragment_curve_speed.*

class CurveSpeedFragment : BaseUndoRedoFragment<SpeedViewModel>(), View.OnClickListener {

    private val playProgressObserver = Observer<Float> { playPosition ->
        playPosition?.let {
            if (cl_edit_panel.visible && viewModel.isPlaying()) {
                curve_speed_view?.setPlayProgress(it)
            }
        }
    }

    override fun getContentViewLayoutId(): Int {
        return R.layout.btm_fragment_curve_speed
    }

    override fun provideEditorViewModel(): SpeedViewModel {
        return EditViewModelFactory.viewModelProvider(this).get(SpeedViewModel::class.java);
    }

    override fun onUpdateUI() {
        //获取当前的曲线变速
        getSelectResourceItem(viewModel.getCurrentCurveSpeedName())?.let {
            viewModel.getCurvedSpeedInfo(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

        viewModel.playState.observe(this, Observer{
            //更新播放按钮状态
            iv_play?.isActivated = it ?: false
        })

        viewModel.curveSpeedInfo.observe(this, Observer{
            it?.let {
                tv_edit_panel_name?.text = it.curveSpeedName
                rv_curve_speed?.selectItemByName(it.curveSpeedName ?: getString(R.string.ck_none))
                tv_src_duration?.text = getString(R.string.ck_curve_speed_src_duration, it.srcDuration)
                tv_dst_duration?.text = getString(R.string.ck_curve_speed_dst_duration, it.dstDuration)
            }
        })
        viewModel.onCurveSpeedOpen()
    }

    private fun initView(view: View) {
        initEditView()
        initListView()
        initListener()
        hideBottomBar()
        showListPanel()
    }

    private fun initEditView() {
        curve_speed_view?.let {
            it.progressChange = { progress, touchPointIndex ->
                if (touchPointIndex > 0) {
                    tv_curve_speed?.show()
                    tv_curve_speed?.text = getString(R.string.ck_curve_speed_value, it.getPointSpeed(touchPointIndex))
                }
                viewModel.pausePlay()
                viewModel.seekToFromSegDelta(progress, false)
            }

            it.pointListChange = { progress, points ->
                tv_curve_speed?.gone()
                viewModel.applyCurveSpeed(points)
                viewModel.seekToFromSegDelta(progress, false)
            }

            it.editModeChange = { mode ->
                when (mode) {
                    CurveSpeedView.EDIT_POINT_MODE_DELETE -> {
                        tv_points_edit.isEnabled = true
                        tv_points_edit?.alpha = 1F
                        tv_points_edit?.setTextColor(resources.getColor(R.color.delete_enable))
                        tv_points_edit?.setText(R.string.ck_remove)
                    }

                    CurveSpeedView.EDIT_POINT_MODE_ADD -> {
                        tv_points_edit?.alpha = 1F
                        tv_points_edit?.setTextColor(resources.getColor(R.color.delete_enable))
                        tv_points_edit.isEnabled = true
                        tv_points_edit?.setText(R.string.ck_add)
                    }

                    CurveSpeedView.EDIT_POINT_MODE_DISABLE -> {
                        tv_points_edit?.alpha = 0.5F
                        tv_points_edit?.setText(R.string.ck_remove)
                        tv_points_edit?.setTextColor(resources.getColor(R.color.delete_disable))
                        tv_points_edit.isEnabled = false

                    }
                }
            }
        }
    }

    private fun initListView() {
        rv_curve_speed?.apply {
            val config = ResourceViewConfig.Builder()
                .panelKey(resourceConfig?.curveSpeedPanel ?: DefaultResConfig.CURVE_SPEED_PANEL)
                .layoutManager(LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false))
                .nullItemInFirstConfig(
                    FirstNullItemConfig(
                        true,
                        R.drawable.null_filter,
                        true,
                        isIdentical = false
                    )
                )
                .resourceTextConfig(
                    ResourceTextConfig(
                        true,
                        textSelectedColor = R.color.transparent_50p_white,
                        textColor = R.color.white,
                        textPosition = TextPosition.DOWN
                    )
                )
                .downloadAfterFetchList(false)
                .downloadIconConfig(DownloadIconConfig(true))
                .selectorConfig(
                    ItemSelectorConfig(
                        true,
                        50,
                        50,
                        R.drawable.bg_curve_speed_selected
                    )
                )
                .build()
            setResourceListInitListener(object : ResourceListInitListener {
                override fun onResourceListInitFinish() {
                    onUpdateUI()
                }
            })
            init(config)
            setOnItemClickListener(object : ResourceItemClickListener {
                override fun onItemClick(item: ResourceItem?, position: Int, select: Boolean) {
                    val select = rv_curve_speed?.getResourceListAdapter()?.resourceModelList?.get(position)?.isSelect
                    item?.let {
                        if (select == false) {
                            viewModel.applyCurveSpeedResource(it)
                        } else if (position != 0) {
                            showEditPanel()
                        }
                    }
                }
            })
        }
    }

    private fun initListener() {
        iv_edit_panel_confirm?.setOnClickListener(this)
        iv_play?.setOnClickListener(this)
        tv_points_edit?.setOnClickListener(this)
        tv_edit_panel_reset?.setOnClickListener(this)
        iv_list_panel_confirm?.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_edit_panel_confirm -> {
                showListPanel()
            }
            R.id.iv_list_panel_confirm -> {
                closeFragment()
                viewModel.onCurveSpeedClose()
            }
            R.id.iv_play -> {
                onIvPlayClick(view)
            }
            R.id.tv_points_edit -> {
                onTvPointsEditClick()
            }
            R.id.tv_edit_panel_reset -> {
                curve_speed_view?.setPoints(viewModel.resetCurveSpeed())
            }
        }
    }

    /**
     * 当播放按钮点击时
     */
    private fun onIvPlayClick(view: View) {
        if (view.isActivated) {
            viewModel.pausePlay()
        } else {
            viewModel.startPlay()
        }
    }

    /**
     * 当编辑操作点按钮点击时
     */
    private fun onTvPointsEditClick() {
        curve_speed_view?.let {
            when (it.getEditPointMode()) {
                CurveSpeedView.EDIT_POINT_MODE_DELETE -> {
                    it.deleteControlPoint()
                }

                CurveSpeedView.EDIT_POINT_MODE_ADD -> {
                    it.addControlPoint()
                }
            }
        }
    }

    private fun showEditPanel() {
        viewModel.changeCurveSpeedEditPanelVisibility(true)
        cl_edit_panel?.show()
        cl_list_panel?.hide()
        setEditViewContent()
        viewModel.playProgress.observe(this, playProgressObserver)
    }

    private fun showListPanel() {
        viewModel.changeCurveSpeedEditPanelVisibility(false)
        cl_list_panel?.show()
        cl_edit_panel?.hide()
        viewModel.playProgress.removeObserver(playProgressObserver)
    }

    private fun setEditViewContent() {
        curve_speed_view?.setPlayProgress(0f)
        curve_speed_view?.setPoints(viewModel.getCurrentCurveSpeed())
    }

    private fun getSelectResourceItem(name: String): ResourceItem? {
        rv_curve_speed?.getResourceListAdapter()?.resourceModelList?.forEach {
            if (name == it.resourceItem.name) {
                return it.resourceItem
            }
        }
        return null
    }
}