package com.bestjoy.app.bjwarrantycard.propertymanagement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.shwy.bestjoy.utils.Intents;

/**
 * 对于家地址，我们需要让用户去匹配对应的小区物业，来使用小区功能
 * @author bestjoy
 *
 */
public class PropertyManagementChooseCommunity extends BaseActionbarActivity{

	private HomeObject mHomeObject;
	@Override
	protected boolean checkIntent(Intent intent) {
		Bundle bundle = intent.getBundleExtra(Intents.EXTRA_ID);
		if (bundle != null) {
			mHomeObject = HomeObject.getHomeObject(bundle);
		}
		
		return false;
	}
	public static Bundle getCommunityBundle() {
		Bundle bundle = new Bundle();
		bundle.putInt(Intents.EXTRA_TYPE, R.id.model_property_manager);
		bundle.putString(Intents.EXTRA_NAME, MyApplication.getInstance().getString(R.string.activity_title_choose_device_general));
		bundle.putLong("aid", MyAccountManager.getInstance().getHomeAIdAtPosition(0));
		bundle.putLong("uid", MyAccountManager.getInstance().getCurrentAccountId());
		return bundle;
	}
	public static void startActivity(Context context) {
		Intent intent = new Intent(context, PropertyManagementChooseCommunity.class);
		intent.putExtra(Intents.EXTRA_ID, getCommunityBundle());
		context.startActivity(intent);
	}

}
