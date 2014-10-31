package com.bestjoy.app.warrantycard.account;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.ImageHelper;
import com.shwy.bestjoy.utils.InfoInterfaceImpl;
/**
 * 保修卡对象
 * @author chenkai
 * 
 * "data": [
        {
            "LeiXin": "类型", 
            "PinPai": "品牌", 
            "XingHao": "型号", 
            "SHBianHao": "12344", 
            "BXPhone": "400-20098000",  //保修电话
            "FPaddr": "图片地址（注意替换为ServerIP/Fapiao/20140419/10665de14261e416423e82f725bf6689.jpg", 
            "BuyDate": "20140812", 
            "BuyPrice": "125", 
            "BuyTuJing": "苏宁", 
            "YanBaoTime": "1年",    默认单位是年，使用的时候该值x365=天数
            "YanBaoDanWei": "苏宁", 
            "UID": 1, 
            "AID": 1, 
            "BID": 1,
            "ZhuBx":0.0,    部件保修天数，单位是年，计算同保修时间
            "Tag":"大厅暖气",  保修卡的标签，比如卧室电视机
            "WY": 1.0,          整机保修时长，单位是年
            "YBPhone":"400-20098005",  延保电话
            "KY":"101000003"     KY编码，用于显示产品图片
            "pky":"101000003"     KY编码，用于显示产品图片
            "hasimg":"false"  true表示有发票
         }
    ]
 *
 */
public class CarBaoxiuCardObject extends InfoInterfaceImpl {
	public static final String JSONOBJECT_NAME = "carbaoxiu";
	public static final String TAG = "CarBaoxiuCardObject";
	public String mLeiXin;
	public String mPinPai;
	public String mXingHao;
	public String mChePai;
	public String mCheJia;
	public String mFaDongJi;
	/**厂家电话*/
	public String mBXPhone;
	/**发票地址*/
	public String mFPaddr = "";
	/**购买日期*/
	public String mBuyDate;
	/**上次保养*/
	public String mLastBaoYanTime;
	/**上次验车*/
	public String mLastYanCheTime;
	/**保险到期*/
	public String mBaoXianDeadline;
	/**保修时间，浮点型，默认是1年*/
	public String mWY = "1";
	public String mYanBaoTime = "0";
	public String mYanBaoDanWei="";
	/**延保电话*/
	public String mYBPhone;
	public String mKY;
	/**用来构建保修卡设备预览图，如mPKY.jpg*/
	public String mPKY;
	/**本地id*/
	public long mId = -1;
	public long mUID = -1, mSID = -1;
	
	public String m4SShopTel = "",m4SWashingShopTel = "",mWeixiuShopTel = "",mChuxianShopTel = "";
	
	private int mZhengjiValidity = -1;
	private long mModifiedTime = new Date().getTime();
	
	
	/**这个值用作不同Activity之间的传递，如选择设备的时候*/
	private static CarBaoxiuCardObject mBaoxiuCardObject = null;
	/**如果服务器返回的保修卡数据中pky字段是000,则表示该保修卡没有设备预览图，直接显示本地的ky_default.jpg*/
	public static final String DEFAULT_BAOXIUCARD_IMAGE_KEY = "000";
	
