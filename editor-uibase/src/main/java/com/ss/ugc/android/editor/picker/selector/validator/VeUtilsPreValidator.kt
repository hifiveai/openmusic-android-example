package com.ss.ugc.android.editor.picker.selector.validator

import com.ss.android.vesdk.VEResult
import com.ss.android.vesdk.VEUtils
import com.ss.ugc.android.editor.core.fileInfo
import com.ss.ugc.android.editor.picker.data.model.MediaItem

/**
 *Author: gaojin
 *Time: 2022/3/3 3:38 下午
 */

class VeUtilsPreValidator(
    private val onInValidate: (() -> Unit)? = null
) : IPreSelectValidator {
    override fun preCheck(incoming: MediaItem, selected: List<MediaItem>): Boolean {
        if (incoming.isImage()) {
            incoming.rotation = fileInfo(incoming.path).rotation
            return true
        }
        return if (VEUtils.isCanImport(incoming.path) != VEResult.TER_OK) {
            onInValidate?.invoke()
            false
        } else {
            true
        }
    }
}