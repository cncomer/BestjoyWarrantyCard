package com.bestjoy.app.haierwarrantycard.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bestjoy.app.haierwarrantycard.R;
import com.bestjoy.app.haierwarrantycard.ui.model.ModleSettings;
import com.bestjoy.app.haierwarrantycard.utils.BitmapUtils;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
	private LinearLayout mDotsLayout;
	private ViewPager mAdsViewPager;
	private boolean mAdsViewPagerIsScrolling = false;
	
	private int[] mAddsDrawableId = new int[]{
			R.drawable.ad1,
			R.drawable.ad2,
			R.drawable.ad3,
	};
	
	private Drawable[] mDotDrawableArray;
	private Bitmap[] mAdsBitmaps;
	
	private ImageView[] mDotsViews = null;
	private ImageView[] mAdsPagerViews = null;
	private int mCurrentPagerIndex = 0;
	private static final int DEFAULT_MAX_ADS_SIZE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDotsLayout = (LinearLayout) findViewById(R.id.dots);
		mAdsViewPager = (ViewPager) findViewById(R.id.adsViewPager);
		this.initViewPagers(3);
		this.initDots(3);
		mAdsViewPager.setAdapter(new AdsViewPagerAdapter());
		
		mAdsViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// TODO ���Ӷ���Ч��
				if (mCurrentPagerIndex != position) {
					mDotsViews[mCurrentPagerIndex].setImageDrawable(mDotDrawableArray[0]);
					mDotsViews[position].setImageDrawable(mDotDrawableArray[1]);
					mCurrentPagerIndex = position;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				mAdsViewPagerIsScrolling  = state == 1;//��ʾ�ڻ���
				
			}
		});
		
		ModleSettings.addModelsAdapter(this, (ListView) findViewById(R.id.listview));
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private void initDots(int count){
		LayoutInflater flater = this.getLayoutInflater();
		if (mDotDrawableArray == null) {
			mDotDrawableArray = new Drawable[2];
			mDotDrawableArray[0] = this.getResources().getDrawable(R.drawable.dot);
			mDotDrawableArray[1] = this.getResources().getDrawable(R.drawable.dot_on);
		}
		mDotsViews = new ImageView[count];
		for (int j = 0; j < count; j++) {
			mDotsViews[j] = (ImageView) flater.inflate(R.layout.dot, mDotsLayout, false);
			if (mCurrentPagerIndex == j) {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[1]);
			} else {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[0]);
			}
			mDotsLayout.addView(mDotsViews[j]);
		}
	}
	
	private void initAdsBitmap() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (mAdsBitmaps == null) {
			mAdsBitmaps = new Bitmap[DEFAULT_MAX_ADS_SIZE];
		} else {
			for(Bitmap bitmap:mAdsBitmaps) {
				bitmap.recycle();
			}
		}
		int adsW = getResources().getDimensionPixelSize(R.dimen.ads_width);
		int adsH = getResources().getDimensionPixelSize(R.dimen.ads_height);
		mAdsBitmaps = BitmapUtils.getSuitedBitmaps(this, mAddsDrawableId, adsW, adsH);
	}
	
	private void initViewPagers(int count) {
		initAdsBitmap();
		mAdsPagerViews = new ImageView[count];
		LayoutInflater flater = getLayoutInflater();
		for (int j = 0; j < count; j++) {
			mAdsPagerViews[j] = (ImageView) flater.inflate(R.layout.ads, null, false);
			mAdsPagerViews[j].setImageBitmap(mAdsBitmaps[j]);
		}
	}
	
	private void initAdsPager(){
	}
	
	class AdsViewPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return mAdsPagerViews.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		private View getView(ViewGroup container, int position) {
			container.addView(mAdsPagerViews[position]);
			return mAdsPagerViews[position];
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return getView(container, position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mAdsPagerViews[position]);
		}
		
	}
	
	@Override
	public void onClick(View v) {
		
	}

}
