package com.ss.ugc.android.editor.preview.infosticker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.SizeF
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.bytedance.ies.nle.editor_jni.NLEClassType
import com.bytedance.ies.nle.editor_jni.NLESegmentTextTemplate
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.data.TextInfo
import com.ss.ugc.android.editor.base.data.TextPanelTab
import com.ss.ugc.android.editor.base.extensions.gone
import com.ss.ugc.android.editor.base.extensions.show
import com.ss.ugc.android.editor.base.extensions.visible
import com.ss.ugc.android.editor.base.theme.*
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.core.utils.clickWithTrigger
import com.ss.ugc.android.editor.preview.R
import com.ss.ugc.android.editor.preview.subvideo.VideoEditorGestureLayout

class InfoStickerGestureView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : VideoEditorGestureLayout(context, attrs, defStyleAttr) {

    lateinit var selectFrame: SelectFrameLayout
    private lateinit var deleteButton: ImageButton
    private lateinit var copyButton: ImageButton
    private lateinit var editButton: ImageButton
    private lateinit var flipButton: ImageButton
    private lateinit var rotateButton: ScaleButton
    private val ivPlaceholders: MutableMap<String, ImageView> = mutableMapOf()

    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var textPanelTab: TextPanelTab? = null

    private var infoStickerGestureListener: InfoStickerGestureListener? = null
    var stickerAdapters: InfoStickerGestureAdapter? = null
    private var adsorbState: AdsorbState = AdsorbState.NONE
    private val editViewConfig = ThemeStore.previewUIConfig.stickerEditViewConfig

    var onInfoStickerDisPlayChangeListener : OnInfoStickerDisPlayChangeListener? = null
        private set

    fun setOnInfoStickerDisPlayChangeListener(onInfoStickerDisPlayChangeListener : OnInfoStickerDisPlayChangeListener?){
        this.onInfoStickerDisPlayChangeListener = onInfoStickerDisPlayChangeListener
    }
    fun setAdsorbState(state: AdsorbState) {
        adsorbState = state
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        clipChildren = false
        selectFrame = findViewById(R.id.select_frame_layout)
        editButton = findViewById(R.id.edit)
        editButton.clickWithTrigger (400){ infoStickerGestureListener?.onEditSticker(view) }
        deleteButton = findViewById(R.id.delete)
        deleteButton.clickWithTrigger (400){
            infoStickerGestureListener?.onDeleteSticker(view)
        }
        copyButton = findViewById(R.id.copy)
        copyButton.clickWithTrigger (400){ infoStickerGestureListener?.onCopySticker(view) }
        rotateButton = findViewById(R.id.scale)
        rotateButton.setOnOptionListener(object : ScaleButton.OnOptionListener {

            override fun onScaleRotateBegin() {
            }

            override fun onScaleRotate(scale: Float, rotate: Float) {
                infoStickerGestureListener?.onScaleRotateSticker(view,scale, rotate)
            }

            override fun onScaleRotateEnd() {
                infoStickerGestureListener?.onStickerRotateEnd(view)
            }
        })

        flipButton = findViewById(R.id.flip)
        flipButton.clickWithTrigger (400){ infoStickerGestureListener?.onFlipSticker(view) }

        paint.color = ADSORBCOLOR
        paint.strokeWidth = ADSORPTION_LINE_WIDTH

        customizeConfig()
        setWillNotDraw(false)
    }

    private fun customizeConfig() {
        editViewConfig.apply {
            //编辑图标
            setEditIcon(editIconConfig)
            //翻转图标
            setFlipIcon(flipIconConfig)
            //复制图标
            setCopyIcon(copyIconConfig)
            //旋转图标
            setRotateIcon(rotateIconConfig)
            //删除图标
            setDeleteIcon(deleteIconConfig)
        }
    }

