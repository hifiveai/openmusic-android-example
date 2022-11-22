package com.longyuan.hifive.model

/**
 * #duxiaxing
 * #date: 2022/7/15
 */
data class HiFiveMusicModel(
    var id : String = "",
    var image : String = "",
    var name : String = "",
    var time : Int = 0,
    var author : String = "",
    var tag : String = "",
    var progress : Int = 0
){
    var isSelect = false//当前选中的音乐
    var isPlaying = false//是否在播放中
    var isInitAudio = false//是否完成播放前的相关操作，如波形图的获取与绘制
    override fun toString(): String {
        return "HiFiveMusicModel(id='$id', image='$image', name='$name', time=$time, author='$author', tag='$tag', progress=$progress)"
    }
}
