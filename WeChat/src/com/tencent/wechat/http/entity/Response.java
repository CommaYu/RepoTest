package com.tencent.wechat.http.entity;

/**
 * <pre>
 * 返回model
 * 	0	成功
 * 	1	未初始化完成,或者需要重新登录
 * retCode
 * 	返回的数据
 * data
 * @author tcloud
 * </pre>
 */
public class Response<T> {

    private Integer retCode = 0;

    private T data;

    public Integer getRetCode() {
        return retCode;
    }

    public void setRetCode(Integer retCode) {
        this.retCode = retCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
