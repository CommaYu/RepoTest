package com.tencent.wechat.ipc;

/**
 * Created by qxb-810 on 2017/1/3.
 * <p/>
 * 微信发送请求，助理给予响应
 * 该响应对应的实体类
 */
public class BridgeResponseVo {

    /**
     * 响应状态，取值success  fail
     */
    private String status;

    /**
     * 附带信息
     */
    private String message;

    /**
     * 微信请求的位置信息
     */
    private PoiInfoVo poi;


    public BridgeResponseVo(String status, String message, PoiInfoVo poi) {
        this.status = status;
        this.message = message;
        this.poi = poi;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PoiInfoVo getPoi() {
        return poi;
    }

    public void setPoi(PoiInfoVo poi) {
        this.poi = poi;
    }


}














