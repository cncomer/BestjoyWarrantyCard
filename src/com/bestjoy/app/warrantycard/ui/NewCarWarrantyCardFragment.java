package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.IBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.HaierPopView;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.ImageHelper;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class NewCarWarrantyCardFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TAG = "NewCarWarrantyCardFragment";
	private static final String TOKEN = NewCarWarrantyCardFragment.class.getName();
	//按钮
	private Button mSaveBtn;
	private TextView mDatePickBtn, mBaoxianDatePickBtn, mBaoYangDatePickBtn, mYanCheDatePickBtn;
	private ImageView mBillImageView;
	private EditText mPinPaiInput, mXingHaoInput, mChePaiInput, mCheJiaInput, mFaDongJiInput, mBaoxiuTelInput;
	private EditText mYanbaoComponyInput, mYanbaoTelInput;
	private Calendar mCalendar, mBaoxianCalendar, mBaoYanCalendar, mYanCheCalendar;
	/**购买途径*/
	private HaierPopView mYanbaoPopView;
	//临时的拍摄照片路径
	private File mBillTempFile, mAvatorTempFile;
	/**请求商品预览图*/
	private static final int REQUEST_AVATOR = 2;
	/**请求发票预览图*/
	private static final int REQUEST_BILL = 3;
	
	/**是否要重新拍摄商品预览图*/
	private static final int DIALOG_PICTURE_AVATOR_CONFIRM = 4;
	private static final int DIALOG_BILL_OP_CONFIRM = 5;
	
	private int mPictureRequest = -1;
	
	private CarBaoxiuCardObject mBaoxiuCardObject;
	private Handler mHandler;
	private Bundle mBundle;

	private boolean mNeedLoadFapiao = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PhotoManagerUtilsV2.getInstance().requestToken(TOKEN);
		mCalendar = Calendar.getInstance();
		mBaoxianCalendar = Calendar.getInstance();
		mBaoxianCalendar.add(Calendar.YEAR, 1);//自动加一年
		mBaoYanCalendar = Calendar.getInstance();
		mYanCheCalendar = Calendar.getInstance();
		if (savedInstanceState == null) {
			mBundle = getArguments();
			DebugUtils.logD(TAG, "onCreate() savedInstanceState == null, getArguments() mBundle=" + mBundle);
		} else {
			mBundle = savedInstanceState.getBundle(TAG);
			mNeedLoadFapiao = savedInstanceState.getBoolean("mNeedLoadFapiao", false);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle + ", mNeedLoadFapiao=" + mNeedLoadFapiao);
		}
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				DebugUtils.logD(TAG, "handleMessage() loadFapiaoFromCameraAsync");
				loadFapiaoFromCameraAsync();
			}
			
		};
		initTempFile();
		
		mBaoxiuCardObject = CarBaoxiuCardObject.getBaoxiuCardObject(mBundle);
		
		setHasOptionsMenu(true);
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		DebugUtils.logD(TAG, "onSaveInstanceState() save mBundle=" + mBundle + ", mNeedLoadFapiao=" + mNeedLoadFapiao);
		outState.putBundle(TAG, mBundle);
		outState.putBoolean("mNeedLoadFapiao", mNeedLoadFapiao);
	}
	@Override
	public void onResume() {
		super.onResume();
		DebugUtils.logD(TAG, "onResume() mNeedLoadFapiao=" + mNeedLoadFapiao + ", mBillTempFile=" + mBillTempFile);
		if (mNeedLoadFapiao) {
			DebugUtils.logD(TAG, "onResume() removeMessages REQUEST_BILL, sendEmptyMessage REQUEST_BILL");
			mHandler.removeMessages(REQUEST_BILL);
			mHandler.sendEmptyMessageDelayed(REQUEST_BILL, 500);
		}
		
	}
	
	private void initTempFile() {
		File tempRootDir = Environment.getExternalStorageDirectory();
		mBillTempFile = new File(tempRootDir, ".billTemp");
		mAvatorTempFile = new File(tempRootDir, ".avatorTemp");
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.activity_new_car_card, container, false);
		 mBillImageView = (ImageView) view.findViewById(R.id.button_scan_bill);
		 mBillImageView.setOnClickListener(this);
		 mPinPaiInput = (EditText) view.findViewById(R.id.car_product_pinpai_input);
		 mXingHaoInput = (EditText) view.findViewById(R.id.car_product_xinghao_input);
		 mChePaiInput = (EditText) view.findViewById(R.id.car_product_chepai_input);
		 mCheJiaInput = (EditText) view.findViewById(R.id.car_product_chejia_input);
		 mFaDongJiInput = (EditText) view.findViewById(R.id.car_product_fadongji_input);
		 mBaoxiuTelInput = (EditText) view.findViewById(R.id.car_product_tel_input);
		 //购买日期
		 mDatePickBtn = (TextView) view.findViewById(R.id.product_buy_date);
		 mDatePickBtn.setOnClickListener(this);
		 
		 mDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
		 
		 mBaoYangDatePickBtn = (TextView) view.findViewById(R.id.car_product_baoyan_input);
		 mBaoYangDatePickBtn.setOnClickListener(this);
		 mBaoYangDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoYanCalendar.getTime()));
		 
		 mYanCheDatePickBtn = (TextView) view.findViewById(R.id.car_product_yanche_input);
		 mYanCheDatePickBtn.setOnClickListener(this);
		 mYanCheDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mYanCheCalendar.getTime()));
		//保险到期
		 mBaoxianDatePickBtn = (TextView) view.findViewById(R.id.car_product_baoxian);
		 mBaoxianDatePickBtn.setOnClickListener(this);
		 mBaoxianDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoxianCalendar.getTime()));
		 
		 mYanbaoPopView = new HaierPopView(getActivity(), view, R.id.product_buy_delay_time, R.id.menu_choose_yanbao);
		 //mYanbaoTimeInput = (EditText) view.findViewById(R.id.product_buy_delay_time);
		 mYanbaoComponyInput = (EditText) view.findViewById(R.id.product_buy_delay_componey);
		 mYanbaoTelInput = (EditText) view.findViewById(R.id.product_buy_delay_componey_tel);
		 
		 mSaveBtn = (Button) view.findViewById(R.id.button_save);
		 mSaveBtn.setOnClickListener(this);

		mYanbaoPopView.setDataSource(getResources().getStringArray(R.array.yanbao_times));

		view.findViewById(R.id.button_scan_qrcode).setOnClickListener(this);
		view.findViewById(R.id.menu_choose).setOnClickListener(this);
		populateBaoxiuInfoView();
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem item = menu.add(0, R.id.button_save, 101, R.string.button_save);
		if (isEditable()) {
			item.setTitle(R.string.button_update);
		}
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
	
