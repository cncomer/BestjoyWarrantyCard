package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.propertymanagement.ChooseCommunityActivity;
import com.bestjoy.app.bjwarrantycard.propertymanagement.PropertyManagementActivity;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.HaierProCityDisEditPopView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;
import com.shwy.bestjoy.utils.UrlEncodeStringBuilder;

public class NewHomeActivity extends BaseActionbarActivity implements View.OnClickListener{
	private static final String TAG = "NewHomeActivity";
	private HaierProCityDisEditPopView mProCityDisEditPopView;
	private EditText mHomeEditText;
	private HomeObject mHomeObject ;
	private Bundle mBundles;
	private Button mEnterCommunityBtn, mResetCommunityBtn;
	@Override
	protected boolean checkIntent(Intent intent) {
		mBundles = intent.getExtras();
		if (mBundles == null) {
			DebugUtils.logD(TAG, "checkIntent failed, due to mBundles is null");
		} else {
			DebugUtils.logD(TAG, "checkIntent true, find mBundles=" + mBundles);
		}
		mHomeObject = HomeObject.getHomeObject(mBundles);
		DebugUtils.logD(TAG, "checkIntent mHomeObject=" + mHomeObject);
		return mHomeObject != null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_home);
		if (this.isFinishing()) {
			return;
		}
		
		mProCityDisEditPopView = new HaierProCityDisEditPopView(this);
		mHomeEditText = (EditText) findViewById(R.id.my_home);
		
		mEnterCommunityBtn = (Button) findViewById(R.id.button_enter);
		mEnterCommunityBtn.setOnClickListener(this);
		if (mHomeObject.mHomeAid > 0) {
			setTitle(R.string.activity_title_update_home);
			mEnterCommunityBtn.setVisibility(View.VISIBLE);
		} else {
			mEnterCommunityBtn.setVisibility(View.GONE);
		}
		
