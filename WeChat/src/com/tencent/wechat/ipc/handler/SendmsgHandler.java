package com.tencent.wechat.ipc.handler;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fly.voice.util.RecordUtil;
import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.manager.Dispatch;
import com.tencent.wechat.manager.PcmUploadManager;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.widget.CustomToast;

/**
 * Author: congqin<br>
 * Data: 2016/12/9.<br>
 * Description: 处理助理发过来的指令 给xxx发微信xxxxxxxx<br>
 * Note:音频文件转码压缩上传，得到链接后，与文本合并后发送。<br>
 */
public class SendmsgHandler extends BaseHandler {

	private static final String TAG = "SendmsgHandler";

	@Override
	public String handle(final BridgeRequestVo requestVo) {

		Log.d(TAG, "handle: | current Thread id = "
				+ Thread.currentThread().getId());

		final String userName = requestVo.getId();
		final String voicePath = requestVo.getVoicepath();
		final String text = requestVo.getContext();

		final FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(userName);
		if (TextUtils.isEmpty(voicePath) || friendVo == null) {
			Log.w(TAG, "handle: voicepath empty or friendVo is null ");
			return BridgeContract.CONTACT_RESPOND_ERROR;
		}

		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {
			@Override
			public void run() {
				RecordUtil.pcm2mp3(voicePath);

				String url = PcmUploadManager.getInstance().uploadMediaFile(
						Constant.systemMap.get("UPLOAD_MEDIAFILE"),
						voicePath.replace(".pcm", ".mp3"));

				ReceiveMsgVO recMsg = new ReceiveMsgVO();
				recMsg.setMsgType(Constant.MSGTYPE_TEXT);
				recMsg.setToUserName(userName);

				FriendVo myInfo = WeChatMain.getWeChatMain().getMyInfo()
						.getData();
				recMsg.setFromUserName(myInfo.getUserName());
				recMsg.setSenderName(myInfo.getUserName());

				if (!TextUtils.isEmpty(url)) {
					recMsg.setContent(text
							+ "（"
							+ WeChatApplication.getContext().getString(
									R.string.mark_send_by_car) + url + "）");
					WeChatApplication.getBinder().sendMessage(recMsg);
				} else {// 音频上传失败
					CustomToast.showToast(
							WeChatApplication.getContext(),
							WeChatApplication.getContext().getString(
									R.string.tip_send_fail), Toast.LENGTH_LONG);
					recMsg.setContent(text);
					recMsg.setSendSuccess(false);
					WeChatApplication.getBinder().addMessage(recMsg, true);
				}
			}
		}, 0);
		SessionActivity.startMe(WeChatApplication.getContext(),
				new SessionInfo(userName), true);
		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
