package com.ss.ugc.android.editor.base.network

import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

enum class RequestType {
    GET,
    POST,
    FILE
}

class RequestInfo(val url: String) {
    var requestType: RequestType = RequestType.GET
    var paramsMap: HashMap<String, Any>? = null
    var queryMap: HashMap<String, String> = HashMap()
    var headerMap: HashMap<String, String> = HashMap()

    var fileList: ArrayList<FileInfo> = ArrayList()
    var errorMsg: String? = null
    var body: ByteArray? = null
    var contentType: String? = null
    override fun toString(): String =
        "RequestInfo(url='$url', requestType=$requestType, paramsMap=$paramsMap, queryMap=$queryMap, headerMap=$headerMap, " +
                "fileList=$fileList, errorMsg=$errorMsg, body=${body?.toString(Charsets.UTF_8)}, contentType=$contentType)"
}

data class FileInfo(val mimeType: String, val path: String, var fileName: String = "")

class ResponseInfo(val isSuccess: Boolean, val inputStream: InputStream?) {
    var contentLength: Long = 0
    var code: Int = 0
}

interface INetWorker {
    @Throws(IOException::class)
    fun execute(requestInfo: RequestInfo): ResponseInfo?
}