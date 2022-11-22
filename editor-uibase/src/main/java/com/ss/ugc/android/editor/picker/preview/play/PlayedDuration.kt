package com.ss.ugc.android.editor.picker.preview.play

import android.os.SystemClock

private const val STATE_PLAYING = 1
private const val STATE_STOPPING = 2

internal class PlayedDuration {

    private var mState = STATE_STOPPING
    private var mPlayedDuration = 0
    private var mStartPlayTime: Long = 0

    fun start() {
        if (mState == STATE_STOPPING) {
            mState = STATE_PLAYING
            mStartPlayTime = SystemClock.elapsedRealtime()
        }
    }

    fun stop() {
        if (mState == STATE_PLAYING) {
            mState = STATE_STOPPING
            val duration = (SystemClock.elapsedRealtime() - mStartPlayTime).toInt()
            if (duration >= 0) {
                mPlayedDuration += duration
            }
        }
    }

    fun getPlayedDuration(): Int {
        if (mState == STATE_PLAYING) {
            val curTime = SystemClock.elapsedRealtime()
            val duration = (curTime - mStartPlayTime).toInt()
            if (duration >= 0) {
                mPlayedDuration += duration
            }
            mStartPlayTime = curTime
        }
        return mPlayedDuration
    }

    fun clear() {
        mPlayedDuration = 0
        if (mState == STATE_PLAYING) {
            mStartPlayTime = SystemClock.elapsedRealtime()
        }
    }

    fun reset() {
        mState = STATE_STOPPING
        mPlayedDuration = 0
        mStartPlayTime = 0
    }
}