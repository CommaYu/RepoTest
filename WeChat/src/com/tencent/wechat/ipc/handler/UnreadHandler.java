package com.tencent.wechat.ipc.handler;

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
public class UnreadHandler extends BaseHandler {
    @Override
    public String handle(BridgeRequestVo requestVo) {

        String userName = requestVo.getId();

        Map<String, UnReadMsgVo> unReadMap = MessageManager.getInstance().getmUnReadMsgMap();
        UnReadMsgVo unReadMsgVo = unReadMap.get(userName);

        String ret = null;

        try {
            JSONObject object = new JSONObject();
            FriendVo friendVo = WeChatMain.getWeChatMain().getAllFriendsMap().get(unReadMsgVo.getmUserName());
            object.put("name", WeChatUtil.getFriendName(friendVo));
            object.put("id", unReadMsgVo.getmUserName());
            object.put("size", unReadMsgVo.getnUnReadNum() + "");

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(object);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", BridgeContract.Status.SUCCESS);
            jsonObject.put("list", jsonArray);
            ret = jsonObject.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return ret;
    }

/**
 *
 * {"status":"success","list":[{},{}]}
 *
 *  {"status":"fail","message":"***"}
 *
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


}
