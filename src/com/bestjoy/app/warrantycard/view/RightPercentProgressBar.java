package com.bestjoy.app.warrantycard.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.shwy.bestjoy.utils.DebugUtils;

public class RightPercentProgressBar extends ProgressBar{
	private static final String TAG = "RightPercentProgressBar";
	private String mProgressUnit = "";
	private String mProgressText = "";
	private TextPaint mTextPaint;

	public RightPercentProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(Color.parseColor("#ffc5d9f0"));
		if (this.isInEditMode()) {
			mTextPaint.setTextSize(13);
		} else {
			mTextPaint.setTextSize(MyApplication.getInstance().mDisplayMetrics.scaledDensity * 13);
		}
		
//		mTextPaint.setTextAlign(Paint.Align.CENTER);
	}
	
	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		DebugUtils.logD(TAG, "onSizeChanged w = " + w + ", h = " + h+ ", oldw = " + oldw + ", oldh = " + oldh);
	}



	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!isIndeterminate()) {
			drawText(canvas);
		}
	}
	
	public void setProgressUnit(String unit) {
		mProgressUnit = unit;
	}
	
	public void setProgressText(String text) {
		mProgressText = text;
	}
	
	
	public void drawText(Canvas canvas) {
		
		int progress = this.getProgress();
		if (TextUtils.isEmpty(mProgressText)) {
			mProgressText = String.valueOf(progress);
		}
		float progressWidth = 0;
		String progressText = String.valueOf(mProgressText) + mProgressUnit;
		float progressTextWidth = mTextPaint.measureText(progressText);
		if (progress >= getMax()) {
			//已经达到最大值了,进度条要填满
			progressWidth = getWidth();
		} else if (progress > 0) {
			progressWidth = 1.0f * getWidth() * (1.0f * progress / getMax()); 
		} else {
			progressWidth = 0;
		}
		DebugUtils.logD(TAG, "drawText progressWidth " + progressWidth);
		DebugUtils.logD(TAG, "drawText progressTextWidth " + progressTextWidth);
		
		FontMetrics fontMetrics = mTextPaint.getFontMetrics(); 
		// 计算文字高度 
		float fontHeight = fontMetrics.bottom - fontMetrics.top; 
		// 计算文字baseline 
		float textBaseY = getHeight() - (getHeight() - fontHeight) / 2 - fontMetrics.bottom; 
    	
		if (progressWidth + progressTextWidth < getWidth()) {
			if (progressWidth < progressTextWidth) {
				canvas.drawText(progressText, 0, textBaseY, mTextPaint);
			} else {
				canvas.drawText(progressText, progressWidth - progressTextWidth - 10, textBaseY, mTextPaint);
			}
			
		} else {
			canvas.drawText(progressText, getWidth() - progressTextWidth - 10, textBaseY, mTextPaint);
		}
	}
	

}
