package com.bestjoy.app.warrantycard.utils;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
/**
 * 使用说明http://developer.baidu.com/map/index.php?title=android-locsdk/guide/v4-2
 * @author bestjoy
 *
 */
public class BaiduLocationManager {
	public static final String TAG = "BaiduLocationManager";
	public LocationClient mLocationClient;
	public GeofenceClient mGeofenceClient;
	private List<LocationChangeCallback> mLocationChangeCallbackList = new LinkedList<LocationChangeCallback>();
	private Context mContext;
	private static final BaiduLocationManager INSTANCE = new BaiduLocationManager();
	private BaiduLocationManager(){}
	
	public static BaiduLocationManager getInstance() {
		return INSTANCE;
	}
	
	public void setContext(Context context) {
		mContext = context;
		mLocationClient = new LocationClient(context);
		mGeofenceClient = new GeofenceClient(context);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02, 定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，若需要将定位点的位置通过百度Android地图 SDK进行地图展示，请返回bd09ll，将无偏差的叠加在百度地图上
		option.setScanSpan(10000);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		mLocationClient.registerLocationListener(new MyLocationListener());
	}
	
	public synchronized void addLocationChangeCallback(LocationChangeCallback callback) {
		if (!mLocationChangeCallbackList.contains(callback)) {
			mLocationChangeCallbackList.add(callback);
		}
	}
	
	public synchronized void removeLocationChangeCallback(LocationChangeCallback callback) {
		if (mLocationChangeCallbackList.contains(callback)) {
			mLocationChangeCallbackList.remove(callback);
		}
	}
	
	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			receiveLocationAsync(location);
		}

	}
	
	private void receiveLocationAsync(BDLocation location) {
		new ReceiveLocationTask(location).execute();
	}
	
	private class ReceiveLocationTask extends AsyncTask<Void, LocationChangeCallback, Void> {

		private BDLocation _location;
		public ReceiveLocationTask(BDLocation location) {
			_location = location;
		}
		@Override
		protected Void doInBackground(Void... params) {
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(_location.getTime());
			sb.append("\nerror code : ");
			sb.append(_location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(_location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(_location.getLongitude());
			sb.append("\nradius : ");
			sb.append(_location.getRadius());
			if (_location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(_location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(_location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(_location.getAddrStr());
				sb.append(_location.getDirection());
			} else if (_location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(_location.getAddrStr());
				//运营商信息
				sb.append("\noperationers : ");
				sb.append(_location.getOperators());
			}
			DebugUtils.logD(TAG, sb.toString());
			
			for(LocationChangeCallback callback : mLocationChangeCallbackList) {
				if (callback.isLocationChanged(_location)) {
					publishProgress(callback);
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(LocationChangeCallback... values) {
			super.onProgressUpdate(values);
			values[0].onLocationChanged(_location);
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	
	public static interface LocationChangeCallback {
		public boolean isLocationChanged(BDLocation location);
		public boolean onLocationChanged(BDLocation location);
	}

}
