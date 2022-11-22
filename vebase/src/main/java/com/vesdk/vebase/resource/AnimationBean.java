package com.vesdk.vebase.resource;

import java.util.List;

public class AnimationBean {

    /**
     * type : animation
     * version : 1.0
     * resource : {"in":{"type":"1","name":"入场动画","list":[{"type":"1","name":"入场1","path":"ruchang1","icon":"ruchang1.jpg"},{"type":"1","name":"入场2","path":"ruchang2","icon":"ruchang2.jpg"}]},"out":{"type":"2","name":"出场动画","list":[{"type":"2","name":"出场1","path":"chuchang1","icon":"chuchang1.jpg"},{"type":"2","name":"出场2","path":"chuchang2","icon":"chuchang2.jpg"}]},"all":{"type":"0","name":"组合动画","list":[{"type":"0","order":1,"name":"组合1","path":"zuhe1","icon":"zuhe1.jpg"},{"type":"0","order":2,"name":"组合2","path":"zuhe2","icon":"zuhe2.jpg"}]}}
     */

    private String animationType;
    private String version;
    private ResourceBean resource;

    public String getAnimationType() {
        return animationType;
    }

    public void setAnimationType(String type) {
        this.animationType = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ResourceBean getResource() {
        return resource;
    }

    public void setResource(ResourceBean resource) {
        this.resource = resource;
    }

    public static class ResourceBean {
        /**
         * in : {"type":"1","name":"入场动画","list":[{"type":"1","name":"入场1","path":"ruchang1","icon":"ruchang1.jpg"},{"type":"1","name":"入场2","path":"ruchang2","icon":"ruchang2.jpg"}]}
         * out : {"type":"2","name":"出场动画","list":[{"type":"2","name":"出场1","path":"chuchang1","icon":"chuchang1.jpg"},{"type":"2","name":"出场2","path":"chuchang2","icon":"chuchang2.jpg"}]}
         * all : {"type":"0","name":"组合动画","list":[{"type":"0","order":1,"name":"组合1","path":"zuhe1","icon":"zuhe1.jpg"},{"type":"0","order":2,"name":"组合2","path":"zuhe2","icon":"zuhe2.jpg"}]}
         */

        private InBean in;
        private OutBean out;
        private Combination combination;

        public InBean getIn() {
            return in;
        }

        public void setIn(InBean in) {
            this.in = in;
        }

        public OutBean getOut() {
            return out;
        }

        public void setOut(OutBean out) {
            this.out = out;
        }

        public Combination getCombination() {
            return combination;
        }

        public void setCombination(Combination combination) {
            this.combination = combination;
        }

        public static class InBean {
            /**
             * type : 1
             * name : 入场动画
             * list : [{"type":"1","name":"入场1","path":"ruchang1","icon":"ruchang1.jpg"},{"type":"1","name":"入场2","path":"ruchang2","icon":"ruchang2.jpg"}]
             */

            private String type;
            private String name;
            private List<ResourceItem> list;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<ResourceItem> getList() {
                return list;
            }

            /**
             * type : 1
             * name : 入场1
             * path : ruchang1
             * icon : ruchang1.jpg
             */
            public void setList(List<ResourceItem> list) {
                this.list = list;
            }

        }

        public static class OutBean {
            /**
             * type : 2
             * name : 出场动画
             * list : [{"type":"2","name":"出场1","path":"chuchang1","icon":"chuchang1.jpg"},{"type":"2","name":"出场2","path":"chuchang2","icon":"chuchang2.jpg"}]
             */

            private String type;
            private String name;
            private List<ResourceItem> list;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<ResourceItem> getList() {
                return list;
            }

            public void setList(List<ResourceItem> list) {
                this.list = list;
            }

        }

        public static class Combination {
            /**
             * type : 0
             * name : 组合动画
             * list : [{"type":"0","order":1,"name":"组合1","path":"zuhe1","icon":"zuhe1.jpg"},{"type":"0","order":2,"name":"组合2","path":"zuhe2","icon":"zuhe2.jpg"}]
             */

            private String type;
            private String name;
            private List<ResourceItem> list;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<ResourceItem> getList() {
                return list;
            }

            public void setList(List<ResourceItem> list) {
                this.list = list;
            }

        }
    }
}
