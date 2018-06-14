package com.tencent.wechat.manager;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.utils.APISecretUtil;
import com.tencent.wechat.common.utils.QArrays;

/**
 * Author: congqin <br>
 * Data:2016/12/2.<br>
 * Description: 负责将音频上传至自主服务器，并获得h5链接<br>
 * Note:
 */
public class PcmUploadManager {

	private static final String TAG = PcmUploadManager.class.getSimpleName();

	private static PcmUploadManager mInstance;

	private Context mContext;

	private PcmUploadManager(Context context) {
		mContext = context;
	}

	public static PcmUploadManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PcmUploadManager(context);
		}
		return mInstance;
	}

	public static PcmUploadManager getInstance() {
		if (mInstance == null) {
			Log.e(TAG, "getInstance: getInstance() with param not called");
		}
		return mInstance;
	}

	/**
	 * @param actionUrl
	 *            自主服务器提供的音频上传接口
	 * @param filePath
	 *            本地音频文件的路径
	 * @return 返回h5页面的url, 手机上点击该链接可听到原始音频
	 */
	public String uploadMediaFile(String actionUrl, String filePath) {
		Log.d(TAG, "uploadMediaFile: filePath = " + filePath);
		String url = "";
		File file = new File(filePath);
		if (file == null) {
			Log.d(TAG, "uploadMediaFile: file is null");
		}

		Map<String, String> params = new HashMap<String, String>();
		params.put("appName", "WXAPP");
		String sign = null;
		try {
			sign = APISecretUtil.generateSign(Constant.OPENID,
					Constant.OPENKEY, null, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpClient client = new HttpClient();
		actionUrl = actionUrl + "?openId=" + Constant.OPENID + "&appName=WXAPP"
				+ "&sign=" + sign;
		Log.d(TAG, "uploadMediaFile() actionUrl = " + actionUrl);
		PostMethod filePost = new PostMethod(actionUrl);
		filePost.setParameter("appName", "WXAPP");

		filePost.setParameter("openId", Constant.OPENID);
		filePost.setParameter("sign", sign);

		getDeviceId();
		try {
			Part[] parts = { new FilePart(file.getName(), file) };
			filePost.setRequestEntity(new MultipartRequestEntity(parts,
					filePost.getParams()));
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(5000);

			// ///////////////okhttp
			// String result = OkHttpUtil.postMedia(actionUrl, null, file);
			// Log.d(TAG, "uploadMediaFile: okhttp result=" + result);
			// ///////////////okhttp

			int code = client.executeMethod(filePost);
			InputStream in = filePost.getResponseBodyAsStream();
			String result = QArrays.inputStream2String(in);
			Log.i(TAG, "upload result = " + result + " code = " + code);

			JSONObject json = new JSONObject(result);
			if (json.getInt("code") == 0) {
				JSONArray jsonArray = (new JSONObject(json.get("data")
						.toString())).getJSONArray("url");
				int iSize = jsonArray.length();
				for (int i = 0; i < iSize; i++) {
					url = jsonArray.get(i).toString();
					String voiceUrl = url;
					Log.i("uploadMediaFile", "voiceUrl = " + voiceUrl);
				}
			}
		} catch (Exception e) {
			Log.i(TAG, "err in upload " + e.toString());
		}
		return url;
	}

	private String getDeviceId() {

		TelephonyManager mTelephonyMgr = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		String imei = mTelephonyMgr.getDeviceId();
		Log.d(TAG, "getDeviceId: imsi=" + imsi);
		Log.d(TAG, "getDeviceId: imei=" + imei);
		return imei;
	}

}
