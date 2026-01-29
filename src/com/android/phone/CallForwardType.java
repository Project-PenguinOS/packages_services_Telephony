// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
/* Copyright (c) 2015, 2017-2018, The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
/**
* Changes from Qualcomm Innovation Center, Inc. are provided under the following license:
* Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
* SPDX-License-Identifier: BSD-3-Clause-Clear
*/

// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
package com.android.phone;

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import android.content.BroadcastReceiver;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.content.Context;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import android.content.Intent;
import android.content.IntentFilter;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.os.Bundle;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
import android.os.PersistableBundle;
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.telephony.ims.ImsMmTelManager;
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.feature.MmTelFeature;
import android.telephony.SubscriptionManager;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.util.Log;
import android.view.MenuItem;

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import com.android.ims.FeatureConnector;
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import com.android.internal.telephony.CommandsInterface;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import com.android.internal.telephony.IccCardConstants;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
import com.android.internal.telephony.TelephonyIntents;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
import android.preference.Preference.OnPreferenceClickListener;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
import android.provider.Settings;
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.

public class CallForwardType extends PreferenceActivity {
    private static final String LOG_TAG = "CallForwardType";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_CF_KEY_VOICE = "button_cf_key_voice";
    private static final String BUTTON_CF_KEY_VIDEO = "button_cf_key_video";

    private Preference mVoicePreference;
    private Preference mVideoPreference;
    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    private boolean mIsUtCapable = false;
    private boolean mIsVtCapable = false;
    boolean mHideVtCfOption = false;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    private FeatureConnector<ImsManager> mFeatureConnector;
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    private int mPhoneId;
    private IntentFilter mIntentFilter;

// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive intent : " + intent);
            String action = intent.getAction();
            int phoneId = intent.getIntExtra(PhoneConstants.PHONE_KEY,
                    SubscriptionManager.INVALID_PHONE_INDEX);
            if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
                if (phoneId == mPhoneId &&
                        IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(
                            intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE))) {
                    Log.d(LOG_TAG, "onSimAbsent, exit");
                    finish();
                }
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                if (mPhone != null) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                    setPreferencesState(
                        QtiPhoneUtilsHelper.isSuppServiceAllowedInAirplaneMode(mPhone));
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                }
            }
        }
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
     };
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false

// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
    private void setPreferencesState(boolean state) {
        if (mVoicePreference != null && findPreference(BUTTON_CF_KEY_VOICE) != null) {
            mVoicePreference.setEnabled(state);
        }
        if (mVideoPreference != null && findPreference(BUTTON_CF_KEY_VIDEO) != null) {
            mVideoPreference.setEnabled(state);
        }
    }

// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
    private ImsMmTelManager.CapabilityCallback mCapabilityCallback =
        new ImsMmTelManager.CapabilityCallback() {
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
            @Override
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
            public void onCapabilitiesStatusChanged(MmTelFeature.MmTelCapabilities capabilities) {
                    boolean isUtCapable = capabilities.isCapable(
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
                            MmTelFeature.MmTelCapabilities.CAPABILITY_TYPE_UT);
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
                    boolean isVtCapable = capabilities.isCapable(
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
                            MmTelFeature.MmTelCapabilities.CAPABILITY_TYPE_VIDEO);
                    if (isUtCapable ==  mIsUtCapable && isVtCapable == mIsVtCapable) {
                        return;
                    }
                    mIsUtCapable = isUtCapable;
                    mIsVtCapable = isVtCapable;
                    showVideoOption(mIsUtCapable && mIsVtCapable && !mHideVtCfOption);
            }
    };

    private void setListeners() throws ImsException {
        ImsManager imsMgr = ImsManager.getInstance(mPhone.getContext(), mPhone.getPhoneId());
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2021-01-25: Telephony: IMS: Fix NullPointerException in phone process
        imsMgr.addCapabilitiesCallback(mCapabilityCallback, mPhone.getContext().getMainExecutor());
// QTI_END: 2021-01-25: Telephony: IMS: Fix NullPointerException in phone process
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    }

    private void removeListeners() {
        ImsManager imsMgr = ImsManager.getInstance(mPhone.getContext(), mPhone.getPhoneId());
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        imsMgr.removeCapabilitiesCallback(mCapabilityCallback);
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    }

    private void showVideoOption(boolean show) {
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        if (!show && mVideoPreference != null && findPreference(BUTTON_CF_KEY_VIDEO) != null) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
            Log.d(LOG_TAG, "remove video option");
            getPreferenceScreen().removePreference(mVideoPreference);
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        } else if (show && mVideoPreference != null) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
            Log.d(LOG_TAG, "enable video option");
            getPreferenceScreen().addPreference(mVideoPreference);
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
            if (mPhone != null) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                mVideoPreference.setEnabled(
                    QtiPhoneUtilsHelper.isSuppServiceAllowedInAirplaneMode(mPhone));
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
            }
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        }
    }
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(LOG_TAG, "onCreate..");
        /*Loading CallForward Setting page*/
        addPreferencesFromResource(R.xml.call_forward_type);
        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mPhone = mSubscriptionInfoHelper.getPhone();
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        mPhoneId = mPhone.getPhoneId();
        mIsUtCapable = mPhone.isUtEnabled();
        mIsVtCapable = mPhone.isVideoEnabled();
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        mFeatureConnector = ImsManager.getConnector(
            mPhone.getContext(), mPhone.getPhoneId(), LOG_TAG,
            new FeatureConnector.Listener<ImsManager>() {
                @Override
                public void connectionReady(ImsManager manager, int subId) throws ImsException {
                    Log.d(LOG_TAG, "ImsManager: connection ready.");
                    setListeners();
                }

                @Override
                public void connectionUnavailable(int reason) {
                    Log.d(LOG_TAG, "ImsManager: connection unavailable.");
                    removeListeners();
                }
// QTI_BEGIN: 2021-01-25: Telephony: IMS: Fix NullPointerException in phone process
            }, mPhone.getContext().getMainExecutor());
