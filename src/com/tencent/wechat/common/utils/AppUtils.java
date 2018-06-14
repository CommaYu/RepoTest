/**
 * Copyright (c) 2014,TravelSky.
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * <p/>
 * Project Name:TravelskyMobileTools
 * Package Name:com.travelsky.mrt.tmt.util
 * File Name:AppUtils.java
 * Date:2014-4-16 下午3:35:23
 */
package com.tencent.wechat.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tencent.wechat.WeChatApplication;

/**
 * 类名: AppUtils <br/>
 * 描述: <br/>
 * 日期: 2014-4-16 下午3:35:23 <br/>
 * <br/>
 * 
 * @author xhyin
 * @version 产品版本信息 yyyy-mm-dd xhyin 修改信息<br/>
 * @since 1.0
 */
public final class AppUtils {

	/**
	 * 消息标识
	 */
	public static final String TAG = AppUtils.class.getSimpleName();

	private static Context mContext = WeChatApplication.getContext();

	// 上次点击的时间
	private static long mLastClickTime = 0;

	// 间隔时间，默认为800ms
	private static final long INTERVAL_TIME = 800;

	private AppUtils() {

	}

	// 输入法管理器初始化
	private static InputMethodManager inputMethodManager = (InputMethodManager) mContext
			.getSystemService(Context.INPUT_METHOD_SERVICE);

	/**
	 * 获取输入法实例
	 * 
	 * @return 输入法实例
	 * @author xhyin
	 * @date 2014-10-21
	 * @since 1.0
	 */
	public static InputMethodManager getInputMethodManager() {
		return inputMethodManager;
	}

	/**
	 * 输入法活动状态
	 * 
	 * @return true输入法软键盘开启，false输入法软键盘关闭
	 * @author xhyin
	 * @date 2014-10-21
	 * @since 1.0
	 */
	public static boolean isActive() {
		return inputMethodManager.isActive();
	}

	/**
	 * 隐藏输入法
	 * 
	 * @param activity
	 *            指定隐藏输入法的Activity
	 * @author yuanye
	 * @date 2014-4-11
	 * @since 1.0
	 */
	public static void hideSoftInput(Activity activity) {
		if (activity != null) {
			hideSoftInput(activity.getWindow().getDecorView());
		}
	}

	/**
	 * 隐藏输入法
	 * 
	 * @param foucsView
	 *            当前处在最前面的拥有焦点的View,可通过下列方式进行获取<br/>
	 * @author zhangrui
	 * @date 2014-4-11
	 * @since 1.0
	 */
	public static void hideSoftInput(View foucsView) {
		if (foucsView != null) {
			inputMethodManager.hideSoftInputFromWindow(
					foucsView.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 判断是否快速连续点击
	 * 
	 * @return boolean true表示是快速连续点击；false表示不是快速连续点击
	 * @author zhanghdo
	 * @date 2013-10-24
	 * @since 1.0
	 */
	public static boolean isFastClick() {
		return isFastClick(INTERVAL_TIME);
	}

	/**
	 * 判断是否是快速点击
	 * 
	 * @return boolean true表示是快速连续点击；false表示不是快速连续点击
	 * @author ZHDong
	 * @date 2014-4-8
	 * @since 1.0
	 */
	public static boolean isFastClick(long intervalTime) {
		long time = System.currentTimeMillis();
		if (Math.abs(time - mLastClickTime) < intervalTime) {
			return true;
		}
		mLastClickTime = time;
		return false;
	}

	/**
	 * 获取绑定应用版本号
	 * 
	 * @return {@link int}版本号
	 * @author xhyin
	 * @date 2015-3-21
	 * @since 1.0
	 */
	public static int getAppVersion() {
		return getAppVersion(mContext);
	}

	/**
	 * 获取当前应用版本信息
	 * 
	 * @param context
	 * @return {@link int}版本信息
	 * @author xhyin
	 * @date 2015-3-21
	 * @since 1.0
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
		return 1;
	}

	/**
	 * 获取版本号
	 * 
	 * @param context
	 *            上下文
	 * @return 版本号
	 * @author xhyin
	 * @date 2014-4-11
	 * @since 1.0
	 */
	public static String getVersion(Context context) {
		String version = "";
		PackageManager packageManager = context.getPackageManager();

		PackageInfo info;
		try {
			info = packageManager.getPackageInfo(context.getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
		return version;
	}

	/**
	 * 获取应用名
	 * 
	 * @param context
	 * @return {@link String}应用名
	 * @author xhyin
	 * @date 2015-4-28
	 * @since 1.0
	 */
	public static String getApplicationName(Context context) {
		String appName = "";
		try {
			PackageManager packageManager = context.getPackageManager();
			ApplicationInfo applicationInfo = packageManager
					.getApplicationInfo(context.getPackageName(), 0);
			appName = packageManager.getApplicationLabel(applicationInfo)
					.toString();
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
		}
		return appName;
	}

	/**
	 * 获取顶层activity 全名称
	 * 
	 * @return
	 */
	public static String getCurrentActivity(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		String cn = am.getRunningTasks(1).get(0).topActivity.getClassName();
		Log.d(TAG, "getCurrentActivity = " + cn);
		return cn;
	}

	/** 判断应用是否安装 */
	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}

}
