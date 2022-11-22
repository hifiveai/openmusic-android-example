package com.ss.ugc.android.davincieditor.image

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.ss.ugc.android.editor.base.imageloder.IImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import java.util.concurrent.ExecutionException

class GlideImageLoader : IImageLoader {

    override fun loadGif(
        context: Context,
        path: String,
        imageView: ImageView,
        option: ImageOption
    ) {
        Glide.with(context).asGif().apply(option.toGlideRequestOption()).load(path).into(imageView)
    }

    override fun resizeBitmapSync(
        context: Context,
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): Bitmap? {
        try {
            return Glide.with(context).asBitmap().apply(RequestOptions().centerCrop())
                .load(bitmap)
                .submit(width, height)
                .get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return null
    }

    override fun loadBitmap(
        context: Context,
        path: String,
        imageView: ImageView,
        option: ImageOption
    ) {
        Glide.with(context).load(path).apply(option.toGlideRequestOption())
            .into(imageView)
        val gifDrawable = imageView.drawable as? GifDrawable
        gifDrawable?.apply {
            start()
        }
    }

    override fun loadBitmapSync(
        context: Context,
        path: String,
        option: ImageOption
    ): Bitmap? {
        try {

            return Glide.with(context).asBitmap().apply(option.toGlideRequestOption())
                .load(path)
                .submit(SIZE_ORIGINAL, SIZE_ORIGINAL)
                .get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        return null
    }

    override fun loadImageBitmap(context: Context, bitmap: Bitmap, imageView: ImageView, option: ImageOption) {
        Glide.with(context).asBitmap().apply(option.toGlideRequestOption()).load(bitmap).into(imageView)
    }
}