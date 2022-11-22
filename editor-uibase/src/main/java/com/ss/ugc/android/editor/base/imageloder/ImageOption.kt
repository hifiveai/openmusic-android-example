package com.ss.ugc.android.editor.base.imageloder

import android.graphics.Bitmap
import android.widget.ImageView

open class ImageOption constructor(builder: Builder) {
    var width = builder.width
    var height = builder.height
    var format = builder.format
    var placeHolder = builder.placeHolder
    var scaleType = builder.scaleType
    var skipMemoryCached = builder.skipMemoryCached
    var skipDiskCached = builder.skipDiskCached
    var roundCorner = builder.roundCorner
    var blurRadius = builder.blurRadius

    open class Builder {
        var width: Int? = null
        var height: Int? = null
        var format: Bitmap.Config? = null
        var placeHolder: Int? = null
        var scaleType: ImageView.ScaleType? = null
        var skipMemoryCached = false
        var skipDiskCached = false
        var roundCorner: Int = 0
        var blurRadius: Int = 0


        fun width(width : Int )= apply {  this.width = width}
        fun height(height : Int )= apply {  this.height = height}
        fun format(format: Bitmap.Config) = apply { this.format = format }
        fun placeHolder(placeHolder: Int )= apply { this.placeHolder = placeHolder }
        fun scaleType( scaleType: ImageView.ScaleType) = apply { this.scaleType = scaleType }

        fun skipMemoryCached( skip : Boolean) = apply { this.skipMemoryCached = skip }
        fun skipDiskCached(skip: Boolean) = apply { this.skipDiskCached = skip }
        fun roundCorner(roundCorner: Int) = apply { this.roundCorner = roundCorner }
        fun blurRadius(blurRadius: Int) = apply { this.blurRadius = blurRadius }

        fun build() = ImageOption(this)

    }
}

