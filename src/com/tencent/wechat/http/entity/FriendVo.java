package com.tencent.wechat.http.entity;

import com.tencent.wechat.common.entity.LastMessageVO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 好友VO
 *
 * @author hdzhang
 */
public class FriendVo implements Serializable {
    private static final long serialVersionUID = 1119691250010584799L;

    // 用户ID
    private String UserName;

    // 昵称
    private String NickName;

    // 头像地址
    private String HeadImgUrl;

    // 成员数量
    private Integer MemberCount;

    /**
     * 群在我们看来就是一个好友，不同的是群有以下  成员列表  字段，而普通好友没有
     */
    private List<FriendVo> MemberList;

    // 备注名称
    private String RemarkName;
    // 性别 0未知，1男性，2女性
    private Integer Sex;
    // 个性签名
    private String Signature;
    // 拼音简拼
    private String PYInitial;
    // 拼音全拼
    private String PYQuanPin;
    // 备注拼音简拼
    private String RemarkPYInitial;
    // 备注拼音全拼
    private String RemarkPYQuanPin;
    // 省份
    private String Province;
    // 城市
    private String City;
    private String EncryChatRoomId;

    /**
     * 群昵称,群成员在该群中显示的名字，如果该成员不是我们的好友，则只有displayname和nickname
     */
    private String DisplayName;

    //最后一条信息
    private LastMessageVO lastMessageVO;

    /**
     * 是否消息免打扰 Statues=0时，消息免打扰；Statues=1时，提示消息
     */
    private Integer Statues;

    private Integer ContactFlag;

    private Integer VerifyFlag;

    private String KeyWord;

    public FriendVo(){
        UserName = "";
        NickName = "";
        HeadImgUrl = "";
        MemberList = new ArrayList<FriendVo>();
    }

    public String getKeyWord() {
        return KeyWord;
    }

    public void setKeyWord(String keyWord) {
        KeyWord = keyWord;
    }

    public Integer getVerifyFlag() {
        return VerifyFlag;
    }

    public void setVerifyFlag(Integer verifyFlag) {
        VerifyFlag = verifyFlag;
    }

    public Integer getStatues() {
        return Statues;
    }

    public void setStatues(Integer statues) {
        Statues = statues;
    }

    public Integer getContactFlag() {
        return ContactFlag;
    }

    public void setContactFlag(Integer contactFlag) {
        ContactFlag = contactFlag;
    }

    public boolean isHasNewMsg() {
        return isHasNewMsg;
    }

    public void setHasNewMsg(boolean hasNewMsg) {
        isHasNewMsg = hasNewMsg;
    }

    private boolean isHasNewMsg;

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getNickName() {
        return NickName;
    }

    public void setNickName(String nickName) {
        NickName = nickName;
    }

    public String getHeadImgUrl() {
        return HeadImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        HeadImgUrl = headImgUrl;
    }

    public Integer getMemberCount() {
        return MemberCount;
    }

    public void setMemberCount(Integer memberCount) {
        MemberCount = memberCount;
    }

    public List<FriendVo> getMemberList() {
        return MemberList;
    }

    public void setMemberList(List<FriendVo> memberList) {
        MemberList = memberList;
    }

    public String getRemarkName() {
        return RemarkName;
    }

    public void setRemarkName(String remarkName) {
        RemarkName = remarkName;
    }

    public Integer getSex() {
        return Sex;
    }

    public void setSex(Integer sex) {
        Sex = sex;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    public String getPYInitial() {
        return PYInitial;
    }

    public void setPYInitial(String pYInitial) {
        PYInitial = pYInitial;
    }

    public String getPYQuanPin() {
        return PYQuanPin;
    }

    public void setPYQuanPin(String pYQuanPin) {
        PYQuanPin = pYQuanPin;
    }

    public String getRemarkPYInitial() {
        return RemarkPYInitial;
    }

    public void setRemarkPYInitial(String remarkPYInitial) {
        RemarkPYInitial = remarkPYInitial;
    }

    public String getRemarkPYQuanPin() {
        return RemarkPYQuanPin;
    }

    public void setRemarkPYQuanPin(String remarkPYQuanPin) {
        RemarkPYQuanPin = remarkPYQuanPin;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getEncryChatRoomId() {
        return EncryChatRoomId;
    }

    public void setEncryChatRoomId(String encryChatRoomId) {
        EncryChatRoomId = encryChatRoomId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof FriendVo)) {
            return false;
        }

        FriendVo other = (FriendVo) obj;
        if (!other.UserName.equals(UserName)) {
            return false;
        }
        return true;
    }

    public LastMessageVO getLastMessageVO() {
        return lastMessageVO;
    }

    public void setLastMessageVO(LastMessageVO lastMessageVO) {
        this.lastMessageVO = lastMessageVO;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

}
