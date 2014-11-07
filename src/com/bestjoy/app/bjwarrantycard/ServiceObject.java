package com.bestjoy.app.bjwarrantycard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.SecurityUtils;
import com.shwy.bestjoy.utils.UrlEncodeStringBuilder;


public class ServiceObject {
	private static final String TAG = "ServiceObject";
	public static final String SERVICE_URL = "http://www.dzbxk.com/bestjoy/";
	public static final String PRODUCT_GENERAL_AVATOR_URL= "http://www.dzbxk.com/pimg/";
	public static final String PRODUCT_AVATOR_URL= "http://www.dzbxk.com/proimg/";
	/**发票路径的前缀*/
	public static final String FAPIAO_URL = "http://www.dzbxk.com/fapiao/";
	
	public static final String HOME_DELETE_URL = SERVICE_URL + "DeleteAddressByAID.ashx?";
	
	private static String mHaierPinpaiName;
	public static final String BX_PHONE_HAIER = "400699999";
	private static String mKasadiPinpaiName;
	public static final String BX_PHONE_KASADI = "4006399699";
	private static String mTongShuaiPinpaiName;
	public static final String BX_PHONE_TONGSHUAI = "4006999999";
	public static void setContext(Context context) {
		mHaierPinpaiName = context.getString(R.string.pinpai_haier);
		mKasadiPinpaiName = context.getString(R.string.pinpai_kasadi);
		mTongShuaiPinpaiName = context.getString(R.string.pinpai_tongshuai);
	}
	public static boolean isHaierPinpai(String pinpaiName) {
		return mHaierPinpaiName.equals(pinpaiName);
	}
	/**
	 * 是否是卡萨帝品牌
	 * @param pinpaiName
	 * @return
	 */
	public static boolean isKasadiPinpai(String pinpaiName) {
		return mKasadiPinpaiName.equals(pinpaiName);
	}
	
	/**
	 * 是否是统帅品牌
	 * @param pinpaiName
	 * @return
	 */
	public static boolean isTongShuaiPinpai(String pinpaiName) {
		return mTongShuaiPinpaiName.equals(pinpaiName);
	}
	/**
	 * 是否是海尔品牌
	 * @param pinpaiName
	 * @return
	 */
	public static boolean isHaierPinpaiGenaral(String pinpaiName) {
		return isHaierPinpai(pinpaiName)
				|| isKasadiPinpai(pinpaiName)
				|| isTongShuaiPinpai(pinpaiName);
	}
	/**
	 * 返回品牌的售后服务电话
	 * @param pinpaiName
	 * @param defaultValue
	 * @return
	 */
	public static String getBXPhoneHaierPinpaiGenaral(String pinpaiName, String defaultValue) {
		if (isHaierPinpai(pinpaiName)) {
			return BX_PHONE_HAIER;
		} else if (isKasadiPinpai(pinpaiName)) {
			return BX_PHONE_KASADI;
		} else if (isTongShuaiPinpai(pinpaiName)) {
			return BX_PHONE_TONGSHUAI;
		} 
		return defaultValue;
	}

	
	/***
	   * 产品图片网址  http://115.29.231.29/pimg/5070A000A.jpg
	   * @return
	   */
	public static String getProdcutGeneralAvatorUrl(String ky) {
		  StringBuilder sb = new StringBuilder(PRODUCT_GENERAL_AVATOR_URL);
		  sb.append(ky).append(".jpg");
		  return sb.toString();
	}
	/***
	   * 产品图片网址  http://115.29.231.29/proimg/507/5070A000A.jpg  说明5070A000A：为Key，507：key 为前三位。
	   * 如果ky只有三位，那么我们认为是要显示ky3对应的默认图片
	   * @return
	   */
	public static String getProdcutAvatorUrl(String ky) {
		if (ky.length() > 3) {
			String ky3 = ky.substring(0, 3);
			  StringBuilder sb = new StringBuilder(PRODUCT_AVATOR_URL);
			  sb.append(ky3).append("/").append(ky).append(".jpg");
			  return sb.toString(); 
		} else {
			return getProdcutGeneralAvatorUrl(ky);
		}
		
	}
	//modify by chenkai, 修改发票后台同步修改新建更新和登录后台, 20140622 begin
	public static String getCreateBaoxiucardUri() {
		StringBuilder sb = new StringBuilder(SERVICE_URL);
		sb.append("20140625/AddBaoXiu.ashx");
		return sb.toString();
	}
	
	public static String getUpdateBaoxiucardUri() {
		StringBuilder sb = new StringBuilder(SERVICE_URL);
		sb.append("20140625/updateBaoXiu.ashx");
		return sb.toString();
	}
	
