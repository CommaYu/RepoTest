package com.tencent.wechat.common.entity;

public class UploadFriendVO {

    /**
     * 好友ID
     */
    private String userID;

    /**
     * 好友昵称
     */
    private String nickName;

    /**
     * 好友备注名称
     */
    private String remarkName;

    /**
     * 好友头像url
     */
    private String headImg;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }
}
