package com.tencent.wechat.common.entity;

public class UserInfoVO {
    //用户头像地址
    private String headImgUrl;
    //昵称
    private String nickName;
    //用户ID
    private String UserName;
    //性别-0未知，1男性，2女性
    private Integer sex;

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }


}
