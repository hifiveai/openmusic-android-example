package com.ss.ugc.android.editor.preview.adjust

import android.app.Application
import android.content.res.AssetManager
import androidx.annotation.StringRes

object ModuleCommon : BaseModule() {
    override fun init(context: Application, appName: String) {
        super.init(context, appName)

//        LifecycleManager.attachToApplication(context)
    }
}

fun getString(@StringRes id: Int): String {
    return ModuleCommon.application.getString(id)
}

@Suppress("SpreadOperator")
fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
    return ModuleCommon.application.getString(id, *formatArgs)
}

fun assets(): AssetManager = ModuleCommon.application.assets