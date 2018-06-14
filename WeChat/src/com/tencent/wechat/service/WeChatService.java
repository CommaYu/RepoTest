package com.tencent.wechat.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.AppManager;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.manager.WeChatManager;
import com.tencent.wechat.ui.activity.VoiceRecordActivity;

public class WeChatService extends Service {
	private static final String TAG = "WeChatService";

	private IBinder mBinder = new ILocalBinderImpl();

	private WeChatManager mWeChatManager;

	/* 微信登录状态，默认未登录 */
	private LoginState mLoginState = LoginState.noLogin;

	public enum LoginState {
		/**
		 * 没有登录
		 */
		noLogin,
		/**
		 * 已经登录
		 */
		login,
		/**
		 * 正在登录中(从扫码到好友加载完毕这一阶段)
		 */
		logining
	}

	public class ILocalBinderImpl extends Binder implements ILocalBinder {

		@Override
		public LoginState getLoginState() {
			return mLoginState;
		}

		@Override
		public void setLoginState(LoginState state) {
			mLoginState = state;
		}

		public void getMessage() {
			try {
				int code = mWeChatManager.synKey();
				if ((code == 2 || code == 4 || code == 6 || code == 7)
						&& mLoginState == LoginState.login) {
					List<ReceiveMsgVO> addMsgList = mWeChatManager.getChatMsg();
					if (addMsgList != null && !addMsgList.isEmpty()) {// 有新消息
						addMessage(addMsgList, false);
					}
				} else if (code == 1100) {
					Log.d(TAG, "getMessage: code == 1100,logout ...");
					WeChatApplication.getBinder().setLoginState(
							WeChatService.LoginState.noLogin);
					mWeChatManager.startLoginActivity();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		public void addMessage(List<ReceiveMsgVO> msgList, boolean isSendMsg) {
			MessageManager.getInstance().addMessage(msgList, isSendMsg);
		}

		public void addMessage(ReceiveMsgVO msg, boolean isSendMsg) {
			List<ReceiveMsgVO> msgList = new ArrayList<ReceiveMsgVO>();
			msgList.add(msg);
			addMessage(msgList, isSendMsg);
		}

		public void sendMessage(ReceiveMsgVO msg) {
			mWeChatManager.sendMessage(msg);
		}

		public void sendVoiceMsg(String receiverUid) {
			if (TextUtils.isEmpty(receiverUid)) {
				Log.e(TAG, "sendVoiceMsg() receiverUid error!!!!");
				return;
			}

			if (mLoginState == LoginState.login) {
				Intent intent = new Intent(AppManager.getInstance()
						.currentActivity(), VoiceRecordActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("user", receiverUid);
				startActivity(intent);
			}
		}

		public void logout() {
			mWeChatManager.logout();
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: ");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mWeChatManager = WeChatManager.getInstance();

		// 把该service创建为前台service
		Notification notification = new Notification.Builder(
				WeChatApplication.getContext()).build();
		startForeground(1, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand: ");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}