package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class SendLocationHolder extends BaseHolder {

	private TextView contentTV;

	private TextView sendTimeTV;

	private ImageView headIV;

	public SendLocationHolder(Context mContext) {
		super(mContext);
	}

	@Override
	protected void initView() {
		root = View.inflate(mContext, R.layout.message_item_send_text, null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		contentTV = (TextView) root.findViewById(R.id.message_content_textview);
	}

	@Override
	protected void update() {

		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		FriendVo sendUser = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(msg.getSenderName());
		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

		contentTV.setText(msg.getContent());
	}
}
