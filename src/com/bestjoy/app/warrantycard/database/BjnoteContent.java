package com.bestjoy.app.warrantycard.database;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.bestjoy.app.bjwarrantycard.MyApplication;

public class BjnoteContent {

	public static final String AUTHORITY = MyApplication.PKG_NAME + ".provider.BjnoteProvider";
    // The notifier authority is used to send notifications regarding changes to messages (insert,
    // delete, or update) and is intended as an optimization for use by clients of message list
    // cursors (initially, the email AppWidget).
    public static final String NOTIFIER_AUTHORITY =  MyApplication.PKG_NAME + ".notify.BjnoteProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    public static final String DEVICE_AUTHORITY = MyApplication.PKG_NAME + ".provider.DeviceProvider";
    public static final String DEVICE_NOTIFIER_AUTHORITY = MyApplication.PKG_NAME + ".notify.DeviceProvider";
    public static final Uri DEVICE_CONTENT_URI = Uri.parse("content://" + DEVICE_AUTHORITY);
    
    // All classes share this
    public static final String RECORD_ID = "_id";

    public static final String[] COUNT_COLUMNS = new String[]{"count(*)"};

    /**
     * This projection can be used with any of the EmailContent classes, when all you need
     * is a list of id's.  Use ID_PROJECTION_COLUMN to access the row data.
     */
    public static final String[] ID_PROJECTION = new String[] {
        RECORD_ID
    };
    public static final int ID_PROJECTION_COLUMN = 0;

