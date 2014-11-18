package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.client.ClientProtocolException;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.privacy.IncomingCallCallbackImp;
import com.bestjoy.app.bjwarrantycard.privacy.IncomingSmsCallbackImp;
import com.bestjoy.app.bjwarrantycard.privacy.MonitorSandbox;
import com.bestjoy.app.bjwarrantycard.privacy.OutgoingCallCallbackImp;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.update.AppAboutActivity;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.FilesUtils;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class SettingsPreferenceActivity extends SherlockPreferenceActivity implements OnPreferenceChangeListener, OnSharedPreferenceChangeListener{

	private static final String TAG = "SettingsPreferenceActivity";
	private static final String KEY_ACCOUNT_CATEGORY = "preferences_account_category";
	private static final String KEY_ACCOUNT_NAME = "preference_key_account_name";
	private static final String KEY_ACCOUNT_NICKNAME = "preference_key_account_nickname";
	private static final String KEY_ACCOUNT_PASSWORD = "preference_key_account_password";
	
	public static final int DIALOG_DATA_NOT_CONNECTED = 10006;//数据连接不可用
	public static final int DIALOG_MOBILE_TYPE_CONFIRM = 10007;//
	public static final int DIALOG_PROGRESS = 10008;
	private EditTextPreference mAccountName, mAccountNickName;
	private Preference mAccountPassword;
	
	private String mOldName, mOldPassword, mOldNickName;
	private Context mContext;
	
	/**程序第一次启动*/
	public static final String KEY_FIRST_STARTUP = "preferences_first_startup";
	
	public static final String KEY_DECODE_1D = "preferences_decode_1D";
	public static final String KEY_DECODE_QR = "preferences_decode_QR";
	public static final String KEY_DECODE_DATA_MATRIX = "preferences_decode_Data_Matrix";

	public static final String KEY_AUTO_REDIRECT = "preferences_auto_redirect";
	static final String KEY_VCF_SAVE = "preferences_vcf_save";
	public static final String KEY_CUSTOM_PRODUCT_SEARCH = "preferences_custom_product_search";

	static final String KEY_PLAY_BEEP = "preferences_play_beep";
	static final String KEY_VIBRATE = "preferences_vibrate";

	public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_out_results_shown";

	public static final String KEY_COLOR_INDEX = "preferences_color_index";
	public static final String KEY_FONT_SIZE = "preferences_font_size";

	public static final String KEY_LATEST_VERSION = "preferences_latest_version";
	public static final String KEY_LATEST_VERSION_CODE_NAME = "preferences_latest_version_code_name";
	public static final String KEY_LATEST_VERSION_INSTALL = "preferences_latest_version_install";
	public static final String KEY_LATEST_VERSION_LEVEL = "preferences_latest_version_level";
	
	// privacy module
	public static final String KEY_PRIVACY_CATEGORY = "preferences_privacy_category";
	public static final String KEY_PRIVACY_OUTGOING_CALL = "preferences_privacy_outgoing_call";
	public static final String KEY_PRIVACY_SMS = "preferences_privacy_mms";
	public static final String KEY_PRIVACY_INCOMING_CALL = "preferences_privacy_incoming_call";

	public static final String KEY_PRIVACY_AREA_CODE = "preferences_privacy_area_code";
	public static final String KEY_PRIVACY_PHONE_NUMBER = "preferences_privacy_phone_number";

	public static final String KEY_ACCOUNT_SETTING = "preference_account_setting";
	public static final String KEY_CARD_SETTING = "preference_card_setting";

	public static final String KEY_MOBILE_CONFIRM_IGNORE = "preferences_mobile_confirm_ignore";
	
	private CheckBoxPreference decode1D;
	private CheckBoxPreference decodeQR;
	private CheckBoxPreference decodeDataMatrix;
	
	
	public static final String KEY_ABOUT_APP = "preference_key_about_app";
	public static final String KEY_CLEAR_CACHE = "preference_key_clear_cache";
	private Preference mAboutApp, mClearCache;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preferences);
        mContext = this;
        PreferenceScreen preferences = getPreferenceScreen();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		
		if (!MyAccountManager.getInstance().hasLoginned()) {
			//如果没有登录，我们移除账户相关的设置项
			preferences.removePreference(preferences.findPreference(KEY_ACCOUNT_CATEGORY));
		} else {
			mAccountName = (EditTextPreference) preferences.findPreference(KEY_ACCOUNT_NAME);
			mAccountNickName = (EditTextPreference) preferences.findPreference(KEY_ACCOUNT_NICKNAME);
			mAccountPassword = (Preference) preferences.findPreference(KEY_ACCOUNT_PASSWORD);
			updateAccountName();
			updateAccountNickName();
			mAccountName.setOnPreferenceChangeListener(this);
			mAccountNickName.setOnPreferenceChangeListener(this);
		}
		
		preferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	    decode1D = (CheckBoxPreference) preferences.findPreference(KEY_DECODE_1D);
	    decodeQR = (CheckBoxPreference) preferences.findPreference(KEY_DECODE_QR);
	    decodeDataMatrix = (CheckBoxPreference) preferences.findPreference(KEY_DECODE_DATA_MATRIX);
	   
	    disableLastCheckedPref();
	    
	    mAboutApp = (Preference) preferences.findPreference(KEY_ABOUT_APP);
	    mClearCache = (Preference) preferences.findPreference(KEY_CLEAR_CACHE);
    }
    
    @Override
	public void onResume() {
		super.onResume();
		//重新获取一次账户密码，有可能之前被改变了
		if (MyAccountManager.getInstance().getAccountObject() != null) {
			mOldPassword = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
		}
		
		
	}
    
    private void updateAccountName() {
    	String name = MyAccountManager.getInstance().getAccountObject().mAccountName;
    	mOldName = name;
    	mAccountName.setText(name);
		mAccountName.setSummary(name);
    }
    
    private void updateAccountNickName() {
    	String name = MyAccountManager.getInstance().getAccountObject().mAccountNickName;
    	mOldNickName = name;
    	mAccountNickName.setText(name);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
     	   Intent upIntent = NavUtils.getParentActivityIntent(this);
     	   if (upIntent == null) {
     		   // If we has configurated parent Activity in AndroidManifest.xml, we just finish current Activity.
     		   finish();
     		   return true;
     	   }
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
    
    @Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	if (preference == mAccountPassword) {
    		ModifyPasswordActivity.startActivity(this, mOldPassword);
    		return true;
    	} else if (preference == mAboutApp) {
			AppAboutActivity.startActivity(mContext);
			return true;
		} else if (preference == mClearCache) {
			clearCacheAsync();
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mAccountName) {
			String newName = (String) newValue;
			if (!mOldName.equals(newName.trim())) {
				//用户名发生变化了，我们需要更新
				updateAccountNameAsync(newName.trim());
			}
			return false;
		} else if (preference == mAccountNickName) { 
			String newName = (String) newValue;
			if (!mOldNickName.equals(newName.trim())) {
				//用户名发生变化了，我们需要更新
				updateAccountNickNameAsync(newName.trim());
			}
			return false;
		}
		return false;
	}
	
	// Prevent the user from turning off both decode options
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    if(key.equals(KEY_DECODE_1D) || key.equals(KEY_DECODE_QR) || key.equals(KEY_DECODE_DATA_MATRIX)) {
	    	disableLastCheckedPref();
	    } else if (key.equals(KEY_PRIVACY_INCOMING_CALL)) {
	    	MonitorSandbox.getInstance().toggleListen(IncomingCallCallbackImp.getInstance());
	    } else if (key.equals(KEY_PRIVACY_SMS)) {
	    	MonitorSandbox.getInstance().toggleListen(IncomingSmsCallbackImp.getInstance());
	    } else if (key.equals(KEY_PRIVACY_OUTGOING_CALL)) {
	    	MonitorSandbox.getInstance().toggleListen(OutgoingCallCallbackImp.getInstance());
	    }
	    
	  }

	    private void disableLastCheckedPref() {
		    Collection<CheckBoxPreference> checked = new ArrayList<CheckBoxPreference>(3);
		    if (decode1D.isChecked()) {
		      checked.add(decode1D);
		    }
		    if (decodeQR.isChecked()) {
		      checked.add(decodeQR);
		    }
		    if (decodeDataMatrix.isChecked()) {
		      checked.add(decodeDataMatrix);
		    }
		    boolean disable = checked.size() < 2;
		    for (CheckBoxPreference pref : new CheckBoxPreference[] {decode1D, decodeQR, decodeDataMatrix}) {
		      pref.setEnabled(!(disable && checked.contains(pref)));
		    }
		  }
	
	  @Override
	   	public Dialog onCreateDialog(int id) {
	   		switch(id) {
	   			 //add by chenkai, 20131201, add network check
	   	      case DIALOG_DATA_NOT_CONNECTED:
	   	    	  return ComConnectivityManager.getInstance().onCreateNoNetworkDialog(this);
	   	      case DIALOG_PROGRESS:
	   	    	  ProgressDialog progressDialog = new ProgressDialog(this);
	   	    	  progressDialog.setMessage(getString(R.string.msg_progressdialog_wait));
	   	    	  progressDialog.setCancelable(false);
	   	    	  return progressDialog;
	   		}
	   		return super.onCreateDialog(id);
	   	}
	
	private UpdateAccountNameTask mUpdateAccountNameTask;
	private void updateAccountNameAsync(String name) {
		AsyncTaskUtils.cancelTask(mUpdateAccountNameTask);
		showDialog(DIALOG_PROGRESS);
		mUpdateAccountNameTask = new UpdateAccountNameTask(name);
		mUpdateAccountNameTask.execute();
	}
	
	/**
	 *    Url:http://115.29.231.29/Haier/UpdateUserName.ashx
			入参：
			UserName	y	要更新的名称
			key	y	Md5(cell+pwd)
			UID	y	用户ID

	 * @author chenkai
	 *
	 */
	private class UpdateAccountNameTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private String _name;
		public UpdateAccountNameTask(String name) {
			_name = name;
		}
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("UpdateUserName.ashx?");
			String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
			long uid = MyAccountManager.getInstance().getAccountObject().mAccountUid;
			sb.append("UserName=").append(URLEncoder.encode(_name))
			.append("&key=").append(SecurityUtils.MD5.md5(cell+pwd))
			.append("&UID=").append(uid);
			InputStream is = null;
			try {
				 is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			     if (is != null) {
			    	 serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
			    	 if (serviceResultObject.isOpSuccessfully()) {
			    		 //如果更新成功，我们需要同步更新本地数据
			    		 AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
			    		 accountObject.mAccountName = _name;
			    		 ContentValues values = new ContentValues();
			    		 values.put(HaierDBHelper.ACCOUNT_NAME, _name);
			    		 accountObject.updateAccount(mContext.getContentResolver(), values);
			    	 }
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
			if (result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(R.string.msg_op_successed);
				updateAccountName();
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
			dismissDialog(DIALOG_PROGRESS);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(R.string.msg_op_canceled);
		}
		
	}
	
	
	private UpdateAccountNickNameTask mUpdateAccountNickNameTask;
	private void updateAccountNickNameAsync(String name) {
		AsyncTaskUtils.cancelTask(mUpdateAccountNickNameTask);
		showDialog(DIALOG_PROGRESS);
		mUpdateAccountNickNameTask = new UpdateAccountNickNameTask(name);
		mUpdateAccountNickNameTask.execute();
	}
	
	private class UpdateAccountNickNameTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private String _name;
		public UpdateAccountNickNameTask(String name) {
			_name = name;
		}
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
			sb.append("20140625/updatenickname.ashx?");
			sb.append("nickname=").append(URLEncoder.encode(_name))
			.append("&uid=").append(MyAccountManager.getInstance().getAccountObject().mAccountUid);
			InputStream is = null;
			try {
				 is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			     if (is != null) {
			    	 serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
			    	 if (serviceResultObject.isOpSuccessfully()) {
			    		 //如果更新成功，我们需要同步更新本地数据
			    		 AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
			    		 accountObject.mAccountNickName = _name;
			    		 ContentValues values = new ContentValues();
			    		 values.put(HaierDBHelper.ACCOUNT_NICKNAME, _name);
			    		 accountObject.updateAccount(mContext.getContentResolver(), values);
			    	 }
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
			if (result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(R.string.msg_op_successed);
				updateAccountNickName();
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
			dismissDialog(DIALOG_PROGRESS);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(R.string.msg_op_canceled);
		}
		
	}
	
	private DeleteCacheTask mDeleteCacheTask;
	private void clearCacheAsync() {
		AsyncTaskUtils.cancelTask(mDeleteCacheTask);
		showDialog(DIALOG_PROGRESS);
		mDeleteCacheTask = new DeleteCacheTask();
		mDeleteCacheTask.execute();
	}
	private class DeleteCacheTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			//删除files/accounts目录
			FilesUtils.deleteFile("DeleteCacheTask ", MyApplication.getInstance().getAccountsRoot());
			//删除cache目录
			FilesUtils.deleteFile("DeleteCacheTask ", MyApplication.getInstance().getCacheDir());
			//删除SD卡cache目录
			FilesUtils.deleteFile("DeleteCacheTask ", MyApplication.getInstance().getExternalStorageCacheRoot());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(R.string.msg_op_canceled);
		}
		
	}
	
	
	public static void startActivity(Context context) {
    	Intent intent = new Intent(context, SettingsPreferenceActivity.class);
    	context.startActivity(intent);
    }
	
}
