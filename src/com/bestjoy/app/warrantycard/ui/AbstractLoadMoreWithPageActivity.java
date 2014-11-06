package com.bestjoy.app.warrantycard.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject;
import com.shwy.bestjoy.utils.AdapterWrapper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterface;
import com.shwy.bestjoy.utils.NetworkUtils;
import com.shwy.bestjoy.utils.PageInfo;
import com.shwy.bestjoy.utils.Query;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

public abstract class AbstractLoadMoreWithPageActivity extends BaseNoActionBarActivity implements AdapterView.OnItemClickListener{

	private static final String TAG ="LoadMoreWithPageActivity";
	protected ListView mListView;
	protected TextView mEmptyView;
	private Query mQuery;
	
	private AdapterWrapper<? extends BaseAdapter> mAdapterWrapper;
	protected ContentResolver mContentResolver;
	
	
	private boolean mFirstinit= false;
	private boolean mDestroyed = false;
	
	private View mLoadMoreStatusView;
	
	private long mLastRefreshTime;
	/**如果导航回该界面，从上次刷新以来已经10分钟了，那么自动开始刷新*/
	private static final int MAX_REFRESH_TIME = 1000 * 60 * 10;
	
	private ProgressBar mFooterViewProgressBar;
	private TextView mFooterViewStatusText;
	protected boolean mIsLoadingMore = false;
	
	private boolean isNeedRequestAgain = true;
	/**如果当前在列表底部了，当有新消息到来的时候我们需要自动滚定到最新的消息处，否则提示下面有新的消息*/
	protected boolean mIsAtListBottom = false;
	protected boolean mIsAtListTop = false;
	
	public static final int LOAD_MORE_TOP = 1;
	public static final int LOAD_MORE_BOTTOM = 2;
	private int mLoadMorePosition = LOAD_MORE_BOTTOM;
	
	private List<OnScrollListener> mOnScrollListenerList = new ArrayList<OnScrollListener>();
	private WakeLock mWakeLock;
	
	//子类必须实现的方法
	/**提供一个CursorAdapter类的包装对象*/
	protected abstract AdapterWrapper<? extends BaseAdapter> getAdapterWrapper();
	/**检查intent是否包含必须数据，如果没有将finish自身*/
//	protected abstract boolean checkIntent(Intent intent);
	/**返回本地的Cursor*/
	protected abstract Cursor loadLocal(ContentResolver contentResolver);
	protected abstract int savedIntoDatabase(ContentResolver contentResolver, List<? extends InfoInterface> infoObjects);
	protected abstract List<? extends InfoInterface> getServiceInfoList(InputStream is, PageInfo pageInfo);
	protected abstract Query getQuery();
	protected abstract void onLoadMoreStartLocked(boolean first);
	/**载入更多的数据已经保存完成*/
	protected abstract void onLoadSaveNewDataFinishLocked(int count);
	protected abstract void onLoadMoreEnd(boolean first);
	protected abstract int getContentLayout();
	/***
	 * 设置加载更多的位置，默认是底部，可以通过传值LOAD_MORE_TOP或者LOAD_MORE_BOTTOM来修改
	 * @param loadMorePosition
	 */
	public void setLoadMorePosition(int loadMorePosition) {
		mLoadMorePosition = loadMorePosition;
	}
	protected ListView getListView() {
		return mListView;
	}
	
	public void addOnScrollListenerList(OnScrollListener list) {
		if (!mOnScrollListenerList.contains(list)) {
			mOnScrollListenerList.add(list);
		}
	}
	