	/**
	 * 发票路径为http://www.dzbxk.com/fapiao/图片名.jpg, 图片名=md5(AID+UID)
	 * @param aid
	 * @param bid
	 * @return
	 */
	public static String getBaoxiucardFapiao(String photoPath) {
		return photoPath;
	}
	//modify by chenkai, 修改发票后台同步修改新建更新和登录后台, 20140622 end
	
	/**
	 * 删除保修数据： serverIP/Haier/DeleteBaoXiuByBIDUID.ashx
	 * @param BID:保修ID
	 * @param UID:用户ID
	 * @return
	 */
	public static String getBaoxiuCardDeleteUrl(String bid, String uid) {
		StringBuilder sb = new StringBuilder(SERVICE_URL);
		sb.append("DeleteBaoXiuByBIDUID.ashx?");
		sb.append("BID=").append(bid)
		.append("&UID=").append(uid);
		return sb.toString();
	}
	
	//add by chenkai, 20140701, 将登录和更新调用的地址抽离出来，以便修改 begin
	/**
	 * 返回登陆调用URL
	 * @return
	 */
	public static String getRegisterUrl(String para, String jsonString) {
		UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
		sb.append("RegisterUser.ashx?")
		.append(para).append("=").appendUrlEncodedString(jsonString);
		return sb.toString();
	}
	/**
	 * 注册验证手机号步骤
	 * @return
	 */
	public static String getRegisterValidateTelUrl(String tel) {
		UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
		sb.append("ValidateUserTel.ashx?tel=").append(tel);
		return sb.toString();
	}
	/**
	 * http://www.dzbxk.com/bestjoy/20140718/GetToken.ashx?data=wangkun&key=8f76e86c54da27e0abb1a3605fb5d440
	 * @param data 加密前的数据
	 * @param key key是 md5(data) 
	 * @return
	 */
	public static String getSecurityToken(String data, String key) {
		UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
		sb.append("20140718/GetToken.ashx?")
		.append("data=").appendUrlEncodedString(data).append("&key=").appendUrlEncodedString(key);
		return sb.toString();
	}
	/**
	 * 根据当天的随机数 md5加密，取前8位字符。例如 780001 md5 后为83dc137dd3bd141fb22b431ccc8fc25e 前八位为 83dc137d
	 * @param password  当天的随机数
	 * @return
	 */
	public static String getSecurityToken(String password) {
		DebugUtils.logD(TAG, "getSecurityToken " + password);
		
		String md5Token = SecurityUtils.MD5.md5(password);
		DebugUtils.logD(TAG, "md5Token " + md5Token);
		
		md5Token = md5Token.substring(0, 8);
		DebugUtils.logD(TAG, "md5Token.substring(0, 8) " + md5Token);
		return md5Token;
	}
	//add by chenkai, 20140701, 将登录和更新调用的地址抽离出来，以便修改 end
	
	//add by chenkai, 20140726, 将发送短信抽离出来，以便修改 begin
	/**
	 * 返回登陆调用URL
	 * @param para
	 * @param desString DES加密后的字串
	 * @return
	 */
	public static String getFindPasswordUrl(String para, String DESString) {
		UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
		sb.append("20140726/SendMessage.ashx?")
		.append(para).append("=").appendUrlEncodedString(DESString);
		return sb.toString();
	}
	//add by chenkai, 20140726, 将发送短信抽离出来，以便修改 begin
	
	//add by chenkai, 20140726, 将添加新家抽离出来，以便修改 begin
	/**
	 * 添加新家
	 * @return
	 */
		public static String getCreateHomeUrl() {
			UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
			sb.append("20140718/Addaddr.ashx?");
			return sb.toString();
		}
		//add by chenkai, 20140726, 将添加新家抽离出来，以便修改 begin
	
	public static class ServiceResultObject {
		public int mStatusCode = 0;
		public String mStatusMessage;
		public JSONObject mJsonData;
		public String mStrData;
		public JSONArray mAddresses;
		public JSONArray mJsonArray;
		public int mTotal = 0;
		
