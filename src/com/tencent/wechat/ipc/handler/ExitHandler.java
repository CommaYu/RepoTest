package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.utils.AppManager;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class ExitHandler extends BaseHandler {
	@Override
	public String handle(BridgeRequestVo requestVo) {

		// 必须在logout之前调用
		AppManager.getInstance().finishAllActivity();

		WeChatApplication.getBinder().logout();

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
