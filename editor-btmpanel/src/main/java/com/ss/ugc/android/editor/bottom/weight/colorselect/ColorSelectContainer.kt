package com.ss.ugc.android.editor.bottom.weight.colorselect

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.LinearLayout
import com.ss.ugc.android.editor.base.extensions.dp
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.bottom.R

class ColorSelectContainer : AutoCenterScrollView {

    private var colorList: List<ResourceItem>? = null
    private lateinit var linearLayoutContainer: LinearLayout

    private var colorItemViewSpacing = 20.dp()
    private var colorItemViewLeftMargin = colorItemViewSpacing / 2
    private var colorItemViewRightMargin = colorItemViewSpacing / 2
    private val colorItemViewWidth = 20.dp()
    private val colorItemViewHeight = 20.dp()

    private var colorSelectedListener: OnColorSelectedListener? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        linearLayoutContainer = LinearLayout(context)
        linearLayoutContainer.gravity = Gravity.CENTER_VERTICAL
        linearLayoutContainer.setPadding(6.dp(), 0, 0, 0)
        val containerLayoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        addView(linearLayoutContainer, containerLayoutParams)
    }

    fun setColorList(colorList: List<ResourceItem>, withNoneAtFirst: Boolean = false) {
        this.colorList = colorList
        linearLayoutContainer.removeAllViews()
        if (withNoneAtFirst) {
            val noneItemView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(colorItemViewWidth, colorItemViewHeight)
            layoutParams.leftMargin = colorItemViewLeftMargin
            layoutParams.rightMargin = colorItemViewRightMargin
            noneItemView.setImageResource(R.drawable.ic_item_filter_no)
            linearLayoutContainer.addView(noneItemView, layoutParams)
            noneItemView.setOnClickListener {
                colorSelectedListener?.onColorSelected(null)
                smoothScrollToFirst()
            }
        }

        this.colorList?.forEach { colorItem ->
            val colorItemView = ColorCircleView(context = context)
            val layoutParams = LinearLayout.LayoutParams(colorItemViewWidth, colorItemViewHeight)
            layoutParams.leftMargin = colorItemViewLeftMargin
            layoutParams.rightMargin = colorItemViewRightMargin
            colorItemView.setColor(Color.rgb(colorItem.rgb[0], colorItem.rgb[1], colorItem.rgb[2]))
            linearLayoutContainer.addView(colorItemView, layoutParams)

            colorItemView.setOnClickListener {
                colorSelectedListener?.onColorSelected(colorItem)
                smoothScrollToCenter(it)
            }
        }
        requestLayout()
    }

    fun setColorSelectedListener(colorSelectedListener: OnColorSelectedListener) {
        this.colorSelectedListener = colorSelectedListener
    }

    fun smoothScrollToFirst() {
        if (linearLayoutContainer.childCount > 0) {
            scrollToLeft(linearLayoutContainer.getChildAt(0))
        }
    }
}

interface OnColorSelectedListener {
    fun onColorSelected(colorItem: ResourceItem?)
}