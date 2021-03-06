package com.bestjoy.app.warrantycard.database;


import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.propertymanagement.HomesCommunityManager;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.shwy.bestjoy.utils.DebugUtils;

public class BjnoteProvider extends ContentProvider{
	private static final String TAG = "BjnoteProvider";
	private SQLiteDatabase mContactDatabase;
	private String[] mTables = new String[]{
			HaierDBHelper.TABLE_NAME_ACCOUNTS,
			HaierDBHelper.TABLE_NAME_HOMES,
			HaierDBHelper.TABLE_NAME_CARDS,
			HaierDBHelper.TABLE_SCAN_NAME,
			HaierDBHelper.TABLE_NAME_DEVICE_XINGHAO,
			HaierDBHelper.TABLE_NAME_MAINTENCE_POINT,
			HaierDBHelper.TABLE_YOUMENG_PUSHMESSAGE_HISTORY,
			HaierDBHelper.TABLE_IM_QUN_HISTORY,
			HaierDBHelper.TABLE_IM_FRIEND_HISTORY,
			HaierDBHelper.TABLE_ACCOUNT_RELATIONSHIP,
			HaierDBHelper.TABLE_HOME_COMMUNITY_SERVICE,
			HaierDBHelper.TABLE_CAR_CARD,
			HaierDBHelper.TABLE_VIEW_CONVERSATION,
			HaierDBHelper.TABLE_NAME_MYLIFE,
			HaierDBHelper.TABLE_BAOXIUCARD_ORDERS,
			HaierDBHelper.TABLE_ACCOUNT_RELATIONSHIP_CONVERSATION,
//			HaierDBHelper.TABLE_HOME_COMMUNITY,
//			ContactsDBHelper.TABLE_NAME_MYLIFE_CONSUME,
	};
	private static final int BASE = 8;
	
	private static final int ACCOUNT = 0x0000;
	private static final int ACCOUNT_ID = 0x0001;
	
	private static final int HOME = 0x0100;
	private static final int HOME_ID = 0x0101;
	
	private static final int DEVICE = 0x0200;
	private static final int DEVICE_ID = 0x0201;
	/**用来预览发票*/
	private static final int BILL_AVATOR = 0x0202;
	
	private static final int SCAN_HISTORY = 0x0300;
	private static final int SCAN_HISTORY_ID = 0x0301;
	
	private static final int XINGHAO = 0x0400;
	private static final int XINGHAO_ID = 0x0401;
	
	private static final int MAINTENCE_POINT = 0x0500;
	private static final int MAINTENCE_POINT_ID = 0x0501;
	
	
	private static final int YMESSAGE = 0x0600;
	private static final int YMESSAGE_ID = 0x0601;
	
	private static final int IM_QUN = 0x0700;
	private static final int IM_QUN_ID = 0x0701;
	private static final int IM_FRIEND = 0x0800;
	private static final int IM_FRIEND_ID = 0x0801;
	
	private static final int ACCOUNT_RELATIONSHIP = 0x0900;
	private static final int ACCOUNT_RELATIONSHIP_ID = 0x0901;
	
	private static final int HOME_COMMUNITY_SERVICE = 0x0a00;
	private static final int HOME_COMMUNITY_SERVICE_ID = 0x0a01;
	
	private static final int CAR_CARD = 0x0b00;
	private static final int CAR_CARD_ID = 0x0b01;
	
	private static final int VIEW_CONVERSATION_HISTORY = 0x0c00;
	private static final int VIEW_CONVERSATION_HISTORY_ID = 0x0c01;
	
	private static final int MY_LIFE = 0x0d00;
	private static final int MY_LIFE_ID = 0x0d01;
	
	private static final int MY_BX_ORDER = 0x0e00;
	private static final int MY_BX_ORDER_ID = 0x0e01;
	
