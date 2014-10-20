package com.bestjoy.app.bjwarrantycard.propertymanagement;

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
	public static final String[] COMMUNITY_PROJECTION = new String[]{
		HaierDBHelper.ID,                 //0
		HaierDBHelper.ACCOUNT_UID,        //1
		HaierDBHelper.HOME_COMMUNITY_HID, //2
		HaierDBHelper.DATA1,              //3
		HaierDBHelper.DATA2,              //4
		HaierDBHelper.DATA3,              //5
		HaierDBHelper.DATA4,              //6
		HaierDBHelper.DATA5,              //7
		HaierDBHelper.DATA6,              //8
		HaierDBHelper.DATA7,              //9
		HaierDBHelper.DATA8,              //10
		HaierDBHelper.DATA9,              //11
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	/**小区id*/
	public static final int INDEX_HID = 2;
	/**DATA1, 服务项目名称*/
	public static final int INDEX_SERVICE_TITLE = 3;
	/**DATA2, 服务项目数据*/
	public static final int INDEX_SERVICE_DATA = 4;
	/**DATA6, 服务项目sid*/
	public static final int INDEX_SERVICE_SID = 8;
	/**DATA7, 类型*/
	public static final int INDEX_SERVICE_TYPE = 9;
	/**DATA8, parent,如果为0表示的是大类，如果非0表示的是INDEX_SERVICE_SID的项目的子项目*/
	public static final int INDEX_PARENT = 10;
	/**DATA9, MM*/
	public static final int INDEX_DATE = 11;
	
	/**返回全部小区*/
	public static Cursor getAllCommunitys(ContentResolver cr, String uid) {
		return cr.query(CONTENT_URI, COMMUNITY_PROJECTION, UID_SELECTION, new String[]{uid}, HaierDBHelper.HOME_COMMUNITY_HID + " desc");
	}
	/**返回小区的全部服务项目*/
	public static Cursor getAllCommunityServices(ContentResolver cr, String uid, String hid) {
		return cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_SELECTION + " and " + HaierDBHelper.DATA8 + "=0 and " + HaierDBHelper.DATA7 + " <?", new String[]{uid, hid, String.valueOf(TYPE_KUAIDI)}, HaierDBHelper.DATA8 + " asc");
	}
	
	/**返回小区的快捷服务项目*/
	public static Cursor getAllKuaijieServices(ContentResolver cr, String uid, String hid) {
		return cr.query(COMMUNITY_SERVICE_CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_SELECTION + " and " + HaierDBHelper.DATA8 + "=0 and " + HaierDBHelper.DATA7 + ">? and " + HaierDBHelper.DATA7 + "<?", new String[]{uid, hid, String.valueOf(TYPE_KUANDAI), String.valueOf(TYPE_TONGZHI)}, null);
	}
	
	/**
	 * 返回小区的快捷服务项目
	 * @param cr
	 * @param uid
	 * @param hid
	 * @param serviceId 服务项目
	 * @return
	 */
	public static Cursor getAllKuaijieServices(ContentResolver cr, String uid, String hid, String serviceId) {
		return cr.query(BjnoteContent.RELATIONSHIP.CONTENT_URI, COMMUNITY_PROJECTION, UID_AND_HID_SELECTION + " and " + HaierDBHelper.DATA8 + "=? and " + HaierDBHelper.DATA7 + ">? and " + HaierDBHelper.DATA7 + "<?", new String[]{uid, hid, serviceId, String.valueOf(TYPE_KUANDAI), String.valueOf(TYPE_TONGZHI)}, null);
	}
}
