package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.ugc.android.editor.picker.data.model.MediaItem

class MinCountConfirmValidator(
    private val minCount: Int,
    private val onValidatePassed: (() -> Unit)? = null,
    private val onInValidate: (() -> Unit)? = null
) : IConfirmValidator {
    override fun check(incoming: MediaItem, selected: List<MediaItem>): Boolean {
        return if (selected.size >= minCount) {
            onValidatePassed?.invoke()
            true
        } else {
            onInValidate?.invoke()
            false
        }
    }
}