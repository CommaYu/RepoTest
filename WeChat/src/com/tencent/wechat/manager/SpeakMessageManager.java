package com.tencent.wechat.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.utils.log.Logging;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.RegexUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;

/**
 * Created by qxb-810 on 2016/12/15.
 * <p/>
 * 负责播报消息，内部封装了TtsManager(播报文字) 和 MediaPlayerManager （播报音频）
 */
public class SpeakMessageManager {

	private final String TAG = "SpeakMessageManager";

	private static SpeakMessageManager mInstance;

	private ReceiveMsgVO currentSpeakMsg; // 当前播报的消息对象，播报结束后，不置为Null,等待新的msg

	public static SpeakMessageManager getInstance() {
		if (mInstance == null) {
			mInstance = new SpeakMessageManager();
		}
		return mInstance;
	}

	private SpeakMessageManager() {
		MediaPlayerManager.getInstance().setMediaPlayerListener(
				new MediaPlayerManager.MediaPlayerListener() {
					@Override
					public void onMediaPlayerStart() {

					}

					@Override
					public void onMediaPlayerStop() {
						isVoicePlaying = false;
						Log.d(TAG, "onMediaPlayerStop: notifylistenerStop");
						notifyListenerStop(currentSpeakMsg);
					}
				});

	}

	TTSManager.TtsManagerListener mTtsManagerListener = new TTSManager.TtsManagerListener() {
		@Override
		public void onTtsStart() {
		}

		@Override
		public void onTtsComplete() {
			Log.d(TAG, "onTtsComplete: locktts listener onttsstop");
			isTextPlaying = false;
			Log.d(TAG, "onTtsComplete: notifyListenerStop");
			notifyListenerStop(currentSpeakMsg);
		}
	};

	public void lockTts() {
		// TTSManager.getInstance().lock();
		TTSManager.getInstance().setTtsManagerListener(mTtsManagerListener);
	}

	public void unLockTts() {
		TTSManager.getInstance().setTtsManagerListener(null);
	}

	private boolean isTextPlaying;
	private boolean isVoicePlaying;

	public void startPlay(final ReceiveMsgVO msg) {
		// stopPlay();
		lockTts();
		currentSpeakMsg = msg;

		switch (msg.getMsgType()) {
		case Constant.MSGTYPE_VOICE:
			isVoicePlaying = true;
			isTextPlaying = true;
			String tts = constructTtsText(msg);
			TTSManager.getInstance().setTtsManagerListener(
					new TTSManager.TtsManagerListener() {
						@Override
						public void onTtsStart() {

						}

						@Override
						public void onTtsComplete() {
							isTextPlaying = false;
							Log.d(TAG,
									"onTtsComplete: msgtype_voice:onTtsComplete");
							MediaPlayerManager.getInstance().startPlay(
									msg.getVoiceUrl());
						}
					});
			TTSManager.getInstance().startSpeak(tts);
			break;
		case Constant.MSGTYPE_TEXT:
		case Constant.MSGTYPE_IMAGE:
		case Constant.MSGTYPE_GET_LOCATION:
		case Constant.MSGTYPE_VIDEO:
		case Constant.MSGTYPE_MICROVIDEO:
		case Constant.MSGTYPE_EMOTICON:
			isTextPlaying = true;
			String ttsText = constructTtsText(msg);
			TTSManager.getInstance().startSpeak(ttsText);
			break;
		default:
			break;
		}

		notifyListenerStart(currentSpeakMsg);
	}

	public void stopPlay() {
		if (currentSpeakMsg == null) {
			return;
		}
		if (isVoicePlaying) {
			MediaPlayerManager.getInstance().stopPlay();
			isVoicePlaying = false;
			Log.d(TAG, "stopPlay: notifyListenerStop");
			notifyListenerStop(currentSpeakMsg);
		}
		if (isTextPlaying) {
			TTSManager.getInstance().stopSpeak();
			isTextPlaying = false;
			Log.d(TAG, "stopPlay: notifyListenerStop");
			notifyListenerStop(currentSpeakMsg);
		}
	}

