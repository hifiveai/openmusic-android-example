package com.ss.ugc.android.editor.main.template

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.Nullable
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.main.R
import java.util.*

class TemplateExportView(c: Context, @Nullable att: AttributeSet?) : View(c, att) {

    private val TAG: String = "Export ANIMATION"

    companion object {
        const val OUTLINE_GAP = 40
        const val UPLOAD_PART_ONE = 0
        const val UPLOAD_PART_TWO = 1
        const val UPLOAD_PART_THREE = 2
        const val UPLOAD_PART_FOUR = 3

        const val RANGE_ONE = 0.15
        const val RANGE_TWO = 0.45
        const val RANGE_THREE = 0.30
        const val RANGE_FOUR = 1.00
    }

    // Thumbnail image
    private var coverBitmap: Bitmap?
    private var coverWidth: Int
    private var coverHeight: Int
    private var coverRatio: Float // ratio = width / height
    private var coverCircumference: Int
    private var coverPaint: Paint?
    private var coverPaintFilter: ColorFilter
    private var hasShrunk: Boolean
    private var appWidth: Int

    // Outline
    private var outlineVerticalColor: Int = Color.WHITE
    private lateinit var outlineGradientLeft: LinearGradient
    private lateinit var outlineGradientRight: LinearGradient
    private var outlineTopColor: Int = R.color.fuchsia_pink
    private var outlineBottomColor: Int = R.color.baby_blue
    private var outlineWidth: Int = 26

    private var outlineTopPaint: Paint
    private var outlineRightPaint: Paint
    private var outlineBottomPaint: Paint
    private var outlineLeftPaint: Paint

    private var outlinePathTop1: Path
    private var outlinePathTop2: Path
    private var outlinePathBottom: Path
    private var outlinePathLeft: Path
    private var outlinePathRight: Path

    // Progress Number
    private var progressFormat: Formatter
    private var progressText: String
    private var progressPaint: Paint
    var progressValue: Double = 0.0

    // Coordinates
    private var coverLeft: Float = 0F
    private var coverTop: Float = 0F
    private var progressLeft: Float = -1F
    private var progressTop: Float = 0F

    // initial positions
    private var outlineTopX1: Float = 0F
    private var outlineTopX2: Float = 0F
    private var outlineTopY: Float = 0F
    private var outlineBottomX: Float = 0F
    private var outlineBottomY: Float = 0F
    private var outlineLeftX: Float = 0F
    private var outlineLeftY: Float = 0F
    private var outlineRightX: Float = 0F
    private var outlineRightY: Float = 0F

    // moveTo positions
    private var topToX1: Float = 0F
    private var topToX2: Float = 0F
    private var bottomToX: Float = 0F
    private var leftToY: Float = 0F
    private var rightToY: Float = 0F

