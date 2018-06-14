package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.common.utils.ExpressionUtil;

/**
 * Created by qxb-810 on 2016/12/15.
 */
public class ReceiveSysHolder extends BaseHolder {

    private TextView contentTV;

    public ReceiveSysHolder(Context mContext) {
        super(mContext);
    }

    @Override
    protected void initView() {
        root = View.inflate(mContext, R.layout.message_item_receive_system, null);
        contentTV = (TextView) root.findViewById(R.id.message_content_textview);
    }

    @Override
    protected void update() {
        CharSequence spannableString = ExpressionUtil.parseEmoji(mContext, msg.getContent());
        contentTV.setText(spannableString);
    }
}
