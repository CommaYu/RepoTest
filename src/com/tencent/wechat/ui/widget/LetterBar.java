package com.tencent.wechat.ui.widget;

/**
 * Created by Administrator on 2016/7/5.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class LetterBar extends View {
	private int mTextColor = Color.WHITE;
	private float mTextSize = 20.0f;

	private float mItemWidth;
	private int mPaddingLeft;
	private int mPaddingTop;
	private int mPaddingRight;
	private int mPaddingBottom;
	private int mContentWidth;
	private int mContentHeight;
	private float mCenter;
	// 索引列表
	private List<String> mSections = new ArrayList<String>();

	// 数量
	private static int mSectionSize;

	private OnLetterSelectListener mOnLetterSelectListener;

	private TextPaint mTextPaint;

	public LetterBar(Context context) {
		this(context, null);
	}

	public LetterBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LetterBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			onSelect(x);
			break;
		case MotionEvent.ACTION_MOVE:
			onSelect(x);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaddingLeft = getPaddingLeft();
		mPaddingTop = getPaddingTop();
		mPaddingRight = getPaddingRight();
		mPaddingBottom = getPaddingBottom();

		mContentWidth = getWidth() - mPaddingLeft - mPaddingRight;
		mContentHeight = getHeight() - mPaddingTop - mPaddingBottom;
		mCenter = mContentHeight * 2.0f / 3.0f;
		mSectionSize = mSections.size();
		mItemWidth = mContentWidth / mSectionSize;
		for (int i = 0; i < mSectionSize; ++i) {
			canvas.drawText(mSections.get(i), mPaddingLeft + mItemWidth
					* (i + 1), mCenter, mTextPaint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void init() {
		initTextPaint();
		mSections.add("群");
		char character = 'A';
		for (int i = 0; i < 27 - 1; i++) {
			mSections.add(String.valueOf((char) (character + i)));
		}
		mSections.add("#");
	}

	private void initTextPaint() {
		mTextPaint = new TextPaint();
		mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTextSize(mTextSize);
		mTextPaint.setColor(mTextColor);
	}

	public void setOnLetterSelectListener(
			OnLetterSelectListener onLetterSelectListener) {
		mOnLetterSelectListener = onLetterSelectListener;
	}

	private void onSelect(float x) {
		int index = (int) ((x - mPaddingLeft) / mContentWidth * mSectionSize);
		if (index < 0) {
			index = 0;
		} else if (index >= mSectionSize) {
			index = mSectionSize - 1;
		}
		Log.d("索引条", "第" + index + "个: " + mSections.get(index));

		if (mOnLetterSelectListener != null) {

			mOnLetterSelectListener.onLetterSelect(mSections.get(index));
		}
	}

	public interface OnLetterSelectListener {
		void onLetterSelect(String letter);
	}
}