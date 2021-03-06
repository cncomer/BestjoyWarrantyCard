package com.bestjoy.app.warrantycard.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.WindowManager;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.bjwarrantycard.im.ConversationItemObject;
import com.bestjoy.app.bjwarrantycard.im.IMHelper;
import com.bestjoy.app.bjwarrantycard.im.RelationshipConversationFragment;
import com.bestjoy.app.bjwarrantycard.im.RelationshipObject;
import com.bestjoy.app.warrantycard.account.AccountObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.ComConnectivityManager;
import com.shwy.bestjoy.utils.ComPreferencesManager;
import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.Intents;
import com.shwy.bestjoy.utils.NotifyRegistrant;

public class IMService extends Service{

	private static final String TAG = "IMService";
	private static final String ACTION_CONNECT_IM_SERVICE = "Action.connect";
	private static final String ACTION_DISCONNECT_IM_SERVICE = "Action.disconnect";
	//为了简单起见，所有的异常都直接往外抛  
    private static final String HOST = "115.29.231.29";//"192.168.1.107";//"115.29.231.29";  //要连接的服务端IP地址  
    private static final int PORT = 1040;   //要连接的服务端对应的监听端口 
    private static final int BUFFER_LENGTH = 4 * 1024; //4k
    private CoversationReceiveServerThread mCoversationReceiveServerThread;
	private DatagramSocket mSocket;
	private HandlerThread mWorkThread;
	private Handler mWorkHandler;
	/**发送登录消息*/
	public static final int WHAT_SEND_MESSAGE_LOGIN = 1000;
	/**获得消息*/
	public static final int WHAT_SEND_MESSAGE = 1001;
	/**发送退出消息*/
	public static final int WHAT_SEND_MESSAGE_EXIT = 1002;
	/**收到用户已离线的消息*/
	public static final int WHAT_SEND_MESSAGE_OFFLINE = 1003;
	/**收到用户验证失败的消息*/
	public static final int WHAT_SEND_MESSAGE_INVALID_USER = 1004;
	/**无网络*/
	public static final int WHAT_SEND_MESSAGE_NO_NETWORK = 1008;
	/**30s*/
	private static final int HEART_BEAT_DELAY_TIME = 30 * 1000;
	/**在会话结束前，我们需要等待，比如退出当前界面*/
	private boolean mIsConnected = false;
	private boolean mIsRecLoginRes = false;
	/**会话中信息不会显示成Notification*/
	private boolean mIsInConversationSession = false;
	private String mConversationSessionTarget = "";
	
	private ContentResolver mContentResolver;
//	WifiManager.MulticastLock mMulticastLock;
	private Handler mUiHandler;
	
