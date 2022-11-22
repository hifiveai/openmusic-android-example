package com.ss.ugc.android.editor.main.template

open class SlotDataITem(
    var trackId: Long,
    var slotId: Long,
    var isSelected: Boolean = false,
    var isLocked: Boolean = false,
    var slotStartTime:Long = 0, // ms
) {
}