package com.bestjoy.app.bjwarrantycard;

import java.io.File;

import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bestjoy.app.bjwarrantycard.privacy.IncomingCallCallbackImp;
import com.bestjoy.app.bjwarrantycard.privacy.IncomingSmsCallbackImp;
import com.bestjoy.app.bjwarrantycard.privacy.MonitorSandbox;
import com.bestjoy.app.bjwarrantycard.privacy.OutgoingCallCallbackImp;
import com.bestjoy.app.warrantycard.account.HomeObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.service.PhotoManagerUtilsV2;
import com.bestjoy.app.warrantycard.ui.SettingsPreferenceActivity;
import com.bestjoy.app.warrantycard.utils.BaiduLocationManager;
import com.bestjoy.app.warrantycard.utils.BeepAndVibrate;
import com.bestjoy.app.warrantycard.utils.DebugUtils;
import com.bestjoy.app.warrantycard.utils.VcfAsyncDownloadUtils;
import com.bestjoy.app.warrantycard.utils.WeatherManager;
import com.bestjoy.app.warrantycard.utils.YouMengMessageHelper;
import com.shwy.bestjoy.contacts.AddrBookUtils;
import com.shwy.bestjoy.utils.BitmapUtils;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DateUtils;
import com.shwy.bestjoy.utils.DeviceStorageUtils;
import com.shwy.bestjoy.utils.DevicesUtils;
import com.shwy.bestjoy.utils.SecurityUtils.SecurityKeyValuesObject;
import com.umeng.analytics.MobclickAgent;

public class MyApplication extends Application{
	
	private static final String TAG ="BJfileApp";
	/**对于不同的保修卡，我们只要确保该变量为正确的应用包名即可*/
	public static final String PKG_NAME = "com.bestjoy.app.bjwarrantycard";
	private Handler mHandler;
	private static MyApplication mInstance;
	public SharedPreferences mPreferManager;
	
	/**成功删除字符串*/
	public static final String mDeleteOk="ok";
	
	private InputMethodManager mImMgr;
	
	public DisplayMetrics mDisplayMetrics;
	
	private boolean mMonitorSvrInit = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		MobclickAgent.setDebugMode(true);
		mHandler = new Handler();
		mInstance = this;
		// init all preference default values.
//		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		//��ʼ���豸�����࣬���ڵõ��豸��Ϣ
		DevicesUtils.getInstance().setContext(this);
		DeviceStorageUtils.getInstance().setContext(this);
		ComPreferencesManager.getInstance().setContext(this);
		
		DateUtils.getInstance().setContext(this);
		VcfAsyncDownloadUtils.getInstance().setContext(this);
		AddrBookUtils.getInstance().setContext(this);
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
		
		mPreferManager = PreferenceManager.getDefaultSharedPreferences(this);
		MyAccountManager.getInstance().setContext(this);
		ServiceObject.setContext(this);
		
		mImMgr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		PhotoManagerUtilsV2.getInstance().setContext(this);
		//用于屏幕适配
		mDisplayMetrics = this.getResources().getDisplayMetrics();
		Log.d(TAG, mDisplayMetrics.toString());
		Log.d(TAG, getDeviceInfo(this));
		
