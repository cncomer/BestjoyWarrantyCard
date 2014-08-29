package com.bestjoy.app.warrantycard.ui;

import com.bestjoy.app.bjwarrantycard.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserInfoSecurityConfirmFragment extends BaseFragment{

	public static final String TAG = "UserInfoSecurityConfirmFragment";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getActivity().setTitle(R.string.title_user_info_security_confirm);
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
	}

	public void setArguments(String cell, String yanzhengma, int code, String message) {
		Bundle args = new Bundle();
		args.putString("cell", cell);
		args.putString("yanzhengma", yanzhengma);
		args.putString("message", message);
		args.putInt("code", code);
		super.setArguments(args);
	}
}
