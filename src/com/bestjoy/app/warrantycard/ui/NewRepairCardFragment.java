package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.bx.order.OrdersListActivity;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.SpeechRecognizerEngine;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.ImageHelper;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class NewRepairCardFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TAG = "NewRepairCardFragment";
	//按钮
	private Button mSaveBtn;
	//联系人信息
	//private EditText mContactNameInput, mContactTelInput;
	//private ProCityDisEditView mProCityDisEditView;
	//private HaierProCityDisEditPopView mProCityDisEditPopView;
	private BaoxiuCardObject mBaoxiuCardObject;
	private AccountObject mAccountObject;
	//预约信息
	private TextView mYuyueDate, mYuyueTime;
	private TextView mProductInfoVew, mAccountInfoView, mContactPlaceView;
	private Calendar mCalendar;
	
	private EditText mAskInput;
	//private Handler mHandler;
	private Button mSpeakButton;
	private SpeechRecognizerEngine mSpeechRecognizerEngine;
	
	
	private ScrollView mScrollView;
	private RadioButton mRepairRadioButton;
	private CardViewFragment mContent;
	private TextView mMalfunctionBtn, mMaintenancePointBtn, mBuyMaintenanceComponentBtn;
	/**预约类型T01为安装 T02为维修, T15为保养, 默认是维修，注意修改默认值需要同步修改布局文件的checked对应的RadioButton*/
	private String mYuyueType = "T02";
	private Bundle mBundle;
	
	private File mCaptureFile;
	private static final int REQUEST_PICK_FROM_CAMERA = 1;
	private ImageView mCaptureButton;
	private Bitmap mCaptureButtonBitmap;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.activity_title_repair);
		mCalendar = Calendar.getInstance();
		mCaptureFile = new File(MyApplication.getInstance().getExternalStorageRoot(""), ".capture");
		
		if (savedInstanceState == null) {
			mBundle = getArguments();
			DebugUtils.logD(TAG, "onCreate() savedInstanceState == null, getArguments() mBundle=" + mBundle);
			if (mCaptureFile.exists()) {
				mCaptureFile.delete();
			}
		} else {
			mBundle = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle);
		}
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
		
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.activity_repair_20140518, container, false);
		 
		 mScrollView = (ScrollView) view.findViewById(R.id.scrollview);
		 
		 mProductInfoVew = (TextView) view.findViewById(R.id.product_info);
		 mAccountInfoView = (TextView) view.findViewById(R.id.account_info);
		 mContactPlaceView = (TextView) view.findViewById(R.id.contact_place);
		 
		 //语音
		 mAskInput = (EditText) view.findViewById(R.id.product_ask_online_input);
		 mSpeakButton =  (Button) view.findViewById(R.id.button_speak);
		 mSpeakButton.setOnClickListener(this);
		 mSpeechRecognizerEngine = SpeechRecognizerEngine.getInstance(getActivity());
		 mSpeechRecognizerEngine.setResultText(mAskInput);
		 
		 mSaveBtn = (Button) view.findViewById(R.id.button_save);
		 mSaveBtn.setOnClickListener(this);
		 
		 mRepairRadioButton = (RadioButton) view.findViewById(R.id.radio_repair);
		 
		 RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		 radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.radio_repair:
					mYuyueType = "T02";
					break;
				case R.id.radio_install:
					mYuyueType = "T01";
					break;
				case R.id.radio_maintenance:
					mYuyueType = "T15";
					break;
				}
			}
			 
		 });

		 //预约时间
		 ((TextView) view.findViewById(R.id.yuyue_info_title)).setTextColor(getResources().getColor(R.color.light_green));
		 view.findViewById(R.id.yuyue_info_divider).setBackgroundResource(R.color.light_green);
		 mYuyueDate = (TextView) view.findViewById(R.id.date);
		 mYuyueTime = (TextView) view.findViewById(R.id.time);
		 //不需要自动填写预约时间
		 //mYuyueDate.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
		 //mYuyueTime.setText(DateUtils.TOPIC_TIME_FORMAT.format(mCalendar.getTime()));
		 mYuyueDate.setOnClickListener(this);
		 mYuyueTime.setOnClickListener(this);
		 
		 mCaptureButton = (ImageView) view.findViewById(R.id.button_capture);
		 mCaptureButton.setOnClickListener(this);
		 mCaptureButtonBitmap = ((BitmapDrawable)mCaptureButton.getDrawable()).getBitmap();
		 populateBaoxiuInfoView();
		 populateHomeInfoView(HomeObject.getHomeObject(mBundle));
	     populateContactInfoView(MyAccountManager.getInstance().getAccountObject());
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	private void populateBaoxiuInfoView() {
		//init layouts
		mProductInfoVew.setText(mBaoxiuCardObject.mPinPai + "    " + mBaoxiuCardObject.mLeiXin + "    " + mBaoxiuCardObject.mXingHao);
	}
	
	public void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject) {
	}
	
	public void populateHomeInfoView(HomeObject homeObject) {
		mContactPlaceView.setText(homeObject.mHomeProvince + homeObject.mHomeCity + homeObject.mHomeDis + homeObject.mHomePlaceDetail);
	}
	
    public void populateContactInfoView(AccountObject accountObject) {
    	mAccountInfoView.setText(accountObject.mAccountName + "      " + accountObject.mAccountTel);
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_scan_qrcode:
			startScan();
			break;
		case R.id.date:
			showDatePickerDialog();
			break;
		case R.id.time:
			showTimePickerDialog();
			break;
		case R.id.button_speak:
			mSpeechRecognizerEngine.showIatDialog(getActivity());
			break;
		case R.id.button_save:
			createRepairCard();
			break;
		case R.id.menu_choose:
			//如果内容为空，我们显示侧边栏
			((NewCardActivity) getActivity()).getSlidingMenu().showMenu(true);
			break;
		case R.id.button_capture:
			pickFromCamera(mCaptureFile, REQUEST_PICK_FROM_CAMERA);
			//获取故障描述补充拍照
			break;
		}
	}
	
	@Override
	protected void onPickFromCameraFinish(int resultCode) {
		if (REQUEST_PICK_FROM_CAMERA == resultCode) {
			mCaptureButton.setImageBitmap(ImageHelper.getXBitmap(mCaptureButtonBitmap, ImageHelper.getSmallBitmap(mCaptureFile.getAbsolutePath(), mCaptureButton.getWidth(), mCaptureButton.getHeight())));
		}
	}

	private void createRepairCard() {
		if(MyAccountManager.getInstance().hasLoginned()) {
			//如果没有注册，我们前往登陆界面
			if(checkInput()) {
				new AlertDialog.Builder(getActivity())
		    	.setMessage(mYuyueType == "T01" ? R.string.sure_install_tips : mYuyueType == "T02" ? R.string.sure_repair_tips : R.string.sure_maintennace_tips)
		    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						createRepairCardAsync();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
			}
		} else {
			//如果没有注册，我们前往登陆/注册界面，这里传递ModelBundle对象过去，以便做合适的跳转
			MyApplication.getInstance().showMessage(R.string.login_tip);
			LoginActivity.startIntent(this.getActivity(), getArguments());
		}
		
	}

	private CreateRepairCardAsyncTask mCreateRepairCardAsyncTask;
	private void createRepairCardAsync(String... param) {
		AsyncTaskUtils.cancelTask(mCreateRepairCardAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mCreateRepairCardAsyncTask = new CreateRepairCardAsyncTask();
		mCreateRepairCardAsyncTask.execute(param);
	}

	private class CreateRepairCardAsyncTask extends AsyncTask<String, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				JSONObject jsonQuery = new JSONObject();
				jsonQuery.put("bid", mBaoxiuCardObject.mBID);
				jsonQuery.put("date", String.valueOf(mCalendar.getTimeInMillis()));
				jsonQuery.put("service_type", mYuyueType);
				jsonQuery.put("ky", mBaoxiuCardObject.mKY);
				jsonQuery.put("Desc", mAskInput.getText().toString().trim());
				if (mCaptureFile.exists()) {
					jsonQuery.put("imgaddr", ImageHelper.bitmapToString(ImageHelper.getSmallBitmap(mCaptureFile.getAbsolutePath(), 1024, 1024), 65));
				} else {
					jsonQuery.put("imgaddr", "");
				}
				DebugUtils.logD(TAG, "para=" + jsonQuery.toString());
				is = NetworkUtils.openPostContectionLocked(ServiceObject.getRepairUrl(), "para", jsonQuery.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject  = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage  = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage  = e.getMessage();
			} catch (JSONException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage  = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dissmissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				//预约成功
				getActivity().finish();
				MyApplication.getInstance().showMessage(result.mStatusMessage);
				if (MyAccountManager.getInstance().hasBaoxiuCards()) {
					MyChooseDevicesActivity.startIntent(getActivity(), getArguments());
				}
				ComPreferencesManager.getInstance().setFirstLaunch(OrdersListActivity.TAG, true);
			}
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dissmissDialog(DIALOG_PROGRESS);
		}
	}
	
	private boolean checkInput() {
		/*if(TextUtils.isEmpty(mTypeInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_type);
			return false;
		}
		if(TextUtils.isEmpty(mPinpaiInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_brand);
			return false;
		}
		if(TextUtils.isEmpty(mModelInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_model);
			return false;
		}
		if(TextUtils.isEmpty(mBianhaoInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_sn);
			return false;
		}
		if(TextUtils.isEmpty(mBaoxiuTelInput.getText().toString().trim())){
			showEmptyInputToast(R.string.product_tel);
			return false;
		}
		

		if(TextUtils.isEmpty(mContactNameInput.getText().toString().trim())){
			showEmptyInputToast(R.string.name);
			return false;
		}
		if(TextUtils.isEmpty(mContactTelInput.getText().toString().trim())){
			showEmptyInputToast(R.string.usr_tel);
			return false;
		}*/
		
		if(TextUtils.isEmpty(mYuyueDate.getText().toString().trim())){
			showEmptyInputToast(R.string.date);
			return false;
		}
		if(TextUtils.isEmpty(mYuyueTime.getText().toString().trim())){
			showEmptyInputToast(R.string.time);
			return false;
		}
//		if(TextUtils.isEmpty(mAskInput.getText().toString().trim())){
//			showEmptyInputToast(R.string.error_des);
//			return false;
//		}
		if(!checkInstallDate()) {
			MyApplication.getInstance().showMessage(R.string.select_date_tips);
			return false;
		}
		if(!checkInstallHour()) {
			MyApplication.getInstance().showMessage(R.string.select_time_out_of_service_tips);
			return false;
		}
		if(!checkInstallMinute()) {
			MyApplication.getInstance().showMessage(R.string.select_clock_tips);
			return false;
		}
		/*String pinpai = mPinpaiInput.getText().toString().trim();
		final String bxPhone = mBaoxiuTelInput.getText().toString().trim();
		//目前只有海尔支持预约安装和预约维修，如果不是，我们需要提示用户
    	if (!ServiceObject.isHaierPinpai(pinpai)) {
    		new AlertDialog.Builder(getActivity())
	    	.setMessage(R.string.must_haier_confirm_yuyue)
	    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (!TextUtils.isEmpty(bxPhone)) {
						Intents.callPhone(getActivity(), bxPhone);
					} else {
						MyApplication.getInstance().showMessage(R.string.msg_no_bxphone);
					}
					
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
		    return false;
    	}*/
		return true;
	}

	private boolean timeEscapeEnough() {
		if((mCalendar.getTimeInMillis() - System.currentTimeMillis()) > 3 * 60 * 60 * 1000)
			return true;
		return false;
	}
	
	private void showEmptyInputToast(int resId) {
		String msg = getResources().getString(resId);
		MyApplication.getInstance().showMessage(getResources().getString(R.string.input_type_please_input) + msg);
	}


	MyDatePickerDialog mMyDatePickerDialog;
	private void showDatePickerDialog() {
		mMyDatePickerDialog = new MyDatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(year, monthOfYear, dayOfMonth);
				//更新UI
				mYuyueDate.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
			}
				
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
		mMyDatePickerDialog.show();
		if(!checkInstallDate())
			mMyDatePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	private boolean checkInstallDate() {
		Calendar cal = Calendar.getInstance();
		int sameYear = mCalendar.get(Calendar.YEAR) - cal.get(Calendar.YEAR);
		int sameMonth = mCalendar.get(Calendar.MONTH) - cal.get(Calendar.MONTH);
		int sameDay = mCalendar.get(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH);
		
		return sameYear > 0
				|| (sameYear == 0 && sameMonth > 0)
				|| (sameYear == 0 && sameMonth == 0 && sameDay > 0);
	}
	
	private boolean checkInstallHour() {
		int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
		return hour >= 8 && hour <= 19;
	}
	
	private boolean checkInstallMinute() {
		return mCalendar.get(Calendar.MINUTE) == 0;
	}
	
	private boolean checkInstallDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		int sameYear = year - cal.get(Calendar.YEAR);
		int sameMonth = month - cal.get(Calendar.MONTH);
		int sameDay = day - cal.get(Calendar.DAY_OF_MONTH);
		return sameYear > 0
				|| (sameYear == 0 && sameMonth > 0)
				|| (sameYear == 0 && sameMonth == 0 && sameDay > 0);
	}

	class MyDatePickerDialog extends DatePickerDialog {
		public MyDatePickerDialog(Context context, OnDateSetListener callBack,
				int year, int monthOfYear, int dayOfMonth) {
			super(context, callBack, year, monthOfYear, dayOfMonth);
			if(!checkInstallDate()) {
				if(mToast != null) {
					mToast.setText(R.string.select_date_out_of_service_tips);
				} else {
					mToast = Toast.makeText(this.getContext(), R.string.select_date_out_of_service_tips, Toast.LENGTH_LONG);
				}
				mToast.show();
			}
		}

		@Override
		public void onDateChanged(DatePicker view, int year, int month, int day) {
			if(!checkInstallDate(year, month, day)) {
				if(mToast != null) {
					mToast.setText(R.string.select_date_out_of_service_tips);
				} else {
					mToast = Toast.makeText(this.getContext(), R.string.select_date_out_of_service_tips, Toast.LENGTH_LONG);
				}
				mToast.show();
				mMyDatePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setEnabled(false);
			} else {
				mMyDatePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setEnabled(true);
			}
		}
	}
	
	Toast mToast;
	MyTimePickerDialog mMyTimePickerDialog;
	private void showTimePickerDialog() {
		mMyTimePickerDialog = new MyTimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute);
				mYuyueTime.setText(DateUtils.TOPIC_TIME_FORMAT .format(mCalendar.getTime())
						+ "-" + DateUtils.TOPIC_TIME_FORMAT.format(new Date(mCalendar.getTimeInMillis() + 60 * 60 * 1000)));
			}
        	
        }, mCalendar.get(Calendar.HOUR_OF_DAY), 0, true);
		mMyTimePickerDialog.show();
		
		if(mCalendar.get(Calendar.HOUR_OF_DAY) < 8 || mCalendar.get(Calendar.HOUR_OF_DAY) > 19)
			mMyTimePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setEnabled(false);
	}
	
	class MyTimePickerDialog extends TimePickerDialog {
		public MyTimePickerDialog(Context context,
				OnTimeSetListener callBack, int hourOfDay, int minute,
				boolean is24HourView) {
			super(context, callBack, hourOfDay, minute, is24HourView);
			if(hourOfDay < 8 || hourOfDay > 19) {
				if(mToast != null) {
					mToast.setText(R.string.select_time_out_of_service_tips);
				} else {					
					mToast = Toast.makeText(this.getContext(), R.string.select_time_out_of_service_tips, Toast.LENGTH_LONG);
				}
				mToast.show();
				//mMyTimePickerDialog.getButton(BUTTON_POSITIVE).setEnabled(false);
			}
		}

		@Override
		public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			if(minute != 0) {
				if(mToast != null) {
					mToast.setText(R.string.select_clock_tips);
				} else {					
					mToast = Toast.makeText(this.getContext(), R.string.select_clock_tips, Toast.LENGTH_LONG);
				}
				mToast.show();
				mMyTimePickerDialog.getButton(BUTTON_POSITIVE).setEnabled(false);
				return;
			} else {
				mMyTimePickerDialog.getButton(BUTTON_POSITIVE).setEnabled(true);
			}
			if(hourOfDay < 8 || hourOfDay > 19) {
				if(mToast != null) {
					mToast.setText(R.string.select_time_out_of_service_tips);
				} else {					
					mToast = Toast.makeText(this.getContext(), R.string.select_time_out_of_service_tips, Toast.LENGTH_LONG);
				}
				mToast.show();
				mMyTimePickerDialog.getButton(BUTTON_POSITIVE).setEnabled(false);
			} else {
				mMyTimePickerDialog.getButton(BUTTON_POSITIVE).setEnabled(true);
			}
		}
	}

	@Override
    public void setScanObjectAfterScan(InfoInterface barCodeObject) {
		 BaoxiuCardObject object = (BaoxiuCardObject) barCodeObject;
		//这里一般我们只设置品牌、型号、编号和名称
		/*if (!TextUtils.isEmpty(object.mLeiXin)) {
			mTypeInput.setText(object.mLeiXin);
		}
		if (!TextUtils.isEmpty(object.mPinPai)) {
			mPinpaiInput.setText(object.mPinPai);
		}
		if (!TextUtils.isEmpty(object.mSHBianHao)) {
			mBianhaoInput.setText(object.mSHBianHao);
		}
		if (!TextUtils.isEmpty(object.mXingHao)) {
			mModelInput.setText(object.mXingHao);
		}
		if (!TextUtils.isEmpty(object.mBXPhone)) {
			mBaoxiuTelInput.setText(object.mBXPhone);
		}*/
	}
	
	@Override
	public InfoInterface getScanObjectAfterScan() {
		return BaoxiuCardObject.getBaoxiuCardObject();
	}
	@Override
	public void updateInfoInterface(InfoInterface infoInterface) {
	}

	@Override
    public void updateArguments(Bundle args) {
	    
    }
}
