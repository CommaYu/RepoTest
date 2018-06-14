package com.tencent.wechat.ipc.launcher;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.bridge.BridgeIntentListener;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.entity.UnReadMsgVo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.ui.activity.WechatActivity;
import com.tencent.wechat.ui.activity.SessionActivity;

/**
 * Author: congqin <br>
 * Data:2017/3/26<br>
 * Description: 监听桌面Launcher 发来的请求<br>
 * Note: <br>
 */
public class LauncherBridgeIntentListenerImpl implements BridgeIntentListener {

	private static final String TAG = LauncherBridgeIntentListenerImpl.class
			.getSimpleName();

	@Override
	public String onIntentRequest(String s) {
		Log.d(TAG, "onIntentRequest: request from launcher: s=" + s);

		String ret = BridgeContract.DEFAULT_RESPOND_ERROR;
		try {
			JSONObject object = new JSONObject(s);
			String focus = object.getString("focus");
			if (!TextUtils.equals(focus, "weixin")) {
				return null;
			}
			String category = object.getString("category");
			if (TextUtils.equals(category, "getmsg")) {
				ret = handleGetmsg(s);
			} else if (TextUtils.equals(category, "showone")) {
				String userName = object.getString("id");
				ret = handleShowone(userName);
			} else if (TextUtils.equals(category, "showall")) {
				ret = handleShowall();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private String handleShowall() {

		Intent intent = new Intent();
		intent.setClass(WeChatApplication.getContext(), WechatActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		WeChatApplication.getContext().startActivity(intent);
		return BridgeContract.DEFAULT_RESPOND_OK;
	}

	private String handleShowone(String userName) {
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

	private String handleGetmsg(String s) {

		String result = BridgeContract.DEFAULT_RESPOND_ERROR;

		Map<String, UnReadMsgVo> mUnReadMsgMap = MessageManager.getInstance()
				.getmUnReadMsgMap();

		JSONObject object = new JSONObject();
		try {
			object.put("status", "success");
			object.put("message", "");

			JSONArray array = new JSONArray();
			if (mUnReadMsgMap == null || mUnReadMsgMap.isEmpty()) {
				object.put("unreadnum", 0);
				object.put("msglist", array);
			} else {
				int unreadnum = 0;
				for (String uername : mUnReadMsgMap.keySet()) {
					JSONObject msgObject = new JSONObject();
					UnReadMsgVo unReadMsgVo = mUnReadMsgMap.get(uername);
					msgObject.put("id", unReadMsgVo.getmUserName());
					String remarkName = unReadMsgVo.getmRemarkName();
					String nickName = unReadMsgVo.getmNickName();
					msgObject.put("name",
							nickName
									+ (TextUtils.isEmpty(remarkName) ? "" : "("
											+ remarkName + ")"));
					msgObject.put("num", unReadMsgVo.getUnReadMsg().size());
					ReceiveMsgVO latestMsg = unReadMsgVo.getUnReadMsg().get(
							unReadMsgVo.getUnReadMsg().size() - 1);
					String content = latestMsg.getContent();

					String fromUser = latestMsg.getFromUserName();
					if (fromUser.startsWith("@@")) {
						FriendVo groupFriend = WeChatMain.getWeChatMain()
								.getAllFriendsMap().get(fromUser);
						List<FriendVo> members = groupFriend.getMemberList();
						if (members != null && !members.isEmpty()) {
							for (FriendVo vo : members) {
								if (latestMsg.getSenderName().equals(
										vo.getUserName())) {
									String sendName = TextUtils.isEmpty(vo
											.getDisplayName()) ? (TextUtils
											.isEmpty(vo.getRemarkName()) ? vo
											.getNickName() : vo.getRemarkName())
											: vo.getDisplayName();
									content = sendName + "：" + content;
									break;
								}
							}
						}
					}

					msgObject.put("content", content);
					long createTime = latestMsg.getCreateTime();
					if (String.valueOf(createTime).length() == 10) {
						createTime = createTime * 1000;
					}
					msgObject.put("timestamp", createTime);
					array.put(msgObject);
					unreadnum += unReadMsgVo.getUnReadMsg().size();
				}
				object.put("msglist", array);
				object.put("unreadnum", unreadnum);
			}
			result = object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
