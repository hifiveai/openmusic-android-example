package com.ss.ugc.android.editor.base.music

import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.listener.IItemClickListener
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.music.tools.MusicDownloader
import com.ss.ugc.android.editor.base.music.tools.MusicPlayer
import com.ss.ugc.android.editor.base.music.viewholder.MusicItemViewHolder
import com.ss.ugc.android.editor.base.network.IScrollRequest
import com.ss.ugc.android.editor.base.utils.postOnUiThread
import com.ss.ugc.android.editor.core.utils.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class BaseMusicListAdapter(
        private val musicItemViewConfig: MusicItemViewConfig?,
        private val itemClickListener: IItemClickListener<MusicItem>?,
        private val scrollRequest: IScrollRequest?,
        private val useMusic: ((MusicItem) -> Unit)?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    private val musicItemList: ArrayList<MusicItem> = arrayListOf()
    private val playingProgressMap = hashMapOf<Int, Float>()
    protected var lastPlayingPosition = -1
    var playingPosition = -1
    private var playingId = -1L

    open var audioPlayListener: IAudioPlayListener? = null

    fun append(list: List<MusicItem>) {
        musicItemList.addAll(list)
        notifyDataSetChanged()
    }

    fun reset() {
        val size = itemCount
        musicItemList.clear()
        lastPlayingPosition = -1
        playingPosition = -1
        playingId = -1L
        playingProgressMap.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(container.context)
                .inflate(getMusicItemLayout(), container, false)
        return MusicItemViewHolder(view)
    }

    private fun getMusicItemLayout(): Int {
        return musicItemViewConfig?.itemLayoutId?.takeIf { it > 0 }
                ?: R.layout.layout_music_list_item_simple
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        bindMusicItem(viewHolder as MusicItemViewHolder, position)
    }

    protected fun bindMusicItem(itemViewHolder: MusicItemViewHolder, position: Int) {
        // 保护越界情况
        val realPosition = getRealPosition(position)
        if (musicItemList.isNullOrEmpty() || realPosition >= musicItemList.size || realPosition < 0) {
            return
        }
        val musicItem = musicItemList[realPosition]
        //歌曲封面图片
        itemViewHolder.setImageCover(musicItem, musicItemViewConfig)
        //歌曲名称
        itemViewHolder.setMusicTitle(musicItem)
        //歌曲时长
        itemViewHolder.setMusicDuration(musicItem, musicItemViewConfig)
        //下载状态
        if (musicItemViewConfig?.isLocalMusic == true) {
            itemViewHolder.showLocalStatus(musicItemViewConfig)
        } else {
            //下载状态
            itemViewHolder.showDownloadStatus(musicItem, musicItemViewConfig)
        }
        itemViewHolder.showPlayStatus(musicItem, musicItemViewConfig)
        showPlayProgressIfNeeded(itemViewHolder, position)
        itemViewHolder.itemView.setOnClickListener {
            if (playingPosition != -1) {
                pauseMusic()
                notifyItemChanged(playingPosition)
                if (playingPosition == realPosition) {
                    playingPosition = -1
                    return@setOnClickListener
                }
            }
            if (lastPlayingPosition != realPosition) {
                lastPlayingPosition = realPosition
                if (playingProgressMap.containsKey(lastPlayingPosition)) {
                    playingProgressMap.remove(lastPlayingPosition)
                }
                onClickNewItem(realPosition)
                notifyDataSetChanged()
            }

            if (!MusicDownloader.isDownLoaded(musicItem)) {
                playingPosition = -1
            }
            launch {
                checkDownload(musicItem, position)
            }
            itemClickListener?.onItemClick(musicItem, realPosition)
        }
        itemViewHolder.songUseButton.setOnClickListener {
            useMusic?.invoke(musicItem)
        }
    }

    protected open fun onClickNewItem(realPosition: Int) {

    }

    protected open fun getRealPosition(position: Int): Int {
        return position
    }

    private suspend fun checkDownload(musicItem: MusicItem, position: Int) {
        if (musicItemViewConfig?.isLocalMusic == true || MusicDownloader.isDownLoaded(musicItem)) {
            playSong(musicItem, position)
        } else if (!MusicDownloader.isDownloading(musicItem)) {
            download(musicItem, position)
        }
    }

    private fun playSong(musicItem: MusicItem, position: Int) {
        MusicPlayer.play(musicItem, musicItemViewConfig?.isLocalMusic ?: false, {
            //playAnim
            audioPlayListener?.onStart(it, musicItem)
            playingPosition = if (it) getRealPosition(position) else -1
            playingId = musicItem.id
            scrollRequest?.requestScroll(lastPlayingPosition)
            notifyItemChanged(getRealPosition(position))
        },
                { current: Int, total: Int ->
                    //onProgress
                    if (total != 0) {
                        val progress = current.toFloat() / total
                        playingProgressMap[position] = progress
                        audioPlayListener?.onProgress(current, total)
                        safeNotifyItemChanged(position)
                    }
                },
                {
                    //onPlayComplete
                    audioPlayListener?.onComplete()
                    playingPosition = -1
                    notifyItemChanged(lastPlayingPosition)
                })
    }

    private suspend fun download(musicItem: MusicItem, position: Int) {
        val realPosition = getRealPosition(position)
        notifyItemChanged(realPosition)
        val start = SystemClock.uptimeMillis()
        if (MusicDownloader.download(musicItem)) {
            if (playingPosition == -1) {
                lastPlayingPosition = realPosition
                notifyDataSetChanged()
                playSong(musicItem, position)
            } else {
                notifyDataSetChanged()
            }
            val time = SystemClock.uptimeMillis() - start
        } else {
            Toaster.show("网络异常，请重试")
        }
    }


    private fun showPlayProgressIfNeeded(viewHolder: MusicItemViewHolder, position: Int) {
        if (musicItemViewConfig?.showProgressText == true) {
            if (playingProgressMap.containsKey(position) && position == lastPlayingPosition) {
                playingProgressMap[position]?.also {
                    viewHolder.showProgress(true)
                    viewHolder.progressBar.progress = it
                    viewHolder.progressBar.setOnProgressChangedListener { _, progress, isFormUser, eventAction ->
                        if (isFormUser && eventAction == MotionEvent.ACTION_DOWN) {
                            MusicPlayer.pause()
                            notifyItemChanged(position)
                            playingPosition = position
                        }
                        if (isFormUser && eventAction == MotionEvent.ACTION_UP) {
                            onSeek((progress * MusicPlayer.getTotalDuration()).toInt())
                        }
                    }
                }
            } else {
                viewHolder.showProgress(false)
            }
        } else {
            viewHolder.showProgress(false)
        }
    }

    override fun getItemCount(): Int {
        return musicItemList.size
    }

    fun getItem(position: Int): MusicItem? {
        val realPosition = getRealPosition(position)
        if (realPosition >= musicItemList.size) {
            return null
        }
        return musicItemList[realPosition]
    }

    fun safeNotifyItemChanged(position: Int) {
        postOnUiThread {
            notifyItemChanged(position)
        }
    }

    open fun onPause() {
        pauseMusic()
        notifyItemChanged(playingPosition)
        playingPosition = -1
    }

    protected open fun pauseMusic() {
        MusicPlayer.pause()
    }


    open fun onSeek(playTime: Int) {
        MusicPlayer.seek(playTime)
        MusicPlayer.resume()
    }

}

