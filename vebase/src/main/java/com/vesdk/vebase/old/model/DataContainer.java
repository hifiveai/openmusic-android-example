package com.vesdk.vebase.old.model;

import android.graphics.Bitmap;

/**
 * time : 2020/5/8
 *
 * description :
 * 数据定义
 */
public class DataContainer {

    public static class FunctionItem {
        public int index;
        public String name;
        public int resId;
        public String resPath;
        public String icon;
        public boolean select;
        public boolean isOverlap;

        public FunctionItem(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public FunctionItem(int index, String name, int resId, String resPath, boolean select, boolean isOverlap) {
            this.index = index;
            this.name = name;
            this.resId = resId;
            this.resPath = resPath;
            this.select = select;
            this.isOverlap = isOverlap;
        }

        public FunctionItem(int index, String name, String icon, String resPath, boolean select, boolean isOverlap) {
            this.index = index;
            this.name = name;
            this.icon = icon;
            this.resPath = resPath;
            this.select = select;
            this.isOverlap = isOverlap;
        }
    }

    public static class Thumbnail {
        public Bitmap thumb;

        public Thumbnail(Bitmap thumb) {
            this.thumb = thumb;
        }
    }
}
