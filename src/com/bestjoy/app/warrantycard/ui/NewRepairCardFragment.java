package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.SpeechRecognizerEngine;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;

public class NewRepairCardFragment extends ModleBaseFragment implements View.OnClickListener{
	private static final String TAG = "NewRepairCardFragment";
	//按钮
	private Button mSaveBtn;
	//联系人信息
	//private EditText mContactNameInput, mContactTelInput;
	//private ProCityDisEditView mProCityDisEditView;
	//private HaierProCityDisEditPopView mProCityDisEditPopView;
	private BaoxiuCardObject mBaoxiuCardObject;
	private HomeObject mHomeObject;
	private AccountObject mAccountObject;
	//预约信息
	private TextView mYuyueDate, mYuyueTime;
	private TextView mProductNameView, mProductInfoVew, mAccountInfoView, mContactPlaceView;
	private Calendar mCalendar;
	
	private EditText mAskInput;
	//private Handler mHandler;
	private Button mSpeakButton;
	private ImageView mMoreInfoView;
	private SpeechRecognizerEngine mSpeechRecognizerEngine;
	
	private long mAid = -1;
	private long mUid = -1;
	private long mBid = -1;
	
	private ScrollView mScrollView;
	private RadioButton mRepairRadioButton;
	private CardViewFragment mContent;
	private TextView mMalfunctionBtn, mMaintenancePointBtn, mBuyMaintenanceComponentBtn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.activity_title_repair);
		mCalendar = Calendar.getInstance();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 View view = inflater.inflate(R.layout.activity_repair_20140518, container, false);
		 
		 mScrollView = (ScrollView) view.findViewById(R.id.scrollview);
		 
		 mMalfunctionBtn = (TextView) view.findViewById(R.id.button_malfunction);
         mMaintenancePointBtn = (TextView) view.findViewById(R.id.button_maintenance_point);
         mBuyMaintenanceComponentBtn = (TextView) view.findViewById(R.id.button_maintenance_componnet);
         mMalfunctionBtn.setOnClickListener(this);
         mMaintenancePointBtn.setOnClickListener(this);
         mBuyMaintenanceComponentBtn.setOnClickListener(this);
		 
		 //mProCityDisEditPopView = new HaierProCityDisEditPopView(this.getActivity(), view);
		 
		 mProductNameView = (TextView) view.findViewById(R.id.product_name);
		 mProductInfoVew = (TextView) view.findViewById(R.id.product_info);
		 mAccountInfoView = (TextView) view.findViewById(R.id.account_info);
		 mContactPlaceView = (TextView) view.findViewById(R.id.contact_place);
		 
		 //语音
		 mAskInput = (EditText) view.findViewById(R.id.product_ask_online_input);
		 mSpeakButton =  (Button) view.findViewById(R.id.button_speak);
		 mSpeakButton.setOnClickListener(this);
		 mSpeechRecognizerEngine = SpeechRecognizerEngine.getInstance(getActivity());
		 mSpeechRecognizerEngine.setResultText(mAskInput);
		 
		 mMoreInfoView = (ImageView) view.findViewById(R.id.image_more_info);
		 mMoreInfoView.setOnClickListener(this);
		 
		 mSaveBtn = (Button) view.findViewById(R.id.button_save);
		 mSaveBtn.setOnClickListener(this);
		 
		 mRepairRadioButton = (RadioButton) view.findViewById(R.id.radio_repair);

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
		 populateBaoxiuInfoView(mBaoxiuCardObject);
		 populateHomeInfoView(mHomeObject);
		 populateContactInfoView(mAccountObject);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	private void populateBaoxiuInfoView(BaoxiuCardObject baoxiuCardObject) {
		//init layouts
		mProductNameView.setText(baoxiuCardObject.mLeiXin);
		mProductInfoVew.setText(baoxiuCardObject.mPinPai + " " + baoxiuCardObject.mXingHao);
	}
	
	public void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject) {
	}
	
	public void populateHomeInfoView(HomeObject homeObject) {
		mContactPlaceView.setText(homeObject.mHomeProvince + homeObject.mHomeCity + homeObject.mHomeDis + homeObject.mHomePlaceDetail);
	}
	
    public void populateContactInfoView(AccountObject accountObject) {
    	mAccountInfoView.setText(accountObject.mAccountName + " " + accountObject.mAccountTel);
	}

	public BaoxiuCardObject getBaoxiuCardObject() {
		BaoxiuCardObject baoxiuCardObject = new BaoxiuCardObject();
		
		return baoxiuCardObject;
	}
	
	public AccountObject getContactInfoObject() {
		AccountObject contactInfoObject = new AccountObject();
		return contactInfoObject;
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
		case R.id.image_more_info:
			goBack();
			break;
		case R.id.button_save:
			createRepairCard();
			break;
		case R.id.menu_choose:
			//如果内容为空，我们显示侧边栏
			((NewCardActivity) getActivity()).getSlidingMenu().showMenu(true);
			break;
		case R.id.button_malfunction:
			break;
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
//    				ModleSettings.doChoose(getActivity(), ModleSettings.createMyInstallDefaultBundle(getActivity()));
//    			} else if (id == R.id.button_onekey_repair) {
//    				ModleSettings.doChoose(getActivity(), ModleSettings.createMyRepairDefaultBundle(getActivity()));
//    			}
    			
    			getActivity().finish();
	    	} else {
	    		new AlertDialog.Builder(getActivity())
		    	.setMessage(R.string.must_haier_confirm_yuyue)
		    	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!TextUtils.isEmpty(mBaoxiuCardObject.mBXPhone)) {
							Intents.callPhone(getActivity(), mBaoxiuCardObject.mBXPhone);
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

	private void goBack() {
		mContent = new CardViewFragment();
		BaoxiuCardObject.setBaoxiuCardObject(mBaoxiuCardObject);
		HomeObject.setHomeObject(mHomeObject);
		mContent.updateInfoInterface(mBaoxiuCardObject);
		mContent.updateInfoInterface(mHomeObject);
		getActivity().getSupportFragmentManager()
		.beginTransaction()
		.setCustomAnimations(R.anim.frag_fade_in, R.anim.frag_fade_out)
		.replace(R.id.content_frame, mContent)
		.commit();
	}

	private void createRepairCard() {
		if(MyAccountManager.getInstance().hasLoginned()) {
			//如果没有注册，我们前往登陆界面
			if(checkInput()) {
				createRepairCardAsync();
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

	private class CreateRepairCardAsyncTask extends AsyncTask<String, Void, Boolean> {
		private String mError;
		int mStatusCode = -1;
		String mStatusMessage = null;
		@Override
		protected Boolean doInBackground(String... params) {
			mError = null;
			InputStream is = null;
			final int LENGTH = 14;
			String[] urls = new String[LENGTH];
			String[] paths = new String[LENGTH];
			urls[0] = ServiceObject.SERVICE_URL + "20140514/NAddHaierYY.ashx?LeiXin=";//AddHaierYuyue.ashx
			paths[0] = mBaoxiuCardObject.mLeiXin;
			urls[1] = "&PinPai=";
			paths[1] = mBaoxiuCardObject.mPinPai;
			urls[2] = "&XingHao=";
			paths[2] = mBaoxiuCardObject.mXingHao;
			urls[3] = "&SHBianhao=";
			paths[3] = mBaoxiuCardObject.mSHBianHao;
			urls[4] = "&BxPhone=";
			paths[4] = mBaoxiuCardObject.mBXPhone;
			urls[5] = "&UserName=";
			paths[5] = MyAccountManager.getInstance().getAccountObject().mAccountName;
			urls[6] = "&Cell=";
			paths[6] = MyAccountManager.getInstance().getAccountObject().mAccountTel;
			urls[7] = "&address=";
			paths[7] = mHomeObject.mHomePlaceDetail;
			urls[8] = "&dstrictid=";
			paths[8] = HomeObject.getDisID(getActivity().getContentResolver(), mHomeObject.mHomeDis);
			urls[9] = "&yytime=";
			paths[9] = BaoxiuCardObject.BUY_DATE_FORMAT_YUYUE_TIME.format(mCalendar.getTime());
			urls[10] = "&Desc=";
			paths[10] = mAskInput.getText().toString().trim();
			urls[11] = "&service_type=";
			paths[11] = mRepairRadioButton.isChecked()?"T02":"T01";//T01为安装 T02为维修
			
			String timeStr = BaoxiuCardObject.DATE_FORMAT_YUYUE_TIME.format(new Date());
			String tip = BaoxiuCardObject.getYuyueSecurityTip(timeStr);
			urls[12] = "&tip=";
			paths[12] = tip;
			urls[13] = "&key=";
			paths[13] = BaoxiuCardObject.getYuyueSecurityKey(MyAccountManager.getInstance().getAccountObject().mAccountTel, timeStr);
			DebugUtils.logD(TAG, "urls = " + Arrays.toString(urls));
			DebugUtils.logD(TAG, "paths = " + Arrays.toString(paths));
			try {
				is = NetworkUtils.openContectionLocked(urls, paths, MyApplication.getInstance().getSecurityKeyValuesObject());
				try {
					JSONObject jsonObject = new JSONObject(NetworkUtils.getContentFromInput(is));
					mStatusCode = Integer.parseInt(jsonObject.getString("StatusCode"));
					mStatusMessage = jsonObject.getString("StatusMessage");
					DebugUtils.logD(TAG, "StatusCode = " + mStatusCode);
					DebugUtils.logD(TAG, "StatusMessage = " + mStatusMessage);
					if (mStatusCode == 1) {
						//操作成功
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mError = e.getMessage();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				mError = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				mError = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dissmissDialog(DIALOG_PROGRESS);
			if (mError != null) {
//				if (result) {
//					//服务器上传信息成功，但本地保存失败，请重新登录同步数据
//					new AlertDialog.Builder(getActivity())
//					.setTitle(R.string.msg_tip_title)
//		   			.setMessage(mError)
//		   			.setCancelable(false)
//		   			.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//		   				@Override
//		   				public void onClick(DialogInterface dialog, int which) {
//		   					LoginActivity.startIntent(getActivity(), null);
//		   				}
//		   			})
//		   			.create()
//		   			.show();
//				} else {
//					MyApplication.getInstance().showMessage(mError);
//				}
				MyApplication.getInstance().showMessage(mError);
			} else if (result) {
				//预约成功
				getActivity().finish();
				MyApplication.getInstance().showMessage(R.string.msg_yuyue_sucess);
				if (MyAccountManager.getInstance().hasBaoxiuCards()) {
					MyChooseDevicesActivity.startIntent(getActivity(), getArguments());
				}
				
			} else {
				MyApplication.getInstance().showMessage(mStatusMessage);
			}
			
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
		if(TextUtils.isEmpty(mAskInput.getText().toString().trim())){
			showEmptyInputToast(R.string.error_des);
			return false;
		}

		if(!timeEscapeEnough()) {
			MyApplication.getInstance().showMessage(R.string.yuyue_time_too_early_tips);
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


	private void showDatePickerDialog() {
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				mCalendar.set(year, monthOfYear, dayOfMonth);
				//更新日期数据
//				mGoodsObject.mDate = mCalendar.getTimeInMillis();
				//更新UI
				mYuyueDate.setText(DateUtils.TOPIC_DATE_TIME_FORMAT.format(mCalendar.getTime()));
			}
				
		}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH))
		.show();
	}

	Toast mToast;
	MyTimePickerDialog mMyTimePickerDialog;
	private void showTimePickerDialog() {
		mMyTimePickerDialog = new MyTimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute);
				mYuyueTime.setText(DateUtils.TOPIC_TIME_FORMAT.format(mCalendar.getTime())
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
		if (infoInterface instanceof BaoxiuCardObject) {
			if (infoInterface != null) {
				mBid = ((BaoxiuCardObject)infoInterface).mBID;
				mAid = ((BaoxiuCardObject)infoInterface).mAID;
				mUid = ((BaoxiuCardObject)infoInterface).mUID;
				mBaoxiuCardObject = (BaoxiuCardObject)infoInterface;
			}
		} else if (infoInterface instanceof HomeObject) {
			if (infoInterface != null) {
				mHomeObject = (HomeObject)infoInterface;
				long aid = mHomeObject.mHomeAid;
				if (aid > 0) {
					mAid = aid;
				}
			}
		} else if (infoInterface instanceof AccountObject) {
			if (infoInterface != null) {
				long uid = ((AccountObject)infoInterface).mAccountUid;
				if (uid > 0) {
					mUid = uid;
				}
				mAccountObject = (AccountObject)infoInterface;
			}
		}
	}
}