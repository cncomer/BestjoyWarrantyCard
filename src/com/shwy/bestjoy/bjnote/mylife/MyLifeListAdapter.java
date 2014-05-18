package com.shwy.bestjoy.bjnote.mylife;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.service.PhotoManagerService;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2.TaskType;
import com.bestjoy.app.warrantycard.view.MarqueeTextView;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;

public class MyLifeListAdapter extends CursorAdapter {
	private static final String TOKEN = MyLifeListAdapter.class.getName();
	public static String[] mProjection = new String[]{
		HaierDBHelper.CONTACT_ID,                //0
		HaierDBHelper.MYLIFE_COM_CELL,           //1
		HaierDBHelper.CONTACT_BID,               //2
		HaierDBHelper.MYLIFE_COM_WEBSITE,        //3
		HaierDBHelper.CONTACT_ADDRESS,           //4
		HaierDBHelper.CONTACT_NAME,              //5
		HaierDBHelper.MYLIFE_COM_NEWS,           //6
		HaierDBHelper.CONTACT_TEL,               //7
		HaierDBHelper.MYLIFE_COM_XIAOFEI_NOTES,  //8
		HaierDBHelper.CONTACT_DATE,               //9
		HaierDBHelper.ACCOUNT_UID,                 //10
		HaierDBHelper.MYLIFE_FREE_JF,             //11
		HaierDBHelper.MYLIFE_TOTAL_JF,            //12
		HaierDBHelper.MYLIFE_GUANGGAO,            //13
		HaierDBHelper.MYLIFE_COM_XF,              //14
			
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_COM_TEL = 1;
	public static final int INDEX_COM_BID = 2;
	public static final int INDEX_COM_WEBSITE = 3;
	public static final int INDEX_COM_ADDRESS = 4;
	public static final int INDEX_COM_NAME = 5;
	public static final int INDEX_COM_NEWS = 6;
	public static final int INDEX_TEL = 7;
	public static final int INDEX_COM_XIAOFEI_NOTES = 8;
	public static final int INDEX_DATE = 9;
	public static final int INDEX_ACCOUNT_MD = 10;
	
	public static final int INDEX_FREE_JF = 11;
	public static final int INDEX_TOTAL_JF = 12;
	public static final int INDEX_GUANGGAO = 13;
	public static final int INDEX_XF_RECORDS = 14;
	
	public static final String ID_WHERE = HaierDBHelper.CONTACT_ID +"=?";
	public static final String TEL_WHERE = HaierDBHelper.CONTACT_TEL +"=?";
	public static final String ACCOUNTMD_TEL_WHERE = HaierDBHelper.ACCOUNT_UID +"=? and " + TEL_WHERE;
	public static final String COMMM_WHERE = HaierDBHelper.CONTACT_BID +"=?";
	public static final String TEL_COMMM_WHERE = TEL_WHERE + " and " + COMMM_WHERE;
	public static final String ACCOUNTMD_TEL_COMMM_WHERE = ACCOUNTMD_TEL_WHERE + " and " + COMMM_WHERE;
	
	private long lastContentChangedTime;

	public MyLifeListAdapter(Context context, Cursor c, boolean autoRefresh) {
		super(context, c, autoRefresh);
	}
	public MyLifeListAdapter(Context context, Cursor c) {
		this(context, c, false);
		PhotoManagerService.getInstance().requestToken(TOKEN);
	}

	
	@Override
	protected void onContentChanged() {
		DebugUtils.logD("GoodsListAdapter", "onContentChanged()");
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastContentChangedTime > 300) {
			super.onContentChanged();
			lastContentChangedTime = currentTime;
		}
		
	}
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		InfoAdapterViewHolder viewHodler = (InfoAdapterViewHolder) view.getTag();
		viewHodler.myLifeObject = populate(cursor, false);
//		viewHodler.myLifeObject.mId = cursor.getLong(INDEX_ID);
//		viewHodler.myLifeObject.mComCell = cursor.getString(INDEX_COM_TEL);
//		viewHodler.myLifeObject.mComMm = cursor.getString(INDEX_COM_BID);
//		viewHodler.myLifeObject.mWebsite = cursor.getString(INDEX_COM_WEBSITE);
//		viewHodler.myLifeObject.mAddress = cursor.getString(INDEX_COM_ADDRESS);
//		viewHodler.myLifeObject.mComName = cursor.getString(INDEX_COM_NAME);
//		viewHodler.myLifeObject.mNewsNote = cursor.getString(INDEX_COM_NEWS);
//		viewHodler.myLifeObject.mTel = cursor.getString(INDEX_TEL);
//		viewHodler.myLifeObject.mXFNotes = cursor.getString(INDEX_COM_XIAOFEI_NOTES);
//		viewHodler.myLifeObject.mDate = cursor.getLong(INDEX_DATE);
//		viewHodler.myLifeObject.mAccountMd = cursor.getString(INDEX_ACCOUNT_MD);
		
