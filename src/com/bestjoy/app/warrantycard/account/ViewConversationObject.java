package com.bestjoy.app.warrantycard.account;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterfaceImpl;

public class ViewConversationObject extends InfoInterfaceImpl {
	private static final String TAG = "ViewConversationObject";
	/**往下找比我本地数据更早的紧邻数据*/
	public static final int DIRECTION_DOWN = 0;
	/**往上找比我本地数据更晚的紧邻数据*/
	public static final int DIRECTION_UP = 1;
	
	public static final String[] PROJECTION = new String[]{
		HaierDBHelper.ID,
		HaierDBHelper.ACCOUNT_UID,
		HaierDBHelper.VIEW_CONVERSATION_SENDER_UID,
		HaierDBHelper.VIEW_CONVERSATION_SENDER_UNAME,
		HaierDBHelper.VIEW_CONVERSATION_MID,
		HaierDBHelper.VIEW_CONVERSATION_KY,
		HaierDBHelper.VIEW_CONVERSATION_MESSAGE,
		HaierDBHelper.VIEW_CONVERSATION_SERVICE_TIME,
		HaierDBHelper.VIEW_CONVERSATION_LOCAL_TIME,
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	public static final int INDEX_SID = 2;
	public static final int INDEX_SNAME = 3;
	public static final int INDEX_MID = 4;
	public static final int INDEX_KY = 5;
	public static final int INDEX_MESSAGE = 6;
	public static final int INDEX_MESSAGE_SERVER_TIME = 7;
	public static final int INDEX_MESSAGE_LOCAL_TIME = 8;
	public static final String UID_KY_SELECTION = HaierDBHelper.ACCOUNT_UID + "=?" + " and " + HaierDBHelper.VIEW_CONVERSATION_KY + "=?";
	public static final String SID_KY_MID_SELECTION = HaierDBHelper.VIEW_CONVERSATION_SENDER_UID + "=?" + " and " + HaierDBHelper.VIEW_CONVERSATION_KY + "=?"+ " and " + HaierDBHelper.VIEW_CONVERSATION_MID + "=?";
	public static final String SORT_BY_MID = HaierDBHelper.VIEW_CONVERSATION_MID + " asc";
	public long mID = -1;
	public String mUID = "";
	public String mSenderUID="";
	public String mSenderName="";
	public long mMID = -1;
	public String mKY = "";
	public String mMessage = "";
	public long mServerTime = -1;
	public long mLocalTime = -1;
	
	
	public static List<ViewConversationObject> parse(JSONArray array) {
		List<ViewConversationObject> list = new ArrayList<ViewConversationObject>(array.length());
		int len = array.length();
		for(int index=0; index<len; index++) {
			try {
				list.add(parse(array.getJSONObject(index)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public static ViewConversationObject parse(JSONObject object) throws JSONException {
		ViewConversationObject viewConversationObject = new ViewConversationObject();
		viewConversationObject.mUID = MyAccountManager.getInstance().getCurrentAccountMd();
		viewConversationObject.mSenderUID = object.getString("UID");
		viewConversationObject.mSenderName = object.getString("userName");
		viewConversationObject.mMID = object.getLong("MID");
		viewConversationObject.mKY = object.getString("KY");
		viewConversationObject.mMessage = object.getString("Message");
		viewConversationObject.mServerTime = object.optLong("ltime", new Date().getTime());
		return viewConversationObject;
	}
	
	public static ViewConversationObject getConversationItemObjectFromCursor(Cursor cursor) {
		ViewConversationObject viewConversationObject = new ViewConversationObject();
		
		viewConversationObject.mID = cursor.getLong(INDEX_ID);
		viewConversationObject.mUID = cursor.getString(INDEX_UID);
		viewConversationObject.mSenderUID = cursor.getString(INDEX_SID);
		viewConversationObject.mSenderName = cursor.getString(INDEX_SNAME);
		viewConversationObject.mMID = cursor.getLong(INDEX_MID);
		viewConversationObject.mKY = cursor.getString(INDEX_KY);
		viewConversationObject.mMessage = cursor.getString(INDEX_MESSAGE);
		
		viewConversationObject.mServerTime = cursor.getLong(INDEX_MESSAGE_SERVER_TIME);
		viewConversationObject.mLocalTime = cursor.getLong(INDEX_MESSAGE_LOCAL_TIME);
		
		return viewConversationObject;
	}
	
	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		values.put(PROJECTION[INDEX_UID], mUID);
		values.put(PROJECTION[INDEX_SID], mSenderUID);
		values.put(PROJECTION[INDEX_SNAME], mSenderName);
		values.put(PROJECTION[INDEX_MID], mMID);
		values.put(PROJECTION[INDEX_KY], mKY);
		values.put(PROJECTION[INDEX_MESSAGE], mMessage);
		values.put(PROJECTION[INDEX_MESSAGE_SERVER_TIME], mServerTime);
		values.put(PROJECTION[INDEX_MESSAGE_LOCAL_TIME], new Date().getTime());
		long id = BjnoteContent.existed(cr, BjnoteContent.VIEW_CONVERSATION_HISTORY.CONTENT_URI, SID_KY_MID_SELECTION, new String[]{mSenderUID, mKY, String.valueOf(mMID)});
		if (id > 0) {
//			int updated = cr.update(BjnoteContent.VIEW_CONVERSATION_HISTORY.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(id)});
//			DebugUtils.logD(TAG, "saveInDatebase update " + mMessage + ", effect rows#" + updated);
//			return updated > 0;
			//不错更新
			return true;
		} else {
			Uri uri = cr.insert(BjnoteContent.VIEW_CONVERSATION_HISTORY.CONTENT_URI, values);
			DebugUtils.logD(TAG, "saveInDatebase insert " + mMessage + ", uri=" + uri);
			return uri != null;
		}
	}

}
