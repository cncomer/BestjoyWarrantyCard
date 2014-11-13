package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.IBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class CardViewActivity extends BaseActionbarActivity implements View.OnClickListener{

	private static final String TAG = "CardViewActivity";
	private ModleBaseFragment mContent;
	public Bundle mBundle;
	
	private TextView mMalfunctionBtn, mMaintenancePointBtn;
	private IBaoxiuCardObject mBaoxiuCardObject;
	
	//
	private View mBottomContentLayout, mBottomContentTop, mContentLayout;
	
	private int mBundleType = -1;
	
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
        mMalfunctionBtn.setOnClickListener(this);
        mMaintenancePointBtn.setOnClickListener(this);
		
		
        View price_guide = findViewById(R.id.button_baoxiucard_price_guide);
		price_guide.setOnClickListener(this);
		View zhengduan = findViewById(R.id.button_zhengduan);
		zhengduan.setOnClickListener(this);
		mBundleType = mBundle.getInt(Intents.EXTRA_TYPE);
		switch(mBundleType) {
		case R.id.model_my_card:
			mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
			mContent = new CardViewFragment();
			price_guide.setVisibility(View.VISIBLE);
			zhengduan.setVisibility(View.GONE);
			break;
		case R.id.model_my_car_card:
			mBaoxiuCardObject = CarBaoxiuCardObject.getBaoxiuCardObject(mBundle);
			mContent = new CarCardViewFragment();
			price_guide.setVisibility(View.GONE);
			zhengduan.setVisibility(View.VISIBLE);
			break;
		}
		
		mContent.setArguments(mBundle);

//		NewRepairCardFragment newRepairCardFragment = new NewRepairCardFragment();
//		newRepairCardFragment.setArguments(mBundle);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
//		.replace(R.id.content_frame_bottom, newRepairCardFragment)
		.commit();
		
		mContentLayout = findViewById(R.id.content_frame);
		
		mBottomContentLayout = findViewById(R.id.content_frame_bottom);
		mBottomContentTop = findViewById(R.id.contet_bottom_frame_top);
		mBottomContentTop.setOnClickListener(this);
		
		
		findViewById(R.id.button_im).setOnClickListener(this);
		findViewById(R.id.button_baoxiu_policy).setOnClickListener(this);
		
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
		updateActionBarOptionMenu();
	}
	
	private void showContent(boolean anim) {
		mContentLayout.setVisibility(View.VISIBLE);
		mBottomContentLayout.setVisibility(View.GONE);
		mBottomContentTop.setVisibility(View.GONE);
		updateActionBarOptionMenu();
	}
	
	private void updateActionBarOptionMenu() {
		mContent.setShowOptionMenu(mBottomContentTop.getVisibility() != View.VISIBLE);
		invalidateOptionsMenu();
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
	    	switch(mBundleType) {
			case R.id.model_my_card:
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
			case R.id.model_my_car_card:
				break;
			}
			break;
	    case R.id.button_zhengduan:
			switch(mBundleType) {
			case R.id.model_my_card:
				break;
			case R.id.model_my_car_card:
				BrowserActivity.startActivity(mContext, "file:///android_asset/car_html/selfcheck_car.html",  mContext.getString(R.string.button_zhenduan));
				break;
			}
	    	break;
		case R.id.button_maintenance_point:
			showMaintenancePointFragment();
			break;
		case R.id.button_im:
			ViewConversationListActivity.startActivity(mContext, mBundle);
			break;
		case R.id.button_baoxiucard_price_guide:
			if (TextUtils.isEmpty(mBaoxiuCardObject.mKY)) {
				DebugUtils.logD(TAG, "no find ky when click policy");
			} else {
//				StringBuilder sb = new StringBuilder("http://www.dzbxk.com/policy/");
//				sb.append(mBaoxiuCardObject.mKY.substring(0, 5)).append(".html");
////				DebugUtils.logD(TAG, "open url " + sb.toString());
				showDialog(DIALOG_PROGRESS);
				new Thread(new Runnable() {
					@Override
					public void run() {
						ServiceResultObject serviceResultObject = new ServiceResultObject();
						InputStream is = null;
						try {
							is = NetworkUtils.openContectionLocked(ServiceObject.checkPolicyPageUrl(mBaoxiuCardObject.mKY.substring(0, 3)), MyApplication.getInstance().getSecurityKeyValuesObject());
							serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
							if (serviceResultObject.isOpSuccessfully()) {
								
							}
						} catch (ClientProtocolException e) {
							e.printStackTrace();
							serviceResultObject.mStatusMessage = e.getMessage();
						} catch (IOException e) {
							e.printStackTrace();
							serviceResultObject.mStatusMessage = e.getMessage();
						} finally {
							NetworkUtils.closeInputStream(is);
						}
						if (serviceResultObject.isOpSuccessfully()) {
							DebugUtils.logD(TAG, "open url " + serviceResultObject.mStrData);
							Intents.openURL(mContext, serviceResultObject.mStrData);
						} else {
							MyApplication.getInstance().showMessageAsync(serviceResultObject.mStatusMessage);
						}
						MyApplication.getInstance().postAsync(new Runnable() {

							@Override
							public void run() {
								dismissDialog(DIALOG_PROGRESS);
							}
							
						});
						
					}
					
				}).start();
//				Intents.openURL(mContext, sb.toString());
			}
			break;
		case R.id.button_baoxiu_policy:
			if (TextUtils.isEmpty(mBaoxiuCardObject.mKY)) {
				DebugUtils.logD(TAG, "no find ky when click policy");
			} else {
//				StringBuilder sb = new StringBuilder("http://www.dzbxk.com/policy/");
//				sb.append(mBaoxiuCardObject.mKY.substring(0, 5)).append(".html");
////				DebugUtils.logD(TAG, "open url " + sb.toString());
				showDialog(DIALOG_PROGRESS);
				new Thread(new Runnable() {
					@Override
					public void run() {
						ServiceResultObject serviceResultObject = new ServiceResultObject();
						InputStream is = null;
						try {
							is = NetworkUtils.openContectionLocked(ServiceObject.checkPolicyPageUrl(mBaoxiuCardObject.mKY), MyApplication.getInstance().getSecurityKeyValuesObject());
							serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
							if (serviceResultObject.isOpSuccessfully()) {
								
							}
						} catch (ClientProtocolException e) {
							e.printStackTrace();
							serviceResultObject.mStatusMessage = e.getMessage();
						} catch (IOException e) {
							e.printStackTrace();
							serviceResultObject.mStatusMessage = e.getMessage();
						} finally {
							NetworkUtils.closeInputStream(is);
						}
						if (serviceResultObject.isOpSuccessfully()) {
							DebugUtils.logD(TAG, "open url " + serviceResultObject.mStrData);
							Intents.openURL(mContext, serviceResultObject.mStrData);
						} else {
							MyApplication.getInstance().showMessageAsync(serviceResultObject.mStatusMessage);
						}
						MyApplication.getInstance().postAsync(new Runnable() {

							@Override
							public void run() {
								dismissDialog(DIALOG_PROGRESS);
							}
							
						});
						
					}
					
				}).start();
//				Intents.openURL(mContext, sb.toString());
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
