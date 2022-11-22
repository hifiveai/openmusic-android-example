package com.vesdk.verecorder.record.preview.model;

/**
 * time : 2020/11/18
 *
 * description :
 */
public class Resolution {
    public String name;
    public int resolution;
    public float ratio;
    public int width;
    public int height;

    public Resolution(String name, int resolution) {
        this.name = name;
        this.resolution = resolution;
    }

    public Resolution(String name, int resolution, float ratio, int width, int height) {
        this.name = name;
        this.resolution = resolution;
        this.ratio = ratio;
        this.width = width;
        this.height = height;
    }

    public Resolution(String name, int resolution, float ratio) {
        this.name = name;
        this.resolution = resolution;
        this.ratio = ratio;
        this.width = resolution/ 16 * 16;
        this.height = ((int) (resolution * ratio)/ 16 * 16);
    }

    @Override
    public String toString() {
        return "name:"+name
                +" resolution:"+resolution
                +" ratio:"+ratio
                +" width:"+width
                +" height:"+height;
    }
}
