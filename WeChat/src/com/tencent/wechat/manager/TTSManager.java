package com.tencent.wechat.manager;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.iflytek.sdk.interfaces.ITtsUiListener;
import com.iflytek.sdk.manager.FlyTtsManager;
import com.iflytek.utils.log.Logging;
import com.tencent.wechat.WeChatApplication;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: tts播报管理类<br>
 * Note:<br>
 */
public class TTSManager {

	private static final String TAG = "TTSManager";

	private static Context mContext;
	private static TTSManager mInstance;

	private FlyTtsManager agent;

	public static TTSManager getInstance() {
		return mInstance;
	}

	public static TTSManager getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new TTSManager(c);
		}
		return mInstance;
	}

	private TTSManager(Context c) {
		mContext = c;
		agent = FlyTtsManager.getInstance();
	}

	// public void setStream(int stream) {
	// agent.create(stream);
	// }

	public ITtsUiListener mITtsUiListener = new ITtsUiListener() {

		@Override
		public void onProgress(int textindex, int textlen) {
			

		}

		@Override
		public void onPlayCompleted() {
			
			Log.d(TAG, "onPlayCompleted()");
			if (listener != null) {
				setAutoMuteStatus(false);
				listener.onTtsComplete();
			}
		}

		@Override
		public void onPlayBegin() {
			
			Log.d(TAG, "onPlayBegin()");
			if (null != listener) {
				setAutoMuteStatus(true);
				listener.onTtsStart();
			}
		}

		@Override
		public void onInterrupted() {
			
			if (listener != null) {
				setAutoMuteStatus(false);
				listener.onTtsComplete();
			}
		}

		@Override
		public void onError(int errorid) {
			
			if (listener != null) {
				setAutoMuteStatus(false);
				listener.onTtsComplete();
			}
		}
	};

	// ITtsClientListener ittsListener = new ITtsClientListener() {
	//
	// @Override
	// public void onTtsInited(boolean arg0, int arg1) {
	// Log.d(TAG, "onTtsInited:  arg0=" + arg0 + "|arg1=" + arg1);
	// }
	//
	// @Override
	// public void onProgressReturn(int arg0, int arg1) {
	// Log.d(TAG, "onProgressReturn:  arg0=" + arg0 + "|arg1=" + arg1);
	// }
	//
	// @Override
	// public void onPlayInterrupted() {
	// Log.d(TAG, "onPlayInterrupted: ");
	// }
	//
	// @Override
	// public void onPlayCompleted() {
	// Log.d(TAG, "onPlayCompleted: ");
	// if (listener != null) {
	// listener.onTtsComplete();
	// }
	// }
	//
	// @Override
	// public void onPlayBegin() {
	// Log.d(TAG, "onPlayBegin: ");
	// }
	// };

	public int startSpeak(String str) {
		agent.create(AudioManager.STREAM_SYSTEM);
		int ret = agent.speak(str, mITtsUiListener);
		Log.d(TAG, "startSpeak: ret=" + ret);
		return 0;
	}

	public int stopSpeak() {
		agent.stop();
		return 0;
	}

	private TtsManagerListener listener;

	public TtsManagerListener getTtsManagerListener() {
		return listener;
	}

	public void setTtsManagerListener(TtsManagerListener l) {
		listener = l;
	}

	public interface TtsManagerListener {
		/**
		 * 开始播放
		 */
		void onTtsStart();

		/**
		 * 停止播放，tts播完了，tts被打断的情况下不回调
		 */
		void onTtsComplete();
	}
	
	/**
     * 高德 AmapAuto车机车镜，地图播报
     * 
     * @param isMute
     *            true 静音 false 取消静音
     * @param EXTRA_MUTE
     *            永久静音
     */
    public void setAutoMuteStatus(boolean isMute) {
        Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", 10047);
        if (isMute) {
            Logging.d(TAG, "automutestatus-静音");
            intent.putExtra("EXTRA_MUTE", 1);
        } else {
            Logging.d(TAG, "automutestatus-取消静音");
            intent.putExtra("EXTRA_MUTE", 0);
        }
        WeChatApplication.getContext().sendBroadcast(intent);
    }

}
