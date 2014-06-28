package com.bestjoy.app.warrantycard.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shwy.bestjoy.utils.DebugUtils;

/**
 * @author Sean Owen
 * @author chenkai
 */
public final class HaierDBHelper extends SQLiteOpenHelper {
private static final String TAG = "HaierDBHelper";
  private static final int DB_VERSION = 3;
  private static final String DB_NAME = "cncom.db";
  public static final String ID = "_id";
  /**0为可见，1为删除，通常用来标记一条数据应该被删除，是不可见的，包含该字段的表查询需要增加deleted=0的条件*/
  public static final String FLAG_DELETED = "deleted";
  public static final String DATE = "date";
  //account table
  public static final String TABLE_NAME_ACCOUNTS = "accounts";
  /**用户唯一识别码*/
  public static final String ACCOUNT_UID = "uid";
  public static final String ACCOUNT_DEFAULT = "isDefault";
  public static final String ACCOUNT_TEL = "tel";
  public static final String ACCOUNT_NAME = "name";
  public static final String ACCOUNT_PWD = "password";
  public static final String ACCOUNT_HOME_COUNT = "home_count";
  /**我的卡片的个数*/
  public static final String ACCOUNT_MYCARD_COUNT = "mycard_count";
  public static final String ACCOUNT_PHONES = "phones";

  public static final String ACCOUNT_HAS_PHOTO = "hasPhoto";
  
  //home table
  public static final String TABLE_NAME_HOMES = "homes";
  /**地址id,每个地址的id,这个目前没用,要是更改地址的话可能会用到*/
  public static final String HOME_AID = "aid";
  public static final String HOME_NAME = "name";
  /**详细地址*/
  public static final String HOME_DETAIL = "home_detail";
  public static final String HOME_DEFAULT = "isDefault";
  /**我的家的保修卡个数*/
  public static final String HOME_CARD_COUNT = "baoxiucard_count";
  /**我的家TAB位置,用户可以调整顺序*/
  public static final String POSITION = "position";
  
  //cards table
  public static final String TABLE_NAME_CARDS = "cards";
  /**所属家*/
  public static final String CARD_AID = "aid";
  /**所属账户*/
  public static final String CARD_UID = "uid";
  /**保修卡服务器id*/
  public static final String CARD_BID = "bid";
  /**名称*/
  public static final String CARD_NAME = "name";
  /**设备类别，比如大类是电视剧*/
  public static final String CARD_TYPE = "LeiXin";
  /**品牌*/
  public static final String CARD_PINPAI = "PinPai";
  /**商品编号*/
  public static final String CARD_SERIAL = "SHBianHao";
  /**型号*/
  public static final String CARD_MODEL = "XingHao";
  /**保修电话*/
  public static final String CARD_BXPhone = "BXPhone";
  /**发票路径*/
  public static final String CARD_FPaddr = "FPaddr";
  /**购买价格*/
  public static final String CARD_PRICE = "BuyPrice";
  /**购买日期*/
  public static final String CARD_BUT_DATE = "BuyDate";
  /**购买途径*/
  public static final String CARD_BUY_TUJING = "BuyTuJing";
  /**延保时间*/
  public static final String CARD_YANBAO_TIME = "YanBaoTime";
  /**延保单位*/
  public static final String CARD_YANBAO_TIME_COMPANY = "YanBaoDanWei";
  /**整机保修时间*/
  public static final String CARD_WY = "wy";
  /**延保电话*/
  public static final String CARD_YBPhone = "YBPhone";
  /**KY编码*/
  public static final String CARD_KY = "ky";
  /**延保单位电话*/
  public static final String CARD_YANBAO_TIME_COPMANY_TEL = "YanBaoDanWeiCommanyTel";
  /**整机保修，目前不定义*/
  public static final String DEVICE_WARRANTY_PERIOD = "warranty_period";
  /**配件保修*/
  public static final String CARD_COMPONENT_VALIDITY = "component_validity";
  
