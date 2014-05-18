package com.bestjoy.app.warrantycard.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.bestjoy.app.bjwarrantycard.R;
import com.shwy.bestjoy.utils.Intents;

public class CardCreateActivity {

	public static Intent createNewCardIntent(Context context, String pmd) {
		Intent intent = new Intent(context, CardCreateActivity.class);
		intent.putExtra(Intents.EXTRA_MD, pmd);
		return intent;
	}
	
	/**
	 * 显示前往创建名片对话框
	 * @param context
	 */
	public static Dialog showCreateCardConfirmDialog(final Context context) {
		return new AlertDialog.Builder(context)
	      .setMessage(R.string.message_create_card_confim)
	      .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					context.startActivity(CardCreateActivity.createNewCardIntent(context, MyAccountManager.getInstance().getCurrentAccountMd()));
				}
	      })
	      .setNegativeButton(android.R.string.cancel, null)
	      .create();
	}
}
