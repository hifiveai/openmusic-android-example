package com.ss.ugc.android.editor.bottom.panel.music

import com.ss.ugc.android.editor.bottom.R

class MusicItemViewConfig private constructor(builder: Builder) {

    val enableMusicWave = builder.enableMusicWave
    val isLocalMusic = builder.isLocalMusic
    val usePlayIcon = builder.usePlayIcon
    val placeHolderDrawable = builder.placeHolderDrawable
    val musicImageViewWidth = builder.musicImageViewWidth
    val musicImageViewHeight = builder.musicImageViewHeight
    val hideIconWhenPlayMusic = builder.hideIconWhenPlayMusic
    val showProgressText = builder.showProgressText

    class Builder {
        var enableMusicWave: Boolean = true //是否显示播放声浪
        var isLocalMusic: Boolean = false   //是否是本地音乐
        var usePlayIcon: Boolean = false    //是否在音乐图片上使用播放小图标
        var placeHolderDrawable: Int = R.drawable.default_music_item_icon  //兜底图标
        var musicImageViewWidth: Float = 55.0f  //音乐图片宽
        var musicImageViewHeight: Float = 55.0f //音乐图片高
        var hideIconWhenPlayMusic: Boolean = false //当播放音乐时隐藏音乐图标
        var showProgressText: Boolean = false  //显示播放进度条

        fun setEnableMusicWave(enableMusicWave: Boolean) = apply { this.enableMusicWave = enableMusicWave }
        fun setIsLocalMusic(isLocalMusic: Boolean) = apply { this.isLocalMusic = isLocalMusic }
        fun setHideIconWhenPlayMusic(hideIconWhenPlayMusic: Boolean) = apply { this.hideIconWhenPlayMusic = hideIconWhenPlayMusic }
        fun setUsePayIcon(usePlayIcon: Boolean) = apply { this.usePlayIcon = usePlayIcon }
        fun setPlaceHolderDrawable(placeHolderDrawable: Int) = apply { this.placeHolderDrawable = placeHolderDrawable }
        fun setMusicImageViewWidth(musicImageViewWidth: Float) = apply { this.musicImageViewWidth = musicImageViewWidth }
        fun setMusicImageViewHeight(musicImageViewHeight: Float) = apply { this.musicImageViewHeight = musicImageViewHeight }
        fun setShowProgressText(showProgressText: Boolean) = apply { this.showProgressText = showProgressText }

        fun build(): MusicItemViewConfig {
            return MusicItemViewConfig(this)
        }
    }
}