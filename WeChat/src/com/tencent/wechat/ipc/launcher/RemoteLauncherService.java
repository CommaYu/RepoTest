package com.tencent.wechat.ipc.launcher;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.iflytek.bridge.ServiceStub;

/**
 * Author: congqin <br>
 * Data:2017/3/26<br>
 * Description: <br>
 * Note: <br>
 */
public class RemoteLauncherService extends Service {

	private static final String TAG = RemoteLauncherService.class
			.getSimpleName();

	private ServiceStub serviceStub;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate: ");
		serviceStub = new ServiceStub();
		serviceStub
				.setBridgeIntentListener(new LauncherBridgeIntentListenerImpl());
		// LauncherBridgeIntentResponse.getInstance().setRespondListener(serviceStub.getIntentResponse());
		LauncherBridgeIntentResponse.getInstance().setServiceStub(serviceStub);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: ");
		return serviceStub.getBridegeServiceStub();
	}

}
