package com.tencent.wechat.ipc;

/**
 * Created by qxb-810 on 2017/1/3.
 * <p/>
 * 助理发送的请求 所对应的实体类
 */
public class BridgeRequestVo {

    /**
     * weixin
     */
    private String focus;
    /**
     * 原始文本，如 发微信给xx晚上一起吃饭
     */
    private String rawText;
    /**
     * 子业务，如login,send,sendmsg,getcontact
     */
    private String category;
    /**
     * 人名
     */
    private String name;
    /**
     * 好友的username
     */
    private String id;
    /**
     * 发送的内容，如  发微信给xx晚上一起吃饭，context="晚上一起吃饭"
     */
    private String context;
    /**
     * 助理音频路径
     */
    private String voicepath;
    /**
     * 发送位置时附带的位置信息
     */
    private PoiInfoVo poi;


    public BridgeRequestVo(String focus, String rawText, String category, String name, String id, String context,
                           String voicepath, PoiInfoVo poi) {
        this.focus = focus;
        this.rawText = rawText;
        this.category = category;
        this.name = name;
        this.id = id;
        this.context = context;
        this.voicepath = voicepath;
        this.poi = poi;
    }

    public String getFocus() {

        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVoicepath() {
        return voicepath;
    }

    public void setVoicepath(String voicepath) {
        this.voicepath = voicepath;
    }

    public PoiInfoVo getPoi() {
        return poi;
    }

    public void setPoi(PoiInfoVo poi) {
        this.poi = poi;
    }
}














