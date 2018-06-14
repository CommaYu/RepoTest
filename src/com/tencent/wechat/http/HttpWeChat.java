package com.tencent.wechat.http;

import com.tencent.wechat.common.entity.ScanVo;
import com.tencent.wechat.http.entity.BatchgetcontactRequestVo;
import com.tencent.wechat.http.entity.FriendVo;
import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.http.entity.Response;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 网页版微信核心接口<br>
 * Note:<br>
 */

public interface HttpWeChat {
    /**
     * 获取二维码地址
     *
     * @return url地址，请求此地址获取二维码图片
     * @throws IOException
     * @throws InterruptedException
     */
    String getQvode() throws IOException, InterruptedException;

    /**
     * 获得登陆状态
     *
     * @return
     */
    ScanVo getLoginState() throws IOException, JSONException, InterruptedException;

    /**
     * 获得最近联系人
     *
     * @return
     */
    Response<List<FriendVo>> getLateConnectUser();

    /**
     * 获取所有好友，这是对外部类提供的接口，该方法并不请求网络加载数据
     *
     * @return 好友数据
     */
    Response<List<FriendVo>> getAllConnectUser();

    /**
     * 向腾讯服务器发网络请求，获取所有好友，注意：未加入通讯录的群不算好友
     *
     * @return 0 成功获取 ， 1 获取失败
     * @throws IOException
     * @throws JSONException
     */
    Integer getAllFriend() throws IOException, JSONException;


    /**
     * 向腾讯服务器发网络请求，获得微信消息，当synKey()方法提示有新消息时，就会调用以下方法获取具体的消息
     *
     * @return 新消息
     * @throws JSONException
     */
    List<ReceiveMsgVO> getChatMsg() throws IOException, JSONException;


    /**
     * 向腾讯服务器发网络请求，发送微信消息
     *
     * @param msg 消息实体
     * @return 0 成功   1 发送失败
     * @throws JSONException
     */
    Response<Integer> sendChatMsg(ReceiveMsgVO msg) throws IOException, JSONException;


    /**
     * 向腾讯服务器发网络请求，获得资源文件内容，主要是图片  语音
     *
     * @param url 资源url
     * @return 资源字节流
     */
    Response<byte[]> getResource(String url) throws IOException;


    /**
     * 获得我的信息，这是对外部类提供的接口，该方法并不请求网络加载数据
     *
     * @return
     */
    Response<FriendVo> getMyInfo();


    /**
     * 向腾讯服务器发网络请求，退出登录
     *
     * @throws IOException
     */
    void logOut() throws IOException;

    /**
     * 获取所有联系人，包括好友和未添加到通讯录的群，这是对外部类提供的接口，该方法并不请求网络加载数据
     *
     * @return 联系人数据
     */
    Map<String, FriendVo> getAllFriendsMap();

    /**
     * 心跳包,向腾讯服务器发网络请求，检查是否有新消息，没有则阻塞25s，有则立即返回,该方法只检查是否有消息，并不能得知消息类型及内容
     *
     * @return 0代表正常没有消息<br>
     * 2代表有新消息  消息响应中AddMsgList里面有内容，ModContactList中可能有内容，如有新人加入我所在的一个群，这时ModContactList中展示的全新的信息，而非增量<br>
     * 4代表好友删除  消息响应中DelContactList里面有内容<br>
     * 6代表好友新增  消息响应中ModContactList里面有内容，AddMsgList中可能也有打招呼的内容<br>
     * 1100异常，需要重新登录
     * @throws IOException
     * @throws JSONException
     */
    Integer synKey() throws IOException, JSONException;

    /**
     * 批量获取联系人，比如某个好友更新了昵称，则调用以下方法，又如群成员发生了变化，也需要调用以下方法更新成员信息
     *
     * @param prams
     * @return 更新后的成员信息
     * @throws IOException
     * @throws JSONException
     */
    List<FriendVo> getBatchContact(List<BatchgetcontactRequestVo> prams) throws IOException, JSONException;


    /**
     * 获取所有群，这是对外部类提供的接口，该方法并不请求网络加载数据
     *
     * @return 联系人数据
     */
    Map<String, FriendVo> getGroupFriendsMap();
}
