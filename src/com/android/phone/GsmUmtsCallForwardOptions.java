// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
/**
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
* Changes from Qualcomm Technologies, Inc. are provided under the following license:
* Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
* SPDX-License-Identifier: BSD-3-Clause-Clear
*/

// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
package com.android.phone;

import android.app.ActionBar;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.content.ContentProvider;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.content.Context;
import android.content.DialogInterface;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.content.Intent;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.content.IntentFilter;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.database.Cursor;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.net.ConnectivityManager;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
import android.net.Network;
import android.net.NetworkCapabilities;
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
import android.os.Build;
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
import android.os.Bundle;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.os.PersistableBundle;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.os.Process;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.os.SystemProperties;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
// QTI_BEGIN: 2020-04-28: Telephony: Redirect to AOSP ACTION_NETWORK_OPERATOR_SETTINGS
import android.provider.Settings;
// QTI_END: 2020-04-28: Telephony: Redirect to AOSP ACTION_NETWORK_OPERATOR_SETTINGS
import android.telephony.CarrierConfigManager;
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
import android.telephony.ims.ImsException;
import android.telephony.ims.ImsMmTelManager;
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
// QTI_BEGIN: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
import android.telephony.ims.feature.ImsFeature;
// QTI_END: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
import android.telephony.ims.feature.MmTelFeature;
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.telephony.SubscriptionManager;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
import android.telephony.TelephonyManager;
// QTI_END: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
// QTI_BEGIN: 2024-11-14: Telephony: Fix APN type checking issue
import android.text.TextUtils;
// QTI_END: 2024-11-14: Telephony: Fix APN type checking issue
import android.util.Log;
import android.view.MenuItem;
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
import android.widget.Toast;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;

// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
import org.codeaurora.ims.QtiCallConstants;

// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
import java.util.ArrayList;

// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
public class GsmUmtsCallForwardOptions extends TimeConsumingPreferenceActivity
    implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    private static final String LOG_TAG = "GsmUmtsCallForwardOptions";

    private static final String NUM_PROJECTION[] = {
        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private static final String BUTTON_CFU_KEY   = "button_cfu_key";
    private static final String BUTTON_CFB_KEY   = "button_cfb_key";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key";
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
    private static final String BUTTON_CFNL_KEY  = "button_cfnl_key";
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

    private static final String KEY_TOGGLE = "toggle";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_ENABLE = "enable";
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
    private static final String KEY_START_HOUR = "start_hour";
    private static final String KEY_END_HOUR = "end_hour";
    private static final String KEY_START_MINUTE = "start_minute";
    private static final String KEY_END_MINUTE = "end_minute";
    private static final String KEY_IS_CFUT = "is_cfut";
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT

    private CallForwardEditPreference mButtonCFU;
    private CallForwardEditPreference mButtonCFB;
    private CallForwardEditPreference mButtonCFNRy;
    private CallForwardEditPreference mButtonCFNRc;
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
    private CallForwardEditPreference mButtonCFNL;

    private boolean mSupportCFNL = true;
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

    private final ArrayList<CallForwardEditPreference> mPreferences =
            new ArrayList<CallForwardEditPreference> ();
    private int mInitIndex= 0;

    private boolean mFirstResume;
    private Bundle mIcicle;
    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;
    private boolean mReplaceInvalidCFNumbers;
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
    private int mServiceClass;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    private BroadcastReceiver mReceiver = null;
    private boolean mCheckData = false;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
    private boolean mIsUtAllowedWhenWifiOn = false;
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    AlertDialog.Builder builder = null;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
    private CarrierConfigManager mCarrierConfig;
// QTI_END: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
    private boolean mCallForwardByUssd;

    private boolean mIsVideoCfEnabled = false;

// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
    private ImsMmTelManager.CapabilityCallback mCapabilityCallback =
        new ImsMmTelManager.CapabilityCallback() {
            @Override
            public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities capabilities) {
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                    mIsVideoCfEnabled = capabilities.isCapable(
                            MmTelFeature.MmTelCapabilities.CAPABILITY_TYPE_UT) &&
                            capabilities.isCapable(
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                            MmTelFeature.MmTelCapabilities.CAPABILITY_TYPE_VIDEO);
                    if (mServiceClass == CommandsInterface.SERVICE_CLASS_DATA_SYNC
                            + CommandsInterface.SERVICE_CLASS_PACKET) {
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                        Log.d(LOG_TAG,"update CF according to UtAvailability=" + mIsVideoCfEnabled);
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                        QtiCallForwardUtils.updateAllCfRows(mPreferences,
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                                mIsVideoCfEnabled);
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
                    }
            }
    };

// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        getWindow().addSystemFlags(
                android.view.WindowManager.LayoutParams
                        .SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);

        addPreferencesFromResource(R.xml.callforward_options);

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.call_forwarding_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();

// QTI_BEGIN: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
        mCarrierConfig = (CarrierConfigManager)
// QTI_END: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
                getSystemService(CARRIER_CONFIG_SERVICE);

        if (mCarrierConfig != null) {
// QTI_BEGIN: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
            PersistableBundle pb = mCarrierConfig.getConfigForSubId(mPhone.getSubId());
// QTI_END: 2018-06-11: Telephony: IMS: No prompt to notify user to switch off enhanced 4G
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
            mCheckData = pb.getBoolean("check_mobile_data_for_cf");
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
            mIsUtAllowedWhenWifiOn = pb.getBoolean("allow_ut_when_wifi_on_bool");
            Log.d(LOG_TAG, "mCheckData = " + mCheckData + ", mIsUtAllowedWhenWifiOn = " +
                    mIsUtAllowedWhenWifiOn);
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        }
        PersistableBundle b = null;
        boolean supportCFB = true;
        boolean supportCFNRc = true;
        boolean supportCFNRy = true;
        if (mSubscriptionInfoHelper.hasSubId()) {
            b = PhoneGlobals.getInstance().getCarrierConfigForSubId(
                    mSubscriptionInfoHelper.getSubId());
        } else {
            b = PhoneGlobals.getInstance().getCarrierConfig();
        }
        if (b != null) {
            mReplaceInvalidCFNumbers = b.getBoolean(
                    CarrierConfigManager.KEY_CALL_FORWARDING_MAP_NON_NUMBER_TO_VOICEMAIL_BOOL);
            mCallForwardByUssd = b.getBoolean(
                    CarrierConfigManager.KEY_USE_CALL_FORWARDING_USSD_BOOL);
            supportCFB = b.getBoolean(
                    CarrierConfigManager.KEY_CALL_FORWARDING_WHEN_BUSY_SUPPORTED_BOOL);
            supportCFNRc = b.getBoolean(
                    CarrierConfigManager.KEY_CALL_FORWARDING_WHEN_UNREACHABLE_SUPPORTED_BOOL);
            supportCFNRy = b.getBoolean(
                    CarrierConfigManager.KEY_CALL_FORWARDING_WHEN_UNANSWERED_SUPPORTED_BOOL);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
            mSupportCFNL = b.getBoolean(
                    CarrierConfigManager.KEY_CALL_FORWARDING_WHEN_NOT_LOGGED_IN_SUPPORTED_BOOL);
        }
        // Disable mSupportCFNL if IMS UT is not registered or build version is older than S.
        if (!mPhone.isUtEnabled() ||
                SystemProperties.getInt("ro.board.api_level", 0) < Build.VERSION_CODES.S) {
            mSupportCFNL = false;
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
        }

        PreferenceScreen prefSet = getPreferenceScreen();
        mButtonCFU = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFU_KEY);
        mButtonCFB = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFB_KEY);
        mButtonCFNRy = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRY_KEY);
        mButtonCFNRc = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRC_KEY);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
        mButtonCFNL  = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNL_KEY);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

        mButtonCFU.setParentActivity(this, mButtonCFU.reason);
        mButtonCFB.setParentActivity(this, mButtonCFB.reason);
        mButtonCFNRy.setParentActivity(this, mButtonCFNRy.reason);
        mButtonCFNRc.setParentActivity(this, mButtonCFNRc.reason);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
        mButtonCFNL.setParentActivity(this, mButtonCFNL.reason);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

        mPreferences.add(mButtonCFU);
        layoutCallForwardItem(supportCFB, mButtonCFB, prefSet);
        layoutCallForwardItem(supportCFNRy, mButtonCFNRy, prefSet);
        layoutCallForwardItem(supportCFNRc, mButtonCFNRc, prefSet);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
        layoutCallForwardItem(mSupportCFNL, mButtonCFNL, prefSet);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

        if (mCallForwardByUssd) {
            //the call forwarding ussd command's behavior is similar to the call forwarding when
            //unanswered,so only display the call forwarding when unanswered item.
            prefSet.removePreference(mButtonCFU);
            prefSet.removePreference(mButtonCFB);
            prefSet.removePreference(mButtonCFNRc);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
            prefSet.removePreference(mButtonCFNL);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
            mPreferences.remove(mButtonCFU);
            mPreferences.remove(mButtonCFB);
            mPreferences.remove(mButtonCFNRc);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
            mPreferences.remove(mButtonCFNL);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
            mButtonCFNRy.setDependency(null);
        }

        // we wait to do the initialization until onResume so that the
        // TimeConsumingPreferenceActivity dialog can display as it
        // relies on onResume / onPause to maintain its foreground state.

// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        /*Retrieve Call Forward ServiceClass*/
        Intent intent = getIntent();
        Log.d(LOG_TAG, "Intent is " + intent);
        mServiceClass = intent.getIntExtra(PhoneUtils.SERVICE_CLASS,
                CommandsInterface.SERVICE_CLASS_VOICE);
        Log.d(LOG_TAG, "serviceClass: " + mServiceClass);

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        mFirstResume = true;
        mIcicle = icicle;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
// QTI_BEGIN: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23

        if (mCheckData) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            mReceiver = new PhoneAppBroadcastReceiver();
            registerReceiver(mReceiver, intentFilter);
        }
// QTI_END: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
    }

    private void layoutCallForwardItem(boolean support, CallForwardEditPreference preference,
            PreferenceScreen prefSet) {
        if (support) {
            mPreferences.add(preference);
        } else {
// QTI_BEGIN: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
            preference.deInit();
// QTI_END: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
            prefSet.removePreference(preference);
        }
    }

// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    /**
     * Receiver for intent broadcasts the Phone app cares about.
     */
    private class PhoneAppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                String state = intent.getStringExtra(PhoneConstants.STATE_KEY);
                final String apnType = intent.getStringExtra(PhoneConstants.DATA_APN_TYPE_KEY);
                Log.d(LOG_TAG, "apntype is: " + apnType + " state is: " + state);
                if (PhoneConstants.DataState.DISCONNECTED.name().equals(state) &&
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
                            QtiPhoneUtilsHelper.hasDefaultApnType(apnType)) {
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
                    Log.d(LOG_TAG, "default data is disconnected.");
                    checkDataStatus();
                }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                if (mPhone != null) {
                    for (CallForwardEditPreference pref : mPreferences) {
                        if (pref != null) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                            pref.setEnabled(
                                QtiPhoneUtilsHelper.isSuppServiceAllowedInAirplaneMode(mPhone));
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                        }
                    }
                }
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
            }
        }
    }

    public void checkDataStatus() {
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        if (mPhone == null) {
            return;
        }
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        int sub = mPhone.getSubId();
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
        // Find out if the sim card is ready.
// QTI_END: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        boolean isSimReady = TelephonyManager.from(this)
                .getSimState(SubscriptionManager.getSlotIndex(sub))
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
                == TelephonyManager.SIM_STATE_READY;
        if (!isSimReady) {
            Log.d(LOG_TAG, "SIM is not ready!");
            String title = (String)this.getResources().getText(R.string.sim_is_not_ready);
            String message = (String)this.getResources()
// QTI_END: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
                    .getText(R.string.sim_is_not_ready);
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
            showAlertDialog(title, message);
            return;
        }
// QTI_END: 2018-05-13: Telephony: Check SIM status before query CF or hotswap SIM card in query CF UI
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        if (mPhone.isUtEnabled() && mCheckData) {
            // check whether the current data network is roaming and roaming is enabled
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
            boolean isDataRoaming = mPhone.getServiceState().getDataRoaming();
            boolean isDataRoamingEnabled = mPhone.getDataRoamingEnabled();
            boolean promptForDataRoaming = isDataRoaming && !isDataRoamingEnabled;
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
            Log.d(LOG_TAG, "sub = " + sub + ", isDataRoaming = " + isDataRoaming +
                    ", isDataRoamingEnabled = " + isDataRoamingEnabled);
            if (promptForDataRoaming) {
                Log.d(LOG_TAG, "data roaming is disabled");
                String title = (String)this.getResources()
                        .getText(R.string.no_mobile_data_roaming);
                String message = (String)this.getResources()
                        .getText(R.string.cf_setting_mobile_data_roaming_alert);
                showAlertDialog(title, message);
                return;
            }
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
            // check if mobile data on current sub is enabled by user or airplane mode
// QTI_END: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
            boolean isDataEnabled = TelephonyManager.from(this).createForSubscriptionId(sub)
                    .isDataEnabled();
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
            boolean isAirplaneMode = Settings.Global.getInt(
                    mPhone.getContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                    PhoneGlobals.AIRPLANE_OFF) == PhoneGlobals.AIRPLANE_ON;
            if (!isDataEnabled || isAirplaneMode) {
// QTI_END: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
                Log.d(LOG_TAG, "Mobile data is not available");
                String title = (String)this.getResources().getText(R.string.no_mobile_data);
                String message = (String)this.getResources()
                        .getText(R.string.cf_setting_mobile_data_off_alert);
                showAlertDialog(title, message);
                return;
            }
            if (!mIsUtAllowedWhenWifiOn) {
                // check network capabilities
                NetworkCapabilities caps = getNetworkCapabilities();
                if (caps == null) {
                    Log.d(LOG_TAG, "Can not get network capabilities!");
                    String title = (String)this.getResources()
                            .getText(R.string.no_network_available);
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
                    String message = (String)this.getResources()
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
                            .getText(R.string.cf_setting_network_alert);
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
                    showAlertDialog(title, message);
                    return;
                }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
                Log.d(LOG_TAG, "network capabilities : " + caps);
                // check if Wi-Fi is on
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.d(LOG_TAG, "Wi-Fi is on");
                    String title = (String)this.getResources().getText(R.string.wifi_on);
                    String message = (String)this.getResources()
                            .getText(R.string.cf_setting_wifi_on_alert);
                    showAlertDialog(title, message);
                    return;
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
                }
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
            }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
            // check if the current sub is the default sub
            if (sub != SubscriptionManager.getDefaultDataSubscriptionId()) {
                Log.d(LOG_TAG, "Show data in use indication if data sub is not on current sub");
                showDataInuseToast();
            }
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        }
        initCallforwarding();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == DialogInterface.BUTTON_POSITIVE) {
            Intent newIntent = new Intent("android.settings.SETTINGS");
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        }
        finish();
        return;
    }

// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
    private NetworkCapabilities getNetworkCapabilities() {
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        if (cm == null) {
            return null;
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
        Network activeNetwork = cm.getActiveNetwork();
        return cm.getNetworkCapabilities(activeNetwork);
// QTI_END: 2024-03-18: Telephony: Allow to query CF from UI when Wi-Fi is on
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    }

// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    @Override
    public void onResume() {
        super.onResume();
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
        QtiCallForwardUtils.registerMmTelCapabilityCallback(mPhone, mCapabilityCallback);
// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        if (mCheckData) {
            checkDataStatus();
        } else {
            initCallforwarding();
        }
    }

// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2026-02-04: Telephony: FR114115 Dialer Enhancements
    @Override
    public void onPause() {
        super.onPause();
        QtiCallForwardUtils.unregisterMmTelCapabilityCallback(mPhone, mCapabilityCallback);
    }


// QTI_END: 2026-02-04: Telephony: FR114115 Dialer Enhancements
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    private void initCallforwarding () {
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        if (mFirstResume) {
            if (mIcicle == null) {
                Log.d(LOG_TAG, "start to init ");
                CallForwardEditPreference pref = mPreferences.get(mInitIndex);
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
                pref.setExpectMore(canExpectMoreCallFwdReq());
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
                pref.init(this, mPhone, mReplaceInvalidCFNumbers, mServiceClass, mCallForwardByUssd);
// QTI_END: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
                pref.startCallForwardOptionsQuery();

            } else {
                mInitIndex = mPreferences.size();

                for (CallForwardEditPreference pref : mPreferences) {
                    Bundle bundle = mIcicle.getParcelable(pref.getKey());
                    pref.setToggled(bundle.getBoolean(KEY_TOGGLE));
                    pref.setEnabled(bundle.getBoolean(KEY_ENABLE));
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                    pref.setExpectMore(canExpectMoreCallFwdReq());
                    if (bundle.getBoolean(KEY_IS_CFUT)) {
                        pref.init(this, mPhone, mReplaceInvalidCFNumbers,
                                mServiceClass, mCallForwardByUssd);
                        pref.restoreCallCallForwardTimerInfo(
                                bundle.getInt(KEY_START_HOUR),
                                bundle.getInt(KEY_START_MINUTE),
                                bundle.getInt(KEY_END_HOUR),
                                bundle.getInt(KEY_END_MINUTE),
                                bundle.getInt(KEY_STATUS),
                                bundle.getString(KEY_NUMBER),
                                bundle.getBoolean(KEY_IS_CFUT));
                    } else {
                        CallForwardInfo cf = new CallForwardInfo();
                        cf.number = bundle.getString(KEY_NUMBER);
                        cf.status = bundle.getInt(KEY_STATUS);
                        pref.init(this, mPhone, mReplaceInvalidCFNumbers,
                                mServiceClass, mCallForwardByUssd);
                        pref.restoreCallForwardInfo(cf);
                    }
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                }
            }
            mFirstResume = false;
            mIcicle = null;
        }
    }

// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    private void showDataInuseToast() {
        String message = (String)this.getResources()
                .getText(R.string.mobile_data_alert);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
    public void onDestroy() {
        super.onDestroy();
// QTI_END: 2024-11-06: Telephony: Fix the issues related to UT service in airplane mode am: e74f539a23 am: e74f539a23
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
        if (mCheckData && mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
        for (CallForwardEditPreference pref : mPreferences) {
            pref.deInit();
        }
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    }

// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (CallForwardEditPreference pref : mPreferences) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_TOGGLE, pref.isToggled());
            bundle.putBoolean(KEY_ENABLE, pref.isEnabled());
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
            if (pref.isCfutEnabled() &&
                    pref.getPrefId() == CommandsInterface.CF_REASON_UNCONDITIONAL) {
                bundle.putString(KEY_NUMBER, pref.getCfutNumber());
                bundle.putInt(KEY_START_HOUR, pref.getStartHour());
                bundle.putInt(KEY_END_HOUR, pref.getEndHour());
                bundle.putInt(KEY_START_MINUTE, pref.getStartMinute());
                bundle.putInt(KEY_END_MINUTE, pref.getEndMinute());
                bundle.putInt(KEY_STATUS, pref.getCfutStatus());
                bundle.putBoolean(KEY_IS_CFUT, pref.isCfutEnabled());
            } else {
                if (pref.callForwardInfo != null) {
                    bundle.putString(KEY_NUMBER, pref.callForwardInfo.number);
                    bundle.putInt(KEY_STATUS, pref.callForwardInfo.status);
                }
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
            }
            outState.putParcelable(pref.getKey(), bundle);
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
// QTI_BEGIN: 2024-10-15: RIL: Revert "IMS: Auto Retry CFU after CSFB"
            mInitIndex++;
            CallForwardEditPreference pref = mPreferences.get(mInitIndex);
            pref.setExpectMore(canExpectMoreCallFwdReq());
            pref.init(this, mPhone, mReplaceInvalidCFNumbers, mServiceClass, mCallForwardByUssd);
            pref.startCallForwardOptionsQuery();
// QTI_END: 2024-10-15: RIL: Revert "IMS: Auto Retry CFU after CSFB"
        }

        super.onFinished(preference, reading);
        updateVideoCfPreference(preference);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

        // Update CFNL also if CFNRc is changed
        if (preference == mButtonCFNRc && !reading && mSupportCFNL) {
            Log.d(LOG_TAG, "CFNRc is changed, updating CFNL also");
            mButtonCFNL.setExpectMore(canExpectMoreCallFwdReq());
            mButtonCFNL.init(this, mPhone, mReplaceInvalidCFNumbers, mServiceClass,
                    mCallForwardByUssd);
            mButtonCFNL.startCallForwardOptionsQuery();
        }
    }

    public void onError(Preference preference, int error) {
        if (preference == mButtonCFNL &&
                error == QtiCallConstants.CODE_UT_CF_SERVICE_NOT_REGISTERED) {
            Log.d(LOG_TAG, "CFNL failed with CODE_UT_CF_SERVICE_NOT_REGISTERED");
            mSupportCFNL = false;
            getPreferenceScreen().removePreference(preference);
            return;
        }
        super.onError(preference, error);
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
        updateVideoCfPreference(preference);
    }

// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    private boolean canExpectMoreCallFwdReq() {
        return (mInitIndex < mPreferences.size()-1);
    }

// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.

    private void updateVideoCfPreference(Preference preference) {
        if (mServiceClass == CommandsInterface.SERVICE_CLASS_DATA_SYNC
            + CommandsInterface.SERVICE_CLASS_PACKET) {
            preference.setEnabled(mIsVideoCfEnabled);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult: done");
        if (resultCode != RESULT_OK) {
            Log.d(LOG_TAG, "onActivityResult: contact picker result not OK.");
            return;
        }
        Cursor cursor = null;
        try {
            // check if the URI returned by the user belongs to the user
            final int currentUser = UserHandle.getUserId(Process.myUid());
            if (currentUser
                    != ContentProvider.getUserIdFromUri(data.getData(), currentUser)) {

                Log.w(LOG_TAG, "onActivityResult: Contact data of different user, "
                        + "cannot access");
                return;
            }
            cursor = getContentResolver().query(data.getData(),
                NUM_PROJECTION, null, null, null);
            if ((cursor == null) || (!cursor.moveToFirst())) {
                Log.d(LOG_TAG, "onActivityResult: bad contact data, no results found.");
                return;
            }

            switch (requestCode) {
                case CommandsInterface.CF_REASON_UNCONDITIONAL:
                    mButtonCFU.onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_BUSY:
                    mButtonCFB.onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NO_REPLY:
                    mButtonCFNRy.onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NOT_REACHABLE:
                    mButtonCFNRc.onPickActivityResult(cursor.getString(0));
                    break;
// QTI_BEGIN: 2025-01-29: Telephony: IMS: Fix contact picker for Call forward not logged in
                case CommandsInterface.CF_REASON_NOT_LOGGED_IN:
                    mButtonCFNL.onPickActivityResult(cursor.getString(0));
                    break;
// QTI_END: 2025-01-29: Telephony: IMS: Fix contact picker for Call forward not logged in
                default:
                    // TODO: may need exception here.
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            CallFeaturesSetting.goUpToTopLevelSetting(this, mSubscriptionInfoHelper);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
// QTI_BEGIN: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding

    private void showAlertDialog(String title, String message) {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .setOnCancelListener(this)
                .create();
        dialog.show();
    }
// QTI_END: 2018-04-09: Telephony: IMS: Add data check for UT supplementary service of CallForwarding
}
