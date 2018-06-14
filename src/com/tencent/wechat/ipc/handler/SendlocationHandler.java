package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.ipc.PoiInfoVo;
import com.tencent.wechat.manager.SendLocationManager;
import com.tencent.wechat.ui.activity.SessionActivity;

/**
 * Author: congqin<br>
 * Data: 2016/12/9.<br>
 * Description: 处理助理发过来的指令 发送位置给好友<br>
 * Note:<br>
 */
public class SendlocationHandler extends BaseHandler {

	public static final String TAG = "SendlocationHandler";

	@Override
	public String handle(BridgeRequestVo requestVo) {

		PoiInfoVo poiInfoVo = requestVo.getPoi();

		String userName = requestVo.getId();

		FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(userName);

		if (friendVo == null) {
			return BridgeContract.CONTACT_RESPOND_ERROR;
		}

		SendLocationManager.getInstance().sendLocation(userName, poiInfoVo);

		SessionActivity.startMe(WeChatApplication.getContext(),
				new SessionInfo(userName), true);

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
