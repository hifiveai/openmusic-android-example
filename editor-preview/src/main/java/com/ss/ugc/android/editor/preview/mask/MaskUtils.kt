package com.ss.ugc.android.editor.preview.mask

import android.graphics.PointF

/**
 * on 2020-02-12.
 */
object MaskUtils {

    /**
     * 计算向量投影
     */
    fun calcProjectionVector(u: PointF, v: PointF): PointF {
        val uv = u.x * v.x + u.y * v.y
        val vv = v.x * v.x + v.y * v.y
        val rate = uv / vv
        return PointF(v.x * rate, v.y * rate)
    }
}
