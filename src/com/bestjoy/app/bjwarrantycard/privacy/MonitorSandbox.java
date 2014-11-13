package com.bestjoy.app.bjwarrantycard.privacy;

import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;

public class MonitorSandbox extends Service{
	private static final String TAG = "MonitorSandbox";
	private Context mContext;
	private static MonitorSandbox INSTANCE;
	private List<IMonitorCallback> mPhoneStateListeners = new LinkedList<IMonitorCallback>();
	private SmsMonitor mSmsMonitor;
	private PhoneMonitor mPhoneMonitor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		INSTANCE = this;
		this.startForeground(101010101, new Notification());
		addAllMonitorCallbacks();
	}

	public static MonitorSandbox getInstance() {
		return INSTANCE;
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return super.onStartCommand(intent, flags, startId);
	}


	private void handleCommand(Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (Intents.MonitorService.ACTION_START_MONITOR.equalsIgnoreCase(action)) {
			DebugUtils.logD(TAG, "handleCommand ACTION_START_MONITOR");
			startListen();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	};
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopListen();
	}

	/**
	 * 每增加一个IMonitorCallback的实现，需要在此处添加。
	 */
	private void addAllMonitorCallbacks() {
		DebugUtils.logD(TAG, "addAllMonitorCallbacks");
		IncomingCallCallbackImp.getInstance().toggle(mPhoneStateListeners);
		OutgoingCallCallbackImp.getInstance().toggle(mPhoneStateListeners);
		IncomingSmsCallbackImp.getInstance().toggle(mPhoneStateListeners);
		
	}
	
	public void toggleListen(IMonitorCallback callback) {
		callback.toggle(mPhoneStateListeners);
	}

	/**
	 * 开始监听电话和短信状态，MonitorSandbox服务开始后必须调用该方法，直到服务终止调用stopListene().
	 */
	public void startListen() {
		startListenSms();
		startListenCall();
	}
	
	/**
	 * MonitorSandbox服务终止时需要调用stopListene().参见onDestroy().
	 */
	public void stopListen() {
		stopListenSms();
		stopListenCall();
	}
	
	public void startListenCall() {
		if (mPhoneMonitor == null) {
			IntentFilter phoneStateFilter = new IntentFilter();
			phoneStateFilter.addAction("android.intent.action.PHONE_STATE");
			phoneStateFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
			mPhoneMonitor = new PhoneMonitor();
			mContext.registerReceiver(mPhoneMonitor, phoneStateFilter);
		}
	}
	
	public void startListenSms() {
		if (mSmsMonitor == null) {
			IntentFilter smsIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
			smsIntentFilter.setPriority(Integer.MAX_VALUE);
			mSmsMonitor = new SmsMonitor();
			mContext.registerReceiver(mSmsMonitor, smsIntentFilter);
		}
	}
	
//	public void startListenIncomingCall() {
//		IntentFilter phoneStateFilter = new IntentFilter();
//		phoneStateFilter.addAction("android.intent.action.PHONE_STATE");
//		phoneStateFilter.addAction("android.intent.action.NEW_OUTGOING_CALL");
//		mPhoneMonitor = new PhoneMonitor();
//		mContext.registerReceiver(mPhoneMonitor, phoneStateFilter);
//	}
	
	public void stopListenCall() {
		if (mPhoneMonitor != null){
			mContext.unregisterReceiver(mPhoneMonitor);
			mPhoneMonitor = null;
		}
		
	}
	
	public void stopListenSms() {
		if (mSmsMonitor != null){
			mContext.unregisterReceiver(mSmsMonitor);
			mSmsMonitor = null;
		}
	}
	
