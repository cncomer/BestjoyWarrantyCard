package com.bestjoy.app.bjwarrantycard.im;

import java.io.InputStream;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.BjnoteContent.RELATIONSHIP;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2.TaskType;
import com.bestjoy.app.warrantycard.ui.PullToRefreshListPageActivity;
import com.bestjoy.app.warrantycard.view.AvatorImageView;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;

public class RelationshipActivityBg extends PullToRefreshListPageActivity{
	private static final String TAG = "RelationshipActivity";
	public static final String FIRST = "RelationshipActivity.FIRST";
	private Handler mHandler;
	private static final int WHAT_REFRESH_LIST = 1000;
	private RelationshipAdapter mRelationshipAdapter;
	private boolean mIsRefresh = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing()) {
			return;
		}
		setShowHomeUp(true);
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case WHAT_REFRESH_LIST:
					mRelationshipAdapter.refreshList();
					break;
				}
			}
			
		};
		PhotoManagerUtilsV2.getInstance().requestToken(TAG);
	}
	@Override
	protected boolean isNeedForceRefreshOnResume() {
		boolean first = ComPreferencesManager.getInstance().isFirstLaunch(FIRST, true);
		if (first) {
			ComPreferencesManager.getInstance().setFirstLaunch(FIRST, false);
		}
		return first;
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		PhotoManagerUtilsV2.getInstance().releaseToken(TAG);
	}
	
	@Override
	protected AdapterWrapper<? extends BaseAdapter> getAdapterWrapper() {
		mRelationshipAdapter = new RelationshipAdapter(mContext, null, true);
		return new AdapterWrapper<CursorAdapter>(mRelationshipAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver cr) {
		return BjnoteContent.RELATIONSHIP.getAllRelationships(cr, MyAccountManager.getInstance().getCurrentAccountUid());
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
		return RelationshipObject.parseList(is, pageInfo);
	}

	@Override
	protected Query getQuery() {
		Query query =  new Query();
		query.qServiceUrl = ServiceObject.getRelationshipUrl(MyAccountManager.getInstance().getCurrentAccountUid(), MyAccountManager.getInstance().getAccountObject().mAccountPwd);
		
		return query;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
		super.onItemClick(parent, itemView, position, id);
		ViewHolder viewHolder = (ViewHolder) itemView.getTag();
		ConversationListActivity.startActivity(mContext, viewHolder._relationshipObject);
	}
	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}
	
	public static void startActivity(Context context) {
		Intent intent = new Intent(context, RelationshipActivityBg.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		
	}
	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}
	
	private class RelationshipAdapter extends CursorAdapter {

		public RelationshipAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		protected void onContentChanged() {
			if (mIsRefresh) {
				return;
			}
			mHandler.removeMessages(WHAT_REFRESH_LIST);
			mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_LIST, 250);
		}


		private void refreshList() {
			super.onContentChanged();
		}
		

		@Override
		public int getItemViewType(int position) {
			RelationshipObject object = RelationshipObject.getFromCursor((Cursor)getItem(position));
			return object.mIsServiceUser;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return super.getView(position, convertView, parent);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = null;
			ViewHolder viewHolder = new ViewHolder();
			if (cursor.getInt(RELATIONSHIP.INDEX_RELASTIONSHIP_TARGET_IS_SERVER) == 0) {
				view = LayoutInflater.from(context).inflate(R.layout.relationship_item, parent, false);
				viewHolder._storeName = (TextView) view.findViewById(R.id.storename);
				viewHolder._title = (TextView) view.findViewById(R.id.title);
				viewHolder._workExperience = (TextView) view.findViewById(R.id.workexperience);
				viewHolder._workplace = (TextView) view.findViewById(R.id.workplace);
			} else {
				view = LayoutInflater.from(context).inflate(R.layout.relationship_user_item, parent, false);
				viewHolder._tel = (TextView) view.findViewById(R.id.tel);
				viewHolder._xinghao = (TextView) view.findViewById(R.id.xinghao);
			}
			
			viewHolder._name = (TextView) view.findViewById(R.id.name);
			viewHolder._leixing = (TextView) view.findViewById(R.id.typename);
			viewHolder._avator = (AvatorImageView) view.findViewById(R.id.avator);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder._relationshipObject = RelationshipObject.getFromCursor(cursor);
			if (viewHolder._relationshipObject.mIsServiceUser == 0) {
				viewHolder._storeName.setText(cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_ORG));
				viewHolder._workplace.setText(cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_WORKPLACE));
				String workExperience = cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_BRIEF);
				if (!TextUtils.isEmpty(workExperience)) {
					viewHolder._workExperience.setVisibility(View.VISIBLE);
					viewHolder._workExperience.setText(workExperience);
				} else {
					viewHolder._workExperience.setVisibility(View.INVISIBLE);
				}
				String title = cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_TITLE);
				if (!TextUtils.isEmpty(title)) {
					viewHolder._title.setText(cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_TITLE));
					viewHolder._title.setVisibility(View.VISIBLE);
				} else {
					viewHolder._title.setVisibility(View.GONE);
				}
			} else {
				viewHolder._tel.setText(viewHolder._relationshipObject.mTargetCell);
				viewHolder._xinghao.setText(viewHolder._relationshipObject.mXinghao);
			}
			viewHolder._name.setText(cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_UNAME));
			viewHolder._leixing.setText(cursor.getString(BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_LEIXING));
			
			if (!TextUtils.isEmpty(viewHolder._relationshipObject.mMM)) {
				PhotoManagerUtilsV2.getInstance().loadPhotoAsync(TAG, viewHolder._avator, viewHolder._relationshipObject.mMM, null, TaskType.PREVIEW);
			}
		}
		
	}
	
	private class ViewHolder {
		private TextView _name, _leixing, _xinghao, _title, _workplace, _workExperience, _storeName, _tel;
		private AvatorImageView _avator;
		private RelationshipObject _relationshipObject;
	}
	@Override
	protected void onRefreshStart() {
		mIsRefresh = true;
	}
	@Override
	protected void onRefreshPostEnd() {
		mIsRefresh = false;
	}
	
	@Override
	protected void onRefreshEnd() {
		BjnoteContent.RELATIONSHIP.delete(getContentResolver(), BjnoteContent.RELATIONSHIP.CONTENT_URI, BjnoteContent.RELATIONSHIP.UID_SELECTION, new String[]{MyAccountManager.getInstance().getCurrentAccountUid()});
	}


}