// QTI_END: 2021-01-25: Telephony: IMS: Fix NullPointerException in phone process
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.

        /*Voice Button*/
        mVoicePreference = (Preference) findPreference(BUTTON_CF_KEY_VOICE);
        mVoicePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            /*onClicking Voice Button*/
            public boolean onPreferenceClick(Preference pref) {
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-04-18: Telephony: Wrong subid extra name passed to GsmUmtsCallForwardOptions activity
                Intent intent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
// QTI_END: 2018-04-18: Telephony: Wrong subid extra name passed to GsmUmtsCallForwardOptions activity
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
                Log.d(LOG_TAG, "Voice button clicked!");
                intent.putExtra(PhoneUtils.SERVICE_CLASS,
                        CommandsInterface.SERVICE_CLASS_VOICE);
                startActivity(intent);
                return true;
            }
        });

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        /*Video Button*/
        mVideoPreference = (Preference) findPreference(BUTTON_CF_KEY_VIDEO);
        mVideoPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode

// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
            /*onClicking Video Button*/
            public boolean onPreferenceClick(Preference pref) {
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
                Intent intent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
                Log.d(LOG_TAG, "Video button clicked!");
                intent.putExtra(PhoneUtils.SERVICE_CLASS,
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
                        (CommandsInterface.SERVICE_CLASS_DATA_SYNC +
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
                        CommandsInterface.SERVICE_CLASS_PACKET));
                startActivity(intent);
                return true;
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
            }
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        });
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        CarrierConfigManager cfgManager = (CarrierConfigManager)
                mPhone.getContext().getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (cfgManager != null) {
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
            mHideVtCfOption = cfgManager.getConfigForSubId(mPhone.getSubId())
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
                .getBoolean("config_hide_vt_callforward_option");
        }
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        /*set preference state base on whether supplementary service is allowed*/
// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
        setPreferencesState(QtiPhoneUtilsHelper.isSuppServiceAllowedInAirplaneMode(mPhone));
// QTI_BEGIN: 2024-07-29: Telephony: Fix call forward option button is not grayed out issue
        registerReceiver(mBroadcastReceiver, mIntentFilter);
// QTI_END: 2024-07-29: Telephony: Fix call forward option button is not grayed out issue
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    }
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false

// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    @Override
    protected void onResume() {
        super.onResume();
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        mFeatureConnector.connect();
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false

        if (mHideVtCfOption || !(mPhone.isUtEnabled() && mPhone.isVideoEnabled())) {
            Log.d(LOG_TAG, "VT or/and Ut Service is not enabled");
            showVideoOption(false);
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        }
    }

// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
    @Override
    protected void onPause() {
        super.onPause();
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        mFeatureConnector.disconnect();
// QTI_BEGIN: 2024-07-29: Telephony: Fix call forward option button is not grayed out issue
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
// QTI_END: 2024-07-29: Telephony: Fix call forward option button is not grayed out issue
// QTI_BEGIN: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false
        unregisterReceiver(mBroadcastReceiver);
    }
// QTI_END: 2018-09-19: Telephony: IMS: Video call forwarding option does not disappear after UT is false

// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
