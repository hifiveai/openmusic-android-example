package com.ss.ugc.android.editor.core.api.video

import android.graphics.PointF

const val CURVE_SPEED_NAME = "curve_speed_name"

data class CropParam(val leftTop: PointF?, val rightTop: PointF?, val leftBottom: PointF?, val rightBottom: PointF?)


/**
 * 视频片段的蒙版
 *
 * @param maskWidth 蒙版宽度
 * @param maskHeight 蒙版高度
 * @param maskCenterX 蒙版中心点x轴 (-0.5-0.5)
 * @param maskCenterY 蒙版中心点y轴 (-0.5-0.5)
 * @param maskRotate 蒙版旋转角度 (0-360)
 * @param maskRoundCorner 蒙版圆角
 * @param invert 蒙版是否反转
 * @param maskFeather 蒙版羽化
 */
data class MaskParam(
    val maskWidth: Float?,
    val maskHeight: Float?,
    val maskCenterX: Float?,
    val maskCenterY: Float?,
    val maskRotate: Float?,
    val maskRoundCorner: Float?,
    val invert: Boolean?,
    val maskFeather: Float?,
    val isDone: Boolean?
)

data class ChangeSpeedParam(
    val speed: Float?,
    val changeTone: Boolean?,
    val keepPlay: Boolean = false,
    val listener: IChangeSpeedListener?
)

data class ChangeCurveSpeedParam(
    val curvePoints: List<PointF>,
    val name: String,
    val listener: IChangeCurveSpeedListener?
)

data class AudioFilterParam(
    val audioFilterPath: String,  //音色资源路径
    val name: String       //变声名称
)
