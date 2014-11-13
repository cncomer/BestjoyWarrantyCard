package com.bestjoy.app.bjwarrantycard.privacy;

import java.util.List;

import android.telephony.SmsMessage;

public interface IMonitorCallback {
	
	void register(List<IMonitorCallback> callbacks);
	void unregister(List<IMonitorCallback> callbacks);
	/**���ƫ�����ô����Ƿ�register��unregister*/
	void toggle(List<IMonitorCallback> callbacks);

	/**���յ�����*/
	void onSmsReceive(SmsMessage[] smsMessages);
	/**�绰״̬����ı�*/
//	void onCallStateChanged(int state, String number, boolean outgoing);
	
	void onPhoneRing(boolean outgoing, String number);
	void onPhoneIdle(boolean outgoing, String number);
	void onPhoneOffhook(boolean outgoing, String number);
	void onPhoneOutgoing(String number);
}
