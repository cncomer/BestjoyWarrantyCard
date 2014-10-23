package com.bestjoy.app.bjwarrantycard.propertymanagement;

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

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;

public class HomesCommunityManager extends BjnoteContent {
	/**查询小区*/
	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "homes_community");
	/**查询小区服务*/
	public static final Uri COMMUNITY_SERVICE_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "homes_community/service");
	/**物业*/
	public static final int TYPE_WUYE = 1;
	/**业委会*/
	public static final int TYPE_YEWEIHUI = 2;
	/**居委会*/
	public static final int TYPE_JUWEIHUI = 3;
	/**街道办*/
	public static final int TYPE_JIEDAOBAN = 4;
	/**派出所*/
	public static final int TYPE_PAICHUSUO = 5;
	/**供水*/
	public static final int TYPE_GONGSHUI = 6;
	/**供电*/
	public static final int TYPE_GONGDIAN = 7;
	/**供气*/
	public static final int TYPE_GONGQI = 8;
	/**供暖*/
	public static final int TYPE_GONGNUAN = 9;
	/**有线*/
	public static final int TYPE_YOUXIAN = 10;
	/**宽带*/
	public static final int TYPE_KUANDAI = 11;
	/**供暖*/
	public static final int TYPE_KUAIDI = 12;
	/**有线*/
	public static final int TYPE_SONGSHUI = 13;
	/**废品*/
	public static final int TYPE_FEIPIN = 14;
	/**搬家*/
	public static final int TYPE_BANJIA = 15;
	/**开锁*/
	public static final int TYPE_KAISUO = 16;
	/**小区通知*/
	public static final int TYPE_TONGZHI = 17;
	/**物业缴费*/
	public static final int TYPE_WUYE_JIAOFEI = 18;
	
	public static final int[] SERVICE_TYPE_IDS = new int[]{
		R.id.community_service_type1, 
		R.id.community_service_type2,
		R.id.community_service_type3, 
		R.id.community_service_type4, 
		R.id.community_service_type5, 
		R.id.community_service_type6, 
		R.id.community_service_type7, 
		R.id.community_service_type8, 
		R.id.community_service_type9, 
		R.id.community_service_type10, 
		R.id.community_service_type11, 
		R.id.community_service_type12, 
		R.id.community_service_type13, 
		R.id.community_service_type14, 
		R.id.community_service_type15, 
		R.id.community_service_type16, 
		R.id.community_service_type17, 
		R.id.community_service_type18, 
	};
	public static final int[] SERVICE_TYPE_CATEGORY = new int[]{
		TYPE_WUYE, 
		TYPE_YEWEIHUI,
		TYPE_JUWEIHUI,
		TYPE_JIEDAOBAN,
		TYPE_PAICHUSUO,
		TYPE_GONGSHUI,
		TYPE_GONGDIAN,
		TYPE_GONGQI,
		TYPE_GONGNUAN,
		TYPE_YOUXIAN,
		TYPE_KUANDAI,
		TYPE_KUAIDI,
		TYPE_SONGSHUI,
		TYPE_FEIPIN,
		TYPE_BANJIA,
		TYPE_KAISUO,
		TYPE_TONGZHI,
		TYPE_WUYE_JIAOFEI,
	};
	public static final int[] FIRST_SERVICE_TYPE_ICONS = new int[]{
		R.drawable.community_type_1, 
		R.drawable.community_type_2,
		R.drawable.community_type_3, 
		R.drawable.community_type_4, 
		R.drawable.community_type_5, 
		R.drawable.community_type_6, 
		R.drawable.community_type_7, 
		R.drawable.community_type_8, 
		R.drawable.community_type_9, 
		R.drawable.community_type_10, 
		R.drawable.community_type_11, 
	};
	
	public static final int[] SERVICE_TYPE_NAMES = new int[]{
		R.string.community_service_type1, 
		R.string.community_service_type2,
		R.string.community_service_type3, 
		R.string.community_service_type4, 
		R.string.community_service_type5, 
		R.string.community_service_type6, 
		R.string.community_service_type7, 
		R.string.community_service_type8, 
		R.string.community_service_type9, 
		R.string.community_service_type10, 
		R.string.community_service_type11, 
		R.string.community_service_type12, 
		R.string.community_service_type13, 
		R.string.community_service_type14, 
		R.string.community_service_type15, 
		R.string.community_service_type16, 
		R.string.community_service_type17, 
		R.string.community_service_type18, 
	};
	/**小区主要服务，0~10共11项服务*/
	public static final int FIRST_SERVICE_POSITION = 10;
	/**小区快捷服务，11~15共5项服务*/
	public static final int SECOND_SERVICE_POSITION = 15;
	
	public static final String UID_SELECTION = HaierDBHelper.ACCOUNT_UID + "=?";
	public static final String UID_AND_HID_SELECTION = UID_SELECTION + " and " + HaierDBHelper.HOME_COMMUNITY_HID + "=?";
	public static final String UID_AND_HID_SELECTION_SERVICEID = UID_AND_HID_SELECTION + " and " + HaierDBHelper.DATA6 + "=?";
	public static final String UID_AND_HID_AND_AID_SELECTION = UID_AND_HID_SELECTION + " and " + HaierDBHelper.HOME_AID + "=?";
	public static final String UID_AND_HID_AND_AID_AND_SID_SELECTION = UID_AND_HID_AND_AID_SELECTION + " and " + HaierDBHelper.DATA3 + "=?";
	public static final String[] COMMUNITY_PROJECTION = new String[]{
		HaierDBHelper.ID,                 //0
		HaierDBHelper.ACCOUNT_UID,        //1
		HaierDBHelper.HOME_AID,           //2
		HaierDBHelper.HOME_COMMUNITY_HID, //3
		HaierDBHelper.DATA1,              //4
		HaierDBHelper.DATA2,              //5
		HaierDBHelper.DATA3,              //6
		HaierDBHelper.DATA4,              //7
		HaierDBHelper.DATA5,              //8
		HaierDBHelper.DATA6,              //9
		HaierDBHelper.DATA7,              //10
		HaierDBHelper.DATA8,              //11
		HaierDBHelper.DATA9,              //12
		
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	public static final int INDEX_AID = 2;
	/**小区id*/
	public static final int INDEX_HID = 3;
	/**DATA6, 服务项目名称*/
	public static final int INDEX_SERVICE_TITLE = 4;
	/**DATA5, 服务项目数据*/
	public static final int INDEX_SERVICE_DATA = 5;
	/**DATA6, 服务项目sid*/
	public static final int INDEX_SERVICE_SID = 6;
	/**DATA7, 类型*/
	public static final int INDEX_SERVICE_TYPE = 7;
	/**DATA8, 日期*/
	public static final int INDEX_DATE = 8;
	/**DATA9, 3表示的是用户编辑过，0为系统推送的值*/
	public static final int INDEX_EDITABLE = 9;
	
	/**返回全部小区*/
	public static Cursor getAllCommunitys(ContentResolver cr, String uid) {
		return cr.query(CONTENT_URI, COMMUNITY_PROJECTION, UID_SELECTION, new String[]{uid}, HaierDBHelper.HOME_COMMUNITY_HID + " desc");
	}
	/**返回小区的全部服务项目*/
	public static Cursor getAllCommunityServices(ContentResolver cr, String uid, String aid, String hid) {
		return cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_AND_AID_SELECTION, new String[]{uid, hid, aid}, HaierDBHelper.DATA7 + " asc");
	}
	/**返回小区的全部固定服务项目*/
	public static Cursor getCommunityMainServices(ContentResolver cr, String uid, String aid, String hid) {
		return cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_AND_AID_SELECTION + " and " + HaierDBHelper.DATA8 + "=0 and " + HaierDBHelper.DATA7 + " <?", new String[]{uid, hid, aid, String.valueOf(TYPE_KUAIDI)}, HaierDBHelper.DATA7 + " asc");
	}
	
	/**返回小区的快捷服务项目*/
	public static Cursor getCommunityKuaijieServices(ContentResolver cr, String uid, String aid, String hid) {
		return cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_AND_AID_SELECTION + " and " + HaierDBHelper.DATA8 + "=0 and " + HaierDBHelper.DATA7 + ">? and " + HaierDBHelper.DATA7 + "<?", new String[]{uid, hid, aid, String.valueOf(TYPE_KUANDAI), String.valueOf(TYPE_TONGZHI)}, HaierDBHelper.DATA7 + " asc");
	}
	
	public static class CommunityServiceObject implements InfoInterface{
		public String mServiceId="",mServiceName="", mServiceContent="";
		public long mAid=-1, mHid=-1, mUid=-1, mId=-1;
		public int mServiceType, mEditable;
		public int mServiceIconResId;
		public int mViewId;
		@Override
		public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
			ContentValues values = new ContentValues();
			values.put(COMMUNITY_PROJECTION[INDEX_UID], mUid);
			values.put(COMMUNITY_PROJECTION[INDEX_AID], mAid);
			values.put(COMMUNITY_PROJECTION[INDEX_HID], mHid);
			values.put(COMMUNITY_PROJECTION[INDEX_SERVICE_TITLE], mServiceName);
			values.put(COMMUNITY_PROJECTION[INDEX_SERVICE_DATA], mServiceContent);
			values.put(COMMUNITY_PROJECTION[INDEX_SERVICE_TYPE], mServiceType);
			values.put(COMMUNITY_PROJECTION[INDEX_SERVICE_SID], mServiceId);
			values.put(COMMUNITY_PROJECTION[INDEX_DATE], new Date().getTime());
			values.put(COMMUNITY_PROJECTION[INDEX_EDITABLE], mEditable);
			if (addtion != null) {
				values.putAll(addtion);
			}
			String[] selectionArgs = new String[]{String.valueOf(mUid), String.valueOf(mHid),  String.valueOf(mAid), mServiceId}; 
			Cursor cursor = cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_AND_AID_AND_SID_SELECTION , selectionArgs, null);
			long id = -1;
			if (cursor != null) {
				if (cursor.moveToNext()) {
					//存在
					id = cursor.getLong(INDEX_ID);
				}
				cursor.close();
			}
			if (id > 0) {
				//已存在，我们做更新操作
				int updated = cr.update(COMMUNITY_SERVICE_CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(id)});
				DebugUtils.logD("CommunityServiceObject", "saveInDatebase() update affected rows " + updated); 
				return updated > 0;
			} else {
				Uri uri = cr.insert(COMMUNITY_SERVICE_CONTENT_URI, values);
				DebugUtils.logD("CommunityServiceObject", "saveInDatebase() insert " + uri); 
				return uri != null;
			}
		}
		
		
	}
	
	public static int getServiceObjectIconResId(int type) {
		if (type <= SERVICE_TYPE_CATEGORY[FIRST_SERVICE_POSITION]) {
			//顶部的主要服务
			return FIRST_SERVICE_TYPE_ICONS[type-1];
		}
		return -1;
	}
	
	public static List<CommunityServiceObject> getAllCommunityServiceObject(ContentResolver cr, String uid, String aid, String hid) {
		
		Cursor cursor = getAllCommunityServices(cr, uid, aid, hid);
		if (cursor != null) {
			List<CommunityServiceObject> communityServiceObjectList = new ArrayList<CommunityServiceObject>(cursor.getCount());
			while(cursor.moveToNext()) {
				communityServiceObjectList.add(getCommunityServiceObjectFromCursor(cursor));
			}
			cursor.close();
			return communityServiceObjectList;
		}
		return new ArrayList<CommunityServiceObject>();
	}
	
	public static CommunityServiceObject getCommunityServiceObjectFromCursor(Cursor cursor) {
		CommunityServiceObject communityServiceObject = new CommunityServiceObject();
		communityServiceObject.mServiceType = cursor.getInt(INDEX_SERVICE_TYPE);
		communityServiceObject.mEditable = cursor.getInt(INDEX_EDITABLE);
		communityServiceObject.mServiceId = cursor.getString(INDEX_SERVICE_SID);
		communityServiceObject.mServiceName = cursor.getString(INDEX_SERVICE_TITLE);
		communityServiceObject.mServiceContent = cursor.getString(INDEX_SERVICE_DATA);
		
		communityServiceObject.mAid = cursor.getLong(INDEX_AID);
		communityServiceObject.mHid = cursor.getLong(INDEX_HID);
		communityServiceObject.mUid = cursor.getLong(INDEX_UID);
		communityServiceObject.mId = cursor.getLong(INDEX_ID);
		communityServiceObject.mServiceIconResId = getServiceObjectIconResId(communityServiceObject.mServiceType);
		return communityServiceObject;
		
	}
	
	public static List<CommunityServiceObject> getAllCommunityServiceObject(JSONArray result) {
		if (result == null) {
			return new ArrayList<CommunityServiceObject>();
		}
		List<CommunityServiceObject> communityServiceObjectList = new ArrayList<CommunityServiceObject>(result.length());
		int len = result.length();
		for(int index=0; index <len; index++) {
			try {
				communityServiceObjectList.add(getCommunityServiceObject(result.getJSONObject(index)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return communityServiceObjectList;
	}
	/**
	 * {"StatusCode":"1","StatusMessage":"返回小区","Data":[{"cell":"021-110","name":"小区物业","level":0,"levelValue":"3","st":0,"stvalue":"1","PhoneID":"1","xid":"1","uid":"575401"}]}
	 * @author bestjoy
	 * @throws JSONException 
	 * @throws NumberFormatException 
	 *
	 */
	public static CommunityServiceObject getCommunityServiceObject(JSONObject result) throws NumberFormatException, JSONException {
		CommunityServiceObject communityServiceObject = new CommunityServiceObject();
		communityServiceObject.mServiceType = Integer.valueOf(result.getString("levelValue"));
		communityServiceObject.mEditable = Integer.valueOf(result.getString("stvalue"));
		communityServiceObject.mServiceId = result.getString("PhoneID");
		communityServiceObject.mServiceName = result.getString("name");
		communityServiceObject.mServiceContent = result.getString("cell");
		
		communityServiceObject.mAid = Long.valueOf(result.getString("aid"));
		communityServiceObject.mHid = Long.valueOf(result.getString("xid"));
		communityServiceObject.mUid = Long.valueOf(result.getString("uid"));
		return communityServiceObject;
		
	}
	
	public static List<CommunityServiceObject> getAllDefaultCommunityServiceObject(HomeObject homeObject) {
		List<CommunityServiceObject> communityServiceObjectList = new ArrayList<CommunityServiceObject>();
		for(int index = 0; index <= FIRST_SERVICE_POSITION; index++) {
			CommunityServiceObject communityServiceObject = new CommunityServiceObject();
			communityServiceObject.mAid = homeObject.mHomeAid;
			communityServiceObject.mUid = homeObject.mHomeUid;
			communityServiceObject.mHid = homeObject.mHid;
			
			communityServiceObject.mServiceType = SERVICE_TYPE_CATEGORY[index];
			communityServiceObject.mServiceName = MyApplication.getInstance().getString(SERVICE_TYPE_NAMES[index]);
			communityServiceObject.mServiceIconResId = FIRST_SERVICE_TYPE_ICONS[index];
			communityServiceObject.mServiceContent = "";
			communityServiceObject.mViewId = SERVICE_TYPE_IDS[index];
			communityServiceObjectList.add(communityServiceObject);
		}
		
		for(int index = FIRST_SERVICE_POSITION+1; index <= SECOND_SERVICE_POSITION; index++) {
			CommunityServiceObject communityServiceObject = new CommunityServiceObject();
			communityServiceObject.mAid = homeObject.mHomeAid;
			communityServiceObject.mUid = homeObject.mHomeUid;
			communityServiceObject.mHid = homeObject.mHid;
			
			communityServiceObject.mServiceType = SERVICE_TYPE_CATEGORY[index];
			communityServiceObject.mServiceName = MyApplication.getInstance().getString(SERVICE_TYPE_NAMES[index]);
			communityServiceObject.mServiceIconResId = -1;
			communityServiceObject.mServiceContent = "";
			communityServiceObject.mViewId = SERVICE_TYPE_IDS[index];
			communityServiceObjectList.add(communityServiceObject);
		}
		return communityServiceObjectList;
	}
	
}
