package com.ss.ugc.android.editor.base.resource

interface ResourceListListener<T> {
    fun onStart()
    fun onSuccess(dataList: List<T>)
    fun onFailure(exception: Exception?, tips: String? = null)
}


interface ResourceDownloadListener {
    fun onSuccess(resourceId: String, filePath: String)
    fun onProgress(resourceId: String, progress: Int, contentLength: Long)
    fun onFailure(resourceId: String, exception: Exception?)
}

interface ResourcesDownloadListener<T> {
    fun onSuccess(dataList: List<T>)

    fun onFailure()
}

interface SimpleResourceListener<T> {
    fun onSuccess(dataList: MutableList<T>)
}
