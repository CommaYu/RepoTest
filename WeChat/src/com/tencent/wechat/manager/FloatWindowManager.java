package com.tencent.wechat.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.sdk.interfaces.ISvwUiListener;
import com.iflytek.sdk.manager.FlySvwManager;
import com.iflytek.utils.log.Logging;
import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.activity.VoiceRecordActivity;

/**
 * Author: congqin<br>
 * Data:2016/12/17.<br>
 * Description: 收到新消息时，负责顶部悬浮框的显示与隐藏<br>
 * Note:<br>
 */
public class FloatWindowManager {

	private static final String TAG = "FloatWindowManager";

	private static FloatWindowManager mInstance;

	private Context mContext;

	private boolean isShow = false;

	private LayoutInflater inflater;

	private WindowManager mWindowManager;

	private WindowManager.LayoutParams wmParams;

	private View contentView;
	private View speechMicLinearLayout;
	private TextView rawtextview;
	private ImageView image;

	private View.OnTouchListener touchListener;

	private FloatWindowManager(Context context) {
		mContext = context;
		inflater = LayoutInflater.from(context);
		mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		createView();
		initParams();
	}

	public static FloatWindowManager getInstance() {
		if (mInstance == null) {
			mInstance = new FloatWindowManager(WeChatApplication.getContext());
		}
		return mInstance;
	}

	private void createView() {
		contentView = inflater.inflate(R.layout.notify_front_layout, null);
		speechMicLinearLayout = contentView.findViewById(R.id.notify_front_layout);
		rawtextview = (TextView) contentView.findViewById(R.id.notify_layout_content_textview);
		image = (ImageView) contentView.findViewById(R.id.notify_layout_head_imageview);

		contentView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (touchListener != null) {
					touchListener.onTouch(view, motionEvent);
				}
				return true;
			}
		});
	}

	private void initParams() {
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = getDisplaysMetrics()[1];
		wmParams.height = 100;
	}

	/**
	 * 显示悬浮框
	 *
	 * @param msg
	 *            收到的消息实体
	 * @param contentText
	 *            需要显示在悬浮框的文本
	 */
	public void showFloatWindow(ReceiveMsgVO msg, CharSequence contentText) {
		if (isShow) {
			hideFloatWindow();
		}
		isShow = true;
		updateView(msg, contentText);
		mWindowManager.addView(contentView, wmParams);
	}

	private void updateView(ReceiveMsgVO msg, CharSequence contentText) {
		String fromUserName = msg.getFromUserName();
		FriendVo sender = WeChatMain.getWeChatMain().getAllFriendsMap().get(fromUserName);
		WeChatApplication.getImageLoader().displayImage(sender.getHeadImgUrl(), image);
		rawtextview.setText(contentText);
	}

	/**
	 * 隐藏悬浮框
	 */
	public void hideFloatWindow() {
		if (isShow) {
			isShow = false;
			mWindowManager.removeViewImmediate(contentView);
		}
	}

	/* 返回屏幕的长和宽 */
	private int[] getDisplaysMetrics() {
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(displaysMetrics);
		int metrics[] = new int[] { displaysMetrics.heightPixels, displaysMetrics.widthPixels };
		Log.d(TAG, "height =" + displaysMetrics.heightPixels);
		Log.d(TAG, "width =" + displaysMetrics.widthPixels);

		return metrics;
	}

	/**
	 * 设置触摸监听器，当触摸悬浮框进入会话界面，当触摸悬浮框外部，隐藏悬浮框
	 *
	 * @param l
	 */
	public void setTouchListener(View.OnTouchListener l) {
		touchListener = l;
	}
}
