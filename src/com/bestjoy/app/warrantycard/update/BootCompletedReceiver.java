package com.bestjoy.app.warrantycard.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.service.IMService;
import com.shwy.bestjoy.utils.DebugUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DebugUtils.logD("BootCompletedReceiver", "onReceive intent " + intent);
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			UpdateService.startUpdateServiceOnBootCompleted(context);
			MyApplication.getInstance().initMonitorService();
			if (MyAccountManager.getInstance().hasLoginned()) {
				IMService.connectIMService(context);
			} else {
				IMService.startService(context);
			}
		} else if ("android.intent.action.USER_PRESENT".equals(action)) {
			UpdateService.startUpdateServiceOnUserPresent(context);
			if (MyAccountManager.getInstance().hasLoginned()) {
				IMService.connectIMServiceOnUserPresent(context);
			} else {
				IMService.startService(context);
			}
		}
	}

}
