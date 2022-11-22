package com.ss.ugc.android.editor.core.api.video

import android.graphics.PointF

data class ImageCoverInfo(
    val path: String,
    val cropLeftTop: PointF,
    val cropRightTop: PointF,
    val cropLeftBottom: PointF,
    val cropRightBottom: PointF
)