	private static final int WHAT_CHECK_SENDING_MESSAGE = 10;
	private static final int WHAT_CHECK_HEART_BEAT = 11;
	private static final int CHECK_HEART_BEAT_DELAY = 20 *1000; //20秒中如果没有检查到心跳包，就认为是和服务器暂时无法连接
	@Override
	public void onCreate() {
		super.onCreate();
		DebugUtils.logD(TAG, "onCreate()");
		mContentResolver = this.getContentResolver();
		mUiHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case WHAT_SEND_MESSAGE_OFFLINE:
					showOfflineDialog((String)msg.obj);
					break;
				}
			}
		};
		mWorkThread = new HandlerThread("IMServiceThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
				case WHAT_CHECK_SENDING_MESSAGE:
					//检查发送时间大于10秒的信息，标记为发送失败
					ContentValues values = new ContentValues();
					values.put(HaierDBHelper.IM_MESSAGE_STATUS, 2);
					int updated = IMHelper.update(mContentResolver, IMHelper.TARGET_TYPE_P2P, values, "(?-" + HaierDBHelper.DATE + ">"+ CHECK_HEART_BEAT_DELAY+") and " + HaierDBHelper.IM_MESSAGE_STATUS +"=0", new String[]{String.valueOf(new Date().getTime())});
					DebugUtils.logD(TAG, "WHAT_CHECK_SENDING_MESSAGE update unsended message to failed, affected rows#" + updated);
					break;
				case WHAT_CHECK_HEART_BEAT:
					//如果10秒钟没有接收到登录应答，我们认为连接丢失，等待重新连接
					mIsConnected = false;
					//通知连接结果
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_NO_NETWORK);
					break;
				case WHAT_SEND_MESSAGE_LOGIN:
					
					mIsRecLoginRes = false;
					AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
					if (accountObject == null) {
						DebugUtils.logD(TAG, "receiveMessageLocked ignore due to  accountObject == null");
						return;
					}
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_LOGIN);
					//发送登录消息
					try {
						sendMessageLocked(IMHelper.createOrJoinConversation(String.valueOf(accountObject.mAccountUid), accountObject.mAccountPwd, accountObject.mAccountName).toString().getBytes("UTF-8"));
						//心跳检测
				 		mWorkHandler.sendEmptyMessageDelayed(WHAT_SEND_MESSAGE_LOGIN, HEART_BEAT_DELAY_TIME);
				 		mWorkHandler.sendEmptyMessageDelayed(WHAT_CHECK_HEART_BEAT, CHECK_HEART_BEAT_DELAY);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case WHAT_SEND_MESSAGE_EXIT:
					try {
						ConversationItemObject message = (ConversationItemObject) msg.obj;
						sendMessageLocked(IMHelper.exitConversation(message.mUid, message.mPwd, message.mUName).toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case WHAT_SEND_MESSAGE:
					ConversationItemObject message = (ConversationItemObject) msg.obj;
					try {
						//在发送消息的时候，我们先在本地新增一条发送中的数据
						message.saveInDatebaseWithoutCheckExisted(mContentResolver, null);
						sendMessageLocked(IMHelper.createMessageConversation(message).toString().getBytes("UTF-8"));
						mWorkHandler.sendEmptyMessageDelayed(WHAT_CHECK_SENDING_MESSAGE, CHECK_HEART_BEAT_DELAY);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
						message.mMessageStatus = 2;
						message.updateInDatebase(mContentResolver, null);
					}
					break;
				
				}
			}
			
		};
//		WifiManager manager = (WifiManager) this .getSystemService(Context.WIFI_SERVICE);
//		mMulticastLock = manager.createMulticastLock("IMService");
//		if (!mMulticastLock.isHeld()) {
//			mMulticastLock.acquire();
//		}
		DebugUtils.logD(TAG, "start Conversation.");
	    //我们开始启动接收UDP包线程
		if (mCoversationReceiveServerThread == null) {
			mCoversationReceiveServerThread = new CoversationReceiveServerThread();
			mCoversationReceiveServerThread.start();
		}
		
		//启动一个计时器来定时检查发送状态
		mWorkHandler.sendEmptyMessageDelayed(WHAT_CHECK_SENDING_MESSAGE, CHECK_HEART_BEAT_DELAY);
		
		ComConnectivityManager.getInstance().addConnCallback(new ComConnectivityManager.ConnCallback() {
			
			@Override
			public void onConnChanged(ComConnectivityManager cm) {
				if (cm.isConnected()) {
					//如果网络变化了，我们立即重新发送心跳包
					mWorkHandler.sendEmptyMessageDelayed(WHAT_SEND_MESSAGE_LOGIN, 500);//立即登录一次
				} else {
					mIsConnected = false;
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_NO_NETWORK);
				}
			}
		});
	}

	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		DebugUtils.logD(TAG, "onStart() " + intent);
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_CONNECT_IM_SERVICE.equals(action)) {
				setIsInConversationSession(false, "");
				mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_EXIT);
				mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
				mWorkHandler.sendEmptyMessageDelayed(WHAT_SEND_MESSAGE_LOGIN, 500);//立即登录一次
			} else if (ACTION_DISCONNECT_IM_SERVICE.equals(action)) {
				setIsInConversationSession(false, "");
				Message msg = obtainExitMessage(intent);
				mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
				mWorkHandler.sendMessage(msg);
			} else if (Intent.ACTION_USER_PRESENT.equals(action)) {
				mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_EXIT);
				mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
				//用户解锁了，我们一定时间后触发登录
				mWorkHandler.sendEmptyMessageDelayed(WHAT_SEND_MESSAGE_LOGIN, HEART_BEAT_DELAY_TIME);
			}
		}
		
	}
	
	private Message obtainExitMessage(Intent intent) {
		ConversationItemObject data = new ConversationItemObject();
		data.mUid = String.valueOf(intent.getLongExtra(Intents.EXTRA_ID, -1));
		data.mUName = intent.getStringExtra(Intents.EXTRA_NAME);
		data.mPwd = intent.getStringExtra(Intents.EXTRA_PASSWORD);
		Message message = Message.obtain();
		message.what = WHAT_SEND_MESSAGE_EXIT;
		message.obj = data;
		return message;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCoversationReceiveServerThread != null) {
			mCoversationReceiveServerThread.cancel();
			mCoversationReceiveServerThread = null;
		}
		if(mSocket != null) {
			mSocket.close();
			DebugUtils.logD(TAG, "close Conversation.");
		}
		
