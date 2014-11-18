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
import com.bestjoy.app.bjwarrantycard.im.RelationshipObject;
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
public class CarBaoxiuCardObject extends IBaoxiuCardObject {
	public static final String TAG = "CarBaoxiuCardObject";
	public String mChePai="";
	public String mCheJia="";
	public String mFaDongJi="";
	/**上次保养*/
	public String mLastBaoYanTime="";
	/**上次验车*/
	public String mLastYanCheTime="";
	/**保险到期*/
	public String mBaoXianDeadline="";
	
	public String m4SShopTel = "",m4SWashingShopTel = "",mWeixiuShopTel = "",mChuxianShopTel = "";
	
	private int mZhengjiValidity = -1;
	
	/**这个值用作不同Activity之间的传递，如选择设备的时候*/
	private static CarBaoxiuCardObject mBaoxiuCardObject = null;
	/**如果服务器返回的保修卡数据中pky字段是000,则表示该保修卡没有设备预览图，直接显示本地的ky_default.jpg*/
	public static final String DEFAULT_BAOXIUCARD_IMAGE_KEY = "000";
	
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
	/**DATA22, ky*/
	public static final int INDEX_KY = 23;
	/**DATA23, pky*/
	public static final int INDEX_PKY = 24;
	
	/**DATA24, 修改时间*/
	public static final int KEY_CARD_MMONE = 25;
	/**DATA25, PinPai*/
	public static final int KEY_CARD_MMTWO = 26;
	
	/**DATA26, pdfpath*/
	public static final int KEY_CARD_PDF_PATH = 27;
	
	public static final String UID_SID_SELECTION = WHERE_UID + " and " + PROJECTION[INDEX_SID] + "=?";
	
