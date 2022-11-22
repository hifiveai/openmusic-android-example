package com.ss.ugc.android.editor.picker.album.config

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.data.model.QueryParam
import com.ss.ugc.android.editor.picker.selector.viewmodel.MaterialSelectedState

/**
 * 素材展示列表配置
 * materialViewHolderConfig：列表holder配置，处理item样式
 * imageQueryParams：图片素材查询参数，过滤某些类型图片资源，比如gif
 * videoQueryParams：视频素材查询参数，过滤某些类型视频资源，比如mpeg
 * listSpanCount：列表展示列数
 * itemClickListener：item点击回调
 * selectorClickListener：选择按钮点击回调
 * createViewHolder：自定义Holder
 * bindViewHolder：自定义binder逻辑
 */
data class MaterialListViewConfig(
    val materialViewHolderConfig: MaterialViewHolderConfig?,
    val imageQueryParams: QueryParam? = null,
    val videoQueryParams: QueryParam? = null,
    val listSpanCount: Int = 4,
    val itemClickListener: ((list: List<MediaItem>, position: Int) -> Unit)? = null,
    val selectorClickListener: ((list: List<MediaItem>, position: Int) -> Unit)? = null,
    val createViewHolder: ((
        parent: ViewGroup, viewType: Int,
        contentClickListener: ((position: Int) -> Unit),
        selectorClickListener: ((position: Int) -> Unit)
    ) -> RecyclerView.ViewHolder)? = null,
    val bindViewHolder: ((
        holder: RecyclerView.ViewHolder, position: Int, data: MediaItem, state: MaterialSelectedState?
    ) -> Unit)? = null
)

