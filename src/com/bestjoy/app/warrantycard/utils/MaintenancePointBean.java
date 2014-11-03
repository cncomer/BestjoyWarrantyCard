package com.bestjoy.app.warrantycard.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;

public class MaintenancePointBean implements InfoInterface{
	private static final String TAG = "MaintenancePointBean";
	/**维修点名称*/
	private String maintenancePointName;
	/**维修点详细地址*/
	private String maintenancePointDetail;
	/**维修点电话*/
	private String maintenancePointTel;
	/**维修点距离*/
	private String maintenancePointDistance;
	
	private String maintenancePointUrl;
	
	private String maintenancePointLat;
	private String maintenancePointLng;

	public static final String[] MAINTENCE_PROJECTION = new String[]{
		HaierDBHelper.ID,
		HaierDBHelper.MAINTENCE_POINT_AID,
		HaierDBHelper.MAINTENCE_POINT_BID,
		HaierDBHelper.MAINTENCE_POINT_NAME,
		HaierDBHelper.MAINTENCE_POINT_ADDRESS,
		HaierDBHelper.MAINTENCE_POINT_TEL,
		HaierDBHelper.MAINTENCE_POINT_DISTANCE,
		HaierDBHelper.MAINTENCE_POINT_DETAIL_URL,
	};
	public static final String MAINTENCE_PROJECTION_POINT_TYPE_SELECTION = HaierDBHelper.MAINTENCE_POINT_TYPE + "=?";
	public static final String MAINTENCE_PROJECTION_AID_SELECTION = HaierDBHelper.MAINTENCE_POINT_AID + "=?";
	public static final String MAINTENCE_PROJECTION_BID_SELECTION = HaierDBHelper.MAINTENCE_POINT_BID + "=?";
	public static final String MAINTENCE_PROJECTION_AID_BID_SELECTION = MAINTENCE_PROJECTION_AID_SELECTION + " and " + MAINTENCE_PROJECTION_BID_SELECTION;
	public static final String MAINTENCE_PROJECTION_AID_BID_TYPE_SELECTION = MAINTENCE_PROJECTION_AID_BID_SELECTION + " and " + MAINTENCE_PROJECTION_POINT_TYPE_SELECTION;
	public static final String MAINTENANCE_POINT_NAME = "name";
	public static final String MAINTENANCE_POINT_ADDRESS = "address";
	public static final String MAINTENANCE_POINT_TELEPHONE = "telephone";
	public static final String MAINTENANCE_POINT_DISTANCE = "distance";
	public static final String MAINTENANCE_POINT_DETAIL_INFO = "detail_info";
	public static final String MAINTENANCE_POINT_DETAIL_URL = "detail_url";
	

	public String getMaintenancePointName() {
		return maintenancePointName;
	}
	public void setMaintenancePointName(String maintenancePointName) {
		this.maintenancePointName = maintenancePointName;
	}

	public String getMaintenancePointDetail() {
		return maintenancePointDetail;
	}
	public void setMaintenancePointDetail(String maintenancePointDetail) {
		this.maintenancePointDetail = maintenancePointDetail;
	}

	public String getMaintenancePointTel() {
		return maintenancePointTel;
	}
	public void setMaintenancePointTel(String maintenancePointTel) {
		this.maintenancePointTel = maintenancePointTel;
	}

	public String getMaintenancePointDistance() {
		return maintenancePointDistance;
	}
	public void setMaintenancePointDistance(String maintenancePointDistance) {
		this.maintenancePointDistance = maintenancePointDistance;
	}

	public String getMaintenancePointUrl() {
		return maintenancePointUrl;
	}
	public void setMaintenancePointUrl(String maintenancePointUrl) {
		this.maintenancePointUrl = maintenancePointUrl;
	}
	
	
	public boolean saveDatabase(ContentResolver cr, ContentValues addtion, String aid, String bid) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		values.put(HaierDBHelper.MAINTENCE_POINT_AID, aid);
		values.put(HaierDBHelper.MAINTENCE_POINT_BID, bid);
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_ADDRESS, maintenancePointDetail);
		values.put(HaierDBHelper.MAINTENCE_POINT_TEL, maintenancePointTel);
		values.put(HaierDBHelper.MAINTENCE_POINT_DISTANCE, maintenancePointDistance);
		values.put(HaierDBHelper.MAINTENCE_POINT_DETAIL_URL, maintenancePointUrl);
		
		Uri uri = cr.insert(BjnoteContent.MaintencePoint.CONTENT_URI, values);
		if (uri != null) {
			DebugUtils.logD(TAG, "saveInDatebase insert");
			return true;
		} else {
			DebugUtils.logD(TAG, "saveInDatebase failly insert");
		}
		return false;
	}
	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_NAME, maintenancePointName);
		values.put(HaierDBHelper.MAINTENCE_POINT_ADDRESS, maintenancePointDetail);
		values.put(HaierDBHelper.MAINTENCE_POINT_TEL, maintenancePointTel);
		values.put(HaierDBHelper.MAINTENCE_POINT_DISTANCE, maintenancePointDistance);
		values.put(HaierDBHelper.MAINTENCE_POINT_DETAIL_URL, maintenancePointUrl);
		
		Uri uri = cr.insert(BjnoteContent.MaintencePoint.CONTENT_URI, values);
		if (uri != null) {
			DebugUtils.logD(TAG, "saveInDatebase insert");
			return true;
		} else {
			DebugUtils.logD(TAG, "saveInDatebase failly insert");
		}
		return false;
	}
}
