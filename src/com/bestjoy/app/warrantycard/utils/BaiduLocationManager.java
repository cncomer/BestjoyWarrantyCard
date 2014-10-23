package com.bestjoy.app.warrantycard.utils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bestjoy.app.bjwarrantycard.R;
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
	private MyLocationListener mMyLocationListener;
	private BaiduLocationManager(){}
	
	public static BaiduLocationManager getInstance() {
		return INSTANCE;
	}
	
	public void setContext(Context context) {
		mContext = context;
		mLocationClient = new LocationClient(context);
		mGeofenceClient = new GeofenceClient(context);
		mMyLocationListener = new MyLocationListener();
		setScanSpan(5000);
	}
	
	public void setScanSpan(int timeInMiliSecond) {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02, 定位SDK可以返回bd09、bd09ll、gcj02三种类型坐标，若需要将定位点的位置通过百度Android地图 SDK进行地图展示，请返回bd09ll，将无偏差的叠加在百度地图上
		
		/**
		 * 说明：
			当所设的整数值大于等于1000（ms）时，定位SDK内部使用定时定位模式。调用requestLocation( )后，每隔设定的时间，定位SDK就会进行一次定位。
			如果定位SDK根据定位依据发现位置没有发生变化，就不会发起网络请求，返回上一次定位的结果；如果发现位置改变，就进行网络请求进行定位，得到新的定位结果。
			定时定位时，调用一次requestLocation，会定时监听到定位结果。

			当不设此项，或者所设的整数值小于1000（ms）时，采用一次定位模式。每调用一次requestLocation( )，定位SDK会发起一次定位。请求定位与监听结果一一对应。
			设定了定时定位后，可以热切换成一次定位，需要重新设置时间间隔小于1000（ms）即可。locationClient对象stop后，将不再进行定位。如果设定了定时定位模式后，
			多次调用requestLocation（），则是每隔一段时间进行一次定位，同时额外的定位请求也会进行定位，但频率不会超过1秒一次。
		 */
		option.setScanSpan(timeInMiliSecond);//设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		option.setOpenGps(true);//设置是否打开gps，使用gps前提是用户硬件打开gps。默认是不打开gps的。
		option.setProdName(mContext.getString(R.string.app_name));//设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务
		
		mLocationClient.setLocOption(option);
		mLocationClient.unRegisterLocationListener(mMyLocationListener);
		mLocationClient.registerLocationListener(mMyLocationListener);
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
			DebugUtils.logD(TAG, "ReceiveLocationTask time " + new Date().getTime());
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
			if (_location != null) {
				for(LocationChangeCallback callback : mLocationChangeCallbackList) {
					if (callback.isLocationChanged(_location)) {
						publishProgress(callback);
					}
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
