package com.ss.ugc.android.editor.base.data

import com.google.gson.annotations.SerializedName

class TemplateText(@SerializedName("bounding_box")
                   val box: List<Float> = emptyList(),
                   @SerializedName("value")
                   val content: String = "",
                   @SerializedName("index")
                   val index: Int = -1) {

}
