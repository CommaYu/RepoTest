package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ui.activity.SessionActivity;

import java.util.List;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public abstract class BaseHolder {

    protected ReceiveMsgVO msg;

    protected boolean showTimeFlag;

    protected View root;

    protected SessionActivity mContext;

    protected String sendName = "";

    protected FriendVo sendUser;

    protected boolean isGroup = false;

    protected WeChatMain mWechat = WeChatMain.getWeChatMain();

    public BaseHolder(Context context) {
        mContext = (SessionActivity) context;
        initView();
        root.setTag(this);
    }

    public void updateData(ReceiveMsgVO msg, boolean showTimeFlag) {
        this.msg = msg;
        this.showTimeFlag = showTimeFlag;
        dealMsg();
        update();
    }

    private void dealMsg() {
        String fromUser = msg.getFromUserName();
        sendUser = mWechat.getAllFriendsMap().get(msg.getSenderName());
        if (fromUser.startsWith("@@")) {
            isGroup = true;
            FriendVo groupFriend = mWechat.getAllFriendsMap().get(fromUser);
            List<FriendVo> members = groupFriend.getMemberList();
            if (members != null && !members.isEmpty()) {
                for (FriendVo vo : members) {
                    if (msg.getSenderName().equals(vo.getUserName())) {
                        sendUser = vo;
                    }
                }
            }
        }

        if (null != sendUser) {
            sendName = TextUtils.isEmpty(sendUser.getDisplayName()) ? (TextUtils.isEmpty(sendUser.getRemarkName()) ?
                    sendUser.getNickName() : sendUser.getRemarkName()) : sendUser.getDisplayName();
        } else {
            sendName = "";
        }
    }


    public View getView() {
        return root;
    }

    protected abstract void update();

    protected abstract void initView();
}
