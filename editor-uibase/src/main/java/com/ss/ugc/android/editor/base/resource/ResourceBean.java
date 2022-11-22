package com.ss.ugc.android.editor.base.resource;

import java.util.List;

public class ResourceBean {

    /**
     * type : filter
     * version : 1.0
     * resource : {"list":[{"order":0,"name":"德古拉","path":"degula","icon":"degula.png"},{"order":1,"name":"暗夜","path":"anye","icon":"ziran.png"},{"order":2,"name":"江浙沪","path":"jiangzhehu","icon":"baixi.png"},{"order":3,"name":"京都","path":"jingdu","icon":"jingdu.png"},{"order":3,"name":"春光乍泄","path":"chunguangzhaxie","icon":"jingdu.png"},{"order":3,"name":"蒸汽波","path":"zhengqibo","icon":"jingdu.png"}]}
     */

    private String type;
    private String version;
    private Resource resource;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public static class Resource {
        private List<ResourceItem> list;

        public List<ResourceItem> getList() {
            return list;
        }

        public void setList(List<ResourceItem> list) {
            this.list = list;
        }

    }
}
