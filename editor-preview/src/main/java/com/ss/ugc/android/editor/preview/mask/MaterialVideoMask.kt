package com.ss.ugc.android.editor.preview.mask

import java.util.*

class MaterialVideoMask {

    enum class ResourceType {
        NONE, LINE, MIRROR, CIRCLE, RECTANGLE, GEOMETRIC_SHAPE;

        companion object {
            fun parseName(type: String): ResourceType {
                return when (type.toUpperCase(Locale.getDefault())) {
                    LINE.name -> LINE
                    MIRROR.name -> MIRROR
                    CIRCLE.name -> CIRCLE
                    RECTANGLE.name -> RECTANGLE
                    GEOMETRIC_SHAPE.name -> GEOMETRIC_SHAPE
                    else -> NONE
                }
            }
        }
    }
}