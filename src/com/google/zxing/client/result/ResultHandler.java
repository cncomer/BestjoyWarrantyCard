/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.result;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;

import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.ui.CaptureActivity;
import com.bestjoy.app.warrantycard.ui.CaptureActivityHandler;
import com.bestjoy.app.warrantycard.ui.SettingsPreferenceActivity;
import com.google.zxing.Result;
import com.shwy.bestjoy.utils.LocaleManager;

/**
 * A base class for the Android-specific barcode handlers. These allow the app
 * to polymorphically suggest the appropriate actions for each data type.
 * 
 * This class also contains a bunch of utility methods to take common actions
 * like opening a URL. They could easily be moved into a helper object, but it
 * can't be static because the Activity instance is needed to launch an intent.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 * @author yeluosuifeng2005@gmail.com (�¿�)
 * @param <Resulthandler>
 */
public abstract class ResultHandler {
	private static final String TAG = "ResultHandler";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyyMMdd");
	private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyyMMdd'T'HHmmss");

	private static final String GOOGLE_SHOPPER_PACKAGE = "com.google.android.apps.shopper";
	private static final String GOOGLE_SHOPPER_ACTIVITY = GOOGLE_SHOPPER_PACKAGE +
	      ".results.SearchResultsActivity";
	private static final String MARKET_URI_PREFIX = "market://search?q=pname:";
	private static final String MARKET_REFERRER_SUFFIX =
	      "&referrer=utm_source%3Dbarcodescanner%26utm_medium%3Dapps%26utm_campaign%3Dscan";
	  
	public static final int MAX_BUTTON_COUNT = 5;

