package com.tencent.wechat.ui.activity;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.wechat.R;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.ipc.BridgeIntentResponse;
import com.tencent.wechat.manager.AudioWrapperManager;
import com.tencent.wechat.manager.CustomMvwManager;
import com.tencent.wechat.manager.IatManager;
import com.tencent.wechat.manager.MvwManager;
import com.tencent.wechat.manager.NotifyManager;
import com.tencent.wechat.manager.TTSManager;
import com.tencent.wechat.manager.TTSManager.TtsManagerListener;
import com.tencent.wechat.manager.VoiceManager;
import com.tencent.wechat.ui.widget.CircleImageView;
import com.tencent.wechat.ui.widget.CustomToast;
import com.tencent.wechat.ui.widget.RoundProgressBar;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 录音界面<br>
 * Note:<br>
 */
public class VoiceRecordActivity extends BaseActivity {

	private static final String TAG = "VoiceRecordActivity";

	private RelativeLayout mRecordLayout;
	private TextView mTipTV;
	private TextView mRecordTime;

	private static final int MAX_TIME = 20;// 最长录音时间
	private static final int MIN_TIME = 2;// 最短录音时间

	private float mRecord_Time;// 录音的时间
	private String toUserName;
	private MediaPlayer mediaPlayer;
	private static final float BEEP_VOLUME = 0.7f;
	private ImageView voiceRecordingImageView;
	private AnimationDrawable voiceAnimation = null;
	private RoundProgressBar roundProgressBar;
	private Button back_btn;
	private CircleImageView imageView;

	private final ReentrantLock recordLock = new ReentrantLock();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate: ");

		Intent intent = this.getIntent();
		toUserName = intent.getStringExtra("user");

