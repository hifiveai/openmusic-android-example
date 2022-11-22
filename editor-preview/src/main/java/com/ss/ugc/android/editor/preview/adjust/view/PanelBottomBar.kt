package com.ss.ugc.android.editor.preview.adjust.view

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.preview.R

private val COMPLETE_BTN_SIZE = SizeUtil.dp2px(44F)

@Suppress("DEPRECATION")
class PanelBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val title = TextView(context)
    private val complete = AlphaButton(context)

    private val defaultBgColor = Color.parseColor("#202020")

    init {
        isClickable = true
        isFocusable = true

        title.setTextColor(ContextCompat.getColor(context,R.color.transparent_80p_white))
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14F)
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.startToStart = LayoutParams.PARENT_ID
        params.endToEnd = LayoutParams.PARENT_ID
        params.topToTop = LayoutParams.PARENT_ID
        params.bottomToBottom = LayoutParams.PARENT_ID
        addView(title, params)

        val imageParams = LayoutParams(COMPLETE_BTN_SIZE, COMPLETE_BTN_SIZE)
        imageParams.endToEnd = LayoutParams.PARENT_ID
        imageParams.topToTop = LayoutParams.PARENT_ID
        imageParams.bottomToBottom = LayoutParams.PARENT_ID
        imageParams.marginEnd = SizeUtil.dp2px(8F)

        val dividerParams = LayoutParams(LayoutParams.MATCH_PARENT, SizeUtil.dp2px(0.5F))
        dividerParams.topToTop = LayoutParams.PARENT_ID

        complete.setImageResource(R.drawable.ic_confirm_n)
        complete.scaleType = ImageView.ScaleType.FIT_CENTER
        complete.contentDescription = "panel_bottom_bar_complete"
        addView(complete, imageParams)

        attrs?.let { attributeSet ->
            val arr = context.obtainStyledAttributes(attributeSet, R.styleable.PanelBottomBar)
            arr.getString(R.styleable.PanelBottomBar_android_text)?.let { title.text = it }
            arr.getColor(R.styleable.PanelBottomBar_android_background, defaultBgColor).let {
                setBackgroundColor(it)
            }
            arr.recycle()
        } ?: setBackgroundColor(defaultBgColor)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        complete.setOnClickListener(l)
    }

    fun setText(text: String) {
        title.text = text
    }
}
