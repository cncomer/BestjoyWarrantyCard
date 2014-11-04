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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.bestjoy.app.warrantycard.view.BaoxiuCardViewSalemanInfoView;
import com.bestjoy.app.warrantycard.view.CircleProgressView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.DialogUtils;
import com.shwy.bestjoy.utils.FilesUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.NotifyRegistrant;

public class CardViewFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TOKEN = CardViewFragment.class.getName();
	private static final String TAG = "CardViewFragment";
	//商品信息
	private TextView  mPinpaiInput, mModelInput, mBaoxiuTelInput, mYanbaoTelInput, mYanbaoCompanyInput, mYanbaoTimeInput, mFapiaoDateInput, mLeixingInput;
	private ImageView mAvatorView;
	private Button mBillView, mUsageView, mBuyYanbaoView;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private HomeObject mHomeObject;
	
	private Handler mHandler;
	
	private Bundle mBundles;
	
	private CircleProgressView mCircleProgressView;
	
	private ImageView mFapiaoDownloadView;
	
	/**是否显示销售人员信息*/
	private static final boolean SHOW_SALES_INFO = true;
	private BaoxiuCardViewSalemanInfoView mMMOne, mMMTwo;
	private static final int WHAT_SHOW_FAPIAO_WAIT = 12;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mHandler = new Handler() {

			@Override
            public void handleMessage(Message msg) {
	            switch(msg.what) {
	            case NotifyRegistrant.EVENT_NOTIFY_MESSAGE_RECEIVED:
	            	Bundle bundle = (Bundle) msg.obj;
	            	boolean status = bundle.getBoolean(PhotoManagerUtilsV2.EXTRA_DOWNLOAD_STATUS);
	            	String photoid = bundle.getString(Intents.EXTRA_PHOTOID);
	            	File fapiao = MyApplication.getInstance().getProductFaPiaoFile(mBaoxiuCardObject.getFapiaoPhotoId());
	            	if (photoid.equals(mBaoxiuCardObject.getFapiaoServicePath())) {
	            		dismissDialog(DIALOG_PROGRESS);
	            		if (status) {
	            			//下载完成
	            			DebugUtils.logD(TAG, "FapiaoTask finished for " + mBaoxiuCardObject.getFapiaoPhotoId());
		            		
		        			if (fapiao.exists()) {
		        				mBillView.setEnabled(false);
		        				mHandler.sendEmptyMessageDelayed(WHAT_SHOW_FAPIAO_WAIT, 6000);
		        				DebugUtils.logD(TAG, "FapiaoTask downloaded " + fapiao.getAbsolutePath());
		        				BaoxiuCardObject.showBill(getActivity(), mBaoxiuCardObject);
		        			}
	            		} else {
	            			MyApplication.getInstance().showMessage(bundle.getString(PhotoManagerUtilsV2.EXTRA_DOWNLOAD_STATUS_MESSAGE));
	            		}
	            		NotifyRegistrant.getInstance().unRegister(mHandler);
	            	}
	            	return;
	            case WHAT_SHOW_FAPIAO_WAIT:
	            	mBillView.setEnabled(true);
	            	break;
	            }
	            super.handleMessage(msg);
            }
			
		};
		BaoxiuCardObject.showBill(getActivity(), null);
		PhotoManagerUtilsV2.getInstance().requestToken(TOKEN);
		if (savedInstanceState != null) {
			mBundles = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundles);
		} else {
			mBundles = getArguments();
		}
		
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundles);
		mHomeObject = HomeObject.getHomeObject(mBundles);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.activity_card_view, container, false);

			//商品信息
			 mPinpaiInput = (TextView) view.findViewById(R.id.product_brand_input);
			 mModelInput = (TextView) view.findViewById(R.id.product_model_input);
			 mLeixingInput = (TextView) view.findViewById(R.id.product_leixing_input);
			 
			 mBaoxiuTelInput = (TextView) view.findViewById(R.id.baoxiu_tel_input);
			 mYanbaoCompanyInput = (TextView) view.findViewById(R.id.product_buy_delay_componey); 
			 mYanbaoTelInput = (TextView) view.findViewById(R.id.product_buy_delay_componey_tel);
			 mYanbaoTimeInput = (TextView) view.findViewById(R.id.product_buy_delay_time);
			 mBuyYanbaoView = (Button) view.findViewById(R.id.button_buy_yanbao);
			 mBuyYanbaoView.setOnClickListener(this);
			 
