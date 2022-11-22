package com.ss.ugc.android.editor.base.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.utils.UIUtils

/**
 * @date: 2021/4/5
 */
class FuncItemDecoration(private val horizontalItemSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.apply {
            left = UIUtils.dp2px(view.context, horizontalItemSpacing.toFloat() / 2)
            right = UIUtils.dp2px(view.context, horizontalItemSpacing.toFloat() / 2)
        }
    }
}