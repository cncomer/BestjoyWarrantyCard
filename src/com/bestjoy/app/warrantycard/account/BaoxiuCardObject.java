package com.bestjoy.app.warrantycard.account;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.im.RelationshipObject;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
/**
 * 保修卡对象
 * @author chenkai
 * 
 * "baoxiu": [
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
public class BaoxiuCardObject extends IBaoxiuCardObject {
	public static final String JSONOBJECT_NAME = "baoxiu";
	public static final String TAG = "BaoxiuCardObject";
	/**发票的名字*/
	public String mFPName = "";
	/**主要配件保修，浮点值*/
	public String mZhuBx = "0";
	public long mAID = -1;
	
	private int mZhengjiValidity = -1, mComponentValidity = -1;
	
	public static final int KEY_CARD_ID = 0;
	public static final int KEY_CARD_UID = 1;
	public static final int KEY_CARD_AID = 2;
	public static final int KEY_CARD_BID = 3;
	public static final int KEY_CARD_DATE = 4;
	public static final int KEY_CARD_NAME = 5;
	public static final int KEY_CARD_TYPE = 6;
	public static final int KEY_CARD_PINPAI = 7;
	public static final int KEY_CARD_SERIAL = 8;
	public static final int KEY_CARD_MODEL = 9;
	public static final int KEY_CARD_BXPhone = 10;
	public static final int KEY_CARD_FPaddr = 11;
	public static final int KEY_CARD_FPname = 12; //add by chenkai, FaPiao's name.
	public static final int KEY_CARD_CARD_PRICE = 13;
	public static final int KEY_CARD_BUT_DATE = 14;
	public static final int KEY_CARD_BUY_TUJING = 15;
	public static final int KEY_CARD_YANBAO_TIME = 16;
	public static final int KEY_CARD_YANBAO_TIME_COMPANY = 17;
	public static final int KEY_CARD_WY = 18;
	public static final int KEY_CARD_YBPHONE = 19;
	public static final int KEY_CARD_KY = 20;
	public static final int KEY_CARD_PKY = 21;
	
	public static final int KEY_CARD_MMONE = 22;
	public static final int KEY_CARD_MMTWO = 23;
	
	
	public static final String WHERE_AID = HaierDBHelper.CARD_AID + "=?";
	public static final String WHERE_BID = HaierDBHelper.CARD_BID + "=?";
	public static final String WHERE_UID_AND_AID = WHERE_UID + " and " + WHERE_AID;
	public static final String WHERE_UID_AND_AID_AND_BID = WHERE_UID_AND_AID + " and " + WHERE_BID;
	
	public static BaoxiuCardObject parseBaoxiuCards(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		BaoxiuCardObject cardObject = new BaoxiuCardObject();
		cardObject.mLeiXin = jsonObject.getString("LeiXin");
		cardObject.mPinPai = jsonObject.getString("PinPai");
		cardObject.mXingHao = jsonObject.getString("XingHao");
		cardObject.mSHBianHao = jsonObject.getString("SHBianHao");
		
		cardObject.mBXPhone = jsonObject.getString("BXPhone");
		
		String buyDate = jsonObject.getString("BuyDate");
		if(buyDate != null) {
			//2010-01-01
			cardObject.mBuyDate = buyDate.replaceAll("[ -]", "");
			DebugUtils.logD(TAG, "reset BuyDate from " + buyDate + " to " + cardObject.mBuyDate);
		} else {
			cardObject.mBuyDate = "";
		}
		cardObject.mBuyPrice = jsonObject.getString("BuyPrice");
		
		cardObject.mBuyTuJing = jsonObject.getString("BuyTuJing");
		cardObject.mYanBaoTime = jsonObject.getString("YanBaoTime");
		if ("null".equals(cardObject.mYanBaoTime)) {
			cardObject.mYanBaoTime = "0";
		}
		cardObject.mYanBaoDanWei = jsonObject.getString("YanBaoDanWei");
		
		cardObject.mCardName = jsonObject.getString("Tag");
		//delete by chenkai, 不要ZhuBx字段了 begin
		//cardObject.mZhuBx = jsonObject.getString("ZhuBx");
		//if ("null".equals(cardObject.mZhuBx)) {
			//cardObject.mZhuBx = "0";
		//}
		//delete by chenkai, 不要ZhuBx字段了 end
		
		cardObject.mUID = jsonObject.getLong("UID");
		cardObject.mAID = jsonObject.getLong("AID");
		cardObject.mBID = jsonObject.getLong("BID");
		cardObject.mWY = jsonObject.getString("WY");
		if ("null".equals(cardObject.mWY)) {
			cardObject.mWY = "0";
		}
		cardObject.mYBPhone = jsonObject.getString("YBPhone");
		cardObject.mKY = jsonObject.getString("KY");
		if ("null".equalsIgnoreCase(cardObject.mKY)) {
			DebugUtils.logE(TAG, "parseBaoxiuCards find illegal value " + cardObject.mKY + " for ky");
			cardObject.mKY = "";
		}
		//解码发票，如果有的话
		//delete by chenkai, 现在FPaddr不再返回数据了，而是使用hasimg来表示是否存在发票图片 begin
		//cardObject.mFPaddr = jsonObject.getString("FPaddr");
		//decodeFapiao(cardObject);
		boolean hasimg = jsonObject.optBoolean("hasimg", false);
		cardObject.mFPaddr = hasimg ? "1" : "0";
		//delete by chenkai, 现在FPaddr不再返回数据了，而是使用hasimg来表示是否存在发票图片 end
		cardObject.mFPaddr = jsonObject.optString("imgaddr", "");
		cardObject.mFPName = jsonObject.optString("imgstr", "");
		cardObject.mPKY = jsonObject.optString("pky", BaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY);
		if ("null".equalsIgnoreCase(cardObject.mPKY)) {
			cardObject.mPKY = BaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY;
		}
		
		JSONObject mmone = jsonObject.optJSONObject("MMOne");
		if (mmone != null) {
			cardObject.mMMOneRelationshipObject = RelationshipObject.parse(mmone);
		}
		
		mmone = jsonObject.optJSONObject("MMTwo");
		if (mmone != null) {
			cardObject.mMMTwoRelationshipObject = RelationshipObject.parse(mmone);
		}
		return cardObject;
	}
	
	public BaoxiuCardObject clone() {
		BaoxiuCardObject newBaoxiuCardObject = new BaoxiuCardObject();
		super.clone(newBaoxiuCardObject);
		newBaoxiuCardObject.mAID = mAID;
		newBaoxiuCardObject.mZhuBx = mZhuBx;
		newBaoxiuCardObject.mFPName = mFPName;
		return newBaoxiuCardObject;
	}
	/**
	 * 获取保修卡对象的Bundle对象
	 * @return
	 */
	public Bundle getBaoxiuCardObjectBundle() {
		Bundle bundle = new Bundle();
		super.populateBaoxiuCardObjectBundle(bundle);
		bundle.putLong("aid", mAID);
		bundle.putString("mZhuBx", mZhuBx);
		bundle.putString("mFPName", mFPName);
		return bundle;
	}
	
	public static BaoxiuCardObject getBaoxiuCardObjectFromBundle(Bundle bundle) {
		BaoxiuCardObject baoxiuCardObject = new BaoxiuCardObject();
		populateBaoxiuCardObjectFromBundle(bundle, baoxiuCardObject);
		baoxiuCardObject.mAID = bundle.getLong("aid", -1);
		baoxiuCardObject.mZhuBx = bundle.getString("mZhuBx");
		baoxiuCardObject.mFPName = bundle.getString("mFPName");
		
		return baoxiuCardObject;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Leixing:").append(mLeiXin).append(", Pinpai:").append(mPinPai)
		.append(", XingHao:").append(mXingHao).append(", BianHao:").append(mSHBianHao).append("]");
		return sb.toString();
	}
	
	public static int deleteBaoxiuCardInDatabaseForAccount(ContentResolver cr, long uid, long aid, long bid) {
		int deleted = cr.delete(BjnoteContent.BaoxiuCard.CONTENT_URI, WHERE_UID_AND_AID_AND_BID, new String[]{String.valueOf(uid), String.valueOf(aid), String.valueOf(bid)});
		DebugUtils.logD(TAG, "deleteBaoxiuCardInDatabaseForAccount bid#" + bid + ", delete " + deleted);
		return deleted;
	}
	
	/**
	 * 删除某个account的全部保修卡
	 * @param cr
	 * @param uid
	 * @return
	 */
	public static int deleteAllBaoxiuCardsInDatabaseForAccount(ContentResolver cr, long uid) {
		int deleted = cr.delete(BjnoteContent.BaoxiuCard.CONTENT_URI, WHERE_UID, new String[]{String.valueOf(uid)});
		DebugUtils.logD(TAG, "deleteAllBaoxiuCardsInDatabaseForAccount uid#" + uid + ", delete " + deleted);
		return deleted;
	}
	 public static int getAllBaoxiuCardsCount(ContentResolver cr, long uid, long aid) {
		 Cursor c = getAllBaoxiuCardsCursor(cr, uid, aid);
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
    public static Cursor getAllBaoxiuCardsCursor(ContentResolver cr, long uid, long aid) {
		return cr.query(BjnoteContent.BaoxiuCard.CONTENT_URI, PROJECTION, WHERE_UID_AND_AID, new String[]{String.valueOf(uid), String.valueOf(aid)}, HaierDBHelper.CARD_BID+" DESC");
	}
    
    public static BaoxiuCardObject getBaoxiuCardObject(ContentResolver cr, long uid, long aid, long bid) {
    	BaoxiuCardObject object = null;
		Cursor c = cr.query(BjnoteContent.BaoxiuCard.CONTENT_URI, PROJECTION, WHERE_UID_AND_AID_AND_BID, new String[]{String.valueOf(uid), String.valueOf(aid), String.valueOf(bid)}, null);
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
    public static BaoxiuCardObject getBaoxiuCardObject(Bundle bundle) {
    	long aid = bundle.getLong("aid", -1);
		long bid = bundle.getLong("bid", -1);
		long uid = bundle.getLong("uid", -1);
		DebugUtils.logD(TAG, "getBaoxiuCardObject() bundle = " + bundle);
		if (uid != -1 && bid != -1 && aid != -1) {
			DebugUtils.logD(TAG, "getBaoxiuCardObject() get BaoxiuCardObject from Database");
			return BaoxiuCardObject.getBaoxiuCardObject(MyApplication.getInstance().getContentResolver(), uid, aid, bid);
		} else {
			
			BaoxiuCardObject newBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObjectFromBundle(bundle);
			newBaoxiuCardObject.mAID = aid;
			newBaoxiuCardObject.mUID = uid;
			newBaoxiuCardObject.mBID = bid;
			DebugUtils.logD(TAG, "getBaoxiuCardObject() new BaoxiuCardObject=" + newBaoxiuCardObject);
			return newBaoxiuCardObject;
		}
    }
    public static List<BaoxiuCardObject> getAllBaoxiuCardObjects(ContentResolver cr, long uid, long aid) {
		Cursor c = getAllBaoxiuCardsCursor(cr, uid, aid);
		List<BaoxiuCardObject> list = new ArrayList<BaoxiuCardObject>();
		if (c != null) {
			list = new ArrayList<BaoxiuCardObject>(c.getCount());
			while(c.moveToNext()) {
				list.add(getFromBaoxiuCardsCursor(c));
			}
			c.close();
		}
		return list;
	}
    
    public static BaoxiuCardObject getFromBaoxiuCardsCursor(Cursor c) {
    	BaoxiuCardObject baoxiuCardObject = new BaoxiuCardObject();
    	baoxiuCardObject.mId = c.getLong(KEY_CARD_ID);
    	baoxiuCardObject.mUID = c.getLong(KEY_CARD_UID);
    	baoxiuCardObject.mAID = c.getLong(KEY_CARD_AID);
    	baoxiuCardObject.mBID = c.getLong(KEY_CARD_BID);
    	baoxiuCardObject.mLeiXin = c.getString(KEY_CARD_TYPE);
    	baoxiuCardObject.mPinPai = c.getString(KEY_CARD_PINPAI);
    	baoxiuCardObject.mXingHao = c.getString(KEY_CARD_MODEL);
    	baoxiuCardObject.mSHBianHao = c.getString(KEY_CARD_SERIAL);
    	baoxiuCardObject.mBXPhone = c.getString(KEY_CARD_BXPhone);
    	baoxiuCardObject.mFPaddr = c.getString(KEY_CARD_FPaddr);
    	baoxiuCardObject.mFPName = c.getString(KEY_CARD_FPname);
    	baoxiuCardObject.mBuyDate = c.getString(KEY_CARD_BUT_DATE);
    	baoxiuCardObject.mBuyPrice = c.getString(KEY_CARD_CARD_PRICE);
    	baoxiuCardObject.mBuyTuJing = c.getString(KEY_CARD_BUY_TUJING);
    	baoxiuCardObject.mYanBaoTime = c.getString(KEY_CARD_YANBAO_TIME);
    	baoxiuCardObject.mYanBaoDanWei = c.getString(KEY_CARD_YANBAO_TIME_COMPANY);
    	baoxiuCardObject.mCardName = c.getString(KEY_CARD_NAME);
    	baoxiuCardObject.mWY = c.getString(KEY_CARD_WY);
    	
    	baoxiuCardObject.mYBPhone = c.getString(KEY_CARD_YBPHONE);
    	baoxiuCardObject.mKY = c.getString(KEY_CARD_KY);
    	baoxiuCardObject.mPKY = c.getString(KEY_CARD_PKY);
    	
    	baoxiuCardObject.mMMOne =c.getString(KEY_CARD_MMONE);
    	baoxiuCardObject.mMMTwo =c.getString(KEY_CARD_MMTWO);
    	
    	baoxiuCardObject.mModifiedTime = c.getLong(KEY_CARD_DATE);
		return baoxiuCardObject;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		String[] selectionArgs =  new String[]{String.valueOf(mUID), String.valueOf(mAID), String.valueOf(mBID)};
		long id = isExsited(cr,selectionArgs);
		values.put(PROJECTION[KEY_CARD_NAME], mCardName);
		values.put(PROJECTION[KEY_CARD_TYPE], mLeiXin);
		values.put(PROJECTION[KEY_CARD_PINPAI], mPinPai);
		values.put(PROJECTION[KEY_CARD_MODEL], mXingHao);
		values.put(PROJECTION[KEY_CARD_SERIAL], mSHBianHao);
		values.put(PROJECTION[KEY_CARD_BXPhone], mBXPhone);
		values.put(PROJECTION[KEY_CARD_FPaddr], mFPaddr);
		values.put(PROJECTION[KEY_CARD_FPname], mFPName);
		values.put(PROJECTION[KEY_CARD_BUT_DATE], mBuyDate);
		values.put(PROJECTION[KEY_CARD_CARD_PRICE], mBuyPrice);
		values.put(PROJECTION[KEY_CARD_BUY_TUJING], mBuyTuJing);
		
		values.put(PROJECTION[KEY_CARD_YANBAO_TIME], mYanBaoTime);
		values.put(PROJECTION[KEY_CARD_YANBAO_TIME_COMPANY], mYanBaoDanWei);
		
		values.put(PROJECTION[KEY_CARD_WY], mWY);
		values.put(PROJECTION[KEY_CARD_YBPHONE], mYBPhone);
		values.put(PROJECTION[KEY_CARD_KY], mKY);
		
		values.put(PROJECTION[KEY_CARD_PKY], mPKY);
		values.put(PROJECTION[KEY_CARD_MMONE], mMMOne);
		values.put(PROJECTION[KEY_CARD_MMTWO], mMMTwo);
		values.put(PROJECTION[KEY_CARD_DATE], new Date().getTime());
		
		if (id > 0) {
			int update = cr.update(BjnoteContent.BaoxiuCard.CONTENT_URI, values,  WHERE_UID_AND_AID_AND_BID, selectionArgs);
			if (update > 0) {
				DebugUtils.logD(TAG, "saveInDatebase update exsited bid#" + mBID);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly update exsited bid#" + mBID);
			}
		} else {
			//如果不存在，新增的时候需要增加uid aid bid值
			values.put(HaierDBHelper.CARD_UID, mUID);
			values.put(HaierDBHelper.CARD_AID, mAID);
			values.put(HaierDBHelper.CARD_BID, mBID);
			Uri uri = cr.insert(BjnoteContent.BaoxiuCard.CONTENT_URI, values);
			if (uri != null) {
				DebugUtils.logD(TAG, "saveInDatebase insert bid#" + mBID);
				mId = ContentUris.parseId(uri);
				return true;
			} else {
				DebugUtils.logD(TAG, "saveInDatebase failly insert bid#" + mBID);
			}
		}
		return false;
	}
	
	private long isExsited(ContentResolver cr, String[] selectionArgs) {
		long id = -1;
		Cursor c = cr.query(BjnoteContent.BaoxiuCard.CONTENT_URI, PROJECTION, WHERE_UID_AND_AID_AND_BID, selectionArgs, null);
		if (c != null) {
			if (c.moveToNext()) {
				id = c.getLong(KEY_CARD_BID);
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
				Date buyDate = BUY_DATE_TIME_FORMAT.parse(mBuyDate);
				//当前日期
				Date now = new Date();
				long passedTimeLong = now.getTime() - buyDate.getTime();
				if (passedTimeLong < 0) {
					passedTimeLong = 0;
				}
				int passedDay = (int) (passedTimeLong / DAY_IN_MILLISECONDS);
				mZhengjiValidity = validity - passedDay;
			} catch (ParseException e) {
				e.printStackTrace();
				mZhengjiValidity = 0;
			} catch (NumberFormatException e) {
				DebugUtils.logE(TAG, "getBaoxiuValidity() NumberFormatException " + e.getMessage()); 
				e.printStackTrace();
				mZhengjiValidity = 0;
			}
		}
		return mZhengjiValidity;
	}
	
	/**
	 * 返回该保修对象的主要部件保修有效期天数，计算保修有效期公式 = 保修天数-已买天数
	 * @return
	 */
	public int getComponentBaoxiuValidity() {
		if (mComponentValidity == -1) {
			if (TextUtils.isEmpty(mZhuBx)) {
				//可能会是空的字串，我们就当做0天
				mComponentValidity = 0;
			} else {
				int validity = (int) (Float.valueOf(mZhuBx) * 365);
				try {
					//转换购买日期
					Date buyDate = BUY_DATE_TIME_FORMAT.parse(mBuyDate);
					//当前日期
					Date now = new Date();
					long passedTimeLong = now.getTime() - buyDate.getTime();
					if (passedTimeLong < 0) {
						passedTimeLong = 0;
					}
					int passedDay = (int) (passedTimeLong / DAY_IN_MILLISECONDS);
					mComponentValidity = validity - passedDay;
				} catch (ParseException e) {
					e.printStackTrace();
					mComponentValidity = 0;
				}
			}
			
		}
		return mComponentValidity;
	}

}
