package com.ss.ugc.android.editor.base.resource

data class ResourceModel(
    var resourceItem: ResourceItem,
    var downloadState: DownloadState = DownloadState.INIT,
    var isSelect: Boolean = false
)