package com.ss.ugc.android.editor.preview

import com.ss.ugc.android.editor.preview.infosticker.InfoStickerGestureView
import com.ss.ugc.android.editor.preview.subvideo.SubVideoViewHolder

interface OnViewPrepareListener{
    fun onVideoGestureViewPrepare(subVideoViewHolder: SubVideoViewHolder){}
    fun onInfoStikerViewPrepare(infoStickerGestureView: InfoStickerGestureView){}
}