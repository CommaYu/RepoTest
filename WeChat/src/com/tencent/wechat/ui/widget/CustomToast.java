package com.tencent.wechat.ui.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.wechat.R;

import java.io.IOException;

/**
 * Author: congqin<br>
 * Data: 2017/1/12<br>
 * Description: 自定义toast 带音效<br>
 * Note:<br>
 */
public class CustomToast {


    /**
     *带默认音效  error.mp3
     * @param context
     * @param content
     * @param duration
     */
    public static void showToast(Context context, String content, int duration) {
        showToast(context, content, duration, true);
    }

    /**
     *
     * @param context
     * @param content
     * @param duration
     * @param needSoundEffect  是否带音效，如果为true 则带默认音效
     */
    public static void showToast(Context context, String content, int duration, boolean needSoundEffect) {
        showToast(context, content, duration, needSoundEffect, -1);
    }

    /**
     *
     * @param context
     * @param content
     * @param duration
     * @param needSoundEffect  是否带音效
     * @param rawId 声音文件资源id
     */
    public static void showToast(Context context, String content, int duration, boolean needSoundEffect, int rawId) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(content);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();

        if (needSoundEffect) {
            int id = rawId > 0 ? rawId : R.raw.error;
            MediaPlayer mp = new MediaPlayer();
            mp.reset();
            try {
                mp.setDataSource(context,
                        Uri.parse("android.resource://" + context.getPackageName() + "/" + id));
                mp.prepare();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mp.start();
        }
    }
}