package com.tencent.wechat.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Author: congqin<br>
 * Data:2016/12/2.<br>
 * Description: 网络管理类，确定wifi 移动流量 是否可用<br>
 * Note:<br>
 */
public class NetworkManager {

	private static final String TAG = "NetworkManager";

	private static NetworkManager mInstance;

	private Context mContext;

	private ConnectivityManager conManager;

	private boolean networkAvailable = false;

	private NetworkManager(Context context) {
		this.mContext = context;
		conManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mContext.registerReceiver(mConnReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		setNetState();
	}

	private void setNetState() {

		boolean isWifiAvailable = false;
		boolean isMobileAvailable = false;

		NetworkInfo activeNetwork = conManager.getActiveNetworkInfo();
		if (activeNetwork != null) {
			if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
					&& activeNetwork.isConnected()) {
				isWifiAvailable = true;
			} else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE
					&& activeNetwork.isConnected()) {
				isMobileAvailable = true;
			}
		}
		Log.d(TAG, "setNetState: isWifiAvailable=" + isWifiAvailable
				+ "|isMobileAvailable=" + isMobileAvailable);
		boolean tmp = isMobileAvailable || isWifiAvailable;
		if (tmp != networkAvailable) {
			networkAvailable = tmp;
			for (NetworkListener l : listenerList) {
				if (networkAvailable) {
					l.onNetworkAvailable();
				} else {
					l.onNetworkUnAvailable();
				}
			}
		}
	}

	public static NetworkManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new NetworkManager(context);
		}
		return mInstance;
	}

	public static NetworkManager getInstance() {
		return mInstance;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @return
	 */
	public boolean isNetworkAvailable() {
		return networkAvailable;
	}

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
					.getAction())) {
				setNetState();
			}

		}
	};

	private List<NetworkListener> listenerList = new ArrayList<NetworkListener>();

	public void registerListener(NetworkListener l) {
		if (!listenerList.contains(l)) {
			listenerList.add(l);
		}
	}

	public void unRegisterListener(NetworkListener l) {
		if (listenerList.contains(l)) {
			listenerList.remove(l);
		}
	}

	public interface NetworkListener {
		/**
		 * 当网络变得不可用的回调
		 */
		void onNetworkAvailable();

		/**
		 * 当网络变得可用的回调
		 */
		void onNetworkUnAvailable();
	}

}
