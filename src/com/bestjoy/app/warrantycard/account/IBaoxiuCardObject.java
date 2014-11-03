package com.bestjoy.app.warrantycard.account;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.im.RelationshipObject;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.Base64;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.ImageHelper;
import com.shwy.bestjoy.utils.InfoInterfaceImpl;
import com.shwy.bestjoy.utils.SecurityUtils;
public abstract class IBaoxiuCardObject extends InfoInterfaceImpl {
	public static final String TAG = "IBaoxiuCardObject";
	//add by chenkai for FaPiao end
	public static  DateFormat BUY_DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMdd");
	public static  DateFormat BUY_DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
	public static  DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy年MM月dd日");
	public static  DateFormat BUY_DATE_FORMAT_TIME = new SimpleDateFormat("HH:mm");
	public static  DateFormat BUY_DATE_FORMAT_YUYUE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static long DAY_IN_MILLISECONDS = 1000 * 60 * 60 * 24;
	
	//用于tip
	public static  DateFormat DATE_FORMAT_YUYUE_TIME = new SimpleDateFormat("yyyyMMddHHmmss");
	/**用于详细界面显示发票日期*/
	public static  DateFormat DATE_FORMAT_FAPIAO_TIME = new SimpleDateFormat("yyyy.MM.dd");
	public String mLeiXin="";
	public String mPinPai="";
	public String mXingHao="";
	public String mSHBianHao="";
	public String mBXPhone="";
	/**这个变量的值为0,1，表示是否有发票*/
	public String mFPaddr = "";
	public String mBuyDate="";
	public String mBuyPrice="";
	public String mBuyTuJing="";
	/**整机保修时间，浮点型，默认是1年*/
	public String mWY = "1";
	public String mYanBaoTime = "0";
	public String mYanBaoDanWei="";
	/**用户定义的保修设备名称，如客厅电视机*/
	public String mCardName="";
	public String mYBPhone="";
	public String mKY="";
	/**用来构建保修卡设备预览图，如mPKY.jpg*/
	public String mPKY="";
	/**本地id*/
	public long mId = -1;
	public long mUID = -1;
	public long mBID = -1;
	
	/**对应关系表中的service_id*/
	public String mMMOne="", mMMTwo="";
	RelationshipObject mMMOneRelationshipObject;
	RelationshipObject mMMTwoRelationshipObject;
	
