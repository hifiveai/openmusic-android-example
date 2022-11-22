package com.ss.ugc.android.editor.base.network

import androidx.annotation.Keep
import java.io.Serializable

@Keep
open class BaseResponse<T> : Serializable {
    var code: Int? = 0
    var message: String? = null
    var data: T? = null

    fun isSuccess(): Boolean{
        return code == 0 && data != null
    }

}