	public static final String UID_SELECTION = HaierDBHelper.ACCOUNT_UID + "=?";
	public static final String UID_SID_SELECTION = UID_SELECTION + " and " + HaierDBHelper.DATA14 + "=?";
	public static final String[] PROJECTION = new String[]{
		HaierDBHelper.ID,              //0
		HaierDBHelper.ACCOUNT_UID,   //1  account id
		HaierDBHelper.DATA1,         //2  汽车型号
		HaierDBHelper.DATA2,         //3  车牌号码
		HaierDBHelper.DATA3,         //4  车架编号
		HaierDBHelper.DATA4,         //5  发动机号
		HaierDBHelper.DATA5,         //6  购买日期
		HaierDBHelper.DATA6,         //7  厂家电话
		HaierDBHelper.DATA7,         //8  发票地址
		HaierDBHelper.DATA8,         //9  上次保养
		HaierDBHelper.DATA9,         //10 上次验车
		HaierDBHelper.DATA10,        //11 保险到期
		HaierDBHelper.DATA11,        //12 延保时间
		HaierDBHelper.DATA12,        //13 延保单位
		HaierDBHelper.DATA13,        //14 延保电话
		HaierDBHelper.DATA14,        //15 保修卡服务器id
		HaierDBHelper.DATA15,        //16 保修期
		HaierDBHelper.DATA16,        //17 4S店
		HaierDBHelper.DATA17,        //18 洗车
		HaierDBHelper.DATA18,        //19 维修
		HaierDBHelper.DATA19,        //20 出险
		HaierDBHelper.DATA20,        //21 修改时间
		HaierDBHelper.DATA21,        //22 品牌
		HaierDBHelper.CARD_KY,       //23
		HaierDBHelper.CARD_PKY,      //24
		HaierDBHelper.DATA22,        //
	};
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	/**DATA1, 汽车型号*/
	public static final int INDEX_XINGHAO = 2;
	/**DATA2, 车牌号码*/
	public static final int INDEX_CHEPAI = 3;
	/**DATA3, 车架编号*/
	public static final int INDEX_CHEJIA = 4;
	/**DATA4, 发动机号*/
	public static final int INDEX_FADONGJI = 5;
	/**DATA5, 购买日期*/
	public static final int INDEX_BUY_DATE = 6;
	/**DATA6, 厂家电话*/
	public static final int INDEX_CHANGJIA_TEL = 7;
	/**DATA7, 发票地址*/
	public static final int INDEX_FAPIAO = 8;
	/**DATA8, 上次保养*/
	public static final int INDEX_BAOYAN = 9;
	/**DATA9, 上次验车*/
	public static final int INDEX_YANCHE = 10;
	/**DATA10, 保险到期*/
	public static final int INDEX_BAOXIAN = 11;
	/**DATA11, 延保时间*/
	public static final int INDEX_YANBAO = 12;
	/**DATA12, 延保单位*/
	public static final int INDEX_YANBAO_COMPANY = 13;
	/**DATA13, 延保单位*/
	public static final int INDEX_YANBAO_TEL = 14;
	/**DATA14, 保修卡服务器id*/
	public static final int INDEX_SID = 15;
	/**DATA15, 保修期*/
	public static final int INDEX_WY = 16;
	
	/**DATA16, 4S店*/
	public static final int INDEX_4S_SHOP = 17;
	/**DATA17, 洗车*/
	public static final int INDEX_WASHING_SHOP = 18;
	/**DATA18, 维修*/
	public static final int INDEX_WEIXIU_SHOP = 19;
	/**DATA19, 出险*/
	public static final int INDEX_CHUXIAN_SHOP = 20;
	/**DATA20, 修改时间*/
	public static final int INDEX_MODIFIED = 21;
	/**DATA21, PinPai*/
	public static final int INDEX_PINPAI = 22;
	/**CARD_KY, ky*/
	public static final int INDEX_KY = 23;
	/**CARD_PKY, pky*/
	public static final int INDEX_PKY = 24;
	
