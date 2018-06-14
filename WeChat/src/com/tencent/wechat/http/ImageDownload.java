package com.tencent.wechat.http;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: congqin<br>
 * Data:<br>
 * Description: 自定义下载器，用于替换imageLoader中的默认下载器<br>
 * Note:<br>
 */
public class ImageDownload extends BaseImageDownloader {
    private final HttpWeChat mWechat = WeChatMain.getWeChatMain();

    public ImageDownload(Context context) {
        super(context);
    }

    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra)
            throws IOException {
        byte[] b = mWechat.getResource(imageUri).getData();
        InputStream sbs = new ByteArrayInputStream(b);
        return sbs;
    }
}
