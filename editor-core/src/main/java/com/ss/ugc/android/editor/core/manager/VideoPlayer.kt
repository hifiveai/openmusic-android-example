package com.ss.ugc.android.editor.core.manager

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import com.bytedance.ies.nlemedia.IGetImageListener
import com.bytedance.ies.nlemedia.ISeekListener
import com.bytedance.ies.nlemedia.SeekMode
import com.bytedance.ies.nlemediajava.NLEPlayer
import com.bytedance.ies.nlemediajava.utils.DefaultInfoListener
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.IEditorContext
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import com.ss.ugc.android.editor.core.utils.ifCanLog
import java.util.concurrent.CopyOnWriteArrayList

class VideoPlayer(val editorContext: IEditorContext) : IVideoPlayer {

    companion object {
       const val TAG = "VideoPlayerImpl"
    }
    val infoListeners by lazy {
        CopyOnWriteArrayList<DefaultInfoListener>()
    }

    override var player: NLEPlayer? = null
    override var posWhenEditorSwitch:Int = 0
    override var isPlaying: Boolean = false
    override var isPlayingInFullScreen: Boolean = false
    override var playPositionWhenCompile: Int = 0
    override var activityResultFlag: Boolean = false
    private var isInPlayRange = false

    override fun init(surfaceView: SurfaceView, workSpace: String?) {
        var nleWorkSpace = workSpace
        if (nleWorkSpace == null) {
            nleWorkSpace = Environment.getExternalStorageDirectory().absolutePath
        }
        player = NLEPlayer(workSpace = nleWorkSpace!!, surfaceView = surfaceView)
        setCallback()
    }

