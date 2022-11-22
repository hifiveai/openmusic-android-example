package com.ss.ugc.android.editor.base.theme


interface IEditorUIConfig {
    //自定义全局样式
    fun customizeGlobalUI(): GlobalUIConfig?

    //自定义底部操作栏样式
    fun customizeBottomUI(): BottomUIConfig?

    //自定义预览区样式
    fun customizePreviewUI(): PreviewUIConfig?

    //自定义多轨区样式
    fun customizeTrackUI(): TrackUIConfig?

}