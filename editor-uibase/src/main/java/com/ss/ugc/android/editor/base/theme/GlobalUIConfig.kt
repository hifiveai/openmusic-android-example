package com.ss.ugc.android.editor.base.theme

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R


class GlobalUIConfig @JvmOverloads constructor(
    //深色Drawable背景（Positive按钮）
    @DrawableRes var btnBgDrawableRes: Int = 0,
    //浅色Drawable背景（Negative按钮）
    @DrawableRes var lightBtnBgDrawableRes: Int = 0,

    @DrawableRes var undoIconRes: Int = 0,
    @DrawableRes var redoIconRes: Int = 0,
    @DrawableRes var playIconRes: Int = 0,
    @DrawableRes var closeIconRes: Int = 0,

    @ColorRes var themeColorRes: Int = R.color.tv_bottom_color,

    //数据请求Loading
    var lottieDataRequestLoadingJson: String = "lottie_double_points_loading.json",
    //视频处理（合成、倒放）Loading
    var lottieVideoProcessLoadingJson: String = "lottie_double_dot_loading.json",
    //
    //var lottieSubtitleProcessLoadingJson: String = "lottie_double_points_loading.json"

    /*
    是否启用剪映样式的导出页
     */
    var applyLVStyleCompile: Boolean = false,
    /**
     * 是否默认显示弹窗的关闭按钮
     */
    var showDialogCloseBtn: Boolean = false
)