	public void removeOnScrollListenerList(OnScrollListener list) {
		if (mOnScrollListenerList.contains(list)) {
			mOnScrollListenerList.remove(list);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugUtils.logD(TAG, "onCreate()");
		if (this.isFinishing()) {
			return;
		}
		DebugUtils.logD(TAG, "setContentView()");
		setContentView(getContentLayout());
		
		mContentResolver = getContentResolver();
		
		mEmptyView = (TextView) findViewById(android.R.id.empty);
		
		mListView = (ListView) findViewById(R.id.listview);
		mAdapterWrapper = getAdapterWrapper();
		mListView.setOnItemClickListener(this);
		addLoadMoreStatusView();
		updateLoadMoreStatusView(false, null);
		mListView.setAdapter(mAdapterWrapper.getAdapter());
		mListView.setEmptyView(mEmptyView);
		removeLoadMoreStatusView();
		mContext = this;
		mFirstinit = true;
		
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				for(OnScrollListener list :mOnScrollListenerList) {
					list.onScrollStateChanged(view, scrollState);
				}
				DebugUtils.logD(TAG, "onScrollStateChanged() needLoadMore()=" + needLoadMore() + ",  mIsUpdate=" + mIsLoadingMore + ", isNeedRequestAgain="+isNeedRequestAgain);
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && needLoadMore() && !mIsLoadingMore && isNeedRequestAgain) {
					DebugUtils.logD(TAG, "we go to load more.");
					loadMoreMessagesAsync();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				for(OnScrollListener list :mOnScrollListenerList) {
					list.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}
				if (totalItemCount > 0) {
					if (firstVisibleItem == 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
						mIsAtListBottom = true;
					} else if (firstVisibleItem > 0 && firstVisibleItem + visibleItemCount < totalItemCount) {
						mIsAtListBottom = false;
					} else {
						mIsAtListBottom = true;
					}
					
				} else {
					mIsAtListBottom = false;
				}
				mIsAtListTop = firstVisibleItem == 0;
				
			}
			
		});
		
		PushAgent.getInstance(mContext).onAppStart();
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		
	}
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}
	}
	
	private LoadMoreFromServiceTask mLoadMoreFromServiceTask;
	public void loadMoreMessagesAsync() {
		AsyncTaskUtils.cancelTask(mLoadMoreFromServiceTask);
		mLoadMoreFromServiceTask = new LoadMoreFromServiceTask();
		mLoadMoreFromServiceTask.execute();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mDestroyed = true;
		AsyncTaskUtils.cancelTask(mLoadLocalTask);
		AsyncTaskUtils.cancelTask(mLoadMoreFromServiceTask);
		if (mAdapterWrapper != null) mAdapterWrapper.releaseAdapter();
	}
	
	private void addLoadMoreStatusView() {
		if (mLoadMoreStatusView != null) return; 
		mLoadMoreStatusView = LayoutInflater.from(mContext).inflate(R.layout.load_more_footer, mListView, false);
		mFooterViewProgressBar = (ProgressBar) mLoadMoreStatusView.findViewById(R.id.load_more_progressBar);
		mFooterViewStatusText = (TextView) mLoadMoreStatusView.findViewById(R.id.load_more_text);
		if (mLoadMorePosition == LOAD_MORE_BOTTOM) {
			mListView.addFooterView(mLoadMoreStatusView, true, false);
		} else if (mLoadMorePosition == LOAD_MORE_TOP) {
			mListView.addHeaderView(mLoadMoreStatusView, true, false);
		}
		
	}
	
	private boolean needLoadMore() {
		if (mLoadMorePosition == LOAD_MORE_TOP) {
			return mIsAtListTop;
		} else if (mLoadMorePosition == LOAD_MORE_BOTTOM) {
			return mIsAtListBottom;
		}
		return false;
	}
	
	private void removeLoadMoreStatusView() {
		if (mLoadMoreStatusView != null) {
			if (mLoadMorePosition == LOAD_MORE_BOTTOM) {
				mListView.removeFooterView(mLoadMoreStatusView);
			} else if (mLoadMorePosition == LOAD_MORE_TOP) {
				mListView.removeHeaderView(mLoadMoreStatusView);
			}
			
			mLoadMoreStatusView = null;
		}
	}
	
	private void updateLoadMoreStatusView(boolean loading, String status) {
		if (mLoadMoreStatusView == null) {
			addLoadMoreStatusView();
		}
		if (loading) {
			mFooterViewProgressBar.setVisibility(View.VISIBLE);
		} else {
			mFooterViewProgressBar.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(status)) mFooterViewStatusText.setText(status);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
		//子类实现具体的动作
	}
	
	private LoadLocalTask mLoadLocalTask;
	private void loadLocalDataAsync() {
		AsyncTaskUtils.cancelTask(mLoadLocalTask);
		mLoadLocalTask = new LoadLocalTask();
		mLoadLocalTask.execute();
	}
	private class LoadLocalTask extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... params) {
			DebugUtils.logD(TAG, "LoadLocalTask load local data....");
			return loadLocal(mContentResolver);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			mAdapterWrapper.changeCursor(result);
			int requestCount = 0;
			if (result != null) {
				requestCount = result.getCount();
			}
			DebugUtils.logD(TAG, "LoadLocalTask load local data finish....localCount is " + requestCount);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
	}
	

	/**更新或是新增的总数 >0表示有更新数据，需要刷新，=-1网络问题， =-2 已是最新数据 =0 没有更多数据*/
	private class LoadMoreFromServiceTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... arg0) {
			mIsLoadingMore = true;
			isNeedRequestAgain = true;
			int insertOrUpdateCount = 0;
			try {
				if (mQuery == null) {
					mQuery = getQuery();
					if (mQuery.mPageInfo == null) {
						mQuery.mPageInfo = new PageInfo();
						mQuery.mPageInfo.reset();
					}
				}
				 //Do work to refresh the list here.
				onLoadMoreStartLocked(mFirstinit);
//				while (isNeedRequestAgain) {
					DebugUtils.logD(TAG, "openConnection....");
					DebugUtils.logD(TAG, "start pageIndex " + mQuery.mPageInfo.mPageIndex + " pageSize = " + mQuery.mPageInfo.mPageSize);
					InputStream is = NetworkUtils.openContectionLocked(ServiceObject.buildPageQuery(mQuery.qServiceUrl, mQuery.mPageInfo.mPageIndex, mQuery.mPageInfo.mPageSize), MyApplication.getInstance().getSecurityKeyValuesObject());
					if (is == null) {
						DebugUtils.logD(TAG, "finish task due to openContectionLocked return null InputStream");
						isNeedRequestAgain = false;
						return 0;
					}
					DebugUtils.logD(TAG, "begin parseList....");
					List<? extends InfoInterface> serviceInfoList = getServiceInfoList(is, mQuery.mPageInfo);
					int newCount = serviceInfoList.size();
					DebugUtils.logD(TAG, "find new date #count = " + newCount + " totalSize = " + mQuery.mPageInfo.mTotalCount);
					
					if (newCount == 0) {
						DebugUtils.logD(TAG, "no more date");
						isNeedRequestAgain = false;
						return 0;
					}
					if (mQuery.mPageInfo.mTotalCount <= mQuery.mPageInfo.mPageIndex * mQuery.mPageInfo.mPageSize) {
						DebugUtils.logD(TAG, "returned data count is less than that we requested, so not need to pull data again");
						isNeedRequestAgain = false;
					}
					
					DebugUtils.logD(TAG, "begin insert or update local database");
					insertOrUpdateCount = savedIntoDatabase(mContentResolver, serviceInfoList);
					onLoadSaveNewDataFinishLocked(insertOrUpdateCount);
					return insertOrUpdateCount;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				isNeedRequestAgain=false;
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				isNeedRequestAgain=false;
				return -1;
			}
		}


		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (mDestroyed) return;
			if (result == -1){
				MyApplication.getInstance().showMessage(R.string.msg_network_error_for_receive);
			} else if (result == -2) {
				MyApplication.getInstance().showMessage(R.string.msg_nonew_for_receive);
			} else if (result == 0) {
//				MyApplication.getInstance().showMessage(R.string.msg_nomore_for_receive);
				DebugUtils.logD(TAG, "no more for receive");
			}
			removeLoadMoreStatusView();
			if (isNeedRequestAgain) {
				mQuery.mPageInfo.mPageIndex+=1;
			}
			mLastRefreshTime = System.currentTimeMillis();
			mIsLoadingMore = false;
		    onLoadMoreEnd(mFirstinit);
		    if (mFirstinit) {
				mFirstinit = false;
			}
		}
	}
	
}
