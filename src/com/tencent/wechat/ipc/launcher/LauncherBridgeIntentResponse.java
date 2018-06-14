package com.tencent.wechat.ipc.launcher;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.bridge.ServiceStub;
import com.iflytek.bridge.aidl.OnBridegeListner;
import com.tencent.wechat.common.entity.UnReadMsgVo;
import com.tencent.wechat.http.WeChatMain;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ConcurrentMap;


/**
 * Author: congqin <br>
 * Data:2017/3/26<br>
 * Description: 负责向桌面Launcher发送请求，显示未读消息<br>
 * Note: <br>
 */
public class LauncherBridgeIntentResponse {

    private static final String TAG = LauncherBridgeIntentResponse.class.getSimpleName();

    private LauncherBridgeIntentResponse() {
    }

    private ServiceStub serviceStub;

    public void setServiceStub(ServiceStub serviceStub) {
        this.serviceStub = serviceStub;
    }

    private static class LazyHolder {
        private static final LauncherBridgeIntentResponse INSTANCE = new LauncherBridgeIntentResponse();
    }

    public static LauncherBridgeIntentResponse getInstance() {
        return LazyHolder.INSTANCE;
    }


    /**
     * 通知桌面显示未读消息
     *
     * @param mUnReadMsgMap 未读消息集合
     */
    public void showUnReadMsgInLauncher(ConcurrentMap<String, UnReadMsgVo> mUnReadMsgMap) {
        String intentStr = constructJson(mUnReadMsgMap);
        String ret = onIntentBack(intentStr);
        Log.d(TAG, "showUnReadMsgInLauncher: ret = " + ret);
    }

    private String constructJson(ConcurrentMap<String, UnReadMsgVo> mUnReadMsgMap) {

        String result = null;
        JSONObject object = new JSONObject();
        try {
            object.put("focus", "weixin");
            object.put("category", "showmsg");

            JSONArray array = new JSONArray();
            if (mUnReadMsgMap == null || mUnReadMsgMap.isEmpty()) {
                object.put("unreadnum", 0);
                object.put("msglist", array);
            } else {
                int unreadnum = 0;
                for (String uername : mUnReadMsgMap.keySet()) {
                    JSONObject msgObject = new JSONObject();
                    UnReadMsgVo unReadMsgVo = mUnReadMsgMap.get(uername);
                    msgObject.put("id", unReadMsgVo.getmUserName());
                    String remarkName = unReadMsgVo.getmRemarkName();
                    String nickName = unReadMsgVo.getmNickName();
                    msgObject.put("name", nickName + (TextUtils.isEmpty(remarkName) ? "" : "(" + remarkName + ")"));
                    msgObject.put("num", unReadMsgVo.getUnReadMsg().size());
                    ReceiveMsgVO latestMsg = unReadMsgVo.getUnReadMsg().get(unReadMsgVo.getUnReadMsg().size() - 1);
                    String content=latestMsg.getContent();

                    String fromUser = latestMsg.getFromUserName();
                    if (fromUser.startsWith("@@")) {
                        FriendVo groupFriend = WeChatMain.getWeChatMain().getAllFriendsMap().get(fromUser);
                        List<FriendVo> members = groupFriend.getMemberList();
                        if (members != null && !members.isEmpty()) {
                            for (FriendVo vo : members) {
                                if (latestMsg.getSenderName().equals(vo.getUserName())) {
                                    String sendName = TextUtils.isEmpty(vo.getDisplayName()) ? (TextUtils.isEmpty(vo.getRemarkName()) ?
                                            vo.getNickName() : vo.getRemarkName()) : vo.getDisplayName();
                                    content=sendName+"："+content;
                                    break;
                                }
                            }
                        }
                    }

                    msgObject.put("content", content);
                    long createTime = latestMsg.getCreateTime();
                    if (String.valueOf(createTime).length() == 10) {
                        createTime = createTime * 1000;
                    }
                    msgObject.put("timestamp", createTime);
                    array.put(msgObject);
                    unreadnum += unReadMsgVo.getUnReadMsg().size();
                }
                object.put("msglist", array);
                object.put("unreadnum", unreadnum);
            }
            result = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String onIntentBack(String intentStr) {
        if (serviceStub != null) {
            OnBridegeListner onBridegeListner = serviceStub.getIntentResponse();
            if (onBridegeListner != null) {
                try {
                    return onBridegeListner.onIntentBack(intentStr);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                Log.d(TAG, "onIntentBack: onBridegeListner is null");
            }
        } else {
            Log.d(TAG, "onIntentBack: serviceStub is null");
        }
        return null;
    }
}
