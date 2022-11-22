package com.longyuan.hifive.ui.fragment
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.adapter.HiFiveFragmentPagerAdapter
import com.longyuan.hifive.manager.HiFiveViewManager
import com.longyuan.hifive.model.HiFiveMusicModel
import com.longyuan.hifive.ui.HiFiveActivity
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.ss.ugc.android.editor.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.hifive_music_main_behavior.*
import kotlinx.android.synthetic.main.hifive_music_main_layout.*

/**
 * #duxiaxing
 * #date: 2022/7/13
 */
class HIFIVEMusicFragment : BaseFragment(),OnRefreshLoadMoreListener{

    private val fragments = arrayListOf(HiFiveMusicListFragment.newInstance(0),
        HiFiveMusicListFragment.newInstance(1))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun getContentView(): Int = R.layout.hifive_music_main_layout

    private fun initView(){
        refreshHiFiveMain.setOnRefreshLoadMoreListener(this)
        hiFiveSongMenuBackIv.setOnClickListener { (activity as HiFiveActivity).back() }
        hiFiveGoSearchIv.setOnClickListener { (activity as HiFiveActivity).goSearch() }
        hiFiveSongMenuVp.offscreenPageLimit = 2
        hiFiveSongMenuVp.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                HiFiveViewManager.setIndicator(hiFiveSongMenuPointLL,position)
            }
        })
        vpHiFiveMusicList.adapter = HiFiveFragmentPagerAdapter(childFragmentManager,fragments)
        vpHiFiveMusicList.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setMusicType()
            }
        })

        tvHiFiveMusicType_1.setOnClickListener { vpHiFiveMusicList.currentItem = 0 }
        tvHiFiveMusicType_2.setOnClickListener { vpHiFiveMusicList.currentItem = 1 }

        updateSheet()
    }

    private fun initViewPager() {
        HiFiveViewManager.initMenu(hiFiveSongMenuVp).let { fragments ->
            hiFiveSongMenuVp.adapter = HiFiveFragmentPagerAdapter(childFragmentManager,fragments)
            HiFiveViewManager.initPoint(hiFiveSongMenuPointLL,fragments.size)
        }
    }

    /**
     * 获取歌单数据
     */
    private fun updateSheet(){
        HiFiveRequestManager.updateSheet {
            HiFiveConfig.initSongMenuData(it.record)
            initViewPager()
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        if (HiFiveConfig.isNetSystemUsable(refreshHiFiveMain.context)) {
            fragments[vpHiFiveMusicList.currentItem].let {
                it.page = 0
                it.isUpdateAllData = false
            }
            refreshLayout.setNoMoreData(false)
            if (HiFiveConfig.musicMenuData.size < 1)updateSheet()
            onLoadMore(refreshLayout)
        }else{
            HiFiveConfig.showToast(refreshHiFiveMain.context,HiFiveConfig.networkErrorMsg)
            refreshHiFiveMain.postDelayed({ refreshLayout.finishRefresh() },100)
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (HiFiveConfig.isNetSystemUsable(refreshHiFiveMain.context)){
            fragments[vpHiFiveMusicList.currentItem].let {
                it.page++
                it.updateData(noMoreData = { refreshLayout.setNoMoreData(true) }){ finishRefresh() }
            }
        }else{
            HiFiveConfig.showToast(refreshHiFiveMain.context,HiFiveConfig.networkErrorMsg)
            refreshHiFiveMain.postDelayed({ refreshLayout.finishLoadMore() },100)
        }
    }

    /**
     * 更新收藏列表
     */
    fun updateCollectionList(model: HiFiveMusicModel, isAdd : Boolean){
        fragments[1].updateData(model,isAdd)
    }

    /**
     * 更新下载时列表中的进度条
     */
    fun updateProgress(musicId : String, progress : Int){
        fragments.forEach {
            it.updateProgress(musicId, progress)
        }
    }

    /**
     * 关联更新播放中音乐的相关状态
     */
    fun updateItemState(musicId: String, isPlaying : Boolean){
        fragments[vpHiFiveMusicList.currentItem].updateItemState(musicId, isPlaying)
    }

    private fun finishRefresh() {
        refreshHiFiveMain.finishRefresh()
        refreshHiFiveMain.finishLoadMore()
    }

    /**
     * 推荐音乐、我的收藏，切换
     */
    private fun setMusicType(){
        activity?.let {
            if (!HiFiveConfig.isNetSystemUsable(it)){
                HiFiveConfig.showToast(it,HiFiveConfig.networkErrorMsg)
                return
            }
        }
        if (vpHiFiveMusicList.currentItem == 0){
            HiFiveViewManager.updateTypeView(tvHiFiveMusicType_1, tvHiFiveMusicType_2)
        }else{
            HiFiveViewManager.updateTypeView(tvHiFiveMusicType_2, tvHiFiveMusicType_1)
        }
        refreshHiFiveMain.setNoMoreData(fragments[vpHiFiveMusicList.currentItem].isUpdateAllData)
        for(i in 0 until fragments.size){
            if (i == vpHiFiveMusicList.currentItem){
                fragments[i].updateItemCheck()
            }else {
                fragments[i].clearSelect()
            }
        }
    }

    /**
     * 跳转到歌单搜索或回到添加音乐页面时清除选中的歌曲
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        fragments[vpHiFiveMusicList.currentItem].clearSelect()
    }

}