package com.ss.ugc.android.editor.picker.preview.play

import android.media.MediaPlayer
import android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.Surface
import com.ss.ugc.android.editor.base.utils.Logger
import java.io.File

open class VideoPlayer : IVideoPlayer {
    private val mediaPlayer = MediaPlayer()
    private val playedDurationInternal = PlayedDuration()
    private var listenerVideo: VideoPlayerListener? = null
    private val handlerThread = HandlerThread("thread-creativex-player")
    private val uiHandler = Handler(Looper.getMainLooper())
    private var workHandler: Handler
    private var isDataSourceValid = true

    override var isPrepared = false

    init {
        mediaPlayer.setOnErrorListener { _, _, _ ->
            uiHandler.post { listenerVideo?.onError() }
            true
        }
        mediaPlayer.setOnCompletionListener {
            uiHandler.post { listenerVideo?.onCompletion() }
        }
        mediaPlayer.setOnPreparedListener {
            uiHandler.post { listenerVideo?.onPrepared(this) }
        }
        mediaPlayer.setOnVideoSizeChangedListener { _, width, height ->
            uiHandler.post {
                listenerVideo?.onVideoSizeChanged(this, width, height)
            }
        }

        mediaPlayer.setOnInfoListener { _, what, _ ->
            if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
                listenerVideo?.onRenderStart()
            }
            false
        }
        mediaPlayer.setScreenOnWhilePlaying(true)
        handlerThread.start()
        workHandler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                try {
                    when (msg.what) {
                        WHAT_CMD_START -> {
                            val obj = msg.obj
                            if (obj is Int) {
                                doStart(obj)
                            } else {
                                doStart()
                            }
                        }
                        WHAT_CMD_PAUSE -> {
                            doPause()
                        }
                        WHAT_CMD_STOP -> {
                            doStop()
                        }
                        WHAT_CMD_RELEASE -> {
                            doRelease()
                        }
                        WHAT_CMD_DATA_SOURCE -> {
                            doSetDataSource(msg.obj as String)
                        }
                        WHAT_CMD_SEEK -> {
                            doSeekTo(msg.obj as Int)
                        }
                        WHAT_CMD_SET_LOOPING -> {
                            doSetLooping(msg.obj as Boolean)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "workHandler handleMsg error, $msg, $e")
                }
            }
        }
    }

    private fun log(msg: String) {
        Logger.d("Player", msg)
    }

    override fun setVideoListener(listener: VideoPlayerListener) {
        listenerVideo = listener
    }

    private fun doSeekTo(msec: Int) {
        if (!isPrepared) {
            doPrepare()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer.seekTo(msec.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer.seekTo(msec)
        }
        log("seek to $msec")
    }

    private fun doSetDataSource(path: String) {
        playedDurationInternal.reset()
        mediaPlayer.setDataSource(path)
        log("setDataSource $path")
    }

    private fun doStop() {
        playedDurationInternal.stop()
        mediaPlayer.stop()
        isPrepared = false
        uiHandler.post { listenerVideo?.onStop() }
        log("stop")
    }

    private fun doPause() {
        playedDurationInternal.stop()
        mediaPlayer.pause()
        uiHandler.post { listenerVideo?.onStop() }
        log("pause")
    }

    private fun doPrepare() {
        mediaPlayer.prepare()
        isPrepared = true
        log("prepare")
    }

    private fun doStart() {
        if (!isPrepared) {
            doPrepare()
        }
        playedDurationInternal.start()
        mediaPlayer.start()
        uiHandler.post { listenerVideo?.onStart() }
        log("play")
    }

    private fun doStart(start: Int) {
        if (!isPrepared) {
            doPrepare()
        }
        mediaPlayer.seekTo(start)
        playedDurationInternal.start()
        mediaPlayer.start()
        uiHandler.post { listenerVideo?.onStart() }
        log("play")
    }

    private fun doRelease() {
        doStop()
        handlerThread.quit()
        playedDurationInternal.clear()
        mediaPlayer.release()
        log("release")
    }

    private fun doSetLooping(looping: Boolean) {
        mediaPlayer.isLooping = looping
    }

    override fun setSurface(surface: Surface) {
        workHandler.post {
            try {
                if (!surface.isValid) {
                    Logger.e(TAG, "surface invalid, maybe has been release")
                    return@post
                }
                mediaPlayer.setSurface(surface)
            } catch (e: Exception) {
                Logger.e(TAG, "setSurface error, $e")
            }
        }
        log("setSurface")
    }

    override fun setDataSource(path: String) {
        val file = File(path)
        val exist = file.exists()
        val fileReadable = file.canRead()

        if (!exist || !fileReadable) {
            val message = "path=$path exist=$exist readable=$fileReadable"
            Logger.e(TAG, message)
            isDataSourceValid = false
            return
        }
        isDataSourceValid = true

        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_DATA_SOURCE
        msg.obj = path
        msg.sendToTarget()
    }

    override fun play() {
        if (!isDataSourceValid) {
            Logger.e(TAG, "dataSource not valid, can not play")
            return
        }
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_START
        msg.sendToTarget()
    }

    override fun play(start: Int) {
        if (!isDataSourceValid) {
            Logger.e(TAG, "dataSource not valid, can not play")
            return
        }
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_START
        msg.obj = start
        msg.sendToTarget()
    }

    override fun pause() {
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_PAUSE
        msg.sendToTarget()
    }

    override fun stop() {
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_STOP
        msg.sendToTarget()
    }

    override fun release() {
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_RELEASE
        msg.sendToTarget()
    }

    override fun seekTo(time: Int) {
        val msg = Message.obtain(workHandler)
        msg.what = WHAT_CMD_SEEK
        msg.obj = time
        msg.sendToTarget()
    }

    override val currentPosition: Int
        get() = try {
            mediaPlayer.currentPosition
        } catch (e: Exception) {
            0
        }

    override val duration: Int
        get() = try {
            mediaPlayer.duration
        } catch (e: Exception) {
            0
        }

    override val playedDuration: Int
        get() = playedDurationInternal.getPlayedDuration()

    override var isLooping: Boolean
        get() = try {
            mediaPlayer.isLooping
        } catch (e: Exception) {
            false
        }
        set(value) {
            val msg = Message.obtain(workHandler)
            msg.what = WHAT_CMD_SET_LOOPING
            msg.obj = value
            msg.sendToTarget()
        }

    override val videoWidth: Int
        get() = try {
            mediaPlayer.videoWidth
        } catch (e: Exception) {
            0
        }

    override val videoHeight: Int
        get() = try {
            mediaPlayer.videoHeight
        } catch (e: Exception) {
            0
        }

    override fun isPlaying(): Boolean = try {
        mediaPlayer.isPlaying
    } catch (e: Exception) {
        false
    }

    companion object {
        const val TAG = "VideoPlayer"

        const val WHAT_CMD_START = 0x01
        const val WHAT_CMD_PAUSE = 0x02
        const val WHAT_CMD_STOP = 0x03
        const val WHAT_CMD_RELEASE = 0x04
        const val WHAT_CMD_DATA_SOURCE = 0x05
        const val WHAT_CMD_SEEK = 0x06
        const val WHAT_CMD_SET_LOOPING = 0x07
    }
}