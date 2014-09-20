package com.bestjoy.app.bjwarrantycard.imbasedympush;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.bestjoy.app.warrantycard.utils.MenuHandlerUtils;
import com.shwy.bestjoy.utils.DebugUtils;

public class IMConversationActivity extends BaseActionbarActivity implements View.OnClickListener{

	private static final String TAG = "IMConversationActivity";
	private static final String EXTRA_SN = "sn";
	private int mCoversationTargetType = IMHelper.TARGET_TYPE_QUN;
	private String mCoversationTarget = "";
	private ListView mListView;
	private EditText mInputEdit;
	private Button mButtonCommit;
	//为了简单起见，所有的异常都直接往外抛  
    private static final String HOST = "115.29.231.29";  //要连接的服务端IP地址  
    private static final int PORT = 1029;   //要连接的服务端对应的监听端口 
    private static final int BUFFER_LENGTH = 4 * 1024; //4k
    private CoversationReceiveServerThread mCoversationReceiveServerThread;
	private DatagramSocket mSocket;
	BufferedOutputStream mOutputStream;
	BufferedInputStream mIntputStream;
	
	private HandlerThread mWorkThread;
	private Handler mWorkHandler;
	private Handler mUiHandler;
	/**发送登录消息*/
	private static final int WHAT_SEND_MESSAGE_LOGIN = 1000;
	/**获得消息*/
	private static final int WHAT_SEND_MESSAGE = 1001;
	/**发送退出消息*/
	private static final int WHAT_SEND_MESSAGE_EXIT = 1002;
	private static final int HEART_BEAT_DELAY_TIME = 30 * 1000;
	private long mMessageIndex = -1;
	/**在会话结束前，我们需要等待，比如退出当前界面*/
	private boolean mIsExsited = true;
	private boolean mIsLogined = false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (this.isFinishing()) {
			return;
		}
		setContentView(R.layout.activity_im_conversation);
		
		mListView = (ListView) findViewById(R.id.listview);
		mInputEdit = (EditText) findViewById(R.id.input);
		mButtonCommit = (Button) findViewById(R.id.button_commit);
		mButtonCommit.setOnClickListener(this);
		//当连接上IM服务器的时候设置为true
		mButtonCommit.setEnabled(false);
		mUiHandler = new Handler();
		mWorkThread = new HandlerThread("IMConversationThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch(msg.what) {
				case WHAT_SEND_MESSAGE_LOGIN:
					//发送登录消息
					try {
						sendMessageLocked(IMHelper.createOrJoinConversation(MyAccountManager.getInstance().getCurrentAccountUid(), MyAccountManager.getInstance().getAccountObject().mAccountPwd).toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case WHAT_SEND_MESSAGE_EXIT:
					try {
						sendMessageLocked(IMHelper.exitConversation(MyAccountManager.getInstance().getCurrentAccountUid(), MyAccountManager.getInstance().getAccountObject().mAccountPwd).toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				case WHAT_SEND_MESSAGE:
					try {
						sendMessageLocked(IMHelper.createMessageConversation(MyAccountManager.getInstance().getCurrentAccountUid(), MyAccountManager.getInstance().getAccountObject().mAccountPwd, mCoversationTargetType, mCoversationTarget, (String)msg.obj).toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					break;
				
				}
			}
			
		};
		mCoversationReceiveServerThread = new CoversationReceiveServerThread();
		mCoversationReceiveServerThread.start();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mWorkHandler.removeMessages(WHAT_SEND_MESSAGE_LOGIN);
		mWorkThread.quit();
		mCoversationReceiveServerThread.cancel();
	}
	
	@Override
	public void onBackPressed() {
		if (!mIsExsited) {
			return;
		}
		super.onBackPressed();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (!mIsExsited) {
			return true;
		}
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
        	if (mIsLogined) {
        		//显示退出登录对话框
        		showDialog(DIALOG_PROGRESS);
        		mWorkHandler.sendEmptyMessage(WHAT_SEND_MESSAGE_EXIT);
        		return true;
        	}
     	   Intent upIntent = NavUtils.getParentActivityIntent(this);
     	   if (upIntent == null) {
     		   // If we has configurated parent Activity in AndroidManifest.xml, we just finish current Activity.
     		   finish();
     		   return true;
     	   }
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
            return true;
        }
        return true;

    }
	
	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.button_commit:
			String text = mInputEdit.getText().toString().trim();
			if (!TextUtils.isEmpty(text)) {
				//不允许只输入空白字符，这样的内容是无意义的
				try {
					sendMessageLocked(text.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}
	
	@Override
	protected boolean checkIntent(Intent intent) {
		mCoversationTarget = intent.getStringExtra(IMHelper.EXTRA_TARGET);
		mCoversationTargetType = intent.getIntExtra(IMHelper.EXTRA_TYPE, -1);
		if (TextUtils.isEmpty(mCoversationTarget)) {
			DebugUtils.logE(TAG, "checkIntent failed, you must supply IMHelper.EXTRA_TARGET");
			return false;
		}
		if (mCoversationTargetType == -1) {
			DebugUtils.logE(TAG, "checkIntent failed, you must supply IMHelper.EXTRA_TYPE");
			return false;
		}
		return true;
	}
	
	public static void startActivity(Context context, int targetType, String target) {
		Intent intent = new Intent(context, IMConversationActivity.class);
		intent.putExtra(IMHelper.EXTRA_TARGET, target);
		intent.putExtra(IMHelper.EXTRA_TYPE, targetType);
		context.startActivity(intent);
	}
	
	private void sendMessageLocked(final byte[] data) {
		if(data.length!=0){
			try{
				DatagramPacket dp=new DatagramPacket(data, data.length, InetAddress.getByName(HOST), PORT);
				mSocket.send(dp);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	private void receiveMessageLocked(String message) {
		DebugUtils.logD(TAG, "receiveMessageLocked receive: " + message);
		if (!TextUtils.isEmpty(message)) {
			MyApplication.getInstance().showMessageAsync(message);
			IMHelper.ImServiceResultObject serviceResult = IMHelper.ImServiceResultObject.parse(message);
			if (serviceResult.isOpSuccessfully()) {
				switch(Integer.valueOf(serviceResult.mType)){
				case IMHelper.TYPE_LOGIN: //登录成功
					mIsLogined = true;
					mIsExsited = false;
					break;
				case IMHelper.TYPE_EXIT: //退出登录成功
					mIsLogined = false;
					mIsExsited = true;
					dismissDialog(DIALOG_PROGRESS);
					break;
				}
			}
		}
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
		    	  DebugUtils.logD(TAG, "start Conversation.");
		    	 mSocket = new DatagramSocket(PORT);
		    	 mWorkHandler.sendEmptyMessage(WHAT_SEND_MESSAGE_LOGIN);//立即登录一次
		 		//心跳检测
		 		mWorkHandler.sendEmptyMessageDelayed(WHAT_SEND_MESSAGE_LOGIN, HEART_BEAT_DELAY_TIME);
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
			} finally {
				mSocket.close();
				DebugUtils.logD(TAG, "close Conversation.");
			}
		      //建立连接后就可以往服务端写数据了  
		}
    	
    }
	
	

}
