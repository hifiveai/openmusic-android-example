package com.ss.ugc.android.editor.preview.adjust

data class VECanvasData(
    var type: String,
    var blur: Float?,
    var image: String?,
    var colorAndroid: Int,
    var width: Int = 0,
    var height: Int = 0
)

data class VEInitData(
    var videoFilePaths: List<String>,
    var videoFileInfos: List<String>?,
    var vTrimIns: List<Int>,
    var vTrimOuts: List<Int>,
    var transitionList: List<VETransitionData>?,
    var audioFilePaths: List<String>?,
    var audioFileInfos: List<String>?,
    var aTrimIns: List<Int>?,
    var aTrimOuts: List<Int>?,
    var speed: List<Float>?,
    var canvasList: List<VECanvasData>,
    var videoRatio: String?
)

data class VETransitionData(
    var transitionName: String,
    var transitionDuration: Int
)