package com.ss.ugc.android.editor.preview.adjust

import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.view.OrientationEventListener
import com.ss.ugc.android.editor.preview.adjust.utils.PadUtil
import java.util.concurrent.CopyOnWriteArrayList

interface OrientationListener {
    fun onOrientationChanged(orientation: Int)
}

object OrientationManager {
    private const val TAG = "OrientationManager"

    private var screenOrientation = Configuration.ORIENTATION_PORTRAIT
    private val listenerList = CopyOnWriteArrayList<OrientationListener>()
    private val isPad = PadUtil.isPad
    private val uiHandler = Handler(Looper.getMainLooper())
    private var hadInit = false

    fun initOrientation() {
        if (!hadInit) {
            hadInit = true
            screenOrientation = ModuleCommon.application.resources.configuration.orientation
        }
    }

    fun getOrientation(): Int {
        return screenOrientation
    }

    fun isLand(): Boolean = screenOrientation == Configuration.ORIENTATION_LANDSCAPE

    private val orientationListener: OrientationEventListener by lazy {
        val context = ModuleCommon.application
        object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                val o = context.resources.configuration.orientation
                if (o != Configuration.ORIENTATION_UNDEFINED && o != screenOrientation && !PadUtil.isInMagicWindow) {
                    screenOrientation = o
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        notify(o)
                    } else {
                        uiHandler.post { notify(o) }
                    }
                }
            }
        }
    }

    fun startMonitor() {
        if (!isPad) return
        orientationListener.enable()
    }

    @Synchronized
    fun register(listener: OrientationListener) {
        if (!isPad) return
        if (listener !in listenerList) {
            listenerList.add(listener)
        }
    }

    @Synchronized
    fun unregister(listener: OrientationListener) {
        if (!isPad) return
        listenerList.remove(listener)
    }

    @Synchronized
    fun notify(orientation: Int) {
        listenerList.forEach { listener ->
            uiHandler.post {
                listener.onOrientationChanged(orientation)
            }
        }
    }

    @Synchronized
    fun updateOrientation(orientation: Int) {
//        BLog.i(TAG, "debug update orientation $orientation")
        if (orientation != Configuration.ORIENTATION_UNDEFINED && orientation != screenOrientation) {
            screenOrientation = orientation

            if (Looper.myLooper() == Looper.getMainLooper()) {
                notify(orientation)
            } else {
                uiHandler.post { notify(orientation) }
            }
        }
    }
}
