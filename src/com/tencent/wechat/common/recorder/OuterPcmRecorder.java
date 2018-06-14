package com.tencent.wechat.common.recorder;

import com.tencent.wechat.WeChatApplication;

import android.os.RemoteException;

public class OuterPcmRecorder implements IPcmRecorder {

    public static OuterPcmRecorder instance = null;
    
    public static synchronized OuterPcmRecorder getInstance() {
        if (instance == null) {
            try {
                instance = new OuterPcmRecorder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public int startRecord() {
        try {
            return WeChatApplication.recorderSvc.startRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int stopRecord() {
        try {
            return WeChatApplication.recorderSvc.stopRecord();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void setOnPcmRecordListener(OnPcmRecordListener listener) {
        WeChatApplication.setPcmRecordListener(listener);
    }

}