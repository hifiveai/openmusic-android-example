package com.ss.ugc.android.editor.bottom.panel.sticker.text.holder

import android.view.View
import com.bytedance.android.winnow.WinnowHolder
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.bottom.R
import kotlinx.android.synthetic.main.btm_holder_text_style_item.view.*

/**
 * time : 2021/1/6
 *
 * description :
 *
 */
class TextStyleHolder(itemView: View) : WinnowHolder<ResourceItem>(itemView) {
    override fun onBindData(data: ResourceItem) {

        ImageLoader.loadBitmap(itemView.context, data.icon, itemView.image_style, ImageOption.Builder().build())

    }

    override fun getLayoutRes(): Int {
        return R.layout.btm_holder_text_style_item
    }
}