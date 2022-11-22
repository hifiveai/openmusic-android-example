package com.ss.ugc.android.editor.bottom.panel.sticker.text.holder

import android.graphics.Color
import android.view.View
import com.bytedance.android.winnow.WinnowHolder
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.bottom.R
import kotlinx.android.synthetic.main.btm_holder_text_color_item.view.*

class TextColorHolder(itemView: View) : WinnowHolder<ResourceItem>(itemView) {
    override fun onBindData(data: ResourceItem) {
        itemView.image_color.setBackgroundColor(Color.rgb(data.rgb[0],data.rgb[1],data.rgb[2]))
    }

    override fun getLayoutRes(): Int {
        return R.layout.btm_holder_text_color_item
    }
}