		setContentView(R.layout.activity_voice_record);
		initView();
		setListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume: ");
		if (null != uiHandler) {
			uiHandler.sendEmptyMessageDelayed(MSG_STARTRECORD_TIPS, 200);
		}
		// 把消息静音，不能干扰录音
		NotifyManager.getInstance().setIsMute(true);
		TTSManager.getInstance().setAutoMuteStatus(true);
		// if (VoiceManager.getInstance().prepareRecord()) {
		// AudioWrapperManager.getInstance().requestAudioFocus();
		// playBeep();
		// // startTransaction();
		// } else {
		// Message.obtain(uiHandler, MSG_SHOW_TOAST,
		// getString(R.string.tip_create_recorder_fail)).sendToTarget();
		// finish();
		// Log.d(TAG, "onCreate: finish...");
		// }
	}

	private void initView() {
	        mTipTV = (TextView) findViewById(R.id.max_record_time_tv);
		voiceRecordingImageView = (ImageView) findViewById(R.id.AnimImageView);
		mRecordLayout = (RelativeLayout) findViewById(R.id.rl_voice_record);
		mRecordTime = (TextView) findViewById(R.id.voice_record_time);
		roundProgressBar = (RoundProgressBar) findViewById(R.id.roundProgressBar);
		back_btn = (Button) findViewById(R.id.back_btn);
		roundProgressBar.setMax(200);

		imageView = (CircleImageView) findViewById(R.id.user_heading_imageview);
		WeChatApplication.loadImage(WeChatMain.getWeChatMain().getAllFriendsMap().get(toUserName).getHeadImgUrl(),
				imageView);
	}

	private void setListener() {
		mRecordLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				commitTransaction();
			}
		});
		mRecordLayout.setClickable(false);
		back_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Log.d(TAG, "onClick: finish...");
			}
		});
		IatManager.getInstance().registerRealTimeListener(mIatListener);
		//IatManager.getInstance().registerIatListener(mIatListener);

		CustomMvwManager.getInstance().setRecordMvwListener(mMvwManagerListener);
	}

	private MvwManager.MvwManagerListener mMvwManagerListener = new MvwManager.MvwManagerListener() {
		@Override
		public void onConfirm() {
			commitTransaction();
		}

		@Override
		public void oncancle() {
			cancelTransaction();
		}
	};

	private IatManager.IatListener mIatListener = new IatManager.IatListener() {

		@Override
		public void onRecognizeResult(String result) {
		    mTipTV.setText(result);
		}

		@Override
		public void onEndOfSpeech() {
			commitTransaction();
		}
	};

	private static final int MSG_SHOW_PROGRESS = 0;
	private static final int MSG_SHOW_TOAST = 1;
	private static final int MSG_STARTRECORD = 2;
	private static final int MSG_STARTRECORD_TIPS = 3;

	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_SHOW_PROGRESS:
				if (isRecording) {
					mRecordTime.setText((int) mRecord_Time + "″");

					int progress = (int) mRecord_Time * 10;
					roundProgressBar.setProgress(progress);

					mRecord_Time += 0.2;
					if (isRecording) {
						uiHandler.sendMessageDelayed(uiHandler.obtainMessage(MSG_SHOW_PROGRESS), 200);
					}

					if (mRecord_Time >= MAX_TIME) {
						commitTransaction();
					}
				}
				break;

			case MSG_SHOW_TOAST:
				String tip = (String) msg.obj;
				CustomToast.showToast(VoiceRecordActivity.this, tip, Toast.LENGTH_SHORT);
				break;

			case MSG_STARTRECORD_TIPS:

				TTSManager.getInstance().setTtsManagerListener(new TtsManagerListener() {

					@Override
					public void onTtsStart() {
						Log.d(TAG, "onTtsStart()");
						if (null != uiHandler) {
							uiHandler.sendEmptyMessageDelayed(MSG_STARTRECORD, 1200);
						}
					}

					@Override
					public void onTtsComplete() {

						Log.d(TAG, "onTtsComplete()");

					}
				});
				TTSManager.getInstance().startSpeak("请说内容");

				break;
			case MSG_STARTRECORD:
				Log.d(TAG, "MSG_STARTRECORD start");
				if (VoiceManager.getInstance().prepareRecord()) {
					AudioWrapperManager.getInstance().requestAudioFocus();
					playBeep();
				} else {
					Log.d(TAG, "MSG_STARTRECORD start error");
					Message.obtain(uiHandler, MSG_SHOW_TOAST, getString(R.string.tip_create_recorder_fail))
							.sendToTarget();
					finish();
					Log.d(TAG, "onCreate: finish...");
				}
				break;
			default:
				break;
			}
		}
	};

	/* 开始录音播放提示音 */
	private void playBeep() {
		Log.d(TAG, "playBeep: ");
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (mediaPlayer == null) {
			mediaPlayer = MediaPlayer.create(this, R.raw.beep);
			mediaPlayer.setOnCompletionListener(beepListener);
			setVolumeControlStream(AudioManager.STREAM_SYSTEM);
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.start();
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			Log.d(TAG, "onCompletion: ");
			startTransaction();
			mTipTV.setText("");
			mRecordLayout.setClickable(true);
			if (mediaPlayer != null) {
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}
	};

	/* 开启录音帧动画 */
	private void startRecordAnimation() {
		voiceRecordingImageView.setImageResource(R.drawable.voice_play_anim);
		voiceAnimation = (AnimationDrawable) voiceRecordingImageView.getDrawable();
		voiceAnimation.start();
	}

	/* 关闭录音帧动画 */
	private void stopRecordAnimation() {
		voiceAnimation.stop();
		voiceRecordingImageView.setImageResource(R.mipmap.voice_recording_f1);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause: ");
		cancelTransaction();
		AudioWrapperManager.getInstance().abandonAudioFocus();
		// 撤销静音
		NotifyManager.getInstance().setIsMute(false);
		TTSManager.getInstance().setAutoMuteStatus(true);
		// 通知助理开启录音器
		boolean ret = BridgeIntentResponse.getInstance().requestRecordOn();
		Log.d(TAG, "startRecording: ret=======>" + ret);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy: ");
		super.onDestroy();
		CustomMvwManager.getInstance().setRecordMvwListener(null);
		//IatManager.getInstance().unRegisterIatListener(mIatListener);
		IatManager.getInstance().unRegisterRealTimeListener();
		// 撤销静音
		NotifyManager.getInstance().setIsMute(false);
		TTSManager.getInstance().setAutoMuteStatus(true);
		// 通知助理开启录音器
		boolean ret = BridgeIntentResponse.getInstance().requestRecordOn();
		Log.d(TAG, "startRecording: ret=======>" + ret);
	}

	private boolean isRecording = false;

	/* 开始录音和转写 */
	private void startTransaction() {
		recordLock.lock();
		try {
			Log.d(TAG, "startTransaction: ");
			if (!isRecording) {
				isRecording = true;
				// MvwManager.getInstance().setMvwListener(mMvwManagerListener);
				CustomMvwManager.getInstance().startWakeup_record();

				VoiceManager.getInstance().startRecord(toUserName);
				Message.obtain(uiHandler, MSG_SHOW_PROGRESS).sendToTarget();
			}
		} finally {
			recordLock.unlock();
		}
	}

	/* 提交操作，关闭界面，异步发送微信消息 */
	private void commitTransaction() {
		recordLock.lock();
		try {
			Log.d(TAG, "commitTransaction: ");
			if (mRecord_Time <= MIN_TIME) {
				cancelTransaction();
				Message.obtain(uiHandler, MSG_SHOW_TOAST, getString(R.string.tip_record_time_too_short)).sendToTarget();
				return;
			}
			if (isRecording) {
				isRecording = false;
				VoiceManager.getInstance().stopRecordAndSend();
				// MvwManager.getInstance().setMvwListener(null);
				CustomMvwManager.getInstance().stopWakeup_record();
			}
			SessionActivity.startMe(this, new SessionInfo(toUserName), false);
		} finally {
			recordLock.unlock();
		}
	}

	/* 取消操作，不发送微信消息，关闭界面 */
	private void cancelTransaction() {
		recordLock.lock();
		try {
			Log.d(TAG, "cancelTransaction: ");
			if (isRecording) {
				isRecording = false;
				VoiceManager.getInstance().cancel();
				// MvwManager.getInstance().setMvwListener(null);
				CustomMvwManager.getInstance().stopWakeup_record();
			}
			finish();
			Log.d(TAG, "cancelTransaction: finish...");
		} finally {
			recordLock.unlock();
		}
	}
}