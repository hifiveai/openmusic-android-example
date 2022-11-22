//@file:JvmName("OptionUtils")
package com.ss.ugc.android.davincieditor.image

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bytedance.ck.ckentrance.editor.BlurTransformation
import com.ss.ugc.android.editor.base.imageloder.ImageOption

fun ImageOption.toGlideRequestOption(): RequestOptions {

    val transformationList = arrayListOf<BitmapTransformation>()

    when (scaleType) {
        ImageView.ScaleType.CENTER_CROP -> transformationList.add(CenterCrop())
        ImageView.ScaleType.CENTER_INSIDE -> transformationList.add(CenterInside())
        ImageView.ScaleType.FIT_CENTER -> transformationList.add(FitCenter())
        else -> {
        }
    }

    if (roundCorner > 0) transformationList.add(RoundedCorners(roundCorner))

    if (blurRadius > 0) transformationList.add(BlurTransformation(blurRadius, 2))

    var option = if (transformationList.isEmpty()) RequestOptions() else RequestOptions.bitmapTransform(MultiTransformation(transformationList))

    if (skipDiskCached) {
        option = option.diskCacheStrategy(DiskCacheStrategy.NONE)
    }
    if (width != null && height != null) {
        option = option.override(width!!, height!!)
    }
    placeHolder?.let {
        option.placeholder(it)
        option.error(it)
    }
    when (format) {
        Bitmap.Config.ARGB_8888 -> {
            option.format(DecodeFormat.PREFER_ARGB_8888)
        }

        Bitmap.Config.RGB_565 -> {
            option.format(DecodeFormat.PREFER_RGB_565)
        }

        else -> {

        }
    }

    return option
}