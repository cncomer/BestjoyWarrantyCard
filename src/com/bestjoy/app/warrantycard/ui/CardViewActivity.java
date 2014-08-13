package com.bestjoy.app.warrantycard.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.text.TextUtils;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;

public class CardViewActivity extends BaseActionbarActivity implements View.OnClickListener{

	private static final String TAG = "CardViewActivity";
	private CardViewFragment mContent;
	public Bundle mBundle;
	
	private TextView mMalfunctionBtn, mMaintenancePointBtn, mBuyMaintenanceComponentBtn;
	private BaoxiuCardObject mBaoxiuCardObject;
	private HomeObject mHomeObject;
	
	//
	private View mBottomContentLayout, mBottomContentTop, mContentLayout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing()) {
			return;
		}
		if (savedInstanceState != null) {
			mBundle = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle);
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// set the Above View
		setContentView(R.layout.card_content_frame);
		mMalfunctionBtn = (TextView) findViewById(R.id.button_malfunction);
        mMaintenancePointBtn = (TextView) findViewById(R.id.button_maintenance_point);
        mBuyMaintenanceComponentBtn = (TextView) findViewById(R.id.button_maintenance_componnet);
        mMalfunctionBtn.setOnClickListener(this);
        mMaintenancePointBtn.setOnClickListener(this);
        mBuyMaintenanceComponentBtn.setOnClickListener(this);
		
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
		mHomeObject = HomeObject.getHomeObject(mBundle);
		
		mContent = new CardViewFragment();
		mContent.setArguments(mBundle);

		NewRepairCardFragment newRepairCardFragment = new NewRepairCardFragment();
		newRepairCardFragment.setArguments(mBundle);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.replace(R.id.content_frame_bottom, newRepairCardFragment)
		.commit();
		
		mContentLayout = findViewById(R.id.content_frame);
		
		mBottomContentLayout = findViewById(R.id.content_frame_bottom);
		mBottomContentTop = findViewById(R.id.contet_bottom_frame_top);
		mBottomContentTop.setOnClickListener(this);
	}
	
	  @Override
      public boolean onCreateOptionsMenu(Menu menu) {
		  return false;
	  }

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundle = getIntent().getExtras();
	    return mBundle != null;
	}
	
	private void showBottomContent(boolean anim) {
		mContentLayout.setVisibility(View.GONE);
		mBottomContentLayout.setVisibility(View.VISIBLE);
		mBottomContentTop.setVisibility(View.VISIBLE);
	}
	
	private void showContent(boolean anim) {
		mContentLayout.setVisibility(View.VISIBLE);
		mBottomContentLayout.setVisibility(View.GONE);
		mBottomContentTop.setVisibility(View.GONE);
	}
	
	@Override
	public void onBackPressed() {
		if (mBottomContentLayout.getVisibility() == View.GONE) {
			super.onBackPressed();
		} else {
			showContent(true);
		}
	}
	
	
	/**
	 * 回到主界面
	 * @param context
	 */
	public static void startActivit(Context context, Bundle bundle) {
		Intent intent = new Intent(context, CardViewActivity.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}

	@Override
    public void onClick(View v) {
	    switch(v.getId()){
	    case R.id.contet_bottom_frame_top:
	    	showContent(true);
	    	break;
	    case R.id.button_malfunction:
			if (ServiceObject.isHaierPinpaiGenaral(mBaoxiuCardObject.mPinPai)) {
				showNewRepairCardFragment();
	    	} else {
	    		new AlertDialog.Builder(this)
		    	.setMessage(R.string.must_haier_confirm_yuyue)
		    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(mBaoxiuCardObject.mBXPhone)) {
							Intents.callPhone(mContext, mBaoxiuCardObject.mBXPhone);
						} else {
							MyApplication.getInstance().showMessage(R.string.msg_no_bxphone);
						}
						
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	    	}
			break;
		case R.id.button_maintenance_point:
			showMaintenancePointFragment();
			break;
		case R.id.button_maintenance_componnet:
			if (true) {
				MyApplication.getInstance().showUnsupportMessage();
				return;
			}
			break;
	    }
	    
    }

	private void showNewRepairCardFragment() {
		NewRepairCardFragment newRepairCardFragment = new NewRepairCardFragment();
		newRepairCardFragment.setArguments(mBundle);
		getSupportFragmentManager()
		.beginTransaction()
//		.replace(R.id.content_frame, mContent)
		.replace(R.id.content_frame_bottom, newRepairCardFragment)
		.commit();
		

		showBottomContent(true);
	}
	
	private void showMaintenancePointFragment() {
		NearestMaintenancePointFragment mNearestMaintenancePointFragment = new NearestMaintenancePointFragment();
		mNearestMaintenancePointFragment.setArguments(mBundle);
		getSupportFragmentManager()
		.beginTransaction()
//		.replace(R.id.content_frame, mContent)
		.replace(R.id.content_frame_bottom, mNearestMaintenancePointFragment)
		.commit();

		showBottomContent(true);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(TAG, mBundle);
		DebugUtils.logW(TAG, "onSaveInstanceState(), we try to save mBundles=" + mBundle);
	}

}
