package com.ss.ugc.android.editor.main.template

import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bytedance.android.winnow.WinnowHolder
import com.ss.ugc.android.editor.base.draft.TemplateDraftItem
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.main.R
import kotlinx.android.synthetic.main.item_template_draft.view.*

class TemplateDraftHolder(itemView: View): WinnowHolder<TemplateDraftItem>(itemView) {

    override fun getLayoutRes(): Int = R.layout.item_template_draft

    private fun convertDpToInt(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    override fun onBindData(data: TemplateDraftItem) {
        itemView.apply {
            draft_image.layoutParams = if (data.draftRatio > 1F) ConstraintLayout.LayoutParams(convertDpToInt(164F), convertDpToInt(93F)) else
                ConstraintLayout.LayoutParams(convertDpToInt(164F), convertDpToInt(349F))
            draft_image_mask.layoutParams = ConstraintLayout.LayoutParams(draft_image.layoutParams.width, draft_image.layoutParams.height)
            ImageLoader.loadBitmap(itemView.context, data.cover, draft_image, ImageOption.Builder().build())
            template_draft_name_text.text = if (data.name.isNullOrEmpty()) "草稿名字" else data.name
            template_draft_info_text.text = "${FileUtil.stringForTime(data.duration.toInt())}  |  ${data.slots}个片段"
            draft_more_settings_button.setImageResource(R.drawable.ic_template_setting_more)
        }
    }

}