    init {
        // Initializing bitmap-related variables
        coverBitmap = null
        coverWidth = 0
        coverHeight = 0
        coverCircumference = 0
        coverRatio = 0F
        hasShrunk = false
        coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        coverPaintFilter = PorterDuffColorFilter(resources.getColor(R.color.transparent_30p_white), PorterDuff.Mode.SCREEN)
        coverPaint!!.colorFilter = coverPaintFilter

        appWidth = 0

        // Initializing outline paint and style
        outlineTopPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outlineTopPaint.apply {
            style = Paint.Style.STROKE
            color = resources.getColor(outlineTopColor)
            strokeWidth = outlineWidth.toFloat()
        }

        outlineBottomPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outlineBottomPaint.apply {
            style = Paint.Style.STROKE
            color = resources.getColor(outlineBottomColor)
            strokeWidth = outlineWidth.toFloat()
        }

        outlineRightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outlineRightPaint.apply {
            style = Paint.Style.STROKE
            color = outlineVerticalColor
            strokeWidth = outlineWidth.toFloat()
        }

        outlineLeftPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        outlineLeftPaint.apply {
            style = Paint.Style.STROKE
            color = outlineVerticalColor
            strokeWidth = outlineWidth.toFloat()
        }

        // Initializing progress number
        progressFormat = Formatter()
        progressText = ""
        progressPaint = Paint()
        progressPaint.apply {
            textSize = 105F
            color = Color.BLACK
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        // Initializing all the paths
        outlinePathTop1 = Path()
        outlinePathTop2 = Path()
        outlinePathBottom = Path()
        outlinePathLeft = Path()
        outlinePathRight = Path()

        animation = null
    }

    fun updateBitmap(bitmap: Bitmap, width: Int, height: Int, appWidth: Int) {
        this.coverBitmap = bitmap
        coverHeight = height
        coverWidth = width
        coverRatio = width.toFloat() / height.toFloat()
        coverCircumference = 2 * (coverHeight + coverWidth)
        this.appWidth = appWidth
        coverLeft = (appWidth - width).toFloat() / 2
        coverTop = 70F
        progressLeft = coverLeft + coverWidth / 2 - progressPaint.textSize * 2 / 3
        progressTop = coverTop + coverHeight / 2
        DLog.d("TemplateExportView", "updateBitmap: coverHeight:$coverHeight coverWidth:$coverWidth  coverRatio:$coverRatio")
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (coverRatio == 0f) return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        DLog.d("TemplateExportView", "updateBitmap: coverHeight:$coverHeight coverWidth:$coverWidth  coverRatio:$coverRatio")

        val desiredWidth = (800 * coverRatio).toInt()
        val desiredHeight = 800
        if (coverHeight > 800 || coverWidth < 200) {
            coverHeight = desiredHeight
            coverWidth = desiredWidth
            coverCircumference = 2 * (coverHeight + coverWidth)
            resizeBitmap(desiredWidth, desiredHeight)
        }
        DLog.d("TemplateExportView", "onMeasure: coverHeight:$coverHeight coverWidth:$coverWidth  coverRatio:$coverRatio")
        initializeOutlineCoordinates()
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    private fun resizeBitmap(desiredWidth: Int, desiredHeight: Int) {
        val shrunkBitmap =
            Bitmap.createScaledBitmap(coverBitmap!!, desiredWidth, desiredHeight, false)
        coverBitmap = shrunkBitmap
        coverLeft = ((appWidth - coverWidth) / 2).toFloat()
        progressLeft = coverLeft + coverWidth / 2 - progressPaint.textSize * 2 / 3
        progressTop = coverTop + coverHeight / 2
        initializeOutlineCoordinates()
    }

    /**
     * 设置坐标起始点。模板导出的起始点为上方中央，逆时针运行。
     */
    private fun initializeOutlineCoordinates() {
        outlineTopX1 = coverLeft + coverWidth / 2
        outlineTopY = coverTop - OUTLINE_GAP - outlineWidth / 2
        outlineLeftX = coverLeft - outlineWidth / 2 - OUTLINE_GAP
        outlineLeftY = outlineTopY + outlineWidth / 2
        outlineBottomX = coverLeft - OUTLINE_GAP
        outlineBottomY = coverTop + coverHeight + outlineWidth / 2 + OUTLINE_GAP
        outlineRightX = coverLeft + coverWidth + outlineWidth / 2 + OUTLINE_GAP
        outlineRightY = outlineBottomY - outlineWidth / 2
        outlineTopX2 = coverLeft + coverWidth + OUTLINE_GAP

        topToX1 = outlineTopX1
        topToX2 = outlineTopX2
        rightToY = outlineRightY
        bottomToX = outlineBottomX
        leftToY = outlineLeftY

        outlineGradientLeft = LinearGradient(
            coverLeft - OUTLINE_GAP, outlineTopY,
            coverLeft - OUTLINE_GAP, coverTop + coverHeight + OUTLINE_GAP, Color.parseColor("#C239AF"),
            Color.parseColor("#67B8ED"), Shader.TileMode.CLAMP
        )
        outlineLeftPaint.shader = outlineGradientLeft

        outlineGradientRight = LinearGradient(
            coverLeft + coverWidth + OUTLINE_GAP, outlineTopY + coverHeight + OUTLINE_GAP,
            coverLeft + coverWidth + OUTLINE_GAP, outlineTopY - OUTLINE_GAP,
            Color.parseColor("#67B8ED"), Color.parseColor("#C239AF"), Shader.TileMode.CLAMP
        )
        outlineRightPaint.shader = outlineGradientRight
    }

    override fun onDraw(canvas: Canvas?) {
        DLog.d(TAG, "Calling onDraw.")
        super.onDraw(canvas)

        if (this.coverBitmap != null) {
            coverBitmap!!.density = Bitmap.DENSITY_NONE
            canvas?.drawBitmap(coverBitmap!!, coverLeft, coverTop, coverPaint)
        }

        if (progressLeft >= 0F) {
            canvas?.drawText(
                progressText, progressLeft,
                progressTop, progressPaint
            )
        }

        outlinePathTop1.reset()
        outlinePathTop1.moveTo(outlineTopX1, outlineTopY)
        outlinePathTop1.lineTo(topToX1, outlineTopY)
        canvas?.drawPath(outlinePathTop1, outlineTopPaint)

        outlinePathRight.reset()
        outlinePathRight.moveTo(outlineRightX, outlineRightY)
        outlinePathRight.lineTo(outlineRightX, rightToY)
        canvas?.drawPath(outlinePathRight, outlineRightPaint)

        outlinePathLeft.reset()
        outlinePathLeft.moveTo(outlineLeftX, outlineLeftY)
        outlinePathLeft.lineTo(outlineLeftX, leftToY)
        canvas?.drawPath(outlinePathLeft, outlineLeftPaint)

        outlinePathBottom.reset()
        outlinePathBottom.moveTo(outlineBottomX, outlineBottomY)
        outlinePathBottom.lineTo(bottomToX, outlineBottomY)
        canvas?.drawPath(outlinePathBottom, outlineBottomPaint)

        outlinePathTop2.reset()
        outlinePathTop2.moveTo(outlineTopX2, outlineTopY)
        outlinePathTop2.lineTo(topToX2, outlineTopY)
        canvas?.drawPath(outlinePathTop2, outlineTopPaint)
    }

    fun onProgressChanged(progress: Int, uploadPart: Int) {
        progressValue = when (uploadPart) {
            UPLOAD_PART_ONE -> progress * RANGE_ONE
            UPLOAD_PART_TWO -> RANGE_ONE * 100 + progress * RANGE_TWO
            UPLOAD_PART_THREE -> RANGE_ONE * 100 + RANGE_TWO * 100 + progress * RANGE_THREE
            UPLOAD_PART_FOUR -> progress.toDouble() // (RANGE_ONE + RANGE_TWO) * 100 + progress * RANGE_THREE
            else -> 100.0
        }
        progressText = "${progressValue.toInt()}%"
        DLog.d(TAG, "Progress updated to: $progressValue")
        resetOutlineCoordinates((progressValue / 100).toFloat())
        invalidate()
    }

    private fun resetOutlineCoordinates(progress: Float) {
        val w = coverWidth.toFloat()
        val h = coverHeight.toFloat()
        val sw = outlineWidth.toFloat()

        when (val length = progress * (coverCircumference + 8 * OUTLINE_GAP + 4 * sw)) {
            in 0F..(w / 2 + OUTLINE_GAP + sw) ->
                topToX1 = outlineTopX1 - length
            in (w / 2 + OUTLINE_GAP + sw)..(w / 2 + h + 3 * OUTLINE_GAP + 2 * sw) -> {
                topToX1 = coverLeft - OUTLINE_GAP - sw
                leftToY = outlineLeftY + length - (w / 2 + OUTLINE_GAP + sw)
            }
            in (w / 2 + h + 3 * OUTLINE_GAP + 2 * sw)..(w * 3 / 2 + h + 5 * OUTLINE_GAP + 3 * sw) -> {
                topToX1 = coverLeft - OUTLINE_GAP - sw
                leftToY = coverTop + coverHeight + OUTLINE_GAP + sw
                bottomToX = outlineBottomX + length - (w / 2 + h + 3 * OUTLINE_GAP + 2 * sw)
            }
            in (w * 3 / 2 + h + 5 * OUTLINE_GAP + 3 * sw)..(w * 3 / 2 + 2 * h + 7 * OUTLINE_GAP + 4 * sw) -> {
                topToX1 = coverLeft - OUTLINE_GAP - sw
                leftToY = coverTop + coverHeight + OUTLINE_GAP + sw
                bottomToX = coverLeft + coverWidth + OUTLINE_GAP + sw
                rightToY = outlineRightY - (length - (w * 3 / 2 + h + 5 * OUTLINE_GAP + 3 * sw))
            }
            in (w * 3 / 2 + 2 * h + 7 * OUTLINE_GAP + 4 * sw)..(coverCircumference + 8 * OUTLINE_GAP + 4 * sw) -> {
                topToX1 = coverLeft - OUTLINE_GAP - sw
                leftToY = coverTop + coverHeight + OUTLINE_GAP + sw
                bottomToX = coverLeft + coverWidth + OUTLINE_GAP + sw
                rightToY = coverTop - OUTLINE_GAP - sw
                topToX2 = outlineTopX2 - (length - (w * 3 / 2 + 2 * h + 7 * OUTLINE_GAP + 4 * sw))
            }
        }
    }

    fun fillFrame() {
        invalidate()
        topToX2 = coverLeft + coverWidth / 2
        coverPaint!!.colorFilter = null
        progressText = ""
    }

    private var isAnimating = false
    fun eraseFrameWithAnimation() {
        isAnimating = true
        val animator = ValueAnimator.ofFloat(0F, 255F)
        animator.addUpdateListener {
            val value = animator.animatedValue as Float
            val newAlpha = 255F - value
            outlineTopPaint.alpha = newAlpha.toInt()
            outlineBottomPaint.alpha = newAlpha.toInt()
            outlineRightPaint.alpha = newAlpha.toInt()
            outlineLeftPaint.alpha = newAlpha.toInt()
            invalidate()
        }
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator.start()
    }
}