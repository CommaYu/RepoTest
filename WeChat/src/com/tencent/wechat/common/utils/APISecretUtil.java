/* 
 *
 * Copyright (C) 1999-2012 IFLYTEK Inc.All Rights Reserved. 
 * 
 * FileName：APISecretUtil.java
 * 
 * Description：
 * 
 * History：
 * Version   Author      Date            Operation 
 * 1.0	  baoxu   2015年8月14日下午4:48:03	       Create	
 */
package com.tencent.wechat.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author baoxu
 * 
 * @version 1.0
 * 
 */
public class APISecretUtil {

    /**
     * @description 生成会话交互唯一标识
     * @author baoxu
     * @create 2015年10月26日下午7:21:12
     * @version 1.0
     * @return
     */
    public static String generateSid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * @description 自动生成签名
     * @author baoxu
     * @create 2015年8月14日下午5:20:42
     * @version 1.0
     * @param openId
     *            应用id
     * @param openKey
     *            应用分配密钥
     * @param apiUrl
     *            api接口Url
     * @param paramMap
     *            api参数Map
     * @return 签名
     * @throws Exception
     */
    public static String generateSign(final String openId,
            final String openKey, final String apiUrl,
            final Map<String, String> paramMap) throws Exception {
        String sign = null;
        if (openId == null || "".equals(openId.trim())) {
            throw new Exception("openId 为空!");
        }
        if (openKey == null || "".equals(openKey.trim())) {
            throw new Exception("openKey 为空!");
        }
        Map<String, String> pMap = new HashMap<String, String>();
        if (paramMap != null) {
        	for (Map.Entry<String, String> entry : paramMap.entrySet()) {
				pMap.put(URLDecoder.decode(entry.getKey(), "UTF-8"),  URLDecoder.decode(entry.getValue(), "UTF-8"));
			}
        }
        if (apiUrl != null && apiUrl.indexOf("?") > 0) {
            URL url = new URL(apiUrl);
            String urlParma = url.getQuery();
            if (urlParma != null && !"".equals(urlParma.trim())) {
                String[] urlParmaArr = urlParma.split("&");
                if(urlParmaArr != null ){
                    for (int i = 0; i < urlParmaArr.length; i++) {
                        if (null == urlParmaArr[i] || 0 == urlParmaArr[i].length()) {
                            continue;
                        }
                        String[] parmaArr = urlParmaArr[i].split("=");
                        if (parmaArr != null && !"openId".equals(parmaArr[0])) {
                            //
                            parmaArr[0] = URLDecoder.decode(parmaArr[0], "utf-8");
                            if(parmaArr.length > 1 && parmaArr[1] != null){
                                parmaArr[1] = URLDecoder.decode(parmaArr[1], "utf-8");
                            }
                            pMap.put(parmaArr[0], parmaArr.length > 1 ? parmaArr[1]
                                    : "");
                        }
                    }
                }
            }
        }
        // 定义申请获得的appKey和appSecret
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(openId);
        if (pMap != null && !pMap.isEmpty()) {
            // 对参数名进行字典排序
            String[] keyArray = pMap.keySet().toArray(new String[0]);
            Arrays.sort(keyArray);
            // 拼接有序的参数名-值串
            for (String key : keyArray) {
                stringBuilder.append(key)
                        .append(URLDecoder.decode(pMap.get(key), "utf-8"));
            }
        }
        stringBuilder.append(openKey);
        String codes = stringBuilder.toString();
        // SHA-1编码， 这里使用的是Apache
        // codec，即可获得签名(shaHex()会首先将中文转换为UTF8编码然后进行sha1计算，使用其他的工具包请注意UTF8编码转换)
        /*
         * 以下sha1签名代码效果等同 byte[] sha =
         * org.apache.commons.codec.digest.DigestUtils
         * .sha(org.apache.commons.codec
         * .binary.StringUtils.getBytesUtf8(codes)); String sign =
         * org.apache.commons .codec.binary.Hex.encodeHexString(sha);
         */
        sign = SHA1Encode(codes);
        return sign;
    }

    /**
     * @description sha1算法。使用jdk自带算法
     * @author baoxu
     * @create 2015年8月14日下午5:21:28
     * @version 1.0
     * @param input
     *            需加密字符串
     * @return 加密字符串
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static String SHA1Encode(String input)
        throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest m;
        try {
            if (input == null)
                return null;
            m = MessageDigest.getInstance("sha-1");
            m.update(input.getBytes("UTF-8"));
            byte s[] = m.digest();
            return hex(s);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String hex(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1,
                                                                             3));
        }
        return sb.toString();
    }

}
