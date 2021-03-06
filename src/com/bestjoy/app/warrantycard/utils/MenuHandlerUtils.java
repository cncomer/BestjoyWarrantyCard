package com.bestjoy.app.warrantycard.utils;

import android.content.Context;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.FeedbackActivity;
import com.bestjoy.app.warrantycard.ui.HomeManagerActivity;
import com.bestjoy.app.warrantycard.ui.LoginActivity;
import com.bestjoy.app.warrantycard.ui.RegisterActivity;
import com.bestjoy.app.warrantycard.ui.SettingsPreferenceActivity;
import com.bestjoy.app.warrantycard.ui.YMessageListActivity;
import com.bestjoy.app.warrantycard.ui.model.ModleSettings;
import com.bestjoy.app.warrantycard.update.AppAboutActivity;

public class MenuHandlerUtils {
	
    public static void onCreateOptionsMenu(Menu menu) {
        SubMenu subMenu1 = menu.addSubMenu(1000, R.string.menu_more, 1000, R.string.menu_more);
//        subMenu1.add(1000, R.string.menu_ymessage, 1000, R.string.menu_ymessage);
        subMenu1.add(1000, R.string.menu_login, 1001, R.string.menu_login);
        subMenu1.add(1000, R.string.menu_register, 1002, R.string.menu_register);
        subMenu1.add(1000, R.string.menu_manage_home, 1003, R.string.menu_manage_home);
        subMenu1.add(1000, R.string.menu_setting, 1004, R.string.menu_setting);
//        subMenu1.add(1000, R.string.menu_about, 1005, R.string.menu_about);
//        subMenu1.add(1000, R.string.menu_exit, 1005, R.string.menu_exit);
//        subMenu1.add(1000, R.string.title_feedback, 1006, R.string.title_feedback);
        MenuItem subMenu1Item = subMenu1.getItem();
        subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    
    public static boolean onOptionsItemSelected(MenuItem item, Context context) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case R.string.menu_login:
        	LoginActivity.startIntent(context, null);
     	   break;
        case R.string.menu_register:
        	RegisterActivity.startIntent(context, null);
      	   break;
        case R.string.menu_setting:
        	SettingsPreferenceActivity.startActivity(context);
      	   break;
        case R.string.menu_about:
        	context.startActivity(AppAboutActivity.createIntent(context));
      	   break;
        case R.string.menu_manage_home:
        	Bundle bundle = ModleSettings.createHomeCommunityBundle(context);
			bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
			HomeManagerActivity.startActivity(context, bundle);
        	break;
        case R.string.menu_ymessage:
        	//Ymessage历史记录
        	YMessageListActivity.startActivity(context, null);
        	break;
        case R.string.title_feedback:
        	FeedbackActivity.startActivity(context, MyAccountManager.getInstance().getCurrentAccountId());
        	break;
        	
//        case R.string.menu_exit:
//        	HaierAccountManager.getInstance().deleteDefaultAccount();
//        	break;

        }
        return false;
    }
    
    public static boolean onPrepareOptionsMenu(Menu menu, Context context) {
    	//如果已经登陆了，那么我们显示设置菜单
//    	MenuItem menuItem = menu.findItem(R.string.menu_setting);
    	MenuItem menuHomeManagerItem = menu.findItem(R.string.menu_manage_home);
    	if (MyAccountManager.getInstance().hasLoginned()) {
//    		if (menuItem != null) {
//    			menuItem.setVisible(true);
//    		}
    		if (menuHomeManagerItem != null) {
    			menuHomeManagerItem.setVisible(true);
    		}
    	} else {
//    		if (menuItem != null) {
//    			menuItem.setVisible(false);
//    		}
    		if (menuHomeManagerItem != null) {
    			menuHomeManagerItem.setVisible(false);
    		}
    	}
		return true;
	}
}
