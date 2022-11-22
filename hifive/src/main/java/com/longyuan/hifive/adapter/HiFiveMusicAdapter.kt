package com.longyuan.hifive.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.longyuan.hifive.model.HiFiveMusicModel
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.view.HiFiveProgress
import com.longyuan.hifive.ui.HiFiveActivity


/**
 * #duxiaxing
 * #date: 2022/7/15
 */
class HiFiveMusicAdapter(var context: Context) : RecyclerView.Adapter<HiFiveMusicAdapter.MusicViewHolder>(){
    private val mData = arrayListOf<HiFiveMusicModel>()
    private val mapProgress = mutableMapOf<String, HiFiveProgress>()
    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivMusicItemPic : ImageView? = null
        var ivMusicItemStateImage : ImageView? = null
        var tvMusicItemName : TextView? = null
        var tvMusicItemAuthor : TextView? = null
        var tvMusicItemTag : TextView? = null
        var tvMusicItemTime : TextView? = null
        var tvMusicItemStateText : TextView? = null
        var musicItemProgress : HiFiveProgress? = null
        var ivMusicItemPlay : ImageView? = null

        init {
            ivMusicItemPic = itemView.findViewById(R.id.ivMusicItemPic)
            tvMusicItemName = itemView.findViewById(R.id.tvMusicItemName)
            tvMusicItemAuthor = itemView.findViewById(R.id.tvMusicItemAuthor)
            tvMusicItemTag = itemView.findViewById(R.id.tvMusicItemTag)
            tvMusicItemTime = itemView.findViewById(R.id.tvMusicItemTime)
            ivMusicItemStateImage = itemView.findViewById(R.id.ivMusicItemStateImage)
            tvMusicItemStateText = itemView.findViewById(R.id.tvMusicItemStateText)
            musicItemProgress = itemView.findViewById(R.id.musicItemProgress)
            ivMusicItemPlay = itemView.findViewById(R.id.ivMusicItemPlay)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MusicViewHolder {
        return MusicViewHolder(LayoutInflater.from(context).inflate(R.layout.hifive_music_adapter,p0,false))
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        mData[position].let {
            holder.tvMusicItemName?.text = it.name
            holder.tvMusicItemName?.setTextColor(if (it.isSelect) Color.parseColor("#FFD600") else Color.WHITE)
            holder.tvMusicItemAuthor?.text = it.author
            holder.tvMusicItemTime?.text = HiFiveConfig.formatMusicTime(it.time)
            holder.tvMusicItemTag?.text = it.tag
            holder.ivMusicItemPic?.let { iv -> HiFiveConfig.loadImageMusic(it.image, iv) }
            holder.itemView.setOnClickListener { _ ->
                clearSelect()
                it.isSelect = true
                notifyItemChanged(position)
                if(context is HiFiveActivity)(context as HiFiveActivity).setPlay(it,
                    startDownload = { holder.ivMusicItemStateImage?.performClick() }){ notifyModel ->
                    notifyItemChanged(mData.indexOf(notifyModel))
                }
                //点击后如未下载则开始自动下载
                if (it.progress == -1)holder.ivMusicItemStateImage?.performClick()
            }
            if (mapProgress.containsKey(it.id)) {
                holder.musicItemProgress?.let { it1 -> mapProgress.put(it.id, it1) }
            }
            when(it.progress){
                -1 -> {
                    holder.ivMusicItemStateImage?.let { v ->
                        v.visibility = View.VISIBLE
                        v.setImageResource(R.mipmap.icon_hifive_cloud_downlaod)
                        holder.musicItemProgress?.let { v2 -> mapProgress[it.id] = v2 }
                        setClickDownload(v,it)
                    }
                    holder.tvMusicItemStateText?.visibility = View.GONE
                    holder.musicItemProgress?.visibility = View.GONE
                    holder.ivMusicItemPlay?.visibility = View.GONE
                }
                100 -> {
                    holder.ivMusicItemStateImage?.visibility = View.GONE
                    holder.tvMusicItemStateText?.visibility = View.VISIBLE
                    holder.musicItemProgress?.visibility = View.GONE
                    holder.tvMusicItemStateText?.setOnClickListener {_->
                        HiFiveRequestManager.useMusic(it.id)
                        (context as HiFiveActivity).setAudio(it.name,HiFiveConfig.hasLocale(it.id)?.filePath ?: "")
                    }
                    holder.ivMusicItemPlay?.let { iv ->
                        iv.visibility = if (it.isSelect && it.isInitAudio) View.VISIBLE else View.GONE
                        iv.setImageResource(if (it.isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24)
                        iv.setOnClickListener {
                            if(context is HiFiveActivity)(context as HiFiveActivity).updatePlayState()
                        }
                    }
                }
                else -> {
                    holder.ivMusicItemStateImage?.visibility = View.GONE
                    holder.tvMusicItemStateText?.visibility = View.GONE
                    holder.musicItemProgress?.visibility = View.VISIBLE
                    holder.musicItemProgress?.progress = it.progress
                    holder.ivMusicItemPlay?.visibility = View.GONE
                }
            }
        }
    }

    override fun getItemCount(): Int = mData.size

    fun clearSelect(){
        for (i in 0 until mData.size){
            mData[i].let {
                if (it.isSelect){
                    it.isSelect = false
                    notifyItemChanged(i)
                }
            }
        }
    }

    fun addData(data: HiFiveMusicModel) {
        mData.add(data)
        notifyItemInserted(mData.size)
        compatibilityDataSizeChanged(1)
    }

    fun addData(newData: Collection<HiFiveMusicModel>) {
        mData.addAll(newData)
        notifyItemRangeInserted(mData.size - newData.size, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }

    fun addData(position: Int, data: HiFiveMusicModel) {
        mData.add(position, data)
        notifyItemInserted(position)
        compatibilityDataSizeChanged(1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNewInstance(newData: Collection<HiFiveMusicModel>){
        mData.clear()
        mData.addAll(newData)
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        mData.removeAt(position)
        notifyItemRemoved(position)
        compatibilityDataSizeChanged(0)
        notifyItemRangeChanged(position, mData.size - position)
    }

    fun getData() : List<HiFiveMusicModel> = mData

    @SuppressLint("NotifyDataSetChanged")
    private fun compatibilityDataSizeChanged(size: Int) {
        val dataSize = mData.size
        if (dataSize == size) {
            notifyDataSetChanged()
        }
    }

    private fun setClickDownload(ivMusicItemStateImage : ImageView,model: HiFiveMusicModel){
        ivMusicItemStateImage.visibility = View.VISIBLE
        ivMusicItemStateImage.setImageResource(R.mipmap.icon_hifive_cloud_downlaod)
        ivMusicItemStateImage.setOnClickListener { view ->
            when{
                !HiFiveConfig.isNetSystemUsable(view.context) -> HiFiveConfig.showToast(view.context,HiFiveConfig.networkErrorMsg)
                HiFiveRequestManager.isDownload(model.id) -> HiFiveConfig.showToast(view.context,"正在下载中，请稍后...")
                else -> {
                    HiFiveRequestManager.getMusic(model.id){ filePath ->
                        model.progress = 0
                        ivMusicItemStateImage.visibility = View.GONE
                        notifyItemChanged(mData.indexOf(model))
                        HiFiveRequestManager.downloadMusic(context,filePath,model.id, progress = { progress,downMusicId ->
                            progress.toInt().let { progressInt ->
                                model.progress = progressInt
                                mapProgress[downMusicId]?.progress = progressInt
                                //(context as HiFiveActivity).updatePlayProgress(downMusicId,progressInt)
                            }
                        }, failed = { downMusicId ->
                            mapProgress.remove(downMusicId)
                            model.progress = -1
                            notifyItemChanged(mData.indexOf(model))
                        }){
                            mapProgress.remove(model.id)
                            model.progress = 100
                            notifyItemChanged(mData.indexOf(model))
                            (context as HiFiveActivity).updateDownloadState(model.id)
                        }
                    }
                }
            }
        }
    }
}