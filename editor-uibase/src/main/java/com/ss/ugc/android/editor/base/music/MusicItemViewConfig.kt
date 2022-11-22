package com.ss.ugc.android.editor.base.music

import com.ss.ugc.android.editor.base.R

/**
 * @date: 2021/8/6
 * @desc:
 */
class MusicItemViewConfig private constructor(builder: Builder) {

    val musicImageViewWidth = builder.musicImageViewWidth
    val musicImageViewHeight = builder.musicImageViewHeight
    val placeHolderDrawable = builder.placeHolderDrawable
    val isLocalMusic = builder.isLocalMusic
    val usePlayIcon = builder.usePlayIcon
    val hideIconWhenPlayMusic = builder.hideIconWhenPlayMusic
    val showProgressText = builder.showProgressText
    val enableMusicWave = builder.enableMusicWave
    val itemLayoutId = builder.itemLayoutId
    val needShowFooter = builder.needShowFooter

    class Builder {
        var musicImageViewWidth: Float = 55.0f  //音乐图片宽
        var musicImageViewHeight: Float = 55.0f //音乐图片高
        var placeHolderDrawable: Int = R.drawable.default_music_item_icon  //兜底图标
        var isLocalMusic: Boolean = false   //是否是本地音乐
        var usePlayIcon: Boolean = false    //是否在音乐图片上使用播放小图标
        var hideIconWhenPlayMusic: Boolean = false //当播放音乐时隐藏音乐图标
        var showProgressText: Boolean = false  //显示播放进度条
        var enableMusicWave: Boolean = false  //是否显示播放声浪
        var itemLayoutId: Int = 0//自定义item布局
        var needShowFooter: Boolean = true//是否需要显示底部footer(没有更多了)
        fun setMusicImageViewWidth(musicImageViewWidth: Float) =
            apply { this.musicImageViewWidth = musicImageViewWidth }

        fun setMusicImageViewHeight(musicImageViewHeight: Float) =
            apply { this.musicImageViewHeight = musicImageViewHeight }

        fun setPlaceHolderDrawable(placeHolderDrawable: Int) =
            apply { this.placeHolderDrawable = placeHolderDrawable }

        fun setIsLocalMusic(isLocalMusic: Boolean) = apply { this.isLocalMusic = isLocalMusic }
        fun setHideIconWhenPlayMusic(hideIconWhenPlayMusic: Boolean) =
            apply { this.hideIconWhenPlayMusic = hideIconWhenPlayMusic }

        fun setShowProgressText(showProgressText: Boolean) =
            apply { this.showProgressText = showProgressText }

        fun setEnableMusicWave(enableMusicWave: Boolean) =
            apply { this.enableMusicWave = enableMusicWave }

        fun setUsePlayIcon(usePlayIcon: Boolean) = apply { this.usePlayIcon = usePlayIcon }
        fun setItemLayout(res: Int) = apply { this.itemLayoutId = res }
        fun setNeedShowFooter(show: Boolean) = apply { this.needShowFooter = show }
        fun build(): MusicItemViewConfig {
            return MusicItemViewConfig(this)
        }
    }
}