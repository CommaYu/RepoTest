package com.tencent.wechat.ui.adapter.holder;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;

/**
 * Author: congqin<br>
 * Data: 2017/1/5<br>
 * Description: 消息类型为 接收的动画表情 时，对应的条目布局<br>
 * Note:<br>
 */
public class ReceiveEmotIconHolder extends BaseHolder {

	private static final String TAG = "ReceiveEmotIconHolder";

	private TextView sendTimeTV;

	private ImageView headIV;

	//private GifImageView contentGIV;

	private ImageView contentIV;

	private TextView senderNameTV;

	public ReceiveEmotIconHolder(Context mContext) {
		super(mContext);
	}

	protected void initView() {
		root = View.inflate(mContext, R.layout.message_item_receive_emot_icon,
				null);

		sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
		headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
		// contentGIV = (GifImageView)
		// root.findViewById(R.id.message_content_gifimageview);
		contentIV = (ImageView) root
				.findViewById(R.id.message_content_imageview);
		senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);
	}

	protected void update() {
		if (showTimeFlag) {
			sendTimeTV
					.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
		} else {
			sendTimeTV.setVisibility(View.GONE);
		}

		WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

		// contentGIV.setVisibility(View.VISIBLE);
		contentIV.setVisibility(View.VISIBLE);
		contentIV.setImageDrawable(null);
		// contentGIV.setImageDrawable(null);
		if (msg.getContent().contains("type=\"1\"")) { // 静态图片
			// contentGIV.setVisibility(View.GONE);
			File file = new File(msg.getImageUrl());
			Uri uri = Uri.fromFile(file);
			contentIV.setImageURI(uri);
		} else if (msg.getContent().contains("type=\"2\"")) { // gif图片
			contentIV.setVisibility(View.GONE);
			// try {
			// // GifDrawable gifDrawable = new GifDrawable(msg.getImageUrl());
			// // contentGIV.setImageDrawable(gifDrawable);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		} else {
			Log.w(TAG,
					"update: unkown emot icon type msg.content="
							+ msg.getContent());
		}

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