    open fun setEditIcon(config: EditIconConfig){
        if (config.enable) {
            editButton.show()
            if (config.editIconDrawableRes > 0) {
                editButton.setImageDrawable(ContextCompat.getDrawable(context, config.editIconDrawableRes))
            }
        } else {
            editButton.gone()
        }
    }

    open fun setFlipIcon(config: FlipIconConfig){
        if (config.enable) {
            flipButton.show()
            if (config.flipIconDrawableRes > 0) {
                flipButton.setImageDrawable(ContextCompat.getDrawable(context, config.flipIconDrawableRes))
            }
            if (config.position != IconPosition.NONE){
                (flipButton.layoutParams as LayoutParams).gravity = config.position.value
            }
        } else {
            flipButton.gone()
        }
    }

    open fun setCopyIcon(config: CopyIconConfig){
        if (config.enable) {
            copyButton.show()
            if (config.copyIconDrawableRes > 0) {
                copyButton.setImageDrawable(ContextCompat.getDrawable(context, config.copyIconDrawableRes))
            }
        } else {
            copyButton.gone()
        }
    }

    open fun setRotateIcon(config: RotateIconConfig){
        if (config.enable) {
            rotateButton.show()
            if (config.rotateIconDrawableRes > 0) {
                rotateButton.setImageDrawable(ContextCompat.getDrawable(context, config.rotateIconDrawableRes))
            }
        } else {
            rotateButton.gone()
        }
    }

    open fun setDeleteIcon(config: DeleteIconConfig){
        if (config.enable) {
            deleteButton.show()
            if (config.deleteIconDrawableRes > 0) {
                deleteButton.setImageDrawable(ContextCompat.getDrawable(context, config.deleteIconDrawableRes))
            }
        } else {
            deleteButton.gone()
        }
    }

    fun setInfoStickerGestureListener(listener: InfoStickerGestureListener) {
        infoStickerGestureListener = listener
        setOnGestureListener(listener)
    }

    fun setAdapter(infoStickerGestureAdapter: InfoStickerGestureAdapter){
        this.stickerAdapters = infoStickerGestureAdapter
        this.stickerAdapters?.bindView(this)
    }

    private fun touchInMenuButton(event: MotionEvent?): Boolean {
        if (event != null) {
            return isTouchPointInView(deleteButton, event.x, event.y) ||
                    isTouchPointInView(copyButton, event.x, event.y) ||
                    isTouchPointInView(rotateButton, event.x, event.y) ||
                    isTouchPointInView(editButton, event.x, event.y)
        }
        return true
    }

    private fun isTouchPointInView(view: View?, x: Float, y: Float): Boolean {
        if (view == null) {
            return false
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val left = location[0].toFloat()
        val top = location[1].toFloat()
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        return (y in top..bottom && x in left..right)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enableEdit) return false

        if (touchInMenuButton(event)) {
            return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (adsorbState == AdsorbState.NONE) return
        val w = measuredWidth.toFloat()
        val h = measuredHeight.toFloat()

        if (adsorbState == AdsorbState.VERTICAL || adsorbState == AdsorbState.ALL) {
            canvas.drawLine(w / 2, 0F, w / 2, LINE_LENGTH, paint)
            canvas.drawLine(w / 2, h, w / 2, h - LINE_LENGTH, paint)
        }

        if (adsorbState == AdsorbState.HORIZONTAL || adsorbState == AdsorbState.ALL) {
            canvas.drawLine(0F, h / 2, LINE_LENGTH, h / 2, paint)
            canvas.drawLine(w, h / 2, w - LINE_LENGTH, h / 2, paint)
        }
    }

    internal fun showFrame() {
        selectFrame.show()
    }

    internal fun showEditButton(show: Boolean) {
        if (show && !(textPanelTab != null &&
                        (textPanelTab == TextPanelTab.SEARCH ||
                                textPanelTab == TextPanelTab.EFFECTS))
        )
            editButton.show() else editButton.gone()
    }

    internal fun showCopyButton(show: Boolean) {
        if (show && !(textPanelTab != null &&
                    (textPanelTab == TextPanelTab.TEMPLATE_TEXT ||
                            textPanelTab == TextPanelTab.EFFECTS))
        ) {
            copyButton.show()
        } else {
            copyButton.gone()
        }
    }

    internal fun animateIn(segmentId: String, position: PointF, previewUrl: String?) {
        removeAllPlaceholders()
        val params = selectFrame.layoutParams as LayoutParams
        val ratio = params.height.toFloat() / params.width
        val valueAnimator =
                ValueAnimator.ofInt(params.width, (params.width * 1.1F).toInt(), params.width)
        valueAnimator.duration = 500
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Int
            params.width = value
            params.height = (params.width * ratio).toInt()
            params.leftMargin = (measuredWidth * position.x - params.width / 2F).toInt()
            params.topMargin = (measuredHeight * position.y - params.height / 2F).toInt()
            selectFrame.layoutParams = params
        }
        valueAnimator.start()
    }

