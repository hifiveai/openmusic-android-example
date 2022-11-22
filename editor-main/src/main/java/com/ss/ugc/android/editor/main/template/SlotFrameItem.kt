package com.ss.ugc.android.editor.main.template

class SlotFrameItem(
    trackId: Long,
    slotId: Long,
    isSelected: Boolean = false,
    isLocked: Boolean = false,
    slotStartTime: Long = 0,
    var mediaPath: String,
) : SlotDataITem(trackId, slotId,isSelected,isLocked,slotStartTime) {
}