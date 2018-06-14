package com.tencent.wechat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.listener.IOnKeyDownListener;
import com.tencent.wechat.ui.adapter.holder.BaseHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveEmotIconHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveImageHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveLocationHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveShareHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveSysHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveTextHolder;
import com.tencent.wechat.ui.adapter.holder.ReceiveVoiceHolder;
import com.tencent.wechat.ui.adapter.holder.SendLocationHolder;
import com.tencent.wechat.ui.adapter.holder.SendTextHolder;
import com.tencent.wechat.ui.adapter.holder.SendVoiceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 7/4 0004.
 */

public class MessagelistviewAdapter extends BaseAdapter implements IOnKeyDownListener {
    private static final String TAG = "MessagelistviewAdapter";
    private Context mContext;

    private final HttpWeChat mWechat = WeChatMain.getWeChatMain();
    private List<ReceiveMsgVO> mChatMsgs;
    private FriendVo mUserInfo;
    private LayoutInflater mLayoutInflater;
    private boolean isGroup = false;
    private List<ReceiveMsgVO> mChatMsgForSpeak = new ArrayList<ReceiveMsgVO>();
    private List<String> posions = new ArrayList<String>();
    private IOnKeyDownListener mIOnKeyDownListener;

    public MessagelistviewAdapter(Context context, List<ReceiveMsgVO> mChatMsgs) {
        this.mContext = context;
        this.mChatMsgs = mChatMsgs;
        this.mLayoutInflater = LayoutInflater.from(context);
        mUserInfo = mWechat.getMyInfo().getData();
        setIOnKeyDownListener(this);

    }

    public void setIOnKeyDownListener(IOnKeyDownListener listener) {
        this.mIOnKeyDownListener = listener;
    }

    @Override
    public int getCount() {
        return mChatMsgs.size();
    }

    public void setChatMsgs(List<ReceiveMsgVO> msgs) {
        this.mChatMsgs = msgs;
    }

    /**
     * 获取位置的消息
     *
     * @return
     */
    public List<ReceiveMsgVO> getChatMsgsForLocation() {
        List<ReceiveMsgVO> msgs = new ArrayList<ReceiveMsgVO>();
        if (null != mChatMsgs) {
            for (int i = 0; i < mChatMsgs.size(); i++) {
                ReceiveMsgVO msg = mChatMsgs.get(i);
                String userName = msg.getSenderName();
                int msgType = msg.getMsgType();
                if (!userName.equals(mUserInfo.getUserName())) {
                    // 接收的的消息
                    if (Constant.MSGTYPE_GET_LOCATION == msgType) {
                        msgs.add(msg);
                    }
                }
            }
        }
        return msgs;
    }

    @Override
    public Object getItem(int position) {
        return mChatMsgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ReceiveMsgVO msg = (ReceiveMsgVO) getItem(position);

        boolean showTimeFlag = false;
        if (position > 0) {
            final ReceiveMsgVO msg2 = (ReceiveMsgVO) getItem(position - 1);
            showTimeFlag = WeChatUtil.needShowTime(msg.getCreateTime(), msg2.getCreateTime());
        }

        if (convertView == null) {
            BaseHolder holder = getHolder(getItemViewType(position));
            holder.updateData(msg, showTimeFlag);
            convertView = holder.getView();
        } else {
            BaseHolder holder = (BaseHolder) convertView.getTag();
            holder.updateData(msg, showTimeFlag);
        }
        return convertView;
    }

    private BaseHolder getHolder(int itemViewType) {
        switch (itemViewType) {

            case Constant.MSGTYPE_TEXT:
                return new ReceiveTextHolder(mContext);

            case Constant.MSGTYPE_EMOTICON:
                // 接收动画表情
                return new ReceiveEmotIconHolder(mContext);

            case Constant.MSGTYPE_IMAGE:
                // 接收图片消息
                return new ReceiveImageHolder(mContext);

            case Constant.MSGTYPE_VOICE:
                // 接收声音消息
                return new ReceiveVoiceHolder(mContext);

            case Constant.MSGTYPE_SHARE:
                // 接收分享消息
                return new ReceiveShareHolder(mContext);

            case Constant.MSGTYPE_MICROVIDEO:
                // 接收微视频消息，用文本显示
//                return new ReceiveVideoHolder(mContext);
                return new ReceiveTextHolder(mContext);

            case Constant.MSGTYPE_VIDEO:
                // 接收视频消息
//                return new ReceiveVideoHolder(mContext);
                return new ReceiveTextHolder(mContext);

            case Constant.MSGTYPE_GET_LOCATION:
                return new ReceiveLocationHolder(mContext);

            case Constant.MSGTYPE_SYS:
                //接收系统消息
                return new ReceiveSysHolder(mContext);

            case -Constant.MSGTYPE_TEXT:
                return new SendTextHolder(mContext);

            case -Constant.MSGTYPE_VOICE:
                return new SendVoiceHolder(mContext);

            case -Constant.MSGTYPE_LOCATION:
                //发送地图位置
                return new SendLocationHolder(mContext);
        }
        return null;
    }

    private String initGroupInfo(String fromUser, FriendVo sendUser, ReceiveMsgVO msg) {
        String sendName = "";
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
            sendName = TextUtils.isEmpty(sendUser.getDisplayName()) ? (TextUtils.isEmpty(sendUser.getRemarkName()) ?
                    sendUser
                            .getNickName() : sendUser.getRemarkName())
                    : sendUser.getDisplayName();
        }
        return sendName;
    }


    /**
     * 对于收到的消息，item 类型就是消息类型，对于发送的消息， item类型是消息类型的负数
     * 如  MSGTYPE_TEXT=1，则对于收到的文本消息，item类型为1；对于发送的文本消息，item类型为 -1；
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        ReceiveMsgVO msg = (ReceiveMsgVO) getItem(position);
        String userName = msg.getSenderName();
        int msgType = msg.getMsgType();
        if (userName.equals(mUserInfo.getUserName())) {
            // 发送的消息
            return -msgType;
        } else {
            // 接收的消息
            return msgType;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 10002;
    }

    @Override
    public void onKeyDown(int keyCode) {

    }

}
