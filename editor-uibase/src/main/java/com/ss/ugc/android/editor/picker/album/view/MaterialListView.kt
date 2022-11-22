package com.ss.ugc.android.editor.picker.album.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.picker.album.config.MaterialListViewConfig
import com.ss.ugc.android.editor.picker.album.view.adapter.MaterialListViewAdapter
import com.ss.ugc.android.editor.picker.album.viewmodel.MaterialDataViewModel
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectModel

class MaterialListView(
    private val context: Context,
    private val lifecycle: LifecycleOwner,
    private val materialListViewConfig: MaterialListViewConfig,
    private val materialListViewAdapter: MaterialListViewAdapter,
    private val materialDataViewModel: MaterialDataViewModel,
    private val materialSelectModel: MaterialSelectModel? = null
) {
    private lateinit var contentView: View
    private lateinit var recyclerView: RecyclerView
    fun init() {
        initView()
        initObserver(lifecycle)
    }

    fun loadData() {
        materialDataViewModel.requestData(context)
    }

    private fun initView() {
        contentView =
            LayoutInflater.from(context).inflate(R.layout.layout_material_list_view, null, false)
        recyclerView = contentView.findViewById(R.id.materialListView)
        val layoutManager =
            GridLayoutManager(context, materialListViewConfig.listSpanCount)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(
            SpacesItemDecoration(
                SizeUtil.dp2px(2f),
                SizeUtil.dp2px(2f),
                columnCountLimit = materialListViewConfig.listSpanCount
            )
        )
        recyclerView.recycledViewPool.setMaxRecycledViews(0, 30)
        recyclerView.adapter = materialListViewAdapter
    }

    fun getListContentView(): View {
        if (!::contentView.isInitialized) {
            init()
        }
        return contentView
    }

    private fun initObserver(lifecycleOwner: LifecycleOwner) {
        materialDataViewModel.apply {
            listData.observe(lifecycleOwner, { listData ->
                materialListViewAdapter.setData(listData!!)
            })
        }

        materialSelectModel?.apply {
            changeData.observe(lifecycleOwner, { changeData ->
                materialDataViewModel.listData.value?.indexOfFirst { media ->
                    media.path == changeData?.path
                }?.let { index ->
                    materialListViewAdapter.notifyItemChanged(index)
                }
            })

            enableSelect.observe(lifecycleOwner, {
                materialListViewAdapter.notifyDataSetChanged()
            })
        }
    }

    fun destroy() {
        materialSelectModel?.changeData?.removeObservers(lifecycle)
        materialSelectModel?.enableSelect?.removeObservers(lifecycle)
        materialDataViewModel.listData.removeObservers(lifecycle)
    }

}