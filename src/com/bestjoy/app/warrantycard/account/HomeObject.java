package com.bestjoy.app.warrantycard.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.DeviceDBHelper;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;
/**
 * 账户的家对象
 * 
 * 需要注意的是，在设计数据库的时候，有{@link HaierDBHelper#HOME_CARD_COUNT}字段，该字段会随着新增或是删除一个BaoxiuCardObject数据
 * 自动增加和减少，所以我们保存的时候不要设置他。当调用
 * @author chenkai
 *
 */
public class HomeObject implements InfoInterface{

	private static final String TAG = "HomeObject";
	public String mHomeName;
	public String mAdminCode = "";
	//住址信息
	public String mHomeProvince, mHomeCity, mHomeDis, mHomePlaceDetail;
	/**家所属账户uid*/
	public long mHomeUid = -1;
	/**住址id,对应服务器上的数据项*/
	public long mHomeAid = -1;
	/**本地_id数据字段值*/
	public long mHomeId = -1;
	public int mHomePosition;
	public boolean mIsDefault = false;
	public int mHomeCardCount;
	/**我的保修卡信息*/
	public List<BaoxiuCardObject> mBaoxiuCards = new LinkedList<BaoxiuCardObject>();
	/**表示当前数据是否过时了，如果是，那么{@link #initBaoxiuCards()}就要重新读取数据库*/
	private boolean mOutOfDate = true;
	public int mCommunityServiceLoaded = 0;
	
	private static String DEFAUL_HOME_NAME;
	/**小区id*/
	public long mHid = -1;
	/**小区名称*/
	public String mHname = "";
	
	public String getHomeTag(Context context) {
		if (TextUtils.isEmpty(mHomeName)) {
			if (DEFAUL_HOME_NAME == null) {
				DEFAUL_HOME_NAME = context.getString(R.string.default_home_name);
			}
			return DEFAUL_HOME_NAME;
		}
		return mHomeName;
	}
	
	public boolean hasCommunity() {
		return mHid > 0;
	}
	
	public HomeObject clone() {
		HomeObject newHomeObject = new HomeObject();
		newHomeObject.mHomeAid = mHomeAid;
		newHomeObject.mHomeId = mHomeId;
		newHomeObject.mHomeUid = mHomeUid;
		newHomeObject.mHomeName = mHomeName;
		newHomeObject.mHomeProvince = mHomeProvince;
		newHomeObject.mHomeCity = mHomeCity;
		newHomeObject.mHomeDis = mHomeDis;
		newHomeObject.mHomePlaceDetail = mHomePlaceDetail;
		
		newHomeObject.mHomePosition = mHomePosition;
		newHomeObject.mIsDefault = mIsDefault;
		newHomeObject.mHomeCardCount = mHomeCardCount;
		newHomeObject.mOutOfDate = true;
		newHomeObject.mHid = mHid;
		newHomeObject.mHname = mHname;
		return newHomeObject;
	}
	
	public static final String[] PROVINCE_PROJECTION = new String[]{
		DeviceDBHelper.DEVICE_PRO_ID,
		DeviceDBHelper.DEVICE_PRO_NAME,
		"_id",
	};
	
	public static final String[] CITY_PROJECTION = new String[]{
		DeviceDBHelper.DEVICE_CITY_ID,
		DeviceDBHelper.DEVICE_CITY_NAME,
		DeviceDBHelper.DEVICE_CITY_PID,
		"_id",
	};
	
	public static final String[] DISTRICT_PROJECTION = new String[]{
		DeviceDBHelper.DEVICE_HAIER_REGION_CODE,
		DeviceDBHelper.DEVICE_HAIER_COUNTRY,
		DeviceDBHelper.DEVICE_HAIER_PROVICE,
		DeviceDBHelper.DEVICE_HAIER_CITY,
		DeviceDBHelper.DEVICE_HAIER_REGION_NAME,
		DeviceDBHelper.DEVICE_HAIER_ADMIN_CODE,
	};
	
	public static final String SELECTION_PROVINCE_NAME = DeviceDBHelper.DEVICE_PRO_NAME + "=?";
	
