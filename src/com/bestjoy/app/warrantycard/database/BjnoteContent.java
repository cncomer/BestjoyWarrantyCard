package com.bestjoy.app.warrantycard.database;


import com.bestjoy.app.bjwarrantycard.MyApplication;

import android.net.Uri;

public class BjnoteContent {

	public static final String AUTHORITY = MyApplication.PKG_NAME + ".provider.BjnoteProvider";
    // The notifier authority is used to send notifications regarding changes to messages (insert,
    // delete, or update) and is intended as an optimization for use by clients of message list
    // cursors (initially, the email AppWidget).
    public static final String NOTIFIER_AUTHORITY =  MyApplication.PKG_NAME + ".notify.BjnoteProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    public static final String DEVICE_AUTHORITY = MyApplication.PKG_NAME + ".provider.DeviceProvider";
    public static final String DEVICE_NOTIFIER_AUTHORITY = MyApplication.PKG_NAME + ".notify.DeviceProvider";
    public static final Uri DEVICE_CONTENT_URI = Uri.parse("content://" + DEVICE_AUTHORITY);
    
    // All classes share this
    public static final String RECORD_ID = "_id";

    public static final String[] COUNT_COLUMNS = new String[]{"count(*)"};

    /**
     * This projection can be used with any of the EmailContent classes, when all you need
     * is a list of id's.  Use ID_PROJECTION_COLUMN to access the row data.
     */
    public static final String[] ID_PROJECTION = new String[] {
        RECORD_ID
    };
    public static final int ID_PROJECTION_COLUMN = 0;

    public static final String ID_SELECTION = RECORD_ID + " =?";
    
    
    public static class Accounts extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "accounts");
    }
    
    public static class Homes extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "homes");
    }
    /**我的保修卡设备*/
    public static class BaoxiuCard extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "baoxiucard");
    	public static final Uri BILL_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "baoxiucard/preview/bill");
    }
    
    public static class DaLei extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "dalei");
    }
    
    public static class XiaoLei extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "xiaolei");
    }
    
    public static class PinPai extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "pinpai");
    }
    
    public static class XingHao extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "xinghao");
    }
    
    public static class Province extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "province");
    }
    
    public static class City extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "city");
    }
    
    public static class District extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "district");
    }
    
    public static class ScanHistory extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "scan_history");
    }
    
    public static class HaierRegion extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.DEVICE_CONTENT_URI, "haierregion");
    }
    
    /**
     * 我的名片
     * @author chenkai
     *
     */
    public static class MyCard extends BjnoteContent{
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mycard");
    }
    
    /**生活圈*/
    public static class MyLife extends BjnoteContent {
    	public static final Uri CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mylife");
    	public static final Uri CONSUME_CONTENT_URI = Uri.withAppendedPath(BjnoteContent.CONTENT_URI, "mylife/consume");
    	public static final boolean DEBUG = false;
    	
    	public static String buildAllMyLifeForTel(String tel) {
    		if (DEBUG) {
    			tel = "13816284988";
    		} 
    		return "http://www.mingdown.com/cell/get2B.ashx?Cell=" + tel;
    	}
    }
}