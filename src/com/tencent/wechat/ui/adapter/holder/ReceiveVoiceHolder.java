package com.tencent.wechat.ui.adapter.holder;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.manager.Dispatch;
import com.tencent.wechat.manager.SpeakMessageManager;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class ReceiveVoiceHolder extends BaseHolder {

	private TextView sendTimeTV;

	private ImageView headIV;

	private ImageView voiceIV;

	private TextView voiceTimeTV;

	private ImageView unReadFlagIV;

	private View receiveVoiceLL;

	private TextView senderNameTV;

	public ReceiveVoiceHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {

		root = View
				.inflate(mContext, R.layout.message_item_receive_voice, null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		voiceIV = (ImageView) root.findViewById(R.id.message_voice_imageview);
		senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);
		voiceTimeTV = (TextView) root.findViewById(R.id.message_voice_time);
		unReadFlagIV = (ImageView) root.findViewById(R.id.iv_unread_voice);
		receiveVoiceLL = root.findViewById(R.id.ll_receive_voice);

		root.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

			@Override
			public void onViewAttachedToWindow(View v) {
				SpeakMessageManager.getInstance().registerListener(
						mSpeakMessageListener);
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				SpeakMessageManager.getInstance().unRegisterListener(
						mSpeakMessageListener);
			}
		});
	}

	private SpeakMessageManager.SpeakMessageListener mSpeakMessageListener = new SpeakMessageManager.SpeakMessageListener() {
		@Override
		public void onStartPlay(ReceiveMsgVO message) {
			Message msg = Message.obtain();
			msg.what = MSG_START_PLAY;
			msg.obj = message;
			Dispatch.getInstance().sendMessageDelayUiThread(msg, 0,
					handleListener);
		}

		@Override
		public void onStopPlay(ReceiveMsgVO message) {
			Message msg = Message.obtain();
			msg.what = MSG_STOP_PLAY;
			msg.obj = message;
			Dispatch.getInstance().sendMessageDelayUiThread(msg, 0,
					handleListener);
		}
	};

	public static final int MSG_START_PLAY = 0;
	public static final int MSG_STOP_PLAY = 1;

	@Override
	protected void update() {
		File file = new File(msg.getVoiceUrl());
		if (msg.getVoiceUrl().endsWith(".mp3")) {
			voiceTimeTV.setText(WeChatUtil.getMp3Duration(file) + "''");
		} else if (msg.getVoiceUrl().endsWith(".amr")) {
			voiceTimeTV.setText(((msg.getVoiceLength() + 500) / 1000 + 1)
					+ "''");
		}
		if (msg.isRead_status()) {
			unReadFlagIV.setVisibility(View.INVISIBLE);
		} else {
			unReadFlagIV.setVisibility(View.VISIBLE);
		}
		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

		setVoiceIcon();

		if (isGroup) {
			senderNameTV.setVisibility(View.VISIBLE);
			// 名字里面含有表情
			CharSequence nameSpannableString = ExpressionUtil.parseEmoji(
					mContext, sendName);
			senderNameTV.setText(nameSpannableString);
		} else {
			senderNameTV.setVisibility(View.GONE);
		}

		receiveVoiceLL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				msg.setRead_status(true);
				unReadFlagIV.setVisibility(View.GONE);
				mContext.onMsgClick(msg);
			}
		});
	}

	private void setVoiceIcon() {
		ReceiveMsgVO currentMsg = SpeakMessageManager.getInstance()
				.getCurrentSpeakMsg();
		boolean isSpeaking = SpeakMessageManager.getInstance().isSpeaking();
		if (currentMsg != null && currentMsg == msg && isSpeaking) {
			voiceIV.setBackgroundResource(R.drawable.voice_from_icon);
			AnimationDrawable voiceAnim = (AnimationDrawable) voiceIV
					.getBackground();
			voiceAnim.start();
		} else {
			voiceIV.setBackgroundResource(R.mipmap.chatfrom_voice_playing_f3);
		}
	}

	Dispatch.HandleListener handleListener = new Dispatch.HandleListener() {
		@Override
		public void handleMessage(Message m) {
			ReceiveMsgVO message = (ReceiveMsgVO) m.obj;
			switch (m.what) {
			case MSG_STOP_PLAY:
				if (message == msg) {
					Drawable backgroud = voiceIV.getBackground();
					if (backgroud instanceof AnimationDrawable) {
						AnimationDrawable voiceAnim = (AnimationDrawable) voiceIV
								.getBackground();
						if (voiceAnim != null) {
							voiceAnim.stop();
						}
					}
					voiceIV.setBackgroundResource(R.mipmap.chatfrom_voice_playing_f3);
				}
				break;
			case MSG_START_PLAY:
				if (message == msg) {
					voiceIV.setBackgroundResource(R.drawable.voice_from_icon);
					AnimationDrawable voiceAnim = (AnimationDrawable) voiceIV
							.getBackground();
					voiceAnim.start();
					msg.setRead_status(true);
					unReadFlagIV.setVisibility(View.GONE);
				}
				break;
			}
		}
	};
}
