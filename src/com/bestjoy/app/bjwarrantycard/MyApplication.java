package com.bestjoy.app.bjwarrantycard;

import java.io.File;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.utils.BeepAndVibrate;
import com.bestjoy.app.warrantycard.utils.BitmapUtils;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.DeviceStorageUtils;
import com.shwy.bestjoy.utils.DevicesUtils;
import com.shwy.bestjoy.utils.SecurityUtils.SecurityKeyValuesObject;

public class MyApplication extends Application{
	
	private static final String TAG ="BJfileApp";
	/**对于不同的保修卡，我们只要确保该变量为正确的应用包名即可*/
	public static final String PKG_NAME = "com.bestjoy.app.bjwarrantycard";
	private Handler mHandler;
	private static MyApplication mInstance;
	public SharedPreferences mPreferManager;
	
	/**成功删除字符串*/
	public static final String mDeleteOk="ok";
	
	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
		mInstance = this;
		// init all preference default values.
//		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		//��ʼ���豸�����࣬���ڵõ��豸��Ϣ
		DevicesUtils.getInstance().setContext(this);
		DeviceStorageUtils.getInstance().setContext(this);
//		//��ʼ���˺Ź�����
//		BjnoteAccountsManager.getInstance().setContext(this);
//		IncomingCallCallbackImp.getInstance().setContext(this);
//		OutgoingCallCallbackImp.getInstance().setContext(this);
//		IncomingSmsCallbackImp.getInstance().setContext(this);
//		initMonitorService();
//		ModuleSettings.getInstance().setContext(this);
		//��ʼ����Ƭ�������,������PhotoManagerService����ά��
//		PhotoManagerUtils.getInstance().setContext(this);
//		startService(PhotoManagerService.getServiceIntent(this));
		
		DateUtils.getInstance().setContext(this);
//		VcfAsyncDownloadUtils.getInstance().setContext(this);
//		BeepAndVibrate.getInstance().setContext(this);
//		AddrBookUtils.getInstance().setContext(this);
//		GoodsManager.getInstance().setContext(this);
//		
//		//add by chenkai, 2013-07-21
//		MyLifeManager.getInstance().setContext(this);
//		ContactBackupManager.getInstance().setContext(this);
//		
//		Contact.init(this);
		//add by chenkai, 20131201, 网络监听
		ComConnectivityManager.getInstance().setContext(this);
		BeepAndVibrate.getInstance().setContext(this);
		
		BitmapUtils.getInstance().setContext(this);
		
		MyAccountManager.getInstance().setContext(this);
		
