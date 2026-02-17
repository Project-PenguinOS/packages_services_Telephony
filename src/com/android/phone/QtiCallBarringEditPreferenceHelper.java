/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.android.phone;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.stub.ImsUtImplBase;
import android.util.Log;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SsData;

import com.qti.extphone.Client;
import com.qti.extphone.ExtPhoneCallbackListener;
import com.qti.extphone.ExtTelephonyManager;
import com.qti.extphone.Status;

import org.codeaurora.ims.QtiImsException;
import org.codeaurora.ims.QtiImsExtConnector;
import org.codeaurora.ims.QtiImsExtListenerBaseImpl;
import org.codeaurora.ims.QtiImsExtManager;

import java.util.concurrent.Executor;

/**
 * This class represents the status of call barring options, enabling/disabling
 */
public class QtiCallBarringEditPreferenceHelper {

    private static final String LOG_TAG = "QtiCallBarringEditPreferenceHelper";

    private final CallBarringEditPreference mCbEditPref;
    private final Context mContext;

    private TimeConsumingPreferenceListener mTcpListener;
    private ExtTelephonyManager mExtTelephonyManager;
    private Phone mPhone;

    private QtiImsExtConnector mQtiImsExtConnector;
    private QtiImsExtManager mQtiImsExtManager;

    private Executor mExecutor;
    private QtiImsExtListenerBaseImpl mImsInterfaceListener;
    private Handler mHandler;
    private boolean mExpectMore;

    public QtiCallBarringEditPreferenceHelper(CallBarringEditPreference cbPref, Context context,
                                                Handler handler) {
        mCbEditPref = cbPref;
        mContext = context;
        if (mContext == null) return;
        mHandler = handler;

        mExtTelephonyManager = ExtTelephonyManager.getInstance(context);
        mExecutor = mContext.getMainExecutor();
        mImsInterfaceListener = new QtiImsExtListenerBaseImpl(mExecutor) {
            @Override
            public void onUTReqFailed(int phoneId, int errCode, String errString) {
                Log.d(LOG_TAG, "onUTReqFailed phoneId=" + phoneId + " errCode= "
                        + errCode + "errString ="+ errString);
                if (errCode == ImsReasonInfo.CODE_LOCAL_CALL_CS_RETRY_REQUIRED) {
                    getCallBarringWithExpectMore();
                } else {
                    Message msg = mHandler.obtainMessage(
                            CallBarringEditPreference.MyHandler.MESSAGE_GET_CALL_BARRING);
                    AsyncResult.forMessage(
                            msg, null, QtiPhoneUtilsHelper.getCommandException(errCode));
                    msg.sendToTarget();
                }
            }

            @Override
            public void queryCallBarringResponse(int[] response) {
                Message msg = mHandler.obtainMessage(
                        CallBarringEditPreference.MyHandler.MESSAGE_GET_CALL_BARRING);
                AsyncResult.forMessage(msg, response, null);
                msg.sendToTarget();
            }
        };
    }

    void init(boolean skipReading, Phone phone, TimeConsumingPreferenceListener listener) {
        if (phone == null || listener == null) {
            return;
        }
        mPhone = phone;
        mTcpListener = listener;

        if (!skipReading) {
            if (!mPhone.isUtEnabled()) {
                if (mPhone.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM &&
                        QtiPhoneUtilsHelper.isBacktoBackSSFeatureSupported()) {
                    getCallBarringWithExpectMore();
                } else {
                    mPhone.getCallBarring(
                            mCbEditPref.getCbFacility(),
                            "",
                            mHandler.obtainMessage(
                                    CallBarringEditPreference.MyHandler
                                            .MESSAGE_GET_CALL_BARRING),
                            mCbEditPref.getServiceClassForCallBarring(mPhone));
                }
            } else {
                if (mQtiImsExtConnector == null) {
                    createQtiImsExtConnector();
                    //Connect will get the QtiImsExtManager instance.
                    if (mQtiImsExtConnector != null) {
                        mQtiImsExtConnector.connect();
                    }
                }
            }
            mTcpListener.onStarted(mCbEditPref, true);

        }
    }

    void setExpectMore(boolean expectMore) {
        mExpectMore = expectMore;
    }

    void deInit() {
        if (mQtiImsExtConnector != null) {
            mQtiImsExtConnector.disconnect();
            mQtiImsExtConnector = null;
            mQtiImsExtManager = null;
        }
        if (mExtTelephonyManager != null && mExtPhoneCallbackListener != null) {
            mExtTelephonyManager.unregisterCallback(mExtPhoneCallbackListener);
        }
        mTcpListener = null;
        mExtPhoneCallbackListener = null;
    }

    void setCallBarringInternal(String password) {
        if (mPhone == null) return;
        mPhone.setCallBarring(
                mCbEditPref.getCbFacility(),
                !mCbEditPref.shouldDisableDependents(),
                password,
                mHandler.obtainMessage(
                        CallBarringEditPreference.MyHandler.MESSAGE_SET_CALL_BARRING),
                mCbEditPref.getServiceClassForCallBarring(mPhone));

        mTcpListener.onStarted(mCbEditPref, false);
    }

    private boolean isCbQueryBlockedByFdn() {
        if (mPhone == null) {
            return false;
        }
        SsData.ServiceType serviceType =
                GsmMmiCode.cbFacilityToServiceType(mCbEditPref.getCbFacility());
        return QtiPhoneUtilsHelper.isRequestBlockedByFdn(
                SsData.RequestType.SS_INTERROGATION,
                serviceType,
                mPhone.getPhoneId(),
                mContext);
    }