    internal fun removePlaceholder(segmentId: String) {
        val ivPlaceholder = ivPlaceholders.remove(segmentId)
        if (ivPlaceholder != null) {
            removeView(ivPlaceholder)
        }
    }

    internal fun removeAllPlaceholders() {
        ivPlaceholders.forEach { (_, ivPlaceholder) ->
            removeView(ivPlaceholder)
        }
        ivPlaceholders.clear()
    }

    internal fun setFramePosition(segmentId: String, x: Float, y: Float) {
        val params = selectFrame.layoutParams as LayoutParams
        val leftMargin = (measuredWidth * x - params.width / 2F).toInt()
        params.leftMargin = leftMargin
        val topMargin = (measuredHeight * y - params.height / 2F).toInt()
        params.topMargin = topMargin
        selectFrame.layoutParams = params

        val ivPlaceholder = ivPlaceholders[segmentId]
        if (ivPlaceholder != null && ivPlaceholder.visible) {
            val placeholderParams = ivPlaceholder.layoutParams as? LayoutParams
            if (placeholderParams != null) {
                placeholderParams.leftMargin = leftMargin
                placeholderParams.topMargin = topMargin
                ivPlaceholder.layoutParams = placeholderParams
            }
        }
    }

    internal fun setTextItemRect(boxes: List<RectF>) {
        selectFrame.setTextItemRect(boxes.map {
            RectF(
                    it.left * measuredWidth,
                    it.top * measuredHeight,
                    it.right * measuredWidth,
                    it.bottom * measuredHeight
            )
        })
    }

    internal fun setFrameSize(sticker: NLETrackSlot, size: SizeF) {
        if (size.width > SIZE_TIMES && size.height > SIZE_TIMES) return

        val params = selectFrame.layoutParams as LayoutParams
        val width = (measuredWidth * size.width).toInt()
        val height = (measuredHeight * size.height).toInt()
        val prevWidth = params.width
        val prevHeight = params.height
        val textTemplate = NLESegmentTextTemplate.dynamicCast(sticker.mainSegment) != null
        val sizeScale = if (textTemplate) {
            1F
        } else {
            SIZE_OFFSET
        }
        params.width = ((width + OP_BUTTON_SIZE) * sizeScale).toInt()
        params.height = ((height + OP_BUTTON_SIZE) * sizeScale).toInt()
        params.leftMargin -= (params.width - prevWidth) / 2
        params.topMargin -= (params.height - prevHeight) / 2
        selectFrame.layoutParams = params

        val ivPlaceholder = ivPlaceholders[sticker.id.toString()]
        if (ivPlaceholder != null && ivPlaceholder.visible) {
            val placeholderParams = ivPlaceholder.layoutParams
            if (placeholderParams != null) {
                placeholderParams.width = params.width
                placeholderParams.height = params.height
                ivPlaceholder.layoutParams = placeholderParams
            }
        }
    }