		mResetCommunityBtn = (Button) findViewById(R.id.button_change);
		mResetCommunityBtn.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mHomeObject = HomeObject.getHomeObject(mBundles);
		if (mHomeObject == null) {
			DebugUtils.logE(TAG, "onResume() HomeObject.getHomeObject(mBundles) return null, mBundles=" + mBundles);
			finish();
			return;
		}
		mProCityDisEditPopView.setHomeObject(mHomeObject);
		mHomeEditText.setText(mHomeObject.getHomeTag(this));
		if (mHomeObject.hasCommunity()) {
			mEnterCommunityBtn.setText(getString(R.string.format_button_enter_community, mHomeObject.mHname));
			mResetCommunityBtn.setVisibility(View.VISIBLE);
		} else {
			mEnterCommunityBtn.setText(R.string.button_relate_community);
			mResetCommunityBtn.setVisibility(View.GONE);
		}
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ModleSettings.createActionBarMenu(menu, null);
		MenuItem homeItem = menu.add(R.string.menu_save, R.string.menu_save, 0, mHomeObject.mHomeAid > 0?R.string.button_update:R.string.menu_save);
		homeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.string.menu_save:
			if(valiInput()) {
				createOrUpdateHomeAsync(mHomeObject.mHomeAid <= 0);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean valiInput() {
		HomeObject mHomeObject = mProCityDisEditPopView.getHomeObject();
		if(TextUtils.isEmpty(mHomeObject.mHomeProvince)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_pro);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomeCity)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_city);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomeDis)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_dis);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomePlaceDetail)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_place_detail);
			return false;
		}
		return true;
	}

	public static void startActivity(Context context, Bundle bundle) {
		Intent intent = new Intent(context, NewHomeActivity.class);
		if (bundle != null) intent.putExtras(bundle);
		context.startActivity(intent);
	}


	CreateOrUpdateHomeAsyncTask mCreateOrUpdateHomeAsyncTask;
	private void createOrUpdateHomeAsync(boolean create) {
		AsyncTaskUtils.cancelTask(mCreateOrUpdateHomeAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mCreateOrUpdateHomeAsyncTask = new CreateOrUpdateHomeAsyncTask();
		mCreateOrUpdateHomeAsyncTask.execute(create);
		
	}
	
	private class CreateOrUpdateHomeAsyncTask extends AsyncTask<Boolean, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(Boolean... create) {
			HomeObject mHomeObject = mProCityDisEditPopView.getHomeObject();
			mHomeObject.mHomeName = mHomeEditText.getText().toString().trim();
			if (create != null && create[0]) {
				return doCreateHome(mHomeObject);
			} else {
				return doUpdateHome(mHomeObject);
			}
			
		}
		
		private ServiceResultObject doCreateHome(HomeObject homeObject) {
			InputStream is = null;
			ServiceResultObject haierResultObject = new ServiceResultObject();
			UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.getCreateHomeUrl());
			sb.append("ShenFen=").appendUrlEncodedString(mHomeObject.mHomeProvince)
			.append("&City=").appendUrlEncodedString(mHomeObject.mHomeCity)
			.append("&QuXian=").appendUrlEncodedString(mHomeObject.mHomeDis)
			.append("&DetailAddr=").appendUrlEncodedString(mHomeObject.mHomePlaceDetail)
			.append("&UID=").appendUrlEncodedString(String.valueOf(MyAccountManager.getInstance().getAccountObject().mAccountUid))
			.append("&Tag=").appendUrlEncodedString(mHomeObject.mHomeName)
			.append("&token=").appendUrlEncodedString(SecurityUtils.MD5.md5(MyAccountManager.getInstance().getAccountObject().mAccountTel + MyAccountManager.getInstance().getAccountObject().mAccountPwd));   //用户md5（cell+pwd）
			
			try {
				is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					String content = NetworkUtils.getContentFromInput(is);
					haierResultObject = ServiceResultObject.parse(content);
					if (haierResultObject.isOpSuccessfully()) {
						//更新服务器上的数据成功，我们需要更新本地的
						if (mHomeObject.mHomeAid == -1) {
							//是新建
							mHomeObject.mHomeUid = MyAccountManager.getInstance().getAccountObject().mAccountUid;
							String data = haierResultObject.mStrData;
							DebugUtils.logD(TAG, "doCreateHome return data " + data);
							if (!TextUtils.isEmpty(data)) {
								int index = data.indexOf(":");
								if (index > 0) {
									data = data.substring(index+1);
									DebugUtils.logD(TAG, "doCreateHome find aid " + data);
									mHomeObject.mHomeAid = Long.valueOf(data);
								}
							}
						} 
						boolean saved = mHomeObject.saveInDatebase(getContentResolver(), null);
						if (!saved) {
							MyApplication.getInstance().showMessageAsync(R.string.msg_local_save_op_failed);
						} else {
							//新建家后，我们删除演示家
							HomeObject.deleteDemoHomeObject(getContentResolver(),  MyAccountManager.getInstance().getAccountObject().mAccountUid, HomeObject.DEMO_HOME_AID);
						}
						//刷新本地家
						MyAccountManager.getInstance().initAccountHomes();
						MyAccountManager.getInstance().updateHomeObject(mHomeObject.mHomeUid);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return haierResultObject;
		}
		
		private ServiceResultObject doUpdateHome(HomeObject homeObject) {
			InputStream is = null;
			ServiceResultObject haierResultObject = new ServiceResultObject();
			final int LENGTH = 7;
			String[] urls = new String[LENGTH];
			String[] paths = new String[LENGTH];
			urls[0] = ServiceObject.SERVICE_URL + "UpdateAddrByID.ashx?AID=";
			paths[0] = String.valueOf(mHomeObject.mHomeAid);
			urls[1] = "&ShenFen=";
			paths[1] = mHomeObject.mHomeProvince;
			urls[2] = "&City=";
			paths[2] = mHomeObject.mHomeCity;
			urls[3] = "&QuXian=";
			paths[3] = mHomeObject.mHomeDis;
			urls[4] = "&DetailAddr=";
			paths[4] = mHomeObject.mHomePlaceDetail;
			urls[5] = "&UID=";
			paths[5] = String.valueOf(MyAccountManager.getInstance().getAccountObject().mAccountUid);
			urls[6] = "&Tag=";
			paths[6] = mHomeObject.mHomeName;
			DebugUtils.logD(TAG, "urls = " + Arrays.toString(urls));
			DebugUtils.logD(TAG, "paths = " + Arrays.toString(paths));
			try {
				is = NetworkUtils.openContectionLocked(urls, paths, MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					String content = NetworkUtils.getContentFromInput(is);
					haierResultObject = ServiceResultObject.parse(content);
					if (haierResultObject.isOpSuccessfully()) {
						//更新服务器上的数据成功，我们需要更新本地的
						boolean saved = mHomeObject.saveInDatebase(getContentResolver(), null);
						if (!saved) {
							MyApplication.getInstance().showMessageAsync(R.string.msg_local_save_op_failed);
						}
						//刷新本地家
						MyAccountManager.getInstance().initAccountHomes();
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return haierResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessageAsync(result.mStatusMessage);
			if(result.isOpSuccessfully()) {
				mProCityDisEditPopView.clear();
				NewHomeActivity.this.finish();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_enter:
			if (mHomeObject.hasCommunity()) {
				//如果已经关联过小区了，我们直接进到小区
				PropertyManagementActivity.startActivity(mContext, mBundles);
			} else {
				ChooseCommunityActivity.startActivity(mContext, mBundles);
			}
			break;
		case R.id.button_change:
			ChooseCommunityActivity.startActivity(mContext, mBundles);
			break;
		}
		
	}
}
