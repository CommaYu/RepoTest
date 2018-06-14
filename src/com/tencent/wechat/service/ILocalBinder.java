package com.tencent.wechat.service;

import android.app.Service;

import com.tencent.wechat.http.entity.ReceiveMsgVO;
import com.tencent.wechat.listener.IOnLogoutListener;
import com.tencent.wechat.listener.IOnMessageSendListener;

import java.util.List;

/**
 * Author: congqin <br>
 * Data:2016/12/11.<br>
 * Description: WechatService代理对象所实现的接口<br>
 * Note: <br>
 */
public interface ILocalBinder {

    /**
     * 获取登录状态
     *
     * @return  见 {@link WeChatService.LoginState}
     */
    WeChatService.LoginState getLoginState();

    /**
     * 设置登录状态
     *
     * @param state 见 {@link WeChatService.LoginState}
     */
    void setLoginState(WeChatService.LoginState state);

    /**
     * 请求腾讯服务器，收消息
     */
    void getMessage();

    /**
     * 添加消息到消息管理器
     *
     * @param msglist   需要被添加的消息列表
     * @param isSendMsg  true 代表这是从车极端发送的消息   false代表这是从腾讯服务器获取的消息
     */
    void addMessage(List<ReceiveMsgVO> msglist, boolean isSendMsg);

    /**
     * 添加消息到消息管理器
     *
     * @param msgVO   需要被添加的消息
     * @param isSendMsg  true 代表这是从车极端发送的消息   false代表这是从腾讯服务器获取的消息
     */
    void addMessage(ReceiveMsgVO msgVO, boolean isSendMsg);

    /**
     * 发送消息（文本+链接），直接发送，不起录音界面
     */
    void sendMessage(ReceiveMsgVO msg);


    /**
     * 起录音界面，录音完毕后自动发送消息（文本+链接）
     */
    void sendVoiceMsg(String receiverUid);


    /**
     * 退出登录
     */
    void logout();


}


