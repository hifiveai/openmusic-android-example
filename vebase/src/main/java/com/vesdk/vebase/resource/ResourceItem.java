package com.vesdk.vebase.resource;

import java.util.List;

public class ResourceItem {
    /**
     * order : 0
     * name : 德古拉
     * path : degula
     * icon : degula.png
     * type :
     */

    private int order;
    private String name; // 资源名称
    private String path; // 资源路径
    private String icon; // 资源展示图标
    private String animationType; //动画类型 1入场 2出场 0组合
    private String singer; //音乐面板展示的歌手名称

    private int alignType; //文字对齐方式

    private String stickerType; //贴纸类型 贴纸4种类型：info/image/text/emoji

    //    "color": [1,0,0,1]
    private List<Float> color; // 字体面板中的底部颜色
    public List<Integer> rgb; // 字体面板中的底部颜色的rgb值

    private boolean select;

    public boolean overlap; // 转场资源 是否交叠



//    public boolean isOverlap() {
//        return overlap;
//    }
//    public void setOverlap(boolean overlap) {
//        this.overlap = overlap;
//    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAnimationType() {
        return animationType;
    }

    public void setAnimationType(String type) {
        this.animationType = type;
    }

    public List<Float> getColor() {
        return color;
    }

    public void setColor(List<Float> color) {
        this.color = color;
    }

    private StyleBean style;

    public StyleBean getStyle() {
        return style;
    }

    public void setStyle(StyleBean style) {
        this.style = style;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public static class StyleBean {
        private List<Float> outlineColor; //样式的描边颜色
        private List<Float> textColor; //样式的字体颜色

        public List<Float> getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(List<Float> backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        private List<Float> backgroundColor; //样式的底色

        public List<Float> getOutlineColor() {
            return outlineColor;
        }

        public void setOutlineColor(List<Float> outlineColor) {
            this.outlineColor = outlineColor;
        }

        public List<Float> getTextColor() {
            return textColor;
        }

        public void setTextColor(List<Float> textColor) {
            this.textColor = textColor;
        }
    }

    public int getAlignType() {
        return alignType;
    }

    public void setAlignType(int alignType) {
        this.alignType = alignType;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getStickerType() {
        return stickerType;
    }

    public void setStickerType(String stickerType) {
        this.stickerType = stickerType;
    }
}
