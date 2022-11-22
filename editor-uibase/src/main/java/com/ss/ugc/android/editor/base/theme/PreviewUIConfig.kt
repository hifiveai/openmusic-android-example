package com.ss.ugc.android.editor.base.theme

import android.view.Gravity
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R

//全屏预览状态下的「关闭全屏」按钮的位置
enum class ExitFullScreenIConPosition{
    Left,
    Right,
    None,
}

data class PreviewUIConfig @JvmOverloads constructor(
    val stickerEditViewConfig: StickerEditViewConfig = StickerEditViewConfig(),
    val videoEditViewConfig: VideoEditViewConfig = VideoEditViewConfig(),
    var exitFullScreenIconConfig: ExitFullScreenIconConfig= ExitFullScreenIconConfig() //退出全屏icon样式
)

/**
 * 贴纸区控件配置，可设置包含矩形框颜色，矩形框粗细，居中提示线颜色，居中提示线长度，居中提示线粗细,各类图标
 */
data class StickerEditViewConfig(
    @ColorRes var rectColor: Int = 0, // 矩形框颜色
    @Dimension(unit = Dimension.DP) var rectStrokeWidth: Int = 0, // 矩形框粗细
    @ColorRes var adsorptionLineColor: Int = 0, // 居中提示线颜色
    @Dimension(unit = Dimension.DP) var adsorptionLineWidth: Int = 0, // 居中提示线粗细
    @Dimension(unit = Dimension.DP) var adsorptionLineLength: Int = 0, // 居中提示线长度
    var editIconConfig: EditIconConfig = EditIconConfig(), // 编辑图标样式
    val flipIconConfig: FlipIconConfig = FlipIconConfig(
        flipIconDrawableRes = R.drawable.ic_sticker_mirror,
        position = IconPosition.RIGHT_TOP), // 翻转图标样式
    var copyIconConfig: CopyIconConfig = CopyIconConfig(), // 拷贝图标样式
    var rotateIconConfig: RotateIconConfig = RotateIconConfig(), // 旋转图标样式
    var deleteIconConfig: DeleteIconConfig = DeleteIconConfig() // 删除图标样式

)

/**
 * 编辑图标配置
 */
data class EditIconConfig(
    @DrawableRes var editIconDrawableRes: Int = 0,
    val enable: Boolean = true
)

/**
 * 翻转图标配置
 */
data class FlipIconConfig(
    @DrawableRes val flipIconDrawableRes: Int = 0,
    val position: IconPosition = IconPosition.NONE,
    val enable: Boolean = true
)

/**
 * 拷贝图标配置
 */
data class CopyIconConfig(
    @DrawableRes var copyIconDrawableRes: Int = 0,
    val enable: Boolean = true
)

/**
 * 旋转图标配置
 */
data class RotateIconConfig(
    @DrawableRes var rotateIconDrawableRes: Int = 0,
    val enable: Boolean = true
)

/**
 * 删除图标配置
 */
data class DeleteIconConfig(
    @DrawableRes var deleteIconDrawableRes: Int = 0,
    val enable: Boolean = true
)

/**
 * 退出全屏设置
 */
val DEFAULT_BIAS = 300
data class ExitFullScreenIconConfig(
    @DrawableRes var exitFullScreenIconDrawableRes: Int = 0,
    val position: ExitFullScreenIConPosition = ExitFullScreenIConPosition.None,
    val enable: Boolean = false,
    var bias:Int = DEFAULT_BIAS
)

/**
 * 视频区控件配置，可设置包含矩形框颜色，矩形框圆角，矩形框粗细，居中提示线颜色，居中提示线长度，居中提示线粗细
 */
data class VideoEditViewConfig(
    @ColorRes var rectColor: Int = 0, // 矩形框颜色
    @Dimension(unit = Dimension.DP) var rectCornerRadius: Int = 0,// 矩形框圆角
    @Dimension(unit = Dimension.DP) var rectStrokeWidth: Int = 0, // 矩形框粗细
    @ColorRes var adsorptionLineColor: Int = 0,                         // 居中提示线颜色
    @Dimension(unit = Dimension.DP) var adsorptionLineWidth: Int = 0, // 居中提示线粗细
    @Dimension(unit = Dimension.DP) var adsorptionLineLength: Int = 0  // 居中提示线长度
)

enum class IconPosition(val value: Int) {
    NONE(-1),//FrameLayout.LayoutParam中gravity默认值
    LEFT_TOP(Gravity.START or Gravity.TOP),//左上
    RIGHT_TOP(Gravity.END or Gravity.TOP),//右上
    LEFT_BOTTOM(Gravity.START or Gravity.BOTTOM),//左下
    RIGHT_BOTTOM(Gravity.END or Gravity.BOTTOM),//右下
}