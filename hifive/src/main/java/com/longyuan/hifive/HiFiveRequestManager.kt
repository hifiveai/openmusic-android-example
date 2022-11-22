package com.longyuan.hifive

import android.content.Context
import android.util.Log
import com.hfopen.sdk.entity.*
import com.hfopen.sdk.hInterface.DataResponse
import com.hfopen.sdk.manager.HFOpenApi
import com.hfopen.sdk.rx.BaseException
import com.longyuan.hifive.model.FileModel
import com.longyuan.hifive.model.HiFiveMusicModel
import com.tsy.sdk.myokhttp.MyOkHttp
import com.tsy.sdk.myokhttp.response.DownloadResponseHandler
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * #duxiaxing
 * #date: 2022/7/26
 */
internal object HiFiveRequestManager {
    private const val tag = "HFOpenApi"
    private val musicStartTime = System.currentTimeMillis()/1000 - 24*60*60
    private const val musicDuration = 365

    val downloading = arrayListOf<String>()

    fun isDownload(id: String) : Boolean {
        downloading.forEach {
            if (id == it)return true
        }
        return false
    }

    fun loginHiFive(nickname : String, failed : (String) -> Unit, success: () -> Unit){
        HFOpenApi.getInstance().baseLogin(nickname, null, null, null, null,
            null, null, null, null, null,
            System.currentTimeMillis().toString(), object : DataResponse<LoginBean> {
                override fun onError(exception: BaseException) {
                    Log.e(tag, "baseLogin errorMsg==" + exception.code)
                    failed(exception.msg ?: "错误code:${exception.code}")
                }

                override fun onSuccess(data: LoginBean, taskId: String) {
                    Log.i(tag,"baseLogin onSuccess===========>${data}")
                    success()
                }
            })
    }

    fun updateSheet(success: (Sheet) -> Unit){
        HFOpenApi.getInstance().sheet(0,0,1,45,null,0,object : DataResponse<Sheet> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "sheet errorMsg==" + exception.code)
            }

