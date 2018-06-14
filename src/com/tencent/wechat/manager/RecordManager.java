package com.tencent.wechat.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.fly.voice.util.RecordUtil;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.recorder.IPcmRecorder;
import com.tencent.wechat.common.recorder.InnerPcmRecorder;
import com.tencent.wechat.common.recorder.OuterPcmRecorder;
import com.tencent.wechat.common.utils.WeChatUtil;

public class RecordManager {

	private static final String TAG = RecordManager.class.getSimpleName();

	/**
	 * 自身创建的录音器
	 */
	private static final int RECORDER_SOURCE_INNER = 0;
	/**
	 * 借助助理服务录音，自身不创建录音器
	 */
	private static final int RECORDER_SOURCE_OUTER = 1;
	/**
	 * 默认使用自身创建的录音器
	 */
	private static final int RECORDER_SOURCE_DEFAULT = RECORDER_SOURCE_OUTER;

	private static RecordManager mRecManager;

	private static Context mContext;

	private static ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	public static RecordManager getInstance(Context c) {
		if (null == mRecManager) {
			mRecManager = new RecordManager(c);
		}
		if (null != WeChatApplication.recorderSvc) {
			recorder = OuterPcmRecorder.getInstance();
		} else {
			recorder = InnerPcmRecorder.getInstance(mContext);
		}
		recorder.setOnPcmRecordListener(pcmRecordListener);
		return mRecManager;

	}

	public static RecordManager getInstance() {
		if (null == mRecManager) {
			mRecManager = new RecordManager(WeChatApplication.getContext());
		}
		if (null != WeChatApplication.recorderSvc) {
			recorder = OuterPcmRecorder.getInstance();
		} else {
			recorder = InnerPcmRecorder.getInstance(mContext);
		}
		recorder.setOnPcmRecordListener(pcmRecordListener);
		return mRecManager;
	}

	static IPcmRecorder recorder;

	private RecordManager(Context c) {
		this.mContext = c;
		if (null != WeChatApplication.recorderSvc) {
			recorder = OuterPcmRecorder.getInstance();
		} else {
			recorder = InnerPcmRecorder.getInstance(mContext);
		}
		recorder.setOnPcmRecordListener(pcmRecordListener);
	}

	private static final IPcmRecorder.OnPcmRecordListener pcmRecordListener = new IPcmRecorder.OnPcmRecordListener() {

		@Override
		public void onRecordData(byte[] dataBuffer, int length) {

			if (null != buffer) {
				try {

					// byte[] leftBuffer = new byte[length / 2];
					// int j = 0;
					// for (int i = 0; i < length; i++) {
					// if (i % 4 >= 2) {
					// leftBuffer[j++] = dataBuffer[i];
					// }
					// }
					if (null != dataBuffer) {
						buffer.write(dataBuffer);
						buffer.close();
					}
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

			if (!mRecordListenerList.isEmpty()) {
				for (RecordListener listener : mRecordListenerList) {
					listener.onRecordData(dataBuffer, length);
				}
			}
		}

	};

	public void initService() {
		// recSession.initService();
	}

	public void unbindService() {
		// recSession.unbindService();
	}

	public void startRecord() {

		// boolean audioFocusGranted =
		// WeChatApplication.getBinder().requestAudioFocus();
		boolean audioFocusGranted = true;
		TTSManager.getInstance().stopSpeak();
		if (audioFocusGranted) {
			if (!mRecordListenerList.isEmpty()) {
				for (RecordListener listener : mRecordListenerList) {
					listener.onRecordStart();
				}
			}
			recorder.startRecord();
		}
	}

	/**
	 * 录音完毕，保存录音文件，有文件保存的回调
	 */
	public void stopRecord() {
		recorder.stopRecord();

		if (!mRecordListenerList.isEmpty()) {
			for (RecordListener listener : mRecordListenerList) {
				listener.onRecordStop();
			}
		}

		if (buffer != null) {
			String voicePath = WeChatUtil.saveFile(Constant.VOICE_RECORD_PATH,
					"/record" + System.currentTimeMillis() + "" + ".pcm", buffer.toByteArray());

			Log.d(TAG, "voicePath=====>" + voicePath);
			RecordUtil.pcm2mp3(voicePath);
			clearBuffer();

			if (!mRecordListenerList.isEmpty()) {
				for (RecordListener listener : mRecordListenerList) {
					listener.onFileSaved(voicePath.replace(".pcm", ".mp3"));// voicePath.replace(".pcm",
																			// ".mp3")
				}
			}
		}
	}

	/**
	 * 取消录音，清除录音数据，不保存录音文件
	 */
	public void cancelRecord() {
		recorder.stopRecord();
		if (!mRecordListenerList.isEmpty()) {
			for (RecordListener listener : mRecordListenerList) {
				listener.onRecordCancel();
			}
		}
		clearBuffer();
	}

	public ByteArrayOutputStream getBuffer() {
		return buffer;
	}

	private void clearBuffer() {
		if (null != buffer && buffer.size() > 0) {
			buffer.reset();
		}
	}

	private static List<RecordListener> mRecordListenerList = new ArrayList<RecordListener>();

	public void registerRecordListener(RecordListener l) {
		if (!mRecordListenerList.contains(l)) {
			mRecordListenerList.add(l);
		}
	}

	public interface RecordListener {

		void onRecordData(byte[] data, int length);

		void onRecordStart();

		void onRecordStop();

		void onRecordCancel();

		void onFileSaved(String path);
	}
}
