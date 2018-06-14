package com.tencent.wechat;

import java.io.File;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;

import com.iflytek.clientadapter.recorder.aidl.RecorderListener;
import com.iflytek.clientadapter.recorder.aidl.RecorderSvc;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.sdk.interfaces.IHmiUiListener;
import com.iflytek.sdk.interfaces.IInitListener;
import com.iflytek.sdk.interfaces.IWakupListener;
import com.iflytek.sdk.manager.FlyHmiManager;
import com.iflytek.sdk.manager.FlySDKManager;
import com.iflytek.sdk.manager.FlySvwManager;
import com.iflytek.sdk.manager.FlyTtsManager;
import com.iflytek.utils.log.Logging;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.wechat.common.db.WeChatSQLiteOpenHelper;
import com.tencent.wechat.common.entity.SessionInfo;
import com.tencent.wechat.common.recorder.IPcmRecorder.OnPcmRecordListener;
import com.tencent.wechat.common.utils.FileUtil;
import com.tencent.wechat.exception.CrashHandler;
import com.tencent.wechat.http.HttpWeChat;
import com.tencent.wechat.http.ImageDownload;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.manager.AudioWrapperManager;
import com.tencent.wechat.manager.CustomMvwManager;
import com.tencent.wechat.manager.Dispatch;
import com.tencent.wechat.manager.FloatWindowManager;
import com.tencent.wechat.manager.IatManager;
import com.tencent.wechat.manager.MessageManager;
import com.tencent.wechat.manager.MvwManager;
import com.tencent.wechat.manager.NetworkManager;
import com.tencent.wechat.manager.NotifyManager;
import com.tencent.wechat.manager.PcmUploadManager;
import com.tencent.wechat.manager.RecordManager;
import com.tencent.wechat.manager.SpeakMessageManager;
import com.tencent.wechat.manager.TTSManager;
import com.tencent.wechat.manager.VoiceManager;
import com.tencent.wechat.service.ILocalBinder;
import com.tencent.wechat.service.WeChatService;
import com.tencent.wechat.ui.activity.SessionActivity;
import com.tencent.wechat.ui.activity.VoiceRecordActivity;

public class WeChatApplication extends Application {
	private static final String TAG = "WeChatApplication";
	private static Context mContext;
	private static HttpWeChat mWechat;
	private static ILocalBinder mBinder;
	private static SQLiteDatabase wechatDB;
	private WeChatSQLiteOpenHelper weChatSQLiteOpenHelper;

