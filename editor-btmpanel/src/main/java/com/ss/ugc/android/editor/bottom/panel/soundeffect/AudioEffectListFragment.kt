package com.ss.ugc.android.editor.bottom.panel.soundeffect

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.music.BaseMusicListAdapter
import com.ss.ugc.android.editor.base.music.BaseMusicListFragment
import com.ss.ugc.android.editor.base.music.MusicItemViewConfig
import com.ss.ugc.android.editor.base.music.data.MusicCollection
import com.ss.ugc.android.editor.base.music.data.MusicType
import com.ss.ugc.android.editor.base.music.data.SelectedMusicInfo
import com.ss.ugc.android.editor.base.music.tools.MusicUtils
import com.ss.ugc.android.editor.base.view.CommonUiState.EMPTY
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.utils.FileUtil
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.Toaster
import java.util.Locale

class AudioEffectListFragment: BaseMusicListFragment() {

    companion object{
        const val MUSIC_COLLECTION_ID = "music_collection_id"       //歌单ID
        const val MUSIC_COLLECTION_NAME = "music_collection_name"   //歌单名称
        const val MUSIC_TAB_NAME = "music_tab_name"                 //tab名称

        fun newInstance(collection: MusicCollection, tabName: String? = null): AudioEffectListFragment {
            val fragment = AudioEffectListFragment()
            val args = Bundle().apply {
                putString(MUSIC_COLLECTION_ID, collection.id)
                val language = Locale.getDefault().language
                putString(MUSIC_COLLECTION_NAME, if (!TextUtils.equals(language, "zh") && !TextUtils.isEmpty(collection.nameEn)) {
                    collection.nameEn
                } else {
                    collection.name
                })
                putString(MUSIC_TAB_NAME, tabName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private var collectionId: String? = null
    private var collectionName: String? = null
    private var tabName: String? = null

    private val listAdapter = BaseMusicListAdapter(configMusicItem(), itemClickListener = null, scrollRequest = null) { musicItem ->
        val musicPath = musicItem.uri
        if (FileUtil.isFileExist(musicPath)) {
            LiveDataBus.getInstance().with(Constants.KEY_ADD_AUDIO, SelectedMusicInfo::class.java).postValue(
                SelectedMusicInfo(musicItem.title, musicPath, MusicType.EFFECT)
            )
        } else {
            Toaster.show("[${musicItem.title}] audio file path is not exist.")
        }
        parentFragment?.apply {
            FragmentHelper().closeFragment(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            collectionId = it.getString(MUSIC_COLLECTION_ID)
            collectionName = it.getString(MUSIC_COLLECTION_NAME)
            tabName = it.getString(MUSIC_TAB_NAME)
            loadData(false)
        }
    }

    override fun provideMusicListAdapter(): BaseMusicListAdapter {
        return listAdapter
    }

    private fun configMusicItem(): MusicItemViewConfig? {
        return MusicItemViewConfig.Builder()
            .setEnableMusicWave(false)
            .setIsLocalMusic(true)
            .setNeedShowFooter(false)
            .setHideIconWhenPlayMusic(true)
            .setShowProgressText(false)
            .build()
    }

    override fun loadData(isRetry: Boolean) {
        collectionId?.also {
//            val soundEffectsList = MusicUtils.getSoundEffectsList(it)
            val soundEffectsList = MusicUtils.getSoundEffectsList()
            if (soundEffectsList.isEmpty()) {
                pageState.postValue(EMPTY)
                return
            }
            listAdapter.append(soundEffectsList)
        }

    }
}