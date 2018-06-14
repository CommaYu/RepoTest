package com.tencent.wechat.ui.adapter.holder;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.manager.SpeakMessageManager;

/**
 * Created by qxb-810 on 2016/12/15.
 * <p/>
 * 该holder 暂时无效，因为车机发出去的语音对应的消息类型为sendTextHolder
 */
public class SendVoiceHolder extends BaseHolder {

	private TextView sendTimeTV;

	private ImageView headIV;

	private ImageView voiceIV;

	private TextView voiceTimeTV;

	private View sendVoiceLL;

	public SendVoiceHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {

		root = View.inflate(mContext, R.layout.message_item_send_voice, null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		voiceIV = (ImageView) root.findViewById(R.id.message_voice_imageview);
		voiceTimeTV = (TextView) root.findViewById(R.id.message_voice_time);

		sendVoiceLL = root.findViewById(R.id.ll_send_voice);
	}

	@Override
	protected void update() {
		File file = new File(msg.getVoiceUrl());
		if (msg.getVoiceUrl().endsWith(".mp3")) {
			voiceTimeTV.setText(WeChatUtil.getMp3Duration(file) + "''");
		} else if (msg.getVoiceUrl().endsWith(".amr")) {
			voiceTimeTV.setText(((msg.getVoiceLength() + 500) / 1000 + 1)
					+ "''");
		}
		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		FriendVo sendUser = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(msg.getSenderName());
		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);
		voiceIV.setImageResource(R.mipmap.chatto_voice_playing_f3);

		sendVoiceLL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SpeakMessageManager.getInstance().startPlay(msg);
			}
		});
	}
}
