package com.ss.ugc.android.editor.base.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import com.ss.ugc.android.editor.core.utils.DLog
import java.io.*
import kotlin.math.roundToInt


object BitmapUtil {
    /**
     * 把图片压缩成最长边 600 像素 且校正方向
     */
    fun compressSize(srcFile: File?): File? {
        if (srcFile != null && srcFile.exists() && srcFile.isFile) {
            if (isGIF(srcFile)) {
                FileOutputStream(srcFile).use { it.write(srcFile.readBytes()) }
                return srcFile
            } else {
                val maxSlideLength = 600F

                // 计算原图片长宽并计算压缩比例
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(srcFile.absolutePath, options)
                val inSampleSize = if (options.outWidth > options.outHeight) {
                    if (options.outWidth > maxSlideLength) {
                        options.outWidth / maxSlideLength
                    } else {
                        1F
                    }
                } else {
                    if (options.outHeight > maxSlideLength) {
                        options.outHeight / maxSlideLength
                    } else {
                        1F
                    }
                }

                // 获取压缩后的图片
                val newOptions = BitmapFactory.Options()
                newOptions.inSampleSize = inSampleSize.toInt()
                val srcBitmap = BitmapFactory.decodeFile(srcFile.absolutePath, newOptions)

                // 计算矩阵旋转角度和缩放值
                val degree = readPictureDegree(srcFile.absolutePath)
                val matrix = Matrix()
                matrix.postRotate(degree.toFloat())

                ByteArrayOutputStream().apply {
                    srcBitmap.let {
                        if (it.config != Bitmap.Config.ARGB_8888) {
                            val resultBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
                            it.recycle()
                            resultBitmap
                        } else {
                            it
                        }
                    }.also { bitmap ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                        bitmap.recycle()
                    }

                    FileOutputStream(srcFile).use {
                        it.write(toByteArray())
                    }
                    close()
                }
                return srcFile
            }
        }
        return null
    }

    fun decodeBitmap(
        path: String,
        maxSlideLength: Float
    ): Bitmap? {
        // 计算原图片长宽并计算压缩比例
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        val inSampleSize = if (options.outWidth > options.outHeight) {
            if (options.outWidth > maxSlideLength) {
                options.outWidth / maxSlideLength
            } else {
                1F
            }
        } else {
            if (options.outHeight > maxSlideLength) {
                options.outHeight / maxSlideLength
            } else {
                1F
            }
        }

        // 获取压缩后的图片
        val newOptions = BitmapFactory.Options()
        newOptions.inSampleSize = inSampleSize.toInt()
        return BitmapFactory.decodeFile(path, newOptions)
    }

    fun compressBitmap(bitmap: Bitmap, result: File, maxSlideLength: Float = 600F) {
        if (bitmap.isRecycled) {
            DLog.e("compressBitmap", "compressBitmap failed,bitmap is isRecycled !!!" )
            return
        }
        // 获取原图片长宽并计算压缩比例
        val inSampleSize = if (bitmap.width > bitmap.height) {
            if (bitmap.width > maxSlideLength) {
                maxSlideLength / bitmap.width
            } else {
                1F
            }
        } else {
            if (bitmap.height > maxSlideLength) {
                maxSlideLength / bitmap.height
            } else {
                1F
            }
        }

        // 获取压缩后的图片
        val matrix = Matrix().apply {
            postScale(inSampleSize, inSampleSize)
        }
        val srcBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        ByteArrayOutputStream().apply {
            srcBitmap.let {
                if (it.config != Bitmap.Config.ARGB_8888) {
                    val resultBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
                    it.recycle()
                    resultBitmap
                } else {
                    it
                }
            }.also { bitmap ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, this)
                bitmap.recycle()
            }
            FileOutputStream(result).use {
                it.write(toByteArray())
            }
            close()
        }
    }

    fun compressBitmap(bitmap: Bitmap, maxSlideLength: Float = 600F): Bitmap? {
        if (bitmap.isRecycled) {
            DLog.e("compressBitmap", "compressBitmap failed,bitmap is isRecycled !!!" )
            return null
        }
        // 获取原图片长宽并计算压缩比例
        val inSampleSize = if (bitmap.width > bitmap.height) {
            if (bitmap.width > maxSlideLength) {
                maxSlideLength / bitmap.width
            } else {
                1F
            }
        } else {
            if (bitmap.height > maxSlideLength) {
                maxSlideLength / bitmap.height
            } else {
                1F
            }
        }

        // 获取压缩后的图片
        val matrix = Matrix().apply {
            postScale(inSampleSize, inSampleSize)
        }
        val srcBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val outputStream = ByteArrayOutputStream().apply {
            srcBitmap.let {
                if (it.config != Bitmap.Config.ARGB_8888) {
                    val resultBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
                    it.recycle()
                    resultBitmap
                } else {
                    it
                }
            }.also { bitmap ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, this)
                bitmap.recycle()
            }
        }
        val bitmapStream = ByteArrayInputStream(outputStream.toByteArray())
        return BitmapFactory.decodeStream(bitmapStream)
    }

    /**
     * 对图片进行模糊处理
     */
    fun blurBitmap(context: Context, bitmap: Bitmap, blurRadius: Float): Bitmap {

        val width = (bitmap.width * 0.4f).roundToInt()
        val height = (bitmap.height * 0.4f).roundToInt()

        val inputBitmap =
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        val outputBitmap =
            Bitmap.createBitmap(inputBitmap)

        val rs = RenderScript.create(context)
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(blurRadius * 25f)
        blurScript.setInput(tmpIn)
        blurScript.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)

        tmpIn.destroy()
        tmpOut.destroy()
        blurScript.destroy()
        rs.destroy()
        inputBitmap.recycle()

        return outputBitmap
    }

    private fun isGIF(srcFile: File) = FileUtil.getImageType(srcFile) == FileUtil.ImageType.GIF

    private fun readPictureDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            degree = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

}