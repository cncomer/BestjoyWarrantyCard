package com.bestjoy.app.bjwarrantycard.imbasedympush;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class IMHelper {

	/**
	 * 消息类型， 0是登录，1是退出， 2是消息， data数据中也会有一个type, 1是群消息，此时target字段对应群id, 比如sn, 2是点对点消息，target是对方uid
	 *
	 */
	public static final String EXTRA_TYPE = "type";
	/**
	 * 消息体
	 */
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_UID = "uid";
	public static final String EXTRA_PWD = "pwd";
	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_TARGET = "target";
	public static final int TYPE_LOGIN = 0;  //登录
	public static final int TYPE_EXIT = 1;  
	public static final int TYPE_MESSAGE = 2; 
	/**群消息*/
	public static final int TARGET_TYPE_QUN = 1;
	/**点对点消息*/
	public static final int TARGET_TYPE_P2P = 1;
	/**
	 * 会话开始前，我们需要先登录IM服务器
	 * @param uid
	 * @param pwd
	 * @return
	 */
	public static JSONObject createOrJoinConversation(String uid, String pwd) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(EXTRA_TYPE, String.valueOf(TYPE_LOGIN));
			jsonObject.put(EXTRA_UID, uid).put(EXTRA_PWD, pwd);
			jsonObject.put(EXTRA_DATA, "");
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	/***
	 * 退出登录状态
	 * @param uid
	 * @param pwd
	 * @return
	 */
	public static JSONObject exitConversation(String uid, String pwd) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(EXTRA_TYPE, String.valueOf(TYPE_EXIT));
			jsonObject.put(EXTRA_UID, uid).put(EXTRA_PWD, pwd);
			jsonObject.put(EXTRA_DATA, "");
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	/**
	 * 创建消息体
	 * @param uid
	 * @param pwd
	 * @param targetType
	 * @param target
	 * @param text
	 * @return
	 */
	public static JSONObject createMessageConversation(String uid, String pwd, int targetType, String target, String text) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(EXTRA_TYPE, String.valueOf(TYPE_MESSAGE));
			jsonObject.put(EXTRA_UID, uid).put(EXTRA_PWD, pwd);
			JSONObject data = new JSONObject();
			data.put(EXTRA_TYPE, String.valueOf(targetType)).put(EXTRA_TARGET, target).put(EXTRA_TEXT, text);
			jsonObject.put(EXTRA_DATA, data);
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	
	public static class ImServiceResultObject {
		public int mStatusCode = 0;
		public String mStatusMessage;
		public JSONObject mJsonData;
		public String mType;
		public String mStrData;
		
		public static ImServiceResultObject parse(String content) {
			ImServiceResultObject resultObject = new ImServiceResultObject();
			if (TextUtils.isEmpty(content)) {
				return resultObject;
			}
			try {
				JSONObject jsonObject = new JSONObject(content);
				resultObject.mStatusCode = Integer.parseInt(jsonObject.getString("StatusCode"));
				resultObject.mStatusMessage = jsonObject.getString("StatusMessage");
				//消息类型
				resultObject.mType = jsonObject.getString("type");
				try {
					resultObject.mJsonData = jsonObject.getJSONObject("Data");
				} catch (JSONException e) {
					resultObject.mStrData = jsonObject.getString("Data");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				resultObject.mStatusMessage = e.getMessage();
			}
			return resultObject;
		}
		public boolean isOpSuccessfully() {
			return mStatusCode == 1;
		}
	}
}
