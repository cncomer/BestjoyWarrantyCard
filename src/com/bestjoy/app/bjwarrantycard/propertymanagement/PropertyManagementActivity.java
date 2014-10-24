package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.graphics.drawable.Drawable;
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
import com.bestjoy.app.bjwarrantycard.propertymanagement.ChooseCommunityActivity.PoiAdapter;
import com.bestjoy.app.bjwarrantycard.propertymanagement.ChooseCommunityActivity.PoiViewHolder;
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
import com.shwy.bestjoy.utils.DialogUtils;
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
	private HashMap<Object, LoadNearbyServiceTask> mLoadNearbyServiceTaskList = new HashMap<Object, LoadNearbyServiceTask>();
	private Drawable mArrowRightDrawable, mArrowDownDrawable;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		mArrowRightDrawable = mContext.getResources().getDrawable(R.drawable.community_right_arrow);
		mArrowDownDrawable = mContext.getResources().getDrawable(R.drawable.community_down_arrow);
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
		for(int index=HomesCommunityManager.FIRST_SERVICE_POSITION;index < HomesCommunityManager.SECOND_SERVICE_POSITION; index++) {
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
			viewHolder._arrow.setOnClickListener(this);
			viewHolder._button_tel.setTag(viewHolder);
			viewHolder._button_tel.setOnClickListener(this);
			
			viewHolder._listview.setVisibility(View.GONE);
			
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
				queryJsonObject.put("aid", mHomeObject.mHomeAid);
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
		if (mHomeObject == null) {
			DebugUtils.logD(TAG, "checkIntent failed, due to HomeObject.getHomeObject(mBundles) return null");
			return false;
		}
		DebugUtils.logD(TAG, "checkIntent mHomeObject.mHid=" + mHomeObject.mHid);
		return mHomeObject != null && mHomeObject.mHid > 0;
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
			return Math.min(HomesCommunityManager.FIRST_SERVICE_POSITION, mCommunityServiceObjectList.size());
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
			ViewHolder viewHolder = (ViewHolder) object;
			showModifyDialog(viewHolder);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		Object object = v.getTag();
		switch(v.getId()) {
		case R.id.arrow://单击了下拉箭头，我们需要显示家附近的服务
			if (object != null && object instanceof ViewHolder) {
				ViewHolder viewHolder = (ViewHolder) object;
				showMorenearbyService(viewHolder);
			}
			break;
			default:
				if (object != null && object instanceof ViewHolder) {
					ViewHolder viewHolder = (ViewHolder) object;
					if (TextUtils.isEmpty(viewHolder._communityServiceObject.mServiceContent)) {
						//没有数据，我们需要让用户输入
						showModifyDialog(viewHolder);
					} else {
						Intents.callPhone(mContext, viewHolder._communityServiceObject.mServiceContent);
					}
				} else if (object instanceof PoiViewHolder) {
					//兴趣点，我们直接拨打电话
					final PoiViewHolder viewHolder = (PoiViewHolder) object;
					if (!TextUtils.isEmpty(viewHolder._homesCommunityObject.mTelephone)) {
						Intents.callPhone(mContext, viewHolder._homesCommunityObject.mTelephone);
					} else {
						DialogUtils.createSimpleConfirmAlertDialog(mContext, getString(R.string.format_empty_nearby_community_service_data, viewHolder._homesCommunityObject.mName), getString(android.R.string.ok), getString(android.R.string.cancel), new DialogUtils.DialogCallbackSimpleImpl() {

							@Override
							public void onCancel(DialogInterface dialog) {
								
							}

							@Override
							public void onDismiss(DialogInterface dialog) {
								
							}

							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch(which) {
								case DialogInterface.BUTTON_POSITIVE:
									Intents.location(mContext, viewHolder._homesCommunityObject.mlat + "," + viewHolder._homesCommunityObject.mlng);
									break;
								}
							}
						});
					}
					
				}
				break;
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
					updateCommunityServiceDataAsync(viewHolder, input.getText().toString().trim());
					
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
	private void updateCommunityServiceDataAsync(ViewHolder viewHolder, String newValue) {
		AsyncTaskUtils.cancelTask(mUpdateCommunityServiceDataTask);
		mUpdateCommunityServiceDataTask = new UpdateCommunityServiceDataTask(viewHolder, newValue);
		mUpdateCommunityServiceDataTask.execute();
		showDialog(DIALOG_PROGRESS);
	}
	private class UpdateCommunityServiceDataTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private ViewHolder _viewHolder;
		private String _newValue;
		public UpdateCommunityServiceDataTask(ViewHolder viewHolder, String newValue) {
			_viewHolder = viewHolder;
			_newValue = newValue;
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
				queryJsonObject.put("cell", _newValue);
				queryJsonObject.put("name", _viewHolder._communityServiceObject.mServiceName);
				queryJsonObject.put("stvalue", _viewHolder._communityServiceObject.mServiceType);
				queryJsonObject.put("uid", _viewHolder._communityServiceObject.mUid);
				queryJsonObject.put("xid", _viewHolder._communityServiceObject.mHid);
				is = NetworkUtils.openContectionLocked(ServiceObject.getCommunityServiceUpdateUrl("para", queryJsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//添加或更新成功
					_viewHolder._communityServiceObject.mServiceContent = _newValue;
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
	
	
	private void showMorenearbyService(ViewHolder viewHolder) {
		View expandLayout = viewHolder._expandLayout;
		if (expandLayout.getVisibility() == View.GONE) {
			//如果没有显示，则展开布局
			expandLayout.setVisibility(View.VISIBLE);
			viewHolder._arrow.setImageDrawable(mArrowDownDrawable);
			if (viewHolder._listview.getAdapter() == null || viewHolder._listview.getAdapter().getCount() == 0) {
				viewHolder._progressLayout.setVisibility(View.VISIBLE);
				//还没有设置数据，启动任务去后台查询
				LoadNearbyServiceTask loadNearbyServiceTask = mLoadNearbyServiceTaskList.get(viewHolder);
				if (loadNearbyServiceTask== null) {
					loadNearbyServiceTask = new LoadNearbyServiceTask(viewHolder);
					mLoadNearbyServiceTaskList.put(viewHolder, loadNearbyServiceTask);
					loadNearbyServiceTask.execute();
				}
			} else {
				viewHolder._progressLayout.setVisibility(View.GONE);
			}
		} else {
			expandLayout.setVisibility(View.GONE);
			viewHolder._arrow.setImageDrawable(mArrowRightDrawable);
		}
	}
	private class LoadNearbyServiceTask extends AsyncTask<Void, Void, ServiceResultObject> {
		private ViewHolder _viewHolder;
		private List<HomesCommunityObject> _homesCommunityObjectList = null;
		private LoadNearbyServiceTask(ViewHolder viewHolder) {
			_viewHolder = viewHolder;
		}

		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				//para={admin_code:310115,address:"浦东区张江高科技园区碧波路49弄1号%20",radius:"2000",pagesize:"50"}
//				InputStream iss = NetworkUtils.openContectionLocked("http://115.29.231.29/Haier/TestAgent.ashx", MyApplication.getInstance().getSecurityKeyValuesObject());
//				DebugUtils.logD(TAG, "chenkai " + NetworkUtils.getContentFromInput(iss));
				JSONObject queryJson = new JSONObject();
				queryJson.put("admin_code", HomeObject.getDisID(mContentResolver, mHomeObject.mHomeProvince, mHomeObject.mHomeCity, mHomeObject.mHomeDis));
				queryJson.put("address", mHomeObject.mHomePlaceDetail);
				queryJson.put("radius", "2000");
				queryJson.put("query", _viewHolder._communityServiceObject.mServiceName);
				queryJson.put("pagesize", "6");
				is = NetworkUtils.openContectionLocked(ServiceObject.getPoiNearbySearchUrl("para", queryJson.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					JSONObject jsonObject = new JSONObject(NetworkUtils.getContentFromInput(is));
					serviceResultObject.mStatusCode = jsonObject.getInt("status");
					serviceResultObject.mStatusMessage = jsonObject.getString("message");
					
					int total = jsonObject.getInt("total");
					DebugUtils.logD(TAG, "find PoiNearby count " + total);
					serviceResultObject.mJsonArray = jsonObject.getJSONArray("results");
					serviceResultObject.mStatusCode = 1;
					_homesCommunityObjectList = HomesCommunityObject.parse(serviceResultObject.mJsonArray);
				}
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (JSONException e) {
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
			_viewHolder._progressLayout.setVisibility(View.GONE);
			mLoadNearbyServiceTaskList.remove(_viewHolder);
			if (result.isOpSuccessfully()) {
				PoiAdapter poiAdapter = new PoiAdapter(mContext) {
					public void bindView(View view, int position) {
						PoiViewHolder viewHolder = (PoiViewHolder) view.getTag();
						viewHolder._homesCommunityObject = _homesCommunityObjectList.get(position);
						viewHolder._name.setText(_homesCommunityObjectList.get(position).mName);
						viewHolder._address.setText(_homesCommunityObjectList.get(position).mTelephone);
						viewHolder._distance.setText(mContext.getString(R.string.title_unit_m_format, _homesCommunityObjectList.get(position).mDistance));
					}
				};
				poiAdapter.changeData(_homesCommunityObjectList);
				_viewHolder._listview.setVisibility(View.VISIBLE);
				_viewHolder._listview.setAdapter(poiAdapter);
				//下拉列表的单击事件
				_viewHolder._listview.setOnItemClickListener(PropertyManagementActivity.this);
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				_viewHolder._expandLayout.setVisibility(View.GONE);
				_viewHolder._arrow.setImageDrawable(mArrowRightDrawable);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mLoadNearbyServiceTaskList.remove(_viewHolder);
			_viewHolder._expandLayout.setVisibility(View.GONE);
		}
		
	}
}
