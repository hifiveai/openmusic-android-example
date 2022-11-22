package com.ss.ugc.android.editor.base.imageloder

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView

interface IImageLoader{

    fun loadBitmapSync(
        context: Context,
        path: String,
        option: ImageOption
    ): Bitmap?

    fun loadBitmap(context: Context, path: String, imageView: ImageView, option: ImageOption)

    fun loadImageBitmap(context: Context, bitmap: Bitmap, imageView: ImageView, option: ImageOption)

    fun loadGif(context: Context, path: String, imageView: ImageView, option: ImageOption)

    fun resizeBitmapSync(context: Context, bitmap: Bitmap, width: Int, height: Int): Bitmap?

}

