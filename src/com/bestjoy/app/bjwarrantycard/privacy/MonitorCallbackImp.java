package com.bestjoy.app.bjwarrantycard.privacy;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsMessage;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.shwy.bestjoy.bjnote.mylife.MyLifeObject;
import com.shwy.bestjoy.contacts.AddrBookUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;

public abstract class MonitorCallbackImp implements IMonitorCallback{
	protected String TAG = "MonitorCallbackImp";
	private SharedPreferences mPrefers = MyApplication.getInstance().mPreferManager;
	protected boolean isEnabled = true;
	protected Context mContext;
	private WindowManager mWm;
	private TextView mFlowDialog;
	/**浮动提示是否已经显示了*/
	private boolean mFlowDialogIsDisplay = false;
	/**当来电还未处理时为true,处理后为false,当mNeedDisplayFlowDialog=true且mFlowDialogIsDisplay=false,才会显示提示。
	 * 主要是针对的一种特殊情况，如已经接听了来电，且挂了电话后监听器才查询到名档网数据，此时不用显示了。*/
	protected boolean mNeedDisplayFlowDialog = false;
	
	protected LinkedList<MyLifeObject> mPendingCloudContacts = new LinkedList<MyLifeObject>();
	protected int[] mDownloadDialogResIds;
	private static final int TITLE_INDEX = 0;
	private static final int TITLES_INDEX = 1;
	private static final int OK_BUTTON_INDEX = 2;
	private static final int CANCEL_BUTTON_INDEX = 3;
	
	protected abstract boolean isEnabled(SharedPreferences prefers);
	protected abstract int[] getDownloadDialogStringRes();
	/**在AsyncTask中做查询操作，子类需要实现这个方法并返回查询到的ContactInfo对象*/
	protected abstract MyLifeObject doQueryInBackground(String... query);
	/**在AsyncTask中查询结束后会调用该方法*/
	protected abstract void onQueryPostExecute(MyLifeObject result);
	/**在AsyncTask中查询开始前会调用该方法*/
	protected abstract void onQueryPreExecute();
	/**是否需要记录下载名片记录*/
	protected abstract boolean isRecordDownload();
	
//	private VcfAsyncDownloadHandler mVcfAsyncDownloadHandler;
//	private VcfAsyncDownloadTask mVcfAsyncDownloadTask;
	
	private AlertDialog mDownloadDialog;
	
	private ProgressDialog mSaveloadDialog;
	
	
    public void setContext(Context context) {
    	mContext = context;
    	mWm = (WindowManager)mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE); 
//    	mVcfAsyncDownloadHandler = new VcfAsyncDownloadHandler() {
//    		
//
//			@Override
//			public void onDownloadFinished(
//					AddressBookParsedResult addressBookParsedResult,
//					String outMsg) {
//				super.onDownloadFinished(addressBookParsedResult, outMsg);
//				if (addressBookParsedResult == null) {
//					DebugUtils.logD(TAG, "onDownloadFinished " + outMsg);
//					mPendingCloudContacts.remove(0);
//					displayDownloadDialog();
//				}
//			}
//			
//			@Override
//			public boolean onDownloadFinishedInterrupted() {
//				return false;
//			}
//
//			public boolean onSaveFinished(Uri contactUri, String mm) {
//				DebugUtils.logD(TAG, "onPostExecute mPendingCloudContacts.remove(0)");
//				mPendingCloudContacts.remove(0);
//				if (mPendingCloudContacts.size() == 0) {
//					//如果已经没有更多的可下载联系人了，我们跳转到显示界面
//					return false;
//				}
//				displayDownloadDialog();
//				return true;
//			}
//    	
//    	};
    	
    	if (mSaveloadDialog == null) {
    		mSaveloadDialog = new ProgressDialog(context);
    		mSaveloadDialog.setMessage(mContext.getString(R.string.msg_progressdialog_wait));
    		mSaveloadDialog.setCancelable(true);
    		mSaveloadDialog.setIndeterminate(true);
    		mSaveloadDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
    }
	
	@Override
	public void onSmsReceive(SmsMessage[] smsMessages) {
		if (!isEnabled) return;
	}

