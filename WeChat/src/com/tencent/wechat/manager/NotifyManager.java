package com.tencent.wechat.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.utils.AppUtils;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.RegexUtil;
import com.tencent.wechat.common.utils.SharedPreferencesUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.activity.VoiceRecordActivity;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 负责1.新消息是否需要显示及如何显示 逻辑判断 2.新消息的显示<br>
 * Note: 新消息的展示方式 1.悬浮框+叮咚声 2.悬浮框+语音播报<br>
 */

public class NotifyManager {

	private static final String TAG = "NotifyManager";

	/* 叮咚声提示默认3000ms 后关闭悬浮框 */
	private static final int DISMISS_DELAY_BEEP_DEFAULT = 3000;

	/* TTS提示完成后 默认1000ms 后关闭悬浮框 */
	private static final int DISMISS_DELAY_TTS_DEFAULT = 5*1000;

	/* 提示声，默认为tts */
	private int soundType = SOUND_TYPE_TTS;
	private static final int SOUND_TYPE_TTS = 0; // tts提示声
	private static final int SOUND_TYPE_TONE = 1; // 叮咚提示声
	private static final int SOUND_TYPE_NO = 2; // 无提示声
	
	private static final int MSG_SHOW_WINDOW = 100;
	private static final int MSG_HIDE_WINDOW = 200;
	
	private static Context mConetxt;

	private static NotifyManager mNotifyManager;

	/** 不进行播报的用户 **/
	private static List<String> shieldUse = new ArrayList<String>();
	
	/** 判断是否正在信息处理  **/
	private boolean isMute = false;
	
	/* 是否提示 */
	private boolean isNotify = true;

	private ReceiveMsgVO currentMsg;

	/* tts播报的文本 */
	private String ttsText;

	/* 悬浮框显示的文本 */
	private CharSequence showText;
	
	/** 判断是否正在信息处理  **/
	private boolean playingLock = false;

	public static NotifyManager getInstance() {
		if (null == mNotifyManager) {
			mNotifyManager = new NotifyManager();
			mConetxt = WeChatApplication.getContext();
		}
		return mNotifyManager;
	}

	/**
	 * 新消息提醒
	 * 
	 * @param weChatMsg
	 *            新消息
	 */
	public void showNotify(ReceiveMsgVO weChatMsg) {
		soundType = SOUND_TYPE_TTS;
		isNotify = true;
		howToNotify(weChatMsg);

		if (!isNotify) {
			return;
		}
		currentMsg = weChatMsg;
		FloatWindowManager.getInstance().setTouchListener(touchListener);
		// 清除上一次提示
		removeNotify();

		constructText();

		Message.obtain(uiHandler, MSG_SHOW_WINDOW).sendToTarget();

		if (soundType == SOUND_TYPE_TTS) {
			TTSManager.getInstance().setTtsManagerListener(mTtsManagerListener);
			AudioWrapperManager.getInstance().requestAudioFocus();
			AudioWrapperManager.getInstance().registerListener(
					mOnAudioFocusChangeListener);
			TTSManager.getInstance().startSpeak(ttsText);
		} else if (soundType == SOUND_TYPE_TONE) {
			soundRing();
			Message msg = Message.obtain(uiHandler, MSG_HIDE_WINDOW);
			uiHandler.sendMessageDelayed(msg, DISMISS_DELAY_BEEP_DEFAULT);
		} else {
			Log.d(TAG, "showNotify: soundType == SOUND_TYPE_TONE");
			Message msg = Message.obtain(uiHandler, MSG_HIDE_WINDOW);
			uiHandler.sendMessageDelayed(msg, DISMISS_DELAY_BEEP_DEFAULT);
		}
	}
	
	/**
	 * 清除消息提示，包括隐藏悬浮框，停止tts
	 */
	public void removeNotify() {
		stopTts();
		uiHandler.removeMessages(MSG_HIDE_WINDOW);
		Message.obtain(uiHandler, MSG_HIDE_WINDOW).sendToTarget();
		ttsText = null;
		showText = null;
	}
	
	/**
	 * 获取当前或是刚播完的tts的msg
	 * @return
	 */
	public ReceiveMsgVO getCurrentMsg() {
		return currentMsg;
	}

