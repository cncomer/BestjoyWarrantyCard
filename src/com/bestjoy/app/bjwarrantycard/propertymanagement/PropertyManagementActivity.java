package com.bestjoy.app.bjwarrantycard.propertymanagement;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.utils.DebugUtils;

/**
 * 对于家地址，我们需要让用户去匹配对应的小区物业，来使用小区功能
 * @author bestjoy
 *
 */
public class PropertyManagementActivity extends BaseActionbarActivity{

	private static final String TAG = "PropertyManagementActivity";
	private HomeObject mHomeObject;
	private ContentResolver mContentResolver;
	private Bundle mBundles;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		
	}
	
	@Override
	protected boolean checkIntent(Intent intent) {
		mContentResolver = this.getContentResolver();
		mBundles = intent.getExtras();
		if (mBundles == null) {
			DebugUtils.logD(TAG, "checkIntent failed, due to mBundles is null");
		} else {
			DebugUtils.logD(TAG, "checkIntent true, find mBundles=" + mBundles);
		}
		mHomeObject = HomeObject.getHomeObject(mBundles);
		return mHomeObject != null;
	}
	public static void startActivity(Context context, Bundle bundle) {
		Intent intent = new Intent(context, PropertyManagementActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

}