//			 TextViewUtils.setBoldText(mPinpaiInput);
//			 TextViewUtils.setBoldText(mModelInput);
//			 TextViewUtils.setBoldText(mBaoxiuInput);
//			 TextViewUtils.setBoldText(mYanbaoInput);
			
			 mAvatorView = (ImageView) view.findViewById(R.id.avator);
			 mAvatorView.setOnClickListener(this);
			 
			 mFapiaoDateInput = (TextView) view.findViewById(R.id.fapiao_date_input);
			 
			 mBillView = (Button) view.findViewById(R.id.button_bill);
			 mBillView.setOnClickListener(this);
			 
			 mUsageView = (Button) view.findViewById(R.id.button_usage);
			 mUsageView.setOnClickListener(this);
			 
	         mCircleProgressView = (CircleProgressView) view.findViewById(R.id.baoxiuday);
	         populateView();
	         
	       //根据SHOW_SALES_INFO的值来决定是否要显示销售员信息布局
			 View salesLayout = view.findViewById(R.id.sales_layout);
			 if (salesLayout != null) {
				 salesLayout.setVisibility(SHOW_SALES_INFO?View.VISIBLE:View.GONE);
			 }
			 if (SHOW_SALES_INFO) {
				 mMMOne = (BaoxiuCardViewSalemanInfoView) view.findViewById(R.id.mmone);
				 mMMOne.setParantFragment(this);
				//销售员
				 mMMOne.setTitle(R.string.salesman_title);
				 mMMOne.setMmType(BaoxiuCardViewSalemanInfoView.TYPE_MM_ONE);
				 
				 //服务员
				 mMMTwo = (BaoxiuCardViewSalemanInfoView) view.findViewById(R.id.mmtwo);
				 mMMTwo.setParantFragment(this);
				 mMMTwo.setTitle(R.string.serverman_title);
				 mMMTwo.setMmType(BaoxiuCardViewSalemanInfoView.TYPE_MM_TWO);
				 
				 if (!TextUtils.isEmpty(mBaoxiuCardObject.mMMOne)) {
					 mMMOne.setSalesPersonInfo(mBaoxiuCardObject, TOKEN);
				 }
				 
				 if (!TextUtils.isEmpty(mBaoxiuCardObject.mMMTwo)) {
					 mMMTwo.setSalesPersonInfo(mBaoxiuCardObject, TOKEN);
				 }
			 }
			 
		return view;
	}

	private void populateView() {
		
		if (!TextUtils.isEmpty(mBaoxiuCardObject.mPKY) && !mBaoxiuCardObject.mPKY.equals(BaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY)) {
			mUsageView.setVisibility(View.VISIBLE);
			 PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, mAvatorView, mBaoxiuCardObject.mPKY, null, PhotoManagerUtilsV2.TaskType.HOME_DEVICE_AVATOR);
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
		 
//		 mPinpaiInput.setText(BaoxiuCardObject.getTagName(mBaoxiuCardObject.mCardName, mBaoxiuCardObject.mPinPai, mBaoxiuCardObject.mLeiXin));
		 mPinpaiInput.setText(mBaoxiuCardObject.mPinPai);
		 mLeixingInput.setText(mBaoxiuCardObject.mLeiXin);
		 mModelInput.setText(mBaoxiuCardObject.mXingHao);
		 
		 mBaoxiuTelInput.setText(mBaoxiuCardObject.mBXPhone);
		 mYanbaoTelInput.setText(mBaoxiuCardObject.mYBPhone);
		 mYanbaoCompanyInput.setText(mBaoxiuCardObject.mYanBaoDanWei);
		 mYanbaoTimeInput.setText(mBaoxiuCardObject.mYanBaoTime);
		 
//		 if (!TextUtils.isEmpty(mBaoxiuCardObject.mWY)) {
//			 //保修期，这里单位是年
//			 float year = Float.valueOf(mBaoxiuCardObject.mWY);
//			 if (year > 0 && (year - 0.5f) < 0.00001f) {
//				 mBaoxiuInput.setText(R.string.unit_half_year);
//			 } else {
//				 mBaoxiuInput.setText(mBaoxiuCardObject.mWY + getString(R.string.unit_year));
//			 }
//		 }
		 int day = mBaoxiuCardObject.getBaoxiuValidity();
		 day = day > 0 ? day : 0;
		 mCircleProgressView.setNumber(day);
		 //计算开始角度和结束角度
		 //外围粗圆环完整度=(今天-购买日期)/（保修年+延保年）X365
	     //如果为负值，则圆环是满圆
		 int startDegree = -90;
		 int validity = day;
		 int validityTotal = (int) ((Float.valueOf(mBaoxiuCardObject.mWY) + Float.valueOf(mBaoxiuCardObject.mYanBaoTime)) * 365 + 0.5f);
		 int endDegree = (int) (1.0 * validity / validityTotal * 360 + 0.5f);
		 mCircleProgressView.setOutterDegree(startDegree, endDegree, true);
		 
	}
	
	@Override
    public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.string.menu_edit).setVisible(mShowOptionMenu);
    	menu.findItem(R.string.menu_delete).setVisible(mShowOptionMenu);
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
			 HomeObject.setHomeObject(mHomeObject);
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
			File fapiao = MyApplication.getInstance().getProductFaPiaoFile(mBaoxiuCardObject.getFapiaoPhotoId());
			if (fapiao.exists()) {
				mBillView.setEnabled(false);
				mHandler.sendEmptyMessageDelayed(WHAT_SHOW_FAPIAO_WAIT, 6000);
				BaoxiuCardObject.showBill(getActivity(), mBaoxiuCardObject);
			} else {
				NotifyRegistrant.getInstance().register(mHandler);
				//需要下载
				showDialog(DIALOG_PROGRESS);
				if (mFapiaoDownloadView == null) {
					mFapiaoDownloadView = new ImageView(getActivity());
				}
				//为了传值給发票下载
				BaoxiuCardObject.setBaoxiuCardObject(mBaoxiuCardObject);
				PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, mFapiaoDownloadView, mBaoxiuCardObject.getFapiaoServicePath(), null, PhotoManagerUtilsV2.TaskType.FaPiao, true);
			}
			
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
		 mBaoxiuCardObject.clear();
	 }
	
	private DeleteCardAsyncTask mDeleteCardAsyncTask;
	private void delteCardAsync() {
		AsyncTaskUtils.cancelTask(mDeleteCardAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mDeleteCardAsyncTask = new DeleteCardAsyncTask();
		mDeleteCardAsyncTask.execute();
	}
	
	@Override
	public void onStop() {
		super.onStop();
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
		private class DownloadProductUsagePdfTask extends AsyncTask<Void, Integer, ServiceResultObject> {

			public long mPdfLength;
			public String mPdfLengthStr;
			@Override
			protected ServiceResultObject doInBackground(Void... arg0) {
				mDownloadGoodsUsagePdfProgressDialog = getProgressDialog();
				ServiceResultObject haierResultObject =  new ServiceResultObject();
				InputStream is = null;
				FileOutputStream fos = null;
				try {
					is = NetworkUtils.openContectionLocked(ServiceObject.getProductPdfUrlForQuery(mBaoxiuCardObject.mKY), MyApplication.getInstance().getSecurityKeyValuesObject());
					if (is != null) {
						haierResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
						if (haierResultObject.isOpSuccessfully()) {
							if (TextUtils.isEmpty(haierResultObject.mStrData)) {
								//没有说明书
								haierResultObject.mStatusCode = 0;
								haierResultObject.mStatusMessage = getString(R.string.msg_no_product_usage);
								return haierResultObject;
							}
							//成功，表示有使用说明书
							NetworkUtils.closeInputStream(is);
							HttpResponse response = NetworkUtils.openContectionLockedV2(ServiceObject.getProductUsageUrl(haierResultObject.mStrData), MyApplication.getInstance().getSecurityKeyValuesObject());
							int code = response.getStatusLine().getStatusCode();
							DebugUtils.logD(TAG, "DownloadProductUsagePdfTask return StatusCode is " + code);
							if (code == HttpStatus.SC_OK) {
								mPdfLength = response.getEntity().getContentLength();
								DebugUtils.logD(TAG, "DownloadProductUsagePdfTask return length of pdf file is " + mPdfLength);
								mPdfLengthStr = FilesUtils.computeLengthToString(mPdfLength);
								is = response.getEntity().getContent();
								
								fos = new FileOutputStream(MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY));
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
							} else if (code == HttpStatus.SC_NOT_FOUND) {
								haierResultObject.mStatusMessage = getString(R.string.msg_no_product_usage);
							}
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					haierResultObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
				} catch (IOException e) {
					e.printStackTrace();
					haierResultObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
				} finally {
					NetworkUtils.closeInputStream(is);
					NetworkUtils.closeOutStream(fos);
				}
				return haierResultObject;
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
			protected void onPostExecute(ServiceResultObject result) {
				super.onPostExecute(result);
				dismissDialog(DIALOG_PROGRESS);
				mDownloadGoodsUsagePdfProgressDialog = null;
				if (result.isOpSuccessfully()) {
					MyApplication.getInstance().showMessage(R.string.msg_product_usage_downloading_ok);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(MyApplication.getInstance().getProductUsagePdf(mBaoxiuCardObject.mKY)));
					intent.setClass(getActivity(), PdfViewerActivity.class);
					startActivity(intent);
				} else {
					DialogUtils.createSimpleConfirmAlertDialog(getActivity(), result.mStatusMessage, getString(android.R.string.ok), getString(android.R.string.cancel), null);
				}
			}
		}
		//add by chenkai, for Usage, 2014.05.31, end

		@Override
        public void updateArguments(Bundle args) {
	        
        }
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putBundle(TAG, mBundles);
			DebugUtils.logD(TAG, "onSaveInstanceState(), we try to save mBundles=" + mBundles);
		}
		
		@Override
	   	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	   		super.onActivityResult(requestCode, resultCode, data);
	   		if (resultCode == Activity.RESULT_OK) {
	   			//扫描到了mm号
	   			String mm = data.getStringExtra(Intents.EXTRA_BID);
	   			DebugUtils.logD(TAG, "onActivityResult return mm=" + mm);
	   			if (!TextUtils.isEmpty(mm)) {
	   				if (requestCode == BaoxiuCardViewSalemanInfoView.TYPE_MM_ONE) {
	   					mMMOne.downloadSalesPersonInfo(mBaoxiuCardObject, mm, TOKEN);
	   				} else if (requestCode == BaoxiuCardViewSalemanInfoView.TYPE_MM_TWO) {
	   					mMMTwo.downloadSalesPersonInfo(mBaoxiuCardObject, mm, TOKEN);
	   				}
	   	   		 }
	   		}
	   	}

}
