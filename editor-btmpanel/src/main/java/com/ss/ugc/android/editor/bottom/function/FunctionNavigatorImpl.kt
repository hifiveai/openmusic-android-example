package com.ss.ugc.android.editor.bottom.function

import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.base.functions.IFunctionHandlerRegister
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider
import com.ss.ugc.android.editor.bottom.IFunctionNavigator
import com.ss.ugc.android.editor.bottom.event.EditModeEvent

/**
 * @date: 2021/4/3
 */
class FunctionNavigatorImpl(
    private val functionItemTree: FunctionItemTreeHelper,
    private val functionHandlerRegister: IFunctionHandlerRegister,
    private val fragment: FunctionBarFragment,
    private var dispatchHandler:  ((FunctionItem) -> Unit)? = null,
    private var checkPanelFragment:  (() -> Unit)? = null
) : IFunctionNavigator {

    private val customFunctionExtension = EditorSDK.instance.functionExtension()

    override fun expandCutFuncItem(isSelectByMainTrack: Boolean) {
        functionItemTree.findFunctionItemByType(FunctionType.TYPE_FUNCTION_CUT)?.apply {
            fragment.showChildList(this)
            if (fragment.getCurrentFuncType() == FunctionType.TYPE_FUNCTION_CUT) {
                return
            }
            if (isSelectByMainTrack) {
                viewModelProvider(fragment).get(EditModeEvent::class.java).setChangeEditMode(true)
            }
        }
    }

    override fun expandStickerFuncItem() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_STICKER_SELECTED) {
            return
        }
        fragment.showChildList(FunctionDataHelper.getStickerSelectItem(), true)
    }

    override fun expandTextTemplateSelectedPanel() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED) {
            return
        }
        fragment.showChildList(FunctionDataHelper.getTextTemplateSelectItem(), true)
    }

    override fun expandFuncItemByType(type: String) {
        if (fragment.getCurrentFuncType() == type) {
            return
        }
        functionItemTree.findFunctionItemByType(type)?.apply {
            fragment.showChildList(this)
        }
    }

    override fun expandAudioFuncItem() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_AUDIO_SELECTED) {
            return
        }
        val audioSelectItem = FunctionDataHelper.getAudioSelectItem()
        customFunctionExtension?.onEditModeExtension(audioSelectItem, functionHandlerRegister)
        fragment.showChildList(audioSelectItem, true)
    }

    override fun expandEffectSelectedPanel() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_EFFECT_SELECTED) {
            return
        }
        val effectSelectItem = FunctionDataHelper.getEffectSelectItem()
        customFunctionExtension?.onEditModeExtension(effectSelectItem, functionHandlerRegister)
        fragment.showChildList(effectSelectItem, true)
    }

    override fun expandAdjustSelectedPanel() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_ADJUST_SELECTED) {
            return
        }
        val adjustSelectItem = FunctionDataHelper.getAdjustSelectItem()
        customFunctionExtension?.onEditModeExtension(adjustSelectItem, functionHandlerRegister)
        fragment.showChildList(adjustSelectItem, true)
    }

    override fun expandFilterSelectedPanel() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_FILTER_SELECTED) {
            return
        }
        val adjustSelectItem = FunctionDataHelper.getFilterSelectItem()
        customFunctionExtension?.onEditModeExtension(adjustSelectItem, functionHandlerRegister)
        fragment.showChildList(adjustSelectItem, true)
    }

    override fun expandTextSelectedPanel() {
        if (fragment.getCurrentFuncType() == FunctionType.FUNCTION_TEXT_SELECTED) {
            return
        }
        val textSelectItem = FunctionDataHelper.getTextSelectItem()
        customFunctionExtension?.onEditModeExtension(textSelectItem, functionHandlerRegister)
        fragment.showChildList(textSelectItem, true)
    }

    override fun showTextPanel() {
        functionItemTree.findFunctionItemByType(FunctionType.TYPE_FUNCTION_TEXT)?.apply {
            dispatchHandler?.invoke(this)
        }
    }

    override fun showAutoPanel() {
        functionItemTree.findFunctionItemByType(FunctionType.TYPE_FUNCTION_AUDIO)?.apply {
            dispatchHandler?.invoke(this)
        }
    }

    override fun showStickerPanel() {
        functionItemTree.findFunctionItemByType(FunctionType.TYPE_FUNCTION_IMAGE_STICKER)?.apply {
            dispatchHandler?.invoke(this)
        }
    }

    override fun showTransactionPanel() {
        dispatchHandler?.invoke(FunctionDataHelper.getTransactionItem())
    }

    override fun showPanelByType(type: String) {
        functionItemTree.findFunctionItemByType(type)?.apply {
            dispatchHandler?.invoke(this)
        }
    }

    override fun backToRoot() {
        checkPanelClose()
        fragment.showRootList(functionItemTree.rootFunctionItemList)
    }

    override fun checkPanelClose() {
        checkPanelFragment?.invoke()
    }

    override fun showTextTemplatePanel(edit: Boolean, index: Int?) {
        val type =
            if (edit) FunctionType.FUNCTION_TEXT_TEMPLATE_EDIT else FunctionType.TYPE_FUNCTION_TEXT_TEMPLATE
        (functionItemTree.findFunctionItemByType(type) ?: getDefaultItem(type)).apply {
            dispatchHandler?.invoke(this)
        }
    }

    private fun getDefaultItem(type: String) = FunctionItem.Builder().type(type).build()
}