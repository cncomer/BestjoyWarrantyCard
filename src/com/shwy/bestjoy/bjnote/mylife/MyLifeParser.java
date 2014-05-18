package com.shwy.bestjoy.bjnote.mylife;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.TextUtils;

import com.shwy.bestjoy.utils.DebugUtils;
import com.shwy.bestjoy.utils.InfoInterfaceImpl;
import com.shwy.bestjoy.utils.PageInfo;
/**展会通会通卡解析类*/
public class MyLifeParser extends InfoInterfaceImpl{
	private static final String TAG = "MyLifeParser";
	public MyLifeParser(){}

	/**
	 * Format 
	 * <ShenHuoQuanVcf>
          <eachvcf>
			<MM>02166302502123456</MM>
			<ComName>上海吾易信息技术有限公司</ComName>
			<Phone>021-66302502</Phone>
			<WorkAdr>上海闸北区江场三路238号</WorkAdr>
			<Website>http://www.dianping.com/shop/29901666742</Website>
			<Note/>
			<xiaoFeiJiLu/>
			<records>
				<totaljifen>500</totaljifen>
				<zongjine>600</zongjine>
				<details>
					<record>
						<XFtime>2013/8/3 7:26:46</XFtime>
						<XFMoney>600</XFMoney>
					</record>
					<record>
						<XFtime>2013/8/3 7:26:53</XFtime>
						<XFMoney>-100</XFMoney>
					</record>
				</details>
			</records>
	      </eachvcf>
	    </ShenHuoQuanVcf>
	 * @return
	 */
	public static List<MyLifeObject> parseList(InputStream is, PageInfo mPageInfo ) {
		 DebugUtils.logExchangeBCParse(TAG, "Start parse");
		 List<MyLifeObject> myLifeObjectList = new LinkedList<MyLifeObject>();
		 MyLifeConsumeRecordsObject mMyLifeConsumeRecordsObject = null;
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
	        XmlPullParser parser = factory.newPullParser();
	        parser.setInput(is, "utf-8");
	        MyLifeObject myLifeObject = null;
	        beginDocument(parser, "info");
	        int type = 0;
	        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT) {
	        	if (type == XmlPullParser.START_TAG) {
	        		String tag = parser.getName();
	                DebugUtils.logExchangeBCParse(TAG, "Start tag "+tag);
	 	          
	 	       	    if ("eachvcf".equals(tag)) {
	 	       	         DebugUtils.logExchangeBCParse(TAG, "new MyLifeObject");
	 	       	         myLifeObject = new MyLifeObject();
	 	       	         if (mPageInfo != null) {
	 	       	        	 myLifeObject.mTel = mPageInfo.mTag;
	 	       	         }
		       	    } else if ("MM".equals(tag)) {
		       	    	myLifeObject.mComMm = nextTextElement(parser);
	       		    } else if ("ComName".equals(tag)) {
	       		    	myLifeObject.mComName = nextTextElement(parser);
 	       		    } else if ("Phone".equals(tag)) {
		       	    	myLifeObject.mComCell = nextTextElement(parser);
	       		    } else if ("WorkAdr".equals(tag)) {
	       		    	myLifeObject.mAddress = nextTextElement(parser);
	       		    	myLifeObject.mAddress = myLifeObject.mAddress.replace("\r\n", "\n");
 	       		    } else if ("Website".equals(tag)) {
		       	    	myLifeObject.mWebsite = nextTextElement(parser);
	       		    } else if ("Note".equals(tag)) {
	       		    	myLifeObject.mNewsNote = nextTextElement(parser);
 	       		    } else if ("GuangGao".equals(tag)) {
	       		    	myLifeObject.mLiveInfo = nextTextElement(parser);
 	       		    } else if ("xiaoFeiJiLu".equals(tag)) {
 	       		       myLifeObject.mXFNotes = nextTextElement(parser);
 	       		    } else if ("totaljifen".equals(tag)) {
 	       		    	//消费记录
  	       		       myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotaljifen = nextTextElement(parser);
  	       		    } else if ("zongjine".equals(tag)) {
 	       		    	//消费记录
   	       		       myLifeObject.mMyLifeConsumeRecordsObjectHolder.mTotalMoney = nextTextElement(parser);
   	       		    } else if ("record".equals(tag)) {
   	       		    	mMyLifeConsumeRecordsObject = new MyLifeConsumeRecordsObject();
   	       		    } else if ("XFtime".equals(tag)) {
   	       		    	mMyLifeConsumeRecordsObject.mXFtime  = nextTextElement(parser);
//   	       		    	 if (!TextUtils.isEmpty(xfTime)) {
//   	       		    	     try {
//								Date date = MyLifeObject.XF_DATE_TIME_FORMAT.parse(xfTime);
//								 mMyLifeConsumeRecordsObject.mXFtime = xfTime;
//							} catch (ParseException e) {
//								e.printStackTrace();
//							}
//   	       		    		
//   	       		    	 }
  	       		    } else if ("XFMoney".equals(tag)) {
  	       		    	mMyLifeConsumeRecordsObject.XFMoney = nextTextElement(parser);
   	       		    }
	        	} else if (type == XmlPullParser.END_TAG) {
	        		String tag = parser.getName();
	        		if ("record".equals(tag)) {
	        			if (mMyLifeConsumeRecordsObject != null && !TextUtils.isEmpty(mMyLifeConsumeRecordsObject.mXFtime)) {
	        				myLifeObject.mMyLifeConsumeRecordsObjectHolder.mMyLifeConsumeRecordsObjectList.add(mMyLifeConsumeRecordsObject);
		        			DebugUtils.logExchangeBCParse(TAG, "add MyLifeConsumeRecordsObject " + mMyLifeConsumeRecordsObject.toString());
	        			}
	        			mMyLifeConsumeRecordsObject = null;
		       	    } else if ("eachvcf".equals(tag)) {
	        			DebugUtils.logExchangeBCParse(TAG, "add MyLifeObject");
	        			if (myLifeObject != null && !TextUtils.isEmpty(myLifeObject.mComMm)) {
	        				myLifeObjectList.add(myLifeObject);
	        				myLifeObject = null;
		       	    	}
		       	    	
		       	    }
	        	}
	        }
	        is.close();
	        DebugUtils.logExchangeBCParse(TAG, "End document");
	        DebugUtils.logExchangeBCParse(TAG, "get MyLifeObjectList size is " + myLifeObjectList.size());
	        return myLifeObjectList;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return myLifeObjectList;
	}
}
