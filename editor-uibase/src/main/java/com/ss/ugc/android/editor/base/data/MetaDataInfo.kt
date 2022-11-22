package com.ss.ugc.android.editor.base.data

/**
 *  author : wenlongkai
 *  date : 2019-08-16 16:44
 *  description :
 */
// [width, height, rotation, duration, Longitude, Latitude, bitrate(kbps), fps， codec_id, key_frame_count]
data class VideoMetaDataInfo(
    val path: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val rotation: Int = 0,
    val duration: Int = 4000,
    val longitude: Int = 0,
    val latitude: Int = 0,
    val bitrate: Int = 0,
    val fps: Int = 0,
    val codecId: Int = 0,
    val videoDuration: Int = 0,
    val codecInfo: String = "unknown",
    val keyFrameCount: Int = Int.MAX_VALUE
) {

    companion object {
        const val MAP_KEY_PATH = "path"
        const val MAP_KEY_WIDTH = "width"
        const val MAP_KEY_HEIGHT = "height"
        const val MAP_KEY_ROTATION = "rotation"
        const val MAP_KEY_DURATION = "duration"
        const val MAP_KEY_LONGITUDE = "longitude"
        const val MAP_KEY_LATITUDE = "latitude"
        const val MAP_KEY_BITRATE = "bitrate"
        const val MAP_KEY_FPS = "fps"
        const val MAP_KEY_CODEC = "codec"
        const val MAP_KEY_VIDEO_DURATION = "video_duration"
        const val MAP_KEY_CODEC_INFO = "codec_info"
        const val MAP_KEY_VIDEO_SIZE = "video_size"
        const val MAP_KEY_KEY_FRAME_COUNT = "key_frame_count"
    }

    private val mapInfo = HashMap<String, Any>()

    init {
        mapInfo[MAP_KEY_PATH] = path
        mapInfo[MAP_KEY_WIDTH] = width
        mapInfo[MAP_KEY_HEIGHT] = height
        mapInfo[MAP_KEY_ROTATION] = rotation
        mapInfo[MAP_KEY_DURATION] = duration
        mapInfo[MAP_KEY_LONGITUDE] = longitude
        mapInfo[MAP_KEY_LATITUDE] = latitude
        mapInfo[MAP_KEY_BITRATE] = bitrate * 1000
        mapInfo[MAP_KEY_FPS] = fps
        mapInfo[MAP_KEY_CODEC] = codecId
        mapInfo[MAP_KEY_VIDEO_DURATION] = videoDuration
        mapInfo[MAP_KEY_CODEC_INFO] = codecInfo
        mapInfo[MAP_KEY_VIDEO_SIZE] = "${width}x$height"
        mapInfo[MAP_KEY_KEY_FRAME_COUNT] = keyFrameCount
    }

    /**
     * 转换成intArrayInfo，[0]: duration [1]: width [2]: height [3]: rotation
     */
    fun toArrayInfo() = intArrayOf(duration, width, height, rotation)

    fun toMap() = HashMap(mapInfo)
}

data class AudioMetaDataInfo(
    val duration: Int = 4000
)
