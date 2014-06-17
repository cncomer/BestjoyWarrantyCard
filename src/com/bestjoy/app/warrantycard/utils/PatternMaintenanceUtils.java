package com.bestjoy.app.warrantycard.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PatternMaintenanceUtils {

	public static List<MaintenancePointBean> getMaintenancePoint(JSONArray addresses) throws JSONException {
		List<MaintenancePointBean> result = new ArrayList<MaintenancePointBean>();
		for(int i = 0; i < addresses.length(); i++) {
			MaintenancePointBean maintenancePointBean = new MaintenancePointBean();
			JSONObject obj = addresses.getJSONObject(i);
			maintenancePointBean.setMaintenancePointName(obj.has(MaintenancePointBean.MAINTENANCE_POINT_NAME)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_NAME):"");
			maintenancePointBean.setMaintenancePointDetail(obj.has(MaintenancePointBean.MAINTENANCE_POINT_ADDRESS)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_ADDRESS):"");
			maintenancePointBean.setMaintenancePointTel(obj.has(MaintenancePointBean.MAINTENANCE_POINT_TELEPHONE)?obj.getString(MaintenancePointBean.MAINTENANCE_POINT_TELEPHONE):"");
			maintenancePointBean.setMaintenancePointDistance(obj.getJSONObject(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_INFO).has(MaintenancePointBean.MAINTENANCE_POINT_DISTANCE)?obj.getJSONObject(MaintenancePointBean.MAINTENANCE_POINT_DETAIL_INFO).getString(MaintenancePointBean.MAINTENANCE_POINT_DISTANCE):"");
			result.add(maintenancePointBean);
		}
		return result;
	}
}