	// city table
	public static final String SELECTION_CITY_NAME = DeviceDBHelper.DEVICE_CITY_NAME + "=?";
	
	// home table
	private static final String WHERE_HOME_ACCOUNTID = HaierDBHelper.ACCOUNT_UID + "=?";
	private static final String WHERE_HOME_ADDRESS_ID = HaierDBHelper.HOME_AID + "=?";
	public static final String WHERE_UID_AND_AID = WHERE_HOME_ACCOUNTID + " and " + WHERE_HOME_ADDRESS_ID;
	private static final String WHERE_ACCOUNT_ID_AND_HOME_ADDRESS_ID = WHERE_HOME_ACCOUNTID + " and " + WHERE_HOME_ADDRESS_ID;
	public static final String[] HOME_PROJECTION = new String[]{
		HaierDBHelper.ACCOUNT_UID,        //0
		HaierDBHelper.HOME_AID,
		HaierDBHelper.HOME_NAME,
		DeviceDBHelper.DEVICE_PRO_NAME,
		DeviceDBHelper.DEVICE_CITY_NAME,
		DeviceDBHelper.DEVICE_DIS_NAME,
		HaierDBHelper.HOME_DETAIL,           //6
		HaierDBHelper.HOME_DEFAULT,
		HaierDBHelper.POSITION,
		HaierDBHelper.ID,
		HaierDBHelper.HOME_CARD_COUNT,      //10
		HaierDBHelper.HOME_COMMUNITY_HID,   //11
		HaierDBHelper.HOME_COMMUNITY_NAME,  //12
		HaierDBHelper.HOME_COMMUNITY_SERVICE_LOADED, //13
	};
	
	public static final int KEY_HOME_UID = 0;
	public static final int KEY_HOME_AID = 1;
	public static final int KEY_HOME_NAME = 2;
	public static final int KEY_HOME_PRO_NAME = 3;
	public static final int KEY_HOME_CITY_NAME = 4;
	public static final int KEY_HOME_DIS_NAME = 5;
	public static final int KEY_HOME_DETAIL = 6;
	public static final int KEY_HOME_DEFAULT = 7;
	public static final int KEY_HOME_POSITION = 8;
	public static final int KEY_HOME_ID = 9;
	public static final int KEY_HOME_CARD_COUNT = 10;
	
	public static final int KEY_HOME_COMMUNITY_HID = 11;
	public static final int KEY_HOME_COMMUNITY_NAME = 12;
	public static final int KEY_HOME_COMMUNITY_SERVICE_LOADED = 13;
	
	public static long getProvinceId(ContentResolver cr, String provinceName) {
		if (TextUtils.isEmpty(provinceName)) {
			return -1;
		}
		long proId = -1;
		Cursor c = cr.query(BjnoteContent.Province.CONTENT_URI, PROVINCE_PROJECTION, SELECTION_PROVINCE_NAME, new String[]{provinceName}, null);
		if (c != null) {
			if (c.moveToNext()) {
				proId = c.getLong(0);
			}
			c.close();
		}
		return proId;
	}
	
	public static Cursor getProvincesLike(ContentResolver cr, String provinceNameLike) {
		if (TextUtils.isEmpty(provinceNameLike)) {
			return cr.query(BjnoteContent.Province.CONTENT_URI, PROVINCE_PROJECTION, null, null, null);
		}
		String selection = DeviceDBHelper.DEVICE_PRO_NAME + " like '" + provinceNameLike + "%'";
		return cr.query(BjnoteContent.Province.CONTENT_URI, PROVINCE_PROJECTION, selection, null, null);
	}
	public static Cursor getCitiesLike(ContentResolver cr, long proId, String cityNameLike) {
		if (proId == -1) {
			return null;
		}
		String selection = DeviceDBHelper.DEVICE_CITY_PID + "=" + proId;
		if (!TextUtils.isEmpty(cityNameLike)) {
			selection += " and " + DeviceDBHelper.DEVICE_CITY_NAME + " like '" + cityNameLike + "%'";
		}
		return cr.query(BjnoteContent.City.CONTENT_URI, CITY_PROJECTION, selection, null, null);
	}
	public static long getCityId(ContentResolver cr, String cityName) {
		if (TextUtils.isEmpty(cityName)) {
			return -1;
		}
		long proId = -1;
		Cursor c = cr.query(BjnoteContent.City.CONTENT_URI, CITY_PROJECTION, SELECTION_CITY_NAME, new String[]{cityName}, null);
		if (c != null) {
			if (c.moveToNext()) {
				proId = c.getLong(0);
			}
			c.close();
		}
		return proId;
	}
	
