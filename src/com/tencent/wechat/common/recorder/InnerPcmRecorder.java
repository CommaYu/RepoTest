package com.tencent.wechat.common.recorder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.tencent.wechat.ipc.BridgeIntentResponse;

public class InnerPcmRecorder implements IPcmRecorder {
	private static final String TAG = "InnerPcmRecorder";

	public static final int DEFAULT_SAMPLE_RATE = 16 * 1000;

	private static final short DEFAULT_BIT_SAMPLES = 16;
	private static final int RECORD_BUFFER_TIMES_FOR_FRAME = 10;

	public static final int DEFAULT_TIMER_INTERVAL = 40;
	private static final short DEFAULT_CHANNELS = 1;// 1为单声道，2为双声道

	private byte[] mBuffer = null;

	/**
	 * 系统录音器
	 */
	private AudioRecord mRecorder = null;

	private Object mReadLock = new Object();
	private boolean mIsRecording = false;
	private static Context mContext = null;
	public static int START_RECORDING_SUCCESS = 0;
	public static final int START_RECORDING_ERROR = -30001;
	/**
	 * 为了支持通过录音方式自动化测试,增加该参数
	 */
	private static RandomAccessFile mTestRecordFile = null;
	/**
	 * 当前 降噪模块处于 什么模式下面 降噪模式 （识别）=0 ； 唤醒模式 （唤醒） = 1
	 */
	public static InnerPcmRecorder instance = null;

	/***
	 * 发送录音缓存的handle
	 */
	private HandlerThread mRecordThread;
	private Handler mRecordHandler;

	public static synchronized InnerPcmRecorder getInstance(Context context) {
		if (instance == null) {
			try {
				instance = new InnerPcmRecorder();
				mContext = context;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	private InnerPcmRecorder() throws Exception {
		initWorkThread();
	}

	private void initWorkThread() {
		if (null == mRecordThread) {
			mRecordThread = new HandlerThread("initWorkThread");
			mRecordThread.start();
			mRecordHandler = new Handler(mRecordThread.getLooper()) {
				@Override
				public void handleMessage(Message msg) {
					
					super.handleMessage(msg);
				}
			};
		}
	}

	private void initAudioRecord(short channels, short bitSamples,
			int sampleRate, int timeInterval) throws Exception {
		if (timeInterval % DEFAULT_TIMER_INTERVAL != 0) {
			Log.e(TAG, "parameter error, timeInterval must be multiple of "
					+ DEFAULT_TIMER_INTERVAL);
			throw new Exception();
		}
		int framePeriod = sampleRate * timeInterval / 1000;
		int recordBufferSize = framePeriod * RECORD_BUFFER_TIMES_FOR_FRAME
				* bitSamples * channels / 8;
		int channelConfig = (channels == 1 ? AudioFormat.CHANNEL_IN_MONO
				: AudioFormat.CHANNEL_IN_STEREO);
		int audioFormat = (bitSamples == 16 ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT);

		int min = AudioRecord.getMinBufferSize(sampleRate, channelConfig,
				audioFormat);
		if (recordBufferSize < min) {
			recordBufferSize = min;
			Log.w("PCM recorder",
					"Increasing buffer size to "
							+ Integer.toString(recordBufferSize));
		}

		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
				channelConfig, audioFormat, recordBufferSize);

		if (mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
			Log.e(TAG, "create AudioRecord error " + mRecorder.getState());
			mRecorder.release();
			mRecorder = null;
		}

		mBuffer = new byte[recordBufferSize / 4];
		Log.d(TAG, "create AudioRecord ok buffer size=" + mBuffer.length);
	}

	/**
	 * 设置测试的录音文件路径
	 * 
	 * @param filepath
	 */
	public static void setTestRecordFile(String filepath) {
		if (null != mTestRecordFile) {
			try {
				mTestRecordFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			mTestRecordFile = new RandomAccessFile(filepath, "r");
			Log.d(TAG, "setTestRecordFile " + filepath);
		} catch (FileNotFoundException e) {
			mTestRecordFile = null;
			e.printStackTrace();
		}

	}

	private int readRecordData() {
		int count = 0;
		try {
			if (mRecorder != null) {
				if (mRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
					Log.d(TAG, "RECORDSTATE_RECORDING error");
					return 0;
				}
				count = mRecorder.read(mBuffer, 0, mBuffer.length);

				if (mRecordListener != null) {
					mRecordListener.onRecordData(mBuffer, count);
				}

			} else {
				Log.d(TAG, "readRecordData null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	private void startReadThread() {
		Thread readThread = new Thread("PcmRecorderNew") {
			@Override
			public void run() {
				Log.d(TAG, "startReadThread OK=" + this.getId());
				// android.os.Process
				// .setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
				while (mIsRecording) {
					// synchronized (mReadLock) {
					readRecordData();
					// }
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Log.d(TAG, "startReadThread finish=" + this.getId());
			}
		};
		readThread.start();

	}

	/**
	 * source代表是哪一个启动录音 有ivw 和 sr
	 */
	public int startRecording() {
		synchronized (mReadLock) {
			if (mRecorder == null) {
				// release();
				try {
					initAudioRecord(DEFAULT_CHANNELS, DEFAULT_BIT_SAMPLES,
							DEFAULT_SAMPLE_RATE, DEFAULT_TIMER_INTERVAL);
					if (mRecorder == null) {
						return START_RECORDING_ERROR;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mIsRecording = true;
				mRecorder.startRecording();
				startReadThread();
			}

		}
		return START_RECORDING_SUCCESS;
	}

	public void stopRecording() {
		if (mRecorder != null) {
			Log.d(TAG, "stopRecording into");
			mIsRecording = false;
			synchronized (mReadLock) {
				if (mRecorder != null) {
					mRecorder.stop();
				}
				release();
				try {
					// 防止部分车型 结束录音之后 迅速开启录音 导致N多问题
					Thread.sleep(50);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			}

			Log.d(TAG, "stopRecording end");
		}

		// 通知助理开启录音器
		boolean ret = BridgeIntentResponse.getInstance().requestRecordOn();
		Log.d(TAG, "startRecording: ret=======>" + ret);
	}

	private void release() {
		mIsRecording = false;
		if (null != mRecorder
				&& mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
			stopRecording();
		}
		// FIXME 在部分机器上release后 read方法会阻塞,增加
		Log.d(TAG, "release begin");
		// synchronized (mReadLock) {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		Log.d(TAG, "release ok");
		// }

		if (null != mTestRecordFile) {
			try {
				mTestRecordFile.close();
			} catch (IOException e) {
			}
			mTestRecordFile = null;
		}
		Log.d(TAG, "release end");
	}

	public int getSampleRate() {
		if (mRecorder != null) {
			return mRecorder.getSampleRate();
		} else {
			return DEFAULT_SAMPLE_RATE;
		}
	}

	public boolean isRecording() {
		if (mRecorder != null) {
			return mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
		} else {
			return false;
		}
	}

	@Override
	public int startRecord() {
		return startRecording();
	}

	@Override
	public int stopRecord() {
		stopRecording();
		return 0;
	}

	@Override
	public void setOnPcmRecordListener(OnPcmRecordListener listener) {
		mRecordListener = listener;
	}

	private OnPcmRecordListener mRecordListener;
}