		YouMengMessageHelper.getInstance().setContext(this);
		//add by chenkai, 20141010, 百度定位api
		BaiduLocationManager.getInstance().setContext(this);
		//add by chenkai, 20141011, 天气
		WeatherManager.getInstance().setContext(this);
		HomeObject.clearHomeObjectCache();
		
		
		//监听短信和电话
		IncomingCallCallbackImp.getInstance().setContext(this);
		OutgoingCallCallbackImp.getInstance().setContext(this);
		IncomingSmsCallbackImp.getInstance().setContext(this);
		initMonitorService();
	}
	
	public synchronized static MyApplication getInstance() {
		return mInstance;
	}
	
	public File getCachedContactFile(String name) {
		return new File(getFilesDir(), name+ ".vcf");
	}
	
	/**得到账户名片的头像图片文件*/
	public File getAccountCardAvatorFile(String name) {
		return new File(getAccountsFileDir(MyAccountManager.getInstance().getCurrentAccountMd()), name+ ".p");
	}
	public File getAccountCardAvatorFile(String accountUid, String name) {
		return new File(getAccountsFileDir(accountUid), name+ ".p");
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
		return new File(getAccountsFileDir(accountUid), cardMm+ ".vcf");
	}
	/**返回files/dirName目录*/
	public File getAppFilesDir(String dirName) {
		File root = new File(getFilesDir(), dirName);
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	/**返回files/accounts目录*/
	public File getAccountsRoot() {
		File accountsRoot = getAppFilesDir("accounts");
		
		if (!accountsRoot.exists()) {
			accountsRoot.mkdirs();
		}
		return accountsRoot;
	}
	/**返回files/accounts/dir目录*/
	public File getAccountsFilesDir(String dir) {
		File accountsRoot = new File(getAccountsRoot(), dir);
		
		if (!accountsRoot.exists()) {
			accountsRoot.mkdirs();
		}
		return accountsRoot;
	}
	/**返回files/accounts/accountMd目录*/
	public File getAccountsFileDir(String accountMd) {
		return getAccountsFilesDir(accountMd);
	}
	
	/**返回产品图像文件files/accounts/product/avator*/
	public File getProductPreviewAvatorFile(String photoid) {
		return new File(getProductSubDir("avator"), photoid+ ".p");
	}
	
	/**返回产品发票文件files/accounts/product/bill/*/
	public File getProductFaPiaoFile(String photoid) {
		return new File(getProductSubDir("bill"), photoid+ ".b");
	}
	/**返回产品目录files/accounts/product*/
	public File getProductDir() {
		File productRoot = new File(getAccountsRoot(), "product");
		if (!productRoot.exists()) {
			productRoot.mkdirs();
		}
		return productRoot;
	}
	/**返回产品目录files/accounts/product/dirName*/
	public File getProductSubDir(String dirName) {
		File productRoot = new File(getProductDir(), dirName);
		if (!productRoot.exists()) {
			productRoot.mkdirs();
		}
		return productRoot;
	}
	/**
	 * 得到包路径下cache/name文件
	 * @param name
	 * @return
	 */
	public File getCachedFile(String name) {
		return new File(getCacheDir(), name);
	}
	/**
	 * 得到包路径下files/accounts/dirName/fileName文件
	 * @param dirName
	 * @param fileName
	 * @return
	 */
	public File getFile(String dirName, String fileName) {
		File dir =  new File(getAccountsRoot(), dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, fileName);
	}
	/**
	 * 得到包路径下cache/dirName/fileName文件
	 * @param dirName
	 * @param fileName
	 * @return
	 */
	public File getCachedFile(String dirName, String fileName) {
		File dir =  new File(getCacheDir(), dirName);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, fileName);
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
    
    //add by chenkai, 20131208, updating check end
    
    /***
     * 显示通常的网络连接错误
     * @return
     */
    public String getGernalNetworkError() {
    	return this.getString(R.string.msg_gernal_network_error);
    }
    /***
     * 得到mnt/sdcard/xxx/目录
     * @return
     */
    private File getExternalStorageRoot() {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root = new File(Environment.getExternalStorageDirectory(), getPackageName());
    	if (!root.exists()) {
    		root.mkdirs();
    	}
    	return root;
    }
    /**
     * return mnt/sdcard/xxx/type目录
     * 返回SD卡的应用根目录，type为子目录名字， 如download、.download
     * @param type
     * @return
     */
    public File getExternalStorageRoot(String type) {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root = new File(getExternalStorageRoot(), type);
    	if (!root.exists()) {
    		root.mkdirs();
    	}
    	return root;
    }
    //add by chenkai, for Usage, 2013-06-05 begin
    /**return mnt/sdcard/xxx/accountmd目录*/
    public File getExternalStorageAccountRoot(String accountMd) {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root =  new File(getExternalStorageRoot(), accountMd);
    	if (!root.exists()) {
    		root.mkdir();
    	}
    	return root;
    }
    /**return mnt/sdcard/xxx/cache目录*/
    public File getExternalStorageCacheRoot() {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root =  new File(getExternalStorageRoot(), "cache");
    	if (!root.exists()) {
    		root.mkdir();
    	}
    	return root;
    }
    /**return mnt/sdcard/xxx/cache/dir目录*/
    public File getExternalStorageCacheRoot(String dir) {
    	if (!hasExternalStorage()) {
    		return null;
    	}
    	File root =  new File(getExternalStorageCacheRoot(), dir);
    	if (!root.exists()) {
    		root.mkdir();
    	}
    	return root;
    }
    /**返回产品使用说明书
     * return mnt/sdcard/xxx/product_usage/ky.pdf
     */
    public File getProductUsagePdf(String ky) {
		File goodsUsagePdfFile =  new File(getExternalStorageRoot("product_usage") , ky + ".pdf");
		return goodsUsagePdfFile;
	}
    /**提示没有SD卡可用*/
    public void showNoSDCardMountedMessage() {
    	showMessage(R.string.msg_sd_unavailable);
    }
    //add by chenkai, for Usage, 2013-06-05 end
    
    public void hideInputMethod(IBinder token) {
    	if (mImMgr != null) {
    		mImMgr.hideSoftInputFromWindow(token, 0);
    	}
    }
    
    /**
     * 返回缓存的品牌型号文件，如果有外置SD卡，该文件会存在外置存储卡/mnt/sdcard/xxxx/xinghao目录下，否则在手机内部存储中xxx/files/accounts/xinghao目录下
     * @param pingpaiCode
     * @return
     */
    public File getCachedXinghaoFile(String pingpaiCode) {
    	File xinghaoFile =  null;
    	if (hasExternalStorage()) {
    		xinghaoFile =  new File(getCachedXinghaoExternalRoot(), pingpaiCode + ".json");
    	} else {
    		xinghaoFile =  new File(getCachedXinghaoInternalRoot() , pingpaiCode + ".json");;
    	}
		return xinghaoFile;
    }
    
    /**
     * 返回缓存在外置存储卡/mnt/sdcard/xxxx/cache/xinghao目录下的型号文件
     * @param pingpaiCode
     * @return
     */
    public File getCachedXinghaoExternalRoot() {
    	return getExternalStorageCacheRoot("xinghao");
    }
    /**
     * 得到手机内部存储中xxx/files/accounts/xinghao目录下的型号文件
     * @return
     */
    public File getCachedXinghaoInternalRoot() {
    	return getAccountsFilesDir("xinghao");
    }
    /**
     * 得到手机内部xxx/files/fileName文件
     * @return
     */
    public File getAppFiles(String fileName) {
    	File root = getFilesDir();
    	if (!root.exists()) {
    		root.mkdirs();
    	}
		return new File(root, fileName);
	}
    
    public static String getDeviceInfo(Context context) {
	    try{
	        JSONObject json = new JSONObject();
	        TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	  
	        String device_id = tm.getDeviceId();
	      
	        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	          
	        String mac = wifi.getConnectionInfo().getMacAddress();
	        json.put("mac", mac);
	      
	       if(TextUtils.isEmpty(device_id) ){
	            device_id = mac;
	       }
	      
	      if( TextUtils.isEmpty(device_id) ){
	           device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	      }
	      
	      json.put("device_id", device_id);
	      return json.toString();
	    }catch(Exception e){
	      e.printStackTrace();
	    }
	    return null;
	}
    
    public String getDeviceTotke() {
    	return mPreferManager.getString("device_token", "");
    }
    public boolean saveDeviceToken(String deviceToken) {
    	boolean ok =  mPreferManager.edit().putString("device_token", deviceToken).commit();
    	DebugUtils.logD(TAG, "saveDeviceToken " + deviceToken + ", saved " + ok);
    	return ok;
    }
    
    public void initMonitorService() {
		if (!mMonitorSvrInit) {
			Intent svr = MonitorSandbox.getMonitorCallServiceInitIntent(this);
			this.startService(svr);
			mMonitorSvrInit = true;
		}
	}
    
    /**区号，对于一些商家座机，本地号码我们需要追加区号，比如上海是021，这个值需要用户在设置里修改*/
    private String mDefaultAreaCode;
    public String getPreferAreaCode() {
		if (mDefaultAreaCode == null) {
			mDefaultAreaCode = getString(R.string.preferences_privacy_area_code_default_value);
		}
		String areaCode = mPreferManager.getString(SettingsPreferenceActivity.KEY_PRIVACY_AREA_CODE, mDefaultAreaCode);
		if (TextUtils.isEmpty(areaCode)) {
			areaCode = mDefaultAreaCode;
		}
		return areaCode;
	}
}
