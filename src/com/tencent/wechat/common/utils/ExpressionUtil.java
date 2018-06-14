package com.tencent.wechat.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: congqin
 * Data:2016/12/21.
 * Description: 负责解析名字及文本消息中的表情
 * Note:  表情分为两种，微信自带表情 [微笑]  emoji <span class="emoji emoji1f389"></span>
 */
public class ExpressionUtil {

    private static final String TAG = ExpressionUtil.class.getSimpleName();

    private static final String REGEX_STR_E = "\\[e\\](.*?)\\[/e\\]";  //[e](.*?)[/e]

    private static final String REGEX_STR_SPAN = "<span class=\"emoji emoji(.*?)\"></span>";

    private static final String REGEX_STR_DEFAULT = "\\[(.*?)\\]";


    private static Map<String, String> defaultExpression = new HashMap<String, String>();

    static {
        defaultExpression.put("OK", "ok");
        defaultExpression.put("NO", "no");
        defaultExpression.put("爱你", "aini");
        defaultExpression.put("傲慢", "aoman");
        defaultExpression.put("白眼", "baiyan");
        defaultExpression.put("抱拳", "baoquan");
        defaultExpression.put("鄙视", "bishi");
        defaultExpression.put("闭嘴", "bizui");
        defaultExpression.put("便便", "bianbian");
        defaultExpression.put("擦汗", "cahan");
        defaultExpression.put("菜刀", "caidao");
        defaultExpression.put("差劲", "chajing");
        defaultExpression.put("呲牙", "ciya");
        defaultExpression.put("悠闲", "dabing");
        defaultExpression.put("大哭", "daku");
        defaultExpression.put("蛋糕", "dangao");
        defaultExpression.put("刀", "dao");
        defaultExpression.put("得意", "deyi");
        defaultExpression.put("凋谢", "diaoxie");
        defaultExpression.put("发呆", "fadai");
        defaultExpression.put("发抖", "fadou");
        defaultExpression.put("饭", "fan");
        defaultExpression.put("奋斗", "fendou");
        defaultExpression.put("发怒", "fennu");
        defaultExpression.put("尴尬", "ganga");
        defaultExpression.put("勾引", "gouyin");
        defaultExpression.put("鼓掌", "guzhang");
        defaultExpression.put("哈欠", "haqian");
        defaultExpression.put("害羞", "haixiu");
        defaultExpression.put("憨笑", "hanxiao");
        defaultExpression.put("坏笑", "huaixiao");
        defaultExpression.put("饥饿", "jie");
        defaultExpression.put("惊恐", "jingkong");
        defaultExpression.put("惊讶", "jingya");
        defaultExpression.put("咖啡", "kafei");
        defaultExpression.put("愉快", "keai");
        defaultExpression.put("可怜", "kelian");
        defaultExpression.put("抠鼻", "koubi");
        defaultExpression.put("骷髅", "kulou");
        defaultExpression.put("酷酷", "kuku");
        defaultExpression.put("快哭了", "kuaiku");
        defaultExpression.put("困", "kun");
        defaultExpression.put("篮球", "lanqiu");
        defaultExpression.put("囧", "lenghan");
        defaultExpression.put("礼物", "liwu");
        defaultExpression.put("流汗", "liuhan");
        defaultExpression.put("流泪", "liulei");
        defaultExpression.put("玫瑰", "meigui");
        defaultExpression.put("难过", "nanguo");
        defaultExpression.put("怄火", "ouhuo");
        defaultExpression.put("啤酒", "pijiu");
        defaultExpression.put("瓢虫", "piaochong");
        defaultExpression.put("撇嘴", "piezui");
        defaultExpression.put("乒乓", "pingpang");
        defaultExpression.put("强", "qiang");
        defaultExpression.put("敲打", "qiao");
        defaultExpression.put("亲亲", "qinqin");
        defaultExpression.put("糗大", "qiuda");
        defaultExpression.put("拳头", "quantou");
        defaultExpression.put("弱", "ruo");
        defaultExpression.put("色", "se");
        defaultExpression.put("闪电", "shandian");
        defaultExpression.put("胜利", "shenli");
        defaultExpression.put("嘴唇", "shiai");
        defaultExpression.put("衰", "shuai");
        defaultExpression.put("睡", "shui");
        defaultExpression.put("太阳", "taiyang");
        defaultExpression.put("调皮", "tiaopi");
        defaultExpression.put("跳跳", "tiaotiao");
        defaultExpression.put("偷笑", "touxiao");
        defaultExpression.put("吐", "tu");
        defaultExpression.put("微笑", "weixiao");
        defaultExpression.put("委屈", "weiqu");
        defaultExpression.put("握手", "woshou");
        defaultExpression.put("西瓜", "xigua");
        defaultExpression.put("吓", "xia");
        defaultExpression.put("爱心", "xin");
        defaultExpression.put("心碎", "xinsui");
        defaultExpression.put("嘘", "xu");
        defaultExpression.put("疑问", "yiwen");
        defaultExpression.put("阴险", "yinxiao");
        defaultExpression.put("拥抱", "yongbao");
        defaultExpression.put("右哼哼", "youhengheng");
        defaultExpression.put("月亮", "yueliang");
        defaultExpression.put("晕", "yun");
        defaultExpression.put("再见", "zaijian");
        defaultExpression.put("炸弹", "zhadan");
        defaultExpression.put("折磨", "zhemo");
        defaultExpression.put("咒骂", "zhouma");
        defaultExpression.put("猪头", "zhutou");
        defaultExpression.put("抓狂", "zhuakuang");
        defaultExpression.put("转圈", "zhuanquan");
        defaultExpression.put("足球", "zuqiu");
        defaultExpression.put("左哼哼", "zuohengheng");
        defaultExpression.put("嘿哈", "1f4aa");
        defaultExpression.put("耶", "270c");
        defaultExpression.put("皱眉", "1f620");
        defaultExpression.put("机智", "1f609");
        defaultExpression.put("奸笑", "1f60f");
        defaultExpression.put("捂脸", "touxiao");
        defaultExpression.put("茶", "2615");
        defaultExpression.put("红包", "1f4b0");
        defaultExpression.put("蜡烛", "2668");
    }


