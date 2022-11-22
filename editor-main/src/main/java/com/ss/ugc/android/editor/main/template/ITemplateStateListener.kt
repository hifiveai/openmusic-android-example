package com.ugc.andorid.editor.template

interface ITemplateStateListener {
    fun onPlayTimeChanged(curPlayTime:String,totalPlayTime:String)
    fun onPlayStateChanged(isChangeToPlay:Boolean)
    fun onPlayActivate()
    fun onPauseActivate()
}