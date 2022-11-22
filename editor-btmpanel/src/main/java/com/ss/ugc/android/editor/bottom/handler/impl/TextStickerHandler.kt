package com.ss.ugc.android.editor.bottom.handler.impl

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.base.data.TextTemplateInfo
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.BaseFunctionHandler
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment
import com.ss.ugc.android.editor.bottom.panel.sticker.template.TextTemplateFragment

/**
 * @date: 2021/3/30
 */
class TextStickerHandler(activity: FragmentActivity, @IdRes containerId: Int) :
    BaseFunctionHandler(activity, containerId) {

    private val handleTypeList = mutableListOf(
        FunctionType.TYPE_FUNCTION_TEXT,
        FunctionType.TYPE_FUNCTION_TEXT_TEMPLATE,
        FunctionType.FUNCTION_TEXT_TEMPLATE_EDIT    // 编辑文本

    )

    override fun isNeedHandle(funcItem: FunctionItem): Boolean {
        return handleTypeList.contains(funcItem.type)
    }

    private var textStickerFragment: TextStickerFragment? = null
    private var textTemplateFragment: TextTemplateFragment? = null

    override fun onHandleClicked(funcItem: FunctionItem) {
        when (funcItem.type) {
            FunctionType.TYPE_FUNCTION_TEXT -> {
                closeFragment(textTemplateFragment)
                textStickerFragment = TextStickerFragment()
                showFragment(textStickerFragment!!)
            }
            FunctionType.FUNCTION_TEXT_TEMPLATE_EDIT,
            FunctionType.TYPE_FUNCTION_TEXT_TEMPLATE -> {
                closeFragment(textStickerFragment)
                textTemplateFragment = TextTemplateFragment()
                if (funcItem.type == FunctionType.FUNCTION_TEXT_TEMPLATE_EDIT) {
                    textTemplateFragment?.apply {
                        arguments = Bundle().apply {
                            putInt(TextTemplateInfo.MODE, TextTemplateInfo.MODE_EDIT)
                        }
                    }
                }
                showFragment(textTemplateFragment!!)
            }
        }
    }
}