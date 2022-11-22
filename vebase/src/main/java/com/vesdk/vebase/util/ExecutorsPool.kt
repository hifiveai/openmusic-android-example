package com.vesdk.vebase.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 *  author : lijunguan
 *  date : 2022/6/8 5:04 下午
 *  description :
 */

class ExecutorsPool {

    val IO_EXECUTOR: Executor by lazy {
        Executors.newSingleThreadExecutor()
    }

    val MAIN_EXECUTOR: Executor by lazy {
        object : Executor {
            val handler = Handler(Looper.getMainLooper())
            override fun execute(command: Runnable) {
                handler.post(command)
            }
        }
    }

    companion object {
        val instance = ExecutorsPool()
    }
}