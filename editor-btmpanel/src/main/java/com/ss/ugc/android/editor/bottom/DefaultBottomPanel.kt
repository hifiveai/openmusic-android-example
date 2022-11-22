package com.ss.ugc.android.editor.bottom

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.fragment.FragmentHelper
import com.ss.ugc.android.editor.base.functions.FunctionItem
import com.ss.ugc.android.editor.base.functions.GenFunctionHandler
import com.ss.ugc.android.editor.base.functions.IDataSetChangeListener
import com.ss.ugc.android.editor.base.functions.IFunctionHandlerRegister
import com.ss.ugc.android.editor.base.functions.IFunctionManager
import com.ss.ugc.android.editor.base.monitior.ReportConstants
import com.ss.ugc.android.editor.base.monitior.ReportUtils
import com.ss.ugc.android.editor.base.viewmodel.adapter.StickerUIViewModel
import com.ss.ugc.android.editor.base.functions.ShowPanelFragmentEvent
import com.ss.ugc.android.editor.bottom.event.*
import com.ss.ugc.android.editor.bottom.function.FunctionBarFragment
import com.ss.ugc.android.editor.bottom.function.FunctionDataHelper
import com.ss.ugc.android.editor.bottom.function.FunctionItemTreeHelper
import com.ss.ugc.android.editor.bottom.function.FunctionManagerImpl
import com.ss.ugc.android.editor.bottom.function.FunctionNavigatorImpl
import com.ss.ugc.android.editor.bottom.function.ROOT_ITEM_TYPE
import com.ss.ugc.android.editor.bottom.handler.FunctionDispatchHandler
import com.ss.ugc.android.editor.bottom.handler.impl.*
import com.ss.ugc.android.editor.bottom.panel.audiofilter.AudioFilterHandler
import com.ss.ugc.android.editor.bottom.handler.impl.AddPipHandler
import com.ss.ugc.android.editor.bottom.handler.impl.AdjustHandler
import com.ss.ugc.android.editor.bottom.handler.impl.AnimationHandler
import com.ss.ugc.android.editor.bottom.handler.impl.AudioHandler
import com.ss.ugc.android.editor.bottom.handler.impl.AudioRecordHandler
import com.ss.ugc.android.editor.bottom.handler.impl.RatioHandler
import com.ss.ugc.android.editor.bottom.handler.impl.FilterHandler
import com.ss.ugc.android.editor.bottom.handler.impl.FunctionCutHandler
import com.ss.ugc.android.editor.bottom.handler.impl.ImageStickerHandler
import com.ss.ugc.android.editor.bottom.handler.impl.SpeedNormalHandler
import com.ss.ugc.android.editor.bottom.handler.impl.TextSelectedHandler
import com.ss.ugc.android.editor.bottom.handler.impl.TextStickerHandler
import com.ss.ugc.android.editor.bottom.handler.impl.TransactionHandler
import com.ss.ugc.android.editor.bottom.handler.impl.VideoCropHandler
import com.ss.ugc.android.editor.bottom.handler.impl.VideoEffectHandler
import com.ss.ugc.android.editor.bottom.handler.impl.VideoMaskHandler
import com.ss.ugc.android.editor.bottom.handler.impl.VolumeHandler
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment
import com.ss.ugc.android.editor.bottom.panel.sticker.text.TextStickerFragment.Companion.COVER_MODE
import com.ss.ugc.android.editor.bottom.theme.BottomPanelConfig
import com.ss.ugc.android.editor.core.NLEEditorContext
import com.ss.ugc.android.editor.core.isPipTrack
import com.ss.ugc.android.editor.core.vm.EditViewModelFactory.Companion.viewModelProvider

/**
 * @date: 2021/3/31
 */