	private final ParsedResult result;
	protected final Activity activity;
	private final Result rawResult;
	private final String customProductSearch;

	
	private final DialogInterface.OnClickListener shopperMarketListener =
	      new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialogInterface, int which) {
	      launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI_PREFIX +
	          GOOGLE_SHOPPER_PACKAGE + MARKET_REFERRER_SUFFIX)));
	    }
	  };
	  
	ResultHandler(Activity activity, ParsedResult result) {
		this(activity, result, null);
	}
	
	ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
	    this.result = result;
	    this.activity = activity;
	    this.rawResult = rawResult;
	    this.customProductSearch = parseCustomSearchURL();

	    // Make sure the Shopper button is hidden by default. Without this, scanning a product followed
	    // by a QR Code would leave the button on screen among the QR Code actions.
	    View shopperButton = activity.findViewById(R.id.shopper_button);
	    if(shopperButton!=null)shopperButton.setVisibility(View.GONE);
	  }

	public ParsedResult getResult() {
		return result;
	}
	
	boolean hasCustomProductSearch() {
	    return customProductSearch != null;
	}

	/**
	 * Indicates how many buttons the derived class wants shown.
	 * 
	 * @return The integer button count.
	 */
	public abstract int getButtonCount();

	/**
	 * The text of the nth action button.
	 * 
	 * @param index
	 *            From 0 to getButtonCount() - 1
	 * @return The button text as a resource ID
	 */
	public abstract int getButtonText(int index);

	/**
	 * Execute the action which corresponds to the nth button.
	 * 
	 * @param index
	 *            The button that was clicked.
	 */
	public abstract void handleButtonPress(int index);
	

	  /**
	   * The Google Shopper button is special and is not handled by the abstract button methods above.
	   *
	   * @param listener The on click listener to install for this button.
	   */
	  protected void showGoogleShopperButton(View.OnClickListener listener) {
	    View shopperButton = activity.findViewById(R.id.shopper_button);
	    shopperButton.setVisibility(View.VISIBLE);
	    shopperButton.setOnClickListener(listener);
	  }

	/**
	 * Create a possibly styled string for the contents of the current barcode.
	 * 
	 * @return The text to be displayed.
	 */
	public CharSequence getDisplayContents() {
		String contents = result.getDisplayResult();
		return contents.replace("\r", "");
	}

	/**
	 * A string describing the kind of barcode that was found, e.g.
	 * "Found contact info".
	 * 
	 * @return The resource ID of the string.
	 */
	public abstract int getDisplayTitle();

	/**
	 * A convenience method to get the parsed type. Should not be overridden.
	 * 
	 * @return The parsed type, e.g. URI or ISBN
	 */
	public final ParsedResultType getType() {
		return result.getType();
	}
	
	/**
	 * Sends an intent to create a new calendar event by prepopulating the Add
	 * Event UI. Older versions of the system have a bug where the event title
	 * will not be filled out.
	 * 
	 * @param summary
	 *            A description of the event
	 * @param start
	 *            The start time as yyyyMMdd or yyyyMMdd'T'HHmmss or
	 *            yyyyMMdd'T'HHmmss'Z'
	 * @param end
	 *            The end time as yyyyMMdd or yyyyMMdd'T'HHmmss or
	 *            yyyyMMdd'T'HHmmss'Z'
	 */
	final void addCalendarEvent(String summary, String start, String end) {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("beginTime", calculateMilliseconds(start));
		if (start.length() == 8) {
			intent.putExtra("allDay", true);
		}
		intent.putExtra("endTime", calculateMilliseconds(end));
		intent.putExtra("title", summary);
		launchIntent(intent);
	}
	
	/**
	   * Sends an intent to create a new calendar event by prepopulating the Add Event UI. Older
	   * versions of the system have a bug where the event title will not be filled out.
	   *
	   * @param summary A description of the event
	   * @param start   The start time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
	   * @param end     The end time as yyyyMMdd or yyyyMMdd'T'HHmmss or yyyyMMdd'T'HHmmss'Z'
	   * @param location a text description of the event location
	   * @param description a text description of the event itself
	   */
	  final void addCalendarEvent(String summary,
	                              String start,
	                              String end,
	                              String location,
	                              String description) {
	    Intent intent = new Intent(Intent.ACTION_EDIT);
	    intent.setType("vnd.android.cursor.item/event");
	    intent.putExtra("beginTime", calculateMilliseconds(start));
	    if (start.length() == 8) {
	      intent.putExtra("allDay", true);
	    }
	    if (end == null) {
	      end = start;
	    }
	    intent.putExtra("endTime", calculateMilliseconds(end));
	    intent.putExtra("title", summary);
	    intent.putExtra("eventLocation", location);
	    intent.putExtra("description", description);
	    launchIntent(intent);
	  }

	 private static long calculateMilliseconds(String when) {
		if (when.length() == 8) {
			// Only contains year/month/day
			Date date;
			synchronized (DATE_FORMAT) {
				date = DATE_FORMAT.parse(when, new ParsePosition(0));
			}
			return date.getTime();
		} else {
			// The when string can be local time, or UTC if it ends with a Z
			Date date;
			synchronized (DATE_TIME_FORMAT) {
				date = DATE_TIME_FORMAT.parse(when.substring(0, 15),
						new ParsePosition(0));
			}
			long milliseconds = date.getTime();
			if (when.length() == 16 && when.charAt(15) == 'Z') {
				Calendar calendar = new GregorianCalendar();
				int offset = calendar.get(Calendar.ZONE_OFFSET)
						+ calendar.get(Calendar.DST_OFFSET);
				milliseconds += offset;
			}
			return milliseconds;
		}
	}
	 
	final void openMap(String geoURI) {
		    launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(geoURI)));
	}
	
	final void gotoBrower(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		launchIntent(intent);
	}

	final void shareByEmail(String contents) {
		sendEmailFromUri("mailto:", activity.getString(R.string.msg_share_subject_line), contents);
	}

	final void sendEmail(String address, String subject, String body) {
		sendEmailFromUri("mailto:" + address, subject, body);
	}

	// Use public Intent fields rather than private GMail app fields to specify
	// subject and body.
	final void sendEmailFromUri(String uri, String subject, String body) {
		Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(uri));
		putExtra(intent, Intent.EXTRA_SUBJECT, subject);
		putExtra(intent, Intent.EXTRA_TEXT, body);
		intent.setType("text/plain");
		launchIntent(intent);
	}

	// Use public Intent fields rather than private GMail app fields to specify subject and body.
	final void sendEmailFromUri(String uri, String email, String subject, String body) {
	    Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse(uri));
	    if (email != null) {
	      intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
	    }
	    putExtra(intent, Intent.EXTRA_SUBJECT, subject);
	    putExtra(intent, Intent.EXTRA_TEXT, body);
	    intent.setType("text/plain");
	    launchIntent(intent);
	}
	  
	final void shareBySMS(String contents) {
		sendSMSFromUri("smsto:", activity
				.getString(R.string.msg_share_subject_line)
				+ ":\n" + contents);
	}

	final void sendSMS(String phoneNumber, String body) {
		sendSMSFromUri("smsto:" + phoneNumber, body);
	}

	final void sendSMSFromUri(String uri, String body) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
		putExtra(intent, "sms_body", body);
		// Exit the app once the SMS is sent
		intent.putExtra("compose_mode", true);
		launchIntent(intent);
	}

	final void sendMMS(String phoneNumber, String subject, String body) {
		sendMMSFromUri("mmsto:" + phoneNumber, subject, body);
	}

	final void sendMMSFromUri(String uri, String subject, String body) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri));
		// The Messaging app needs to see a valid subject or else it will treat
		// this an an SMS.
		if (subject == null || subject.length() == 0) {
			putExtra(intent, "subject", activity
					.getString(R.string.msg_default_mms_subject));
		} else {
			putExtra(intent, "subject", subject);
		}
		putExtra(intent, "sms_body", body);
		intent.putExtra("compose_mode", true);
		launchIntent(intent);
	}

	final void dialPhone(String phoneNumber) {
		launchIntent(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
				+ phoneNumber)));
