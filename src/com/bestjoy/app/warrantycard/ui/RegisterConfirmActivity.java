package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.HaierProCityDisEditPopView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;


public class RegisterConfirmActivity extends BaseActionbarActivity implements View.OnClickListener{
	private static final String TAG = "RegisterActivity";

	private HaierProCityDisEditPopView mProCityDisEditPopView;
	
	private EditText mUsrNameEditText;
	private EditText usrPwdEditText;
	private EditText usrPwdConfirmEditText;
	private EditText usrHomeNameEditText;
	private String usrPwdConfirm;
	
	private AccountObject mAccountObject;
	
	private HomeObject mHomeObject;
	private Button mConfrimReg;
	private Bundle mBundles;
	
	private static final int REQUEST_LOGIN = 1;

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundles = getIntent().getExtras();
		if (mBundles == null) {
			DebugUtils.logD(TAG, "finish due to checkIntent mBundles is null");
			return false;
		}
		String tel = mBundles.getString(Intents.EXTRA_TEL);
		if (TextUtils.isEmpty(tel)) {
			DebugUtils.logD(TAG, "finish due to checkIntent tel is null");
			return false;
		}
		mAccountObject = new AccountObject();
		mAccountObject.mAccountTel = tel;
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugUtils.logD(TAG, "onCreate()");
		if (isFinishing()) {
			return;
		}
		setContentView(R.layout.activity_register);
		this.initViews();
	}

	private void initViews() {
		 mProCityDisEditPopView = new HaierProCityDisEditPopView(this); 
		 usrHomeNameEditText = (EditText) findViewById(R.id.tag);
		mUsrNameEditText = (EditText) findViewById(R.id.usr_name);
		usrPwdEditText = (EditText) findViewById(R.id.usr_pwd);
		usrPwdConfirmEditText = (EditText) findViewById(R.id.usr_repwd);
		
		mConfrimReg = (Button) findViewById(R.id.button_save_reg);
		mConfrimReg.setOnClickListener(this);
	}
	
	public static void startIntent(Context context, Bundle bundle) {
		Intent intent = new Intent(context, RegisterConfirmActivity.class);
		if (bundle == null) {
			bundle = new Bundle();
		}
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	private RegisterAsyncTask mRegisterAsyncTask;
	private void registerAsync(String... param) {
		AsyncTaskUtils.cancelTask(mRegisterAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mConfrimReg.setEnabled(false);
		mRegisterAsyncTask = new RegisterAsyncTask();
		mRegisterAsyncTask.execute(param);
	}

	private class RegisterAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			ServiceResultObject serviceResultObject  = new ServiceResultObject();
			InputStream is = null;
			final int LENGTH = 3;
			String[] urls = new String[LENGTH];
			String[] paths = new String[LENGTH];
			urls[0] = ServiceObject.SERVICE_URL + "Register.ashx?cell=";
			paths[0] = mAccountObject.mAccountTel;
			urls[1] = "&UserName=";
			paths[1] = mAccountObject.mAccountName;
			urls[2] = "&pwd=";
			paths[2] = mAccountObject.mAccountPwd;
			DebugUtils.logD(TAG, "urls = " + Arrays.toString(urls));
			DebugUtils.logD(TAG, "paths = " + Arrays.toString(paths));
			try {
				is = NetworkUtils.openContectionLocked(urls, paths, MyApplication.getInstance().getSecurityKeyValuesObject());
				try {
					serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					if (serviceResultObject.isOpSuccessfully()) {
						String data = serviceResultObject.mJsonData.getString("Data");
						DebugUtils.logD(TAG, "Data = " + data);
						mAccountObject.mAccountUid = Long.parseLong(data.substring(data.indexOf(":")+1));
						DebugUtils.logD(TAG, "Uid = " + mAccountObject.mAccountUid);
						
//						mAccountObject.mAccountHomes.add(mHomeObject);
//						boolean saveResult = HaierAccountManager.getInstance().saveAccountObject(getContentResolver(), mAccountObject);
//					    if (!saveResult) {
//					    	//注册成功，但无法创建账户，请尝试重新登陆
//					    	mError = mContext.getString(R.string.msg_register_save_fail);
//					    }
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
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
			mConfrimReg.setEnabled(true);
			if (!result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			} else {
				//注册后，我们要做一次登陆
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				MyAccountManager.getInstance().saveLastUsrTel(mAccountObject.mAccountTel);
				startActivityForResult(LoginOrUpdateAccountDialog.createLoginOrUpdate(mContext, true, mAccountObject.mAccountTel, mAccountObject.mAccountPwd), REQUEST_LOGIN);
			}
			dismissDialog(DIALOG_PROGRESS);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			mConfrimReg.setEnabled(true);
		}
	}
	
	 public boolean onCreateOptionsMenu(Menu menu) {
		 return false;
	 }
	 
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button_save_reg:
				DebugUtils.logD(TAG, "button_save onClick");
				mAccountObject.mAccountName = mUsrNameEditText.getText().toString().trim();
				mAccountObject.mAccountName = mUsrNameEditText.getText().toString().trim();
				mAccountObject.mAccountPwd = usrPwdEditText.getText().toString().trim();
				mAccountObject.mAccountPwd = usrPwdEditText.getText().toString().trim();
				usrPwdConfirm = usrPwdConfirmEditText.getText().toString().trim();

				mHomeObject = mProCityDisEditPopView.getHomeObject();
				if(valiInput()) {
					registerAsync();
				}
				break;
		}
	}

	private boolean valiInput() {
		if (mAccountObject != null && TextUtils.isEmpty(mAccountObject.mAccountName)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_name);
			return false;
		}
		if (TextUtils.isEmpty(mAccountObject.mAccountPwd)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_pwd);
			return false;
		}
		if (TextUtils.isEmpty(mHomeObject.mHomeProvince)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_pro);
			return false;
		}
		if (TextUtils.isEmpty(mHomeObject.mHomeCity)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_city);
			return false;
		}
		if (TextUtils.isEmpty(mHomeObject.mHomeDis)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_dis);
			return false;
		}
		if (TextUtils.isEmpty(mHomeObject.mHomePlaceDetail)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_usr_place_detail);
			return false;
		}
		if (!usrPwdConfirm.equals(mAccountObject.mAccountPwd)) {
			MyApplication.getInstance().showMessage(R.string.msg_input_pwd_not_match_tips);
			return false;
		}
		if (mAccountObject.mAccountPwd.length() < 6) {
			MyApplication.getInstance().showMessage(R.string.msg_usr_pwd_too_short_tips);
			return false;
		}
		return true;
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
						MainActivity20141010.startActivityForTop(mContext);
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
