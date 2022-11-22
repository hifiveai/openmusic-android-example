package com.ss.ugc.android.editor.main

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.createScaledBitmap
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import com.ss.ugc.android.editor.core.utils.DLog
import java.util.*

class ExportThumbnailView(c: Context, @Nullable att: AttributeSet?) : View(c, att) {

//    private val TAG: String = "Export ANIMATION"
//
//    // Thumbnail image
//    private var thumbnailBitmap: Bitmap?
//    private var thumbnailWidth: Int
//    private var thumbnailHeight: Int
//    private var thumbnailRatio: Float // ratio = width / height
//    private var thumbnailCircumference: Int
//    private var thumbnailPaint: Paint?
//    private var thumbnailColorFilter: ColorFilter
//    private var hasShrunk: Boolean
//    private var appWidth: Int
//    private var desiredWidth = 0
//    private var desiredHeight = 0
//
//    // Outline
//    private var outlineColor: Int = Color.WHITE
//    private var outlineWidth: Int = 12
//    private var outlineTopPaint: Paint
//    private var outlineRightPaint: Paint
//    private var outlineBottomPaint: Paint
//    private var outlineLeftPaint: Paint
//    private var outlinePathTop: Path
//    private var outlinePathBottom: Path
//    private var outlinePathLeft: Path
//    private var outlinePathRight: Path
//    private lateinit var outlineGradientTop: LinearGradient
// //   private var outlineGradientBottom: LinearGradient
// //   private var outlineGradientLeft: LinearGradient
// //   private var outlineGradientRight: LinearGradient
//
//    // Progress Number
//    private var progressFormat: Formatter
//    private var progressText: String
//    private var progressPaint: Paint
//
//    // Coordinates
//    private var thumbnailLeft: Float = 0F
//    private val thumbnailTop: Float = 0F
//    private var progressLeft: Float = -1F
//    private var progressTop: Float = 0F
//    private var hasInitialized: Boolean = false
//
//    // initial positions
//    private var outlineTopX: Float = 0F
//    private var outlineTopY: Float = 0F
//    private var outlineBottomX: Float = 0F
//    private var outlineBottomY: Float = 0F
//    private var outlineLeftX: Float = 0F
//    private var outlineLeftY: Float = 0F
//    private var outlineRightX: Float = 0F
//    private var outlineRightY: Float = 0F
//
//    // moveTo positions
//    private var topToX: Float = 0F
//    private var topToY: Float = 0F
//    private var bottomToX: Float = 0F
//    private var bottomToY: Float = 0F
//    private var leftToX: Float = 0F
//    private var leftToY: Float = 0F
//    private var rightToX: Float = 0F
//    private var rightToY: Float = 0F
//
//    init {
//        // Initializing bitmap-related variables
//        thumbnailBitmap = null
//        thumbnailWidth = 0
//        thumbnailHeight = 0
//        thumbnailCircumference = 0
//        thumbnailRatio = 0F
//        hasShrunk = false
//        thumbnailPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//        thumbnailColorFilter = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)
//        thumbnailPaint!!.colorFilter = thumbnailColorFilter
//        appWidth = 0
//
//        // Initializing outline paint and style
//        outlineTopPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//        outlineTopPaint.style = Paint.Style.STROKE
//        outlineTopPaint.color = outlineColor
//        outlineTopPaint.strokeWidth = outlineWidth.toFloat()
//
//        outlineBottomPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//        outlineBottomPaint.style = Paint.Style.STROKE
//        outlineBottomPaint.color = outlineColor
//        outlineBottomPaint.strokeWidth = outlineWidth.toFloat()
//
//        outlineRightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//        outlineRightPaint.style = Paint.Style.STROKE
//        outlineRightPaint.color = outlineColor
//        outlineRightPaint.strokeWidth = outlineWidth.toFloat()
//
//        outlineLeftPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//        outlineLeftPaint.style = Paint.Style.STROKE
//        outlineLeftPaint.color = outlineColor
//        outlineLeftPaint.strokeWidth = outlineWidth.toFloat()
//
//        // Initializing progress number
//        progressFormat = Formatter()
//        progressText = ""
//        progressPaint = Paint()
//        progressPaint.textSize = 65F
//        progressPaint.color = Color.WHITE
//        progressPaint.isAntiAlias = true
//
//        // Initializing all the paths
//        outlinePathTop = Path()
//        outlinePathBottom = Path()
//        outlinePathLeft = Path()
//        outlinePathRight = Path()
//
//        animation = null
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        if (thumbnailRatio != 0F) {
//            desiredWidth = (800 * thumbnailRatio).toInt()
//            desiredHeight = 800
//            if (thumbnailHeight > 800) {
//                hasShrunk = true
//                thumbnailHeight = desiredHeight
//                thumbnailWidth = desiredWidth
//                thumbnailCircumference = 2 * (thumbnailHeight + thumbnailWidth)
//            }
//        }
//        setMeasuredDimension(desiredWidth, desiredHeight)
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        DLog.d(TAG, "Calling onDraw.")
//        super.onDraw(canvas)
//
//        if (this.thumbnailBitmap != null) {
//            if (!hasShrunk) {
//                thumbnailBitmap!!.density = Bitmap.DENSITY_NONE
//                canvas?.drawBitmap(thumbnailBitmap!!, thumbnailLeft, thumbnailTop, thumbnailPaint)
//            } else {
//                var shrunkBitmap =
//                    createScaledBitmap(thumbnailBitmap!!, desiredWidth, desiredHeight, false)
//                shrunkBitmap.density = Bitmap.DENSITY_NONE
//                thumbnailLeft = ((appWidth - thumbnailWidth) / 2).toFloat()
//                progressLeft = thumbnailLeft + thumbnailWidth / 2 - progressPaint.textSize * 2 / 3
//                progressTop = thumbnailTop + thumbnailHeight / 2 - progressPaint.textSize / 3
//                canvas?.drawBitmap(shrunkBitmap, thumbnailLeft, thumbnailTop, thumbnailPaint)
//            }
//        }
//
//        if (progressLeft >= 0F) {
//            canvas?.drawText(
//                progressText, progressLeft,
//                progressTop, progressPaint
//            )
//        }
//
//        outlinePathTop.reset()
//        outlinePathTop.moveTo(outlineTopX, outlineTopY)
//        outlinePathTop.lineTo(topToX, topToY)
//        canvas?.drawPath(outlinePathTop, outlineTopPaint)
//
//        outlinePathRight.reset()
//        outlinePathRight.moveTo(outlineRightX, outlineRightY)
//        outlinePathRight.lineTo(rightToX, rightToY)
//        canvas?.drawPath(outlinePathRight, outlineRightPaint)
//
//        outlinePathLeft.reset()
//        outlinePathLeft.moveTo(outlineLeftX, outlineLeftY)
//        outlinePathLeft.lineTo(leftToX, leftToY)
//        canvas?.drawPath(outlinePathLeft, outlineLeftPaint)
//
//        outlinePathBottom.reset()
//        outlinePathBottom.moveTo(outlineBottomX, outlineBottomY)
//        outlinePathBottom.lineTo(bottomToX, bottomToY)
//        canvas?.drawPath(outlinePathBottom, outlineBottomPaint)
//    }
//
//    fun updateBitmap(bitmap: Bitmap, width: Int, height: Int, appWidth: Int) {
//        this.thumbnailBitmap = bitmap
//        thumbnailHeight = height
//        thumbnailWidth = width
//        thumbnailRatio = width.toFloat() / height.toFloat()
//        thumbnailCircumference = 2 * (thumbnailHeight + thumbnailWidth)
//        this.appWidth = appWidth
//        thumbnailLeft = ((appWidth - width) / 2).toFloat()
//
//        progressLeft = thumbnailLeft + thumbnailWidth / 2 - progressPaint.textSize * 2 / 3
//        progressTop = thumbnailTop + thumbnailHeight / 2 - progressPaint.textSize / 3
//
////        outlineGradientTop = LinearGradient(outlineTopX, outlineTopY, topToX, topToY, Color.parseColor("#C239AF"),
////            Color.parseColor("#67B8ED"), Shader.TileMode.CLAMP)
////        outlineTopPaint.shader = outlineGradientTop
//
//        requestLayout()
//    }
//
//    fun onProgressChanged(progress: Float) {
//        progressText = "${(progress * 100).toInt()}%"
//        DLog.d(TAG, "Progress updated to: $progress")
//        resetOutlineCoordinates(
//            thumbnailWidth.toFloat(),
//            thumbnailHeight.toFloat(),
//            outlineWidth.toFloat(),
//            progress
//        )
//        invalidate()
//    }
//
//    private fun resetOutlineCoordinates(w: Float, h: Float, strokeWidth: Float, progress: Float) {
//        val length = progress * thumbnailCircumference
//        if (hasInitialized) {
//            when (length) {
//                in 0F..(w + strokeWidth) ->
//                    topToX = outlineTopX + length
//                in (w + strokeWidth)..(w + h + 2 * strokeWidth) -> {
//                    topToX = thumbnailLeft + w + strokeWidth
//                    rightToY = outlineRightY + length - w - strokeWidth
//                }
//                in (w + h + 2 * strokeWidth)..(2 * w + h + 2 * strokeWidth) -> {
//                    rightToY = thumbnailTop + h + strokeWidth
//                    bottomToX = outlineBottomX - (length - w - h - 2 * strokeWidth)
//                }
//                in (h + 2 * w + 2 * strokeWidth)..(4 * strokeWidth + thumbnailCircumference.toFloat()) -> {
//                    bottomToX = thumbnailLeft - strokeWidth
//                    leftToY = outlineLeftY - (length - h - 2 * w - 2 * strokeWidth)
//                }
//            }
//        } else {
//            hasInitialized = true
//            outlineTopX = thumbnailLeft
//            outlineTopY = thumbnailTop - strokeWidth / 2
//            outlineRightX = thumbnailLeft + w + strokeWidth / 2
//            outlineRightY = thumbnailTop
//            outlineBottomX = thumbnailLeft + w
//            outlineBottomY = thumbnailTop + h + strokeWidth / 2
//            outlineLeftX = thumbnailLeft - strokeWidth / 2
//            outlineLeftY = thumbnailTop + h
//
//            topToX = outlineTopX
//            topToY = outlineTopY
//            rightToX = outlineRightX
//            rightToY = outlineRightY
//            bottomToX = outlineBottomX
//            bottomToY = outlineBottomY
//            leftToX = outlineLeftX
//            leftToY = outlineLeftY
//        }
//    }
//
//    fun fillFrame() {
//        invalidate()
//        leftToY = thumbnailTop - outlineWidth
//        thumbnailPaint?.colorFilter = null
//        progressText = ""
//    }
//
//    private var isAnimating = false
//    fun eraseFrameWithAnimation() {
//        isAnimating = true
//        val animator = ValueAnimator.ofInt(0, 255)
//        animator.addUpdateListener {
//            val value = animator.animatedValue as Int
//            val newAlpha = 255 - value
//            outlineTopPaint.alpha = newAlpha
//            outlineBottomPaint.alpha = newAlpha
//            outlineRightPaint.alpha = newAlpha
//            outlineLeftPaint.alpha = newAlpha
//            invalidate()
//        }
//        animator.duration = 800
//        animator.interpolator = LinearInterpolator()
//        animator.start()
//    }
}