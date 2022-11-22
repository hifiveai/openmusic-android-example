package com.ss.ugc.android.editor.base.model;

public class FilterItem {
    private int type;
    private int title;
    private int icon;
    private String pic;
    private String resource;
    private String effectId;
    private boolean isLoading;

    public FilterItem(int title, int icon, String resource, int type) {
        this.title = title;
        this.icon = icon;
        this.resource = resource;
        this.type = type;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getEffectId() {
        return effectId;
    }

    public void setEffectId(String effectId) {
        this.effectId = effectId;
    }

    public void setLoading(boolean isLoading) { this.isLoading = isLoading; }

    public boolean getLoading() { return isLoading; }
}
