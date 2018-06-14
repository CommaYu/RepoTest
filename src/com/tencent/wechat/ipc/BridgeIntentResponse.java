package com.tencent.wechat.ipc;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iflytek.bridge.ServiceStub;
import com.iflytek.bridge.aidl.OnBridegeListner;
import com.tencent.wechat.http.entity.FriendVo;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Author: congqin <br>
 * Data:2017/3/26<br>
 * Description: 负责向Speechclient发送请求<br>
 * Note: <br>
 */
public class BridgeIntentResponse {

    private static final String TAG = "BridgeIntentResponse";

    private OnBridegeListner mOnBridegeListner;

    private static BridgeIntentResponse sInstance;

    private BridgeIntentResponse() {
        mOnBridegeListner = ServiceStub.getInstance().getIntentResponse();
    }

    public static BridgeIntentResponse getInstance() {
        if (sInstance == null) {
            sInstance = new BridgeIntentResponse();
        }
        return sInstance;
    }

    private String onIntentBack(String intentStr) {
        String ret = null;
        try {
            mOnBridegeListner = ServiceStub.getInstance().getIntentResponse();
            if (mOnBridegeListner != null) {
                ret = mOnBridegeListner.onIntentBack(intentStr);
            } else {
                Log.e(TAG, "onIntentBack: mOnBridegeListenr is null");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return "";
        }
        return ret;
    }

    /**
     * 上传微信联系人给语音助理
     *
     * @param friendVoList 联系人列表
     * @return true 成功  false失败
     */
    public boolean uploadContact(Collection<FriendVo> friendVoList) {
        String intentStr = IntentConstructor.getContactIntent(friendVoList);
        String ret = onIntentBack(intentStr);
        return resultSuccess(ret);
    }

    /**
     * 请求助理打开录音器（因为微信自己录音完毕）
     *
     * @return true成功  false失败
     */
    public boolean requestRecordOn() {
        String intentStr = IntentConstructor.getRecordIntent(true);
        String ret = onIntentBack(intentStr);
        return resultSuccess(ret);
    }

    /**
     * 请求助理关闭录音器（因为微信需要自己创建录音器录音）
     *
     * @return true成功  false失败
     */
    public boolean requestRecordOff() {
        String intentStr = IntentConstructor.getRecordIntent(false);
        String ret = onIntentBack(intentStr);
        return resultSuccess(ret);
    }

    /**
     * 请求助理执行导航
     *
     * @param poi
     * @return true成功  false失败
     */
    public boolean requestForNavi(PoiInfoVo poi) {
        String intentStr = IntentConstructor.getNaviIntent(poi);
        String ret = onIntentBack(intentStr);
        return resultSuccess(ret);
    }

    /**
     * 请求助理当前位置信息
     *
     * @return
     */
    public BridgeResponseVo getLocation() {
        String intentStr = IntentConstructor.getLocationIntent();
        String response = onIntentBack(intentStr);
        return parseResponse(response);
    }


    private BridgeResponseVo parseResponse(String response) {
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<BridgeResponseVo>() {
        }.getType();
        return gson.fromJson(response, type);
    }

    /**
     * 解析助理返回的json串
     * 确实是否执行成功
     *
     * @param result 助理返回的json串
     * @return true成功  false失败
     */
    private boolean resultSuccess(String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        }

        JSONObject jsonObject = null;
        String status = null;
        try {
            jsonObject = new JSONObject(result);
            status = jsonObject.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ("success".equals(status)) {
            return true;
        }
        return false;
    }
}
