package com.shwy.bestjoy.bjnote.mylife;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.ui.PullToRefreshListPageActivityWithActionBar;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.view.MarqueeTextView;
import com.shwy.bestjoy.contacts.AddrBookUtils;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.FilesUtils;
import com.shwy.bestjoy.utils.ImageHelper;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class MyLifeMainActivity extends PullToRefreshListPageActivityWithActionBar implements View.OnClickListener{
	public static final String TAG = "MyLifeMainActivity";
	private static final String TOKEN = MyLifeListAdapter.class.getName();
	private static final int mAvatorWidth = 1200, mAvatorHeight =1200;
	private Bundle mBundle;
	private File mAvatorFile;
	private Uri mAvatorUri;
	private boolean mNeedUpdateAvatorFromCamera = false;
	private boolean mNeedUpdateAvatorFromGallery = false;
	private Handler mHandler;
	private int mPictureRequest = -1;
	/**请求商品预览图*/
	private static final int REQUEST_MEMBER_CARD_IMAGE = 2;
	private static final int REQUEST_UPDATE = 1;
	private static final int DIALOG_BILL_OP_CONFIRM = 5;
	
	private MyLifeListAdapter myLifeListAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		File tempRootDir = MyApplication.getInstance().getExternalStorageCacheRoot("MemberCard");
		if (tempRootDir != null) {
			mAvatorFile = new File(tempRootDir, ".avatorTemp");
		}
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				DebugUtils.logD(TAG, "handleMessage() loadFapiaoFromCameraAsync");
				updateAvatorAsync();
			}
			
		};
		mPullRefreshListView.setBackgroundResource(R.color.wt);
		Drawable divider = new ColorDrawable(0x19000000);
		mListView.setDivider(divider);
		mListView.setDividerHeight((int) (15*MyApplication.getInstance().mDisplayMetrics.density));
		
		if (savedInstanceState != null) {
			mNeedUpdateAvatorFromCamera = savedInstanceState.getBoolean("mNeedUpdateAvatorFromCamera");
			mNeedUpdateAvatorFromGallery = savedInstanceState.getBoolean("mNeedUpdateAvatorFromGallery");
			long lifeObjectId = savedInstanceState.getLong(MyLifeObject.PROJECTION[MyLifeObject.INDEX_ID], -1);
			if (lifeObjectId != -1) {
				mTempLifeObject = MyLifeObject.getFromDatabaseWithId(lifeObjectId);
			}
			
			DebugUtils.logD(TAG, "onCreate savedInstanceState != null, get mNeedUpdateAvatorFromCamera " + mNeedUpdateAvatorFromCamera + ", mNeedUpdateAvatorFromGallery " + mNeedUpdateAvatorFromGallery + ", lifeObjectId=" + lifeObjectId);
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
		
		DebugUtils.logD(TAG, "onResume() mNeedUpdateAvatorFromCamera=" + mNeedUpdateAvatorFromCamera + ", mNeedUpdateAvatorFromGallery=" + mNeedUpdateAvatorFromGallery);
		if (mNeedUpdateAvatorFromCamera || mNeedUpdateAvatorFromGallery) {
			DebugUtils.logD(TAG, "onResume() removeMessages REQUEST_BILL, sendEmptyMessage REQUEST_BILL");
			mHandler.removeMessages(REQUEST_UPDATE);
			mHandler.sendEmptyMessageDelayed(REQUEST_UPDATE, 500);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ModleSettings.createActionBarMenu(menu, mBundle);
		MenuItem item = menu.add(0, R.string.menu_edit_for_delete, 1, R.string.menu_edit_for_delete);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item = menu.add(0, R.string.menu_delete, 2, R.string.menu_delete);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		item = menu.add(0, R.string.menu_done, 3, R.string.menu_done);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.model_my_member_card).setVisible(!mDeletedMode);
		menu.findItem(R.string.menu_edit_for_delete).setVisible(!mDeletedMode);
		menu.findItem(R.string.menu_delete).setVisible(mDeletedMode);
		menu.findItem(R.string.menu_done).setVisible(mDeletedMode);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.model_my_member_card://新建会员卡
			showEditDialog();
			return true;
		case R.string.menu_edit_for_delete:
			mDeletedMode = true;
			myLifeListAdapter.notifyDataSetChanged();
			invalidateOptionsMenu();
			return true;
		case R.string.menu_delete:
			if (mSelectIds.size() == 0) {
				MyApplication.getInstance().showMessage(R.string.msg_no_selection_for_delete);
			} else {
				deleteShopInfoAsync();
			}
			return true;
		case R.string.menu_done:
			mDeletedMode = false;
			mSelectIds.clear();
			myLifeListAdapter.notifyDataSetChanged();
			invalidateOptionsMenu();
			return true;
		default:
			//当选择了一个Home时候，我们要设置HomeObject对象
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mUpdateAvatorTask);
		AsyncTaskUtils.cancelTask(mDownloadMemberCardImageTask);
		AsyncTaskUtils.cancelTask(mQueryShopTask);
		AsyncTaskUtils.cancelTask(mSaveShopAsyncTask);
	}
	
	private void showEditDialog() {
		final EditText input = new EditText(mContext);
		input.setMinLines(2);
		input.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
		.setTitle(R.string.msg_input_phone_title)
		.setView(input)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					queryShopInfoAsync(input.getText().toString().trim());
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
	
	@Override
	public Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_BILL_OP_CONFIRM:
			return new AlertDialog.Builder(mContext)
			.setItems(this.getResources().getStringArray(R.array.avator_op_items), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
					case 0:
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempLifeObject.mMemberCardImage))), "image/*");
						Intents.launchIntent(mContext, intent);
