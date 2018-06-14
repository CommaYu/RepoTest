package com.tencent.wechat.common.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: congqin <br>
 * Data:2017/1/17<br>
 * Description: 启动SessionActivity 必须携带此对象作为参数 <br>
 * Note: <br>
 */
public class SessionInfo implements Parcelable {

    /**/
    public String user;

//    boolean needSendMessage;
//
//    boolean needSendLocation;


    public SessionInfo(String user) {
        this.user = user;
    }



    protected SessionInfo(Parcel in) {
        user = in.readString();
    }

    public static final Creator<SessionInfo> CREATOR = new Creator<SessionInfo>() {
        @Override
        public SessionInfo createFromParcel(Parcel in) {
            return new SessionInfo(in);
        }

        @Override
        public SessionInfo[] newArray(int size) {
            return new SessionInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
    }
}
