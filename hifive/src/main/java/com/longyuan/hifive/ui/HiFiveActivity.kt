package com.longyuan.hifive.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.longyuan.hifive.HiFiveConfig
import com.longyuan.hifive.HiFiveConfig.playModel
import com.longyuan.hifive.HiFiveRequestManager
import com.longyuan.hifive.R
import com.longyuan.hifive.manager.OnSeekChangeListener
import com.longyuan.hifive.manager.SPUtils
import com.longyuan.hifive.model.FileModel
import com.longyuan.hifive.model.HiFiveMusicModel
import com.longyuan.hifive.model.HiFiveSongMenuModel
import com.longyuan.hifive.ui.fragment.HIFIVEMusicFragment
import com.longyuan.hifive.ui.fragment.HiFiveSearchFragment
import com.longyuan.hifive.ui.fragment.HiFiveSongMenuListFragment
import com.ss.ugc.android.editor.base.music.data.SelectedMusicInfo
import com.ss.ugc.android.editor.base.music.tools.AudioPlayer
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.utils.LiveDataBus
import kotlinx.android.synthetic.main.activity_hi_five.*

class HiFiveActivity : AppCompatActivity() {
    private val defaultTime = "00:00"
    private val hifiveMusicFragment = HIFIVEMusicFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this,R.color.hifive_background)

        setContentView(R.layout.activity_hi_five)

        closePlay()

        supportFragmentManager.beginTransaction().replace(R.id.hiFiveFrameLayout,hifiveMusicFragment).commit()
    }


    //设置视频剪辑的音乐
    //EditorActivityDelegate
    fun setAudio(title : String, path : String){
        LiveDataBus.getInstance().with(Constants.KEY_ADD_AUDIO, SelectedMusicInfo::class.java).postValue(SelectedMusicInfo(title, path))
        finish()
    }

    /**
     * 跳转到歌单歌曲列表
     */
    fun goMenuList(hiFiveSongMenuModel: HiFiveSongMenuModel) {
        HiFiveSongMenuListFragment().let { fragment ->
            Bundle().apply {
                putSerializable("model",hiFiveSongMenuModel)
                fragment.arguments = this
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.hiFiveFrameLayout,fragment,"").hide(hifiveMusicFragment)
                .show(fragment).addToBackStack("").commit()
        }
        musicPlayLayout.visibility = View.GONE
        AudioPlayer.stop()
    }

    /**
     * 跳转到搜索
     */
    fun goSearch(){
        HiFiveSearchFragment().let { fragment ->
            supportFragmentManager.beginTransaction()
                .add(R.id.hiFiveFrameLayout,fragment,"").hide(hifiveMusicFragment)
                .show(fragment).addToBackStack("").commit()
        }
        closePlay()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        closePlay()
    }

    fun back(){
        if (supportFragmentManager.backStackEntryCount == 0) finish() else supportFragmentManager.popBackStackImmediate(null,0)
        closePlay()
    }

    /**
     * 清除播放设置
     */
    private fun closePlay(){
        musicPlayLayout.visibility = View.GONE
        playModel = null
        playPath = ""
        playFile = null
        AudioPlayer.stop()
        AudioPlayer.clear()
    }

    private var playPath : String = ""
    private var playFile : FileModel? = null
    fun setPlay(model: HiFiveMusicModel, startDownload : () -> Unit, notifyItem : (HiFiveMusicModel) -> Unit){
        if (model.id == playModel?.id)return
        playPath = ""
        playFile = null
        AudioPlayer.stop()
        AudioPlayer.clear()
        playModel = model
        musicPlayLayout.visibility = View.VISIBLE
        hiFivePlayProgress.visibility = View.GONE
        HiFiveConfig.loadImageRound(model.image,ivHiFiveMusicImage)

        ivHiFiveMusicPlayCloud.visibility = if (HiFiveConfig.hasLocale(model.id) == null) View.VISIBLE else View.GONE
        ivHiFiveMusicPlayCloud.setOnClickListener {
            when{
                !HiFiveConfig.isNetSystemUsable(it.context) -> HiFiveConfig.showToast(it.context,HiFiveConfig.networkErrorMsg)
                HiFiveRequestManager.isDownload(model.id) -> HiFiveConfig.showToast(it.context,"正在下载中，请稍后...")
                else -> startDownload()
            }
        }
        ivHiFiveMusicPlayCollect.isSelected = HiFiveConfig.hasCollection(model.id)
        initCollectionImage()
        ivHiFiveMusicPlayCollect.setOnClickListener {
            when{
                it.isSelected -> {
                    HiFiveRequestManager.removeCollection(model.id){
                        ivHiFiveMusicPlayCollect.isSelected = false
                        initCollectionImage()
                        hifiveMusicFragment.updateCollectionList(model,false)
                    }
                }
                else -> {
                    HiFiveRequestManager.addCollection(model){
                        ivHiFiveMusicPlayCollect.isSelected = true
                        initCollectionImage()
                        hifiveMusicFragment.updateCollectionList(model,true)
                    }
                }
            }
        }
        tvHiFiveMusicPlayName.text = model.name
        tvHiFiveMusicPlayStartTime.text = defaultTime
        tvHiFiveMusicPlayEndTime.text = HiFiveConfig.formatMusicTime(model.time)

        hiFiveMusicPlayProgress.setOnSeekBarChangeListener(object : OnSeekChangeListener{
            override fun onProgressChanged(progress: Int, isChanged: Boolean) {}

            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch(progress: Int) {
                //model.time*(progress/100)*1000
                Log.i("tag", "onStopTrackingTouch===============>$progress")
                AudioPlayer.mediaPlayer?.seekTo(model.time*10*progress)
            }
        })
        hiFiveMusicPlayProgress.visibility = View.INVISIBLE
        playFile = null
        ivHiFiveMusicPlay.visibility = View.GONE
        ivHiFiveMusicPlay.isSelected = false
        ivHiFiveMusicPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        ivHiFiveMusicPlay.let {
            it.setOnClickListener { _->
                model.isInitAudio = true
                ivHiFiveMusicPlay.isSelected = !it.isSelected
                when{
                    //暂停
                    !it.isSelected -> {
                        ivHiFiveMusicPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                        AudioPlayer.pause()
                        hifiveMusicFragment.updateItemState(model.id,false)
                        model.isPlaying = false
                        notifyItem(model)
                    }
                    //暂停->播放
                    playPath == playFile?.filePath -> {
                        ivHiFiveMusicPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                        AudioPlayer.resume()
                        hifiveMusicFragment.updateItemState(model.id,true)
                        model.isPlaying = true
                        notifyItem(model)
                    }
                    //开始播放
                    else -> {
                        ivHiFiveMusicPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                        playPath = playFile?.filePath ?: ""
                        AudioPlayer.play(model.id.hashCode().toLong(), playFile?.filePath ?: "", false,
                            playAnim = { Log.i("tag","AudioPlayer play========>playAnim") }, onProgress = { current, total ->
                                //Log.i("tag", "AudioPlayer play========>current::$current,total::$total")
                                runOnUiThread {
                                    val musicProgress = (current * 100) / total
                                    hiFiveMusicPlayProgress.setCurrent(musicProgress)
                                    //val time = ((musicProgress.toDouble() / 100) * model.time).toInt()
                                    val time = (model.time * current)/total
                                    //Log.i("tag", "AudioPlayer play========>time::$time")
                                    tvHiFiveMusicPlayStartTime.text = HiFiveConfig.formatMusicTime(time)
                                }
                            }
                        ) {
                            Log.i("tag", "AudioPlayer play========>complete")
                            runOnUiThread {
                                ivHiFiveMusicPlay.isSelected = false
                                ivHiFiveMusicPlay.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                                hifiveMusicFragment.updateItemState(model.id,false)
                                model.isPlaying = false
                                notifyItem(model)
                            }
                        }
                        hifiveMusicFragment.updateItemState(model.id,true)
                        model.isPlaying = true
                        notifyItem(model)
                    }
                }
            }
        }
        setMusicSeekbar(model.id)
    }

    /**
     * 更新播放音乐下载进度条（暂未使用）
     */
    fun updatePlayProgress(musicId: String, progress : Int){
        if (musicId == playModel?.id) {
            ivHiFiveMusicPlayCloud.visibility = View.GONE
            hiFivePlayProgress.visibility = View.VISIBLE
            hiFivePlayProgress.progress = progress
        }
    }

    /**
     * 列表中下载完成后调用此处更新状态并初始化播放之前的相关设置
     */
    fun updateDownloadState(musicId : String){
        if (musicId == playModel?.id){
            hiFivePlayProgress.visibility = View.GONE
            ivHiFiveMusicPlayCloud.visibility = View.GONE
            setMusicSeekbar(musicId)
        }
        hifiveMusicFragment.updateProgress(musicId,100)
    }

    /**
     * 更新播放音乐的播放状态，用于和列表item状态关联
     */
    fun updatePlayState(){
        ivHiFiveMusicPlay.performClick()
    }

    private fun initCollectionImage(){
        ivHiFiveMusicPlayCollect.setImageResource(if (ivHiFiveMusicPlayCollect.isSelected) R.mipmap.icon_hifive_collection else R.mipmap.icon_hifive_uncollection)
    }

    /**
     * 设置并显示音频频谱波形图
     */
    private fun setMusicSeekbar(musicId : String){
        HiFiveConfig.localMusicVolume = SPUtils.getVolumeData(this, musicId)
        HiFiveConfig.hasLocale(musicId)?.let { fileModel ->
            playFile = fileModel
            HiFiveConfig.initSeekBar(musicId,fileModel){info ->
                //判断处理后的音乐是否是当前选中的音乐
                if (info.id == playModel?.id) {
                    hiFiveMusicPlayProgress.visibility = View.VISIBLE
                    ivHiFiveMusicPlay.visibility = View.VISIBLE
                    hiFiveMusicPlayProgress.setAudioVolumeInfo(info)
                    ivHiFiveMusicPlay.performClick()
                }
                if (SPUtils.getVolumeData(this,info.id).isBlank())SPUtils.saveVolumeData(this,info.id,info.toJsonString())
            }
        }
    }


    override fun onPause() {
        super.onPause()
        //AudioPlayer.pause()
        if (ivHiFiveMusicPlay.isSelected)ivHiFiveMusicPlay.performClick()
    }


    override fun onDestroy() {
        AudioPlayer.stop()
        AudioPlayer.clear()
        playModel = null
//        HiFiveConfig.localFile.forEach {
//            HiFiveConfig.deleteSingleFile(it.filePath)
//        }
        super.onDestroy()
    }
}