package com.longyuan.hifive.model

import java.io.Serializable

/**
 * #duxiaxing
 * #date: 2022/7/14
 */
data class HiFiveSongMenuModel(
    var id : Int,
    var name : String = "",
    var image : String = ""
) : Serializable
