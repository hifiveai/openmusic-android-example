package com.ss.ugc.android.editor.base.music.tools

import com.ss.ugc.android.editor.base.music.data.MusicItem
import java.lang.Exception

object MusicPlayer {

    fun play(
        item: MusicItem,
        isLocal: Boolean,
        playAnim: (playing: Boolean) -> Unit,
        onProgress: ((current: Int, total: Int) -> Unit)?,
        onPlayComplete: () -> Unit = {}
    ) {
        val playPath = when {
            isLocal -> {item.uri}
            MusicDownloader.isDownLoaded(item) -> {
                MusicDownloader.getDownloadedPath(item)
            }
            else -> {
                item.previewUrl
            }
        }

        playPath?.also {
            AudioPlayer.play(
                item.id,
                it,
                playAnim = playAnim,
                onProgress = onProgress,
                onPlayComplete = onPlayComplete
            )
        }
    }

    fun pause() {
        AudioPlayer.pause()
    }

    fun resume() {
        AudioPlayer.resume()
    }

    fun clear() {
        AudioPlayer.clear()
    }

    /**
     * 设置播放时间
     * @param playTime 播放时间
     */
    fun seek(playTime: Int) {
        AudioPlayer.mediaPlayer?.seekTo(playTime)
    }

    fun isPlaying(item: MusicItem): Boolean {
        return AudioPlayer.isPlaying(item.id)
    }

    fun isPlaying(id: Long): Boolean {
        return AudioPlayer.isPlaying(id)
    }

    fun getCurrentPosition(): Int {
        return try {
            AudioPlayer.mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }
    fun getTotalDuration(): Int {
        return try {
            AudioPlayer.mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }
}