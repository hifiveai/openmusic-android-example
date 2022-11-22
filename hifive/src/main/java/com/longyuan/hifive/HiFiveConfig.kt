package com.longyuan.hifive

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hfopen.sdk.entity.MusicRecord
import com.hfopen.sdk.entity.SheetRecord
import com.longyuan.hifive.manager.AudioVolumeInfoUtil
import com.longyuan.hifive.manager.OnActionListener
import com.longyuan.hifive.model.AudioVolumeInfo
import com.longyuan.hifive.model.FileModel
import com.longyuan.hifive.model.HiFiveMusicModel
import com.longyuan.hifive.model.HiFiveSongMenuModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*


/**
 * #duxiaxing
 * #date: 2022/7/15
 */
internal object HiFiveConfig {
    var isLoginHiFive = false//是否有初始化并登录过hifive音乐sdk
    const val networkErrorMsg = "当前网络不可用，请检查网络连接"
    private var savePath = ""//文件保存路径
    private const val fileType = "mp3"//文件格式
    var collectionSheetId = 38068//收藏歌单id
    const val localCollectionSize = 100//获取并临时保存的收藏歌单歌曲条数
    var playModel: HiFiveMusicModel? = null//正在播放的歌曲
    var localMusicVolume = ""//本地保存的波形数据

    val localFile = arrayListOf<FileModel>()//本地已下载的歌曲
    val musicCache = arrayListOf<AudioVolumeInfo>()//波形已进行过初始化的歌曲

    val musicCollection = arrayListOf<HiFiveMusicModel>()//临时保存的收藏歌单歌曲
    val musicMenuData = arrayListOf<HiFiveSongMenuModel>()//歌单
    val musicMenu = arrayListOf<List<HiFiveSongMenuModel>>()//对歌单进行分页处理

    //private val options = RequestOptions.bitmapTransform(RoundedCorners(10))
    private val options = RequestOptions().transform(MultiTransformation(CenterCrop(), RoundedCorners(10)))
    private val optionsMusic = RequestOptions().transform(MultiTransformation(CenterCrop(), RoundedCorners(5)))
        .format(DecodeFormat.PREFER_RGB_565).priority(Priority.LOW)
        .skipMemoryCache(false).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    fun loadImageRound(url: String, imageView: ImageView){
        Glide.with(imageView).load(url).apply(options).into(imageView)
    }

    fun loadImageMusic(url: String, imageView: ImageView){
        Glide.with(imageView).load(url).apply(optionsMusic).into(imageView)
    }

    /**
     * 检查网络状态，ture无网络
     */
    fun isNetSystemUsable(context: Context?): Boolean {
        var isNetUsable = false
        context?.let {
            kotlin.runCatching {
                val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                manager.activeNetworkInfo?.let {
                    if (it.state == NetworkInfo.State.CONNECTED)isNetUsable = true
                }
            }
        }
        return isNetUsable
    }

    /**
     * 将获取到的歌单数据进行转换
     */
    fun initSongMenuData(datas : List<SheetRecord>){
        musicMenuData.clear()
        musicMenu.clear()
        datas.forEach {
            musicMenuData.add(HiFiveSongMenuModel(it.sheetId,it.sheetName,it.cover[0].url))
        }
        var list = arrayListOf<HiFiveSongMenuModel>()
        var count = 0
        musicMenuData.forEach {
            count ++
            list.add(it)
            if (list.size == 9 || count == HiFiveConfig.musicMenuData.size){
                musicMenu.add(list)
                list = arrayListOf()
            }
        }
    }

    /**
     * 将hifive音乐sdk获取到的音乐数据进行转换
     */
    fun formatMusicData(datas : List<MusicRecord>) : List<HiFiveMusicModel>{
        val list = arrayListOf<HiFiveMusicModel>()
        datas.forEach { music ->
            if (playModel != null && music.musicId == playModel?.id){
                list.add(playModel!!)
            }else {
                val tag = StringBuilder().apply {
                    music.tag?.forEach { tag ->
                        append(tag.tagName).append(" ")
                    }
                }.toString()
                var image = ""
                music.cover?.let { if (it.isNotEmpty()) image = it[0].url }
                var author = ""
                music.artist?.let { artists -> if (artists.isNotEmpty()) author = artists[0].name }
                list.add(
                    HiFiveMusicModel(
                        music.musicId, image, music.musicName, music.duration, author, tag,
                        if (hasLocale(music.musicId) != null) 100 else -1
                    )
                )
            }
        }
        return list
    }

    /**
     * 将时间转换成分秒
     */
    fun formatMusicTime(time : Int) : String{
        val minute = time/60
        val second = time % 60
        return (if (minute > 9) minute.toString() else "0${minute}") + ":" + (if (second > 9) second.toString() else "0$second")
    }

    /**
     * 判断本地是否有已下载的歌曲
     */
    fun hasLocale(id: String) : FileModel?{
        localFile.forEach {
            if (it.fileName.startsWith(id))return it
        }
        return null
    }

