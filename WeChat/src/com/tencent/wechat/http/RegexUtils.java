package com.tencent.wechat.http;

import android.text.TextUtils;

import com.tencent.wechat.http.entity.PropVo;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    //获得uid
    private static final Pattern uid = Pattern.compile("(?<=(window.QRLogin.uuid\\s{0,3}=\\s{0,3}\"))[^\"]+");

    //获得登陆js返回的状态码
    private static Pattern statusCode = Pattern.compile("(?<=(window.code=))[^;]+");

    //获得重定向的url
    private static Pattern RedirectUrl = Pattern.compile("(?<=(window.redirect_uri=\"))[^\"]+");

    //获得头像base64
    private static Pattern dataUser = Pattern.compile("(?<=(base64,))[^']+");


    //xml 返回的key  用正则 免得使用 jdom
    private static Pattern retPatt = Pattern.compile("(?<=(\\<ret\\>))[^<]+");
    private static Pattern skeyPatt = Pattern.compile("(?<=(\\<skey\\>))[^<]+");
    private static Pattern wxsidPatt = Pattern.compile("(?<=(\\<wxsid\\>))[^<]+");
    private static Pattern wxuinPatt = Pattern.compile("(?<=(\\<wxuin\\>))[^<]+");
    private static Pattern passTicketPatt = Pattern.compile("(?<=(\\<pass_ticket\\>))[^<]+");
    private static Pattern isgrayscalePatt = Pattern.compile("(?<=(\\<isgrayscale\\>))[^<]+");


    public static String getUuid(String content) {
        Matcher m = uid.matcher(content);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    public static String retStatus(String content) {
        Matcher m = statusCode.matcher(content);
        if (m.find()) {
            return m.group();
        }

        return null;
    }


    public static String getRedirectUrl(String content) {

        Matcher m = RedirectUrl.matcher(content);

        if (m.find()) {
            return m.group();
        }


        return null;

    }

    /**
     * 读取配置文件中的key
     *
     * @param ret
     * @return
     */
    public static PropVo readProp(String ret) {
        PropVo prop = null;
        Matcher m = retPatt.matcher(ret);
        if (m.find()) {
            String retCode = m.group();
            if ("0".equals(retCode)) {
                prop = new PropVo();
                m = skeyPatt.matcher(ret);
                if (m.find()) {
                    prop.setSkey(m.group());
                }
                m = wxsidPatt.matcher(ret);
                if (m.find()) {
                    prop.setWxsid(m.group());
                }

                m = wxuinPatt.matcher(ret);
                if (m.find()) {
                    prop.setWxuin(m.group());
                }

                m = passTicketPatt.matcher(ret);
                if (m.find()) {
                    prop.setPassTicket(m.group());
                }

                m = isgrayscalePatt.matcher(ret);
                if (m.find()) {
                    prop.setIsgrayscale(m.group());
                }
            }
        }
        return prop;
    }

    /**
     * 返回位置消息中的经纬度
     *
     * @param url 位置url
     * @return 经纬度  形如   31.830469,117.143898   纬度在前，经度在后
     */
    public static String getLongLatitude(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String REGEX_STR = "coord=([0-9\\.,]+)";
        Pattern p = Pattern.compile(REGEX_STR);
        Matcher m = p.matcher(url);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }


    /**
     * 获得扫描二维码的头像
     *
     * @param status
     * @return
     * @throws IOException
     */
    public static String getHeadImage(String status) throws IOException {
        Matcher m = dataUser.matcher(status);
        if (m.find()) {
            return m.group();
        }
        return "";

    }


}
