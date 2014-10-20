package com.bestjoy.app.bjwarrantycard.propertymanagement;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;

import com.shwy.bestjoy.utils.InfoInterface;

public class HomesCommunityObject implements InfoInterface {
	public String mName;
	public long mlat, mlng;
	public int mDistance;
	public long mHid, mId;
	public String mCity, mProv, mDis, mAddressDetail;
	
//	public static HomesCommunityObject getHomesCommunityObject() {
//		
//	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		return false;
	}
	
	
	/**
	 * http://www.dzbxk.com/bestjoy/GetNearbyxq.ashx?para={admin_code:310115,address:%22%E6%B5%A6%E4%B8%9C%E5%8C%BA%E5%BC%A0%E6%B1%9F%E9%AB%98%E7%A7%91%E6%8A%80%E5%9B%AD%E5%8C%BA%E7%A2%A7%E6%B3%A2%E8%B7%AF49%E5%BC%841%E5%8F%B7%20%22,radius:%222000%22,pagesize:%2250%22}
	 * 
	 * 
	 * 
	 * @return
	 */
	public static List<HomesCommunityObject> parse(JSONArray communities) {
		int count = communities.length();
		List<HomesCommunityObject> list = null;
		if (count > 0) {
			list = new ArrayList<HomesCommunityObject>(count);
			JSONObject jsonObject = null;
			for(int index=0; index<count; index++){
				try {
					jsonObject = communities.getJSONObject(index);
					HomesCommunityObject homesCommunityObject = new HomesCommunityObject();
					homesCommunityObject.mName = jsonObject.getString("name");
					homesCommunityObject.mAddressDetail = jsonObject.getString("address");
					homesCommunityObject.mDistance = jsonObject.getJSONObject("detail_info").getInt("distance");
					list.add(homesCommunityObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			list = new ArrayList<HomesCommunityObject>();
		}
		
		return list;
		
	}

}
