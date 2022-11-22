package com.ss.ugc.android.editor.base.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.utils.UIUtils


class ItemOffsetDecoration(
    private val itemOffset: Rect,
    private val marginTop: Int? = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position: Int = parent.getChildAdapterPosition(view)
        val left: Int = itemOffset.left
        val top: Int = itemOffset.top
        val right: Int = itemOffset.right
        val bottom: Int = itemOffset.bottom

        val realTop = if (position == 0 && marginTop != null) {
            top + marginTop
        }else{
            top
        }
        outRect.set(
            UIUtils.dp2px(view.context, left.toFloat()),
            UIUtils.dp2px(view.context, realTop.toFloat()),
            UIUtils.dp2px(view.context, right.toFloat()),
            UIUtils.dp2px(view.context, bottom.toFloat())
        )
    }
}