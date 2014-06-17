package com.bestjoy.app.warrantycard.utils;

import android.text.TextUtils;

public class MaintenancePointBean {
	/**维修点名称*/
	private String maintenancePointName;
	/**维修点详细地址*/
	private String maintenancePointDetail;
	/**维修点电话*/
	private String maintenancePointTel;
	/**维修点距离*/
	private String maintenancePointDistance;
	
	public static final String MAINTENANCE_POINT_NAME = "name";
	public static final String MAINTENANCE_POINT_ADDRESS = "address";
	public static final String MAINTENANCE_POINT_TELEPHONE = "telephone";
	public static final String MAINTENANCE_POINT_DISTANCE = "distance";
	public static final String MAINTENANCE_POINT_DETAIL_INFO = "detail_info";
	

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
		Float f = 0f;
		if(!TextUtils.isEmpty(maintenancePointDistance)) {
			f = Float.valueOf(maintenancePointDistance) / 1000;
		}
		this.maintenancePointDistance = String.format("%.1f", f) + "km";
	}
}
