/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */
package com.android.phone.settings.hiddenmenu;

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
}
