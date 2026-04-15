/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.phone;

import android.content.Context;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import org.codeaurora.ims.QtiImsException;
import org.codeaurora.ims.QtiImsExtListenerBaseImpl;
import org.codeaurora.ims.QtiImsExtConnector;
import org.codeaurora.ims.QtiImsExtManager;

/**
 * This class heps to query the feature support for disable all barring option.
 */
public class QtiCallBarringDeselectAllHelper {
    private static final String LOG_TAG = "QtiCallBarringDeselectAllHelper";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    private QtiImsExtConnector mQtiImsExtConnector;
    private QtiImsExtManager mQtiImsExtManager;
    private Phone mPhone;
    private CallBarringDeselectAllPreference mDisableAll;

    /**
     * QtiCallBarringDeselectAllHelper constructor.
     *
     * @param pref CallBarringDeselectAllPreference preference
     */
    public QtiCallBarringDeselectAllHelper(CallBarringDeselectAllPreference pref) {
        mPhone = PhoneFactory.getDefaultPhone();
        mDisableAll = pref;
    }

    private void createQtiImsExtConnector(Context context) {
        Log.i(LOG_TAG, "createQtiImsExtConnector");
        try {
            mQtiImsExtConnector = new QtiImsExtConnector(context,
                    new QtiImsExtConnector.IListener() {
                        @Override
                        public void onConnectionAvailable(QtiImsExtManager qtiImsExtManager) {
                            Log.i(LOG_TAG, "QtiImsExtConnector onConnectionAvailable");
                            mQtiImsExtManager = qtiImsExtManager;
                            queryDisableAllSupport();
                        }
                        @Override
                        public void onConnectionUnavailable() {
                            mQtiImsExtManager = null;
                        }
                    });
        } catch (QtiImsException e) {
            Log.e(LOG_TAG, "Unable to create QtiImsExtConnector");
        }
    }

    private void queryDisableAllSupport() {
        boolean isSupported = false;
        if(mQtiImsExtManager != null) {
            try {
                isSupported =
                    mQtiImsExtManager.isDeactivateAllCallBarringSupported(mPhone.getPhoneId());
            } catch (QtiImsException e) {
               Log.e(LOG_TAG, "Failed to query disable all support", e);
            }
        }
        mDisableAll.setEnabled(isSupported);
    }

    public void deInit() {
        if (mQtiImsExtConnector != null) {
            mQtiImsExtConnector.disconnect();
            mQtiImsExtConnector = null;
            mQtiImsExtManager = null;
        }
    }

    public void init(Phone phone) {
        mPhone = phone;
        // Check carrier config: if Set All Call Barring is not supported over IMS
        // for this operator (IR92 v9 and below), disable the button immediately.
        if (!QtiPhoneUtilsHelper.isSupportSetAllCallBarringOverIms(phone)) {
            Log.d(LOG_TAG, "Set All Call Barring not supported over IMS for this operator");
            mDisableAll.setEnabled(false);
            return;
        }
        if (mQtiImsExtConnector == null) {
            createQtiImsExtConnector(mDisableAll.getContext());
            //Connect will get the QtiImsExtManager instance.
            if(mQtiImsExtConnector != null) {
                mQtiImsExtConnector.connect();
            }
        } else {
            queryDisableAllSupport();
        }
    }
}
