package com.bestjoy.app.warrantycard.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.bestjoy.app.bjwarrantycard.R;

public class CardViewActivity extends BaseActionbarActivity {

	private CardViewFragment mContent;
	public Bundle mBundle;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing()) {
			return;
		}
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mContent = new CardViewFragment();
		mContent.setArguments(mBundle);
		// set the Above View
		setContentView(R.layout.card_content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
	}
	
	  @Override
      public boolean onCreateOptionsMenu(Menu menu) {
		  return false;
	  }

	@Override
	protected boolean checkIntent(Intent intent) {
		mBundle = getIntent().getExtras();
	    return mBundle != null;
	}
	
	/**
	 * 回到主界面
	 * @param context
	 */
	public static void startActivit(Context context, Bundle bundle) {
		Intent intent = new Intent(context, CardViewActivity.class);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		context.startActivity(intent);
	}

}
