package com.vesdk.vebase.util;

/**
 * time : 2020/11/18
 *
 * description :
 */
public class AutoPlusCircleInteger {
    private int defaultValue = 0;
    private int max = 0;
    private int cur = 0;

    public AutoPlusCircleInteger(int defaultValue, int max) {
        this.defaultValue = defaultValue;
        this.max = max;
        this.cur = defaultValue;
    }

    public synchronized int get() {
        if (cur < max) {
            cur++;
        } else {
            cur = 0;
        }
        return cur;
    }
}
