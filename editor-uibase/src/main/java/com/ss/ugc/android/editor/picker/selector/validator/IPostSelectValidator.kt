package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

/**
 * 选择后的验证逻辑
 */
interface IPostSelectValidator {
    fun postCheck(incoming: MediaItem, selected: List<MediaItem>): Boolean
}