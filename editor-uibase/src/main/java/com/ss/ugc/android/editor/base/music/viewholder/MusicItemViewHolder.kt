package com.ss.ugc.android.editor.base.music.viewholder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.bytedance.ies.nlemediajava.utils.MediaUtil
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.extensions.gone
import com.ss.ugc.android.editor.base.extensions.hide
import com.ss.ugc.android.editor.base.extensions.show
import com.ss.ugc.android.editor.base.extensions.visible
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption.Builder
import com.ss.ugc.android.editor.base.music.MusicItemViewConfig
import com.ss.ugc.android.editor.base.music.data.MusicItem
import com.ss.ugc.android.editor.base.music.tools.MusicDownloader
import com.ss.ugc.android.editor.base.music.tools.MusicPlayer
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.base.utils.TextUtil
import com.ss.ugc.android.editor.base.utils.UIUtils
import com.ss.ugc.android.editor.base.utils.Utils
import com.ss.ugc.android.editor.base.view.ProgressBar

open class MusicItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val songItemImageView = itemView.findViewById<ImageView>(R.id.image_music)
    val songPlayerView = itemView.findViewById<ImageView>(R.id.music_play_ic)
    val songTitleTextView = itemView.findViewById<TextView>(R.id.music_name)
    val songAuthorTextView = itemView.findViewById<TextView>(R.id.music_singer)
    val songDurationTextView = itemView.findViewById<TextView>(R.id.music_time)
    val songDownLoadImageView = itemView.findViewById<View>(R.id.iv_download_image)
    val songUseButton = itemView.findViewById<View>(R.id.button_use)
    val songDownloadingAnim = itemView.findViewById<LottieAnimationView>(R.id.lottie_loading_view)
    val songPlayAnimation = itemView.findViewById<LottieAnimationView>(R.id.music_play_animation)
    val progressBar = itemView.findViewById<ProgressBar>(R.id.pb_progress)

    init {
        ThemeStore.setCommonBackgroundRes(songUseButton)
        ThemeStore.globalUIConfig.lottieDataRequestLoadingJson.also {
            val lottieTask = LottieCompositionFactory.fromAsset(itemView.context, it)
            lottieTask.addListener { result ->
                songDownloadingAnim?.setComposition(result)
                songDownloadingAnim?.playAnimation()
            }
        }
    }

    fun showPlayStatus(musicItem: MusicItem, musicItemViewConfig: MusicItemViewConfig?) {
        if (MusicPlayer.isPlaying(musicItem)) {
            songPlayAnimation.show()
            if (musicItemViewConfig?.hideIconWhenPlayMusic == true) {
                songItemImageView.hide()
            }
            songPlayerView.gone()
            if (!songPlayAnimation.isAnimating) {
                songPlayAnimation.playAnimation()
            }
        } else {
            if (musicItemViewConfig?.hideIconWhenPlayMusic == true) {
                songItemImageView.show()
            }
            songPlayAnimation.gone()
            songPlayAnimation.pauseAnimation()
            progressBar.gone()
            songDurationTextView.show()
        }
    }

    fun showLocalStatus(musicItemViewConfig: MusicItemViewConfig) {
        if (!songUseButton.visible) {
            songUseButton.show()
        }
        songDownLoadImageView.gone()
        songDownloadingAnim.gone()
        if (musicItemViewConfig?.usePlayIcon) {
            songPlayerView.show()
        } else {
            songPlayerView.gone()
        }
        songDownloadingAnim.cancelAnimation()
    }

    fun showDownloadStatus(musicItem: MusicItem, musicItemViewConfig: MusicItemViewConfig?) {
        when {
            MusicDownloader.isDownloading(musicItem) -> {
                songDownLoadImageView.gone()
                songUseButton.gone()
                songPlayerView.gone()
                songDownloadingAnim.show()
                songDownloadingAnim.playAnimation()
            }
            MusicDownloader.isDownLoaded(musicItem) -> {
                songUseButton.show()
                if (musicItemViewConfig?.usePlayIcon == true) {
                    songPlayerView.show()
                } else {
                    songPlayerView.gone()
                }
                songDownLoadImageView.gone()
                songDownloadingAnim.gone()
                songDownloadingAnim.cancelAnimation()
            }
            else -> {
                songUseButton.gone()
                songPlayerView.gone()
                songDownLoadImageView.show()
                songDownloadingAnim.gone()
                songDownloadingAnim.cancelAnimation()
            }
        }
    }

    fun setMusicDuration(musicItem: MusicItem, musicItemViewConfig: MusicItemViewConfig?) {
        val durationTxt = if (musicItem.duration != 0) {
            Utils.getTimeStr((musicItem.duration * 1000).toLong())
        } else if (musicItemViewConfig?.isLocalMusic == true && musicItem.duration == 0) {
            FileUtil.stringForTime(MediaUtil.getAudioFileInfo(musicItem.uri)?.duration ?: 0)
        } else {
            "00:00"
        }
        songDurationTextView.text = durationTxt
    }

    fun setMusicTitle(musicItem: MusicItem) {
        songTitleTextView.text = TextUtil.handleText(musicItem.title, 40)
        if (musicItem.author.isNullOrEmpty()) {
            songAuthorTextView.visibility = View.GONE
        } else {
            songAuthorTextView.visibility = View.VISIBLE
            songAuthorTextView.text = TextUtil.handleText(musicItem.author, 40)
        }
    }

    fun showProgress(isShow: Boolean) {
        if (isShow) {
            progressBar.show()
            songDurationTextView.gone()
        } else {
            progressBar.gone()
            songDurationTextView.show()
        }
    }

    fun setImageCover(musicItem: MusicItem, musicItemViewConfig: MusicItemViewConfig?) {
        val context: Context = itemView.context
        val imageUrl = musicItem.coverUrl?.cover_medium
        songItemImageView.layoutParams?.apply {
            width = UIUtils.dp2px(context, musicItemViewConfig?.musicImageViewWidth ?: 55F)
            height = UIUtils.dp2px(context, musicItemViewConfig?.musicImageViewHeight ?: 55F)
        }
        if (TextUtils.isEmpty(imageUrl)) {
            musicItemViewConfig?.also {
                songItemImageView.setImageResource(it.placeHolderDrawable)
            }
        } else {
            val imageOption = if (musicItemViewConfig?.placeHolderDrawable == null) {
                Builder().build()
            } else {
                Builder().placeHolder(musicItemViewConfig.placeHolderDrawable).build()
            }
            imageUrl?.also {
                ImageLoader.loadBitmap(
                        context,
                        it,
                        songItemImageView,
                        imageOption
                )
            }
        }
    }
}