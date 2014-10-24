package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.internal.widget.IcsLinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.BDLocation;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.propertymanagement.PropertyManagementActivity;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.DeviceDBHelper;
import com.bestjoy.app.warrantycard.service.IMService;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2.TaskType;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager.LocationChangeCallback;
import com.bestjoy.app.warrantycard.utils.CodeConstants;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.JsonParser;
import com.bestjoy.app.warrantycard.utils.MenuHandlerUtils;
import com.bestjoy.app.warrantycard.utils.SpeechRecognizerEngine;
import com.bestjoy.app.warrantycard.utils.WeatherManager;
import com.bestjoy.app.warrantycard.utils.WeatherManager.WeatherObject;
import com.bestjoy.app.warrantycard.utils.WeatherManager.WeekWeather;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.BitmapUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.FilesUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;

public class HomePageFragment extends BaseFragment implements View.OnClickListener {
	public static final String TAG = "HomeFragment";
	private LinearLayout mDotsLayout;
	private IcsLinearLayout mWeatherLayout;
	private ViewPager mAdsViewPager;
	private boolean mAdsViewPagerIsScrolling = false;
	
	private LocationChangeCallback mLocationChangeCallback;
	
	private int[] mAddsDrawableId = new int[]{
			R.drawable.ad1,
			R.drawable.ad2,
			R.drawable.ad3,
	};
	
	private Drawable[] mDotDrawableArray;
	private Bitmap[] mAdsBitmaps;
	
	private ImageView[] mDotsViews = null;
	private ImageView[] mAdsPagerViews = null;
	private int mCurrentPagerIndex = 0;
	private static final int DEFAULT_MAX_ADS_SIZE = 3;
	
	private Handler mHandler;
	