//		if (mMulticastLock.isHeld()) {
//			mMulticastLock.release();
//			mMulticastLock = null;
//		}
	}


	/**
	 * 调用该接口发送信息
	 * @param message
	 */
	public void sendMessageAsync(ConversationItemObject message) {
		Message msg = Message.obtain();
		msg.what = WHAT_SEND_MESSAGE;
		msg.obj = message;
		mWorkHandler.sendMessage(msg);
	}
	
	public boolean isConnected() {
		return mIsConnected;
	}
	
	public void setIsInConversationSession(boolean inConversationSession, String target) {
		mIsInConversationSession = inConversationSession;
		mConversationSessionTarget = target;
	}
	
	public static void startService(Context context) {
		DebugUtils.logD(TAG, "startService().");
		Intent intent = new Intent(context, IMService.class);
		context.startService(intent);
	}
	
	public static void connectIMService(Context context) {
		DebugUtils.logD(TAG, "connectIMService().");
		Intent intent = new Intent(context, IMService.class);
		intent.setAction(ACTION_CONNECT_IM_SERVICE);
		context.startService(intent);
	}
	
	public static void connectIMServiceOnUserPresent(Context context) {
		DebugUtils.logD(TAG, "connectIMServiceOnUserPresent().");
		Intent intent = new Intent(context, IMService.class);
		intent.setAction(Intent.ACTION_USER_PRESENT);
		context.startService(intent);
	}
	
	public static void disconnectIMService(Context context, AccountObject accountObject) {
		DebugUtils.logD(TAG, "disconnectIMService().");
		Intent intent = new Intent(context, IMService.class);
		intent.setAction(ACTION_DISCONNECT_IM_SERVICE);
		intent.putExtra(Intents.EXTRA_ID, accountObject.mAccountUid);
		intent.putExtra(Intents.EXTRA_PASSWORD, accountObject.mAccountPwd);
		intent.putExtra(Intents.EXTRA_NAME, accountObject.mAccountName);
		context.startService(intent);
	}
	
	public static void stopService(Context context) {
		Intent intent = new Intent(context, IMService.class);
		context.stopService(intent);
	}
	
	private MyBinder myBinder = new MyBinder();
	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}
	
	public class MyBinder extends Binder{
        
        public IMService getService(){
            return IMService.this;
        }
    }
    
	private void sendMessageLocked(final byte[] data) throws IOException {
		DebugUtils.logD(TAG, "sendMessageLocked data.length " + data.length);
		if(data.length!=0 && mSocket != null){
			DatagramPacket dp=new DatagramPacket(data, data.length, InetAddress.getByName(HOST), PORT);
			mSocket.send(dp);
		}
	}
	
	
	private void receiveMessageLocked(String message) {
		DebugUtils.logD(TAG, "receiveMessageLocked receive: " + message);
		AccountObject accountObject = MyAccountManager.getInstance().getAccountObject();
		if (accountObject == null) {
			DebugUtils.logD(TAG, "receiveMessageLocked ignore due to  accountObject == null");
			return;
		}
		if (!TextUtils.isEmpty(message)) {
			IMHelper.ImServiceResultObject serviceResult = IMHelper.ImServiceResultObject.parse(message);
			if (serviceResult.isOpSuccessfully()) {
				int type = Integer.valueOf(serviceResult.mType);
				switch(type){
				case IMHelper.TYPE_LOGIN: //登录成功
					mIsConnected = true;
					mIsRecLoginRes = true;
					mWorkHandler.removeMessages(WHAT_CHECK_HEART_BEAT);
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_LOGIN);
					break;
				case IMHelper.TYPE_EXIT: //退出登录成功
					mIsConnected = false;
					setIsInConversationSession(false, "");
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_EXIT);
					break;
				case IMHelper.TYPE_MESSAGE: //我发送的消息得到了返回
				case IMHelper.TYPE_MESSAGE_FORWARD: //收到别人发送的消息
					ConversationItemObject conversationItemObject = IMHelper.getConversationItemObject(serviceResult.mJsonData);
					if (conversationItemObject != null) {
						conversationItemObject.mUid = serviceResult.mUid;
						conversationItemObject.mUName = serviceResult.mUName;
						conversationItemObject.mPwd = serviceResult.mPwd;
						StringBuilder sb = new StringBuilder();
						sb.append(conversationItemObject.mUid).append('_').append(conversationItemObject.mTarget).append('_').append(conversationItemObject.mServiceDate);
						conversationItemObject.mServiceId = sb.toString();
						DebugUtils.logD(TAG, "getConversationItemObject(JSONObject result) mServiceId " + conversationItemObject.mServiceId);
						boolean op = false;
						String[] selectionArgs = new String[]{conversationItemObject.mTarget, conversationItemObject.mUid, conversationItemObject.mUid, conversationItemObject.mTarget};
						if (IMHelper.TYPE_MESSAGE == type) {
							if (conversationItemObject.mUid.equals(MyAccountManager.getInstance().getCurrentAccountUid())) {
								//服务器返回了我们之前发送的信息，表明该条消息发送成功，我们更新本地信息的发送状态
								conversationItemObject.mMessageStatus = 1;
								op = conversationItemObject.updateInDatebase(mContentResolver, null);
							}
							selectionArgs = new String[]{conversationItemObject.mUid, conversationItemObject.mTarget, conversationItemObject.mTarget, conversationItemObject.mUid};
						} else if (IMHelper.TYPE_MESSAGE_FORWARD == type) {
							//收到其他用户发来的消息，我们只要保存就好了
							conversationItemObject.mId = -1;
							conversationItemObject.mMessageStatus = 1; //对于收到的信息发送状态总是已发送的
							if (mIsInConversationSession) {
								//已在会话中，我们需要确定这条消息是否是当前会话的，是的话，标记为已读
								if (conversationItemObject.mTarget.equals(MyAccountManager.getInstance().getCurrentAccountUid()) && 
										mConversationSessionTarget.equals(conversationItemObject.mUid)) {
									conversationItemObject.setReadStatus(ConversationItemObject.SEEN);
									DebugUtils.logD(TAG, "mark seen " + conversationItemObject.mMessage);
								} else {
									DebugUtils.logD(TAG, "mark unseen " + conversationItemObject.mMessage);
								}
							} else {
								//不在会话中，我们需要标记该消息未读
								conversationItemObject.setReadStatus(ConversationItemObject.UN_SEEN);
								DebugUtils.logD(TAG, "mark unseen " + conversationItemObject.mMessage);
							}
							op = conversationItemObject.saveInDatebase(mContentResolver, null);
							selectionArgs = new String[]{conversationItemObject.mTarget, conversationItemObject.mUid, conversationItemObject.mUid, conversationItemObject.mTarget};
						}
						if (op) {
							ContentValues values = new ContentValues();
							values.clear();
							values.put(BjnoteContent.RELATIONSHIP.RELATIONSHIP_PROJECTION[BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_NEW_MESSAGE], conversationItemObject.mMessage);
							values.put(BjnoteContent.RELATIONSHIP.RELATIONSHIP_PROJECTION[BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_NEW_MESSAGE_TIME], conversationItemObject.mServiceDate);
							values.put(BjnoteContent.RELATIONSHIP.RELATIONSHIP_PROJECTION[BjnoteContent.RELATIONSHIP.INDEX_RELASTIONSHIP_LOCAL_DATE], new Date().getTime());
							int updated = BjnoteContent.update(mContentResolver, BjnoteContent.RELATIONSHIP.CONVERSATION_CONTENT_URI, values, RelationshipObject.WHERE_UID_AND_TARGET + " or " + RelationshipObject.WHERE_TARGET_AND_UID, selectionArgs);
							if (updated == 0) {
								ComPreferencesManager.getInstance().setFirstLaunch(RelationshipConversationFragment.FIRST, true);
							}
						}
						if (!mIsInConversationSession) {
							//MyApplication.getInstance().showMessageAsync("Rec message " + conversationItemObject.mMessage);
						}
					}
					break;
				}
			} else if (serviceResult.mStatusCode == 2) {
				int type = Integer.valueOf(serviceResult.mType);
				switch(type){
				case IMHelper.TYPE_LOGIN: //登录失败
					mIsConnected = false;
					mIsRecLoginRes = true;
					mWorkHandler.removeMessages(WHAT_CHECK_HEART_BEAT);
					mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
					mUiHandler.sendEmptyMessage(WHAT_SEND_MESSAGE_OFFLINE);
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_OFFLINE);
					return;
				case IMHelper.TYPE_EXIT: //退出登录失败,目前暂时
					mIsConnected = false;
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_EXIT);
					break;
				}
				MyApplication.getInstance().showMessageAsync(serviceResult.mStatusMessage);
			} else {
				int type = Integer.valueOf(serviceResult.mType);
				switch(type){
				case IMHelper.TYPE_LOGIN: //登录失败
					mIsConnected = false;
					mWorkHandler.removeMessages(WHAT_CHECK_HEART_BEAT);
					mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_INVALID_USER);
					return;
				case IMHelper.TYPE_EXIT: //退出登录失败,目前暂时
					mIsConnected = false;
					NotifyRegistrant.getInstance().notify(WHAT_SEND_MESSAGE_EXIT);
					break;
				}
				MyApplication.getInstance().showMessageAsync(serviceResult.mStatusMessage);
			}
		}
	}
	
	public void showOfflineDialog(String title) {
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(R.string.msg_offline_confirm)
		.create();
		
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.menu_login),  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mWorkHandler.sendEmptyMessage(WHAT_SEND_MESSAGE_LOGIN);//立即登录一次
				
			}
		});
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.button_cancel),  new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		dialog.show();
		
	}
	private class CoversationReceiveServerThread extends Thread {

		private boolean _cancel = false;
		
		private void cancel() {
			_cancel = true;
		}
		@Override
		public void run() {
			super.run();
		      try {
		    	  if(mSocket == null){
	  	  		    mSocket = new DatagramSocket(null);
	  	  		    mSocket.setReuseAddress(true);
	  	  		    mSocket.bind(new InetSocketAddress(8909)); //8904
		  	     }
		    	 DebugUtils.logD(TAG, "准备接受UDP");
				byte[] buffer = new byte[BUFFER_LENGTH];
				DatagramPacket dp=new DatagramPacket(buffer,BUFFER_LENGTH);
				while(!_cancel){
					mSocket.receive(dp);
//					lst.append("对方（来自"+dp.getAddress().getHostAddress()+"，接口:"+dp.getPort()+"） "+"当前时间："+"\n"+new String(buf,0,dp.getLength())+"\n");
					String message = new String(buffer, 0, dp.getLength(), "utf-8");
					receiveMessageLocked(message);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		    //建立连接后就可以往服务端写数据了  
		}
    	
    }


}
