package com.ss.ugc.android.editor.core.api.sticker

import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.NLETrackSlot

/**
 * 字体模板
 */
interface ITextTemplateEditor {
    /**
     * 应用文字模板
     */
    fun applyTextTemplate(param: TextTemplateParam, depNodes: List<NLEResourceNode>): NLETrackSlot?

    fun updateTextTemplate(param: TextTemplateParam, depNodes: List<NLEResourceNode>): NLETrackSlot?

    fun updateTextTemplate(): NLETrackSlot?
}