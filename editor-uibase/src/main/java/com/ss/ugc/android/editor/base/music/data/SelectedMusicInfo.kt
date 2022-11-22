package com.ss.ugc.android.editor.base.music.data

import androidx.annotation.Keep
import com.ss.ugc.android.editor.base.music.data.MusicType.SONG

@Keep
data class SelectedMusicInfo(
    val title: String,
    val path: String,
    val type: MusicType = SONG,
    val musicId: String? = null
)

enum class MusicType {
    SONG, EFFECT
}