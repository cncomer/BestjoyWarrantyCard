package com.bestjoy.app.bjwarrantycard.bx.order;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.warrantycard.account.IBaoxiuCardObject;
import com.bestjoy.app.warrantycard.account.MyAccountManager;
import com.bestjoy.app.warrantycard.database.BjnoteContent;
import com.bestjoy.app.warrantycard.database.HaierDBHelper;
import com.shwy.bestjoy.utils.DebugUtils;
/**
 * 保修卡报修记录
 * @author bestjoy
 *
 */
public class BaoxiuCardOrderObject extends IBaoxiuCardObject{
	public static final String TAG = "BaoxiuCardOrderObject";
	public static final String[] PROJECTION = new String[]{
		 HaierDBHelper.ID,
		 HaierDBHelper.ACCOUNT_UID,
		 HaierDBHelper.BX_ORDER_BID,
		 HaierDBHelper.BX_ORDER_SID,
		 HaierDBHelper.BX_ORDER_APPLY_PRODUCT_TYPE,
		 HaierDBHelper.BX_ORDER_APPLY_PRODUCT_XINGHAO,
		 HaierDBHelper.BX_ORDER_APPLY_SERVER_TYPE,
		 HaierDBHelper.BX_ORDER_APPLY_STATUS,
		 HaierDBHelper.BX_ORDER_APPLY_TIME,
		 HaierDBHelper.BX_ORDER_STIME,
		 HaierDBHelper.BX_ORDER_EVALUATED,
		 HaierDBHelper.BX_ORDER_APPLY_PRODUCT_PINPAI,
	};
	
	public static final int INDEX_ID = 0;
	public static final int INDEX_UID = 1;
	public static final int INDEX_BID = 2;
	public static final int INDEX_SID = 3;
	public static final int INDEX_PRODUCT_TYPE = 4;
	public static final int INDEX_PRODUCT_XINGHAO = 5;
	public static final int INDEX_SERVER_TYPE = 6;
	public static final int INDEX_STATUS = 7;
	public static final int INDEX_APPLY_TIME = 8;
	public static final int INDEX_ORDER_STIME = 9;
	public static final int INDEX_ORDER_EVALUATED = 10;
	public static final int INDEX_PRODUCT_PINPAI = 11;
	
	public static final String SORT_ORDER_BY_STIME = HaierDBHelper.BX_ORDER_STIME + " desc";
	public static final String SELECTION_UID = HaierDBHelper.ACCOUNT_UID + "=?";
	public static final String SELECTION_UID_SID = SELECTION_UID + " and " + HaierDBHelper.BX_ORDER_SID + "=?";
	public static final String SELECTION_STATUS = HaierDBHelper.BX_ORDER_APPLY_STATUS + "=?";
	public static final int ORDER_TYPE_ALL = 0; //全部
	public static final int ORDER_TYPE_APPLING = 1; //正在派单
	public static final int ORDER_TYPE_ASSIGNING = 2; //已派单
	public static final int ORDER_TYPE_FINISH = 3; //已完工
	public static final int ORDER_TYPE_EVALUATING = 4; //已评价
	public static final int ORDER_TYPE_COMPLAIN = 5; //投诉维权
	
	public long mSID = -1;
	public String mServerType="";
	public int mStatus = ORDER_TYPE_APPLING;
	/**服务器记录最后修改时间*/
	public long mServerModifyTime = -1;
	/**申请时间*/
	public long mApplyTime = -1;
	
	public int mEvaluated = 0;
	
	public static Cursor getLocalAllOrders(String selection, String[] selectionArgs) {
		return MyApplication.getInstance().getContentResolver().query(BjnoteContent.MyBXOrder.CONTENT_URI, PROJECTION, selection, selectionArgs, SORT_ORDER_BY_STIME);
	}
	
