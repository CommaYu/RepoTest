package com.tencent.wechat.common.entity;


import android.graphics.Bitmap;

import com.tencent.wechat.http.entity.ReceiveMsgVO;

public class NotifiModel {
    public Bitmap bitmap;
    public String text;
    public ReceiveMsgVO currentMsg;
    public String userName;
    public String remarkName;

    public NotifiModel(Bitmap bitmap, String text, String userName, ReceiveMsgVO currentMsg, String remarkName) {
        super();
        this.bitmap = bitmap;
        this.text = text;
        this.userName = userName;
        this.currentMsg = currentMsg;
        this.remarkName = remarkName;
    }

}