    private void createQtiImsExtConnector() {
        try {
            mQtiImsExtConnector = new QtiImsExtConnector(
                    mContext,
                    new QtiImsExtConnector.IListener() {
                        @Override
                        public void onConnectionAvailable(QtiImsExtManager mgr) {
                            Log.i(LOG_TAG, "QtiImsExtConnector onConnectionAvailable");
                            mQtiImsExtManager = mgr;
                            queryImsCallBarringStatus();
                        }

                        @Override
                        public void onConnectionUnavailable() {
                            Log.i(LOG_TAG, "QtiImsExtConnector onConnectionUnavailable");
                            mQtiImsExtManager = null;
                        }
                    });
        } catch (QtiImsException e) {
            Log.e(LOG_TAG, "Unable to create QtiImsExtConnector", e);
        }
    }

    protected void queryImsCallBarringStatus() {
        if (isCbQueryBlockedByFdn()) {
            Log.d(LOG_TAG, "queryImsCallBarringStatus blocked by FDN check");
            sendErrorResponse(CommandException.Error.FDN_CHECK_FAILURE);
            return;
        }
        if (mQtiImsExtManager == null) {
            Log.e(LOG_TAG, "IMS Service not connected");
            sendErrorResponse();
            return;
        }

        try {
            mQtiImsExtManager.queryCallBarring(
                    mPhone.getPhoneId(),
                    getCBTypeFromFacility(mCbEditPref.getCbFacility()),
                    "",
                    mCbEditPref.getServiceClassForCallBarring(mPhone),
                    mExpectMore,
                    mImsInterfaceListener);
        } catch (QtiImsException e) {
            Log.e(LOG_TAG, "queryCallBarring failed", e);
            sendErrorResponse();
        }
    }

    private void sendErrorResponse() {
        sendErrorResponse(CommandException.Error.GENERIC_FAILURE);
    }

    private void sendErrorResponse(CommandException.Error err) {
        Message msg = mHandler.obtainMessage(
                CallBarringEditPreference.MyHandler.MESSAGE_GET_CALL_BARRING);
        AsyncResult.forMessage(msg, null, new CommandException(err));
        msg.sendToTarget();
    }

    private int getCBTypeFromFacility(String facility) {
        if (CommandsInterface.CB_FACILITY_BAOC.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_ALL_OUTGOING;
        } else if (CommandsInterface.CB_FACILITY_BAOIC.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_OUTGOING_INTL;
        } else if (CommandsInterface.CB_FACILITY_BAOICxH.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_OUTGOING_INTL_EXCL_HOME;
        } else if (CommandsInterface.CB_FACILITY_BAIC.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_ALL_INCOMING;
        } else if (CommandsInterface.CB_FACILITY_BAICr.equals(facility)) {
            return ImsUtImplBase.CALL_BLOCKING_INCOMING_WHEN_ROAMING;
        } else if (CommandsInterface.CB_FACILITY_BA_ALL.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_ALL;
        } else if (CommandsInterface.CB_FACILITY_BA_MO.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_OUTGOING_ALL_SERVICES;
        } else if (CommandsInterface.CB_FACILITY_BA_MT.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_INCOMING_ALL_SERVICES;
        } else if (CommandsInterface.CB_FACILITY_BIC_ACR.equals(facility)) {
            return ImsUtImplBase.CALL_BARRING_ANONYMOUS_INCOMING;
        }
        return ImsUtImplBase.INVALID_RESULT;
    }

    private void getCallBarringWithExpectMore() {
        if (isCbQueryBlockedByFdn()) {
            Log.d(LOG_TAG, "getCallBarringWithExpectMore blocked by FDN check");
            sendErrorResponse(CommandException.Error.FDN_CHECK_FAILURE);
            return;
        }
        if (mExtTelephonyManager == null || !mExtTelephonyManager.isServiceConnected()) {
            sendErrorResponse();
            return;
        }
        try {
            int[] events = new int[] {};
            Client mClient = mExtTelephonyManager.registerCallbackWithEvents(
                    mContext.getPackageName(), mExtPhoneCallbackListener, events);
            mExtTelephonyManager.getFacilityLockForApp(
                    mPhone.getPhoneId(),
                    mCbEditPref.getCbFacility(),
                    "" /* password */,
                    mCbEditPref.getServiceClassForCallBarring(mPhone),
                    null /* appId */,
                    mExpectMore,
                    mClient);
        } catch (Exception e) {
            Log.e(LOG_TAG, "getCallBarringWithExpectMore exception", e);
            sendErrorResponse();
        }
    }

    private ExtPhoneCallbackListener mExtPhoneCallbackListener = new ExtPhoneCallbackListener() {
        @Override
        public void getFacilityLockForAppResponse(Status status, int[] response) {
            Message msg = mHandler.obtainMessage(
                    CallBarringEditPreference.MyHandler.MESSAGE_GET_CALL_BARRING);
            if (status.get() == Status.SUCCESS) {
                AsyncResult.forMessage(msg, response, null);
            } else {
                AsyncResult.forMessage(
                        msg, response,
                        new CommandException(
                                CommandException.Error.GENERIC_FAILURE));
            }
            msg.sendToTarget();
        }
    };

}
