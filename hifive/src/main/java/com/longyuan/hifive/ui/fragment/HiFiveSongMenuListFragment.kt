package com.longyuan.hifive.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.adapter.HiFiveMusicAdapter
import com.longyuan.hifive.model.HiFiveSongMenuModel
import com.longyuan.hifive.ui.HiFiveActivity
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.hifive_song_menu_list_fragment.*

/**
 * #duxiaxing
 * #date: 2022/7/18
 */
class HiFiveSongMenuListFragment : BaseFragment() {
    private var page = 1
    private val pageSize = 20
    private var sheetId : Long = 0

    override fun getContentView(): Int = R.layout.hifive_song_menu_list_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val model : HiFiveSongMenuModel? = arguments?.getSerializable("model") as HiFiveSongMenuModel?
        sheetId = model?.id?.toLong() ?: 0
        hiFiveSongMenuListBackIv.setOnClickListener { (activity as HiFiveActivity).back() }
        hiFiveSongMenuListRv.layoutManager = LinearLayoutManager(context)
        activity?.let { hiFiveSongMenuListRv.adapter = HiFiveMusicAdapter(it) }
        hiFiveSongMenuTitleTv.text = model?.name
        hiFiveSongMenuListRefresh.setOnRefreshListener {
            if (HiFiveConfig.isNetSystemUsable(it.layout.context)){
                page = 1
                it.setNoMoreData(false)
                updateMusic()
            }else{
                HiFiveConfig.showToast(it.layout.context,HiFiveConfig.networkErrorMsg)
                it.layout.postDelayed({ it.finishRefresh() },100)
            }
        }
        hiFiveSongMenuListRefresh.setOnLoadMoreListener {
            if (HiFiveConfig.isNetSystemUsable(it.layout.context)){
                page++
                updateMusic()
            }else{
                HiFiveConfig.showToast(it.layout.context,HiFiveConfig.networkErrorMsg)
                it.layout.postDelayed({ it.finishLoadMore() },100)
            }
        }
        hiFiveSongMenuListRefresh.autoRefresh()
    }

    private fun updateMusic(){
        HiFiveRequestManager.updateSongMenuMusic(sheetId,page,pageSize, failed = {
            HiFiveConfig.showToast(activity,it)
            finishRefresh()
        }){ data ->
            HiFiveConfig.formatMusicData(data.record).let { musics ->
                (hiFiveSongMenuListRv.adapter as HiFiveMusicAdapter).let { adapter ->
                    if (page == 1)adapter.setNewInstance(musics) else adapter.addData(musics)

                    finishRefresh()
                    if (adapter.getData().size >= data.meta.totalCount ||
                        data.record.size < pageSize)hiFiveSongMenuListRefresh.setNoMoreData(true)
                }
            }
        }
    }

    private fun finishRefresh(){
        hiFiveSongMenuListRefresh.finishRefresh()
        hiFiveSongMenuListRefresh.finishLoadMore()
    }
}