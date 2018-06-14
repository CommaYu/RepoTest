package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.ipc.BridgeRequestVo;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public abstract class BaseHandler {

    public abstract String handle(BridgeRequestVo requestVo);

}
