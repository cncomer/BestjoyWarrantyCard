package com.bestjoy.app.bjwarrantycard.bx.order;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.PullToRefreshListPageActivityWithActionBar;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.ServiceResultObject;

public class OrdersListActivity extends PullToRefreshListPageActivityWithActionBar implements View.OnClickListener{
	public static final String TAG = "OrdersListActivity";
	
	private Button btn_all;
	private Button btn_unpay;
	
	private ListCursorAdapter mListCursorAdapter;
	
	private int mSelectedTextColor, mUnSelectedTextColor;
	private Drawable mSelectedTextBg, mUnSelectedTextBg;
	/**订单类型*/
    private int mOrderType = -1;
    
    private Handler mHandler;
    /**刷新界面，以便按钮能够适时灰化*/
    private static final int WHAT_UPDATE_TIME = 10;
    
    /**刷新服务器数据*/
    private static final int WHAT_REFRESH = 11;
    /**10分钟刷新一次*/
    private static final int WHAT_REFRESH_TIME_DELAY = 10 * 60 * 1000;
    private Query mQuery;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		mPullRefreshListView.setBackgroundResource(R.color.wt);
		Drawable divider = new ColorDrawable(0xffededed);
		mListView.setDivider(divider);
		mListView.setDividerHeight((int) (7*MyApplication.getInstance().mDisplayMetrics.density));
		
		FrameLayout topButtonsLayout = (FrameLayout) findViewById(R.id.content);
		View view = LayoutInflater.from(mContext).inflate(R.layout.sort_top_layout, topButtonsLayout, false);
		mSelectedTextColor = this.getResources().getColor(R.color.sort_text_color_selected);
		mUnSelectedTextColor = this.getResources().getColor(R.color.sort_text_color_unselected);
		mSelectedTextBg = this.getResources().getDrawable(R.drawable.sort_btn_selected);
		mUnSelectedTextBg = this.getResources().getDrawable(R.drawable.sort_btn_unselected);
		
		btn_all = (Button) view.findViewById(R.id.time);
		btn_all.setText(R.string.button_order_all);
		btn_all.setOnClickListener(this);
		
		btn_unpay = (Button) view.findViewById(R.id.type);
		btn_unpay.setText(R.string.button_order_complain);
		btn_unpay.setOnClickListener(this);
		
//		topButtonsLayout.addView(view);
		
