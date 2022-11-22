package com.ss.ugc.android.editor.base.network


interface IFetchListener<T> {

    fun onSuccess(response: T)

    fun onFailure(ex: NetException)
}