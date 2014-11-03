package com.bestjoy.app.warrantycard.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.propertymanagement.HomesCommunityObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager.LocationChangeCallback;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.MaintenancePointBean;
import com.bestjoy.app.warrantycard.utils.PatternMaintenanceUtils;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.SecurityUtils;

public class NearestMaintenancePointFragment extends PullToRefreshListPageForFragment{
	private static final String TAG = "NearestMaintenancePointFragment";
	
	private MalPointAdapter mMalPointAdapter;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private Bundle mBundle;
	private LocationChangeCallback mLocationChangeCallback;
	
	private HomeObject mHomeObject;
	private Query mQuery;
	private int mBundleType = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mBundle = getArguments();
			DebugUtils.logD(TAG, "onCreate() savedInstanceState == null, getArguments() mBundle=" + mBundle);
		} else {
			mBundle = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle);
		}
		mBundleType = mBundle.getInt(Intents.EXTRA_TYPE);
		switch(mBundleType) {
		case R.id.model_my_card:
			mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
			break;
		case R.id.model_my_car_card:
			mLocationChangeCallback = new MyLocationChangeCallback();
			BaiduLocationManager.getInstance().addLocationChangeCallback(mLocationChangeCallback);
			mHomeObject = new HomeObject();
			break;
		}
		
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.button_maintenance_point);
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		switch(mBundleType) {
		case R.id.model_my_card:
			break;
		case R.id.model_my_car_card:
			mPullRefreshListView.setRefreshing();
			mPullRefreshListView.getLoadingLayoutProxy().setRefreshingLabel(getString(R.string.pull_to_refresh_locationing_label));
			mPullRefreshListView.getLoadingLayoutProxy().setLastUpdatedLabel("");
			BaiduLocationManager.getInstance().mLocationClient.requestLocation();
			break;
		}
	}
	
	



	@Override
	public void onStop() {
		super.onStop();
		switch(mBundleType) {
		case R.id.model_my_card:
			break;
		case R.id.model_my_car_card:
			BaiduLocationManager.getInstance().removeLocationChangeCallback(mLocationChangeCallback);
			break;
		}
		
	}



	@Override
	protected boolean isNeedForceRefreshOnResume() {
		switch(mBundleType) {
		case R.id.model_my_card:
			break;
		case R.id.model_my_car_card:
			return false;
		}
		return true;
	}



	@Override
	public void onItemClick(AdapterView<?> listView, View view, int pos, long arg3) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String url = holder._maintenancePoint.getMaintenancePointUrl();
		if(!TextUtils.isEmpty(url)) {				
			BrowserActivity.startActivity(mGlobalContext, url, mGlobalContext.getString(R.string.repair_point_detail));
		} else {
			MyApplication.getInstance().showMessage(R.string.repair_point_detail_no_uri_tips);
		}
	}
	private class ViewHolder {
		private TextView _name, _detail, _distance;
		private ImageView _phone;
		private MaintenancePointBean _maintenancePoint;
	}
	public class MalPointAdapter extends CursorAdapter {

		private Context _context;
		private MalPointAdapter (Context context, Cursor cursor, boolean autoRefresh) {
			super(context, cursor, autoRefresh);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			DebugUtils.logD(TAG, "MalPointAdapter newView context = " + context + ", cursor = " + cursor + ", parent = " + parent);
			View convertView = LayoutInflater.from(_context).inflate(R.layout.nearest_point_list_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder._name = (TextView) convertView.findViewById(R.id.mal_point_name);
			holder._detail = (TextView) convertView.findViewById(R.id.mal_point_detail);
			holder._distance = (TextView) convertView.findViewById(R.id.mal_point_distance);
			holder._phone = (ImageView) convertView.findViewById(R.id.mal_point_tel);
			convertView.setTag(holder);
			return convertView;
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder._maintenancePoint = PatternMaintenanceUtils.getMaintenancePointFromCursor(cursor);
			Float f = 0f;
			if(!TextUtils.isEmpty((holder._maintenancePoint.getMaintenancePointDistance()))) {
				f = Float.valueOf(holder._maintenancePoint.getMaintenancePointDistance()) / 1000;
			}
			holder._name.setText(holder._maintenancePoint.getMaintenancePointName());
			holder._detail.setText(holder._maintenancePoint.getMaintenancePointDetail());
			holder._distance.setText(String.format("%.1f", f) + _context.getResources().getString(R.string.maintence_point_distance_unit));
			final String tel = holder._maintenancePoint.getMaintenancePointTel();
			holder._phone.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(!TextUtils.isEmpty(tel)) {
						Intents.callPhone(getActivity(), tel);						
					} else {
						MyApplication.getInstance().showMessage(R.string.no_tel_tips);
					}
				}
			});
		}
		
	}
	
	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mMalPointAdapter = new MalPointAdapter(MyApplication.getInstance(), null, false);
		return new AdapterWrapper<CursorAdapter>(mMalPointAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		switch(mBundleType) {
		case R.id.model_my_card:
			return contentResolver.query(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION, MaintenancePointBean.MAINTENCE_PROJECTION_AID_BID_TYPE_SELECTION, new String[]{String.valueOf(mBaoxiuCardObject.mAID), String.valueOf(mBaoxiuCardObject.mBID), BaoxiuCardObject.TAG}, null);
		case R.id.model_my_car_card:
			return contentResolver.query(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION, MaintenancePointBean.MAINTENCE_PROJECTION_POINT_TYPE_SELECTION, new String[]{CarBaoxiuCardObject.TAG}, null);
		}
		return null;
	}

	@Override
	protected int savedIntoDatabase(ContentResolver contentResolver, List<? extends InfoInterface> infoObjects) {
		int insertOrUpdateCount = 0;
		if (infoObjects != null) {
			ContentValues values = new ContentValues();
			switch(mBundleType) {
			case R.id.model_my_card:
				values.put(HaierDBHelper.MAINTENCE_POINT_AID, mBaoxiuCardObject.mAID);
				values.put(HaierDBHelper.MAINTENCE_POINT_BID, mBaoxiuCardObject.mBID);
				values.put(HaierDBHelper.MAINTENCE_POINT_TYPE, BaoxiuCardObject.TAG);
				break;
			case R.id.model_my_car_card:
				values.put(HaierDBHelper.MAINTENCE_POINT_TYPE, CarBaoxiuCardObject.TAG);
				break;
			}
			
			for(InfoInterface object:infoObjects) {
				if (object.saveInDatebase(contentResolver, values)) {
					insertOrUpdateCount++;
				}
			}
		}
		return insertOrUpdateCount;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		int type = mBundle.getInt(Intents.EXTRA_TYPE);
		switch(type) {
		case R.id.model_my_car_card:{
			List <MaintenancePointBean> maintenancePoint = new ArrayList<MaintenancePointBean>();
			try {
				ServiceResultObject serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				maintenancePoint = PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses);
				DebugUtils.logD(TAG, "mMaintenancePoint = " + maintenancePoint);
				mQuery.mPageInfo.mTotalCount = serviceResultObject.mTotal;
				JSONObject queryJson = new JSONObject();
				try {
					queryJson.put("admin_code", mHomeObject.mAdminCode);
					queryJson.put("address", mHomeObject.mHomePlaceDetail);
					queryJson.put("radius", "10000");
					queryJson.put("query", getString(R.string.weixiu_nearby_points));
					queryJson.put("pagesize", String.valueOf(mQuery.mPageInfo.mPageSize));
					queryJson.put("pageindex", String.valueOf(mQuery.mPageInfo.mPageIndex));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mQuery.qServiceUrl = ServiceObject.getPoiNearbySearchUrl("para", queryJson.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return maintenancePoint;
		}
		case R.id.model_my_card: {
			List <MaintenancePointBean> maintenancePoint = new ArrayList<MaintenancePointBean>();
			try {
				ServiceResultObject serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
				maintenancePoint = PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			DebugUtils.logD(TAG, "mMaintenancePoint = " + maintenancePoint);
			return maintenancePoint;
//		case R.id.model_my_car_card:
//			if (is != null) {
//				try {
//					JSONObject jsonObject = new JSONObject(NetworkUtils.getContentFromInput(is));
//					pageInfo.mTotalCount = jsonObject.getInt("total");
//					DebugUtils.logD(TAG, "find PoiNearby count " + pageInfo.mTotalCount);
//					JSONArray jsonArray = jsonObject.getJSONArray("results");
//					return HomesCommunityObject.parse(jsonArray);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//			break;
		}
		}
		return new ArrayList<InfoInterface>();
		
	}

	@Override
	protected Query getQuery() {
		mQuery =  new Query();
		mQuery.mPageInfo = new PageInfo();
		switch(mBundleType) {
		case R.id.model_my_card:
			String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("GetNearby.ashx?")
			.append("AID=").append(mBaoxiuCardObject.mAID)
			.append("&BID=").append(mBaoxiuCardObject.mBID)
			.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd));
			DebugUtils.logD(TAG, "param " + sb.toString());
			mQuery.qServiceUrl = sb.toString();
			break;
		case R.id.model_my_car_card:
			JSONObject queryJson = new JSONObject();
			try {
				queryJson.put("admin_code", mHomeObject.mAdminCode);
				queryJson.put("address", mHomeObject.mHomePlaceDetail);
				queryJson.put("radius", "10000");
				queryJson.put("query", getString(R.string.weixiu_nearby_points));
				queryJson.put("pagesize", String.valueOf(mQuery.mPageInfo.mPageSize));
				queryJson.put("pageindex", String.valueOf(mQuery.mPageInfo.mPageIndex));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			DebugUtils.logD(TAG, "param " + queryJson.toString());
			mQuery.qServiceUrl = ServiceObject.getPoiNearbySearchUrl("para", queryJson.toString());
			break;
		}
		return mQuery;
	}

	@Override
	protected void onRefreshStart() {
		switch(mBundleType) {
		case R.id.model_my_card:
			PatternMaintenanceUtils.deleteCachedData(MyApplication.getInstance().getContentResolver(), String.valueOf(mBaoxiuCardObject.mAID), String.valueOf(mBaoxiuCardObject.mBID));
			break;
		case R.id.model_my_car_card:
			PatternMaintenanceUtils.deleteCachedData(MyApplication.getInstance().getContentResolver(), CarBaoxiuCardObject.TAG);
			break;
		}
		
	}

	@Override
	protected void onRefreshEnd() {
	}

	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}
	
	private class MyLocationChangeCallback implements LocationChangeCallback {

		@Override
		public boolean isLocationChanged(BDLocation location) {
			if (getActivity() == null) {
				return false;
			}
			DebugUtils.logD(TAG, "isLocationChanged location " + location);
			if (location.getProvince() == null
					|| location.getCity() == null
					|| location.getDistrict() == null) {
				DebugUtils.logD(TAG, "isLocationChanged getProvince() " + location.getProvince() + ", getCity() " +location.getCity() + ", getDistrict() " + location.getDistrict());
				return false;
			}
			mHomeObject.mHomeProvince = location.getProvince().replaceAll("[省市]", "");
			mHomeObject.mHomeCity = location.getCity().replaceAll("[省市]", "");
			mHomeObject.mHomeDis = location.getDistrict();
			mHomeObject.mHomePlaceDetail = location.getAddrStr();
			mHomeObject.mAdminCode = HomeObject.getDisID(getActivity().getContentResolver(), mHomeObject.mHomeProvince, mHomeObject.mHomeCity, mHomeObject.mHomeDis);
			DebugUtils.logD(TAG, "isLocationChanged getAdminCode " + mHomeObject.mAdminCode + ",  getAddrStr() " + location.getAddrStr());
			return true;
		}

		@Override
		public boolean onLocationChanged(BDLocation location) {
			forceRefresh();
			return true;
		}
		
	}
}
