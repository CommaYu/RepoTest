package com.tencent.wechat.ipc.handler;

import com.tencent.wechat.common.utils.SharedPreferencesUtil;
import com.tencent.wechat.ipc.BridgeContract;
import com.tencent.wechat.ipc.BridgeRequestVo;

/**
 * Author: congqin<br>
 * Data: 2016/12/13.<br>
 * Description: 处理助理发过来的指令(关闭自动播报) 返回处理结果<br>
 * Note:<br>
 */
public class TTsPlayCloseHandler extends BaseHandler {
	@Override
	public String handle(BridgeRequestVo requestVo) {

		SharedPreferencesUtil.getInstance().saveIsNotify(false);

		return BridgeContract.DEFAULT_RESPOND_OK;
	}
}
