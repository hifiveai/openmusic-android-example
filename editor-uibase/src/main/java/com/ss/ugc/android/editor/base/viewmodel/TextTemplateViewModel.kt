package com.ss.ugc.android.editor.base.viewmodel

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bytedance.ies.nle.editor_jni.NLEResourceNode
import com.bytedance.ies.nle.editor_jni.NLESegmentTextTemplate
import com.bytedance.ies.nle.editor_jni.NLETextTemplateClip
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.bytedance.ies.nlemediajava.TextTemplate
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.resource.CategoryInfo
import com.ss.ugc.android.editor.base.resource.ResourceHelper
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.resource.ResourceListListener
import com.ss.ugc.android.editor.base.utils.postOnUiThread
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.core.Constants
import com.ss.ugc.android.editor.core.api.sticker.TextTemplateParam
import com.ss.ugc.android.editor.core.event.SelectStickerEvent
import com.ss.ugc.android.editor.core.getCurrentTextTemplate
import com.ss.ugc.android.editor.core.selectedTextTemplatePath
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory
import java.io.File

@Keep
class TextTemplateViewModel(activity: FragmentActivity) : BaseEditorViewModel(activity) {

    private val stickerUI by lazy {
        EditViewModelFactory.viewModelProvider(activity).get(StickerUIViewModel::class.java)
    }

    private val categoryInfoLiveData: MutableLiveData<List<CategoryInfo>> = MutableLiveData()
    val categoryInfoList: LiveData<List<CategoryInfo>> get() = categoryInfoLiveData

    fun fetchPanelInfo(panel: String) {
        EditorSDK.instance.config.resourceProvider?.fetchPanelInfo(panel, object :
            ResourceListListener<CategoryInfo> {
            override fun onStart() {
            }

            override fun onSuccess(dataList: List<CategoryInfo>) {
                categoryInfoLiveData.value = dataList
            }

            override fun onFailure(exception: Exception?, tips: String?) {
                categoryInfoLiveData.value = ArrayList()
            }
        })
    }

    fun tryAddOrUpdateTextTemplate(item: ResourceItem?) {
        item?.apply {
            val param = convertToParam(item)
            val nodes = convertToResourceNode(item)
            curTextTemplate()?.apply {
                updateTextTemplate(param, nodes)
                return
            }

            applyTextTemplate(param, nodes)
        }
    }

    private fun convertToParam(item: ResourceItem): TextTemplateParam {
        return TextTemplateParam(item.path, item.name, item.resourceId)
    }

    private fun convertToResourceNode(item: ResourceItem): List<NLEResourceNode> {
        if (item.dep == null) return emptyList()
        return item.dep.map { resItem ->
            NLEResourceNode().apply {
                resourceFile = if (File(resItem.path).exists()) {
                    resItem.path
                } else {
                    ResourceHelper.getInstance().getTextRootPath("text_template.bundle") + resItem.path
                }
                resItem.resourceId?.let { resId ->
                    this.resourceId = resId
                    this.setExtra(Constants.ORIGIN_RES_ID, resId)
                }
                resItem.extra?.let { extra ->
                    this.setExtra(Constants.DEP_RES_URS_ID, extra)
                }
            }
        }
    }

    fun curTextTemplate(): NLESegmentTextTemplate? {
        return nleEditorContext.getCurrentTextTemplate()
    }

    fun curTrackSlot(): NLETrackSlot? {
        return nleEditorContext.selectedNleTrackSlot
    }

    private fun updateTextTemplate(item: TextTemplateParam, nodes: List<NLEResourceNode>) {
        nleEditorContext.stickerEditor.updateTextTemplate(item, nodes)?.also {
            addDefaultContent(it)
            updateTextTemplate(it)
        }
    }

    private fun applyTextTemplate(item: TextTemplateParam, nodes: List<NLEResourceNode>) {
        nleEditorContext.stickerEditor.applyTextTemplate(item, nodes)?.also {
            addDefaultContent(it)
            updateTextTemplate(it)
        }
    }

    /**
     * notice：文字模板特殊处理-填充默认文字内容
     */
    private fun addDefaultContent(slot: NLETrackSlot) {
        NLESegmentTextTemplate.dynamicCast(slot.mainSegment)?.apply {
            getTemplateInfo(slot)?.apply {
                text_list.forEach() { item ->
                    addTextClip(NLETextTemplateClip().apply {
                        content = item.value
                        index = item.index
                    })
                }
            }
        }
    }

    fun previewTextTemplate(on: Boolean) {
        curTrackSlot()?.let {
            if (on) {
                nleEditorContext.videoPlayer.player?.startStickerAnimationPreview(it, 4)
            } else {
                nleEditorContext.videoPlayer.player?.stopStickerAnimationPreview(it)
            }
        }
    }

    fun selectTemplatePath(): String {
        return nleEditorContext.selectedTextTemplatePath()
    }

    fun updateTextTemplate(slot: NLETrackSlot?) {
        slot?.apply {
            nleEditorContext.commit()
            nleEditorContext.videoPlayer.player?.startStickerAnimationPreview(this, 4)
            // 这里延迟执行下，好让文字模版边框能获取到完整显示时的值
            postOnUiThread(10) {
                sendSelectEvent(this)
            }
        }
    }

    fun updateTextTemplate() {
        nleEditorContext.stickerEditor.updateTextTemplate()?.apply {
            nleEditorContext.commit()
            sendSelectEvent(this)
        }
    }

    private fun sendSelectEvent(slot: NLETrackSlot? = null) {
        stickerUI.selectStickerEvent.value = if (slot == null) {
            null
        } else {
            SelectStickerEvent(slot)
        }
    }

    fun getTextContent(index: Int): String {
        nleEditorContext.videoPlayer.player!!.getTextTemplateInfo(curTrackSlot())?.let {
            return it.textContent(index)
        }
        return ""
    }

    fun getTemplateInfo(): TextTemplate? {
        return getTemplateInfo(curTrackSlot())
    }

    private fun getTemplateInfo(slot: NLETrackSlot?): TextTemplate? {
        return nleEditorContext.videoPlayer.player!!.getTextTemplateInfo(slot)
    }
}