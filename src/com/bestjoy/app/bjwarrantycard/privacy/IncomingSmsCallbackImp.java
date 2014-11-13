package com.bestjoy.app.bjwarrantycard.privacy;

import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.ui.SettingsPreferenceActivity;
import com.shwy.bestjoy.bjnote.mylife.MyLifeObject;
import com.shwy.bestjoy.utils.Contents;
import com.shwy.bestjoy.utils.DebugUtils;

public class IncomingSmsCallbackImp extends MonitorCallbackImp{
	protected String TAG = "IncomingSmsCallbackImp";
	private static IncomingSmsCallbackImp mInstance = new IncomingSmsCallbackImp();
	private int[] mDownloadDialogResIds = new int[]{
			R.string.msg_privacy_sms_download_tip,
			R.string.msg_privacy_multiunknowncall_download_tip,
			R.string.msg_privacy_unknowncall_download_confirm,
			R.string.msg_privacy_unknowncall_download_cancel
	};
	
	private IncomingSmsCallbackImp(){}
	
	public static MonitorCallbackImp getInstance() {
		return mInstance;
	}
	
	@Override
	public void onPhoneRing(boolean outgoing, String number) {
		
	}

	@Override
	public void onPhoneIdle(boolean outgoing, String number) {
		
	}

	@Override
	public void onPhoneOffhook(boolean outgoing, String number) {
		
	}
	
	@Override
	public void onSmsReceive(SmsMessage[] smsMessages) {
		super.onSmsReceive(smsMessages);
		StringBuilder sb = new StringBuilder();
        for (SmsMessage message : smsMessages) {
              DebugUtils.logD(TAG, message.getOriginatingAddress() + " : " + 
                  message.getDisplayOriginatingAddress() + " : " + 
                  message.getDisplayMessageBody() + " : " + 
                  message.getTimestampMillis());
              sb.append(message.getDisplayMessageBody());
        }
        DebugUtils.logD(TAG,"SmsMessage is " + sb.toString());
        String mm = Contents.MingDang.isCloudUri(sb.toString());
        if (!TextUtils.isEmpty(mm)) {
        	DebugUtils.logD(TAG,"find mm " + mm);
        	doAsyncQuery(mm);
        }
	}

	@Override
	public boolean isEnabled(SharedPreferences prefers) {
		return prefers.getBoolean(SettingsPreferenceActivity.KEY_PRIVACY_SMS, false);
	}

	@Override
	protected int[] getDownloadDialogStringRes() {
		return mDownloadDialogResIds;
	}

	@Override
	protected MyLifeObject doQueryInBackground(String... query) {
		return null;
	}

	@Override
	protected void onQueryPostExecute(MyLifeObject result) {
		if (result != null) {
			displayDownloadDialog();
		}
		
	}

	@Override
	protected void onQueryPreExecute() {}
	@Override
	protected boolean isRecordDownload() {
		return false;
	}

}
