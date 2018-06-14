package com.tencent.wechat.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

public interface HttpCallback {

    public void before(HttpClient client, HttpUriRequest request);

    public void after(HttpClient client, HttpUriRequest request, HttpResponse response);

}
