package com.ss.ugc.android.editor.bottom.panel.audio

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.music.BaseMusicListAdapter
import com.ss.ugc.android.editor.base.music.BaseMusicListFragment
import com.ss.ugc.android.editor.base.music.MusicItemViewConfig
import com.ss.ugc.android.editor.base.music.data.CoverUrl
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.music.data.SelectedMusicInfo
import com.ss.ugc.android.editor.base.music.tools.MusicUtils
import com.ss.ugc.android.editor.base.network.IScrollRequest
import com.ss.ugc.android.editor.base.resource.ResourceHelper
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.utils.FileUtil
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.Toaster

/**
 * 本地音乐列表Fragment
 */
class LocalMusicFragment : BaseMusicListFragment(), IScrollRequest {

    companion object {
        const val TAG = "LocalMusicFragment"
    }

    private var adapter =
        BaseMusicListAdapter(configMusicItem(), itemClickListener = null, this) { musicItem ->
            val musicPath = musicItem.uri
            if (FileUtil.isFileExist(musicPath)) {
                LiveDataBus.getInstance()
                    .with(Constants.KEY_ADD_AUDIO, SelectedMusicInfo::class.java).postValue(
                    SelectedMusicInfo(musicItem.title, musicPath)
                )
            } else {
                Toaster.show("[${musicItem.title}] audio file path is not exist.")
            }
            parentFragment?.apply {
                FragmentHelper().closeFragment(this)
            }
        }

    private fun configMusicItem(): MusicItemViewConfig {
        return MusicItemViewConfig.Builder()
            .setEnableMusicWave(false)
            .setIsLocalMusic(true)
            .setNeedShowFooter(false)
            .setShowProgressText(true)
            .build()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData(false)
    }

    override fun provideMusicListAdapter(): BaseMusicListAdapter {
        return adapter
    }

    override fun loadData(isRetry: Boolean) {
        val musicList = if (arguments?.getInt("type") == 1) {
            MusicUtils.getMusicData(context)
        } else {
            getListData()
        }
        adapter.append(musicList)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            adapter.onPause()
        }
    }

    /**
     * 获取json文件里配置的音乐库列表
     */
    private fun getListData(): ArrayList<MusicItem> {
        val list: ArrayList<MusicItem> = ArrayList()
        ResourceHelper.getInstance().musicList.forEachIndexed { index, it ->
            val coverUrl = CoverUrl()
            coverUrl.cover_medium = it.icon

            val item = MusicItem()
            item.id = index.toLong() + 1000
            item.title = it.name
            item.uri = it.path
            item.author = it.singer
            item.coverUrl = coverUrl
            list.add(item)
        }
        return list
    }

    override fun requestScroll(position: Int) {
        getRecyclerView()?.apply {
            val layoutManager = layoutManager as LinearLayoutManager
            if (layoutManager.findFirstVisibleItemPosition() < position || layoutManager.findLastVisibleItemPosition() > position) {
                smoothScrollToPosition(position)
            }
        }
    }

}