package com.bestjoy.app.warrantycard.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bestjoy.app.warrantycard.database.BjnoteContent;

import android.content.ContentResolver;
import android.database.Cursor;

public class PatternMaintenanceUtils {

	public static List<MaintenancePointBean> getMaintenancePoint(JSONArray addresses, ContentResolver cr, long aid, long bid) throws JSONException {
		List<MaintenancePointBean> result = new ArrayList<MaintenancePointBean>();
		if(addresses == null) return result;
		for(int i = 0; i < addresses.length(); i++) {
			MaintenancePointBean maintenancePointBean = new MaintenancePointBean();
			JSONObject obj = addresses.getJSONObject(i);
			JSONObject detailObj = obj.getJSONObject(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_INFO);
			maintenancePointBean.setMaintenancePointName(obj.has(MaintenancePointBean.MAINTENANCE_POINT_NAME)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_NAME):"");
			maintenancePointBean.setMaintenancePointDetail(obj.has(MaintenancePointBean.MAINTENANCE_POINT_ADDRESS)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_ADDRESS):"");
			maintenancePointBean.setMaintenancePointTel(obj.has(MaintenancePointBean.MAINTENANCE_POINT_TELEPHONE)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_TELEPHONE):"");
			maintenancePointBean.setMaintenancePointDistance(detailObj.has(MaintenancePointBean.MAINTENANCE_POINT_DISTANCE)?detailObj.getString(MaintenancePointBean.MAINTENANCE_POINT_DISTANCE):"");
			maintenancePointBean.setMaintenancePointUrl(detailObj.has(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_URL)?detailObj.getString(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_URL):"");
			maintenancePointBean.saveDatabase(cr, null, String.valueOf(aid), String.valueOf(bid));

			result.add(maintenancePointBean);
		}
		return result;
	}

	public static List<MaintenancePointBean> getMaintenancePointClean(JSONArray addresses, ContentResolver cr, long aid, long bid) throws JSONException {
		deleteCachedData(cr, String.valueOf(aid), String.valueOf(bid));
		
		return getMaintenancePoint(addresses, cr, aid, bid);
	}
	
	public static MaintenancePointBean getMaintenancePointFromCursor(Cursor c) {
		MaintenancePointBean maintenancePointBean = new MaintenancePointBean();
		maintenancePointBean.setMaintenancePointName(c.getString(c.getColumnIndex(MaintenancePointBean.MAINTENANCE_POINT_NAME)));
		maintenancePointBean.setMaintenancePointDetail(c.getString(c.getColumnIndex(MaintenancePointBean.MAINTENANCE_POINT_ADDRESS)));
		maintenancePointBean.setMaintenancePointTel(c.getString(c.getColumnIndex(MaintenancePointBean.MAINTENANCE_POINT_TELEPHONE)));
		maintenancePointBean.setMaintenancePointDistance(c.getString(c.getColumnIndex(MaintenancePointBean.MAINTENANCE_POINT_DISTANCE)));
		maintenancePointBean.setMaintenancePointUrl(c.getString(c.getColumnIndex(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_URL)));
		
		return maintenancePointBean;
	}
	
	public static List<MaintenancePointBean> getMaintenancePointLocal(ContentResolver cr, long aid, long bid){
		List<MaintenancePointBean> result = new ArrayList<MaintenancePointBean>();
		Cursor c = cr.query(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION, MaintenancePointBean.MAINTENCE_PROJECTION_AID_BID_SELECTION, new String[]{String.valueOf(aid), String.valueOf(bid)}, null);
		if (c != null) {
			while (c.moveToNext()) {
				result.add(getMaintenancePointFromCursor(c));
			}
			c.close();
		}
		return result;
	}

	public static int deleteCachedData(ContentResolver cr, String aid, String bid) {
		return cr.delete(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION_AID_BID_SELECTION, new String[]{aid, bid});
	}
	
	public static long isExsited(ContentResolver cr, String aid, String bid) {
		long id = -1;
		Cursor c = cr.query(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION, MaintenancePointBean.MAINTENCE_PROJECTION_AID_BID_SELECTION, new String[]{aid, bid}, null);
		if (c != null) {
			if (c.moveToNext()) {
				id = c.getLong(0);
			}
			c.close();
		}
		return id;
	}
}
