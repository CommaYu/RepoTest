package com.tencent.wechat.http;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.wechat.http.entity.DeflateDecompressingEntity;
import com.tencent.wechat.http.entity.GzipDecompressingEntity;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatHttpUtil {

    public static final String TAG = "ChatHttpUtil";


    private Lock lock = new ReentrantLock();

    private DefaultHttpClient httpClient = null;

    private Map<String, String> cookieMap = new HashMap<String, String>();


    public ChatHttpUtil() {
        httpClient = (DefaultHttpClient) getClient();
    }

    public void resetCookie(String cookie) {
        if (!TextUtils.isEmpty(cookie)) {
            lock.lock();
            try {
                cookieMap = new HashMap<String, String>();
                String[] cooks = cookie.split(";");
                for (String cook : cooks) {
                    String[] c = cook.split("=");
                    cookieMap.put(c[0], c[1]);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public String post(String url, String content, String cookie,
                       String referer, HttpCallback callback) throws IOException {
        return post(url, content, cookie, referer, callback, "utf-8");
    }

    public String post(String url, String content, String cookie,
                       String referer, HttpCallback callback, String chartSet) throws IOException {

        DefaultHttpClient httpClient = (DefaultHttpClient) getClient();
        String retVal = "";
        try {

            StringEntity se = new StringEntity(content, chartSet);
            HttpPost httppost = new HttpPost(url);
            if (!TextUtils.isEmpty(referer)) {
                httppost.addHeader("Referer", cookie);
            }

            StringBuilder sbc = new StringBuilder();
            if (!TextUtils.isEmpty(cookie)) {
                sbc.append(cookie);
            }
            lock.lock();
            try {
                Set<String> keys = cookieMap.keySet();
                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }

            setBaseHttpPost(httppost);
            httppost.addHeader("Cookie", sbc.toString());
            httppost.setEntity(se);

            if (null != callback) {
                callback.before(httpClient, httppost);
            }

            HttpResponse resp = httpClient.execute(httppost);

            if (null != callback) {
                callback.after(httpClient, httppost, resp);
            }

            HttpEntity entity = resp.getEntity();

            Header[] h = resp.getHeaders("Content-Encoding");
            if (null != h && h.length >= 1) {

                if ("gzip".equals(h[0].getValue())) {
                    entity = new GzipDecompressingEntity(entity);

                } else if ("deflate".equals(h[0].getValue())) {
                    entity = new DeflateDecompressingEntity(entity);
                }
            }
            retVal = EntityUtils.toString(entity, "utf-8");

            lock.lock();
            try {

                List<Cookie> r = httpClient.getCookieStore().getCookies();

                for (int i = 0; i < r.size(); i++) {
                    Cookie c = r.get(i);
                    cookieMap.put(c.getName(), c.getValue());
                }
            } finally {
                lock.unlock();
            }

        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return retVal;

    }

    public String postJs(String url, String content, String cookie,
                         String referer, HttpCallback callback) throws IOException {

        DefaultHttpClient httpClient = (DefaultHttpClient) getClient();

        HttpParams p = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(p, 6 * 1000);
        HttpConnectionParams.setSoTimeout(p, 6 * 1000);

        String retVal = "";
        try {

            StringEntity se = new StringEntity(content);
            HttpPost httppost = new HttpPost(url);
            if (!TextUtils.isEmpty(referer)) {
                httppost.addHeader("Referer", referer);
            }

            StringBuilder sbc = new StringBuilder();
            if (!TextUtils.isEmpty(cookie)) {
                sbc.append(cookie);
            }
            lock.lock();
            try {

                Set<String> keys = cookieMap.keySet();

                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }
            setBaseHttpPost(httppost);
            httppost.addHeader("Cookie", sbc.toString());
            httppost.addHeader("Accept",
                    "application/json, text/javascript, */*; q=0.01");
            httppost.addHeader("Content-Type",
                    "application/json; charset=utf-8");
            httppost.setEntity(se);

            if (null != callback) {
                callback.before(httpClient, httppost);
            }

            HttpResponse resp = httpClient.execute(httppost);

            if (null != callback) {
                callback.after(httpClient, httppost, resp);
            }

            HttpEntity entity = resp.getEntity();
            Header[] h = resp.getHeaders("Content-Encoding");
            if (null != h && h.length >= 1) {

                if ("gzip".equals(h[0].getValue())) {
                    entity = new GzipDecompressingEntity(entity);

                } else if ("deflate".equals(h[0].getValue())) {
                    entity = new DeflateDecompressingEntity(entity);
                }
            }
            retVal = EntityUtils.toString(entity, "utf-8");

            lock.lock();
            try {

                List<Cookie> r = httpClient.getCookieStore().getCookies();

                for (int i = 0; i < r.size(); i++) {
                    Cookie c = r.get(i);
                    cookieMap.put(c.getName(), c.getValue());
                }
            } finally {
                lock.unlock();
            }

        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return retVal;

    }

    public String get(String url, String refer, HttpCallback callback)
            throws IOException {

        return get(url, null, null, refer, callback);
    }

    public String get(String url, Map<String, String> params,
                      String cookie, String referer, HttpCallback callback)
            throws IOException {

        DefaultHttpClient httpClient = (DefaultHttpClient) getClient();
        HttpParams p = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(p, 6 * 1000);
//        HttpConnectionParams.setSoTimeout(p,6*1000);

        String retVal = "";
        try {

            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    qparams.add(new BasicNameValuePair(param.getKey(), param
                            .getValue()));
                }
            }
            String paramstr = URLEncodedUtils.format(qparams, "utf-8");
            if (!TextUtils.isEmpty(paramstr)) {
                url = url + "?" + paramstr;
            }

            HttpGet httpget = new HttpGet(url);
            if (!TextUtils.isEmpty(referer)) {
                httpget.addHeader("Referer", referer);
            }

            StringBuilder sbc = new StringBuilder();
            if (!TextUtils.isEmpty(cookie)) {
                sbc.append(cookie);
            }
            lock.lock();
            try {

                Set<String> keys = cookieMap.keySet();

                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }

            setBaseHttpPost(httpget);
            httpget.addHeader("Cookie", sbc.toString());
            if (null != callback) {
                callback.before(httpClient, httpget);
            }

            Log.d(TAG, "get: execute get begin...");
            HttpResponse resp = httpClient.execute(httpget);
            Log.d(TAG, "get: execute get end...");

            if (null != callback) {
                callback.after(httpClient, httpget, resp);
            }

            HttpEntity entity = resp.getEntity();
            Header[] h = resp.getHeaders("Content-Encoding");
            if (null != h && h.length >= 1) {

                if ("gzip".equals(h[0].getValue())) {
                    entity = new GzipDecompressingEntity(entity);

                } else if ("deflate".equals(h[0].getValue())) {
                    entity = new DeflateDecompressingEntity(entity);
                }
            }
            retVal = EntityUtils.toString(entity, "utf-8");

            lock.lock();
            try {

                List<Cookie> r = httpClient.getCookieStore().getCookies();

                for (int i = 0; i < r.size(); i++) {
                    Cookie c = r.get(i);
                    cookieMap.put(c.getName(), c.getValue());
                }
            } finally {
                lock.unlock();
            }
        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return retVal;

    }

    public byte[] getImage(String url) throws IOException {
        HttpClient httpClient = getClient();
//        HttpParams p = httpClient.getParams();
//        HttpConnectionParams.setConnectionTimeout(p, 6 * 1000);
//        HttpConnectionParams.setSoTimeout(p, 6 * 1000);
        try {
            HttpGet gmethod = new HttpGet(url);
            setBaseHttpPost(gmethod);

            gmethod.addHeader("Accept", "image/png,image/*;q=0.8,*/*;q=0.5");
            StringBuilder sbc = new StringBuilder();
            lock.lock();
            try {

                Set<String> keys = cookieMap.keySet();

                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }

            gmethod.addHeader("Cookie", sbc.toString());
            HttpResponse response = httpClient.execute(gmethod);

            return EntityUtils.toByteArray(response.getEntity());

        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public byte[] getVoice(String url) throws IOException {

        HttpClient httpClient = getClient();
        try {
            HttpGet gmethod = new HttpGet(url);
            setBaseHttpPost(gmethod);

            gmethod.addHeader("Accept", "audio/webm,audio/ogg,audio/wav,audio/*;q=0.9,application/ogg;q=0.7,video/*;" +
                    "q=0.6,*/*;q=0.5");
            StringBuilder sbc = new StringBuilder();
            lock.lock();
            try {
                Set<String> keys = cookieMap.keySet();
                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }

            gmethod.addHeader("Cookie", sbc.toString());
            HttpResponse response = httpClient.execute(gmethod);

            return EntityUtils.toByteArray(response.getEntity());

        } catch (IOException e) {
            throw e;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public byte[] getVideo(String url) throws IOException {
        HttpClient httpClient = getClient();
        try {
            HttpGet gmethod = new HttpGet(url);
//            setBaseHttpPost(gmethod);

            gmethod.addHeader("Accept", "*/*");
            gmethod.addHeader("Accept-Encoding", "identity;q=1, *;q=0");
            gmethod.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
            gmethod.addHeader("Connection", "keep-alive");
            gmethod.addHeader("Host", "wx.qq.com");
            gmethod.addHeader("Range", "bytes=0-");
            gmethod.addHeader("Referer", "https://wx.qq.com/?&lang=zh_CN");


            StringBuilder sbc = new StringBuilder();
            lock.lock();
            try {
                Set<String> keys = cookieMap.keySet();
                for (String c : keys) {
                    sbc.append(c + "=" + cookieMap.get(c) + ";");
                }
            } finally {
                lock.unlock();
            }

            gmethod.addHeader("Cookie", sbc.toString());
            HttpResponse response = httpClient.execute(gmethod);

            return EntityUtils.toByteArray(response.getEntity());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private HttpClient getClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient = (DefaultHttpClient) (MyClientConnectionManager.getSSLInstance(httpClient));
        httpClient.setRedirectHandler(new NoRedirectHandler());
        return httpClient;

    }


    public String getSynckech(String url, Map<String, String> params,
                              String cookie, String referer)
            throws IOException {
        String retVal = "";
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                qparams.add(new BasicNameValuePair(param.getKey(), param
                        .getValue()));
            }
        }
        String paramstr = URLEncodedUtils.format(qparams, "utf-8");
        if (!TextUtils.isEmpty(paramstr)) {
            url = url + "?" + paramstr;
        }
        Log.i(TAG, "=====>" + url);
        HttpGet httpget = new HttpGet(url);
        if (TextUtils.isEmpty(referer)) {
            httpget.addHeader("Referer", cookie);
        }
        StringBuilder sbc = new StringBuilder();
        if (TextUtils.isEmpty(cookie)) {
            sbc.append(cookie);
        }

        //modified by congqin in 12.4
        lock.lock();
        try {
            Set<String> keys = cookieMap.keySet();
            for (String c : keys) {
                sbc.append(c + "=" + cookieMap.get(c) + ";");
            }
        } finally {
            lock.unlock();
        }
        setBaseHttpPost(httpget);
        httpget.addHeader("Cookie", sbc.toString());
        //modified by congqin end

        Exception e = null;
        for (int i = 0; i < 3; i++) {
            try {
                HttpResponse resp = httpClient.execute(httpget);
                HttpEntity entity = resp.getEntity();
                Header[] h = resp.getHeaders("Content-Encoding");
                if (null != h && h.length >= 1) {

                    if ("gzip".equals(h[0].getValue())) {
                        entity = new GzipDecompressingEntity(entity);

                    } else if ("deflate".equals(h[0].getValue())) {
                        entity = new DeflateDecompressingEntity(entity);
                    }
                }
                retVal = EntityUtils.toString(entity, "utf-8");
                e = null;
                break;
            } catch (Exception e1) {
                e = e1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                continue;
            }
        }
        if (null != e) {
            throw new RuntimeException(e);
        }

        return retVal;

    }

    public String getCookie(String name) {
        return cookieMap.get(name);
    }


    private static void setBaseHttpPost(HttpUriRequest request) {

        request.addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 " +
                        "Safari/537.36");
        request.addHeader("Accept-Encoding", "gzip, deflate");
        request.addHeader("Accept-Language",
                "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        request.addHeader("Connection", "keep-alive");
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Accept", "*/*");

    }
}

class NoRedirectHandler implements RedirectHandler {
    @Override
    public boolean isRedirectRequested(HttpResponse response,
                                       HttpContext context) {
        return false;
    }

    @Override
    public URI getLocationURI(HttpResponse response, HttpContext context)
            throws ProtocolException {
        return null;
    }
}
