package com.longyuan.hifive.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.adapter.HiFiveMusicAdapter
import com.longyuan.hifive.model.HiFiveMusicModel
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.hifive_music_list_fragment.*

/**
 * #duxiaxing
 * #date: 2022/8/8
 */
class HiFiveMusicListFragment : BaseFragment(){

    companion object{
        fun newInstance(position : Int) : HiFiveMusicListFragment{
            return HiFiveMusicListFragment().apply {
                val bundle = Bundle()
                bundle.putInt("position",position)
                arguments = bundle
            }
        }
    }

    private var position = 0//与ViewPager相对应position
    private val musicPageSize = 20
    var page = 1
    var isUpdateAllData = false//是否已全部加载该页面数据，用于ViewPager切换时重置刷新控件状态

    override fun getContentView(): Int = R.layout.hifive_music_list_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        position = arguments?.getInt("position",0) ?: 0
        activity.let { rvMusicList.layoutManager = LinearLayoutManager(it) }
        rvMusicList.adapter = HiFiveMusicAdapter(rvMusicList.context)
        updateData(null,null)
    }

    private fun getDataSize() : Int{
        if (rvMusicList == null || rvMusicList.adapter == null)return 0
        return (rvMusicList.adapter as HiFiveMusicAdapter).getData().size
    }

    fun updateData(model: HiFiveMusicModel, isAdd : Boolean){
        if (rvMusicList == null || rvMusicList.adapter == null)return
        (rvMusicList.adapter as HiFiveMusicAdapter).let { adapter ->
            if (isAdd) {
                HiFiveMusicModel(model.id,model.image,model.name,model.time,model.author,model.tag,
                    if (model.progress < 100) -1 else model.progress).apply {
                    isSelect = model.isSelect
                    isInitAudio = model.isInitAudio
                    isPlaying = model.isPlaying
                    adapter.addData(0,this)
                }
            } else {
                looper@for (i in 0 until adapter.getData().size) {
                    if (adapter.getData()[i].id == model.id){
                        adapter.remove(i)
                        break@looper
                    }
                }
            }
            hiFiveMusicCollectionNull.visibility = if (adapter.getData().isNotEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setNewData(data : List<HiFiveMusicModel>){
        (rvMusicList.adapter as HiFiveMusicAdapter).let {
            it.setNewInstance(data)
            hiFiveMusicCollectionNull.visibility = if (it.getData().isNotEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun addData(data : List<HiFiveMusicModel>){
        (rvMusicList.adapter as HiFiveMusicAdapter).let {
            it.addData(data)
            hiFiveMusicCollectionNull.visibility = if (it.getData().isNotEmpty()) View.GONE else View.VISIBLE
        }

    }

    /**
     * 更新下载时列表中的进度条，除点击下载页面的进度条正常显示以外，其它页面暂定只在下载完时更新状态
     */
    fun updateProgress(musicId : String, progress : Int){
        if (rvMusicList == null || rvMusicList.adapter == null)return
        (rvMusicList.adapter as HiFiveMusicAdapter).let { adapter ->
            adapter.getData().forEach {
                if (it.id == musicId){
                    //以此判断是否是点击下载的页面
                    if (it.progress < progress){
                        it.progress = progress
                        adapter.notifyItemChanged(adapter.getData().indexOf(it))
                    }
                    return
                }
            }
        }
    }

    /**
     * 更新列表item状态，使其和底部播放状态相关联
     */
    fun updateItemState(musicId: String, isPlaying : Boolean){
        if (rvMusicList == null || rvMusicList.adapter == null)return
        (rvMusicList.adapter as HiFiveMusicAdapter).let { adapter ->
            adapter.getData().forEach {
                if (it.id == musicId){
                    if (it.isPlaying != isPlaying){
                        it.isInitAudio = true
                        it.isPlaying = isPlaying
                        adapter.notifyItemChanged(adapter.getData().indexOf(it))
                    }
                    return
                }
            }
        }
    }

    /**
     * 页面切换时，根据播放中的音乐，设置改音乐在列表中的状态
     */
    fun updateItemCheck(){
        clearSelect()
        (rvMusicList.adapter as HiFiveMusicAdapter).let { adapter ->
            adapter.getData().forEach {
                if (it.id == HiFiveConfig.playModel?.id) {
                    it.isSelect = true
                    HiFiveConfig.playModel?.let { play ->
                        it.isInitAudio = play.isInitAudio
                        it.isPlaying = play.isPlaying
                    }
                    adapter.notifyItemChanged(adapter.getData().indexOf(it))
                }
            }
        }
    }

    /**
     * 清除列表中的选中播放状态
     */
    fun clearSelect(){
        (rvMusicList.adapter as HiFiveMusicAdapter).clearSelect()
    }

    fun updateData(noMoreData: (() -> Unit)?, loadComplete: (() -> Unit)?){
        when(position){
            0 -> {
                HiFiveRequestManager.updateMusic(page,musicPageSize, failed = { loadComplete?.invoke() }){
                    HiFiveConfig.formatMusicData(it.record).let { musics ->
                        if (page == 1)setNewData(musics) else addData(musics)
                    }
                    loadComplete?.invoke()
                    if (getDataSize() >= it.meta.totalCount || it.record.size < musicPageSize){
                        noMoreData?.invoke()
                        isUpdateAllData = true
                    }
                }
            }
            else -> {
                if (HiFiveConfig.musicCollection.size > 0 && HiFiveConfig.musicCollection.size < HiFiveConfig.localCollectionSize){
                    isUpdateAllData = true
                    setNewData(HiFiveConfig.musicCollection)
                    loadComplete?.invoke()
                    noMoreData?.invoke()
                    return
                }
                HiFiveRequestManager.updateCollection(page,musicPageSize, failed = { loadComplete?.invoke() }){ size, list ->
                    if (page == 1)setNewData(list) else addData(list)
                    loadComplete?.invoke()
                    if (getDataSize() >= size || list.size < musicPageSize){
                        noMoreData?.invoke()
                        isUpdateAllData = true
                    }
                }
            }
        }
    }

}