	public static Cursor getDistrictsLike(ContentResolver cr, long cityId, String districtNameLike) {
		if (cityId == -1) {
			return null;
		}
		String selection = DeviceDBHelper.DEVICE_DIS_CID + "=" + cityId;
		if (!TextUtils.isEmpty(districtNameLike)) {
			selection += " and " + DeviceDBHelper.DEVICE_DIS_NAME + " like '" + districtNameLike + "'%";
		}
		return cr.query(BjnoteContent.District.CONTENT_URI, DISTRICT_PROJECTION, selection, null, null);
	}
	
	public static String getDisID(ContentResolver cr, String pro, String city, String disName) {
		String selection = DeviceDBHelper.DEVICE_HAIER_PROVICE + "='" + pro + "' and " + DeviceDBHelper.DEVICE_HAIER_CITY + "='" + city + "' and " + DeviceDBHelper.DEVICE_HAIER_REGION_NAME + "='" + disName + "'";
		Cursor cursor = cr.query(BjnoteContent.HaierRegion.CONTENT_URI, DISTRICT_PROJECTION, selection, null, null);
		if(cursor.moveToNext()) {
			return cursor.getString(cursor.getColumnIndex(DeviceDBHelper.DEVICE_HAIER_ADMIN_CODE));
		}
		
		return null;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		long id = isExsited(cr,mHomeUid, mHomeAid);
		values.put(HaierDBHelper.HOME_NAME, mHomeName);
		values.put(DeviceDBHelper.DEVICE_PRO_NAME, mHomeProvince);
		values.put(DeviceDBHelper.DEVICE_CITY_NAME, mHomeCity);
		values.put(DeviceDBHelper.DEVICE_DIS_NAME, mHomeDis);
		values.put(HaierDBHelper.HOME_DETAIL, mHomePlaceDetail);
		values.put(HaierDBHelper.DATE, new Date().getTime());
		values.put(HaierDBHelper.POSITION, mHomePosition);
		values.put(HaierDBHelper.HOME_COMMUNITY_HID, mHid);
		values.put(HaierDBHelper.HOME_COMMUNITY_NAME, mHname);
		//对于家，只有位置是0的才是默认，其余的都不是, Home的uid和aid只有新增的时候会插入
		if (mHomePosition == 0) {
			values.put(HaierDBHelper.HOME_DEFAULT, 1);
		} else {
			values.put(HaierDBHelper.HOME_DEFAULT, 0);
		}
		if (id > 0) {
			int update = cr.update(BjnoteContent.Homes.CONTENT_URI, values,  WHERE_UID_AND_AID, new String[]{String.valueOf(mHomeUid), String.valueOf(mHomeAid)});
			if (update > 0) {
				DebugUtils.logD(TAG, "saveInDatebase update exsited aid#" + mHomeAid);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly update exsited aid#" + mHomeAid);
			}
		} else {
			values.put(HaierDBHelper.HOME_AID, mHomeAid);
			values.put(HaierDBHelper.ACCOUNT_UID, mHomeUid);
			Uri uri = cr.insert(BjnoteContent.Homes.CONTENT_URI, values);
			if (uri != null) {
				DebugUtils.logD(TAG, "saveInDatebase insert aid#" + mHomeAid);
				mHomeId = ContentUris.parseId(uri);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly insert aid#" + mHomeAid);
			}
		}
		return false;
	}
	
	private long isExsited(ContentResolver cr, long uid, long aid) {
		long id = -1;
		Cursor c = cr.query(BjnoteContent.Homes.CONTENT_URI, HOME_PROJECTION, WHERE_UID_AND_AID, new String[]{String.valueOf(uid), String.valueOf(aid)}, null);
		if (c != null) {
			if (c.moveToNext()) {
				id = c.getLong(KEY_HOME_AID);
			}
			c.close();
		}
		return id;
	}
	