		//默认显示的是全部订单TAB
		setOrderTypeTab(BaoxiuCardOrderObject.ORDER_TYPE_ALL);
		
		
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
				case WHAT_REFRESH:
					forceRefresh();
					mHandler.sendEmptyMessageDelayed(WHAT_REFRESH, WHAT_REFRESH_TIME_DELAY);
					break;
				}
			}
			
		};
		mHandler.sendEmptyMessageDelayed(WHAT_REFRESH, WHAT_REFRESH_TIME_DELAY);
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	protected boolean isNeedForceRefreshOnResume() {
		//第一次我们需要刷新
		if (ComPreferencesManager.getInstance().isFirstLaunch(TAG, true)) {
			ComPreferencesManager.getInstance().setFirstLaunch(TAG, false);
			return true;
		}
		//上次刷新后，如果超过了10分钟，我们需要重新获取服务器数据一次以便同步状态
		long nowTime = new Date().getTime();
		long lastRefreshTime = ComPreferencesManager.getInstance().mPreferManager.getLong(TAG+".refreshTime", nowTime);
		if (Math.abs(nowTime - lastRefreshTime) >= WHAT_REFRESH_TIME_DELAY) {
			DebugUtils.logD(TAG, "isNeedForceRefreshOnResume() > WHAT_REFRESH_TIME_DELAY");
			return true;
		}
		
		return false;
	}
	
	/**
	 * 设置订单类型对应的Tab
	 */
	private void setOrderTypeTab(int type) {
		mOrderType = type;
		if (mOrderType == BaoxiuCardOrderObject.ORDER_TYPE_ALL) {//全部
			btn_all.setBackgroundDrawable(mSelectedTextBg);
			btn_all.setTextColor(mSelectedTextColor);
			btn_unpay.setBackgroundDrawable(mUnSelectedTextBg);
			btn_unpay.setTextColor(mUnSelectedTextColor);
		} else if (mOrderType == BaoxiuCardOrderObject.ORDER_TYPE_COMPLAIN) {//投诉
			btn_all.setBackgroundDrawable(mUnSelectedTextBg);
			btn_all.setTextColor(mUnSelectedTextColor);
			btn_unpay.setBackgroundDrawable(mSelectedTextBg);
			btn_unpay.setTextColor(mSelectedTextColor);
		}
		getQuery();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mListCursorAdapter != null) {
			mListCursorAdapter.changeCursor(null);
			mListCursorAdapter = null;
		}
		mHandler.removeMessages(WHAT_REFRESH);
		mHandler.removeMessages(WHAT_UPDATE_TIME);	
	}
	
	
	public static void startActivity(Context context) {
		Intent intent = new Intent(context, OrdersListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.time:
			setOrderTypeTab(BaoxiuCardOrderObject.ORDER_TYPE_ALL);
			forceRefresh();
			break;
		case R.id.type:
//			setOrderTypeTab(BaoxiuCardOrderObject.ORDER_TYPE_COMPLAIN);
//			forceRefresh();
			MyApplication.getInstance().showUnsupportMessage();
			break;
		}
	}

	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mListCursorAdapter = new ListCursorAdapter(mContext, null, true);
		return new AdapterWrapper<CursorAdapter>(mListCursorAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		String select = BaoxiuCardOrderObject.SELECTION_UID;
		
		String[] selectArgs = new String[]{String.valueOf(mOrderType)};
		if (mOrderType != BaoxiuCardOrderObject.ORDER_TYPE_ALL) {
			select = BaoxiuCardOrderObject.SELECTION_UID + " and " + BaoxiuCardOrderObject.SELECTION_STATUS;
			selectArgs = new String[]{MyAccountManager.getInstance().getCurrentAccountUid(), String.valueOf(mOrderType)};
		} else {
			selectArgs  = new String[]{MyAccountManager.getInstance().getCurrentAccountUid()};
		}
		return BaoxiuCardOrderObject.getLocalAllOrders(select, selectArgs);
	}

	@Override
	protected int savedIntoDatabase(ContentResolver cr, List<? extends InfoInterface> infoObjects) {
		int insertOrUpdateCount = 0;
		if (infoObjects != null) {
			for(InfoInterface object:infoObjects) {
				if (object.saveInDatebase(cr, null)) {
					insertOrUpdateCount++;
				}
			}
		}
		return insertOrUpdateCount;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		ServiceResultObject serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
		if (serviceResultObject.isOpSuccessfully()) {
			try {
				serviceResultObject.mJsonArrayData = serviceResultObject.mJsonData.getJSONArray("rows");
				pageInfo.mTotalCount = serviceResultObject.mJsonData.optInt("total", 0);
				return BaoxiuCardOrderObject.parse(serviceResultObject.mJsonArrayData);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return new ArrayList<BaoxiuCardOrderObject>();
	}

	@Override
	protected Query getQuery() {
		if (mQuery == null) {
			mQuery =  new Query();
			mQuery.mPageInfo = new PageInfo();
		}
		mQuery.qServiceUrl = ServiceObject.getBXOrdersUrl("para", getFilterServiceUrl());
		return mQuery;
	}
	
	private String getFilterServiceUrl() {
		try {
			JSONObject queryJsonObject = new JSONObject();
			queryJsonObject.put("uid", MyAccountManager.getInstance().getCurrentAccountUid());
			queryJsonObject.put("filter", mOrderType);
			queryJsonObject.put("pageindex", mQuery.mPageInfo.mPageIndex);
			queryJsonObject.put("pagesize", mQuery.mPageInfo.mPageSize);
			return queryJsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onRefreshEnd() {
		BaoxiuCardOrderObject.deleteCachedAllOrdersForUid(MyAccountManager.getInstance().getCurrentAccountId());
	}
	
	@Override
	protected void onRefreshStart() {
		ComPreferencesManager.getInstance().mPreferManager.edit().putLong(TAG+".refreshTime", new Date().getTime()).commit();
	}

	@Override
	protected void onRefreshPostEnd() {
	}

	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}
	
//	private DeleteBillTask mDeleteBillTask;
//	private void deleteBillAsync(BaoxiuCardOrderObject billObject) {
//		AsyncTaskUtils.cancelTask(mDeleteBillTask);
//		mDeleteBillTask = new DeleteBillTask(billObject);
//		mDeleteBillTask.execute();
//		showDialog(DIALOG_PROGRESS);
//	}
//	private class DeleteBillTask extends AsyncTask<Void, Void, ServiceResultObject> {
//
//		private BaoxiuCardOrderObject _billObject;
//		public DeleteBillTask(BaoxiuCardOrderObject billObject) {
//			_billObject = billObject;
//		}
//		
//		@Override
//		protected ServiceResultObject doInBackground(Void... params) {
//			InputStream is = null;
//			ServiceResultObject serviceResultObject = new ServiceResultObject();
//			try {
//				JSONObject jsonQuerry = new JSONObject();
//				jsonQuerry.put("OrderID", _billObject.getBillNumber());
//				jsonQuerry.put("Uid", MyAccountManager.getInstance().getCurrentAccountUid());
//				is = NetworkUtils.openContectionLocked(ServiceObject.getDeleteBillUrl("para", jsonQuerry.toString()), MyApplication.getInstance().getSecurityKeyValuesObject());
//				serviceResultObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
//				if (serviceResultObject.isOpSuccessfully()) {
//					//删除成功，我们还要删除本地的数据
//					boolean deleted =BillListManager.deleteBillByNumber(getContentResolver(), _billObject.getBillNumber());
//					DebugUtils.logD(TAG, "DeleteBillTask delete local bill " + _billObject.getBillNumber() + ", deleted " + deleted);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//				serviceResultObject.mStatusMessage = e.getMessage();
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//				serviceResultObject.mStatusMessage = e.getMessage();
//			} catch (IOException e) {
//				e.printStackTrace();
//				serviceResultObject.mStatusMessage = e.getMessage();
//			} finally {
//				NetworkUtils.closeInputStream(is);
//			}
//			return serviceResultObject;
//		}
//
//		@Override
//		protected void onPostExecute(ServiceResultObject result) {
//			super.onPostExecute(result);
//			dismissDialog(DIALOG_PROGRESS);
//		}
//
//		@Override
//		protected void onCancelled() {
//			super.onCancelled();
//			dismissDialog(DIALOG_PROGRESS);
//		}
//		
//	}
	public static  DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy年MM月dd HH:mm");
	private class ListCursorAdapter extends CursorAdapter {
		private String[] mBxStatuTextArray;
		private String mProductTypeFormat, mProductXinghaoFormat, mServerTypeFormat, mOrderStatuFormat;
		private String getStatusText(int type) {
			return mBxStatuTextArray[type];
		}

		public ListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
			mBxStatuTextArray = context.getResources().getStringArray(R.array.bx_order_statu_text);
			mProductTypeFormat = context.getString(R.string.bx_order_product_type);
			mProductXinghaoFormat = context.getString(R.string.bx_order_product_xinghao);
			mServerTypeFormat = context.getString(R.string.bx_order_server_type);
			mOrderStatuFormat = context.getString(R.string.bx_order_status);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = LayoutInflater.from(mContext).inflate(R.layout.bx_order_list_item, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder._applyTime = (TextView) view.findViewById(R.id.bx_order_apply_time);
			viewHolder._pType = (TextView) view.findViewById(R.id.bx_order_product_type);
			viewHolder._pXinghao = (TextView) view.findViewById(R.id.bx_order_product_xinghao);
			viewHolder._sType = (TextView) view.findViewById(R.id.bx_order_server_type);
			viewHolder._evaluatBtn = (Button) view.findViewById(R.id.button_evaluate);
			viewHolder._statu = (TextView) view.findViewById(R.id.bx_order_status);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			
			viewHolder._baoxiuCardOrderObject = BaoxiuCardOrderObject.getBaoxiuCardOrderObjectFromCursor(cursor);
			viewHolder._applyTime.setText(DATE_TIME_FORMAT.format(new Date(viewHolder._baoxiuCardOrderObject.mApplyTime)));
			
			if (viewHolder._baoxiuCardOrderObject.mStatus == BaoxiuCardOrderObject.ORDER_TYPE_FINISH && viewHolder._baoxiuCardOrderObject.mEvaluated == 0) {
				//只有已结单才能进行评价
				viewHolder._evaluatBtn.setVisibility(View.VISIBLE);
			} else {
				viewHolder._evaluatBtn.setVisibility(View.INVISIBLE);
			}
			
			viewHolder._pType.setText(String.format(mProductTypeFormat, viewHolder._baoxiuCardOrderObject.mLeiXin));
			viewHolder._pXinghao.setText(String.format(mProductXinghaoFormat, viewHolder._baoxiuCardOrderObject.mXingHao));
			viewHolder._sType.setText(String.format(mServerTypeFormat, viewHolder._baoxiuCardOrderObject.mPinPai));
			
			SpannableStringBuilder sb = new SpannableStringBuilder();
			sb.append(mOrderStatuFormat).append(getStatusText(viewHolder._baoxiuCardOrderObject.mStatus));
			sb.setSpan(new ForegroundColorSpan(Color.BLUE), mOrderStatuFormat.length(), sb.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			viewHolder._statu.setText(sb.toString());
			
		}
		
	}
	
	private class ViewHolder {
		private TextView _applyTime, _pType, _pXinghao, _sType, _statu;
		private Button _evaluatBtn;
		private BaoxiuCardOrderObject _baoxiuCardOrderObject;
	}
}