		public static ServiceResultObject parse(String content) {
			ServiceResultObject resultObject = new ServiceResultObject();
			if (TextUtils.isEmpty(content)) {
				return resultObject;
			}
			try {
				JSONObject jsonObject = new JSONObject(content);
				resultObject.mStatusCode = Integer.parseInt(jsonObject.getString("StatusCode"));
				resultObject.mStatusMessage = jsonObject.getString("StatusMessage");
				DebugUtils.logD("HaierResultObject", "StatusCode = " + resultObject.mStatusCode);
				DebugUtils.logD("HaierResultObject", "StatusMessage = " +resultObject.mStatusMessage);
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

		public static ServiceResultObject parseAddress(String content) {
			ServiceResultObject resultObject = new ServiceResultObject();
			if (TextUtils.isEmpty(content)) {
				return resultObject;
			}
			try {
				JSONObject jsonObject = new JSONObject(content);
				resultObject.mAddresses = jsonObject.getJSONArray("results");
				resultObject.mStatusCode = Integer.parseInt(jsonObject.getString("status"));
				resultObject.mStatusMessage = jsonObject.getString("message");
				DebugUtils.logD("HaierResultObject", "mAddresses = " + resultObject.mAddresses);
				DebugUtils.logD("HaierResultObject", "StatusCode = " + resultObject.mStatusCode);
				DebugUtils.logD("HaierResultObject", "StatusMessage = " +resultObject.mStatusMessage);
				resultObject.mTotal = jsonObject.getInt("total");
				try {
					resultObject.mJsonData = jsonObject.getJSONObject("results");
				} catch (JSONException e) {
					resultObject.mStrData = jsonObject.getString("results");
				}
			} catch (JSONException e) {
				e.printStackTrace();
				resultObject.mStatusMessage = e.getMessage();
			}
			return resultObject;
		}
		
		public static ServiceResultObject parseArray(String content) {
			ServiceResultObject resultObject = new ServiceResultObject();
			if (TextUtils.isEmpty(content)) {
				return resultObject;
			}
			try {
				JSONObject jsonObject = new JSONObject(content);
				resultObject.mStatusCode = Integer.parseInt(jsonObject.getString("StatusCode"));
				resultObject.mStatusMessage = jsonObject.getString("StatusMessage");
				DebugUtils.logD("HaierResultObject", "mAddresses = " + resultObject.mAddresses);
				DebugUtils.logD("HaierResultObject", "StatusCode = " + resultObject.mStatusCode);
				DebugUtils.logD("HaierResultObject", "StatusMessage = " +resultObject.mStatusMessage);
				try {
					resultObject.mJsonArray = jsonObject.getJSONArray("Data");
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
	
	//add by chenkai, for Usage, 2014.05.31 begin
		/**www.51cck.com/PD前9位数字/PD前13位[.htm][.pdf]*/
	    public static final String GOODS_INTRODUCTION_BASE = "http://www.51cck.com/";
		/***
		   * www.51cck.com/KY前9位数字/KY.pdf
		   * @return
		   */
		  public static String getProductUsageUrl(String ky9, String ky) {
			  StringBuilder sb = new StringBuilder(GOODS_INTRODUCTION_BASE);
			  sb.append(ky9).append("/").append(ky).append(".pdf");
			  return sb.toString();
		  }
		  /***
		   * http://www.51cck.com/haier/264574251GD0N5T00W.pdf
		   * @return
		   */
		  public static String getProductUsageUrl(String file) {
			  StringBuilder sb = new StringBuilder(GOODS_INTRODUCTION_BASE);
			  sb.append("haier/").append(file);
			  return sb.toString();
		  }
		 //add by chenkai, for Usage, 2014.05.31 end
		  
		  /**
		   * 查询是否有使用说明书,http://115.29.231.29/haier/getPDfByKy.ashx?KY=2050100P1&token=df6037a3709a77279dde4334c4038178
		   * @param ky 产品的9位KY
		   * @param token Md5(KY)
		   * @return
		   */
		  public static String getProductPdfUrlForQuery(String ky) {
			  StringBuilder sb = new StringBuilder(SERVICE_URL);
			  sb.append("getPDfByKy.ashx?KY=").append(ky).append("&token=").append(SecurityUtils.MD5.md5(ky));
			  return sb.toString();
		  }
		  
		  
		  /**
		   * http://www.dzbxk.com/bestjoy/Apple/RegisterDevice.ashx?UID=1&pushtoken=test&devicetype=android
		   */
		  public static String getUpdateDeviceTokenUrl(String uid, String deviceToken, String deviceType) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Apple/RegisterDevice.ashx?UID=").appendUrlEncodedString(uid)
			  .append("&pushtoken=").appendUrlEncodedString(deviceToken)
			  .append("&devicetype=").appendUrlEncodedString(deviceType);
			  return sb.toString();
			  
		  }
		  public static String getBaoxiucardSalesmanAvatorPreview(String mm) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder("http://www.mingdown.com/mmimage/");
			  sb.append(mm).append(".jpg");
			  return sb.toString();
		  }
		  
		  public static String updateBaoxiucardSalesmanInfo(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("AddBIDMM.ashx?")
			  .append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  /**
		   * 从条码中识别出保修卡条码，从网络获取条码的保修卡对象信息
		   * @return
		   */
		  public static String queryBaoxiuCardUrlFromBarCode() {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Deal.ashx?para=");
			  return sb.toString();
		  }
		  /**
		   * 意见反馈url
		   * @param para
		   * @param jsonString
		   * @return
		   */
		  public static String getFeedbackUrl(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("AddSuggestion.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  
		  public static String getWeatherUrl(String adminCode, String citykey, String uid, String token) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Weather/GetWeatherByAdminCode.ashx?admin_code=").append(adminCode).append("&citykey=").append(citykey)
			  .append("&uid=").append(uid == null?"":uid)
			  .append("&token=").append(token == null?"":token)
			  .append("&devicetype=").append("1");
			  return sb.toString();
		  }
		  
		  public static String getWeatherIcon(String photoId) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Weather/icon/day/").append(photoId).append(".png");
			  return sb.toString();
		  }
		  
		  /**
		   * 获取suid的关系
		   *  http://115.29.231.29/Haier/Start/GetServiceUserByUID.ashx?uid=682038&pwd=844605
		   */
		  public static String getRelationshipUrl(String uid, String pwd) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Start/GetServiceUserByUID.ashx?uid=").append(uid).append("&pwd=").append(pwd);
			  return sb.toString();
		  }
		  /**
		   * http://115.29.231.29/Haier/Start/GetMessageByUIDByTID.ashx?UID=607421&TID=42300&type=p2p
		   * @param uid
		   * @param target
		   * @return
		   */
		  public static String getMessagesUrlByUidByTid(String uid, String target, String targetType) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Start/GetMessageByUIDByTID.ashx?uid=").append(uid).append("&TID=").append(target).append("&type=").append(targetType);
			  return sb.toString();
		  }
		  public static String getMessagesUrlByUidByTid(String uid, String target, int targetType) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Start/GetMessageByUIDByTID.ashx?uid=").append(uid).append("&TID=").append(target).append("&type=").append(targetType);
			  return sb.toString();
		  }
		  
		  public static String buildPageQuery(String url, int pageIndex, int pageSize) {
			  StringBuilder sb = new StringBuilder(url).append('&');
			  sb.append("pageindex=").append(pageIndex).append('&');
			  sb.append("pagesize=").append(pageSize);
	    	  return sb.toString();
		  }
		  
		  public static String getPoiNearbySearchUrl(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("GetNearbyxq.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  /**
		   * 关联家和小区
		   * @param para
		   * @param jsonString
		   * @return
		   */
		  public static String relatedHomeToCommunity(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Xiaoqu/addXiaoQuDetail.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  public static String getCommunityServiceUpdateUrl(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Xiaoqu/addPhone.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  
		  public static String getCommunityServices(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Xiaoqu/GetXiaoQuAround.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  /**
		   * 新建汽车卡
		   * @return
		   */
		  public static String createCarBaoxiuCardUrl() {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Car/AddCar.ashx?");
			  return sb.toString();
		  }
		  /**
		   * 更新汽车卡
		   * @return
		   */
		  public static String updateCarBaoxiuCardUrl() {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Car/updateCar.ashx?");
			  return sb.toString();
		  }
		  /***
		   * www.dzbxk.com/bestjoy/Car/GetCar.ashx?para={uid:"575401"}
		   * @param para
		   * @param jsonString
		   * @return
		   */
		  public static String getAllCarBaoxiuCardsUrl(String uid) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Car/GetCar.ashx?").append("uid=").appendUrlEncodedString(uid);
			  return sb.toString();
		  }
		  /**
		   * 更新汽车卡中的电话
		   * @return
		   */
		  public static String updateCarBaoxiuCardPhoneUrl(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("Car/updatecarphone.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  
		  public static String getCarBaoxiuCardDeleteUrl(String bid, String uid) {
				StringBuilder sb = new StringBuilder(SERVICE_URL);
				sb.append("Car/DeleteBaoXiuByCIDUID.ashx?");
				sb.append("cid=").append(bid)
				.append("&uid=").append(uid);
				return sb.toString();
			}
		  
		  public static String getViewConversationUrl(String KY, String currentid, String fangxiang) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("GetKYMessage.ashx?KY=").append(KY).append("&currentid=").append(currentid).append("&fangxiang=").append(fangxiang);
			  return sb.toString();
		  }
		  
		  public static String postViewConversationUrl(String KY, String uid, String message) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("LeaveMessage/AddKeyMessage.ashx?KY=").append(KY).append("&UID=").append(uid).append("&Message=").append(message);
			  return sb.toString();
		  }
		  public static String postViewConversationUrl(String para, String jsonString) {
			  UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(SERVICE_URL);
			  sb.append("LeaveMessage/AddKeyMessage.ashx?").append(para).append("=").appendUrlEncodedString(jsonString);
			  return sb.toString();
		  }
		  
}
