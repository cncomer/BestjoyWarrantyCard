package com.bestjoy.app.warrantycard.account;

import org.json.JSONException;
import org.json.JSONObject;

import com.shwy.bestjoy.utils.InfoInterfaceImpl;

public class HomeParser extends InfoInterfaceImpl{
	private static final String TAG = "AccountParser";
	
	
	/**
	 * {"ShenFen":"江苏宝应宝应大道","City":"扬州","QuXian":"江苏宝应宝应大道","DetailAddr":"江苏宝应宝应大道","UID":1,"AID":1,"Tag": "第一个家"}
	 * @param jsonObject
	 * @param accountObject
	 * @throws JSONException
	 */
	public static HomeObject parseHomeAddress(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		HomeObject homeObject = new HomeObject();
		homeObject.mHomeProvince = jsonObject.getString("ShenFen");
		homeObject.mHomeCity = jsonObject.getString("City");
		homeObject.mHomeDis = jsonObject.getString("QuXian");
		homeObject.mHomePlaceDetail = jsonObject.getString("DetailAddr");
		
		homeObject.mHomeUid = jsonObject.getLong("UID");
		homeObject.mHomeAid = jsonObject.getLong("AID");
		homeObject.mHomeName = jsonObject.getString("Tag");
		
		//小区相关
		//"xiaoqu":{"aid":"787018","xiaoqu_name":"大宁龙盛雅苑小区","xid":"1"}}
		JSONObject xiaoqu = jsonObject.optJSONObject("xiaoqu");
		if (xiaoqu != null) {
			homeObject.mHid = xiaoqu.optLong("xid", -1);
			homeObject.mHname = xiaoqu.optString("xiaoqu_name", "");
		}
		return homeObject;
	}
	/**
	 * 
	 * @param jsonObject
	 * @param accountObject
	 * @param position  位置
	 * @return
	 * @throws JSONException
	 */
	public static HomeObject parseHomeAddress(JSONObject jsonObject, AccountObject accountObject, int position) throws JSONException {
		HomeObject homeObject = parseHomeAddress(jsonObject, accountObject);
		homeObject.mHomePosition = position;
		return homeObject;
	}
}
