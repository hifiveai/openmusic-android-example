package com.ss.ugc.android.editor.main.template

class SlotTextItem(
    textTrackId: Long,
    textSlotId: Long,
    isSelected: Boolean = false,
    isLocked: Boolean = false,
    slotStartTime: Long = 0,
    var mainTrackSlotId: Long?,
    var textData: String,
    var mainTrackSlotMediaPath: String?, // 文本轨的其实时间对应主轨道上的slot的素材路径
) : SlotDataITem(textTrackId, textSlotId,isSelected,isLocked,slotStartTime) {
}