package com.ss.ugc.android.editor.picker.album.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException

/**
 * @param columnCountLimit 限制固定列数 >0，无限制则为 <=0
 * @param rowCountLimit 限制固定行数 >0, 无限制则为 <=0
 */
class SpacesItemDecoration(
    private val verticalSpace: Int,         // 不含首尾，请使用 padding
    private val horizontalSpace: Int,       // 不含首尾，请使用 padding
    private val columnCountLimit: Int = 0,
    private val rowCountLimit: Int = 0
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        if (columnCountLimit > 0) {
            parent.adapter?.apply {

                // horizontal
                val layoutPosition = parent.getChildLayoutPosition(view)
                if (layoutPosition + 1 % columnCountLimit == 0) { // the end of row
                    outRect.left = horizontalSpace / 2
                }
                if (layoutPosition % columnCountLimit == 0) { // the first of row
                    outRect.right = horizontalSpace / 2
                }
                if (layoutPosition % columnCountLimit != 0 && layoutPosition + 1 % columnCountLimit != 0) { // the middle of row
                    outRect.left = horizontalSpace / 2
                    outRect.right = horizontalSpace / 2
                }

                val itemCountOfLastRow = (itemCount % columnCountLimit).let {
                    if (it == 0) {
                        columnCountLimit
                    } else {
                        it
                    }
                }

                // vertical
                val adapterPosition = parent.getChildAdapterPosition(view)
                if (adapterPosition < columnCountLimit) { // the first row
                    outRect.bottom = verticalSpace / 2
                }
                if (adapterPosition >= itemCount - itemCountOfLastRow) { // the last row
                    outRect.top = verticalSpace / 2
                }
                if (adapterPosition >= columnCountLimit && adapterPosition < itemCount - itemCountOfLastRow) { // the middle row
                    outRect.top = verticalSpace / 2
                    outRect.bottom = verticalSpace / 2
                }
            }
        } else if (rowCountLimit > 0) {
            parent.adapter?.apply {
                val layoutPosition = parent.getChildLayoutPosition(view)
                if (layoutPosition + 1 % rowCountLimit == 0) { // the end of column
                    outRect.top = verticalSpace / 2
                }
                if (layoutPosition % rowCountLimit == 0) { // the first of column
                    outRect.bottom = verticalSpace / 2
                }
                if (layoutPosition + 1 % rowCountLimit != 0 && layoutPosition % rowCountLimit != 0) {
                    outRect.top = verticalSpace / 2
                    outRect.bottom = verticalSpace / 2
                }

                val itemCountOfLastColumn = (itemCount % rowCountLimit).let {
                    if (it == 0) {
                        rowCountLimit
                    } else {
                        it
                    }
                }
                val adapterPosition = parent.getChildAdapterPosition(view)
                if (adapterPosition < columnCountLimit) { // the first column
                    outRect.right = horizontalSpace / 2
                }
                if (adapterPosition >= itemCount - itemCountOfLastColumn) { // the last column
                    outRect.left = horizontalSpace / 2
                }
                if (adapterPosition >= columnCountLimit && adapterPosition < itemCount - itemCountOfLastColumn) { // the middle column
                    outRect.left = horizontalSpace / 2
                    outRect.right = horizontalSpace / 2
                }
            }
        } else {
            throw RuntimeException("Neither rowCountLimit nor horizontalSpace is larger than 0")
        }
    }
}