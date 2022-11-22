package com.longyuan.hifive.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.longyuan.hifive.R
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.model.HiFiveSongMenuModel
import com.ss.ugc.android.editor.base.utils.SizeUtil

/**
 * #duxiaxing
 * #date: 2022/7/14
 */
class HiFiveSongMenuAdapter(context: Context, private val itemClick: ((HiFiveSongMenuModel) -> Unit)?) :
    RecyclerView.Adapter<HiFiveSongMenuAdapter.SongMenuViewHolder>() {
    private val mData = arrayListOf<HiFiveSongMenuModel>()
    var context : Context? = context

    class SongMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var hiFiveMusicSongMenuIv : ImageView? = null
        var hiFiveMusicSongMenuTv : TextView? = null

        init {
            hiFiveMusicSongMenuIv = itemView.findViewById(R.id.hiFiveMusicSongMenuIv)
            hiFiveMusicSongMenuTv = itemView.findViewById(R.id.hiFiveMusicSongMenuTv)
            val with = (SizeUtil.getScreenWidth(itemView.context) - SizeUtil.dp2px(64f))/3
            hiFiveMusicSongMenuIv?.layoutParams?.height = with
        }
    }

    fun addData(data: HiFiveSongMenuModel) {
        mData.add(data)
        notifyItemInserted(mData.size)
        compatibilityDataSizeChanged(1)
    }

    fun addData(newData: Collection<HiFiveSongMenuModel>) {
        mData.addAll(newData)
        notifyItemRangeInserted(mData.size - newData.size, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun compatibilityDataSizeChanged(size: Int) {
        val dataSize = mData.size
        if (dataSize == size) {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): SongMenuViewHolder {
        LayoutInflater.from(context).inflate(R.layout.hifive_song_menu_adapter,p0,false).let {
            return SongMenuViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder : SongMenuViewHolder, position: Int) {
        mData[position].let {
            holder.hiFiveMusicSongMenuTv?.text = it.name
            holder.hiFiveMusicSongMenuIv
            holder.hiFiveMusicSongMenuIv?.let { iv -> HiFiveConfig.loadImageRound(it.image, iv) }
            holder.itemView.setOnClickListener { _ ->itemClick?.invoke(it) }
        }
    }

    override fun getItemCount(): Int = mData.size
}