package com.ss.ugc.android.editor.bottom.function

import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.functions.*
import com.ss.ugc.android.editor.base.functions.FunctionType.Companion.FUNCTION_AUDIO_SELECTED
import com.ss.ugc.android.editor.base.functions.FunctionType.Companion.FUNCTION_EFFECT_SELECTED
import com.ss.ugc.android.editor.base.functions.FunctionType.Companion.FUNCTION_STICKER_SELECTED
import com.ss.ugc.android.editor.base.functions.FunctionType.Companion.FUNCTION_TEXT_SELECTED

class FunctionManagerImpl(
    private val functionItemTreeHelper: FunctionItemTreeHelper,
    private val functionHandlerRegister: IFunctionHandlerRegister,
    private val listChangeListener: IDataSetChangeListener?
) : IFunctionManager {

    private val editModeMap: HashMap<String, ArrayList<FunctionItem>> = hashMapOf()

    init {
        //origin function list data
        listChangeListener?.onDataChanged(FunctionDataHelper.getFunctionItemList())
        EditorSDK.instance.functionBarConfig()?.apply {
            expendFuncItemOnTrackSelected(FUNCTION_TEXT_SELECTED)?.let {
                editModeMap[FUNCTION_TEXT_SELECTED] = it.getChildList()
            }
            expendFuncItemOnTrackSelected(FUNCTION_STICKER_SELECTED)?.let {
                editModeMap[FUNCTION_STICKER_SELECTED] = it.getChildList()
            }
            expendFuncItemOnTrackSelected(FUNCTION_EFFECT_SELECTED)?.let {
                editModeMap[FUNCTION_EFFECT_SELECTED] = it.getChildList()
            }
            expendFuncItemOnTrackSelected(FUNCTION_AUDIO_SELECTED)?.let {
                editModeMap[FUNCTION_AUDIO_SELECTED] = it.getChildList()
            }
        }
    }

    override fun addFuncItemToCut(
        funcItem: FunctionItem,
        index: Int,
        genFunctionHandler: GenFunctionHandler
    ) {
        functionItemTreeHelper.findFunctionItemByType(FunctionType.TYPE_FUNCTION_CUT)?.apply {
            if (!containsChild(funcItem)) {
                val success = functionItemTreeHelper.addFuncItemToParent(this, funcItem, index)
                if (success) {
                    registerFunctionHandler(genFunctionHandler)
                    listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
                }
            } else {
                registerFunctionHandler(genFunctionHandler)
            }
        }
    }

    override fun replaceFuncItem(
        funcType: String,
        funcItem: FunctionItem,
        genFunctionHandler: GenFunctionHandler
    ) {
        functionItemTreeHelper.findFunctionItemByType(funcType)?.apply {
            val success = functionItemTreeHelper.replace(this, funcItem)
            if (success) {
                registerFunctionHandler(genFunctionHandler)
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        }
    }

    override fun addFuncItemToParent(
        parentType: String,
        childItem: FunctionItem,
        index: Int,
        genFunctionHandler: GenFunctionHandler
    ) {
        functionItemTreeHelper.findFunctionItemByType(parentType)?.apply {
            if (!containsChild(childItem)) {
                val success = functionItemTreeHelper.addFuncItemToParent(this, childItem, index)
                if (success) {
                    registerFunctionHandler(genFunctionHandler)
                    listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
                }
            } else {
                registerFunctionHandler(genFunctionHandler)
            }
        }
    }

    override fun removeFuncItemFromParent(parentType: String, index: Int) {
        functionItemTreeHelper.findFunctionItemByType(parentType)?.apply {
            val success = functionItemTreeHelper.removeFuncItemFromParent(this, index)
            if (success) {
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        }
    }

    override fun removeFuncItemFromParent(parentType: String, childType: String) {
        functionItemTreeHelper.findFunctionItemByType(parentType)?.apply {
            val success = functionItemTreeHelper.removeFuncItemFromParent(this, childType)
            if (success) {
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        }
    }

    override fun removeFuncItemFromCutAt(index: Int) {
        functionItemTreeHelper.findFunctionItemByType(FunctionType.TYPE_FUNCTION_CUT)?.apply {
            val success = functionItemTreeHelper.removeFuncItemFromParent(this, index)
            if (success) {
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        }
    }

    override fun removeFuncItemFromCutByType(childType: String) {
        functionItemTreeHelper.findFunctionItemByType(FunctionType.TYPE_FUNCTION_CUT)?.apply {
            val success = functionItemTreeHelper.removeFuncItemFromParent(this, childType)
            if (success) {
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        }
    }

    override fun addFuncItemToRoot(
        funcItem: FunctionItem,
        index: Int,
        genFunctionHandler: GenFunctionHandler
    ) {
        if (!functionItemTreeHelper.containsItem(funcItem)) {
            val success = functionItemTreeHelper.addFuncItemToRoot(index, funcItem)
            if (success) {
                registerFunctionHandler(genFunctionHandler)
                listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
            }
        } else {
            registerFunctionHandler(genFunctionHandler)
        }
    }

    override fun removeFuncItemFromRootAt(index: Int) {
        val success = functionItemTreeHelper.removeFuncItemFromRootAt(index)
        if (success) {
            listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
        }
    }

    override fun removeFuncItemFromRootByType(childType: String) {
        val success = functionItemTreeHelper.removeFuncItemFromRootByType(childType)
        if (success) {
            listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)
        }
    }

    override fun disableFuncItem(funcType: String, updateAll: Boolean) {
        disableFuncItems(arrayOf(funcType), updateAll)
    }

    override fun disableFuncItems(funcTypesArray: Array<String>, updateAll: Boolean) {
        funcTypesArray.forEach { targetType ->
            handleEnableOrDisable(targetType, isEnable = false, updateAll)
        }
    }

    private fun handleEnableOrDisable(
        targetType: String,
        isEnable: Boolean,
        updateAll: Boolean = true
    ) {
        var found: FunctionItem? = null
        //先从树结构查找
        functionItemTreeHelper.findFunctionItemByType(targetType)?.apply {
            enable = isEnable
            found = this
        }
        //树结构未查找则从编辑模式查找
        if (found == null) {
            editModeMap.values.forEach { group ->
                group.forEach {
                    if (targetType == it.type) {
                        it.enable = isEnable
                        found = it
                    }
                }
            }
        }
        if (updateAll) {
            val parent = functionItemTreeHelper.findParentByType(targetType)
            if (parent != null) {
                listChangeListener?.onDataChanged(parent.getChildList())
            } else {
                editModeMap.forEach { (_, value) ->
                    if (value.map { it.type }.contains(targetType)) {
                        listChangeListener?.onDataChanged(value)
                        return
                    }
                }
                //需要启动/禁用的item不存在时，回调一级菜单会有问题。逻辑不可考先注释掉
//            listChangeListener?.onDataChanged(functionItemTreeHelper.rootFunctionItemList)}
            }
        } else {
            listChangeListener?.notifyItemChange(found)
        }
    }

    override fun enableFuncItem(funcType: String, updateAll: Boolean) {
        enableFuncItems(arrayOf(funcType), updateAll)
    }

    override fun enableFuncItems(funcTypesArray: Array<String>, updateAll: Boolean) {
        funcTypesArray.forEach { targetType ->
            handleEnableOrDisable(targetType, isEnable = true, updateAll)
        }
    }

    private fun registerFunctionHandler(genFunctionHandler: GenFunctionHandler) {
        functionHandlerRegister.onRegister(genFunctionHandler)
    }
}