package com.ss.ugc.android.editor.preview.adjust

import android.app.Application

open class BaseModule {
    var application: Application by Delegate.notNullSingleValue()
    var appName: String by Delegate.notNullSingleValue()

    open fun init(context: Application, appName: String) {
        this.application = context
        this.appName = appName
    }
}