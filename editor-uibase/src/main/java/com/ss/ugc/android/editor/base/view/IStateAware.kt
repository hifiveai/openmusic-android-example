package com.ss.ugc.android.editor.base.view


interface IStateAware <STATE : Enum<STATE>>{

    fun setState(state: STATE)

    fun getState(): STATE
}