	public static void clearHomeObjectCache() {
		synchronized(mLock) {
			mHomeObjectCache.clear();
		}
	}
	
	/**
	 * 删除某个account的全部home
	 * @param cr
	 * @param uid
	 * @return
	 */
	public static int deleteAllHomesInDatabaseForAccount(ContentResolver cr, long uid) {
		int deleted = cr.delete(BjnoteContent.Homes.CONTENT_URI, WHERE_HOME_ACCOUNTID, new String[]{String.valueOf(uid)});
		DebugUtils.logD(TAG, "deleteAllHomesInDatabaseForAccount uid#" + uid + ", delete " + deleted);
		if (deleted > 0) {
			synchronized(mLock) {
				mHomeObjectCache.clear();
			}
			
		}
		return deleted;
	}
	
	public static int deleteHomeInDatabaseForAccount(ContentResolver cr, long uid, long aid) {
		int deleted = cr.delete(BjnoteContent.Homes.CONTENT_URI, WHERE_UID_AND_AID, new String[]{String.valueOf(uid), String.valueOf(aid)});
		DebugUtils.logD(TAG, "deleteHomeInDatabaseForAccount aid#" + aid + ", delete " + deleted);
		if (deleted > 0) {
			synchronized(mLock) {
				StringBuilder sb = new StringBuilder();
				sb.append(uid).append('_').append(aid);
				mHomeObjectCache.remove(sb.toString());
			}
		}
		return deleted;
	}
	
	public static Cursor getAllHomesCursor(ContentResolver cr, long uid) {
		return cr.query(BjnoteContent.Homes.CONTENT_URI, HOME_PROJECTION, WHERE_HOME_ACCOUNTID, new String[]{String.valueOf(uid)}, HaierDBHelper.HOME_AID + " asc");
	}
	
	public static List<HomeObject> getAllHomeObjects(ContentResolver cr, long uid) {
		Cursor c = getAllHomesCursor(cr, uid);
		List<HomeObject> list = new ArrayList<HomeObject>();
		
		synchronized(mLock) {
			mHomeObjectCache.clear();
		}
		if (c != null) {
			list = new ArrayList<HomeObject>(c.getCount());
			while(c.moveToNext()) {
				list.add(getFromHomeSCursor(c, cr));
			}
			c.close();
		}
		return list;
	}
	
