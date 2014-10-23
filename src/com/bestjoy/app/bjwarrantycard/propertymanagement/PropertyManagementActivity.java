package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.propertymanagement.HomesCommunityManager.CommunityServiceObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager.LocationChangeCallback;
import com.bestjoy.app.warrantycard.utils.ClingHelper;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.MyGridView;
import com.bestjoy.app.warrantycard.view.MyListView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;

/**
 * 对于家地址，我们需要让用户去匹配对应的小区物业，来使用小区功能
 * @author bestjoy
 *
 */
public class PropertyManagementActivity extends BaseActionbarActivity implements View.OnClickListener, View.OnLongClickListener, OnItemClickListener, OnItemLongClickListener{

	private static final String TAG = "PropertyManagementActivity";
	private HomeObject mHomeObject;
	private ContentResolver mContentResolver;
	private Bundle mBundles;
	private boolean mIsFirst = true;
	private List<CommunityServiceObject> mCommunityServiceObjectList = new ArrayList<CommunityServiceObject>();
	private MyGridView mMyGridView;
	private MyGridViewAdapter mMyGridViewAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		setContentView(R.layout.activity_property_management);
		//设置小区标题
		setTitle(mHomeObject.mHname);
		if (mHomeObject.mCommunityServiceLoaded == 1) {
			loadLocalCommunityServiceData();
		} else {
			loadLocalCommunityServiceData();
			loadCommunityServiceData();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mLoadLocalCommunityServiceData);
		AsyncTaskUtils.cancelTask(mLoadCommunityServiceData);
	}
	
	private void updateViews() {
		mMyGridView = (MyGridView) findViewById(R.id.main_service);
		mMyGridViewAdapter = new MyGridViewAdapter();
		mMyGridView.setAdapter(mMyGridViewAdapter);
		mMyGridView.setOnItemClickListener(this); 
		mMyGridView.setOnItemLongClickListener(this);
		
		initKuaijieServices();
	}
	
	private void initKuaijieServices() {
		LinearLayout kuaijieLayout = (LinearLayout) findViewById(R.id.kuaijie_service_layout);
		kuaijieLayout.removeAllViews();
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		for(int index=HomesCommunityManager.FIRST_SERVICE_POSITION+1;index <= HomesCommunityManager.SECOND_SERVICE_POSITION; index++) {
			View view = layoutInflater.inflate(R.layout.community_service_list_item, kuaijieLayout, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder._arrow = (ImageView) view.findViewById(R.id.arrow);
			viewHolder._name = (TextView) view.findViewById(R.id.name);
			viewHolder._button_tel = (ImageView) view.findViewById(R.id.button_tel);
			viewHolder._content = (TextView) view.findViewById(R.id.content);
			viewHolder._listview = (MyListView) view.findViewById(R.id.listview);
			viewHolder._progressLayout = view.findViewById(R.id.progress_layout);
			viewHolder._expandLayout = view.findViewById(R.id.expand_layout);
			viewHolder._communityServiceObject = mCommunityServiceObjectList.get(index);
			
			view.setId(viewHolder._communityServiceObject.mViewId);
			
			viewHolder._name.setText(viewHolder._communityServiceObject.mServiceName);
			if (!TextUtils.isEmpty(viewHolder._communityServiceObject.mServiceContent)) {
				viewHolder._content.setText(viewHolder._communityServiceObject.mServiceContent);
			}
			
			viewHolder._arrow.setTag(viewHolder);
			viewHolder._button_tel.setTag(viewHolder);
			viewHolder._button_tel.setOnClickListener(this);
			
			viewHolder._listview.setVisibility(View.GONE);
			//下拉列表的单击事件
			viewHolder._listview.setOnItemClickListener(this);
			
			viewHolder._content.setTag(viewHolder);
			viewHolder._content.setOnClickListener(this);
			viewHolder._content.setOnLongClickListener(this);
			view.setTag(viewHolder);
			
			kuaijieLayout.addView(view);
		}
		
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
			if (result.size() == 0) {
				//如果没有，我们先显示本地默认数据
				result = HomesCommunityManager.getAllDefaultCommunityServiceObject(mHomeObject);
			}
			mCommunityServiceObjectList = result;
			updateViews();
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
				serviceResultObject = ServiceResultObject.parseArray(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					List<CommunityServiceObject> communityServiceObjectList = HomesCommunityManager.getAllCommunityServiceObject(serviceResultObject.mJsonArray);
					int loadItemCount = 0;
					for(CommunityServiceObject communityServiceObject : communityServiceObjectList) {
						if (communityServiceObject.saveInDatebase(mContentResolver, null)) {
							loadItemCount++;
						}
					}
					if (loadItemCount > 0) {
						mHomeObject.mCommunityServiceLoaded = 1;
						ContentValues values = new ContentValues();
						values.put(HaierDBHelper.HOME_COMMUNITY_SERVICE_LOADED, mHomeObject.mCommunityServiceLoaded);
						int updated = BjnoteContent.update(mContentResolver, BjnoteContent.Homes.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mHomeObject.mHomeId)});
						if (updated > 0) {
							DebugUtils.logD(TAG, "LoadCommunityServiceData update mCommunityServiceLoaded to 1 for Community " + mHomeObject.mHname);
							MyAccountManager.getInstance().initAccountHomes();
						}
					}
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
			if (result.isOpSuccessfully()) {
				if (mHomeObject.mCommunityServiceLoaded == 1) {
					loadLocalCommunityServiceData();
				} else {
					MyApplication.getInstance().showMessage(R.string.msg_load_community_service_failed);
				}
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				loadLocalCommunityServiceData();
			}
			ClingHelper.showGuide("PropertyManagementActivity.community_cling", PropertyManagementActivity.this);
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
	
	
	private class MyGridViewAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return Math.min(HomesCommunityManager.FIRST_SERVICE_POSITION + 1, mCommunityServiceObjectList.size());
		}

		@Override
		public CommunityServiceObject getItem(int position) {
			return mCommunityServiceObjectList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).mId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.community_service_gridview_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder._icon = (ImageView) convertView.findViewById(R.id.icon);
				viewHolder._name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder._communityServiceObject = getItem(position);
			viewHolder._icon.setImageResource(viewHolder._communityServiceObject.mServiceIconResId);
			viewHolder._name.setText(viewHolder._communityServiceObject.mServiceName);
			return convertView;
		}
		
	}
	
	private class ViewHolder {
		private ImageView _icon;
		private TextView _name;
		private TextView _content;
		private ImageView _button_tel;
		private ImageView _arrow;
		private MyListView _listview;
		private CommunityServiceObject _communityServiceObject;
		private View _progressLayout, _expandLayout;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return onLongClick(view);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onClick(view);
	}

	@Override
	public boolean onLongClick(View v) {
		Object object = v.getTag();
		if (object != null && object instanceof ViewHolder) {
			ViewHolder viewHolder = (ViewHolder) v.getTag();
			showModifyDialog(viewHolder);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		Object object = v.getTag();
		if (object != null && object instanceof ViewHolder) {
			ViewHolder viewHolder = (ViewHolder) v.getTag();
			if (TextUtils.isEmpty(viewHolder._communityServiceObject.mServiceContent)) {
				//没有数据，我们需要让用户输入
				showModifyDialog(viewHolder);
			} else {
				Intents.callPhone(mContext, viewHolder._communityServiceObject.mServiceContent);
			}
		}
	}
	
	
	private void showModifyDialog(final ViewHolder viewHolder) {
		final EditText input = new EditText(mContext);
		input.setText(viewHolder._communityServiceObject.mServiceContent);
		input.setSelection(viewHolder._communityServiceObject.mServiceContent.length());
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
		.setTitle(getString(R.string.format_title_modify_community_service_data, viewHolder._communityServiceObject.mServiceName))
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					viewHolder._communityServiceObject.mServiceContent = input.getText().toString().trim();
					updateCommunityServiceDataAsync(viewHolder);
					
				}
			})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
			}
			
		});
		
		dialog.show();
	}
	
	private UpdateCommunityServiceDataTask mUpdateCommunityServiceDataTask;
	private void updateCommunityServiceDataAsync(ViewHolder viewHolder) {
		AsyncTaskUtils.cancelTask(mUpdateCommunityServiceDataTask);
		mUpdateCommunityServiceDataTask = new UpdateCommunityServiceDataTask(viewHolder);
		mUpdateCommunityServiceDataTask.execute();
		showDialog(DIALOG_PROGRESS);
	}
	private class UpdateCommunityServiceDataTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private ViewHolder _viewHolder;
		public UpdateCommunityServiceDataTask(ViewHolder viewHolder) {
			_viewHolder = viewHolder;
		}
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			try {
				//if (para != null && !string.IsNullOrEmpty(para.cell)
				//&& !string.IsNullOrEmpty(para.name) 
				//&& !string.IsNullOrEmpty(para.stvalue) 
				//&& StrHelper.IsNum(para.uid) 
				//&& StrHelper.IsNum(para.xid))
				JSONObject queryJsonObject = new JSONObject();
				queryJsonObject.put("cell", _viewHolder._communityServiceObject.mServiceContent);
				queryJsonObject.put("name", _viewHolder._communityServiceObject.mServiceName);
				queryJsonObject.put("stvalue", _viewHolder._communityServiceObject.mServiceType);
				queryJsonObject.put("uid", _viewHolder._communityServiceObject.mUid);
				queryJsonObject.put("xid", _viewHolder._communityServiceObject.mHid);
				is = NetworkUtils.openContectionLocked(ServiceObject.getCommunityServiceUpdateUrl("para", queryJsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//添加或更新成功
					boolean save = _viewHolder._communityServiceObject.saveInDatebase(mContentResolver, null);
					DebugUtils.logD(TAG, "UpdateCommunityServiceDataTask save CommunityServiceObject with content " + _viewHolder._communityServiceObject.mServiceContent + ", effected rows " + save);
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
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			if (result.isOpSuccessfully()) {
				if (_viewHolder._listview != null) {
					_viewHolder._content.setText(_viewHolder._communityServiceObject.mServiceContent);
				}
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}

}
