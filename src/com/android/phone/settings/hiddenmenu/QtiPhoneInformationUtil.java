/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */
package com.android.phone.settings.hiddenmenu;

import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsMmTelManager;
import android.util.Log;

public class QtiPhoneInformationUtil {
    private static final String TAG = "QtiPhoneInformationUtil";

    /**
     * System property controlling whether the test VoLTE/VoNR switches on the hidden menu's
     * Data/Network tab are shown.
     */
    private static final String PROPERTY_TEST_VOLTE_VONR_SWITCH =
            "persist.vendor.radio.test_volte_vonr_switch";

    /**
     * Returns whether the VoLTE/VoNR switches should be shown, based on the
     * {@link #PROPERTY_TEST_VOLTE_VONR_SWITCH} system property.
     *
     * @param context The {@link Context} instance.
     * @param subId The subscription ID to check.
     * @return {@code true} if the switches should be shown, {@code false} otherwise.
     */
    public static boolean isVoLteVoNrSwitchVisible(Context context, int subId) {
        return SystemProperties.getBoolean(PROPERTY_TEST_VOLTE_VONR_SWITCH, false);
    }

    /**
     * Returns whether VoLTE or ViLTE service is available.
     *
     * @param imsMmTelManager The {@link ImsMmTelManager} instance.
     * @return {@code true} if VoLTE or ViLTE service is available, {@code false} otherwise.
     */
    public static boolean isVolteEnabled(ImsMmTelManager imsMmTelManager) {
        if (imsMmTelManager == null) {
            return false;
        }

        try {
            boolean availableVolte = PhoneInformationUtil.isVoiceServiceAvailable(imsMmTelManager);
            boolean availableVt = PhoneInformationUtil.isVideoServiceAvailable(imsMmTelManager);

            Log.d(TAG, "availableVolte:  " + availableVolte + " availableVt: " +
                    availableVt);
            return availableVolte || availableVt;
        } catch (Exception e) {
            Log.e(TAG, "isVolteEnabled e=" + e);
        }
        return false;
    }

    /**
     * Returns whether VoNr service is available.
     *
     * @param telephonyManager The {@link TelephonyManager} instance.
     * @return {@code true} if VoNr service is available, {@code false} otherwise.
     */
    public static boolean isVoNrEnabled(TelephonyManager telephonyManager) {
        if (telephonyManager == null) {
            return false;
        }

        try {
            boolean result = telephonyManager.isVoNrEnabled();
            Log.d(TAG, "isVoNrEnabled " + result);
            return result;
        } catch (IllegalStateException e) {
            Log.e(TAG, "isVoNrEnabled IllegalStateException =", e);
        }
        return false;
    }

    /**
     * Get the effective VoLTE enabled state for the given subId.
     * Priority:
     * 1. SubscriptionManager.ENHANCED_4G_MODE_ENABLED subscription property, if explicitly set.
     * 2. CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL, if not set.
     * 3. Default to true if carrier config is unavailable.
     */
    public static boolean getVoLteEnabled(Context context, int subId) {
        int voLteSetting = SubscriptionManager.getIntegerSubscriptionProperty(
                subId, SubscriptionManager.ENHANCED_4G_MODE_ENABLED, -1, context);
        if (voLteSetting != -1) {
            return voLteSetting == 1;
        }

        CarrierConfigManager carrierConfigManager = PhoneInformationUtil.getCarrierConfig(context);
        if (carrierConfigManager != null) {
            PersistableBundle b = carrierConfigManager.getConfigForSubId(subId,
                    CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL);
            if (b != null) {
                return b.getBoolean(
                        CarrierConfigManager.KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL, true);
            }
        }
        return true;
    }

    /**
     * Get the effective VoNR enabled state for the given subId.
     * Priority:
     * 1. SubscriptionManager.NR_ADVANCED_CALLING_ENABLED subscription property, if explicitly
     *    set.
     * 2. CarrierConfigManager.KEY_VONR_ON_BY_DEFAULT_BOOL, if not set.
     * 3. Default to true if carrier config is unavailable.
     */
    public static boolean getVoNrEnabled(Context context, int subId) {
        int voNRSetting = SubscriptionManager.getIntegerSubscriptionProperty(
                subId, SubscriptionManager.NR_ADVANCED_CALLING_ENABLED, -1, context);
        if (voNRSetting != -1) {
            return voNRSetting == 1;
        }

        CarrierConfigManager carrierConfigManager = PhoneInformationUtil.getCarrierConfig(context);
        if (carrierConfigManager != null) {
            PersistableBundle b = carrierConfigManager.getConfigForSubId(subId,
                    CarrierConfigManager.KEY_VONR_ON_BY_DEFAULT_BOOL);
            if (b != null) {
                return b.getBoolean(CarrierConfigManager.KEY_VONR_ON_BY_DEFAULT_BOOL, true);
            }
        }
        return true;
    }

    public static void setVoImsOptInSetting(boolean isChecked, Context context, int subId) {
        if (context == null) {
            return;
        }

        CarrierConfigManager carrierConfigManager = PhoneInformationUtil.getCarrierConfig(context);
        if (carrierConfigManager == null ) {
            return;
        }

        PersistableBundle b = carrierConfigManager.getConfigForSubId(subId,
            CarrierConfigManager.KEY_EDITABLE_ENHANCED_4G_LTE_BOOL,
            CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL
        );
        //If Enhanced 4G LTE Mode is uneditable, hidden and VoLTE is disabled we
        //will enable VoIMS opt-in to allow the user to change the IMS enabled
        //setting, this is to adapt to the logic in ImsManager.java
        if (b != null) {
            boolean isUiUnEditable = !b.getBoolean(CarrierConfigManager.
                    KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, false) || b.getBoolean
                    (CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, false);
            if (isUiUnEditable) {
                SubscriptionManager.setSubscriptionProperty(subId,
                        SubscriptionManager.VOIMS_OPT_IN_STATUS, (isChecked ? "0" : "1"));
            }
        }
    }
}
