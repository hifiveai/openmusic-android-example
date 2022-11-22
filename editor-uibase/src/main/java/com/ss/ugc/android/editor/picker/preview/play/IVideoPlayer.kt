package com.ss.ugc.android.editor.picker.preview.play

import android.view.Surface

interface IVideoPlayer {

    val currentPosition: Int

    val duration: Int

    val playedDuration: Int

    var isLooping: Boolean

    val isPrepared: Boolean

    val videoWidth: Int

    val videoHeight: Int

    fun setDataSource(path: String)

    fun setSurface(surface: Surface)

    fun play()

    fun play(start: Int)

    fun seekTo(time: Int)

    fun pause()

    fun stop()

    fun release()

    fun isPlaying(): Boolean

    fun setVideoListener(listener: VideoPlayerListener)
}

interface VideoPlayerListener {
    fun onStart()
    fun onStop()
    fun onRenderStart()
    fun onPrepared(player: IVideoPlayer)
    fun onVideoSizeChanged(player: IVideoPlayer, width: Int, height: Int)
    fun onCompletion()
    fun onError()

    open class Default : VideoPlayerListener {
        override fun onStart() = Unit

        override fun onStop() = Unit

        override fun onRenderStart() = Unit

        override fun onPrepared(player: IVideoPlayer) = Unit

        override fun onVideoSizeChanged(player: IVideoPlayer, width: Int, height: Int) = Unit

        override fun onCompletion() = Unit

        override fun onError() = Unit
    }
}