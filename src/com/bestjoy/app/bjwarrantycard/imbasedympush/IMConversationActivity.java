package com.bestjoy.app.bjwarrantycard.imbasedympush;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.shwy.bestjoy.utils.DebugUtils;

public class IMConversationActivity extends BaseActionbarActivity implements View.OnClickListener{

	private static final String TAG = "IMConversationActivity";
	private static final String EXTRA_SN = "sn";
	private String mSN = "";
	private ListView mListView;
	private EditText mInputEdit;
	private Button mButtonCommit;
	//为了简单起见，所有的异常都直接往外抛  
    private static final String HOST = "115.29.231.29";  //要连接的服务端IP地址  
    private static final int PORT = 1029;   //要连接的服务端对应的监听端口 
	
	
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
		new CoversationToServerThread().start();
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.button_commit:
			String text = mInputEdit.getText().toString().trim();
			if (!TextUtils.isEmpty(text)) {
				//不允许只输入空白字符，这样的内容是无意义的
			}
			break;
		}
	}
	
	@Override
	protected boolean checkIntent(Intent intent) {
		mSN = intent.getStringExtra(EXTRA_SN);
		if (TextUtils.isEmpty(mSN)) {
			DebugUtils.logE(TAG, "checkIntent failed, you must supply sn");
			return false;
		}
		return true;
	}
	
	public static void startActivity(Context context, String sn) {
		Intent intent = new Intent(context, IMConversationActivity.class);
		intent.putExtra(EXTRA_SN, sn);
		context.startActivity(intent);
	}
	
	
	private class CoversationToServerThread extends Thread {

		@Override
		public void run() {
			super.run();
			
		      //与服务端建立连接  
		      try {
				Socket client = new Socket(HOST, PORT);
				Log.d(TAG, "client.isConnected() " + client.isConnected());
				new CoversationToUserThread(client).start();
				BufferedInputStream is = new BufferedInputStream(System.in);
				BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
				byte[] buffer = new byte[1024];
				int read = is.read(buffer);
				while(read != -1) {
					out.write(buffer, 0, read);
					out.flush();
					read = is.read(buffer);
				}
		      out.close();  
		      client.close();  
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		      //建立连接后就可以往服务端写数据了  
		}
    	
    }
    
    private class CoversationToUserThread extends Thread {

    	private Socket _client;
    	public CoversationToUserThread(Socket client) {
    		_client = client;
    	}
		@Override
		public void run() {
			super.run();
		      try {
				BufferedInputStream is = new BufferedInputStream(_client.getInputStream());
				BufferedOutputStream out = new BufferedOutputStream(System.out);
				byte[] buffer = new byte[1024];
				int read = is.read(buffer);
				while(read != -1) {
					out.write(buffer, 0, read);
					out.flush();
					read = is.read(buffer);
				}
		      out.close();  
		      _client.close();  
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		      //建立连接后就可以往服务端写数据了  
		}
    	
    }

}
