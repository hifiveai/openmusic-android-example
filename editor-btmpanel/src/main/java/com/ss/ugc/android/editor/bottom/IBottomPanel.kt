package com.ss.ugc.android.editor.bottom

import com.ss.ugc.android.editor.base.functions.IFunctionManager
import com.ss.ugc.android.editor.bottom.theme.BottomPanelConfig

/**
 * @date: 2021/3/31
 * @desc:
 */
interface IBottomPanel {

    /**
     * event interface exposed to the outside
     */
    var eventListener: IEventListener?

    /**
     * use config to initialize
     */
    fun init(bottomPanelConfig: BottomPanelConfig? = null)

    /**
     * use IFunctionManager to add or remove FunctionItem
     */
    fun getFunctionManager(): IFunctionManager

    /**
     * use IFunctionNavigator to navigate to the specific FunctionItem
     */
    fun getFunctionNavigator(): IFunctionNavigator

    /**
     * show bottom function bar
     */
    fun show()

    /**
     * hide bottom function bar
     */
    fun hide()

    fun onBackPressed(): Boolean

    fun getCurrentPanelFragmentTag():String?

    fun closeOtherPanel(curTag: String?)

}
