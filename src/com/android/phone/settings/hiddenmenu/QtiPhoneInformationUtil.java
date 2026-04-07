/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */
package com.android.phone.settings.hiddenmenu;

import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsMmTelManager;
import android.util.Log;

public class QtiPhoneInformationUtil {
    private static final String TAG = "QtiPhoneInformationUtil";

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
