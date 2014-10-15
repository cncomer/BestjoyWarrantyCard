package com.bestjoy.app.warrantycard.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.im.RelationshipActivity;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.update.UpdateService;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.umeng.message.PushAgent;

public class MainActivity20141010 extends BaseActionbarActivity implements View.OnClickListener {
	private static final String TAG = "MainActivity20141010";
	private int mCurrentTab = R.id.button_home;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setContentView(R.layout.activity_main_20141010);
		
		UpdateService.startUpdateServiceOnAppLaunch(mContext);
		YouMengMessageHelper.getInstance().startCheckDeviceTokenAsync();
		//统计应用启动数据
		PushAgent.getInstance(mContext).onAppStart();
		
		initButtons();
		showContent(R.id.button_home);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	public void initButtons() {
		findViewById(R.id.button_im).setOnClickListener(this);
		findViewById(R.id.button_new_card).setOnClickListener(this);
		findViewById(R.id.button_maintenance).setOnClickListener(this);
		findViewById(R.id.button_scan).setOnClickListener(this);
		findViewById(R.id.button_remote).setOnClickListener(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		YouMengMessageHelper.getInstance().cancelCheckDeviceTokenTask();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_home:
			if (mCurrentTab != R.id.button_home) {
				mCurrentTab = R.id.button_home;
				showContent(R.id.button_home);
			}
			break;
		case R.id.button_scan:
			Intent scanIntent = new Intent(mContext, CaptureActivity.class);
			startActivity(scanIntent);
			break;
		case R.id.button_im:
			if (MyAccountManager.getInstance().hasLoginned()) {
				RelationshipActivity.startActivity(mContext);
			} else {
				MyApplication.getInstance().showNeedLoginMessage();
			}
			break;
		}
	}
	
	private void showContent(int id) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch(id) {
		case R.id.button_home:
			this.getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.top1));
			ft.replace(R.id.content_frame, new HomePageFragment(), HomePageFragment.TAG);
			ft.commit();
			break;
		case R.id.button_new_card:
		case R.id.button_maintenance:
		case R.id.button_remote:
			MyApplication.getInstance().showUnsupportMessage();
			break;
		}
		
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}
	
	/**
	 * 回到主界面
	 * @param context
	 */
	public static void startActivityForTop(Context context) {
		Intent intent = new Intent(context, MainActivity20141010.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

}