    /**
     * 将形如  <span class="emoji emoji1f61d"></span>  转换成  [e]1f61d[/e]
     *
     * @param str
     * @return
     */
    public static String getEmoji(String str) {
        String result = str;
        Pattern p = Pattern.compile(REGEX_STR_SPAN);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String emoji = m.group();
            String emojiCode = emoji.substring(24, emoji.indexOf("\"></span>"));
            result = result.replace(emoji, "[e]" + emojiCode + "[/e]");
        }
        return result;
    }

    /**
     * 清除emoji字符串
     *
     * @param str
     * @return
     */
    public static String removeEmoji(String str) {
        String result = str;
        Pattern p = Pattern.compile(REGEX_STR_SPAN);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String emoji = m.group();
            result = result.replace(emoji, "");
        }
        return result;
    }


    /**
     * 清除默认表情字符串，如[大哭]
     *
     * @param str
     * @return
     */
    public static String removeDefault(String str) {
        String result = str;
        Pattern p = Pattern.compile(REGEX_STR_DEFAULT);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String group0 = m.group();
            String group1 = m.group(1);
            if (defaultExpression.containsKey(group1)) {
                result = result.replace(group0, "");
            }
        }
        return result;
    }

    /**
     * @param context
     * @param str
     * @return
     * @desc <pre>
     * 将形如[e]1f61d[/e]替换成表情图片
     * </pre>
     * @author Weiliang Hu
     * @date 2013-12-17
     */
    public static SpannableString getExpressionString(Context context, String str) {
        SpannableString spannableString = new SpannableString(str);
        Pattern sinaPatten = Pattern.compile(REGEX_STR_E, Pattern.CASE_INSENSITIVE);
        dealExpression(context, spannableString, sinaPatten, 0);
        return spannableString;
    }

    /**
     * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
     */
    private static void dealExpression(Context context, SpannableString spannableString, Pattern patten, int start) {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            Log.d(TAG, "dealExpression: ===================find");
            try {
                String key = matcher.group();
                Log.d("Key", key);
                if (matcher.start() < start) {
                    Log.e(TAG, "dealExpression: < start");
                    continue;
                }


//                if (i < 10) {
//                    id = mContext.getResources().getIdentifier(
//                            "view_loading_0000" + i, "drawable",
//                            mContext.getPackageName());
//                } else {
//                    id = mContext.getResources().getIdentifier(
//                            "view_loading_000" + i, "drawable",
//                            mContext.getPackageName());
//                }


//                Field field = R.drawable.class.getDeclaredField("emoji_"    // java.lang.NoSuchFieldException
//                        + key.substring(24, key.indexOf("\"></span>")));
//                if(field==null){
//                    Log.e(TAG, "dealExpression: field is null");
//                }
//                int resId = Integer.parseInt(field.get(null).toString());

                int resId = context.getResources().getIdentifier("emoji_" + key.substring(24, key.indexOf
                        ("\"></span>")), "drawable", context.getPackageName());


                Log.d(TAG, "dealExpression: resId====>" + resId + "|emoji=" + key.substring(24, key.indexOf
                        ("\"></span>")));
                if (resId != 0) {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                    ImageSpan imageSpan = new ImageSpan(bitmap);
//                    TextAppearanceSpan
                    int end = matcher.start() + key.length();
                    spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                } else {

//                    Spannable is a CharSequence and you should be able to use
//                    CharSequence.subSequence(int start, int end) without

                    Log.d(TAG, "dealExpression: ======>else");
                    int end = matcher.start() + key.length();
                    spannableString.setSpan("[表情]", matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//                    spannableString.subSequence();
                    CharSequence cq = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


//    public static SpannableString parseEmoji(Context context, String content) {
//
//        Pattern p = Pattern.compile(REGEX_STR_SPAN, Pattern.CASE_INSENSITIVE);
//
//        SpannableString spannableString = new SpannableString(content);
//        dealExpression(context, spannableString, p, 0);
//
//        return spannableString;
//    }

    /**
     * 将内容字符串中的emoji 形如<span class="emoji emoji1f61d"></span>
     * 替换成相应图片，没有资源则直接替换成 [表情]
     *
     * @param context
     * @param content
     * @return
     */
    public static CharSequence parseEmoji(Context context, CharSequence content) {

        Pattern pattern = Pattern.compile(REGEX_STR_SPAN, Pattern.CASE_INSENSITIVE);

        SpannableStringBuilder ret = new SpannableStringBuilder();

        Matcher matcher = pattern.matcher(content);

        int start = 0;
        int end = 0;

        while (matcher.find()) {
            String key = matcher.group();
            start = matcher.start();
            ret.append(content, end, start);
            end = start + key.length();
            ret.append("[表情]");

            int resId = context.getResources().getIdentifier("emoji_" + key.substring(24, key.indexOf
                    ("\"></span>")), "drawable", context.getPackageName());

            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                ImageSpan imageSpan = new ImageSpan(bitmap);
                ret.setSpan(imageSpan, ret.length() - 4, ret.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        ret.append(content, end, content.length());
        return ret;
    }

    /**
     * 解析微信自带表情  如[微笑]
     *
     * @param context
     * @param content
     * @return
     */
    public static CharSequence parseDefault(Context context, CharSequence content) {
        SpannableString spannableString = new SpannableString(content);

        Pattern pattern = Pattern.compile(REGEX_STR_DEFAULT, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1);

            String str = defaultExpression.get(key);

            if (str != null) {
                int id = context.getResources().getIdentifier("default_" + str, "drawable", context.getPackageName());
                if (id != 0) {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
                    ImageSpan imageSpan = new ImageSpan(bitmap);
                    int end = matcher.start() + key.length();
                    spannableString.setSpan(imageSpan, matcher.start(), matcher.end(), Spannable
                            .SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }

        return spannableString;
//            Log.d(TAG, "dealExpression: ===================find");
//            try {
//                String key = matcher.group();
//                Log.d("Key", key);
//                if (matcher.start() < start) {
//                    Log.e(TAG, "dealExpression: < start");
//                    continue;
//                }
//
//
////                if (i < 10) {
////                    id = mContext.getResources().getIdentifier(
////                            "view_loading_0000" + i, "drawable",
////                            mContext.getPackageName());
////                } else {
////                    id = mContext.getResources().getIdentifier(
////                            "view_loading_000" + i, "drawable",
////                            mContext.getPackageName());
////                }
//
//
////                Field field = R.drawable.class.getDeclaredField("emoji_"    // java.lang.NoSuchFieldException
////                        + key.substring(24, key.indexOf("\"></span>")));
////                if(field==null){
////                    Log.e(TAG, "dealExpression: field is null");
////                }
////                int resId = Integer.parseInt(field.get(null).toString());
//
//                int resId = context.getResources().getIdentifier("emoji_" + key.substring(24, key.indexOf
//                        ("\"></span>")), "drawable", context.getPackageName());
//
//
//                Log.d(TAG, "dealExpression: resId====>" + resId + "|emoji=" + key.substring(24, key.indexOf
//                        ("\"></span>")));
//                if (resId != 0) {
//                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
//                    ImageSpan imageSpan = new ImageSpan(bitmap);
////                    TextAppearanceSpan
//                    int end = matcher.start() + key.length();
//                    spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//
//                } else {
//
////                    Spannable is a CharSequence and you should be able to use
////                    CharSequence.subSequence(int start, int end) without
//
//                    Log.d(TAG, "dealExpression: ======>else");
//                    int end = matcher.start() + key.length();
//                    spannableString.setSpan("[表情]", matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
////                    spannableString.subSequence();
//                    CharSequence cq = null;
//                }

    }

}
