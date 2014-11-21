package com.bestjoy.app.bjwarrantycard.im;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.ui.BaseSlidingFragmentActivity;
import com.bestjoy.app.warrantycard.ui.ModleBaseFragment;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.shwy.bestjoy.utils.ComPreferencesManager;

public class RelationshipActivity extends BaseSlidingFragmentActivity implements 
	SlidingMenu.OnOpenedListener, SlidingMenu.OnClosedListener{
	private static final String TAG = "RelationshipActivity";
	private Handler mHandler;
	private Fragment mContent;
	private RelationshipFragment mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing()) {
			return;
		}
		
		if (savedInstanceState != null) {
			mContent = (ModleBaseFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent-wuyi");
			mMenu = (RelationshipFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mMenu-wuyi");
			DebugUtils.logW(TAG, "onCreate() savedInstanceState != null, we try to get Fragment from FragmentManager, mContent=" + mContent + ", mMenu=" + mMenu);
		}
		setContentView(R.layout.content_frame);
		setBehindContentView(R.layout.menu_frame);
		if (mContent == null) {
			mContent = new RelationshipConversationFragment();
			// set the Above View
			getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.content_frame, mContent)
			.commit();
		} else {
			// set the Above View
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.content_frame, mContent)
			.commit();
		}
		
		if (mMenu == null) {
			mMenu = new RelationshipFragment();
			// set the Behind View
			getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.menu_frame, mMenu)
			.commit();

		} else {
			// set the Behind View
			getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.menu_frame, mMenu)
			.commit();

		}
		
		
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
//		sm.setBehindOffsetRes(R.dimen.choose_device_slidingmenu_offset);
//        sm.setAboveOffsetRes(R.dimen.choose_device_slidingmenu_offset);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindScrollScale(0.25f);
		sm.setFadeEnabled(true);
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
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onOpened() {
		//当SlidingMenu打开后，我们需要隐藏掉手动打开SlidinMenu按钮
		this.invalidateOptionsMenu();
		boolean first = ComPreferencesManager.getInstance().isFirstLaunch(RelationshipFragment.FIRST, true);
		if (first) {
			ComPreferencesManager.getInstance().setFirstLaunch(RelationshipFragment.FIRST, false);
			mMenu.forceRefresh();
		}
	}


	@Override
	public void onClosed() {
		//当SlidingMenu关闭后，我们需要重新显示手动打开SlidinMenu按钮
		this.invalidateOptionsMenu();
		
	}
	
	 public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, R.string.menu_contact, 1, R.string.menu_contact);
		item.setIcon(R.drawable.menu_contact);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         return true;
     }
	 @Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			MenuItem menuItem = menu.findItem(R.string.menu_contact);
			if (menuItem != null) {
				menuItem.setVisible(!getSlidingMenu().isMenuShowing());
			}
			return true;
		}
	 @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.string.menu_contact:
        	 SlidingMenu sm = getSlidingMenu();
        	 sm.showMenu();
        	 return true;
         }
         return super.onOptionsItemSelected(item);
	 }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}
	
	public static void startActivity(Context context) {
		Intent intent = new Intent(context, RelationshipActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
