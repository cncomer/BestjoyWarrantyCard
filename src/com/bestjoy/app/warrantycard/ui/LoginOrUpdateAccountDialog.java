package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.im.RelationshipActivity;
import com.bestjoy.app.bjwarrantycard.propertymanagement.HomesCommunityManager;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.AccountParser;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.service.IMService;
import com.bestjoy.app.warrantycard.update.UpdateService;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.UrlEncodeStringBuilder;
/**
 * 这个类用来更新和登录账户使用。
 * @author chenkai
 *
 */
public class LoginOrUpdateAccountDialog extends Activity{

	private static final String TAG = "LoginOrUpdateAccountDialog";
	private AccountObject mAccountObject;
	private String mTel, mPwd;
	private boolean mIsLogin = false;
	private TextView mStatusView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_or_update_layout);
		mStatusView = (TextView) findViewById(R.id.title);
		Intent intent = getIntent();
		mIsLogin = intent.getBooleanExtra(Intents.EXTRA_TYPE, true);
		mTel = intent.getStringExtra(Intents.EXTRA_TEL);
		mPwd = intent.getStringExtra(Intents.EXTRA_PASSWORD);
		loginAsync();
	}

	private LoginAsyncTask mLoginAsyncTask;
	private void loginAsync() {
		mStatusView.setText(mIsLogin?R.string.msg_login_dialog_title_wait:R.string.msg_update_dialog_title_wait);
		AsyncTaskUtils.cancelTask(mLoginAsyncTask);
		mLoginAsyncTask = new LoginAsyncTask();
		mLoginAsyncTask.execute();
	}
	private class LoginAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private InputStream _is;
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			mAccountObject = null;
			ServiceResultObject serviceResultObject  = new ServiceResultObject();
			_is = null;
			UrlEncodeStringBuilder sb = new UrlEncodeStringBuilder(ServiceObject.SERVICE_URL);
			sb.append("20140625/loginandroid.ashx?cell=").append(mTel)
			.append("&pwd=").appendUrlEncodedString(mPwd).append("&devicetype=").append("android");
			try {
				_is = NetworkUtils.openContectionLocked(sb.toString(), mPwd, null);
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(_is));
				if (serviceResultObject.isOpSuccessfully()) {
					mAccountObject = AccountParser.parseJson(serviceResultObject.mJsonData, mStatusView);
					NetworkUtils.closeInputStream(_is);
					ContentResolver cr = LoginOrUpdateAccountDialog.this.getContentResolver();
					if (mAccountObject != null && mAccountObject.mAccountUid > 0) {
						//Step1 删除演示账户
						MyAccountManager.deleteAccountForUid(cr, AccountObject.DEMO_ACCOUNT_UID);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete AccountObject demo");
						int deleted = BjnoteContent.delete(cr, BjnoteContent.Homes.CONTENT_URI, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete Homes effected rows#" + deleted);
						BjnoteContent.delete(cr, BjnoteContent.BaoxiuCard.CONTENT_URI, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete BaoxiuCards effected rows#" + deleted);
						BjnoteContent.delete(cr, BjnoteContent.RELATIONSHIP.CONTENT_URI, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete RELATIONSHIP effected rows#" + deleted);
						BjnoteContent.delete(cr, BjnoteContent.IM.CONTENT_URI_FRIEND, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete IM.FRIEND effected rows#" + deleted);
						BjnoteContent.delete(cr, BjnoteContent.IM.CONTENT_URI_QUN, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete IM.QUN effected rows#" + deleted);
						BjnoteContent.delete(cr, BjnoteContent.YMESSAGE.CONTENT_URI, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete YMESSAGE effected rows#" + deleted);
						BjnoteContent.delete(cr, HomesCommunityManager.COMMUNITY_SERVICE_CONTENT_URI, null, null);
						DebugUtils.logD(TAG, "LoginAsyncTask start to delete COMMUNITY_SERVICES effected rows#" + deleted);
						//标识下次不用拉取演示数据了
			        	MyApplication.getInstance().mPreferManager.edit().putBoolean("need_load_demo_home", false).commit();
			        	//标识下次需要拉取数据了
			        	ComPreferencesManager.getInstance().setFirstLaunch(MyChooseCarCardsActivity.TAG, true);
			        	DebugUtils.logD(TAG, "LoginAsyncTask start to reset need_load_demo_home as false");
			        	
			        	if (mAccountObject.mAccountHomes.size() == 0) {
							//Setp2 家数据为空，我们需要创建演示家
							HomeObject homeObject = HomeObject.getDemoHomeObject(mAccountObject.mAccountUid, HomeObject.DEMO_HOME_AID);
							DebugUtils.logD(TAG, "LoginAsyncTask start to insert HomeObject demo " + homeObject.toString());
							mAccountObject.mAccountHomes.add(homeObject);
							//Setp3, 创建保修卡演示数据
							ServiceResultObject serviceObject = new ServiceResultObject();
							sb = new UrlEncodeStringBuilder("http://www.dzbxk.com/bestjoy/GetBaoXiuDataByUID.ashx?");
							sb.append("UID=").append(AccountObject.DEMO_ACCOUNT_UID)
							.append("&AID=").append(HomeObject.DEMO_HOME_AID);
				            _is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				            if (_is != null) {
				            	serviceObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(_is));
				            	if (serviceObject.isOpSuccessfully()) {
				            		if (serviceObject.mStrData != null) {
				            			JSONArray baoxiuCards = new JSONArray(serviceObject.mStrData);
				            			//JSONArray baoxiuCards = serviceObject.mJsonData.getJSONArray(BaoxiuCardObject.JSONOBJECT_NAME);
					            		int len = baoxiuCards.length();
					            		BaoxiuCardObject baoxiuCardObject = null;
					            		for(int index=0; index < len; index++) {
					            			baoxiuCardObject = BaoxiuCardObject.parseBaoxiuCards(baoxiuCards.getJSONObject(index), null);
					            			baoxiuCardObject.mAID = HomeObject.DEMO_HOME_AID;
					            			baoxiuCardObject.mUID = mAccountObject.mAccountUid;
					            			homeObject.mBaoxiuCards.add(baoxiuCardObject);
					            		}
				            		} else {
				            			MyApplication.getInstance().showMessageAsync(R.string.msg_get_no_demo_data);
				            		}
				            		
				            	}
				            }
						}
			        	
						boolean saveAccountOk = MyAccountManager.getInstance().saveAccountObject(cr, mAccountObject);
						if (!saveAccountOk) {
							//登录成功了，但本地数据保存失败，通常不会走到这里
							serviceResultObject.mStatusMessage = LoginOrUpdateAccountDialog.this.getString(R.string.msg_login_save_success);
						}
					}
				
				} 
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
			} catch (JSONException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(_is);
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				//通常不走到这里
				onCancelled();
				return;
			}
			if (!result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				setResult(Activity.RESULT_CANCELED);
			} else if (mAccountObject != null) {
				//如果登陆成功
				IMService.connectIMService(LoginOrUpdateAccountDialog.this);
				setResult(Activity.RESULT_OK);
				//每次登陆，我们都需要注册设备Token
				YouMengMessageHelper.getInstance().saveDeviceTokenStatus(false);
				//登录成功，我们需要检查是否能够上传设备Token到服务器绑定uid和token
				UpdateService.startCheckDeviceTokenToService(LoginOrUpdateAccountDialog.this);
				//每次登录我们都重新设置需要重新拉好友列表
				ComPreferencesManager.getInstance().setFirstLaunch(RelationshipActivity.FIRST, true);
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				setResult(Activity.RESULT_CANCELED);
			}
			finish();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			MyApplication.getInstance().showMessage(R.string.msg_op_canceled);
			setResult(Activity.RESULT_CANCELED);
			finish();
			
		}
		
		public void cancelTask(boolean cancel) {
			super.cancel(cancel);
			//由于IO操作是不可中断的，所以我们这里关闭IO流来终止任务
			NetworkUtils.closeInputStream(_is);
			
		}
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mLoginAsyncTask != null) {
			mLoginAsyncTask.cancelTask(true);
			DebugUtils.logD(TAG, "login or update is canceled by user");
		}
	}
	
	public static Intent createLoginOrUpdate(Context context, boolean login, String tel, String pwd) {
		Intent intent = new Intent(context, LoginOrUpdateAccountDialog.class);
		intent.putExtra(Intents.EXTRA_TYPE, login);
		intent.putExtra(Intents.EXTRA_TEL, tel);
		intent.putExtra(Intents.EXTRA_PASSWORD, pwd);
		return intent;
	}
	
}