	private static final int ACCOUNT_RELATIONSHIP_CONVERSATION = 0x0f00;
	private static final int ACCOUNT_RELATIONSHIP_CONVERSATION_ID = 0x0f01;
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	 static {
	        // URI matching table
	        UriMatcher matcher = sURIMatcher;
	        matcher.addURI(BjnoteContent.AUTHORITY, "accounts", ACCOUNT);
	        matcher.addURI(BjnoteContent.AUTHORITY, "accounts/#", ACCOUNT_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "homes", HOME);
	        matcher.addURI(BjnoteContent.AUTHORITY, "homes/#", HOME_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "baoxiucard", DEVICE);
	        matcher.addURI(BjnoteContent.AUTHORITY, "baoxiucard/#", DEVICE_ID);
	        matcher.addURI(BjnoteContent.AUTHORITY, "baoxiucard/preview/bill", BILL_AVATOR);
	        
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "scan_history", SCAN_HISTORY);
	        matcher.addURI(BjnoteContent.AUTHORITY, "scan_history/#", SCAN_HISTORY_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "xinghao", XINGHAO);
	        matcher.addURI(BjnoteContent.AUTHORITY, "xinghao/#", XINGHAO_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "maintencepoint", MAINTENCE_POINT);
	        matcher.addURI(BjnoteContent.AUTHORITY, "maintencepoint/#", MAINTENCE_POINT_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "ymessage", YMESSAGE);
	        matcher.addURI(BjnoteContent.AUTHORITY, "ymessage/#", YMESSAGE_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "im/qun", IM_QUN);
	        matcher.addURI(BjnoteContent.AUTHORITY, "im/qun/#", IM_QUN_ID);
	        matcher.addURI(BjnoteContent.AUTHORITY, "im/friend", IM_FRIEND);
	        matcher.addURI(BjnoteContent.AUTHORITY, "im/friend/#", IM_FRIEND_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "relationship", ACCOUNT_RELATIONSHIP);
	        matcher.addURI(BjnoteContent.AUTHORITY, "relationship/#", ACCOUNT_RELATIONSHIP_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "homes_community/service", HOME_COMMUNITY_SERVICE);
	        matcher.addURI(BjnoteContent.AUTHORITY, "homes_community/service/#", HOME_COMMUNITY_SERVICE_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "car_card", CAR_CARD);
	        matcher.addURI(BjnoteContent.AUTHORITY, "car_card/#", CAR_CARD_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "view_conversation_history", VIEW_CONVERSATION_HISTORY);
	        matcher.addURI(BjnoteContent.AUTHORITY, "view_conversation_history/#", VIEW_CONVERSATION_HISTORY_ID);
	        
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "mylife", MY_LIFE);
	        matcher.addURI(BjnoteContent.AUTHORITY, "mylife/#", MY_LIFE_ID);
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "bx_order", MY_BX_ORDER);
	        matcher.addURI(BjnoteContent.AUTHORITY, "bx_order/#", MY_BX_ORDER_ID);
	        
	        
	        matcher.addURI(BjnoteContent.AUTHORITY, "relationship_conversation", ACCOUNT_RELATIONSHIP_CONVERSATION);
	        matcher.addURI(BjnoteContent.AUTHORITY, "relationship_conversation/#", ACCOUNT_RELATIONSHIP_CONVERSATION_ID);
	        
	        //TODO 增加
	 }
	
	synchronized SQLiteDatabase getDatabase(Context context) {
        // Always return the cached database, if we've got one
        if (mContactDatabase != null) {
            return mContactDatabase;
        }


        HaierDBHelper helper = new HaierDBHelper(context);
        mContactDatabase = helper.getWritableDatabase();
        mContactDatabase.setLockingEnabled(true);
        return mContactDatabase;
	}
	
	@Override
	public boolean onCreate() {
		return false;
	}
	
	/**
     * Wrap the UriMatcher call so we can throw a runtime exception if an unknown Uri is passed in
     * @param uri the Uri to match
     * @return the match value
     */
    private static int findMatch(Uri uri, String methodName) {
        int match = sURIMatcher.match(uri);
        if (match < 0) {
            throw new IllegalArgumentException("Unknown uri: " + uri);
        } 
        DebugUtils.logD(TAG, methodName + ": uri=" + uri + ", match is " + match);
        return match;
    }
    
    private void notifyChange(int match) {
    	Context context = getContext();
    	Uri notify = BjnoteContent.CONTENT_URI;
    	switch(match) {
    	case ACCOUNT:
    	case ACCOUNT_ID:
    		notify = BjnoteContent.Accounts.CONTENT_URI;
    		break;
		case HOME:
		case HOME_ID:
			notify = BjnoteContent.Homes.CONTENT_URI;
			break;
		case DEVICE:
		case DEVICE_ID:
			notify = BjnoteContent.BaoxiuCard.CONTENT_URI;
			break;
		case SCAN_HISTORY:
		case SCAN_HISTORY_ID:
			notify = BjnoteContent.ScanHistory.CONTENT_URI;
			break;
		case XINGHAO:
		case XINGHAO_ID:
			notify = BjnoteContent.XingHao.CONTENT_URI;
			break;
		case YMESSAGE:
		case YMESSAGE_ID:
			notify = BjnoteContent.YMESSAGE.CONTENT_URI;
			break;
		case MAINTENCE_POINT:
    	case MAINTENCE_POINT_ID:
    		notify = BjnoteContent.MaintencePoint.CONTENT_URI;
    		break;
    	case IM_FRIEND:
		case IM_FRIEND_ID:
			notify = BjnoteContent.IM.CONTENT_URI_FRIEND;
			break;
		case IM_QUN:
		case IM_QUN_ID:
			notify = BjnoteContent.IM.CONTENT_URI_QUN;
			break;
		case ACCOUNT_RELATIONSHIP:
		case ACCOUNT_RELATIONSHIP_ID:
			notify = BjnoteContent.RELATIONSHIP.CONTENT_URI;
			break;
		case ACCOUNT_RELATIONSHIP_CONVERSATION_ID:
		case ACCOUNT_RELATIONSHIP_CONVERSATION:
			notify = BjnoteContent.RELATIONSHIP.CONVERSATION_CONTENT_URI;
			break;
		case HOME_COMMUNITY_SERVICE:
		case HOME_COMMUNITY_SERVICE_ID:
			notify = HomesCommunityManager.COMMUNITY_SERVICE_CONTENT_URI;
			break;
		case CAR_CARD:
		case CAR_CARD_ID:
			notify = BjnoteContent.MyCarCards.CONTENT_URI;
			break;
		case VIEW_CONVERSATION_HISTORY:
		case VIEW_CONVERSATION_HISTORY_ID:
			notify = BjnoteContent.VIEW_CONVERSATION_HISTORY.CONTENT_URI;
			break;
		case MY_LIFE:
		case MY_LIFE_ID:
			notify = BjnoteContent.MyLife.CONTENT_URI;
			break;
		case MY_BX_ORDER_ID:
		case MY_BX_ORDER:
			notify = BjnoteContent.MyBXOrder.CONTENT_URI;
			break;
			
    	}
    	ContentResolver resolver = context.getContentResolver();
        resolver.notifyChange(notify, null);
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = findMatch(uri, "delete");
        Context context = getContext();

        // See the comment at delete(), above
        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match>>BASE];
        DebugUtils.logProvider(TAG, "delete data from table " + table);
        int count = 0;
        switch(match) {
//	        case ACCOUNT:
//	    	case ACCOUNT_ID:
//			case HOME:
//			case HOME_ID:
//			case DEVICE:
//			case DEVICE_ID:
//			case SCAN_HISTORY:
//			case SCAN_HISTORY_ID:
//			case XINGHAO:
//			case XINGHAO_ID:
//			case YMESSAGE:
//			case YMESSAGE_ID:
//			case MAINTENCE_POINT:
//	    	case MAINTENCE_POINT_ID:
        default:
        	count = db.delete(table, buildSelection(match, uri, selection), selectionArgs);
        }
        if (count >0) notifyChange(match);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		 int match = findMatch(uri, "insert");
         Context context = getContext();

         // See the comment at delete(), above
         SQLiteDatabase db = getDatabase(context);
         String table = mTables[match>>BASE];
         DebugUtils.logProvider(TAG, "insert values into table " + table);
