/*
 * Copyright (C) 2006 The Android Open Source Project
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

 * Changes from Qualcomm Technologies, Inc. are provided under the following license:
 *
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.phone;

import static com.qti.extphone.ExtTelephonyManager.FEATURE_BACK_TO_BACK_SUPPLEMENTARY_SERVICE_REQ;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsReasonInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.FdnUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SsData;
import com.qti.extphone.ExtTelephonyManager;
import com.qti.extphone.ServiceCallback;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Utilities for TelephonyManager / TelecomManager / PhoneAccount access.
 */
public final class QtiPhoneUtilsHelper {

    private static final String LOG_TAG = "QtiPhoneUtilsHelper";
    private static TelephonyManager sTelephonyManager;
    private static TelecomManager sTelecomManager;
    private static final int INVALID = -1;
    private static int sBackToBackSSFeature = INVALID;
    private static ExtTelephonyManager sExtTelephonyManager;


    /**
     * To get the phone account using subscription ID.
     */
    static PhoneAccount getPhoneAccount(int subId) {
        PhoneAccountHandle handle = getTelephonyManager() != null
                ? getTelephonyManager().getPhoneAccountHandleForSubscriptionId(subId)
                : null;
        return getTelecomManager() != null && handle != null
                ? getTelecomManager().getPhoneAccount(handle)
                : null;
    }

    /**
     * Returns true if device is in Multi-SIM mode, false otherwise.
     * (Original comment said Single Standby but logic checks active modem count > 1.)
     */
    static boolean isMultiSimMode() {
        return getTelephonyManager() != null
                && getTelephonyManager().getActiveModemCount() > 1;
    }

    /**
     * To get the instance of TelephonyManager.
     */
    static TelephonyManager getTelephonyManager() {
        if (sTelephonyManager == null) {
            sTelephonyManager =
                    PhoneGlobals.getInstance().getSystemService(TelephonyManager.class);
        }
        return sTelephonyManager;
    }

    /**
     * To get the instance of TelecomManager.
     */
    static TelecomManager getTelecomManager() {
        if (sTelecomManager == null) {
            sTelecomManager =
                    PhoneGlobals.getInstance().getSystemService(TelecomManager.class);
        }
        return sTelecomManager;
    }

    static CommandException getCommandException(int code) {
            CommandException.Error error = CommandException.Error.GENERIC_FAILURE;

        switch(code) {
            case ImsReasonInfo.CODE_UT_NOT_SUPPORTED:
                error = CommandException.Error.REQUEST_NOT_SUPPORTED;
                break;
            case ImsReasonInfo.CODE_UT_CB_PASSWORD_MISMATCH:
                error = CommandException.Error.PASSWORD_INCORRECT;
                break;
            case ImsReasonInfo.CODE_UT_SERVICE_UNAVAILABLE:
                error = CommandException.Error.RADIO_NOT_AVAILABLE;
                break;
            case ImsReasonInfo.CODE_FDN_BLOCKED:
                error = CommandException.Error.FDN_CHECK_FAILURE;
                break;
            case ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_DIAL:
                error = CommandException.Error.SS_MODIFIED_TO_DIAL;
                break;
            case ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_USSD:
                error = CommandException.Error.SS_MODIFIED_TO_USSD;
                break;
            case ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_SS:
                error = CommandException.Error.SS_MODIFIED_TO_SS;
                break;
            case ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_DIAL_VIDEO:
                error = CommandException.Error.SS_MODIFIED_TO_DIAL_VIDEO;
                break;
            default:
                break;
        }

        return new CommandException(error);
    }

    static boolean isBacktoBackSSFeatureSupported() {
        if (sBackToBackSSFeature == INVALID &&
                sExtTelephonyManager != null) {
            sBackToBackSSFeature =
                   (sExtTelephonyManager.isFeatureSupported(
                   FEATURE_BACK_TO_BACK_SUPPLEMENTARY_SERVICE_REQ)) ? 1 : 0;
        }
        return (sBackToBackSSFeature == 1);
    }

    static void connectExtTelephonyManager(Context context) {
        sExtTelephonyManager = ExtTelephonyManager.getInstance(context);

        sExtTelephonyManager.connectService(mExtTelManagerServiceCallback);
    }

    public static ExtTelephonyManager getExtTelManager() {
        return sExtTelephonyManager;
    }

    private static ServiceCallback mExtTelManagerServiceCallback = new ServiceCallback() {
        @Override
        public void onConnected() {
            Log.d(LOG_TAG, "mExtTelManagerServiceCallback: service connected");
        }

        @Override
        public void onDisconnected() {
            Log.d(LOG_TAG, "mExtTelManagerServiceCallback: service disconnected");
        }
    };

