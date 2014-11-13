package com.bestjoy.app.warrantycard.utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.FilesUtils;

public class WeatherManager {
	private static final String TAG = "WeatherManager";
	private Context mContext;
	private static final WeatherManager INSTANCE = new WeatherManager();
	
	private static final HashMap<String, String> mWeekdayMap = new HashMap<String, String>();
	
	private static final File mLocalWeatherCachedFile = MyApplication.getInstance().getFile("weather", "weather.xml");
	private WeatherManager(){}
	
	public void setContext(Context context) {
		mContext = context;
		mWeekdayMap.put("Monday", context.getString(R.string.monday));
		mWeekdayMap.put("Tuesday", context.getString(R.string.tuesday));
		mWeekdayMap.put("Wednesday", context.getString(R.string.wednesday));
		mWeekdayMap.put("Thursday", context.getString(R.string.thursday));
		mWeekdayMap.put("Friday", context.getString(R.string.friday));
		mWeekdayMap.put("Saturday", context.getString(R.string.saturday));
		mWeekdayMap.put("Sunday", context.getString(R.string.sunday));
	}
	
	public static WeatherManager getInstance() {
		return INSTANCE;
	}
	
	public static class WeatherEvent {
		public String _eventName, _eventTip;
		public int _eventLevel;
	}
	
	public static class WeatherObject {
		public String _weekday;
		public String _weatherIcon;
		public List<WeatherEvent> _weatherEventList =  new ArrayList<WeatherEvent>(2);
		/**
		 * {"events":[{"name":"加油日","desc":""}],"weekday":"Saturday","tianqi":"多云转晴","icon":"01"}
		 * @author bestjoy
		 * @throws JSONException 
		 *
		 */
		public static WeatherObject parse(JSONObject jsonObject) throws JSONException {
			WeatherObject weatherObject = new WeatherObject();
			weatherObject._weekday = jsonObject.getString("weekday");//mWeekdayMap.get(jsonObject.getString("weekday"));
			weatherObject._weatherIcon = jsonObject.getString("icon");
			JSONArray events = jsonObject.optJSONArray("events");
			if (events != null) {
				//解析天气事件
				int len = events.length();
				JSONObject eventJsonObject = null;
				for(int index=0; index<len; index++) {
					WeatherEvent weatherEvent = new WeatherEvent();
					eventJsonObject = events.getJSONObject(index);
					weatherEvent._eventName = eventJsonObject.getString("name");
					weatherEvent._eventTip = eventJsonObject.getString("desc");
					weatherEvent._eventLevel = eventJsonObject.getInt("level");
					weatherObject._weatherEventList.add(weatherEvent);
				}
			}
			return weatherObject;
		}
	}
	
	public static class WeekWeather {
		public List<WeatherObject> mWeatherObjectList = new ArrayList<WeatherObject>(7);
	}
	
	public WeekWeather getWeekWeather(JSONArray weatherArray) {
		WeekWeather weekWeather = new WeekWeather();
		int len = weatherArray.length();
		for(int index=0; index < len; index++) {
			try {
				weekWeather.mWeatherObjectList.add(WeatherObject.parse(weatherArray.getJSONObject(index)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return weekWeather.mWeatherObjectList.size() > 0 ? weekWeather : null;
	}
	
	/**
	 * 将数据缓存为文件weather.xml
	 * @param is
	 * @return
	 */
	public boolean saveWeather(InputStream is) {
		if (!mLocalWeatherCachedFile.getParentFile().exists()) {
			mLocalWeatherCachedFile.getParentFile().mkdirs();
		}
		return FilesUtils.saveFile(is, mLocalWeatherCachedFile);
	}
	/**
	 * 返回缓存的weather.xml文件
	 * @return
	 */
	public File getCachedWeatherFile() {
		return mLocalWeatherCachedFile;
	}
	
	public boolean isOldCahcedWeatherFile() {
		if (!mLocalWeatherCachedFile.exists()) {
			//如果没有缓存，那么我们就认为是需要更新天气数据
			return true;
		}
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());
		
		Calendar last = Calendar.getInstance();
		last.setTime(new Date(mLocalWeatherCachedFile.lastModified()));
		return today.get(Calendar.HOUR_OF_DAY) != last.get(Calendar.HOUR_OF_DAY);
	}
	
	public boolean isExsitedCahcedWeatherFile() {
		return mLocalWeatherCachedFile.exists();
	}

}
