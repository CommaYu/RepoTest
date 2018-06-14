package com.tencent.wechat.ui.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.ExpressionUtil;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.RegexUtils;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeIntentResponse;
import com.tencent.wechat.ipc.PoiInfoVo;

/**
 * Author: congqin<br>
 * Data: 2016/12/15.<br>
 * Description: 消息类型为 接收的位置 时，对应的条目布局<br>
 * Note:<br>
 */
public class ReceiveLocationHolder extends BaseHolder {

    private TextView sendTimeTV;

    private ImageView locationIV;

    private ImageView headIV;

    private ImageView naviIV;

    private TextView contentTV;

    private View contentLL;

    private TextView senderNameTV;


    public ReceiveLocationHolder(Context mContext) {
        super(mContext);
    }

    @Override
    protected void initView() {
        root = View.inflate(mContext, R.layout.message_item_receive_location, null);

        sendTimeTV = (TextView) root.findViewById(R.id.tv_sendtime);
        headIV = (ImageView) root.findViewById(R.id.message_head_imageview);
        locationIV = (ImageView) root.findViewById(R.id.message_text_imageview);
        contentTV = (TextView) root.findViewById(R.id.message_content_textview);
        naviIV = (ImageView) root.findViewById(R.id.message_content_navibutton);
        senderNameTV = (TextView) root.findViewById(R.id.message_user_textview);
        contentLL = root.findViewById(R.id.message_content_ll);

    }


    @Override
    protected void update() {

        if (showTimeFlag) {
            sendTimeTV.setText(WeChatUtil.convertTimeToStr(msg.getCreateTime()));
        } else {
            sendTimeTV.setVisibility(View.GONE);
        }

        WeChatApplication.loadImage(sendUser.getHeadImgUrl(), headIV);

        if (isGroup) {
            senderNameTV.setVisibility(View.VISIBLE);
            //名字里面含有表情
            CharSequence nameSpannableString = ExpressionUtil.parseEmoji(mContext, sendName);
            senderNameTV.setText(nameSpannableString);
        } else {
            senderNameTV.setVisibility(View.GONE);
        }

        contentTV.setText(msg.getContent());
        String imageUrl = msg.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            locationIV.setVisibility(View.GONE);
            naviIV.setVisibility(View.VISIBLE);
        } else {
            naviIV.setVisibility(View.GONE);
            locationIV.setVisibility(View.VISIBLE);
            WeChatApplication.loadImage(msg.getImageUrl(), locationIV);
        }

        contentLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取经纬度
                String latLongtitude = RegexUtils.getLongLatitude(msg.getUrl());
                if (!TextUtils.isEmpty(latLongtitude)) {
                    String[] str = latLongtitude.split(",");
                    String longitude = str[1];
                    String latitude = str[0];
                    PoiInfoVo poi = new PoiInfoVo();
                    poi.setPoiname(msg.getContent());
                    poi.setLongitude(longitude);
                    poi.setLatitude(latitude);
                    poi.setCoord_type(BridgeContract.COORD_TYPE_GCJ);  //腾讯地图目前使用gcj02坐标系
                    BridgeIntentResponse.getInstance().requestForNavi(poi);
                }
            }
        });
    }
}
