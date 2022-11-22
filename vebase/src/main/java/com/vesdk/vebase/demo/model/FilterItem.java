package com.vesdk.vebase.demo.model;

/**
 *  on 2019-07-21 12:23
 */
public class FilterItem {
    private int type;
    private String title;
    private int icon;
    private String pic;
    private String resource;

    public FilterItem(String title, int icon, String resource) {
        this.title = title;
        this.icon = icon;
        this.resource = resource;
    }
    public FilterItem(String title, String pic, String resource) {
        this.title = title;
        this.pic = pic;
        this.resource = resource;
    }
    public FilterItem(String title, int icon, String resource, int type) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
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
}
