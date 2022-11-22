package com.vesdk.vebase.resource;


import androidx.annotation.Keep;

import java.util.List;

@Keep
public class EditorResBean {
    private String type;
    private ResourceBean resource;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceBean getResource() {
        return resource;
    }

    public void setResource(ResourceBean resource) {
        this.resource = resource;
    }

    @Keep
    public static class ResourceBean {
        private List<ResourceItem> list;

        public List<ResourceItem> getList() {
            return list;
        }

        public void setList(List<ResourceItem> list) {
            this.list = list;
        }
    }
}
