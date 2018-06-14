package com.tencent.wechat.common.entity;


import com.tencent.wechat.http.entity.ReceiveMsgVO;

/**
 * 自动播报语音消息体
 *
 * @author li
 */
public class AutomationTTSModel {
    public int myId;
    public ReceiveMsgVO msgVO;

    public AutomationTTSModel(int myId, ReceiveMsgVO msgVO) {
        super();
        this.myId = myId;
        this.msgVO = msgVO;
    }
}
