package com.tencent.wechat.ui.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.MessageReceiveEvent;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.entity.UnReadMsgVo;
import com.tencent.wechat.common.utils.AppUtils;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.RegexUtils;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeIntentResponse;
import com.tencent.wechat.ipc.BridgeResponseVo;
import com.tencent.wechat.ipc.PoiInfoVo;
import com.tencent.wechat.listener.IOnKeyDownListener;
import com.tencent.wechat.manager.AutomationTTS;
import com.tencent.wechat.manager.CustomMvwManager;
import com.tencent.wechat.manager.FloatWindowManager;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.manager.SendLocationManager;
import com.tencent.wechat.manager.SpeakMessageManager;
import com.tencent.wechat.ui.adapter.MessagelistviewAdapter;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 会话界面，展示消息列表，且 收到新消息时会自动播报<br>
 * Note:<br>
 */

public class SessionActivity extends BaseActivity implements
        View.OnClickListener, AutomationTTS.AutomationTTSListenr {

    private static final String TAG = "SessionActivity";

    private final HttpWeChat mWechat = WeChatMain.getWeChatMain();
    private List<ReceiveMsgVO> mChatMsgs;
    private MessagelistviewAdapter mMsgAdapter;

    private TextView contactNameTv;
    private ImageView notifyView;
    private ImageButton sendLocIBtn;
    private ImageButton sendMsgIBtn;
    private ImageButton backIBtn;
    private ListView msgListView;

    /* 是否处于可交互状态，onResume置为true;onPause置为false */
    boolean isActive = false;

    /* 我自己的信息 */
    private FriendVo mOwnInfo;

    /* 该会话界面好友的信息 */
    private FriendVo contactUserInfo;

    private String sendName;
    private TextView tv_unread_num;
    private IOnKeyDownListener mIOnKeyDownListener;
    public int scrollStates;
    private int unReadNum = 0;

    /* 收到的消息 */
    private LinkedBlockingQueue<ReceiveMsgVO> autoMsgQueue;
    private AutomationTTS mAutomationTTS;

    /* 最近一次点击按钮的时间戳 */
    private long last_click_time = 0;

    /* 防抖时间。防止连续点击界面发送消息的按钮 */
    private final int OnWait_Time = 500;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_session);
        initView();
        handleIntent();

        AppUtils.hideSoftInput(this);
        EventBus.getDefault().register(this);

        autoMsgQueue = new LinkedBlockingQueue<ReceiveMsgVO>();
        mAutomationTTS = new AutomationTTS(autoMsgQueue, this);
        CustomMvwManager.getInstance().setCustomMvwListener(
                mCustomMvwManagerListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
        setIntent(intent);
        handleIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onresume");
        FloatWindowManager.getInstance().hideFloatWindow();
        isActive = true;
        SpeakMessageManager.getInstance().lockTts();
        isActive = true;
        MessageManager.getInstance().setCurrentContact(
                contactUserInfo.getUserName());

        ConcurrentMap<String, UnReadMsgVo> mUnReadMsgMap = MessageManager
                .getInstance().getmUnReadMsgMap();
        UnReadMsgVo unReadMsgVo = mUnReadMsgMap.get(contactUserInfo
                .getUserName());
        if (unReadMsgVo != null && unReadMsgVo.getUnReadMsg().size() > 0) {
            List<ReceiveMsgVO> unReadList = unReadMsgVo.getUnReadMsg();
            for (ReceiveMsgVO msg : unReadList) {
                mAutomationTTS.addMsg(msg);
            }
            int index = mChatMsgs.indexOf(unReadMsgVo.getUnReadMsg().get(0));
            msgListView.setSelection(index);
        } else {
            msgListView.setSelection(mChatMsgs.size() - 1);
        }

        MessageManager.getInstance().readMsg(contactUserInfo.getUserName());
        MessageManager.getInstance().modifyFriendNewMsg(contactUserInfo, false);
        CustomMvwManager.getInstance().startWakeup_session();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        isActive = false;
        MessageManager.getInstance().setCurrentContact("");

        // 认为自动播报被打断，清空自动播报队列
        autoMsgQueue.clear();
        SpeakMessageManager.getInstance().stopPlay();
        SpeakMessageManager.getInstance().unLockTts();
        CustomMvwManager.getInstance().stopWakeup_session();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mAutomationTTS.doClearOperation();
        CustomMvwManager.getInstance().setCustomMvwListener(null);
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        backIBtn = (ImageButton) findViewById(R.id.back_ibtn);
        contactNameTv = (TextView) findViewById(R.id.contact_name);
        notifyView = (ImageView) findViewById(R.id.notify_flag);
        sendMsgIBtn = (ImageButton) findViewById(R.id.send_voice);
        sendLocIBtn = (ImageButton) findViewById(R.id.send_location);
        tv_unread_num = (TextView) findViewById(R.id.tv_unread_num);
        msgListView = (ListView) findViewById(R.id.message_listview);

        tv_unread_num.setVisibility(View.INVISIBLE);

        sendMsgIBtn.setOnClickListener(this);
        sendLocIBtn.setOnClickListener(this);
        backIBtn.setOnClickListener(this);
        msgListView.setOnScrollListener(new OnScrollListenerImpl());
    }

    private void updateView() {
        sendName = WeChatUtil.getFriendName(contactUserInfo);
        CharSequence nameSpannableString = ExpressionUtil.parseEmoji(
                WeChatApplication.getContext(), sendName);
        contactNameTv.setText(nameSpannableString);
        mMsgAdapter = new MessagelistviewAdapter(this, mChatMsgs);
        msgListView.setAdapter(mMsgAdapter);
    }

    private void handleIntent() {
        mOwnInfo = mWechat.getMyInfo().getData();

        Intent intent = this.getIntent();
        SessionInfo sessionInfo = null;
        if (intent != null) {
            sessionInfo = intent.getParcelableExtra("sessionInfo");
        }
        if (mOwnInfo == null || sessionInfo == null) {
            Log.e(TAG, "handleIntent: wrong params");
            finish();
            return;
        }

        contactUserInfo = mWechat.getAllFriendsMap().get(sessionInfo.user);

        if (null != MessageManager.getInstance().getFriendMsg(
                contactUserInfo.getUserName())) {
            mChatMsgs = MessageManager.getInstance().getFriendMsg(
                    contactUserInfo.getUserName());
        } else {
            mChatMsgs = new ArrayList<ReceiveMsgVO>();
        }
        updateView();
    }

    /* 监听自定义唤醒 发消息、发位置 */
    CustomMvwManager.CustomMvwManagerListener mCustomMvwManagerListener = new CustomMvwManager.CustomMvwManagerListener() {
        @Override
        public void onMessage() {
            sendMessage();
        }

        @Override
        public void onLocation() {
            sendLocation();
        }

        @Override
        public void onNavigate() {
            startNavi();
        }

        @Override
        public void onBack() {
            onBackPressed();
        }

    };

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        if (System.currentTimeMillis() - last_click_time < OnWait_Time) {
            Log.d(TAG, "onClick: frequent");
            last_click_time = System.currentTimeMillis();
            return;
        }
        Log.d(TAG, "onClick: normal");
        last_click_time = System.currentTimeMillis();

        switch (v.getId()) {
        case R.id.send_voice:
            sendMessage();
            break;
        case R.id.send_location:
            sendLocation();
            break;
        case R.id.back_ibtn:
            onBackPressed();
            break;
        }
    }

    public void setChatMsgs(List<ReceiveMsgVO> chatMsgs) {
        mChatMsgs = chatMsgs;
        mMsgAdapter.setChatMsgs(mChatMsgs);
        mMsgAdapter.notifyDataSetChanged();
    }

    /* 当收到新消息 */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MessageReceiveEvent event) {

        String uid = event.getWhat();
        ReceiveMsgVO msgVO = event.getObj();
        Log.d(TAG, " uid = " + uid);
        if (TextUtils.equals(uid, contactUserInfo.getUserName())) {
            setChatMsgs(MessageManager.getInstance().getFriendMsg(uid));

            if (isActive) {
                String userName = msgVO.getSenderName();
                if (!userName.equals(mOwnInfo.getUserName())) {
                    // 如果是好友发来的消息，则加入播报队列
                    mAutomationTTS.addMsg(msgVO);
                } else {
                    // 如果是自己发的消息，则滑到最下方
                    msgListView.setSelection(msgListView.getBottom());
                }
            }
        } else if (TextUtils.equals(uid, mOwnInfo.getUserName())) {
            Log.d(TAG, "=======>get message from self");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIOnKeyDownListener != null) {
            mIOnKeyDownListener.onKeyDown(keyCode);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setIOnKeyDownListener(IOnKeyDownListener listener) {
        this.mIOnKeyDownListener = listener;
    }

    /*
     * 自动播报队列的回调，即将有新消息要播报，将此消息条目滚动至最下面
     */
    @Override
    public void onPlay(ReceiveMsgVO currentMsg) {
        int index = mChatMsgs.indexOf(currentMsg);
        Log.d(TAG, "onStartPlay: index=" + index);
        if (index >= 0 && msgListView != null) {
            Log.d(TAG, "onPlay: scroll...");
            msgListView.smoothScrollToPosition(index);
        }
    }

    class OnScrollListenerImpl implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            scrollStates = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
        }
    }

    /* 语音“发送消息”和点击按钮都会触发此方法 */
    private void sendMessage() {
        if (isActive) {
            WeChatApplication.getBinder().sendVoiceMsg(
                    contactUserInfo.getUserName());
        }
    }

    /* 语音“发送位置”和点击按钮都会触发此方法 */
    private void sendLocation() {
        if (isActive) {
            BridgeResponseVo responseVo = BridgeIntentResponse.getInstance()
                    .getLocation();
            if (responseVo != null) {
                PoiInfoVo poi = responseVo.getPoi();
                SendLocationManager.getInstance().sendLocation(
                        contactUserInfo.getUserName(), poi);
            } else {
                Log.e(TAG, "sendLocation: responseVo is null");
            }
        }
    }

    private void startNavi() {
        if (null != mMsgAdapter) {
            List<ReceiveMsgVO> msgs = mMsgAdapter.getChatMsgsForLocation();
            if (null != msgs && msgs.size() > 0) {
                ReceiveMsgVO msg = msgs.get(msgs.size() - 1);
                if (null != msg) {
                    // 获取经纬度
                    Log.e(TAG, "开始导航");
                    String latLongtitude = RegexUtils.getLongLatitude(msg
                            .getUrl());
                    if (!TextUtils.isEmpty(latLongtitude)) {
                        String[] str = latLongtitude.split(",");
                        String longitude = str[1];
                        String latitude = str[0];
                        PoiInfoVo poi = new PoiInfoVo();
                        poi.setPoiname(msg.getContent());
                        poi.setLongitude(longitude);
                        poi.setLatitude(latitude);
                        poi.setCoord_type(BridgeContract.COORD_TYPE_GCJ); // 腾讯地图目前使用gcj02坐标系
                        BridgeIntentResponse.getInstance().requestForNavi(poi);
                    }
                }
            }
        }
    }

    /*
     * 当会话界面的一条文字或语音消息条目被点击时，回调此方法 1.清空自动播报队列 2.暂停播报 3.将该消息加入到自动播报队列
     * 
     * @param clickMsg 被点击的文本或语音消息
     */
    public void onMsgClick(ReceiveMsgVO clickMsg) {
        autoMsgQueue.clear();
        SpeakMessageManager.getInstance().stopPlay();
        mAutomationTTS.addMsg(clickMsg);
    }

    /**
     * 外部类启动 这个activity 必须调用此方法
     * 
     * @param context
     * @param sessionInfo
     *            启动此activity 必须传递的数据
     * @param needNewTask
     *            当传的context不是activity 时，需要传 true
     */
    public static void startMe(Context context, SessionInfo sessionInfo,
            boolean needNewTask) {
        Intent intent = new Intent();
        intent.setClass(context, SessionActivity.class);
        intent.putExtra("sessionInfo", sessionInfo);

        if (needNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

}
