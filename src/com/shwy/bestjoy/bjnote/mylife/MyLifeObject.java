package com.shwy.bestjoy.bjnote.mylife;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.shwy.bestjoy.contacts.AddrBookUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;

public class MyLifeObject implements InfoInterface {
	public static final String TAG = "MyLifeObject";
	
	public static final String[] PROJECTION = new String[]{
		HaierDBHelper.ID,                  //0
		HaierDBHelper.ACCOUNT_UID,          //1
		HaierDBHelper.MYLIFE_SHOP_ID,          //2
		HaierDBHelper.CONTACT_NAME,          //3
		HaierDBHelper.MYLIFE_COM_CELL,          //4
		HaierDBHelper.MYLIFE_GUANGGAO,          //5
		HaierDBHelper.MYLIFE_COM_WEBSITE,          //6
		HaierDBHelper.MYLIFE_COM_PROVINCE,          //7
		HaierDBHelper.MYLIFE_COM_CITY,          //8
		HaierDBHelper.MYLIFE_COM_DIST,          //9
		HaierDBHelper.CONTACT_ADDRESS,          //10
		HaierDBHelper.MYLIFE_LANG,          //11
		HaierDBHelper.MYLIFE_LAT,          //12
		HaierDBHelper.MYLIFE_COM_NEWS,          //13
		HaierDBHelper.MYLIFE_TOTAL_JF,          //14
		HaierDBHelper.MYLIFE_FREE_JF,          //15
		HaierDBHelper.MYLIFE_MEMBER_CARD_IMAGE_ADDR,          //16
		HaierDBHelper.MYLIFE_SHOP_IMAGE_ADDR,          //17
		HaierDBHelper.CONTACT_DATE,          //18
		HaierDBHelper.MYLIFE_COM_CELL_RAW,   //19
		
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	public static final int INDEX_SID = 2;
	public static final int INDEX_SHOP_NAME = 3;
	public static final int INDEX_SHOP_TEL = 4;
	public static final int INDEX_SHOP_GUANGGAO = 5;
	public static final int INDEX_SHOP_WEBSITE = 6;
	public static final int INDEX_SHOP_PROVINCE = 7;
	public static final int INDEX_SHOP_CITY = 8;
	public static final int INDEX_SHOP_DIST = 9;
	public static final int INDEX_SHOP_ADDRESS = 10;
	public static final int INDEX_SHOP_LONG = 11;
	public static final int INDEX_SHOP_LAT = 12;
	public static final int INDEX_SHOP_NEWS = 13;
	public static final int INDEX_SHOP_TOTAL_JF = 14;
	public static final int INDEX_SHOP_FREE_JF = 15;
	public static final int INDEX_SHOP_MEMBER_CARD_IAMGE = 16;
	public static final int INDEX_SHOP_IMAGE = 17;
	public static final int INDEX_DATE = 18;
	public static final int INDEX_SHOP_TEL_RAW = 19;
	
	public static final String ID_WHERE = PROJECTION[INDEX_ID] +"=?";
	public static final String SHOP_ID_WHERE = PROJECTION[INDEX_SID] +"=?";
	public static final String ACCOUNT_UID_AND_SHOPID_WHERE = PROJECTION[INDEX_UID] +"=? and " + SHOP_ID_WHERE;
	
	public static final String SELECTION_UID = PROJECTION[INDEX_UID] +"=?";
	/**活动日期格式yyyy-MM-dd*/
	public static  DateFormat XF_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //2013/8/3 7:26:46
	public long mId = -1;
	/**当前账号*/
	public long mAccountUID = -1;
	/**店家id*/
	public long mShopID = -1;
	/**公司电话*/
	public String[] mComCell = new String[]{};
	/**公司名称*/
	public String mComName = "";
	/**公司所在省*/
	public String mComProv = "";
	/**公司所在市*/
	public String mComCity = "";
	/**公司所在区*/
	public String mComDist = "";
	/**公司地址*/
	public String mAddress = "";
	/**公司地址经度*/
	public String mLongitude = "";
	/**公司地址纬度*/
	public String mLatitude = "";
	/**公司网址*/
	public String mWebsite = "";
	/**最新信息*/
	public String mNewsNote = "";
	/**店家照片*/
	public String mShopImage = "";
	/**会员卡预览*/
	public String mMemberCardImage = "";
	public long mDate = -1;
	/**广告*/
	public String mLiveInfo = "";
	/**总消费积分*/
    public String mTotalXiaofeiJifen="0";
    /**可用积分*/
    public String mFreeJifen="0";
    /**形如xxx,xxxx,xxxx*/
    public String mComCellRawStr = "";
	/**我的消费记录情况*/
	MyLifeConsumeRecordsObjectHolder mMyLifeConsumeRecordsObjectHolder = new MyLifeConsumeRecordsObjectHolder();
	
	public class MyLifeConsumeRecordsObjectHolder {
		/**总金额*/
	    public String mTotalMoney;
	    /**可用积分*/
	    public String mTotaljifen;
		/**我的消费记录*/
		public List<MyLifeConsumeRecordsObject> mMyLifeConsumeRecordsObjectList = new LinkedList<MyLifeConsumeRecordsObject>();
		/**
		 * 解析字串得到消费记录列表
		 * @param consumeRecordsString
		 * @return 如果没有找到消费记录，则返回null
		 */
		public List<MyLifeConsumeRecordsObject> populateConsumeRecords(String consumeRecordsString) {
			List<MyLifeConsumeRecordsObject> list = MyLifeConsumeRecordsObject.parse(consumeRecordsString);
			return list;
		}
		
		public boolean hasRecords() {
			return mMyLifeConsumeRecordsObjectList != null && mMyLifeConsumeRecordsObjectList.size() > 0;
		}
	}
	public static String getShopImagePhotoIdFromAddr(String shopImageAddr) {
		String photoId = "";
		int indexStart = shopImageAddr.lastIndexOf("/");
		int indexEnd = shopImageAddr.lastIndexOf(".");
		if (indexStart > 0 && indexEnd > 0) {
			photoId =  shopImageAddr.substring(indexStart+1, indexEnd) + ".shop";
		} else {
			photoId = shopImageAddr.hashCode() + ".shop";
		}
		DebugUtils.logD(TAG, "getShopImagePhotoIdFromAddr(String shopImageAddr) photoId" + photoId + " from shopImageAddr " + shopImageAddr);
		return photoId;
	}
	
	public static String getMemberCardImagePhotoIdFromAddr(String memberCardImageAddr) {
		String photoId = "";
		int indexStart = memberCardImageAddr.lastIndexOf("/");
		int indexEnd = memberCardImageAddr.lastIndexOf(".");
		if (indexStart > 0 && indexEnd > 0) {
			photoId =  memberCardImageAddr.substring(indexStart+1, indexEnd) + ".membercard";
		} else {
			photoId = memberCardImageAddr.hashCode() + ".membercard";
		}
		DebugUtils.logD(TAG, "getShopImagePhotoIdFromAddr(String shopImageAddr) photoId" + photoId + " from shopImageAddr " + memberCardImageAddr);
		return photoId;
	}
	/**存储在外置存储卡*/
	public static File getImageCachedFile(String photoId) {
		return new File(MyApplication.getInstance().getExternalStorageCacheRoot("MemberCard"), photoId);
	}
	
	public static AddressBookParsedResult toAddressBookParsedResult(MyLifeObject myLifeObject) {
		byte[] shopImageByte = null;
		File shopImageFile = getImageCachedFile(getShopImagePhotoIdFromAddr(myLifeObject.mShopImage));
		if (shopImageFile.exists()) {
			try {
				Bitmap shopImage = BitmapFactory.decodeStream(new FileInputStream(shopImageFile));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				shopImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				shopImageByte = baos.toByteArray();
				shopImage.recycle();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		AddressBookParsedResult addressBookParsedResult = new AddressBookParsedResult(new String[]{myLifeObject.mComName}, 
				null, myLifeObject.mComCell, null, null, new String[]{myLifeObject.mAddress}, myLifeObject.mComName, null, myLifeObject.mComName, null, shopImageByte, null, null, null);
		return addressBookParsedResult;
	}
	
	public Uri saveToContact() {
		return AddrBookUtils.getInstance().createContactEntry(toAddressBookParsedResult(this));
	}
	
    public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
    	ContentValues values = new ContentValues(11);
    	if (addtion != null) {
    		values.putAll(addtion);
    	}
		values.put(PROJECTION[INDEX_UID], mAccountUID);
		values.put(PROJECTION[INDEX_SID], mShopID);
		values.put(PROJECTION[INDEX_SHOP_NAME], mComName);
		values.put(PROJECTION[INDEX_SHOP_TEL], convertShopCellsToString());
		values.put(PROJECTION[INDEX_SHOP_TEL_RAW], mComCellRawStr);
		
		values.put(PROJECTION[INDEX_SHOP_GUANGGAO], mLiveInfo);
		values.put(PROJECTION[INDEX_SHOP_WEBSITE], mWebsite);
		values.put(PROJECTION[INDEX_SHOP_PROVINCE], mComProv);
		values.put(PROJECTION[INDEX_SHOP_CITY], mComCity);
		values.put(PROJECTION[INDEX_SHOP_DIST], mComDist);
		values.put(PROJECTION[INDEX_SHOP_ADDRESS], mAddress);
		
		values.put(PROJECTION[INDEX_SHOP_LONG], mLongitude);
		values.put(PROJECTION[INDEX_SHOP_LAT], mLatitude);
		values.put(PROJECTION[INDEX_SHOP_NEWS], mNewsNote);
		values.put(PROJECTION[INDEX_SHOP_TOTAL_JF], mTotalXiaofeiJifen);
		values.put(PROJECTION[INDEX_SHOP_FREE_JF], mFreeJifen);
		values.put(PROJECTION[INDEX_SHOP_MEMBER_CARD_IAMGE], mMemberCardImage);
		values.put(PROJECTION[INDEX_SHOP_IMAGE], mShopImage);
		values.put(PROJECTION[INDEX_DATE], mDate == -1 ? new Date().getTime() : mDate);
		long id = BjnoteContent.existed(cr, BjnoteContent.MyLife.CONTENT_URI, ACCOUNT_UID_AND_SHOPID_WHERE, new String[]{String.valueOf(mAccountUID), String.valueOf(mShopID)});
    	if (id == -1) {
    		Uri uri = cr.insert(BjnoteContent.MyLife.CONTENT_URI, values);
    		DebugUtils.logD(TAG, "saveInDatebase insert " + toString() + ", uri = " + uri);
    		if (uri != null) {
    			mId = ContentUris.parseId(uri);
    		}
    		return uri != null;
    	} else {
    		int updated = cr.update(BjnoteContent.MyLife.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(id)});
    		DebugUtils.logD(TAG, "saveInDatebase update " + toString() + ", updated " + updated);
    		return updated > 0;
    	}
    }
    /**将电话数组转换为xxx,xxx形式的字符串*/
    public String convertShopCellsToString() {
    	if (mComCell == null || mComCell.length == 0) {
    		return "";
    	}
    	StringBuilder sb = new StringBuilder();
    	for(String cell : mComCell) {
    		sb.append(cell).append(",");
    	}
    	if (sb.length() > 0) {
    		sb.deleteCharAt(sb.length() -1);
    	}
    	DebugUtils.logD(TAG, "convertShopCellsToString " + sb.toString());
    	return sb.toString();
    }
    /**将xxx,xxx形式的电话字符串转换为电话数组*/
    public static String[] convertStringToShopCells(String cellsString) {
    	if (TextUtils.isEmpty(cellsString)) {
    		return new String[]{};
    	}
    	return cellsString.split(",");
    	
    }
    
    public static MyLifeObject getFromDatabase(String cell) {
    	Cursor cursor = MyApplication.getInstance().getContentResolver().query(BjnoteContent.MyLife.CONTENT_URI, PROJECTION, PROJECTION[INDEX_SHOP_TEL] +" like '%" + cell + "%'", null, null);
    	MyLifeObject myLifeObject = null;
    	if (cursor != null) {
    		tag:while(cursor.moveToNext()) {
    			myLifeObject = getFromCursor(cursor);
    			if(myLifeObject.mComCellRawStr.contains(cell)) {
    				break tag;
    			}
    		}
    	}
    	DebugUtils.logD(TAG, "getFromDatabase find cell " + cell + ", MyLifeObject " + myLifeObject);
    	return myLifeObject;
    }
    
    public static MyLifeObject getFromDatabaseWithId(long id) {
    	Cursor cursor = MyApplication.getInstance().getContentResolver().query(BjnoteContent.MyLife.CONTENT_URI, PROJECTION, ID_WHERE, new String[]{String.valueOf(id)}, null);
    	MyLifeObject myLifeObject = null;
    	if (cursor != null) {
    		if(cursor.moveToNext()) {
    			myLifeObject = getFromCursor(cursor);
    		}
    	}
    	DebugUtils.logD(TAG, "getFromDatabaseWithId find id " + id + ", MyLifeObject " + myLifeObject);
    	return myLifeObject;
    }
    
    public static MyLifeObject getFromCursor(Cursor cursor) {
    	MyLifeObject myLifeObject = new MyLifeObject();
    	myLifeObject.mId = cursor.getLong(INDEX_ID);
    	myLifeObject.mAccountUID = cursor.getLong(INDEX_UID);
    	myLifeObject.mShopID = cursor.getLong(INDEX_SID);
    	myLifeObject.mComName = cursor.getString(INDEX_SHOP_NAME);
    	myLifeObject.mAddress = cursor.getString(INDEX_SHOP_ADDRESS);
    	myLifeObject.mComProv = cursor.getString(INDEX_SHOP_PROVINCE);
    	myLifeObject.mComCity = cursor.getString(INDEX_SHOP_CITY);
    	myLifeObject.mComDist = cursor.getString(INDEX_SHOP_DIST);
		
		String cellStr = cursor.getString(INDEX_SHOP_TEL);
		myLifeObject.mComCell = convertStringToShopCells(cellStr);
		myLifeObject.mComCellRawStr = cursor.getString(INDEX_SHOP_TEL_RAW);
		myLifeObject.mLatitude = cursor.getString(INDEX_SHOP_LAT);
		myLifeObject.mLongitude = cursor.getString(INDEX_SHOP_LONG);
		myLifeObject.mShopImage = cursor.getString(INDEX_SHOP_IMAGE);
		
		myLifeObject.mTotalXiaofeiJifen = cursor.getString(INDEX_SHOP_TOTAL_JF);
		myLifeObject.mFreeJifen = cursor.getString(INDEX_SHOP_FREE_JF);
		myLifeObject.mLiveInfo = cursor.getString(INDEX_SHOP_GUANGGAO);
		myLifeObject.mMemberCardImage = cursor.getString(INDEX_SHOP_MEMBER_CARD_IAMGE);
		return myLifeObject;
    }
    
    
   /**
    * {
  		"StatusCode": "1",
  		"StatusMessage": "成功返回",
  		"Data": {
    		"shop": {
      		"shop_id": "3907819",
      		"name": "盛世开元酒楼",
      		"big_cate": "美食",
      		"small_cate1": null,
      		"small_cate2": null,
      		"province": "北京",
      		"city": "北京",
      		"area": "海淀区",
      		"address": "老营房路西段1号(汉拿山国际俱乐部对面)",
      		"phone": "010-88866999,88891666",
      		"latitude": "39.96037",
      		"longitude": "116.26708",
      		"photos": "http://i1.dpfile.com/2010-11-16/5849490_m.jpg"
    	},
    	"cell": "88866999"
  		}
    * @param array
    * @return
    */
	public static List<MyLifeObject> parseList(JSONArray array) {
		List<MyLifeObject> myLifeObjectList = new ArrayList<MyLifeObject>(10);
		if (array != null) {
			int len = array.length();
			for(int index = 0; index < len; index++) {
				try {
					myLifeObjectList.add(parse(array.getJSONObject(index)));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		return myLifeObjectList;
	}
	
	private static final Pattern PATTERN_SHOP_CELL = Pattern.compile("(\\d+)-\\d+");
	public static MyLifeObject parse(JSONObject object) throws JSONException {
		MyLifeObject mMyLifeObject = new MyLifeObject();
		mMyLifeObject.mAccountUID = MyAccountManager.getInstance().getCurrentAccountId();
		JSONObject shop = object.getJSONObject("shop");
		mMyLifeObject.mShopID = shop.optLong("shop_id", -1l);
		mMyLifeObject.mComName = shop.optString("name", "");
		mMyLifeObject.mAddress = shop.optString("address", "");
		mMyLifeObject.mComProv = shop.optString("province", "");
		mMyLifeObject.mComCity = shop.optString("city", "");
		mMyLifeObject.mComDist = shop.optString("area", "");
		
		mMyLifeObject.mComCellRawStr = shop.optString("phone", "");
		if (!TextUtils.isEmpty(mMyLifeObject.mComCellRawStr)) {
			//"phone": "010-88866999,88891666",
			Matcher matcher = PATTERN_SHOP_CELL.matcher(mMyLifeObject.mComCellRawStr);
			String areaCode = "";
			if (matcher.find()) {
				areaCode = matcher.group(1);
				DebugUtils.logD(TAG, "parse find areaCode=" + areaCode + ", form cellStr=" + mMyLifeObject.mComCellRawStr);
			}
			String[] cells = mMyLifeObject.mComCellRawStr.split(",");
			int index = 0;
			for(String cell : cells) {
				cell = cell.replaceAll("[-() ]", "");
				if (areaCode.length() > 0 && !cell.startsWith(areaCode) && !cell.startsWith("1")) {
					cell = areaCode + cell;
					DebugUtils.logD(TAG, "parse reset ShopCell from " + cell + " to " + cells[index]);
				}
				cells[index] = cell;
				index ++;
			}
			mMyLifeObject.mComCell = cells;
		}
		mMyLifeObject.mLatitude = shop.optString("latitude", "");
		mMyLifeObject.mLongitude = shop.optString("longitude", "");
		mMyLifeObject.mShopImage = shop.optString("photos", "");
		
		JSONObject huiyuan = object.optJSONObject("huiyuan");
		if (huiyuan != null) {
			mMyLifeObject.mTotalXiaofeiJifen = huiyuan.optString("total", "0.0");
			mMyLifeObject.mFreeJifen = huiyuan.optString("totaljifen", "0.0");
			mMyLifeObject.mLiveInfo = huiyuan.optString("huodong", "");
			mMyLifeObject.mMemberCardImage = huiyuan.optString("huiyuancardaddr", "");
		}
		return mMyLifeObject;
	}
    
	public String toString() {
		StringBuilder sb = new StringBuilder(TAG);
		sb.append("Shop[ShopId=").append(mShopID)
		.append(", ShopName=").append(mComName).append("]");
		return sb.toString();
	}
	
	public String toFriendlyString() {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(mComName)) {
			sb.append(mComName);
		}
		sb.append("-(");
		if (!TextUtils.isEmpty(mComCity)) {
			sb.append(mComCity);
		}
		if (!TextUtils.isEmpty(mComDist)) {
			sb.append(" ").append(mComDist);
		}
		sb.append(")");
		return sb.toString();
	}
}
