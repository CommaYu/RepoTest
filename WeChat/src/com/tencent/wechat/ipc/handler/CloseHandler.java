package com.tencent.wechat.ipc.handler;

import android.content.Intent;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;

/**
 * Author: congqin<br>
 * Data: 2016/12/9.<br>
 * Description: 处理助理发过来的指令 关闭微信 (退到后台运行)<br>
 * Note:<br>
 */
public class CloseHandler extends BaseHandler {
	@Override
	public String handle(BridgeRequestVo requestVo) {

		// if (WeChatUtil.isRunningForeground(WeChatApplication.getContext())) {

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		WeChatApplication.getContext().startActivity(startMain);
		// / }

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
