package com.tencent.wechat.ui.adapter.holder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.ui.activity.SpaceImageDetailActivity;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class ReceiveImageHolder extends BaseHolder {


    private TextView sendTimeTV;

    private ImageView headIV;

    private ImageView contentIV;

    private TextView senderNameTV;

    public ReceiveImageHolder(Context mContext) {
        super(mContext);
    }


    protected void initView() {
        root = View.inflate(mContext, R.layout.message_item_receive_img, null);

        sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
        headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
        contentIV = (ImageView) root.findViewById(R.id.message_content_imageview);
        senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);
    }


    protected void update() {

        if (showTimeFlag) {
            sendTimeTV.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
        } else {
            sendTimeTV.setVisibility(View.GONE);
        }

        WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

        WeChatApplication.loadImage(msg.getImageUrl(), contentIV);

        if (isGroup) {
            senderNameTV.setVisibility(View.VISIBLE);
            //名字里面含有表情
            CharSequence nameSpannableString = ExpressionUtil.parseEmoji(mContext, sendName);
            senderNameTV.setText(nameSpannableString);
        } else {
            senderNameTV.setVisibility(View.GONE);
        }

        final int[] location = new int[2];
        contentIV.getLocationOnScreen(location);
        final int width = contentIV.getWidth();
        final int height = contentIV.getHeight();

        contentIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SpaceImageDetailActivity.class);
                intent.putExtra("images", msg.getImageUrl());

                intent.putExtra("locationX", location[0]);
                intent.putExtra("locationY", location[1]);

                intent.putExtra("width", width);
                intent.putExtra("height", height);
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(0, 0);
            }
        });

    }
}
