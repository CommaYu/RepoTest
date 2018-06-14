package com.tencent.wechat.common.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.wechat.R;
import com.tencent.wechat.common.Constant;
import com.tencent.wechat.WeChatApplication;
import com.tencent.wechat.http.entity.FriendVo;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeChatUtil {

    private static final String TAG = "WeChatUtil";

    public static String getPgvPvi() {
        return "60" + getRandomNum(8);
    }

    public static String getPgvSi() {
        return "s60" + getRandomNum(8);
    }

    public static String getDeviceID() {
        return "e" + getRandomNum(15);
    }

    public static String getProp(String key, String refer) {
        if (!TextUtils.isEmpty(refer)) {
            String host = getHost(refer);
            String url = Constant.systemMap.get(key);
            if (host.endsWith("/")) {
                return host + url.substring(1);
            }
            return host + url;
        }
        return Constant.systemMap.get(key);
    }

    public static String getHost(String refer) {
        if (refer.indexOf("wx2") != -1) {
            return "https://wx2.qq.com/";
        }
        return "https://wx.qq.com/";
    }

    public static String getFileHost(String refer) {
        if (refer.indexOf("wx2") != -1) {
            return "https://file2.wx.qq.com";
        }
        return "https://file.wx.qq.com";
    }

    public static String getRandomNum(int num) {
        StringBuffer sb = new StringBuffer();
        int n;
        for (int i = 0; i < num; i++) {
            n = 1 + (int) (Math.random() * 10);
            sb.append(String.valueOf(n));
        }
        return sb.toString();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;

    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * byte[]转Bitmap
     *
     * @param b byte[]
     * @return Bitmap
     */
    public static Bitmap bytes2Bimap(byte[] b) {
        if (b != null && b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    /**
     * @param filePath 相对路径 例如："/voice”
     * @param fileName 文件名称例如："/sun.png"
     */
    public static String getFilePath(String filePath, String fileName) {
        // 判断sd卡是否可用
        String absolutePath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 可用
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            absolutePath = rootDir + "/wechat" + filePath;
            // creatDirFile(rootDir + "/wechat" + filePath);
            // // 创建文件
            // saveFile(rootDir + "/wechat" + filePath + fileName, b);
        } else {
            // 不可用
            absolutePath = WeChatApplication.getContext().getFilesDir().getPath() + filePath;

        }
        // 创建文件夹
        creatDirFile(absolutePath);
        // 创建文件
        return absolutePath + fileName;
    }

    /**
     * @param filePath 相对路径 例如："/voice”
     * @param fileName 文件名称例如："/sun.png"
     * @param b        数据
     */
    public static String saveFile(String filePath, String fileName, byte[] b) {
        // 判断sd卡是否可用
        String absolutePath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 可用
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
           // File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            absolutePath = rootDir + "/wechat" + filePath;
        } else {
            // 不可用
            absolutePath = WeChatApplication.getContext().getFilesDir().getPath() + filePath;

        }
      //  absolutePath = WeChatApplication.getContext().getFilesDir().getPath() + filePath;
        Log.d("Debug",absolutePath);
        // 创建文件夹
        creatDirFile(absolutePath);
        // 创建文件
        saveFile(absolutePath + fileName, b);
        return absolutePath + fileName;
    }

    public static String getRootPath() {
        // 判断sd卡是否可用
        String absolutePath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 可用
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            absolutePath = rootDir + "/wechat";
        } else {
            // 不可用
            absolutePath = WeChatApplication.getContext().getFilesDir().getPath();

        }
        return absolutePath;
    }

    public static void deleteAllCache(String filePath) {
        File root = new File(filePath);
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllCache(f.getAbsolutePath());
                    try {
                        final File to = new File(f.getAbsolutePath() + System.currentTimeMillis());
                        f.renameTo(to);
                        to.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllCache(f.getAbsolutePath());
                        try {
                            final File to = new File(f.getAbsolutePath() + System.currentTimeMillis());
                            f.renameTo(to);
                            to.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    /**
     * 创建文件
     *
     * @param fileName 文件名“路径+名称”
     * @param b        数据
     */
    private static void saveFile(String fileName, byte[] b) {
        File file = new File(fileName);
        if(!file.getParentFile().exists()){
            Log.e(TAG, "saveFile: dfdfafafdaf null");
            file.getParentFile().mkdirs();
        }
        try {
            // 在指定的文件夹中创建文件
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e("保存文件异常", e.getMessage());
        }
    }

    private static void creatDirFile(String filePath) {
        File dirFile = new File(filePath);
        if (!dirFile.exists()) {
            // 如果该目录不存在则先创建目录
            dirFile.mkdirs();
        }
    }

    /**
     * 获取APP分享信息内容
     */
    public static String getAppMsgContent(String s) {
        String content = "";
        int start = s.indexOf("&lt;des&gt;") + "&lt;des&gt;".length();
        int end = s.indexOf("&lt;/des&gt");
        try {
            content = s.substring(start, end);
        } catch (Exception e) {
            content = "";
        }

        return content;
    }

    public static void playVoice(String name) {
        File dir = new File(name);
        // 播放音频文件
        MediaPlayer mediaPlayer = new MediaPlayer();
        FileInputStream fis;

        try {
            fis = new FileInputStream(dir);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            
            e.printStackTrace();
        }
    }

    /**
     * URL转换为链接
     *
     * @param urlText
     * @return String
     * @author Boyer
     */
    public static String urlToLink(String urlText) {
        // 匹配的条件选项为结束为空格(半角和全角)、换行符、字符串的结尾或者遇到其他格式的文本
        String regexp = "(((http|ftp|https|file)://)|((?<!((http|ftp|https|file)://))www\\.))" // 以http...或www开头
                + ".*?" // 中间为任意内容，惰性匹配
                + "(?=(&nbsp;|\\s|　|<br />|$|[<>]))"; // 结束条件
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(urlText);
        StringBuffer stringbuffer = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group().substring(0, 3).equals("www") ? "http://" + matcher.group() : matcher.group();
            String tempString = "<a href=\"" + url + "\">" + matcher.group() + "</a>";
            // 这里对tempString中的"\"和"$"进行一次转义，因为下面对它替换的过程中appendReplacement将"\"和"$"作为特殊字符处理
            int tempLength = tempString.length();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < tempLength; ++i) {
                char c = tempString.charAt(i);
                if (c == '\\' || c == '$') {
                    buffer.append("\\").append(c);
                } else {
                    buffer.append(c);
                }
            }
            tempString = buffer.toString();
            matcher.appendReplacement(stringbuffer, tempString);
        }
        matcher.appendTail(stringbuffer);
        return stringbuffer.toString();
    }

    /**
     * 将时间戳转化为Sting
     *
     * @param time
     * @return
     */
    public static String convertTimeToStr(long time) {
        long b = new Long(time + "000");
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
        String d = format.format(b);
        return d;
    }

    /**
     * 判断消息列表中消息条目是否应该显示时间
     *
     * @param end   本条消息的时间
     * @param start 上条消息的时间
     * @return 当时间间隔大于5min返回true, 否则返回false
     */
    public static boolean needShowTime(long end, long start) {
        if ((end - start) > 60 * 5) {
            return true;
        }
        return false;
    }

    /**
     * 得到mp3的时长
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static int getMp3Duration(File file) {
        try {
            MP3File f = (MP3File) AudioFileIO.read(file);
            MP3AudioHeader audioHeader = (MP3AudioHeader) f.getAudioHeader();
            return audioHeader.getTrackLength();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 得到amr的时长
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static int getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0,
                0, 0};
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            long length = file.length();// 文件的长度
            int pos = 6;// 设置初始位置
            int frameCount = 0;// 初始帧数
            int packedPos = -1;

            byte[] datas = new byte[1];// 初始数据值
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }

            duration += frameCount * 20;// 帧数*20
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return (int) ((duration / 1000) + 1);
    }

    /**
     * 判断是否是 非好友，如文件助手之类的
     *
     * @param friendVo
     * @return 非好友返回true, 否则返回false
     */
    public static boolean isNotFriend(FriendVo friendVo) {
        return friendVo.getVerifyFlag() != 0 || "fil".equals(friendVo.getKeyWord()) || "fme".equals(friendVo
                .getKeyWord());
    }

    /**
     * 判断收到该好友新消息时是否通知，如在手机端屏蔽了某些群的消息，则返回false
     *
     * @param friendVo
     * @return 需要通知返回true, 不需要通知返回false
     */
    public static boolean isFriendNotify(FriendVo friendVo) {
        if (friendVo == null) return false;
        boolean isNotify = true;
        Integer status = friendVo.getStatues();
        if (status == null) {
            return isNotify;
        }
        int contactFlag = friendVo.getContactFlag();
        String uid = friendVo.getUserName();
        if (uid.startsWith("@@")) {
            if (status == 0) {
                isNotify = false;
            }
        } else {
            if (contactFlag == 515 || contactFlag == 513) {
                isNotify = false;
            }
        }
        return isNotify;
    }

    /**
     * 获取好友的名字，有备注名返回备注名，否则返回昵称
     *
     * @param friendVo
     * @return 好友的名字
     */
    public static String getFriendName(FriendVo friendVo) {
        String name = TextUtils.isEmpty(friendVo.getRemarkName()) ? friendVo
                .getNickName() : friendVo.getRemarkName();
        String userName = friendVo.getUserName();

        if (TextUtils.isEmpty(name) && userName.startsWith("@@")) {
            //群聊天时，会存在没有名字的情况，把组内成员的名称串起来当做群组名
            List<FriendVo> members = friendVo.getMemberList();
            if (members != null && !members.isEmpty()) {
                for (int i = 0; i < members.size(); i++) {
                    FriendVo member = members.get(i);
                    if (i != 0) {
                        name += ",";
                    }
                    name += TextUtils.isEmpty(member.getRemarkName()) ? member
                            .getNickName() : member.getRemarkName();
                    if (i >= 2) {
                        break;
                    }
                }
            }
        }
        return name;
    }

    /**
     * 判断应用是否在前台运行
     *
     * @param context
     * @return 在前台运行返回true, 否则返回false
     */
    public static boolean isRunningForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }

    /**
     * 检测收到的文本消息是否是通过另一台车机发送的语音消息<br>
     * 判断是否符合 模式 "（此消息由车载微信发送，获取语音请访问：" + url +"）
     *
     * @param msgContent 消息内容
     * @return 如果是车机发送的语音，则返回下载url，否则返回null
     */
    public static String getCarVoiceUrl(String msgContent) {

        String REGEX_STR = "（" + WeChatApplication.getContext().getString(R.string.mark_send_by_car) + "(.*?)）";

        String url = null;
        Pattern p = Pattern.compile(REGEX_STR);
        Matcher m = p.matcher(msgContent);
        while (m.find()) {
            //这个url是h5页面的url
            url = m.group(1);
            int index = url.indexOf("=");
            //这个url 是下载url
            url = Constant.systemMap.get("DOWNLOAD_MEDIAFILE") + url.substring(index + 1, url.length());
            Log.d(TAG, "getCarUrl: url=" + url);
        }
        return url;
    }

    /**
     * 检测收到的文本消息是否是通过另一台车机发送的位置消息<br>
     * 判断是否符合 模式 （位置： url  ）
     *
     * @param msgContent 消息内容
     * @return 如果是车机发送的位置，则返回h5 链接，形如http://apis.map.qq.com/uri/v1/geocoder?referer=car&coord=31.834399,117.143269；否则返回null
     */
    public static String getCarPoiUrl(String msgContent) {

        String REGEX_STR = "（位置：(.*?)）";

        String url = null;
        Pattern p = Pattern.compile(REGEX_STR);
        Matcher m = p.matcher(msgContent);
        while (m.find()) {
            url = m.group(1);//这个url是h5页面的url
        }
        return url;
    }

    /**
     * 对于另一台车机发来的位置消息，移除开头的“我在” 和后面的位置链接
     *
     * @param msgContent
     * @return
     */
    public static String removeCarPoi(String msgContent) {
        String ret = null;
        ret = msgContent.replace("我在", "");
        ret = ret.replaceAll("（位置：.*", "");
        return ret;
    }

    /**
     * 移除内容中的超链接  形如  对方验证通过后，才能聊天。&lt;a href="weixin://findfriend/verifycontact"&gt;发送朋友验证&lt;/a&gt;
     *
     * @param content
     * @return
     */
    public static String removeHref(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        return content.replaceAll("&lt;.*&gt;", "");
    }
}
