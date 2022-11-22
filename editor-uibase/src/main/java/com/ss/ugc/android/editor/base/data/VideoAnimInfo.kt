package com.ss.ugc.android.editor.base.data




const val MAX_VIDEO_ANIM_DURATION = 60000L
const val MIN_VIDEO_ANIM_DURATION = 100L
const val ID_NONE = "none"
const val ANIM_IN = "in"
const val ANIM_OUT = "out"
const val ANIM_GROUP = "group"


data class VideoAnimInfo(
    /**
     * 名字
     */
    val name: String,

    /**
     * 效果的标识
     */
    val effectId: String,

    /**
     * 资源id
     */
    val effectResId: String,

    /**
     * 文件路径，注意：这个路径应该是草稿目录下的相对路径
     */
    val path: String,

    /**
     * 动画时长，默认500ms
     */
    val duration: Long = 500,

    val minDuration: Long = MIN_VIDEO_ANIM_DURATION,

    val maxDuration: Long = MAX_VIDEO_ANIM_DURATION,

    /**
     * resourceId
     */
    val resourceId: String,

    val categoryName: String = ""
)