package com.ss.ugc.android.editor.base.theme

import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.theme.AlignInParent.RIGHT
import com.ss.ugc.android.editor.base.theme.resource.*

data class BottomUIConfig @JvmOverloads constructor(
    var funcBarViewConfig: FuncBarViewConfig = FuncBarViewConfig(),
    var optPanelViewConfig: OptPanelViewConfig = OptPanelViewConfig(),
    var resourceListViewConfig: ResourceListViewConfig = ResourceListViewConfig(),
    var voiceRecognizeViewConfig: VoiceRecognizeViewConfig = VoiceRecognizeViewConfig(),
    var filterPanelViewConfig: FilterPanelViewConfig = FilterPanelViewConfig(),
    var adjustPanelViewConfig: AdjustPanelViewConfig = AdjustPanelViewConfig(),
    var videoMaskPanelViewConfig: VideoMaskPanelViewConfig = VideoMaskPanelViewConfig()
)

data class FuncBarViewConfig @JvmOverloads constructor(
    @DrawableRes var funcBarBackgroundDrawableRes: Int = 0,
    @DrawableRes var backIconDrawableRes: Int = 0,
    @ColorRes var backIconContainerBackgroundColor: Int = 0,
    @Dimension(unit = Dimension.DP) var backIconMarginStart: Int = 0,
    @Dimension(unit = Dimension.DP) var funcBarHeight: Int = 60,
    @Dimension(unit = Dimension.DP) var itemImageViewWidth: Int = 0,
    @Dimension(unit = Dimension.DP) var itemImageViewHeight: Int = 0,
    @Dimension(unit = Dimension.SP) var itemTextViewSize: Int = 0,
    @ColorRes var itemTextViewColor: Int = -1,
    @Dimension(unit = Dimension.DP) var itemTextTopMargin: Int = 0,
    @Dimension(unit = Dimension.DP) var itemSpacing: Int = 0,
    var childrenAlignInParent: AlignInParent = RIGHT
)

data class OptPanelViewConfig @JvmOverloads constructor(
    @DrawableRes var closeIconDrawableRes: Int = 0,
    @ColorRes var slidingBarColor: Int = ThemeStore.globalUIConfig.themeColorRes,
    @Dimension(unit = Dimension.SP) var panelNameTextViewSize: Int = 0,
    @ColorRes var panelNameTextViewColor: Int = 0,
    @ColorRes var selectedItemColor: Int = ThemeStore.globalUIConfig.themeColorRes ?: 0
)

data class ResourceListViewConfig @JvmOverloads constructor(
    var loadingView: ((parent: ViewGroup) -> View)? = null,
    var emptyView: ((parent: ViewGroup) -> View)? = null,
    var errorRetryView: ((parent: ViewGroup) -> Pair<View, View>)? = null, // emptyView to retryView

    val downloadIconConfig: DownloadIconConfig = DownloadIconConfig(),
    val resourceImageConfig: ResourceImageConfig = ResourceImageConfig(),
    val resourceTextConfig: ResourceTextConfig = ResourceTextConfig(),
    val itemSelectorConfig: ItemSelectorConfig = ItemSelectorConfig(),
    val firstNullItemConfig: FirstNullItemConfig = FirstNullItemConfig(),
    val customItemConfig: CustomItemConfig = CustomItemConfig()
)


data class VoiceRecognizeViewConfig @JvmOverloads constructor(
    @DrawableRes var dialogCheckBoxDrawableRes: Int = R.drawable.selector_white_bg_checkbox_btn, //识别弹窗CheckBox样式
    var lottieSubtitleProcessLoadingJson:String = "lottie_double_points_loading.json",
    var dialogShowImage: Boolean = true   //识别弹窗是否显示图片
)

data class FilterPanelViewConfig @JvmOverloads constructor(
    //滤镜"无"项背景
    @DrawableRes val nullFilterBgRes: Int = R.drawable.null_filter,
    //滤镜"无"项icon
    @DrawableRes val nullFilterIconRes: Int = R.drawable.ic_item_filter_no,
    //滤镜选择框的icon
    @DrawableRes val filterSelectorIconRes: Int = R.drawable.filter_select_icon
)

data class AdjustPanelViewConfig @JvmOverloads constructor(
    @DrawableRes var brightnessIconRes: Int = 0,
    @DrawableRes var contrastIconRes: Int = 0,
    @DrawableRes var temperatureIconRes: Int = 0,
    @DrawableRes var saturationIconRes: Int = 0,
    @DrawableRes var fadeIconRes: Int = 0,
    @DrawableRes var highlightIconRes: Int = 0,
    @DrawableRes var shadowIconRes: Int = 0,
    @DrawableRes var vignettingIconRes: Int = 0,
    @DrawableRes var sharpIconRes: Int = 0,
    @DrawableRes var lightSensationIconRes: Int = 0, //曝光
    @DrawableRes var toneIconRes: Int = 0, //色调
)

data class VideoMaskPanelViewConfig @JvmOverloads constructor(
    //蒙版"清楚"按钮圆角显示
    @DrawableRes var nullItemResource: Int = R.drawable.null_filter_round,
    //蒙版按钮选择框样式
    @DrawableRes var selectorBorderRes: Int = R.drawable.item_bg_mask_selected,
    //蒙版背景透明
    @DrawableRes var backgroundResource: Int = R.drawable.transparent_image,
    @DrawableRes var resourcePlaceHolder: Int = R.drawable.null_filter
)

enum class AlignInParent {
    LEFT, CENTER, RIGHT
}