package com.bestjoy.app.bjwarrantycard.im;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
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
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2.TaskType;
import com.bestjoy.app.warrantycard.ui.PullToRefreshListPageForFragment;
import com.bestjoy.app.warrantycard.view.AvatorImageView;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;

public class RelationshipConversationFragment extends PullToRefreshListPageForFragment{
	private static final String TAG = "RelationshipConversationFragment";
	public static final String FIRST = "RelationshipConversationFragment.FIRST";
	private Handler mHandler;
	private static final int WHAT_REFRESH_LIST = 1000;
	private RelationshipAdapter mRelationshipAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case WHAT_REFRESH_LIST:
					mHandler.removeMessages(WHAT_REFRESH_LIST);
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
		mRelationshipAdapter = new RelationshipAdapter(getActivity(), null, true);
		return new AdapterWrapper<CursorAdapter>(mRelationshipAdapter);
	}

	@Override
	protected Cursor loadLocal(ContentResolver cr) {
		return BjnoteContent.RELATIONSHIP.getAllRelationshipConversation(cr, MyAccountManager.getInstance().getCurrentAccountUid());
	}

	@Override
	protected int savedIntoDatabase(ContentResolver cr, List<? extends InfoInterface> infoObjects) {
		int insertOrUpdateCount = 0;
		if (infoObjects != null) {
			for(InfoInterface object:infoObjects) {
				if (object instanceof RelationshipObject) {
					if (((RelationshipObject)object).saveInDatebaseForRelationshipConversation(cr, null)) {
						insertOrUpdateCount++;
					}
				} 
				
			}
		}
		return insertOrUpdateCount;
	}

	@Override
	protected List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo) {
		return RelationshipObject.parseRelationshipConversationList(is, pageInfo);
	}

	@Override
	protected Query getQuery() {
		Query query =  new Query();
		query.qServiceUrl = ServiceObject.getRelationshipConversationUrl(MyAccountManager.getInstance().getCurrentAccountUid(), MyAccountManager.getInstance().getAccountObject().mAccountPwd);
		
		return query;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
		super.onItemClick(parent, itemView, position, id);
		ViewHolder viewHolder = (ViewHolder) itemView.getTag();
		ConversationListActivity.startActivity(getActivity(), viewHolder._relationshipObject);
	}
	
	@Override
	protected int getContentLayout() {
		return R.layout.pull_to_refresh_page_activity;
	}
	
	private static  DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
	private class RelationshipAdapter extends CursorAdapter {

		public RelationshipAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}

		@Override
		protected void onContentChanged() {
			if (mIsUpdate) {
				return;
			}
			mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_LIST, 250);
		}


		private void refreshList() {
			super.onContentChanged();
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return super.getView(position, convertView, parent);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.relationship_conversation_item, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder._title = (TextView) view.findViewById(R.id.title);
			viewHolder._tel = (TextView) view.findViewById(R.id.tel);
			viewHolder._name = (TextView) view.findViewById(R.id.name);
			viewHolder._avator = (AvatorImageView) view.findViewById(R.id.avator);
			viewHolder._workplace = (TextView) view.findViewById(R.id.message);
			viewHolder._workExperience = (TextView) view.findViewById(R.id.time);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder._relationshipObject = RelationshipObject.getFromCursor(cursor);
			if (viewHolder._relationshipObject.mIsServiceUser == 0) {
				String title = viewHolder._relationshipObject.mTargetTitle;
				if (!TextUtils.isEmpty(title)) {
					viewHolder._title.setText(title);
					viewHolder._title.setVisibility(View.VISIBLE);
				} else {
					viewHolder._title.setVisibility(View.GONE);
				}
			} else {
				viewHolder._title.setVisibility(View.GONE);
			}
			viewHolder._tel.setText(viewHolder._relationshipObject.mTargetCell);
			viewHolder._name.setText(viewHolder._relationshipObject.mTargetName);
			viewHolder._workplace.setText(viewHolder._relationshipObject.mLastNewMessageText);
			viewHolder._workExperience.setText(TIME_FORMAT.format(new Date(viewHolder._relationshipObject.mLastNewMessageTime)));
			
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
	}
	@Override
	protected void onRefreshPostEnd() {
		mHandler.removeMessages(WHAT_REFRESH_LIST);
		mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_LIST, 250);
	}
	
	@Override
	protected void onRefreshEnd() {
//		BjnoteContent.RELATIONSHIP.delete(getActivity().getContentResolver(), BjnoteContent.RELATIONSHIP.CONVERSATION_CONTENT_URI, BjnoteContent.RELATIONSHIP.UID_SELECTION, new String[]{MyAccountManager.getInstance().getCurrentAccountUid()});
	}


}
