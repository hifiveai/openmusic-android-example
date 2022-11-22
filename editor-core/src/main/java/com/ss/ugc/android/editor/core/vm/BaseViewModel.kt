package com.ss.ugc.android.editor.core.vm

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import com.ss.ugc.android.editor.core.LifecycleViewModel

@Keep
open class BaseViewModel(val activity: FragmentActivity) : LifecycleViewModel(activity.application) {

}