package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;

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
	public static final String UID_SELECTION = HaierDBHelper.ACCOUNT_UID + "=?";
	public static final String UID_AND_HID_SELECTION = UID_SELECTION + " and " + HaierDBHelper.HOME_COMMUNITY_HID + "=?";
	public static final String UID_AND_HID_SELECTION_SERVICEID = UID_AND_HID_SELECTION + " and " + HaierDBHelper.DATA6 + "=?";
	public static final String UID_AND_HID_AND_AID_SELECTION = UID_AND_HID_SELECTION + " and " + HaierDBHelper.HOME_AID + "=?";
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
	
	/**
	 * {"StatusCode":"1","StatusMessage":"返回小区","Data":[{"cell":"021-110","name":"小区物业","level":0,"levelValue":"3","st":0,"stvalue":"1","PhoneID":"1","xid":"1","uid":"575401"}]}
	 * @author bestjoy
	 *
	 */
	public static class CommunityServiceObject {
		public String mServiceId,mServiceName, mServiceContent;
		public long mAid, mHid, mUid, mId;
		public int mServiceType, mEditable;
		
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
		return communityServiceObject;
		
	}
}