    fun getBasePath(context: Context): String {
        if (savePath.isNotBlank())return savePath
        //判断是否有存储卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            val file = context.getExternalFilesDir("hifive")
            if (file != null) {
                val pathBase_external = file.absolutePath
                if (!file.exists()) file.mkdirs()
                savePath = pathBase_external
                if (TextUtils.isEmpty(pathBase_external)) return savePath
            }
        }
        val pathBase = context.filesDir.absolutePath + "/hifive"
        val file = File(pathBase)
        if (!file.exists()) {
            file.mkdirs()
        }
        savePath = pathBase
        return savePath
    }

    /**
     *
     * 获取指定目录内所有文件路径
     *
     * @param dirPath 需要查询的文件目录
     *
     * @param _type 查询类型，比如mp3什么的
     */
    fun getLocalSave(dirPath : String){
        val f = File(dirPath)
        //判断路径是否存在
        if (!f.exists()) {
            return
        }
        val files = f.listFiles() ?: return
        for (_file in files) { //遍历目录
            if (_file.isFile && _file.name.endsWith(fileType)) {
                //val _name = _file.name
                val filePath = _file.absolutePath //获取文件路径
                //val fileName = _file.name.substring(0, _name.length - 4) //获取文件名
                val fileName = _file.name //获取文件名
                Log.d("tag", "================>fileName:$fileName,filePath:$filePath,${_file.length()}")
                localFile.add(FileModel(fileName,filePath,_file.length()))
            }
        }
    }

    /** 删除单个文件
     * @param deleteFilePath 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    fun deleteSingleFile(deleteFilePath: String) {
        val file = File(deleteFilePath)
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile) {
            if (file.delete()) {
                Log.e("tag", "Copy_Delete.deleteSingleFile: 删除单个文件" + deleteFilePath + "成功！")
            } else {
                Log.e("tag","删除单个文件" + deleteFilePath + "失败！")
            }
        } else {
            Log.e("tag","删除单个文件失败：" + deleteFilePath + "不存在！")
        }
    }


    /**
     * 获取本地已下载的歌曲列表
     */
    fun initLocalData(context: Context){
        Thread{
            getLocalSave(getBasePath(context))
        }.start()
    }

    fun deleteCollectionLocal(musicId : String){
        musicCollection.forEach {
            if (it.id == musicId){
                musicCollection.remove(it)
                return
            }
        }
    }

    fun hasCollection(musicId: String) : Boolean{
        musicCollection.forEach {
            if (it.id == musicId)return true
        }
        return false
    }

    /**
     * 获取波形数据
     */
    fun initSeekBar(musicId : String, fileModel: FileModel, complete : (info : AudioVolumeInfo) -> Unit){
        musicCache.forEach {
            if (it.id == musicId){
                Log.i("tag","从临时保存的数据中获取到了波形数据===============>${it}")
                //logLong("tag_long", Arrays.toString(it.mHeightsAtThisZoomLevel))
                complete(it)
                return
            }
        }
        if (localMusicVolume.isNotBlank()){
            AudioVolumeInfo().let { info ->
                info.formatJson(localMusicVolume)
                if (info.id.isNotBlank()) {
                    Log.i("tag","从SharedPreferences中获取到了波形数据===============>${info}")
                    //logLong("tag_long", Arrays.toString(info.mHeightsAtThisZoomLevel))
                    musicCache.add(info)
                    complete(info)
                    return
                }
            }
        }
        val start = System.currentTimeMillis()
        Observable.create<AudioVolumeInfo> { e->
            AudioVolumeInfoUtil.getInstance().getInfo(fileModel.filePath,object :
                OnActionListener<AudioVolumeInfo> {
                override fun isNeedProgress(): Boolean = false

                override fun onStart() {
                    Log.i("tag","onStart=====>")
                }

                override fun onProgress(progress: Int) {
                    Log.i("tag","onProgress=====>$progress")
                }

                override fun onFail(errorInfo: String?) {
                    Log.i("tag","onFail=====>$errorInfo")
                }

                override fun onSuccess(model: AudioVolumeInfo) {
                    model.id = musicId
                    Log.i("tag","文件读取成功onSuccess=====>${model}")
                    e.onNext(model)
                    e.onComplete()
                }
            })
        }.subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AudioVolumeInfo> {
                override fun onSubscribe(d: Disposable) {}

                override fun onNext(t: AudioVolumeInfo) {
                    Log.e("tag","读取音乐源文件及相关操作总耗时============>${System.currentTimeMillis() - start}ms")
                    musicCache.add(t)
                    complete(t)
                }

                override fun onError(e: Throwable) {
                    Log.i("tag","onError=====>${e.cause}")
                }

                override fun onComplete() {}
            })
    }

    fun showToast(context: Context?, string: String){
        context?.let { if (string.isNotBlank())Toast.makeText(context,string,Toast.LENGTH_SHORT).show() }
    }

}