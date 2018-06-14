package com.tencent.wechat.common;

import java.util.HashMap;
import java.util.Map;

public class Constant {
    public static final String VOICE_FILE_PATH = "/voice";
    public static final String IMAGE_FILE_PATH = "/img";
    public static final String TEMP_FILE_PATH = "/temp";
    public static final String TEMP_IMAGE_FILE_NAME = "/tempimagefile.png";
    public static final String VOICE_RECORD_PATH = "/Record/voice";// 录音存储路径


    public final static String ACTIVITY_TITLE_KEY = "name";

    public final static String SHAREDPREFERENCES_NAME = "wechat_share";


    /*登录异常，网络异常*/
    public static final int LOGIN_STATE_NET_ERROR = -1;

    /*未扫描*/
    public static final int LOGIN_STATE_NOT_SCAN = 1000;

    /*扫描成功,还未确认，此时可以获取头像*/
    public static final int LOGIN_STATE_SCANNED = 1001;

    /*登录成功*/
    public static final int LOGIN_STATE_SUCCESS = 1002;

    /*二维码过期*/
    public static final int LOGIN_STATE_QVODE_INVALID = 1003;


    public static final int MSGTYPE_TEXT = 1;

    public static final int MSGTYPE_IMAGE = 3;

    public static final int MSGTYPE_FILE = 6;

    public static final int MSGTYPE_VOICE = 34;

    public static final int MSGTYPE_VERIFYMSG = 37;   //有人要加我为好友 37

    public static final int MSGTYPE_POSSIBLEFRIEND_MSG = 40;

    public static final int MSGTYPE_SHARECARD = 42;

    public static final int MSGTYPE_VIDEO = 43;

    public static final int MSGTYPE_EMOTICON = 47;

    public static final int MSGTYPE_LOCATION = 48;

    /*分享或转账提示，对于自己手机端发送的分享，网页版无法同步*/
    public static final int MSGTYPE_SHARE = 49;

    public static final int MSGTYPE_VOIPMSG = 50;

    public static final int MSGTYPE_STATUSNOTIFY = 51;

    public static final int MSGTYPE_VOIPNOTIFY = 52;

    public static final int MSGTYPE_VOIPINVITE = 53;

    public static final int MSGTYPE_MICROVIDEO = 62;

    public static final int MSGTYPE_GET_LOCATION = 200; //接收的消息是分享的位置类型

    public static final int MSGTYPE_SYSNOTICE = 9999;


    public static final int MSGTYPE_SYS = 10000;   //系统消息  ，如你已添加xx现在可以开始聊天了，收到红包，手机查看

    public static final int MSGTYPE_RECALLED = 10002;
    public static final int MSG_SEND_STATUS_READY = 0;
    public static final int MSG_SEND_STATUS_SENDING = 1;
    public static final int MSG_SEND_STATUS_SUCC = 2;
    public static final int MSG_SEND_STATUS_FAIL = 5;


    public static final Map<String, String> systemMap = new HashMap<String, String>();

    static {


        //获得uid
        systemMap.put("GET_UUID", "https://login.weixin.qq.com/jslogin");

        //获得扫描图片
        systemMap.put("GET_QCODE", "https://login.weixin.qq.com/qrcode/");

        //获得登陆状态
        systemMap.put("GET_LOGIN_STATUS", "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login");

        //PING
        systemMap.put("WEBWXSTATREPORT_URL", "/cgi-bin/mmwebwx-bin/webwxstatreport?fun=new&lang=zh_CN");

        //初始化聊天界面
        systemMap.put("GET_INIT_URL", "/cgi-bin/mmwebwx-bin/webwxinit");

        //获得联系人
        systemMap.put("GET_BATCH_CONTACT", "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&lang=zh_CN");

        //获取所有好友
        systemMap.put("GET_CONTACT", "/cgi-bin/mmwebwx-bin/webwxgetcontact");

        //判断是否有消息
        systemMap.put("GET_CHECK_MSG", "/cgi-bin/mmwebwx-bin/synccheck");

        //通知手机
        systemMap.put("NOTIFY_PHONE", "/cgi-bin/mmwebwx-bin/webwxstatusnotify");

        //获得消息
        systemMap.put("GET_MESSAGE", "/cgi-bin/mmwebwx-bin/webwxsync?lang=zh_CN");

        //发送消息
        systemMap.put("SEND_MESSAGE", "/cgi-bin/mmwebwx-bin/webwxsendmsg");

        //发送图片信息
        systemMap.put("SEND_IMAGE_MSG", "/cgi-bin/mmwebwx-bin/webwxsendmsgimg?fun=async&f=json");

        //语音url
        systemMap.put("VOID_URL", "/cgi-bin/mmwebwx-bin/webwxgetvoice");

        //图片url
        systemMap.put("IMAGE_URL", "/cgi-bin/mmwebwx-bin/webwxgetmsgimg");

        //位置缩略图url
        systemMap.put("LOC_IMAGE_URL", "/cgi-bin/mmwebwx-bin/webwxgetpubliclinkimg");

        //微视频URL
        systemMap.put("VIDEO_URL", "/cgi-bin/mmwebwx-bin/webwxgetvideo");

        //
        systemMap.put("SYSTEM_REPORT", "/cgi-bin/mmwebwx-bin/webwxstatreport?fun=new&lang=zh_CN");

        //登出
        systemMap.put("LOG_OUT", "/cgi-bin/mmwebwx-bin/webwxlogout");

        //上传多媒体文件
        systemMap.put("UPLOAD_MEDIA", "/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json");

        //上传文件
        systemMap.put("UPLOAD_FILE", "https://file2.wx.qq.com/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json");

        //发送文件
        systemMap.put("SEND_FILE", "/cgi-bin/mmwebwx-bin/webwxsendappmsg?fun=async&f=json&lang=zh_CN");

        //上传声音文件
        systemMap.put("UPLOAD_MEDIAFILE", "http://autodev.openspeech.cn/wx/v1/voice");

        //语音文件下载地址
//        systemMap.put("DOWNLOAD_MEDIAFILE", "http://iflycar.hfdn.openstorage.cn/czwx/");
        systemMap.put("DOWNLOAD_MEDIAFILE", "http://iflycar-test.oss-cn-shanghai.aliyuncs.com/");

        //上传位置的地址
        systemMap.put("UPLOAD_POI", "http://autodev.openspeech.cn/wx/v2.0/marker");
    }

    /*助理包名*/
    public static final String PACKAGE_NAME_SPEECHCLIENT = "com.iflytek.autofly.voicecoreservice";


    /*openKey 用于后台鉴权,由后台给出*/
    public static final String OPENKEY = "21e4c943d057492c9d2568db40bedd1e";

    /*openId 用于后台鉴权,由后台给出*/
    public static final String OPENID = "auto_wx";


    // 请求后台时，传的坐标系，和后台进行约定
    public static final String COORD_TYPE_WGS84="WGS84"; //国际坐标系

    public static final String COORD_TYPE_BD09="BD09";//百度坐标系

    public static final String COORD_TYPE_BD09MC="BD09MC";//百度墨卡托

    public static final String COORD_TYPE_GJC02="GJC02";//火星坐标系，默认为火星坐标系
}
