/**
 * Copyright (c) 2014,TravelSky.
 * All Rights Reserved.
 * TravelSky CONFIDENTIAL
 * <p/>
 * Project Name:SkyOne4Android
 * Package Name:com.travelsky.pss.skyone.widget
 * File Name:RectangleStatisticsView.java
 * Date:2014-4-9 上午10:00:37
 */
package com.tencent.wechat.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 带圆角的LinearLayout，绘制时自动会裁剪出圆角效果。仅仅影响子View的绘制，不会对背景做出圆角效果。
 *
 * @author zhanghdo
 * @version 1.0 2013-11-27 上午11:48:04
 * @since 1.0 2013-11-27 上午11:48:04
 */
public class RoundedLinearLayout extends LinearLayout {

    private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
            | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG;

    /**
     * 上面两个角为圆角效果
     */
    public static final int MODE_TOP_ONLY = 0;

    /**
     * 下面两个角为圆角效果
     */
    public static final int MODE_BOTTOM_ONLY = 1;

    /**
     * 左面两个角为圆角效果
     */
    public static final int MODE_LEFT_ONLY = 2;

    /**
     * 右面两个角为圆角效果
     */
    public static final int MODE_RIGHT_ONLY = 3;

    /**
     * 所有角都为圆角效果
     */
    public static final int MODE_ALL = 4;
    /**
     * 圆角X轴默认半径
     */
    private static final int DEFAULT_RADIUS = 30;

    /**
     * Canvas 绘制参数
     */
    private static final int DEFAULT_ALPHA = 0xff;

    /**
     * 裁剪模式，默认为MODE_ALL，即所有角都为圆角效果
     */
    private transient int mClipMode = MODE_ALL;

    /**
     * 圆角x轴半径，默认30
     */
    private transient float rx = DEFAULT_RADIUS;

    /**
     * 圆角y轴半径，默认30
     */
    private transient float ry = DEFAULT_RADIUS;

    /**
     * 画笔
     */
    private transient Paint mPaint;

    /**
     * 用于设置画笔的擦除模式
     */
    private transient Xfermode xfermode;

    /**
     * 构造方法<br/>
     *
     * @param context {@link Context}
     * @param attrs   {@link AttributeSet}
     */
    public RoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // setWillNotDraw(false)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint = new Paint();
        xfermode = new PorterDuffXfermode(Mode.SRC_OUT);
    }

    /**
     * 描述 RoundedLinearLayout<br/>
     *
     * @param context 上下文
     */
    public RoundedLinearLayout(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mPaint.reset();
        // 创建新图层，将子控件绘制在其上
        canvas.saveLayerAlpha(0, 0, getWidth(), getBottom(), DEFAULT_ALPHA, LAYER_FLAGS);
        super.dispatchDraw(canvas);
        mPaint.setXfermode(xfermode);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(0);
        mPaint.setAntiAlias(true);
        // 擦除圆角矩形以外的区域
        canvas.drawPath(getClipPath(), mPaint);
        canvas.restore();
    }

    /**
     * 获取待擦除的圆角以外的区域
     *
     * @return 待擦除的圆角以外的区域
     * @author zhanghdo
     * @date 2013-11-27
     * @since 1.0
     */
    private Path getClipPath() {
        Path path = new Path();
        RectF roundRectF = new RectF();
        roundRectF.top = 0;
        roundRectF.left = 0;
        roundRectF.bottom = getHeight();
        roundRectF.right = getWidth();
        // 每个圆角的x、y半径
        float[] radii = new float[8];
        // 根据不同的模式，设置好矩形圆角
        switch (mClipMode) {

            case MODE_ALL:
                for (int i = 0; i < 8; i++) {
                    if (i % 2 == 0) {
                        radii[i] = rx;
                    } else {
                        radii[i] = ry;
                    }
                }
                break;
            case MODE_BOTTOM_ONLY:
                for (int i = 0; i < 8; i++) {
                    if (i >> 1 != 2 && i >> 1 != 3) {
                        radii[i] = 0;
                        continue;
                    }
                    if (i % 2 == 0) {
                        radii[i] = rx;
                    } else {
                        radii[i] = ry;
                    }
                }
                break;
            case MODE_LEFT_ONLY:
                for (int i = 0; i < 8; i++) {
                    if (i >> 1 != 0 && i >> 1 != 2) {
                        radii[i] = 0;
                        continue;
                    }
                    if (i % 2 == 0) {
                        radii[i] = rx;
                    } else {
                        radii[i] = ry;
                    }
                }
                break;
            case MODE_RIGHT_ONLY:
                for (int i = 0; i < 8; i++) {
                    if (i >> 1 != 1 && i >> 1 != 3) {
                        radii[i] = 0;
                        continue;
                    }
                    if (i % 2 == 0) {
                        radii[i] = rx;
                    } else {
                        radii[i] = ry;
                    }
                }
                break;
            case MODE_TOP_ONLY:
                for (int i = 0; i < 8; i++) {
                    if (i >> 1 != 0 && i >> 1 != 1) {
                        radii[i] = 0;
                        continue;
                    }
                    if (i % 2 == 0) {
                        radii[i] = rx;
                    } else {
                        radii[i] = ry;
                    }
                }
                break;
            default:
                break;
        }
        // 区域取反
        path.setFillType(FillType.INVERSE_WINDING);
        path.addRoundRect(roundRectF, radii, Direction.CW);
        return path;
    }

    /**
     * 设置圆角显示模式
     *
     * @param mClipMode 显示模式
     * @author zhanghdo
     * @date 2013-11-28
     * @since 1.0
     */
    public void setmClipMode(int mClipMode) {
        this.mClipMode = mClipMode;
    }

    /**
     * 设置圆角x轴半径
     *
     * @param rx 圆角x轴半径
     * @author zhanghdo
     * @date 2013-11-28
     * @since 1.0
     */
    public void setRx(float rx) {
        this.rx = rx;
    }

    /**
     * 设置圆角y轴半径
     *
     * @param ry 圆角y轴半径
     * @author zhanghdo
     * @date 2013-11-28
     * @since 1.0
     */
    public void setRy(float ry) {
        this.ry = ry;
    }

    /**
     * 设置圆角x、y轴半径
     *
     * @param radius 圆角x 、y轴半径
     * @author zhanghdo
     * @date 2013-11-28
     * @since 1.0
     */
    public void setRadius(float radius) {
        rx = radius;
        ry = radius;
    }
}
