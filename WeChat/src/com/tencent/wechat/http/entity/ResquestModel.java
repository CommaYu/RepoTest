package com.tencent.wechat.http.entity;

import com.tencent.wechat.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;


public class ResquestModel {

    private Map<String, Object> attribute = new HashMap<String, Object>();

    private PropVo sysP;

    private String device;

    public void add(String key, Object value) {
        attribute.put(key, value);
    }

    public ResquestModel(PropVo sysP, String device) {
        super();
        this.sysP = sysP;
        this.device = device;
    }

    public String toJson() {

        if (null != sysP) {
            Map<String, String> baseRequest = new HashMap<String, String>();
            baseRequest.put("Uin", sysP.getWxuin());
            baseRequest.put("Sid", sysP.getWxsid());
            baseRequest.put("Skey", sysP.getSkey());
            baseRequest.put("DeviceID", device);
            attribute.put("BaseRequest", baseRequest);
        }
        return JSONUtils.toJson(attribute);
    }
}
