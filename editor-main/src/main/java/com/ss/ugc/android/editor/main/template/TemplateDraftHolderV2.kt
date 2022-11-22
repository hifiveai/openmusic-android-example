package com.ss.ugc.android.editor.main.template

import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.android.winnow.WinnowHolder
import com.ss.ugc.android.editor.base.draft.TemplateDraftItem
import com.ss.ugc.android.editor.base.imageloder.ImageLoader
import com.ss.ugc.android.editor.base.imageloder.ImageOption
import com.ss.ugc.android.editor.base.utils.FileUtil
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.core.utils.DLog
import com.ss.ugc.android.editor.main.R

class TemplateDraftHolderV2(itemView: View) : WinnowHolder<TemplateDraftItem>(itemView) {

    val templateImage = findViewById<ImageView>(R.id.template_image)!!
    val templateImageMask = findViewById<ImageView>(R.id.template_image_mask)!!
    private val templateInfoText = findViewById<TextView>(R.id.template_info_text)!!
    val templateMoreSettingsButton = findViewById<ImageView>(R.id.template_more_settings_button)!!
    val templateNameText = findViewById<TextView>(R.id.template_name_text)!!
    val templateSelector = findViewById<ImageView>(R.id.template_selector)!!
    private val templateImageLayout = findViewById<View>(R.id.template_image_layout)!!

    private val itemWidth by lazy {
        val screenW = SizeUtil.getScreenWidth(itemView.context)
        val horizontalMargin = convertDpToInt(7f) * 2
        ((screenW - horizontalMargin) * 0.5f - horizontalMargin).toInt()
    }

    override fun getLayoutRes(): Int = R.layout.item_template_draft_2

    private fun convertDpToInt(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    override fun onBindData(data: TemplateDraftItem) {
        val size = resizeCoverDisplay(templateImageLayout, data)
        ImageLoader.loadBitmap(
            itemView.context,
            data.cover,
            templateImage,
            ImageOption.Builder().height(size.height).width(size.width).placeHolder(R.color.place_holder).build()
        )
        templateNameText.text = data.name.ifEmpty { "草稿名字" }
        templateInfoText.text =
            "${FileUtil.stringForTime(data.duration.toInt())}  |  ${data.slots}个片段"
        templateMoreSettingsButton.setImageResource(R.drawable.ic_template_setting_more)
    }

    private fun resizeCoverDisplay(coverView: View, templateItem: TemplateDraftItem): Size {
        val ratio = 1f / templateItem.draftRatio
        DLog.d(TAG, "aspectRatio=${templateItem.draftRatio} ratio =$ratio")

        val reqWidth = itemWidth
        val reqHeight = (itemWidth.toFloat() * ratio).toInt()
        if (coverView.width != reqWidth || coverView.height != reqHeight) {
            coverView.updateLayoutParams<ViewGroup.LayoutParams> {
                width = reqWidth
                height = reqHeight
            }
        }
        return Size(reqWidth, reqHeight)
    }

    private inline fun <reified T : ViewGroup.LayoutParams> View.updateLayoutParams(
        block: T.() -> Unit
    ) {
        val params = layoutParams as T
        block(params)
        layoutParams = params
    }

    companion object {
        const val TAG = "TemplateDraftHolderV2"
    }
}