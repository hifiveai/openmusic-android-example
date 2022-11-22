package com.ss.ugc.android.editor.bottom.function

import com.ss.ugc.android.editor.base.EditorConfig.IFunctionBarConfig
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.FunctionType
import com.ss.ugc.android.editor.bottom.DefaultFunctionBarConfig

/**
 * @date: 2021/4/2
 */
object FunctionDataHelper {

    private val defaultFunctionBarConfig = DefaultFunctionBarConfig()

    private val customFunctionBarConfig = EditorSDK.instance.functionBarConfig()

    private var rootFunctionItemList: ArrayList<FunctionItem> = getRealFunctionBarConfig().createFunctionItemList()

    //update root function item list
    fun setFunctionItemList(functionItemList: ArrayList<FunctionItem>){
        this.rootFunctionItemList = functionItemList
    }

    private fun getRealFunctionBarConfig(): IFunctionBarConfig {
        return customFunctionBarConfig ?: defaultFunctionBarConfig
    }

    fun getFunctionItemList(): ArrayList<FunctionItem> {
        return rootFunctionItemList
    }

    fun getTextSelectItem(): FunctionItem{
        return (customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_TEXT_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_TEXT_SELECTED)!!)
    }

    fun getStickerSelectItem(): FunctionItem{
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_STICKER_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_STICKER_SELECTED)!!
    }

    fun getEffectSelectItem(): FunctionItem{
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_EFFECT_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_EFFECT_SELECTED)!!
    }

    fun getAdjustSelectItem(): FunctionItem{
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_ADJUST_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_ADJUST_SELECTED)!!
    }

    fun getFilterSelectItem(): FunctionItem{
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_FILTER_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_FILTER_SELECTED)!!
    }

    fun getAudioSelectItem(): FunctionItem {
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_AUDIO_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_AUDIO_SELECTED)!!
    }

    fun getTextTemplateSelectItem(): FunctionItem{
        return customFunctionBarConfig?.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED)
            ?: defaultFunctionBarConfig.expendFuncItemOnTrackSelected(FunctionType.FUNCTION_TEXT_TEMPLATE_SELECTED)!!
    }

    fun getTransactionItem(): FunctionItem{
        return customFunctionBarConfig?.createTransactionItem()
            ?: defaultFunctionBarConfig.createTransactionItem()!!
    }

}
