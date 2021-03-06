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
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.utils.ClingHelper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.DialogUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;

public class ChooseCommunityActivity extends BaseActionbarActivity implements OnItemClickListener{

	private static final String TAG ="ChooseCommunityActivity";
	private ContentResolver mContentResolver;
	
	
	private HomeObject mHomeObject;
	private Bundle mBundles;
	
	private View mProgressLayout;
	private EditText mSearchInput;
	private ListView mListView;
	private PoiAdapter mPoiAdapter;
	private int mQuerySize = 500;
	private List<HomesCommunityObject> mHomesCommunityObjectList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugUtils.logD(TAG, "onCreate()");
		if (this.isFinishing()) {
			return;
		}
		setContentView(R.layout.activity_choose_community);
		mProgressLayout = findViewById(R.id.progress_layout);
		TextView status = (TextView) findViewById(R.id.progress_status);
		status.setText(getString(R.string.msg_load_communities_wait, mHomeObject.mHomePlaceDetail));
		mSearchInput = (EditText) findViewById(R.id.search_input);
		mSearchInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				filter(s.toString().trim());
			}
			
		});
		mListView = (ListView) findViewById(R.id.listview);
		mPoiAdapter = new PoiAdapter(this);
		mListView.setAdapter(mPoiAdapter);
		mListView.setOnItemClickListener(this);
//		GeoCoder geoCoder = GeoCoder.newInstance();
//		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {  
//		    public void onGetGeoCodeResult(GeoCodeResult result) {  
//		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
//		            //没有检索到结果  
//		        }  else {
//		        	//获取地理编码结果  
//		        	loadServerDataAsync();
//		        }
//		        
//		    }  
//		 
//		    @Override  
//		    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {  
//		        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
//		            //没有找到检索结果  
//		        }  
//		        //获取反向地理编码结果  
//		    }  
//		};
//		geoCoder.setOnGetGeoCodeResultListener(listener);
//		geoCoder.geocode(new GeoCodeOption()  
//	    .city(mHomeObject.mHomeCity)  
//	    .address(mHomeObject.mHomePlaceDetail));
		loadServerDataAsync();
	}
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mQueryServiceTask);
		AsyncTaskUtils.cancelTask(mRelateCommunityTask);
	}
	
	private FilterTask mFilterTask;
	private void filter(String filter) {
		if (TextUtils.isEmpty(filter)) {
			mPoiAdapter.changeData(mHomesCommunityObjectList);
			mPoiAdapter.notifyDataSetChanged();
			return;
		}
		AsyncTaskUtils.cancelTask(mFilterTask);
		mFilterTask = new FilterTask();
		mFilterTask.execute(filter);
	}
	private class FilterTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			List<HomesCommunityObject> newData = new ArrayList<HomesCommunityObject>(mHomesCommunityObjectList.size());
			for(HomesCommunityObject communityObject : mHomesCommunityObjectList){
				if (communityObject.mName.contains(params[0])) {
					newData.add(communityObject);
				}
			}
			mPoiAdapter.changeData(newData);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mPoiAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	
	
	private QueryServiceTask mQueryServiceTask;
	private void loadServerDataAsync() {
		mProgressLayout.setVisibility(View.VISIBLE);
		AsyncTaskUtils.cancelTask(mQueryServiceTask);
		mQueryServiceTask = new QueryServiceTask();
		mQueryServiceTask.execute();
	}

	/**更新或是新增的总数 >0表示有更新数据，需要刷新，=-1网络问题， =-2 已是最新数据 =0 没有更多数据*/
	private class QueryServiceTask extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(Void... arg0) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				//para={admin_code:310115,address:"浦东区张江高科技园区碧波路49弄1号%20",radius:"2000",pagesize:"50"}