//		((CaptureActivity)activity).setDialReturnStatus(true);
	}

	final void dialPhoneFromUri(String uri) {
		launchIntent(new Intent(Intent.ACTION_DIAL, Uri.parse(uri)));
	}
	
	final void openURL(String url) {
	    launchIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	  }
	
	// Uses the mobile-specific version of Product Search, which is formatted for small screens.
	  final void openProductSearch(String upc) {
	    Uri uri = Uri.parse("http://www.google." + LocaleManager.getProductSearchCountryTLD() +
	        "/m/products?q=" + upc + "&source=zxing");
	    launchIntent(new Intent(Intent.ACTION_VIEW, uri));
	  }

	  final void openBookSearch(String isbn) {
	    Uri uri = Uri.parse("http://books.google." + LocaleManager.getBookSearchCountryTLD() +
	        "/books?vid=isbn" + isbn);
	    launchIntent(new Intent(Intent.ACTION_VIEW, uri));
	  }

	  final void webSearch(String query) {
		    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		    intent.putExtra("query", query);
		    launchIntent(intent);
	  }
	  
	/**
	 * Do a geo search using the address as the query.
	 * 
	 * @param address
	 *            The address to find
	 * @param title
	 *            An optional title, e.g. the name of the business at this
	 *            address
	 */

	final void getDirections(double latitude, double longitude) {
		launchIntent(new Intent(Intent.ACTION_VIEW, Uri
				.parse("http://maps.google." + LocaleManager.getCountryTLD()
						+ "/maps?f=d&daddr=" + latitude + ',' + longitude)));
	}

	final void openGoogleShopper(String query) {
	    try {
	      activity.getPackageManager().getPackageInfo(GOOGLE_SHOPPER_PACKAGE, 0);
	      // If we didn't throw, Shopper is installed, so launch it.
	      Intent intent = new Intent(Intent.ACTION_SEARCH);
	      intent.setClassName(GOOGLE_SHOPPER_PACKAGE, GOOGLE_SHOPPER_ACTIVITY);
	      intent.putExtra(SearchManager.QUERY, query);
	      activity.startActivity(intent);
	    } catch (PackageManager.NameNotFoundException e) {
	      // Otherwise offer to install it from Market.
	      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	      builder.setTitle(R.string.msg_google_shopper_missing);
	      builder.setMessage(R.string.msg_install_google_shopper);
	      builder.setIcon(R.drawable.shopper_icon);
	      builder.setPositiveButton(R.string.button_ok, shopperMarketListener);
	      builder.setNegativeButton(R.string.button_cancel, null);
	      builder.show();
	    }
	  }
	
	void launchIntent(Intent intent) {
		if (intent != null) {
			try {
				activity.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(R.string.app_name);
				builder.setMessage(R.string.msg_intent_failed);
				builder.setPositiveButton(R.string.button_ok, null);
				builder.show();
			}
		}
	}

	final void gobackAndScan() {// 返回扫描
		((CaptureActivity) activity).resetStatusView();// 重新设置状态view
		CaptureActivityHandler mHandler = (CaptureActivityHandler) ((CaptureActivity) activity)
				.getHandler();
		mHandler.sendEmptyMessage(R.id.restart_preview);// 开始预览
	}
	/**退出预览*/
	final void exitScan() {
		((CaptureActivity) activity).finish();
	}

	public static void putExtra(Intent intent, String key, String value) {
		if (value != null && value.length() > 0) {
			intent.putExtra(key, value);
		}
	}
	
	public static void putExtra(Intent intent, String key, int value) {
		if (value >=0) {
			intent.putExtra(key, value);
		}
	}

	protected void showNotOurResults(int index,
			AlertDialog.OnClickListener proceedListener) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(activity);
		if (prefs.getBoolean(SettingsPreferenceActivity.KEY_NOT_OUR_RESULTS_SHOWN,
				false)) {
			// already seen it, just proceed
			proceedListener.onClick(null, index);
		} else {
			// note the user has seen it
			prefs.edit().putBoolean(
					SettingsPreferenceActivity.KEY_NOT_OUR_RESULTS_SHOWN, true)
					.commit();
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(R.string.msg_not_our_results);
			builder.setPositiveButton(R.string.button_ok, proceedListener);
			builder.show();
		}
	}
	
	private String parseCustomSearchURL() {
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	    String customProductSearch = prefs.getString(SettingsPreferenceActivity.KEY_CUSTOM_PRODUCT_SEARCH, null);
	    if (customProductSearch != null && customProductSearch.trim().length() == 0) {
	      return null;
	    }
	    return customProductSearch;
	  }
	
	String fillInCustomSearchURL(String text) {
	    String url = customProductSearch.replace("%s", text);
	    if (rawResult != null) {
	      url = url.replace("%f", rawResult.getBarcodeFormat().toString());
	    }
	    return url;
	 }

	public final static ResultHandler getInstance(Activity param1) {
		return new ResultHandler(param1, null) {

			@Override
			public int getButtonCount() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getButtonText(int index) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getDisplayTitle() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void handleButtonPress(int index) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
