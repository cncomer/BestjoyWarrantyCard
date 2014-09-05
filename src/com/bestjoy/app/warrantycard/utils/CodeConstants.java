package com.bestjoy.app.warrantycard.utils;

import java.util.Date;

import com.shwy.bestjoy.utils.DateUtils;

public class CodeConstants {
	
	public static final int NOT_FOUND = 404;
	public static final int NOT_SUPPORT = 401;
	public static final int TRAIN = 201;  //火车
	public static final int AIRPLANE = 202; //飞机
	public static final int PINPAI = 203;   //品牌
	
	
	/**
	 * http://train.qunar.com/list_num.htm?fromStation=G147&ex_track=bd_aladding_train_s2_checi
	 * @return
	 */
	public static String getTrainUrl(String ceshi) {
		StringBuilder sb = new StringBuilder("http://train.qunar.com/list_num.htm?fromStation=");
		sb.append(ceshi).append("&ex_track=bd_aladding_train_s2_checi");
		return sb.toString();
	}
	
	/**
	 * http://www.umetrip.com/mskyweb/fs/fc.do?flightNo=MU5114&date=2014-09-03&channel=
	 * @return
	 */
	public static String getAirPlaneUrl(String flightNo) {
		StringBuilder sb = new StringBuilder("http://www.umetrip.com/mskyweb/fs/fc.do?flightNo=");
		sb.append(flightNo).append("&date=").append(DateUtils.TOPIC_DATE_TIME_FORMAT.format(new Date())).append("&channel=");
		return sb.toString();
	}
	
	

}
