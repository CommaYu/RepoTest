package com.tencent.wechat.common.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeChatSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public WeChatSQLiteOpenHelper(Context context) {
        super(context, DBConstant.DB_NAME, null, DATABASE_VERSION);
        ;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建好友信息表
        db.execSQL("create table " + DBConstant.TAB_FRIENDS + "(" +
                FriendColumn._ID + " integer primary key autoincrement not null," +
                FriendColumn.USER_ID + " text not null," +
                FriendColumn.NICK_NAME + " text," +
                FriendColumn.REMARK_NAME + " text," +
                FriendColumn.USER_HEAD + " text);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        

    }

    public SQLiteDatabase getSQLiteDatabase() {
        return getWritableDatabase();
    }


}
