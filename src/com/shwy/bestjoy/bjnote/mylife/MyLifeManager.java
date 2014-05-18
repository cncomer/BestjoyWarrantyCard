package com.shwy.bestjoy.bjnote.mylife;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.shwy.bestjoy.utils.Contents;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.NetworkUtils;

public class MyLifeManager {
	private static final String TAG = "MyLifeManager";
	/**http://www.mingdown.com/cell/del2B.ashx?m=商家MM|本机号码*/
	private static final String DELETE_SERVICE_BASE_URL = "http://www.mingdown.com/cell/del2B.ashx?m=";
	/**http://www.mingdown.com/ljzc.asmx/SHB para=urlencode(商家MM|本机号码), post的方式*/
	private static final String RECORD_DOWNLOAD_BASE_URL = "http://www.mingdown.com/ljzc.asmx/SHB";
	private Context mContext;
	
	private static final MyLifeManager INSTANCE = new MyLifeManager();
	private MyLifeManager(){};
	
	public void setContext(Context context) {
		mContext = context;
	}
	
	public static MyLifeManager getInstance() {
		return INSTANCE;
	}
	
	public boolean deleteMyLifeFromServiceLocked(MyLifeObject object) {
		StringBuilder sb = new StringBuilder();
		sb.append(object.mComMm).append("|").append(object.mTel);
		boolean result = false;
		InputStream is = null;
		try {
			is = NetworkUtils.openContectionLocked(DELETE_SERVICE_BASE_URL, sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			if (is != null) {
				String resultStr = NetworkUtils.getContentFromInput(is);
				if (MyApplication.mDeleteOk.equals(resultStr)) {
					result = true;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			NetworkUtils.closeInputStream(is);
		}
		return result;
	}
	/***
	 * 如果用户选择保存,调用该方法来记录本次下载
	 * @param comMm
	 * @param tel
	 * @return
	 */
	public boolean recordDownloadLocked(String comMm, String tel) {
		boolean result = false;
		if (TextUtils.isEmpty(tel)) {
			DebugUtils.logD(TAG, "tel is empty, so we just return true");
			return true;
		}
		StringBuilder sb = new StringBuilder(comMm);
		sb.append('|').append(tel).append('|');
		InputStream is = null;
		try {
			is = NetworkUtils.openPostContectionLocked(RECORD_DOWNLOAD_BASE_URL, "para", sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			String resultStr = NetworkUtils.getContentFromInput(is);
			//<string xmlns="http://tempuri.org/">ok</string>
			if ("OK".equalsIgnoreCase(resultStr)) {
				result = true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			NetworkUtils.closeInputStream(is);
		}
		DebugUtils.logD(TAG, "recordDownloadLocked " +result);
		return result;
	}
	/**
	 * 删除所有与账号相关的数据库数据
	 * @param accountMd
	 * @param tel
	 * @param mm
	 * @return
	 */
	public int deleteAllMyLifeDataFromDatabaseLocked(String accountMd, String tel) {
		  int deleted =  mContext.getContentResolver().delete(BjnoteContent.MyLife.CONTENT_URI, MyLifeListAdapter.ACCOUNTMD_TEL_WHERE, new String[]{accountMd, tel});
		  DebugUtils.logD(TAG, "deleteAllMyLifeDataFromDatabaseLocked accountMd=" + accountMd + ", tel=" + tel + ", deleted " + deleted);
		  return deleted;
	}
	/**
	 * 删除某个账号对应的手机的MM商家数据库数据
	 * @param accountMd 当前账户
	 * @param tel  账户默认手机号码
	 * @param mm  要删除的商家MM号码
	 * @return
	 */
	 public int deleteMyLifeFromDatabaseLocked(String accountMd, String tel, String comMm) {
		  int deleted =  mContext.getContentResolver().delete(BjnoteContent.MyLife.CONTENT_URI, MyLifeListAdapter.ACCOUNTMD_TEL_COMMM_WHERE, new String[]{accountMd, tel, comMm});
		  DebugUtils.logD(TAG, "deleteMyLifeFromDatabaseLocked accountMd=" + accountMd + ", tel=" + tel + ", comMm=" + comMm + ", deleted " + deleted);
		  return deleted;
	  }
	 
	 /**
	 * 创建商家联系人
	 * @param addressResult
	 * @return >0 表示创建或是更新成功了， -1 表示的是失败了， -2表示不是商家名片,不用处理商家名片的逻辑
	 */
    public final long createMerchantContactEntryLocked(AddressBookParsedResult addressResult) {
    	DebugUtils.logD(TAG, "begin createMerchantContactEntryLocked");
    	long id  = -2L;
    	if (addressResult.isMerchant()) {
    		MyLifeObject object = new MyLifeObject();
    		object.mAccountMd = MyAccountManager.getInstance().getCurrentAccountMd();;
    		object.mTel = MyAccountManager.getInstance().getDefaultPhoneNumber();
    		
    		object.mComMm = addressResult.getBid();
    		object.mComName = addressResult.getFirstName();
    		if (addressResult.hasPhoneNumbers()) {
    			object.mComCell = addressResult.getPhoneNumbers()[0];
    		}
    		if (addressResult.hasAddresses()) {
    			object.mAddress = addressResult.getAddresses()[0];
    		}
    		object.mNewsNote = addressResult.getNote();
    		String[] urls = addressResult.getURL();
    		if (urls != null) {
    			for(String url:urls) {
    				if (Contents.MingDang.isCloudUri(url) == null) {
    					object.mWebsite = url;
    					break;
    				}
    			}
    			if (TextUtils.isEmpty(object.mWebsite)) {
    				object.mWebsite = addressResult.getDirectCloudUrl();
    			}
    		}
    		ContentResolver cr = MyApplication.getInstance().getContentResolver();
    		ContentValues values = new ContentValues(11);
    		values.put(HaierDBHelper.MYLIFE_COM_CELL, object.mComCell);
    		values.put(HaierDBHelper.MYLIFE_COM_NEWS, object.mNewsNote);
    		values.put(HaierDBHelper.CONTACT_ADDRESS, object.mAddress);
    		values.put(HaierDBHelper.CONTACT_NAME, object.mComName);
    		values.put(HaierDBHelper.MYLIFE_COM_XIAOFEI_NOTES, object.mXFNotes);
    		values.put(HaierDBHelper.MYLIFE_COM_WEBSITE, object.mWebsite);
    		values.put(HaierDBHelper.CONTACT_DATE, System.currentTimeMillis());
    		id = MyLifeObject.getExsitedTopic(cr, object.mComMm, object.mAccountMd, object.mTel);
    		if (id  == -1) {
    			values.put(HaierDBHelper.ACCOUNT_UID, object.mAccountMd);
        		values.put(HaierDBHelper.CONTACT_BID, object.mComMm);
        		values.put(HaierDBHelper.CONTACT_TEL, object.mTel);
        		Uri uri = cr.insert(BjnoteContent.MyLife.CONTENT_URI, values);
        		if (uri != null) {
        			DebugUtils.logContactAsyncDownload(TAG, object.mComMm + " added in datebase ok, uri is " + uri);
        			id = ContentUris.parseId(uri);
        		}
    		} else {
    			int updated = cr.update(BjnoteContent.MyLife.CONTENT_URI, values, MyLifeListAdapter.ACCOUNTMD_TEL_COMMM_WHERE, new String[]{ object.mAccountMd, object.mTel, object.mComMm});
    			DebugUtils.logContactAsyncDownload(TAG, object.mComMm + " has existed in datebase, we just update it #updated " + updated);
    		}
    	}
		DebugUtils.logD(TAG, "finish createMerchantContactEntryLocked id= " + id);
		return id;
	}
    /**
     * www.mingdown.com/SHB?para=urlencode(商家MM|本机号码|消费记录),
     * @return
     */
    public boolean saveConsumeNotes(String comMm, String tel, String notes) {
    	boolean result = false;
		StringBuilder sb = new StringBuilder(comMm);
		sb.append('|').append(tel).append('|').append(notes);
		InputStream is = null;
		try {
			is = NetworkUtils.openPostContectionLocked(RECORD_DOWNLOAD_BASE_URL, "para", sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			String resultStr = NetworkUtils.getContentFromInput(is);
			//<string xmlns="http://tempuri.org/">ok</string>
			if (resultStr.contains("ok")) {
				result = true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			NetworkUtils.closeInputStream(is);
		}
		DebugUtils.logD(TAG, "saveConsumeNotes " +result);
		return result;
    }

}
