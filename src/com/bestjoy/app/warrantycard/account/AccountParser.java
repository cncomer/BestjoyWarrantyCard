package com.bestjoy.app.warrantycard.account;

import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterfaceImpl;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;
/**
 * 这个类用来解析登陆返回的json数据，生成AccountObject账户对象。<br/>
 * 登陆测试数据如下：<br/>
 * http://115.29.231.29/Haier/login.ashx?cell=18621951097&pwd=wangkun
 * <br/>
 * <br/>
 * 返回数据如下<br/>
 * {"usersdata":{"StatusCode":"1","StatusMessage":"返回用户数据","Data":{"cell":"18621951097","pwd":"wangkun","yanma":null,"userName":"王坤","UID":1}},"address":[{"ShenFen":"江苏宝应宝应大道","City":"扬州","QuXian":"江苏宝应宝应大道","DetailAddr":"江苏宝应宝应大道","UID":1,"AID":1}]}
 * <br/>
 * 数据说明如下<br/>
 * 其中："usersdata" 里是上次的数据<br/>
 * cell:手机<br/>
 * pwd:密码<br/>
 * yama:验证码<br/>
 * username:名字<br/>
 * uid:唯一iD<br/>
 * <p/>
 * "address"：地址列表<br/>
 * ShenFen：省<br/>
 * City:市<br/>
 * QuXian:区县<br/>
 * DetailAddr：具体地址<br/>
 * UID:用户id <br/>
 * AID:地址id<br/>
 * 
 * @author chenkai
 *
 */
public class AccountParser extends InfoInterfaceImpl{
	private static final String TAG = "AccountParser";

	/**
	 * 解析JSON格式的Account数据，如果返回null，表示解析失败，如果返回accountObject.mStatusCode == 0，表示登录失败
	 * @param is
	 * @param goodsObject
	 * @return
	 * @throws JSONException 
	 */
	public static AccountObject parseJson(JSONObject jsonObject, final TextView view) throws JSONException {
		if (view != null) {
			MyApplication.getInstance().postAsync(new Runnable() {

				@Override
				public void run() {
					view.setText(R.string.msg_login_download_accountinfo_wait);
				}
			});
		}
		
		AccountObject accountObject = new AccountObject();
		//解析userdata
		parseUserData(jsonObject, accountObject);
		
		if (view != null) {
			MyApplication.getInstance().postAsync(new Runnable() {

				@Override
				public void run() {
					view.setText(R.string.msg_login_download_addressinfo_wait);
				}
			});
		}
		//解析address
		parseAddress(jsonObject, accountObject);
		
		if (view != null) {
			MyApplication.getInstance().postAsync(new Runnable() {

				@Override
				public void run() {
					view.setText(R.string.msg_login_download_baoxiuinfo_wait);
				}
			});
		}
		//解析保修卡
		parseBaoxiuCards(jsonObject, accountObject);
		return accountObject;
	}
	
	/***
	 * "usersdata": {"cell":"18621951097","pwd":"wangkun","yanma":null,"userName":"王坤","UID":1}
	 * @param jsonObject
	 * @param accountObject
	 * @throws JSONException
	 */
	public static void parseUserData(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		//解析userdata
		JSONObject userData = jsonObject.getJSONObject("account");
		accountObject.mAccountNickName = userData.getString("nickname");
		accountObject.mAccountTel = userData.getString("cell");
		accountObject.mAccountPwd = userData.getString("pwd");
		accountObject.mAccountName = userData.getString("userName");
		accountObject.mAccountUid = userData.getLong("UID");
	}
	
	/**
	 * 一个用户会有多个家地址，所以这里是一个数组，数组里每一个元素又是一个JSON对象，形如
	 * "address":[{"ShenFen":"江苏宝应宝应大道","City":"扬州","QuXian":"江苏宝应宝应大道","DetailAddr":"江苏宝应宝应大道","UID":1,"AID":1}]
	 * @param jsonObject
	 * @param accountObject
	 * @throws JSONException
	 */
	public static void parseAddress(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		//解析addresses
		JSONArray addresses = jsonObject.getJSONArray("address");
		accountObject.mAccountHomes.clear();
		if (addresses != null) {
			int len = addresses.length();
			HomeObject homeObject = null;
			for(int index=0; index < len; index++) {
				homeObject = HomeParser.parseHomeAddress(addresses.getJSONObject(index), accountObject, index);
				if (homeObject != null) {
					accountObject.mAccountHomes.add(homeObject);
				}
			}
			accountObject.mAccountHomeCount = accountObject.mAccountHomes.size();
		}
	}
	
	public static void parseBaoxiuCards(JSONObject jsonObject, AccountObject accountObject) throws JSONException {
		//解析baoxiu
		JSONArray baoxiuCards = jsonObject.getJSONArray(BaoxiuCardObject.JSONOBJECT_NAME);
		if (baoxiuCards != null) {
			int len = baoxiuCards.length();
			BaoxiuCardObject baoxiuCardObject = null;
			for(int index=0; index < len; index++) {
				baoxiuCardObject = BaoxiuCardObject.parseBaoxiuCards(baoxiuCards.getJSONObject(index), accountObject);
				if (baoxiuCardObject != null) {
					//每一个保修卡只会属于一个Home
					for(HomeObject homeObject : accountObject.mAccountHomes) {
						if (baoxiuCardObject.mAID == homeObject.mHomeAid) {
							homeObject.mBaoxiuCards.add(baoxiuCardObject);
							homeObject.mHomeCardCount += 1;
							continue;
						}
					}
					DebugUtils.logD(TAG, "find baoxiuCard " + baoxiuCardObject.toString());
				}
			}
		}
	}
	
}
