package com.ss.ugc.android.editor.resource

import android.text.TextUtils
import com.ss.ugc.android.editor.base.ResourceConfig
import com.ss.ugc.android.editor.base.resource.*
import com.ss.ugc.android.editor.base.resource.base.DefaultResConfig
import com.ss.ugc.android.editor.base.resource.base.IResourceProvider
import kotlinx.coroutines.*
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * @date: 2021/2/26
 * @desc: buildIn resource(in Assets)
 */
class BuildInResourceProvider : IResourceProvider,CoroutineScope {

    override val resourceConfig: ResourceConfig = ResourceConfig.Builder().builder()

    override fun isUseBuildInResource(): Boolean {
        return true
    }

    private val executeScope: CoroutineScope by lazy {
        CoroutineScope(Executors.newSingleThreadExecutor {
            Thread(it, "Resource")
        }.asCoroutineDispatcher() + SupervisorJob())
    }

    override fun fetchResourceList(panel: String, downloadAfterFetch: Boolean, listener: ResourceListListener<ResourceItem>?) {
        //TODO: 线程池替换
        launch {
            withContext(Dispatchers.IO) {
                when (panel) {
                    DefaultResConfig.MASK_PANEL -> ResourceHelper.getInstance().videoMaskList
                    DefaultResConfig.TRANSITION_PANEL -> ResourceHelper.getInstance().transitionList
                    DefaultResConfig.BLEND_MODE_PANEL -> ResourceHelper.getInstance().blendModeList
                    DefaultResConfig.STICKER_PANEL -> ResourceHelper.getInstance().stickerList
                    DefaultResConfig.FILTER_PANEL -> ResourceHelper.getInstance().filterList2
                    DefaultResConfig.TEXT_FLOWER_PANEL -> ResourceHelper.getInstance().textFlowerList
                    DefaultResConfig.TEXT_FONT_PANEL -> ResourceHelper.getInstance().textFontList
                    DefaultResConfig.TEXT_BUBBLE -> ResourceHelper.getInstance().textBubbleList
                    DefaultResConfig.CURVE_SPEED_PANEL -> ResourceHelper.getInstance().curveSpeedList
                    DefaultResConfig.AUDIO_FILTER -> ResourceHelper.getInstance().audioFilterList
                    DefaultResConfig.CANVAS_STYLE_PANEL -> ResourceHelper.getInstance().canvasStyleList
                    DefaultResConfig.ADJUST_PANEL -> ResourceHelper.getInstance().adjustList
                    else -> emptyList()
                }
            }?.let {
                listener?.onSuccess(it)
            }
        }
    }

    override fun isResourceReady(resourceId: String): Boolean {
        return true
    }

    override fun fetchResource(resourceId: String, listener: ResourceDownloadListener) {
        //buildIn resource do not need to fetchResource
    }

    override fun updateResource(
        resourceId: String,
        resourcePath: String,
        extraParams: Map<String, String>,
        listener: ResourceDownloadListener
    ) {

    }

    override fun getTextList(jsonFileName: String): MutableList<ResourceItem>? {
        return when (jsonFileName) {
            "align" -> ResourceHelper.getInstance().textAlignTypeList
            "style" -> ResourceHelper.getInstance().textStyleList
            "color" -> ResourceHelper.getInstance().textColorsList
            else -> mutableListOf()
        }
    }

    override fun fetchTextList(jsonFileName: String,
        listener: SimpleResourceListener<ResourceItem>?) {
        //TODO: 线程池替换
        launch {
            withContext(Dispatchers.IO) {
                getTextList(jsonFileName)
            }?.let {
                listener?.onSuccess(it)
            }
        }
    }

