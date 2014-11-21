package com.bestjoy.app.warrantycard.view;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.internal.nineoldandroids.animation.ValueAnimator;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.bjwarrantycard.im.ConversationListActivity;
import com.bestjoy.app.bjwarrantycard.im.RelationshipObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.CarBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.IBaoxiuCardObject;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.ui.CaptureActivity;
import com.bestjoy.app.warrantycard.utils.VcfAsyncDownloadUtils;
import com.bestjoy.app.warrantycard.utils.VcfAsyncDownloadUtils.VcfAsyncDownloadHandler;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.Contents;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.SecurityUtils;

public class BaoxiuCardSalemanInfoView extends RelativeLayout implements View.OnClickListener, OnLongClickListener{

	private static final String TAG = "BaoxiuCardViewSalemanInfoView";
	private ImageView mAvator;
	private TextView mName, mTitle;
	
	private ValueAnimator mAnim;
	private Handler mHandler;
	private Fragment mFragment;
	
	public static final int TYPE_MM_ONE = 1;
	public static final int TYPE_MM_TWO = 2;
	
	private int mMMType = 0;
	private String mToken = TAG;
	
	private boolean mIsDownload = false;
	
	private Dialog mActionsDialog; 
	
	private VcfAsyncDownloadHandler mVcfAsyncDownloadAndUpdateSalesInfoHandler;
	
	private class Person {
		private String _mm;
		private RelationshipObject _relationshipObject;
		private AddressBookParsedResult _addressBookParsedResult;
		private IBaoxiuCardObject _baoxiuCardObject;
	}
	
	private Person mSalesPerson;
	
	public void setParantFragment(Fragment fragment) {
		mFragment = fragment;
	}
	
	public BaoxiuCardSalemanInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (isInEditMode()) {
			return;
		}
			
