package com.bestjoy.app.warrantycard.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.BaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.shwy.bestjoy.utils.Intents;
/**
 * 新建保修卡、预约维修、预约安装 UI
 * @author chenkai
 *
 */
public class NewCardActivity extends BaseSlidingFragmentActivity implements 
	SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener{
	private static final String TAG = "NewCardActivity";
	private ModleBaseFragment mContent;
	private NewCardChooseFragment mMenu;
	private Bundle mBundles;
	/**表示是否是第一次进入*/
	private boolean mIsFirstOnResume = true;
	private static final String  KEY_FIRST_SHOW = "NewCardActivity.first";
	/**
	 * 仅仅用作新建的时候并没有登录，这会导致我们需要前往登录/注册界面，一旦登录成功后会返回该界面这个过程使用，其余情况请忽略改变量.
	 */
	private boolean mHasRegistered = false;	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugUtils.logD(TAG, "onCreate()");
		if (isFinishing()) {
			return ;
		}
		
		
		if (savedInstanceState != null) {
			mContent = (ModleBaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
			mMenu = (NewCardChooseFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mMenu");
		}
		int type = mBundles.getInt(Intents.EXTRA_TYPE);
		if (mContent == null) {
			switch(type) {
			case R.id.model_my_card:
				mContent = new NewWarrantyCardFragment();
				break;
			case R.id.model_install:
				mContent = new NewInstallCardFragment();
				break;
			case R.id.model_repair:
				mContent = new NewRepairCardFragment();
				break;
			}
		}
		mContent.setArguments(mBundles);
		
		if (mMenu == null) {
			mMenu = new NewCardChooseFragment();
		}
		
		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
		
		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, mMenu)
		.commit();
		
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
//		sm.setBehindOffsetRes(R.dimen.choose_device_slidingmenu_offset);
//        sm.setAboveOffsetRes(R.dimen.choose_device_slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeDegree(0.25f);
		sm.setMode(SlidingMenu.RIGHT);
		sm.setTouchModeAbove(SlidingMenu.RIGHT);
		sm.setBehindOffsetRes(R.dimen.choose_device_choose_slidingmenu_offset);
		sm.setOnOpenedListener(this);
		sm.setOnClosedListener(this);
		
		setSlidingActionBarEnabled(false);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.new_card_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (getSlidingMenu().isMenuShowing()) {
//			menu.findItem(R.string.menu_search).setVisible(true);
			menu.findItem(R.string.menu_done).setVisible(true);
		} else {
//			menu.findItem(R.string.menu_search).setVisible(false);
			menu.findItem(R.string.menu_done).setVisible(false);
		}
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mIsFirstOnResume) {
			//更新产品信息
			mContent.updateInfoInterface(BaoxiuCardObject.getBaoxiuCardObject());
			//更新家信息
			mContent.updateInfoInterface(HomeObject.getHomeObject());
			//更新联系人信息，默认是用的账户信息
			mContent.updateInfoInterface(MyAccountManager.getInstance().getAccountObject());
			mIsFirstOnResume = false;
		}
		//add by chenkai, 如果已经前往登录过了，我们需要重新将账户信息和默认家信息填充.
		if (mHasRegistered) {
			mHasRegistered = false;
			mContent.updateInfoInterface(MyAccountManager.getInstance().getAccountObject().mAccountHomes.get(0));
			//更新联系人信息，默认是用的账户信息
			mContent.updateInfoInterface(MyAccountManager.getInstance().getAccountObject());
		}
		
		boolean first = MyApplication.getInstance().mPreferManager.getBoolean(KEY_FIRST_SHOW, true);
		if (first) {
			//第一次，我们需要显示使用向导
			new AlertDialog.Builder(NewCardActivity.this)
			.setTitle(R.string.usage_tips)
			.setMessage(R.string.baoxiu_choose_tip)
			.setCancelable(false)
			.setPositiveButton(R.string.button_iknown, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MyApplication.getInstance().mPreferManager.edit().putBoolean(KEY_FIRST_SHOW, false).commit();
				}
			})
			.show();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.string.menu_search:
			break;
		case R.string.menu_done:
			BaoxiuCardObject object = mMenu.getBaoxiuCardObject();
			if (!TextUtils.isEmpty(object.mLeiXin)) {
				//目前只要是选择了小类别，我们就允许更新数据
				mContent.setBaoxiuObjectAfterSlideMenu(object);
			}
			getSlidingMenu().showContent(true);
			break;
			 // Respond to the action bar's Up/Home button
        case android.R.id.home:
     	   Intent upIntent = NavUtils.getParentActivityIntent(this);
     	   if (upIntent == null) {
     		   // If we has configurated parent Activity in AndroidManifest.xml, we just finish current Activity.
     		   finish();
     		   return true;
     	   }
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onOpened() {
		//当SlidingMenu打开后，我们需要隐藏掉手动打开SlidinMenu按钮
		this.invalidateOptionsMenu();
	}


	@Override
	public void onClosed() {
		//当SlidingMenu关闭后，我们需要重新显示手动打开SlidinMenu按钮
		this.invalidateOptionsMenu();
		
	}
	
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		DebugUtils.logD(TAG, "onNewIntent " + intent);
		mHasRegistered = intent.getBooleanExtra(Intents.EXTRA_HAS_REGISTERED, false);
	}
	
	public static void startIntent(Context context, Bundle bundle) {
		Intent intent = new Intent(context, NewCardActivity.class);
		if (bundle != null) intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	public static void startIntentClearTop(Context context, Bundle bundle) {
		Intent intent = new Intent(context, NewCardActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (bundle != null) intent.putExtras(bundle);
		context.startActivity(intent);
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundles = intent.getExtras();
		if (mBundles == null) {
			DebugUtils.logD(TAG, "checkIntent failed, due to mBundles is null");
		}
		return mBundles != null;
	}
	
}