	private static DisplayImageOptions mOptions;
	private static ImageLoader mImageLoader = ImageLoader.getInstance();

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Log.d(TAG, "onServiceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Log.d(TAG, "onServiceConnected name " + name.getPackageName());
			mBinder = (ILocalBinder) service;
		}
	};

	public static void exitApp() {
		Logging.e(TAG, "exitApp()");
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}

	public static RecorderSvc recorderSvc;
	private static OnPcmRecordListener mOnPcmRecordListener;
	private static RecorderListener.Stub listener = new RecorderListener.Stub() {

		@Override
		public void onRecorderDataReceiver(byte[] data, int len)
				throws RemoteException {
			Log.d(TAG, "-----> onRecorderDataReceiver： len = " + len);
			int leftLen, rightLen;
			byte[] leftData, rightData;
			leftLen = rightLen = data.length / 2;
			leftData = new byte[leftLen];
			rightData = new byte[rightLen];
			// 左右声道分开
			for (int i = 0, j = 0, k = 0; i < data.length; i++) {
				if (i % 4 < 2) {
					leftData[j++] = data[i];
				} else {
					rightData[k++] = data[i];
				}
			}
			// 识别用左声道
			if (mOnPcmRecordListener != null) {
				mOnPcmRecordListener.onRecordData(leftData, leftLen);
			}
		}

		@Override
		public void onError(int code, String msg) throws RemoteException {

		}
	};

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "-----> onServiceConnected： name = " + name);
			recorderSvc = RecorderSvc.Stub.asInterface(service);
			try {
				recorderSvc.registerListener(listener);
				// recorderSvc.startRecord();
			} catch (RemoteException e) {
				Log.d(TAG, "-----> RemoteException: " + e.getMessage());
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "-----> onServiceDisconnected： name = " + name);
			recorderSvc = null;
		}

	};

	public static void setPcmRecordListener(
			OnPcmRecordListener onPcmRecordListener) {
		mOnPcmRecordListener = onPcmRecordListener;
	}

	private void startWeChatService() {
		Log.d(TAG, "startWeChatService");
		Intent intent = new Intent(this, WeChatService.class);
		intent.setPackage("com.tencent.wechat");
		bindService(intent, connection, BIND_AUTO_CREATE);

		Intent myIntent = new Intent(
				"com.iflytek.clientadapter.recorder.RecorderService")
				.setPackage("com.iflytek.autofly.voicecoreservice");
		bindService(myIntent, conn, Context.BIND_AUTO_CREATE);
	}

	/**
	 * 初始化ImageLoader
	 */
	private void initImageLoad() {
		mOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.mipmap.image_loading_icon)
				// 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.mipmap.image_loading_icon)
				// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.mipmap.image_loading_icon)
				// 设置图片加载或解码过程中发生错误显示的图片
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
				.cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中
				// .displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
				.build(); // 创建配置过的DisplayImageOption对象

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).defaultDisplayImageOptions(mOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.imageDownloader(new ImageDownload(getApplicationContext()))// 替换下载器
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		mImageLoader.init(config);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initImageLoad();
		weChatSQLiteOpenHelper = new WeChatSQLiteOpenHelper(this);
		wechatDB = weChatSQLiteOpenHelper.getWritableDatabase();
		if (mContext == null) {
			mContext = getApplicationContext();
		}
		if (mWechat == null) {
			mWechat = WeChatMain.getWeChatMain();
		}
		CrashHandler.getInstance().init(mContext);
		SpeechUtility.createUtility(mContext, "appid=5874904d");

		// 初始化语音助理
		FlyTtsManager.getInstance(mContext);
		FlySvwManager.getInstance(mContext);
		// 初始化end
		SpeakMessageManager.getInstance();
		PcmUploadManager.getInstance(mContext);
		VoiceManager.getInstance(mContext);
		RecordManager.getInstance(mContext);
		IatManager.getInstance(mContext);
		TTSManager.getInstance(mContext);
		CustomMvwManager.getInstance(mContext);
		FloatWindowManager.getInstance();
		NotifyManager.getInstance();
		AudioWrapperManager.getInstance(mContext);
		NetworkManager.getInstance(mContext);
		Dispatch.getInstance();

		startWeChatService();

		registerScreenState();
		// 使用自定义唤醒,不需要检查资源
		// checkMvwRes();
		FlySDKManager.getInstance().init(mContext, new IInitListener() {

			@Override
			public void onSuccess() {
				Logging.d(TAG, "onSuccess");
				// 交互状态
				FlyHmiManager.getInstance().setListener(new IHmiUiListener() {

					@Override
					public void onError(int arg0, String arg1) {
						Logging.d(TAG, "onError");
					}

					@Override
					public void onInteractionEnd() {
						Logging.d(TAG, "onInteractionEnd");
						MessageManager.getInstance().setContinueShow(true);
					}

					@Override
					public void onInteractionStart() {
						Logging.d(TAG, "onInteractionStart");
						MessageManager.getInstance().setContinueShow(false);
						NotifyManager.getInstance().removeNotify();
					}

					@Override
					public void onVadEnd() {
						Logging.d(TAG, "onVadEnd");
					}

					@Override
					public void onVadStart() {
						Logging.d(TAG, "onVadStart");
					}

					@Override
					public void onResultText(String arg0, String arg1,
							String rawText, int arg2, int arg3) {
						Logging.d(TAG, "onResultText");
					}

					@Override
					public void onShowTips(String arg0) {
						Logging.d(TAG, "onShowTips");
					}

					@Override
					public void onVolume(int arg0) {
					}

					// @Override
					// public void onPartialResult(String result, int age,
					// int sex) {
					// // TODO Auto-generated method stub
					//
					// }
				});

			}

			@Override
			public void onError(int errorid, String errortips) {

			}
		}, new IWakupListener() {

			@Override
			public void onGlobalWakeup(String result, int score) {
				Logging.d("onGlobalWakeup", result);
				ReceiveMsgVO msgVO = NotifyManager.getInstance()
						.getCurrentMsg();
				if (null == msgVO) {
					return;
				}
				String fromUserName = msgVO.getFromUserName();
				if (null == fromUserName) {
					return;
				}
				if ("撤销屏蔽".equals(result) || "取消屏蔽".equals(result)) {
					NotifyManager.getInstance().clearShieldUse();
					TTSManager.getInstance().startSpeak("已为您" + result);
				}
				if ("屏蔽消息".equals(result)) {
					NotifyManager.getInstance().addShieldUse(msgVO);
					TTSManager.getInstance().startSpeak("已为您" + result);
				}
				if ("回复微信".equals(result)) {
					Intent intent = new Intent(WeChatApplication.getContext(),
							VoiceRecordActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("user", fromUserName);
					startActivity(intent);
				}
				if ("查看".equals(result)) {
					SessionActivity.startMe(WeChatApplication.getContext(),
							new SessionInfo(fromUserName), true);
				}
				if ("静音".equals(result)) {
					NotifyManager.getInstance().setIsMute(true);
					TTSManager.getInstance().startSpeak("已为您" + result);
				}
				if ("取消静音".equals(result)) {
					NotifyManager.getInstance().setIsMute(false);
					TTSManager.getInstance().startSpeak("已为您" + result);
				}
			}

			@Override
			public void onMainUIWakeup(String result, int score) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onOneshotWakeup(String result, int score) {
				// TODO Auto-generated method stub

			}
		});
	}

	// 检查自定义唤醒资源是否需要复制
	private void checkMvwRes() {
		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {
			@Override
			public void run() {
				File resFile = new File(MvwManager.MVW_PATH + "/mvw");
				if (!resFile.exists()) {
					Log.d(TAG, "start copy custom mvw res... ");
					FileUtil.copyAssets(mContext, "cmvw.zip",
							MvwManager.ROOT_PATH + "/tmp/cmvw.zip");
					FileUtil.unZip(MvwManager.ROOT_PATH + "/tmp/cmvw.zip",
							MvwManager.ROOT_PATH);
				} else {
					Log.d(TAG, "custom mvw res already exist!!");
				}
				MvwManager.getInstance(mContext);
			}
		}, 0);
	}

	private void registerScreenState() {
		final IntentFilter filter = new IntentFilter();
		// 屏幕灭屏广播
		filter.addAction("android.intent.autofly.SCREEN");

		BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, final Intent intent) {
				Log.d(TAG, "onReceive");
				String action = intent.getAction();

				if ("android.intent.autofly.SCREEN".equals(action)) {
					String screenState = intent.getStringExtra("status");
					if ("on".equals(screenState)) {
						MessageManager.getInstance().setIsScreenOn(true);
					} else if ("off".equals(screenState)) {
						MessageManager.getInstance().setIsScreenOn(false);
					}
					Log.d(TAG, "screenState");
				}
			}
		};
		Log.d(TAG, "registerReceiver");
		registerReceiver(mBatInfoReceiver, filter);
	}

	// private void initOkhttp() {
	// // HttpHeaders headers = new HttpHeaders();
	// // headers.put("commonHeaderKey1", "commonHeaderValue1"); //所有的 header 都
	// 不支持 中文
	// // headers.put("commonHeaderKey2", "commonHeaderValue2");
	// // HttpParams params = new HttpParams();
	// // params.put("commonParamsKey1", "commonParamsValue1"); //所有的 params 都
	// 支持 中文
	// // params.put("commonParamsKey2", "这里支持中文参数");
	//
	// //必须调用初始化
	// OkHttpUtils.init(this);
	// //以下都不是必须的，根据需要自行选择
	// OkHttpUtils.getInstance()//
	// .debug("OkHttpUtils") //是否打开调试
	// .setConnectTimeout(OkHttpUtils.DEFAULT_MILLISECONDS) //全局的连接超时时间
	// .setReadTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS) //全局的读取超时时间
	// .setWriteTimeOut(OkHttpUtils.DEFAULT_MILLISECONDS) //全局的写入超时时间
	// //.setCookieStore(new MemoryCookieStore())
	// //cookie使用内存缓存（app退出后，cookie消失）
	// .setCookieStore(new PersistentCookieStore());
	// //cookie持久化存储，如果cookie不过期，则一直有效
	// // .addCommonHeaders(headers) //设置全局公共头
	// // .addCommonParams(params);
	// }

	/**
	 * 加载图片
	 * 
	 * @param uri
	 *            地址
	 * @param imageView
	 *            ImageView
	 */
	public static void loadImage(String uri, ImageView imageView) {
		if (uri.startsWith("http")) {
			mImageLoader.displayImage(uri, imageView, mOptions);
		} else if (uri.startsWith("/")) {
			mImageLoader.displayImage("file://" + uri, imageView);
		}
	}

	public static ImageLoader getImageLoader() {
		return mImageLoader;
	}

	public static void destoryImageLoader() {
		mImageLoader.clearMemoryCache();
		mImageLoader.clearDiskCache();
		mImageLoader.destroy();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		destoryImageLoader();
	}

	public static HttpWeChat getHttpWeChat() {
		return mWechat;
	}

	public static Context getContext() {
		return mContext;
	}

	public static ILocalBinder getBinder() {
		return mBinder;
	}

	public static SQLiteDatabase getDB() {
		return wechatDB;
	}

}
