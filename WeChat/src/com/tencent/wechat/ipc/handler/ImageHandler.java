package com.tencent.wechat.ipc.handler;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Base64;

import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;

/**
 * Author: congqin<br>
 * Data: 2016/12/13.<br>
 * Description: 处理助理发过来的指令 将头像进行BASE64编码返回<br>
 * Note:<br>
 */
public class ImageHandler extends BaseHandler {

	private static final String TAG = "ImageHandler";

	@Override
	public String handle(BridgeRequestVo requestVo) {

		String ret = "";
		String userName = requestVo.getId();
		FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap()
				.get(userName);

		ImageSize size = new ImageSize(50, 50);
		Bitmap bitmap = WeChatApplication.getImageLoader().loadImageSync(
				friendVo.getHeadImgUrl(), size);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		byte[] byteArray = stream.toByteArray();

		String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

		JSONObject retObj = new JSONObject();
		try {
			retObj.put("status", BridgeContract.Status.SUCCESS);
			retObj.put("message", encoded);
			ret = retObj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			ret = BridgeContract.DEFAULT_RESPOND_ERROR;
		}
		return ret;
	}
}
