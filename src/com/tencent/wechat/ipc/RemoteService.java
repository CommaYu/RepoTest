package com.tencent.wechat.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.iflytek.bridge.ServiceStub;

/**
 * Author: congqin <br>
 * Data: 2016/12/9<br>
 * Description: 该service提供给speechclient来连接<br>
 * Note: <br>
 */
public class RemoteService extends Service {

	private static final String TAG = "RemoteService";

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceStub.getInstance().setBridgeIntentListener(
				new BridgeIntentListenerImpl());
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: =====>");
		return ServiceStub.getInstance().getBridegeServiceStub();
	}
}
