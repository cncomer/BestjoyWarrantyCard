package com.bestjoy.app.bjwarrantycard.imbasedympush;

import org.json.JSONException;
import org.json.JSONObject;

public class IMHelper {

	/**
	 * 消息类型， 0是登录，1是退出， 2是正常消息
	 */
	public static final String EXTRA_TYPE = "type";
	/**
	 * 消息体
	 */
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_UID = "uid";
	public static final String EXTRA_PWD = "pwd";
	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_SN = "sn";
	public static final int TYPE_LOGIN = 0;  //登录
	public static final int TYPE_EXIT = 1;  
	public static final int TYPE_MESSAGE = 2; 
	/**
	 * 会话开始前，我们需要先登录IM服务器
	 * @param sn
	 * @param uid
	 * @param pwd
	 * @param text
	 * @return
	 */
	public static JSONObject createOrJoinConversation(String sn, String uid, String pwd, String text) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(EXTRA_TYPE, TYPE_LOGIN);
			JSONObject data = new JSONObject();
			data.put(EXTRA_UID, uid).put(EXTRA_PWD, pwd).put(EXTRA_TEXT, text).put(EXTRA_SN, sn);
			jsonObject.put(EXTRA_DATA, data);
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