    private fun setCallback() {
        player?.setOnInfoListener(object : DefaultInfoListener() {
            // 播放到末尾
            override fun onPlayToEnd() {
                //pause()
                infoListeners.forEach { it.onPlayToEnd() }
            }
        })
        addInfoListener(object : DefaultInfoListener() {

            override fun onPlayToEnd() {
                if(editorContext.isLoopPlay){  //若循环播放
                    seek(0)
                    play()
                    //LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PLAY)
                }else{  //若不需要循环播放
                    LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PAUSE)
                }
            }
        })

    }

   private fun startPlayRange() {
        // 停掉区间播放
        isPlaying = true
        if (totalDuration() - curPosition() < 50) { //track轨道滑到最后，track返回的position不是末尾的值 做了此处理
            refreshCurrentFrame()
            seek(0)
        }
        player?.play()
        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PLAY)
    }

    override fun play() {
        // 停掉区间播放
        cancelPlayRange()

        if (totalDuration() - curPosition() < 50) { //track轨道滑到最后，track返回的position不是末尾的值 做了此处理
            refreshCurrentFrame()
            seek(0)
        }
        player?.play()
        isPlaying = true
        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PLAY)
    }

    override fun resume() {
        if (!editorContext.videoEditor.isCompilingVideo) {
            if (activityResultFlag) {
                //复位
                activityResultFlag = false
                return
            }
            if(posWhenEditorSwitch > 0){
                return
            }
            if (playPositionWhenCompile > 0) {
                seek(0)
                seek(playPositionWhenCompile)
            } else {
                seek(0)
            }
        }
    }

    /**
     * 调用pause后
     *  1.暂停播放
     *  2.播放图标置为暂停状态
     *  3.停止移动轨道
     *  4.停止计算当前播放时间的runnable
     */
    override var isPlayWhileEditorSwitch = false
    override fun pause() {
        cancelPlayRange()
        isPlayWhileEditorSwitch = isPlaying
        isPlaying = false
        player?.pause()
        LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PAUSE)
    }

    override fun prepare() {
        player?.prepare()
    }

    override fun destroy() {
        player?.destroy()
        cancelPlayRange()
    }

    private val playRangeHandler = Handler(Looper.getMainLooper())

    inner class PlayRunnable(private val playEndTime: Int, val seek: Boolean) : Runnable {
        override fun run() {
            val curPosition = curPosition()
            DLog.d("playDuration current $curPosition")
            if (curPosition >= playEndTime - 30) {
                DLog.d(" playDuration current pause cur=$curPosition ,endTime=$playEndTime")
                pause()
                if (seek) {
                    seek(playEndTime)
                }
                cancelPlayRange()
                LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java).postValue(NLEEditorContext.STATE_PAUSE)
            } else {
                DLog.d(" playDuration postDelayed")
                playRangeHandler.postDelayed(this, 20)
            }
        }

    }

    override fun isInPlayRange(): Boolean {
        return isInPlayRange
    }

    override fun playRange(seqIn: Int, seqOut: Int, seek: Boolean) {
        DLog.d("start playDuration  $seqIn $seqOut")
        cancelPlayRange(false)
        isInPlayRange = true
        player?.seekDone(seqIn, object : ISeekListener{
            override fun onSeekDone(ret: Int) {
                DLog.d(" playDuration onSeekDone")
                playRangeHandler.postDelayed({ startPlayRange() },20)
                playRangeHandler.postDelayed(PlayRunnable(seqOut,seek),30)
            }
        })

        /*player?.seekDone(seqIn, object : ISeekListener {
            override fun onSeekDone(ret: Int) {
                player?.pause()
                player?.setInOut(seqIn, seqOut)
                addInfoListener(object : DefaultInfoListener() {

                    override fun onPlayToEnd() {
                        super.onPlayToEnd()
                        val curPos = player?.getCurrentPosition()?.toInt() ?: 0
                        GlobalScope.launch(Dispatchers.Main) {
                            // bug,不同线程同时调用，卡3s左右
                            player?.setInOut(0, -1)
                            player?.seek(curPos / 1000, flags = SeekMode.EDITOR_SEEK_FLAG_LastSeek)
                            player?.refreshCurrentFrame()
                        }
                        removeInfoListener(this)
                    }
                })
                play()
            }
        })*/
    }

    fun addInfoListener(defaultInfoListener: DefaultInfoListener) {
        infoListeners.add(defaultInfoListener)
    }

    fun removeInfoListener(defaultInfoListener: DefaultInfoListener) {
        infoListeners.remove(defaultInfoListener)
    }

    override fun seekToPosition(position: Int, model: SeekMode, ifMoveTrack: Boolean) {
//        val selectedTrackEndTime = editorContext.selectedNleTrack?.maxEnd?.div(1000)?.toInt()
//        player?.also {
//            if (selectedTrackEndTime != null) { //在选中轨道的情况下
//                if (position > selectedTrackEndTime) {
//                    it.seek(selectedTrackEndTime, model)
//                } else {
//                    it.seek(position, model)
//                }
//            } else {  //在没选中轨道的情况下
//                var longestTrackEndTime = 0 //寻找拥有最长末尾时间的track。
//                editorContext.nleModel.tracks.forEach {
//                    if ((it.maxEnd / 1000).toInt() > longestTrackEndTime) longestTrackEndTime =
//                        (it.maxEnd / 1000).toInt()
//                }
//                if (position > longestTrackEndTime) { //在删除了最长的track中最后的slot时，是有可能走这段逻辑的
//                    it.seek(longestTrackEndTime, model)
//                } else {
//                    it.seek(position, model)
//                }
//            }
//        }
        player?.seek(position,model)
        if (ifMoveTrack) {
            LiveDataBus.getInstance().with(Constants.KEY_MAIN, Int::class.java)
                .postValue(NLEEditorContext.STATE_SEEK)
        }
    }

    override fun seek(position: Int) {
        player?.seek(position, SeekMode.EDITOR_SEEK_FLAG_LastSeek)
        cancelPlayRange()
    }

    private fun cancelPlayRange(resetFlag: Boolean = true) {
        ifCanLog { DLog.i(TAG, "cancelPlayRange$resetFlag") }
        playRangeHandler.removeCallbacksAndMessages(null)
        if (resetFlag) {
            playRangeHandler.postDelayed({ isInPlayRange = false }, 20)//延迟20ms标记退出区间播放
        }
    }

    // 单位：毫秒
    override fun curPosition(): Int {
        return ((player?.getCurrentPosition() ?: 0) / 1000).toInt()
    }

    // 单位：毫秒
    override fun totalDuration(): Int {
        return player?.getDuration() ?: 0
    }

    override fun getReverseVideoPaths(): Array<String> {
        return player?.reverseVideoPaths?.toTypedArray() ?: emptyArray()
    }

    override fun refreshCurrentFrame() {
        player?.refreshCurrentFrame()
    }

    override fun getImages(timeStamps: IntArray, width: Int, height: Int, listener: IGetImageListener) {
        this.player?.getImages(timeStamps, width, height, listener)
    }

    override fun cancelGetVideoFrames() {
        this.player?.cancelGetVideoFrames()
    }

}