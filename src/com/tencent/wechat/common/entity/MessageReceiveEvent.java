package com.tencent.wechat.common.entity;

import com.tencent.wechat.http.entity.ReceiveMsgVO;

/**
 * Created by Administrator on 2016/6/27.
 */
public class MessageReceiveEvent {

    private String what;

    private ReceiveMsgVO obj;

    public MessageReceiveEvent(String what, ReceiveMsgVO obj) {
        this.what = what;
        this.obj = obj;
    }

    public String getWhat() {
        return what;
    }

    public ReceiveMsgVO getObj() {
        return obj;
    }
}
