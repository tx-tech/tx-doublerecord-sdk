package com.txt.sl.entity.bean;

import java.util.List;

/**
 * Created by JustinWjq
 *
 * @date 2020/6/17.
 * description：
 */
public  class OrderBean
{

    /**
     * name : 与投保人关系
     * key : relationship
     * options : [{"key":"0","name":"本人","children":[]},{"key":"1","name":"配偶","children":[]},{"key":"2","name":"父母","children":[]},{"key":"3","name":"子女","children":[]},{"key":"4","name":"其他","children":[]}]
     */

    private String name;
    private String key;
    private List<OptionsBean> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<OptionsBean> getOptions() {
        return options;
    }

    public void setOptions(List<OptionsBean> options) {
        this.options = options;
    }

    public static class OptionsBean {
        /**
         * key : 0
         * name : 本人
         * children : []
         */

        private String key;
        private String name;
        private List<?> children;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<?> getChildren() {
            return children;
        }

        public void setChildren(List<?> children) {
            this.children = children;
        }
    }
}
