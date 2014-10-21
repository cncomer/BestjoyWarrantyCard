package com.bestjoy.app.warrantycard.utils;

import android.app.Activity;
import android.view.View;

import com.bestjoy.app.bjwarrantycard.R;
import com.shwy.bestjoy.utils.ComPreferencesManager;

public class ClingHelper {

	public static void showGuide(String key, Activity activity) {
		final View clingLayout = activity.findViewById(R.id.cling);
		if (clingLayout != null) {
			if (ComPreferencesManager.getInstance().isFirstLaunch(key, true)) {
				ComPreferencesManager.getInstance().setFirstLaunch(key, false);
				clingLayout.setVisibility(View.VISIBLE);
				activity.findViewById(R.id.cling_ok).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						clingLayout.setVisibility(View.GONE);
					}
				});
			} else {
				clingLayout.setVisibility(View.GONE);
			}
			
		}
	}
}
