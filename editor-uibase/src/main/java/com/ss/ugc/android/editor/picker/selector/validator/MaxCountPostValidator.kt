package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

class MaxCountPostValidator(
    private val maxCount: Int,
    private val onValidatePassed: (() -> Unit)? = null,
    private val onInValidate: (() -> Unit)? = null
) : IPostSelectValidator {
    override fun postCheck(incoming: MediaItem, selected: List<MediaItem>): Boolean {
        return if (selected.size >= maxCount) {
            onValidatePassed?.invoke()
            true
        } else {
            onInValidate?.invoke()
            false
        }
    }
}