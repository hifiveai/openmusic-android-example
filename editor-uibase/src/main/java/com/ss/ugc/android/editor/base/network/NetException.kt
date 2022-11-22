package com.ss.ugc.android.editor.base.network

import java.lang.RuntimeException


data class NetException(
    var code: Int? = 0,
    var msg: String? = null
) : RuntimeException(msg)