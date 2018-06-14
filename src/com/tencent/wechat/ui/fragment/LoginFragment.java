package com.tencent.wechat.ui.fragment;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.json.JSONException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.entity.ScanVo;
import com.tencent.wechat.common.utils.AppManager;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.manager.NetworkManager;
import com.tencent.wechat.service.WeChatService;
import com.tencent.wechat.ui.activity.LoginActivity;
import com.tencent.wechat.ui.activity.WechatActivity;

public class LoginFragment extends Fragment implements
		NetworkManager.NetworkListener, ImageLoadingListener {

	private static final String TAG = "LoginFragment";

	private ImageView loginIV; // 显示二维码和头像的imageview
	private TextView tipTV; // 显示提示语

	// 网络请求工具类
	private final HttpWeChat mWechat = WeChatMain.getWeChatMain();

	/* 主线程handler 操作界面ui */
	private UiHandler mUiHandler;

	/* 子线程handler 循环获取登录状态 */
	private LoginHandler mLoginHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUiHandler = new UiHandler(this);
		HandlerThread loginThread = new HandlerThread("login thread");
		loginThread.start();
		mLoginHandler = new LoginHandler(loginThread.getLooper(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_login, container, false);
		loginIV = (ImageView) view.findViewById(R.id.login_imageview);
		tipTV = (TextView) view.findViewById(R.id.tips_textview);

		if (NetworkManager.getInstance().isNetworkAvailable()) {
			mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_QVODE)
					.sendToTarget();
			tipTV.setText(R.string.tip_loading_qvode);
		} else {
			tipTV.setText(R.string.tip_network_disconnect);
		}
		NetworkManager.getInstance().registerListener(this);
		return view;
	}

	@Override
	public void onNetworkAvailable() {
		mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_QVODE).sendToTarget();
		tipTV.setText(R.string.tip_loading_qvode);
	}

	@Override
	public void onNetworkUnAvailable() {
		tipTV.setText(R.string.tip_network_disconnect);
	}

	/**
	 * 获取登录状态 0 成功 1 uid 为空 2 扫描成功，但未点确认 4 二维码过期重新扫描
	 * 
	 * @throws IOException
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	private void getLoginState() throws IOException, JSONException,
			InterruptedException {
		if (getActivity() != null && !getActivity().isFinishing()) {
			ScanVo state = mWechat.getLoginState();
			switch (state.getCode()) {
			case Constant.LOGIN_STATE_SUCCESS:// 登录成功
				mUiHandler.obtainMessage(MSG_UIHANDLER_SHOW_MAINACTIVITY)
						.sendToTarget();
				break;
			case Constant.LOGIN_STATE_SCANNED:// 扫描成功，但未点确认，可以获取到用户头像
				String conteString = state.getContent();
				byte[] head = Base64.decode(conteString, Base64.DEFAULT);
				mUiHandler.obtainMessage(MSG_UIHANDLER_SHOW_HEAD_IMAGE, head)
						.sendToTarget();
				mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_LOGIN_STATE)
						.sendToTarget();
				WeChatApplication.getBinder().setLoginState(
						WeChatService.LoginState.logining);
				break;
			case Constant.LOGIN_STATE_QVODE_INVALID:
				mUiHandler.obtainMessage(MSG_UIHANDLER_SHOW_INVALID)
						.sendToTarget();
				WeChatApplication.getBinder().setLoginState(
						WeChatService.LoginState.noLogin);
				mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_QVODE)
						.sendToTarget();
				break;
			case Constant.LOGIN_STATE_NOT_SCAN:
				mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_LOGIN_STATE)
						.sendToTarget();
				break;
			default:
				Log.e(TAG, "getLoginState: scanvo.getcode = " + state.getCode());
			}
		}
	}

	private static final int MSG_LOGINHANDLER_GET_QVODE = 0;
	private static final int MSG_LOGINHANDLER_GET_LOGIN_STATE = 1;

	static class LoginHandler extends Handler {

		private WeakReference<LoginFragment> mReference;

		public LoginHandler(Looper looper, LoginFragment fragment) {
			super(looper);
			this.mReference = new WeakReference<LoginFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LoginFragment loginFragment = mReference.get();
			if (loginFragment == null) {
				Log.e(TAG, "handleMessage: loginFragment is null");
				return;
			}
			switch (msg.what) {
			case MSG_LOGINHANDLER_GET_QVODE:
				try {
					String qvode = loginFragment.mWechat.getQvode();
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_QVODE, qvode).sendToTarget();
				} catch (IOException e) {
					e.printStackTrace();
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
				} catch (InterruptedException e) {
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
					e.printStackTrace();
				}
				break;

			case MSG_LOGINHANDLER_GET_LOGIN_STATE:
				try {
					loginFragment.getLoginState();
				} catch (IOException e) {
					e.printStackTrace();
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
				} catch (JSONException e) {
					e.printStackTrace();
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
				} catch (InterruptedException e) {
					e.printStackTrace();
					loginFragment.mUiHandler.obtainMessage(
							MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
				}
				break;
			}
		}
	}

	private static final int MSG_UIHANDLER_SHOW_ERROR = 100;
	private static final int MSG_UIHANDLER_SHOW_QVODE = 101; // 显示二维码
	private static final int MSG_UIHANDLER_SHOW_MAINACTIVITY = 102;
	private static final int MSG_UIHANDLER_SHOW_HEAD_IMAGE = 103; // 显示用户头像
	private static final int MSG_UIHANDLER_SHOW_INVALID = 104;

	static class UiHandler extends Handler {

		private WeakReference<LoginFragment> mReference;

		public UiHandler(LoginFragment fragment) {
			this.mReference = new WeakReference<LoginFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			LoginFragment loginFragment = mReference.get();
			LoginActivity activity = (LoginActivity) loginFragment
					.getActivity();
			if (activity == null) {
				return;
			}
			switch (msg.what) {
			case MSG_UIHANDLER_SHOW_ERROR:
				loginFragment.loginIV.setVisibility(View.INVISIBLE);
				loginFragment.tipTV.setText(R.string.tip_net_error);
				break;
			case MSG_UIHANDLER_SHOW_QVODE:
				loginFragment.loginIV.setVisibility(View.VISIBLE);
				ImageLoader imageLoader = WeChatApplication.getImageLoader();
				imageLoader.displayImage((String) msg.obj,
						loginFragment.loginIV, loginFragment);
				break;
			case MSG_UIHANDLER_SHOW_MAINACTIVITY:
				Log.d(TAG, "handleMessage: login success");
				activity.startActivity(new Intent(activity, WechatActivity.class));
				AppManager.getInstance().finishWithOut(WechatActivity.class);
				break;
			case MSG_UIHANDLER_SHOW_HEAD_IMAGE:
				loginFragment.loginIV.setVisibility(View.VISIBLE);
				byte[] bbyte1 = (byte[]) msg.obj;
				loginFragment.loginIV.setImageBitmap(WeChatUtil
						.bytes2Bimap(bbyte1));
				break;
			case MSG_UIHANDLER_SHOW_INVALID:
				loginFragment.loginIV.setVisibility(View.INVISIBLE); //
				loginFragment.tipTV.setText(R.string.tip_loading_qvode);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onLoadingStarted(String s, View view) {

	}

	@Override
	public void onLoadingFailed(String s, View view, FailReason failReason) {
		mUiHandler.obtainMessage(MSG_UIHANDLER_SHOW_ERROR).sendToTarget();
		Log.e(TAG,
				"onLoadingFailed: s=" + s + "|FailReason="
						+ failReason.toString());
	}

	@Override
	public void onLoadingComplete(String s, View view, Bitmap bitmap) {
		tipTV.setText(R.string.tip_scan_to_login);
		mLoginHandler.obtainMessage(MSG_LOGINHANDLER_GET_LOGIN_STATE)
				.sendToTarget();
	}

	@Override
	public void onLoadingCancelled(String s, View view) {

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		NetworkManager.getInstance().unRegisterListener(this);
	}
}
