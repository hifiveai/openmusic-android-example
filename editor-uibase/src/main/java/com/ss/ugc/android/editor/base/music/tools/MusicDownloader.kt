package com.ss.ugc.android.editor.base.music.tools

import android.text.TextUtils
import com.ss.ugc.android.editor.base.constants.PathConstants
import com.ss.ugc.android.editor.base.download.DownloadManager
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.music.data.getFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet

object MusicDownloader {

    private val downloadIdSet = CopyOnWriteArraySet<Long>()
    private val musicDirPath = PathConstants.DOWNLOAD_MUSIC_SAVE_PATH

    suspend fun download(item: MusicItem): Boolean {
        downloadIdSet.add(item.id)
        return withContext(Dispatchers.IO) {
            coroutineContext[Job]?.invokeOnCompletion {
                downloadIdSet.remove(item.id)
            }
            val result = if (item.previewUrl.isNullOrBlank()) {
                false
            } else {
                val musicDir = File(musicDirPath + File.separator + item.id)
                if (!musicDir.exists()) {
                    musicDir.mkdirs()
                }
                val musicFileName = item.getFileName()
                DownloadManager.download(item.previewUrl!!, musicDir.absolutePath, musicFileName)
            }
            result
        }
    }


    fun isDownloading(item: MusicItem): Boolean {
        return downloadIdSet.contains(item.id)
    }

    fun isDownLoaded(item: MusicItem): Boolean {
        if (TextUtils.isEmpty(item.previewUrl)) {
            return false
        }
        val musicFileName = item.getFileName()
        val file = File(musicDirPath + File.separator + item.id, musicFileName)
        if (file.exists()) {
            return true
        }
        return false
    }

    fun getDownloadedPath(item: MusicItem): String? {
        return if (isDownLoaded(item)) {
            val musicFileName = item.getFileName()
            musicDirPath + File.separator + item.id + File.separator + musicFileName
        } else {
            null
        }
    }
}