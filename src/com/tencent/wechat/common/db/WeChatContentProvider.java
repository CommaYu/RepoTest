package com.tencent.wechat.common.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WeChatContentProvider extends ContentProvider {

    private static final String PROVIDER_NAME = "com.iflytek.wechat.friends";

    private static final int FRIENDS = 1;
    private static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "friends", FRIENDS);
    }

    @Override
    public boolean onCreate() {
        WeChatSQLiteOpenHelper weChatSQLiteOpenHelper = new WeChatSQLiteOpenHelper(getContext());
        db = weChatSQLiteOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        if (uriMatcher.match(uri) == FRIENDS) {
            c = db.query(DBConstant.TAB_FRIENDS, projection, selection, selectionArgs, null,
                    null, null);
        }
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case FRIENDS:
                return "vnd.android.cursor.dir/vnd.wechat.friends";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        
        return 0;
    }

}
