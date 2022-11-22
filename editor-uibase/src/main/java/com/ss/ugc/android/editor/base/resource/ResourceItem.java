package com.ss.ugc.android.editor.base.resource;

import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.Keep;

import java.util.List;
import java.util.Locale;

@Keep
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
    private String name_en;
    private String path; // 资源路径
    private String icon; // 资源展示图标
    private String animationType; //动画类型 1入场 2出场 0组合
    private String singer; //音乐面板展示的歌手名称

    private int alignType; //文字对齐方式

    private String stickerType; //贴纸类型 贴纸4种类型：info/image/text/emoji
    private String resourceId = "";

    //    "color": [1,0,0,1]
    private List<Float> color; // 字体面板中的底部颜色
    public List<Integer> rgb; // 字体面板中的底部颜色的rgb值

    public boolean overlap; // 转场资源 是否交叠
    // 蒙版类型
    public String mask;

    public String selectedIcon;

    public Float blurRadius = 0f; //画布模糊半径
    public Bitmap videoFrame;
    public String extra;

//    依赖的资源
    public List<ResourceItem> dep;

    private String hint;
    private String tags;
    private Long duration;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        String language = Locale.getDefault().getLanguage();
        if (!TextUtils.equals(language, "zh") && !TextUtils.isEmpty(name_en)) {
            return name_en;
        } else {
            return name;
        }
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

    public List<ResourceItem> getDep() {
        return dep;
    }

    public void setDep(List<ResourceItem> dep) {
        this.dep = dep;
    }

    @Keep
    public static class StyleBean {
        private List<Float> outlineColor; //样式的描边颜色
        private List<Float> textColor; //样式的字体颜色
        private List<Float> shadowColor; // 阴影颜色

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

        public List<Float> getShadowColor() {
            return shadowColor;
        }

        public void setShadowColor(List<Float> shadowColor) {
            this.shadowColor = shadowColor;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(String selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public Float getBlurRadius() { return blurRadius; }

    public void setBlurRadius(Float blurRadius) { this.blurRadius = blurRadius; }

    public Bitmap getVideoFrame() { return videoFrame; }

    public void setVideoFrame(Bitmap videoFrame) { this.videoFrame = videoFrame; }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Long getDuration() {
        return this.duration;
    }
}