    override fun fetchCategoryResourceList(
        panel: String,
        category: String,
        listener: ResourceListListener<ResourceItem>?
    ) {
        val resourceList = when (panel) {
            DefaultResConfig.VIDEOEFFECT_PANEL -> {
                when (category) {
                    DefaultResConfig.VIDEOEFFECT_CATEGORY_HOT -> ResourceHelper.getInstance()
                        .getVideoEffectList("hot")
                    DefaultResConfig.VIDEOEFFECT_CATEGORY_ENV -> ResourceHelper.getInstance()
                        .getVideoEffectList("env")
                    else -> ResourceHelper.getInstance().getVideoEffectList("basic")
                }
            }

            DefaultResConfig.TEXT_TEMPLATE -> {
                when (category) {
                    DefaultResConfig.TEXT_TEMPLATE_CATEGORY_HOT -> ResourceHelper.getInstance()
                        .getTextResourceList("text_template.json")
                    DefaultResConfig.TEXT_TEMPLATE_CATEGORY_BASIC -> ResourceHelper.getInstance()
                        .getTextResourceList("text_template.json")
                    else -> ResourceHelper.getInstance().getTextResourceList("text_template.json")
                }
            }

            DefaultResConfig.TEXT_ANIMATION -> {
                ResourceHelper.getInstance().getTextAnimationList(category)
            }

            DefaultResConfig.STICKER_ANIMATION -> {
                ResourceHelper.getInstance().getStickerAnimationList(category)
            }


            DefaultResConfig.ANIMATION_PANEL -> {
                when (category) {
                    DefaultResConfig.ANIMATION_CATEGORY_IN -> ResourceHelper.getInstance()
                        .getAnimationList(1)
                    DefaultResConfig.ANIMATION_CATEGORY_OUT -> ResourceHelper.getInstance()
                        .getAnimationList(2)
                    else -> ResourceHelper.getInstance().getAnimationList(0)
                }
            }

            else ->{
                arrayListOf<ResourceItem>()
            }
        }
        listener?.onSuccess(resourceList)
    }

    override fun fetchPanelInfo(panel: String, listener: ResourceListListener<CategoryInfo>?) {
        val result = ArrayList<CategoryInfo>()
        val isZH = TextUtils.equals(Locale.getDefault().language, "zh")
        when (panel) {
            DefaultResConfig.VIDEOEFFECT_PANEL -> {
                result.add(CategoryInfo(if (isZH) "基础" else "Basic", DefaultResConfig.VIDEOEFFECT_CATEGORY_BASIC))
            }

            DefaultResConfig.TEXT_TEMPLATE -> {
                result.add(CategoryInfo(if (isZH) "热门" else "Hot", DefaultResConfig.TEXT_TEMPLATE_CATEGORY_HOT))
            }

            DefaultResConfig.TEXT_ANIMATION -> {
                result.add(CategoryInfo(if (isZH) "入场动画" else "In", DefaultResConfig.TEXT_ANIM_CATEGORY_IN))
                result.add(CategoryInfo(if (isZH) "出场动画" else "Out", DefaultResConfig.TEXT_ANIM_CATEGORY_OUT))
                result.add(CategoryInfo(if (isZH) "循环动画" else "Loop", DefaultResConfig.TEXT_ANIM_CATEGORY_LOOP))
            }

            DefaultResConfig.ANIMATION_PANEL -> {
                result.add(CategoryInfo(if (isZH) "入场动画" else "In", DefaultResConfig.ANIMATION_CATEGORY_IN))
                result.add(CategoryInfo(if (isZH) "出场动画" else "Out", DefaultResConfig.ANIMATION_CATEGORY_OUT))
                result.add(CategoryInfo(if (isZH) "组合动画" else "Combo", DefaultResConfig.ANIMATION_CATEGORY_ALL))
            }
        }
        listener?.onSuccess(result)
    }

    override fun pushResource(
        resourcePath: String,
        platform: String,
        extraParams: Map<String, String>,
        listener: ResourceDownloadListener
    ) {

    }

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    override fun getCanvasBlurList(): ArrayList<ResourceItem> {
        val blurCanvasList = arrayListOf<ResourceItem>()
        val blurList = listOf(0.1f, 0.45f, 0.75f, 1f)
        blurList.forEach { blur ->
            blurCanvasList.add(ResourceItem().apply {
                name = blur.toString()
                icon = ""
                path = ""
                blurRadius = blur
            })
        }
        return blurCanvasList
    }

}