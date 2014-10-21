package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.propertymanagement.HomesCommunityManager.CommunityServiceObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.WeatherManager;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager.LocationChangeCallback;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.NetworkUtils;

/**
 * 对于家地址，我们需要让用户去匹配对应的小区物业，来使用小区功能
 * @author bestjoy
 *
 */
public class PropertyManagementActivity extends BaseActionbarActivity{

	private static final String TAG = "PropertyManagementActivity";
	private HomeObject mHomeObject;
	private ContentResolver mContentResolver;
	private Bundle mBundles;
	private boolean mIsFirst = true;
	private List<CommunityServiceObject> mCommunityServiceObjectList = new ArrayList<CommunityServiceObject>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		
		//设置小区标题
		setTitle(mHomeObject.mHname);
		loadLocalCommunityServiceData();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mLoadLocalCommunityServiceData);
		AsyncTaskUtils.cancelTask(mLoadCommunityServiceData);
	}
	
	private LoadLocalCommunityServiceData mLoadLocalCommunityServiceData;
	private void loadLocalCommunityServiceData() {
		AsyncTaskUtils.cancelTask(mLoadLocalCommunityServiceData);
		mLoadLocalCommunityServiceData = new LoadLocalCommunityServiceData();
		mLoadLocalCommunityServiceData.execute();
	}
	private class LoadLocalCommunityServiceData extends AsyncTask<Void, Void, List<CommunityServiceObject>> {

		@Override
		protected  List<CommunityServiceObject> doInBackground(Void... params) {
			return HomesCommunityManager.getAllCommunityServiceObject(mContentResolver, String.valueOf(mHomeObject.mHomeUid), String.valueOf(mHomeObject.mHomeAid), String.valueOf(mHomeObject.mHid));
		}

		@Override
		protected void onPostExecute(List<CommunityServiceObject> result) {
			super.onPostExecute(result);
			if (result != null && result.size() == 0) {
				loadCommunityServiceData();
			} else {
				mCommunityServiceObjectList = result;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	
	
	
	
	private LoadCommunityServiceData mLoadCommunityServiceData;
	private void loadCommunityServiceData() {
		AsyncTaskUtils.cancelTask(mLoadCommunityServiceData);
		mLoadCommunityServiceData = new LoadCommunityServiceData();
		mLoadCommunityServiceData.execute();
		showDialog(DIALOG_PROGRESS);
	}
	private class LoadCommunityServiceData extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			try {
				JSONObject queryJsonObject = new JSONObject();
				queryJsonObject.put("xid", mHomeObject.mHid);
				queryJsonObject.put("uid", mHomeObject.mHomeUid);
				is = NetworkUtils.openContectionLocked(ServiceObject.getCommunityServices("para", queryJsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//添加成功
//					if (updated > 0) {
//						//刷新本地家
//						MyAccountManager.getInstance().initAccountHomes();
//					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}
	
	@Override
	protected boolean checkIntent(Intent intent) {
		mContentResolver = this.getContentResolver();
		mBundles = intent.getExtras();
		if (mBundles == null) {
			DebugUtils.logD(TAG, "checkIntent failed, due to mBundles is null");
		} else {
			DebugUtils.logD(TAG, "checkIntent true, find mBundles=" + mBundles);
		}
		mHomeObject = HomeObject.getHomeObject(mBundles);
		return mHomeObject != null;
	}
	public static void startActivity(Context context, Bundle bundle) {
		Intent intent = new Intent(context, PropertyManagementActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	private class MyLocationChangeCallback implements LocationChangeCallback {

		@Override
		public boolean isLocationChanged(BDLocation location) {
//			BaiduLocationManager.getInstance().setScanSpan(60*1000);//60秒请求一次定位数据
//			String adminCode = HomeObject.getDisID(getActivity().getContentResolver(), location.getProvince().replaceAll("[省市]", ""), location.getCity().replaceAll("[省市]", ""), location.getDistrict());
//			if (!TextUtils.isEmpty(adminCode)) {
//				String lastAdminCode = ComPreferencesManager.getInstance().mPreferManager.getString("admincode", "");
//				if (!lastAdminCode.equals(adminCode) 
//						|| WeatherManager.getInstance().isOldCahcedWeatherFile()) {
//					ComPreferencesManager.getInstance().mPreferManager.edit().putString("admincode", adminCode).commit();
//					return true;
//				}
//			}
			return false;
		}

		@Override
		public boolean onLocationChanged(BDLocation location) {
//			updateWeatherAsync(ComPreferencesManager.getInstance().mPreferManager.getString("admincode", ""));
			return true;
		}
		
	}

}
