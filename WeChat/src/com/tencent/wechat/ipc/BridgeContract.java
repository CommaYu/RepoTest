package com.tencent.wechat.ipc;

/**
 * Created by qxb-810 on 2016/12/9.
 */
public class BridgeContract {

    public class Status {
        public static final String SUCCESS = "success";
        public static final String FAIL = "fail";
    }


    public static final String DEFAULT_RESPOND_OK = "{\"status\":\"success\",\"message\":\"\"}";
    public static final String DEFAULT_RESPOND_ERROR = "{\"status\":\"fail\",\"message\":\"\"}";

    public static final String CONTACT_RESPOND_ERROR = "{\"status\":\"fail\",\"message\":\"联系人异常，请稍后重试\"}";

    public static final String LOGIN_RESPOND_LOGIN = "{\"status\":\"success\",\"message\":\"login\"}";
    public static final String LOGIN_RESPOND_NOLOGIN = "{\"status\":\"success\",\"message\":\"nologin\"}";
    public static final String LOGIN_RESPOND_LOGINING = "{\"status\":\"success\",\"message\":\"logining\"}";


    public static final String INTENT_GET_LOCATION = "{\"focus\":\"weixin\",\"category\":\"getlocation\"}";
    public static final String INTENT_RECORDER_ON = "{\"focus\":\"weixin\",\"category\":\"recordon\"}";
    public static final String INTENT_RECORDER_OFF = "{\"focus\":\"weixin\",\"category\":\"recordoff\"}";


    public static final String COORD_TYPE_BDL="bdl"; //百度墨卡托 坐标系
    public static final String COORD_TYPE_BD9="bd9"; //百度09
    public static final String COORD_TYPE_GCJ="gcj"; //国测局02
    public static final String COORD_TYPE_WGS="wgs"; //国际标准

}