//						Intents.openURL(mContext, Uri.fromFile(MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempInfoAdapterViewHolder.myLifeObject.mMemberCardImage))).toString());
						break;
					case 1:
						mPictureRequest = REQUEST_MEMBER_CARD_IMAGE;
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

	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		myLifeListAdapter = new MyLifeListAdapter(mContext, null, false);
		return new AdapterWrapper<MyLifeListAdapter>(myLifeListAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		return contentResolver.query(BjnoteContent.MyLife.CONTENT_URI, MyLifeObject.PROJECTION, MyLifeObject.SELECTION_UID, new String[]{MyAccountManager.getInstance().getCurrentAccountUid()}, MyLifeObject.PROJECTION[MyLifeObject.INDEX_SID] + " desc");
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
		//{"StatusCode":"1","StatusMessage":"成功","Data":{"total":0,"rows":[]}}
		List<MyLifeObject> list = new LinkedList<MyLifeObject>();
		ServiceResultObject serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
		if (serviceResultObject.isOpSuccessfully()) {
			try {
				pageInfo.mTotalCount = serviceResultObject.mJsonData.getInt("total");
				list = MyLifeObject.parseList(serviceResultObject.mJsonData.getJSONArray("rows"));
			} catch (JSONException e) {
				e.printStackTrace();
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
		JSONObject queryObject = new JSONObject();
		try {
			queryObject.put("uid", MyAccountManager.getInstance().getCurrentAccountUid());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		query.qServiceUrl = ServiceObject.getAllRelatedShops("para", queryObject.toString());
		return query;
	}

	@Override
	protected void onRefreshStart() {
		
	}

	@Override
	protected void onRefreshEnd() {
		mContext.getContentResolver().delete(BjnoteContent.MyLife.CONTENT_URI, MyLifeObject.SELECTION_UID, new String[]{MyAccountManager.getInstance().getCurrentAccountUid()});
	}

	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		if (!MyAccountManager.getInstance().hasLoginned()) {
			DebugUtils.logE(TAG, "checkIntent failed, login out.");
			return false;
		}
		mBundle = intent.getExtras();
		return true;
	}
	
	public static void startActivity(Context context, Bundle options) {
		Intent intent = new Intent(context, MyLifeMainActivity.class);
		if (options != null) {
			intent.putExtras(options);
		}
		context.startActivity(intent);
	}
	
	public class MyLifeListAdapter extends CursorAdapter {
		private String _format_xiaofei_jf, _format_free_jf;

		public MyLifeListAdapter(Context context, Cursor c, boolean autoRefresh) {
			super(context, c, autoRefresh);
			PhotoManagerUtilsV2.getInstance().requestToken(TOKEN);
			_format_xiaofei_jf = context.getString(R.string.format_xiaofei_jf);
			_format_free_jf = context.getString(R.string.format_free_jf);
		}
		public MyLifeListAdapter(Context context, Cursor c) {
			this(context, c, false);
		}

		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			InfoAdapterViewHolder viewHodler = (InfoAdapterViewHolder) view.getTag();
			viewHodler.myLifeObject = MyLifeObject.getFromCursor(cursor);
			
			viewHodler.mComName.setText(viewHodler.myLifeObject.mComName);
			
			if (!TextUtils.isEmpty(viewHodler.myLifeObject.mLiveInfo)) {
				viewHodler.mMarqueeTextView.setVisibility(View.VISIBLE);
				viewHodler.mMarqueeTextView.setText(viewHodler.myLifeObject.mLiveInfo);
				viewHodler.mMarqueeTextView.startFor0();
			} else {
				viewHodler.mMarqueeTextView.setVisibility(View.GONE);
				viewHodler.mMarqueeTextView.stopScroll();
			}
			if (MyApplication.getInstance().hasExternalStorage()) {
				if (!TextUtils.isEmpty(viewHodler.myLifeObject.mShopImage)) {
					PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TOKEN, viewHodler.qAvator, viewHodler.myLifeObject.mShopImage, null, PhotoManagerUtilsV2.TaskType.ShopImage);
				}
			}
			
			viewHodler.mJF.setText(String.format(_format_free_jf, viewHodler.myLifeObject.mFreeJifen));
			viewHodler.mTotalXiaoFei.setText(String.format(_format_xiaofei_jf, viewHodler.myLifeObject.mTotalXiaofeiJifen));
			
			
			if (mDeletedMode) {
				viewHodler.mCheckBox.setVisibility(View.VISIBLE);
				viewHodler.mRightPanel.setVisibility(View.GONE);
				Boolean checked = mSelectIds.get(viewHodler.myLifeObject.mShopID);
				if (checked == null) {
					checked = false;
				}
				viewHodler.mCheckBox.setChecked(checked);
			} else {
				viewHodler.mCheckBox.setVisibility(View.GONE);
				viewHodler.mRightPanel.setVisibility(View.VISIBLE);
				viewHodler.mComTelBtn.setTag(viewHodler);
				viewHodler.mMemberCardBtn.setTag(viewHodler);
				viewHodler.mComLocationBtn.setTag(viewHodler);
				
			}
			
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.mylife_list_item, parent, false);
			InfoAdapterViewHolder viewHodler = new InfoAdapterViewHolder();
			viewHodler.qAvator = (ImageView) view.findViewById(R.id.avator);
			viewHodler.mComName = (TextView) view.findViewById(R.id.name);
			viewHodler.mComTelBtn = (ImageView) view.findViewById(R.id.tel);
			viewHodler.mMemberCardBtn = (ImageView) view.findViewById(R.id.camera);
			viewHodler.mComLocationBtn = (ImageView) view.findViewById(R.id.location);
			viewHodler.mJF = (TextView) view.findViewById(R.id.free_jf);
			viewHodler.mTotalXiaoFei = (TextView) view.findViewById(R.id.xiaofei_jf);
			viewHodler.mMarqueeTextView = (MarqueeTextView) view.findViewById(R.id.guanggao);
			
			viewHodler.mRightPanel = view.findViewById(R.id.right_panel);
			viewHodler.mJfLayout = view.findViewById(R.id.jf_layout);
			viewHodler.mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
			
			viewHodler.mComTelBtn.setOnClickListener(MyLifeMainActivity.this);
			viewHodler.mMemberCardBtn.setOnClickListener(MyLifeMainActivity.this);
			viewHodler.mComLocationBtn.setOnClickListener(MyLifeMainActivity.this);
			
			view.setTag(viewHodler);
			return view;
		}
		
		public void release() {
			PhotoManagerUtilsV2.getInstance().releaseToken(TOKEN);
		}
	}
	
	public static class InfoAdapterViewHolder {
		private ImageView qAvator;
		private ImageView mComTelBtn, mMemberCardBtn, mComLocationBtn;
		private TextView mComName, mComAddress, mJF, mGuanggao, mTotalXiaoFei;
		private View mJfLayout, mRightPanel;
		private CheckBox mCheckBox;
		private MarqueeTextView mMarqueeTextView;
		public MyLifeObject myLifeObject = new MyLifeObject();
	}

	@Override
	public void onClick(View v) {
		Object object = v.getTag();
		if (object != null && object instanceof InfoAdapterViewHolder) {
			InfoAdapterViewHolder infoAdapterViewHolder = (InfoAdapterViewHolder) object;
			switch(v.getId()) {
			case R.id.camera:
				mTempLifeObject = infoAdapterViewHolder.myLifeObject;
				if (TextUtils.isEmpty(mTempLifeObject.mMemberCardImage)) {
					//还没有上传过会员卡,我们直接调用相机
					mPictureRequest = REQUEST_MEMBER_CARD_IMAGE;
					onCapturePhoto();
				} else {
					File cardImageFile = MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempLifeObject.mMemberCardImage));
					if (cardImageFile.exists()) {
						//如果有，我们显示操作选项，查看或是拍摄发票
						onCreateDialog(DIALOG_BILL_OP_CONFIRM).show();
					} else {
						//下载并显示
						downloadMemberCardImageAsync();
					}
				}
				break;
			case R.id.tel:
				if (infoAdapterViewHolder.myLifeObject.mComCell.length == 1) {
					Intents.callPhone(mContext, infoAdapterViewHolder.myLifeObject.mComCell[0]);
				} else {
					//多余一个电话，我们需要弹框供用户选择
					popPhoneChoose(infoAdapterViewHolder.myLifeObject.mComCell);
				}
				
				break;
			case R.id.location:
				//lat<纬度>,lng<经度> 39.916979519873,116.41004950566&title=我的位置&content=百度奎科大厦&src=appName
				StringBuilder sb = new StringBuilder();
				sb.append(infoAdapterViewHolder.myLifeObject.mLatitude).append(",").append(infoAdapterViewHolder.myLifeObject.mLongitude)
				.append("&title=").append(infoAdapterViewHolder.myLifeObject.mComName)
				.append("&content=").append(infoAdapterViewHolder.myLifeObject.mAddress).append("&src=").append(MyApplication.PKG_NAME);
				Intents.locationBaiduMap(mContext, sb.toString());
				break;
			}
		}
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mDeletedMode) {
			InfoAdapterViewHolder viewHolder = (InfoAdapterViewHolder) view.getTag();
			boolean checked = !viewHolder.mCheckBox.isChecked();
			if (!checked) {
				mSelectIds.remove(viewHolder.myLifeObject.mShopID);
			} else {
				mSelectIds.put(viewHolder.myLifeObject.mShopID, checked);
			}
			viewHolder.mCheckBox.setChecked(checked);
		}
		
	}
	
	private void popPhoneChoose(final String[] phones) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.msg_choose_phone_title);
		builder.setItems(phones, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intents.callPhone(mContext, phones[which]);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				DebugUtils.logD(TAG, "click cancelBtn mPendingCloudContacts.clear()");
			}
			
		})
		.show();
	}
	
	
	@Override
	public void onPickFromGalleryStart() {
		mAvatorUri = null;
		mNeedUpdateAvatorFromGallery = false;
		pickFromGallery();
	}
	
	@Override
	public void onPickFromCameraStart() {
		if (mAvatorFile != null && mAvatorFile.exists()) {
			mAvatorFile.delete();
		}
		mNeedUpdateAvatorFromCamera = false;
		pickFromCamera(mAvatorFile);
	}
	
	@Override
	public void onPickFromGalleryFinish(Uri data, int requestCode) {
		DebugUtils.logD(TAG, "onPickFromGalleryFinish() mNeedUpdateAvatorFromGallery " + mNeedUpdateAvatorFromGallery + ", mAvatorUri " + data);
		if (data != null) {
			mAvatorUri = data;
			mNeedUpdateAvatorFromGallery = true;
		}
	}
	
	@Override
	public void onPickFromCameraFinish(int requestCode) {
		DebugUtils.logD(TAG, "onPickFromCameraFinish() mNeedUpdateAvatorFromCamera " + mNeedUpdateAvatorFromCamera + ", mAvatorFile " + mAvatorFile.getAbsolutePath());
		if (mAvatorFile.exists()) {
			mNeedUpdateAvatorFromCamera = true;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("mNeedUpdateAvatorFromCamera", mNeedUpdateAvatorFromCamera);
		outState.putBoolean("mNeedUpdateAvatorFromGallery", mNeedUpdateAvatorFromGallery);
		long myLifeObjectId = mTempLifeObject == null?-1:mTempLifeObject.mId;
		outState.putLong(MyLifeObject.PROJECTION[MyLifeObject.INDEX_ID], myLifeObjectId);
		
		DebugUtils.logD(TAG, "onSaveInstanceState() save mNeedUpdateAvatorFromCamera " + mNeedUpdateAvatorFromCamera + ", mNeedUpdateAvatorFromGallery " + mNeedUpdateAvatorFromGallery+" ,myLifeObjectId="+myLifeObjectId);
	}
	
	
	private MyLifeObject mTempLifeObject;
	private UpdateAvatorTask mUpdateAvatorTask;
	private void updateAvatorAsync() {
		if (mTempLifeObject == null) {
			return;
		}
		AsyncTaskUtils.cancelTask(mUpdateAvatorTask);
		showDialog(DIALOG_PROGRESS);
		mUpdateAvatorTask = new UpdateAvatorTask();
		mUpdateAvatorTask.execute();
	}
	private class UpdateAvatorTask extends AsyncTask<Void, Void, ServiceResultObject> {

		private Bitmap _newBitmap;
		@Override
		protected ServiceResultObject doInBackground(Void... arg0) {
			ServiceResultObject haierResultObject = new ServiceResultObject();
			InputStream is = null;
			try {
				if (mNeedUpdateAvatorFromCamera) {
					_newBitmap = ImageHelper.getSmallBitmap(mAvatorFile.getAbsolutePath(), mAvatorWidth, mAvatorHeight);
					mNeedUpdateAvatorFromCamera = false;
				} else if (mNeedUpdateAvatorFromGallery) {
					mNeedUpdateAvatorFromGallery = false;
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					_newBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mAvatorUri), null, options); 
					
					options.inSampleSize = ImageHelper.calculateInSampleSize(options, mAvatorWidth, mAvatorWidth);
					// Decode bitmap with inSampleSize set
				    options.inJustDecodeBounds = false;
				    
				    _newBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mAvatorUri), null, options); 
				}
				JSONObject queryObject = new JSONObject();
				queryObject.put("shopid", String.valueOf(mTempLifeObject.mShopID));
				queryObject.put("uid", MyAccountManager.getInstance().getCurrentAccountUid());
				queryObject.put("imgstr", ImageHelper.bitmapToString(_newBitmap, 65));
				is = NetworkUtils.openPostContectionLocked(ServiceObject.getPostUpdateMemberCardImageUrl(),"para", queryObject.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
				if (is != null) {
					haierResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
					if (haierResultObject.isOpSuccessfully()) {
						mTempLifeObject.mMemberCardImage = haierResultObject.mStrData;
						ContentValues values = new ContentValues();
						values.put(MyLifeObject.PROJECTION[MyLifeObject.INDEX_SHOP_MEMBER_CARD_IAMGE], haierResultObject.mStrData);
						int updated = BjnoteContent.update(MyApplication.getInstance().getContentResolver(), BjnoteContent.MyLife.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mTempLifeObject.mId)});
						DebugUtils.logD(TAG, "UpdateAvatorTask  updated " + updated + ", addr=" + haierResultObject.mStrData);
						
						File cardImageFile = MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempLifeObject.mMemberCardImage));
						if (mAvatorFile.exists()) {
							FilesUtils.saveFile(new FileInputStream(mAvatorFile), cardImageFile);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			} catch (IOException e) {
				e.printStackTrace();
				haierResultObject.mStatusMessage = e.getMessage();
			}
			return haierResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			if (result.isOpSuccessfully()) {
				myLifeListAdapter.getCursor().requery();
				myLifeListAdapter.notifyDataSetChanged();
			}
			mTempLifeObject = null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
			mTempLifeObject = null;
		}
		
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		DebugUtils.logD(TAG, "onActivityResult() requestCode=" + requestCode + ", resultCode=" + resultCode);
		if (resultCode == Activity.RESULT_OK) {
			if (REQUEST_MEMBER_CARD_IMAGE == requestCode) {
                if (mAvatorFile.exists()) {
                	mNeedUpdateAvatorFromCamera = true;
				}
                return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 调用相机拍摄图片
	 */
	private void onCapturePhoto() {
		if (!MyApplication.getInstance().hasExternalStorage()) {
			showDialog(DIALOG_MEDIA_UNMOUNTED);
			return;
		}
		 if (mAvatorFile != null && mAvatorFile.exists()) {
			 mAvatorFile.delete();
		}
		Intent intent = null;
		if (mPictureRequest == REQUEST_MEMBER_CARD_IMAGE) {
			intent = ImageHelper.createCaptureIntent(Uri.fromFile(mAvatorFile));
		}
		startActivityForResult(intent, mPictureRequest);
	}
	
	private DownloadMemberCardImageTask mDownloadMemberCardImageTask;
	private void downloadMemberCardImageAsync() {
		if (!MyApplication.getInstance().hasExternalStorage()) {
			showDialog(DIALOG_MEDIA_UNMOUNTED);
			return;
		}
		AsyncTaskUtils.cancelTask(mDownloadMemberCardImageTask);
		showDialog(DIALOG_PROGRESS);
		mDownloadMemberCardImageTask = new DownloadMemberCardImageTask();
		mDownloadMemberCardImageTask.execute();
	}
	
	private class DownloadMemberCardImageTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			InputStream is = null;
			try {
				is = NetworkUtils.openContectionLocked(mTempLifeObject.mMemberCardImage, MyApplication.getInstance().getSecurityKeyValuesObject());
				return PhotoManagerUtilsV2.createCachedBitmapFile(is, MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempLifeObject.mMemberCardImage)));
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
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			if (result) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(MyLifeObject.getImageCachedFile(MyLifeObject.getMemberCardImagePhotoIdFromAddr(mTempLifeObject.mMemberCardImage))), "image/*");
				Intents.launchIntent(mContext, intent);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			dismissDialog(DIALOG_PROGRESS);
		}
		
	}
	
	private QueryShopTask mQueryShopTask;
	private void queryShopInfoAsync(String queryPhone) {
		AsyncTaskUtils.cancelTask(mQueryShopTask);
		mQueryShopTask = new QueryShopTask();
		mQueryShopTask.execute(queryPhone);
		showDialog(DIALOG_PROGRESS);
	}
	private class QueryShopTask extends AsyncTask<String, Void, ServiceResultObject> {
		MyLifeObject _myLifeObject;
		@Override
		protected ServiceResultObject doInBackground(String... params) {
			InputStream is = null;
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			try {
				JSONObject queryObject = new JSONObject();
				queryObject.put("admin_code", ComPreferencesManager.getInstance().mPreferManager.getString("admincode", ""));
				queryObject.put("cell", params[0]);
				queryObject.put("uid", MyAccountManager.getInstance().hasLoginned()?MyAccountManager.getInstance().getCurrentAccountUid():"");
				is = NetworkUtils.openContectionLocked(ServiceObject.getShopCellJianKongUrl("para", queryObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					_myLifeObject = MyLifeObject.parse(serviceResultObject.mJsonData);
					if (_myLifeObject == null) {
						serviceResultObject.mStatusCode = 0;
						serviceResultObject.mStatusMessage = mContext.getString(R.string.msg_input_phone_cant_find_shopinfo);
					}
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
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				//查询成功，我们弹框让用户确定添加
				showConfirmRelatedToShopDialog(_myLifeObject);
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
	
	
	private void showConfirmRelatedToShopDialog(final MyLifeObject myLifeObject) {
		new AlertDialog.Builder(mContext)
		.setMessage(mContext.getString(R.string.format_msg_input_phone_find_shopinfo_need_confirm, myLifeObject.toFriendlyString()))
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveShopInfoAsync(myLifeObject);
				
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.show();
	}
	
	private SaveShopAsyncTask mSaveShopAsyncTask;
	private void saveShopInfoAsync(final MyLifeObject myLifeObject) {
		AsyncTaskUtils.cancelTask(mSaveShopAsyncTask);
		mSaveShopAsyncTask = new SaveShopAsyncTask();
		mSaveShopAsyncTask.execute(myLifeObject);
		showDialog(DIALOG_PROGRESS);
	}
	private class SaveShopAsyncTask extends AsyncTask<MyLifeObject, Void, ServiceResultObject> {

		private Uri _contactUrl;
		private int _which = 0;
		@Override
		protected ServiceResultObject doInBackground(MyLifeObject... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			MyLifeObject myLifeObject = params[0];
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
			dismissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				if (_contactUrl != null) {
					AddrBookUtils.getInstance().viewContact(_contactUrl);
				} else {
					MyApplication.getInstance().showMessage(R.string.save_success);
					myLifeListAdapter.getCursor().requery();
					myLifeListAdapter.notifyDataSetChanged();
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
	
	
	
	
	
	private HashMap<Long, Boolean> mSelectIds = new HashMap<Long, Boolean>();
	private boolean mDeletedMode = false;
	private DeleteShopAsyncTask mDeleteShopAsyncTask;
	private void deleteShopInfoAsync() {
		AsyncTaskUtils.cancelTask(mSaveShopAsyncTask);
		mDeleteShopAsyncTask = new DeleteShopAsyncTask();
		mDeleteShopAsyncTask.execute();
		showDialog(DIALOG_PROGRESS);
	}
	private class DeleteShopAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
		protected ServiceResultObject doInBackground(Void... params) {
			ServiceResultObject serviceResultObject = new ServiceResultObject();
			InputStream is = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			try {
				JSONObject queryObject = new JSONObject();
				queryObject.put("uid", MyAccountManager.getInstance().getCurrentAccountUid());
				JSONArray shopArray = new JSONArray();
				for(Long shopId : mSelectIds.keySet()) {
					shopArray.put(String.valueOf(shopId));
				}
				queryObject.put("shopid", shopArray);
				DebugUtils.logD(TAG, "DeleteShopAsyncTask queryJson para=" + queryObject.toString());
			
				DebugUtils.logD(TAG, "DeleteShopAsyncTask start deleting ShopIds from Service......");
				is = NetworkUtils.openContectionLocked(ServiceObject.getDeleteRelatedShopsUrl("para", queryObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					for(Long shopId : mSelectIds.keySet()) {
						//删除服务器上的数据成功，我们还需要删除本地的
						int deleted = BjnoteContent.delete(contentResolver, BjnoteContent.MyLife.CONTENT_URI, MyLifeObject.SHOP_ID_WHERE, new String[]{String.valueOf(shopId)});
						DebugUtils.logD(TAG, "DeleteShopAsyncTask delete local ShopId " + shopId + " " + (deleted > 0));
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
			}
			return serviceResultObject;
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			if (result.isOpSuccessfully()) {
				mDeletedMode = false;
				mSelectIds.clear();
				myLifeListAdapter.getCursor().requery();
				myLifeListAdapter.notifyDataSetChanged();
				invalidateOptionsMenu();
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
	

}