		PhotoManagerService.getInstance().loadPhotoAsync(TOKEN, viewHodler.qAvator, viewHodler.myLifeObject.mComMm, null, TaskType.PREVIEW);
		
		viewHodler.mComName.setText(viewHodler.myLifeObject.mComName);
		viewHodler.mComAddress.setText(viewHodler.myLifeObject.mAddress);
		if (!TextUtils.isEmpty(viewHodler.myLifeObject.mComCell)) {
			viewHodler.mComTel.setVisibility(View.VISIBLE);
			final String tel = viewHodler.myLifeObject.mComCell;
			viewHodler.mComTel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intents.dialPhone(v.getContext(), tel);
				}
			});
		} else {
			viewHodler.mComTel.setVisibility(View.GONE);
		}
		
		if (!TextUtils.isEmpty(viewHodler.myLifeObject.mLiveInfo)) {
			viewHodler.mMarqueeTextView.setVisibility(View.VISIBLE);
			viewHodler.mMarqueeTextView.setText(viewHodler.myLifeObject.mLiveInfo);
			viewHodler.mMarqueeTextView.startFor0();
		} else {
			viewHodler.mMarqueeTextView.setVisibility(View.GONE);
			viewHodler.mMarqueeTextView.stopScroll();
		}
		
		if (!TextUtils.isEmpty(viewHodler.myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotaljifen)) {
			viewHodler.mJF.setText(context.getString(R.string.format_jf, viewHodler.myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotaljifen));
		} else {
			viewHodler.mJF.setText("");
		}
		
		if (!TextUtils.isEmpty(viewHodler.myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotalMoney)) {
			viewHodler.mTotal.setText(context.getString(R.string.format_xiaofei, viewHodler.myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotalMoney));
		} else {
			viewHodler.mTotal.setText("");
		}
		
	}
	
	private String repalceNull(String value, String replace) {
		if (TextUtils.isEmpty(value)) return replace;
		else return value;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.mylife_list_item, parent, false);
		InfoAdapterViewHolder viewHodler = new InfoAdapterViewHolder();
		viewHodler.qAvator = (ImageView) view.findViewById(R.id.avator);
		viewHodler.mComName = (TextView) view.findViewById(R.id.name);
		viewHodler.mComTel = (ImageView) view.findViewById(R.id.tel);
		viewHodler.mComAddress = (TextView) view.findViewById(R.id.address);
		viewHodler.mJF = (TextView) view.findViewById(R.id.jf);
		viewHodler.mTotal = (TextView) view.findViewById(R.id.total);
		viewHodler.mMarqueeTextView = (MarqueeTextView) view.findViewById(R.id.guanggao);
		view.setTag(viewHodler);
		return view;
	}
	
	public static class InfoAdapterViewHolder {
		private ImageView qAvator;
		private ImageView mComTel;
		private TextView mComName, mComAddress, mJF, mGuanggao, mTotal;
		private MarqueeTextView mMarqueeTextView;
		public MyLifeObject myLifeObject = new MyLifeObject();
	}
	
	public void onClick(Context context, View itemView) {
		InfoAdapterViewHolder viewHodler = (InfoAdapterViewHolder) itemView.getTag();
//		context.startActivity(MyLifeListItemActivity.createIntent(context, viewHodler.myLifeObject));
	}
	
	public static MyLifeObject populate(Cursor c, boolean recordsNeed) {
		MyLifeObject object = new MyLifeObject();
		object.mAccountMd = c.getString(INDEX_ACCOUNT_MD);
		object.mId = c.getLong(INDEX_ID);
		object.mTel = c.getString(INDEX_TEL);
		object.mComName = c.getString(INDEX_COM_NAME);
		object.mComMm = c.getString(INDEX_COM_BID);
		object.mComCell = c.getString(INDEX_COM_TEL);
		object.mDate = c.getLong(INDEX_DATE);
		object.mWebsite = c.getString(INDEX_COM_WEBSITE);
		object.mAddress = c.getString(INDEX_COM_ADDRESS);
		object.mNewsNote = c.getString(INDEX_COM_NEWS);
		object.mXFNotes = c.getString(INDEX_COM_XIAOFEI_NOTES);
		object.mLiveInfo = c.getString(INDEX_GUANGGAO);
		object.mMyLifeConsumeRecordsObjectHolder.mTotaljifen = c.getString(INDEX_FREE_JF);
		object.mMyLifeConsumeRecordsObjectHolder.mTotalMoney = c.getString(INDEX_TOTAL_JF);
		if (recordsNeed) {
			List<MyLifeConsumeRecordsObject> list = object.mMyLifeConsumeRecordsObjectHolder.populateConsumeRecords(c.getString(INDEX_XF_RECORDS));
			if (list != null) {
				object.mMyLifeConsumeRecordsObjectHolder.mMyLifeConsumeRecordsObjectList = list;
			}
		}
		return object;
	}
	
	
	public void release() {
		PhotoManagerService.getInstance().releaseToken(TOKEN);
	}
}