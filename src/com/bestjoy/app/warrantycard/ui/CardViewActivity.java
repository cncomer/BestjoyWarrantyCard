package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.NotifyRegistrant;

public class CardViewActivity extends BaseActionbarActivity implements View.OnClickListener{
	private static final String TOKEN = CardViewActivity.class.getName();

	private CardViewFragment mContent;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private HomeObject mHomeObject;
	
	private Handler mHandler;

	/**表示是否是第一次进入*/
	private boolean mIsFirstOnResume = true;
	private Bundle mBundles;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing()) {
			return;
		}
		mHandler = new Handler();
		BaoxiuCardObject.showBill(mContext, null);
		NotifyRegistrant.getInstance().register(mHandler);
		PhotoManagerUtilsV2.getInstance().requestToken(TOKEN);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mContent = new CardViewFragment();
		// set the Above View
		setContentView(R.layout.card_content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
	}

	 @Override
     public boolean onCreateOptionsMenu(Menu menu) {
  	     getSupportMenuInflater().inflate(R.menu.card_view_activity_menu, menu);
         return true;
     }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem menuItem) {
		 switch(menuItem.getItemId()) {
		 case R.string.menu_edit:
			 //编辑卡片
			 BaoxiuCardObject.setBaoxiuCardObject(mBaoxiuCardObject);
			 NewCardActivity.startIntent(mContext, mBundles);
			 finish();
			 break;
		 case R.string.menu_delete:
			 showDeleteDialog();
			 break;
		 }
		 return super.onOptionsItemSelected(menuItem);
	 }
	 
	 private void showDeleteDialog() {
		 new AlertDialog.Builder(this)
		 	.setTitle(R.string.msg_tip_title)
	    	.setMessage(R.string.sure_delete)
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//删除卡片
					 if (ComConnectivityManager.getInstance().isConnected()) {
						 delteCardAsync();
					 } else {
						 showDialog(DIALOG_DATA_NOT_CONNECTED);
					 }
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id) {
		case R.id.button_bill:
			BaoxiuCardObject.showBill(mContext, mBaoxiuCardObject);
			break;
		case R.id.button_usage:
			break;
		case R.id.button_malfunction:
		case R.id.button_maintenance_point:
		case R.id.button_maintenance_componnet:
			if (true) {
				MyApplication.getInstance().showUnsupportMessage();
				return;
			}
			//目前只有海尔支持预约安装和预约维修，如果不是，我们需要提示用户
	    	if (ServiceObject.isHaierPinpai(mBaoxiuCardObject.mPinPai)) {
	    		BaoxiuCardObject.setBaoxiuCardObject(mBaoxiuCardObject);
    			HomeObject.setHomeObject(mHomeObject);
//    			if (id == R.id.button_onekey_install) {
//    				ModleSettings.doChoose(mContext, ModleSettings.createMyInstallDefaultBundle(mContext));
//    			} else if (id == R.id.button_onekey_repair) {
//    				ModleSettings.doChoose(mContext, ModleSettings.createMyRepairDefaultBundle(mContext));
//    			}
    			
    			finish();
	    	} else {
	    		new AlertDialog.Builder(mContext)
		    	.setMessage(R.string.must_haier_confirm_yuyue)
		    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(mBaoxiuCardObject.mBXPhone)) {
							Intents.callPhone(mContext, mBaoxiuCardObject.mBXPhone);
						} else {
							MyApplication.getInstance().showMessage(R.string.msg_no_bxphone);
						}
						
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
	    	}
			break;
		}
		
	}
	
	private boolean checkHaierPinpai() {
		boolean result = ServiceObject.isHaierPinpai(mBaoxiuCardObject.mPinPai);
		if (!result) {
			//不是海尔的品牌，我们给个提示
			MyApplication.getInstance().showMessage(R.string.msg_pinpai_haier_suppot_only);
		}
		return result;
	}

	private void showEmptyInputToast(int resId) {
			String msg = getResources().getString(resId);
			MyApplication.getInstance().showMessage(getResources().getString(R.string.input_type_please_input) + msg);
		}
	 
	 @Override
	 public void onDestroy() {
		 super.onDestroy();
		 PhotoManagerUtilsV2.getInstance().releaseToken(TOKEN);
		 NotifyRegistrant.getInstance().unRegister(mHandler);
	 }

	@Override
	protected boolean checkIntent(Intent intent) {
		//mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject();
		mBundles = intent.getExtras();
		//return mBaoxiuCardObject != null && mBaoxiuCardObject.mBID > 0 && mBundles != null;
		return mBundles != null;
	}
	
	private DeleteCardAsyncTask mDeleteCardAsyncTask;
	private void delteCardAsync() {
		AsyncTaskUtils.cancelTask(mDeleteCardAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mDeleteCardAsyncTask = new DeleteCardAsyncTask();
		mDeleteCardAsyncTask.execute();
	}
	
	private class DeleteCardAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			InputStream is = null;
			ServiceResultObject resultObject = new ServiceResultObject();
			try {
				is = NetworkUtils.openContectionLocked(ServiceObject.getBaoxiuCardDeleteUrl(String.valueOf(mBaoxiuCardObject.mBID), String.valueOf(mBaoxiuCardObject.mUID)), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					String content = NetworkUtils.getContentFromInput(is);
					resultObject = ServiceResultObject.parse(content);
					if (resultObject.isOpSuccessfully()) {
						//删除服务器成功后还要删除本地的数据
						int deleted = BaoxiuCardObject.deleteBaoxiuCardInDatabaseForAccount(mContext.getContentResolver(), mBaoxiuCardObject.mUID, mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
						if (deleted > 0) {
							//本地删除成功后我们还要刷新对应HomeObject对象的保修卡数据
							MyAccountManager.getInstance().updateHomeObject(mBaoxiuCardObject.mAID);
						}
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				resultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				resultObject.mStatusMessage = e.getMessage();
				e.printStackTrace();
			}
			return resultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			if (result.isOpSuccessfully()) {
				finish();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}
	
	/**
	 * 回到主界面
	 * @param context
	 */
	public static void startActivit(Context context, Bundle bundle) {
		Intent intent = new Intent(context, CardViewActivity.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}

}
