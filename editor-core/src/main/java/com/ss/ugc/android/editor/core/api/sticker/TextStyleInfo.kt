package com.ss.ugc.android.editor.core.api.sticker

import com.ss.ugc.android.editor.core.EditorCoreInitializer
import com.ss.ugc.android.editor.core.utils.GsonUtil

/**
 * time : 2021/1/6
 *
 * description :
 * 文字样式 bean
 */
class TextStyleInfo {
    var alignType = 0
    var background = false
    var backgroundColor: List<Int>? = null
    var boldWidth = 0
    var charSpacing = 0
    var effectPath: String? = null
    var fallbackFontPathList: List<String>? = null
    var fontPath: String? = null
    var fontSize = 0
    var innerPadding = 0.0
    var italicDegree = 0
    var ktvColor: List<Int>? = null
    var ktvOutlineColor: List<Int>? = null
    var ktvShadowColor: List<Int>? = null
    var lineGap = 0.0
    var lineMaxWidth = 0
    var outline = false
    var outlineColor: List<Int>? = null
    var outlineWidth = 0.0
    var shadow = false
    var shadowColor: List<Int>? = null
    var shadowOffset: List<Double>? = null
    var shadowSmoothing = 0.0
    var shapeFlipX = false
    var shapeFlipY = false
    var shapePath: String? = null
    var text: String? = null
    var textColor: List<Double>? = null
    var typeSettingKind = 0
    var underline = false
    var underlineOffset = 0.0
    var underlineWidth = 0.0
    var useEffectDefaultColor = false
    var version: String? = null


    companion object {
        fun emptySticker(): TextStyleInfo {
            return GsonUtil.fromJson(EditorCoreInitializer.instance.getDefaultTextStyle(), TextStyleInfo::class.java)
        }
    }

}