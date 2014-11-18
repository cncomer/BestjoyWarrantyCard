package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.IBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.account.ViewConversationObject;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.utils.JsonParser;
import com.bestjoy.app.warrantycard.utils.SpeechRecognizerEngine;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class ViewConversationListActivity extends AbstractLoadMoreWithPageActivity implements View.OnClickListener{

	private static final String TAG = "ViewConversationListActivity";
	private static final int WHAT_REQUEST_REFRESH_LIST = 11000;
	private static final int WHAT_REQUEST_REFRESH_LIST_BY_MYSELF = 11001;
	private static final int WHAT_REQUEST_NEW_DATA = 12000;
	private EditText mInputEdit;
	private ImageView mTextIcon, mVoiceIcon;
	private TextView mConnectedStatusView;
	private Button mVoiceBtn;
	private Handler mUiHandler;
	
	private ConversationAdapter mConversationAdapter;
	private long mCurrentMessageId = -1;
	private Query mQuery;
	private IBaoxiuCardObject mIBaoxiuCardObject;
	private Bundle mBundles;
	private int mBundleType = -1;
	/**当前的最小mid*/
	private long mCurrentMinId = Integer.MAX_VALUE;
	/**当前的最大mid*/
	private long mCurrentMaxId = 0;
	
	private boolean mIsConnected = false;
	
	private String[] mSelectionArgs;
	private boolean mIsLoadingServiceData = false;
	
	ComConnectivityManager.ConnCallback ConnCallback = new ComConnectivityManager.ConnCallback() {
		
		@Override
		public void onConnChanged(ComConnectivityManager cm) {
			if (cm.isConnected() && !mIsConnected) {
				mIsConnected = true;
				mUiHandler.removeMessages(WHAT_REQUEST_NEW_DATA);
				loadNewMessagesAsync();
				mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_NEW_DATA, 5000);
			} else {
				mIsConnected = false;
			}
			
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		setShowHomeUp(true);
		setLoadMorePosition(LOAD_MORE_TOP);
		mListView = getListView();
		
		mUiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case WHAT_REQUEST_REFRESH_LIST:
					mConversationAdapter.callSuperOnContentChanged();
					if (mIsAtListBottom) {
						mListView.setSelection(mConversationAdapter.getCount()-1);
					} else if (mConversationAdapter.getCount() > 0){
						MyApplication.getInstance().showMessage(R.string.new_msg_comming);
					}
					break;
				case WHAT_REQUEST_REFRESH_LIST_BY_MYSELF:
					mConversationAdapter.callSuperOnContentChanged();
					mListView.setSelection(mConversationAdapter.getCount()-1);
					break;
				case WHAT_REQUEST_NEW_DATA:
					//定时去查看是否有新数据
					
					if (!mIsLoadingMore && !mIsLoadingServiceData) {
						loadNewMessagesAsync();
					}
					mUiHandler.removeMessages(WHAT_REQUEST_NEW_DATA);
					mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_NEW_DATA, 5000);
					break;
				}
			}
		};
		
		initEditLayout();
		
		mBundleType = mBundles.getInt(Intents.EXTRA_TYPE);
		switch(mBundleType) {
		case R.id.model_my_card:
			mIBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundles);
			break;
		case R.id.model_my_car_card:
			mIBaoxiuCardObject = CarBaoxiuCardObject.getBaoxiuCardObject(mBundles);
			break;
		}
		if (false) {
			mIBaoxiuCardObject.mKY = "2020004CJ";
		}
		mSelectionArgs = new String[]{MyAccountManager.getInstance().getCurrentAccountMd(), mIBaoxiuCardObject.mKY};
		mIsConnected = ComConnectivityManager.getInstance().isConnected();
		ComConnectivityManager.getInstance().addConnCallback(ConnCallback);
		
		if (!mIsConnected) {
			ComConnectivityManager.getInstance().onCreateNoNetworkDialog(mContext).show();
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mIsConnected) {
			mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_NEW_DATA, 5000);
		}
		
	}
	
	
	 @Override
     public boolean onCreateOptionsMenu(Menu menu) {
		 return false;
	 }
	
	private void initEditLayout() {
		mInputEdit = (EditText) findViewById(R.id.input);
		mInputEdit.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendMessageLocked();
				}
				return false;
			}
			
		});
		
		mVoiceBtn = (Button) findViewById(R.id.button_voice);
		
		mTextIcon = (ImageView) findViewById(R.id.button_text_icon);
		mTextIcon.setOnClickListener(this);
		
		mVoiceIcon = (ImageView) findViewById(R.id.button_voice_icon);
		mVoiceIcon.setOnClickListener(this);
		
		findViewById(R.id.button_add_icon).setOnClickListener(this);
		
		//当前连接状态，点击可以出发重新连接
		mConnectedStatusView = (TextView) findViewById(R.id.status_view);
		mConnectedStatusView.setOnClickListener(this);
		
		updateEditLayout(true);
		initVoiceLayout();
		
	}
	/**
	 * 是否显示语音输入
	 * @param showVoiceInput
	 */
	private void updateEditLayout(boolean showVoiceIcon) {
		if (showVoiceIcon) {
			mVoiceIcon.setVisibility(View.VISIBLE);
			mTextIcon.setVisibility(View.GONE);
			mVoiceBtn.setVisibility(View.GONE);
			mInputEdit.setVisibility(View.VISIBLE);
		} else {
			mVoiceIcon.setVisibility(View.GONE);
			mTextIcon.setVisibility(View.VISIBLE);
			mVoiceBtn.setVisibility(View.VISIBLE);
			mInputEdit.setVisibility(View.GONE);
		}
	}
	
	private View mVoiceInputPopLayout;
	private TextView mVoiceInputStatus;
	private EditText mAskInput;
	private ImageView mVoiceImage;
	private SpeechRecognizerEngine mSpeechRecognizerEngine;
	private VoiceButtonTouchListener mVoiceButtonTouchListener;
	private void initVoiceLayout() {
		mVoiceInputPopLayout = findViewById(R.id.voice_input_layout);
		mVoiceInputPopLayout.setVisibility(View.GONE);
		mVoiceInputStatus = (TextView) findViewById(R.id.voice_input_status);
		mVoiceButtonTouchListener = new VoiceButtonTouchListener();
		mVoiceBtn.setOnTouchListener(mVoiceButtonTouchListener);
		
		mAskInput = (EditText) findViewById(R.id.voice_input_confirm);
		mVoiceImage = (ImageView) findViewById(R.id.voice_input_imageview);
		mSpeechRecognizerEngine = SpeechRecognizerEngine.getInstance(mContext);
		
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
		public void onResult(RecognizerResult arg0, boolean arg1) {
			
			String text = JsonParser.parseIatResult(arg0.getResultString());
			DebugUtils.logD(TAG, "chenkai onResult " + text);
			if (!TextUtils.isEmpty(text)) {
				//BeepAndVibrate.getInstance().playBeepSoundAndVibrate();
				mAskInput.append(text);
				mAskInput.setSelection(mAskInput.length());
				
				if (mVoiceInputPopLayout.getVisibility() == View.VISIBLE && !mVoiceButtonTouchListener._isCanceled && mVoiceButtonTouchListener._isUp) {
					mVoiceInputPopLayout.setVisibility(View.GONE);
					doVoiceQuery(text);
				}
			}
		}
	
		@Override
		public void onVolumeChanged(int volume) {
			mVoiceImage.setImageLevel(volume);
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			
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
					doVoiceQuery(query);
				}
				break;
			}
			return false;
		}
	}
	
	private void doVoiceQuery(String query) {
		DebugUtils.logD(TAG, "doVoiceQuery() query=" + query);
//		mInputEdit.getText().clear();
//		mInputEdit.append(query);
		//这里是语音识别后进入文本编写界面以便用户修改输入内容
//		updateEditLayout(true);
//		sendMessageLocked();
		
		//显示弹出对话框让用户确认输入
		showCheckVoiceDialog(query);
		
	}
	
	private void showCheckVoiceDialog(String query) {
		final EditText input = new EditText(mContext);
		input.setText(query);
		input.setSelection(query.length());
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
		.setMessage(R.string.text_check_voice_title_for_im_send)
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (!ComConnectivityManager.getInstance().isConnected()) {
						MyApplication.getInstance().showMessage(MyApplication.getInstance().getGernalNetworkError());
						return;
					}
					String text = input.getText().toString().trim();
					if (!TextUtils.isEmpty(text)) {
						if (text.length() > 512) {
							MyApplication.getInstance().showMessage(R.string.msg_too_long_text_for_conversation);
						} else {
							sendMessageAsync(text);
						}
					}
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
	public void onDestroy() {
		super.onDestroy();
		ComConnectivityManager.getInstance().removeConnCallback(ConnCallback);
		mUiHandler.removeMessages(WHAT_REQUEST_NEW_DATA);
		AsyncTaskUtils.cancelTask(mLoadNewMessageAsyncTask);
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.button_voice:
			break;
		case R.id.button_text_icon:
			updateEditLayout(true);
			break;
		case R.id.button_voice_icon:
			updateEditLayout(false);
			break;
		}
	}
	
	private void sendMessageLocked() {
		if (!ComConnectivityManager.getInstance().isConnected()) {
			MyApplication.getInstance().showMessage(MyApplication.getInstance().getGernalNetworkError());
			return;
		}
		String text = mInputEdit.getText().toString().trim();
		if (!TextUtils.isEmpty(text)) {
			if (text.length() > 512) {
				MyApplication.getInstance().showMessage(R.string.msg_too_long_text_for_conversation);
			} else {
				sendMessageAsync(text);
			}
		}
	}
	
	private SendMessageTask mSendMessageTask;
	private void sendMessageAsync(String text) {
		AsyncTaskUtils.cancelTask(mSendMessageTask);
		showDialog(DIALOG_PROGRESS);
		mSendMessageTask = new SendMessageTask();
		mSendMessageTask.execute(text);
	}
	private class SendMessageTask extends AsyncTask<String, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(String... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				JSONObject queryJSONObject = new JSONObject();
				queryJSONObject.put("KY", mIBaoxiuCardObject.mKY);
				queryJSONObject.put("UID", MyAccountManager.getInstance().getCurrentAccountUid());
				queryJSONObject.put("Message", params[0]);
				DebugUtils.logD(TAG, "SendMessageTask queryJSONObject " + queryJSONObject.toString());
				is = NetworkUtils.openContectionLocked(ServiceObject.postViewConversationUrl("para", queryJSONObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//发送成功，我们将信息保存在本地
					ViewConversationObject viewConversationObject = ViewConversationObject.parse(serviceResultObject.mJsonData);
					viewConversationObject.mSenderNickName = MyAccountManager.getInstance().getAccountObject().mAccountNickName;
					if (viewConversationObject.saveInDatebase(getContentResolver(), null)) {
					}
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
			dismissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				mInputEdit.getText().clear();
				mUiHandler.removeMessages(WHAT_REQUEST_REFRESH_LIST_BY_MYSELF);
				mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_REFRESH_LIST_BY_MYSELF, 250);
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
	protected boolean checkIntent(Intent intent) {
		mBundles = intent.getExtras();
		if (mBundles == null) {
			DebugUtils.logE(TAG, "checkIntent failed, you must supply Bundles");
			return false;
		}
		return true;
	}
	
	public static void startActivity(Context context, Bundle bundle) {
		Intent intent = new Intent(context, ViewConversationListActivity.class);
		if (bundle != null) intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	
	
	private class ConversationAdapter extends CursorAdapter{
		private static final int TYPE_TOP = 0;
		private static final int TYPE_LEFT = 1;
		private static final int TYPE_RIGHT = 2;
		
		private static final int TYPE_COUNT = 3;

		public ConversationAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}
		
		@Override
		protected void onContentChanged() {
			//如果用户正在下拉刷新，我们不要刷新列表数据，因为框架会帮我们查询，changeCursor()
			//一秒内延迟刷新，提高性能
//			mUiHandler.removeMessages(WHAT_REQUEST_REFRESH_LIST);
//			mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_REFRESH_LIST, 250);
			return;
		}
		
		private void callSuperOnContentChanged() {
			super.onContentChanged();
		}


		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = null;
			ViewHolder viewHolder = new ViewHolder();
			int viewType = getItemViewType(cursor.getPosition());
			if (TYPE_LEFT == viewType) {
				view = LayoutInflater.from(context).inflate(R.layout.conversation_item_left, parent, false);
			} else if (TYPE_RIGHT == viewType) {
				view = LayoutInflater.from(context).inflate(R.layout.conversation_item_right, parent, false);
			}
			viewHolder._avator = (ImageView) view.findViewById(R.id.avator);
//			viewHolder._avator.setVisibility(View.VISIBLE);
			viewHolder._name = (TextView) view.findViewById(R.id.name);
			viewHolder._content = (TextView) view.findViewById(R.id.content);
			viewHolder._time = (TextView) view.findViewById(R.id.date);
			viewHolder._error = (ImageView) view.findViewById(R.id.error);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder._ViewConversationObject = ViewConversationObject.getConversationItemObjectFromCursor(cursor);
			viewHolder._name.setText(viewHolder._ViewConversationObject.mSenderNickName /*+ "#" + viewHolder._ViewConversationObject.mMID*/);
			viewHolder._content.setText(viewHolder._ViewConversationObject.mMessage);
			viewHolder._time.setText(DateUtils.DATE_FULL_TIME_FORMAT.format(viewHolder._ViewConversationObject.mServerTime));
			viewHolder._error.setVisibility(View.GONE);
		}
		

		@Override
		public int getItemViewType(int position) {
			Cursor c = (Cursor) getItem(position);
			String sender = c.getString(ViewConversationObject.INDEX_SID);
			if (sender.equals(MyAccountManager.getInstance().getCurrentAccountUid())) {
				//用户自己的消息显示在右边
				return TYPE_RIGHT;
			} else {
				return TYPE_LEFT;
			}
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}
	}
	
	private static class ViewHolder {
		private ImageView _avator, _error;
		private TextView _name, _content, _time;
		private ViewConversationObject _ViewConversationObject;
	}
	
	private LoadNewMessageAsyncTask mLoadNewMessageAsyncTask;
	private void loadNewMessagesAsync() {
		DebugUtils.logD(TAG, "start loadNewMessagesAsync.....");
		AsyncTaskUtils.cancelTask(mLoadNewMessageAsyncTask);
		mLoadNewMessageAsyncTask = new LoadNewMessageAsyncTask();
		mLoadNewMessageAsyncTask.execute();
		mIsLoadingServiceData = true;
	}
	private class LoadNewMessageAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private int _newCount = 0;
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				is = NetworkUtils.openContectionLocked(ServiceObject.buildPageQuery(ServiceObject.getViewConversationUrl(mIBaoxiuCardObject.mKY, String.valueOf(mCurrentMaxId), String.valueOf(ViewConversationObject.DIRECTION_UP)), 1, 20), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					if (serviceResultObject.mJsonData != null && serviceResultObject.mJsonData.getInt("total") > 0 && serviceResultObject.mJsonData.getJSONArray("message") != null) {
						//有新数据，我们解析并保存
						List<ViewConversationObject> list = ViewConversationObject.parse(serviceResultObject.mJsonData.getJSONArray("message"));
						long maxMid = mCurrentMaxId;
						DebugUtils.logD(TAG, "LoadNewMessageAsyncTask save new messages.....");
						for (ViewConversationObject viewConversationObject : list) {
							if (viewConversationObject.saveInDatebase(getContentResolver(), null)) {
								_newCount++;
							}
							maxMid = Math.max(maxMid, viewConversationObject.mMID);
						}
						mCurrentMaxId = maxMid;
						DebugUtils.logD(TAG, "LoadNewMessageAsyncTask reset mCurrentMaxId=" + mCurrentMaxId + ", mCurrentMinId=" + mCurrentMinId);
					}
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
			if (!result.isOpSuccessfully()) {
				MyApplication.getInstance().showMessage(result.mStatusMessage);
			}
			if (_newCount > 0) {
				mUiHandler.removeMessages(WHAT_REQUEST_REFRESH_LIST);
				mUiHandler.sendEmptyMessageDelayed(WHAT_REQUEST_REFRESH_LIST, 250);
			}
			mIsLoadingServiceData = false;
			
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mIsLoadingServiceData = false;
		}
		
	}

	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mConversationAdapter = new ConversationAdapter(this, null, true);
		return new AdapterWrapper<CursorAdapter>(mConversationAdapter);
	}
    
	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(BjnoteContent.VIEW_CONVERSATION_HISTORY.CONTENT_URI, ViewConversationObject.PROJECTION, ViewConversationObject.UID_KY_SELECTION, mSelectionArgs, ViewConversationObject.SORT_BY_MID);
		return cursor;
	}

	@Override
	protected int savedIntoDatabase(ContentResolver cr, List<? extends InfoInterface> infoObjects) {
		int result = 0;
		for (InfoInterface viewConversationObject : infoObjects) {
			if (viewConversationObject.saveInDatebase(getContentResolver(), null)) {
			}
		}
		return result;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		ServiceResultObject serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
		List<ViewConversationObject> list = new ArrayList<ViewConversationObject>();
		try {
			if (serviceResultObject.isOpSuccessfully()) {
				pageInfo.mTotalCount = serviceResultObject.mJsonData.getInt("total");
				if (serviceResultObject.mJsonData != null && pageInfo.mTotalCount > 0 && serviceResultObject.mJsonData.getJSONArray("message") != null) {
					list = ViewConversationObject.parse(serviceResultObject.mJsonData.getJSONArray("message"));
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	protected Query getQuery() {
		if (mQuery == null) {
			mQuery =  new Query();
			mQuery.mPageInfo = new PageInfo();
			mQuery.qServiceUrl = ServiceObject.getViewConversationUrl(mIBaoxiuCardObject.mKY, String.valueOf(mCurrentMinId), String.valueOf(ViewConversationObject.DIRECTION_DOWN));
		}
		return mQuery;
	}

	@Override
	protected int getContentLayout() {
		return R.layout.activity_view_conversation;
	}

	@Override
	protected void onLoadMoreStart() {
		//每次载入更多的时候，我们先要记录下当前的位置
		final Cursor cursor = mConversationAdapter.getCursor();
		if (cursor != null && cursor.getCount() > 0) {
			if (cursor.moveToPosition(mFirstVisibleItem)) {
				mCurrentMessageId =  cursor.getLong(ViewConversationObject.INDEX_ID);
			} 
			if (cursor.moveToFirst()) {
				mCurrentMinId = cursor.getLong(ViewConversationObject.INDEX_MID);
			}
			if (cursor.moveToLast()) {
				mCurrentMaxId =  cursor.getLong(ViewConversationObject.INDEX_MID);
			} 
		}
	    DebugUtils.logD(TAG, "onLoadMoreStart mCurrentMinId=" + mCurrentMinId + ", mCurrentMaxId=" + mCurrentMaxId + ", mCurrentMessageId="+mCurrentMessageId + ", mFirstVisibleItem="+mFirstVisibleItem);
		
		mQuery.qServiceUrl = ServiceObject.getViewConversationUrl(mIBaoxiuCardObject.mKY, String.valueOf(mCurrentMinId), String.valueOf(ViewConversationObject.DIRECTION_DOWN));
		mQuery.mPageInfo.mPageIndex=1;
	}

	@Override
	protected void onLoadMoreEnd() {
		final Cursor cursor = loadLocal(mContentResolver);
		
		mUiHandler.post(new Runnable() {

			@Override
			public void run() {
				mConversationAdapter.changeCursor(cursor);
				if (mCurrentMessageId == -1) {
					mListView.setSelection(cursor.getCount()-1);
				}
			}
			
		});
	}
	@Override
	protected void onPostLoadMoreEnd() {
		final Cursor cursor = mConversationAdapter.getCursor();
		int position = mFirstVisibleItem;
		long id = 0;
		for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
			id =  cursor.getLong(ViewConversationObject.INDEX_ID);
			if (mCurrentMessageId == id) {
				position = cursor.getPosition();
			}
		}
		if (mCurrentMessageId != -1) mListView.setSelection(position);
		DebugUtils.logD(TAG, "onPostLoadMoreEnd current item position=" + position + ", mCurrentMessageId=" + mCurrentMessageId);
	}

}
