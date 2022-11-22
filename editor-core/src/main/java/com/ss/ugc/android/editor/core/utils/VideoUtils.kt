package com.ss.ugc.android.editor.core.utils

import com.google.gson.annotations.SerializedName


object VECompileBpsConfig {

    @SerializedName("bps_for_1080p")
    val bpsFor1080p: Int = 1024 * 1024 * 16

    @SerializedName("bps_for_720p")
    val bpsFor720p: Int = 1024 * 1024 * 10

    @SerializedName("bps_for_480p")
    val bpsFor480p: Int = (1024 * 1024 * 4.5).toInt()

    @SerializedName("bps_for_2k")
    val bpsFor540p: Int = (1024 * 1024 * 4.5).toInt()

    @SerializedName("bps_for_2k")
    val bpsFor2K: Int = 1024 * 1024 * 21

    @SerializedName("bps_for_4k")
    val bpsFor4K: Int = 1024 * 1024 * 24

    @SerializedName("use_material_bps")
    val useMaterialBps: Boolean = false
}

