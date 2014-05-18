package com.shwy.bestjoy.bjnote.mylife;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;

public class MyLifeObject implements InfoInterface {
	public static final String TAG = "MyLifeObject";

	/**活动日期格式yyyy-MM-dd*/
	public static  DateFormat XF_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //2013/8/3 7:26:46
	public long mId = -1;
	public String mComMm;
	/**公司名称*/
	public String mComName;
	/**公司电话*/
	public String mComCell;
	/**公司地址*/
	public String mAddress;
	/**公司网址*/
	public String mWebsite;
	/**最新信息*/
	public String mNewsNote;
	/**本机号码*/
	public String mTel;
	/**当前账号*/
	public String mAccountMd;
	public long mDate;
	/**我的消费记录情况*/
	MyLifeConsumeRecordsObjectHolder mMyLifeConsumeRecordsObjectHolder = new MyLifeConsumeRecordsObjectHolder();
	/**我的消费备注*/
	public String mXFNotes;
	
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
	
	/**广告*/
	public String mLiveInfo;
	
	
	public boolean saveConsumeNotes(ContentResolver cr) {
		ContentValues values = new ContentValues(3);
		values.put(HaierDBHelper.MYLIFE_COM_XIAOFEI_NOTES, mXFNotes);
		int updated = cr.update(BjnoteContent.MyLife.CONTENT_URI, values, MyLifeListAdapter.ID_WHERE, new String[]{String.valueOf(mId)});
		DebugUtils.logContactAsyncDownload(TAG, "saveConsumeNotes updated " + (updated > 0));
		return updated > 0;
	}
	/***
     * 
     * @param cr
     * @param addtion 必须包含ContactsDBHelper.CONTACT_TEL, ContactsDBHelper.ACCOUNT_MD
     * @return
     */
    public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
    	ContentValues values = new ContentValues(11);
    	if (addtion != null) {
    		values.putAll(addtion);
    		mTel = addtion.getAsString(HaierDBHelper.CONTACT_TEL);
    		mAccountMd = addtion.getAsString(HaierDBHelper.ACCOUNT_UID);
    		if (TextUtils.isEmpty(mTel)) {
    			throw new RuntimeException(TAG + " saveInDatebase " +" param addtion must containsKey ContactsDBHelper.CONTACT_TEL");
    		}
    		if (TextUtils.isEmpty(mAccountMd)) {
    			throw new RuntimeException(TAG + " saveInDatebase " +" param addtion must containsKey ContactsDBHelper.ACCOUNT_MD");
    		}
    	}
		values.put(HaierDBHelper.MYLIFE_COM_CELL, mComCell);
		values.put(HaierDBHelper.MYLIFE_COM_NEWS, mNewsNote);
		values.put(HaierDBHelper.CONTACT_ADDRESS, mAddress);
		values.put(HaierDBHelper.CONTACT_NAME, mComName);
		StringBuilder sb = new StringBuilder();
		boolean head = true;
		for (MyLifeConsumeRecordsObject each : mMyLifeConsumeRecordsObjectHolder.mMyLifeConsumeRecordsObjectList) {
			//由于第一次我们保存的时候需要添加一个头标记，所以第一次后我们就要将其置为false了
			sb.append(each.toSaveString(head));
			if (head) {
				head = false;
			}
		}
		values.put(HaierDBHelper.MYLIFE_COM_XF, sb.toString());
		
		values.put(HaierDBHelper.MYLIFE_TOTAL_JF, mMyLifeConsumeRecordsObjectHolder.mTotalMoney);
		values.put(HaierDBHelper.MYLIFE_FREE_JF, mMyLifeConsumeRecordsObjectHolder.mTotaljifen);
		
		values.put(HaierDBHelper.MYLIFE_COM_XIAOFEI_NOTES, mXFNotes);
		values.put(HaierDBHelper.MYLIFE_COM_WEBSITE, mWebsite);
		values.put(HaierDBHelper.MYLIFE_GUANGGAO, mLiveInfo);
		values.put(HaierDBHelper.CONTACT_DATE, System.currentTimeMillis());
    	if (!isExsitedTopic(cr, mComMm, mAccountMd, mTel)) {
    		values.put(HaierDBHelper.ACCOUNT_UID, mAccountMd);
    		values.put(HaierDBHelper.CONTACT_BID, mComMm);
    		values.put(HaierDBHelper.CONTACT_TEL, mTel);
    		Uri uri = cr.insert(BjnoteContent.MyLife.CONTENT_URI, values);
    		return uri != null;
    	} else {
    		DebugUtils.logContactAsyncDownload(TAG, mComMm + " has existed in datebase, we just update it ");
    		int updated = cr.update(BjnoteContent.MyLife.CONTENT_URI, values, MyLifeListAdapter.ACCOUNTMD_TEL_COMMM_WHERE, new String[]{mAccountMd, mTel, mComMm});
    		return updated > 0;
    	}
    }
    
    public static boolean isExsitedTopic(ContentResolver cr, String comMm, String accountMd, String tel) {
    	Cursor cursor = cr.query(BjnoteContent.MyLife.CONTENT_URI, BjnoteContent.ID_PROJECTION, MyLifeListAdapter.ACCOUNTMD_TEL_COMMM_WHERE, new String[]{accountMd, tel, comMm}, null);
    	boolean exsited = cursor != null && cursor.getCount() > 0;
    	if (cursor != null) {
    		cursor.close();
    	}
    	return exsited;
    }
    
    public static long getExsitedTopic(ContentResolver cr, String comMm, String accountMd, String tel) {
    	Cursor cursor = cr.query(BjnoteContent.MyLife.CONTENT_URI, BjnoteContent.ID_PROJECTION, MyLifeListAdapter.ACCOUNTMD_TEL_COMMM_WHERE, new String[]{accountMd, tel, comMm}, null);
    	long id = -1;
    	if (cursor != null && cursor.moveToNext()) {
    		id = cursor.getLong(0);
    	}
    	if (cursor != null) {
    		cursor.close();
    	}
    	return id;
    }
	
	public String toString() {
		StringBuilder sb = new StringBuilder(TAG);
		sb.append("[mComMm=").append(mComMm)
		.append(" mComName=").append(mComName).append("]");
		return sb.toString();
	}
}
