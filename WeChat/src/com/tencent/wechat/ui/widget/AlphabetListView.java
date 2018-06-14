package com.tencent.wechat.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 类名: AlphabetListView <br/>
 * 描述: 带字母索引的ListView<br/>
 * 日期: 2014-4-9 下午2:12:24 <br/>
 * <br/>
 *
 * @author zhanghdo
 * @version 产品版本信息 2014-4-9 zhanghdo 修改信息<br/>
 * @see
 * @since 1.0
 */
@SuppressLint("NewApi")
public class AlphabetListView extends FrameLayout implements OnTouchListener {

    private static final int INDICATOR_DURATION = 1000;

    /**
     * 默认索引条字体颜色
     */
    private static final String ALPHABET_TEXT_COLOR = "#5C697F";

    /**
     * 默认索引条字体大小
     */
    private static final int ALPHABET_TEXT_SIZE = 18;

    /**
     * 默认索引条宽度
     */
    private static final int ALPHABET_TEXTVIEW_WIDTH = 40;

    /**
     * 默认索引条区域top值
     */
    private static final int ALPHABET_LAYOUT_TOP = 20;

    /**
     * 默认提示字体大小
     */
    private static final int ALPHABET_TIP_TEXT_SIZE = 50;

    /**
     * 提示textview的尺寸
     */
    private static final int ALPHABET_TIP_TEXTVIEW_SIZE = 70;

    /**
     * 默认提示框Alpha值
     */
    private static final float ALPHABET_TIP_TEXTVIEW_ALPHA = 0.7f;

    private Context mContext;

    private ListView mListView;

    private LinearLayout mAlphabetLayout;

    private TextView mTextView;

    private AlphabetPositionListener mPositionListener;

    private Handler mHandler;

    private HideIndicator mHideIndicator = new HideIndicator();

    private int mIndicatorDuration = INDICATOR_DURATION;

    private int mAlphabetListSize;

    private List<String> mAlphabetList;

    /**
     * 带字母索引的ListView构造函数<br/>
     *
     * @param context Context
     */
    public AlphabetListView(Context context) {
        this(context, null);
    }

    /**
     * 带字母索引的ListView构造函数<br/>
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public AlphabetListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAlphabetList = new ArrayList<String>();
        mContext = context;
        mHandler = new Handler();
        mListView = new ListView(mContext);
        mListView.setDivider(null);
        mListView.setVerticalScrollBarEnabled(false);
        mTextView = new TextView(mContext);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ALPHABET_TIP_TEXT_SIZE);
        mTextView.setTextColor(Color.WHITE);
        mTextView.setBackgroundColor(Color.BLACK);
        mTextView.setAlpha(ALPHABET_TIP_TEXTVIEW_ALPHA);
        mTextView.setMinWidth(ALPHABET_TIP_TEXTVIEW_SIZE);
        mTextView.setMinHeight(ALPHABET_TIP_TEXTVIEW_SIZE);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setVisibility(View.INVISIBLE);
        LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textLayoutParams.gravity = Gravity.CENTER;
        mTextView.setLayoutParams(textLayoutParams);
        this.addView(mListView);
        this.addView(mTextView);
    }

    /**
     * 方法描述:设置提示信息显示时间
     *
     * @param duration 提示信息显示时间
     * @author zhanghdo
     * @date 2014-4-9
     * @since 1.0
     */
    public void setIndicatorDuration(int duration) {
        this.mIndicatorDuration = duration;
    }

    /**
     * 设置索引条字符list
     *
     * @param alphabetList alphabetList
     * @author ZHDong
     * @date 2014-5-12
     * @since 1.0
     */
    public void setAlphabet(List<String> alphabetList) {
        mAlphabetList.clear();
        mAlphabetList.addAll(alphabetList);
        mAlphabetListSize = mAlphabetList.size();
        initAlphabetLayout(mContext);
    }

    /**
     * 类名: HideIndicator <br/>
     * 描述: HideIndicator <br/>
     * 日期: 2014-5-7 下午4:35:43 <br/>
     * <br/>
     *
     * @author yaoy
     * @version 产品版本信息 yyyy-mm-dd yy 修改信息<br/>
     * @see
     * @since 1.0
     */
    private final class HideIndicator implements Runnable {

