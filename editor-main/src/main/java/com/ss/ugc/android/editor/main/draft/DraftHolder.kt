package com.ss.ugc.android.editor.main.draft

import android.view.View
import com.bytedance.android.winnow.WinnowHolder
import com.ss.ugc.android.editor.base.draft.DraftItem
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.utils.DataFormatUtil
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.main.R
import kotlinx.android.synthetic.main.item_draft.view.*

/**
 * time : 2021/2/8
 * author : tanxiao
 * description :
 *
 */
class DraftHolder(itemView: View) : WinnowHolder<DraftItem>(itemView) {
    override fun onBindData(data: DraftItem) {
//        Glide.with(itemView.context)
//                .load(data.icon)
//                .into(itemView.icon)

        ImageLoader.loadBitmap(itemView.context, data.icon, itemView.icon, ImageOption.Builder().build())
        itemView.duration.text = FileUtil.stringForTime(data.duration.toInt())
        itemView.time.text = DataFormatUtil.formatTime(data.updateTime, "yyyy-MM-dd")
    }

    override fun getLayoutRes(): Int = R.layout.item_draft
}