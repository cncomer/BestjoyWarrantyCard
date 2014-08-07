package com.bestjoy.app.warrantycard.account;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.utils.DebugUtils;

public class MyAccountManager {
	private static final String TAG = "HaierAccountManager";
	private AccountObject mHaierAccount;
	private Context mContext;
	SharedPreferences mSharedPreferences;
	private static MyAccountManager mInstance = new MyAccountManager();
	
	private MyAccountManager() {}
	
	public static MyAccountManager getInstance() {
		return mInstance;
	}
	
	public void setContext(Context context) {
		mContext = context; 
		if (mContext == null) {
			throw new RuntimeException("MyAccountManager.setContext(null), you must apply a Context object.");
		}
		mContentResolver = mContext.getContentResolver();
		mHaierAccount = null;
		mSharedPreferences = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
		initAccountObject();
	}
	
	public void initAccountObject() {
		//如果没有默认账户，我们使用Demo账户
		if (mHaierAccount == null) {
			mHaierAccount = AccountObject.getHaierAccountFromDatabase(mContext);
		}
		if (mHaierAccount == null) {
			mHaierAccount = AccountObject.getHaierAccountFromDatabase(mContext, AccountObject.DEMO_ACCOUNT_UID);
		}
		initAccountHomes();
	}
	
	public synchronized void initAccountHomes() {
		if (mHaierAccount != null) {
			mHaierAccount.mAccountHomes = HomeObject.getAllHomeObjects(mContext.getContentResolver(), mHaierAccount.mAccountUid);
			//XXX 如果保修卡数据太多，这里太耗时了，我们不做加载,在我的家的时候再做加载
//			for(HomeObject homeObject : mHaierAccount.mAccountHomes) {
//				homeObject.initBaoxiuCards(mContext.getContentResolver());
//			}
			mHaierAccount.mAccountHomeCount = mHaierAccount.mAccountHomes.size();
		}
	}
	
	public void deleteDefaultAccount() {
		if (mHaierAccount != null) {
			DebugUtils.logD(TAG, "start deleteDefaultAccount() for uid " + mHaierAccount.mAccountUid);
			//删除全部保修卡数据
			int deleted = BaoxiuCardObject.deleteAllBaoxiuCardsInDatabaseForAccount(mContext.getContentResolver(), mHaierAccount.mAccountUid);
			DebugUtils.logD(TAG, "deleted " + deleted + " BaoxiuCards");
			//删除全部家数据
			deleted = HomeObject.deleteAllHomesInDatabaseForAccount(mContext.getContentResolver(), mHaierAccount.mAccountUid);
			DebugUtils.logD(TAG, "deleted " + deleted + " Homes");
			//删除账户数据
			deleted = AccountObject.deleteAccount(mContext.getContentResolver(), mHaierAccount.mAccountUid);
			DebugUtils.logD(TAG, "deleted " + deleted + " Account =" + mHaierAccount.toString());
			mHaierAccount = null;
			DebugUtils.logD(TAG, "end deleteDefaultAccount()");
		} else {
			DebugUtils.logD(TAG, "deleteDefaultAccount() nothing to do");
		}
	}
	/**
	 * 删除指定uid的账户
	 * @param uid
	 */
	public static void deleteAccountForUid(ContentResolver cr, long uid) {
		DebugUtils.logD(TAG, "start deleteAccountForUid() for uid " + uid);
		//删除全部保修卡数据
		int deleted = BaoxiuCardObject.deleteAllBaoxiuCardsInDatabaseForAccount(cr, uid);
		DebugUtils.logD(TAG, "deleted " + deleted + " BaoxiuCards");
		//删除全部家数据
		deleted = HomeObject.deleteAllHomesInDatabaseForAccount(cr, uid);
		DebugUtils.logD(TAG, "deleted " + deleted + " Homes");
		//删除账户数据
		deleted = AccountObject.deleteAccount(cr, uid);
		DebugUtils.logD(TAG, "deleted " + deleted + " Accounts");
		DebugUtils.logD(TAG, "end deleteAccountForUid()");
	}
	
	public AccountObject getAccountObject() {
		return mHaierAccount;
	}
	
	public String getDefaultPhoneNumber() {
		return mHaierAccount != null ? mHaierAccount.mAccountTel : null;
	}
	
