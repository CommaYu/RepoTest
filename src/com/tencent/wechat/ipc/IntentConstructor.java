package com.tencent.wechat.ipc;

import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.entity.FriendVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class IntentConstructor {

    /**
     * 按协议构造联系人json数据
     *
     * @param friendVoList
     * @return
     */
    public static String getContactIntent(Collection<FriendVo> friendVoList) {
        String ret = "";

        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();
            if (friendVoList != null && !friendVoList.isEmpty()) {
                for (FriendVo friendVo : friendVoList) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", WeChatUtil.getFriendName(friendVo));
                    obj.put("originalname", friendVo.getNickName());
                    obj.put("id", friendVo.getUserName());
                    jsonArray.put(obj);
                }
            }

            jsonObject.put("focus", "weixin");
            jsonObject.put("category", "contact");
            jsonObject.put("list", jsonArray);

            ret = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 按协议构造json数据,通知助理打开或关闭录音器
     *
     * @param on true打开录音器   false关闭录音器
     * @return
     */
    public static String getRecordIntent(boolean on) {
        if (on) {
            return BridgeContract.INTENT_RECORDER_ON;
        } else {
            return BridgeContract.INTENT_RECORDER_OFF;
        }
    }

    public static String getNaviIntent(PoiInfoVo poi) {

        String ret = "";

        try {
            JSONObject poiObj = new JSONObject();
            poiObj.put("poiname", poi.getPoiname());
            poiObj.put("longitude", poi.getLongitude());
            poiObj.put("latitude", poi.getLatitude());
            poiObj.put("coord_type", poi.getCoord_type());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("focus", "weixin");
            jsonObject.put("category", "navi");
            jsonObject.put("poi", poiObj);

            ret = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ret;

    }

    public static String getLocationIntent() {
        return BridgeContract.INTENT_GET_LOCATION;
    }
}
