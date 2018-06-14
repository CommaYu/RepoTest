package com.tencent.wechat.common.entity;

import java.io.Serializable;

public class LastMessageVO implements Serializable {
    private static final long serialVersionUID = 1119691250010584799L;
    // 信息内容
    private String content;

    private boolean isRead;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

}
