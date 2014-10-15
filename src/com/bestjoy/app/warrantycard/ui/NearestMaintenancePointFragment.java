package com.bestjoy.app.warrantycard.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.MaintenancePointBean;
import com.bestjoy.app.warrantycard.utils.PatternMaintenanceUtils;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.shwy.bestjoy.utils.SecurityUtils;

public class NearestMaintenancePointFragment extends PullToRefreshListPageForFragment{
	private static final String TAG = "NearestMaintenancePointFragment";
	
	private MalPointAdapter mMalPointAdapter;
	private BaoxiuCardObject mBaoxiuCardObject;
	
	private Bundle mBundle;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			mBundle = getArguments();
			DebugUtils.logD(TAG, "onCreate() savedInstanceState == null, getArguments() mBundle=" + mBundle);
		} else {
			mBundle = savedInstanceState.getBundle(TAG);
			DebugUtils.logD(TAG, "onCreate() savedInstanceState != null, restore mBundle=" + mBundle);
		}
		mBaoxiuCardObject = BaoxiuCardObject.getBaoxiuCardObject(mBundle);
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.button_maintenance_point);
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
	public void onItemClick(AdapterView<?> listView, View view, int pos, long arg3) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String url = holder._maintenancePoint.getMaintenancePointUrl();
		if(!TextUtils.isEmpty(url)) {				
			BrowserActivity.startActivity(mGlobalContext, url, mGlobalContext.getString(R.string.repair_point_detail));
		} else {
			MyApplication.getInstance().showMessage(R.string.repair_point_detail_no_uri_tips);
		}
	}
	private class ViewHolder {
		private TextView _name, _detail, _distance;
		private ImageView _phone;
		private MaintenancePointBean _maintenancePoint;
	}
	public class MalPointAdapter extends CursorAdapter {

		private Context _context;
		private MalPointAdapter (Context context, Cursor cursor, boolean autoRefresh) {
			super(context, cursor, autoRefresh);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View convertView = LayoutInflater.from(_context).inflate(R.layout.nearest_point_list_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder._name = (TextView) convertView.findViewById(R.id.mal_point_name);
			holder._detail = (TextView) convertView.findViewById(R.id.mal_point_detail);
			holder._distance = (TextView) convertView.findViewById(R.id.mal_point_distance);
			holder._phone = (ImageView) convertView.findViewById(R.id.mal_point_tel);
			convertView.setTag(holder);
			return convertView;
		}
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
			holder._maintenancePoint = PatternMaintenanceUtils.getMaintenancePointFromCursor(cursor);
			Float f = 0f;
			if(!TextUtils.isEmpty((holder._maintenancePoint.getMaintenancePointDistance()))) {
				f = Float.valueOf(holder._maintenancePoint.getMaintenancePointDistance()) / 1000;
			}
			holder._name.setText(holder._maintenancePoint.getMaintenancePointName());
			holder._detail.setText(holder._maintenancePoint.getMaintenancePointDetail());
			holder._distance.setText(String.format("%.1f", f) + _context.getResources().getString(R.string.maintence_point_distance_unit));
			final String tel = holder._maintenancePoint.getMaintenancePointTel();
			holder._phone.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(!TextUtils.isEmpty(tel)) {
						Intents.callPhone(getActivity(), tel);						
					} else {
						MyApplication.getInstance().showMessage(R.string.no_tel_tips);
					}
				}
			});
		}
		
	}
	
	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mMalPointAdapter = new MalPointAdapter(getActivity(), null, false);
		return new AdapterWrapper<CursorAdapter>(mMalPointAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver contentResolver) {
		return contentResolver.query(BjnoteContent.MaintencePoint.CONTENT_URI, MaintenancePointBean.MAINTENCE_PROJECTION, MaintenancePointBean.MAINTENCE_PROJECTION_AID_BID_SELECTION, new String[]{String.valueOf(mBaoxiuCardObject.mAID), String.valueOf(mBaoxiuCardObject.mBID)}, null);
	}

	@Override
	protected int savedIntoDatabase(ContentResolver contentResolver, List<? extends InfoInterface> infoObjects) {
		int insertOrUpdateCount = 0;
		if (infoObjects != null) {
			ContentValues values = new ContentValues();
			values.put(HaierDBHelper.MAINTENCE_POINT_AID, mBaoxiuCardObject.mAID);
			values.put(HaierDBHelper.MAINTENCE_POINT_BID, mBaoxiuCardObject.mBID);
			for(InfoInterface object:infoObjects) {
				if (object.saveInDatebase(contentResolver, values)) {
					insertOrUpdateCount++;
				}
			}
		}
		return insertOrUpdateCount;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		List <MaintenancePointBean> maintenancePoint = new ArrayList<MaintenancePointBean>();
		try {
			ServiceResultObject serviceResultObject = ServiceResultObject.parseAddress(NetworkUtils.getContentFromInput(is));
			if (serviceResultObject.isOpSuccessfully()) {
				maintenancePoint = PatternMaintenanceUtils.getMaintenancePoint(serviceResultObject.mAddresses, getActivity().getContentResolver(), mBaoxiuCardObject.mAID, mBaoxiuCardObject.mBID);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		DebugUtils.logD(TAG, "mMaintenancePoint = " + maintenancePoint);
		return maintenancePoint;
	}

	@Override
	protected Query getQuery() {
		String cell = MyAccountManager.getInstance().getAccountObject().mAccountTel;
		String pwd = MyAccountManager.getInstance().getAccountObject().mAccountPwd;
		
		StringBuilder sb = new StringBuilder(ServiceObject.SERVICE_URL);
		sb.append("GetNearby.ashx?")
		.append("AID=").append(mBaoxiuCardObject.mAID)
		.append("&BID=").append(mBaoxiuCardObject.mBID)
		.append("&token=").append(SecurityUtils.MD5.md5(cell+pwd));
		DebugUtils.logD(TAG, "param " + sb.toString());
		
		Query query =  new Query();
		query.qServiceUrl = sb.toString();
		return query;
	}

	@Override
	protected void onRefreshStart() {
		PatternMaintenanceUtils.deleteCachedData(getActivity().getContentResolver(), String.valueOf(mBaoxiuCardObject.mAID), String.valueOf(mBaoxiuCardObject.mBID));
		
	}

	@Override
	protected void onRefreshEnd() {
	}

	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}
}