  //这里是设备表的扩展，如遥控器
  
  
  /**型号数据表，这个我们会新增到预置的device.db数据库文件中*/
  public static final String TABLE_NAME_DEVICE_XINGHAO = "xinghao";
  /**品牌五位code码，用来过滤数据用的*/
  public static final String DEVICE_XINGHAO_PCODE = "pcode";
  public static final String DEVICE_XINGHAO_MN = "MN";
  public static final String DEVICE_XINGHAO_KY = "KY";
  public static final String DEVICE_XINGHAO_WY = "WY";
  
  // Qrcode scan part begin
  public static final String TABLE_SCAN_NAME = "history";
  public static final String ID_COL = "id";
  public static final String TEXT_COL = "text";
  public static final String FORMAT_COL = "format";
  public static final String DISPLAY_COL = "display";
  public static final String TIMESTAMP_COL = "timestamp";
  public static final String DETAILS_COL = "details";
  // Qrcode scan part end
  
  // MyCard begin
  public static final String TABLE_NAME_MY_CARD = "mycard";
  public static final String CARD_ACCOUNT_PWD = "account_pwd";
  public static final String CONTACT_ID = "_id";
  public static final String CONTACT_NAME="name";
  public static final String CONTACT_TEL="tel";
  public static final String CONTACT_BID="bid";
  public static final String CONTACT_DATE="date";
  public static final String CONTACT_NOTE="note";
  public static final String CONTACT_ORG="org";
  public static final String CONTACT_EMAIL="email";
  public static final String CONTACT_ADDRESS = "address";
  /**通常一些表中本身就有如tel的数据，如果需要使用其他的如tel数据来查询，那么可以使用该字段*/
  public static final String CONTACT_FILTER="filter";
  public static final String CONTACT_TITLE="title";
  public static final String CONTACT_PASSWORD="password";
  public static final String CONTACT_TYPE="type";
  public static final String CARD_ACCOUNT_MD = ACCOUNT_UID;
  /**名片是否有头像*/
  public static final String CONTACT_HAS_PHOTO = "has_photo";
  //MyCard end
  
//生活圈开始
  /**生活圈主表*/
  public static final String TABLE_NAME_MYLIFE = "mylife";
  /**消费记录表名*/
  public static final String TABLE_NAME_MYLIFE_CONSUME = "mylife_consume";
  /**商家电话*/
  public static final String MYLIFE_COM_CELL = "comCell";
  /**商家网址*/
  public static final String MYLIFE_COM_WEBSITE = "website";
  /**商家最新优惠活动*/
  public static final String MYLIFE_COM_NEWS = "news";
  /**我的消费备注*/
  public static final String MYLIFE_COM_XIAOFEI_NOTES = "xiaofeibeizhu";
  /**我的消费记录*/
  public static final String MYLIFE_COM_XF = "xiaofeijf";
  
  /**我的总消费积分*/
  public static final String MYLIFE_TOTAL_JF = "total_jf";
  /**我的可用消费积分*/
  public static final String MYLIFE_FREE_JF = "free_jf";
  /**广告*/
  public static final String MYLIFE_GUANGGAO = "guanggao";
  //生活圈结束
  