	public static BaoxiuCardOrderObject getBaoxiuCardOrderObjectFromCursor(Cursor c) {
		BaoxiuCardOrderObject baoxiuCardOrderObject = new BaoxiuCardOrderObject();
		baoxiuCardOrderObject.mUID = c.getLong(INDEX_UID);
		baoxiuCardOrderObject.mId = c.getLong(INDEX_ID);
		baoxiuCardOrderObject.mBID = c.getLong(INDEX_BID);
		baoxiuCardOrderObject.mSID = c.getLong(INDEX_SID);
		baoxiuCardOrderObject.mStatus = c.getInt(INDEX_STATUS);
		baoxiuCardOrderObject.mXingHao = c.getString(INDEX_PRODUCT_XINGHAO);
		baoxiuCardOrderObject.mLeiXin = c.getString(INDEX_PRODUCT_TYPE);
		baoxiuCardOrderObject.mServerType = c.getString(INDEX_SERVER_TYPE);
		baoxiuCardOrderObject.mApplyTime = c.getLong(INDEX_APPLY_TIME);
		baoxiuCardOrderObject.mServerModifyTime = c.getLong(INDEX_ORDER_STIME);
		baoxiuCardOrderObject.mEvaluated = c.getInt(INDEX_ORDER_EVALUATED);
		baoxiuCardOrderObject.mPinPai = c.getString(INDEX_PRODUCT_PINPAI);
		return baoxiuCardOrderObject;
	}
	
	public static BaoxiuCardOrderObject parse(JSONObject object) throws JSONException {
		BaoxiuCardOrderObject baoxiuCardOrderObject = new BaoxiuCardOrderObject();
		baoxiuCardOrderObject.mUID = object.getLong("UID");
		baoxiuCardOrderObject.mBID = object.getLong("BID");
		baoxiuCardOrderObject.mSID = object.getLong("sid");
		baoxiuCardOrderObject.mXingHao = object.getString("XingHao");
		baoxiuCardOrderObject.mLeiXin = object.getString("LeiXin");
		baoxiuCardOrderObject.mServerType = object.getString("serviceType");
		baoxiuCardOrderObject.mStatus = object.getInt("applyStatus");
		baoxiuCardOrderObject.mApplyTime = object.getLong("applyTime");
		baoxiuCardOrderObject.mServerModifyTime = object.getLong("updateTime");
		baoxiuCardOrderObject.mPinPai = object.getString("PinPai");
		baoxiuCardOrderObject.mEvaluated = object.getBoolean("hasPingJia")?1:0;
		return baoxiuCardOrderObject;
	}
	
	public static List<BaoxiuCardOrderObject> parse(JSONArray array) {
		List<BaoxiuCardOrderObject> list = new ArrayList<BaoxiuCardOrderObject>();
		int len = array.length();
		for(int index = 0; index < len; index++) {
			try {
				list.add(parse(array.getJSONObject(index)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public boolean saveInDatebase(ContentResolver cr, ContentValues addtion) {
		ContentValues values = new ContentValues();
		if (addtion != null) {
			values.putAll(addtion);
		}
		
		values.put(PROJECTION[INDEX_UID], mUID);
		values.put(PROJECTION[INDEX_BID], mBID);
		values.put(PROJECTION[INDEX_SID], mSID);
		values.put(PROJECTION[INDEX_PRODUCT_PINPAI], mPinPai);
		values.put(PROJECTION[INDEX_PRODUCT_TYPE], mLeiXin);
		values.put(PROJECTION[INDEX_PRODUCT_XINGHAO], mXingHao);
		values.put(PROJECTION[INDEX_SERVER_TYPE], mServerType);
		values.put(PROJECTION[INDEX_STATUS], mStatus);
		values.put(PROJECTION[INDEX_APPLY_TIME], mApplyTime);
		values.put(PROJECTION[INDEX_ORDER_STIME], mServerModifyTime);
		values.put(PROJECTION[INDEX_ORDER_EVALUATED], mEvaluated);
		
		
		long id = BjnoteContent.existed(cr, BjnoteContent.MyBXOrder.CONTENT_URI, SELECTION_UID_SID, new String[]{String.valueOf(mUID), String.valueOf(mSID)});
		if (id > 0) {
			int updated = BjnoteContent.update(cr, BjnoteContent.MyBXOrder.CONTENT_URI, values, BjnoteContent.ID_SELECTION, new String[]{String.valueOf(id)});
			DebugUtils.logD(TAG, "saveInDatebase update #effect rows " + updated);
			return updated > 0;
		} else {
			Uri uri = BjnoteContent.insert(cr, BjnoteContent.MyBXOrder.CONTENT_URI, values);
			DebugUtils.logD(TAG, "saveInDatebase insert uri " + uri);
			return uri != null;
		}
	}
	
	public static int deleteCachedAllOrdersForUid(long uid) {
		return MyApplication.getInstance().getContentResolver().delete(BjnoteContent.MyBXOrder.CONTENT_URI, SELECTION_UID, new String[]{String.valueOf(uid)});
	}
	
	
}
