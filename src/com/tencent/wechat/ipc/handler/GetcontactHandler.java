package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.common.utils.WeChatUtil;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Author: congqin<br>
 * Data: 2016/12/13.<br>
 * Description: 处理助理发过来的指令  获取微信联系人（用于构建词典）<br>
 * Note:<br>
 */
public class GetcontactHandler extends BaseHandler {
    @Override
    public String handle(BridgeRequestVo requestVo) {

        String ret = "";

        try {
            List<FriendVo> friendVoList = WeChatMain.getWeChatMain().getAllConnectUser().getData();

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

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", BridgeContract.Status.SUCCESS);
            jsonObject.put("list", jsonArray);

            ret = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            ret = BridgeContract.CONTACT_RESPOND_ERROR;
        }

        return ret;
    }
}
