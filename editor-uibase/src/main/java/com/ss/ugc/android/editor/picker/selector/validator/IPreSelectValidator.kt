package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

/**
 * 选择前的验证逻辑
 */
interface IPreSelectValidator {
    fun preCheck(incoming: MediaItem, selected: List<MediaItem>): Boolean
}