	public String getCurrentAccountMd() {
		return getCurrentAccountUid();
	}
	
	public String getCurrentAccountUid() {
		return mHaierAccount != null ? String.valueOf(mHaierAccount.mAccountUid) : null; 
	}
	public long getCurrentAccountId() {
		return mHaierAccount != null ? mHaierAccount.mAccountUid : -1; 
	}
	/**默认情况即使用户没有登录，系统会初始化一个演示账户，一旦用户登陆了，就会将演示账户删掉*/
	public boolean hasLoginned() {
		return mHaierAccount != null && mHaierAccount.mAccountId > 0;
	}
	/**是否有保修卡*/
	public boolean hasBaoxiuCards() {
		if (mHaierAccount != null) {
			return mHaierAccount.mAccountBaoxiuCardCount > 0;
		}
		return false;
	}
	/**新建保修卡后都需要调用该方法来更新家,该操作会重新计算mHaierAccount.mAccountBaoxiuCardCount*/
	public void updateHomeObject(long aid) {
		if (mHaierAccount != null) {
			mHaierAccount.mAccountBaoxiuCardCount = 0;
			for(HomeObject home : mHaierAccount.mAccountHomes) {
				if (home.mHomeAid  == aid) {
					home.initBaoxiuCards(mContext.getContentResolver());
				}
				//重新初始化保修卡数据
				mHaierAccount.mAccountBaoxiuCardCount += home.mHomeCardCount;
			}
		}
	}
	
	public boolean hasHomes() {
		return mHaierAccount != null && mHaierAccount.mAccountHomes.size() > 0;
	}
	/**
	 * 返回上一次登陆时候使用的用户名
	 * @return
	 */
	public String getLastUsrTel() {
		return mSharedPreferences.getString("lastUserTel", "");
	}
	
    public void saveLastUsrTel(String userName) {
    	mSharedPreferences.edit().putString("lastUserTel", (userName == null ? "" : userName)).commit();
	}
    
    public boolean saveAccountObject(ContentResolver cr, AccountObject accountObject) {
    	
    	if (mHaierAccount != accountObject) {
    		boolean success = accountObject.saveInDatebase(cr, null);
    		if (success) {
    			updateAccount();
    			return true;
    		}
    	}
    	return false;
    	
    }
    /**
     * 更新账户，每当我们增删家和保修卡数据的时候，调用该方法可以同步当前账户信息.
     */
    public void updateAccount() {
    	mHaierAccount = null;
    	initAccountObject();
    }
    
    
    public static final String CARD_ACCOUNT_UID_SELECTION = HaierDBHelper.ACCOUNT_UID + "=?";
	
	public static final String CONTACT_ID_AND_CARD_ACCOUNT_UID_SELECTION =  HaierDBHelper.CONTACT_ID + "=?" + " and " + CARD_ACCOUNT_UID_SELECTION;
	
	public static final String CARD_ACCOUNT_UID_AND_CONTACT_BID_SELECTION =  CARD_ACCOUNT_UID_SELECTION + " and " + HaierDBHelper.CONTACT_BID + "=?";
	private ContentResolver mContentResolver;
    public synchronized Cursor getCardForAccount(String uid) {
		if (mHaierAccount == null) return null;
		if (uid == null) {
			uid = String.valueOf(mHaierAccount.mAccountUid);
		}
		return mContentResolver.query(BjnoteContent.MyCard.CONTENT_URI, null, CARD_ACCOUNT_UID_SELECTION, new String[]{String.valueOf(uid)}, HaierDBHelper.CONTACT_ID + " desc");
	}
    
    public synchronized Cursor getCard(String uid, String mm) {
		if (mHaierAccount == null) return null;
		if (uid == null) {
			uid = String.valueOf(mHaierAccount.mAccountUid);
		}
		return mContentResolver.query(BjnoteContent.MyCard.CONTENT_URI, null, CARD_ACCOUNT_UID_AND_CONTACT_BID_SELECTION, new String[]{uid, mm}, HaierDBHelper.CONTACT_ID + " desc");
	}
    /**
     * 初始化演示账户
     * @return
     */
    public static AccountObject initDemoAccountObject(Context context) {
    	return AccountObject.getHaierAccountFromDatabase(context, AccountObject.DEMO_ACCOUNT_UID);
    }
    
}
