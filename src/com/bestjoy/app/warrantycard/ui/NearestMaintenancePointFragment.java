package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.MaintenancePointBean;
import com.bestjoy.app.warrantycard.utils.PatternMaintenanceUtils;
import com.costum.android.widget.PullAndLoadListView;
import com.costum.android.widget.PullAndLoadListView.OnLoadMoreListener;
import com.costum.android.widget.PullToRefreshListView.OnRefreshListener;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class NearestMaintenancePointFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TAG = "NearestMaintenancePointFragment";
	
	private PullAndLoadListView mMalPointListView;
	private MalPointAdapter mMalPointAdapter;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private List <MaintenancePointBean> mMaintenancePoint;
	private static final int STATE_IDLE = 0;
	private static final int STATE_FREASHING = STATE_IDLE + 1;
	private static final int STATE_FREASH_COMPLETE = STATE_FREASHING + 1;
	private static final int STATE_FREASH_CANCEL = STATE_FREASH_COMPLETE + 1;
	private int mLoadState = STATE_IDLE;
	private int mLoadPageIndex = 0;
	private Bundle mBundle;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState == null) {
			mBundle = getArguments();
			DebugUtils.logD(TAG, "onCreate() savedInstanceState == null, getArguments() mBundle=" + mBundle);
		} else {
			mBundle = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle);
		}
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
		getActivity().setTitle(R.string.button_maintenance_point);
		mMaintenancePoint = new ArrayList<MaintenancePointBean>();
		mMalPointAdapter = new MalPointAdapter(getActivity());
		queryNearestPointSync();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.nearest_maintenance_point_fragment, container, false);

		mMalPointListView = (PullAndLoadListView) view.findViewById(R.id.listview);
		mMalPointListView.setAdapter(mMalPointAdapter);
		mMalPointListView.setOnItemClickListener(mMalPointAdapter);
		mMalPointListView.setAdapter(mMalPointAdapter);
		mMalPointListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                GetDataTask();
            }
        });
		mMalPointListView.setOnLoadMoreListener(new OnLoadMoreListener() {
			
			public void onLoadMore() {
				// Do the work to load more items at the end of list
				// here
				LoadMoreDataTask();
			}
		});
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public BaoxiuCardObject getBaoxiuCardObject() {
		BaoxiuCardObject baoxiuCardObject = new BaoxiuCardObject();
		
		return baoxiuCardObject;
	}
	
	public AccountObject getContactInfoObject() {
		AccountObject contactInfoObject = new AccountObject();
		return contactInfoObject;
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void updateInfoInterface(InfoInterface infoInterface) {
	}

	@Override
	public void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject) {
		
	}


	public class MalPointAdapter extends BaseAdapter implements ListView.OnItemClickListener{

		private Context _context;
		private MalPointAdapter (Context context) {
			_context = context;
		}
		@Override
		public int getCount() {
			return mMaintenancePoint != null ? mMaintenancePoint.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(_context).inflate(R.layout.nearest_point_list_item, parent, false);
				holder = new ViewHolder();
				holder._name = (TextView) convertView.findViewById(R.id.mal_point_name);
				holder._detail = (TextView) convertView.findViewById(R.id.mal_point_detail);
				holder._distance = (TextView) convertView.findViewById(R.id.mal_point_distance);
				holder._phone = (ImageView) convertView.findViewById(R.id.mal_point_tel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Float f = 0f;
			if(!TextUtils.isEmpty((mMaintenancePoint.get(position).getMaintenancePointDistance()))) {
				f = Float.valueOf(mMaintenancePoint.get(position).getMaintenancePointDistance()) / 1000;
			}
			holder._name.setText(mMaintenancePoint.get(position).getMaintenancePointName());
			holder._detail.setText(mMaintenancePoint.get(position).getMaintenancePointDetail());
			holder._distance.setText(String.format("%.1f", f) + _context.getResources().getString(R.string.maintence_point_distance_unit));
			final int pos = position;
			holder._phone.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String tel = mMaintenancePoint.get(pos).getMaintenancePointTel();
					if(!TextUtils.isEmpty(tel)) {
						Intents.callPhone(getActivity(), tel);						
					} else {
						MyApplication.getInstance().showMessage(R.string.no_tel_tips);
					}
				}
			});
			return convertView;
		}

		private class ViewHolder {
			private TextView _name, _detail, _distance;
			private ImageView _phone;
		}

		@Override
		public void onItemClick(AdapterView<?> listView, View view, int pos, long arg3) {
			if(mLoadState == STATE_FREASHING) return;
			String url = mMaintenancePoint.get(pos-1).getMaintenancePointUrl();
			if(!TextUtils.isEmpty(url)) {				
				BrowserActivity.startActivity(_context, url, _context.getString(R.string.repair_point_detail));
			} else {
				MyApplication.getInstance().showMessage(R.string.repair_point_detail_no_uri_tips);
			}
		}
	}

	//refresh data begin
	private RefreshNearestPointAsyncTask mRefreshNearestPointAsyncTask;
	private void GetDataTask(String... param) {
		mLoadState = STATE_FREASHING;
		AsyncTaskUtils.cancelTask(mRefreshNearestPointAsyncTask);
		mRefreshNearestPointAsyncTask = new RefreshNearestPointAsyncTask();
		mRefreshNearestPointAsyncTask.execute(param);
	}

	private class RefreshNearestPointAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			//更新保修卡信息
			DebugUtils.logD(TAG, "CreateNewWarrantyCardAsyncTask for AID " + mBaoxiuCardObject.mAID);
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;

			String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
			
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("GetNearby.ashx?")
			.append("AID=").append(mBaoxiuCardObject.mAID)
			.append("&BID=").append(mBaoxiuCardObject.mBID)
			.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd))
			.append("&page_num=").append(0);//0page
			DebugUtils.logD(TAG, "param " + sb.toString());
			try {
				is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				mMaintenancePoint = PatternMaintenanceUtils.getMaintenancePointClean(serviceResultObject.mAddresses, getActivity().getContentResolver(), mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
				DebugUtils.logD(TAG, "mMaintenancePoint = " + mMaintenancePoint);
				DebugUtils.logD(TAG, "StatusCode = " + serviceResultObject.mStatusCode);
				DebugUtils.logD(TAG, "StatusMessage = " + serviceResultObject.mStatusMessage);
				if (serviceResultObject.isOpSuccessfully()) {
					String data = serviceResultObject.mStrData;
					DebugUtils.logD(TAG, "Data = " + data);
				}
			} catch (JSONException e) {
				DebugUtils.logD(TAG, "JSONException = " + e);
				e.printStackTrace();
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

			mMalPointAdapter.notifyDataSetChanged();
			if(result.mAddresses == null || result.mAddresses.length() == 0) {
				MyApplication.getInstance().showMessage(R.string.maintence_point_query_fail);
			} else {
				mLoadPageIndex = 1;
			}
			mMalPointListView.onRefreshComplete();
			mLoadState = STATE_FREASH_COMPLETE;
			DebugUtils.logD(TAG, "huasong onPostExecute onLoadMoreComplete");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mMalPointListView.onRefreshComplete();
			mLoadState = STATE_FREASH_CANCEL;
		}
	}
	//refresh data end
	
	//load more data begin
	private LoadMoreNearestPointAsyncTask mLoadMoreNearestPointAsyncTask;
	private void LoadMoreDataTask(String... param) {
		mLoadState = STATE_FREASHING;
		AsyncTaskUtils.cancelTask(mLoadMoreNearestPointAsyncTask);
		mLoadMoreNearestPointAsyncTask = new LoadMoreNearestPointAsyncTask();
		mLoadMoreNearestPointAsyncTask.execute(param);
	}

	private class LoadMoreNearestPointAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			if(mLoadPageIndex == 0) return null;
			//更新保修卡信息
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;

			String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
			
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("GetNearby.ashx?")
			.append("AID=").append(mBaoxiuCardObject.mAID)
			.append("&BID=").append(mBaoxiuCardObject.mBID)
			.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd))
			.append("&page_num=").append(++mLoadPageIndex);//0 page
			DebugUtils.logD(TAG, "param " + sb.toString());
			try {
				is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				mMaintenancePoint.addAll(PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses, getActivity().getContentResolver(), mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID));
				DebugUtils.logD(TAG, "mMaintenancePoint = " + mMaintenancePoint);
				DebugUtils.logD(TAG, "StatusCode = " + serviceResultObject.mStatusCode);
				DebugUtils.logD(TAG, "StatusMessage = " + serviceResultObject.mStatusMessage);
				if (serviceResultObject.isOpSuccessfully()) {
					String data = serviceResultObject.mStrData;
					DebugUtils.logD(TAG, "Data = " + data);
				}
			} catch (JSONException e) {
				DebugUtils.logD(TAG, "JSONException = " + e);
				e.printStackTrace();
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
			mMalPointAdapter.notifyDataSetChanged();

			mMalPointListView.onLoadMoreComplete();
			mLoadState = STATE_FREASH_COMPLETE;
			DebugUtils.logD(TAG, "huasong onPostExecute onLoadMoreComplete");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mMalPointListView.onLoadMoreComplete();
			mLoadState = STATE_FREASH_CANCEL;
		}
	}
	//load more data end

	private QueryNearestPointAsyncTask mQueryNearestPointAsyncTask;
	private void queryNearestPointSync(String... param) {
		if(PatternMaintenanceUtils.isExsited(getActivity().getContentResolver(), String.valueOf(mBaoxiuCardObject.mAID), String.valueOf(mBaoxiuCardObject.mBID)) > 0){
			mMaintenancePoint = PatternMaintenanceUtils.getMaintenancePointLocal(getActivity().getContentResolver(), mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
			mMalPointAdapter.notifyDataSetChanged();
			mLoadPageIndex = mMaintenancePoint.size() / 10;
		} else {
			mLoadState = STATE_FREASHING;
			AsyncTaskUtils.cancelTask(mQueryNearestPointAsyncTask);
			showDialog(DIALOG_PROGRESS);
			mQueryNearestPointAsyncTask = new QueryNearestPointAsyncTask();
			mQueryNearestPointAsyncTask.execute(param);
		}
	}

	private class QueryNearestPointAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			//更新保修卡信息
			DebugUtils.logD(TAG, "CreateNewWarrantyCardAsyncTask for AID " + mBaoxiuCardObject.mAID);
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;

			String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
			
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("GetNearby.ashx?")
			.append("AID=").append(mBaoxiuCardObject.mAID)
			.append("&BID=").append(mBaoxiuCardObject.mBID)
			.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd))
			.append("&page_num=").append(0);//0page
			DebugUtils.logD(TAG, "param " + sb.toString());
			try {
				is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				mMaintenancePoint = PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses, getActivity().getContentResolver(), mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
				DebugUtils.logD(TAG, "mMaintenancePoint = " + mMaintenancePoint);
				DebugUtils.logD(TAG, "StatusCode = " + serviceResultObject.mStatusCode);
				DebugUtils.logD(TAG, "StatusMessage = " + serviceResultObject.mStatusMessage);
				if (serviceResultObject.isOpSuccessfully()) {
					String data = serviceResultObject.mStrData;
					DebugUtils.logD(TAG, "Data = " + data);
				}
			} catch (JSONException e) {
				DebugUtils.logD(TAG, "JSONException = " + e);
				e.printStackTrace();
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
			dissmissDialog(DIALOG_PROGRESS);
			mMalPointAdapter.notifyDataSetChanged();
			if(result.mAddresses == null || result.mAddresses.length() == 0) {
				MyApplication.getInstance().showMessage(R.string.maintence_point_query_fail);
			} else {
				mLoadPageIndex = 1;
			}
			mLoadState = STATE_FREASH_COMPLETE;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dissmissDialog(DIALOG_PROGRESS);
			mLoadState = STATE_FREASH_CANCEL;
		}
	}

	@Override
    public void updateArguments(Bundle args) {
	    // TODO Auto-generated method stub
	    
    }
}