	@Override
	public void onPhoneRing(boolean outgoing, String number) {
		if (!isEnabled) return;
		mNeedDisplayFlowDialog = true;
	}

	@Override
	public void onPhoneIdle(boolean outgoing, String number) {
		if (!isEnabled) return;
		mNeedDisplayFlowDialog = false;
	}

	@Override
	public void onPhoneOffhook(boolean outgoing, String number) {
		if (!isEnabled) return;
		mNeedDisplayFlowDialog = false;
	}

	@Override
	public void onPhoneOutgoing(String number) {
		if (!isEnabled) return;
		mNeedDisplayFlowDialog = true;
	}

	@Override
	public void register(List<IMonitorCallback> callbacks) {
		if (callbacks != null && isEnabled(mPrefers)) callbacks.add(this);
	}

	@Override
	public void unregister(List<IMonitorCallback> callbacks) {
		if (callbacks != null)callbacks.remove(this);
	}
	
	@Override
	public void toggle(List<IMonitorCallback> callbacks) {
		isEnabled = isEnabled(mPrefers);
		if (isEnabled) {
			callbacks.add(this);
		} else {
			callbacks.remove(this);
		}
	}
	
	protected synchronized void displayCallFlowDialog(MyLifeObject contactInfo) {
		if (!mNeedDisplayFlowDialog) {
			DebugUtils.logD(TAG, "we need not to displayCallFlowDialog");
			return;
		}
		if (!mFlowDialogIsDisplay) {
			 WindowManager.LayoutParams params = new WindowManager.LayoutParams();    
	         params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;    
	         params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | 
	        		 WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;   
	         params.format = PixelFormat.RGBA_8888;
	         params.width = WindowManager.LayoutParams.WRAP_CONTENT;    
	         params.height = WindowManager.LayoutParams.WRAP_CONTENT;
	         params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
	         params.y = (int) (60 * MyApplication.getInstance().mDisplayMetrics.density);
	         mFlowDialog = new TextView(mContext);
	         mFlowDialog.setText(mContext.getString(R.string.call_contact_info, contactInfo.toFriendlyString()));
	         mWm.addView(mFlowDialog, params);
	         mFlowDialogIsDisplay = true;
		} else {
			mFlowDialog.setText(mContext.getString(R.string.call_contact_info, contactInfo.toFriendlyString()));
		}
	}
	
	protected synchronized void hideCallFlowDialog() {
		if (mFlowDialogIsDisplay) {
			if (mFlowDialog != null) mWm.removeView(mFlowDialog);
			mFlowDialogIsDisplay = false;
		}
	}
	
