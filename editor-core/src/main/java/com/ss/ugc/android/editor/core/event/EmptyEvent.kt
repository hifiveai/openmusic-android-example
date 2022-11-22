package com.ss.ugc.android.editor.core.event

/**
 * time : 2020/12/30
 *
 * description :
 *
 */
open class EmptyEvent
data class VolumeEvent(val volume: Float? = null)
data class ClosePanelEvent(val closeAll: Boolean = true)