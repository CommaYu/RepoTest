package com.tencent.wechat.manager;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.AppManager;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.listener.IOnLogoutListener;
import com.tencent.wechat.listener.IOnMessageSendListener;
import com.tencent.wechat.ui.activity.LoginActivity;
import com.tencent.wechat.ui.widget.CustomToast;

/**
 * Author: congqin <br>
 * Data: <br>
 * Description: 管理消息的接收和发送 <br>
 * Note: 内部封装了 wechatMain , 供WechatService调用<br>
 */
public class WeChatManager {
	private static final String TAG = "WeChatManager";

	private static WeChatManager instance;

	private static HttpWeChat mWechat;

	/**
	 * 信息发送监听接口，处理信息发送的监听操作
	 */
	private IOnMessageSendListener mIOnMessageSendListener;

	/**
	 * 登出接口
	 */
	private IOnLogoutListener mIOnLogoutListener;

	public static WeChatManager getInstance() {
		if (instance == null) {
			instance = new WeChatManager();
			mWechat = WeChatApplication.getHttpWeChat();
		}
		return instance;
	}

	public void registerMessageSendListener(IOnMessageSendListener listener) {
		this.mIOnMessageSendListener = listener;
	}

	public void registerLogoutListener(IOnLogoutListener listener) {
		this.mIOnLogoutListener = listener;
	}

	/**
	 * 获取新消息
	 * 
	 * @return 新消息，可能为空
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<ReceiveMsgVO> getChatMsg() throws IOException, JSONException {
		return mWechat.getChatMsg();
	}

	/**
	 * 发送心跳包，判断服务器是否有新消息
	 * 
	 * @return 0 正常，无消息<br>
	 *         2 4 6 7 有新消息或新状态，需要获取消息
	 * @throws IOException
	 * @throws JSONException
	 */
	public int synKey() throws IOException, JSONException {
		return mWechat.synKey();
	}

	/**
	 * 开启子线程，发送文本信息
	 * 
	 * @param msg
	 *            被发送的信息实体
	 */
	public void sendMessage(final ReceiveMsgVO msg) {
		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {
			@Override
			public void run() {
				try {
					int code = mWechat.sendChatMsg(msg).getData();
					if (code == 0) { // 发送成功
										// 成功发送消息后，心跳包也会有反馈，selector=2，这时去获取消息，消息列表为空，只有synckey值。
						msg.setSendSuccess(true);
					} else {
						Log.e(TAG, "run: send message failed");
						CustomToast.showToast(
								WeChatApplication.getContext(),
								WeChatApplication.getContext().getString(
										R.string.tip_send_fail),
								Toast.LENGTH_LONG);
						msg.setSendSuccess(false);
					}
					WeChatApplication.getBinder().addMessage(msg, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0);
	}

	/**
	 * 开子线程发送网络请求，退出登录
	 */
	public void logout() {
		Log.d(TAG, "logout: ");
		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {
			@Override
			public void run() {
				try {
					mWechat.logOut();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0);
	}

	/**
	 * 启动登录界面
	 */
	public void startLoginActivity() {
		Intent intent = new Intent(WeChatApplication.getContext(),
				LoginActivity.class);
		Activity currentActivity = AppManager.getInstance().currentActivity();
		if (currentActivity != null) {
			AppManager.getInstance().currentActivity().startActivity(intent);
			AppManager.getInstance().finishWithOut(LoginActivity.class);
		}
	}

}
