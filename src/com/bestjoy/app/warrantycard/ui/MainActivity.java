package com.bestjoy.app.warrantycard.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.ServiceObject.ServiceResultObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.update.UpdateService;
import com.bestjoy.app.warrantycard.utils.BitmapUtils;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.shwy.bestjoy.utils.AsyncTaskUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.FilesUtils;
import com.shwy.bestjoy.utils.NetworkUtils;

public class MainActivity extends BaseActionbarActivity implements View.OnClickListener {
	private static final String TAG = "MainActivity";
	private LinearLayout mDotsLayout;
	private ViewPager mAdsViewPager;
	private boolean mAdsViewPagerIsScrolling = false;
	
	private int[] mAddsDrawableId = new int[]{
			R.drawable.ad1,
			R.drawable.ad2,
			R.drawable.ad3,
	};
	
	private Drawable[] mDotDrawableArray;
	private Bitmap[] mAdsBitmaps;
	
	private ImageView[] mDotsViews = null;
	private ImageView[] mAdsPagerViews = null;
	private int mCurrentPagerIndex = 0;
	private static final int DEFAULT_MAX_ADS_SIZE = 3;
	
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setContentView(R.layout.activity_main);
		mDotsLayout = (LinearLayout) findViewById(R.id.dots);
		mAdsViewPager = (ViewPager) findViewById(R.id.adsViewPager);
		this.initViewPagers(3);
		this.initDots(3);
		mAdsViewPager.setAdapter(new AdsViewPagerAdapter());
		
		mAdsViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (mCurrentPagerIndex != position) {
					mDotsViews[mCurrentPagerIndex].setImageDrawable(mDotDrawableArray[0]);
					mDotsViews[position].setImageDrawable(mDotDrawableArray[1]);
					mCurrentPagerIndex = position;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				mAdsViewPagerIsScrolling  = state == 1;
				
			}
		});
		
		//ModleSettings.addModelsAdapter(this, (ListView) findViewById(R.id.listview));
		UpdateService.startUpdateServiceOnAppLaunch(mContext);
		
		findViewById(R.id.button_my_card).setOnClickListener(this);
		findViewById(R.id.button_telecontrol).setOnClickListener(this);
		findViewById(R.id.button_qr_scan).setOnClickListener(this);
		
		YouMengMessageHelper.getInstance().startCheckDeviceTokenAsync();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		invalidateOptionsMenu();
		changeAdsDelay();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mChangeAdsRunnable);
	}
	
	 @Override
     public boolean onCreateOptionsMenu(Menu menu) {
  	     boolean result = super.onCreateOptionsMenu(menu);
  	     MenuItem subMenu1Item = menu.findItem(R.string.menu_more);
  	   subMenu1Item.getSubMenu().add(1000, R.string.menu_refresh, 1005, R.string.menu_refresh);
  	     subMenu1Item.getSubMenu().add(1000, R.string.menu_exit, 1006, R.string.menu_exit);
//  	     subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_light);
         return result;
     }
	 
	 public boolean onPrepareOptionsMenu(Menu menu) {
		 menu.findItem(R.string.menu_exit).setVisible(MyAccountManager.getInstance().hasLoginned());
		 menu.findItem(R.string.menu_refresh).setVisible(MyAccountManager.getInstance().hasLoginned());
	     return super.onPrepareOptionsMenu(menu);
	 }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem menuItem) {
		 switch(menuItem.getItemId()) {
		 case R.string.menu_exit:
			 new AlertDialog.Builder(mContext)
				.setMessage(R.string.msg_existing_system_confirm)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteAccountAsync();
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.show();
			 return true;
		 case R.string.menu_refresh:
			 if (MyAccountManager.getInstance().hasLoginned()) {
				 //做一次登陆操作
				 //目前只删除本地的所有缓存文件
//				try{
//			        Runtime.getRuntime().exec("adb shell pm clear " + MyApplication.PKG_NAME);
//		        } catch(IOException ex) {
//		            ex.printStackTrace();
//		        }
				 File dir = MyApplication.getInstance().getCachedXinghaoInternalRoot();
				 FilesUtils.deleteFile("Updating ", dir);
				 
				 dir = MyApplication.getInstance().getCachedXinghaoExternalRoot();
				 if (dir != null) {
					 FilesUtils.deleteFile("Updating ", dir);
				 }
			 }
			 break;
		 }
		 return super.onOptionsItemSelected(menuItem);
	 }
	 
	 private DeleteAccountTask mDeleteAccountTask;
	 private void deleteAccountAsync() {
		 AsyncTaskUtils.cancelTask(mDeleteAccountTask);
		 showDialog(DIALOG_PROGRESS);
		 mDeleteAccountTask = new DeleteAccountTask();
		 mDeleteAccountTask.execute();
	 }
	 private class DeleteAccountTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			MyAccountManager.getInstance().deleteDefaultAccount();
			MyAccountManager.getInstance().saveLastUsrTel("");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dismissDialog(DIALOG_PROGRESS);
			invalidateOptionsMenu();
			MyApplication.getInstance().showMessage(R.string.msg_op_successed);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			invalidateOptionsMenu();
			dismissDialog(DIALOG_PROGRESS);
		}
		
		
		 
	 }
	 


	private void initDots(int count){
		LayoutInflater flater = this.getLayoutInflater();
		if (mDotDrawableArray == null) {
			mDotDrawableArray = new Drawable[2];
			mDotDrawableArray[0] = this.getResources().getDrawable(R.drawable.dot);
			mDotDrawableArray[1] = this.getResources().getDrawable(R.drawable.dot_on);
		}
		mDotsViews = new ImageView[count];
		for (int j = 0; j < count; j++) {
			mDotsViews[j] = (ImageView) flater.inflate(R.layout.dot, mDotsLayout, false);
			if (mCurrentPagerIndex == j) {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[1]);
			} else {
				mDotsViews[j].setImageDrawable(mDotDrawableArray[0]);
			}
			mDotsLayout.addView(mDotsViews[j]);
		}
	}
	
	private void initAdsBitmap() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (mAdsBitmaps == null) {
			mAdsBitmaps = new Bitmap[DEFAULT_MAX_ADS_SIZE];
		} else {
			for(Bitmap bitmap:mAdsBitmaps) {
				bitmap.recycle();
			}
		}
		int adsW = getResources().getDimensionPixelSize(R.dimen.ads_width);
		int adsH = getResources().getDimensionPixelSize(R.dimen.ads_height);
		mAdsBitmaps = BitmapUtils.getSuitedBitmaps(this, mAddsDrawableId, 800, 1024);
	}
	
	private void initViewPagers(int count) {
		initAdsBitmap();
		mAdsPagerViews = new ImageView[count];
		LayoutInflater flater = getLayoutInflater();
		for (int j = 0; j < count; j++) {
			mAdsPagerViews[j] = (ImageView) flater.inflate(R.layout.ads, null, false);
			mAdsPagerViews[j].setImageBitmap(mAdsBitmaps[j]);
		}
	}
	
	private void initAdsPager(){
	}
	
	class AdsViewPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return mAdsPagerViews.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		private View getView(ViewGroup container, int position) {
			container.addView(mAdsPagerViews[position]);
			return mAdsPagerViews[position];
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return getView(container, position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mAdsPagerViews[position]);
		}
		
	}
	
	private void changeAdsDelay() {
		mHandler.postDelayed(mChangeAdsRunnable, DEFAULT_DELAY);
	}
	private static long DEFAULT_DELAY = 5000;
	private ChangeAdsRunnable mChangeAdsRunnable = new ChangeAdsRunnable();
	private class ChangeAdsRunnable implements Runnable {
		@Override
		public void run() {
			if (!mAdsViewPagerIsScrolling) {
				int pageCount = mAdsViewPager.getAdapter().getCount();
				int nextPage = mCurrentPagerIndex % pageCount + 1;
				if (nextPage >= pageCount) {
					nextPage = 0;
				}
				mAdsViewPager.setCurrentItem(nextPage);
				
			}
			mHandler.postDelayed(this, DEFAULT_DELAY);
		}
		
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}
	/**
	 * 回到主界面
	 * @param context
	 */
	public static void startActivityForTop(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_my_card:
			//判断有没有登陆，没有的话提示登录
			if (!MyAccountManager.getInstance().hasLoginned()) {
				 LoginActivity.startIntent(this, null);
				 MyApplication.getInstance().showNeedLoginMessage();
				 return;
			} else {
				//如果已经登录，判断是否是演示账户，是的话显示演示家
				if (MyAccountManager.getInstance().getAccountObject().isDemoAccountObject()) {
					//LoginActivity.startIntent(this, null);
					//MyApplication.getInstance().showNeedLoginMessage();
					boolean needLoadDemo = MyApplication.getInstance().mPreferManager.getBoolean("need_load_demo_home", true);
					if (needLoadDemo) {
						//如果是第一次，我们需要拉取演示家数据
						if (!ComConnectivityManager.getInstance().isConnected()) {
							//没有联网，这里提示用户
							ComConnectivityManager.getInstance().onCreateNoNetworkDialog(mContext).show();
						} else {
							new AlertDialog.Builder(mContext)
							.setMessage(R.string.msg_need_to_get_demo_data)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									loadDemoCardDataAsync();
								}
							})
							.setNegativeButton(android.R.string.cancel, null)
							.show();
						}
						return;
					}
				}
			}
			//判断是否有家，没有的话，就要去新建一个家
			if (!MyAccountManager.getInstance().hasHomes()) {
				HomeObject.setHomeObject(new HomeObject());
				MyApplication.getInstance().showNeedHomeMessage();
				NewHomeActivity.startActivity(this);
				return;
			}
			//判断是否有保修卡
			
			if (MyAccountManager.getInstance().hasBaoxiuCards()) {
				Bundle bundle = ModleSettings.createMyCardDefaultBundle(this);
				bundle.putLong("aid", MyAccountManager.getInstance().getHomeAIdAtPosition(0));
				bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
				MyChooseDevicesActivity.startIntent(this, bundle);
			} else {
				Bundle bundle = ModleSettings.createMyCardDefaultBundle(this);
				bundle.putLong("aid", MyAccountManager.getInstance().getHomeAIdAtPosition(0));
				bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
				NewCardActivity.startIntent(this, bundle);
			}
			break;
		case R.id.button_telecontrol:
			MyApplication.getInstance().showUnsupportMessage();
			break;
		case R.id.button_qr_scan:
			Intent scanIntent = new Intent(this, CaptureActivity.class);
			startActivity(scanIntent);
			break;
		}
		
	}
	private LoadtDemoCardDataTask mLoadtDemoCardDataTask;
	public void loadDemoCardDataAsync() {
		showDialog(DIALOG_PROGRESS);
		AsyncTaskUtils.cancelTask(mLoadtDemoCardDataTask);
		mLoadtDemoCardDataTask = new LoadtDemoCardDataTask();
		mLoadtDemoCardDataTask.execute();
	}
	private class LoadtDemoCardDataTask extends AsyncTask<Void, Void, ServiceResultObject> {

		@Override
        protected ServiceResultObject doInBackground(Void... params) {
			ContentResolver cr = mContext.getContentResolver();
			if (!MyAccountManager.getInstance().hasHomes()) {
				//如果没有家，我们创建演示家
				HomeObject homeObject = HomeObject.getDemoHomeObject(AccountObject.DEMO_ACCOUNT_UID, HomeObject.DEMO_HOME_AID);
				DebugUtils.logD(TAG, "LoadtDemoCardDataTask.doInBackground() start to insert HomeObject demo " + homeObject.toString());
				if (homeObject.saveInDatebase(cr, null)) {
					DebugUtils.logD(TAG, "initAccountHomes");
					MyAccountManager.getInstance().initAccountHomes();
				}
			}
			ServiceResultObject serviceObject = new ServiceResultObject();
	        //http://www.dzbxk.com/bestjoy/GetBaoXiuDataByUID.ashx?UID=351356&AID=353766
			StringBuilder sb = new StringBuilder("http://www.dzbxk.com/bestjoy/GetBaoXiuDataByUID.ashx?");
			sb.append("UID=").append(AccountObject.DEMO_ACCOUNT_UID)
			.append("&AID=").append(HomeObject.DEMO_HOME_AID);
			InputStream is = null;
			try {
	            is = NetworkUtils.openContectionLocked(sb.toString(), MyApplication.getInstance().getSecurityKeyValuesObject());
	            if (is != null) {
	            	serviceObject = ServiceResultObject.parse(NetworkUtils.getContentFromInput(is));
	            	if (serviceObject.isOpSuccessfully()) {
	            		if (serviceObject.mStrData != null) {
	            			JSONArray baoxiuCards = new JSONArray(serviceObject.mStrData);
	            			//JSONArray baoxiuCards = serviceObject.mJsonData.getJSONArray(BaoxiuCardObject.JSONOBJECT_NAME);
		            		int len = baoxiuCards.length();
		            		BaoxiuCardObject baoxiuCardObject = null;
		            		for(int index=0; index < len; index++) {
		            			baoxiuCardObject = BaoxiuCardObject.parseBaoxiuCards(baoxiuCards.getJSONObject(index), null);
		            			baoxiuCardObject.mAID = HomeObject.DEMO_HOME_AID;
		            			baoxiuCardObject.mUID = AccountObject.DEMO_ACCOUNT_UID;
		            			baoxiuCardObject.saveInDatebase(cr, null);
		            		}
		            		MyAccountManager.getInstance().updateHomeObject(HomeObject.DEMO_HOME_AID);
	            		} else {
	            			serviceObject.mStatusMessage = mContext.getString(R.string.msg_get_no_demo_data);
	            		}
	            		
	            	}
	            }
            } catch (ClientProtocolException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = e.getMessage();
            } catch (IOException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = MyApplication.getInstance().getGernalNetworkError();
            } catch (JSONException e) {
	            e.printStackTrace();
	            serviceObject.mStatusMessage = e.getMessage();
            } finally {
            	NetworkUtils.closeInputStream(is);
            }
	        return serviceObject;
        }

		@Override
        protected void onPostExecute(ServiceResultObject result) {
	        super.onPostExecute(result);
	        dismissDialog(DIALOG_PROGRESS);
	        if (result.isOpSuccessfully()) {
	        	//标识下次不用拉取演示数据了
	        	MyApplication.getInstance().mPreferManager.edit().putBoolean("need_load_demo_home", false).commit();
	        	//判断是否有家，没有的话，就要去新建一个家
				if (!MyAccountManager.getInstance().hasHomes()) {
					HomeObject.setHomeObject(new HomeObject());
					MyApplication.getInstance().showNeedHomeMessage();
					NewHomeActivity.startActivity(mContext);
					return;
				}
				//判断是否有保修卡
				if (MyAccountManager.getInstance().hasBaoxiuCards()) {
					Bundle newBundle = ModleSettings.createMyCardDefaultBundle(mContext);
					newBundle.putLong("aid", MyAccountManager.getInstance().getAccountObject().mAccountHomes.get(0).mHomeAid);
					newBundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					MyChooseDevicesActivity.startIntent(mContext, newBundle);
				} else {
					Bundle newBundle = ModleSettings.createMyCardDefaultBundle(mContext);
					newBundle.putLong("aid", MyAccountManager.getInstance().getAccountObject().mAccountHomes.get(0).mHomeAid);
					newBundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
					NewCardActivity.startIntent(mContext, newBundle);
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
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		YouMengMessageHelper.getInstance().cancelCheckDeviceTokenTask();
	}

}
