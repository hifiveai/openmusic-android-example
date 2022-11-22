package com.ss.ugc.android.editor.core.manager

import android.view.SurfaceView
import com.bytedance.ies.nlemedia.IGetImageListener
import com.bytedance.ies.nlemedia.SeekMode
import com.bytedance.ies.nlemediajava.NLEPlayer

interface IVideoPlayer {

    var player: NLEPlayer?

    var isPlaying: Boolean

    var isPlayingInFullScreen: Boolean

    //onActivityResult 标志位，用于处理seek逻辑
    var activityResultFlag: Boolean

    //保存compile发生时的位置
    var playPositionWhenCompile: Int

    //当切换出编辑页时，视频的播放位置
    var posWhenEditorSwitch :Int

    fun init(surfaceView: SurfaceView, workSpace: String? = null)

    fun play()

    fun resume()

    fun pause()

    fun prepare()

    fun destroy()

    fun playRange(seqIn: Int, seqOut: Int, seek: Boolean = false)
    fun isInPlayRange():Boolean

    fun seekToPosition(
        position: Int,
        model: SeekMode = SeekMode.EDITOR_SEEK_FLAG_LastSeek,
        ifMoveTrack: Boolean = true
    )

    fun seek(position: Int)

    fun curPosition(): Int

    fun totalDuration(): Int

    fun getReverseVideoPaths(): Array<String>

    fun refreshCurrentFrame()

    fun getImages(timeStamps: IntArray, width: Int, height: Int, listener: IGetImageListener)

    fun cancelGetVideoFrames()

    var isPlayWhileEditorSwitch:Boolean
}