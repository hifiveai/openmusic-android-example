package com.ss.ugc.android.editor.preview.adjust.view

import android.content.Context
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.ss.ugc.android.editor.preview.R

class SelectChildLinearLayout : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

        val selectedColor = ContextCompat.getColor(context,R.color.style_theme)
        val nonSelectColor =
            ContextCompat.getColor(context,R.color.transparent_80p_white)

        val color = if (selected) selectedColor else nonSelectColor

        for (i in 0..childCount) {
            val child = getChildAt(i)
            child?.isSelected = selected

            if (child is ImageView) {
                child.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }
}
