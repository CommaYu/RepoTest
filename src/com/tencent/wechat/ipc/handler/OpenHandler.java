package com.tencent.wechat.ipc.handler;

import android.content.Context;
import android.content.Intent;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.ui.activity.LoginActivity;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class OpenHandler extends BaseHandler {

	public static final String TAG = "OpenHandler";

	@Override
	public String handle(BridgeRequestVo requestVo) {

		Context context = WeChatApplication.getContext();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClassName(context, LoginActivity.class.getName());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