//	@Override
//    public void onPrepareOptionsMenu(Menu menu) {
//		MenuItem menuItem = menu.findItem(R.id.button_save);
//		if (menuItem != null) {
//			if (isEditable()) {
//				menuItem.setTitle(R.string.button_update);
//			}
//		}
//    }
	
	 @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		 switch(item.getItemId()){
		 case R.id.button_save:
			 onSave();
			 return true;
		 }
        return false;
    }
	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		PhotoManagerUtilsV2.getInstance().releaseToken(TOKEN);
		CarBaoxiuCardObject.showBill(getActivity(), null);
		DebugUtils.logD(TAG, "onDestroyView() mNeedLoadFapiao=" + mNeedLoadFapiao + ", mBillTempFile=" + mBillTempFile);
		AsyncTaskUtils.cancelTask(mLoadFapiaoTask);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		DebugUtils.logD(TAG, "onDestroy() mNeedLoadFapiao=" + mNeedLoadFapiao + ", mBillTempFile=" + mBillTempFile);
		mBillImageView.setImageBitmap(null);
		mBundle.putBundle(CarBaoxiuCardObject.TAG, null);
	}
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_BILL_OP_CONFIRM:
			return new AlertDialog.Builder(getActivity())
			.setItems(this.getResources().getStringArray(R.array.bill_op_items), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
					case 0:
						CarBaoxiuCardObject.showBill(getActivity(), mBaoxiuCardObject);
						break;
					case 1:
						mPictureRequest = REQUEST_BILL;
						onCapturePhoto();
						break;
					}
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.create();
		}
		
		return super.onCreateDialog(id);
	}
	
	public boolean isEditable() {
		return mBaoxiuCardObject != null && mBaoxiuCardObject.mBID > 0;
	}
	
	private void populateBaoxiuInfoView() {
		//init layouts
		if (!isEditable()) {
			mPinPaiInput.setText(mBaoxiuCardObject.mPinPai);
			mXingHaoInput.setText(mBaoxiuCardObject.mXingHao);
			mChePaiInput.setText(mBaoxiuCardObject.mChePai);
			mCheJiaInput.setText(mBaoxiuCardObject.mCheJia);
			mFaDongJiInput.setText(mBaoxiuCardObject.mFaDongJi);
			mBaoxiuTelInput.setText(mBaoxiuCardObject.mBXPhone);
			
			mYanbaoPopView.getText().clear();
			mYanbaoComponyInput.getText().clear();
			mYanbaoTelInput.getText().clear();
		} else {
			mPinPaiInput.setText(mBaoxiuCardObject.mPinPai);
			mXingHaoInput.setText(mBaoxiuCardObject.mXingHao);
			mChePaiInput.setText(mBaoxiuCardObject.mChePai);
			mCheJiaInput.setText(mBaoxiuCardObject.mCheJia);
			mFaDongJiInput.setText(mBaoxiuCardObject.mFaDongJi);
			mBaoxiuTelInput.setText(mBaoxiuCardObject.mBXPhone);

			mYanbaoPopView.setText(mBaoxiuCardObject.mYanBaoTime);
			mYanbaoComponyInput.setText(mBaoxiuCardObject.mYanBaoDanWei);
			mYanbaoTelInput.setText(mBaoxiuCardObject.mYBPhone);
			
			mCalendar.setTimeInMillis(Long.valueOf(mBaoxiuCardObject.mBuyDate));
			mDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
			
			mBaoxianCalendar.setTimeInMillis(Long.valueOf(mBaoxiuCardObject.mBaoXianDeadline));
			mBaoxianDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoxianCalendar.getTime()));
			
			mBaoYanCalendar.setTimeInMillis(Long.valueOf(mBaoxiuCardObject.mLastBaoYanTime));
			mBaoYangDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoYanCalendar.getTime()));
			
			mYanCheCalendar.setTimeInMillis(Long.valueOf(mBaoxiuCardObject.mLastYanCheTime));
			mYanCheDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoYanCalendar.getTime()));
			//如果有发票，我们显示出来
			if (mBaoxiuCardObject.hasBillAvator()) {
				PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, mBillImageView, mBaoxiuCardObject.getFapiaoServicePath(), null, PhotoManagerUtilsV2.TaskType.FaPiao);
			}
			
			//设置标题为编辑保修卡
			getActivity().setTitle(R.string.button_edit_card);
			mSaveBtn.setText(R.string.button_update);
		}
		
	}
	
	public void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject) {
		if (slideManuObject instanceof CarBaoxiuCardObject) {
			CarBaoxiuCardObject object = (CarBaoxiuCardObject) slideManuObject;
			 mPinPaiInput.setText(object.mPinPai);
			 mXingHaoInput.setText(object.mXingHao);
			 mChePaiInput.setText(object.mChePai);
			 mCheJiaInput.setText(object.mCheJia);
			 mFaDongJiInput.setText(object.mFaDongJi);
			 mBaoxiuTelInput.setText(object.mBXPhone);
			 mBaoxiuCardObject.mKY = object.mKY;
			 mBaoxiuCardObject.mWY = object.mWY;
		}
	}
	
	private CarBaoxiuCardObject getBaoxiuCardObject() {
		mBaoxiuCardObject.mPinPai = mPinPaiInput.getText().toString().trim();
		mBaoxiuCardObject.mXingHao = mXingHaoInput.getText().toString().trim();
		mBaoxiuCardObject.mChePai = mChePaiInput.getText().toString().trim();
		mBaoxiuCardObject.mCheJia = mCheJiaInput.getText().toString().trim();
		mBaoxiuCardObject.mFaDongJi = mFaDongJiInput.getText().toString().trim();
		mBaoxiuCardObject.mBXPhone = mBaoxiuTelInput.getText().toString().trim();
		
		mBaoxiuCardObject.mBuyDate = String.valueOf(mCalendar.getTimeInMillis());
		mBaoxiuCardObject.mBaoXianDeadline = String.valueOf(mBaoxianCalendar.getTimeInMillis());
		
		String yanbaotime = mYanbaoPopView.getText().toString().trim();
		if (yanbaotime != null && yanbaotime.contains(getActivity().getString(R.string.year))) yanbaotime = yanbaotime.substring(0, yanbaotime.length() - 1);
		mBaoxiuCardObject.mYanBaoTime = yanbaotime;
		mBaoxiuCardObject.mYanBaoDanWei = mYanbaoComponyInput.getText().toString().trim();
		mBaoxiuCardObject.mYBPhone = mYanbaoTelInput.getText().toString().trim();
		
		mBaoxiuCardObject.mLastBaoYanTime = String.valueOf(mBaoYanCalendar.getTimeInMillis());
		mBaoxiuCardObject.mLastYanCheTime = String.valueOf(mYanCheCalendar.getTimeInMillis());
		
		return mBaoxiuCardObject;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_scan_bill:
			if (mBaoxiuCardObject != null && !mBaoxiuCardObject.hasLocalBill()) {
				//如果没有发票，我们直接调用相机
				mPictureRequest = REQUEST_BILL;
				onCapturePhoto();
			} else {
				//如果有，我们显示操作选项，查看或是拍摄发票
				onCreateDialog(DIALOG_BILL_OP_CONFIRM).show();
			}
			break;
		case R.id.button_scan_qrcode:
//			Intent scanIntent = new Intent(getActivity(), CaptureActivity.class);
//			scanIntent.putExtra(Intents.EXTRA_SCAN_TASK, true);
//			startActivityForResult(scanIntent, REQUEST_SCAN);
			startScan();
			break;
		case R.id.product_buy_date:
			showDatePickerDialog();
			break;
		case R.id.car_product_baoxian:
			showBaoXianDatePickerDialog();
			break;
		case R.id.car_product_baoyan_input:
			showBaoYanDatePickerDialog();
			break;
		case R.id.car_product_yanche_input:
			showYanCheDatePickerDialog();
			break;
		case R.id.button_save:
//			if (mBaoxiuCardObject.hasBill()) {
//				saveNewWarrantyCardAndSync();
//			} else {
//				MyApplication.getInstance().showMessage(R.string.msg_cant_show_bill);
//			}
			onSave();
			break;
		case R.id.menu_choose:
			//如果内容为空，我们显示侧边栏
			((NewCardActivity) getActivity()).getSlidingMenu().showMenu(true);
			break;
		}
		
	}
	
	private void onSave() {
		if (!MyAccountManager.getInstance().hasLoginned() || MyAccountManager.getInstance().getCurrentAccountId() == AccountObject.DEMO_ACCOUNT_UID) {
			MyApplication.getInstance().showNeedLoginMessage();
			LoginActivity.startIntent(getActivity(), mBundle);
			return;
		}
		 if (ComConnectivityManager.getInstance().isConnected()) {
			 if (!checkInput()) {
				 return;
			 }
			 if (isEditable()) {
					updateWarrantyCardAsync();
				} else {
					saveNewWarrantyCardAndSync();
				}
		 } else {
			 showDialog(DIALOG_DATA_NOT_CONNECTED);
		 }
	}
	
	private CreateNewWarrantyCardAsyncTask mCreateNewWarrantyCardAsyncTask;
	private void saveNewWarrantyCardAndSync() {
		mSaveBtn.setEnabled(false);
		AsyncTaskUtils.cancelTask(mCreateNewWarrantyCardAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mCreateNewWarrantyCardAsyncTask = new CreateNewWarrantyCardAsyncTask();
		mCreateNewWarrantyCardAsyncTask.execute();
	}

	private class CreateNewWarrantyCardAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		/*{
		    "StatusCode": "1", 
		    "StatusMessage": "成功返回数据", 
		    "Data": "Bid:4"
		}*/
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			//更新保修卡信息
			CarBaoxiuCardObject baoxiuCardObject = getBaoxiuCardObject();
			DebugUtils.logD(TAG, "CreateNewWarrantyCardAsyncTask for SID " + baoxiuCardObject.mBID);
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			
			AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("xinghao", baoxiuCardObject.mXingHao)
				.put("che_haoma", baoxiuCardObject.mChePai)
				.put("che_jiahao", baoxiuCardObject.mCheJia)
				.put("fadongjihao", baoxiuCardObject.mFaDongJi)
				.put("buydate", baoxiuCardObject.mBuyDate)
				.put("changjia_phone", baoxiuCardObject.mBXPhone)
				.put("shangcibaoyang", baoxiuCardObject.mLastBaoYanTime)
				.put("uid", baoxiuCardObject.mUID)
//				.put("WY", baoxiuCardObject.mWY)
				.put("shangciyanche", baoxiuCardObject.mLastYanCheTime)
				.put("baoyangdaoqi", baoxiuCardObject.mBaoXianDeadline)
				.put("yanbaodanwei", baoxiuCardObject.mYanBaoDanWei)
				.put("yanbaotime", baoxiuCardObject.mYanBaoTime)
				.put("yanbaophone", baoxiuCardObject.mYBPhone)
				.put("fsphone", baoxiuCardObject.m4SShopTel)
				.put("washphone", baoxiuCardObject.m4SWashingShopTel)
				.put("weixiuphone", baoxiuCardObject.mWeixiuShopTel)
				.put("chuxianphone", baoxiuCardObject.mChuxianShopTel)
				.put("pinpai", baoxiuCardObject.mPinPai)
				.put("imgstr", baoxiuCardObject.getBase64StringFromBillAvator());
				
				DebugUtils.logD(TAG, "para=" + jsonObject.toString());
				is = NetworkUtils.openPostContectionLocked(ServiceObject.createCarBaoxiuCardUrl(), "para", jsonObject.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				try {
					serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					if (serviceResultObject.isOpSuccessfully()) {
						if (serviceResultObject.mJsonData != null) {
							baoxiuCardObject = CarBaoxiuCardObject.parseBaoxiuCards(serviceResultObject.mJsonData, accountObject);
							//如果后台返回了保修卡数据,我们解析它保存在本地
							DebugUtils.logE(TAG, "new SID=" + baoxiuCardObject.mBID);
							if (baoxiuCardObject.mBID > 0) {
								//正常情况
								boolean savedOk = baoxiuCardObject.saveInDatebase(getActivity().getContentResolver(), null);
								if (!savedOk) {
									//通常不会发生
									serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
								} 
								return serviceResultObject;
							}
						}
						serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
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
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			mSaveBtn.setEnabled(true);
			dissmissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				//添加成功
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				getActivity().finish();
				mBaoxiuCardObject.clear();
				mBundle.putBundle(IBaoxiuCardObject.TAG, null);
				MyChooseCarCardsActivity.startIntent(getActivity(), mBundle);
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dissmissDialog(DIALOG_PROGRESS);
			mSaveBtn.setEnabled(true);
		}
	}
	
	//########################更新操作 开始################################################
	/*
	 ServerIP/Haier/UpdateBaoXiu.ashx
	参数：
	LeiXin 
	XingHao 
	SHBianHao 
	BXPhone 
	BuyTuJing 
	YanBaoDanWei 
	Tag 
	YanBaoTime 
	YBPhone 
	BID 
	BuyDate 
	BuyPrice 
	FPaddr 
	AID 
	说明
	跟添加保修数据的参数唯一不同的事一个有UID(用户ID)没有BID 
	这个有BID(保修ID) 没有用户ID 

	 */
	private UpdateWarrantyCardAsyncTask mUpdateWarrantyCardAsyncTask;
	private void updateWarrantyCardAsync() {
		mSaveBtn.setEnabled(false);
		AsyncTaskUtils.cancelTask(mUpdateWarrantyCardAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mUpdateWarrantyCardAsyncTask = new UpdateWarrantyCardAsyncTask();
		mUpdateWarrantyCardAsyncTask.execute();
	}

	private class UpdateWarrantyCardAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {
		/*{
		    "StatusCode": "1", 
		    "StatusMessage": "成功返回数据", 
		    "Data": "Bid:4"
		}*/
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			//更新保修卡信息
			CarBaoxiuCardObject baoxiuCardObject = getBaoxiuCardObject();
			DebugUtils.logD(TAG, "UpdateWarrantyCardAsyncTask SID " + baoxiuCardObject.mBID+", ky=" + baoxiuCardObject.mKY);
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("xinghao", baoxiuCardObject.mXingHao)
				.put("che_haoma", baoxiuCardObject.mChePai)
				.put("che_jiahao", baoxiuCardObject.mCheJia)
				.put("fadongjihao", baoxiuCardObject.mFaDongJi)
				.put("buydate", baoxiuCardObject.mBuyDate)
				.put("changjia_phone", baoxiuCardObject.mBXPhone)
				.put("shangcibaoyang", baoxiuCardObject.mLastBaoYanTime)
				.put("uid", baoxiuCardObject.mUID)
				.put("cid", baoxiuCardObject.mBID)
				.put("shangciyanche", baoxiuCardObject.mLastYanCheTime)
				.put("baoyangdaoqi", baoxiuCardObject.mBaoXianDeadline)
				.put("yanbaodanwei", baoxiuCardObject.mYanBaoDanWei)
				.put("yanbaotime", baoxiuCardObject.mYanBaoTime)
				.put("yanbaophone", baoxiuCardObject.mYBPhone)
				.put("fsphone", baoxiuCardObject.m4SShopTel)
				.put("washphone", baoxiuCardObject.m4SWashingShopTel)
				.put("weixiuphone", baoxiuCardObject.mWeixiuShopTel)
				.put("chuxianphone", baoxiuCardObject.mChuxianShopTel)
				.put("pinpai", baoxiuCardObject.mPinPai)
				.put("imgstr", baoxiuCardObject.getBase64StringFromBillAvator());
				
				DebugUtils.logD(TAG, "para=" + jsonObject.toString());
				is = NetworkUtils.openPostContectionLocked(ServiceObject.updateCarBaoxiuCardUrl(), "para", jsonObject.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				try {
					serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					if (serviceResultObject.isOpSuccessfully()) {
						if (serviceResultObject.mJsonData != null) {
							baoxiuCardObject = CarBaoxiuCardObject.parseBaoxiuCards(serviceResultObject.mJsonData, accountObject);
							//如果后台返回了保修卡数据,我们解析它保存在本地
							DebugUtils.logE(TAG, "new SID=" + baoxiuCardObject.mBID);
							if (baoxiuCardObject.mBID > 0) {
								//正常情况
								boolean savedOk = baoxiuCardObject.saveInDatebase(getActivity().getContentResolver(), null);
								if (!savedOk) {
									//通常不会发生
									serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
								} 
								return serviceResultObject;
							}
						}
						serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					serviceResultObject.mStatusMessage = getActivity().getString(R.string.msg_local_save_card_failed);
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
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			mSaveBtn.setEnabled(true);
			dissmissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(R.string.update_success);
				getActivity().finish();
				mBaoxiuCardObject.clear();
				mBundle.putBundle(CarBaoxiuCardObject.TAG, null);
				MyChooseCarCardsActivity.startIntent(getActivity(), getArguments());
			} else {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dissmissDialog(DIALOG_PROGRESS);
			mSaveBtn.setEnabled(true);
		}
	}
	
	
	//########################更新操作 结束#################################################

	private boolean checkInput() {
		if(TextUtils.isEmpty(mPinPaiInput.getText().toString().trim())){
			showEmptyInputToast(R.string.car_product_pinpai);
			return false;
		}
		if(TextUtils.isEmpty(mXingHaoInput.getText().toString().trim())){
			showEmptyInputToast(R.string.car_product_xinghao);
			return false;
		}
		if(TextUtils.isEmpty(mCheJiaInput.getText().toString().trim())){
			showEmptyInputToast(R.string.car_product_chejia);
			return false;
		}
		if(TextUtils.isEmpty(mFaDongJiInput.getText().toString().trim())){
			showEmptyInputToast(R.string.car_product_fadongji);
			return false;
		}
//		if(TextUtils.isEmpty(mBianhaoInput.getText().toString().trim())){
//			showEmptyInputToast(R.string.product_sn);
//			return false;
//		}
//		if(TextUtils.isEmpty(mBaoxiuTelInput.getText().toString().trim())){
//			showEmptyInputToast(R.string.product_tel);
//			return false;
//		}
		/*if(TextUtils.isEmpty(mDatePickBtn.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_date);
			return false;
		}
		if(TextUtils.isEmpty(mPriceInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_cost);
			return false;
		}
		if(TextUtils.isEmpty(mTujingInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_entry);
			return false;
		}
		if(TextUtils.isEmpty(mYanbaoTimeInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_delay_time);
			return false;
		}
		if(TextUtils.isEmpty(mYanbaoComponyInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_delay_componey);
			return false;
		}
		if(TextUtils.isEmpty(mYanbaoTelInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_buy_delay_componey_tel);
			return false;
		}*/
		return true;
	}

	private void showEmptyInputToast(int resId) {
		String msg = getResources().getString(resId);
		MyApplication.getInstance().showMessage(getResources().getString(R.string.input_type_please_input) + msg);
	}

	private void showDatePickerDialog() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(year, monthOfYear, dayOfMonth);
				//更新UI
				mDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
			}
				
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH))
		.show();
	}
	private void showBaoXianDatePickerDialog() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mBaoxianCalendar.set(year, monthOfYear, dayOfMonth);
				//更新UI
				mBaoxianDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
			}
				
		}, mBaoxianCalendar.get(Calendar.YEAR), mBaoxianCalendar.get(Calendar.MONTH), mBaoxianCalendar.get(Calendar.DAY_OF_MONTH))
		.show();
	}
	private void showBaoYanDatePickerDialog() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mBaoYanCalendar.set(year, monthOfYear, dayOfMonth);
				//更新UI
				mBaoYangDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mBaoYanCalendar.getTime()));
			}
				
		}, mBaoYanCalendar.get(Calendar.YEAR), mBaoYanCalendar.get(Calendar.MONTH), mBaoYanCalendar.get(Calendar.DAY_OF_MONTH))
		.show();
	}
	private void showYanCheDatePickerDialog() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mYanCheCalendar.set(year, monthOfYear, dayOfMonth);
				//更新UI
				mYanCheDatePickBtn.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mYanCheCalendar.getTime()));
			}
				
		}, mYanCheCalendar.get(Calendar.YEAR), mYanCheCalendar.get(Calendar.MONTH), mYanCheCalendar.get(Calendar.DAY_OF_MONTH))
		.show();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		DebugUtils.logD(TAG, "onActivityResult() requestCode=" + requestCode + ", resultCode=" + resultCode);
		if (resultCode == Activity.RESULT_OK) {
			if (REQUEST_BILL == requestCode) {
                if (mBillTempFile.exists()) {
                	mNeedLoadFapiao = true;
				}
                return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
    public void setScanObjectAfterScan(InfoInterface barCodeObject) {
		CarBaoxiuCardObject object = (CarBaoxiuCardObject) barCodeObject;
		 mPinPaiInput.setText(object.mPinPai);
		 mXingHaoInput.setText(object.mXingHao);
		 mChePaiInput.setText(object.mChePai);
		 mCheJiaInput.setText(object.mCheJia);
		 mFaDongJiInput.setText(object.mFaDongJi);
		 mBaoxiuTelInput.setText(object.mBXPhone);
		 mBaoxiuCardObject.mKY = object.mKY;
		 mBaoxiuCardObject.mWY = object.mWY;
		//这里一般我们只设置品牌、型号、编号和名称
//		if (!TextUtils.isEmpty(object.mLeiXin)) {
//			mTypeInput.setText(object.mLeiXin);
//		}
//		if (!TextUtils.isEmpty(object.mPinPai)) {
//			mPinpaiInput.setText(object.mPinPai);
//		}
//		if (!TextUtils.isEmpty(object.mSHBianHao)) {
//			mBianhaoInput.setText(object.mSHBianHao);
//		}
//		if (!TextUtils.isEmpty(object.mXingHao)) {
//			mModelInput.setText(object.mXingHao);
//		}
//		if (!TextUtils.isEmpty(object.mBXPhone)) {
//			mBaoxiuTelInput.setText(object.mBXPhone);
//		}
//		if (!TextUtils.isEmpty(object.mWY)) {
//			mWyInput.setText(object.mWY);
//		}
	}
	
	@Override
	public InfoInterface getScanObjectAfterScan() {
		return CarBaoxiuCardObject.getBaoxiuCardObject();
	}
	
	/**
	 * 调用相机拍摄图片
	 */
	private void onCapturePhoto() {
		if (!MyApplication.getInstance().hasExternalStorage()) {
			showDialog(DIALOG_MEDIA_UNMOUNTED);
			return;
		}
		Intent intent = null;
		if (mPictureRequest == REQUEST_AVATOR) {
			intent = ImageHelper.createCaptureIntent(Uri.fromFile(mAvatorTempFile));
		} else if (mPictureRequest == REQUEST_BILL) {
			intent = ImageHelper.createCaptureIntent(Uri.fromFile(mBillTempFile));
		}
		startActivityForResult(intent, mPictureRequest);
	}
	
	@Override
	public void updateInfoInterface(InfoInterface infoInterface) {
	}
	@Override
	public void updateArguments(Bundle bundle) {
		mBundle = bundle;
		mBaoxiuCardObject.mBID = mBundle.getLong("sid", -1);
		mBaoxiuCardObject.mUID = mBundle.getLong("uid", -1);
	}
	
	private LoadFapiaoTask mLoadFapiaoTask;
	private void loadFapiaoFromCameraAsync() {
		AsyncTaskUtils.cancelTask(mLoadFapiaoTask);
		showDialog(DIALOG_PROGRESS);
		mLoadFapiaoTask = new LoadFapiaoTask();
		mLoadFapiaoTask.execute();
	}
	private class LoadFapiaoTask extends AsyncTask<Void, Void, Boolean> {

		private Bitmap last = null;
		
		@Override
        protected void onPreExecute() {
	        super.onPreExecute();
	        last = mBaoxiuCardObject.mBillTempBitmap;
	        mBillImageView.setImageBitmap(last);
        }

		@Override
		protected Boolean doInBackground(Void... arg0) {
			DebugUtils.logW(TAG, "LoadFapiaoTask doInBackground()");
			int tryTime = 0;
			while (tryTime < 2) {
				try {
					mBaoxiuCardObject.updateBillAvatorTempLocked(mBillTempFile);
					return true;
				} catch(OutOfMemoryError e) {
					e.printStackTrace();
					DebugUtils.logW(TAG, "updateBillAvatorTempLocked oom " + e.getMessage());
					tryTime ++;
				}
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			DebugUtils.logW(TAG, "LoadFapiaoTask onPostExecute()");
			dismissDialog(DIALOG_PROGRESS);
			if (result) {
				mBillImageView.setImageBitmap(mBaoxiuCardObject.mBillTempBitmap);
				if (last != null) {
					last.recycle();
				}
			} else {
				new AlertDialog.Builder(getActivity())
				.setMessage(R.string.error_oom_for_fapiao)
				.setPositiveButton(R.string.button_ok, null)
				.show();
			}
			mNeedLoadFapiao = false;
			
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			DebugUtils.logW(TAG, "LoadFapiaoTask onCancelled()");
			dismissDialog(DIALOG_PROGRESS);
			mNeedLoadFapiao = false;
		}
		
	}
	
}
