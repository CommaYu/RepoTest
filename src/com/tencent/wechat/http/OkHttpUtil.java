package com.tencent.wechat.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import android.util.Log;

/**
 * Author: congqin <br>
 * Data:2017/1/14<br>
 * Description: Okhttp库的封装类，供WechatMain调用 <br>
 * Note: <br>
 */
public class OkHttpUtil {

	public static final String TAG = "OkHttpUtil";

	private static OkHttpClient client;

	private static List<Cookie> cookieList = new ArrayList<Cookie>();

	private static final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();

	public static final Map<String, String> cookieMap = new HashMap<String, String>();

	static {

		client = new OkHttpClient.Builder()
				.cookieJar(new CookieJar() {

					@Override
					public void saveFromResponse(HttpUrl url,
							List<Cookie> cookies) {
						// cookieStore.put(url.host(), cookies);
						Log.d(TAG, "saveFromResponse: ");
						// cookieList.addAll(cookies);
						for (Cookie c : cookies) {
							cookieMap.put(c.name(), c.value());
							Log.d(TAG, "saveFromResponse: cookie=" + c.name()
									+ ":" + c.value());
						}

					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl url) {
						Log.d(TAG, "loadForRequest: ");

						Log.d(TAG, "loadForRequest: url=" + url);
						return cookieList;
						// return cookieList;

						// List<Cookie> cookies = cookieStore.get(url.host());
						// return cookies != null ? cookies : new
						// ArrayList<Cookie>();
					}
				}).connectTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS).followSslRedirects(false)
				.followRedirects(false).build();
	}

	public static void clearCookie() {
		cookieList.clear();
		cookieStore.clear();
		cookieMap.clear();
	}

	public static String getStringSync(String url, Map<String, String> params)
			throws IOException {
		return getStringSync(url, params, null);
	}

	public static String getStringSync(String url, Map<String, String> params,
			Map<String, String> headers) throws IOException {
		ResponseBody body = getSync(url, params, headers);
		String ret = body.string();
		Log.d(TAG, "getStringSync: ret=" + ret);
		return ret;
	}

	public static byte[] getByteSync(String url, Map<String, String> params)
			throws IOException {
		return getByteSync(url, params, null);
	}

	public static byte[] getByteSync(String url, Map<String, String> params,
			Map<String, String> headers) throws IOException {
		ResponseBody body = getSync(url, params, headers);
		return body.bytes();
	}

	public static InputStream getStreamSync(String url,
			Map<String, String> params) throws IOException {
		return getStreamSync(url, params, null);
	}

	public static InputStream getStreamSync(String url,
			Map<String, String> params, Map<String, String> headers)
			throws IOException {
		ResponseBody body = getSync(url, params, headers);
		return body.byteStream();
	}

	public static ResponseBody getSync(String url, Map<String, String> params,
			Map<String, String> headers) throws IOException {

		HttpUrl httpUrl = HttpUrl.parse(url);
		HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				String val = params.get(key);
				key = URLEncoder.encode(key, "UTF-8");
				val = URLEncoder.encode(val, "UTF-8");
				urlBuilder.addEncodedQueryParameter(key, val);
			}
		}

		Request.Builder requestBuilder = new Request.Builder().url(urlBuilder
				.build());

		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				requestBuilder.header(key, headers.get(key));
			}
		}

		Set<String> keys = cookieMap.keySet();

		StringBuilder sbc = new StringBuilder();
		for (String c : keys) {
			sbc.append(c + "=" + cookieMap.get(c) + ";");
		}
		requestBuilder.header("Cookie", sbc.toString());

		Request request = requestBuilder.build();
		Call call = client.newCall(request);
		Response response = call.execute();

		return response.body();
	}

	public static String postJsonSync(String url, Map<String, String> params,
			String json, Map<String, String> headers) throws IOException {
		HttpUrl httpUrl = HttpUrl.parse(url);
		HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				String val = params.get(key);
				key = URLEncoder.encode(key, "UTF-8");
				val = URLEncoder.encode(val, "UTF-8");
				urlBuilder.addEncodedQueryParameter(key, val);
			}
		}

		Request.Builder requestBuilder = new Request.Builder().url(urlBuilder
				.build());

		if (headers != null && headers.size() > 0) {
			for (String key : headers.keySet()) {
				requestBuilder.header(key, headers.get(key));
			}
		}

		Set<String> keys = cookieMap.keySet();

		StringBuilder sbc = new StringBuilder();
		for (String c : keys) {
			sbc.append(c + "=" + cookieMap.get(c) + ";");
		}
		requestBuilder.header("Cookie", sbc.toString());

		RequestBody requestBody = RequestBody.create(
				MediaType.parse("application/json; charset=utf-8"), json);
		Request request = requestBuilder.post(requestBody).build();
		Call call = client.newCall(request);
		Response response = call.execute();
		return response.body().string();
	}

	/**
	 * 通过此接口向自主服务器上传mp3音频
	 * 
	 * @param url
	 * @param params
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String postMedia(String url, Map<String, String> params,
			File file) throws IOException {

		HttpUrl httpUrl = HttpUrl.parse(url);
		HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
		if (params != null && params.size() > 0) {
			for (String key : params.keySet()) {
				String val = params.get(key);
				key = URLEncoder.encode(key, "UTF-8");
				val = URLEncoder.encode(val, "UTF-8");
				urlBuilder.addEncodedQueryParameter(key, val);
			}
		}

		MediaType MEDIA_TYPE_PNG = MediaType.parse("audio/mp3");
		RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart(file.getName(), file.getName(),
						RequestBody.create(MEDIA_TYPE_PNG, file)).build();

		Request request = new Request.Builder().url(urlBuilder.build())
				.post(requestBody).build();

		Call call = client.newCall(request);
		Response response = call.execute();
		if (!response.isSuccessful())
			throw new IOException("Unexpected code " + response);
		return response.body().string();
	}

}