	public static Cursor getAlCarCards(ContentResolver cr, String uid) {
		return cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, WHERE_UID, new String[]{uid}, HaierDBHelper.DATA14 + " desc");
	}
	
	public static CarBaoxiuCardObject parseBaoxiuCards(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		CarBaoxiuCardObject cardObject = new CarBaoxiuCardObject();
		cardObject.mPinPai = jsonObject.getString("pinpai");
		cardObject.mXingHao = jsonObject.getString("xinghao");
		cardObject.mChePai = jsonObject.getString("che_haoma");
		
		cardObject.mCheJia = jsonObject.getString("che_jiahao");
		
		cardObject.mBuyDate = jsonObject.getString("buydate");
		cardObject.mFaDongJi = jsonObject.getString("fadongjihao");
		
		cardObject.mBXPhone = jsonObject.getString("changjia_phone");
		
		cardObject.mLastBaoYanTime = jsonObject.getString("shangcibaoyang");
		cardObject.mLastYanCheTime = jsonObject.getString("shangciyanche");
		cardObject.mBaoXianDeadline = jsonObject.getString("baoyangdaoqi");
		
		cardObject.mWY = jsonObject.getString("wy");
		if ("null".equals(cardObject.mWY)) {
			cardObject.mWY = "0";
		}
		
		
		cardObject.mYanBaoTime = jsonObject.getString("yanbaotime");
		if ("null".equals(cardObject.mYanBaoTime) || "".equals(cardObject.mYanBaoTime)) {
			cardObject.mYanBaoTime = "0";
		}
		cardObject.mYanBaoDanWei = jsonObject.getString("yanbaodanwei");
		cardObject.mYBPhone = jsonObject.getString("yanbaophone");
		
		cardObject.mUID = jsonObject.getLong("uid");
		cardObject.mBID = jsonObject.getLong("cid");
		
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
		
		JSONObject mmone = jsonObject.optJSONObject("MMOne");
		if (mmone != null) {
			cardObject.mMMOneRelationshipObject = RelationshipObject.parse(mmone);
		}
		
		mmone = jsonObject.optJSONObject("MMTwo");
		if (mmone != null) {
			cardObject.mMMTwoRelationshipObject = RelationshipObject.parse(mmone);
		}
		cardObject.mPdfPath = jsonObject.optString("pdfpath", "");
		cardObject.mFPaddr = jsonObject.optString("fapiao_addr", "");
		if ("null".equalsIgnoreCase(cardObject.mFPaddr)) {
			cardObject.mFPaddr = "";
		}
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
		super.clone(newBaoxiuCardObject);
		newBaoxiuCardObject.mChePai = mChePai;
		
		newBaoxiuCardObject.mCheJia = mCheJia;
		newBaoxiuCardObject.mFaDongJi = mFaDongJi;
		
		newBaoxiuCardObject.mLastBaoYanTime = mLastBaoYanTime;
		newBaoxiuCardObject.mLastYanCheTime = mLastYanCheTime;
		newBaoxiuCardObject.mBaoXianDeadline = mBaoXianDeadline;
		
		newBaoxiuCardObject.m4SShopTel = m4SShopTel;
		newBaoxiuCardObject.m4SWashingShopTel = m4SWashingShopTel;
		newBaoxiuCardObject.mWeixiuShopTel = mWeixiuShopTel;
		newBaoxiuCardObject.mChuxianShopTel = mChuxianShopTel;
		
		return newBaoxiuCardObject;
	}
	/**
	 * 获取保修卡对象的Bundle对象
	 * @return
	 */
	public Bundle getBaoxiuCardObjectBundle() {
		Bundle bundle = new Bundle();
		super.populateBaoxiuCardObjectBundle(bundle);
		bundle.putString("mChePai", mChePai);
		
		bundle.putString("mCheJia", mCheJia);
		bundle.putString("mFaDongJi", mFaDongJi);
		
		bundle.putString("mLastBaoYanTime", mLastBaoYanTime);
		bundle.putString("mLastYanCheTime", mLastYanCheTime);
		bundle.putString("mBaoXianDeadline", mBaoXianDeadline);
		
		bundle.putString("m4SShopTel", m4SShopTel);
		bundle.putString("m4SWashingShopTel", m4SWashingShopTel);
		bundle.putString("mWeixiuShopTel", mWeixiuShopTel);
		bundle.putString("mChuxianShopTel", mChuxianShopTel);
		
		return bundle;
	}
	
	public static CarBaoxiuCardObject getBaoxiuCardObjectFromBundle(Bundle bundle) {
		CarBaoxiuCardObject baoxiuCardObject = new CarBaoxiuCardObject();
		populateBaoxiuCardObjectFromBundle(bundle, baoxiuCardObject);
		baoxiuCardObject.mChePai = bundle.getString("mChePai", "");
		baoxiuCardObject.mCheJia = bundle.getString("mZhuBx", "");
		baoxiuCardObject.mFaDongJi = bundle.getString("mFaDongJi", "");
		
		baoxiuCardObject.mLastBaoYanTime = bundle.getString("mLastBaoYanTime", "");
		baoxiuCardObject.mLastYanCheTime = bundle.getString("mLastYanCheTime", "");
		baoxiuCardObject.mBaoXianDeadline = bundle.getString("mBaoXianDeadline", "");
		
		baoxiuCardObject.m4SShopTel = bundle.getString("m4SShopTel", "");
		baoxiuCardObject.m4SWashingShopTel = bundle.getString("m4SWashingShopTel", "");
		baoxiuCardObject.mWeixiuShopTel = bundle.getString("mWeixiuShopTel", "");
		baoxiuCardObject.mChuxianShopTel = bundle.getString("mChuxianShopTel", "");
		return baoxiuCardObject;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[PinPai:").append(mPinPai).append(", XingHao:").append(mXingHao).append(", ChePai:").append(mChePai).append("]");
		return sb.toString();
	}
	
	/**
	 * 删除某个account的全部保修卡
	 * @param cr
	 * @param uid
	 * @return
	 */
	public static int deleteAllBaoxiuCardsInDatabaseForAccount(ContentResolver cr, long uid) {
		int deleted = cr.delete(BjnoteContent.MyCarCards.CONTENT_URI, WHERE_UID, new String[]{String.valueOf(uid)});
		DebugUtils.logD(TAG, "deleteAllBaoxiuCardsInDatabaseForAccount uid#" + uid + ", delete " + deleted);
		return deleted;
	}
	/**
	 * 删除某个account的全部保修卡
	 * @param cr
	 * @param uid
	 * @return
	 */
	public static int deleteBaoxiuCardInDatabaseForAccount(ContentResolver cr, long uid, long bid) {
		int deleted = cr.delete(BjnoteContent.MyCarCards.CONTENT_URI, UID_SID_SELECTION, new String[]{String.valueOf(uid), String.valueOf(bid)});
		DebugUtils.logD(TAG, "deleteBaoxiuCardInDatabaseForAccount bid#" + bid + ", delete " + deleted);
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
		return cr.query(BjnoteContent.MyCarCards.CONTENT_URI, PROJECTION, WHERE_UID, new String[]{String.valueOf(uid)}, PROJECTION[INDEX_SID]+" DESC");
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
			newBaoxiuCardObject.mBID = bid;
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
    	baoxiuCardObject.mBID = c.getLong(INDEX_SID);
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
    	
    	baoxiuCardObject.mMMOne =c.getString(KEY_CARD_MMONE);
    	baoxiuCardObject.mMMTwo =c.getString(KEY_CARD_MMTWO);
    	
    	baoxiuCardObject.mModifiedTime = c.getLong(INDEX_MODIFIED);
    	baoxiuCardObject.mPdfPath = c.getString(KEY_CARD_PDF_PATH);
    	
		return baoxiuCardObject;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		String[] selectionArgs =  new String[]{String.valueOf(mUID), String.valueOf(mBID)};
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
		values.put(PROJECTION[INDEX_SID], mBID);
		values.put(PROJECTION[INDEX_WY], mWY);
		
		values.put(PROJECTION[INDEX_4S_SHOP], m4SShopTel);
		values.put(PROJECTION[INDEX_WASHING_SHOP], m4SWashingShopTel);
		values.put(PROJECTION[INDEX_WEIXIU_SHOP], mWeixiuShopTel);
		values.put(PROJECTION[INDEX_CHUXIAN_SHOP], mChuxianShopTel);
		values.put(PROJECTION[INDEX_KY], mKY);
		values.put(PROJECTION[INDEX_PKY], mPKY);
		values.put(PROJECTION[INDEX_MODIFIED], mModifiedTime);
		
		values.put(PROJECTION[KEY_CARD_MMONE], mMMOne);
		values.put(PROJECTION[KEY_CARD_MMTWO], mMMTwo);
		
		boolean op = false;
		if (id > 0) {
			int update = cr.update(BjnoteContent.MyCarCards.CONTENT_URI, values,  UID_SID_SELECTION, selectionArgs);
			if (update > 0) {
				DebugUtils.logD(TAG, "saveInDatebase update exsited bid#" + mBID);
				op = true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly update exsited bid#" + mBID);
			}
		} else {
			Uri uri = cr.insert(BjnoteContent.MyCarCards.CONTENT_URI, values);
			if (uri != null) {
				DebugUtils.logD(TAG, "saveInDatebase insert bid#" + mBID);
				mId = ContentUris.parseId(uri);
				op = true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly insert bid#" + mBID);
			}
		}
		if (op) {
			DebugUtils.logD(TAG, "saveInDatebase save RelationshipObject");
			if (mMMOneRelationshipObject != null) {
				mMMOneRelationshipObject.saveInDatebase(cr, null);
			}
			if (mMMTwoRelationshipObject != null) {
				mMMTwoRelationshipObject.saveInDatebase(cr, null);
			}
		}
		return op;
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
		return haveDay;
	}
	
	public static int getValidityDay(int rangeDay, String timeStamp) {
		if (TextUtils.isEmpty(timeStamp)) {
			return 0;
		}
		return getValidityDay(rangeDay, Long.valueOf(timeStamp));
	}
}
