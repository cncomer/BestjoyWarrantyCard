package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.shwy.bestjoy.utils.FeedbackHelper;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;
public class FeedbackActivity extends BaseNoActionBarActivity{

	private static final String TAG = "LoginOrUpdateAccountDialog";
	private AccountObject mAccountObject;
	private EditText mFeedbackInput;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		setShowHomeUp(true);
		mFeedbackInput = (EditText) findViewById(R.id.feedback);
	}
	
	 @Override
     public boolean onCreateTitleBarOptionsMenu(Menu menu) {
		 menu.add(0, R.string.button_send, 0, R.string.button_send);
		 return true;
     }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		 switch(item.getItemId()){
		 case R.string.button_send:
			 final String message = mFeedbackInput.getText().toString().trim();
			 if (!TextUtils.isEmpty(message)) {
				 FeedbackHelper.AbstractFeedbackObject feedbackObject = new FeedbackHelper.AbstractFeedbackObject() {

					@Override
					public void onFeedbackEnd(ServiceResultObject serviceResultObject) {
						dismissDialog(DIALOG_PROGRESS);
						MyApplication.getInstance().showMessage(serviceResultObject.mStatusMessage);
						if (serviceResultObject.isOpSuccessfully()) {
							finish();
						}
					}

					@Override
					public void onFeedbackStart() {
						showDialog(DIALOG_PROGRESS);
					}

					@Override
					public void onFeedbackonCancelled() {
						dismissDialog(DIALOG_PROGRESS);
					}

					@Override
					public InputStream openConnection()
							throws ClientProtocolException, JSONException, IOException {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("uid", String.valueOf(mAccountObject.mAccountUid))
						.put("sugg_content", message)
						.put("sugg_time", "")
						.put("sugg_id", "");
						return NetworkUtils.openContectionLocked(ServiceObject.getFeedbackUrl("para", jsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
					}
					 
				 };
				 FeedbackHelper.getInstance().feedbackAsync(feedbackObject);
			 }
			 break;
		 }
		return super.onOptionsItemSelected(item);
	 }

	public static void startActivity(Context context, long accountUid) {
		Intent intent = new Intent(context, FeedbackActivity.class);
		intent.putExtra(Intents.EXTRA_ID, accountUid);
		context.startActivity(intent);
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		long uid = intent.getLongExtra(Intents.EXTRA_ID, -1);
		
		if (uid == -1) {
			mAccountObject = new AccountObject();
		} else{
			mAccountObject = AccountObject.getHaierAccountFromDatabase(mContext, uid);
		}
		return mAccountObject != null;
	}
	
}
