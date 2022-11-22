package com.ss.ugc.android.editor.base.resourceview

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.airbnb.lottie.LottieCompositionFactory
import com.ss.ugc.android.editor.base.EditorSDK
import com.ss.ugc.android.editor.base.R
import com.ss.ugc.android.editor.base.monitior.RecyclerEventHelper
import com.ss.ugc.android.editor.base.resource.*
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resource.base.IResourceProvider
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.utils.FontUtils
import com.ss.ugc.android.editor.base.utils.SizeUtil
import com.ss.ugc.android.editor.base.view.FuncItemDecoration
import com.ss.ugc.android.editor.base.view.ItemSpaceDecoration
import com.ss.ugc.android.editor.core.utils.Toaster
import kotlinx.android.synthetic.main.btm_common_resource_list_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class ResourceListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), CoroutineScope {

    private var recyclerView: RecyclerView? = null
    private val editorConfig = EditorSDK.instance.config
    private val resourceProvider: IResourceProvider? = editorConfig.resourceProvider
    private val recyclerEventHelper = RecyclerEventHelper()
    private var resourceViewConfig: ResourceViewConfig? = null
    private var resourceListAdapter: ResourceListAdapter? = null
    private var resourceListInitListListener: ResourceListInitListener? = null
    private var currentSelectId: String? = null
    private val visibleListener: ((SparseArray<View>) -> Unit)? = {
        for (i in 0 until it.size()) {
            val position: Int = it.keyAt(i)
            resourceListAdapter?.resourceModelList?.get(position)?.let { model ->
                resourceViewConfig!!.resourceListReporter?.resourceItemShow(
                    model.resourceItem,
                    position
                )
            }
        }
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    fun init(config: ResourceViewConfig) {
        resourceViewConfig = config
        currentSelectId = null
        LayoutInflater.from(context).inflate(R.layout.btm_common_resource_list_view, this, true)
        recyclerView = findViewById(R.id.resourceRecyclerList)
        recyclerView?.apply {
            layoutManager = config.layoutManager ?: LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

            val spaceDecoration = if (layoutManager is GridLayoutManager) {
                //宫格类型的，默认动态均分上下左右间距
                this.clipChildren = false//下载角标超过item，需要让其不被裁剪
                val selectorSize = config.selectorConfig.selectorWidth
                val screenWidth = SizeUtil.getScreenWidth(context)
                val spanCount = (layoutManager as GridLayoutManager).spanCount
                val padding = (screenWidth - spanCount * selectorSize) * 1f / (spanCount + 1) / 2
                ItemSpaceDecoration(spanCount, padding.toInt(), true)
            } else {
                FuncItemDecoration(10)
            }
            addItemDecoration(config.itemDecoration ?: spaceDecoration)
            resourceListAdapter =
                ResourceListAdapter(resourceViewConfig!!, resourceProvider?.isUseBuildInResource())
            adapter = resourceListAdapter
            recyclerEventHelper.recyclerView = this
            recyclerEventHelper.onVisibleCallback = visibleListener
            fetchResourceList()
        }
    }

    fun scrollToPosition(position: Int) {
        RecyclerViewSmoothScrollUtil.smoothScrollToHighlightPosition(recyclerView?.layoutManager,recyclerView,position)
    }

    fun setResourceListInitListener(initListener: ResourceListInitListener) {
        this.resourceListInitListListener = initListener
    }

    fun setOnItemClickListener(listener: ResourceItemClickListener) {
        resourceListAdapter?.setOnItemClickListener(object : ResourceModelClickListener {
            override fun onItemClick(item: ResourceModel?, position: Int) {
                item?.let { resourceModel ->
                    resourceViewConfig!!.resourceListReporter?.resourceItemClick(
                        item.resourceItem,
                        position
                    )
                    currentSelectId = resourceModel.resourceItem.resourceId
                    if (resourceProvider?.isResourceReady(resourceModel.resourceItem.resourceId) == true || resourceModel.resourceItem.resourceId.isEmpty()) {
                        listener.onItemClick(resourceModel.resourceItem, position,resourceModel.isSelect)
                    } else {
                        resourceModel.downloadState = DownloadState.LOADING
                        resourceListAdapter?.apply {
                            val loadingIndex = resourceModelList.indexOf(resourceModel)
                            notifyItemChanged(loadingIndex)
                        }
                        resourceProvider?.fetchResource(
                            resourceModel.resourceItem.resourceId,
                            object : ResourceDownloadListener {
                                override fun onSuccess(
                                    resourceId: String,
                                    filePath: String
                                ) {
                                    currentSelectId?.let { id ->
                                        resourceListAdapter?.resourceModelList?.find { it.resourceItem.resourceId == resourceId }
                                            ?.let { item ->
                                                item.downloadState = DownloadState.SUCCESS
                                                val index =
                                                    resourceListAdapter?.resourceModelList?.indexOf(
                                                        item
                                                    )
                                                index?.let {
                                                    resourceListAdapter?.notifyItemChanged(index)
                                                }
                                            }
                                        if (id == resourceId && resourceId.isNotBlank()) {
                                            listener.onItemClick(
                                                item.resourceItem,
                                                position,
                                                item.isSelect
                                            )
                                        }
                                    }
                                }

                                override fun onProgress(
                                    resourceId: String,
                                    progress: Int,
                                    contentLength: Long) {
                                }

                                override fun onFailure(resourceId: String, exception: Exception?) {
                                    resourceModel.downloadState = DownloadState.INIT
                                    resourceListAdapter?.apply {
                                        val loadingIndex = resourceModelList.indexOf(resourceModel)
                                        notifyItemChanged(loadingIndex)
                                    }
                                    Toaster.show(context.getString(R.string.ck_tips_res_download_failed))
                                }
                            })
                    }
                }
            }
        })
    }

    fun getRecyclerView(): RecyclerView? {
        return recyclerView
    }

    fun getResourceListAdapter(): ResourceListAdapter? {
        return resourceListAdapter
    }

    private fun fetchResourceList() {
        resourceViewConfig?.apply {
            resourceProvider ?: return@apply
            if (panelKey == DefaultResConfig.CANVAS_BLUR_PANEL) {
                val dataList = resourceProvider.getCanvasBlurList()
                hideLoadingView()
                setupList(
                    dataList.map { item -> ResourceModel(item) }
                )
                resourceListInitListListener?.onResourceListInitFinish()
                return
            }
            if (hasCategory) {
                if (categoryKey!!.isNotBlank()) {
                    resourceProvider.fetchCategoryResourceList(
                        panelKey!!,
                        categoryKey!!,
                        object : ResourceListListener<ResourceItem> {
                            override fun onSuccess(dataList: List<ResourceItem>) {
                                hideLoadingView()
                                setupList(
                                    dataList.map { item -> ResourceModel(item) }
                                )
                                resourceListInitListListener?.onResourceListInitFinish()
                            }

                            override fun onFailure(exception: Exception?, tips: String?) {
                                hideLoadingView()
                                showErrorView(tips)
                            }

                            override fun onStart() {
                                showLoadingView()
                            }
                        })
                }
            } else {
                resourceProvider.fetchResourceList(
                    panelKey!!,
                    downloadAfterFetchList,
                    object : ResourceListListener<ResourceItem> {
                        override fun onSuccess(dataList: List<ResourceItem>) {
                            hideLoadingView()
                            setupList(
                                dataList.map { item -> ResourceModel(item) }
                            )
                            resourceListInitListListener?.onResourceListInitFinish()
                        }

                        override fun onFailure(exception: Exception?, tips: String?) {
                            hideLoadingView()
                            showErrorView(tips)
                        }

                        override fun onStart() {
                            showLoadingView()
                        }
                    })
            }
        }
    }

    private fun reportShowItem(recyclerView: RecyclerView) {
        when (val layoutManager = recyclerView.layoutManager) {
            is LinearLayoutManager -> {
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                for (i in firstPosition..lastPosition) {
                    resourceListAdapter?.let {
                        resourceViewConfig!!.resourceListReporter?.resourceItemShow(
                            it.resourceModelList[i].resourceItem,
                            i
                        )
                    }
                }
            }
            is GridLayoutManager -> {
                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                for (i in firstPosition..lastPosition) {
                    resourceListAdapter?.let {
                        resourceViewConfig!!.resourceListReporter?.resourceItemShow(
                            it.resourceModelList[i].resourceItem,
                            i
                        )
                    }
                }
            }
        }
    }

    private fun showErrorView(tips: String?) {
        resourceLoadingLayout.visibility = View.GONE
        resourceListError.visibility = View.VISIBLE
        if (tips != null) {
            resourceErrorTxt.text = tips
            resourceErrorIcon.visibility = View.GONE
        } else {
            resourceErrorTxt.text = resources.getString(R.string.ck_network_error_click_retry)
            resourceErrorIcon.visibility = View.VISIBLE
            resourceListError.setOnClickListener {
                fetchResourceList()
            }
        }
        resourceRecyclerList.visibility = View.GONE
    }

    private fun showLoadingView() {
        resourceLoadingLayout.visibility = View.VISIBLE
        resourceListError.visibility = View.GONE
        resourceRecyclerList.visibility = View.GONE
        lottieLoadingView.visibility = View.VISIBLE
        ThemeStore.globalUIConfig?.lottieDataRequestLoadingJson?.also {
            val lottieTask = LottieCompositionFactory.fromAsset(context, it)
            lottieTask.addListener { result ->
                lottieLoadingView?.setComposition(result)
                lottieLoadingView?.playAnimation()
            }
        }
    }

    private fun hideLoadingView() {
        resourceLoadingLayout.visibility = View.GONE
        lottieLoadingView.visibility = View.GONE
        resourceListError.visibility = View.GONE
        resourceRecyclerList.visibility = View.VISIBLE
    }

    private fun setupList(dataList: List<ResourceModel>) {
        resourceListAdapter?.setData(dataList)
    }

    fun replaceTextResourceListIcon(iconName: String) {
        resourceListAdapter?.resourceModelList?.forEach {
            it.resourceItem.icon = it.resourceItem.extra?.let { extra ->
                if (extra.isNotBlank()) {
                    val obj = JSONObject(extra)
                    val icon = obj.optString(iconName, "")
                    if (icon.isBlank()) {
                        return@let ""
                    }
                    icon
                } else {
                    ""
                }
            }
        }
    }

    fun selectItem(currentResourcePath: String, isFromFontPanel: Boolean = false): Int {
        var itemIndex = -1
        resourceListAdapter?.apply {
            resourceModelList.forEachIndexed { index, it ->
                if (isFromFontPanel) {
                    it.isSelect =
                        FontUtils.findFontFilePath(it.resourceItem.path) == currentResourcePath
                } else {
                    it.isSelect = it.resourceItem.path == currentResourcePath
                }
                if (it.isSelect) {
                    itemIndex = index
                }
            }
            notifyDataSetChanged()
        }
        return itemIndex
    }

    fun updateCustomItemIcon(iconPath: String) {
        resourceListAdapter?.apply {
            val enableNullItem =
                resourceViewConfig?.nullItemInFirstConfig?.addNullItemInFirst ?: false
            val enableCustomItem =
                resourceViewConfig?.customItemConfig?.addCustomItemInFirst ?: false
            if (enableNullItem && enableCustomItem) {
                resourceModelList[1].resourceItem.path = iconPath
                resourceModelList[1].resourceItem.icon = iconPath
                notifyItemChanged(1)
            } else if (!enableNullItem && enableCustomItem) {
                resourceModelList[0].resourceItem.path = iconPath
                resourceModelList[0].resourceItem.icon = iconPath
                notifyItemChanged(0)
            }
        }
    }

    fun selectItemByName(name: String, isRefresh: Boolean = true) {
        resourceListAdapter?.apply {
            resourceModelList.forEach {
                it.isSelect = it.resourceItem.name == name
            }
            if (isRefresh) {
                notifyItemRangeChanged(0, resourceModelList.size)
            }
        }
    }

    fun updateMultiItemIcon(iconList: ArrayList<Bitmap?>, startPosition: Int, endPosition: Int) {
        resourceListAdapter?.apply {
            resourceModelList.forEachIndexed { index, resource ->
                if( index in startPosition..endPosition) {
                    resource.resourceItem.videoFrame = iconList[index - startPosition]
                }
            }
            notifyItemRangeChanged(startPosition, endPosition - startPosition + 1)
        }
    }

    fun clearItemShowReporter() {
        recyclerEventHelper.resetShownSet()
        recyclerEventHelper.recyclerView = null
        recyclerEventHelper.onVisibleCallback = null
    }
}