  //维修点开始
  /**维修点主表*/
  public static final String TABLE_NAME_MAINTENCE_POINT = "maintence_point";
  /**维修点名称*/
  public static final String MAINTENCE_POINT_NAME = "name";
  /**维修点坐标id*/
  public static final String MAINTENCE_POINT_LOCATION_ID = "location_id";
  /**维修点地址*/
  public static final String MAINTENCE_POINT_ADDRESS = "address";
  /**维修点street_id*/
  public static final String MAINTENCE_POINT_STREET_ID = "street_id";
  /**维修点电话*/
  public static final String MAINTENCE_POINT_TEL = "telephone";
  /**uid*/
  public static final String MAINTENCE_POINT_UID = "uid";
  /**aid*/
  public static final String MAINTENCE_POINT_AID = "aid";
  /**bid*/
  public static final String MAINTENCE_POINT_BID = "bid";
  /**维修点详细信息id*/
  public static final String MAINTENCE_POINT_DETAIL_INFO_ID = "detail_info_id";
  public static final String MAINTENCE_POINT_LOCATION_LAT = "lat";
  public static final String MAINTENCE_POINT_LOCATION_LNG = "lng";
  public static final String MAINTENCE_POINT_DISTANCE = "distance";
  public static final String MAINTENCE_POINT_TYPE = "type";//life
  public static final String MAINTENCE_POINT_TAG = "tag";//八佰伴
  public static final String MAINTENCE_POINT_DETAIL_URL = "detail_url";
  public static final String MAINTENCE_POINT_OVERALL_RATING = "overall_rating";
  public static final String MAINTENCE_POINT_IMAG_NUM = "image_num";
  public static final String MAINTENCE_POINT_COMMENT_NUM = "comment_num";
  //维修点结束
  