    internal fun setFrameRotate(segmentId: String, rotation: Float) {
        selectFrame.rotation = rotation

        val ivPlaceholder = ivPlaceholders[segmentId]
        if (ivPlaceholder != null && ivPlaceholder.visible) {
            ivPlaceholder.rotation = rotation
        }
    }

    internal fun dismissFrame() {
        selectFrame.gone()
    }

    fun setTextPanelTab(tab: TextPanelTab?, textInfo: TextInfo?) {
        textPanelTab = tab
        if (tab == null) {
            editButton.show()
            copyButton.show()
            flipButton.gone()
        } else if (tab == TextPanelTab.SEARCH || tab == TextPanelTab.TEMPLATE_TEXT) {
            editButton.gone()
            copyButton.show()
            flipButton.gone()
            deleteButton.show()
            rotateButton.show()
        } else {
            editButton.gone()
            showCopyButton(ThemeStore.previewUIConfig.stickerEditViewConfig.editIconConfig.enable)
            if (tab == TextPanelTab.BUBBLE) {
                if (textInfo?.textBubbleInfo == null) {
                    flipButton.gone()
                } else {
                    flipButton.show()
                }
            } else {
                flipButton.gone()
            }
        }
    }

    fun setTextTemplateAction(switchingTemplate: Boolean, updateText: Boolean) {
        editButton.gone()
        if (updateText || switchingTemplate) copyButton.gone() else copyButton.show()
    }

    fun checkFlipButtonVisibility(textInfo: TextInfo?) {
        if (textInfo == null || textPanelTab != TextPanelTab.BUBBLE) {
            flipButton.gone()
            return
        }
        if (textInfo.textBubbleInfo == null) {
            flipButton.gone()
            return
        }
        if (editViewConfig.flipIconConfig.enable) {
            flipButton.show()
            if (editViewConfig.flipIconConfig.flipIconDrawableRes != 0) {
                flipButton.setImageResource(editViewConfig.flipIconConfig.flipIconDrawableRes)
            } else {
                flipButton.setImageResource(
                    if (textInfo.shapeFlipX != textInfo.shapeFlipY) {
                        R.drawable.bg_flip_bubble_vertical
                    } else {
                        R.drawable.bg_flip_bubble_hori
                    }
                )
            }
        } else {
            flipButton.gone()
        }
    }

    /**
     *  设置矩形框边框颜色
     */
    fun updateAdsorbColor(@ColorInt rectColor: Int) {
        if (rectColor == 0) return
        ADSORBCOLOR = rectColor
    }

    fun updateAdsorptionLineWidth(width : Float){
        ADSORPTION_LINE_WIDTH = SizeUtil.dp2px(width).toFloat()
    }

    fun updateAdsorptionLineLength(length : Float){
        LINE_LENGTH = SizeUtil.dp2px(length).toFloat()
    }


    companion object {
        private const val SIZE_TIMES = 500
        private const val SIZE_OFFSET = 1.15f
        private val PLACEHOLDER_PADDING = SizeUtil.dp2px(18F)
        private val OP_BUTTON_SIZE = PLACEHOLDER_PADDING * 2
        @JvmStatic
        var ADSORBCOLOR = Color.parseColor("#00E5F6")
        @JvmStatic
        var LINE_LENGTH = SizeUtil.dp2px(40F).toFloat()
        @JvmStatic
        var ADSORPTION_LINE_WIDTH = SizeUtil.dp2px(1F).toFloat()
    }

    fun onClear(){
        infoStickerGestureListener = null
        onInfoStickerDisPlayChangeListener = null
        dismissFrame()
        removeAllPlaceholders()
        stickerAdapters?.onStop()
        stickerAdapters = null

    }

    enum class AdsorbState {
        NONE, VERTICAL, HORIZONTAL, ALL
    }
}
