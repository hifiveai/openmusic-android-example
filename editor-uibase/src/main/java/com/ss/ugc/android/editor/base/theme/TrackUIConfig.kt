package com.ss.ugc.android.editor.base.theme

import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes

class TrackUIConfig @JvmOverloads constructor(
    //视频轨道转场icon
    @DrawableRes
    var trackTransitionCustomIcon: Int? = null,
    //主轨道拖动的左边icon
    @DrawableRes
    var mainTrackLeftMoveCustomIcon: Int? = null,
    //主轨道拖动的右边icon
    @DrawableRes
    var mainTrackRightMoveCustomIcon: Int? = null,
    //副轨拖动的左边icon
    @DrawableRes
    var viceTrackLeftMoveCustomIcon: Int? = null,
    //副轨拖动的右边icon
    @DrawableRes
    var viceTrackRightMoveCustomIcon: Int? = null,
    //添加素材的icon
    @DrawableRes
    var addMediaCustomIcon: Int? = null,
    //贴纸轨道颜色
    @ColorRes
    var stickerTrackColor: Int? = null,
    //图片轨道颜色
    @ColorRes
    var imageTrackColor: Int? = null,
    // 文字轨道颜色
    @ColorRes
    var textTrackColor: Int? = null,
    // 字幕轨道颜色
    @ColorRes
    var subtitleTrackColor: Int? = null,
    //特效轨道颜色
    @ColorRes
    var effectTrackColor: Int? = null,
    //录音时音频波浪线颜色
    @ColorRes
    var recordWaveColor: Int? = null,
    //音频波浪线颜色
    @ColorRes
    var audioWaveColor: Int? = null,
    // 录音后的音频波浪线颜色
    @ColorRes
    var afterRecordWaveColor: Int? = null,
    //音频轨道背景颜色
    @ColorRes
    var audioTrackBgColor: Int? = null,
    //录音过程中音频轨道背景颜色
    @ColorRes
    var recordTrackBgColor: Int? = null,
    // 主轨高度
    @Dimension(unit = Dimension.DP) var mainTrackHeight: Int? = null,
    //缩略图每帧高度
    @Dimension(unit = Dimension.DP) var itemFrameHeight: Int? = if (mainTrackHeight == null) null else mainTrackHeight,
    //缩略图每帧宽度
    @Dimension(unit = Dimension.DP) var mainTrackItemFrameWidth: Int? = null,

    // 副轨高度
    @Dimension(unit = Dimension.DP) var viceTrackHeight: Int? = null,
    // 副轨缩略图高度
    @Dimension(unit = Dimension.DP) var viceTrackItemFrameHeight: Int? = if (viceTrackHeight == null) null else viceTrackHeight,
    // 副轨缩略图宽度
    @Dimension(unit = Dimension.DP) var viceTrackItemFrameWidth: Int? = null,

    //主轨中自定义常规变速的icon
    @DrawableRes
    var mainTrackNormalChangeSpeedIcon: Int? = null,
    //主轨中自定义曲线变速的icon
    @DrawableRes
    var mainTrackCurveChangeSpeedIcon: Int? = null,
    //副轨中自定义常规变速的icon
    @DrawableRes
    var viceTrackNormalChangeSpeedIcon: Int? = null,
    //副轨中自定义曲线变速的icon
    @DrawableRes
    var viceTrackCurveChangeSpeedIcon: Int? = null,
    //开启原声icon
    @DrawableRes
    var enableOriginalVoiceIcon: Int? = null,
    //关闭原声icon
    @DrawableRes
    var disableOriginalVoiceIcon: Int? = null,
    //主轨上左上角 「关闭原声」的 小 icon
    @DrawableRes
    var disableOriginalVoiceTip: Int? = null,
    // 主轨道，开始处  是否显示设置封面Button
    var showCoverSettingUI: Boolean = true
)