package com.tencent.wechat.ui.fragment;

import java.io.IOException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Scene;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.iflytek.sdk.interfaces.ISvwUiListener;
import com.iflytek.sdk.manager.FlySvwManager;
import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.MessageReceiveEvent;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.utils.AppManager;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.manager.CustomMvwManager;
import com.tencent.wechat.manager.Dispatch;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.manager.NotifyManager;
import com.tencent.wechat.manager.SpeakMessageManager;
import com.tencent.wechat.manager.TTSManager;
import com.tencent.wechat.manager.WeChatManager;
import com.tencent.wechat.service.WeChatService;
import com.tencent.wechat.ui.activity.LogOutActivity;
import com.tencent.wechat.ui.activity.WechatActivity;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.activity.VoiceRecordActivity;
import com.tencent.wechat.ui.widget.CircleImageView;

/**
 * Created by Administrator on 2016/7/4.
 */
public class HomeFragment extends Fragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

	private static final String TAG = "HomeFragment";

	/**
	 * HttpWeChat
	 */
	private final HttpWeChat mWechat = WeChatApplication.getHttpWeChat();
	/**
	 * 所依附Activity
	 */
	private WechatActivity mFragmentActivity;

	/**
	 * 会话/好友列表RadioGroup
	 */
	private RadioGroup mRadioGroup;

	/**
	 * 用户头像Imageview
	 */
	private CircleImageView mUserHeadImageview;

	/**
	 * 当前用户信息
	 */
	private FriendVo mUserInfo;

	private View mNewMsgIcon;

	private ProgressDialog mProgressDialog;

	/* 最大重试数 */
	private static final int MAX_RETRY_TIME = 5;

	/* 重试 */
	private int retryTime = 0;

	private final String MVW_SCENE = "weichat_main";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mFragmentActivity = (WechatActivity) getActivity();
		View view = inflater.inflate(R.layout.fragment_home, container, false);
		setupViews(view);
		setupListener();
		loadData();
		loadAllFriend();
		EventBus.getDefault().register(this);
		return view;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
//		FlySvwManager.getInstance().start(MVW_SCENE, "取消屏蔽,屏蔽消息,回复微信,查看,静音", mMvwListener);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		FlySvwManager.getInstance().stop(MVW_SCENE);
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		FlySvwManager.getInstance().stop(MVW_SCENE);
	}

	private ISvwUiListener mMvwListener = new ISvwUiListener() {

		@Override
		public void onError(int error) {
			Log.d(TAG, "ISvwUiListener onError: " + error);
		}

		@Override
		public void onCusWakeup(String scene, int nMvwId, String result, int score) {
			Log.d(TAG, "scene : " + scene +"result:" + result);
			if (MVW_SCENE.equals(scene)) {
				ReceiveMsgVO msgVO = NotifyManager.getInstance().getCurrentMsg();
				if (null == msgVO) {
					return;
				}
				String fromUserName =  msgVO.getFromUserName();
				if (null == fromUserName) {
					return;
				}
				if ("取消屏蔽".equals(result)) {
					NotifyManager.getInstance().clearShieldUse();
					TTSManager.getInstance().startSpeak("已为您"+result);
				}
				if ("屏蔽消息".equals(result)) {
					NotifyManager.getInstance().addShieldUse(msgVO);
					TTSManager.getInstance().startSpeak("已为您"+result);
				}
				if ("回复微信".equals(result)) {
					Intent intent = new Intent(WeChatApplication.getContext(), VoiceRecordActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("user", fromUserName);
					startActivity(intent);
				}
				if ("查看".equals(result)) {
					SessionActivity.startMe(WeChatApplication.getContext(), new SessionInfo(fromUserName), true);
				}
				if ("静音".equals(result)) {
					NotifyManager.getInstance().setIsMute(true);
					TTSManager.getInstance().startSpeak("已为您"+result);
				}
				if ("取消静音".equals(result)) {
					NotifyManager.getInstance().setIsMute(false);
					TTSManager.getInstance().startSpeak("已为您"+result);
				}
			}
		}
	};

	private Runnable r = new Runnable() {
		@Override
		public void run() {
			if (isValidContext(mFragmentActivity) && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			mFragmentActivity.replaceContent(mFragmentActivity.createFragment(HomeMessageFragment.class.getName()),
					R.id.home_fragment_right_layout, "HomeMessageFragment");

			WeChatApplication.getBinder().setLoginState(WeChatService.LoginState.login);

			mFragmentActivity.getMessage();

		}
	};
	
	@SuppressLint("NewApi")
	private boolean isValidContext (Context c){  
        
        Activity a = (Activity)c;  
          
        if (a.isDestroyed() || a.isFinishing()){  
            Log.i(TAG, "Activity is invalid." + " isDestoryed-->" + a.isDestroyed() + " isFinishing-->" + a.isFinishing());  
            return false;  
        }else{  
            return true;  
        }  
    }  

	private void loadAllFriend() {
		Log.d(TAG, "loadAllFriend: ===");
		mProgressDialog.show();
		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {

			@Override
			public void run() {
				retryTime++;
				if (retryTime > MAX_RETRY_TIME) {
					WeChatManager.getInstance().startLoginActivity();
					return;
				}
				try {
					int code = mWechat.getAllFriend();
					if (code == 0) {
						retryTime = 0;
						Dispatch.getInstance().postDelayedByUIThread(r, 0);
					} else {
						Log.e(TAG, "run: getAllFriend error");
						Dispatch.getInstance().postRunnableByExecutors(this, 0);
					}
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}, 0);
	}

	/* 初始化布局中的组件 */
	private void setupViews(View view) {
		mUserHeadImageview = (CircleImageView) view.findViewById(R.id.user_heading_imageview);
		mRadioGroup = (RadioGroup) view.findViewById(R.id.message_friends_radiogroup);
		mNewMsgIcon = view.findViewById(R.id.new_msg_icon);
		mProgressDialog = new ProgressDialog(mFragmentActivity);
		mProgressDialog.setCanceledOnTouchOutside(false);
	}

	/* 设置组件的监听事件 */
	private void setupListener() {
		mRadioGroup.setOnCheckedChangeListener(this);
		mUserHeadImageview.setOnClickListener(this);
	}

	/* 加载数据 */
	private void loadData() {
		mUserInfo = mWechat.getMyInfo().getData();
		// 显示用户头像
		WeChatApplication.loadImage(mUserInfo.getHeadImgUrl(), mUserHeadImageview);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.home_message_radiobutton:
			mFragmentActivity.replaceContent(mFragmentActivity.createFragment(HomeMessageFragment.class.getName()),
					R.id.home_fragment_right_layout, "HomeMessageFragment");
			break;
		case R.id.home_friends_radiobutton:
			mFragmentActivity.replaceContent(mFragmentActivity.createFragment(HomeFriendsFragment.class.getName()),
					R.id.home_fragment_right_layout, "HomeFriendsFragment");
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_heading_imageview:
			Intent intent = new Intent(mFragmentActivity, LogOutActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(MessageReceiveEvent event) {
		if (MessageManager.getInstance().getmUnReadMsgMap().isEmpty()) {
			mNewMsgIcon.setVisibility(View.INVISIBLE);
		} else {
			mNewMsgIcon.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
		EventBus.getDefault().unregister(this);
		FlySvwManager.getInstance().stop(MVW_SCENE);
	}
}
