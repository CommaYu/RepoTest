package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.service.WeChatService;

/**
 * Author: congqin<br>
 * Data: 2016/12/13.<br>
 * Description: 处理助理发过来的指令 返回微信的登录状态<br>
 * Note:<br>
 */
public class LoginHandler extends BaseHandler {
	@Override
	public String handle(BridgeRequestVo requestVo) {

		WeChatService.LoginState loginState = WeChatApplication.getBinder()
				.getLoginState();
		switch (loginState) {
		case noLogin:
			return BridgeContract.LOGIN_RESPOND_NOLOGIN;
		case logining:
			return BridgeContract.LOGIN_RESPOND_LOGINING;
		case login:
			return BridgeContract.LOGIN_RESPOND_LOGIN;
		default:
			return BridgeContract.LOGIN_RESPOND_NOLOGIN;
		}
	}
}
