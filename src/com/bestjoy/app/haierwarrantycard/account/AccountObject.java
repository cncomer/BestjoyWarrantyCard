package com.bestjoy.app.haierwarrantycard.account;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.bestjoy.app.haierwarrantycard.database.BjnoteContent;
import com.bestjoy.app.haierwarrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;

public class AccountObject implements InfoInterface{
	private static final String TAG = "HaierAccount";
	
	private static final String[] PROJECTION = new String[]{
		HaierDBHelper.ID,
		HaierDBHelper.ACCOUNT_MD,
		HaierDBHelper.ACCOUNT_NAME,
		HaierDBHelper.ACCOUNT_TEL,
		HaierDBHelper.ACCOUNT_PWD,
		HaierDBHelper.ACCOUNT_CARD_COUNT,
		HaierDBHelper.ACCOUNT_HOME_COUNT,
	};
	
	private static final int KEY_ID = 0;
	private static final int KEY_MD = 1;
	private static final int KEY_NAME = 2;
	private static final int KEY_TEL = 3;
	private static final int KEY_PWD = 4;
	private static final int KEY_CARD_COUNT = 5;
	private static final int KEY_HOME_COUNT = 6;
	
	private static final String WHERE_DEFAULT = HaierDBHelper.ACCOUNT_DEFAULT + "=1";
	
	public long mAccountId;
	public Long mAccountUid;
	public String mAccountName;
	public String mAccountTel;
	public String mAccountPwd;
	public int mAccountCardCount;
	public int mAccountHomeCount;
	
	
	
	/**登陆或注册的时候会用到，表示当前的状态，statuscode:状态 1:成功   0：失败*/
	public int mStatusCode;
	/**登陆时候服务器返回的数据*/
	public String mStatusMessage;
	
	public List<HomeObject> mAccountHomes = new LinkedList<HomeObject>();
	
	public boolean isLogined() {
		return mStatusCode != 0;
	}
	
	public static AccountObject getHaierAccountFromDatabase(Context context) {
		AccountObject haierAccount = null;
		Cursor c = context.getContentResolver().query(BjnoteContent.Accounts.CONTENT_URI, PROJECTION, WHERE_DEFAULT, null, null);
		if (c != null) {
			if (c.moveToNext()) {
				haierAccount = new AccountObject();
				String idStr = c.getString(KEY_ID);
				if (TextUtils.isEmpty(idStr)) {
					DebugUtils.logD(TAG, "getHaierAccountFromDatabase accountId is " + idStr);
					return null;
				}
				haierAccount.mAccountId = Long.parseLong(idStr);
				DebugUtils.logD(TAG, "getHaierAccountFromDatabase accountId is " + haierAccount.mAccountId);
				if (haierAccount.mAccountId <= 0) {
					
				}
				haierAccount.mAccountUid = c.getLong(KEY_MD);
				haierAccount.mAccountName = c.getString(KEY_NAME);
				haierAccount.mAccountTel = c.getString(KEY_TEL);
				haierAccount.mAccountPwd = c.getString(KEY_PWD);
				haierAccount.mAccountCardCount = c.getInt(KEY_CARD_COUNT);
				haierAccount.mAccountHomeCount = c.getInt(KEY_HOME_COUNT);
			}
		}
		
		return haierAccount;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		// TODO Auto-generated method stub
		return false;
	}
	
}