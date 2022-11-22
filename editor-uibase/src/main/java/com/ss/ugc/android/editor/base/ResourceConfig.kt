package com.ss.ugc.android.editor.base

import com.ss.ugc.android.editor.base.resource.base.AnimationResConfig
import com.ss.ugc.android.editor.base.resource.base.StickerAnimationResConfig
import com.ss.ugc.android.editor.base.resource.base.TextAnimationResConfig
import com.ss.ugc.android.editor.base.resource.base.TextPanelConfig

/**
 * 剪辑资源配置
 */
open class ResourceConfig constructor(builder: Builder) {

    val accessKey: String? = builder.accessKey
    val appVersion = builder.appVersion
    val filterPanel: String? = builder.filterPanel
    val stickerPanel: String? = builder.stickerPanel
    val textPanelConfig: TextPanelConfig? = builder.textPanelConfig
    val textFlowerPanel: String? = builder.textFlowerPanel
    val textBubblePanel: String? = builder.textBubblePanel
    val maskPanel: String? = builder.maskPanel
    val tonePanel: String? = builder.tonePanel
    val animationConfig: AnimationResConfig? = builder.animationResConfig
    val videoEffectPanel: String? = builder.videoEffectPanel
    val textAnimationResConfig: TextAnimationResConfig? = builder.textAnimationResConfig
    val stickerAnimationResConfig: StickerAnimationResConfig? = builder.stickerAnimationResConfig
    val transitionPanel: String? = builder.transitionPanel
    val blendModePanel: String? = builder.blendModePanel
    val curveSpeedPanel: String? = builder.curveSpeedPanel
    val canvasPanel: String? = builder.canvasPanel
    var textTemplatePanel: String? = builder.textTemplatePanel
    var businessId: String? = builder.businessId
    val useCache: Boolean = builder.useCache
    open class Builder {
        open var accessKey: String? = null
        open var appVersion: String? = null
        open var filterPanel: String? = null
        open var stickerPanel: String? = null
        open var textPanelConfig: TextPanelConfig? = null
        open var textFlowerPanel: String? = null
        open var textBubblePanel: String? = null
        open var transitionPanel: String? = null
        open var blendModePanel: String? = null
        open var maskPanel: String? = null
        open var tonePanel: String? = null
        open var videoEffectPanel: String? = null
        open var textAnimationResConfig: TextAnimationResConfig? = null
        open var stickerAnimationResConfig: StickerAnimationResConfig? = null
        open var animationResConfig: AnimationResConfig? = null
        open var curveSpeedPanel: String? = null
        open var canvasPanel: String? = null
        open var textTemplatePanel: String? = null
        open var businessId: String? = null
        open var useCache: Boolean = true
        fun businessId(businessId: String?) = apply {this.businessId = businessId}
        fun accessKey(accessKey: String) = apply { this.accessKey = accessKey }
        fun appVersion(appVersion: String) = apply { this.appVersion = appVersion }
        fun filterPanel(filterPanel: String) = apply { this.filterPanel = filterPanel }
        fun stickerPanel(stickerPanel: String) = apply { this.stickerPanel = stickerPanel }
        fun textPanelConfig(textPanelConfig: TextPanelConfig) =
            apply { this.textPanelConfig = textPanelConfig }

        fun textFlowerPanel(textFlowerPanel: String) =
            apply { this.textFlowerPanel = textFlowerPanel }
        fun textBubblePanel(textBubblePanel: String) =
            apply { this.textBubblePanel = textBubblePanel }
        fun transitionPanel(panel: String) = apply { this.transitionPanel = panel }
        fun maskPanel(maskPanel: String) = apply { this.maskPanel = maskPanel }
        fun animationResConfig(config: AnimationResConfig) = apply { this.animationResConfig = config }
        fun videoEffectPanel(videoEffectPanel: String) = apply { this.videoEffectPanel = videoEffectPanel }
        fun textAnimationResConfig(textAnimationResConfig: TextAnimationResConfig) = apply { this.textAnimationResConfig = textAnimationResConfig }
        fun stickerAnimationResConfig(stickerAnimationResConfig: StickerAnimationResConfig) = apply { this.stickerAnimationResConfig = stickerAnimationResConfig }
        fun blendModePanel(blendMode: String) = apply { this.blendModePanel = blendMode }
        fun tonePanel(tonePanel: String) = apply { this.tonePanel = tonePanel }
        fun curveSpeedPanel(curveSpeedPanel: String) = apply { this.curveSpeedPanel = curveSpeedPanel }
        fun textTemplatePanel(config: String) =
            apply { this.textTemplatePanel = config }

        fun canvasPanel(canvasPanel: String) = apply { this.canvasPanel = canvasPanel }
        fun useCache(use: Boolean) = apply { this.useCache = use }
        fun builder(): ResourceConfig {
            return ResourceConfig(this)
        }

        fun builder(builder: Builder): ResourceConfig {
            return ResourceConfig(builder)
        }
    }
}