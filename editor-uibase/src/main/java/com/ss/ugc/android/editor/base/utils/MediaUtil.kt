package com.ss.ugc.android.editor.base.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.text.TextUtils
import android.util.LruCache
import android.util.Size
import androidx.annotation.WorkerThread
import com.ss.android.vesdk.VEResult
import com.ss.android.vesdk.VEUtils
import com.ss.ugc.android.editor.base.data.AudioMetaDataInfo
import com.ss.ugc.android.editor.base.data.VideoMetaDataInfo
import com.ss.ugc.android.editor.base.logger.ILog
import java.io.File
import java.io.FileInputStream

object MediaUtil {

    private const val TAG = "MediaUtil"

    // 未经修改的videoInfo cache
    private val realVideoInfoCache = LruCache<String, VideoMetaDataInfo?>(500)
    private val audioInfoCache = LruCache<String, AudioMetaDataInfo?>(500)

    const val EXPECT_HEIGHT_VALUE: Int = 1920
    const val EXPECT_WIDTH_VALUE: Int = 1920

    /**
     * 获取真实信息
     */
    @Synchronized
    fun getRealVideoMetaDataInfo(path: String): VideoMetaDataInfo {
        var info = realVideoInfoCache[path]
        if (info == null) {
            if (isImage(path)) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, options)
                val rotation = ExifUtils.getExifOrientation(path)
                val size: Size = options.run {
                    Size(outWidth, outHeight)
                }
                info = VideoMetaDataInfo(
                    path = path,
                    width = size.width,
                    height = size.height,
                    rotation = rotation,
                    duration = 60 * 1000, // MediaUtil.DEFAULT_DURATION
                    codecInfo = "image"
                )
            } else {
                val veInfo = IntArray(11)
                val ret = VEUtils.getVideoFileInfo(path, veInfo)
                if (ret == VEResult.TER_OK &&
                    veInfo[0] > 0 && veInfo[1] > 0 && veInfo[10] > 0
                ) {
                    info = VideoMetaDataInfo(
                        path = path,
                        width = veInfo[0],
                        height = veInfo[1],
                        rotation = veInfo[2],
                        duration = veInfo[10],
                        longitude = veInfo[4],
                        latitude = veInfo[5],
                        bitrate = veInfo[6],
                        fps = veInfo[7],
                        codecId = veInfo[8],
                        videoDuration = veInfo[3],
                        codecInfo = VEUtils.getVideoEncodeTypeByID(veInfo[8]),
                        keyFrameCount = veInfo[9]
                    )
                } else {
                    ILog.e(TAG, "VEUtils.getVideoFileInfo $path: $ret")
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(path)
                        val width = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                        )?.toInt() ?: 0
                        val height = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                        )?.toInt() ?: 0
                        val rotation = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
                        )?.toInt() ?: 0
                        val duration = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_DURATION
                        )?.toInt() ?: 0
                        val bitrate = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_BITRATE
                        )?.toInt() ?: 0
                        val fps = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE
                        )?.toInt() ?: 0
                        val codecInfo = retriever.extractMetadata(
                            MediaMetadataRetriever.METADATA_KEY_MIMETYPE
                        ) ?: "unknown"

                        info = VideoMetaDataInfo(
                            path = path,
                            width = width,
                            height = height,
                            rotation = rotation,
                            duration = duration,
                            bitrate = bitrate,
                            fps = fps,
                            codecInfo = codecInfo
                        )
                    } catch (e: Exception) {
                        ILog.e(TAG, "MediaMetadataRetriever $path   $e")
                        info = VideoMetaDataInfo(
                            path = path,
                            width = 0,
                            height = 0,
                            rotation = 0,
                            duration = 0
                        )
                    } finally {
                        retriever.release()
                    }
                }
            }
            realVideoInfoCache.put(path, info)
        }
        return info!!
    }

    fun getVideoSize(path: String): Size {
        val info =
            getRealVideoMetaDataInfo(
                path
            )
        return Size(info.width, info.height)
    }

    fun getSizeForThumbnail(path: String, thumbWidth: Int): Size {
        var w = 0
        var h = 0
        try {
            val info =
                getRealVideoMetaDataInfo(
                    path
                )
            w = info.width
            h = info.height
        } catch (e: Exception) {
            ILog.e(TAG, "MediaMetadataRetriever exception $e")
        }
        return if (w > h) {
            Size(-1, thumbWidth)
        } else {
            Size(thumbWidth, -1)
        }
    }

    @Synchronized
    fun removeRealVideoCache(path: String?) {
        path?.let {
            realVideoInfoCache.remove(it)
        }
    }

    // sampleRate, channelSize, sampleSize, duration
    fun getAudioMetaDataInfo(path: String): AudioMetaDataInfo {
        val getAudioInfo = {
            val veInfo = IntArray(10)
            val ret = VEUtils.getAudioFileInfo(path, veInfo)
            val audioInfo = if (path.isEmpty() || !File(path).exists()) {
                ILog.e(TAG, "getAudioMetaDataInfo, path.exists()? ${File(path).exists()}, $path")
                AudioMetaDataInfo(
                    0
                )
            } else if (ret == VEResult.TER_OK && veInfo[3] > 0) {
                AudioMetaDataInfo(
                    veInfo[3]
                )
            } else {
                ILog.e(TAG, "getAudioMetaDataInfo veRet: $ret, veDuration: ${veInfo[3]}")
                val retriever = MediaMetadataRetriever()
                val duration = try {
                    retriever.setDataSource(path)
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0
                } catch (e: Exception) {
                    ILog.e(TAG, "getAudioMetaDataInfo MediaMetadataRetriever$e")
                    0
                }
                AudioMetaDataInfo(
                    duration = duration
                )
            }
            audioInfoCache.put(path, audioInfo)
            audioInfo
        }
        return audioInfoCache[path] ?: getAudioInfo()
    }




    private const val NOTIFY_ACTION = "android.intent.action.MEDIA_SCANNER_SCAN_FILE"
    private const val FILE_PROVIDER_AUTHORITY = "com.lemon.lv.provider"

    private val RIFF_HEAD = byteArrayOf(0x52, 0x49, 0x46, 0x46)
    private val WEBP_HEAD = byteArrayOf(0x57, 0x45, 0x42, 0x50)
    private val IMAGE_HEIC_HEAD = byteArrayOf(
        0x00, 0x00, 0x00,
        0x18, 0x66, 0x74,
        0x79, 0x70, 0x68,
        0x65, 0x69, 0x63
    )
    private val IMAGE_JPEG_HEAD = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val IMAGE_PNG_HEAD = byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A
    )
    private val IMAGE_GIF87A_HEAD =
        byteArrayOf(0x47.toByte(), 0x49, 0x46, 0x38, 0x37, 0x61)

    private val IMAGE_GIF89A_HEAD =
        byteArrayOf(0x47.toByte(), 0x49, 0x46, 0x38, 0x39, 0x61)

    private val IMAGE_BMP_HEAD =
        byteArrayOf(0x42.toByte(), 0x4D)

    private val imageHeaders = arrayOf(
        IMAGE_JPEG_HEAD,
        IMAGE_PNG_HEAD,
        IMAGE_GIF87A_HEAD,
        IMAGE_GIF89A_HEAD,
        IMAGE_BMP_HEAD,
        IMAGE_HEIC_HEAD
    )

    private const val DEFAULT_DURATION = 3000

    // 保存完成后通知相册scan
    @WorkerThread
    fun notifyAlbum(
        context: Context,
        filePath: String,
        callback: (isSuccess: Boolean, msg: String) -> Unit
    ) {
        scanFileWithScanIntent(
            context,
            filePath,
            callback
        )
    }

    private fun scanFileWithScanIntent(
        context: Context,
        filePath: String,
        callback: (isSuccess: Boolean, msg: String) -> Unit
    ) {
        if (filePath.isBlank()) {
            callback.invoke(false, "the export file path is blank!")
            return
        }
        // android Q使用intent扫描方式，反而会扫描不出图片，改为使用MediaScanner
        if (Build.VERSION.SDK_INT >= 29) {
            scanFileWithMediaScanner(
                context,
                filePath,
                callback
            )
            return
        }
        insertVideoMediaToSysDB(
            context.contentResolver,
            filePath,
            callback
        )
        val scanIntent = Intent(NOTIFY_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var uri: Uri? = null
            try {
                uri = FileProvider.getUriForFile(
                    context,
                    FILE_PROVIDER_AUTHORITY,
                    File(filePath)
                )
            } catch (e: Exception) {
                ILog.w(TAG, "failed to get file" + e.message)
            }
            scanIntent.data = uri
        } else {
            scanIntent.data = Uri.fromFile(File(filePath))
        }
        // 添加这一句表示对目标应用临时授权该Uri所代表的文件
        scanIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.sendBroadcast(scanIntent)
        scanFileWithMediaScanner(
            context,
            filePath,
            callback
        )
    }

    private fun scanFileWithMediaScanner(
        context: Context,
        filePath: String,
        callback: (isSuccess: Boolean, msg: String) -> Unit
    ) {
        val file = File(filePath)
        if (file.exists()) {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath), null
            ) { path, uri ->
                ILog.i(TAG, "scan completed:$path")
                if (uri == null || uri.toString().isEmpty()) {
                    callback.invoke(
                        false,
                        "MediaScannerConnection.scanFile fail, uri: $uri path: $path "
                    )
                } else {
                    callback.invoke(true, "MediaScannerConnection.scanFile succeed.")
                }
            }
        } else {
            ILog.e(TAG, "scan file, but file is empty")
            callback.invoke(false, "scan file, but the file is empty")
        }
    }

    private fun insertVideoMediaToSysDB(
        cr: ContentResolver,
        path: String,
        callback: (isSuccess: Boolean, msg: String) -> Unit
    ) {
        val nowInMs = System.currentTimeMillis()
        val extension = "mp4"
        val nowInSec = nowInMs / 1000
        val file = File(path)
        val values = ContentValues()
        values.put(MediaStore.Video.Media.TITLE, File(path).name)
        values.put(MediaStore.Video.Media.DISPLAY_NAME, File(path).nameWithoutExtension)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/$extension")
        values.put(MediaStore.Video.Media.DATE_TAKEN, nowInMs)
        values.put(MediaStore.Video.Media.DATE_MODIFIED, nowInSec)
        values.put(MediaStore.Video.Media.DATE_ADDED, nowInSec)
        values.put(MediaStore.Video.Media.DATA, path)
        values.put(MediaStore.Video.Media.SIZE, file.length())
        val durationMs =
            getDuration(path)
        values.put(MediaStore.Video.Media.DURATION, durationMs)

        try {
            val uri = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null || uri.toString().isEmpty()) {
                callback.invoke(false, "insert video: $path fail! uri: $uri")
            } else {
                callback.invoke(true, "insert video succeed")
            }
        } catch (e: Exception) {
            ILog.e(TAG, "failed to insert video:$path to mediastore!")
            callback.invoke(false, "ContentResolver insert video: $path fail! exception: $e")
        }
    }

    fun getDuration(path: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        } catch (ignored: Throwable) {
            DEFAULT_DURATION.toLong()
        }
    }


    fun isImage(path: String): Boolean {
        if (!FileUtil.isFileExist(path)) return false
        try {
            val header = ByteArray(12)
            FileInputStream(path).use {
                it.read(header)
            }

            if (RIFF_HEAD.contentEquals(header.sliceArray(0 until 4)) &&
                WEBP_HEAD.contentEquals(header.sliceArray(8 until 12))
            ) {
                return true
            }

            for (i in imageHeaders) {
                if (i.contentEquals(header.sliceArray(i.indices))) {
                    return true
                }
            }

            val headerStr = header.joinToString(" ") {
                it.toString(16).padStart(2, '0')
            }
            ILog.d(TAG, "file header: $headerStr")
        } catch (ignored: Exception) {
            ILog.e(TAG, "isImage $path error $ignored")
        }
        return false
    }

    /**
     * IOS only support JPEG/PNG image format
     */
    fun isIOSSupportImageFormat(path: String): Boolean {
        if (!FileUtil.isFileExist(path)) return false
        try {
            val header = ByteArray(12)
            FileInputStream(path).use {
                it.read(header)
            }

            if (IMAGE_JPEG_HEAD.contentEquals(header.sliceArray(
                    IMAGE_JPEG_HEAD.indices)) ||
                IMAGE_PNG_HEAD.contentEquals(header.sliceArray(
                    IMAGE_PNG_HEAD.indices))
            ) {
                return true
            }

            val headerStr = header.joinToString(" ") {
                it.toString(16).padStart(2, '0')
            }
            ILog.d(TAG, "file header: $headerStr")
        } catch (ignored: Exception) {
            ILog.e(TAG, "isIOSSupportImageFormat $path error $ignored")
        }
        return false
    }

    /**
     * 用于判断文件格式是否为heic
     */
    fun isHeicImg(path: String): Boolean {
        if (!FileUtil.isFileExist(path)) {
            return false
        }
        val header = ByteArray(12)
        FileInputStream(path).use {
            it.read(header)
        }
        return IMAGE_HEIC_HEAD.contentEquals(header.sliceArray(
            IMAGE_HEIC_HEAD.indices))
    }
}

fun imagePixelCount(filepath: String?): Int {

    if (TextUtils.isEmpty(filepath)) {
        return 0
    }
    val file = File(filepath)
    if (!file.exists()) {
        return 0
    }

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(filepath, options)
    return options.outWidth * options.outHeight
}

fun videoFramePixelCount(filepath: String?): Int {
    if (TextUtils.isEmpty(filepath)) {
        return 0
    }

    val mmr = MediaMetadataRetriever()
    try {
        mmr.setDataSource(filepath)
        val widthString =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)

        val heightString =
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

        val width = widthString?.toInt() ?: 0
        val height = heightString?.toInt() ?: 0

        return width * height
    } catch (e: Exception) {
    } finally {
        mmr.release()
    }
    return 0
}

/**
 * rotate the material angle to 0
 * swap width height if angle is 90 or 270
 */
fun VideoMetaDataInfo.ignoreAngle(): VideoMetaDataInfo {
    return if ((rotation == 90 || rotation == 270)) {
        copy(width = height, height = width, rotation = 0)
    } else {
        copy(width = width, height = height, rotation = 0)
    }
}