	private View mContent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PhotoManagerUtilsV2.getInstance().requestToken(TAG);
		mHandler = new Handler();
		this.setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().invalidateOptionsMenu();
		changeAdsDelay();
	}
	
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initVoiceLayout(mContent);
		if (!WeatherManager.getInstance().isOldCahcedWeatherFile()) {
			DebugUtils.logD(TAG, "onActivityCreated loadCachedWeatherFile");
			loadWeatherAsync();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContent = inflater.inflate(R.layout.home_fragment, container, false);
		mDotsLayout = (LinearLayout) mContent.findViewById(R.id.dots);
		mAdsViewPager = (ViewPager) mContent.findViewById(R.id.adsViewPager);
		this.initViewPagers(3);
		this.initDots(3);
		mAdsViewPager.setAdapter(new AdsViewPagerAdapter());
		
		mAdsViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (mCurrentPagerIndex != position) {
					mDotsViews[mCurrentPagerIndex].setImageDrawable(mDotDrawableArray[0]);
					mDotsViews[position].setImageDrawable(mDotDrawableArray[1]);
					mCurrentPagerIndex = position;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				mAdsViewPagerIsScrolling  = state == 1;
				
			}
		});
		
		 View weatherLayout = mContent.findViewById(R.id.weather_layout);
		if (weatherLayout != null) {
			mWeatherLayout = (IcsLinearLayout) weatherLayout;
			mWeatherLayout.removeAllViews();
			mLocationChangeCallback = new MyLocationChangeCallback();
			BaiduLocationManager.getInstance().addLocationChangeCallback(mLocationChangeCallback);
			BaiduLocationManager.getInstance().mLocationClient.start();
			BaiduLocationManager.getInstance().mLocationClient.requestLocation();
		}
		
		mContent.findViewById(R.id.ic_module_baoxiucard).setOnClickListener(this);
		mContent.findViewById(R.id.ic_module_home).setOnClickListener(this);
		mContent.findViewById(R.id.ic_module_car).setOnClickListener(this);
		mContent.findViewById(R.id.ic_module_vip).setOnClickListener(this);
		return mContent;
	}

	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mChangeAdsRunnable);
	}
	
	/**
	 * 目前语音识别对于火车字母+2位数字的车次，会将数字识别成大写，这里做了一个映射
	 */
	private static final HashMap<Character, Character> CHARS_FOR_TRAIN = new HashMap<Character, Character>();
	static {
		CHARS_FOR_TRAIN.put('一', '1');
		CHARS_FOR_TRAIN.put('二', '2');
		CHARS_FOR_TRAIN.put('三', '3');
		CHARS_FOR_TRAIN.put('四', '4');
		CHARS_FOR_TRAIN.put('五', '5');
		CHARS_FOR_TRAIN.put('六', '6');
		CHARS_FOR_TRAIN.put('七', '7');
		CHARS_FOR_TRAIN.put('八', '8');
		CHARS_FOR_TRAIN.put('九', '9');
	}
	
	private String[] mVoiceXinghaoModificationSrc, mVoiceXinghaoModificationDes, mVoiceXinghaoModificationDesTel;;
	private View mVoiceInputPopLayout;
	private TextView mVoiceInputStatus;
	private EditText mAskInput;
	private ImageView mVoiceBtn, mVoiceImage;
	private SpeechRecognizerEngine mSpeechRecognizerEngine;
	private VoiceButtonTouchListener mVoiceButtonTouchListener;
	private String[] mVoiceKeepKeys = null;
	private String[] mVoiceTrainSupport = null;
	private void initVoiceLayout(View content) {
		mVoiceKeepKeys = getActivity().getResources().getStringArray(R.array.voice_kepp_keys);
		mVoiceXinghaoModificationSrc = getActivity().getResources().getStringArray(R.array.xinghao_modification_src);
		mVoiceXinghaoModificationDes = getActivity().getResources().getStringArray(R.array.xinghao_modification_des);
		mVoiceXinghaoModificationDesTel = getActivity().getResources().getStringArray(R.array.xinghao_modification_des_tel);
		
		mVoiceTrainSupport = getActivity().getResources().getStringArray(R.array.voice_train_support);
		mVoiceInputPopLayout = content.findViewById(R.id.voice_input_layout);
		mVoiceInputPopLayout.setVisibility(View.GONE);
		mVoiceBtn = (ImageView) content.findViewById(R.id.button_voice);
		mVoiceInputStatus = (TextView) content.findViewById(R.id.voice_input_status);
		mVoiceButtonTouchListener = new VoiceButtonTouchListener();
		mVoiceBtn.setOnTouchListener(mVoiceButtonTouchListener);
		
		mAskInput = (EditText) content.findViewById(R.id.voice_input_confirm);
		
		mVoiceImage = (ImageView) content.findViewById(R.id.voice_input_imageview);
		mSpeechRecognizerEngine = SpeechRecognizerEngine.getInstance(getActivity());
		
	}
	
	private int getModifiedXinghaoIndex(String query) {
		int index = 0;
		for(String key:mVoiceXinghaoModificationSrc) {
			if (query.contains(key)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	private RecognizerListener mRecognizerListener = new RecognizerListener() {
		public boolean _isCanceled = false;
		
		@Override
		public void onBeginOfSpeech() {
			mAskInput.setHint(R.string.hint_voice_input);
			DebugUtils.logD(TAG, "chenkai onBeginOfSpeech");
			
		}
	
		@Override
		public void onEndOfSpeech() {
			mAskInput.setHint(R.string.hint_voice_input_wait);
			DebugUtils.logD(TAG, "chenkai onEndOfSpeech");
			mSpeechRecognizerEngine.stopListen();
		}
	
		@Override
		public void onError(SpeechError err) {
			//MyApplication.getInstance().showMessage(err.getPlainDescription(true));
			DebugUtils.logD(TAG, "chenkai onError " + err.getPlainDescription(true));
			if (mVoiceInputPopLayout.getVisibility() == View.VISIBLE) {
				mVoiceInputPopLayout.setVisibility(View.GONE);
			}
		}
	
		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			
			String text = JsonParser.parseIatResult(results.getResultString());
			DebugUtils.logD(TAG, "chenkai onResult " + text);
			if (!TextUtils.isEmpty(text)) {
				//BeepAndVibrate.getInstance().playBeepSoundAndVibrate();
				mAskInput.append(text);
				mAskInput.setSelection(mAskInput.length());
				if (isLast) {
					mVoiceInputStatus.setText(R.string.msg_check_voice_input_by_up);
					if (mVoiceInputPopLayout.getVisibility() == View.VISIBLE && !mVoiceButtonTouchListener._isCanceled && mVoiceButtonTouchListener._isUp) {
						mVoiceInputPopLayout.setVisibility(View.GONE);
						doVoiceQuery(text, true);
					}
				}
				
			}
		}
	
		@Override
		public void onVolumeChanged(int volume) {
			mVoiceImage.setImageLevel(volume);
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	private class VoiceButtonTouchListener implements View.OnTouchListener{
		private float _downX = 0.0f;
		private float _downY = 0.0f;
		private boolean _isCanceled = false;
		private boolean _isUp = false;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				_isCanceled = false;
				_isUp = false;
				mVoiceInputPopLayout.setVisibility(View.VISIBLE);
				_downX = event.getX();
				_downY = event.getY();
				mVoiceInputStatus.setText(R.string.msg_cancel_voice_input_down);
				mAskInput.setText("");
				mAskInput.setHint(R.string.hint_voice_input);
				mVoiceImage.setImageResource(R.drawable.voice_input_listen);
				mSpeechRecognizerEngine.stopListen();
				mSpeechRecognizerEngine.startListen(mRecognizerListener);
				break;
			case MotionEvent.ACTION_MOVE:
				float moveX = event.getX();
				float moveY = event.getY();
				if (_downY - moveY > 100) {
					_isCanceled = true;
					mVoiceInputStatus.setText(R.string.msg_cancel_voice_input_by_cancel);
					mSpeechRecognizerEngine.stopListen();
					mSpeechRecognizerEngine.cancel();
					mVoiceImage.setImageResource(R.drawable.voice_cancel);
					mAskInput.setText("");
					mAskInput.setHint(R.string.hint_voice_input_canceled);
				} else if (_isCanceled){
					_isCanceled = false;
					mVoiceInputStatus.setText(R.string.msg_cancel_voice_input_down);
					mAskInput.setHint(R.string.hint_voice_input);
					mSpeechRecognizerEngine.stopListen();
					mSpeechRecognizerEngine.startListen(mRecognizerListener);
					mVoiceImage.setImageResource(R.drawable.voice_input_listen);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				_isCanceled = true;
				mVoiceInputPopLayout.setVisibility(View.GONE);
				mVoiceInputStatus.setText(R.string.msg_cancel_voice_input_by_cancel);
				mSpeechRecognizerEngine.stopListen();
				break;
			case MotionEvent.ACTION_UP:
				_isUp = true;
				String query = mAskInput.getText().toString().trim();
				mSpeechRecognizerEngine.stopListen();
				if (_isCanceled) {
					mVoiceInputPopLayout.setVisibility(View.GONE);
					mSpeechRecognizerEngine.cancel();
				} else if (query.length() > 0) {
					mVoiceInputPopLayout.setVisibility(View.GONE);
					doVoiceQuery(query, true);
				}
				break;
			}
			return false;
		}
	}
	
	private void doVoiceQuery(String query, boolean needCheck) {
		DebugUtils.logD(TAG, "doVoiceQuery() query=" + query);
		
		AsyncTaskUtils.cancelTask(mVoiceQueryTask);
		mVoiceQueryTask = new VoiceQueryTask(query, needCheck);
		mVoiceQueryTask.execute();
		//MyApplication.getInstance().showMessage(getString(R.string.msg_query_pinpai_wait_format, query));
	}
	private VoiceQueryTask mVoiceQueryTask;
	private class VoiceQueryTask extends AsyncTask<String, Void, Integer> {

		private String _pinpai, _bxPhone;
		private String _query;
		private boolean _needCheck = false;
		
		private VoiceQueryTask (String query, boolean needCheck) {
			_query = query;
			_needCheck = needCheck;
		}
		@Override
		protected Integer doInBackground(String... params) {
			DebugUtils.logD(TAG, "VoiceQueryTask.doInBackground()");
			//如果是保留字，我们暂时不支持
			for(String keepKey : mVoiceKeepKeys) {
				if (_query.contains(keepKey)) {
					DebugUtils.logD(TAG, "query contains keppKey " + keepKey + ", so do nothing.");
					return CodeConstants.NOT_SUPPORT;
				}
			}
			_query = _query.replaceAll(" ", "");
			DebugUtils.logD(TAG, "doVoiceQuery() start query-replaced " + _query);
			//首先检索品牌
			StringBuilder sb = new StringBuilder(DeviceDBHelper.DEVICE_PINPAI_NAME);
			sb.append(" like ").append("'%").append(_query).append("%'");
			DebugUtils.logD(TAG, "pinpai selection " + sb.toString());
			Cursor c = getActivity().getContentResolver().query(BjnoteContent.PinPai.CONTENT_URI, NewCardChooseFragment.PINPAI_PROJECTION, sb.toString(), null, null);
			if (c != null) {
				if (c.moveToNext()) {
					_pinpai = c.getString(1);
					_bxPhone = c.getString(6);
					DebugUtils.logD(TAG, "find pinpai=" + _pinpai + ", bxphone=" + _bxPhone);
					return CodeConstants.PINPAI;
				}
				c.close();
			}
			int pinpaiIndex = getModifiedXinghaoIndex(_query);
			if (pinpaiIndex != -1) {
				_pinpai = mVoiceXinghaoModificationDes[pinpaiIndex];
				_bxPhone = mVoiceXinghaoModificationDesTel[pinpaiIndex];
				return CodeConstants.PINPAI;
			}
			
			//猜测是否是火车
			if (_query.length() > 1) {
				char firstChar = _query.charAt(0);
				boolean isSupport = false;
				for (String support : mVoiceTrainSupport) {
					if (support.charAt(0) == firstChar) {
						isSupport = true;
						break;
					}
				}
				if (isSupport) {
					StringBuilder newSb = new StringBuilder();
					newSb.append(firstChar);
					String checi = _query.substring(1);
					char cr = 0;
					char findReplace = 0;
					for (int index=0; index < checi.length(); index++) {
						cr = checi.charAt(index);
						for(char key:CHARS_FOR_TRAIN.keySet()) {
							if (cr == key) {
								findReplace = CHARS_FOR_TRAIN.get(key);
								break;
							}
						}
						if (findReplace != 0) {
							newSb.append(findReplace);
						} else {
							newSb.append(cr);
						}
					}
					_query = newSb.toString();

					if (TextUtils.isDigitsOnly(_query.substring(1))) {
						_query = _query.replace(mVoiceTrainSupport[0], "G");
						_query = _query.replace(mVoiceTrainSupport[1], "D");
						_query = _query.replace(mVoiceTrainSupport[2], "T");
						return CodeConstants.TRAIN;
					}
				}
			}
			
			//猜测是否是飞机航班
			if (_query.length() >= 4) {
				if (_query.startsWith("h2")) {
					_query = _query.replace("h2", "h");//吉祥航空识别的时候h会识别成h2
				}
				if ( _query.matches("[0-9a-zA-Z]{2}\\d{2,6}") && TextUtils.isDigitsOnly(_query.substring(2))) {
					return CodeConstants.AIRPLANE;
				}
			}
			
			return _needCheck?CodeConstants.NOT_FOUND:CodeConstants.NOT_SUPPORT;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			switch(result) {
			case CodeConstants.NOT_SUPPORT:
				MyApplication.getInstance().showMessage(getString(R.string.msg_query_no_support_format, _query));
				break;
			case CodeConstants.PINPAI:
				if (!TextUtils.isEmpty(_pinpai)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setMessage(_pinpai+" " + _bxPhone);
					
					if (!TextUtils.isEmpty(_bxPhone)) {
						builder.setPositiveButton(R.string.text_call, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intents.callPhone(getActivity(), _bxPhone);
								
							}
						});
					}
					builder.setNegativeButton(android.R.string.cancel, null)
					.show();
				} 
				break;
			case CodeConstants.TRAIN:
				if (!TextUtils.isEmpty(_query)) {
					new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.tip_find_train, _query))
					.setPositiveButton(R.string.text_train, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intents.openURL(getActivity(), CodeConstants.getTrainUrl(_query));
								
							}
						})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
				} 
				break;
			case CodeConstants.AIRPLANE:
				if (!TextUtils.isEmpty(_query)) {
					new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.tip_find_flightno, _query))
					.setPositiveButton(R.string.text_flightno, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intents.openURL(getActivity(), CodeConstants.getAirPlaneUrl(_query));
								
							}
						})
					.setNegativeButton(android.R.string.cancel, null)
					.show();
				} 
				break;
			case CodeConstants.NOT_FOUND:
				//没有找到，我们猜测是否是
				if (!TextUtils.isEmpty(_query)) {
					showCheckVoiceDialog(_query);
				}
				break;
			}
			
			
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	
	private void showCheckVoiceDialog(String query) {
		final EditText input = new EditText(getActivity());
		input.setText(query);
		input.setSelection(query.length());
		final AlertDialog dialog = new AlertDialog.Builder(getActivity())
		.setMessage(R.string.text_check_voice_title)
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					doVoiceQuery(input.getText().toString().trim(), false);
					
				}
			})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
			}
			
		});
		
		dialog.show();
	}
	
	 @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
  	     super.onCreateOptionsMenu(menu, inflater);
  	     MenuHandlerUtils.onCreateOptionsMenu(menu);
  	     MenuItem subMenu1Item = menu.findItem(R.string.menu_more);
  	     subMenu1Item.getSubMenu().add(1000, R.string.menu_refresh, 2005, R.string.menu_refresh);
  	     subMenu1Item.getSubMenu().add(1000, R.string.menu_exit, 2006, R.string.menu_exit);
     }
	 
	 public void onPrepareOptionsMenu(Menu menu) {
		 MenuHandlerUtils.onPrepareOptionsMenu(menu, getActivity());
		 menu.findItem(R.string.menu_exit).setVisible(MyAccountManager.getInstance().hasLoginned());
		 menu.findItem(R.string.menu_refresh).setVisible(MyAccountManager.getInstance().hasLoginned());
	 }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem menuItem) {
		 switch(menuItem.getItemId()) {
		 case R.string.menu_exit:
			 new AlertDialog.Builder(getActivity())
				.setMessage(R.string.msg_existing_system_confirm)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteAccountAsync();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
			 return true;
		 case R.string.menu_refresh:
			 if (MyAccountManager.getInstance().hasLoginned()) {
				 //做一次登陆操作
				 //目前只删除本地的所有缓存文件
//				try{
//			        Runtime.getRuntime().exec("adb shell pm clear " + MyApplication.PKG_NAME);
//		        } catch(IOException ex) {
//		            ex.printStackTrace();
//		        }
				 File dir = MyApplication.getInstance().getCachedXinghaoInternalRoot();
				 FilesUtils.deleteFile("Updating ", dir);
				 
				 dir = MyApplication.getInstance().getCachedXinghaoExternalRoot();
				 if (dir != null) {
					 FilesUtils.deleteFile("Updating ", dir);
				 }
			 }
			 break;
		 }
		 return MenuHandlerUtils.onOptionsItemSelected(menuItem, getActivity());
	 }
	 
	 private DeleteAccountTask mDeleteAccountTask;
	 private void deleteAccountAsync() {
		 AsyncTaskUtils.cancelTask(mDeleteAccountTask);
		 showDialog(DIALOG_PROGRESS);
		 mDeleteAccountTask = new DeleteAccountTask();
		 mDeleteAccountTask.execute();
	 }
	 private class DeleteAccountTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			IMService.disconnectIMService(getActivity(), MyAccountManager.getInstance().getAccountObject());
			MyAccountManager.getInstance().deleteDefaultAccount();
			MyAccountManager.getInstance().saveLastUsrTel("");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			getActivity().invalidateOptionsMenu();
			MyApplication.getInstance().showMessage(R.string.msg_op_successed);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			getActivity().invalidateOptionsMenu();
			dismissDialog(DIALOG_PROGRESS);
		}
		
		
		 
	 }
	 


	private void initDots(int count){
		LayoutInflater flater = getActivity().getLayoutInflater();
		if (mDotDrawableArray == null) {
			mDotDrawableArray = new Drawable[2];
			mDotDrawableArray[0] = this.getResources().getDrawable(R.drawable.dot);
			mDotDrawableArray[1] = this.getResources().getDrawable(R.drawable.dot_on);
		}
		mDotsViews = new ImageView[count];
		for (int j = 0; j < count; j++) {
			mDotsViews[j] = (ImageView) flater.inflate(R.layout.dot, mDotsLayout, false);
			if (mCurrentPagerIndex == j) {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[1]);
			} else {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[0]);
			}
			mDotsLayout.addView(mDotsViews[j]);
		}
	}
	
	private void initAdsBitmap() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (mAdsBitmaps == null) {
			mAdsBitmaps = new Bitmap[DEFAULT_MAX_ADS_SIZE];
		} else {
			for(Bitmap bitmap:mAdsBitmaps) {
				bitmap.recycle();
			}
		}
		int adsW = getResources().getDimensionPixelSize(R.dimen.ads_width);
		int adsH = getResources().getDimensionPixelSize(R.dimen.ads_height);
		mAdsBitmaps = BitmapUtils.getSuitedBitmaps(getActivity(), mAddsDrawableId, 800, 1024);
	}
	
	private void initViewPagers(int count) {
		initAdsBitmap();
		mAdsPagerViews = new ImageView[count];
		LayoutInflater flater = getActivity().getLayoutInflater();
		for (int j = 0; j < count; j++) {
			mAdsPagerViews[j] = (ImageView) flater.inflate(R.layout.ads, null, false);
			mAdsPagerViews[j].setImageBitmap(mAdsBitmaps[j]);
		}
	}
	
	class AdsViewPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return mAdsPagerViews.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		private View getView(ViewGroup container, int position) {
			container.addView(mAdsPagerViews[position]);
			return mAdsPagerViews[position];
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return getView(container, position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mAdsPagerViews[position]);
		}
		
	}
	
	private void changeAdsDelay() {
		mHandler.postDelayed(mChangeAdsRunnable, DEFAULT_DELAY);
	}
	private static long DEFAULT_DELAY = 5000;
	private ChangeAdsRunnable mChangeAdsRunnable = new ChangeAdsRunnable();
	private class ChangeAdsRunnable implements Runnable {
		@Override
		public void run() {
			if (!mAdsViewPagerIsScrolling) {
				int pageCount = mAdsViewPager.getAdapter().getCount();
				int nextPage = mCurrentPagerIndex % pageCount + 1;
				if (nextPage >= pageCount) {
					nextPage = 0;
				}
				mAdsViewPager.setCurrentItem(nextPage);
				
			}
			mHandler.postDelayed(this, DEFAULT_DELAY);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ic_module_baoxiucard:
		case R.id.ic_module_home://物业卡
			//判断有没有登陆，没有的话提示登录
			if (!MyAccountManager.getInstance().hasLoginned()) {
				 LoginActivity.startIntent(getActivity(), null);
				 MyApplication.getInstance().showNeedLoginMessage();
				 return;
			} else {
				//如果已经登录，判断是否是演示账户，是的话显示演示家
				if (MyAccountManager.getInstance().getAccountObject().isDemoAccountObject()) {
					//LoginActivity.startIntent(this, null);
					//MyApplication.getInstance().showNeedLoginMessage();
					boolean needLoadDemo = MyApplication.getInstance().mPreferManager.getBoolean("need_load_demo_home", true);
					if (needLoadDemo) {
						//如果是第一次，我们需要拉取演示家数据
						if (!ComConnectivityManager.getInstance().isConnected()) {
							//没有联网，这里提示用户
							ComConnectivityManager.getInstance().onCreateNoNetworkDialog(getActivity()).show();
						} else {
							new AlertDialog.Builder(getActivity())
							.setMessage(R.string.msg_need_to_get_demo_data)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									loadDemoCardDataAsync();
								}
							})
							.setNegativeButton(android.R.string.cancel, null)
							.show();
						}
						return;
					}
				}
			}
			//判断是否有家，没有的话，就要去新建一个家
			if (!MyAccountManager.getInstance().hasHomes()) {
				Bundle bundles = ModleSettings.createHomeCommunityBundle(getActivity());
				bundles.putLong("aid", -1);
				bundles.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
				MyApplication.getInstance().showNeedHomeMessage();
				NewHomeActivity.startActivity(getActivity(), bundles);
				return;
			}
			//判断是否有保修卡
			switch(v.getId()) {
			case R.id.ic_module_home:
				List<HomeObject> communities = HomeObject.getAllCommunities();
				if (communities.size() == 1) {
					//直接进入我得小区
					Bundle bundle = ModleSettings.createPickCommunityBundle(getActivity());
					bundle.putString(Intents.EXTRA_NAME, getActivity().getString(R.string.activity_title_my_homecommunity));
					bundle.putLong("aid", communities.get(0).mHomeAid);
					bundle.putLong("uid", communities.get(0).mHomeUid);
					PropertyManagementActivity.startActivity(getActivity(), bundle);
				} else {
					//没有小区或是有多个小区，我们需要前往家列表进行选择
					Bundle bundle = ModleSettings.createPickCommunityBundle(getActivity());
					bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					HomeManagerActivity.startActivity(getActivity(), bundle);
				}
				break;
			case R.id.ic_module_baoxiucard:
				if (MyAccountManager.getInstance().hasBaoxiuCards()) {
					Bundle bundle = ModleSettings.createMyCardDefaultBundle(getActivity());
					bundle.putLong("aid", MyAccountManager.getInstance().getHomeAIdAtPosition(0));
					bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					MyChooseDevicesActivity.startIntent(getActivity(), bundle);
				} else {
					Bundle bundle = ModleSettings.createMyCardDefaultBundle(getActivity());
					bundle.putLong("aid", MyAccountManager.getInstance().getHomeAIdAtPosition(0));
					bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					NewCardActivity.startIntent(getActivity(), bundle);
				}
				break;
			}
			break;
		case R.id.button_telecontrol:
			MyApplication.getInstance().showUnsupportMessage();
			break;
		case R.id.button_qr_scan:
			Intent scanIntent = new Intent(getActivity(), CaptureActivity.class);
			startActivity(scanIntent);
			break;
		}
		
	}
	private LoadtDemoCardDataTask mLoadtDemoCardDataTask;
	public void loadDemoCardDataAsync() {
		showDialog(DIALOG_PROGRESS);
		AsyncTaskUtils.cancelTask(mLoadtDemoCardDataTask);
		mLoadtDemoCardDataTask = new LoadtDemoCardDataTask();
		mLoadtDemoCardDataTask.execute();
	}
	private class LoadtDemoCardDataTask extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
        protected ServiceResultObject doInBackground(Void... params) {
			ContentResolver cr = getActivity().getContentResolver();
			if (!MyAccountManager.getInstance().hasHomes()) {
				//如果没有家，我们创建演示家
				HomeObject homeObject = HomeObject.getDemoHomeObject(AccountObject.DEMO_ACCOUNT_UID, HomeObject.DEMO_HOME_AID);
				DebugUtils.logD(TAG, "LoadtDemoCardDataTask.doInBackground() start to insert HomeObject demo " + homeObject.toString());
				if (homeObject.saveInDatebase(cr, null)) {
					DebugUtils.logD(TAG, "initAccountHomes");
					MyAccountManager.getInstance().initAccountHomes();
				}
			}
			ServiceResultObject serviceObject = new ServiceResultObject();
	        //http://www.dzbxk.com/bestjoy/GetBaoXiuDataByUID.ashx?UID=351356&AID=353766
			StringBuilder sb = new StringBuilder("http://www.dzbxk.com/bestjoy/GetBaoXiuDataByUID.ashx?");
			sb.append("UID=").append(AccountObject.DEMO_ACCOUNT_UID)
			.append("&AID=").append(HomeObject.DEMO_HOME_AID);
			InputStream is = null;
			try {
	            is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
	            if (is != null) {
	            	serviceObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
	            	if (serviceObject.isOpSuccessfully()) {
	            		if (serviceObject.mStrData != null) {
	            			JSONArray baoxiuCards = new JSONArray(serviceObject.mStrData);
	            			//JSONArray baoxiuCards = serviceObject.mJsonData.getJSONArray(BaoxiuCardObject.JSONOBJECT_NAME);
		            		int len = baoxiuCards.length();
		            		BaoxiuCardObject baoxiuCardObject = null;
		            		for(int index=0; index < len; index++) {
		            			baoxiuCardObject = BaoxiuCardObject.parseBaoxiuCards(baoxiuCards.getJSONObject(index), null);
		            			baoxiuCardObject.mAID = HomeObject.DEMO_HOME_AID;
		            			baoxiuCardObject.mUID = AccountObject.DEMO_ACCOUNT_UID;
		            			baoxiuCardObject.saveInDatebase(cr, null);
		            		}
		            		MyAccountManager.getInstance().updateHomeObject(HomeObject.DEMO_HOME_AID);
	            		} else {
	            			serviceObject.mStatusMessage = getActivity().getString(R.string.msg_get_no_demo_data);
	            		}
	            		
	            	}
	            }
            } catch (ClientProtocolException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = e.getMessage();
            } catch (IOException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
            } catch (JSONException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = e.getMessage();
            } finally {
            	NetworkUtils.closeInputStream(is);
            }
	        return serviceObject;
        }

		@Override
        protected void onPostExecute(ServiceResultObject result) {
	        super.onPostExecute(result);
	        dismissDialog(DIALOG_PROGRESS);
	        if (result.isOpSuccessfully()) {
	        	//标识下次不用拉取演示数据了
	        	MyApplication.getInstance().mPreferManager.edit().putBoolean("need_load_demo_home", false).commit();
	        	//判断是否有家，没有的话，就要去新建一个家
				if (!MyAccountManager.getInstance().hasHomes()) {
					Bundle bundles = ModleSettings.createHomeCommunityBundle(getActivity());
					bundles.putLong("aid", -1);
					bundles.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					MyApplication.getInstance().showNeedHomeMessage();
					NewHomeActivity.startActivity(getActivity(), bundles);
					return;
				}
				//判断是否有保修卡
				if (MyAccountManager.getInstance().hasBaoxiuCards()) {
					Bundle newBundle = ModleSettings.createMyCardDefaultBundle(getActivity());
					newBundle.putLong("aid", MyAccountManager.getInstance().getAccountObject().mAccountHomes.get(0).mHomeAid);
					newBundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					MyChooseDevicesActivity.startIntent(getActivity(), newBundle);
				} else {
					Bundle newBundle = ModleSettings.createMyCardDefaultBundle(getActivity());
					newBundle.putLong("aid", MyAccountManager.getInstance().getAccountObject().mAccountHomes.get(0).mHomeAid);
					newBundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					NewCardActivity.startIntent(getActivity(), newBundle);
				}
	        } else {
	        	MyApplication.getInstance().showMessage(result.mStatusMessage);
	        }
        }

		@Override
        protected void onCancelled() {
	        super.onCancelled();
	        dismissDialog(DIALOG_PROGRESS);
        }
		
		
		
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mContent = null;
		YouMengMessageHelper.getInstance().cancelCheckDeviceTokenTask();
		BaiduLocationManager.getInstance().mLocationClient.stop();
		BaiduLocationManager.getInstance().removeLocationChangeCallback(mLocationChangeCallback);
		PhotoManagerUtilsV2.getInstance().releaseToken(TAG);
	}
	
	
	
	private class MyLocationChangeCallback implements LocationChangeCallback {

		@Override
		public boolean isLocationChanged(BDLocation location) {
			BaiduLocationManager.getInstance().setScanSpan(60*1000);//60秒请求一次定位数据
			String adminCode = HomeObject.getDisID(getActivity().getContentResolver(), location.getProvince().replaceAll("[省市]", ""), location.getCity().replaceAll("[省市]", ""), location.getDistrict());
			if (!TextUtils.isEmpty(adminCode)) {
				String lastAdminCode = ComPreferencesManager.getInstance().mPreferManager.getString("admincode", "");
				if (!lastAdminCode.equals(adminCode) 
						|| WeatherManager.getInstance().isOldCahcedWeatherFile()) {
					ComPreferencesManager.getInstance().mPreferManager.edit().putString("admincode", adminCode).commit();
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean onLocationChanged(BDLocation location) {
			updateWeatherAsync(ComPreferencesManager.getInstance().mPreferManager.getString("admincode", ""));
			return true;
		}
		
	}
	
	private UpdateWeatherTask mUpdateWeatherTask;
	private void updateWeatherAsync(String adminCode) {
		AsyncTaskUtils.cancelTask(mUpdateWeatherTask);
		mUpdateWeatherTask = new UpdateWeatherTask();
		mUpdateWeatherTask.execute(adminCode);
	}
	//下载天气数据
	private class UpdateWeatherTask extends AsyncTask<String, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(String... params) {
			ServiceResultObject result = new ServiceResultObject();
			InputStream is = null;
			try {
				is = NetworkUtils.openContectionLocked(ServiceObject.getWeatherUrl(params[0]), MyApplication.getInstance().getSecurityKeyValuesObject());
				boolean saved = WeatherManager.getInstance().saveWeather(is);
				DebugUtils.logD(TAG, "UpdateWeatherTask saveWeather " + saved);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				result.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				result.mStatusMessage = e.getMessage();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return result;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			//显示数据
			loadWeatherAsync();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	
	
	private LoadWeatherTask mLoadWeatherTask;
	private void loadWeatherAsync() {
		AsyncTaskUtils.cancelTask(mLoadWeatherTask);
		mLoadWeatherTask = new LoadWeatherTask();
		mLoadWeatherTask.execute();
	}
	private class LoadWeatherTask extends AsyncTask<Void, Void, WeekWeather> {

		@Override
		protected WeekWeather doInBackground(Void... params) {
			try {
				String content = NetworkUtils.getContentFromInput(new FileInputStream(WeatherManager.getInstance().getCachedWeatherFile()));
				ServiceResultObject result = ServiceResultObject.parseArray(content);
				if (result.isOpSuccessfully() && result.mJsonArray != null) {
					return WeatherManager.getInstance().getWeekWeather(result.mJsonArray);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(WeekWeather result) {
			super.onPostExecute(result);
			mWeatherLayout.removeAllViews();
			if (getActivity() == null) {
				return;
			}
			if (result == null) {
				mWeatherLayout.setVisibility(View.GONE);
			} else {
				//add weather items
				for(final WeatherObject weatherObject : result.mWeatherObjectList) {
					View weatherItem = getActivity().getLayoutInflater().inflate(R.layout.weather_item, mWeatherLayout, false);
					//加载星期数
					TextView weekday = (TextView) weatherItem.findViewById(R.id.textview);
					weekday.setText(weatherObject._weekday);
					//加载天气图标
					ImageView icon = (ImageView) weatherItem.findViewById(R.id.icon);
					PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TAG, icon, weatherObject._weatherIcon, null, TaskType.WeatherIcon);
					//加载天气事件
					
					Button event = (Button) weatherItem.findViewById(R.id.event);
					if (weatherObject._weatherEventList.size() > 1) {
						event.setText(R.string.view_weather_event_more);
//						event.setOnClickListener(new View.OnClickListener() {
//
//							@Override
//							public void onClick(View v) {
//								StringBuilder sb = new StringBuilder();
//								for(WeatherEvent weatherEvent: weatherObject._weatherEventList) {
//									sb.append(weatherEvent._eventName).append("/");
//								}
//								if (sb.toString().endsWith("/")) {
//									sb.deleteCharAt(sb.length() - 1);
//								}
//								DialogUtils.createSimpleConfirmAlertDialog(getActivity(), sb.toString(), null);
//							}
//							
//						});
					} else if (weatherObject._weatherEventList.size() == 1) {
						event.setText(weatherObject._weatherEventList.get(0)._eventName);
					} else {
						event.setVisibility(View.INVISIBLE);
					}
					
					mWeatherLayout.addView(weatherItem);
				}
				mWeatherLayout.setVisibility(View.VISIBLE);
			}
			
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}

}
