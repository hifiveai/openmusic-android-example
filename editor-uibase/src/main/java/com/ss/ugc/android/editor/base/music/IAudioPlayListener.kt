package com.ss.ugc.android.editor.base.music

import com.ss.ugc.android.editor.base.music.data.MusicItem

/**
 * @date: 2021/8/6
 * @desc: 音频播放状态监听
 */
interface IAudioPlayListener {

    fun onStart(playing: Boolean, musicItem: MusicItem)

    fun onPause(isReset: Boolean)

    fun onProgress(current: Int, total: Int)

    fun onComplete()
}