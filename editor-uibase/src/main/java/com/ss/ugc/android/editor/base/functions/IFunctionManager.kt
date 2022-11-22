package com.ss.ugc.android.editor.base.functions

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity

interface IFunctionManager {

    /**
     * add funcItem in cut bar in the specific index
     * genFunctionHandler will handle item click logic
     */
    fun addFuncItemToCut(funcItem: FunctionItem, index: Int, genFunctionHandler: GenFunctionHandler)


    fun replaceFuncItem(
        funcType: String,
        funcItem: FunctionItem,
        genFunctionHandler: GenFunctionHandler
    )

    /**
     * remove funcItem in cut bar at the specific index
     */
    fun removeFuncItemFromCutAt(index: Int)

    /**
     * remove funcItem in cut bar by the specific type
     */
    fun removeFuncItemFromCutByType(childType: String)

    /**
     * add a child FuncItem to the parent FuncItem by parent type
     */
    fun addFuncItemToParent(
        parentType: String,
        childItem: FunctionItem,
        index: Int,
        genFunctionHandler: GenFunctionHandler
    )

    /**
     * remove the child of the specified index from the parent of the specified type
     */
    fun removeFuncItemFromParent(parentType: String, index: Int)

    /**
     * remove the child of the specified type from the parent of the specified type
     */
    fun removeFuncItemFromParent(parentType: String, childType: String)

    /**
     * add funcItem in root bar in the specific index
     * genFunctionHandler will handle item click logic
     */
    fun addFuncItemToRoot(
        funcItem: FunctionItem,
        index: Int,
        genFunctionHandler: GenFunctionHandler
    )

    /**
     * remove funcItem in root bar at the specific index
     */
    fun removeFuncItemFromRootAt(index: Int)

    /**
     * remove funcItem in root bar by the specific type
     */
    fun removeFuncItemFromRootByType(childType: String)

    /**
     * disable the funcItem by specified type
     */
    fun disableFuncItem(funcType: String, updateAll: Boolean = true)

    /**
     * disable the funcItems by specified type array
     * @param updateAll TODO@cjx 理论上不应该全部更新，后期直接去掉看旧业务是否有影响
     */
    fun disableFuncItems(funcTypesArray: Array<String>, updateAll: Boolean = true)

    /**
     * enable the funcItem by specified type
     */
    fun enableFuncItem(funcType: String, updateAll: Boolean = true)

    /**
     * enable the funcItems by specified type array
     */
    fun enableFuncItems(funcTypesArray: Array<String>, updateAll: Boolean = true)

}

interface GenFunctionHandler {
    fun create(activity: FragmentActivity, @IdRes containerViewId: Int): IFunctionHandler?
}

interface IFunctionHandlerRegister {
    fun onRegister(genFunctionHandler: GenFunctionHandler)
}

interface IDataSetChangeListener {
    fun onDataChanged(functionItemList: ArrayList<FunctionItem>)
    fun notifyItemChange(found: FunctionItem?)
}