            override fun onSuccess(data: Sheet, taskId: String) {
                //Log.i(tag,"sheet onSuccess===========>${data}")
                success(data)
            }
        })
    }

    fun updateMusic(musicPage : Int, musicPageSize : Int, failed: () -> Unit, success: (MusicList) -> Unit){
        HFOpenApi.getInstance().baseHot(musicStartTime, musicDuration, musicPage, musicPageSize, null,object : DataResponse<MusicList> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "baseHot errorMsg==" + exception.code)
                failed.invoke()
            }

            override fun onSuccess(data: MusicList, taskId: String) {
                Log.i(tag,"baseHot onSuccess totalCount===========>${data.meta.totalCount}")
                success(data)
            }
        })
    }

    fun getMusic(musicId : String, complete : (String) -> Unit){
        HFOpenApi.getInstance().ugcHQListen(musicId, "mp3", "320", object : DataResponse<HQListen> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "getMusic ugcHQListen errorMsg==" + exception.code)
            }

            override fun onSuccess(data: HQListen, taskId: String) {
                Log.e(tag, "getMusic ugcHQListen data==$data")
                complete(data.fileUrl ?: "")
            }
        })
    }


    fun useMusic(musicId : String){
        HFOpenApi.getInstance().ugcReportListen(musicId, 10000, System.currentTimeMillis(),
            "mp3", "320", object : DataResponse<Any> {
                override fun onError(exception: BaseException) {
                    Log.e(tag, "useMusic ugcReportListen errorMsg=====>" + exception.code)
                }

                override fun onSuccess(data: Any, taskId: String) {
                    Log.e(tag, "useMusic ugcReportListen data=====>$data")
                }
            })
    }

    fun downloadMusic(context: Context, url : String, name : String, progress : (Long,String) -> Unit, failed: (String) -> Unit, finish : () -> Unit){
        val fileNamePath = HiFiveConfig.getBasePath(context) + "/$name.mp3"
        downloading.add(name)
//        HFOpenApi.getInstance().downLoadFile(url, fileNamePath ,object : DownLoadResponse {
//            override fun fail(error_msg: String) {
//                Log.i(tag + "download","downLoad fail=======>$error_msg")
//                downloading.remove(name)
//                kotlin.runCatching {
//                    HiFiveConfig.deleteSingleFile(fileNamePath)
//                }.onFailure {
//                    Log.i(tag + "download","deleteSingleFile runCatching=======>${it.cause}")
//                }
//                failed(name)
//            }
//
//            override fun progress(currentBytes: Long, totalBytes: Long) {
//                Log.i(tag + "download", "downLoad progress=======>${currentBytes},${totalBytes},${(currentBytes*100)/totalBytes}%")
//                progress((currentBytes*100)/totalBytes,name)
//            }
//
//            override fun size(totalBytes: Long) {
//                Log.i(tag + "download","downLoad size=======>$totalBytes")
//            }
//
//            override fun succeed(file: File) {
//                Log.i(tag + "download","downLoad succeed=======>path:::${file.path},name:::${file.name}")
//                downloading.remove(name)
//                HiFiveConfig.localFile.add(FileModel(file.name,file.path,file.length()))
//                finish()
//            }
//        })
        down.download()
            .url(url)
            .filePath(fileNamePath)
            .tag(this)
            .enqueue(object : DownloadResponseHandler() {
                override fun onStart(totalBytes: Long) {
                    //response.size(totalBytes)
                    Log.i(tag + "download","downLoad size=======>$totalBytes")
                }

                override fun onFinish(downloadFile: File) {
                    Log.i(tag + "download","downLoad succeed=======>path:::${downloadFile.path},name:::${downloadFile.name}")
                    downloading.remove(name)
                    HiFiveConfig.localFile.add(FileModel(downloadFile.name,downloadFile.path,downloadFile.length()))
                    finish()
                }

                override fun onProgress(currentBytes: Long, totalBytes: Long) {
                    Log.i(tag + "download", "downLoad progress=======>${currentBytes},${totalBytes},${(currentBytes*100)/totalBytes}%")
                    progress((currentBytes*100)/totalBytes,name)
                }

                override fun onFailure(error_msg: String) {
                    HiFiveConfig.showToast(context,error_msg)
                    Log.i(tag + "download","downLoad fail=======>$error_msg")
                    downloading.remove(name)
                    kotlin.runCatching {
                        HiFiveConfig.deleteSingleFile(fileNamePath)
                    }.onFailure {
                        Log.i(tag + "download","deleteSingleFile runCatching=======>${it.cause}")
                    }
                    failed(name)
                }
            })
    }

    private val down by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10000L, TimeUnit.MILLISECONDS)
            .readTimeout(10000L, TimeUnit.MILLISECONDS)
            .build()
        MyOkHttp(okHttpClient)
    }

    fun updateCollectionSheetId(failed: () -> Unit, success: (Int) -> Unit){
        HFOpenApi.getInstance().memberSheet(null, 1, 100, object : DataResponse<VipSheet> {
            
            override fun onError(exception: BaseException) {
                Log.e(tag, "memberSheet errorMsg==" + exception.code)
                failed()
            }

            override fun onSuccess(data: VipSheet, taskId: String) {
                Log.i(tag,"memberSheet onSuccess===========>${data}")
                data.record.forEach {
                    if (it.type == 2){
                        success(it.sheetId)
                        return
                    }
                }
            }
        })
    }

    fun updateCollection(page : Int, pageSize : Int, failed : () -> Unit,success : (Int,List<HiFiveMusicModel>) -> Unit){
        HFOpenApi.getInstance().memberSheetMusic(HiFiveConfig.collectionSheetId,page,pageSize,object : DataResponse<VipSheetMusic> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "updateCollection memberSheetMusic errorMsg==" + exception.code)
                failed()
            }

            override fun onSuccess(data: VipSheetMusic, taskId: String) {
                Log.i(tag,"updateCollection memberSheetMusic onSuccess===========>${data}")
                success(data.meta.totalCount,HiFiveConfig.formatMusicData(data.record))
            }
        })
    }

    fun addCollection(model: HiFiveMusicModel, success: () -> Unit){
        HFOpenApi.getInstance().addMemberSheetMusic(HiFiveConfig.collectionSheetId, model.id, object : DataResponse<Any> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "addMemberSheetMusic errorMsg==" + exception.code)
            }

            override fun onSuccess(data: Any, taskId: String) {
                Log.i(tag,"addMemberSheetMusic onSuccess===========>${data}")
                HiFiveConfig.musicCollection.add(0,model)
                success()
            }
        })
    }

    fun removeCollection(musicId : String, success: () -> Unit){
        HFOpenApi.getInstance().removeMemberSheetMusic(HiFiveConfig.collectionSheetId, musicId, object : DataResponse<Any> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "removeMemberSheetMusic errorMsg==" + exception.code)
            }

            override fun onSuccess(data: Any, taskId: String) {
                Log.i(tag,"removeMemberSheetMusic onSuccess===========>${data}")
                HiFiveConfig.deleteCollectionLocal(musicId)
                success()
            }
        })
    }

    /**
     * 搜索
     */
    fun updateSearch(keyWord : String, page: Int,pageSize: Int, failed: (String) -> Unit,success: (MusicList) -> Unit){
        HFOpenApi.getInstance().searchMusic(null, null, null, null,
            null, null, null, keyWord, null, 1,
            0, page, pageSize,null, object : DataResponse<MusicList> {
                override fun onError(exception: BaseException) {
                    Log.e(tag, "searchMusic errorMsg==" + exception.code)
                    failed(exception.msg ?: "")
                }

                override fun onSuccess(data: MusicList, taskId: String) {
                    Log.i(tag,"searchMusic onSuccess===========>${data}")
                    success(data)
                }
            })
    }

    /**
     * 获取歌单音乐列表
     */
    fun updateSongMenuMusic(sheetId : Long, page: Int, pageSize: Int, failed: (String) -> Unit, success: (MusicList) -> Unit){
        HFOpenApi.getInstance().sheetMusic(sheetId, 0, page, pageSize, object : DataResponse<MusicList> {
            override fun onError(exception: BaseException) {
                Log.e(tag, "updateSongMenuMusic onError==" + exception.code)
                failed(exception.msg ?: "")
            }

            override fun onSuccess(data: MusicList, taskId: String) {
                Log.e(tag, "updateSongMenuMusic onSuccess==$data")
                success(data)
            }
        })
    }
}