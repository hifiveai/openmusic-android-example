package com.ss.ugc.android.editor.core.api.canvas

sealed class CanvasRatio
object ORIGINAL : CanvasRatio()
object RATIO_9_16 : CanvasRatio()
object RATIO_3_4 : CanvasRatio()
object RATIO_1_1 : CanvasRatio()
object RATIO_4_3 : CanvasRatio()
object RATIO_16_9 : CanvasRatio()