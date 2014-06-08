package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.vudroid.pdfdroid.PdfViewerActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.utils.FilesLengthUtils;
import com.bestjoy.app.warrantycard.utils.TextViewUtils;
import com.bestjoy.app.warrantycard.view.CircleProgressView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.NotifyRegistrant;

public class CardViewFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TOKEN = CardViewFragment.class.getName();
	private static final String TAG = "CardViewFragment";
	//商品信息
	private TextView  mPinpaiInput, mModelInput, mBaoxiuInput, mYanbaoInput, mYanbaoText, mFapiaoDateInput;
	private ImageView mAvatorView, mUsageView, mFlagYanbao;
	private Button mBillView;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private HomeObject mHomeObject;
	
	private Handler mHandler;
	
	private Bundle mBundles;
	
	private CircleProgressView mCircleProgressView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mHandler = new Handler();
		BaoxiuCardObject.showBill(getActivity(), null);
		NotifyRegistrant.getInstance().register(mHandler);
		PhotoManagerUtilsV2.getInstance().requestToken(TOKEN);
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject();
		mHomeObject = HomeObject.getHomeObject();
		mBundles = getArguments();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.activity_card_view, container, false);

			//商品信息
			 mPinpaiInput = (TextView) view.findViewById(R.id.product_brand_input);
			 mModelInput = (TextView) view.findViewById(R.id.product_model_input);
			 mBaoxiuInput = (TextView) view.findViewById(R.id.baoxiu_input);
			 mYanbaoText = (TextView) view.findViewById(R.id.yanbao); 
			 mYanbaoInput = (TextView) view.findViewById(R.id.yanbao_input);
			 
			 TextViewUtils.setBoldText(mPinpaiInput);
			 TextViewUtils.setBoldText(mModelInput);
			 TextViewUtils.setBoldText(mBaoxiuInput);
			 TextViewUtils.setBoldText(mYanbaoInput);
			
			 mAvatorView = (ImageView) view.findViewById(R.id.avator);
			 mAvatorView.setOnClickListener(this);
			 
			 mFapiaoDateInput = (TextView) view.findViewById(R.id.fapiao_date_input);
			 //已延保标示
			 mFlagYanbao = (ImageView) view.findViewById(R.id.flag_yanbao); 
			 
			 mBillView = (Button) view.findViewById(R.id.button_bill);
			 mBillView.setOnClickListener(this);
			 
			 mUsageView = (ImageView) view.findViewById(R.id.button_usage);
			 mUsageView.setOnClickListener(this);
			 
	         mCircleProgressView = (CircleProgressView) view.findViewById(R.id.baoxiuday);
	         
			 populateView();
		return view;
	}

	private void populateView() {
		 if (!TextUtils.isEmpty(mBaoxiuCardObject.mKY)) {
			 mUsageView.setVisibility(View.VISIBLE);
			 PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, mAvatorView, mBaoxiuCardObject.mKY, null, PhotoManagerUtilsV2.TaskType.HOME_DEVICE_AVATOR);
		 } else {
			 //设置默认的ky图片
			 mAvatorView.setImageResource(R.drawable.ky_default);
			 mUsageView.setVisibility(View.GONE);
		 }
		 if (!mBaoxiuCardObject.hasBillAvator()) {
			 mBillView.setVisibility(View.INVISIBLE);
		 } else {
			 mBillView.setVisibility(View.VISIBLE);
		 }
		 
		 try {
			 mFapiaoDateInput.setText(BaoxiuCardObject.DATE_FORMAT_FAPIAO_TIME.format(BaoxiuCardObject.BUY_DATE_TIME_FORMAT.parse(mBaoxiuCardObject.mBuyDate)));
		 } catch (ParseException e) {
			 mFapiaoDateInput.setText(mBaoxiuCardObject.mBuyDate);
		 }
		 
		 mPinpaiInput.setText(BaoxiuCardObject.getTagName(mBaoxiuCardObject.mCardName, mBaoxiuCardObject.mPinPai, mBaoxiuCardObject.mLeiXin));
		 mModelInput.setText(mBaoxiuCardObject.mXingHao);
		 
		 if (!TextUtils.isEmpty(mBaoxiuCardObject.mWY)) {
			 //保修期，这里单位是年
			 float year = Float.valueOf(mBaoxiuCardObject.mWY);
			 if (year > 0 && (year - 0.5f) < 0.00001f) {
				 mBaoxiuInput.setText(R.string.unit_half_year);
			 } else {
				 mBaoxiuInput.setText(mBaoxiuCardObject.mWY + getString(R.string.unit_year));
			 }
		 }
		 int day = mBaoxiuCardObject.getBaoxiuValidity();
		 mCircleProgressView.setNumber(day > 0 ? day : 0);
		 //计算开始角度和结束角度
		 //外围粗圆环完整度=(今天-购买日期)/（保修年+延保年）X365
	     //如果为负值，则圆环是满圆
		 int startDegree = -90;
		 int validity = day;
		 int validityTotal = (int) ((Float.valueOf(mBaoxiuCardObject.mWY) + Float.valueOf(mBaoxiuCardObject.mYanBaoTime)) * 365 + 0.5f);
		 int endDegree = (int) (1.0 * validity / validityTotal * 360 + 0.5f);
		 mCircleProgressView.setOutterDegree(startDegree, endDegree, true);
		
		 mYanbaoInput.setVisibility(View.INVISIBLE);
		 mYanbaoText.setVisibility(View.INVISIBLE);
		 mFlagYanbao.setVisibility(View.INVISIBLE);
		 if (!TextUtils.isEmpty(mBaoxiuCardObject.mYanBaoTime)) {
			//延保期，单位是年
			 float year = Float.valueOf(mBaoxiuCardObject.mYanBaoTime);
			 if (year > 0) {
				 mYanbaoInput.setVisibility(View.VISIBLE);
				 mYanbaoText.setVisibility(View.VISIBLE);
				 mFlagYanbao.setVisibility(View.VISIBLE);
				 if ((year - 0.5f) < 0.00001f) {
					 mYanbaoInput.setText(R.string.unit_half_year);
				 } else {
					 mYanbaoInput.setText(mBaoxiuCardObject.mYanBaoTime + getString(R.string.unit_year));
				 }
			 }
			
			 
		 }
	}
	
	 @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		 inflater.inflate(R.menu.card_view_activity_menu, menu);
     }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem menuItem) {
		 switch(menuItem.getItemId()) {
		 case R.string.menu_edit:
			 BaoxiuCardObject.setBaoxiuCardObject(mBaoxiuCardObject);
			 NewCardActivity.startIntent(getActivity(), mBundles);
			 getActivity().finish();
			 break;
		 case R.string.menu_delete:
			 showDeleteDialog();
			 break;
		 }
		 return super.onOptionsItemSelected(menuItem);
	 }
	 
	 private void showDeleteDialog() {
		 new AlertDialog.Builder(getActivity())
		 	.setTitle(R.string.msg_tip_title)
	    	.setMessage(R.string.sure_delete)
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
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
	public void onResume() {
		getActivity().setTitle(R.string.title_baoxiucard_info);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id) {
		case R.id.button_bill:
			BaoxiuCardObject.showBill(getActivity(), mBaoxiuCardObject);
			break;
		case R.id.button_usage:
			//add by chenkai, for Usage, 2014.05.31 begin
			if (TextUtils.isEmpty(mBaoxiuCardObject.mKY)) {
				DebugUtils.logE(TAG, "ky is null, so ignore guild button");
				return;
			}
			if (!MyApplication.getInstance().hasExternalStorage()) {
				//没有SD,我们需要提示用户
				MyApplication.getInstance().showNoSDCardMountedMessage();
				return;
			}
			File pdfFile = MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY);
			if (pdfFile.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY)));
				intent.setClass(getActivity(), PdfViewerActivity.class);
				startActivity(intent);
			} else {
				//开始下载
				downloadProductUsagePdfAsync();
			}
			//add by chenkai, for Usage, 2014.05.31 end
			break;
		}
		
	}
	
	 @Override
	 public void onDestroy() {
		 super.onDestroy();
		 PhotoManagerUtilsV2.getInstance().releaseToken(TOKEN);
		 NotifyRegistrant.getInstance().unRegister(mHandler);
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
						int deleted = BaoxiuCardObject.deleteBaoxiuCardInDatabaseForAccount(getActivity().getContentResolver(), mBaoxiuCardObject.mUID, mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
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
				getActivity().finish();
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
		Intent intent = new Intent(context, CardViewFragment.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}

	@Override
	public void updateInfoInterface(InfoInterface infoInterface) {
		if (infoInterface instanceof BaoxiuCardObject) {
			if (infoInterface != null) {
				mBaoxiuCardObject = (BaoxiuCardObject)infoInterface;
			}
		} else if (infoInterface instanceof HomeObject) {
			if (infoInterface != null) {
				mHomeObject = (HomeObject)infoInterface;
			}
		} else if (infoInterface instanceof AccountObject) {
			if (infoInterface != null) {
				long uid = ((AccountObject)infoInterface).mAccountUid;
			}
		}		
	}

	@Override
	public void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject) {
	}
	
	//add by chenkai, for Usage, 2014.05.31, begin
		private DownloadProductUsagePdfTask mDownloadProductUsagePdfTask;
		private ProgressDialog mDownloadGoodsUsagePdfProgressDialog;
		private void downloadProductUsagePdfAsync() {
			showDialog(DIALOG_PROGRESS);
			AsyncTaskUtils.cancelTask(mDownloadProductUsagePdfTask);
			mDownloadProductUsagePdfTask = new DownloadProductUsagePdfTask();
			mDownloadProductUsagePdfTask.execute();
		}
		private class DownloadProductUsagePdfTask extends AsyncTask<Void, Integer, Boolean> {

			public long mPdfLength;
			public String mPdfLengthStr;
			String mErrorStr;
			@Override
			protected Boolean doInBackground(Void... arg0) {
				mDownloadGoodsUsagePdfProgressDialog = getProgressDialog();
				String url = ServiceObject.getProductUsageUrl(mBaoxiuCardObject.mKY);
				InputStream is = null;
				try {
					HttpResponse response = NetworkUtils.openContectionLockedV2(url, MyApplication.getInstance().getSecurityKeyValuesObject());
					int code = response.getStatusLine().getStatusCode();
					DebugUtils.logD(TAG, "DownloadProductUsagePdfTask return StatusCode is " + code);
					if (code == HttpStatus.SC_OK) {
						mPdfLength = response.getEntity().getContentLength();
						DebugUtils.logD(TAG, "DownloadProductUsagePdfTask return length of pdf file is " + mPdfLength);
						mPdfLengthStr = FilesLengthUtils.computeLengthToString(mPdfLength);
						is = response.getEntity().getContent();
						
						FileOutputStream fos = new FileOutputStream(MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY));
						byte[] buffer = new byte[4096];
						int read = is.read(buffer);
						long readAll  = read;
						int percent = 0;
						while(read != -1) {
							percent = Math.round((100.0f * readAll/mPdfLength)) ;
							publishProgress(percent);
							fos.write(buffer, 0, read);
							read = is.read(buffer);
							readAll += read;
						}
						fos.flush();
						fos.close();
						return true;
					} else if (code == HttpStatus.SC_NOT_FOUND) {
						mErrorStr = getString(R.string.msg_no_product_usage);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					mErrorStr = MyApplication.getInstance().getGernalNetworkError();
				} catch (IOException e) {
					e.printStackTrace();
					mErrorStr = MyApplication.getInstance().getGernalNetworkError();
				} finally {
					NetworkUtils.closeInputStream(is);
				}
				return false;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				if (mDownloadGoodsUsagePdfProgressDialog != null) {
					mDownloadGoodsUsagePdfProgressDialog.setMessage(getString(R.string.msg_product_usage_downloading_format, values[0], mPdfLengthStr));
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				dismissDialog(DIALOG_PROGRESS);
				mDownloadGoodsUsagePdfProgressDialog = null;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				dismissDialog(DIALOG_PROGRESS);
				mDownloadGoodsUsagePdfProgressDialog = null;
				if (result) {
					MyApplication.getInstance().showMessage(R.string.msg_product_usage_downloading_ok);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY)));
					intent.setClass(getActivity(), PdfViewerActivity.class);
					startActivity(intent);
				} else {
					if (TextUtils.isEmpty(mErrorStr)) {
						MyApplication.getInstance().showMessage(R.string.msg_product_usage_downloading_failed);
					} else {
						MyApplication.getInstance().showMessage(mErrorStr);
						
					}
				}
			}
		}
		//add by chenkai, for Usage, 2014.05.31, end

}