	private View.OnTouchListener touchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {

			if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
				removeNotify();
			}
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				startSessionActivity();
				removeNotify();
			}
			return false;
		}
	};

	/* 启动会话界面 */
	private void startSessionActivity() {
		String fromUserName = currentMsg.getFromUserName();
		FriendVo senderVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(fromUserName);
		if (senderVo == null) {
			Log.e(TAG, "startSessionActivity: senderVo is null");
			return;
		}
		SessionActivity.startMe(mConetxt, new SessionInfo(fromUserName), true);
	}

	private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS
					|| focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				removeNotify();
				AudioWrapperManager.getInstance().abandonAudioFocus();
			}
		}
	};

	private TTSManager.TtsManagerListener mTtsManagerListener = new TTSManager.TtsManagerListener() {
		@Override
		public void onTtsStart() {
			playingLock = true;
		}

		@Override
		public void onTtsComplete() {
			Message msg = Message.obtain(uiHandler, MSG_HIDE_WINDOW);
			uiHandler.sendMessageDelayed(msg, DISMISS_DELAY_TTS_DEFAULT);
			AudioWrapperManager.getInstance().abandonAudioFocus();
		}
	};

	/* 播放叮咚音效 */
	private void soundRing() {
		MediaPlayer mp = new MediaPlayer();
		mp.reset();
		try {
			mp.setDataSource(
					mConetxt,
					Uri.parse("android.resource://" + mConetxt.getPackageName()
							+ "/" + R.raw.tone));
			mp.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mp.start();
	}

	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SHOW_WINDOW:
				FloatWindowManager.getInstance().showFloatWindow(currentMsg,
						showText);
				break;
			case MSG_HIDE_WINDOW:
				FloatWindowManager.getInstance().hideFloatWindow();
				playingLock = false;
				break;
			default:
				break;
			}
		}
	};

	/* 构建文本，xxxx发来xxx，用于tts播报和顶浮框显示 */
	private void constructText() {
		Log.d(TAG, "constructTtsText: msgType=" + currentMsg.getMsgType());
		boolean isGroup = currentMsg.isGroupMsg();
		String fromUserName = currentMsg.getFromUserName();
		FriendVo fromVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(fromUserName);
		String from = WeChatUtil.getFriendName(fromVo);
		String str = null;
		switch (currentMsg.getMsgType()) {
		case Constant.MSGTYPE_TEXT:
			str = "发来微信消息,要查看么？";
			break;
		case Constant.MSGTYPE_IMAGE:
			str = "发来图片";
			break;
		case Constant.MSGTYPE_VOICE:
			str = "发来语音";
			break;
		case Constant.MSGTYPE_VIDEO:
		case Constant.MSGTYPE_MICROVIDEO:
			str = "发来视频";
			break;
		case Constant.MSGTYPE_GET_LOCATION:
			str = "发来位置";
			break;
		case Constant.MSGTYPE_SHARE:
			str = "发来分享";
			break;
		default:
			str = "发来微信消息,要查看么？";
			break;
		}
		showText = ExpressionUtil.parseEmoji(mConetxt, from + str);

		from = ExpressionUtil.removeEmoji(from); // 清除emoji字符串
		from = RegexUtil.removeNoneTtsChar(from); // 清除不能播报的特殊字符，如颜文字
		ttsText = (TextUtils.isEmpty(from) ? "微信好友" : from) + str;
	}

	private void stopTts() {
		if (mTtsManagerListener == TTSManager.getInstance()
				.getTtsManagerListener()) {
			TTSManager.getInstance().stopSpeak();
		}
	}

	/* 根据外部条件进行逻辑判断决定该消息是否提示，以及如何提示 */
	private void howToNotify(ReceiveMsgVO newMsg) {
		//是否进行静音操作
		if (isMute) {
			isNotify = false;
			return;
		}
		//不播报的用户过滤
		String fromusename = newMsg.getFromUserName();
		if (null != shieldUse && shieldUse.contains(fromusename)) {
			isNotify = false;
			return;
		}
		
		String senderName = newMsg.getSenderName();
		String fromUserName = newMsg.getFromUserName();
		boolean isGroup = newMsg.isGroupMsg();
		String myUserName = WeChatMain.getWeChatMain().getMyInfo().getData()
				.getUserName();

		// 检查屏幕是否亮，是否播报 

		//检查到时群消息
		if (isGroup) {
			isNotify = false;
			return;
		}
		// 当前消息发送人是自己，不做任何提示
		if (senderName.equals(myUserName)) {
			isNotify = false;
			return;
		}

		// 当前正处于该好友的会话界面，不做任何提示
		if (TextUtils.equals(fromUserName, MessageManager.getInstance()
				.getCurrentContact())) {
			isNotify = false;
			return;
		}

		// 手机上设置了不接收该好友的消息提醒
		FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(newMsg.getFromUserName());
		if (!WeChatUtil.isFriendNotify(friendVo)) {
			isNotify = false;
			return;
		}

		isNotify = true;

		// 在设置中设置不接收新消息提醒
		if (!SharedPreferencesUtil.getInstance().getIsNotify()) {
			soundType = SOUND_TYPE_NO;
			return;
		}

		// 当前处于其他好友的会话界面，叮咚声提示
		if (!TextUtils
				.isEmpty(MessageManager.getInstance().getCurrentContact())) {
			soundType = SOUND_TYPE_TONE;
			return;
		}

		// 当前处于录音界面，叮咚声提示
		if (VoiceRecordActivity.class.getName().equals(
				AppUtils.getCurrentActivity(mConetxt))) {
			soundType = SOUND_TYPE_TONE;
			return;
		}

		// 其他情况，tts提示
		soundType = SOUND_TYPE_TTS;
	}

	/**
	 * 添加屏蔽用户
	 * @param msg
	 */
	public void addShieldUse(ReceiveMsgVO msg) {
		Log.d(TAG, "addShieldUse");
		String fromusename = msg.getFromUserName();
		shieldUse.add(fromusename);
	}
	
	/**
	 * 清空屏蔽用户列表
	 */
	public void clearShieldUse(){
		Log.d(TAG, "clearShieldUse()");
		if (null != shieldUse) {
			shieldUse.clear();
			shieldUse = new ArrayList<String>();
		}else {
			shieldUse = new ArrayList<String>();
		}
	}
	
	public boolean getPlayingLock() {
		return playingLock;
	}

	public void setPlayingLock(boolean playingLock) {
		this.playingLock = playingLock;
	}

	public boolean getIsMute() {
		return isMute;
	}

	public void setIsMute(boolean isMute) {
		this.isMute = isMute;
	}
}
