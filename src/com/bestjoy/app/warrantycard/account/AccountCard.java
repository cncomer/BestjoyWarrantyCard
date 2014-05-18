package com.bestjoy.app.warrantycard.account;

import com.bestjoy.app.warrantycard.database.HaierDBHelper;

import android.database.Cursor;

/**名片对象*/
public class AccountCard {
	private static final String TAG = "AccountCard";
	public String mName = "";
	public String mTel ="";
	public String mPMD = "";
	public String mMM = "";
	public String mOrg = "";
	public String mTitle = "";
	public String mNote ="";
	public String mType = "";
	public String mEmail = "";
	/**账户密码*/
	public String mPMDPWD = "";
	public String mPwd = "";
	/**名片id*/
	public String mId = "";
	
	public static AccountCard mTempAccountCard;

	public AccountCard(){}
	
	public boolean compareChange(AccountCard newAccountCard) {
		if (this == newAccountCard) {
			return false;
		}
		if (newAccountCard == null) {
			return true;
		}
		if (!mName.equals(newAccountCard.mName)) {
			return true;
		}
		
		if (!mTel.equals(newAccountCard.mTel)) {
			return true;
		}
		if (!mOrg.equals(newAccountCard.mOrg)) {
			return true;
		}
		if (!mTitle.equals(newAccountCard.mTitle)) {
			return true;
		}
		if (!mNote.equals(newAccountCard.mNote)) {
			return true;
		}
		if (!mType.equals(newAccountCard.mType)) {
			return true;
		}
		if (!mPwd.equals(newAccountCard.mPwd)) {
			return true;
		}
		if (!mEmail.equals(newAccountCard.mEmail)) {
			return true;
		}
		return false;
	}
	
	public static AccountCard getFromDatabase(String accountMd, String mm) {
		Cursor cursor = MyAccountManager.getInstance().getCard(accountMd, mm);
		if (cursor == null) {
			return null;
		}
		AccountCard accountCard  = null;
		if (cursor.moveToNext()) {
			accountCard = getFromCursor(cursor);
		}
		if (cursor != null) {
			cursor.close();
		}
		return accountCard;
	}
	
	/**需要调用cursor.close()*/
	public static AccountCard getFromCursor(Cursor cursor) {
		AccountCard accountCard = new AccountCard();
		accountCard.mName = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_NAME));
		if (accountCard.mName == null) {
			accountCard.mName = "";
		}
		accountCard.mTel = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_TEL));
		if (accountCard.mTel == null) {
			accountCard.mTel = "";
		}
		accountCard.mMM = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_BID));
		if (accountCard.mMM == null) {
			accountCard.mMM = "";
		}
		accountCard.mOrg = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_ORG));
		if (accountCard.mOrg == null) {
			accountCard.mOrg = "";
		}
		accountCard.mNote = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_NOTE));
		if (accountCard.mNote == null) {
			accountCard.mNote = "";
		}
		accountCard.mPMD = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CARD_ACCOUNT_MD));
		if (accountCard.mPMD == null) {
			accountCard.mPMD = "";
		}
		accountCard.mPMDPWD = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CARD_ACCOUNT_PWD));
		if (accountCard.mPMDPWD == null) {
			accountCard.mPMDPWD = "";
		}
		accountCard.mPwd = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_PASSWORD));
		if (accountCard.mPwd == null) {
			accountCard.mPwd = "";
		}
		accountCard.mId = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_ID));
		if (accountCard.mId == null) {
			accountCard.mId = "";
		}
		accountCard.mType = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_TYPE));
		if (accountCard.mType == null) {
			accountCard.mType = "";
		}
		accountCard.mTitle = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_TITLE));
		if (accountCard.mTitle == null) {
			accountCard.mTitle = "";
		}
		accountCard.mEmail = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_EMAIL));
		if (accountCard.mEmail == null) {
			accountCard.mEmail = "";
		}
		return accountCard;
	}
	
	public static String getContactTagAndMm(Cursor cardCursor) {
		return cardCursor.getString(cardCursor.getColumnIndex(HaierDBHelper.CONTACT_TYPE)) + "\n" +
	    				cardCursor.getString(cardCursor.getColumnIndex(HaierDBHelper.CONTACT_BID));
	}
	
}
