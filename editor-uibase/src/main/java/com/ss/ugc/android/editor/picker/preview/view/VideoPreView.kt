package com.ss.ugc.android.editor.picker.preview.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import androidx.lifecycle.OnLifecycleEvent
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.picker.PickComponentConfig
import com.ss.ugc.android.editor.picker.data.model.MediaItem
import com.ss.ugc.android.editor.picker.preview.play.IVideoPlayer
import com.ss.ugc.android.editor.picker.preview.play.VideoPlayer
import com.ss.ugc.android.editor.picker.preview.play.VideoPlayerListener
import com.ss.ugc.android.editor.picker.preview.viewmodel.MaterialPreViewModel

class VideoPreView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    materialPreViewModel: MaterialPreViewModel,
    pickComponentConfig: PickComponentConfig
) : BasePreView(
    context, lifecycleOwner, materialPreViewModel, pickComponentConfig
) {
    private var videoPlayer: IVideoPlayer? = null
    private lateinit var textureView: TextureView
    private var videoSurface: Surface? = null
    private var videoThumbnailView: ImageView? = null
    private var isDestroyed = false
    private var videoErrorOccurred: Boolean = false
    private var videoPrepared: Boolean = false

    override var lifecycleObserver = object : LifecycleObserver {
        @Keep
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            if (isCurrentPage) {
                playVideoActual()
            } else {
                pauseVideoActual()
            }
        }

        @Keep
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            pauseVideoActual()
        }
    }

    override fun handlePageStateIdle(idle: Boolean) {
        super.handlePageStateIdle(idle)
        if (idle && isCurrentPage) {
            playVideoActual()
        } else {
            pauseVideoActual()
        }
    }

    override fun handlePageSelect(mediaItem: MediaItem) {
        super.handlePageSelect(mediaItem)
        if (mediaItem == data) {
            playVideoActual()
        } else {
            pauseVideoActual(true)
        }
    }

    override fun init() {
        super.init()
        initView()
    }

    private fun initView() {
        initTextureView()
        initThumbnailView()
    }

    private fun initTextureView() {
        textureView = provideTextureView(contentView)
    }

    private fun initThumbnailView() {
        videoThumbnailView = provideThumbnailView(contentView)
    }

    private fun initPlayer(path: String) {
        videoPlayer?.run {
            isLooping = true
            setDataSource(path)
            setVideoListener(object : VideoPlayerListener.Default() {
                override fun onPrepared(player: IVideoPlayer) {
                    videoPrepared = true
                    if (!isCurrentPage) {
                        player.pause()
                    }
                    updateRenderSize(player.videoWidth, player.videoHeight)
                }

                override fun onRenderStart() {
                    videoThumbnailView?.visibility = View.GONE
                }

                override fun onError() {
                    super.onError()
                    videoErrorOccurred = true
                }
            })
        }
    }

    private fun updateRenderSize(videoWidth: Int, videoHeight: Int) {
        if (videoWidth <= 0 || videoHeight <= 0) return
        var resultWidth = 0
        var resultHeight = 0
        val screenHeight = SizeUtil.getScreenHeight(context)
        val screenWidth = SizeUtil.getScreenWidth(context)
        val tempHeight = SizeUtil.getScreenWidth(context).toFloat() * videoHeight / videoWidth
        if (tempHeight >= screenHeight) {
            // 应该以屏幕高进行缩放
            resultHeight = screenHeight
            resultWidth = (resultHeight.toFloat() * videoWidth / videoHeight).toInt()
        } else {
            // 应该以屏幕宽进行缩放
            resultWidth = screenWidth
            resultHeight = tempHeight.toInt()
        }
        val oldParams = textureView.layoutParams
        if (oldParams != null && (oldParams.width != resultWidth || oldParams.height != resultHeight)) {
            oldParams.width = resultWidth
            oldParams.height = resultHeight
            textureView.layoutParams = oldParams
        }
    }

    private fun showThumbnail(mediaItem: MediaItem) {
        videoThumbnailView?.let {
            it.visibility = View.VISIBLE
            load(mediaItem.uri, it, it.measuredWidth, it.measuredHeight)
        }
    }

    fun playVideoActual() {
        if (isDestroyed) {
            return
        }
        videoPlayer?.run {
            if (!isPlaying()) play()
        }
    }

    fun pauseVideoActual(reset: Boolean = false) {
        if (isDestroyed) {
            return
        }
        videoPlayer?.run {
            if (isPlaying()) pause()
            if (isPrepared && reset) seekTo(0)
        }
    }

    override fun provideContentView(): ViewGroup {
        return (LayoutInflater.from(context)
            .inflate(R.layout.layout_preview_video, null, false) as ViewGroup)
    }

    override fun onPreViewShow() {
        data?.let {
            showThumbnail(it)
            videoPlayer = VideoPlayer()
            initPlayer(it.path)

            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    texture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    videoPlayer?.also { player ->
                        val surface = Surface(texture)
                        videoSurface = surface
                        player.setSurface(surface)
                    }
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) = Unit

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
            }
        }
    }

    private fun provideTextureView(content: ViewGroup): TextureView {
        return content.findViewById(R.id.video_texture)
    }

    private fun provideThumbnailView(content: ViewGroup): ImageView {
        return content.findViewById(R.id.iv_video_thumbnail)
    }

    override fun destroy() {
        if (isDestroyed) return
        isDestroyed = true
        super.destroy()
        videoPlayer?.stop()
        videoPlayer?.release()
        videoSurface?.release()
    }

    private fun load(
        uri: Uri,
        view: ImageView,
        resWidth: Int,
        resHeight: Int
    ): Boolean {
        val requestOptions = if (resWidth > 0 && resHeight > 0) {
            val overrideWidth = Math.min(view.measuredWidth, resWidth)
            val overrideHeight = Math.min(view.measuredHeight, resHeight)
            ImageOption.Builder().width(overrideWidth).height(overrideHeight).build()
        } else {
            ImageOption.Builder().width(view.measuredWidth).height(view.measuredHeight).build()
        }
        pickComponentConfig.imageLoader.loadBitmap(
            view.context,
            uri.toString(),
            view,
            requestOptions
        )
        return true
    }

}