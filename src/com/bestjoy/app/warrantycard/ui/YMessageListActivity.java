package com.bestjoy.app.warrantycard.ui;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.Intents;
import com.umeng.message.entity.UMessage;

public class YMessageListActivity extends BaseActionbarActivity implements OnItemClickListener{

	private YmessageCursorAdapter mYmessageCursorAdapter;
	private int mGategoryId = -1;
	private static final int[] GATEGORY_ICON = new int[]{
		R.drawable.ymeng_icon_0,
		R.drawable.ymeng_icon_1,
		R.drawable.ymeng_icon_2,
		R.drawable.ymeng_icon_3,
		R.drawable.ymeng_icon_4,
		R.drawable.ymeng_icon_5,
	};
	private static final int[] GATEGORY_TITLE = new int[]{
		R.string.title_ymeng_category0,
		R.string.title_ymeng_category1,
		R.string.title_ymeng_category2,
		R.string.title_ymeng_category3,
		R.string.title_ymeng_category4,
		R.string.title_ymeng_category5,
	};
	@Override
	protected boolean checkIntent(Intent intent) {
		mGategoryId = intent.getIntExtra(Intents.EXTRA_TYPE, -1);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		if (mGategoryId > 0) {
			setTitle(GATEGORY_TITLE[mGategoryId]);
		}
		
		ListView listView = (ListView) findViewById(R.id.listview);
		View progressBar = findViewById(R.id.progressBar);
		progressBar.setVisibility(View.VISIBLE);
		mYmessageCursorAdapter = new YmessageCursorAdapter(this, null, true);
		listView.setAdapter(mYmessageCursorAdapter);
		listView.setOnItemClickListener(this);
		loadUmessagesAsync();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncTaskUtils.cancelTask(mLoadUmessageAsyncTask);
		mYmessageCursorAdapter.changeCursor(null);
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		 return false;
	 }
	
	private LoadUmessageAsyncTask mLoadUmessageAsyncTask;
	private void loadUmessagesAsync() {
		AsyncTaskUtils.cancelTask(mLoadUmessageAsyncTask);
		mLoadUmessageAsyncTask = new LoadUmessageAsyncTask();
		mLoadUmessageAsyncTask.execute();
	}
	
	class LoadUmessageAsyncTask extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			if (mGategoryId > 0) {
				return mContext.getContentResolver().query(BjnoteContent.YMESSAGE.CONTENT_URI, BjnoteContent.YMESSAGE.PROJECTION, BjnoteContent.YMESSAGE.WHERE_YMESSAGE_CATEGORY, new String[]{String.valueOf(mGategoryId)}, "" + HaierDBHelper.YOUMENG_MESSAGE_SERVER_TIME + " desc");
			} else {
				//显示分类信息
				return mContext.getContentResolver().query(BjnoteContent.YMESSAGE.CONTENT_URI, BjnoteContent.YMESSAGE.PROJECTION, " (1=1) group by "+ HaierDBHelper.YOUMENG_MESSAGE_CATEGORY + "", null, "" + HaierDBHelper.YOUMENG_MESSAGE_CATEGORY + " asc");
			}
			
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			mYmessageCursorAdapter.changeCursor(result);
			findViewById(R.id.progressBar).setVisibility(View.GONE);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			findViewById(R.id.progressBar).setVisibility(View.GONE);
		}
		
	}
	
	private class YmessageCursorAdapter extends CursorAdapter {

		public YmessageCursorAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}
		
		protected void onContentChanged() {
			super.onContentChanged();
	    }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = null;
			ViewHolder viewHolder = new ViewHolder();
			if (mGategoryId == -1) {
				view = LayoutInflater.from(context).inflate(R.layout.ymeng_gategory_list_item, parent, false);
				viewHolder._icon = (ImageView) view.findViewById(R.id.icon);
			} else {
				view = LayoutInflater.from(context).inflate(R.layout.ymeng_list_item, parent, false);
			}
			
			
			viewHolder._title = (TextView) view.findViewById(R.id.title);
			viewHolder._text = (TextView) view.findViewById(R.id.content);
			viewHolder._text.setAutoLinkMask(Linkify.ALL);
			viewHolder._date = (TextView) view.findViewById(R.id.time);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			UMessage message = YouMengMessageHelper.getInstance().getUMessageFromCursor(cursor);
			if (message != null) {
				viewHolder._text.setText(message.text);
				viewHolder._title.setText(message.title);
			} else {
				viewHolder._title.setText(cursor.getString(BjnoteContent.YMESSAGE.INDEX_TITLE));
				viewHolder._text.setText(cursor.getString(BjnoteContent.YMESSAGE.INDEX_TEXT));
			}
			viewHolder._date.setText(DateUtils.TOPIC_SUBJECT_DATE_TIME_FORMAT.format(new Date(cursor.getLong(BjnoteContent.YMESSAGE.INDEX_DATE))));
			if (mGategoryId == -1) {
				viewHolder._categoryId = Integer.valueOf(message.extra.get("type"));
				viewHolder._icon.setImageResource(GATEGORY_ICON[viewHolder._categoryId]);
			}
		}
		
	}
	
	private class ViewHolder {
		private TextView _date, _title, _text;
		private ImageView _icon;
		private int _categoryId = -1;
	}
	
	public static void startActivity(Context context, Bundle bundle) {
		Intent intent = new Intent(context, YMessageListActivity.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}
	public static void startActivity(Context context, int category) {
		Intent intent = new Intent(context, YMessageListActivity.class);
		intent.putExtra(Intents.EXTRA_TYPE, category);
		context.startActivity(intent);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mGategoryId == -1) {
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			startActivity(mContext, viewHolder._categoryId);
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
     	   if (mGategoryId > 0) {
     		   finish();
     		   return true;
     	   }
     	   default:
     		  return super.onOptionsItemSelected(item);
        }
        
    }

}
