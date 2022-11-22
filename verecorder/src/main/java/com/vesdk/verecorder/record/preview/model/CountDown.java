package com.vesdk.verecorder.record.preview.model;

/**
 * time : 2020/11/18
 *
 * description :
 */
public class CountDown {
    public String name;
    public int delay;
    public int res;

    public CountDown(String name, int delay) {
        this.name = name;
        this.delay = delay;
    }

    public CountDown(String name, int delay, int res) {
        this.name = name;
        this.delay = delay;
        this.res = res;
    }
}
