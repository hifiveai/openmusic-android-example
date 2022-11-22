//package com.ss.ugc.android.editor.base.listener
//
//import android.os.Handler
//import android.os.Looper
//import com.ss.android.vesdk.VEListener.VEEditorCompileListener
//
///**
// * @date: 2021/3/3
// * callback on MainThread
// */
//class MainCompileListenerWrapper(val listener: VEEditorCompileListener) : VEEditorCompileListener {
//
//    private val handler = Handler(Looper.getMainLooper())
//
//    override fun onCompileDone() {
//        handler.post {
//            listener.onCompileDone()
//        }
//    }
//
//    override fun onCompileProgress(p0: Float) {
//        handler.post {
//            listener.onCompileProgress(p0)
//        }
//    }
//
//    override fun onCompileError(p0: Int, p1: Int, p2: Float, p3: String?) {
//        handler.post {
//            listener.onCompileError(p0, p1, p2, p3)
//        }
//    }
//}