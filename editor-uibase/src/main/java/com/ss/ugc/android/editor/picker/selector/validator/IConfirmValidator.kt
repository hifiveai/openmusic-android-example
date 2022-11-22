package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

/**
 * 验证是否满足提交条件。在选择或者取消选择的时候触发
 */
interface IConfirmValidator {
    fun check(incoming: MediaItem, selected: List<MediaItem>): Boolean
}