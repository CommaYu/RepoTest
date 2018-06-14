package com.tencent.wechat.common.entity;

import java.io.Serializable;

public class ResponseMediaFile implements Serializable {
    private static final long serialVersionUID = -4678573581003052817L;
    private int code;
    private String msg;
    private BaseData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BaseData getData() {
        return data;
    }

    public void setData(BaseData data) {
        this.data = data;
    }

    private class BaseData implements Serializable {
        private static final long serialVersionUID = -4678573581003052817L;
        private String url;
    }
}
