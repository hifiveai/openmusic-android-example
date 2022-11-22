package com.ss.ugc.android.editor.base.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

@SuppressLint("ViewConstructor")
class StatusView constructor(
    context: Context,
    providers: Map<CommonUiState, (parent: ViewGroup) -> View>,
    initState: CommonUiState = CommonUiState.NONE,
    attrs: AttributeSet? = null)
    : FrameLayout(context, attrs), IStateAware<CommonUiState> {

    private val viewCache: MutableMap<CommonUiState, View> = mutableMapOf()
    private val viewProviders: MutableMap<CommonUiState, (parent: ViewGroup) -> View> = mutableMapOf()
    private var state: CommonUiState

    init {
        viewProviders.putAll(providers)
        state = initState
    }

    override fun setState(state: CommonUiState) {
        detachState()
        this.state = state
        attachState()
    }

    override fun getState(): CommonUiState = state

    public fun updateViewProvider(state: CommonUiState, provider: (parent: ViewGroup) -> View) {
        viewProviders[state] = provider
    }

    private fun attachState() {
        viewProviders[this.state]?.let { provider ->
            if (!viewCache.containsKey(this.state)) {
                val v = provider.invoke(this).apply {
                    this.layoutParams = LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                }
                this.addView(v)
                viewCache[this.state] = v
            }

            viewCache[this.state]?.let {
                it.visibility = View.VISIBLE
            }
        }
    }

    private fun detachState() {
        viewCache[this.state]?.let {
            it.visibility = View.GONE
        }
    }
}