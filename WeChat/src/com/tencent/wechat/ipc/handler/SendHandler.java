package com.tencent.wechat.ipc.handler;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.manager.Dispatch;

/**
 * Author: congqin<br>
 * Data: 2016/12/9.<br>
 * Description: 处理助理发过来的指令 发微信给好友<br>
 * Note:<br>
 */
public class SendHandler extends BaseHandler {

	private static final String TAG = "SendHandler";

	@Override
	public String handle(BridgeRequestVo requestVo) {

		final String userName = requestVo.getId();

		if (TextUtils.isEmpty(userName)) {
			Log.w(TAG, "handle: id is empty ");
			return BridgeContract.CONTACT_RESPOND_ERROR;
		}

		FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(userName);

		if (friendVo == null) {
			Log.e(TAG, "handle: =========>friendVo is null");
			return BridgeContract.CONTACT_RESPOND_ERROR;
		} else {

			Dispatch.getInstance().postDelayedByUIThread(new Runnable() {
				@Override
				public void run() {
					WeChatApplication.getBinder().sendVoiceMsg(userName);
				}
			}, 0);

			// WeChatApplication.getBinder().sendVoiceMsg(userName);
			return BridgeContract.DEFAULT_RESPOND_OK;
		}
	}
}
