package com.tencent.wechat.ipc.handler;

import android.util.Log;

import com.tencent.wechat.common.entity.UnReadMsgVo;
import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;
import com.tencent.wechat.manager.MessageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class AllUnreadHandler extends BaseHandler {
    private static final String TAG = "AllUnreadHandler";

    @Override
    public String handle(BridgeRequestVo requestVo) {

        Map<String, UnReadMsgVo> unReadMap = MessageManager.getInstance().getmUnReadMsgMap();
        Log.d(TAG, "handle: unReadMap size=" + unReadMap.size());
        String ret = null;
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("status", BridgeContract.Status.SUCCESS);

            JSONArray jsonArray = new JSONArray();
            for (String uid : unReadMap.keySet()) {
                UnReadMsgVo unReadMsgVo = unReadMap.get(uid);
                JSONObject object = new JSONObject();
                FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap().get(unReadMsgVo.getmUserName());
                object.put("name", WeChatUtil.getFriendName(friendVo));
                object.put("id", unReadMsgVo.getmUserName());
                object.put("size", unReadMsgVo.getnUnReadNum() + "");
                jsonArray.put(object);
            }

            jsonObject.put("list", jsonArray);
            ret = jsonObject.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
/**
 {"status":"success","message":[{},{}]}
 [
 {
 "name": "张三",
 "id": "7878787878",
 "size": "3",
 },
 {
 "name": "李四",
 "id": "787878700",
 "size": "2",
 }
 ]
 */
        return ret;
    }
}
