package com.tencent.wechat.common.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;

/**
 * Created by Administrator on 2016/7/18.
 */
public class SharedPreferencesUtil {

	private static final String IS_NOTIFY_KEY = "IS_NOTIFY";

	private static SharedPreferencesUtil instance;

	private static SharedPreferences sharedPreferences;

	private static SharedPreferences.Editor editor;

	public static SharedPreferencesUtil getInstance() {
		if (sharedPreferences == null) {
			instance = new SharedPreferencesUtil();
			sharedPreferences = WeChatApplication.getContext()
					.getSharedPreferences(Constant.SHAREDPREFERENCES_NAME,
							Activity.MODE_PRIVATE);
			editor = sharedPreferences.edit();
		}
		return instance;
	}

	public void saveIsNotify(boolean isNotify) {
		putBoolean(IS_NOTIFY_KEY, isNotify);
	}

	public boolean getIsNotify() {
		return sharedPreferences.getBoolean(IS_NOTIFY_KEY, true);
	}

	private void putBoolean(String key, boolean bool) {
		editor.putBoolean(key, bool);
		editor.commit();
	}

}
