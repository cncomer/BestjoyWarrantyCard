package com.shwy.bestjoy.bjnote.wifi;

/*
 * Copyright (C) 2010 ZXing authors
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


import java.util.List;
import java.util.regex.Pattern;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.bestjoy.app.bjwarrantycard.MyApplication;
import com.bestjoy.app.bjwarrantycard.R;
import com.bestjoy.app.warrantycard.ui.BaseActionbarActivity;
import com.shwy.bestjoy.utils.Intents;

/**
 * A new activity showing the progress of Wifi connection
 * 
 * @author Vikram Aggarwal
 * @author chenkai
 */
public final class WifiActivity extends BaseActionbarActivity {

	private static final String TAG = WifiActivity.class.getSimpleName();

	private static final int MAX_ERROR_COUNT = 3;
	private static final int FAILURE_NO_NETWORK_ID = -1;
	private static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");

	private WifiManager wifiManager;
	private TextView statusView;
	private WifiReceiver wifiReceiver;
	private boolean receiverRegistered;
	private int networkId;
	private int errorCount;
	private IntentFilter mWifiStateFilter;
	private String mCurrentSSID;

	public String getCurrentSSID() {
		return mCurrentSSID;
	}
	
	void gotError() {
		errorCount++;
		Log.d(TAG, "Encountered another error.  Errorcount = " + errorCount);
		if (errorCount > MAX_ERROR_COUNT) {
			errorCount = 0;
			doError(R.string.wifi_connect_failed);
		}
	}

	private int doError(int resource_string) {
		statusView.setText(resource_string);
		// Give up on the connection
		if (wifiManager != null) {
			wifiManager.disconnect();
			if (networkId > 0) {
				wifiManager.removeNetwork(networkId);
				networkId = -1;
			}
		}

		if (receiverRegistered) {
			unregisterReceiver(wifiReceiver);
			receiverRegistered = false;
		}
		return -1;
	}

	// #################################################################################
	private WifiConfiguration changeNetworkCommon(String ssid) {
		updateStatusView(R.string.wifi_creating_network);
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		// Android API insists that an ascii SSID must be quoted to be correctly
		// handled.
		config.SSID = quoteNonHex(ssid);
		return config;
	}

	// Adding a WEP network
	private void changeNetworkWEP(WifiManager wifiManager, String ssid, String password) {
		WifiConfiguration config = changeNetworkCommon(ssid);
		config.wepKeys[0] = quoteNonHex(password, 10, 26, 58);
		config.wepTxKeyIndex = 0;
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		updateNetwork(wifiManager, config);
	}

	// Adding a WPA or WPA2 network
	private void changeNetworkWPA(WifiManager wifiManager, String ssid, String password) {
		WifiConfiguration config = changeNetworkCommon(ssid);
		// Hex passwords that are 64 bits long are not to be quoted.
		config.preSharedKey = quoteNonHex(password, 64);
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
		config.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		updateNetwork(wifiManager, config);
	}

	// Adding an open, unsecured network
	private void changeNetworkUnEncrypted(WifiManager wifiManager, String ssid) {
		WifiConfiguration config = changeNetworkCommon(ssid);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		updateNetwork(wifiManager, config);
	}

	/**
	 * Update the network: either create a new network or modify an existing
	 * network
	 * 
	 * @param config
	 *            the new network configuration
	 * @return network ID of the connected network.
	 */
	private void updateNetwork(WifiManager wifiManager, WifiConfiguration config) {
		Integer foundNetworkID = findNetworkInExistingConfig(wifiManager,config.SSID);
		if (foundNetworkID != null) {
			Log.i(TAG, "Removing old configuration for network " + config.SSID);
			updateStatusView(R.string.wifi_modifying_network);
			wifiManager.removeNetwork(foundNetworkID);
			wifiManager.saveConfiguration();
		} else {
			updateStatusView(R.string.wifi_creating_network);
		}
		int networkId = wifiManager.addNetwork(config);
		if (networkId >= 0) {
			// Try to disable the current network and start a new one.
			mCurrentSSID = config.SSID;
			if (wifiManager.enableNetwork(networkId, true)) {
				Log.i(TAG, "Associating to network " + config.SSID);
//				wifiManager.saveConfiguration();
			} else {
				Log.w(TAG, "Failed to enable network " + config.SSID);
			}
		} else {
			mCurrentSSID = null;
			Log.w(TAG, "Unable to add network " + config.SSID);
		}
	}

