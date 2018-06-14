package com.tencent.wechat.common.entity;

/**
 * Created by Administrator on 2016/6/27.
 */
public class WeChatSeviceReceiveEvent {

    private String msg;

    public WeChatSeviceReceiveEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
