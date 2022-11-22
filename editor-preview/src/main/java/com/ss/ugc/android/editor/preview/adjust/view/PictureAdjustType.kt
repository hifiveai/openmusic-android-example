//package com.ss.ugc.android.editor.preview.adjust.view
//
//
///**
// * 画面调节info
// * on 2019-07-25.
// */
//enum class PictureAdjustType(
//    val typeValue: String = "",
//    val defaultValue: Int = 0,
//    val rangeMin: Int = 0,
//    val rangeMax: Int = 100,
//    val baseRange: Int = 100,
//    val path: String = ""
//) {
//    None,
//    All,
//    BRIGHTNESS(
//        "brightness", 0, -50, 50, 50, InnerResourceHelper.getBrightnessPath(ModuleCommon.application) ?: ""
//    ),
//    CONTRAST(
//        "contrast", 0, -50, 50, 50, InnerResourceHelper.getContrastPath(ModuleCommon.application) ?: ""
//    ),
//    SATURATION(
//        "saturation", 0, -50, 50, 50, InnerResourceHelper.getSaturationPath(ModuleCommon.application) ?: ""
//    ),
//    SHARP(
//        "sharp", 0, 0, 100, 100, InnerResourceHelper.getSharpPath(ModuleCommon.application) ?: ""
//    ),
//    HIGHLIGHT(
//        "highlight", 0, -50, 50, 50, InnerResourceHelper.getHighlightPath(ModuleCommon.application) ?: ""
//    ),
//    SHADOW(
//        "shadow", 0, 0, 100, 100, InnerResourceHelper.getShadowPath(ModuleCommon.application) ?: ""
//    ),
//    COLOR_TEMPERATURE(
//        "color_temperature", 0, -50, 50, 50, InnerResourceHelper.getTemperaturePath(ModuleCommon.application) ?: ""
//    ),
//    HUE(
//        "hue", 0, -50, 50, 50, InnerResourceHelper.getTonePath(ModuleCommon.application) ?: ""
//    ),
//    FADE(
//        "fade", 0, 0, 100, 100, InnerResourceHelper.getFadePath(ModuleCommon.application) ?: ""
//    ),
//    LIGHT_SENSATION(
//        "light_sensation", 0, -50, 50, 50, InnerResourceHelper.getLightSensationPath(ModuleCommon.application) ?: ""
//    ),
//    VIGNETTING(
//        "vignetting", 0, 0, 100, 100, InnerResourceHelper.getVignettingPath(ModuleCommon.application) ?: ""
//    ),
//    PARTICLE(
//        "particle", 0, 0, 100, 100, InnerResourceHelper.getParticlePath(ModuleCommon.application) ?: ""
//    ),
//}
