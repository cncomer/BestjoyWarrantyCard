package com.bestjoy.app.warrantycard.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.R;

public class CircleProgressView extends TextView{
	private static final String TAG = "CircleProgressView";
	private static final int DEFAULT_MAX_NUMBER = 9999;
	private static final String DEFAULT_MAX_NUMBER_STRING = "9999+";
	private String mNumber = "";
	/**图标视图的宽高*/
	private int mDefaultIconTextWidth, mIconTextWidth;
	
	private int mOutterCircleColor, mInnerCircleColor, mNumberColor;
	private int mInnerCircleWidth, mOutterCircleWidth;
	
	private int mStartDegree = -90;
	private int mEndDegree = 360;

	public CircleProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mNumber = DEFAULT_MAX_NUMBER_STRING;
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
		mInnerCircleColor = typedArray.getColor(R.styleable.CircleProgressView_innerCircleColor, getResources().getColor(R.color.circle_inner_bg)); 
	    mOutterCircleColor = typedArray.getColor(R.styleable.CircleProgressView_outerCircleColor, getResources().getColor(R.color.circle_outer_bg));
	    mNumberColor = typedArray.getColor(R.styleable.CircleProgressView_textColor, getResources().getColor(R.color.circle_number_bg)); 
	    
	    mInnerCircleWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressView_innerCircleWidth, 1);
	    mOutterCircleWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressView_outerCircleWidth, 3);
	    typedArray.recycle();
	}
	
	public CircleProgressView(Context context) {
		super(context);
		mDefaultIconTextWidth = (int) getPaint().measureText("  " + DEFAULT_MAX_NUMBER_STRING);
		mInnerCircleColor = getResources().getColor(R.color.circle_number_bg);
		mOutterCircleColor = mInnerCircleColor;
		mNumberColor = mInnerCircleColor;
		mInnerCircleWidth = (int) (1 * getResources().getDisplayMetrics().density);
		mOutterCircleWidth = (int) (3 * getResources().getDisplayMetrics().density);
	}
	
	public void setEndDegree(int degree) {
		mEndDegree = degree;
		invalidate();
	}
	
	public void setStartDegree(int degree) {
		mStartDegree = degree;
		invalidate();
	}
	/**
	 * 
	 * @param startDegree
	 * @param endDegree
	 * @param anim 是否动画显示
	 */
	public void setOutterDegree(int startDegree, int endDegree, boolean anim) {
		mStartDegree = startDegree;
		mEndDegree = endDegree;
		//TODO 实现动画效果
		invalidate();
	}
    
    private void drawInnerCircle(Canvas canvas) {
		Paint paint = new Paint(getPaint());
		paint.setAntiAlias(true);
		paint.setColor(mInnerCircleColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(mInnerCircleWidth);
		int cx = mIconTextWidth / 2;
		canvas.drawCircle(cx, cx, cx - (mOutterCircleWidth + mInnerCircleWidth), paint);
	}
    
    private void drawInnerArc(Canvas canvas) {
		Paint paint = new Paint(getPaint());
		paint.setAntiAlias(true);
		paint.setColor(mInnerCircleColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(mInnerCircleWidth);
		int width = mIconTextWidth- mOutterCircleWidth-mInnerCircleWidth;
		RectF oval = new RectF(mOutterCircleWidth+mInnerCircleWidth, mOutterCircleWidth+mInnerCircleWidth, width, width);
		canvas.drawArc(oval, 0, 360, false, paint);
	}
    
    private void drawOutterCircle(Canvas canvas) {
		Paint paint = new Paint(getPaint());
		paint.setAntiAlias(true);
		paint.setColor(mOutterCircleColor);
		paint.setStyle(Paint.Style.STROKE);
		int cx = mIconTextWidth / 2;
		paint.setStrokeWidth(mOutterCircleWidth);
		canvas.drawCircle(cx, cx, cx - mOutterCircleWidth, paint);
	}
    
    /**
     * 绘制外圈,
     * <p/>圆环完整度=(今天-购买日期)/（保修年+延保年）X365 如果为负值，则圆环是满圆
     * @param canvas
     */
    private void drawOutterArc(Canvas canvas) {
		Paint paint = new Paint(getPaint());
		paint.setAntiAlias(true);
		paint.setColor(mOutterCircleColor);
		paint.setStyle(Paint.Style.STROKE);
		int cx = mIconTextWidth / 2;
		paint.setStrokeWidth(mOutterCircleWidth);
		int width = mIconTextWidth - mOutterCircleWidth;
		RectF oval = new RectF(mOutterCircleWidth, mOutterCircleWidth, width, width);
		canvas.drawArc(oval, mStartDegree, mEndDegree, false, paint);
	}
	
    private void drawNumber(Canvas canvas) {
    	TextPaint textPaint = new TextPaint(this.getPaint());
    	textPaint.setColor(mNumberColor);
    	textPaint.setTextSize(getTextSize());
    	textPaint.setAntiAlias(true);
    	textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	textPaint.setTextAlign(Align.CENTER); 
		textPaint.measureText(mNumber);
		
		FontMetrics fontMetrics = textPaint.getFontMetrics(); 
		// 计算文字高度 
		float fontHeight = fontMetrics.bottom - fontMetrics.top; 
		// 计算文字baseline 
		float textBaseY = mIconTextWidth - (mIconTextWidth - fontHeight) / 2 - fontMetrics.bottom; 
    	canvas.drawText(mNumber, mIconTextWidth/2, textBaseY, textPaint);
	}
    
    public void setNumber(int number) {
    	if (number > DEFAULT_MAX_NUMBER) {
    		mNumber = DEFAULT_MAX_NUMBER_STRING;
    	} else {
    		mNumber = String.valueOf(number);
    	}
    	requestLayout();
    }
    
    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}
	
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
 
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			TextPaint textPaint = getPaint();
			float iconRealTextWidth = textPaint.measureText("  " + mNumber);
			mIconTextWidth = Math.max((int)(mDefaultIconTextWidth), (int)iconRealTextWidth) + this.getPaddingLeft() + this.getPaddingRight() + 2 * (mInnerCircleWidth + mOutterCircleWidth);
			result = mIconTextWidth;
		}
		return result;
	}
	
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
 
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the text
			TextPaint textPaint = getPaint();
			float iconRealTextWidth = textPaint.measureText("  " + mNumber);
			mIconTextWidth = Math.max((int)(mDefaultIconTextWidth), (int)iconRealTextWidth) + this.getPaddingTop() + this.getPaddingBottom() + 2 * (mInnerCircleWidth + mOutterCircleWidth);
			result = mIconTextWidth;
		}
		return result;
	}
	
    @Override
	public void onDraw(Canvas canvas) {
    	drawInnerArc(canvas);
    	drawOutterArc(canvas);
		drawNumber(canvas);
	}
}
