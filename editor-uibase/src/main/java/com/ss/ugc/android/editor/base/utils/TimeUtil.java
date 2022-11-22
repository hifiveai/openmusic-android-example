package com.ss.ugc.android.editor.base.utils;

import android.annotation.SuppressLint;
import android.hardware.SensorEvent;
import android.os.SystemClock;

/**
 * time : 2020/5/28
 * author : tanxiao
 * description :
 */
public class TimeUtil {

    @SuppressLint("NewApi")
    public static double getTimestamp(SensorEvent sensorEvent) {
        long cur_time_nano = System.nanoTime();
        long delta_nano_time = Math.abs(cur_time_nano - sensorEvent.timestamp);
        long delta_elapsed_nano_time = Math.abs(SystemClock.elapsedRealtimeNanos() - sensorEvent.timestamp);
        return (cur_time_nano - Math.min(delta_nano_time, delta_elapsed_nano_time));
    }

}
