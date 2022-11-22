package com.ss.ugc.android.editor.base.data

import android.graphics.Color
import android.graphics.RectF
import com.bytedance.ies.nle.editor_jni.NLESegmentTextSticker
import com.bytedance.ies.nle.editor_jni.NLETrack
import com.bytedance.ies.nle.editor_jni.NLETrackSlot
import com.ss.ugc.android.editor.base.data.AlignInfo.Type.ALIGN
import com.ss.ugc.android.editor.base.data.ShadowInfo.Type.COLOR
import com.ss.ugc.android.editor.base.utils.DraftTypeUtils
import com.ss.ugc.android.editor.core.emptyStickerToEmpty
import com.ss.ugc.android.editor.core.isImageSticker
import com.ss.ugc.android.editor.core.isInfoSticker
import com.ss.ugc.android.editor.core.isTextSticker
import com.ss.ugc.android.editor.core.utils.GsonUtil

/**
 * time : 2020/12/29
 *
 * description :
 *
 */
class TextInfo(
        val materialId: String,
        /**
         * 文字内容
         */
        var text: String = "",
        /**
         * 阴影
         */
        var shadow: Boolean = false,
        /**
         * 阴影颜色
         */
        var shadowColor: Int = Color.TRANSPARENT,

        /**
         * 阴影透明度
         */
        var shadowAlpha: Float = ShadowInfo.DEFAULT_ALPHA,
        /**
         * 阴影模糊度
         */
        var shadowSmoothing: Float = ShadowInfo.DEFAULT_SMOOTHING,
        /**
         * 阴影距离
         */
        val shadowDistance: Float = ShadowInfo.DEFAULT_DISTANCE,
        /**
         * 阴影角度
         */
        var shadowAngle: Float = ShadowInfo.DEFAULT_ANGLE,

        /**
         * 颜色
         */
        val textColor: Int = Color.WHITE,
        /**
         * 边框颜色
         */
        var strokeColor: Int = 0,

        val fontPath: String = "",

        /**文本模板名称*/
        var styleName: String = "",

        /**背景*/
        var backgroundColor: Int = Color.TRANSPARENT,

        /**字间距*/
        val letterSpace: Float = 0F,
        /**
         * 行距
         */
        val lineLeading: Float = AlignInfo.DEFAULT_LINE_SPACING,

        /**
         * 补充信息
         */
        var extraInfo: String? = null,

        /**
         * 文字大小
         */
        val textSize: Float = MaterialText.DEFAULT_TEXT_SIZE,

        /**文本类型 区分普通文本还是字幕文本*/
        val textType: String = DraftTypeUtils.MetaType.TYPE_TEXT,
        /**
         * 文本不透明度
         */
        var textAlpha: Float = 1F,

        /**
         * 边框粗细
         */
        var borderWidth: Float = MaterialText.DEFAULT_BORDER_SIZE,
        /**
         * 背景不透明度
         */
        var backgroundAlpha: Float = 1F,
        /**
         * 对齐方式
         */
        val textAlign: Int = MaterialText.ALIGN_CENTER,
        /**
         * 文本花字是否使用资源包内置颜色
         */
        val useEffectDefaultColor: Boolean = true,
        /**
         * 字体 effectId
         */
        val fontId: String = "",
        /**
         * 字体 resourceID
         */
        val fontResourceId: String = "",
        /**
         * 字体名称 上报使用
         */
        val fontTitle: String = "系统",
        /**
         * 气泡翻转 X , Y
         */
        val shapeFlipX: Boolean = false,
        /**
         * 气泡翻转 X , Y
         */
        val shapeFlipY: Boolean = false,
        /**
         * 文本花字
         */
        val textEffectInfo: TextEffectInfo? = null,
        /**
         * 文本气泡
         */
        var textBubbleInfo: TextEffectInfo? = null,
        /**
         * 文本方向
         */
        val textOrientation: Int = 0,
        /**
         * KTV文本动画颜色主色
         */
        val ktvColor: Int = Color.TRANSPARENT,

        /**
         * 文本朗读对应的audioId
         */
        var textToAudioIds: MutableList<String>? = null,

        /**
         * 粗体宽度（范围[-0.05, 0.05] 默认0不加粗，正值加粗）
         */
        val boldWidth: Float = MaterialText.DEFAULT_BOLD_WIDTH,

        /**
         * 斜体角度 范围[0, 45] 默认0
         */
        val italicDegree: Int = MaterialText.DEFAULT_ITALIC_DEGREE,

        /**
         * 是否有下划线
         */
        val underline: Boolean = MaterialText.DEFAULT_UNDER_LINE,

        /**
         * 下划线宽度 范围[0.0, 1.0]
         */
        val underlineWidth: Float = MaterialText.DEFAULT_UNDER_LINE_WIDTH,

        /**
         * 下划线与文字偏移 范围[0.0, 1.0]
         */
        val underlineOffset: Float = MaterialText.DEFAULT_UNDER_LINE_OFFSET,
        /**
         * 字幕,歌词生成类型
         */
        val subType: Int = MaterialText.DEFAULT_SUB_TYPE,
        /**
         * 贴纸的宽高
         * 仅关键帧回调后才有值
         */
        var size: RectF? = null
) {
    companion object {
        fun build(nleTrack: NLETrack, slot: NLETrackSlot?): TextInfo? {
            return GsonUtil.fromJson(slot.toString(), TextInfo::class.java).apply {
                slot?.apply {
                    //文字不支持翻转
                    if (isInfoSticker() || isImageSticker()) {
                        textBubbleInfo = TextEffectInfo("", "", "")
                    }
                    if (isTextSticker()) {
                        //空文字，点击屏幕需要删除
                        text =
                            NLESegmentTextSticker.dynamicCast(mainSegment)?.content?.emptyStickerToEmpty()
                                ?: ""
                    }
                }
            }
        }
    }
}

