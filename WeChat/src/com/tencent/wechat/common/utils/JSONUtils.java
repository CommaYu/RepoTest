/**
 * Copyright (c) 2014,TravelSky.
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * <p/>
 * Project Name:TravelskyMobileTools
 * Package Name:com.travelsky.mrt.tmt.util
 * File Name:JSONUtils.java
 * Date:2015年7月20日 15:17:00
 */
package com.tencent.wechat.common.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * 类名: JSONUtils <br/>
 * 描述: JSON工具类(GSON)<br/>
 * 日期: 2014-4-15 下午4:12:22 <br/>
 * <br/>
 *
 * @author xhyin
 * @since 1.0
 * @version 产品版本信息 yyyy-mm-dd xhyin 修改信息<br/>
 * @see
 */
public final class JSONUtils {

    /**
     * 消息标识
     */
    public static final String TAG = JSONUtils.class.getSimpleName();

    /**
     * 空的 {@code JSON} 数据 - <code>"{}"</code>。
     */
    public static final String EMPTY_JSON = "{}";

    /**
     * 空的 {@code JSON} 数组(集合)数据 - {@code "[]"}。
     */
    public static final String EMPTY_JSON_ARRAY = "[]";

    // 默认日期格式
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss SSS";

    /**
     *
     * 禁止外部实例化<br/>
     */
    private JSONUtils() {

    }

    /**
     *
     * 根据指定的时间格式创建Gson对象
     *
     * @param datePattern
     *             日期格式模式。
     * @return Gson对象
     * @since 1.0
     * @author xhyin
     * @date 2014-4-16
     */
    private static Gson createGson(String datePattern, boolean excludesFieldsWithoutExpose) {
        return createGson(false, datePattern, excludesFieldsWithoutExpose);
    }

    private static Gson createGson(boolean isSerializeNulls, String datePattern, boolean excludesFieldsWithoutExpose) {
        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        if (isSerializeNulls) {
            builder.serializeNulls();
        }
        if (TextUtils.isEmpty(datePattern)) {
            datePattern = DEFAULT_DATE_PATTERN;
        }
        builder.setDateFormat(datePattern);
        if (excludesFieldsWithoutExpose) {
            builder.excludeFieldsWithoutExposeAnnotation();
        }
        return builder.create();
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。
     *
     * @param <T>
     *            要转换的目标类型。
     * @param json
     *            给定的 {@code JSON} 字符串。
     * @param token
     *            {@code com.google.gson.reflect.TypeToken} 的类型指示类对象。
     * @param datePattern
     *            日期格式模式。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static <T> T fromJson(String json, TypeToken<T> token, String datePattern) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        Gson gson = createGson(datePattern, false);
        try {
            return gson.fromJson(json, token.getType());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。
     *
     * @param <T>
     *            要转换的目标类型。
     * @param json
     *            给定的 {@code JSON} 字符串。
     * @param token
     *            {@code com.google.gson.reflect.TypeToken} 的类型指示类对象。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static <T> T fromJson(String json, TypeToken<T> token) {
        return fromJson(json, token, null);
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。<strong>此方法通常用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     *
     * @param <T>
     *            要转换的目标类型。
     * @param json
     *            给定的 {@code JSON} 字符串。
     * @param clazz
     *            要转换的目标类。
     * @param datePattern
     *            日期格式模式。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static <T> T fromJson(String json, Class<T> clazz, String datePattern) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        Gson gson = createGson(datePattern, false);
        // LogUtils.e(TAG, "gson = " + gson);
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将给定的 {@code JSON} 字符串转换成指定的类型对象。<strong>此方法通常用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     *
     * @param <T>
     *            要转换的目标类型。
     * @param json
     *            给定的 {@code JSON} 字符串。
     * @param clazz
     *            要转换的目标类。
     * @return 给定的 {@code JSON} 字符串表示的指定的类型对象。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return fromJson(json, clazz, null);
    }

    /**
     * 将给定的目标对象根据指定的条件参数转换成 {@code JSON} 格式的字符串。
     * <p />
     * <strong>该方法转换发生错误时，不会抛出任何异常。若发生错误时，曾通对象返回 <code>"{}"</code>； 集合或数组对象返回
     * <code>"[]"</code></strong>
     *
     * @param target
     *            目标对象。
     * @param targetType
     *            目标对象的类型。
     * @param isSerializeNulls
     *            是否序列化 {@code null} 值字段。
     * @param datePattern
     *            日期字段的格式化模式。
     * @param excludesFieldsWithoutExpose
     *            是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target, Type targetType, boolean isSerializeNulls, String datePattern,
                                boolean excludesFieldsWithoutExpose) {
        if (target == null) {
            return EMPTY_JSON;
        }
        Gson gson = createGson(isSerializeNulls, datePattern, excludesFieldsWithoutExpose);
        String result;
        try {
            if (targetType != null) {
                result = gson.toJson(target, targetType);
            } else {
                result = gson.toJson(target);
            }
        } catch (Exception e) {
            result = (target instanceof Collection || target instanceof Iterator || target instanceof Enumeration ||
                    target
                    .getClass().isArray()) ? EMPTY_JSON_ARRAY : EMPTY_JSON;
        }
        return result;
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target
     *            要转换成 {@code JSON} 的目标对象。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target) {
        return toJson(target, null, false, null, false);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * </ul>
     *
     * @param target
     *            要转换成 {@code JSON} 的目标对象。
     * @param datePattern
     *            日期字段的格式化模式。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target, String datePattern) {
        return toJson(target, null, false, datePattern, false);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法只用来转换普通的 {@code JavaBean}
     * 对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target
     *            要转换成 {@code JSON} 的目标对象。
     * @param excludesFieldsWithoutExpose
     *            是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target, boolean excludesFieldsWithoutExpose) {
        return toJson(target, null, false, null, excludesFieldsWithoutExpose);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法只会转换标有 {@literal @Expose} 注解的字段；</li>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSSS}；</li>
     * </ul>
     *
     * @param target
     *            要转换成 {@code JSON} 的目标对象。
     * @param targetType
     *            目标对象的类型。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target, Type targetType) {
        return toJson(target, targetType, false, null, false);
    }

    /**
     * 将给定的目标对象转换成 {@code JSON} 格式的字符串。<strong>此方法通常用来转换使用泛型的对象。</strong>
     * <ul>
     * <li>该方法不会转换 {@code null} 值字段；</li>
     * <li>该方法会转换所有未标注或已标注 {@literal @Since} 的字段；</li>
     * <li>该方法转换时使用默认的 日期/时间 格式化模式 - {@code yyyy-MM-dd HH:mm:ss SSS}；</li>
     * </ul>
     *
     * @param target
     *            要转换成 {@code JSON} 的目标对象。
     * @param targetType
     *            目标对象的类型。
     * @param excludesFieldsWithoutExpose
     *            是否排除未标注 {@literal @Expose} 注解的字段。
     * @return 目标对象的 {@code JSON} 格式的字符串。
     * @since 1.0
     * @author xhyin
     * @date 2014-4-15
     */
    public static String toJson(Object target, Type targetType, boolean excludesFieldsWithoutExpose) {
        return toJson(target, targetType, false, null, excludesFieldsWithoutExpose);
    }

}