        @Override
        public void run() {
            mTextView.setVisibility(View.INVISIBLE);
        }
    }


    /**
     * 方法描述:设置ListView的Adapter
     *
     * @param adapter          机场选择ListView的adapter
     * @param positionListener 获得索引位置接口
     * @author zhanghdo
     * @date 2014-4-9
     * @since 1.0
     */
    public void setAdapter(BaseAdapter adapter, AlphabetPositionListener positionListener) {
        if (positionListener == null) {
            throw new IllegalArgumentException("AlphabetPositionListener is required");
        }
        mListView.setAdapter(adapter);
        this.mPositionListener = positionListener;

    }

    /**
     * 方法描述:创建字母布局
     *
     * @param context
     * @author zhanghdo
     * @date 2014-4-9
     * @since 1.0
     */
    private void initAlphabetLayout(Context context) {
        this.removeView(mAlphabetLayout);
        mAlphabetLayout = new LinearLayout(context);
        mAlphabetLayout.setTop(ALPHABET_LAYOUT_TOP);
        mAlphabetLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams alphabetLayoutParams = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.FILL_PARENT);
        alphabetLayoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        mAlphabetLayout.setLayoutParams(alphabetLayoutParams);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        for (int i = 0; i < mAlphabetListSize; i++) {
            TextView textView = new TextView(context);
            textView.setTextColor(Color.parseColor(ALPHABET_TEXT_COLOR));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ALPHABET_TEXT_SIZE);
            textView.setText(mAlphabetList.get(i));
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(params);
            textView.setTag(i);
            textView.setWidth(ALPHABET_TEXTVIEW_WIDTH);
            mAlphabetLayout.addView(textView);
        }
        mAlphabetLayout.setOnTouchListener(this);
        this.addView(mAlphabetLayout);
    }

    /**
     * 方法描述:获取控件中的ListView
     *
     * @return {@link ListView}
     * @author zhanghdo
     * @date 2014-4-9
     * @since 1.0
     */
    public ListView getListView() {
        return mListView;
    }

    /**
     * 类名: AlphabetPositionListener <br/>
     * 描述: 获取索引位置接口<br/>
     * 日期: 2014-4-9 下午3:46:49 <br/>
     * <br/>
     *
     * @author zhanghdo
     * @version 产品版本信息 2014-4-9 zhanghdo 修改信息<br/>
     * @see
     * @since 1.0
     */
    public interface AlphabetPositionListener {

        int UNKNOW = -1;

        /**
         * 方法描述:根据字符获取索引位置
         *
         * @param letter 点击的字符
         * @return {@link int}
         * @author zhanghdo
         * @date 2014-4-9
         * @since 1.0
         */
        int getPosition(String letter);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int l = (int) (event.getY() / ((float) mAlphabetLayout.getHeight() / (float) mAlphabetListSize));
                if (l > mAlphabetListSize - 1) {
                    l = mAlphabetListSize - 1;
                } else if (l < 0) {
                    l = 0;
                }
                int pos = mPositionListener.getPosition(mAlphabetList.get(l));
                if (pos != -1) {
                    mTextView.setText(mAlphabetList.get(l));
                    mTextView.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mHideIndicator);
                    mHandler.postDelayed(mHideIndicator, mIndicatorDuration);
                    mListView.setSelection(pos);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                l = (int) ((event.getY() + mAlphabetLayout.getHeight() / (float) mAlphabetListSize / (float) 2) / (
                        (float) mAlphabetLayout
                        .getHeight() / (float) mAlphabetListSize));
                if (l > mAlphabetListSize - 1) {
                    l = mAlphabetListSize - 1;
                } else if (l < 0) {
                    l = 0;
                }
                pos = mPositionListener.getPosition(mAlphabetList.get(l));
                if (pos != -1) {
                    mTextView.setText(mAlphabetList.get(l));
                    mTextView.setVisibility(View.VISIBLE);
                    mHandler.removeCallbacks(mHideIndicator);
                    mHandler.postDelayed(mHideIndicator, mIndicatorDuration);
                    mListView.setSelection(pos);
                }
                break;
            case MotionEvent.ACTION_UP:
                mAlphabetLayout.setBackgroundResource(0);
                break;
            default:
                break;
        }
        return true;
    }
}
