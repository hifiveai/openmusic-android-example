package com.ss.ugc.android.editor.picker.album.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.album.config.MaterialListViewConfig
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectModel
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectedState
import com.ss.ugc.android.editor.picker.selector.viewmodel.SelectedState

/**
 * 素材列表Adapter，负责适配具体的Item样式，
 * 优先使用自定义ViewHolder，如果没有则使用默认ViewHolder
 * 如果使用自定义ViewHolder，需要在MaterialListViewConfig中实现bindViewHolder接口
 */
class MaterialListViewAdapter(
    private val pickComponentConfig: PickComponentConfig,
    private val materialListViewConfig: MaterialListViewConfig,
    private val materialSelectModel: MaterialSelectModel?,
    minVideoTimeThreshold:Long = -1
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val dataList = mutableListOf<MediaItem>()
    private val minVideoTimeThreshold = minVideoTimeThreshold
    private val selectorClickListener: (position: Int) -> Unit = { position ->
        materialListViewConfig.selectorClickListener?.invoke(dataList, position)
    }

    private val itemClickListener: (position: Int) -> Unit = { position ->
        materialListViewConfig.itemClickListener?.invoke(dataList, position)
    }

    fun setData(data: List<MediaItem>) {
        this.dataList.clear()
        this.dataList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return materialListViewConfig.createViewHolder?.invoke(
            parent,
            viewType,
            itemClickListener,
            selectorClickListener,
        ) ?: createDefaultViewHolder(
            parent,
            viewType
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        materialListViewConfig.bindViewHolder?.invoke(
            holder,
            position,
            dataList[position],
            materialSelectModel?.getSelectState(dataList[position])
        )
            ?: (holder as? DefaultMaterialViewHolder)?.bind(
                dataList[position],
                position, SelectedState(
                    materialSelectModel?.getSelectState(dataList[position])
                        ?: MaterialSelectedState.NON_SELECTED,
                    materialSelectModel?.enableSelect?.value ?: true
                ),
                minVideoTimeThreshold = this.minVideoTimeThreshold
            )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun createDefaultViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_default_material_item, parent, false)
        return DefaultMaterialViewHolder(
            itemView,
            itemClickListener,
            selectorClickListener,
            pickComponentConfig,
            materialListViewConfig.materialViewHolderConfig
        )
    }
}