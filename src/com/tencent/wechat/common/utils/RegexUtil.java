package com.tencent.wechat.common.utils;

import java.util.regex.Pattern;

public class RegexUtil {


    public static final String FIRST_LETTER_STRING = "^[a-zA-Z]{1}";

    public static boolean isLetter(String s) {
        return Pattern.matches(FIRST_LETTER_STRING, s);
    }

    /**
     * 过滤掉除中文、字母、数字以外的字符，以便tts播报
     */
    public static String removeNoneTtsChar(String src) {
        return src.replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
    }

    /**
     * 输入含有单个字符的字符串，判断此字符是否可播
     *
     * @param c
     * @return
     */
    public static boolean isNoneTtsChar(String c) {
        return c.matches("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]");
    }

}