	/*
	 * 根据消息类型构建tts播报文本 某些特殊字符（如emoji码）作为首字符会导致tts异常，进行过滤
	 */
	private String constructTtsText(ReceiveMsgVO msg) {

		String fromUser = msg.getFromUserName();
		String sender = "";
		FriendVo senderVo = null;

		if (msg.getFromUserName().startsWith("@@")) {
			FriendVo groupFriend = WeChatMain.getWeChatMain()
					.getAllFriendsMap().get(fromUser);
			List<FriendVo> members = groupFriend.getMemberList();
			if (members != null && !members.isEmpty()) {
				for (FriendVo vo : members) {
					if (msg.getSenderName().equals(vo.getUserName())) {
						senderVo = vo;
					}
				}
			}
		} else {
			senderVo = WeChatMain.getWeChatMain().getAllFriendsMap()
					.get(fromUser);
		}

		if (senderVo == null) {
			Log.e(TAG, "constructTtsText: senderVo is null");
			return "好友说";
		}

		sender = TextUtils.isEmpty(senderVo.getDisplayName()) ? (TextUtils
				.isEmpty(senderVo.getRemarkName()) ? senderVo.getNickName()
				: senderVo.getRemarkName()) : senderVo.getDisplayName();
		sender = ExpressionUtil.removeEmoji(sender);
		sender = RegexUtil.removeNoneTtsChar(sender);
		sender = TextUtils.isEmpty(sender) ? "好友" : sender;

		switch (msg.getMsgType()) {
		case Constant.MSGTYPE_TEXT:
			String content = ExpressionUtil.removeEmoji(msg.getContent());
			content = ExpressionUtil.removeDefault(content);
			while (content.length() > 0) {
				String firstLetter = content.substring(0, 1);
				if (!RegexUtil.isNoneTtsChar(firstLetter)) {
					return sender + "说，" + content;
				}
				content = content.substring(1, content.length());
			}
			return sender + "发来表情符号";
		case Constant.MSGTYPE_VOICE:
			return sender + "说，";
		case Constant.MSGTYPE_IMAGE:
			return sender + "发来图片";
		case Constant.MSGTYPE_EMOTICON:
			return sender + "发来动画表情";
		case Constant.MSGTYPE_VIDEO:
		case Constant.MSGTYPE_MICROVIDEO:
			return sender + "发来视频";
		case Constant.MSGTYPE_GET_LOCATION:
			return sender + "发来位置," + msg.getContent() + ",点击开始导航";
		default:
			return null;
		}
	}

	private void notifyListenerStart(ReceiveMsgVO msg) {
		for (SpeakMessageListener listener : listenerList) {
			listener.onStartPlay(msg);
		}
	}

	private void notifyListenerStop(ReceiveMsgVO msg) {
		for (SpeakMessageListener listener : listenerList) {
			listener.onStopPlay(msg);
		}
	}

	private List<SpeakMessageListener> listenerList = new ArrayList<SpeakMessageListener>();

	public void unRegisterListener(SpeakMessageListener listener) {
		if (listenerList.contains(listener)) {
			listenerList.remove(listener);
			Log.d(TAG, "unRegisterListener:  list size =" + listenerList.size());
		}
	}

	public void registerListener(SpeakMessageListener listener) {
		if (!listenerList.contains(listener)) {
			listenerList.add(listener);
			Log.d(TAG, "registerListener:  list size = " + listenerList.size());
		}
	}

	public interface SpeakMessageListener {
		/**
		 * 开始播报
		 * 
		 * @param msg
		 */
		void onStartPlay(ReceiveMsgVO msg);

		/**
		 * 暂停播报，可能是被打断了，也可能是播报完成，这两种情况不加区分
		 * 
		 * @param msg
		 */
		void onStopPlay(ReceiveMsgVO msg);

	}

	public ReceiveMsgVO getCurrentSpeakMsg() {
		return currentSpeakMsg;
	}

	public boolean isSpeaking() {
		return isTextPlaying || isVoicePlaying;
	}
	
}
