package com.tencent.wechat.ui.adapter.holder;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class ReceiveVideoHolder extends BaseHolder {

	private TextView sendTimeTV;

	private ImageView headIV;

	private ImageView contentIV;

	private VideoView contentVV;

	private TextView senderNameTV;

	public ReceiveVideoHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {

		root = View
				.inflate(mContext, R.layout.message_item_receive_video, null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		contentIV = (ImageView) root.findViewById(R.id.message_video_imageview);
		contentVV = (VideoView) root.findViewById(R.id.message_video_view);
		senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);
	}

	@Override
	protected void update() {

		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

		if (isGroup) {
			senderNameTV.setVisibility(View.VISIBLE);
			// 名字里面含有表情
			CharSequence nameSpannableString = ExpressionUtil.parseEmoji(
					mContext, sendName);
			senderNameTV.setText(nameSpannableString);
		} else {
			senderNameTV.setVisibility(View.GONE);
		}

		if (contentVV.isPlaying()) {
			contentVV.stopPlayback();
		}

		String localVideoUrl = msg.getLocalMediaUrl();
		if (TextUtils.isEmpty(localVideoUrl)) {
			contentIV.setVisibility(View.VISIBLE);
			contentVV.setVisibility(View.GONE);
			WeChatApplication.loadImage(msg.getImageUrl(), contentIV);
			contentIV.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								byte[] b = WeChatMain.getWeChatMain()
										.getResource(msg.getVideoUrl())
										.getData();
								String loaclVideoUrl = WeChatUtil.saveFile(
										"/video",
										"/" + msg.getMsgId() + ".mp4", b);
								msg.setLocalMediaUrl(loaclVideoUrl);
								((Activity) mContext)
										.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												// notifyDataSetChanged();
											}
										});
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}).start();

				}
			});
		} else {
			contentIV.setVisibility(View.GONE);
			contentVV.setVisibility(View.VISIBLE);
			contentVV
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.start();
						}
					});
			contentVV.setVideoURI(Uri.parse(localVideoUrl));
			contentVV.start();
		}

	}

}