	private static HomeObject getFromHomeSCursor(Cursor c, ContentResolver cr) {
		HomeObject homeObject = new HomeObject();
		synchronized(mLock) {
			homeObject = new HomeObject();
			homeObject.mHomeId = c.getLong(KEY_HOME_ID);
			homeObject.mHomeUid = c.getLong(KEY_HOME_UID);
			homeObject.mHomeAid = c.getLong(KEY_HOME_AID);
			homeObject.mHomeName = c.getString(KEY_HOME_NAME);
			homeObject.mHomeProvince = c.getString(KEY_HOME_PRO_NAME);
			homeObject.mHomeCity = c.getString(KEY_HOME_CITY_NAME);
			homeObject.mHomeDis = c.getString(KEY_HOME_DIS_NAME);
			homeObject.mHomePlaceDetail = c.getString(KEY_HOME_DETAIL);
			homeObject.mHomePosition = c.getInt(KEY_HOME_POSITION);
			homeObject.mHomeCardCount = c.getInt(KEY_HOME_CARD_COUNT);
//			homeObject.mHomeCardCount = BaoxiuCardObject.getAllBaoxiuCardsCount(cr, homeObject.mHomeUid, homeObject.mHomeAid);
			homeObject.mIsDefault = c.getInt(KEY_HOME_DEFAULT) == 1;
			homeObject.mHid = c.getLong(KEY_HOME_COMMUNITY_HID);
			homeObject.mHname = c.getString(KEY_HOME_COMMUNITY_NAME);
			homeObject.mCommunityServiceLoaded = c.getInt(KEY_HOME_COMMUNITY_SERVICE_LOADED);
			
			StringBuilder sb = new StringBuilder();
			sb.append(homeObject.mHomeUid).append('_').append(homeObject.mHomeAid);
			mHomeObjectCache.put(sb.toString(), homeObject);
		}
		return homeObject;
	}
	private static final int MAX_CACHE_SIZE = 20;
	private static LinkedHashMap<String, HomeObject> mHomeObjectCache = new LinkedHashMap<String, HomeObject>(MAX_CACHE_SIZE){

		@Override
		protected boolean removeEldestEntry(Entry<String, HomeObject> eldest) {
			 return size() > MAX_CACHE_SIZE;
		}
		
	};
	private static Object mLock = new Object();
	public static HomeObject getHomeObject(ContentResolver cr, long uid, long aid) {
		StringBuilder sb = new StringBuilder();
		sb.append(uid).append('_').append(aid);
		
		synchronized(mLock) {
			if (mHomeObjectCache.containsKey(sb.toString())) {
				return mHomeObjectCache.get(sb.toString());
			}
		}
		
		Cursor c = cr.query(BjnoteContent.Homes.CONTENT_URI, HOME_PROJECTION, WHERE_ACCOUNT_ID_AND_HOME_ADDRESS_ID, new String[]{String.valueOf(uid), String.valueOf(aid)}, null);
		HomeObject homeObject = null;
		if (c != null) {
			if (c.moveToNext()) {
				homeObject = getFromHomeSCursor(c, cr);
			}
			c.close();
		}
		return homeObject;
	}
	public static HomeObject getHomeObject(Bundle bundle) {
		long aid = bundle.getLong("aid", -1);
		long uid = bundle.getLong("uid", -1);
		long hid = bundle.getLong("hid", -1);
		DebugUtils.logD(TAG, "getHomeObject() bundle = " + bundle);
		if (uid > 0 && aid > 0) {
			DebugUtils.logD(TAG, "getHomeObject() get getHomeObject from Database");
			return getHomeObject(MyApplication.getInstance().getContentResolver(), uid, aid);
		} else {
			HomeObject newHomeObject =  new HomeObject();
			newHomeObject.mHomeAid = aid;
			newHomeObject.mHomeUid = uid;
			newHomeObject.mHid = hid;
			DebugUtils.logD(TAG, "getHomeObject() new HomeObject=" + newHomeObject);
			return newHomeObject;
		}
	}
	/**
	 * 从数据库中找所有该HomeObject的保修卡，并附值给mBaoxiuCards成员
	 */
	public void initBaoxiuCards(ContentResolver cr) {
		mBaoxiuCards = BaoxiuCardObject.getAllBaoxiuCardObjects(cr, mHomeUid, mHomeAid);
		//使用数据库的数据设置保修卡的个数
		mHomeCardCount = mBaoxiuCards.size();
	}
	
	public boolean hasBaoxiuCards() {
		return mHomeCardCount > 0;
	}
	
	/**
	 * 是否有有效的地址，如果各个字段都是空的，那么我们认为丢弃该家
	 * @return
	 */
	public boolean hasValidateAddress() {
		return !TextUtils.isEmpty(mHomeName)
				|| !TextUtils.isEmpty(mHomeProvince)
				|| !TextUtils.isEmpty(mHomeCity)
				|| !TextUtils.isEmpty(mHomeDis)
				|| !TextUtils.isEmpty(mHomePlaceDetail);
	}
	
	private static HomeObject mHomeObject;
	/**
	 * 当我们设置过mHomeObject值后，需要使用这个方法来获取，这会重置mHomeObject对象为null.
	 * @return
	 */
	public static HomeObject getHomeObject() {
		HomeObject object = null;
		if (mHomeObject != null) {
			object = mHomeObject;
			mHomeObject = null;
		}
		return object;
	}
	/**
	 * 需要在Activity之间传递家对象的时候，需要调用该方法来设置，之后使用getHomeObject()来获得.
	 * @param baoxiucardObject
	 */
	public static void setHomeObject(HomeObject homeObject) {
		mHomeObject = homeObject;
	}
	
