package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class ReceiveShareHolder extends BaseHolder {

	private TextView sendTimeTV;

	private ImageView contentIV;

	private ImageView headIV;

	private TextView contentTV;

	private TextView titleTV;

	private TextView senderNameTV;

	public ReceiveShareHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {
		root = View
				.inflate(mContext, R.layout.message_item_receive_share, null);
		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		titleTV = (TextView) root.findViewById(R.id.app_msg_title_textview);
		contentTV = (TextView) root.findViewById(R.id.app_msg_content_textview);
		contentIV = (ImageView) root.findViewById(R.id.app_msg_imageview);
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
		WeChatApplication.loadImage(msg.getImageUrl(), contentIV);

		titleTV.setText(msg.getFileName());
		contentTV.setText(msg.getFileNameContent());

		if (isGroup) {
			senderNameTV.setVisibility(View.VISIBLE);
			// 名字里面含有表情
			CharSequence nameSpannableString = ExpressionUtil.parseEmoji(
					mContext, sendName);
			senderNameTV.setText(nameSpannableString);
		} else {
			senderNameTV.setVisibility(View.GONE);
		}
	}

}
