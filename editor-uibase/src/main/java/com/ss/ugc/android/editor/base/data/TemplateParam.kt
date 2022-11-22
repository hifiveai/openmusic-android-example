package com.ss.ugc.android.editor.base.data

import android.graphics.RectF
import android.util.SizeF
import com.google.gson.annotations.SerializedName

/**
 * time : 2020/12/28
 *
 * description :
 *
 */
class TemplateParam(@SerializedName("text_list")
                    val texts: List<TemplateText>,
                    @SerializedName("bounding_box")
                    val box: List<Float> = emptyList()) {
    fun boundingBox(): SizeF? {
        return if (box.size != 4) {
            null
        } else {
            SizeF(
                    (box[2] / 2F + 0.5F) - (box[0] / 2F + 0.5F),
                    (0.5F - box[1] / 2F) - (0.5F - box[3] / 2F)
            )
        }
    }

    fun textsBounds(): List<RectF> {
        return texts.mapNotNull {
            if (it.box.size != 4) {
                null
            } else {
                RectF(
                        it.box[0] / 2F + 0.5F,
                        0.5F - it.box[3] / 2F,
                        it.box[2] / 2F + 0.5F,
                        0.5F - it.box[1] / 2F
                )
            }
        }
    }

    fun textContent(index: Int): String {
        if (index < 0 || index > texts.size) {
            return ""
        }

        texts.forEach {
            if (it.index == index) {
                return it.content
            }
        }

        return ""
    }

}