		mVcfAsyncDownloadAndUpdateSalesInfoHandler = new VcfAsyncDownloadHandler() {

			@Override
			public void onDownloadStart() {
				//实现该方法忽略默认的下载中提示信息
				mIsDownload = true;
				MyApplication.getInstance().showMessage(R.string.msg_download_mm_wait);
			}

			@Override
			public void onDownloadFinished(
					AddressBookParsedResult addressBookParsedResult,
					String outMsg) {
				mIsDownload = false;
				if (addressBookParsedResult != null) {
					//update bid and aid
					UpdateSalesInfoAsyncTask task = new UpdateSalesInfoAsyncTask(addressBookParsedResult);
		   			task.execute();
				}
			}

			@Override
			public boolean onDownloadFinishedInterrupted() {
				return true;
			}
			
		};
	}
	
	private Runnable mHideActionRunnable =  new Runnable() {

		@Override
		public void run() {
			if (mActionsDialog.isShowing()) {
				mActionsDialog.dismiss();
			}
		}
		
	};
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (isInEditMode()) {
			return;
		}
		
		mAvator = (ImageView) findViewById(R.id.avator);
		mAvator.setOnClickListener(this);
		mAvator.setOnLongClickListener(this);
		mName = (TextView) findViewById(R.id.name);
		mName.setText("");
		mTitle = (TextView) findViewById(R.id.title); 
		mHandler = new Handler();
		
		
	}
	
	public void updateView() {
		if (mSalesPerson != null && mSalesPerson._relationshipObject != null) {
			mName.setText(mSalesPerson._relationshipObject.mTargetName);
			mTitle.setText(mSalesPerson._relationshipObject.mTargetTitle);
			
			PhotoManagerUtilsV2.getInstance().loadPhotoAsync(mToken, mAvator, mSalesPerson._mm, mSalesPerson._addressBookParsedResult != null ?mSalesPerson._addressBookParsedResult.getPhoto():null, PhotoManagerUtilsV2.TaskType.PREVIEW);
		} else {
			mAvator.setImageResource(R.drawable.baoxiuka_avator_default_scan);
			mName.setText("");
			if (mMMType == TYPE_MM_ONE) {
				mTitle.setText(R.string.salesman_title);
			} else if (mMMType == TYPE_MM_TWO) {
				mTitle.setText(R.string.serverman_title);
			}
			
		}
	}
	
	public void setMmType(int mmType) {
		mMMType = mmType;
	}
	
	public void setSalesPersonInfo(IBaoxiuCardObject baoxiuCardObjectult, String token) {
		
		mSalesPerson = new Person();
		mToken = token;
		mSalesPerson._addressBookParsedResult = null;
		mSalesPerson._baoxiuCardObject = baoxiuCardObjectult;
		if (baoxiuCardObjectult != null) {
			if (mMMType == TYPE_MM_ONE) {
				mSalesPerson._relationshipObject = RelationshipObject.getFromCursorByServiceId(mFragment.getActivity().getContentResolver(), String.valueOf(baoxiuCardObjectult.mUID), baoxiuCardObjectult.mMMOne);
			} else if (mMMType == TYPE_MM_TWO) {
				mSalesPerson._relationshipObject = RelationshipObject.getFromCursorByServiceId(mFragment.getActivity().getContentResolver(), String.valueOf(baoxiuCardObjectult.mUID), baoxiuCardObjectult.mMMTwo);
			}
		}
		if (mSalesPerson._relationshipObject != null) {
			mSalesPerson._mm = mSalesPerson._relationshipObject.mMM;
			updateView();
		}
		
	}
	
	public void downloadSalesPersonInfo(IBaoxiuCardObject baoxiuCardObjectult, String mm, String token) {
		mToken = token;
		mSalesPerson = new Person();
		mSalesPerson._addressBookParsedResult = null;
		mSalesPerson._baoxiuCardObject = baoxiuCardObjectult;
		mSalesPerson._relationshipObject = null;
		VcfAsyncDownloadUtils.getInstance().executeTaskSimply(mm, false, mVcfAsyncDownloadAndUpdateSalesInfoHandler,  PhotoManagerUtilsV2.TaskType.PREVIEW);
	}
	
	public boolean hasMM() {
		return mSalesPerson != null && !TextUtils.isEmpty(mSalesPerson._mm);
	}
	
	public boolean hasTarget() {
		return mSalesPerson != null && mSalesPerson._relationshipObject != null && !TextUtils.isEmpty(mSalesPerson._relationshipObject.mTarget);
	}
	
	public void setTitle(int resid) {
		mTitle.setText(resid);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.avator:
			if (mIsDownload) {
				MyApplication.getInstance().showMessage(R.string.msg_download_mm_wait);
				return;
			}
			//当已经有mm号码的时候，单击头像会pop出actions菜单；否则是进入条码扫描识别联系人信息。
			if (!hasMM()) {
				Intent scanIntent = new Intent(mFragment.getActivity(), CaptureActivity.class);
				scanIntent.putExtra(Intents.EXTRA_SCAN_TASK, true);
				mFragment.startActivityForResult(scanIntent, mMMType);
			} else {
				//pop actions
				if (mActionsDialog == null) {
					View view = LayoutInflater.from(getContext()).inflate(R.layout.saleman_info_actions, null);
					view.findViewById(R.id.button_call).setOnClickListener(this);
					view.findViewById(R.id.button_info).setOnClickListener(this);
					view.findViewById(R.id.button_shop).setOnClickListener(this);
					view.findViewById(R.id.button_sms).setOnClickListener(this);
					mActionsDialog = new AlertDialog.Builder(getContext()).show();
					mActionsDialog.getWindow().setContentView(view);
//					WindowManager.LayoutParams pl = mActionsDialog.getWindow().getAttributes();
//					pl.width = WindowManager.LayoutParams.WRAP_CONTENT;
//					pl.height = WindowManager.LayoutParams.WRAP_CONTENT;
//					pl.gravity = Gravity.CENTER;
//					mActionsDialog.getWindow().setBackgroundDrawable(null);
				}
				if (!mActionsDialog.isShowing()) {
					mActionsDialog.show();
				}
				
				mHandler.removeCallbacks(mHideActionRunnable);
				mHandler.postDelayed(mHideActionRunnable, 2000);
				
			}
			break;
		case R.id.button_call:
			if (mSalesPerson._relationshipObject != null && !TextUtils.isEmpty(mSalesPerson._relationshipObject.mTargetCell)) {
				Intents.callPhone(mFragment.getActivity(), mSalesPerson._relationshipObject.mTargetCell);
			}
			break;
		case R.id.button_info:
			if (hasMM()) {
				Intents.openURL(mFragment.getActivity(), Contents.MingDang.buildDirectCloudUri(mSalesPerson._mm));
			} else {
				DebugUtils.logD(TAG, "ignore open Contact Info page due to non-MM");
			}
			break;
		case R.id.button_sms:
			if (hasTarget()) {
				ConversationListActivity.startActivity(mFragment.getActivity(), mSalesPerson._relationshipObject);
			}
			break;
		}
		
		
	}

	@Override
	public boolean onLongClick(View v) {
		switch(v.getId()) {
		case R.id.avator:
			if (mIsDownload) {
				MyApplication.getInstance().showMessage(R.string.msg_download_mm_wait);
				return true;
			}
			if (hasMM()) {
				new AlertDialog.Builder(mFragment.getActivity())
				.setItems(R.array.delete_or_recapture, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							Intent scanIntent = new Intent(mFragment.getActivity(), CaptureActivity.class);
							scanIntent.putExtra(Intents.EXTRA_SCAN_TASK, true);
							mFragment.startActivityForResult(scanIntent, mMMType);
							break;
						case 1: //删除
							deleteSalesInfoAsync();
							break;
						}
						
					}
				})
				.setPositiveButton(android.R.string.cancel, null)
				.show();
			} else {
				Intent scanIntent = new Intent(mFragment.getActivity(), CaptureActivity.class);
				scanIntent.putExtra(Intents.EXTRA_SCAN_TASK, true);
				mFragment.startActivityForResult(scanIntent, mMMType);
			}
			return true;
		}
		return false;
	}
	
	private class UpdateSalesInfoAsyncTask extends AsyncTask<Void, Void, ServiceResultObject> {
		AddressBookParsedResult _addressBookParsedResult;
		public UpdateSalesInfoAsyncTask(AddressBookParsedResult addressBookParsedResult) {
			_addressBookParsedResult = addressBookParsedResult;
		}
		@Override
		protected ServiceResultObject doInBackground(Void... arg0) {
			
			//这里我们先尝试去下载名片信息
			ServiceResultObject serviceResultObject  = new ServiceResultObject();
			InputStream is = null;
			StringBuilder sb = new StringBuilder();
			sb.append(mSalesPerson._baoxiuCardObject.mBID).append("_").append(mSalesPerson._mm).append("_").append(mSalesPerson._mm);
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("BID", mSalesPerson._baoxiuCardObject.mBID);
				jsonObject.put("UID", mSalesPerson._baoxiuCardObject.mUID);
				jsonObject.put("MM", _addressBookParsedResult.getBid());
				jsonObject.put("type", mMMType);
				jsonObject.put("token", SecurityUtils.MD5.md5(sb.toString()));
				DebugUtils.logD(TAG, "UpdateSalesInfoAsyncTask jsonObject = " + jsonObject.toString());
				if (mSalesPerson._baoxiuCardObject instanceof BaoxiuCardObject) {
					is = NetworkUtils.openContectionLocked(ServiceObject.updateBaoxiucardSalesmanInfo("para", jsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				} else {
					is = NetworkUtils.openContectionLocked(ServiceObject.updateCarBaoxiucardSalesmanInfo("para", jsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				}
				
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					mSalesPerson._addressBookParsedResult = _addressBookParsedResult;
					mSalesPerson._mm = _addressBookParsedResult.getBid();
					if (serviceResultObject.mJsonData != null) {
						mSalesPerson._relationshipObject =  RelationshipObject.parse(serviceResultObject.mJsonData);
						
						if (mMMType == TYPE_MM_ONE) {
							mSalesPerson._baoxiuCardObject.mMMOne = mSalesPerson._relationshipObject.mRelationshipServiceId;
						} else if (mMMType == TYPE_MM_TWO) {
							mSalesPerson._baoxiuCardObject.mMMTwo = mSalesPerson._relationshipObject.mRelationshipServiceId;
						}
						boolean updated = mSalesPerson._baoxiuCardObject.saveInDatebase(MyApplication.getInstance().getContentResolver(), null);
						DebugUtils.logD(TAG, "UpdateSalesInfoAsyncTask update BaoxiuCardObject#updated " +updated);
						if (updated) {
							//保存关系数据
							mSalesPerson._relationshipObject.saveInDatebase(mFragment.getActivity().getContentResolver(), null);
							//本地更新成功，我们增加这几个值
							if (mMMType == TYPE_MM_ONE) {
								mSalesPerson._mm = mSalesPerson._relationshipObject.mMM;
							} else if (mMMType == TYPE_MM_TWO) {
								mSalesPerson._mm = mSalesPerson._relationshipObject.mMM;
							}
						}
					} else {
						serviceResultObject.mStatusCode = -1;
						serviceResultObject.mStatusMessage = mFragment.getActivity().getString(R.string.msg_get_no_content_from_server);
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
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			if (result.isOpSuccessfully()) {
				updateView();
			}
			
		}
		
		
	}
	
	private DeleteSalesInfo mDeleteSalesInfo;
	private void deleteSalesInfoAsync() {
		mFragment.getActivity().showDialog(BaseActionbarActivity.DIALOG_PROGRESS);
		AsyncTaskUtils.cancelTask(mDeleteSalesInfo);
		mDeleteSalesInfo = new DeleteSalesInfo();
		mDeleteSalesInfo.execute();
	}
	private class DeleteSalesInfo extends AsyncTask<Void, Void, ServiceResultObject> {
		@Override
		protected ServiceResultObject doInBackground(Void... arg0) {
			
			//这里我们先尝试去下载名片信息
			ServiceResultObject serviceResultObject  = new ServiceResultObject();
			InputStream is = null;
			StringBuilder sb = new StringBuilder();
			sb.append(mSalesPerson._baoxiuCardObject.mBID).append("_").append(mSalesPerson._mm).append("_").append(mSalesPerson._mm);
			try {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("bid", mSalesPerson._baoxiuCardObject.mBID);
				jsonObject.put("uid", mSalesPerson._baoxiuCardObject.mUID);
				jsonObject.put("type", mMMType);
				is = NetworkUtils.openContectionLocked(ServiceObject.deleteBaoxiucardSalesmanInfo("para", jsonObject.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
				if (serviceResultObject.isOpSuccessfully()) {
					//服务器删除成功，我们需要删除本地的关系
					ContentResolver cr = mFragment.getActivity().getContentResolver();
					int deleted = BjnoteContent.delete(cr, BjnoteContent.RELATIONSHIP.CONTENT_URI, BjnoteContent.ID_SELECTION, new String[]{mSalesPerson._relationshipObject.mRelationshipId});
					DebugUtils.logD(TAG, "DeleteSalesInfo delete RelationshipId " + mSalesPerson._relationshipObject.mRelationshipId + " deleted " + deleted);
					//删除保修卡中的相关数据
					ContentValues values = new ContentValues();
					if (mSalesPerson._baoxiuCardObject instanceof BaoxiuCardObject) {
						if (mMMType == TYPE_MM_ONE) {
							mSalesPerson._baoxiuCardObject.mMMOne = "";
							values.put(IBaoxiuCardObject.PROJECTION[BaoxiuCardObject.KEY_CARD_MMONE], "");
							int updated = BjnoteContent.update(cr, BjnoteContent.BaoxiuCard.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mSalesPerson._baoxiuCardObject.mId)});
							DebugUtils.logD(TAG, "DeleteSalesInfo update BaoxiuCardObject's MMONE id " + mSalesPerson._baoxiuCardObject.mId + " updated " + updated);
						} else if (mMMType == TYPE_MM_TWO) {
							mSalesPerson._baoxiuCardObject.mMMTwo = "";
							values.put(IBaoxiuCardObject.PROJECTION[BaoxiuCardObject.KEY_CARD_MMTWO], "");
							int updated = BjnoteContent.update(cr, BjnoteContent.BaoxiuCard.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mSalesPerson._baoxiuCardObject.mId)});
							DebugUtils.logD(TAG, "DeleteSalesInfo update BaoxiuCardObject's MMTWO id " + mSalesPerson._baoxiuCardObject.mId + " updated " + updated);
						}
					} else if (mSalesPerson._baoxiuCardObject instanceof CarBaoxiuCardObject) {
						if (mMMType == TYPE_MM_ONE) {
							mSalesPerson._baoxiuCardObject.mMMOne = "";
							values.put(IBaoxiuCardObject.PROJECTION[CarBaoxiuCardObject.KEY_CARD_MMONE], "");
							int updated = BjnoteContent.update(cr, BjnoteContent.MyCarCards.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mSalesPerson._baoxiuCardObject.mId)});
							DebugUtils.logD(TAG, "DeleteSalesInfo update CarBaoxiuCardObject's MMONE id " + mSalesPerson._baoxiuCardObject.mId + " updated " + updated);
						} else if (mMMType == TYPE_MM_TWO) {
							mSalesPerson._baoxiuCardObject.mMMTwo = "";
							values.put(IBaoxiuCardObject.PROJECTION[CarBaoxiuCardObject.KEY_CARD_MMTWO], "");
							int updated = BjnoteContent.update(cr, BjnoteContent.MyCarCards.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(mSalesPerson._baoxiuCardObject.mId)});
							DebugUtils.logD(TAG, "DeleteSalesInfo update CarBaoxiuCardObject's MMTWO id " + mSalesPerson._baoxiuCardObject.mId + " updated " + updated);
						}
					}
					mSalesPerson._addressBookParsedResult = null;
					mSalesPerson._relationshipObject = null;
					mSalesPerson._mm = "";
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
		protected void onCancelled() {
			super.onCancelled();
			mFragment.getActivity().dismissDialog(BaseActionbarActivity.DIALOG_PROGRESS);
		}

		@Override
		protected void onPostExecute(ServiceResultObject result) {
			super.onPostExecute(result);
			mFragment.getActivity().dismissDialog(BaseActionbarActivity.DIALOG_PROGRESS);
			MyApplication.getInstance().showMessage(result.mStatusMessage);
			if (result.isOpSuccessfully()) {
				updateView();
			}
			
		}
		
		
	}
	
	public void onDestroy() {
		AsyncTaskUtils.cancelTask(mDeleteSalesInfo);
	}

}
