package com.ss.ugc.android.editor.base.music.tools

import android.media.MediaPlayer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object AudioPlayer {

    private var lock: AtomicBoolean = AtomicBoolean(true)
    private var playId: Long = 0
    var mediaPlayer: MediaPlayer? = null
    private var paused = false
    private var prepared = false
    private var playProgress: ((current: Int, total: Int) -> Unit)? = null

    private var timer: Timer? = null

    private fun acquireLock() {
        while (true) {
            if (lock.compareAndSet(true, false)) break
        }
    }

    private fun releaseLock() {
        lock.compareAndSet(false, true)
    }

    private inline fun runInLock(block: () -> Unit) {
        try {
            acquireLock()
            block()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            releaseLock()
        }
    }

    fun play(
        id: Long,
        playPath: String,
        looping: Boolean = true,
        playAnim: (playing: Boolean) -> Unit,
        onProgress: ((current: Int, total: Int) -> Unit)?,
        onPlayComplete: () -> Unit = {}
    ) {
        runInLock {
            try {
                playProgress = onProgress
                if (playId == id) {
                    val player = mediaPlayer ?: return
                    paused =
                        if (!paused) {
                            if (player.isPlaying) {
                                player.pause()
                                playAnim(false)
                                true
                            } else {
                                false
                            }
                        } else {
                            if (prepared) {
                                player.seekTo(0)
                                player.start()
                                startProgress(playProgress)
                                player.isLooping = false
                                playAnim(true)
                            }
                            false
                        }
                } else {
                    paused = false
                    startPlayer(
                        playPath,
                        looping,
                        playAnim,
                        onPlayComplete
                    )
                    playId = id
                }
            } catch (e: Throwable) {
                playId = 0
                mediaPlayer?.let {
                    it.release()
                    mediaPlayer = null
                }
            }
        }
    }

    fun pause() {
        runInLock {
            val player = mediaPlayer ?: return@runInLock
            if (playId == 0L) return@runInLock

            paused = true
            if (player.isPlaying) {
                player.pause()
            }
            cancelTimer()
        }
    }

    fun resume() {
        runInLock {
            val player = mediaPlayer ?: return@runInLock
            if (playId == 0L) return@runInLock

            paused = false
            if (prepared) {
                player.isLooping = false
                player.start()
                startProgress(playProgress)
            }
        }
    }

    private fun startPlayer(
        playPath: String,
        looping: Boolean = true,
        playAnim: (playing: Boolean) -> Unit,
        onPlayComplete: () -> Unit = {}
    ) {
        val player: MediaPlayer
        if (mediaPlayer == null) {
            player = MediaPlayer()
            player.isLooping = looping
            mediaPlayer = player
        } else {
            player = mediaPlayer ?: return
            player.reset()
        }
        player.setOnPreparedListener {
            runInLock {
                prepared = true
                if (!paused) {
                    player.isLooping = false
                    player.seekTo(0)
                    it.start()
                    startProgress(playProgress)
                    playAnim(true)
                }
            }
        }

        player.setOnCompletionListener {
            runInLock {
                paused = true
                onPlayComplete()
                playProgress?.invoke(mediaPlayer?.duration ?: 0, mediaPlayer?.duration ?: 0)
                cancelTimer()
            }
        }

        player.setDataSource(playPath)
        player.prepareAsync()
    }

    private fun startProgress(onProgress: ((current: Int, total: Int) -> Unit)?) {
        val duration = mediaPlayer?.duration ?: 0
        if (duration > 0) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    val current = mediaPlayer?.currentPosition ?: 0
                    onProgress?.invoke(current, duration)
                }
            }, 100, 100)
        }
    }

    private fun cancelTimer(){
        timer?.cancel()
        timer = null
    }

    fun clear() {
        runInLock {
            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                player.stop()
            }
            player.setOnPreparedListener(null)
            player.release()
            cancelTimer()
            mediaPlayer = null
            paused = false
            playId = 0
        }
    }

    fun stop() {
        runInLock {
            val player = mediaPlayer ?: return
            if (playId == 0L) return@runInLock

            if (player.isPlaying) {
                player.stop()
                cancelTimer()
            }
            paused = false
            playId = 0
        }
    }

    fun isPlaying(id: Long): Boolean {
        return (playId == id) && !paused && (mediaPlayer?.isPlaying ?: false)
    }
}