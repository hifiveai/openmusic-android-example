package com.ss.ugc.android.editor.preview

import com.ss.ugc.android.editor.preview.subvideo.VideoGestureLayout


interface IAdapter{
    fun attach(videoGestureLayout: VideoGestureLayout)

    fun detach()

    fun onOrientationChange(orientation: Int?)
}