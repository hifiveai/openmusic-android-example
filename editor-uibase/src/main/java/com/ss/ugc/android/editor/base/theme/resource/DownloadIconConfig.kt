package com.ss.ugc.android.editor.base.theme.resource

import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import com.ss.ugc.android.editor.base.R


data class DownloadIconConfig(
    val enableDownloadIcon: Boolean = true,
    @Dimension(unit = Dimension.DP) val iconWidth: Int = 12,
    @Dimension(unit = Dimension.DP) val iconHeight: Int = 12,
    @DrawableRes val iconResource: Int = R.drawable.ic_download
)