  public HaierDBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }
  
  private SQLiteDatabase mWritableDatabase;
  private SQLiteDatabase mReadableDatabase;
  
  public synchronized SQLiteDatabase openWritableDatabase() {
	  if (mWritableDatabase == null) {
		  mWritableDatabase = getWritableDatabase();
	  }
	  return mWritableDatabase;
  }
  
  public synchronized SQLiteDatabase openReadableDatabase() {
	  if (mReadableDatabase == null) {
		  mReadableDatabase = getReadableDatabase();
	  }
	  return mReadableDatabase;
  }
  
  public synchronized void closeReadableDatabase() {
	  if (mReadableDatabase != null && mReadableDatabase.isOpen()) {
		  mReadableDatabase.close();
		  mReadableDatabase = null;
	  }
  }
  
  public synchronized void closeWritableDatabase() {
	  if (mWritableDatabase != null && mWritableDatabase.isOpen()) {
		  mWritableDatabase.close();
		  mWritableDatabase = null;
	  }
  }
  
  public synchronized void closeDatabase() {
	  closeReadableDatabase();
	  closeWritableDatabase();
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
      DebugUtils.logD(TAG, "onCreate");
   
       // Create Account table
  	   createAccountTable(sqLiteDatabase);
  		//Create Homes table
  		createHomesTable(sqLiteDatabase);
  		// Create devices table
  		createBaoxiuCardsTable(sqLiteDatabase);
  		// Create scan history
  		createScanHistory(sqLiteDatabase);
  		
  		createXinghaoTable(sqLiteDatabase);
  		
  		createCardTable(sqLiteDatabase);
  	    //在我的名片表上增加插入删除触发器，以便同步更新账户表的card_count字段
  		createTriggerForMyCardTable(sqLiteDatabase);
  		
  		//创建维修点本地缓存数据
  		createMaintenancePointTable(sqLiteDatabase);
  	    //增加生活圈
  		createMyLifeTable(sqLiteDatabase);
  		
  }
  
  private void createTriggerForAccountTable(SQLiteDatabase sqLiteDatabase) {
	  String sql = "CREATE TRIGGER insert_account" + " BEFORE INSERT " + " ON " + TABLE_NAME_ACCOUNTS + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET isDefault = 0 WHERE uid != new.uid and isDefault = 1; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER update_default_account" + " BEFORE UPDATE OF isDefault " + " ON " + TABLE_NAME_ACCOUNTS + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET isDefault = 0 WHERE uid != old.uid and isDefault = 1; END;";
	  sqLiteDatabase.execSQL(sql);
	  
  }
  
  private void createTriggerForHomeTable(SQLiteDatabase sqLiteDatabase) {
	  String sql = "CREATE TRIGGER insert_home_update_account" + " AFTER INSERT " + " ON " + TABLE_NAME_HOMES + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET home_count = home_count+1 WHERE uid = new.uid; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER delete_home_update_account" + " AFTER DELETE " + " ON " + TABLE_NAME_HOMES + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET home_count = home_count-1 WHERE uid = old.uid; END;";
	  sqLiteDatabase.execSQL(sql);
	
  }
  
  private void createTriggerForBaoxiuCardsTable(SQLiteDatabase sqLiteDatabase) {
	  String sql = "CREATE TRIGGER insert_cards_update_home" + " AFTER INSERT " + " ON " + TABLE_NAME_CARDS + 
			  " BEGIN UPDATE " + TABLE_NAME_HOMES + " SET baoxiucard_count = baoxiucard_count+1 WHERE aid = new.aid; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER delete_card_update_home" + " AFTER DELETE " + " ON " + TABLE_NAME_CARDS + 
			  " BEGIN UPDATE " + TABLE_NAME_HOMES + " SET baoxiucard_count = baoxiucard_count-1 WHERE aid = old.aid; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER insert_cards_update_account" + " AFTER INSERT " + " ON " + TABLE_NAME_CARDS + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET baoxiucard_count = baoxiucard_count+1 WHERE uid = new.uid; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER delete_card_update_account" + " AFTER DELETE " + " ON " + TABLE_NAME_CARDS + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET baoxiucard_count = baoxiucard_count-1 WHERE uid = old.uid; END;";
	  sqLiteDatabase.execSQL(sql);
	
  }
  
  
  private void createAccountTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_ACCOUNTS + " (" +
	            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            ACCOUNT_UID + " INTEGER NOT NULL DEFAULT 0, " +
	            ACCOUNT_TEL + " TEXT, " +
	            ACCOUNT_PWD + " TEXT, " +
	            ACCOUNT_DEFAULT + " INTEGER NOT NULL DEFAULT 1, " +
	            ACCOUNT_HOME_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	            ACCOUNT_MYCARD_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	            HOME_CARD_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	            ACCOUNT_NAME + " TEXT, " +
	            ACCOUNT_PHONES  + " TEXT, " +
	            ACCOUNT_HAS_PHOTO + " INTEGER NOT NULL DEFAULT 0, " +
	            DATE + " TEXT" +
	            ");");
	  createTriggerForAccountTable(sqLiteDatabase);
  }
  
  private void createHomesTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_HOMES + " (" +
	            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            ACCOUNT_UID + " INTEGER NOT NULL DEFAULT 0, " +
	            HOME_AID + " INTEGER, " +
	            HOME_NAME + " TEXT, " +
	            HOME_CARD_COUNT + " INTEGER NOT NULL DEFAULT 0, " +
	            DeviceDBHelper.DEVICE_PRO_NAME + " TEXT, " +
	            DeviceDBHelper.DEVICE_CITY_NAME + " TEXT, " +
	            DeviceDBHelper.DEVICE_DIS_NAME + " TEXT, " +
	            HOME_DETAIL + " TEXT, " +
	            HOME_DEFAULT + " INTEGER NOT NULL DEFAULT 1, " +
	            POSITION + " INTEGER NOT NULL DEFAULT 1, " +
	            DATE + " TEXT" +
	            ");");
	  createTriggerForHomeTable(sqLiteDatabase);
  }
  
  private void createBaoxiuCardsTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_CARDS + " (" +
	            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            CARD_UID + " INTEGER NOT NULL DEFAULT 0, " +  //账户id
	            CARD_AID + " INTEGER, " +     //家id
	            CARD_BID + " INTEGER, " +     //保修卡服务器id
	            CARD_TYPE + " TEXT, " +
	            CARD_NAME + " TEXT, " +
	            CARD_PINPAI + " TEXT, " +
	            CARD_MODEL + " TEXT, " +
	            CARD_SERIAL + " TEXT, " +
	            CARD_BXPhone + " TEXT, " +
	            CARD_FPaddr + " TEXT, " +
	            CARD_BUT_DATE + " TEXT, " +
	            CARD_PRICE + " TEXT, " +
	            CARD_BUY_TUJING + " TEXT, " +
	            CARD_WY + " TEXT, " +
	            CARD_YBPhone + " TEXT, " +
	            CARD_KY + " TEXT, " +
	            CARD_YANBAO_TIME + " TEXT, " +
	            CARD_YANBAO_TIME_COMPANY + " TEXT, " +
	            CARD_YANBAO_TIME_COPMANY_TEL + " TEXT, " +
	            DEVICE_WARRANTY_PERIOD + " TEXT, " +
	            CARD_COMPONENT_VALIDITY + " TEXT, " +
	            DATE + " TEXT" +
	            ");");
	  createTriggerForBaoxiuCardsTable(sqLiteDatabase);
  }
  
  private void createScanHistory(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_SCAN_NAME + " (" +
	            ID_COL + " INTEGER PRIMARY KEY, " +
	            TEXT_COL + " TEXT, " +
	            FORMAT_COL + " TEXT, " +
	            DISPLAY_COL + " TEXT, " +
	            TIMESTAMP_COL + " INTEGER, " +
	            DETAILS_COL + " TEXT);");
  }
  
  private void createXinghaoTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_DEVICE_XINGHAO + " (" +
	            ID + " INTEGER PRIMARY KEY, " +
	            DEVICE_XINGHAO_PCODE + " TEXT, " +
	            DEVICE_XINGHAO_MN + " TEXT, " +
	            DEVICE_XINGHAO_KY + " TEXT, " +
	            DEVICE_XINGHAO_WY + " TEXT, " +
	            DATE + " TEXT);");
  }
  
  private void createCardTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_MY_CARD + " (" +
	            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            ACCOUNT_UID + " INTEGER NOT NULL DEFAULT 0, " +
	            CARD_ACCOUNT_PWD + " TEXT, " +
	            CONTACT_NAME + " TEXT, " +
	            CONTACT_TEL + " TEXT, " +
	            CONTACT_BID + " TEXT, " +
	            CONTACT_EMAIL + " TEXT, " +
	            CONTACT_ADDRESS + " TEXT, " +
	            CONTACT_ORG + " TEXT, " +
	            CONTACT_DATE + " TEXT, " +
	            CONTACT_PASSWORD + " TEXT, " +
	            CONTACT_NOTE + " TEXT, " +
	            CONTACT_TYPE + " TEXT, " +
	            CONTACT_HAS_PHOTO + " INTEGER NOT NULL DEFAULT 0, " +
	            CONTACT_TITLE + " TEXT" +
	            ");");
  }
  private void createMyLifeTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_MYLIFE + " (" +
	            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            MYLIFE_COM_CELL + " TEXT, " +     //商家电话
	            CONTACT_BID + " TEXT, " +         //商家MM
	            MYLIFE_GUANGGAO + " TEXT, " +     //商家广告
	            MYLIFE_COM_WEBSITE + " TEXT, " + //商家网址
	            CONTACT_ADDRESS + " TEXT, " +  //商家地址
	            CONTACT_NAME + " TEXT, " +     //商家名称
	            MYLIFE_COM_NEWS + " TEXT, " +  //商家最新优惠信息
	            CONTACT_TEL + " TEXT, " +  //消费者默认手机号码
	            ACCOUNT_UID + " INTEGER NOT NULL DEFAULT 0, " +  //账号id
	            MYLIFE_COM_XIAOFEI_NOTES + " TEXT, " +  //消费者消费备注
	            MYLIFE_COM_XF + " TEXT, " +  //消费者消费记录
	            MYLIFE_TOTAL_JF + " TEXT, " +  
	            MYLIFE_FREE_JF + " TEXT, " + 
	            CONTACT_DATE + " TEXT" +
	            ");");
  }
  
  private void createMaintenancePointTable(SQLiteDatabase sqLiteDatabase) {
	  sqLiteDatabase.execSQL(
	            "CREATE TABLE " + TABLE_NAME_MAINTENCE_POINT + " (" +
	            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
	            MAINTENCE_POINT_AID + " TEXT, " +
	            MAINTENCE_POINT_BID + " TEXT, " +
	            MAINTENCE_POINT_NAME + " TEXT, " +
	            MAINTENCE_POINT_LOCATION_ID + " TEXT, " +
	            MAINTENCE_POINT_ADDRESS + " TEXT, " +
	            MAINTENCE_POINT_STREET_ID + " TEXT, " +
	            MAINTENCE_POINT_TEL + " TEXT, " +
	            MAINTENCE_POINT_UID + " TEXT, " +
	            MAINTENCE_POINT_DETAIL_INFO_ID + " TEXT, " +
	            MAINTENCE_POINT_LOCATION_LAT + " TEXT, " +
	            MAINTENCE_POINT_LOCATION_LNG + " TEXT, " +
	            MAINTENCE_POINT_DISTANCE + " TEXT, " +
	            MAINTENCE_POINT_TYPE + " TEXT, " +
	            MAINTENCE_POINT_TAG + " TEXT, " +
	            MAINTENCE_POINT_DETAIL_URL + " TEXT, " +
	            MAINTENCE_POINT_OVERALL_RATING + " TEXT, " +
	            MAINTENCE_POINT_IMAG_NUM + " TEXT, " +
	            MAINTENCE_POINT_COMMENT_NUM + " TEXT" +
	            ");");
  }
  
  private void createTriggerForMyCardTable(SQLiteDatabase sqLiteDatabase) {
	  String sql = "CREATE TRIGGER insert_contact_mycard" + " AFTER INSERT " + " ON " + TABLE_NAME_MY_CARD + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET mycard_count = mycard_count+1 WHERE uid = new.uid; END;";
	  sqLiteDatabase.execSQL(sql);
	  
	  sql = "CREATE TRIGGER delete_contact_mycard" + " AFTER DELETE " + " ON " + TABLE_NAME_MY_CARD + 
			  " BEGIN UPDATE " + TABLE_NAME_ACCOUNTS + " SET mycard_count = mycard_count-1 WHERE uid = old.uid; END;";
	  sqLiteDatabase.execSQL(sql);
  }
  
  private void addTextColumn(SQLiteDatabase sqLiteDatabase, String table, String column) {
	    String alterForTitleSql = "ALTER TABLE " + table +" ADD " + column + " TEXT";
		sqLiteDatabase.execSQL(alterForTitleSql);
  }
  private void addIntColumn(SQLiteDatabase sqLiteDatabase, String table, String column, int defaultValue) {
	    String alterForTitleSql = "ALTER TABLE " + table +" ADD " + column + " INTEGER NOT NULL DEFAULT " + defaultValue;
		sqLiteDatabase.execSQL(alterForTitleSql);
}

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
	  DebugUtils.logD(TAG, "onUpgrade oldVersion " + oldVersion + " newVersion " + newVersion);
	  if (newVersion <= 2) {
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ACCOUNTS);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_HOMES);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CARDS);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SCAN_NAME);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_DEVICE_XINGHAO);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MYLIFE);
		    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MY_CARD);
		    
		    
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "insert_account");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "update_default_account");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "insert_home_update_account");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "delete_home_update_account");
		    
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "insert_cards_update_home");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "delete_card_update_home");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "insert_cards_update_account");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "delete_cards_update_account");
		    
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "insert_contact_mycard");
		    sqLiteDatabase.execSQL("DROP TRIGGER IF EXISTS " + "delete_contact_mycard");
		    
		    
		    onCreate(sqLiteDatabase);
		    return;
		} 
	  
	  if (oldVersion == 2) {
		  //版本2我们增加了品牌数据表的wy字段
		  addTextColumn(sqLiteDatabase, TABLE_NAME_DEVICE_XINGHAO, DEVICE_XINGHAO_WY);
		  oldVersion = 3;
	  }
  }
}