class ShadowInfo(
        val color: Int = Color.TRANSPARENT,
        val alpha: Float = DEFAULT_ALPHA,
        val smooth: Float = DEFAULT_SMOOTHING,
        val distance: Float = DEFAULT_DISTANCE,
        val angle: Float = DEFAULT_ANGLE,
        val type: Type = COLOR
) {
    val enable: Boolean = color != Color.TRANSPARENT

    companion object {
        const val DEFAULT_ALPHA = 0.8F
        const val DEFAULT_SMOOTHING = 0.9999F
        const val DEFAULT_DISTANCE = 8F
        const val DEFAULT_ANGLE = -45F
    }

    enum class Type { COLOR, ALPHA, SMOOTH, DISTANCE, ANGLE }
}

class AlignInfo(
        val align: Int = MaterialText.ALIGN_CENTER,
        val orientation: Int = 0,
        val lineSpacing: Float = DEFAULT_LINE_SPACING,
        val type: Type = ALIGN
) {
    companion object {
        const val DEFAULT_LINE_SPACING = 0.1F
    }

    enum class Type { ALIGN, LINE_SPACING }
}

class MaterialText {
    companion object {
        const val TYPE_TEXT = DraftTypeUtils.MetaType.TYPE_TEXT
        const val TYPE_SUBTITLE = DraftTypeUtils.MetaType.TYPE_SUBTITLE
        const val TYPE_LYRIC = DraftTypeUtils.MetaType.TYPE_LYRIC

        const val DEFAULT_FONT_ID = ""
        const val DEFAULT_FONT_TITLE: String = "系统"
        const val DEFAULT_BORDER_SIZE = 0.06F
        const val MAX_BORDER_WIDTH = 0.15F
        const val DEFAULT_TEXT_SIZE = 15F

        val DEFAULT_SHADOW_COLOR = Color.parseColor("#2e2e2e")

        const val ALIGN_LEFT = 0
        const val ALIGN_CENTER = 1
        const val ALIGN_RIGHT = 2
        const val ALIGN_UP = 3
        const val ALIGN_DOWN = 4

        const val DEFAULT_BOLD_WIDTH = 0F
        const val DEFAULT_ITALIC_DEGREE = 0
        const val DEFAULT_UNDER_LINE = false
        const val DEFAULT_UNDER_LINE_WIDTH = 0.05F
        const val DEFAULT_UNDER_LINE_OFFSET = 0.22F
        const val SELECTED_BOLD_WIDTH = 0.008F
        const val SELECTED_ITALIC_DEGREE = 10

        const val DEFAULT_SUB_TYPE = 0
        const val VIDEO_SUBTITLE_TYPE = 1
        const val RECORD_SUBTITLE_TYPE = 2
    }
}


/**
 * 文字特效数据实体类
 */
data class TextEffectInfo(
        val path: String,
        val type: String,
        val resourceId: String,
        val value: Float = 0F,
        val effectId: String = "",
        val name: String = "",
        val categoryName: String = "",
        val categoryId: String = "",
        val materialId: String? = null,
        val sourcePlatform: Int = 0
) {
    override fun toString(): String {
        return "TextEffectInfo(name='$name')"
    }

//    companion object {
//        internal fun build(effect: MaterialEffect): TextEffectInfo? = TextEffectInfo(
//                effect.path,
//                effect.type,
//                effect.value,
//                effect.effectId,
//                effect.name,
//                effect.categoryName ?: "",
//                effect.categoryId ?: "",
//                effect.id,
//                effect.resourceId,
//                effect.sourcePlatform
//        )
//    }
}