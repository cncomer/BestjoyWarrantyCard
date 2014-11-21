package com.bestjoy.app.bjwarrantycard.privacy;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.SettingsPreferenceActivity;
import com.shwy.bestjoy.bjnote.mylife.MyLifeObject;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.Contents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class IncomingCallCallbackImp extends MonitorCallbackImp{
	protected String TAG = "IncomingCallCallbackImp";
	private int[] mDownloadDialogResIds = new int[]{
			R.string.msg_privacy_unknowncall_download_tip,
			R.string.msg_privacy_multiunknowncall_download_tip,
			R.string.msg_privacy_unknowncall_download_confirm,
			R.string.msg_privacy_unknowncall_download_cancel
	};
	private static IncomingCallCallbackImp mInstance = new IncomingCallCallbackImp();
	private IncomingCallCallbackImp(){}
	
	public static MonitorCallbackImp getInstance() {
		return mInstance;
	}
	
	@Override
	public void onPhoneRing(boolean outgoing, String number) {
		super.onPhoneRing(outgoing, number);
//		String phoneNum = Contents.MingDang.buildValidPhoneNumber(number, MyApplication.getInstance().getPreferAreaCode());
		if (!TextUtils.isEmpty(number)) {
			doAsyncQuery(number);
		}
	}

	@Override
	public void onPhoneIdle(boolean outgoing, String number) {
		super.onPhoneIdle(outgoing, number);
		if (!outgoing) {
			hideCallFlowDialog();
			displayDownloadDialog();
		}
    	
	}

	@Override
	public void onPhoneOffhook(boolean outgoing, String number) {
		super.onPhoneOffhook(outgoing, number);
		hideCallFlowDialog();
	}

	@Override
	public boolean isEnabled(SharedPreferences prefers) {
		return prefers.getBoolean(SettingsPreferenceActivity.KEY_PRIVACY_INCOMING_CALL, true);
	}

	@Override
	protected int[] getDownloadDialogStringRes() {
		return mDownloadDialogResIds;
	}

	//查询位置号码开始
	@Override
	protected MyLifeObject doQueryInBackground(String... query) {
		InputStream is = null;
		//首先查询本地数据库，是否存在该号码对应联系人
		MyLifeObject contactInfo = MyLifeObject.getFromDatabase(query[0]);
		//如果不存在，我们查询名档网数据
		if (contactInfo == null) {
			try {
				contactInfo = null;
				JSONObject queryObject = new JSONObject();
				queryObject.put("admin_code", ComPreferencesManager.getInstance().mPreferManager.getString("admincode", ""));
				queryObject.put("cell", query[0]);
				queryObject.put("uid", MyAccountManager.getInstance().hasLoginned()?MyAccountManager.getInstance().getCurrentAccountUid():"");
				is = NetworkUtils.openContectionLocked(ServiceObject.getShopCellJianKongUrl("para", queryObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				
				ServiceResultObject serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					return MyLifeObject.parse(serviceResultObject.mJsonData);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
		}
		return contactInfo;
	}

	//
	@Override
	protected void onQueryPostExecute(MyLifeObject result) {
		if (result != null) {
			displayCallFlowDialog(result);
		}
	}

	//查询开始前，如果需要，我们隐藏之前的来电显示pop框
	@Override
	protected void onQueryPreExecute() {
		hideCallFlowDialog();
	}
	//查询位置号码结束

	@Override
	protected boolean isRecordDownload() {
		return false;
	}
}