		mPreferManager = PreferenceManager.getDefaultSharedPreferences(this);
		ServiceObject.setContext(this);
		
	}
	
	public synchronized static MyApplication getInstance() {
		return mInstance;
	}
	
	public File getCachedContactFile(String name) {
		return new File(getFilesDir(), name+ ".vcf");
	}
	
	/**得到账户名片的头像图片文件*/
	public File getAccountCardAvatorFile(String name) {
		return new File(getAccountDir(MyAccountManager.getInstance().getCurrentAccountMd()), name+ ".p");
	}
	public File getAccountCardAvatorFile(String accountUid, String name) {
		return new File(getAccountDir(accountUid), name+ ".p");
	}
	/**返回缓存目录caches/下面的临时头像文件*/
	public File getCachedPreviewAvatorFile(String name) {
		return new File(getCacheDir(), name+ ".p");
	}
	/**返回缓存目录caches/下面的临时vcf文件*/
	public File getCachedPreviewContactFile(String name) {
		return new File(getCacheDir(), name+ ".vcf");
	}
	public File getAccountCardFile(String accountUid, String cardMm) {
		if (TextUtils.isEmpty(accountUid) || TextUtils.isEmpty(cardMm)) {
			DebugUtils.logE(TAG, "getAccountCardFile return null due to accountMd=" + accountUid + " cardMm" + cardMm);
			return null;
		}
		return new File(getAccountDir(accountUid), cardMm+ ".vcf");
	}
	
	public File getAppFilesDir(String dirName) {
		File root = new File(getFilesDir(), dirName);
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	public File getAccountsRoot() {
		File accountsRoot = getAppFilesDir("accounts");
		
		if (!accountsRoot.exists()) {
			accountsRoot.mkdirs();
		}
		return accountsRoot;
	}
	
	public File getAccountDir(String accountMd) {
		File accountRoot = new File(getAppFilesDir("accounts"), accountMd);
		
		if (!accountRoot.exists()) {
			accountRoot.mkdirs();
		}
		return accountRoot;
	}
	
	/**返回产品图像文件files/product/avator*/
	public File getProductPreviewAvatorFile(String photoid) {
		return new File(getProductSubDir("avator"), photoid+ ".p");
	}
	
	/**返回产品发票文件files/product/bill/*/
	public File getProductFaPiaoFile(String photoid) {
		return new File(getProductSubDir("bill"), photoid+ ".b");
	}
	
	public File getProductDir() {
		File productRoot = new File(getAppFilesDir("accounts"), "product");
		if (!productRoot.exists()) {
			productRoot.mkdirs();
		}
		return productRoot;
	}
	public File getProductSubDir(String dirName) {
		File productRoot = new File(getProductDir(), dirName);
		if (!productRoot.exists()) {
			productRoot.mkdirs();
		}
		return productRoot;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		ComConnectivityManager.getInstance().endConnectivityMonitor();
	}
	
	
	public boolean hasExternalStorage() {
	    	return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public void showMessageAsync(final int resId) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mInstance, resId, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void showMessageAsync(final String msg) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mInstance, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void showMessageAsync(final int resId, final int length) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mInstance, resId, resId).show();
			}
		});
	}
	
	public void showMessageAsync(final String msg, final int length) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mInstance, msg, length).show();
			}
		});
	}
	
	public void showShortMessageAsync(final int msgId, final int toastId) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(mInstance, msgId, toastId).show();
			}
		});
	}
	
	public void showMessage(int resId) {
		Toast.makeText(mInstance, resId, Toast.LENGTH_LONG).show();
	}
	
	public void showMessage(String msg) {
		Toast.makeText(mInstance, msg, Toast.LENGTH_LONG).show();
	}
	public void showMessage(String msg, int length) {
		Toast.makeText(mInstance, msg, length).show();
	}
	
	public void showMessage(int resId, int length) {
		Toast.makeText(mInstance, resId, length).show();
	}
	
	public void showShortMessage(int resId) {
		showMessage(resId, Toast.LENGTH_SHORT);
	}
	
	public void postAsync(Runnable runnable){
		mHandler.post(runnable);
	}
	public void postDelay(Runnable runnable, long delayMillis){
		mHandler.postDelayed(runnable, delayMillis);
	}
	
	
	public void showUnsupportMessage() {
    	showMessage(R.string.msg_unsupport_operation);
    }
	/**显示需要先登录提示信息*/
	public void showNeedLoginMessage() {
    	showMessage(R.string.msg_need_login_operation);
    }
	
	/**显示需要先新建家提示信息*/
	public void showNeedHomeMessage() {
    	showMessage(R.string.msg_need_home_operation);
    }
	
	//add by chenkai, 20131123, security support begin
    private SecurityKeyValuesObject mSecurityKeyValuesObject;
    public SecurityKeyValuesObject getSecurityKeyValuesObject() {
    	if (mSecurityKeyValuesObject == null) {
    		//Here, we need to notice.
    		new Exception("warnning getSecurityKeyValuesObject() return null").printStackTrace();
    	}
    	return mSecurityKeyValuesObject;
    }
    public void setSecurityKeyValuesObject(SecurityKeyValuesObject securityKeyValuesObject) {
    	mSecurityKeyValuesObject = securityKeyValuesObject;
    }
    
  //add by chenkai, 20131208, updating check begin
    public File buildLocalDownloadAppFile(int downloadedVersionCode) {
    	StringBuilder sb = new StringBuilder("Warranty_");
    	sb.append(String.valueOf(downloadedVersionCode))
    	.append(".apk");
    	return new File(getExternalStorageRoot(".download"), sb.toString());
    }
    
    /**
     * 返回SD卡的应用根目录，type为子目录名字， 如download、.download
     * @param type
     * @return
     */
    public File getExternalStorageRoot(String type) {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root = new File(Environment.getExternalStorageDirectory(), getPackageName());
    	if (!root.exists()) {
    		root.mkdirs();
    	}
    	root =  new File(root, type);
    	if (!root.exists()) {
    		root.mkdir();
    	}
    	return root;
    }
    //add by chenkai, 20131208, updating check end
}