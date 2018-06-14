package com.tencent.wechat.common.entity;

/**
 * 登陆结果
 *
 * @author tcloud
 */
public class ScanVo {


    /**
     * 0  成功
     * 1  uid 为空
     * 2  扫描成功，但未点确认
     * 3  408：未扫描
     * 4  二维码过期重新扫描
     */
    private int code;

    private String content;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
