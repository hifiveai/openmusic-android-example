package com.vesdk.verecorder.record.demo.view;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

public abstract class CustomCountDownTimer {
    private static final int MSG = 1;
    private final long mMillisInFuture;
    private final long mCountdownInterval;
    private long mStopTimeInFuture;
    private long mPauseTimeInFuture;
    private boolean isStop = false;
    private boolean isPause = false;

    /**
     * @param millisInFuture    总倒计时时间
     * @param countDownInterval 倒计时间隔时间
     */
    public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
    }

    private synchronized CustomCountDownTimer start(long millisInFuture) {
        isStop = false;
        if (millisInFuture <= 0) {
            onFinish();
            return this;
        }
        mStopTimeInFuture = millisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * 开始倒计时
     */
    public synchronized final void start() {
        start(mMillisInFuture);
    }

    /**
     * 停止倒计时
     */
    public synchronized final void stop() {
        isStop = true;
        mHandler.removeMessages(MSG);
    }

    /**
     * 暂时倒计时
     * 调用{@link #restart()}方法重新开始
     */
    public synchronized final void pause() {
        if (isStop) {
            return;
        }

        isPause = true;
        mPauseTimeInFuture = mStopTimeInFuture;
        mHandler.removeMessages(MSG);
    }

    /**
     * 重新开始
     */
    public synchronized final void restart() {
        if (isStop || !isPause) {
            return;
        }

        isPause = false;
        start(mPauseTimeInFuture);
    }

    /**
     * 倒计时间隔回调
     *
     * @param millisUntilFinished 剩余毫秒数
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * 倒计时结束回调
     */
    public abstract void onFinish();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized ( CustomCountDownTimer.this) {
                if (isStop || isPause) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - mCountdownInterval;
                if (millisLeft <= 0) {
                    onFinish();
                } else {
                    onTick(millisLeft);
                    mStopTimeInFuture = millisLeft;
                    sendMessageDelayed(obtainMessage(MSG), mCountdownInterval);
                }
            }
        }
    };
}