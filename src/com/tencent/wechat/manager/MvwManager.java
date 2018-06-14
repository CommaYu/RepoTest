package com.tencent.wechat.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.iflytek.mvw.IMvwListener;
import com.iflytek.mvw.MvwSession;
import com.tencent.wechat.WeChatApplication;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 离线唤醒功能，采用独立的唤醒引擎，不依赖助理和msc<br>
 * Note: 在微信启动录音界面开始录音后，支持 发送发送 取消取消<br>
 */
public class MvwManager {

	private static final String TAG = "MvwManager";

	public static final String ROOT_PATH = "/mnt/sdcard/wechat";
	public static final String MVW_PATH = ROOT_PATH + "/cmvw";

	private Context mContext;

	private static MvwManager mInstance;

	private MvwSession mIvw = null;

	private static final int MVW_SCENE_WECHAT = 1;

	/* 发送发送 */
	private static final int MVW_ID_CONFIRM = 0;

	/* 取消取消 */
	private static final int MVW_ID_CANCLE = 1;

	private MvwManager(Context c) {
		mContext = c;
		mIvw = MvwSession.getInstance(mContext, mvwListener, MVW_PATH
				+ "/mvw/FirstRes");
	}

	public static MvwManager getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new MvwManager(c);
		}
		return mInstance;
	}

	public static MvwManager getInstance() {
		if (mInstance == null) {
			mInstance = new MvwManager(WeChatApplication.getContext());
		}
		return mInstance;
	}

	RecordManager.RecordListener mRecordListener = new RecordManager.RecordListener() {
		@Override
		public void onRecordData(byte[] data, int length) {
			// /*提取左声道数据送入唤醒引擎*/
			// byte[] leftBuffer = new byte[length / 2];
			// int j = 0;
			// for (int i = 0; i < length; i++) {
			// if (i % 4 >= 2) {
			// leftBuffer[j++] = data[i];
			// }
			// }

			// 防止引擎对此进行修改
			final byte[] writeData = data;

			if (null != writeData) {
				sendRecordBuffer(writeData);
			}
		}

		@Override
		public void onRecordStart() {
			// 开启一个唤醒场景
			int ret = mIvw.start(MVW_SCENE_WECHAT);
			Log.d(TAG, "onRecordStart: mIvw.start|ret=" + ret);
			Log.d(TAG, "onRecordStart: mIvw.start|ret=" + ret);
		}

		@Override
		public void onRecordStop() {
			mIvw.stop();
		}

		@Override
		public void onRecordCancel() {
			mIvw.stop();
		}

		@Override
		public void onFileSaved(String path) {

		}
	};

	private IMvwListener mvwListener = new IMvwListener() {

		@Override
		public void onVwInited(boolean state, int errId) {
			if (state) {
				Log.d(TAG, "onVwInited: mvw init success");
				// 开始接受音频
				RecordManager.getInstance().registerRecordListener(
						mRecordListener);
			} else {
				Log.d(TAG, "onVwInited: mvw init error|errorId=" + errId);
			}
		}

		@Override
		public void onVwWakeup(int nMvwScene, int nMvwId, int nMvwScore,
				String lParam) {
			Log.d(TAG, "onVwWakeup: nMvwScene=" + nMvwScene + "|nMvwId="
					+ nMvwId + "|nMvwScore=" + nMvwScore);
			if (nMvwScene == MVW_SCENE_WECHAT) {
				if (nMvwId == MVW_ID_CONFIRM) {
					Log.d(TAG, "onVwWakeup: confirm");
					if (mMvwManagerListener != null) {
						mMvwManagerListener.onConfirm();
					} else {
						Log.d(TAG, "onVwWakeup: mMvwManagerListener is null");
					}
				} else if (nMvwId == MVW_ID_CANCLE) {
					Log.d(TAG, "onVwWakeup: cancle");
					if (mMvwManagerListener != null) {
						mMvwManagerListener.oncancle();
					} else {
						Log.d(TAG, "onVwWakeup: mMvwManagerListener is null");
					}
				}
			}
		}
	};

	/************************ 增加唤醒音频处理线程 ******************************/
	private HandlerThread recordHandlerThread;
	private Handler recordHandler;

	/**
	 * 初始化处理线程
	 */
	public void initRecordHandlerThread() {
		recordHandlerThread = new HandlerThread("mvw-recordHandlerThread");
		recordHandlerThread.start();
		if (recordHandlerThread != null) {
			recordHandler = new Handler(recordHandlerThread.getLooper()) {
				@Override
				public void handleMessage(Message msg) {
					
					super.handleMessage(msg);

					Bundle bundle = msg.getData();
					// long startTime = bundle.getLong("START_TIME");
					byte[] buffer = bundle.getByteArray("RECORD_BUFFER");
					if (null != buffer && null != mIvw) {
						mIvw.appendAudioData(buffer);
					}
				}
			};

			Log.d(TAG, "initRecordHandlerThread init finish--->");
		} else {
			Log.d(TAG, "initRecordHandlerThread init fail--->");
		}
	}

	/**
	 * 发送录音给唤醒引擎
	 * 
	 * @param buffer
	 */
	public void sendRecordBuffer(byte[] buffer) {
		

		Bundle bundle = new Bundle();
		bundle.putByteArray("RECORD_BUFFER", buffer);
		Message msg = new Message();
		msg.setData(bundle);

		try {
			if (recordHandler == null || recordHandlerThread == null) {
				initRecordHandlerThread();
			}
			recordHandler.sendMessage(msg);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/******************** 增加唤醒音频处理线程 end *************************/

	private MvwManagerListener mMvwManagerListener;

	public void setMvwListener(MvwManagerListener listener) {
		mMvwManagerListener = listener;
	}

	/**
	 * 唤醒回调
	 */
	public interface MvwManagerListener {
		/**
		 * 发送发送
		 */
		void onConfirm();

		/**
		 * 取消取消
		 */
		void oncancle();
	}
}
