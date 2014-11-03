package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2.TaskType;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.view.RightPercentProgressBar;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.SecurityUtils;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class MyChooseCarCardsActivity extends PullToRefreshListPageActivityWithActionBar implements OnClickListener, OnLongClickListener{
	private static final String TOKEN = MyChooseCarCardsActivity.class.getName();
	public static final String TAG = "MyChooseCarCardsActivity";

	private Bundle mBundle = null;
	private boolean mIsOnResume = false;
	private CarsCursorAdapter mCarsCursorAdapter;
	private boolean mIsEditMode;
	private HashMap<Long, Boolean> deleteHomeIDList = new HashMap<Long, Boolean>();
	private boolean mIsHasData = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//每次进来我们先重置这个静态成员
		CarBaoxiuCardObject.setBaoxiuCardObject(null);
		String title = mBundle.getString(Intents.EXTRA_NAME);
		if (!TextUtils.isEmpty(title)) {
			setTitle(title);
		}
	}
	/**只有第一次会自动刷新*/
	@Override
	protected boolean isNeedForceRefreshOnResume() {
		if (ComPreferencesManager.getInstance().isFirstLaunch(TAG, true)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mIsOnResume = true;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mIsOnResume = false;
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mUpdatePhoneDataTask);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ModleSettings.createActionBarMenu(menu, mBundle);
		MenuItem deleteItem = menu.add(R.string.menu_delete, R.string.menu_delete, 0, R.string.menu_delete);
		MenuItem editItem = menu.add(R.string.menu_edit_for_delete, R.string.menu_edit_for_delete, 0, R.string.menu_edit_for_delete);
		MenuItem doneItem = menu.add(R.string.menu_back, R.string.menu_back, 0, R.string.menu_back);
		deleteItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		editItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mIsEditMode) {
			menu.findItem(R.string.menu_edit_for_delete).setVisible(false);
			menu.findItem(R.string.menu_back).setVisible(true);
			menu.findItem(R.string.menu_delete).setVisible(deleteHomeIDList.size() > 0);
		} else {
			menu.findItem(R.string.menu_edit_for_delete).setVisible(mIsHasData);
			menu.findItem(R.string.menu_back).setVisible(false);
			menu.findItem(R.string.menu_delete).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			return super.onOptionsItemSelected(item);
		case R.string.menu_delete:
			if(deleteHomeIDList.size() <= 0) {
				MyApplication.getInstance().showMessage(R.string.none_select_tips);
			} else {				
				new AlertDialog.Builder(mContext)
				.setMessage(R.string.msg_delete_home_confirm)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doDeleteHomeAsync();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
			}
			break;
		case R.string.menu_edit_for_delete:
			mIsEditMode = true;
			mCarsCursorAdapter.notifyDataSetChanged();
			invalidateOptionsMenu();
			break;
		case R.string.menu_back:
			onMenuDoneClick();
			break;
		default:
			Bundle newBundle = new Bundle();
		    newBundle.putAll(mBundle);
		    newBundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
			boolean handle = ModleSettings.onActionBarMenuSelected(item, mContext, newBundle);
			if (!handle) {
				return super.onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void onMenuDoneClick() {
		mIsEditMode = false;
		deleteHomeIDList.clear();
		mCarsCursorAdapter.notifyDataSetChanged();
		invalidateOptionsMenu();
	}

	DeleteHomeAsyncTask mDeleteHomeAsyncTask;
	private void doDeleteHomeAsync() {
		AsyncTaskUtils.cancelTask(mDeleteHomeAsyncTask);
		showDialog(DIALOG_PROGRESS);
		mDeleteHomeAsyncTask = new DeleteHomeAsyncTask();
		mDeleteHomeAsyncTask.execute();
		
	}
	
	private class DeleteHomeAsyncTask extends AsyncTask<Integer, Void, Cursor> {
		@Override
		protected Cursor doInBackground(Integer... param) {
			boolean deleted = false;
			ContentResolver cr = mContext.getContentResolver();
			long uid = MyAccountManager.getInstance().getAccountObject().mAccountUid;
			for(long bid : deleteHomeIDList.keySet()) {
				deleted = deleteFromService(uid, bid, deleteHomeIDList.size() == 1);
				if (deleted) {
					//还要删除本地的家数据
					CarBaoxiuCardObject.deleteBaoxiuCardInDatabaseForAccount(cr, uid, bid);
				}
			}
			deleteHomeIDList.clear();
			return loadLocal(cr);
		}

		/**
		 * 从服务器上删除家数据
		 * @param AID
		 * @return
		 */
		private synchronized boolean deleteFromService(long uid, long bid, boolean showTip) {
			InputStream is = null;
			try {
				is = NetworkUtils.openContectionLocked(ServiceObject.getCarBaoxiuCardDeleteUrl(String.valueOf(bid), String.valueOf(uid)), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					String content = NetworkUtils.getContentFromInput(is);
					ServiceResultObject resultObject = ServiceResultObject.parse(content);
					if (showTip) {
						MyApplication.getInstance().showMessageAsync(resultObject.mStatusMessage);
					}
					return resultObject.isOpSuccessfully();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				NetworkUtils.closeInputStream(is);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			dismissDialog(DIALOG_PROGRESS);
			super.onPostExecute(result);
			if (result != null && result.getCount() == 0) {
				MainActivity20141010.startActivityForTop(mContext);
			} else {
				mCarsCursorAdapter.changeCursor(result);
			}
			onMenuDoneClick();
//			if (mCarsCursorAdapter.getCount() == 0) {
//				MainActivity20141010.startActivityForTop(mContext);
//			} else {
//				mCarsCursorAdapter.notifyDataSetChanged();
//			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(mIsEditMode) {
				onMenuDoneClick();
				return true;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void onItemClick(AdapterView<?> listView, View view, int pos, long arg3) {
		if(mIsEditMode) {
			CarViewHolder viewHolder = (CarViewHolder) view.getTag();
			viewHolder._checkbox.setChecked(!viewHolder._checkbox.isChecked());
			if (viewHolder._checkbox.isChecked()) {
				deleteHomeIDList.put(viewHolder._carBaoxiuCardObject.mBID, viewHolder._checkbox.isChecked());
			} else {
				deleteHomeIDList.remove(viewHolder._carBaoxiuCardObject.mBID);
			}
			invalidateOptionsMenu();
		} else {
			CarViewHolder viewHolder = (CarViewHolder) view.getTag();
		    Bundle newBundle = new Bundle();
		    newBundle.putAll(mBundle);
		    newBundle.putLong("uid", viewHolder._carBaoxiuCardObject.mUID);
		    newBundle.putLong("bid", viewHolder._carBaoxiuCardObject.mBID);
		    CardViewActivity.startActivit(mContext, newBundle);
		}
	}
	
	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mCarsCursorAdapter = new CarsCursorAdapter(mContext, null, true);
		return new AdapterWrapper<CarsCursorAdapter>(mCarsCursorAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		return CarBaoxiuCardObject.getAlCarCards(contentResolver, MyAccountManager.getInstance().getCurrentAccountUid());
	}

	@Override
	protected int savedIntoDatabase(ContentResolver contentResolver, List<? extends InfoInterface> infoObjects) {
		int insertOrUpdateCount = 0;
		if (infoObjects != null) {
			for(InfoInterface object:infoObjects) {
				if (object.saveInDatebase(contentResolver, null)) {
					insertOrUpdateCount++;
				}
			}
		}
		return insertOrUpdateCount;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		//{"StatusCode":"1","StatusMessage":"返回分页数据","Data":{"total":0,"data":[]}}
		String content = NetworkUtils.getContentFromInput(is);
		List<CarBaoxiuCardObject> list = new ArrayList<CarBaoxiuCardObject>();
		if (!TextUtils.isEmpty(content)) {
			ServiceResultObject serviceResultObject = ServiceResultObject.parse(content);
			if (serviceResultObject.isOpSuccessfully()) {
				//得到总个数
				try {
					pageInfo.mTotalCount = serviceResultObject.mJsonData.getLong("total");
					JSONArray jsonArray = serviceResultObject.mJsonData.getJSONArray("data");
					if (jsonArray != null && jsonArray.length() > 0) {
						int len = jsonArray.length();
						list = new ArrayList<CarBaoxiuCardObject>(len);
						for(int index=0; index<len; index++) {
							list.add(CarBaoxiuCardObject.parseBaoxiuCards(jsonArray.getJSONObject(index), null));
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		if (ComPreferencesManager.getInstance().isFirstLaunch(TAG, true)) {
			ComPreferencesManager.getInstance().setFirstLaunch(TAG, false);
		}
		return list;
	}

	@Override
	protected Query getQuery() {
		Query query = new Query();
		query.qServiceUrl = ServiceObject.getAllCarBaoxiuCardsUrl(MyAccountManager.getInstance().getCurrentAccountUid());
		return query;
	}

	@Override
	protected void onRefreshStart() {
		
	}

	@Override
	protected void onRefreshEnd() {
		
	}
	@Override
	protected void onLoadLocalEnd() {
		mIsHasData = mCarsCursorAdapter.getCount() > 0;
		invalidateOptionsMenu();
	}

	@Override
	protected int getContentLayout() {
		return R.layout.car_list_pull_to_refresh_page_activity;
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundle = intent.getExtras();
		if (mBundle == null) {
			DebugUtils.logD(TAG, "you must pass Bundle object in createChooseDevice()");
			return false;
		}
		return true;
	}	
	public static void startIntent(Context context, Bundle bundle) {
		Intent intent = new Intent(context, MyChooseCarCardsActivity.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}
	
	
	private class CarsCursorAdapter extends CursorAdapter {

		private String mProgressUnitText = "";
		public CarsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mProgressUnitText = context.getString(R.string.progress_unit_day);
		}

		@Override
		protected void onContentChanged() {
			DebugUtils.logD(TAG, "CarsCursorAdapter onContentChanged mIsOnResume " + mIsOnResume);
			if (!mIsOnResume) super.onContentChanged();
		}



		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.car_card_list_item, parent, false);
			CarViewHolder viewHolder = new CarViewHolder();
			viewHolder._iconView = (ImageView) view.findViewById(R.id.avator);
			viewHolder._checkbox = (CheckBox) view.findViewById(R.id.checkbox);
			viewHolder._titleView = (TextView) view.findViewById(R.id.title);
			viewHolder._baoxiuqiView = (TextView) view.findViewById(R.id.flag_baoxiu);
			viewHolder._guobaoView = (TextView) view.findViewById(R.id.flag_guobao);
			
			viewHolder._4sShopTel = (TextView) view.findViewById(R.id.car_product_4s_shop_title);
			viewHolder._washingShopTel = (TextView) view.findViewById(R.id.car_product_washing_shop_title);
			viewHolder._weixiuShopTel = (TextView) view.findViewById(R.id.car_product_weiixiu_shop_title);
			viewHolder._chuxianShopTel = (TextView) view.findViewById(R.id.car_product_chuxian_shop_title);
			
			viewHolder._baoyanProgress = (RightPercentProgressBar) view.findViewById(R.id.car_product_baoyan_progress);
			viewHolder._baoxianProgress = (RightPercentProgressBar) view.findViewById(R.id.car_product_baoxian_progress);
			viewHolder._nianjianProgress = (RightPercentProgressBar) view.findViewById(R.id.car_product_nianjian_progress);
			viewHolder._baoyanProgress.setProgressUnit(mProgressUnitText);
			viewHolder._baoxianProgress.setProgressUnit(mProgressUnitText);
			viewHolder._nianjianProgress.setProgressUnit(mProgressUnitText);
			
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			CarViewHolder viewHolder = (CarViewHolder) view.getTag();
			CarBaoxiuCardObject card = CarBaoxiuCardObject.getFromBaoxiuCardsCursor(cursor);
			viewHolder._carBaoxiuCardObject = card;
			
			if(mIsEditMode) {
				viewHolder._checkbox.setVisibility(View.VISIBLE);
			} else {
				viewHolder._checkbox.setVisibility(View.GONE);
			}
			Boolean checked = deleteHomeIDList.get(viewHolder._carBaoxiuCardObject.mBID);
			if(checked != null && checked) {
				viewHolder._checkbox.setChecked(true);
			} else {
				viewHolder._checkbox.setChecked(false);
			}
			
			//产品图片
			if (!TextUtils.isEmpty(card.mPKY) && !card.mPKY.equals(BaoxiuCardObject.DEFAULT_BAOXIUCARD_IMAGE_KEY)) {
				PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, viewHolder._iconView , card.mPKY, null, TaskType.HOME_DEVICE_AVATOR);
			} else {
				//设置默认的ky图片
				viewHolder._iconView.setImageResource(R.drawable.ky_default);
			}
			
			viewHolder._titleView.setText(card.mPinPai + "-" + card.mXingHao);
			
			//整机保修
			int validity = card.getBaoxiuValidity();
			if (validity > 0) {
				viewHolder._baoxiuqiView.setVisibility(View.VISIBLE);
				viewHolder._guobaoView.setVisibility(View.GONE);
				if (validity > 9999) {
					viewHolder._baoxiuqiView.setText(getString(R.string.baoxiucard_validity_toomuch));
				} else {
					viewHolder._baoxiuqiView.setText(getString(R.string.baoxiucard_validity, validity));
				}
			} else {
				viewHolder._baoxiuqiView.setVisibility(View.GONE);
				viewHolder._guobaoView.setVisibility(View.VISIBLE);
			}
			int day = CarBaoxiuCardObject.getValidityDay(90, card.mLastBaoYanTime);
			viewHolder._baoyanProgress.setProgress(day);
			viewHolder._baoyanProgress.setProgressText(String.valueOf(day));
			viewHolder._baoxianProgress.setProgress(CarBaoxiuCardObject.getValidityDay(365, card.mBaoXianDeadline));
			viewHolder._baoyanProgress.setProgressText(String.valueOf(day));
			viewHolder._nianjianProgress.setProgress(CarBaoxiuCardObject.getValidityDay(365, card.mLastYanCheTime));
			viewHolder._baoyanProgress.setProgressText(String.valueOf(day));
			
			if (mIsEditMode) {
				viewHolder._4sShopTel.setOnClickListener(null);
				viewHolder._washingShopTel.setOnClickListener(null);
				viewHolder._weixiuShopTel.setOnClickListener(null);
				viewHolder._chuxianShopTel.setOnClickListener(null);
				
				viewHolder._4sShopTel.setOnLongClickListener(null);
				viewHolder._washingShopTel.setOnLongClickListener(null);
				viewHolder._weixiuShopTel.setOnLongClickListener(null);
				viewHolder._chuxianShopTel.setOnLongClickListener(null);
			} else {
				viewHolder._4sShopTel.setTag(viewHolder);
				viewHolder._washingShopTel.setTag(viewHolder);
				viewHolder._weixiuShopTel.setTag(viewHolder);
				viewHolder._chuxianShopTel.setTag(viewHolder);
				
				viewHolder._4sShopTel.setOnClickListener(MyChooseCarCardsActivity.this);
				viewHolder._washingShopTel.setOnClickListener(MyChooseCarCardsActivity.this);
				viewHolder._weixiuShopTel.setOnClickListener(MyChooseCarCardsActivity.this);
				viewHolder._chuxianShopTel.setOnClickListener(MyChooseCarCardsActivity.this);
				
				viewHolder._4sShopTel.setOnLongClickListener(MyChooseCarCardsActivity.this);
				viewHolder._washingShopTel.setOnLongClickListener(MyChooseCarCardsActivity.this);
				viewHolder._weixiuShopTel.setOnLongClickListener(MyChooseCarCardsActivity.this);
				viewHolder._chuxianShopTel.setOnLongClickListener(MyChooseCarCardsActivity.this);
			}
			
		}
		
	}
	public static class CarViewHolder {
		public static int TYPE_PHONE_4S = 1;
		public static int TYPE_PHONE_WASHING = 2;
		public static int TYPE_PHONE_WEIXIU = 3;
		public static int TYPE_PHONE_CHUXIAN = 4;
		public CarBaoxiuCardObject _carBaoxiuCardObject;
		private TextView _4sShopTel, _washingShopTel, _weixiuShopTel, _chuxianShopTel;
		private ImageView _iconView;
		private TextView _titleView, _baoxiuqiView, _guobaoView;
		private CheckBox _checkbox;
		private RightPercentProgressBar _baoyanProgress, _baoxianProgress, _nianjianProgress;
		
		public static String getPhone(View view, CarViewHolder viewHolder) {
			switch(view.getId()){
			case R.id.car_product_4s_shop_title:
				return viewHolder._carBaoxiuCardObject.m4SShopTel;
			case R.id.car_product_washing_shop_title:
				return viewHolder._carBaoxiuCardObject.m4SWashingShopTel;
			case R.id.car_product_weiixiu_shop_title:
				return viewHolder._carBaoxiuCardObject.mWeixiuShopTel;
			case R.id.car_product_chuxian_shop_title:
				return viewHolder._carBaoxiuCardObject.mChuxianShopTel;
			}
			return "";
		}
		
		public static void setPhone(View view, CarViewHolder viewHolder, String newValue) {
			switch(view.getId()){
			case R.id.car_product_4s_shop_title:
				viewHolder._carBaoxiuCardObject.m4SShopTel = newValue;
			case R.id.car_product_washing_shop_title:
				viewHolder._carBaoxiuCardObject.m4SWashingShopTel = newValue;
			case R.id.car_product_weiixiu_shop_title:
				viewHolder._carBaoxiuCardObject.mWeixiuShopTel = newValue;
			case R.id.car_product_chuxian_shop_title:
				viewHolder._carBaoxiuCardObject.mChuxianShopTel = newValue;
			}
		}
		
		public static int getPhoneType(View view, CarViewHolder viewHolder) {
			switch(view.getId()){
			case R.id.car_product_4s_shop_title:
				return TYPE_PHONE_4S;
			case R.id.car_product_washing_shop_title:
				return TYPE_PHONE_WASHING;
			case R.id.car_product_weiixiu_shop_title:
				return TYPE_PHONE_WEIXIU;
			case R.id.car_product_chuxian_shop_title:
				return TYPE_PHONE_CHUXIAN;
			}
			return 0;
		}
		
		public static String getPhoneTitle(View view, CarViewHolder viewHolder) {
			switch(view.getId()){
			case R.id.car_product_4s_shop_title:
				return viewHolder._4sShopTel.getText().toString();
			case R.id.car_product_washing_shop_title:
				return viewHolder._washingShopTel.getText().toString();
			case R.id.car_product_weiixiu_shop_title:
				return viewHolder._weixiuShopTel.getText().toString();
			case R.id.car_product_chuxian_shop_title:
				return viewHolder._chuxianShopTel.getText().toString();
			}
			return "";
		}
		
	}

	@Override
	public boolean onLongClick(View v) {
		Object object = v.getTag();
		if (object != null && object instanceof CarViewHolder) {
			CarViewHolder viewHolder = (CarViewHolder) object;
			String tel = CarViewHolder.getPhone(v, viewHolder);
			String title = CarViewHolder.getPhoneTitle(v, viewHolder);;
			showModifyDialog(v, tel, title);
			return true;
		}
		return false;
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch(id){
		case R.id.car_product_4s_shop_title:
		case R.id.car_product_washing_shop_title:
		case R.id.car_product_weiixiu_shop_title:
		case R.id.car_product_chuxian_shop_title:
			CarViewHolder viewHolder = (CarViewHolder) v.getTag();
			String tel = CarViewHolder.getPhone(v, viewHolder);
			String title = CarViewHolder.getPhoneTitle(v, viewHolder);;
			if (TextUtils.isEmpty(tel)) {
				//没有数据，我们需要让用户输入
				showModifyDialog(v, tel, title);
			} else {
				Intents.callPhone(mContext, tel);
			}
			break;
		}
		
	}
	
	private void showModifyDialog(final View view, String tel, String title) {
		final EditText input = new EditText(mContext);
		input.setMinLines(2);
		input.setText(tel);
		input.setSelection(tel.length());
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
		.setTitle(getString(R.string.format_title_modify_community_service_data, title))
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					updatePhoneDataAsync(view, input.getText().toString().trim());
					
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
		//一开始就设置为false,这样只有用户改变了内容才可以点击
	    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
	}
	
	
	private UpdatePhoneDataTask mUpdatePhoneDataTask;
	private void updatePhoneDataAsync(View view, String newValue) {
		AsyncTaskUtils.cancelTask(mUpdatePhoneDataTask);
		mUpdatePhoneDataTask = new UpdatePhoneDataTask(view, newValue);
		mUpdatePhoneDataTask.execute();
		showDialog(DIALOG_PROGRESS);
	}
	private class UpdatePhoneDataTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private View _view;
		private String _newValue;
		private CarViewHolder _viewHolder;
		public UpdatePhoneDataTask(View view, String newValue) {
			_view = view;
			_newValue = newValue;
			_viewHolder = (CarViewHolder) _view.getTag();
		}
		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			
			try {
				//if (para != null && !string.IsNullOrEmpty(para.cell)
				//&& !string.IsNullOrEmpty(para.name) 
				//&& !string.IsNullOrEmpty(para.stvalue) 
				//&& StrHelper.IsNum(para.uid) 
				//&& StrHelper.IsNum(para.xid))
				JSONObject queryJsonObject = new JSONObject();
				queryJsonObject.put("phone", _newValue);
				queryJsonObject.put("stvalue", CarViewHolder.getPhoneType(_view, _viewHolder));
				queryJsonObject.put("uid", _viewHolder._carBaoxiuCardObject.mUID);
				queryJsonObject.put("cid", _viewHolder._carBaoxiuCardObject.mBID);
				is = NetworkUtils.openContectionLocked(ServiceObject.updateCarBaoxiuCardPhoneUrl("para", queryJsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//添加或更新成功
					CarViewHolder.setPhone(_view, _viewHolder, _newValue);;
					boolean save = _viewHolder._carBaoxiuCardObject.saveInDatebase(getContentResolver(), null);
					DebugUtils.logD(TAG, "UpdatePhoneDataTask save " + CarViewHolder.getPhoneTitle(_view, _viewHolder) + " with newValue " +  _newValue + ", updated " + save);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				serviceResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
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
			MyApplication.getInstance().showMessage(result.mStatusMessage);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}
	
	public void onMovedToScrapHeap(View view) {
		Object object = view.getTag();
		if (object != null && object instanceof CarViewHolder) {
			CarViewHolder viewHolder = (CarViewHolder) object;
			viewHolder._nianjianProgress.getAnimation();
//			ObjectAnimator anim = ObjectAnimator .ofFloat(phone, "translationX", -500f, 0f); 
		}
	 }

}