	public static long DEMO_HOME_AID = 353766;
	public static final String WHERE_HOME_AID = HaierDBHelper.HOME_AID + "=?";
	public boolean isDemoHomeObject() {
		return mHomeAid == DEMO_HOME_AID;
	}
	
	public static ContentValues getDemoHomeObjectContentValues(long uid) {
		ContentValues values = new ContentValues();
		values.put(HaierDBHelper.ACCOUNT_UID, uid);
		values.put(HaierDBHelper.HOME_AID, DEMO_HOME_AID);
		values.put(HaierDBHelper.HOME_NAME, MyApplication.getInstance().getResources().getString(R.string.demo_home));
		values.put(DeviceDBHelper.DEVICE_PRO_NAME, MyApplication.getInstance().getResources().getString(R.string.demo_home_province));
		values.put(DeviceDBHelper.DEVICE_CITY_NAME, MyApplication.getInstance().getResources().getString(R.string.demo_home_city));
		values.put(DeviceDBHelper.DEVICE_DIS_NAME, MyApplication.getInstance().getResources().getString(R.string.demo_home_district));
		values.put(HaierDBHelper.HOME_DETAIL, MyApplication.getInstance().getResources().getString(R.string.demo_home_detail));
		values.put(HaierDBHelper.HOME_DEFAULT, 0);
		values.put(HaierDBHelper.DATE, new Date().getTime());
		return values;
    }
	
	public static HomeObject getDemoHomeObject(long uid, long aid) {
		HomeObject homeObject = new HomeObject();
		homeObject.mHomeUid = uid;
		homeObject.mHomeAid = aid;
		homeObject.mHomeName = MyApplication.getInstance().getResources().getString(R.string.demo_home);
		homeObject.mHomeProvince = MyApplication.getInstance().getResources().getString(R.string.demo_home_province);
		homeObject.mHomeCity = MyApplication.getInstance().getResources().getString(R.string.demo_home_city);
		homeObject.mHomeDis = MyApplication.getInstance().getResources().getString(R.string.demo_home_district);
		homeObject.mHomePlaceDetail = MyApplication.getInstance().getResources().getString(R.string.demo_home_detail);
		homeObject.mIsDefault = false;
		return homeObject;
	}
	/**
	 * 删除指定aid的假数据
	 * @param cr
	 * @param aid
	 * @return
	 */
	public static boolean deleteDemoHomeObject(ContentResolver cr, long uid, long aid) {
		boolean deleted = cr.delete(BjnoteContent.Homes.CONTENT_URI, WHERE_UID_AND_AID, new String[]{String.valueOf(uid), String.valueOf(aid)}) > 0;
		DebugUtils.logD(TAG, "deleteDemoHomeObject() delete Homes for uid=" + uid + ", aid="+ aid);
		if (deleted) {
			DebugUtils.logD(TAG, "deleteDemoHomeObject() delete BaoxiuCards for uid=" + uid + ", aid="+ aid);
			cr.delete(BjnoteContent.BaoxiuCard.CONTENT_URI, BaoxiuCardObject.WHERE_UID_AND_AID, new String[]{String.valueOf(uid), String.valueOf(aid)});
		}
		return deleted;
	}
	/**
	 * 返回所有的小区
	 * @return
	 */
	public static List<HomeObject> getAllCommunities() {
		List<HomeObject> list = new ArrayList<HomeObject>();
		if (MyAccountManager.getInstance().hasHomes()) {
			for(HomeObject homeObject : MyAccountManager.getInstance().getAccountObject().mAccountHomes) {
				if (homeObject.hasCommunity()) {
					list.add(homeObject);
				}
			}
		}
		return list;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("HomeObject[")
		.append("uid=").append(mHomeUid)
		.append(", Hname=").append(mHname)
		.append(", aid=").append(mHomeAid)
		.append(", hid=").append(mHid)
		.append(", homeName=").append(mHomeName)
		.append(", placeDetail=").append(mHomePlaceDetail)
		.append(", isDefault=").append(mIsDefault)
		.append("]");
		return sb.toString();
	}
	
	public String toFriendString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mHomeCity)
		.append(" ").append(mHomeDis)
		.append(" ").append(mHomePlaceDetail);
		return sb.toString();
	}
	
}
