package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.HaierProCityDisEditPopView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DevicesUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class RegisterActivity extends BaseActionbarActivity implements View.OnClickListener{
	private static final String TAG = "RegisterActivity";
	
	private static final int REQUEST_LOGIN = 1;
	
	private Button mNextButton;
	private EditText mTelInput, mNameInput, mPasswordInput, mConfirmPasswordInput;
	
	private AccountObject mAccountObject;
	private Bundle mBundles;
	
	private HaierProCityDisEditPopView mProCityDisEditPopView;
	private EditText mHomeEditText;
	private HomeObject mHomeObject ;

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundles = getIntent().getExtras();
		return mBundles != null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugUtils.logD(TAG, "onCreate()");
		if (isFinishing()) {
			return ;
		}
		setTitle(R.string.title_new_user_register);
		mAccountObject = new AccountObject();
		setContentView(R.layout.activity_register_confirm);
		initViews();
	}

	private void initViews() {
		mNextButton = (Button) findViewById(R.id.button_next);
		mNextButton.setOnClickListener(this);
		
		mNameInput = (EditText) findViewById(R.id.name_input);
		mPasswordInput = (EditText) findViewById(R.id.password_input);
		mConfirmPasswordInput = (EditText) findViewById(R.id.confirm_password_input);
		mTelInput = (EditText) findViewById(R.id.tel_input);
		
		mProCityDisEditPopView = new HaierProCityDisEditPopView(this);
		mHomeEditText = (EditText) findViewById(R.id.my_home);
		mHomeEditText.setText(R.string.my_home);
	}
	
	 public boolean onCreateOptionsMenu(Menu menu) {
		 return false;
	 }
	 
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == Activity.RESULT_OK) {
				// login successfully
				MyApplication.getInstance().showMessage(R.string.msg_login_confirm_success);
				//注册成功，如果是先新建后注册，那么回到新建界面
				int modelId = ModleSettings.getModelIdFromBundle(mBundles);
				switch(modelId) {
				case R.id.model_my_card:
				case R.id.model_install:
				case R.id.model_repair:
					mBundles.putBoolean(Intents.EXTRA_HAS_REGISTERED, true);
					NewCardActivity.startIntentClearTop(mContext, mBundles);
					finish();
					break;
					default ://否则回到主界面
						MainActivity.startActivityForTop(mContext);
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public static void startIntent(Context context, Bundle modelBundel) {
		Intent intent = new Intent(context, RegisterActivity.class);
		if (modelBundel == null) {
			modelBundel = new Bundle();
		}
		intent.putExtras(modelBundel);
		context.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// add by chenkai, 开始前先检查网络 begin
		if (!ComConnectivityManager.getInstance().isConnected()) {
			ComConnectivityManager.getInstance().onCreateNoNetworkDialog(this);
			return;
		}
		// add by chekai, 开始前先检查网络 end
		switch (v.getId()) {
			case R.id.button_next:
				mAccountObject.mAccountName = mNameInput.getText().toString().trim();
				mAccountObject.mAccountPwd = mPasswordInput.getText().toString().trim();
				//modify by chenkai, 2014.06.04，去掉号码之间的空白符号 begin
				//mAccountObject.mAccountTel = mTelInput.getText().toString().trim();
				mAccountObject.mAccountTel = mTelInput.getText().toString().trim().replaceAll("[- +]", "");
				//modify by chenkai, 2014.06.04，去掉号码之间的空白符号 end
				
				if(valiInput()) {
					registerAsync();
				}
				break;
		}
	}
	
	private boolean valiInput() {
		if (TextUtils.isEmpty(mAccountObject.mAccountName)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_name);
			return false;
		}
		if (TextUtils.isEmpty(mAccountObject.mAccountPwd)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_pwd);
			return false;
		}
		//delete by chenkai, 不用确认密码 begin
		/*
		String usrPwdConfirm = mConfirmPasswordInput.getText().toString().trim();
		if (!usrPwdConfirm.equals(mAccountObject.mAccountPwd)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_pwd_not_match_tips);
			return false;
		}
		*/
		//delete by chenkai, 不用确认密码 end
		if (TextUtils.isEmpty(mAccountObject.mAccountTel)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usrtel);
			return false;
		}
		//add by chenkai, 对手机号码非11位的排除注册, 2014.06.04 begin
		if (mAccountObject.mAccountTel.length() < 11) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usrtel_invalid);
			return false;
		}
		//add by chenkai, 对手机号码非11位的排除注册, 2014.06.04 end
		return valiHomeInput();
	}
	
	private boolean valiHomeInput() {
		mHomeObject = mProCityDisEditPopView.getHomeObject();
		if(TextUtils.isEmpty(mHomeObject.mHomeProvince)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_pro);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomeCity)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_city);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomeDis)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_dis);
			return false;
		} else if (TextUtils.isEmpty(mHomeObject.mHomePlaceDetail)){
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_place_detail);
			return false;
		}
		return true;
	}
	
	private RegisterAsyncTask mRegisterAsyncTask;
	private void registerAsync() {
		AsyncTaskUtils.cancelTask(mRegisterAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mNextButton.setEnabled(false);
		mRegisterAsyncTask = new RegisterAsyncTask();
		mRegisterAsyncTask.execute();
	}

	private class RegisterAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			try {
				//modify by chenkai, 增强注册安全 begin
				is = NetworkUtils.openContectionLocked(ServiceObject.getSecurityToken(mAccountObject.mAccountName, SecurityUtils.MD5.md5(mAccountObject.mAccountName)), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					ServiceResultObject getTokenObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					NetworkUtils.closeInputStream(is);
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("cell", mAccountObject.mAccountTel)
					.put("UserName", mAccountObject.mAccountName)
					.put("pwd", mAccountObject.mAccountPwd)
					.put("admin_code", mProCityDisEditPopView.getDisID())
					.put("tag", mHomeEditText.getText().toString().trim())
					.put("detail", mHomeObject.mHomePlaceDetail)
					.put("iemi", DevicesUtils.getInstance().getImei())
					.put("imsi", DevicesUtils.getInstance().getIMSI());
					
					DebugUtils.logD(TAG, "RegisterAsyncTask doInBackground() jsonObject " + jsonObject.toString());

					
					String desJsonObject = SecurityUtils.DES.enCrypto(jsonObject.toString().getBytes(), ServiceObject.getSecurityToken(getTokenObject.mStrData));
					DebugUtils.logD(TAG, "RegisterAsyncTask doInBackground() desJsonObject " + desJsonObject);
					
					is = NetworkUtils.openContectionLocked(ServiceObject.getRegisterUrl("para", desJsonObject), MyApplication.getInstance().getSecurityKeyValuesObject());
					if (is != null) {
						serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch(UnknownHostException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
			} catch (IOException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
			} catch (JSONException e) {
	            e.printStackTrace();
	            serviceResultObject.mStatusMessage = e.getMessage();
            } finally {
				NetworkUtils.closeInputStream(is);
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			mNextButton.setEnabled(true);
			if (result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				//注册后，我们要做一次登陆
				MyAccountManager.getInstance().saveLastUsrTel(mAccountObject.mAccountTel);
				startActivityForResult(LoginOrUpdateAccountDialog.createLoginOrUpdate(mContext, true, mAccountObject.mAccountTel, mAccountObject.mAccountPwd), REQUEST_LOGIN);
			} else if (result.mStatusCode == 2) {
				//2当前手机的电话号码已经注册了，我们需要弹框用户提示找回密码
				new AlertDialog.Builder(mContext)
				.setMessage(result.mStatusMessage)
				.setPositiveButton(R.string.button_find_password, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Bundle bundle = new Bundle();
						bundle.putString(Intents.EXTRA_TEL, mAccountObject.mAccountTel);
						LoginActivity.startIntent(mContext, bundle);
						finish();
					}
				})
				.setNegativeButton(R.string.button_cancel, null)
				.show();
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			mNextButton.setEnabled(true);
		}
	}
	
}
