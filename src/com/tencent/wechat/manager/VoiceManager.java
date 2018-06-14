package com.tencent.wechat.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.utils.AppUtils;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.BridgeIntentResponse;
import com.tencent.wechat.ui.widget.CustomToast;

/**
 * Author: congqin<br>
 * Data:2016/12/2.<br>
 * Description: 通过车机发送文字+语音url链接 的管理类<br>
 * Note: 封装了RecordManager IatManager PcmUploadManager<br>
 */
public class VoiceManager {

	private static final String TAG = "VoiceManager";

	private static final int MSG_UPLOAD_VOICE_FILE = 3;
	private static final int MSG_SEND_VOICE_MESSAGE = 4;

	private Context mContext;

	private static VoiceManager mInstance;

	private HandlerThread mHandlerThread;

	private VoiceHandler mVoiceHandler;

	// 消息中的文本部分是否构建完成
	private boolean textReady = false;

	// 消息中的url部分是否构建完成
	private boolean urlReady = false;

	private VoiceManager(Context c) {
		mContext = c;
		RecordManager.getInstance(c);
		PcmUploadManager.getInstance();
		IatManager.getInstance(c);

		RecordManager.getInstance().registerRecordListener(mRecordListener);
		IatManager.getInstance().registerIatListener(mIatListener);
		init();
	}

	public static VoiceManager getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new VoiceManager(c);
		}
		return mInstance;
	}

	public static VoiceManager getInstance() {
		if (mInstance == null) {
			mInstance = new VoiceManager(WeChatApplication.getContext());
		}
		return mInstance;
	}

	private void init() {
		mHandlerThread = new HandlerThread("work thread");
		mHandlerThread.start();
		mVoiceHandler = new VoiceHandler(mHandlerThread.getLooper());
	}

	private RecordManager.RecordListener mRecordListener = new RecordManager.RecordListener() {
		@Override
		public void onRecordData(byte[] data, int length) {

		}

		@Override
		public void onRecordStart() {

		}

		@Override
		public void onRecordStop() {

		}

		@Override
		public void onRecordCancel() {

		}

		@Override
		public void onFileSaved(String path) {
			Message.obtain(mVoiceHandler, MSG_UPLOAD_VOICE_FILE, path).sendToTarget();
		}
	};
	private IatManager.IatListener mIatListener = new IatManager.IatListener() {
		@Override
		public void onRecognizeResult(String result) {
			Log.d(TAG, "iat recognize result=========>" + result);
			textReady = true;
			recMsg.setContent(result + ((recMsg.getContent() == null) ? "" : recMsg.getContent()));
			Message.obtain(mVoiceHandler, MSG_SEND_VOICE_MESSAGE, result).sendToTarget();
		}

		@Override
		public void onEndOfSpeech() {

		}
	};

	private class VoiceHandler extends Handler {

		public VoiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_UPLOAD_VOICE_FILE:
				Log.d(TAG, "=====>MSG_UPLOAD_VOICE_FILE");
				String filePath = (String) msg.obj;
				if (TextUtils.isEmpty(filePath)) {
					Log.e(TAG, "====>filePath is null or empty");
					return;
				}
				String url = PcmUploadManager.getInstance().uploadMediaFile(Constant.systemMap.get("UPLOAD_MEDIAFILE"),
						filePath);
				if (!TextUtils.isEmpty(url)) {
					urlReady = true;

					recMsg.setContent((recMsg.getContent() == null ? "" : recMsg.getContent()) + "（"
							+ WeChatApplication.getContext().getString(R.string.mark_send_by_car) + url + "）");
					Message.obtain(mVoiceHandler, MSG_SEND_VOICE_MESSAGE, url).sendToTarget();
				} else {
					Log.e(TAG, "========>url is empty ");
					urlReady = false;
					recMsg.setToUserName(toUserName);
					FriendVo myInfo = WeChatMain.getWeChatMain().getMyInfo().getData();
					recMsg.setFromUserName(myInfo.getUserName());
					recMsg.setSenderName(myInfo.getUserName());
					recMsg.setSendSuccess(false);
					WeChatApplication.getBinder().addMessage(recMsg, true);
					CustomToast.showToast(WeChatApplication.getContext(),
							WeChatApplication.getContext().getString(R.string.tip_send_fail), Toast.LENGTH_LONG);
				}
				break;
			case MSG_SEND_VOICE_MESSAGE:
				Log.d(TAG, "=====>MSG_SEND_VOICE_MESSAGE");
				recMsg.setToUserName(toUserName);
				FriendVo myInfo = WeChatMain.getWeChatMain().getMyInfo().getData();
				recMsg.setFromUserName(myInfo.getUserName());
				recMsg.setSenderName(myInfo.getUserName());
				Log.d(TAG, "textReady=======>" + textReady + ";urlReady" + urlReady);
				if (textReady && urlReady) {
					Log.d(TAG, "=========>sending");
					WeChatApplication.getBinder().sendMessage(recMsg);
					textReady = false;
					urlReady = false;
				}
				break;
			default:
				break;
			}
		}
	}

	private ReceiveMsgVO recMsg;

	String toUserName;

	/**
	 * 开启录音和转写
	 * 
	 * @return 0 成功 -1 失败
	 */
	public int startRecord(String toUserName) {
		Log.d(TAG, "startRecord: ");
		textReady = false;
		urlReady = false;
		this.toUserName = toUserName;
		recMsg = new ReceiveMsgVO();
		recMsg.setMsgType(Constant.MSGTYPE_TEXT);
		RecordManager.getInstance().startRecord();
		return 0;
	}

	/**
	 * 接收录音并上传音频给后台，成功后将文字+url链接合并发送
	 * 
	 * @return 0成功 -1 失败
	 */
	public int stopRecordAndSend() {
		Log.d(TAG, "stopRecordAndSend: ");
		RecordManager.getInstance().stopRecord();
		return 0;
	}

	public void cancel() {
		Log.d(TAG, "cancel: ");
		RecordManager.getInstance().cancelRecord();
	}

	/**
	 * 判断能否创建内部录音器 首先判断语音助理是否安装，没有安装则直接返回true
	 * 安装了，则让助理关闭录音器，助理执行成功，则返回true,否则返回false.
	 * 
	 * @return true代表可以创建录音器，false不能创建录音器
	 */
	public boolean prepareRecord() {
		if (null == WeChatApplication.recorderSvc
				|| !AppUtils.isAppInstalled(mContext, Constant.PACKAGE_NAME_SPEECHCLIENT)) {
			return true;
		}
		// if (!AppUtils.isAppInstalled(mContext,
		// Constant.PACKAGE_NAME_SPEECHCLIENT)) {
		// return true;
		// }
		boolean ret = BridgeIntentResponse.getInstance().requestRecordOff();
		Log.d(TAG, "prepareRecord: ret=======>" + ret);
		return ret;
	}

}
