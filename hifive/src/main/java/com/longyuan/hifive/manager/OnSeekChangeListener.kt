package com.longyuan.hifive.manager

/**
 * #duxiaxing
 * #date: 2022/7/26
 */
interface OnSeekChangeListener {
    fun onProgressChanged(progress : Int, isChanged : Boolean)

    fun onStartTrackingTouch()

    fun onStopTrackingTouch(progress : Int)
}