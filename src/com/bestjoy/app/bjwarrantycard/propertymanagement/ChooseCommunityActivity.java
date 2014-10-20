package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
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
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.DialogUtils;
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
		mPoiAdapter = new PoiAdapter();
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
			try {
				//para={admin_code:310115,address:"浦东区张江高科技园区碧波路49弄1号%20",radius:"2000",pagesize:"50"}
				JSONObject queryJson = new JSONObject();
				queryJson.put("admin_code", HomeObject.getDisID(mContentResolver, mHomeObject.mHomeProvince, mHomeObject.mHomeCity, mHomeObject.mHomeDis));
				queryJson.put("address", mHomeObject.mHomePlaceDetail);
				queryJson.put("radius", "1000");
				queryJson.put("pagesize", String.valueOf(mQuerySize));
				InputStream is = NetworkUtils.openContectionLocked(ServiceObject.getPoiNearbySearchUrl("para", queryJson.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
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
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return serviceResultObject;
		}


		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			mProgressLayout.setVisibility(View.GONE);
			if (result.isOpSuccessfully()) {
				mPoiAdapter.notifyDataSetChanged();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mProgressLayout.setVisibility(View.GONE);
		}
		
	}
	
	private class PoiAdapter extends BaseAdapter{

		private List<HomesCommunityObject> mHomesCommunityObjectList = new ArrayList<HomesCommunityObject>();
		public void changeData(List<HomesCommunityObject> datas) {
			mHomesCommunityObjectList = datas;
		}
		@Override
		public int getCount() {
			return mHomesCommunityObjectList.size();
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
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.home_community_list_item, parent, false);
				viewHolder = new ViewHolder();
				viewHolder._name = (TextView) convertView.findViewById(R.id.name);
				viewHolder._address = (TextView) convertView.findViewById(R.id.detail);
				viewHolder._distance = (TextView) convertView.findViewById(R.id.distance);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder._name.setText(mHomesCommunityObjectList.get(position).mName);
			viewHolder._address.setText(mHomesCommunityObjectList.get(position).mAddressDetail);
			viewHolder._distance.setText(getString(R.string.title_unit_m_format, mHomesCommunityObjectList.get(position).mDistance));
			return convertView;
		}
		
	}
	
	private class ViewHolder {
		private TextView _name, _address, _distance;
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
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		HomesCommunityObject homesCommunityObject = mHomesCommunityObjectList.get(position);
		int formatResId = R.string.format_relate_home_to_community;
		if (ComPreferencesManager.getInstance().isFirstLaunch(TAG, true)) {
			ComPreferencesManager.getInstance().setFirstLaunch(TAG, false);
			formatResId = R.string.format_relate_home_to_community_first_time;
		}
		DialogUtils.createSimpleConfirmAlertDialog(mContext, mContext.getString(formatResId, mHomeObject.toFriendString(), homesCommunityObject.mName), new DialogUtils.DialogCallback(){

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
					
					break;
				}
			}
			
		});
	}
	
	
}
