package com.tencent.wechat.ipc;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iflytek.bridge.BridgeIntentListener;
import com.tencent.wechat.ipc.handler.BaseHandler;

import java.lang.reflect.Type;

/**
 * Author: congqin <br>
 * Data:2016/12/9<br>
 * Description: 监听助理发来的请求<br>
 * Note: <br>
 */

public class BridgeIntentListenerImpl implements BridgeIntentListener {

    private static final String TAG = BridgeIntentListenerImpl.class.getSimpleName();

    @Override
    public String onIntentRequest(String requestStr) {
        Log.d(TAG, "onIntentRequest: requestStr=" + requestStr);

        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<BridgeRequestVo>() {
        }.getType();
        BridgeRequestVo requestVo = gson.fromJson(requestStr, type);

        if (requestVo == null || !TextUtils.equals("weixin", requestVo.getFocus()) || TextUtils.isEmpty(requestVo
                .getCategory())) {
            return BridgeContract.DEFAULT_RESPOND_ERROR;
        }

        String category = requestVo.getCategory();

        BaseHandler handler = HandlerFactory.getHandler(category);
        if (handler == null) {
            Log.e(TAG, "handler is null");
            return BridgeContract.DEFAULT_RESPOND_ERROR;
        }
        return handler.handle(requestVo);
    }
}
