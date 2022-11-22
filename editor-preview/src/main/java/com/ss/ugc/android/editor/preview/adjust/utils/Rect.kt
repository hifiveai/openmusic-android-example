package com.ss.ugc.android.editor.preview.adjust.utils

import android.graphics.Rect
import android.graphics.RectF

/**
 * Returns a [Rect] representation of this rectangle. The resulting rect will be sized such
 * that this rect can fit within it.
 */
inline fun RectF.toRect(): Rect {
    val r = Rect()
    roundOut(r)
    return r
}

/**
 * Returns a [RectF] representation of this rectangle.
 */
inline fun Rect.toRectF(): RectF = RectF(this)