	public static Cursor getAlCarCards(ContentResolver cr, String uid) {
		return cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, UID_SELECTION, new String[]{uid}, HaierDBHelper.DATA14 + " desc");
	}
	
	public static CarBaoxiuCardObject parseBaoxiuCards(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		CarBaoxiuCardObject cardObject = new CarBaoxiuCardObject();
		cardObject.mPinPai = jsonObject.getString("pinpai");
		cardObject.mXingHao = jsonObject.getString("xinghao");
		cardObject.mChePai = jsonObject.getString("che_haoma");
		
		cardObject.mCheJia = jsonObject.getString("che_jiahao");
		
		cardObject.mBuyDate = jsonObject.getString("buydate");
//		if(buyDate != null) {
//			//2010-01-01
//			cardObject.mBuyDate = buyDate.replaceAll("[ -]", "");
//			DebugUtils.logD(TAG, "reset BuyDate from " + buyDate + " to " + cardObject.mBuyDate);
//		} else {
//			cardObject.mBuyDate = "";
//		}
		cardObject.mFaDongJi = jsonObject.getString("fadongjihao");
		
		cardObject.mBXPhone = jsonObject.getString("changjia_phone");
		cardObject.mFPaddr = jsonObject.optString("fapiao_addr", "");
		
		cardObject.mLastBaoYanTime = jsonObject.getString("shangcibaoyang");
		cardObject.mLastYanCheTime = jsonObject.getString("shangciyanche");
		cardObject.mBaoXianDeadline = jsonObject.getString("baoyangdaoqi");
		
		cardObject.mWY = jsonObject.getString("wy");
		if ("null".equals(cardObject.mWY)) {
			cardObject.mWY = "0";
		}
		
		
		cardObject.mYanBaoTime = jsonObject.getString("yanbaotime");
		if ("null".equals(cardObject.mYanBaoTime)) {
			cardObject.mYanBaoTime = "0";
		}
		cardObject.mYanBaoDanWei = jsonObject.getString("yanbaodanwei");
		cardObject.mYBPhone = jsonObject.getString("yanbaophone");
		
		cardObject.mUID = jsonObject.getLong("uid");
		cardObject.mSID = jsonObject.getLong("cid");
		
		cardObject.mKY = jsonObject.getString("ky");
		if ("null".equalsIgnoreCase(cardObject.mKY)) {
			DebugUtils.logE(TAG, "parseBaoxiuCards find illegal value " + cardObject.mKY + " for ky");
			cardObject.mKY = "";
		}
		cardObject.mPKY = jsonObject.optString("pky", CarBaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY);
		if ("null".equalsIgnoreCase(cardObject.mPKY)) {
			cardObject.mPKY = CarBaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY;
		}
		
		cardObject.m4SShopTel = jsonObject.getString("fsphone");
		cardObject.m4SWashingShopTel = jsonObject.getString("washphone");
		cardObject.mWeixiuShopTel = jsonObject.getString("weixiuphone");
		cardObject.mChuxianShopTel = jsonObject.getString("chuxianphone");
		
		cardObject.mModifiedTime = Long.valueOf(jsonObject.getString("rtime"));
		
		return cardObject;
	}
	
	private static final int MAX_CACHE_SIZE = 20;
	private static LinkedHashMap<String, HomeObject> mHomeObjectCache = new LinkedHashMap<String, HomeObject>(MAX_CACHE_SIZE){

		@Override
		protected boolean removeEldestEntry(Entry<String, HomeObject> eldest) {
			 return size() > MAX_CACHE_SIZE;
		}
		
	};
	private static Object mLock = new Object();
	
	public CarBaoxiuCardObject clone() {
		CarBaoxiuCardObject newBaoxiuCardObject = new CarBaoxiuCardObject();
		newBaoxiuCardObject.mUID = mUID;
		newBaoxiuCardObject.mSID = mSID;
		newBaoxiuCardObject.mId = mId;
		newBaoxiuCardObject.mWY = mWY;
		newBaoxiuCardObject.mPinPai = mPinPai;
		newBaoxiuCardObject.mXingHao = mXingHao;
		newBaoxiuCardObject.mChePai = mChePai;
		
		newBaoxiuCardObject.mCheJia = mCheJia;
		newBaoxiuCardObject.mFaDongJi = mFaDongJi;
		newBaoxiuCardObject.mXingHao = mXingHao;
		newBaoxiuCardObject.mBXPhone = mBXPhone;
		
		newBaoxiuCardObject.mFPaddr = mFPaddr;
		newBaoxiuCardObject.mBuyDate = mBuyDate;
		newBaoxiuCardObject.mLastBaoYanTime = mLastBaoYanTime;
		newBaoxiuCardObject.mLastYanCheTime = mLastYanCheTime;
		newBaoxiuCardObject.mBaoXianDeadline = mBaoXianDeadline;
		newBaoxiuCardObject.mYanBaoTime = mYanBaoTime;
		newBaoxiuCardObject.mYanBaoDanWei = mYanBaoDanWei;
		newBaoxiuCardObject.mYBPhone = mYBPhone;
		newBaoxiuCardObject.mKY = mKY;
		newBaoxiuCardObject.mPKY = mPKY;
		
		newBaoxiuCardObject.m4SShopTel = m4SShopTel;
		newBaoxiuCardObject.m4SWashingShopTel = m4SWashingShopTel;
		newBaoxiuCardObject.mWeixiuShopTel = mWeixiuShopTel;
		newBaoxiuCardObject.mChuxianShopTel = mChuxianShopTel;
		newBaoxiuCardObject.mModifiedTime = mModifiedTime;
		
		return newBaoxiuCardObject;
	}
	/**
	 * 获取保修卡对象的Bundle对象
	 * @return
	 */
	public Bundle getBaoxiuCardObjectBundle() {
		Bundle bundle = new Bundle();
		bundle.putLong("id", mId);
		bundle.putLong("uid", mUID);
		bundle.putLong("mSID", mSID);
		bundle.putString("wy", mWY);
		bundle.putString("mPinPai", mPinPai);
		bundle.putString("mXingHao", mXingHao);
		bundle.putString("mChePai", mChePai);
		
		bundle.putString("mCheJia", mCheJia);
		bundle.putString("mFaDongJi", mFaDongJi);
		bundle.putString("mBXPhone", mBXPhone);
		
		bundle.putString("mFPaddr", mFPaddr);
		bundle.putString("mBuyDate", mBuyDate);
		bundle.putString("mLastBaoYanTime", mLastBaoYanTime);
		
		bundle.putString("mLastYanCheTime", mLastYanCheTime);
		bundle.putString("mBaoXianDeadline", mBaoXianDeadline);
		
		bundle.putString("mYanBaoTime", mYanBaoTime);
		bundle.putString("mYanBaoDanWei", mYanBaoDanWei);
		bundle.putString("mYBPhone", mYBPhone);
		
		bundle.putString("mKY", mKY);
		bundle.putString("mPKY", mPKY);
		
		bundle.putString("m4SShopTel", m4SShopTel);
		bundle.putString("m4SWashingShopTel", m4SWashingShopTel);
		bundle.putString("mWeixiuShopTel", mWeixiuShopTel);
		bundle.putString("mChuxianShopTel", mChuxianShopTel);
		bundle.putLong("mModifiedTime", mModifiedTime);
		
		return bundle;
	}
	
	public static CarBaoxiuCardObject getBaoxiuCardObjectFromBundle(Bundle bundle) {
		CarBaoxiuCardObject baoxiuCardObject = new CarBaoxiuCardObject();
		bundle = bundle.getBundle(TAG);
		if (bundle == null) {
			return baoxiuCardObject;
		}
		baoxiuCardObject.mId = bundle.getLong("id", -1);
		baoxiuCardObject.mUID = bundle.getLong("uid", -1);
		baoxiuCardObject.mSID = bundle.getLong("mSID", -1);
		baoxiuCardObject.mPinPai = bundle.getString("mPinPai", "");
		baoxiuCardObject.mXingHao = bundle.getString("mXingHao", "");
		
		baoxiuCardObject.mWY = bundle.getString("wy");
		baoxiuCardObject.mChePai = bundle.getString("mChePai", "");
		baoxiuCardObject.mCheJia = bundle.getString("mZhuBx", "");
		baoxiuCardObject.mFaDongJi = bundle.getString("mFaDongJi", "");
		baoxiuCardObject.mBXPhone = bundle.getString("mBXPhone", "");
		baoxiuCardObject.mFPaddr = bundle.getString("mFPaddr", "");
		
		baoxiuCardObject.mBuyDate = bundle.getString("mBuyDate", "");
		baoxiuCardObject.mLastBaoYanTime = bundle.getString("mLastBaoYanTime", "");
		baoxiuCardObject.mLastYanCheTime = bundle.getString("mLastYanCheTime", "");
		baoxiuCardObject.mBaoXianDeadline = bundle.getString("mBaoXianDeadline", "");
		
		baoxiuCardObject.mYanBaoTime = bundle.getString("mYanBaoTime", "");
		baoxiuCardObject.mYanBaoDanWei = bundle.getString("mYanBaoDanWei");
		baoxiuCardObject.mYBPhone = bundle.getString("mYBPhone");
		
		baoxiuCardObject.mKY = bundle.getString("mKY");
		baoxiuCardObject.mPKY = bundle.getString("mPKY");
		
		baoxiuCardObject.m4SShopTel = bundle.getString("m4SShopTel", "");
		baoxiuCardObject.m4SWashingShopTel = bundle.getString("m4SWashingShopTel", "");
		baoxiuCardObject.mWeixiuShopTel = bundle.getString("mWeixiuShopTel", "");
		baoxiuCardObject.mChuxianShopTel = bundle.getString("mChuxianShopTel", "");
		baoxiuCardObject.mModifiedTime = bundle.getLong("mModifiedTime", new Date().getTime());
		return baoxiuCardObject;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[PinPai:").append(mPinPai).append(", XingHao:").append(mXingHao).append(", ChePai:").append(mChePai).append("]");
		return sb.toString();
	}
	
	public static int deleteBaoxiuCardInDatabaseForAccount(ContentResolver cr, long uid, long bid) {
		int deleted = cr.delete(BjnoteContent.MyCarCards.CONTENT_URI, UID_SID_SELECTION, new String[]{String.valueOf(uid), String.valueOf(bid)});
		DebugUtils.logD(TAG, "deleteBaoxiuCardInDatabaseForAccount sid#" + bid + ", delete " + deleted);
		return deleted;
	}
	
	/**
	 * 删除某个account的全部保修卡
	 * @param cr
	 * @param uid
	 * @return
	 */
	public static int deleteAllBaoxiuCardsInDatabaseForAccount(ContentResolver cr, long uid) {
		int deleted = cr.delete(BjnoteContent.MyCarCards.CONTENT_URI, UID_SELECTION, new String[]{String.valueOf(uid)});
		DebugUtils.logD(TAG, "deleteAllBaoxiuCardsInDatabaseForAccount uid#" + uid + ", delete " + deleted);
		return deleted;
	}
	 public static int getAllBaoxiuCardsCount(ContentResolver cr, long uid) {
		 Cursor c = getAllBaoxiuCardsCursor(cr, uid);
		 if (c != null) {
			 int count = c.getCount();
			 c.close();
			 return count;
		 }
		 return 0;
	 }
	/**
	 * 获取某个账户某个家的全部保修卡数据
	 * @param cr
	 * @param uid
	 * @param aid
	 * @return
	 */
    public static Cursor getAllBaoxiuCardsCursor(ContentResolver cr, long uid) {
		return cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, UID_SELECTION, new String[]{String.valueOf(uid)}, PROJECTION[INDEX_SID]+" DESC");
	}
    
    public static CarBaoxiuCardObject getBaoxiuCardObject(ContentResolver cr, long uid, long bid) {
    	CarBaoxiuCardObject object = null;
		Cursor c = cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, UID_SID_SELECTION, new String[]{String.valueOf(uid), String.valueOf(bid)}, null);
		if (c != null) {
			if (c.moveToNext()) {
				object = getFromBaoxiuCardsCursor(c);
			}
			c.close();
		}
		return object;
	}
    /**
     * 根据Bundle对象得到保修卡对象，如果Bundle中给定了aid, bid, uid,我们需要从数据库中获取保修卡对象.
     * @param bundle
     * @return
     */
    public static CarBaoxiuCardObject getBaoxiuCardObject(Bundle bundle) {
		long bid = bundle.getLong("bid", -1);
		long uid = bundle.getLong("uid", -1);
		DebugUtils.logD(TAG, "getBaoxiuCardObject() bundle = " + bundle);
		if (uid != -1 && bid != -1) {
			DebugUtils.logD(TAG, "getBaoxiuCardObject() get BaoxiuCardObject from Database");
			return CarBaoxiuCardObject.getBaoxiuCardObject(MyApplication.getInstance().getContentResolver(), uid, bid);
		} else {
			
			CarBaoxiuCardObject newBaoxiuCardObject = CarBaoxiuCardObject.getBaoxiuCardObjectFromBundle(bundle);
			newBaoxiuCardObject.mUID = uid;
			newBaoxiuCardObject.mSID = bid;
			DebugUtils.logD(TAG, "getBaoxiuCardObject() new BaoxiuCardObject=" + newBaoxiuCardObject);
			return newBaoxiuCardObject;
		}
    }
    public static List<CarBaoxiuCardObject> getAllBaoxiuCardObjects(ContentResolver cr, long uid) {
		Cursor c = getAllBaoxiuCardsCursor(cr, uid);
		List<CarBaoxiuCardObject> list = new ArrayList<CarBaoxiuCardObject>();
		if (c != null) {
			list = new ArrayList<CarBaoxiuCardObject>(c.getCount());
			while(c.moveToNext()) {
				list.add(getFromBaoxiuCardsCursor(c));
			}
			c.close();
		}
		return list;
	}
    
    public static CarBaoxiuCardObject getFromBaoxiuCardsCursor(Cursor c) {
    	CarBaoxiuCardObject baoxiuCardObject = new CarBaoxiuCardObject();
    	baoxiuCardObject.mId = c.getLong(INDEX_ID);
    	baoxiuCardObject.mUID = c.getLong(INDEX_UID);
    	baoxiuCardObject.mSID = c.getLong(INDEX_SID);
    	baoxiuCardObject.mPinPai =c.getString(INDEX_PINPAI);
    	baoxiuCardObject.mXingHao = c.getString(INDEX_XINGHAO);
    	baoxiuCardObject.mChePai = c.getString(INDEX_CHEPAI);
    	baoxiuCardObject.mCheJia = c.getString(INDEX_CHEJIA);
    	baoxiuCardObject.mFaDongJi = c.getString(INDEX_FADONGJI);
    	baoxiuCardObject.mBuyDate = c.getString(INDEX_BUY_DATE);
    	baoxiuCardObject.mBXPhone = c.getString(INDEX_CHANGJIA_TEL);
    	baoxiuCardObject.mFPaddr = c.getString(INDEX_FAPIAO);
    	
    	baoxiuCardObject.mLastBaoYanTime = c.getString(INDEX_BAOYAN);
    	baoxiuCardObject.mLastYanCheTime = c.getString(INDEX_YANCHE);
    	baoxiuCardObject.mBaoXianDeadline = c.getString(INDEX_BAOXIAN);
    	baoxiuCardObject.mWY = c.getString(INDEX_WY);
    	baoxiuCardObject.mYanBaoTime = c.getString(INDEX_YANBAO);
    	baoxiuCardObject.mYanBaoDanWei = c.getString(INDEX_YANBAO_COMPANY);
    	baoxiuCardObject.mYBPhone = c.getString(INDEX_YANBAO_TEL);
    	baoxiuCardObject.mKY = c.getString(INDEX_KY);
    	baoxiuCardObject.mPKY = c.getString(INDEX_PKY);
    	
    	baoxiuCardObject.m4SShopTel = c.getString(INDEX_4S_SHOP);
    	baoxiuCardObject.m4SWashingShopTel = c.getString(INDEX_WASHING_SHOP);
    	baoxiuCardObject.mWeixiuShopTel =c.getString(INDEX_WEIXIU_SHOP);
    	baoxiuCardObject.mChuxianShopTel =c.getString(INDEX_CHUXIAN_SHOP);
    	baoxiuCardObject.mModifiedTime = c.getLong(INDEX_MODIFIED);
    	
		return baoxiuCardObject;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		String[] selectionArgs =  new String[]{String.valueOf(mUID), String.valueOf(mSID)};
		long id = isExsited(cr,selectionArgs);
		
		values.put(PROJECTION[INDEX_UID], mUID);
		values.put(PROJECTION[INDEX_PINPAI], mPinPai);
		values.put(PROJECTION[INDEX_XINGHAO], mXingHao);
		values.put(PROJECTION[INDEX_CHEPAI], mChePai);
		values.put(PROJECTION[INDEX_CHEJIA], mCheJia);
		values.put(PROJECTION[INDEX_FADONGJI], mFaDongJi);
		values.put(PROJECTION[INDEX_BUY_DATE], mBuyDate);
		values.put(PROJECTION[INDEX_CHANGJIA_TEL], mBXPhone);
		values.put(PROJECTION[INDEX_FAPIAO], mFPaddr);
		values.put(PROJECTION[INDEX_BAOYAN], mLastBaoYanTime);
		values.put(PROJECTION[INDEX_YANCHE], mLastYanCheTime);
		values.put(PROJECTION[INDEX_BAOXIAN], mBaoXianDeadline);
		
		values.put(PROJECTION[INDEX_YANBAO], mYanBaoTime);
		values.put(PROJECTION[INDEX_YANBAO_COMPANY], mYanBaoDanWei);
		values.put(PROJECTION[INDEX_YANBAO_TEL], mYBPhone);
		values.put(PROJECTION[INDEX_SID], mSID);
		values.put(PROJECTION[INDEX_WY], mWY);
		
		values.put(PROJECTION[INDEX_4S_SHOP], m4SShopTel);
		values.put(PROJECTION[INDEX_WASHING_SHOP], m4SWashingShopTel);
		values.put(PROJECTION[INDEX_WEIXIU_SHOP], mWeixiuShopTel);
		values.put(PROJECTION[INDEX_CHUXIAN_SHOP], mChuxianShopTel);
		values.put(PROJECTION[INDEX_KY], mKY);
		values.put(PROJECTION[INDEX_PKY], mPKY);
		values.put(PROJECTION[INDEX_MODIFIED], mModifiedTime);
		
		
		if (id > 0) {
			int update = cr.update(BjnoteContent.MyCarCards.CONTENT_URI, values,  UID_SID_SELECTION, selectionArgs);
			if (update > 0) {
				DebugUtils.logD(TAG, "saveInDatebase update exsited bid#" + mSID);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly update exsited bid#" + mSID);
			}
		} else {
			Uri uri = cr.insert(BjnoteContent.MyCarCards.CONTENT_URI, values);
			if (uri != null) {
				DebugUtils.logD(TAG, "saveInDatebase insert bid#" + mSID);
				mId = ContentUris.parseId(uri);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly insert bid#" + mSID);
			}
		}
		return false;
	}
	
	private long isExsited(ContentResolver cr, String[] selectionArgs) {
		long id = -1;
		Cursor c = cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, UID_SID_SELECTION, selectionArgs, null);
		if (c != null) {
			if (c.moveToNext()) {
				id = c.getLong(INDEX_SID);
			}
			c.close();
		}
		return id;
	}
	
	/**
	 * 返回该保修对象的整机保修有效期天数，计算保修有效期公式 = 延保时间+保修天数-已买天数
	 * @return
	 */
	public int getBaoxiuValidity() {
		if (mZhengjiValidity == -1) {
			if (TextUtils.isEmpty(mWY)) {
				mWY = "0";
			}
			if (TextUtils.isEmpty(mYanBaoTime)) {
				mYanBaoTime = "0";
			}
			
			try {
				int validity = (int) ((Float.valueOf(mWY) + Float.valueOf(mYanBaoTime)) * 365);
				//转换购买日期
				long buyDate = Long.valueOf(mBuyDate);
				//当前日期
				Date now = new Date();
				long passedTimeLong = now.getTime() - buyDate;
				if (passedTimeLong < 0) {
					passedTimeLong = 0;
				}
				int passedDay = (int) (passedTimeLong / BaoxiuCardObject.DAY_IN_MILLISECONDS);
				mZhengjiValidity = validity - passedDay;
			} catch (NumberFormatException e) {
				DebugUtils.logE(TAG, "getBaoxiuValidity() NumberFormatException " + e.getMessage()); 
				e.printStackTrace();
				mZhengjiValidity = 0;
			}
		}
		return mZhengjiValidity;
	}
	
	public static int getValidityDay(int rangeDay, long timeStamp) {
		Date now = new Date();
		long haveTimeLong = timeStamp + rangeDay * BaoxiuCardObject.DAY_IN_MILLISECONDS - now.getTime();
		if (haveTimeLong < 0) {
			haveTimeLong = 0;
		}
		int haveDay = (int) (haveTimeLong / BaoxiuCardObject.DAY_IN_MILLISECONDS);
		if (haveDay > 365) {
			haveDay = 365;
		}
		return haveDay;
	}
	
	public static int getValidityDay(int rangeDay, String timeStamp) {
		if (TextUtils.isEmpty(timeStamp)) {
			return 0;
		}
		return getValidityDay(rangeDay, Long.valueOf(timeStamp));
	}
	
	
	
	/**
	 * 当我们设置过mBaoxiuCardObject值后，需要使用这个方法来获取，这会重置mBaoxiuCardObject对象为null.
	 * @return
	 */
	public static CarBaoxiuCardObject getBaoxiuCardObject() {
		CarBaoxiuCardObject object = null;
		if (mBaoxiuCardObject != null) {
			object = mBaoxiuCardObject;
			mBaoxiuCardObject = null;
		}
		return object;
	}
	/**
	 * 需要在Activity之间传递保修卡对象的时候，需要调用该方法来设置，之后使用getBaoxiuCardObject()来获得.
	 * @param baoxiucardObject
	 */
	public static void setBaoxiuCardObject(CarBaoxiuCardObject baoxiucardObject) {
		mBaoxiuCardObject = baoxiucardObject;
	}
	
	/**
	 * 标签的内容应该是“备注标签+类型”如“客厅空调”
	 * @param cardName   备注标签
	 * @param cardType   类型
	 * @return
	 */
	public static String getTagName(String cardName, String cardType) {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(cardName)) {
			sb.append(cardName);
		}
		sb.append(cardType);
		return sb.toString();
	}
	
	/**
	 * 标签的内容应该是“备注标签+品牌+类型”如“客厅海尔空调”
	 * @param cardName   备注标签
	 * @param pinpai     品牌
	 * @param cardType   类型
	 * @return
	 */
	public static String getTagName(String cardName, String pinpai, String cardType) {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(cardName)) {
			sb.append(cardName).append('-');
		}
		if (!TextUtils.isEmpty(pinpai)) {
			sb.append(pinpai);
		}
		sb.append(cardType);
		return sb.toString();
	}
	private static final int mAvatorWidth = 1200, mAvatorHeight =1200;
	public static final String PHOTOID_SEPERATOR = "_";
	/**占位符号*/
	public static final String PHOTOID_PLASEHOLDER = "00_00_00";
	/**临时拍摄的照片路径，当保存成功的时候会将该文件路径重命名为mBillAvator*/
	public Bitmap mBillTempBitmap;
	/**本地发票图片路径*/
	public File mBillFile;
	/**临时拍摄的照片路径，当保存成功的时候会将该文件路径重命名为mBillAvator*/
	public File mBillTempFile;
	
	public static CarBaoxiuCardObject objectUseForbill = null;
	/**是否有发票,如果有发票文件或是有发票的拍摄获得的临时文件,我们认为是有发票的*/
	public boolean hasLocalBill() {
		if (mBillFile == null) {
			mBillFile = MyApplication.getInstance().getProductFaPiaoFile(getFapiaoPhotoId());
		}
		return mBillFile.exists() || mBillTempFile != null;
	}
	/**
	 * 是否有发票
	 * @return
	 */
	public boolean hasBillAvator() {
		//modify by chenkai, 20140701, 将发票地址存进数据库（不再拼接），增加海尔奖励延保时间 begin
		return !TextUtils.isEmpty(mFPaddr) && mFPaddr.startsWith("http");
		//modify by chenkai, 20140701, 将发票地址存进数据库（不再拼接），增加海尔奖励延保时间 end
	}
	/**
	 * 添加发票时候使用，用来表示是否有临时的拍摄发票文件，有的话，我们认为是要上传的
	 * @return
	 */
	public boolean hasTempBill() {
		return mBillTempFile != null && mBillTempFile.exists() ;
	}
	
	/**
	 * http://115.29.231.29/Fapiao/20140421/01324df60b0734de0f973c7907af55fc.jpg
	 * 返回 20140421_01324df60b0734de0f973c7907af55fc
	 * @return
	 */
	public String getFapiaoPhotoId() {
//		if (!TextUtils.isEmpty(mFPaddr) && mFPaddr.startsWith(HaierServiceObject.FAPIAO_PREFIX)) {
//			String photoId = mFPaddr.substring(HaierServiceObject.FAPIAO_PREFIX.length());
//			photoId = photoId.replaceAll("/", "_");
//			return photoId;
//		}
		if (hasBillAvator()) {
			return BaoxiuCardObject.getFapiaoPhotoIdFromFpAddr(mFPaddr);
		}
		DebugUtils.logD(TAG, "getFapiaoPhotoId() " + PHOTOID_PLASEHOLDER);
		return PHOTOID_PLASEHOLDER;
	}
	
	
	/**保存临时的发票拍摄作为该商品的使用发票预览图*/
	public boolean saveBillAvatorTempFileLocked() {
		if (mBillTempBitmap != null) {
			File newPath = MyApplication.getInstance().getProductFaPiaoFile(getFapiaoPhotoId());
			boolean result = ImageHelper.bitmapToFile(mBillTempBitmap, newPath, 100);
			if (result) {
				mBillFile = newPath;
				if (mBillTempFile != null && mBillTempFile.exists()) {
					mBillTempFile.delete();
					mBillTempFile = null;
				}
			}
			return result;
		} else {
			return false;
		}
	}
	
	 /**
     * 返回商品发票预览图的Base64编码字符串
     * @return
     */
    public String getBase64StringFromBillAvator(){
    	//默认返回""
    	String result = "";
    	//如果此时还没有临时商品预览图，我们从文件中构建
        if (mBillTempBitmap == null) {
        	if (mBillFile == null) {
    			mBillFile = MyApplication.getInstance().getProductFaPiaoFile(getFapiaoPhotoId());
    		}
        	if (mBillFile != null && mBillFile.exists()) {
        		Bitmap billTempBitmap = ImageHelper.getSmallBitmap(mBillFile.getAbsolutePath(), mAvatorWidth, mAvatorHeight);
        		if (billTempBitmap != null) {
        			result = ImageHelper.bitmapToString(billTempBitmap, 100);
        		} else{
        			new Exception("getBase64StringFromBillAvator() getSmallBitmap return null").printStackTrace();
        		}
        	}
        } else {
        	 result = ImageHelper.bitmapToString(mBillTempBitmap, 100);
        }
        
       return result == null ? "":result;
    }
    
    public void updateBillAvatorTempLocked(File file) {
    	mBillTempFile = file;
    	mBillTempBitmap = ImageHelper.getSmallBitmap(file.getAbsolutePath(), mAvatorWidth, mAvatorHeight);
//    	mBillTempBitmap = ImageHelper.rotateBitmap(mBillTempBitmap, 90);
    	mBillTempBitmap = ImageHelper.scaleBitmapFile(mBillTempBitmap, mAvatorWidth, mAvatorHeight);
		ImageHelper.bitmapToFile(mBillTempBitmap, mBillTempFile, 65);
		mBillTempBitmap.recycle();
		mBillTempBitmap = ImageHelper.getSmallBitmap(file.getAbsolutePath(), mAvatorWidth, mAvatorHeight);
    }
	
	public void clear() {
		if (mBillTempBitmap != null) {
			mBillTempBitmap.recycle();
			mBillTempBitmap = null;
		}
		if (mBillTempFile != null && mBillTempFile.exists()) {
			mBillTempFile.delete();
			mBillTempFile = null;
		}
	}
	
	public static void showBill(Context context, CarBaoxiuCardObject baociuCardObject) {
		objectUseForbill = baociuCardObject;
		if (baociuCardObject != null) {
			MyApplication.getInstance().showMessage(R.string.msg_wait_for_fapiao_show);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(BjnoteContent.BaoxiuCard.BILL_CONTENT_URI, "image/png");
			context.startActivity(intent);
		}
	}
	
	/***
	 * 返回远程发票的绝对路径
	 * @return
	 */
	public String getFapiaoServicePath() {
		return mFPaddr;
	}

}
