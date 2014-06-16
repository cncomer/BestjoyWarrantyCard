package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.MaintenancePointBean;
import com.bestjoy.app.warrantycard.utils.PatternMaintenanceUtils;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class NearestMaintenancePointFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TAG = "NearestMaintenancePointFragment";
	
	private ListView mMalPointListView;
	private MalPointAdapter mMalPointAdapter;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private HomeObject mHomeObject;
	private List <MaintenancePointBean> mMaintenancePoint;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.button_maintenance_point);
		mMaintenancePoint = new ArrayList<MaintenancePointBean>();
		queryNearestPointSync();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.nearest_maintenance_point_fragment, container, false);

		mMalPointListView = (ListView) view.findViewById(R.id.listview);
		mMalPointAdapter = new MalPointAdapter(getActivity());
		mMalPointListView.setAdapter(mMalPointAdapter);
		mMalPointListView.setOnItemClickListener(mMalPointAdapter);
		mMalPointListView.setAdapter(mMalPointAdapter);
		
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
		if (infoInterface instanceof BaoxiuCardObject) {
			if (infoInterface != null) {
				mBaoxiuCardObject = (BaoxiuCardObject)infoInterface;
			}
		} else if (infoInterface instanceof HomeObject) {
			if (infoInterface != null) {
				mHomeObject = (HomeObject)infoInterface;
			}
		} else if (infoInterface instanceof AccountObject) {
			if (infoInterface != null) {
				long uid = ((AccountObject)infoInterface).mAccountUid;
			}
		}		
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
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder._name.setText(mMaintenancePoint.get(position).getMaintenancePointName());
			holder._detail.setText(mMaintenancePoint.get(position).getMaintenancePointDetail());
			holder._distance.setText(mMaintenancePoint.get(position).getMaintenancePointDistance());
			return convertView;
		}

		private class ViewHolder {
			private TextView _name, _detail, _distance;
		}

		@Override
		public void onItemClick(AdapterView<?> listView, View view, int pos, long arg3) {
		}
		
	}
	
	

	private QueryNearestPointAsyncTask mQueryNearestPointAsyncTask;
	private void queryNearestPointSync(String... param) {
		AsyncTaskUtils.cancelTask(mQueryNearestPointAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mQueryNearestPointAsyncTask = new QueryNearestPointAsyncTask();
		mQueryNearestPointAsyncTask.execute(param);
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
			
			StringBuilder sb = new StringBuilder("http://www.dzbxk.com/bestjoy/");
			sb.append("GetNearby.ashx?")
			.append("AID=").append(mBaoxiuCardObject.mAID)
			.append("&BID=").append(mBaoxiuCardObject.mBID)
			.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd));
			DebugUtils.logD(TAG, "param " + sb.toString());
			try {
				is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				mMaintenancePoint = PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses);
				DebugUtils.logD(TAG, "mMaintenancePoint = " + mMaintenancePoint);
				DebugUtils.logD(TAG, "StatusCode = " + serviceResultObject.mStatusCode);
				DebugUtils.logD(TAG, "StatusMessage = " + serviceResultObject.mStatusMessage);
				if (serviceResultObject.isOpSuccessfully()) {
					String data = serviceResultObject.mStrData;
					DebugUtils.logD(TAG, "Data = " + data);
				}
			} catch (JSONException e) {
				DebugUtils.logD("huasong", "huasong  JSONException = " + e);
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
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dissmissDialog(DIALOG_PROGRESS);
		}
	}
}
