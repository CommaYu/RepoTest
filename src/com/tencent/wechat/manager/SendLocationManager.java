package com.tencent.wechat.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.common.utils.APISecretUtil;
import com.tencent.wechat.http.OkHttpUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.PoiInfoVo;

/**
 * Author: congqin<br>
 * Data: 2016/12/2<br>
 * Description: 负责发送位置消息<br>
 * Note: 根据位置信息，调用后台接口，获取h5地址，构造文本，再调用wechatservice 发文本消息的接口<br>
 */
public class SendLocationManager {

	private static final String TAG = "SendLocationManager";

	private static SendLocationManager mInstance;

	private SendLocationManager() {
	}

	public static SendLocationManager getInstance() {
		if (mInstance == null) {
			mInstance = new SendLocationManager();
		}
		return mInstance;
	}

	/**
	 * 发位置消息
	 * 
	 * @param toUserName
	 *            要发给哪个好友
	 * @param poiInfoVo
	 *            poi信息
	 */
	public void sendLocation(final String toUserName, final PoiInfoVo poiInfoVo) {

		if (poiInfoVo == null || TextUtils.isEmpty(toUserName)) {
			Log.e(TAG, "sendLocation: poiInfoVo is null");
			return;
		}

		Dispatch.getInstance().postRunnableByExecutors(new Runnable() {
			@Override
			public void run() {
				String name = poiInfoVo.getName();
				String coord_type = poiInfoVo.getCoord_type();
				String lat = poiInfoVo.getLatitude();
				String lng = poiInfoVo.getLongitude();
				Log.d(TAG, "run: lat=" + lat + ";lng=" + lng);

				Map<String, String> params = new HashMap<String, String>();
				params.put("lat", lat);
				params.put("lng", lng);
				params.put("type", matchCoordType(coord_type));
				String sign = null;
				try {
					sign = APISecretUtil.generateSign(Constant.OPENID,
							Constant.OPENKEY, null, params);
				} catch (Exception e) {
					e.printStackTrace();
				}

				params.put("sign", sign);
				params.put("openId", Constant.OPENID);

				String retUrl = "";
				try {
					String result = OkHttpUtil.getStringSync(
							Constant.systemMap.get("UPLOAD_POI"), params);
					Log.d(TAG, "run: result=" + result);

					JSONObject json = new JSONObject(result);
					if (json.getInt("code") == 0) {
						retUrl = (new JSONObject(json.get("data").toString())).getString("url");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				ReceiveMsgVO recMsg = new ReceiveMsgVO();

				recMsg.setMsgType(Constant.MSGTYPE_TEXT);
				recMsg.setToUserName(toUserName);

				if (!TextUtils.isEmpty(retUrl)) {
					recMsg.setContent("我在" + name + "（" + "位置：" + retUrl + "）");
				} else {
					recMsg.setContent("我在" + name);
				}

				FriendVo myInfo = WeChatMain.getWeChatMain().getMyInfo()
						.getData();
				recMsg.setFromUserName(myInfo.getUserName());
				recMsg.setSenderName(myInfo.getUserName());

				WeChatApplication.getBinder().sendMessage(recMsg);
			}
		}, 0);
	}

	/* 助理对接协议中的坐标系名称和后台接口中的坐标系名称不一样，以下进行转换 */
	private String matchCoordType(String coord_type) {
		if (TextUtils.equals(coord_type, BridgeContract.COORD_TYPE_WGS)) {
			return Constant.COORD_TYPE_WGS84;
		} else if (TextUtils.equals(coord_type, BridgeContract.COORD_TYPE_BD9)) {
			return Constant.COORD_TYPE_BD09;
		} else if (TextUtils.equals(coord_type, BridgeContract.COORD_TYPE_BDL)) {
			return Constant.COORD_TYPE_BD09MC;
		} else if (TextUtils.equals(coord_type, BridgeContract.COORD_TYPE_GCJ)) {
			return Constant.COORD_TYPE_GJC02;
		} else {
			return Constant.COORD_TYPE_GJC02; // 默认为火星坐标系
		}
	}
}
