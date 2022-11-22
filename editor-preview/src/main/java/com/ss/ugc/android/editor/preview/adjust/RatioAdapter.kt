package com.vega.edit.adjust

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.preview.R
import com.ss.ugc.android.editor.preview.adjust.view.CropAdjustRect
import org.jetbrains.annotations.NotNull

/**
 */
internal class RatioAdapter(
    @NotNull val data: MutableList<RatioItem>,
    val listener: OnRatioClickListener
) : RecyclerView.Adapter<RatioViewHolder>() {
    var lastSelectedIndex: Int = -1
    var curSelectedIndex: Int = if (data.size > 0) 0 else -1 // select first item by default

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.crop_item_layout, parent, false)
        return RatioViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RatioViewHolder, position: Int) {
        data[position].let { item ->
            holder.canvasRatioIv.setImageResource(item.ratioIcon)
            holder.canvasRatioTv.text = item.title
            holder.itemView.isEnabled = item.enable
            holder.itemView.isSelected = position == curSelectedIndex
            holder.itemView.setOnClickListener {
                if (position == curSelectedIndex) return@setOnClickListener
                lastSelectedIndex = curSelectedIndex
                curSelectedIndex = position
                if (lastSelectedIndex >= 0) {
                    notifyItemChanged(lastSelectedIndex)
                }
                notifyItemChanged(curSelectedIndex)
                listener.onClick(item.cropMode)
            }
        }
    }

    fun resetAllRatioSelected() {
        lastSelectedIndex = -1
        curSelectedIndex = -1
        notifyDataSetChanged()
    }

    fun setAllRatioEnable(enable: Boolean) {
        data.forEach { it.enable = enable }
        notifyDataSetChanged()
    }

    fun select(ratio: String) {
        (data.find { ratio == it.ratio } ?: data.first())?.let {
            lastSelectedIndex = curSelectedIndex
            curSelectedIndex = data.indexOf(it)
            listener.onClick(it.cropMode)
            notifyItemChanged(curSelectedIndex)
            notifyItemChanged(lastSelectedIndex)
        }
    }
}

internal class RatioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val canvasRatioIv: ImageView = itemView.findViewById(R.id.canvasRatioIv)
    val canvasRatioTv: TextView = itemView.findViewById(R.id.canvasRatioTv)
}

internal class RatioItem(
    val title: String,
    val ratio: String,
    val cropMode: CropAdjustRect.CropMode,
    val ratioIcon: Int,
    var enable: Boolean = true
)

internal interface OnRatioClickListener {
    fun onClick(cropMode: CropAdjustRect.CropMode)
}
