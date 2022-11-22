package com.ss.ugc.android.editor.bottom

/**
 * @date: 2021/4/2
 */
interface IFunctionNavigator {
    /**
     * expand cut FuncItem
     */
    fun expandCutFuncItem(isSelectByMainTrack: Boolean)

    /**
     * expand audio FuncItem
     */
    fun expandAudioFuncItem()

    /**
     * expand sticker FuncItem
     */
    fun expandStickerFuncItem()

    /**
     * expand text Template FuncItem
     */
    fun expandTextTemplateSelectedPanel()

    /**
     * expand any FuncItem by specific type
     */
    fun expandFuncItemByType(type: String)

    fun expandEffectSelectedPanel()

    fun expandAdjustSelectedPanel()
    fun expandFilterSelectedPanel()

    fun expandTextSelectedPanel()

    fun showTextPanel()

    fun showAutoPanel()

    fun showStickerPanel()

    fun showTransactionPanel()

    fun showPanelByType(type: String)

    /**
     * come back to root, reset to normal state
     */
    fun backToRoot()

    fun showTextTemplatePanel(edit: Boolean = false, index: Int?)

    fun checkPanelClose()
}