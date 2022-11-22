package com.ss.ugc.android.editor.base.event

class InfoStickerOperationEvent (val operation: InfoStickerOperationType)

enum class InfoStickerOperationType {
    DELETE, MIRROR, COPY, SCALE_ROTATE
}