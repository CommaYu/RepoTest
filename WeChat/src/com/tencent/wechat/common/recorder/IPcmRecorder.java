package com.tencent.wechat.common.recorder;

/**
 * Created by qxb-810 on 2016/12/2.
 */
public interface IPcmRecorder {

    int startRecord();

    int stopRecord();

    void setOnPcmRecordListener(OnPcmRecordListener listener);

    public interface OnPcmRecordListener {
        void onRecordData(final byte[] dataBuffer, final int length);
    }


}
