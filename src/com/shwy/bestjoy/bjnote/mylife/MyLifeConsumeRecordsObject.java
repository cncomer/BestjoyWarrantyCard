package com.shwy.bestjoy.bjnote.mylife;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.shwy.bestjoy.utils.DebugUtils;

public class MyLifeConsumeRecordsObject {
	public static final String TAG = "MyLifeConsumeRecordsObject";

	public static final String mAsc1 = "XXXXXX";
	public static final String mAsc2 = "______";
	public static final String mAsc3 = "++++++++++";
	public String mXFtime;
	public String XFMoney;
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder(TAG);
		sb.append("[mXFtime=").append(mXFtime)
		.append(" XFMoney=").append(XFMoney).append("]");
		return sb.toString();
	}
	/**
	 * ��װ��[0x3]mXFtime[0x1]XFMoney[0x2]mXFtime[0x1]XFMoney�Ĵ�
	 * @param head ����Ǵ�ͷ�����ͷ���
	 * @return
	 */
	public String toSaveString(boolean head) {
		StringBuilder sb = new StringBuilder();
		if (head) {
			sb.append(mAsc3);
		}
		sb.append(mXFtime).append(mAsc1).append(XFMoney).append(mAsc2);
		DebugUtils.logD(TAG, "toSaveString " + sb.toString());
		return sb.toString();
	}
	
	/**
	 * ����[0x3]mXFtime[0x1]XFMoney[0x2]mXFtime[0x1]XFMoney�Ĵ����ֽ�õ����Ѽ�¼�������ʱ������ѽ��
	 * @param savedString
	 * @return ���û��ƥ�䣬�򷵻�null
	 */
	public static List<MyLifeConsumeRecordsObject> parse(String savedString) {
		DebugUtils.logD(TAG, "enter parse " + savedString);
		if (TextUtils.isEmpty(savedString)) {
			return null;
		}
		if (savedString.startsWith(mAsc3)) {
			savedString = savedString.substring(mAsc3.length()); 
			String[] pairs = savedString.split(mAsc2);
			if (pairs != null) {
				int len = pairs.length;
				if (len > 0) {
					List<MyLifeConsumeRecordsObject> list = new ArrayList<MyLifeConsumeRecordsObject>(len);
					String[] keyAndValue = null;
					for(String pair : pairs) {
						keyAndValue = pair.split(mAsc1);
						if (keyAndValue != null && keyAndValue.length > 1) {
							MyLifeConsumeRecordsObject object = new MyLifeConsumeRecordsObject();
							object.mXFtime = keyAndValue[0];
							object.XFMoney = keyAndValue[1];
							DebugUtils.logD(TAG, "add a MyLifeConsumeRecordsObject " + object.toString());
							list.add(object);
						}
					}
					return list;
				}
			}
		}
		return null;
	}
}
