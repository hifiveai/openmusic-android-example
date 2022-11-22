package com.ss.ugc.android.editor.picker.album.view.adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.album.config.MaterialViewHolderConfig
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.selector.viewmodel.SelectedState
import kotlinx.android.synthetic.main.layout_default_material_item.view.*

/**
 * 默认的素材列表的ViewHolder，可以用过MaterialListViewConfig自定义
 */
class DefaultMaterialViewHolder(
    itemView: View,
    contentClickListener: (position: Int) -> Unit,
    selectorClickListener: (position: Int) -> Unit,
    private val pickComponentConfig: PickComponentConfig,
    private val materialViewHolderConfig: MaterialViewHolderConfig?
) : RecyclerView.ViewHolder(itemView) {
    private var data: MediaItem? = null
    private var minVideoTimeThreshold:Long = -1
    var position: Int? = null

    init {
        itemView.setOnClickListener {
            position?.let(contentClickListener)
        }

        itemView.selectLayout.setOnClickListener {
            position?.let(selectorClickListener)
        }
    }

    fun bind(
        mediaData: MediaItem,
        position: Int,
        state: SelectedState,
        minVideoTimeThreshold: Long = -1
    ) {
        this.minVideoTimeThreshold = minVideoTimeThreshold
        this.data = mediaData
        this.position = position
        bindData(mediaData, position, state)
        bindState(mediaData, state)
    }

    private fun bindState(mediaData: MediaItem, state: SelectedState) {
        materialViewHolderConfig?.let { config ->
            if (config.showSelectIcon) {
                itemView.selectImageView.visibility = View.VISIBLE
                if (state.materialSelectedState.isSelected()) {
                    config.selectedIcon?.let { selectedIcon ->
                        itemView.selectImageView.setImageResource(selectedIcon)
                    }
                } else {
                    itemView.selectImageView.setImageResource(config.noSelectIcon)
                }
            } else {
                itemView.selectImageView.visibility = View.GONE
            }
        }

        if ((!state.enableSelect && !state.materialSelectedState.isSelected() && materialViewHolderConfig?.showNonSelectableMask == true)
            || (isEnableMaskForTimeLimit) && mediaData.isVideo()) {
            itemView.maskView.visibility = View.VISIBLE
        } else {
            itemView.maskView.visibility = View.GONE
        }
    }

    //相册的视频素材是否由于其播放时间少于要替换的素材而被置灰
    private var isEnableMaskForTimeLimit = false
    private fun enableMaskForTimeLimt(videoDuration: Long, minVideoTimeThreshold: Long) {
        isEnableMaskForTimeLimit = videoDuration < minVideoTimeThreshold
    }

    private fun bindData(mediaData: MediaItem, position: Int, state: SelectedState) {
        if(minVideoTimeThreshold != -1.toLong()) enableMaskForTimeLimt(mediaData.duration,this.minVideoTimeThreshold)
        loadThumbnail(mediaData, itemView.thumbIv)
        if (mediaData.isVideo()) {
            itemView.durationTv.visibility = View.VISIBLE
            itemView.durationTv.text = with(mediaData.duration / 1000) {
                val min = this / 60
                val sec = this % 60
                "${if (min < 10) "0$min" else "$min"}:${if (sec < 10) "0$sec" else "$sec"}"
            }
        } else {
            itemView.durationTv.visibility = View.GONE
        }
    }

    private fun loadThumbnail(media: MediaItem, ivThumbnail: ImageView) {
        val previousTag = ivThumbnail.getTag(R.id.thumbIv)
        if (null == previousTag || previousTag != media.uri) {
            ivThumbnail.setTag(R.id.thumbIv, media.uri)
            load(
                media.uri,
                ivThumbnail,
                ivThumbnail.measuredWidth,
                ivThumbnail.measuredHeight
            )
        }
    }

    private fun load(
        uri: Uri,
        view: ImageView,
        resWidth: Int,
        resHeight: Int
    ): Boolean {
        val requestOptions = if (resWidth > 0 && resHeight > 0) {
            val overrideWidth = Math.min(view.measuredWidth, resWidth)
            val overrideHeight = Math.min(view.measuredHeight, resHeight)
            ImageOption.Builder().width(overrideWidth).height(overrideHeight).build()
        } else {
            ImageOption.Builder().width(view.measuredWidth).height(view.measuredHeight).build()
        }
        pickComponentConfig.imageLoader.loadBitmap(
            view.context,
            uri.toString(),
            view,
            requestOptions
        )
        return true
    }
}