//				InputStream iss = NetworkUtils.openContectionLocked("http://115.29.231.29/Haier/TestAgent.ashx", MyApplication.getInstance().getSecurityKeyValuesObject());
//				DebugUtils.logD(TAG, "chenkai " + NetworkUtils.getContentFromInput(iss));
				JSONObject queryJson = new JSONObject();
				queryJson.put("admin_code", HomeObject.getDisID(mContentResolver, mHomeObject.mHomeProvince, mHomeObject.mHomeCity, mHomeObject.mHomeDis));
				queryJson.put("address", mHomeObject.mHomePlaceDetail);
				queryJson.put("radius", "1000");
				queryJson.put("pagesize", String.valueOf(mQuerySize));
				is = NetworkUtils.openContectionLocked(ServiceObject.getPoiNearbySearchUrl("para", queryJson.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					JSONObject jsonObject = new JSONObject(NetworkUtils.getContentFromInput(is));
					serviceResultObject.mStatusCode = jsonObject.getInt("status");
					serviceResultObject.mStatusMessage = jsonObject.getString("message");
					
					int total = jsonObject.getInt("total");
					DebugUtils.logD(TAG, "find PoiNearby count " + total);
					serviceResultObject.mJsonArray = jsonObject.getJSONArray("results");
					serviceResultObject.mStatusCode = 1;
					mHomesCommunityObjectList = HomesCommunityObject.parse(serviceResultObject.mJsonArray);
					mPoiAdapter.changeData(mHomesCommunityObjectList);
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
			mProgressLayout.setVisibility(View.GONE);
			if (result.isOpSuccessfully()) {
				mPoiAdapter.notifyDataSetChanged();
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
			ClingHelper.showGuide("ChooseCommunityActivity.community_cling", ChooseCommunityActivity.this);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mProgressLayout.setVisibility(View.GONE);
		}
		
	}
	
	public static class PoiAdapter extends BaseAdapter{
		private Context _context;
		public PoiAdapter(Context context) {
			_context = context;
		}

		private List<HomesCommunityObject> _homesCommunityObjectList = new ArrayList<HomesCommunityObject>();
		public void changeData(List<HomesCommunityObject> datas) {
			_homesCommunityObjectList = datas;
		}
		@Override
		public int getCount() {
			return _homesCommunityObjectList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PoiViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(_context).inflate(R.layout.home_community_list_item, parent, false);
				viewHolder = new PoiViewHolder();
				viewHolder._name = (TextView) convertView.findViewById(R.id.name);
				viewHolder._address = (TextView) convertView.findViewById(R.id.detail);
				viewHolder._distance = (TextView) convertView.findViewById(R.id.distance);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (PoiViewHolder) convertView.getTag();
			}
			viewHolder._homesCommunityObject = _homesCommunityObjectList.get(position);
			
			bindView(convertView, position);
			return convertView;
		}
		
		public void bindView(View view, int position) {
			PoiViewHolder viewHolder = (PoiViewHolder) view.getTag();
			viewHolder._homesCommunityObject = _homesCommunityObjectList.get(position);
			viewHolder._name.setText(_homesCommunityObjectList.get(position).mName);
			viewHolder._address.setText(_homesCommunityObjectList.get(position).mAddressDetail);
			viewHolder._distance.setText(_context.getString(R.string.title_unit_m_format, _homesCommunityObjectList.get(position).mDistance));
		}
		
	}
	
	public static class PoiViewHolder {
		public TextView _name, _address, _distance;
		public HomesCommunityObject _homesCommunityObject;
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		mContentResolver = getContentResolver();
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
		Intent intent = new Intent(context, ChooseCommunityActivity.class);
		if (bundle != null) intent.putExtras(bundle);
		context.startActivity(intent);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final HomesCommunityObject homesCommunityObject = mHomesCommunityObjectList.get(position);
		DialogUtils.createSimpleConfirmAlertDialog(mContext, mContext.getString(R.string.format_relate_home_to_community, mHomeObject.toFriendString(), homesCommunityObject.mName), getString(android.R.string.ok), getString(android.R.string.cancel), new DialogUtils.DialogCallbackSimpleImpl(){

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
					relateCommunityAsync(homesCommunityObject.mName, homesCommunityObject.mQid);
					break;
				}
			}
			
		});
	}
	
	private RelateCommunityTask mRelateCommunityTask;
	private void relateCommunityAsync(String communityName, String communityUid) {
		AsyncTaskUtils.cancelTask(mRelateCommunityTask);
		mRelateCommunityTask = new RelateCommunityTask();
		mRelateCommunityTask.execute(communityName, communityUid);
		showDialog(DIALOG_PROGRESS);
	}
	private class RelateCommunityTask extends AsyncTask<String, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(String... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			try {
				JSONObject queryJsonObject = new JSONObject();
				queryJsonObject.put("xiaoqu_name", params[0]);
				queryJsonObject.put("aid", mHomeObject.mHomeAid);
				queryJsonObject.put("qid", params[1]);
				if (mHomeObject.mHid > 0) {
					queryJsonObject.put("xid", String.valueOf(mHomeObject.mHid));
				}
				is = NetworkUtils.openContectionLocked(ServiceObject.relatedHomeToCommunity("para", queryJsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//添加成功
					long hid = Long.parseLong(serviceResultObject.mStrData);
					mBundles.putLong("hid", hid);
					mHomeObject.mHid = hid;
					mHomeObject.mHname = params[0]; 
					mHomeObject.mCommunityServiceLoaded = 0;
					ContentValues values = new ContentValues();
					values.put(HaierDBHelper.HOME_COMMUNITY_HID, hid);
					values.put(HaierDBHelper.HOME_COMMUNITY_NAME, mHomeObject.mHname);
					values.put(HaierDBHelper.HOME_COMMUNITY_SERVICE_LOADED, mHomeObject.mCommunityServiceLoaded);
					int updated = BjnoteContent.update(getContentResolver(), BjnoteContent.Homes.CONTENT_URI, values, HomeObject.WHERE_UID_AND_AID, new String[]{String.valueOf(mHomeObject.mHomeUid), String.valueOf(mHomeObject.mHomeAid)});
					DebugUtils.logD(TAG, "RelateCommunityTask update Home with hid " + hid + ", hname " + params[0] + ", updated rows " + updated);
					if (updated > 0) {
						//刷新本地家
						MyAccountManager.getInstance().initAccountHomes();
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
				int type = mBundles.getInt(Intents.EXTRA_TYPE, -1);
				if (type == R.id.model_pick_community) {//选择小区
					PropertyManagementActivity.startActivity(mContext, mBundles);
				}
				finish();
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}
	
	
}
