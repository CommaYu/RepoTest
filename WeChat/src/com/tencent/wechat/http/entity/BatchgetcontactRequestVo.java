package com.tencent.wechat.http.entity;

import java.io.Serializable;

public class BatchgetcontactRequestVo implements Serializable {

    private static final long serialVersionUID = -8610896037467031934L;

    private String UserName;

    private String EncryChatRoomId;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getEncryChatRoomId() {
        return EncryChatRoomId;
    }

    public void setEncryChatRoomId(String encryChatRoomId) {
        EncryChatRoomId = encryChatRoomId;
    }

}
