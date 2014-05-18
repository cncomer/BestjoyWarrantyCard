package com.bestjoy.app.warrantycard.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.WindowManager;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.AccountCard;
import com.bestjoy.app.warrantycard.account.CardCreateActivity;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.utils.VcfAsyncDownloadUtils.VcfAsyncDownloadHandler;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.shwy.bestjoy.contacts.AddrBookUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.NetworkUtils;

/**
 * 助手类，用于记录下载，将下载人记录存入对方名片下载数据库，将交换人的名号保存到对方收名片夹的名片接受记录表中.
 * 格式:http://www.mingdown.com/cell/jiaohuan.ashx?mm1=下载的名号&mm2=要交换的名号
 * @author chenkai
 *
 */
public class RecordDownloadUtils {
	private static final String TAG = "RecordDownloadUtils";
	/**http://www.mingdown.com/cell/adddownloadrecord.aspx?*/
	private static final String mDownloadRecordUriPrefix = "http://www.mingdown.com/cell/adddownloadrecord.aspx?";

	/**
	 * 该操作是一个阻塞联网操作，不能放在UI线程
	 * Http://www.mingdown.com/cell/ adddownloadrecord.aspx?MM=下载的名片名号 &&cell=下载人的默认本机号码
	 * @param downloadedMm 下载的名片名号
	 * @param tel           下载人的默认本机号码
	 * @return 
	 */
	public static boolean recordDownloadLocked(String downloadedMm, String tel) {
		DebugUtils.logD(TAG, "recordDownloadLocked downloadedMm=" + downloadedMm + " tel=" + tel);
		if (TextUtils.isEmpty(tel)) {
			DebugUtils.logD(TAG, "tel is empty, so we just return true");
			return true;
		}
		StringBuilder sb = new StringBuilder(mDownloadRecordUriPrefix);
		sb.append("MM=").append(downloadedMm)
		.append("&&cell=").append(tel);
		InputStream is = null;
		try {
			is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			DebugUtils.logD(TAG, "record download successfully.");
			return true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public static void recordDownloadInThread(final String downloadedMm) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				DebugUtils.logD(TAG, "recordDownloadInThread run()" );
				recordDownloadLocked(downloadedMm, MyAccountManager.getInstance().getDefaultPhoneNumber());
			}
			
		}).start();
	}
	/**
	 * 记录交换
	 * @param downloadedMm 要下载的名号
	 * @param exchangeMm   用于交换的名号
	 * @return 如果记录成功，返回true,否则返回false
	 */
	public static boolean recordExchange(String downloadedMm, String exchangeMm) {
		StringBuilder sb = new StringBuilder("http://www.mingdown.com/cell/jiaohuan.ashx?");
		sb.append("mm1=").append(downloadedMm).append("&mm2=").append(exchangeMm);
		try {
			InputStream is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
			if (is == null) {
				return false;
			}
			String resultStr = NetworkUtils.getContentFromInput(is);
			DebugUtils.logD(TAG, "service return " + resultStr);
			if ("ok".equals(resultStr)) {
				DebugUtils.logD(TAG, "finish recording exchange " + resultStr);
				return true;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	 private static void downloadDirectAndExchange(Context context, final String downloadedMm, final VcfAsyncDownloadHandler handler) {
		  final Cursor cardCursor = MyAccountManager.getInstance().getCardForAccount(null);
	    	if(cardCursor != null && cardCursor.getCount() != 0){
	    		//选择要使用的名片
	    		String[] items = new String[cardCursor.getCount()+1];
    			int cardIndex = 0;
    			//首先添加一个直接下载选项
    			items[cardIndex] = context.getString(R.string.msg_download_not_exchange);
    			cardIndex++;
    			while(cardCursor.moveToNext()) {
  	  	    		items[cardIndex] = AccountCard.getContactTagAndMm(cardCursor);
  	  	    	    cardIndex++;
    			}
    			AlertDialog dialog = new AlertDialog.Builder(context)
    			.setTitle(R.string.msg_download_and_exchange_title)
  	    		.setItems(items, new DialogInterface.OnClickListener() {
  					
  					@Override
  					public void onClick(DialogInterface dialog, int which) {
  						if (which == 0) {
  							moduleCardOp(null, downloadedMm, handler);
  						} else {
  							cardCursor.moveToPosition(which - 1);
	  						moduleCard(cardCursor, downloadedMm, handler);
  						}
	  				    if (cardCursor != null) cardCursor.close();
  					}
  				})
  				.setCancelable(true)
  				.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
					@Override
					public void onCancel(DialogInterface dialog) {
						if (cardCursor != null) cardCursor.close();
						
					}
			    }).create();
    			
    			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); 
    			dialog.show();	

	    	} else {//no card
	    	    if (cardCursor != null) cardCursor.close();
	    	    CardCreateActivity.showCreateCardConfirmDialog(context).show();
	    	}
	 }
	 
	 private static void downloadAndExchange(Context context, final String downloadedMm, final VcfAsyncDownloadHandler handler) {
		  final Cursor cardCursor = MyAccountManager.getInstance().getCardForAccount(null);
	    	if(cardCursor != null && cardCursor.getCount() != 0){
	    		//选择要使用的名片
	    		String[] items = new String[cardCursor.getCount()];
	    		if (items.length == 1) {
	    			cardCursor.moveToFirst();
	    			moduleCardOp(null, downloadedMm, handler);
	    		    if (cardCursor != null) cardCursor.close();
	    		} else {
	    			int cardIndex = 0;
	    			while(cardCursor.moveToNext()) {
	  	  	    		items[cardIndex] = AccountCard.getContactTagAndMm(cardCursor);
	  	  	    	    cardIndex++;
	    			}
	    			AlertDialog dialog = new AlertDialog.Builder(context)
	  	    		
	  	    		.setItems(items, new DialogInterface.OnClickListener() {
	  					
	  					@Override
	  					public void onClick(DialogInterface dialog, int which) {
	  						cardCursor.moveToPosition(which - 1);
	  						moduleCard(cardCursor, downloadedMm, handler);
		  				    if (cardCursor != null) cardCursor.close();
	  					}
	  				})
	  				.setCancelable(true)
	  				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					
						@Override
						public void onCancel(DialogInterface dialog) {
							if (cardCursor != null) cardCursor.close();
							
						}
				    }).create();
	    			
	    			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); 
	    			dialog.show();	
	  				
	    		}

	    	} else {//no card
	    	    if (cardCursor != null) cardCursor.close();
	    	    CardCreateActivity.showCreateCardConfirmDialog(context).show();
	    	}
	 }
	  /**
	   * 选择名片下载交换
	   * @param addressResult
	   * @deprecated 使用{@link #downloadAndExchange(Context, AddressBookParsedResult, VcfAsyncDownloadHandler, boolean)}代替
	   */
	  public static void downloadAndExchange(Context context, final AddressBookParsedResult addressResult, VcfAsyncDownloadHandler handler) {
		  downloadAndExchange(context, addressResult.getBid(), handler, true);
	  }
	  /**
	   * 显示直接下载或是交换现在对话框
	   * @param context
	   * @param addressResult
	   * @param handler
	   * @param showDirectDownload 是否显示直接下载选项
	   */
	  public static void downloadAndExchange(Context context, final AddressBookParsedResult addressResult, VcfAsyncDownloadHandler handler, boolean showDirectDownload) {
		  if (!MyAccountManager.getInstance().hasLoginned()) {
				MyApplication.getInstance().showMessage(R.string.msg_need_login_operation);
				return;
		  }
		  downloadAndExchange(context, addressResult.getBid(), handler, showDirectDownload);
	  }
	  /**
	   * 直接下载名片
	   * @param context
	   * @param downloadMm
	   * @param handler
	   */
	  public static void downloadOnly(Context context, String downloadMm, VcfAsyncDownloadHandler handler) {
		  moduleCardOp(null, downloadMm, handler);
	  }
	  
	  /**
	   * 
	   * @param context
	   * @param addressResult
	   * @param handler
	   * @param showDirectDownload 是否显示直接下载选项
	   */
	  public static void downloadAndExchange(Context context, String downloadMm, VcfAsyncDownloadHandler handler, boolean showDirectDownload) {
		  if (showDirectDownload) {
			  downloadDirectAndExchange(context, downloadMm, handler);
		  } else {
			  downloadAndExchange(context, downloadMm, handler);
		  }
	  }
	  
	  public static void moduleCard(Cursor cursor, String downloadedMm, VcfAsyncDownloadHandler handler) {
		    String bid = cursor.getString(cursor.getColumnIndex(HaierDBHelper.CONTACT_BID));
		    moduleCardOp(bid, downloadedMm, handler);
      }
	  /**
	   * 如果mMm为null,则表示我们只想要下载对方名片，否则是要交换并下载
	   * @param mMm
	   * @param downloadedMm
	   * @param handler
	   */
	  public static void moduleCardOp(String mMm, String downloadedMm, VcfAsyncDownloadHandler handler) {
		 if (mMm == null) {
			 downloadContactOp(handler, downloadedMm, true);
		 } else {
			 new RecordExchangeTask(mMm, downloadedMm, handler).execute();
		 }
//		  //默认总是成功的，也就是说只要不是要交换，我们总是走的直接下载流程  
//		  boolean success = true;
//		    if (mMm != null) {
//		    	//如果mMm不为空，先要交换，这时候根据交换的返回值来确定下一步操作
//		    	success = RecordDownloadUtils.recordDownload(downloadedMm, mMm);
//		    }
//		    if (success) {
//		    	if (handler != null) {
//		    		AddrBookUtils.getInstance().downloadContactLock(downloadedMm, handler);
//		    	} else {
//		    		//如果没有提供VcfAsyncDownloadHandler，那么我们使用偏好设置来决定是否下载后跳转显示联系人
//		    		AddrBookUtils.getInstance().downloadAndViewContactLock(downloadedMm);
//		    	}
//		    	
//		    } else {
//		    	BJfileApp.getInstance().showMessage(R.string.msg_download_and_exchange_failed);
//		    }
	 }
	  
	  private static void downloadContactOp(VcfAsyncDownloadHandler handler, String downloadedMm, boolean recordDownload) {
		  if (handler != null) {
	    		AddrBookUtils.getInstance().downloadContactLock(downloadedMm, handler, recordDownload);
	    	} else {
	    		//如果没有提供VcfAsyncDownloadHandler，那么我们使用偏好设置来决定是否下载后跳转显示联系人
	    		AddrBookUtils.getInstance().downloadAndViewContactLock(downloadedMm,recordDownload);
	    	}
	  }
	  
	  private static class RecordExchangeTask extends AsyncTask<Void, Void, Boolean> {

		  private String rMm, rDownloadedMm;
		  private VcfAsyncDownloadHandler rHandler;
		  private ProgressDialog rDialog;
		  public RecordExchangeTask(String mm, String downloadedMm, VcfAsyncDownloadHandler handler) {
			  rMm = mm;
			  rDownloadedMm = downloadedMm;
			  rHandler = handler;
		  }
		@Override
		protected Boolean doInBackground(Void... params) {
			return RecordDownloadUtils.recordExchange(rDownloadedMm, rMm);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			rDialog.dismiss();
			if (result) {
				MyApplication.getInstance().showShortMessage(R.string.msg_download_and_exchange_ok);
				//如果是交换下载，那么我们就不用再记录下载了，所以传递false
				downloadContactOp(rHandler, rDownloadedMm, rMm == null);
			} else {
				MyApplication.getInstance().showShortMessage(R.string.msg_download_and_exchange_failed);
			}
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			rDialog = new ProgressDialog(MyApplication.getInstance());
			rDialog.setCancelable(false);
			rDialog.setMessage(MyApplication.getInstance().getString(R.string.msg_download_and_exchange_wait));
			rDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			rDialog.show();
			
		}
		  
	  }
}