class DefaultBottomPanel(
    private val hostActivity: FragmentActivity,
    @IdRes private val funcBarContainerViewId: Int,
    @IdRes private val optPanelContainerViewId: Int
) : IBottomPanel {

    override var eventListener: IEventListener? = null
    private var functionBarFragment: FunctionBarFragment = FunctionBarFragment.newInstance()
    private var functionManager: IFunctionManager? = null
    private var functionNavigator: IFunctionNavigator? = null
    private var functionItemTreeHelper: FunctionItemTreeHelper? = null
    private var hasInitialized = false
    private var currentPanelFragmentTag :String? = null

    private val nleEditorContext by lazy {
        viewModelProvider(hostActivity).get(NLEEditorContext::class.java)
    }

    private val functionDispatchHandler = FunctionDispatchHandler()
        .apply {
            addHandler(AudioHandler(hostActivity, optPanelContainerViewId))
            addHandler(AnimationHandler(hostActivity, optPanelContainerViewId))
            addHandler(SpeedNormalHandler(hostActivity, optPanelContainerViewId))
            addHandler(CurveSpeedHandler(hostActivity, optPanelContainerViewId))
            addHandler(VolumeHandler(hostActivity, optPanelContainerViewId))
            addHandler(FilterHandler(hostActivity, optPanelContainerViewId))
            addHandler(AdjustHandler(hostActivity, optPanelContainerViewId))
            addHandler(ImageStickerHandler(hostActivity, optPanelContainerViewId))
            addHandler(TextStickerHandler(hostActivity, optPanelContainerViewId))
            addHandler(RatioHandler(hostActivity, optPanelContainerViewId))
            addHandler(AddPipHandler(hostActivity, optPanelContainerViewId))
            addHandler(VideoMaskHandler(hostActivity, optPanelContainerViewId))
            addHandler(VideoCropHandler(hostActivity, optPanelContainerViewId))
            addHandler(VideoEffectHandler(hostActivity, optPanelContainerViewId))
            addHandler(TextSelectedHandler(hostActivity, optPanelContainerViewId))
            addHandler(TransactionHandler(hostActivity, optPanelContainerViewId))
//            addHandler(com.ss.ugc.android.editor.advanced.recognize.RecognizeHandler(hostActivity, optPanelContainerViewId))
//            addHandler(com.ss.ugc.android.editor.advanced.recognize.ToneHandler(hostActivity, optPanelContainerViewId))
            addHandler(AudioFilterHandler(hostActivity, optPanelContainerViewId))
            addHandler(AudioRecordHandler(hostActivity,optPanelContainerViewId))
            addHandler(TextTemplateSelectedHandler(hostActivity, optPanelContainerViewId))
            addHandler(AudioRecordHandler(hostActivity,optPanelContainerViewId))
            addHandler(StickerSelectedHandler(hostActivity, optPanelContainerViewId))
            addHandler(AudioRecordHandler(hostActivity, optPanelContainerViewId))
            addHandler(CanvasHandler(hostActivity, optPanelContainerViewId))
        }

    private val functionHandlerRegister = object : IFunctionHandlerRegister {
        override fun onRegister(genFunctionHandler: GenFunctionHandler) {
            genFunctionHandler.create(hostActivity, optPanelContainerViewId)?.apply {
                functionDispatchHandler.addHandler(this)
            }
        }
    }

    private val dataSetChangeListener = object : IDataSetChangeListener {
        override fun onDataChanged(functionItemList: ArrayList<FunctionItem>) {
            functionBarFragment.updateFunctionList(functionItemList)
        }

        override fun notifyItemChange(found: FunctionItem?) {
            found?.let {
                functionBarFragment.notifyItemChange(it)
            }
        }
    }

    override fun init(bottomPanelConfig: BottomPanelConfig?) {
        if (hasInitialized) {
            throw IllegalStateException("Duplicate initialization.")
        }
//        val customizeFunctionItemList = EditorSDK.instance.functionBarConfig().createFunctionItemList()
//        if (!customizeFunctionItemList.isNullOrEmpty()) {
//            FunctionDataHelper.setFunctionItemList(customizeFunctionItemList)
//        }
        functionItemTreeHelper = FunctionItemTreeHelper(FunctionDataHelper.getFunctionItemList())
        functionBarFragment.functionItemTreeHelper = functionItemTreeHelper;
        functionManager = FunctionManagerImpl(functionItemTreeHelper!!, functionHandlerRegister, dataSetChangeListener)
        functionNavigator = FunctionNavigatorImpl(functionItemTreeHelper!!, functionHandlerRegister, functionBarFragment, {
            functionDispatchHandler.onItemClicked(it)
        }, {
            if (currentPanelFragmentTag != null) {
                val panelFragment = hostActivity.supportFragmentManager.findFragmentByTag(currentPanelFragmentTag)
                if (panelFragment != null) {
                    FragmentHelper().closeFragment(panelFragment)
                }
            }
        })

        functionDispatchHandler.addHandler(
            FunctionCutHandler(hostActivity, optPanelContainerViewId, functionManager!!)
        )
        handleEvent()
        hasInitialized = true
    }

    private fun handleEvent() {
        viewModelProvider(hostActivity).get(FuncItemClickEvent::class.java).clickedLeafItem.observe(hostActivity, Observer {
            it?.apply {
                functionDispatchHandler.onItemClicked(this)
            }
        })

        viewModelProvider(hostActivity).get(FuncItemClickEvent::class.java).clickedItem.observe(hostActivity, Observer {
            it?.also {
                eventListener?.onFuncItemClicked(it)
                handleMonitorEvent(it)
                if (it.hasChildren()) {
                    eventListener?.onShowChildren(it)
                }
            }
        })

        viewModelProvider(hostActivity).get(HideChildrenEvent::class.java).hideChildrenEvent.observe(hostActivity, Observer {
            it?.apply {
                eventListener?.onHideChildren(this)
            }
        })

        viewModelProvider(hostActivity).get(EditModeEvent::class.java).editModeChangeEvent.observe(
            hostActivity,
            Observer {
                it?.apply {
                    eventListener?.onEditModeChanged(this)
                }
            })

        viewModelProvider(hostActivity).get(CanvasModeEvent::class.java).canvasModeChangeEvent.observe(
            hostActivity,
            Observer {
                it?.apply {
                    eventListener?.onCanvasModeChanged(this)
                }
            })

        viewModelProvider(hostActivity).get(CheckRootStateEvent::class.java).checkStateEvent.observe(
            hostActivity,
            Observer {
                it?.apply {
                    if (this) {
                        eventListener?.onBackToRootState()
                    }
                }
            })

        viewModelProvider(hostActivity).get(BackClickEvent::class.java).backClickedEvent.observe(
            hostActivity,
            Observer {
            if (it == null) {
                functionNavigator?.backToRoot()
            } else {
                val parent = functionItemTreeHelper?.findParent(it)
                if (parent == null) {
                    functionNavigator?.backToRoot()
                } else {
                    if (parent.type == ROOT_ITEM_TYPE) {
                        functionNavigator?.backToRoot()
                    } else {
                        functionNavigator?.expandFuncItemByType(parent.type)
                    }
                }
            }
        })

        viewModelProvider(hostActivity).get(StickerUIViewModel::class.java).showTextPanelEvent.observe(hostActivity, Observer {
            it?.let {
                if (it.isFromCover) {
                    val textStickerFragment = TextStickerFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean(COVER_MODE, true)
                        }
                    }
                    FragmentHelper(optPanelContainerViewId).bind(hostActivity)
                        .startFragment(textStickerFragment)
                } else {
                    functionNavigator?.showTextPanel()
                }
            }
        })

        viewModelProvider(hostActivity).get(StickerUIViewModel::class.java).textTemplatePanelTab.observe(hostActivity
        ) {
            if (it != null) {
                functionNavigator?.showTextTemplatePanel(it.edit, it?.index)
            }
        }

        viewModelProvider(hostActivity).get(StickerUIViewModel::class.java).showStickerAnimPanelEvent.observe(hostActivity, Observer {
            functionNavigator?.showStickerPanel()
        })
        viewModelProvider(hostActivity).get(ShowPanelFragmentEvent::class.java).showPanelFragmentEvent.observe(hostActivity,Observer{
            currentPanelFragmentTag = it
        })

    }

    private fun handleMonitorEvent(item: FunctionItem) {
        val eventParamsMap = HashMap<String, String>()
        eventParamsMap["action"] = EditorSDK.instance.functionTypeMapper()?.convert(item.type) ?: item.type
        if (nleEditorContext.isPipTrack()) {
            eventParamsMap["type"] = "pip"
        } else {
            eventParamsMap["type"] = "main"
        }
        val eventKey = if (functionItemTreeHelper?.isRootItem(item) == true) {
            ReportConstants.ROOT_FUNCTION_ITEM_CLICKED_EVENT
        } else {
            ReportConstants.CUT_FUNCTION_ITEM_CLICKED_EVENT
        }
        ReportUtils.doReport(eventKey, eventParamsMap)
    }

    override fun getFunctionManager(): IFunctionManager {
        if (!hasInitialized) {
            throw IllegalStateException("You haven not init BottomPanel yet.")
        }
        return functionManager!!
    }

    override fun getFunctionNavigator(): IFunctionNavigator {
        if (!hasInitialized) {
            throw IllegalStateException("You haven not init BottomPanel yet.")
        }
        return functionNavigator!!
    }

    override fun show() {
        if (!hasInitialized) {
            throw IllegalStateException("You haven not init BottomPanel yet.")
        } else {
            val transaction = hostActivity.supportFragmentManager.beginTransaction()
            if (functionBarFragment.isAdded && functionBarFragment.isHidden) {
                transaction.show(functionBarFragment)
            } else {
                transaction.add(funcBarContainerViewId, functionBarFragment)
            }
            transaction.commit()
        }
    }

    override fun hide() {
        if (!hasInitialized) {
            throw IllegalStateException("You haven not init BottomPanel yet.")
        } else {
            if (functionBarFragment.isAdded && !functionBarFragment.isHidden) {
                hostActivity.supportFragmentManager.beginTransaction().hide(functionBarFragment).commit()
            }
        }
    }

    override fun getCurrentPanelFragmentTag(): String? {
        return currentPanelFragmentTag
    }

    override fun closeOtherPanel(curTag: String?) {
        if (currentPanelFragmentTag != curTag) {
            if (currentPanelFragmentTag != null) {
                val panelFragment =
                    hostActivity.supportFragmentManager.findFragmentByTag(currentPanelFragmentTag)
                if (panelFragment != null) {
                    FragmentHelper().closeFragment(panelFragment)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (currentPanelFragmentTag != null) {
            val panelFragment = hostActivity.supportFragmentManager.findFragmentByTag(currentPanelFragmentTag)
            if (panelFragment != null) {
                FragmentHelper().closeFragment(panelFragment)
                return true
            }
        }
        functionItemTreeHelper ?: return false
        val parentItem = functionBarFragment.getParentItem()
        functionDispatchHandler.onBack()

        parentItem ?: return false
        val parent = functionItemTreeHelper!!.findParent(parentItem)
        if (parent == null) {
            functionNavigator?.backToRoot()
        } else {
            if (parent.type == ROOT_ITEM_TYPE) {
                functionNavigator?.backToRoot()
            } else {
                functionNavigator?.expandFuncItemByType(parent.type)
            }
        }
        return true
    }

}