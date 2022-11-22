package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

class MaxSizePreValidator(
    private val maxSize: Long,
    private val onValidatePassed: (() -> Unit)? = null,
    private val onInValidate: (() -> Unit)? = null
) : IPreSelectValidator {
    override fun preCheck(incoming: MediaItem, selected: List<MediaItem>): Boolean {
        return if (incoming.size < maxSize) {
            onValidatePassed?.invoke()
            true
        } else {
            onInValidate?.invoke()
            false
        }
    }
}