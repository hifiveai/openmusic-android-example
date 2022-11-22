package com.ss.ugc.android.editor.base.download

import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.network.RequestInfo
import com.ss.ugc.android.editor.core.utils.DLog
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.resume
import kotlin.jvm.Throws

private data class DownloadParams(val url: String, val path: String, val name: String)

interface DownloadListener {
    fun onSucceed()
    fun onFailed()
    fun onProgressing(progress: Int)
}

object DownloadManager {

    private const val TAG = "DownloadManager"

    private val downloading = mutableMapOf<DownloadParams, TaskInfo>()
    private val callbackMap = mutableMapOf<DownloadParams, MutableList<DownloadListener>>()

    fun downloadSync(url: String, dir: String, name: String): Boolean {
        val path = dir + File.separator + name
        val file = File(path)
        if (file.exists() && file.isFile) {
            return true
        }

        val params = DownloadParams(url, dir, name)
        val taskInfo: TaskInfo
        synchronized(this@DownloadManager) {
            if (downloading.containsKey(params)) return true
            taskInfo = TaskInfo()
            downloading[params] = taskInfo
        }

        val failed = {
            forEachCallback(params) { it.onFailed() }
            finish(params)
        }

        val succeed = {
            forEachCallback(params) { it.onSucceed() }
            finish(params)
        }

        try {
            val netWorker = EditorSDK.instance.netWorker()
            val requestInfo = RequestInfo(url)
            val responseInfo = netWorker.execute(requestInfo)
            if (responseInfo == null || !responseInfo.isSuccess) {
                failed()
            } else {
                val result = writeStreamToFile(responseInfo.inputStream, path, responseInfo.contentLength, null)
                if (result != null) {
                    succeed()
                } else {
                    DLog.w(TAG, "download failed: $url")
                    failed()
                }
            }
        } catch (e: Exception) {
            DLog.w(TAG, "download failed: $url\n$e")
            failed()
        }
        return false
    }

    fun download(url: String, dir: String, name: String, listener: DownloadListener) {
        val path = dir + File.separator + name
        val file = File(path)
        // 文件存在，并且不是空文件才算是下载过的
        if (file.exists() && file.isFile && file.length() > 0) {
            listener.onSucceed()
            return
        }

        val params = DownloadParams(url, dir, name)
        val taskInfo: TaskInfo
        synchronized(this@DownloadManager) {
            var callbacks = callbackMap[params]
            if (callbacks == null) {
                callbacks = mutableListOf()
                callbackMap[params] = callbacks
            }

            callbacks.add(listener)

            if (downloading.containsKey(params)) return

            taskInfo = TaskInfo()
            downloading[params] = taskInfo
        }

        val failed = {
            forEachCallback(params) { it.onFailed() }
            finish(params)
        }

        val succeed = {
            forEachCallback(params) { it.onSucceed() }
            finish(params)
        }

        try {
            val netWorker = EditorSDK.instance.netWorker()
            val requestInfo = RequestInfo(url)
            val responseInfo = netWorker.execute(requestInfo)
            if (responseInfo == null || !responseInfo.isSuccess) {
                failed()
            } else {

                val result = writeStreamToFile(responseInfo.inputStream, path, responseInfo.contentLength, listener)
                if (result != null) {
                    succeed()
                } else {
                    DLog.w(TAG, "download failed: $url")
                    failed()
                }
            }
        } catch (e: Exception) {
            DLog.w(TAG, "download failed: $url\n$e")
            failed()
        }
    }

    suspend fun download(
        url: String,
        path: String,
        name: String,
        onProgressing: ((Int) -> Unit)? = null
    ) = suspendCancellableCoroutine<Boolean> { continuation ->
        continuation.invokeOnCancellation {
            cancel(url, path, name)
        }

        download(url, path, name, object : DownloadListener {
            override fun onSucceed() {
                continuation.resume(true)
            }

            override fun onFailed() {
                continuation.resume(false)
            }

            override fun onProgressing(progress: Int) {
                onProgressing?.invoke(progress)
            }
        })
    }

    private inline fun forEachCallback(params: DownloadParams, block: (DownloadListener) -> Unit) {
        val callbacks = mutableListOf<DownloadListener>()
        synchronized(this@DownloadManager) {
            callbackMap[params]?.let {
                callbacks.addAll(it)
            }
        }
        callbacks.forEach(block)
    }

    @Synchronized
    private fun finish(params: DownloadParams) {
        downloading.remove(params)
        callbackMap.remove(params)
    }

    @Synchronized
    fun cancel(url: String, dir: String, name: String) {
        val params = DownloadParams(url, dir, name)
        callbackMap.remove(params)
        downloading.remove(params)?.canceled = true
    }

    @Throws(IOException::class)
    fun writeStreamToFile(inputStream: InputStream?, filePath: String?, contentLength: Long, progressListener: DownloadListener?): File? {
        var outputStream: OutputStream? = null
        return try {
            val file = File(filePath)
            outputStream = FileOutputStream(file)
            var read = 0
            var writeLength = 0
            val bytes = ByteArray(4096)
            while (inputStream!!.read(bytes).also { read = it } != -1) {
                writeLength += read
                if (progressListener != null) {
                    if (writeLength < contentLength && contentLength > 0) {
                        progressListener.onProgressing((writeLength * 1f / contentLength * 100).toInt())
                    }
                }
                outputStream.write(bytes, 0, read)
            }
            file
        } catch (e: IOException) {
            DLog.e(TAG, e)
            removeFile(filePath)
            throw e
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    DLog.e(TAG, e)
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close()
                } catch (e: IOException) {
                    DLog.e(TAG, e)
                }
            }
        }
    }

    private fun removeFile(path: String?): Boolean {
        path ?: return false
        val file = File(path)
        return file.exists() && file.canWrite() && file.delete()
    }
}
