package com.vesdk.vebase.demo.model;


/**
 */

public class FuncItem {
    private String name;
    private int imageId;

    public FuncItem(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }
}