	private Integer findNetworkInExistingConfig(WifiManager wifiManager, String ssid) {
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals(ssid)) {
				return existingConfig.networkId;
			}
		}
		return null;
	}

	private static String quoteNonHex(String value, int... allowedLengths) {
		return isHexOfLength(value, allowedLengths) ? value
				: convertToQuotedString(value);
	}

	/**
	 * @param value
	 *            input to check
	 * @param allowedLengths
	 *            allowed lengths, if any
	 * @return true if value is a non-null, non-empty string of hex digits, and
	 *         if allowed lengths are given, has an allowed length
	 */
	private static boolean isHexOfLength(CharSequence value, int... allowedLengths) {
		if (value == null || !HEX_DIGITS.matcher(value).matches()) {
			return false;
		}
		if (allowedLengths.length == 0) {
			return true;
		}
		for (int length : allowedLengths) {
			if (value.length() == length) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Encloses the incoming string inside double quotes, if it isn't already
	 * quoted.
	 * 
	 * @param string
	 *            : the input string
	 * @return a quoted string, of the form "input". If the input string is
	 *         null, it returns null as well.
	 */
	static String convertToQuotedString(String string) {
		if (string == null) {
			return null;
		}
		if (TextUtils.isEmpty(string)) {
			return "";
		}
		int lastPos = string.length() - 1;
		if (lastPos < 0
				|| (string.charAt(0) == '"' && string.charAt(lastPos) == '"')) {
			return string;
		}
		return '\"' + string + '\"';
	}

	// #################################################################################

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent == null
				|| (!intent.getAction().equals(Intents.WifiConnect.ACTION))) {
			finish();
			return;
		}

		setContentView(R.layout.network);
		statusView = (TextView) findViewById(R.id.networkStatus);

		final String ssid = intent.getStringExtra(Intents.WifiConnect.SSID);
		final String password = intent.getStringExtra(Intents.WifiConnect.PASSWORD);
		final String networkTypeString = intent.getStringExtra(Intents.WifiConnect.TYPE);

		// This is not available before onCreate
		wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);

		Runnable configureRunnable = new Runnable() {
			@Override
			public void run() {
				// Start WiFi, otherwise nothing will work
				if (!wifiManager.isWifiEnabled()) {
					Log.i(TAG, "Enabling wi-fi...");
					if (wifiManager.setWifiEnabled(true)) {
						Log.i(TAG, "Wi-fi enabled");
					} else {
						Log.w(TAG, "Wi-fi could not be enabled!");
						return;
					}
					// This happens very quickly, but need to wait for it to
					// enable. A little busy wait?
					int count = 0;
					while (!wifiManager.isWifiEnabled()) {
						if (count >= 10) {
							Log.i(TAG,
									"Took too long to enable wi-fi, quitting");
							return;
						}
						Log.i(TAG, "Still waiting for wi-fi to enable...");
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException ie) {
							// continue
						}
						count++;
					}
				}
				NetworkType networkType = NetworkType.forIntentValue(networkTypeString);
				if (networkType == NetworkType.NO_PASSWORD) {
					changeNetworkUnEncrypted(wifiManager, ssid);
				} else {
					if (password == null || password.length() == 0) {
						throw new IllegalArgumentException();
					}
					if (networkType == NetworkType.WEP) {
						changeNetworkWEP(wifiManager, ssid, password);
					} else if (networkType == NetworkType.WPA) {
						changeNetworkWPA(wifiManager, ssid, password);
					}
				}
			}
		};
		new Thread(configureRunnable).start();

		// So we know when the network changes
		wifiReceiver = new WifiReceiver(wifiManager, this, statusView, ssid);

		// The order matters!
		mWifiStateFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mWifiStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mWifiStateFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(wifiReceiver, mWifiStateFilter);
		receiverRegistered = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (receiverRegistered) {
			unregisterReceiver(wifiReceiver);
			receiverRegistered = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (wifiReceiver != null && mWifiStateFilter != null
				&& !receiverRegistered) {
			registerReceiver(wifiReceiver, mWifiStateFilter);
			receiverRegistered = true;
		}
	}

	@Override
	protected void onDestroy() {
		if (wifiReceiver != null) {
			if (receiverRegistered) {
				unregisterReceiver(wifiReceiver);
				receiverRegistered = false;
			}
			wifiReceiver = null;
		}
		super.onDestroy();
	}
	
	private void updateStatusView(final int res) {
		MyApplication.getInstance().postAsync(new Runnable() {

			@Override
			public void run() {
				statusView.setText(res);
			}
			
		});
	}

	@Override
	protected boolean checkIntent(Intent intent) {
		return true;
	}

}