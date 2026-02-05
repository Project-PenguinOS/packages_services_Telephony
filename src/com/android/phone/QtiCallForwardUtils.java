/**
* Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
* SPDX-License-Identifier: BSD-3-Clause-Clear
*/

package com.android.phone;

import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsException;
import android.util.Log;
import com.android.internal.telephony.Phone;
import java.util.ArrayList;

/**
 * Utility methods for managing call-forwarding UI state and IMS MMTEL capability callbacks.
 */
public class QtiCallForwardUtils {
    private static final String LOG_TAG = "QtiCallForwardUtils";

    /**
     * Enables or disables every call forwarding row in the provided list. Use this to
     * block or allow user interaction while network operations are in progress.
     *
     * @param preferences   non-null list of CallForwardEditPreference rows to update
     * @param shouldEnable  true to enable all rows; false to disable all rows
     */
    public static void updateAllCfRows(ArrayList<CallForwardEditPreference> preferences,
            boolean shouldEnable) {
        for (CallForwardEditPreference pref : preferences) {
            pref.setEnabled(shouldEnable);
        }
    }

    /**
     * Registers an ImsMmTelManager.CapabilityCallback for the subscription tied to the given Phone.
     * The callback runs on the Phone context’s main executor. No action occurs if phone or
     * capabilityCallback is null. ImsException and IllegalArgumentException are caught and logged.
     *
     * @param phone               Phone providing the subscription ID and executor context
     * @param capabilityCallback  callback to receive IMS MMTEL capability updates
     */
    public static void registerMmTelCapabilityCallback(Phone phone,
            ImsMmTelManager.CapabilityCallback capabilityCallback) {
        if (capabilityCallback == null || phone == null) return;
        try {
            ImsMmTelManager imsMmTelManager = ImsMmTelManager
                    .createForSubscriptionId(phone.getSubId());
            imsMmTelManager.registerMmTelCapabilityCallback(
                    phone.getContext().getMainExecutor(),
                    capabilityCallback
            );
        } catch (ImsException | IllegalArgumentException e) {
            Log.w(LOG_TAG, "Exception when registering CapabilityCallback: " + e.getMessage());
        }
    }

    /**
     * Unregisters the provided ImsMmTelManager.CapabilityCallback from the Phone’s subscription.
     * No action occurs if phone or capabilityCallback is null. Any Exception during unregistration
     * is caught and logged.
     *
     * @param phone               Phone providing the subscription ID
     * @param capabilityCallback  callback instance previously registered via
     *                            registerMmTelCapabilityCallback
     */
    public static void unregisterMmTelCapabilityCallback(Phone phone,
            ImsMmTelManager.CapabilityCallback capabilityCallback) {
        if (capabilityCallback == null || phone == null) return;
        try {
            ImsMmTelManager imsMmTelManager = ImsMmTelManager
                    .createForSubscriptionId(phone.getSubId());
            imsMmTelManager.unregisterMmTelCapabilityCallback(capabilityCallback);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Exception when unregistering CapabilityCallback: " + e.getMessage());
        }
    }
}