    static boolean isRequestBlockedByFdn(SsData.RequestType requestType,
            SsData.ServiceType serviceType, int phoneId, Context context) {
        if (context == null) {
            return false;
        }
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryIso = (telephonyManager != null) ?
                telephonyManager.getNetworkCountryIso(phoneId).toUpperCase(Locale.ROOT) :
                "";
        ArrayList<String> controlStrings = GsmMmiCode.getControlStrings(requestType, serviceType);
        return FdnUtils.isSuppServiceRequestBlockedByFdn(phoneId, controlStrings, countryIso);
    }

    /**
     * To check whether supplementary service is allowed in airplane mode.
     */
    static boolean isSuppServiceAllowedInAirplaneMode(Phone phone) {
        if (phone == null) {
            return false;
        }
        final PhoneGlobals app = PhoneGlobals.getInstance();
        int subId = phone.getSubId();
        PersistableBundle b = SubscriptionManager.isValidSubscriptionId(subId)
                ? app.getCarrierConfigForSubId(subId) : app.getCarrierConfig();
        boolean config = b != null && b.getBoolean(
                CarrierConfigManager.KEY_DISABLE_SUPPLEMENTARY_SERVICES_IN_AIRPLANE_MODE_BOOL);
        if (!config) {
            return true;
        }
        boolean isAirplaneModeOn = Settings.Global.getInt(phone.getContext().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, PhoneGlobals.AIRPLANE_OFF)
                == PhoneGlobals.AIRPLANE_ON;
        return !isAirplaneModeOn || phone.isWifiCallingEnabled() && phone.isImsRegistered();
    }

    static boolean hasDefaultApnType(String apnType) {
        if (TextUtils.isEmpty(apnType)) {
            return false;
        }
        for (String str : apnType.split(",")) {
            if (str.equals(PhoneConstants.APN_TYPE_DEFAULT)) {
                return true;
            }
        }
        return false;
    }

    static boolean isUtEnabledToDisableClir(Context context, Phone phone) {
        if (context == null || phone == null) {
            return false;
        }

        boolean skipClir = false;
        CarrierConfigManager configManager = (CarrierConfigManager)
                context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configManager != null) {
            PersistableBundle pb = configManager.getConfigForSubId(phone.getSubId());
            if (pb != null) {
                skipClir = pb.getBoolean("config_disable_clir_over_ut");
            }
        }
        return phone.isUtEnabled() && skipClir;
    }

    static boolean isDisableOutCallBarringOverIms(Phone phone) {
        if (phone == null) {
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager)phone.getContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configManager != null) {
            PersistableBundle pb = configManager.getConfigForSubId(phone.getSubId());
            return pb != null ? pb.getBoolean("config_disable_outgoing_callbarring_over_ims") :
            false;
        }
        return false;
    }

    static boolean isDisableChangePasswordOverIms(Phone phone) {
        if (phone == null) {
            return false;
        }
        CarrierConfigManager configManager = (CarrierConfigManager)phone.getContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configManager != null) {
            PersistableBundle pb = configManager.getConfigForSubId(phone.getSubId());
            return pb != null ? pb.getBoolean("config_disable_change_password_over_ims") : false;
        }
        return false;
    }

    /**
     * Returns true if "Set All Call Barring" (Deactivate all) is supported over IMS.
     * Requires IR92 v10 or above. Returns false only for operators that explicitly
     * set config_support_set_all_call_barring_over_ims=false (IR92 v9 and below).
     */
    static boolean isSupportSetAllCallBarringOverIms(Phone phone) {
        if (phone == null) {
            return true;
        }
        CarrierConfigManager configManager = (CarrierConfigManager)phone.getContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
        if (configManager != null) {
            PersistableBundle pb = configManager.getConfigForSubId(phone.getSubId());
            return pb != null
                    ? pb.getBoolean("config_support_set_all_call_barring_over_ims", true)
                    : true;
        }
        return true;
    }

    static int getActiveNetworkType(Phone phone) {
        if (phone == null) {
            return ConnectivityManager.TYPE_NONE;
        }
        ConnectivityManager cm = (ConnectivityManager)phone.getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if ((ni == null) || !ni.isConnected()){
                return ConnectivityManager.TYPE_NONE;
            }
            return ni.getType();
        }
        return ConnectivityManager.TYPE_NONE;
    }
}
