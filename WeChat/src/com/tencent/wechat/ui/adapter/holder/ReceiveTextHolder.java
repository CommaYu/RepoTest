package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
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
public class ReceiveTextHolder extends BaseHolder {

	public static final int MSG_START_PLAY = 0;
	public static final int MSG_STOP_PLAY = 1;

	private TextView sendTimeTV;

	private ImageView headIV;

	private TextView contentTV;

	private TextView senderNameTV;

	private View receiveTextLL;

	private ImageView messageIV;

	public ReceiveTextHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {
		root = View.inflate(mContext, R.layout.message_item_receive_text, null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		contentTV = (TextView) root.findViewById(R.id.message_content_textview);

		messageIV = (ImageView) root.findViewById(R.id.message_text_imageview);

		senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);

		receiveTextLL = root.findViewById(R.id.ll_receive_text);

		root.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

			@Override
			public void onViewAttachedToWindow(View v) {
				Log.d("SpeakMessageManager", "onViewAttachedToWindow: ");
				SpeakMessageManager.getInstance().registerListener(
						mSpeakMessageListener);
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				Log.d("SpeakMessageManager", "onViewDetachedFromWindow: ");
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

	@Override
	protected void update() {

		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);
		setVoiceIcon();
		CharSequence spannableString = ExpressionUtil.parseEmoji(mContext,
				msg.getContent());
		spannableString = ExpressionUtil
				.parseDefault(mContext, spannableString);
		contentTV.setText(spannableString);

		if (isGroup) {
			senderNameTV.setVisibility(View.VISIBLE);
			// 名字里面含有表情
			CharSequence nameSpannableString = ExpressionUtil.parseEmoji(
					mContext, sendName);
			senderNameTV.setText(nameSpannableString);
		} else {
			senderNameTV.setVisibility(View.GONE);
		}

		receiveTextLL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mContext.onMsgClick(msg);
			}
		});
	}

	private void setVoiceIcon() {
		ReceiveMsgVO currentMsg = SpeakMessageManager.getInstance()
				.getCurrentSpeakMsg();
		boolean isSpeaking = SpeakMessageManager.getInstance().isSpeaking();
		if (currentMsg != null && currentMsg == msg && isSpeaking) {
			messageIV.setBackgroundResource(R.drawable.text_to_voice);
			AnimationDrawable voiceAnim = (AnimationDrawable) messageIV
					.getBackground();
			voiceAnim.start();
		} else {
			messageIV.setBackgroundResource(R.mipmap.text_to_voice3);
		}
	}

	Dispatch.HandleListener handleListener = new Dispatch.HandleListener() {
		@Override
		public void handleMessage(Message m) {
			ReceiveMsgVO message = (ReceiveMsgVO) m.obj;
			switch (m.what) {
			case MSG_STOP_PLAY:
				if (message == msg) {
					Drawable backgroud = messageIV.getBackground();
					if (backgroud instanceof AnimationDrawable) {
						AnimationDrawable voiceAnim = (AnimationDrawable) messageIV
								.getBackground();
						if (voiceAnim != null) {
							voiceAnim.stop();
						}
					}
					messageIV.setBackgroundResource(R.mipmap.text_to_voice3);
				}
				break;
			case MSG_START_PLAY:
				if (message == msg) {
					messageIV.setBackgroundResource(R.drawable.text_to_voice);
					AnimationDrawable voiceAnim = (AnimationDrawable) messageIV
							.getBackground();
					voiceAnim.start();
				}
				break;
			}
		}
	};
}
