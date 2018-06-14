package com.tencent.wechat.common;

public enum CacheInfo {

    /**
     * 全部消息
     */
    ALL_MESSAGE("ALL_MESSAGE");

    private String tag;

    private CacheInfo(String tag) {
        this.tag = tag;
    }

    public CacheInfo value(String tab) {
        for (CacheInfo t : CacheInfo.values()) {
            if (t.getTag().equals(tab)) {
                return t;
            }
        }
        return null;
    }

    public String getTag() {
        return tag;
    }

}
