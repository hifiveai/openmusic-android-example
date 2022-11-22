package com.ss.ugc.android.editor.base.event

/**
 * time : 2021/1/14
 *
 * description :
 *
 */
class TextOperationEvent(val operation: TextOperationType)

enum class TextOperationType {
    DELETE, FLIP, EDIT, COPY, SCALE_ROTATE
}
