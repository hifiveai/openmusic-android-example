package com.ss.ugc.android.editor.base.imageloder

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.ss.ugc.android.editor.base.EditorSDK

object ImageLoader : IImageLoader {
    override fun loadBitmapSync(context: Context, path: String, option: ImageOption): Bitmap? {
        return EditorSDK.instance.imageLoader().loadBitmapSync(context, path, option)
    }

    override fun loadGif(
        context: Context,
        path: String,
        imageView: ImageView,
        option: ImageOption
    ) {
        EditorSDK.instance.imageLoader().loadGif(context, path, imageView, option)
    }

    override fun loadBitmap(
        context: Context,
        path: String,
        imageView: ImageView,
        option: ImageOption
    ) {
        EditorSDK.instance.imageLoader().loadBitmap(context, path, imageView, option)
    }

    override fun loadImageBitmap(
        context: Context,
        bitmap: Bitmap,
        imageView: ImageView,
        option: ImageOption
    ) {
        EditorSDK.instance.imageLoader().loadImageBitmap(context, bitmap, imageView, option)
    }

    override fun resizeBitmapSync(
        context: Context,
        bitmap: Bitmap,
        width: Int,
        height: Int
    ): Bitmap? {
        return EditorSDK.instance.imageLoader().resizeBitmapSync(context, bitmap, width, height)
    }


}