//         switch(match) {
//	         case MY_CARD:
//	         case MY_CARD_ID:
//	         case RECEIVED_CONTACT:
//	         case RECEIVED_CONTACT_ID:
//	         case EXCHANGE_TOPIC:
//	     	 case EXCHANGE_TOPIC_ID:
//	     	 case EXCHANGE_TOPIC_LIST:
//	     	 case EXCHANGE_TOPIC_LIST_ID:
//	     	 case CIRCLE_TOPIC:
//			 case CIRCLE_TOPIC_ID:
//			 case CIRCLE_TOPIC_LIST:
//			 case CIRCLE_TOPIC_LIST_ID:
//			 case CIRCLE_MEMBER_DETAIL:
//			 case CIRCLE_MEMBER_DETAIL_ID:
//			 case ACCOUNT:
//			 case ACCOUNT_ID:
//			 case FEEDBACK:
//			 case FEEDBACK_ID:
//			 case QUANPHOTO:
//			 case QUANPHOTO_ID:
//			 case ZHT:
//			 case ZHT_ID:
//	     		break;
//         }
         //Insert 操作不允许设置_id字段，如果有的话，我们需要移除
         if (values.containsKey(HaierDBHelper.ID)) {
      		values.remove(HaierDBHelper.ID);
      	 }
     	 long id = db.insert(table, null, values);
     	 if (id > 0) {
     		notifyChange(match);
   		    return ContentUris.withAppendedId(uri, id);
     	 }
		 return null;
	}
	private static final String[] COLUMN_NAME = { MediaStore.Images.ImageColumns.DATA};
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		 int match = findMatch(uri, "query");
         Context context = getContext();
         // See the comment at delete(), above
         SQLiteDatabase db = getDatabase(context);
         int i =match>>BASE;
         String table = mTables[i];
         DebugUtils.logProvider(TAG, "query table " + table);
         Cursor result = null;
         switch(match) {
//			case ACCOUNT:
//			case ACCOUNT_ID:
//			case HOME:
//			case HOME_ID:
//			case DEVICE:
//			case DEVICE_ID:
//			case SCAN_HISTORY:
//			case SCAN_HISTORY_ID:
//			case XINGHAO:
//			case XINGHAO_ID:
//			case YMESSAGE:
//			case YMESSAGE_ID:
//			case MAINTENCE_POINT:
//			case MAINTENCE_POINT_ID:
         case BILL_AVATOR:
        	 MatrixCursor matrixCursor = new MatrixCursor(COLUMN_NAME);
        	 File file = MyApplication.getInstance().getProductFaPiaoFile(BaoxiuCardObject.objectUseForbill.getFapiaoPhotoId());
        	 matrixCursor.addRow(new String[]{file.getAbsolutePath()});
        	 result = matrixCursor;
        	 break;
         default:
        	     result = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
         }
         if(result != null)result.setNotificationUri(getContext().getContentResolver(), uri);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int match = findMatch(uri, "update");
        Context context = getContext();
        
        SQLiteDatabase db = getDatabase(context);
        String table = mTables[match>>BASE];
        DebugUtils.logProvider(TAG, "update data for table " + table);
        int count = 0;
        switch(match) {
//	        case ACCOUNT:
//	    	case ACCOUNT_ID:
//			case HOME:
//			case HOME_ID:
//			case DEVICE:
//			case DEVICE_ID:
//			case SCAN_HISTORY:
//			case SCAN_HISTORY_ID:
//			case XINGHAO:
//			case XINGHAO_ID:
//			case YMESSAGE:
//			case YMESSAGE_ID:
//			case MAINTENCE_POINT:
//			case MAINTENCE_POINT_ID:
        default:
        	    count = db.update(table, values, buildSelection(match, uri, selection), selectionArgs);
        }
        if (count >0) notifyChange(match);
		return count;
	}
	
	private String buildSelection(int match, Uri uri, String selection) {
		long id = -1;
		switch(match) {
	    	case ACCOUNT_ID:
			case HOME_ID:
			case DEVICE_ID:
			case SCAN_HISTORY_ID:
			case XINGHAO_ID:
			case YMESSAGE_ID:
			case IM_FRIEND_ID:
			case IM_QUN_ID:
			case ACCOUNT_RELATIONSHIP_ID:
			case HOME_COMMUNITY_SERVICE_ID:
			case CAR_CARD_ID:
			case VIEW_CONVERSATION_HISTORY_ID:
			case MY_LIFE_ID:
			case MY_BX_ORDER_ID:
			case ACCOUNT_RELATIONSHIP_CONVERSATION_ID:
			try {
				id = ContentUris.parseId(uri);
			} catch(java.lang.NumberFormatException e) {
				e.printStackTrace();
			}
			break;
		}
		
		if (id == -1) {
			return selection;
		}
		DebugUtils.logProvider(TAG, "find id from Uri#" + id);
		StringBuilder sb = new StringBuilder();
		sb.append(HaierDBHelper.ID);
		sb.append("=").append(id);
		if (!TextUtils.isEmpty(selection)) {
			sb.append(" and ");
			sb.append(selection);
		}
		DebugUtils.logProvider(TAG, "rebuild selection#" + sb.toString());
		return sb.toString();
	}


	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		int match = findMatch(uri, "openFile");
		switch(match) {
			case BILL_AVATOR:
	        	File file = MyApplication.getInstance().getProductFaPiaoFile(BaoxiuCardObject.objectUseForbill.getFapiaoPhotoId());
	        	if (BaoxiuCardObject.objectUseForbill.mBillTempFile != null && BaoxiuCardObject.objectUseForbill.mBillTempFile.exists()) {
			    	return ParcelFileDescriptor.open(BaoxiuCardObject.objectUseForbill.mBillTempFile, ParcelFileDescriptor.MODE_READ_WRITE);
			    } else if (file.exists()) {
			        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
			    } else {
			    	DebugUtils.logE(TAG, "openFile can't find file " + file.getAbsolutePath());
			    }
				break;
		}
		throw new FileNotFoundException("no Image found for uri " + uri);
	}
	
}
