package com.shwy.bestjoy.bjnote.mylife;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.R;

public class MyLifeConsumeRecordsListAdapter extends BaseAdapter {
	private List<MyLifeConsumeRecordsObject> mListData;
	private String mFormatStr;
	private int mCount = 0;
	public MyLifeConsumeRecordsListAdapter(List<MyLifeConsumeRecordsObject> data) {
		changeData(data);
	}
	
	public void changeData(List<MyLifeConsumeRecordsObject> newData) {
		mListData = newData;
		if (mListData != null) {
			mCount = mListData.size();
		} else {
			mCount = 0;
		}
	}
	
	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public MyLifeConsumeRecordsObject getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mFormatStr == null) {
			mFormatStr = parent.getContext().getString(R.string.format_consume_record);
		}
		InfoAdapterViewHolder infoAdapterViewHolder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.mylife_consume_records_list_item, parent, false);
			infoAdapterViewHolder = new InfoAdapterViewHolder();
			infoAdapterViewHolder.mNumber = (TextView) convertView.findViewById(R.id.number);
			infoAdapterViewHolder.mTime = (TextView) convertView.findViewById(R.id.record_time);
			infoAdapterViewHolder.mJF = (TextView) convertView.findViewById(R.id.record_jf);
			convertView.setTag(infoAdapterViewHolder);
		} else {
			infoAdapterViewHolder = (InfoAdapterViewHolder) convertView.getTag();
		}
		infoAdapterViewHolder.mNumber.setText(String.valueOf(position+1));
		MyLifeConsumeRecordsObject myLifeConsumeRecordsObject = getItem(position);
		infoAdapterViewHolder.mJF.setText(String.format(mFormatStr, myLifeConsumeRecordsObject.XFMoney));
		infoAdapterViewHolder.mTime.setText(myLifeConsumeRecordsObject.mXFtime);
		return convertView;
	}
	
	public static class InfoAdapterViewHolder {
		private TextView mNumber, mTime, mJF;
	}
}