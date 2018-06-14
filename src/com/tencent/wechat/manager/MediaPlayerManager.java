package com.tencent.wechat.manager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.tencent.wechat.WeChatApplication;

/**
 * Created by Administrator on 2016/6/21.
 */
public class MediaPlayerManager {

	private static final String TAG = "MediaPlayer";

	private static MediaPlayerManager mInstance;

	private static MediaPlayer mMediaPlayer;

	private AudioManager mAudioManager;

	public static MediaPlayerManager getInstance() {
		if (mInstance == null) {
			mInstance = new MediaPlayerManager();
		}
		return mInstance;
	}

	private MediaPlayerManager() {
		mAudioManager = (AudioManager) WeChatApplication.getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		mMediaPlayer = new MediaPlayer();

		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						resumeVolume();
						if (listener != null) {
							listener.onMediaPlayerStop();
						}
					}
				});
	}

	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	/**
	 * 开始播放音频
	 * 
	 * @param name
	 *            播放的音频文件地址
	 */
	public void startPlay(String name) {
		setToMaxVolume();
		File dir = new File(name);
		FileInputStream fis = null;
		try {
			mMediaPlayer.reset();
			fis = new FileInputStream(dir);
			FileDescriptor fd = fis.getFD();
			mMediaPlayer.setDataSource(fd);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onMediaPlayerStop();
			}
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private int currentVolume;

	private void resumeVolume() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume,
				0);
	}

	private void setToMaxVolume() {
		currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		Log.d(TAG, "setToMaxVolume: currentVolume = " + currentVolume);

		int maxvolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxvolume, 0);

		// int volume =
		// mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// Log.d(TAG, "getMaxStreamVolume: maxvolume=" + maxvolume);
		// Log.d(TAG, "getMaxStreamVolume: volume=" + volume);
		//
		//
		// mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxvolume,
		// 0);
		// volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// Log.d(TAG, "getMaxStreamVolume: volume=" + volume);

	}

	/**
	 * 暂停后恢复播放
	 */
	public void resume() {
		mMediaPlayer.start();
	}

	/**
	 * 停止播放
	 */
	public void stopPlay() {
		resumeVolume();
		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			// mMediaPlayer.release(); release掉后就不能再调用 reset()了
		}
	}

	/**
	 * 暂停播放
	 */
	public void pause() {
		if (mMediaPlayer.isPlaying()) {
			Log.d("MediaPlayer暂停播放", "pause");
			mMediaPlayer.pause();
		}
	}

	private MediaPlayerListener listener;

	public void setMediaPlayerListener(MediaPlayerListener l) {
		listener = l;
	}

	interface MediaPlayerListener {
		/**
		 * 开始播放
		 */
		void onMediaPlayerStart();

		/**
		 * 停止播放，播完了，被打断不回调
		 */
		void onMediaPlayerStop();
	}

}
