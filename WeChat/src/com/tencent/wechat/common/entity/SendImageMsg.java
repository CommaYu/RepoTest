package com.tencent.wechat.common.entity;

/**
 * 发送图片信息实体类
 *
 * @author lusain
 */

public class SendImageMsg {
    /**
     * 发送消息类型
     */
    private int Type;
    /**
     * 媒体ID
     */
    private String MediaId;

    private String FromUserName;

    private String ToUserName;

    private String LocalID;

    private String ClientMsgId;

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
    }

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        ToUserName = toUserName;
    }

    public String getLocalID() {
        return LocalID;
    }

    public void setLocalID(String localID) {
        LocalID = localID;
    }

    public String getClientMsgId() {
        return ClientMsgId;
    }

    public void setClientMsgId(String clientMsgId) {
        ClientMsgId = clientMsgId;
    }


}
