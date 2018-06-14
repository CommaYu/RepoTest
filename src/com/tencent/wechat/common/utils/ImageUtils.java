package com.tencent.wechat.common.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageUtils {

    // 添加拍照后的图片到媒体库
    public static void noticePhotoUpdate(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 获取自定义照相图片存储路径
     *
     * @return
     */
    public static String getCameraPath() {
        String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        String DIRECTORY = DCIM + "/Camera/";
        return DIRECTORY;
    }


    public static byte[] bitmap2Byte(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();
        return bitmapByte;
    }
}
