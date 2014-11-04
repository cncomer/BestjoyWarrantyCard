package com.bestjoy.app.warrantycard.ui;

import android.os.Bundle;

import com.shwy.bestjoy.utils.InfoInterface;

public abstract class ModleBaseFragment extends BaseFragment{

	/**请求扫描条码*/
	public static final int REQUEST_SCAN = 1;
	public boolean mShowOptionMenu = true;
	public void setShowOptionMenu(boolean showOptionMenu) {
		mShowOptionMenu = showOptionMenu;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	/**
	 * 可以用来从其他地方更新当前Fragment的数据
	 * @param infoInterface
	 */
	public abstract void updateInfoInterface(InfoInterface infoInterface);
	/**
	 * 侧滑菜单要更新当前Fragment的数据
	 * @param slideManuObject
	 */
    public abstract void setBaoxiuObjectAfterSlideMenu(InfoInterface slideManuObject);
    /**
	 * 可以用来从其他地方更新当前Fragment的数据
	 * @param infoInterface
	 */
	public abstract void updateArguments(Bundle args);
}
