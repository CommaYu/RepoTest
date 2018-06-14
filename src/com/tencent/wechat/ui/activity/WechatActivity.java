package com.tencent.wechat.ui.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.AppUtils;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.listener.IOnKeyDownListener;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.service.ILocalBinder;
import com.tencent.wechat.service.WeChatService;
import com.tencent.wechat.ui.fragment.HomeFragment;

public class WechatActivity extends BaseActivity {

	private static final String TAG = WechatActivity.class.getSimpleName();

	private ILocalBinder mBinder;

	private boolean mIsLogin = true;

	private IOnKeyDownListener mIOnKeyDownListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: ");
		setContentView(R.layout.activity_main);
		mBinder = WeChatApplication.getBinder();
		loadContacts();
		replaceContent(createFragment(HomeFragment.class.getName()),
				R.id.main_content_layout, "HomeFragment");

		// permissionThread.start();
		//
		// Handler permissionHandler = new
		// Handler(permissionThread.getLooper()){
		//
		// public void handleMessage(android.os.Message msg) {
		// PermissionsUtil.checkAndRequestPermissions(MainActivity.this);
		// askForPermission();
		// }
		// };
		//
		// permissionHandler.sendEmptyMessage(2000);

	}

	private HandlerThread permissionThread = new HandlerThread("wechat");

	private void loadContacts() {
		List<FriendVo> contacts = WeChatApplication.getHttpWeChat()
				.getLateConnectUser().getData();
		MessageManager.getInstance().addContact(contacts);
		if (null != WeChatApplication.getHttpWeChat().getMyInfo().getData()) {
			MessageManager.getInstance().setMyUserName(
					WeChatApplication.getHttpWeChat().getMyInfo().getData()
							.getUserName());
		} else {
			Log.d(TAG, "loadContacts() minfo is null");
		}
	}

	public void getMessage() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!isFinishing()
						&& mBinder.getLoginState() == WeChatService.LoginState.login) {
					mBinder.getMessage();
					Log.d(TAG, "run: getMessage...");
				}
				Log.d(TAG, "run: isFinishing()=" + isFinishing() + "|mIsLogin="
						+ mIsLogin);
			}
		}).start();
	}

	/**
	 * 加载图片
	 * 
	 * @param uri
	 *            地址
	 * @param imageView
	 *            ImageView
	 */
	public void loadImage(String uri, ImageView imageView) {
		WeChatApplication.loadImage(uri, imageView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause: ");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop: ");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy: ");
		WeChatApplication.getImageLoader().clearMemoryCache();
		WeChatApplication.getImageLoader().clearDiskCache();
		MessageManager.getInstance().clear();
		if (null != mBinder) {
			mBinder.setLoginState(WeChatService.LoginState.noLogin);
		}
		// 删除/sdcard/wechat下的所有文件夹，wechat不删
		WeChatUtil.deleteAllCache(WeChatUtil.getRootPath());
		mBinder = null;
	}

	/**
	 * 点空白处隐藏软键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		View view = this.getCurrentFocus();
		if (view != null) {
			AppUtils.hideSoftInput(view);// 隐藏软键盘
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume: ");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);
			return true;
		} else if (mIOnKeyDownListener != null) {
			mIOnKeyDownListener.onKeyDown(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent: ");
		String userName = intent.getStringExtra("USER_NAME_USER");
		ReceiveMsgVO currentMsg = (ReceiveMsgVO) intent
				.getSerializableExtra("CURRENT_MESSAGE");
		Intent intent1 = new Intent(this, SessionActivity.class);
		Bundle bundle = new Bundle();
		FriendVo friendVo = WeChatApplication.getHttpWeChat()
				.getAllFriendsMap().get(userName);
		if (friendVo == null) {
			return;
		}
		bundle.putSerializable("user", friendVo);
		bundle.putSerializable("CURRENT_MESSAGE", currentMsg);
		intent1.putExtras(bundle);
		MessageManager.getInstance().modifyFriendNewMsg(friendVo, false);
		startActivity(intent1);
		super.onNewIntent(intent);
	}

	public void setIOnKeyDownListener(IOnKeyDownListener listener) {
		this.mIOnKeyDownListener = listener;
	}

	// /**
	// * 请求用户给予悬浮窗的权限
	// */
	//
	// @TargetApi(23)
	// public void askForPermission() {
	// if (!Settings.canDrawOverlays(this)) {
	// Intent intent = new Intent(
	// Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
	// Uri.parse("package:" + getPackageName()));
	// startActivity(intent);
	// }
	// }

}
