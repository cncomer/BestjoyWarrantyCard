package com.bestjoy.app.haierwarrantycard.database;

import com.bestjoy.app.haierwarrantycard.MyApplication;

public class DeviceDBHelper {

	public static final int VERSION = 3;
	public static final String KEY_VERSION = "version";
	//设备数据库
	  public static final String DB_DEVICE_NAME = "device.db";
	  public static final String TABLE_NAME_DEVICE_DALEI = "DaLei";
	  public static final String DEVICE_DALEI_NAME = "Name";
	  public static final String DEVICE_DALEI_ID = "ID";
	  
	  public static final String TABLE_NAME_DEVICE_XIAOLEI = "XiaoLei";
	  public static final String DEVICE_XIALEI_DID = "DID";
	  public static final String DEVICE_XIALEI_XID = "XID";
	  public static final String DEVICE_XIALEI_NAME = "XName";
	  
	  public static final String TABLE_NAME_DEVICE_PINPAI = "PinPai";
	  public static final String DEVICE_PINPAI_XID = "XID";
	  public static final String DEVICE_PINPAI_PID = "PID";
	  public static final String DEVICE_PINPAI_NAME = "PName";
	  public static final String DEVICE_PINPAI_PINYIN = "PinYin";
	  public static final String DEVICE_PINPAI_CODE = "Code";
	  public static final String DEVICE_PINPAI_BXPHONE = "TEL";
	  
	  public static final String TABLE_NAME_DEVICE_CITY_ = "T_City";
	  public static final String DEVICE_CITY_ID = "CityID";
	  public static final String DEVICE_CITY_NAME = "CityName";
	  public static final String DEVICE_CITY_PID = "ProID";
	  public static final String DEVICE_CITY_SORT = "CitySort";
	  
	  public static final String TABLE_NAME_DEVICE_DISTRICT_ = "T_District";
	  public static final String DEVICE_DIS_ID = "Id";
	  public static final String DEVICE_DIS_NAME = "DisName";
	  public static final String DEVICE_DIS_CID = "CityID";
	  public static final String DEVICE_DIS_DISSORT = "DisSort";
	  
	  public static final String TABLE_NAME_DEVICE_PROVINCE = "T_Province";
	  public static final String DEVICE_PRO_ID = "ProID";
	  public static final String DEVICE_PRO_NAME = "ProName";
	  public static final String DEVICE_PRO_SORT = "ProSort";
	  public static final String DEVICE_PRO_REMARK = "ProRemark";
	  
	  public static boolean isNeedReinstallDeviceDatabase() {
		  return VERSION > MyApplication.getInstance().mPreferManager.getInt(KEY_VERSION, 0);
	  }
}