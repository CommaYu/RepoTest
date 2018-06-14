package com.tencent.wechat.common.entity;

import com.tencent.wechat.http.entity.ReceiveMsgVO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qxb-810 on 2016/12/11.
 */
public class UnReadMsgVo {

    private String mUserName;
    private String mRemarkName;
    private String mNickName;
    private String HeadImgUrl;
    private List<ReceiveMsgVO> unReadMsg;

    public int nUnReadNum = 0;

    public UnReadMsgVo() {
        
        unReadMsg = new ArrayList<ReceiveMsgVO>();
    }


    public List<ReceiveMsgVO> getUnReadMsg() {
        return unReadMsg;
    }

    public void setUnReadMsg(ReceiveMsgVO readMsg) {
        unReadMsg.add(readMsg);
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getmRemarkName() {
        return mRemarkName;
    }

    public void setmRemarkName(String mRemarkName) {
        this.mRemarkName = mRemarkName;
    }

    public String getmNickName() {
        return mNickName;
    }

    public void setmNickName(String mNickName) {
        this.mNickName = mNickName;
    }

    public int getnUnReadNum() {
        return nUnReadNum;
    }

    public void setnUnReadNum(int nUnReadNum) {
        this.nUnReadNum = nUnReadNum;
    }

    public String getHeadImgUrl() {
        return HeadImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        HeadImgUrl = headImgUrl;
    }

}