//	/**
//	 * 来电监听
//	 */
//	private PhoneStateListener mCallMonitor = new PhoneStateListener(){
//
//		@Override
//		public void onCallStateChanged(int state, String incomingNumber) {
//			super.onCallStateChanged(state, incomingNumber);
//			mPhoneState = state;
//			if (mPhoneStateListeners != null && mPhoneStateListeners.size() >0) {
//				for(IMonitorCallback listener:mPhoneStateListeners) {
//					listener.onCallStateChanged(mPhoneState, incomingNumber, false);
//				}
//			}
//	    }  
//		
//	};
	
    public class PhoneMonitor extends BroadcastReceiver{
    	private boolean isOutgoing;
    	private String incomingNumber;
    	private String outgoingNumber;

        @Override
        public void onReceive(Context context, Intent intent) {
            //如果是拨打电话
            if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){   
            	isOutgoing = true;
            	outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);        
                notifyCallMonitor(true, -1, outgoingNumber);
            } else {                        
                    //如果是来电
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    int state = tm.getCallState();
                    switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                    	isOutgoing = false;//标识当前是来电
                    	incomingNumber = intent.getStringExtra("incoming_number");
                        DebugUtils.logD(TAG, "RINGING :"+ incomingNumber);
                        notifyCallMonitor(false, state, incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:                                
                        if (!isOutgoing) {
                        	DebugUtils.logD(TAG, "incoming ACCEPT :"+ incomingNumber);
                        	notifyCallMonitor(false, state, incomingNumber);
                        } else {
                        	DebugUtils.logD(TAG, "outgoing ACCEPT :"+ outgoingNumber);
                        	notifyCallMonitor(true, state, outgoingNumber);
                        }
                        
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:                                
                        if(!isOutgoing){
                        	DebugUtils.logD(TAG, "incoming IDLE");
                        	notifyCallMonitor(false, state, incomingNumber);
                        } else {
                        	DebugUtils.logD(TAG, "outgoing IDLE");
                        	notifyCallMonitor(true, state, outgoingNumber);
                        }
                        outgoingNumber = null;
                        incomingNumber = null;
                        break;
                    } 
            }
        }
    }
    
    private void notifyCallMonitor(boolean outgoing, int state, String number) {
    	for(IMonitorCallback callback:mPhoneStateListeners) {
//    		if (outgoing) {
//    			callback.onPhoneOutgoing(number);
//    			
//    		} else {
    			switch(state) {
    			case -1:
    				callback.onPhoneOutgoing(number);
    				break;
    			case TelephonyManager.CALL_STATE_RINGING:
    				callback.onPhoneRing(outgoing, number);
    				break;
    			case TelephonyManager.CALL_STATE_OFFHOOK:
    				callback.onPhoneOffhook(outgoing, number);
    				break;
    			case TelephonyManager.CALL_STATE_IDLE:
    				callback.onPhoneIdle(outgoing, number);
    				break;
    			}
//    		}
    	}
    }
    
//	public void addPhoneMonitorCallback(IMonitorCallback listener) {
//		if (!mPhoneStateListeners.contains(listener)) {
//			DebugUtils.logD(TAG, "add a PhoneStateListener " + listener);
//			mPhoneStateListeners.add(listener);
//		}
//	}
//	
//	public void removePhoneMonitorCallback(IMonitorCallback listener) {
//		if (mPhoneStateListeners.contains(listener)) {
//			DebugUtils.logD(TAG, "remove a PhoneStateListener " + listener);
//			mPhoneStateListeners.remove(listener);
//		}
//	}
//	
//	public void addSmsMonitorCallback(IMonitorCallback listener) {
//		if (!mSmsMonitorCallbacks.contains(listener)) {
//			DebugUtils.logD(TAG, "add a PhoneStateListener " + listener);
//			mSmsMonitorCallbacks.add(listener);
//		}
//	}
//	
//	public void removeSmsMonitorCallback(PhoneStateListener listener) {
//		if (mSmsMonitorCallbacks.contains(listener)) {
//			DebugUtils.logD(TAG, "remove a PhoneStateListener " + listener);
//			mSmsMonitorCallbacks.remove(listener);
//		}
//	}
	
	public class SmsMonitor extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ("android.provider.Telephony.SMS_RECEIVED".equals(action)) {
				DebugUtils.logD(TAG, "android.provider.Telephony.SMS_RECEIVED");
				if (mPhoneStateListeners != null && mPhoneStateListeners.size() > 0) {
					SmsMessage[] smsMessages = Intents.getMessagesFromIntent(intent);
					for (IMonitorCallback callback:mPhoneStateListeners) {
						callback.onSmsReceive(smsMessages);
					}
				}
			}
		}
	}
	
	public static Intent getMonitorCallServiceInitIntent(Context context) {
		Intent callMonitorSvr = new Intent(context, MonitorSandbox.class);
		callMonitorSvr.setAction(Intents.MonitorService.ACTION_START_MONITOR);
		return callMonitorSvr;
	}

}
