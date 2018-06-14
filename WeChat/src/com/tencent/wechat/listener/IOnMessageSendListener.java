package com.tencent.wechat.listener;

import com.tencent.wechat.http.entity.ReceiveMsgVO;

public interface IOnMessageSendListener {
    public void onMessageSend(ReceiveMsgVO msg, boolean isSuccess);
}
