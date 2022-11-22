package com.ss.ugc.android.editor.base.resourceview

import androidx.recyclerview.widget.RecyclerView
import com.ss.ugc.android.editor.base.resource.ResourceItem
import com.ss.ugc.android.editor.base.theme.ThemeStore
import com.ss.ugc.android.editor.base.theme.resource.*


class ResourceViewConfig private constructor(builder: Builder) {

    val layoutManager = builder.layoutManager
    val itemDecoration = builder.itemDecoration
    val panelKey = builder.panelKey
    val hasCategory = builder.hasCategory
    val categoryKey = builder.categoryKey
    val downloadAfterFetchList = builder.downloadAfterFetchList
    val nullItemInFirstConfig = builder.firstNullItemConfig
    val resourceTextConfig = builder.resourceTextConfig
    val resourceImageConfig = builder.resourceImageConfig
    val downloadIconConfig = builder.downloadIconConfig
    val selectorConfig = builder.itemSelectorConfig
    val customItemConfig = builder.customItemConfig
    val iconStyle = builder.iconStyle
    val resourceListReporter = builder.resourceListReporter

    fun enableFirstNullItem(): Boolean {
        return nullItemInFirstConfig.addNullItemInFirst
    }
    class Builder {
        //列表布局定制
        var layoutManager: RecyclerView.LayoutManager? = null
        var itemDecoration: RecyclerView.ItemDecoration? = null

        //面板资源标识（内置和Loki都一致）
        var panelKey: String? = null

        //分类资源标识
        var categoryKey: String? = null

        //是否需要从分类tab下拉取资源
        var hasCategory: Boolean = false

        // 列表拉取成功后是否自动下载资源
        var downloadAfterFetchList = false

        // 定制"空"项
        var firstNullItemConfig: FirstNullItemConfig = ThemeStore.getFirstNullItemConfig()

        //定制文字样式
        var resourceTextConfig: ResourceTextConfig = ThemeStore.getResourceTextConfig()

        //定制资源缩略图样式
        var resourceImageConfig: ResourceImageConfig = ThemeStore.getResourceImageConfig()

        //定制下载icon的样式
        var downloadIconConfig: DownloadIconConfig = ThemeStore.getDownloadIconConfig()

        //定制item的选中框的样式
        var itemSelectorConfig: ItemSelectorConfig = ThemeStore.getItemSelectorConfig()

        //定制自定义项
        var customItemConfig: CustomItemConfig = ThemeStore.getCustomItemConfig()

        //定制icon的来源
        var iconStyle: IconStyle = IconStyle.IMAGE

        //监控打点接口注入
        var resourceListReporter: IResourceListReporter? = null

        fun layoutManager(layoutManager: RecyclerView.LayoutManager) = apply {
            this.layoutManager = layoutManager
        }

        fun itemDecoration(itemDecoration: RecyclerView.ItemDecoration) = apply {
            this.itemDecoration = itemDecoration
        }

        fun panelKey(panelKey: String?) = apply {
            this.panelKey = panelKey
        }

        fun downloadAfterFetchList(downloadAfterFetchList: Boolean) = apply {
            this.downloadAfterFetchList = downloadAfterFetchList
        }

        fun nullItemInFirstConfig(firstNullItemConfig: FirstNullItemConfig) = apply {
            this.firstNullItemConfig = firstNullItemConfig
        }

        fun resourceImageConfig(resourceImageConfig: ResourceImageConfig) = apply {
            this.resourceImageConfig = resourceImageConfig
        }

        fun downloadIconConfig(downloadIconConfig: DownloadIconConfig) = apply {
            this.downloadIconConfig = downloadIconConfig
        }

        fun selectorConfig(itemSelectorConfig: ItemSelectorConfig) = apply {
            this.itemSelectorConfig = itemSelectorConfig
        }

        fun customItemConfig(customItemConfig: CustomItemConfig) = apply {
            this.customItemConfig = customItemConfig
        }

        fun categoryKey(categoryKey: String) = apply { this.categoryKey = categoryKey }

        fun resourceListReporter(resourceListReporter: IResourceListReporter) =
            apply { this.resourceListReporter = resourceListReporter }

        fun resourceTextConfig(resourceTextConfig: ResourceTextConfig) = apply {
            this.resourceTextConfig = resourceTextConfig
        }

        fun hasCategory(hasCategory: Boolean) = apply {
            this.hasCategory = hasCategory
        }

        fun iconStyle(iconStyle: IconStyle) = apply {
            this.iconStyle = iconStyle
        }

        fun build(): ResourceViewConfig {
            return ResourceViewConfig(this)
        }
    }
}

interface IResourceListReporter {
    fun resourceItemShow(resource: ResourceItem, position: Int)
    fun resourceItemClick(resource: ResourceItem, position: Int)
}

enum class IconStyle {
    IMAGE,
    VIDEO_FRAME
}
