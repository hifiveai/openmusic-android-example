package com.ss.ugc.android.editor.base.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.ss.ugc.android.editor.base.R

open class AlphaButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    open var normalAlpha: Float = 1F
        set(value) {
            if (field != value) field = checkBounds(value)
        }

    open var pressedAlpha: Float = 0.5F
        set(value) {
            if (field != value) field = checkBounds(value)
        }

    open var disableAlpha: Float = 0.4F
        set(value) {
            if (field != value) field = checkBounds(value)
        }

    private fun checkBounds(value: Float): Float =
        if (value < 0F) 0F else if (value > 1F) 1F else value

    init {
        attrs?.let {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.AlphaButton)
            normalAlpha = arr.getFloat(R.styleable.AlphaButton_normalAlpha, normalAlpha)
            pressedAlpha = arr.getFloat(R.styleable.AlphaButton_pressedAlpha, pressedAlpha)
            disableAlpha = arr.getFloat(R.styleable.AlphaButton_disableAlpha, disableAlpha)
            arr.recycle()
        }
    }

    override fun setPressed(pressed: Boolean) {
        if (isEnabled) {
            alpha = if (pressed) pressedAlpha else normalAlpha
        } else {
            alpha = disableAlpha
        }
        super.setPressed(pressed)
    }

    override fun setEnabled(enabled: Boolean) {
        alpha = if (enabled) normalAlpha else disableAlpha
        super.setEnabled(enabled)
    }
}
