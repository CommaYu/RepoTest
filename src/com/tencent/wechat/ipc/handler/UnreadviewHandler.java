package com.tencent.wechat.ipc.handler;

import android.util.Log;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.ui.activity.SessionActivity;

/**
 * Created by qxb-810 on 2016/12/12.
 */
public class UnreadviewHandler extends BaseHandler {

	private static final String TAG = "UnreadviewHandler";

	@Override
	public String handle(BridgeRequestVo requestVo) {

		String userName = requestVo.getId();

		FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(userName);

		if (friendVo == null) {
			Log.e(TAG, "handle: friendVo is null");
			return BridgeContract.CONTACT_RESPOND_ERROR;
		}

		SessionActivity.startMe(WeChatApplication.getContext(),
				new SessionInfo(userName), true);

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
