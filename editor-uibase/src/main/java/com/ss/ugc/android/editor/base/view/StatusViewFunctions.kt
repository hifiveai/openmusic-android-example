package com.ss.ugc.android.editor.base.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ss.ugc.android.editor.base.R


@JvmOverloads
fun statusLoadingView(parent: ViewGroup, configure: ((loading: LoadingView) -> Unit)? = null): LoadingView {
    return LoadingView(parent.context).apply {
        configure?.invoke(this)
    }
}

@JvmOverloads
fun stateEmptyView(parent: ViewGroup,
    configure: ((title: TextView, desc: TextView) -> Unit)? = null): View {
    return LayoutInflater.from(parent.context).inflate(
        R.layout.editor_default_empty_state, parent, false).apply {
        val title = this.findViewById<TextView>(R.id.tv_title)
        val desc = this.findViewById<TextView>(R.id.tv_desc)
        configure?.invoke(title, desc)
    }
}

@JvmOverloads
fun stateErrorView(parent: ViewGroup,
    configure: ((title: TextView, desc: TextView, button: TextView) -> Unit)? = null): View {
    return LayoutInflater.from(parent.context).inflate(
        R.layout.editor_default_error_state, parent, false).apply {
        val title = this.findViewById<TextView>(R.id.tv_title)
        val desc = this.findViewById<TextView>(R.id.tv_desc)
        val button = this.findViewById<TextView>(R.id.tv_button)
        configure?.invoke(title, desc, button)
    }
}
