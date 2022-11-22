package com.vesdk.vebase.util

import android.content.Context
import com.vesdk.vebase.log.LogKit
import com.ss.android.medialib.FFMpegManager
import com.ss.android.vesdk.VEUtils
import com.vesdk.vebase.Constant
import java.io.File

/**
 * description :
 */
object ReEncodeUtil {


    fun reEncodeVideo(context: Context, path: String?, callBack: ReEncodeInterface?) {
        ExecutorsPool.instance.IO_EXECUTOR.execute {
            val videoPath =
                context.getExternalFilesDir(null)!!.absolutePath + File.separator + "duet_input.mp4"
            val audioPath =
                context.getExternalFilesDir(null)!!.absolutePath + File.separator + "duet_input.wav"

            LogKit.d(Constant.TAG, "合拍视频 开始处理....")
            val params = FFMpegManager.RencodeParams()
            //sdk内部对出入点做了判断,若outpoint-inpoint < minDurationInMs会报错，minDurationInMs默认3s，可自行控制，防止处理后的合拍视频为0或者时间很短的情况，没有意义
            params.minDurationInMs = 1000 //限制生成的视频不小于1s
            params.readfrom = path
            params.saveto = videoPath
            params.outputWav = audioPath
            params.inpoint = 0
            params.outpoint = VEUtils.getVideoFileInfo(path!!)!!.duration.toLong()
            params.screenWidth = 540
            params.fullScreen = false
            params.pos = 0
            params.rotateAngle = 0
            params.isCPUEncode = true
            // 对合拍视频进行抽离出视频和音频 并且把音频文件从aac转成pcm，提升性能
            val ret = FFMpegManager.getInstance().rencodeAndSplitFile(params)
            ExecutorsPool.instance.MAIN_EXECUTOR.execute {
                if (callBack != null) {
                    if (ret == 0) {
                        callBack.complete(ret, params.saveto, params.outputWav)
                    } else {
                        callBack.error(ret)
                    }
                }
            }
        }
    }

    interface ReEncodeInterface {
        fun complete(ret: Int, videoPath: String, audioPath: String)
        fun error(ret: Int)
    }
}