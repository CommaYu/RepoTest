package com.tencent.wechat.common.db;

import android.net.Uri;

public class DBConstant {

    /**
     * 数据库名称
     */
    public static final String DB_NAME = "wechat.db";

    /**
     * 表名称
     */
    public static final String TAB_FRIENDS = "friend";

    public static final Uri CONTENT_URI = Uri.parse("content://com.iflytek.wechat/friends");


}
