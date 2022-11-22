package com.ss.ugc.android.editor.base.model;

/**
 */
public class ButtonItem {
    private int icon;
    private int title;
    private int desc;

    public ButtonItem() {}

    public ButtonItem(int icon) {
        this.icon = icon;
    }

    public ButtonItem(int icon, int title) {
        this.icon = icon;
        this.title = title;
    }

    public ButtonItem(int icon, int title, int desc) {
        this.icon = icon;
        this.title = title;
        this.desc = desc;
    }

    public int getIcon() {
        return icon;
    }

    public ButtonItem setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public int getTitle() {
        return title;
    }

    public ButtonItem setTitle(int title) {
        this.title = title;
        return this;
    }

    public int getDesc() {
        return desc;
    }

    public ButtonItem setDesc(int desc) {
        this.desc = desc;
        return this;
    }
}