    public static final String ID_SELECTION = RECORD_ID + " =?";
    
    
    public static class Accounts extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "accounts");
    }
    
    public static class Homes extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "homes");
    }
    /**我的保修卡设备*/
    public static class BaoxiuCard extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "baoxiucard");
    	public static final Uri BILL_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "baoxiucard/preview/bill");
    }
    
    public static class DaLei extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "dalei");
    }
    
    public static class XiaoLei extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "xiaolei");
    }
    
    public static class PinPai extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "pinpai");
    }
    
    public static class XingHao extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "xinghao");
    }
    
    public static class Province extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "province");
    }
    
    public static class City extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "city");
    }
    
    public static class District extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "district");
    }
    
    public static class ScanHistory extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "scan_history");
    }
    
    public static class HaierRegion extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "haierregion");
    }
    public static class YMESSAGE extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "ymessage");
    	
    	public static String[] PROJECTION = new String[]{
    		HaierDBHelper.ID,
    		HaierDBHelper.YOUMENG_MESSAGE_ID,
    		HaierDBHelper.YOUMENG_TITLE,
    		HaierDBHelper.YOUMENG_TEXT,
    		HaierDBHelper.YOUMENG_MESSAGE_ACTIVITY,
    		HaierDBHelper.YOUMENG_MESSAGE_URL,
    		HaierDBHelper.YOUMENG_MESSAGE_CUSTOM,
    		HaierDBHelper.YOUMENG_MESSAGE_RAW, 
    		HaierDBHelper.DATE,
    	};
    	
    	public static final int INDEX_ID = 0;
    	public static final int INDEX_MESSAGE_ID = 1;
    	public static final int INDEX_TITLE = 2;
    	public static final int INDEX_TEXT = 3;
    	public static final int INDEX_MESSAGE_ACTIVITY = 4;
    	public static final int INDEX_MESSAGE_URL = 5;
    	public static final int INDEX_MESSAGE_CUSTOM = 6;
    	public static final int INDEX_MESSAGE_RAW = 7;
    	public static final int INDEX_DATE = 8;
    	
    	public static final String WHERE_YMESSAGE_ID = HaierDBHelper.YOUMENG_MESSAGE_ID + "=?";
    	public static final String WHERE_YMESSAGE_CATEGORY = HaierDBHelper.YOUMENG_MESSAGE_CATEGORY + "=?";
    }
    /**调用该类的CONTENT_URI来关闭设备数据库*/
    public static class CloseDeviceDatabase extends BjnoteContent{
    	private static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "closedevice");
    	/**调用该方法来关闭设备数据库*/
    	public static void closeDeviceDatabase(ContentResolver cr) {
    		cr.query(CONTENT_URI, null, null, null, null);
    	}
    }
    
    /**
     * 我的名片
     * @author chenkai
     *
     */
    public static class MyCard extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mycard");
    }
    
    /**生活圈-会员卡*/
    public static class MyLife extends BjnoteContent {
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mylife");
    	public static final Uri CONSUME_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mylife/consume");
    	public static final boolean DEBUG = false;
    	
    	public static String buildAllMyLifeForTel(String tel) {
    		if (DEBUG) {
    			tel = "13816284988";
    		} 
    		return "http://www.mingdown.com/cell/get2B.ashx?Cell=" + tel;
    	}
    }
    
    public static class MaintencePoint extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "maintencepoint");
    }
    
    public static class IM extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "im/qun");
    	public static final Uri CONTENT_URI_QUN = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "im/qun");
    	public static final Uri CONTENT_URI_FRIEND = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "im/friend");
    }
    
    public static class RELATIONSHIP extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "relationship");
    	/**最新的关系会话表*/
    	public static final Uri CONVERSATION_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "relationship_conversation");
    	public static final String UID_SELECTION = HaierDBHelper.RELATIONSHIP_UID + "=?";
    	public static final String SORT_BY_SID = HaierDBHelper.RELATIONSHIP_SERVICE_ID + " desc";
    	public static final String[] RELATIONSHIP_PROJECTION = new String[]{
    		HaierDBHelper.ID,              //0
    		HaierDBHelper.RELATIONSHIP_SERVICE_ID,   //1
    		HaierDBHelper.RELATIONSHIP_TYPE,  //2
    		HaierDBHelper.RELATIONSHIP_TARGET,       //3
    		HaierDBHelper.RELATIONSHIP_UID,         //4
    		HaierDBHelper.RELATIONSHIP_NAME,           //5
    		HaierDBHelper.DATA1,         //6
    		HaierDBHelper.DATA2,  //7
    		HaierDBHelper.DATA3,  //8
    		HaierDBHelper.DATA4,  //9
    		HaierDBHelper.DATA5,  //10
    		HaierDBHelper.DATA6,  //11
    		HaierDBHelper.DATA7,  //12
    		HaierDBHelper.DATA8,  //13
    		HaierDBHelper.DATA9,  //14
    		HaierDBHelper.DATE,   //15
    		HaierDBHelper.RELATIONSHIP_TARGET_IS_SERVER,
    		HaierDBHelper.RELATIONSHIP_CONVERSATION_NEW_MESSAGE,
    		HaierDBHelper.RELATIONSHIP_CONVERSATION_NEW_MESSAGE_COUNT,
    		HaierDBHelper.RELATIONSHIP_CONVERSATION_NEW_MESSAGE_TIME,
    	};
    	public static final int INDEX_RELASTIONSHIP_ID = 0;
    	public static final int INDEX_RELASTIONSHIP_SERVICE_ID = 1;
    	public static final int INDEX_RELASTIONSHIP_TARGET_TYPE = 2;
    	public static final int INDEX_RELASTIONSHIP_TARGET = 3;
    	public static final int INDEX_RELASTIONSHIP_UID = 4;
    	public static final int INDEX_RELASTIONSHIP_UNAME = 5;
    	/**DATA1*/
    	public static final int INDEX_RELASTIONSHIP_TITLE = 6;
    	/**DATA2*/
    	public static final int INDEX_RELASTIONSHIP_ORG = 7;
    	/**DATA3*/
    	public static final int INDEX_RELASTIONSHIP_WORKPLACE = 8;
    	/**DATA4*/
    	public static final int INDEX_RELASTIONSHIP_BRIEF = 9;
    	/**DATA5*/
    	public static final int INDEX_RELASTIONSHIP_CELL = 10;
    	/**DATA6, 用作头像*/
    	public static final int INDEX_RELASTIONSHIP_AVATOR = 11;
    	/**DATA7, 类型*/
    	public static final int INDEX_RELASTIONSHIP_LEIXING = 12;
    	/**DATA8, 型号*/
    	public static final int INDEX_RELASTIONSHIP_XINGHAO = 13;
    	/**DATA9, MM*/
    	public static final int INDEX_RELASTIONSHIP_MM = 14;
    	public static final int INDEX_RELASTIONSHIP_LOCAL_DATE = 15;
    	/**自己是否是服务人员*/
    	public static final int INDEX_RELASTIONSHIP_TARGET_IS_SERVER = 16;
    	public static final int INDEX_RELASTIONSHIP_NEW_MESSAGE = 17;
    	public static final int INDEX_RELASTIONSHIP_NEW_MESSAGE_COUNT = 18;
    	public static final int INDEX_RELASTIONSHIP_NEW_MESSAGE_TIME = 19;
    	/**返回我的全部关系*/
    	public static Cursor getAllRelationships(ContentResolver cr, String uid) {
    		return cr.query(BjnoteContent.RELATIONSHIP.CONTENT_URI, RELATIONSHIP_PROJECTION, UID_SELECTION, new String[]{uid}, SORT_BY_SID);
    	}
    	public static Cursor getAllRelationshipConversation(ContentResolver cr, String uid) {
    		return cr.query(BjnoteContent.RELATIONSHIP.CONVERSATION_CONTENT_URI, RELATIONSHIP_PROJECTION, UID_SELECTION, new String[]{uid}, SORT_BY_SID);
    	}
    	
    	
    }
    
    public static class VIEW_CONVERSATION_HISTORY extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "view_conversation_history");
    }
    
    public static class MyCarCards extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "car_card");
    }
    
    public static class MyBXOrder extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "bx_order");
    }
    
    public static long existed(ContentResolver cr, Uri uri, String where, String[] selectionArgs) {
    	long id = -1;
		Cursor c = cr.query(uri, ID_PROJECTION, where, selectionArgs, null);
		if (c != null) {
			if (c.moveToNext()) {
				id = c.getLong(0);
			}
			c.close();
		}
		return id;
	}
	
	public static int update(ContentResolver cr, Uri uri, ContentValues values, String where, String[] selectionArgs) {
		return cr.update(uri, values, where, selectionArgs);
	}
	
	public static Uri insert(ContentResolver cr, Uri uri, ContentValues values) {
		return cr.insert(uri, values);
	}
	
	public static int delete(ContentResolver cr, Uri uri,  String where, String[] selectionArgs) {
		return cr.delete(uri, where, selectionArgs);
	}
}