	public long mModifiedTime = new Date().getTime();
	
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
		HaierDBHelper.DATA22,        //23 ky
		HaierDBHelper.DATA23,        //24 pky
		HaierDBHelper.DATA24,        //25 mmone
		HaierDBHelper.DATA25,        //26 mmTwo
		HaierDBHelper.DATA26,        //27
	};
	
	public static final String WHERE_UID = HaierDBHelper.ACCOUNT_UID + "=?";
	
	/**这个值用作不同Activity之间的传递，如选择设备的时候*/
	private static IBaoxiuCardObject mBaoxiuCardObject = null;
	/**如果服务器返回的保修卡数据中pky字段是000,则表示该保修卡没有设备预览图，直接显示本地的ky_default.jpg*/
	public static final String DEFAULT_BAOXIUCARD_IMAGE_KEY = "000";
	
	
	public void clone(IBaoxiuCardObject newBaoxiuCardObject) {
		newBaoxiuCardObject.mId = mId;
		newBaoxiuCardObject.mUID = mUID;
		newBaoxiuCardObject.mBID = mBID;
		
		newBaoxiuCardObject.mWY = mWY;
		
		newBaoxiuCardObject.mCardName = mCardName;
		
		newBaoxiuCardObject.mLeiXin = mLeiXin;
		newBaoxiuCardObject.mPinPai = mPinPai;
		newBaoxiuCardObject.mXingHao = mXingHao;
		newBaoxiuCardObject.mSHBianHao = mSHBianHao;
		
		newBaoxiuCardObject.mBXPhone = mBXPhone;
		newBaoxiuCardObject.mFPaddr = mFPaddr;
		newBaoxiuCardObject.mBuyDate = mBuyDate;
		newBaoxiuCardObject.mBuyPrice = mBuyPrice;
		newBaoxiuCardObject.mBuyTuJing = mBuyTuJing;
		newBaoxiuCardObject.mYanBaoTime = mYanBaoTime;
		newBaoxiuCardObject.mYanBaoDanWei = mYanBaoDanWei;
		newBaoxiuCardObject.mYBPhone = mYBPhone;
		newBaoxiuCardObject.mKY = mKY;
		newBaoxiuCardObject.mPKY = mPKY;
		newBaoxiuCardObject.mModifiedTime = mModifiedTime;
	}
	/**
	 * 获取保修卡对象的Bundle对象
	 * @return
	 */
	public Bundle populateBaoxiuCardObjectBundle(Bundle bundle) {
		bundle.putLong("id", mId);
		bundle.putLong("uid", mUID);
		bundle.putLong("bid", mBID);
		bundle.putString("wy", mWY);
		bundle.putString("mCardName", mCardName);
		
		bundle.putString("mLeiXin", mLeiXin);
		bundle.putString("mPinPai", mPinPai);
		bundle.putString("mXingHao", mXingHao);
		
		bundle.putString("mSHBianHao", mSHBianHao);
		bundle.putString("mBXPhone", mBXPhone);
		bundle.putString("mFPaddr", mFPaddr);
		
		bundle.putString("mBuyDate", mBuyDate);
		bundle.putString("mBuyPrice", mBuyPrice);
		
		bundle.putString("mBuyTuJing", mBuyTuJing);
		bundle.putString("mYanBaoTime", mYanBaoTime);
		bundle.putString("mYanBaoDanWei", mYanBaoDanWei);
		
		bundle.putString("mYBPhone", mYBPhone);
		bundle.putString("mKY", mKY);
		bundle.putString("mPKY", mPKY);
		
		bundle.putString("mMMOne", mMMOne);
		bundle.putString("mMMTwo", mMMTwo);
		return bundle;
	}
	
	public static void populateBaoxiuCardObjectFromBundle(Bundle bundle, IBaoxiuCardObject baoxiuCardObject) {
		bundle = bundle.getBundle(TAG);
		if (bundle != null) {
			baoxiuCardObject.mId = bundle.getLong("id", -1);
			baoxiuCardObject.mUID = bundle.getLong("uid", -1);
			baoxiuCardObject.mBID = bundle.getLong("bid", -1);
			baoxiuCardObject.mWY = bundle.getString("wy");
			baoxiuCardObject.mCardName = bundle.getString("mCardName");
			baoxiuCardObject.mLeiXin = bundle.getString("mLeiXin");
			baoxiuCardObject.mPinPai = bundle.getString("mPinPai");
			baoxiuCardObject.mXingHao = bundle.getString("mXingHao");
			
			baoxiuCardObject.mSHBianHao = bundle.getString("mSHBianHao");
			baoxiuCardObject.mBXPhone = bundle.getString("mBXPhone");
			baoxiuCardObject.mFPaddr = bundle.getString("mFPaddr");
			baoxiuCardObject.mBuyDate = bundle.getString("mBuyDate");
			
			baoxiuCardObject.mBuyPrice = bundle.getString("mBuyPrice");
			baoxiuCardObject.mBuyTuJing = bundle.getString("mBuyTuJing");
			baoxiuCardObject.mYanBaoTime = bundle.getString("mYanBaoTime");
			baoxiuCardObject.mYanBaoDanWei = bundle.getString("mYanBaoDanWei");
			baoxiuCardObject.mYBPhone = bundle.getString("mYBPhone");
			
			baoxiuCardObject.mKY = bundle.getString("mKY");
			baoxiuCardObject.mPKY = bundle.getString("mPKY");
			
			baoxiuCardObject.mMMOne = bundle.getString("mMMOne");
			baoxiuCardObject.mMMTwo = bundle.getString("mMMTwo");
		}
	}
	
	/**
	 * 当我们设置过mBaoxiuCardObject值后，需要使用这个方法来获取，这会重置mBaoxiuCardObject对象为null.
	 * @return
	 */
	public static IBaoxiuCardObject getBaoxiuCardObject() {
		IBaoxiuCardObject object = null;
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
	public static void setBaoxiuCardObject(IBaoxiuCardObject baoxiucardObject) {
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
	
	public static IBaoxiuCardObject objectUseForbill = null;
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
		if (hasBillAvator()) {
			return getFapiaoPhotoIdFromFpAddr(mFPaddr);
		}
		DebugUtils.logD(TAG, "getFapiaoPhotoId() " + PHOTOID_PLASEHOLDER);
		return PHOTOID_PLASEHOLDER;
	}
	
	public static String getFapiaoPhotoIdFromFpAddr(String fPaddr) {
		int indexStart = fPaddr.lastIndexOf("/");
		int indexEnd = fPaddr.lastIndexOf(".");
		if (indexStart > 0 && indexEnd > 0) {
			String photoId =  fPaddr.substring(indexStart+1, indexEnd);
			DebugUtils.logD(TAG, "getFapiaoPhotoId(String fPaddr) " + photoId + " from fPaddr " + fPaddr);
			return photoId;
		}
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
	
	public static void showBill(Context context, IBaoxiuCardObject baociuCardObject) {
		objectUseForbill = baociuCardObject;
		if (baociuCardObject != null) {
			MyApplication.getInstance().showMessage(R.string.msg_wait_for_fapiao_show);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(BjnoteContent.BaoxiuCard.BILL_CONTENT_URI, "image/png");
			context.startActivity(intent);
		}
	}
	
	/**
	 * 当前时间精确到秒base64（20140514121212）
	 * @return
	 */
	public static String getYuyueSecurityTip(String timeStr) {
		String tip = "";
		try {
			DebugUtils.logD(TAG, "getYuyueSecurityTip getTime " + timeStr);
			tip = Base64.encodeToString(timeStr.getBytes("UTF-8"), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		DebugUtils.logD(TAG, "getYuyueSecurityTip getTip " + tip);
		return tip;
	}
	/**
	 * 加密后的字符（md5(md5(cell+tip))）
	 * @return
	 */
	public static String getYuyueSecurityKey(String cell, String tip) {
		String key = SecurityUtils.MD5.md5(cell+tip);
		DebugUtils.logD(TAG, "md5(cell+tip) " + key);
		key = SecurityUtils.MD5.md5(key);
		DebugUtils.logD(TAG, "md5(md5(cell+tip) " + key);
		return key;
	}
	
	/***
	 * 返回远程发票的绝对路径
	 * @return
	 */
	public String getFapiaoServicePath() {
		return mFPaddr;
	}

}
