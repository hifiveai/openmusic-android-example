package com.ss.ugc.android.editor.base.resource.base

import com.ss.ugc.android.editor.base.ResourceConfig
import com.ss.ugc.android.editor.base.resource.*


interface IResourceProvider {

    val resourceConfig: ResourceConfig

    //是否使用内置资源
    fun isUseBuildInResource(): Boolean

    fun fetchResourceList(
        panel: String,
        downloadAfterFetch: Boolean,
        listener: ResourceListListener<ResourceItem>?
    )

    fun isResourceReady(resourceId: String): Boolean

    fun fetchResource(resourceId: String, listener: ResourceDownloadListener)

    fun updateResource(
        resourceId: String,
        resourcePath: String,
        extraParams: Map<String, String>,
        listener: ResourceDownloadListener
    )

    fun pushResource(
        resourcePath: String,
        platform: String,
        extraParams: Map<String, String>,
        listener: ResourceDownloadListener
    )

    fun getTextList(jsonFileName: String): MutableList<ResourceItem>?

    fun fetchTextList(jsonFileName: String, listener: SimpleResourceListener<ResourceItem>?)

    fun fetchCategoryResourceList(
        panel: String,
        category: String,
        listener: ResourceListListener<ResourceItem>?
    )

    fun fetchPanelInfo(
        panel: String,
        listener: ResourceListListener<CategoryInfo>?
    )

    fun getCanvasBlurList(): ArrayList<ResourceItem>
}