	protected void displayDownloadDialog() {
		if (mDownloadDialog != null) {
			mDownloadDialog.dismiss();
		}
		int count = mPendingCloudContacts.size();
		if (count > 0) {
			int[] resIds = getDownloadDialogStringRes();
			AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getInstance());
			if (count == 1) {
				builder.setTitle(MyAccountManager.getInstance().hasLoginned()?resIds[TITLE_INDEX]:R.string.msg_privacy_unknowncall_download_contact_tip);
				builder.setMessage(mPendingCloudContacts.get(0).toFriendlyString());
				builder.setPositiveButton(resIds[OK_BUTTON_INDEX], new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doDownloadContact(0);
					}
				});
			} else {
				String[] items = new String[count];
				int index = 0;
				for(MyLifeObject contactInfo : mPendingCloudContacts) {
					items[index] = contactInfo.toFriendlyString();
					index++;
				}
				builder.setTitle(MyAccountManager.getInstance().hasLoginned()?resIds[TITLES_INDEX]:R.string.msg_privacy_unknowncall_download_contact_tip);
				builder.setItems(items, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						doDownloadContact(which);
					}
				});
			}
			builder.setNegativeButton(resIds[CANCEL_BUTTON_INDEX], new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mPendingCloudContacts.clear();
					DebugUtils.logD(TAG, "click cancelBtn mPendingCloudContacts.clear()");
				}
				
			});
			mDownloadDialog = builder.create();
			mDownloadDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); 
			mDownloadDialog.show();
		}
	}
	
	private void doDownloadContact(int which) {
		mSaveloadDialog.show();
		new SaveAsyncTask().execute(which);
	}
	
	/**
	 * 查询入口，当需要发起名档网查询时需要调用该方法，query可以是手机号码也可以是MM号码，这里只是一个包装，
	 * 实际上，会触发{@link #doQueryInBackground(String...)}等系列调用，由子类实现。
	 * @param query
	 */
	protected void doAsyncQuery(String... query) {
		QueryAsyncTask task = new QueryAsyncTask();
	    task.execute(query);
	}
	
	private class QueryAsyncTask extends AsyncTask<String, Void, MyLifeObject> {

		@Override
		protected MyLifeObject doInBackground(String... query) {
			return doQueryInBackground(query[0].replaceAll("[-() ]", ""));
		}

		@Override
		protected void onPostExecute(MyLifeObject result) {
			super.onPostExecute(result);
			if (result != null) {
				boolean ignore = false;
				if (result.mId < 0 && !mPendingCloudContacts.contains(result)){
					for(MyLifeObject hasExistedObject : mPendingCloudContacts) {
						if (hasExistedObject.mId == result.mId) {
							ignore = true;
							break;
						}
					}
		        	 if (!ignore) {
		        		 mPendingCloudContacts.add(0, result);
		        		 DebugUtils.logD(TAG, "mPendingCloudContacts add " + result);
		        	 } else {
		        		 DebugUtils.logD(TAG, "mPendingCloudContacts ignore add existed " + result);
		        	 }
		         }
				onQueryPostExecute(result);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onQueryPreExecute();
		}
	}
	
	
	private class SaveAsyncTask extends AsyncTask<Integer, Void, ServiceResultObject> {

		private Uri _contactUrl;
		private int _which = 0;
		@Override
		protected ServiceResultObject doInBackground(Integer... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			_which = params[0];
			MyLifeObject myLifeObject = mPendingCloudContacts.get(_which);
			if (MyAccountManager.getInstance().hasLoginned()) {
				//我们需要关联用户和店铺，以便将店铺添加进会员卡
				InputStream is = null;
				try {
					JSONObject queryObject = new JSONObject();
					queryObject.put("shopid", String.valueOf(myLifeObject.mShopID));
					queryObject.put("phone", myLifeObject.mComCellRawStr);
					queryObject.put("uid", MyAccountManager.getInstance().getCurrentAccountUid());
					is = NetworkUtils.openContectionLocked(ServiceObject.relatedShopAndUserUrl("para", queryObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
					
					serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					if (serviceResultObject.isOpSuccessfully()) {
						if (!myLifeObject.saveInDatebase(MyApplication.getInstance().getContentResolver(), null)) {
							serviceResultObject.mStatusCode = 0;
							serviceResultObject.mStatusMessage = MyApplication.getInstance().getString(R.string.save_fail);
						}
						serviceResultObject.mStatusMessage = MyApplication.getInstance().getString(R.string.save_success);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					serviceResultObject.mStatusMessage = e.getMessage();
				} catch (IOException e) {
					e.printStackTrace();
					serviceResultObject.mStatusMessage = e.getMessage();
				} catch (JSONException e) {
					e.printStackTrace();
					serviceResultObject.mStatusMessage = e.getMessage();
				} finally {
					NetworkUtils.closeInputStream(is);
				}
			} else {
				myLifeObject.saveInDatebase(MyApplication.getInstance().getContentResolver(), null);
				_contactUrl = myLifeObject.saveToContact();
				if (_contactUrl == null) {
					serviceResultObject.mStatusCode = 0;
					serviceResultObject.mStatusMessage = MyApplication.getInstance().getString(R.string.save_fail);
				} else {
					serviceResultObject.mStatusCode = 1;
					serviceResultObject.mStatusMessage = MyApplication.getInstance().getString(R.string.save_success);
				}
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			mSaveloadDialog.dismiss();
			mPendingCloudContacts.remove(_which);
			displayDownloadDialog();
			if (result.isOpSuccessfully()) {
				if (_contactUrl != null) {
					AddrBookUtils.getInstance().viewContact(_contactUrl);
				} else {
					MyApplication.getInstance().showMessage(R.string.save_success);
				}
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mSaveloadDialog.dismiss();
		}

	}
}
