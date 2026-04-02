// QTI_BEGIN: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
/**
* Changes from Qualcomm Innovation Center, Inc. are provided under the following license:
// QTI_END: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
* Copyright (c) 2024-2025 Qualcomm Innovation Center, Inc. All rights reserved.
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
// QTI_BEGIN: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
* SPDX-License-Identifier: BSD-3-Clause-Clear
*/

// QTI_END: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
package com.android.phone;

import static com.android.phone.TimeConsumingPreferenceActivity.EXCEPTION_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.RESPONSE_ERROR;
// QTI_BEGIN: 2019-04-19: Telephony: IMS: Add error code support for CFUT failure
import static com.android.phone.TimeConsumingPreferenceActivity.RADIO_OFF_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.FDN_CHECK_FAILURE;
import static com.android.phone.TimeConsumingPreferenceActivity.STK_CC_SS_TO_DIAL_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.STK_CC_SS_TO_USSD_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.STK_CC_SS_TO_SS_ERROR;
import static com.android.phone.TimeConsumingPreferenceActivity.STK_CC_SS_TO_DIAL_VIDEO_ERROR;
// QTI_END: 2019-04-19: Telephony: IMS: Add error code support for CFUT failure

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import android.os.SystemProperties;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import android.telephony.CarrierConfigManager;
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsException;
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2019-04-19: Telephony: IMS: Add error code support for CFUT failure
import android.telephony.ims.ImsReasonInfo;
// QTI_END: 2019-04-19: Telephony: IMS: Add error code support for CFUT failure
import android.telephony.TelephonyManager;
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
// QTI_BEGIN: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
import android.widget.Toast;
// QTI_END: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable

// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import org.codeaurora.ims.QtiImsException;
import org.codeaurora.ims.QtiImsExtListenerBaseImpl;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
import org.codeaurora.ims.QtiImsExtConnector;
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
import org.codeaurora.ims.QtiImsExtManager;
import org.codeaurora.ims.utils.QtiImsExtUtils;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
import org.codeaurora.ims.QtiCallConstants;
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SsData;
// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
import com.android.internal.telephony.Phone;

// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
import com.qti.extphone.Client;
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
import com.qti.extphone.ExtPhoneCallbackListener;
// QTI_END: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
import com.qti.extphone.ExtTelephonyManager;
import com.qti.extphone.IExtPhoneCallback;
import com.qti.extphone.QtiCallForwardInfo;
import com.qti.extphone.Status;

// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
import java.util.concurrent.Executor;
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
import java.util.HashMap;
import java.util.Locale;

public class CallForwardEditPreference extends EditPhoneNumberPreference {
    private static final String LOG_TAG = "CallForwardEditPreference";
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer

    private static final String SRC_TAGS[]       = {"{0}"};

    private static final int DEFAULT_NO_REPLY_TIMER_FOR_CFNRY = 20;

    private CharSequence mSummaryOnTemplate;
    /**
     * Remembers which button was clicked by a user. If no button is clicked yet, this should have
     * {@link DialogInterface#BUTTON_NEGATIVE}, meaning "cancel".
     *
     * TODO: consider removing this variable and having getButtonClicked() in
     * EditPhoneNumberPreference instead.
     */
    private int mButtonClicked;
    private int mServiceClass;
    private MyHandler mHandler = new MyHandler();
    int reason;
    private Phone mPhone;
    CallForwardInfo callForwardInfo;
    private TimeConsumingPreferenceListener mTcpListener;
    // Should we replace CF queries containing an invalid number with "Voicemail"
    private boolean mReplaceInvalidCFNumber = false;
    private boolean mCallForwardByUssd = false;
    private CarrierXmlParser mCarrierXmlParser;
    private int mPreviousCommand = MyHandler.MESSAGE_GET_CF;
    private Object mCommandException;
    private CarrierXmlParser.SsEntry.SSAction mSsAction =
            CarrierXmlParser.SsEntry.SSAction.UNKNOWN;
    private int mAction;
    private HashMap<String, String> mCfInfo;
    private long mDelayMillisAfterUssdSet = 1000;

// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    private boolean mExpectMore;
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
    private boolean mIsCfutEnabled;
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    private boolean mAllowSetCallFwding = false;
    /*Variables which holds CFUT response data*/
    private int mStartHour;
    private int mStartMinute;
    private int mEndHour;
    private int mEndMinute;
    private int mStatus;
    private String mNumber;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
    private QtiImsExtConnector mQtiImsExtConnector;
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    private QtiImsExtManager mQtiImsExtManager;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    private ExtTelephonyManager mExtTelephonyManager;
    private Client mClient;
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    private Context mContext;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
    private Executor mExecutor;
    private QtiImsExtListenerBaseImpl mImsInterfaceListener;
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes

    public CallForwardEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSummaryOnTemplate = this.getSummaryOn();
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        mContext = context;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
        mExecutor = mContext.getMainExecutor();
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CallForwardEditPreference, 0, R.style.EditPhoneNumberPreference);
        reason = a.getInt(R.styleable.CallForwardEditPreference_reason,
                CommandsInterface.CF_REASON_UNCONDITIONAL);
        a.recycle();

// QTI_BEGIN: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
        mExtTelephonyManager = ExtTelephonyManager.getInstance(getContext());
// QTI_END: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
        mImsInterfaceListener = new QtiImsExtListenerBaseImpl(mExecutor) {
            @Override
            public void onSetCallForwardUncondTimer(int phoneId, int status) {
                if (DBG) Log.d(LOG_TAG, "onSetCallForwardTimer phoneId= " + phoneId +" status= "
                                        +status);
                try {
                    mQtiImsExtManager.getCallForwardUncondTimer(phoneId,
                            reason,
                            mServiceClass,
                            mImsInterfaceListener);
                } catch (QtiImsException e) {
                    if (DBG) Log.d(LOG_TAG, "setCallForwardUncondTimer exception! ");
                }
            }

            @Override
            public void onGetCallForwardUncondTimer(int phoneId, int startHour, int endHour,
                    int startMinute, int endMinute, int reason, int status, String number,
                    int service) {
                Log.d(LOG_TAG,"onGetCallForwardUncondTimer phoneId=" + phoneId + " startHour= "
                        + startHour + " endHour = " + endHour + "endMinute = " + endMinute
                        + "status = " + status + "number = " + number + "service= " +service);
                mStartHour = startHour;
                mStartMinute = startMinute;
                mEndHour = endHour;
                mEndMinute = endMinute;
                mStatus = status;
                mNumber = number;

                mHandler.sendMessage(mHandler.obtainMessage(mHandler.MESSAGE_GET_CFUT));
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes

// QTI_BEGIN: 2025-08-26: Telephony: Fix call forwarding icon showing issue am: 4cdf9e8923 am: 4cdf9e8923
                updateCallForwardingPreferenceForCfut(mStatus == 1, number);
// QTI_END: 2025-08-26: Telephony: Fix call forwarding icon showing issue am: 4cdf9e8923 am: 4cdf9e8923
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
            }

            @Override
            public void queryCallForwardStatusResponse(int phoneId, ImsCallForwardInfo[] cfInfoList)
            {
                Log.d(LOG_TAG, "queryCallForwardStatusResponse phoneId=" + phoneId);

                int size = cfInfoList.length;
                CallForwardInfo[] cfInfo = new CallForwardInfo[size];
                for (int i = 0; i < size; i++) {
                    cfInfo[i] = new CallForwardInfo();
                    cfInfo[i].status = cfInfoList[i].getStatus();
                    cfInfo[i].reason = cfInfoList[i].getCondition();
                    cfInfo[i].toa = cfInfoList[i].getToA();
                    cfInfo[i].number = cfInfoList[i].getNumber();
                    cfInfo[i].timeSeconds = cfInfoList[i].getTimeSeconds();

                    //Check if the service class signifies Video call forward
                    //As per 3GPP TS 29002 MAP Specification : Section 17.7.10,
                    //the BearerServiceCode for "allDataCircuitAsynchronous"
                    //is '01010000' ( i.e. 80). Hence, SERVICE_CLASS_DATA_SYNC
                    //(1<<4) and SERVICE_CLASS_PACKET (1<<6) together make
                    //video service class.

                    if (cfInfoList[i].getServiceClass() ==
                                    (CommandsInterface.SERVICE_CLASS_DATA_SYNC +
                            CommandsInterface.SERVICE_CLASS_PACKET)) {
                        cfInfo[i].serviceClass = cfInfoList[i].getServiceClass();
                    } else {
                        cfInfo[i].serviceClass = CommandsInterface.SERVICE_CLASS_VOICE;
                    }

                    updateCallForwardingPreference(cfInfo[i]);
                }

                Message msg = mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                        // unused in this case
                        CommandsInterface.CF_ACTION_DISABLE, MyHandler.MESSAGE_GET_CF, null);
                AsyncResult.forMessage(msg, cfInfo, null);
                msg.sendToTarget();
            }

            @Override
            public void onUTReqFailed(int phoneId, int errCode, String errString) {
                if (DBG) Log.d(LOG_TAG, "onUTReqFailed phoneId=" + phoneId + " errCode= "
                        + errCode + "errString =" + errString);
                Message msg;
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL && mIsCfutEnabled) {
                    msg = mHandler.obtainMessage(mHandler.MESSAGE_GET_UT_FAILED);
                    msg.arg1 = errCode;
                    msg.sendToTarget();
                } else {
                    if (errCode == ImsReasonInfo.CODE_LOCAL_CALL_CS_RETRY_REQUIRED) {
                        queryCallForwardStatus();
                    } else {
                        msg = mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                                // unused in this case
                                CommandsInterface.CF_ACTION_DISABLE, MyHandler.MESSAGE_GET_CF,
                                null);
                        if (errCode == QtiCallConstants.CODE_UT_CF_SERVICE_NOT_REGISTERED) {
                            AsyncResult.forMessage(msg, null,
                                    new QtiImsException("Service Not Registered", errCode));
                        } else {
                            AsyncResult.forMessage(msg, null,
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
                                                   QtiPhoneUtilsHelper.getCommandException(errCode));
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
                        }
                        msg.sendToTarget();
                    }
                }

            }
        };
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes

        Log.d(LOG_TAG, "mServiceClass=" + mServiceClass + ", reason=" + reason);
    }

    public CallForwardEditPreference(Context context) {
        this(context, null);
    }

// QTI_BEGIN: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
    void init(TimeConsumingPreferenceListener listener, Phone phone,
// QTI_END: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
            boolean replaceInvalidCFNumber, int serviceClass, boolean callForwardByUssd) {
        mPhone = phone;
        mTcpListener = listener;
        mReplaceInvalidCFNumber = replaceInvalidCFNumber;
// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        mServiceClass = serviceClass;
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
        mCallForwardByUssd = callForwardByUssd;
        Log.d(LOG_TAG,
                "init :mReplaceInvalidCFNumber " + mReplaceInvalidCFNumber + ", mCallForwardByUssd "
                        + mCallForwardByUssd);
        if (mCallForwardByUssd) {
            mCfInfo = new HashMap<String, String>();
            TelephonyManager telephonyManager = new TelephonyManager(getContext(),
                    phone.getSubId());
            mCarrierXmlParser = new CarrierXmlParser(getContext(),
                    telephonyManager.getSimCarrierId());
        }
    }

// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
    private void createQtiImsExtConnector(Context context) {
        try {
            mQtiImsExtConnector = new QtiImsExtConnector(context,
                    new QtiImsExtConnector.IListener() {
                        @Override
                        public void onConnectionAvailable(QtiImsExtManager qtiImsExtManager) {
                            Log.i(LOG_TAG, "QtiImsExtConnector onConnectionAvailable");
                            mQtiImsExtManager = qtiImsExtManager;
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
                            queryImsCallForwardStatus();
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
                        }
                        @Override
                        public void onConnectionUnavailable() {
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                            Log.i(LOG_TAG, "QtiImsExtConnector onConnectionUnavailable");
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
                            mQtiImsExtManager = null;
                            //QtiImsExtManager is not available so set
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                            //mIsCfutEnabled to false so that no Timer related operations will hit
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
                            //and remove spinner.
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                            mIsCfutEnabled = false;
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
                            mTcpListener.onFinished(CallForwardEditPreference.this, false);
                        }
                    });
        } catch (QtiImsException e) {
            Log.e(LOG_TAG, "Unable to create QtiImsExtConnector");
        }
    }

    public void deInit() {
        if (mQtiImsExtConnector != null) {
            mQtiImsExtConnector.disconnect();
            mQtiImsExtConnector = null;
            mQtiImsExtManager = null;
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
            mIsCfutEnabled = false;
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
        }
// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
        mExtTelephonyManager.unregisterCallback(mExtPhoneCallbackListener);
// QTI_END: 2024-06-17: Telephony: Unregister to ExtPhoneCallback
// QTI_BEGIN: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
    }

// QTI_END: 2019-02-01: Telephony: IMS: decouple ims-ext-common from boot jars
// QTI_BEGIN: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
    private boolean isUtUnavailableForVideoCallForward() {
        return !mPhone.isUtEnabled() && (mServiceClass == CommandsInterface.SERVICE_CLASS_DATA_SYNC
                + CommandsInterface.SERVICE_CLASS_PACKET);
    }

// QTI_END: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
// QTI_BEGIN: 2021-06-22: Telephony: IMS: Move deInit CallForwardEditPreference to onDestroy.
    //Used to check if CFUT(CFU with timer) is supported
// QTI_END: 2021-06-22: Telephony: IMS: Move deInit CallForwardEditPreference to onDestroy.
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
    private boolean shouldCfutEnabled() {
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        //Timer is enabled only when UT services are enabled
        CarrierConfigManager cfgManager = (CarrierConfigManager)
                mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        return (SystemProperties.getBoolean("persist.radio.ims.cmcc", false)
                || (cfgManager != null) ?
                cfgManager.getConfigForSubId(mPhone.getSubId())
                    .getBoolean("config_enable_cfu_time") : false)
                && mPhone.isUtEnabled();
    }

// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
    int getStartHour() {
        return mStartHour;
    }

    int getStartMinute() {
        return mStartMinute;
    }

    int getEndHour() {
        return mEndHour;
    }

    int getEndMinute() {
        return mEndMinute;
    }

    int getCfutStatus() {
        return mStatus;
    }

    String getCfutNumber() {
        return mNumber;
    }

    boolean isCfutEnabled() {
        return mIsCfutEnabled;
    }

    void restoreCallCallForwardTimerInfo(int startHour, int startMinute, int endHour,
            int endMinute, int status, String number, boolean shouldCfutEnabled) {
        if (mQtiImsExtConnector == null) {
            createQtiImsExtConnector(mContext);
            //Connect will get the QtiImsExtManager instance.
            mQtiImsExtConnector.connect();
        }
        mStartHour = startHour;
        mStartMinute = startMinute;
        mEndHour = endHour;
        mEndMinute = endMinute;
        mStatus = status;
        mNumber = number;
        mIsCfutEnabled = shouldCfutEnabled;
        reason = CommandsInterface.CF_REASON_UNCONDITIONAL;
        setTimeSettingVisibility(true);
        handleCallForwardTimerResult();
        updateSummaryText();
    }

// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
    void restoreCallForwardInfo(CallForwardInfo cf) {
        handleCallForwardResult(cf);
        updateSummaryText();
    }

// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    void setExpectMore(boolean expectMore) {
        mExpectMore = expectMore;
    }

// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    @Override
    protected void onBindDialogView(View view) {
        // default the button clicked to be the cancel button.
        mButtonClicked = DialogInterface.BUTTON_NEGATIVE;
        super.onBindDialogView(view);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mButtonClicked = which;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        Log.d(LOG_TAG, "mButtonClicked=" + mButtonClicked + ", positiveResult=" + positiveResult);
// QTI_BEGIN: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
        if (isUtUnavailableForVideoCallForward()) {
            Toast.makeText(mContext, R.string.ut_unavailable_to_set_video_cf_toast,
                    Toast.LENGTH_SHORT).show();
            return;
        }
// QTI_END: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
        // Ignore this event if the user clicked the cancel button, or if the dialog is dismissed
        // without any button being pressed (back button press or click event outside the dialog).
        if (isUnknownStatus() && this.mButtonClicked != DialogInterface.BUTTON_NEGATIVE) {
            int action = (mButtonClicked == DialogInterface.BUTTON_POSITIVE) ?
                CommandsInterface.CF_ACTION_REGISTRATION :
                CommandsInterface.CF_ACTION_DISABLE;
            final String number = (action == CommandsInterface.CF_ACTION_DISABLE) ?
                    "" : getPhoneNumber();

            Log.d(LOG_TAG, "reason=" + reason + ", action=" + action + ", number=" + number);

            // Display no forwarding number while we're waiting for confirmation.
            setSummaryOff("");

            mPhone.setCallForwardingOption(action,
                    reason,
                    number,
                    mServiceClass,
                    0,
                    mHandler.obtainMessage(MyHandler.MESSAGE_SET_CF,
                        action,
                        MyHandler.MESSAGE_SET_CF));
        } else if (this.mButtonClicked != DialogInterface.BUTTON_NEGATIVE) {
            int action = (isToggled() || (mButtonClicked == DialogInterface.BUTTON_POSITIVE)) ?
                    CommandsInterface.CF_ACTION_REGISTRATION :
                    CommandsInterface.CF_ACTION_DISABLE;
            int time = 0;
            if (reason == CommandsInterface.CF_REASON_NO_REPLY) {
                PersistableBundle carrierConfig = PhoneGlobals.getInstance()
                        .getCarrierConfigForSubId(mPhone.getSubId());
                if (carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_SUPPORT_NO_REPLY_TIMER_FOR_CFNRY_BOOL, true)) {
                    // Get timer value from carrier config
                    time = carrierConfig.getInt(
                            CarrierConfigManager.KEY_NO_REPLY_TIMER_FOR_CFNRY_SEC_INT,
                            DEFAULT_NO_REPLY_TIMER_FOR_CFNRY);
                }
            }
            final String number = getPhoneNumber();

            Log.d(LOG_TAG, "callForwardInfo=" + callForwardInfo);
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
            final int editStartHour = isAllDayChecked()? 0 : getStartTimeHour();
            final int editStartMinute = isAllDayChecked()? 0 : getStartTimeMinute();
            final int editEndHour = isAllDayChecked()? 0 : getEndTimeHour();
            final int editEndMinute = isAllDayChecked()? 0 : getEndTimeMinute();
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer

// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
            boolean isCFSettingChanged = true;
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
            if (action == CommandsInterface.CF_ACTION_REGISTRATION
                    && callForwardInfo != null
                    && callForwardInfo.status == 1
                    && number.equals(callForwardInfo.number)) {
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL){
                    // need to check if the time period for CFUT is changed
                    if (isAllDayChecked()){
                        isCFSettingChanged = isTimerValid();
                    } else {
                        isCFSettingChanged = mStartHour != editStartHour
                                || mStartMinute != editStartMinute
                                || mEndHour != editEndHour
                                || mEndMinute != editEndMinute;
                    }
                } else {
                    // no change, do nothing
                    if (DBG) Log.d(LOG_TAG, "no change, do nothing");
                    isCFSettingChanged = false;
                }
            }
            if (DBG) Log.d(LOG_TAG, "isCFSettingChanged = " + isCFSettingChanged);
            if (isCFSettingChanged) {
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                // set to network
                Log.d(LOG_TAG, "reason=" + reason + ", action=" + action
                        + ", number=" + number);

                // Display no forwarding number while we're waiting for
                // confirmation
                setSummaryOn("");

                // the interface of Phone.setCallForwardingOption has error:
                // should be action, reason...
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                        && !isAllDayChecked() && mIsCfutEnabled
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                        && (action != CommandsInterface.CF_ACTION_DISABLE)) {

                    Log.d(LOG_TAG, "setCallForwardingUncondTimerOption,"
                                                +"starthour = " + editStartHour
                                                + "startminute = " + editStartMinute
                                                + "endhour = " + editEndHour
                                                + "endminute = " + editEndMinute);
                    try {
                        mQtiImsExtManager.setCallForwardUncondTimer(mPhone.getPhoneId(),
                                editStartHour,
                                editStartMinute,
                                editEndHour,
                                editEndMinute,
                                action,
                                reason,
                                mServiceClass,
                                number,
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
                                mImsInterfaceListener);
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                    } catch (QtiImsException e) {
                        Log.d(LOG_TAG, "setCallForwardUncondTimer exception!" +e);
                    }
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2018-08-01: Telephony: IMS: Dismiss dialog for BUSY_SAVING_DIALOG after set CFUT failed
                    mAllowSetCallFwding = true;
// QTI_END: 2018-08-01: Telephony: IMS: Dismiss dialog for BUSY_SAVING_DIALOG after set CFUT failed
// QTI_BEGIN: 2019-04-12: Telephony: Fix conflict after AOSP update
                } else if (!mCallForwardByUssd) {
                    // the interface of Phone.setCallForwardingOption has error:
                    // should be action, reason...
// QTI_END: 2019-04-12: Telephony: Fix conflict after AOSP update
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                    mPhone.setCallForwardingOption(action,
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                            reason,
                            number,
                            mServiceClass,
                            time,
                            mHandler.obtainMessage(MyHandler.MESSAGE_SET_CF,
                                    action,
                                    MyHandler.MESSAGE_SET_CF));
                } else {
                    if (action == CommandsInterface.CF_ACTION_REGISTRATION) {
                        mCfInfo.put(CarrierXmlParser.TAG_ENTRY_NUMBER, number);
                        mCfInfo.put(CarrierXmlParser.TAG_ENTRY_TIME, Integer.toString(time));
                    } else {
                        mCfInfo.clear();
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(mHandler.MESSAGE_SET_CF_USSD,
                            action, MyHandler.MESSAGE_SET_CF));
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                }
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                if (mTcpListener != null) {
                    mTcpListener.onStarted(this, false);
                }
            }
        }
    }

// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    void handleCallForwardTimerResult() {
        setToggled(mStatus == 1);
        setPhoneNumber(mNumber);
        /*Setting Timer*/
        if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL) {
            setAllDayCheckBox(!(mStatus == 1 && isTimerValid()));
            //set timer info even all be zero
            setPhoneNumberWithTimePeriod(mNumber, mStartHour, mStartMinute, mEndHour, mEndMinute);
        }
    }

// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    void handleCallForwardResult(CallForwardInfo cf) {
        callForwardInfo = cf;
        Log.d(LOG_TAG, "handleGetCFResponse done, callForwardInfo=" + callForwardInfo);
        // In some cases, the network can send call forwarding URIs for voicemail that violate the
        // 3gpp spec. This can cause us to receive "numbers" that are sequences of letters. In this
        // case, we must detect these series of characters and replace them with "Voicemail".
        // PhoneNumberUtils#formatNumber returns null if the number is not valid.
        if (mReplaceInvalidCFNumber && !TextUtils.isEmpty(callForwardInfo.number)
                && (PhoneNumberUtils.formatNumber(callForwardInfo.number, getCurrentCountryIso())
                == null)) {
            callForwardInfo.number = getContext().getString(R.string.voicemail);
            Log.i(LOG_TAG, "handleGetCFResponse: Overridding CF number");
        }

// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        if (DBG) Log.d(LOG_TAG, "handleGetCFResponse done, callForwardInfo=" + callForwardInfo);
        if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL) {
            mStartHour = 0;
            mStartMinute = 0;
            mEndHour = 0;
            mEndMinute = 0;
        }
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        setUnknownStatus(callForwardInfo.status == CommandsInterface.SS_STATUS_UNKNOWN);
        setToggled(callForwardInfo.status == 1);
        boolean displayVoicemailNumber = false;
        if (TextUtils.isEmpty(callForwardInfo.number)) {
            PersistableBundle carrierConfig =
                    PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
            if (carrierConfig != null) {
                displayVoicemailNumber = carrierConfig.getBoolean(CarrierConfigManager
                        .KEY_DISPLAY_VOICEMAIL_NUMBER_AS_DEFAULT_CALL_FORWARDING_NUMBER_BOOL);
                Log.d(LOG_TAG, "display voicemail number as default");
            }
        }
        String voicemailNumber = mPhone.getVoiceMailNumber();
        setPhoneNumber(displayVoicemailNumber ? voicemailNumber : callForwardInfo.number);
    }

    /**
     * Starts the Call Forwarding Option query to the network and calls
     * {@link TimeConsumingPreferenceListener#onStarted}. Will call
     * {@link TimeConsumingPreferenceListener#onFinished} when finished, or
     * {@link TimeConsumingPreferenceListener#onError} if an error has occurred.
     */
    void startCallForwardOptionsQuery() {
        if (!mCallForwardByUssd) {
// QTI_BEGIN: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
            if (isUtUnavailableForVideoCallForward()) {
                Log.d(LOG_TAG, "Video CF query cannot be triggered due to UT is false now");
                return;
            }
// QTI_END: 2019-05-31: Telephony: IMS: Pop-up message for user after set video CF if UT is unavailable
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
            mIsCfutEnabled = shouldCfutEnabled();
            Log.d(LOG_TAG, "shouldCfutEnabled=" + mIsCfutEnabled);
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
            if (mPhone != null &&  mPhone.isUtEnabled()) {
                if (mQtiImsExtConnector == null) {
                    createQtiImsExtConnector(mContext);
                    //Connect will get the QtiImsExtManager instance.
                    mQtiImsExtConnector.connect();
                } else {
                    queryImsCallForwardStatus();
                }
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
            } else {
// QTI_END: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
                if (mPhone.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM &&
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
                        QtiPhoneUtilsHelper.isBacktoBackSSFeatureSupported()) {
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
                    queryCallForwardStatus();
                } else {
                    mPhone.getCallForwardingOption(reason, mServiceClass,
                            mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                            // unused in this case
                            CommandsInterface.CF_ACTION_DISABLE,
                            MyHandler.MESSAGE_GET_CF, null));
                }
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
            }
// QTI_END: 2019-03-13: Telephony: Avoid to send 2 PUT requests to network when SS service is activated from UI
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(mHandler.MESSAGE_GET_CF_USSD,
                    // unused in this case
                    CommandsInterface.CF_ACTION_DISABLE, MyHandler.MESSAGE_GET_CF, null));
        }
        if (mTcpListener != null) {
            mTcpListener.onStarted(this, true);
        }
    }

// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
    private boolean isCfQueryBlockedByFdn() {
        if (mPhone == null) {
            return false;
        }
        SsData.ServiceType serviceType = GsmMmiCode.cfReasonToServiceType(reason);
// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
        return QtiPhoneUtilsHelper.isRequestBlockedByFdn(SsData.RequestType.SS_INTERROGATION,
                serviceType, mPhone.getPhoneId(), getContext());
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
    }

// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    private void queryCallForwardStatus() {
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
        if (isCfQueryBlockedByFdn()) {
            Log.d(LOG_TAG, "queryCallForwardStatus blocked by FDN check");
            sendErrorResponse(CommandException.Error.FDN_CHECK_FAILURE);
            return;
        }

// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
        if (!mExtTelephonyManager.isServiceConnected()) {
            sendErrorResponse();
            return;
        }

        try {
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
            int[] events = new int[] {};
            mClient = mExtTelephonyManager.registerCallbackWithEvents(
                    mContext.getPackageName(), mExtPhoneCallbackListener, events);
// QTI_END: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
            mExtTelephonyManager.queryCallForwardStatus(mPhone.getPhoneId(), reason,
                    mServiceClass, null /*number*/, mExpectMore,
                    mClient);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception " + e);
            sendErrorResponse();
        }
    }

    private void sendErrorResponse() {
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
        sendErrorResponse(CommandException.Error.GENERIC_FAILURE);
    }

    private void sendErrorResponse(CommandException.Error err) {
// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
        Message msg = mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                // unused in this case
                CommandsInterface.CF_ACTION_DISABLE, MyHandler.MESSAGE_GET_CF, null);
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
        AsyncResult.forMessage(msg, null, new CommandException(err));
// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
        msg.sendToTarget();
    }

// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
    private ExtPhoneCallbackListener mExtPhoneCallbackListener = new ExtPhoneCallbackListener() {
// QTI_END: 2023-01-09: Telephony: FR84002: Re-design ExtTelephonyManager interface
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
        @Override
        public void queryCallForwardStatusResponse(Status status, QtiCallForwardInfo[] infos) {
            Message msg = mHandler.obtainMessage(MyHandler.MESSAGE_GET_CF,
                    // unused in this case
                    CommandsInterface.CF_ACTION_DISABLE, MyHandler.MESSAGE_GET_CF, null);
            if (status.get() == Status.SUCCESS) {
                CallForwardInfo[] cfInfo = new CallForwardInfo[infos.length];
                for (int i = 0; i < infos.length; i++) {
                    cfInfo[i] = new CallForwardInfo();
                    cfInfo[i].status = infos[i].status;
                    cfInfo[i].reason = infos[i].reason;
                    cfInfo[i].serviceClass = infos[i].serviceClass;
                    cfInfo[i].toa = infos[i].toa;
                    cfInfo[i].number = infos[i].number;
                    cfInfo[i].timeSeconds = infos[i].timeSeconds;
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2022-01-17: Telephony: Update call forward preference

                    updateCallForwardingPreference(cfInfo[i]);
// QTI_END: 2022-01-17: Telephony: Update call forward preference
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
                }
                AsyncResult.forMessage(msg, cfInfo, null);
            } else {
                AsyncResult.forMessage(msg, null,
                        new CommandException(CommandException.Error.GENERIC_FAILURE));
            }
            msg.sendToTarget();
        }
    };

    private void queryImsCallForwardStatus() {
// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
// QTI_BEGIN: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
        if (isCfQueryBlockedByFdn()) {
            Log.d(LOG_TAG, "queryImsCallForwardStatus blocked by FDN check");
            sendErrorResponse(CommandException.Error.FDN_CHECK_FAILURE);
            return;
        }
// QTI_END: 2023-03-16: Telephony: Enable telephony FDN check for side car SS requests.
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
        if (mQtiImsExtManager != null) {
            try {
                if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL &&
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                        mIsCfutEnabled) {
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
                    setTimeSettingVisibility(true);
                    mQtiImsExtManager.getCallForwardUncondTimer(mPhone.getPhoneId(),
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
                            reason, mServiceClass, mImsInterfaceListener);
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
                } else {
                    mQtiImsExtManager.queryCallForwardStatus(mPhone.getPhoneId(),
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
                            reason, mServiceClass, mExpectMore, mImsInterfaceListener);
// QTI_END: 2025-01-10: Telephony: FR104165 - IMS Enhancements: Sidecar Threading Enhancement Telephony Changes
// QTI_BEGIN: 2021-12-15: Telephony: Query call forward status properly
                }
            } catch (QtiImsException e){
                Log.d(LOG_TAG, "queryCallForwardStatus failed. " +
                        "Exception = " + e);
                sendErrorResponse();
            }
// QTI_END: 2021-12-15: Telephony: Query call forward status properly
// QTI_BEGIN: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
        }
    }

// QTI_END: 2021-05-27: Telephony: Add CallForwarding and CallBarring expectMore support.
    private void updateSummaryText() {
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        if (DBG) Log.d(LOG_TAG, "updateSummaryText, complete fetching for reason " + reason);
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
        if (isToggled()) {
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
            String number = getRawPhoneNumber();
            if (reason == CommandsInterface.CF_REASON_UNCONDITIONAL
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
// QTI_BEGIN: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
                    && mIsCfutEnabled && isTimerValid()){
// QTI_END: 2024-04-23: Telephony: IMS-UT: Save/restore InstanceState during language changed for CFUT
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
                number = getRawPhoneNumberWithTime();
            }
// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
            if (number != null && number.length() > 0) {
                // Wrap the number to preserve presentation in RTL languages.
                String wrappedNumber = BidiFormatter.getInstance().unicodeWrap(
                        number, TextDirectionHeuristics.LTR);
                String values[] = { wrappedNumber };
                String summaryOn = String.valueOf(
                        TextUtils.replace(mSummaryOnTemplate, SRC_TAGS, values));
                int start = summaryOn.indexOf(wrappedNumber);

                SpannableString spannableSummaryOn = new SpannableString(summaryOn);
                PhoneNumberUtils.addTtsSpan(spannableSummaryOn,
                        start, start + wrappedNumber.length());
                setSummaryOn(spannableSummaryOn);
            } else {
                setSummaryOn(getContext().getString(R.string.sum_cfu_enabled_no_number));
            }
        }

    }

// QTI_BEGIN: 2022-01-17: Telephony: Update call forward preference
    private void updateCallForwardingPreference(CallForwardInfo cfInfo) {
        if (cfInfo == null || cfInfo.reason != CommandsInterface.CF_REASON_UNCONDITIONAL) {
            return;
        }

        if (cfInfo.serviceClass == (CommandsInterface.SERVICE_CLASS_DATA_SYNC +
                CommandsInterface.SERVICE_CLASS_PACKET)) {
            mPhone.setVideoCallForwardingPreference(cfInfo.status == 1);
            mPhone.notifyCallForwardingIndicator();
        } else {
            mPhone.setVoiceCallForwardingFlag(1, (cfInfo.status == 1), cfInfo.number);
        }
    }

// QTI_END: 2022-01-17: Telephony: Update call forward preference
// QTI_BEGIN: 2025-08-26: Telephony: Fix call forwarding icon showing issue am: 4cdf9e8923 am: 4cdf9e8923
    private void updateCallForwardingPreferenceForCfut(boolean enable, String number) {
        // The expected behavior by design is
        // 1. if CFUT is enabled (isTimerValid == true), shall not show CF icon.
        // 2. if CFUT is not enabled ((isTimerValid == false)), CF icon showing depends on
        // the actual CF status queried from network.
        mPhone.setVoiceCallForwardingFlag(1, isTimerValid() ? false : enable, number);
// QTI_END: 2025-08-26: Telephony: Fix call forwarding icon showing issue am: 4cdf9e8923 am: 4cdf9e8923
    }

    /**
     * @return The ISO 3166-1 two letters country code of the country the user is in based on the
     *      network location.
     */
    private String getCurrentCountryIso() {
        final TelephonyManager telephonyManager =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return "";
        }
        return telephonyManager.getNetworkCountryIso().toUpperCase(Locale.ROOT);
    }

// QTI_BEGIN: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed
    private void handleUtReqFailed(int errCode) {
        if (mAllowSetCallFwding) {
            mTcpListener.onFinished(CallForwardEditPreference.this, false);
            mAllowSetCallFwding = false;
        } else {
            mTcpListener.onFinished(CallForwardEditPreference.this, true);
        }
        int error = RESPONSE_ERROR;
        if (errCode == ImsReasonInfo.CODE_FDN_BLOCKED) {
            error = FDN_CHECK_FAILURE;
        } else if (errCode == ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_DIAL) {
            error = STK_CC_SS_TO_DIAL_ERROR;
        } else if (errCode == ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_DIAL_VIDEO) {
            error = STK_CC_SS_TO_DIAL_VIDEO_ERROR;
        } else if(errCode == ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_USSD) {
            error = STK_CC_SS_TO_USSD_ERROR;
        } else if (errCode == ImsReasonInfo.CODE_UT_SS_MODIFIED_TO_SS) {
            error = STK_CC_SS_TO_SS_ERROR;
        } else if (errCode == ImsReasonInfo.CODE_RADIO_OFF) {
            error = RADIO_OFF_ERROR;
        }
        mTcpListener.onError(CallForwardEditPreference.this, error);
    }

// QTI_END: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed
// QTI_BEGIN: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    private void handleGetCFTimerResponse() {
        if (mAllowSetCallFwding) {
            mTcpListener.onFinished(CallForwardEditPreference.this, false);
            mAllowSetCallFwding = false;
        } else {
            mTcpListener.onFinished(CallForwardEditPreference.this, true);
        }
        handleCallForwardTimerResult();
        updateSummaryText();
    }

    //used to check if timer infor is valid
    private boolean isTimerValid() {
        return mStartHour != 0 || mStartMinute != 0 || mEndHour != 0 || mEndMinute != 0;
    }

// QTI_END: 2018-03-26: Telephony: IMS: Call forward unconditional timer
    // Message protocol:
    // what: get vs. set
    // arg1: action -- register vs. disable
    // arg2: get vs. set for the preceding request
    private class MyHandler extends Handler {
        static final int MESSAGE_GET_CF = 0;
        static final int MESSAGE_SET_CF = 1;
        static final int MESSAGE_GET_CF_USSD = 2;
        static final int MESSAGE_SET_CF_USSD = 3;
// QTI_BEGIN: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
        static final int MESSAGE_GET_CFUT = 4;
// QTI_END: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
// QTI_BEGIN: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed
        static final int MESSAGE_GET_UT_FAILED = 5;
// QTI_END: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed

        TelephonyManager.UssdResponseCallback mUssdCallback =
                new TelephonyManager.UssdResponseCallback() {
                    @Override
                    public void onReceiveUssdResponse(final TelephonyManager telephonyManager,
                            String request, CharSequence response) {
                        if (mSsAction == CarrierXmlParser.SsEntry.SSAction.UNKNOWN) {
                            return;
                        }

                        HashMap<String, String> analysisResult = mCarrierXmlParser.getFeature(
                                CarrierXmlParser.FEATURE_CALL_FORWARDING)
                                .getResponseSet(mSsAction,
                                        response.toString());

                        Throwable throwableException = null;
                        if (analysisResult.get(CarrierXmlParser.TAG_RESPONSE_STATUS_ERROR)
                                != null) {
                            throwableException = new CommandException(
                                    CommandException.Error.GENERIC_FAILURE);
                        }

                        Object obj = null;
                        if (mSsAction == CarrierXmlParser.SsEntry.SSAction.QUERY) {
                            obj = makeCallForwardInfo(analysisResult);
                        }

                        sendCfMessage(obj, throwableException);
                    }

                    @Override
                    public void onReceiveUssdResponseFailed(final TelephonyManager telephonyManager,
                            String request, int failureCode) {
                        Log.d(LOG_TAG, "receive the ussd result failed");
                        Throwable throwableException = new CommandException(
                                CommandException.Error.GENERIC_FAILURE);
                        sendCfMessage(null, throwableException);
                    }
                };

        @Override
        public void handleMessage(Message msg) {
// QTI_BEGIN: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
            Log.i(LOG_TAG, "handleMessage : " + msg.what);
// QTI_END: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
            switch (msg.what) {
                case MESSAGE_GET_CF:
                    handleGetCFResponse(msg);
                    break;
                case MESSAGE_SET_CF:
                    handleSetCFResponse(msg);
                    break;
                case MESSAGE_GET_CF_USSD:
                    prepareUssdCommand(msg, CarrierXmlParser.SsEntry.SSAction.QUERY);
                    break;
                case MESSAGE_SET_CF_USSD:
                    prepareUssdCommand(msg, CarrierXmlParser.SsEntry.SSAction.UNKNOWN);
                    break;
// QTI_BEGIN: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
                case MESSAGE_GET_CFUT:
                    handleGetCFTimerResponse();
                    break;
// QTI_END: 2020-03-30: Telephony: IMS: Use handler to update CFUT UI
// QTI_BEGIN: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed
                case MESSAGE_GET_UT_FAILED:
                    handleUtReqFailed(msg.arg1);
                    break;
// QTI_END: 2020-07-13: Telephony: IMS: Use handler to update UI thread when UT failed
            }
        }

        private void handleGetCFResponse(Message msg) {
            Log.d(LOG_TAG, "handleGetCFResponse: done");

            mTcpListener.onFinished(CallForwardEditPreference.this, msg.arg2 != MESSAGE_SET_CF);

            AsyncResult ar = (AsyncResult) msg.obj;

            callForwardInfo = null;
            boolean summaryOff = false;
            if (ar.exception != null) {
                Log.d(LOG_TAG, "handleGetCFResponse: ar.exception=" + ar.exception);
                if (ar.exception instanceof CommandException) {
                    mTcpListener.onException(CallForwardEditPreference.this,
                            (CommandException) ar.exception);
// QTI_BEGIN: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
                } else if (ar.exception instanceof QtiImsException) {
                    mTcpListener.onError(CallForwardEditPreference.this,
                            ((QtiImsException) ar.exception).getCode());
// QTI_END: 2021-11-29: Telephony: Revert " Revert "Support standalone Call Forwarding ..."
                } else {
                    // Most likely an ImsException and we can't handle it the same way as
                    // a CommandException. The best we can do is to handle the exception
                    // the same way as mTcpListener.onException() does when it is not of type
                    // FDN_CHECK_FAILURE.
                    mTcpListener.onError(CallForwardEditPreference.this, EXCEPTION_ERROR);
                }
            } else {
                if (ar.userObj instanceof Throwable) {
                    mTcpListener.onError(CallForwardEditPreference.this, RESPONSE_ERROR);
                }
                CallForwardInfo cfInfoArray[] = (CallForwardInfo[]) ar.result;
                if (cfInfoArray == null || cfInfoArray.length == 0) {
                    Log.d(LOG_TAG, "handleGetCFResponse: cfInfoArray.length==0");
                    if (!(ar.userObj instanceof Throwable)) {
                        mTcpListener.onError(CallForwardEditPreference.this, RESPONSE_ERROR);
                    }
                } else {
                    for (int i = 0, length = cfInfoArray.length; i < length; i++) {
                        Log.d(LOG_TAG, "handleGetCFResponse, cfInfoArray[" + i + "]="
                                + cfInfoArray[i]);
                        if ((mServiceClass & cfInfoArray[i].serviceClass) != 0) {
                            // corresponding class
                            CallForwardInfo info = cfInfoArray[i];
                            handleCallForwardResult(info);

                            summaryOff = (info.status == CommandsInterface.SS_STATUS_UNKNOWN);

                            if (ar.userObj instanceof Throwable) {
                                Log.d(LOG_TAG, "Skipped duplicated error dialog");
                                continue;
                            }

                            // Show an alert if we got a success response but
                            // with unexpected values.
                            // Handle the fail-to-disable case.
                            if (msg.arg2 == MESSAGE_SET_CF &&
                                    msg.arg1 == CommandsInterface.CF_ACTION_DISABLE &&
                                    info.status == 1) {
                                // Skip showing error dialog since some operators return
                                // active status even if disable call forward succeeded.
                                // And they don't like the error dialog.
                                if (isSkipCFFailToDisableDialog()) {
                                    Log.d(LOG_TAG, "Skipped Callforwarding fail-to-disable dialog");
                                    continue;
                                }
                                CharSequence s;
                                switch (reason) {
                                    case CommandsInterface.CF_REASON_BUSY:
                                        s = getContext().getText(R.string.disable_cfb_forbidden);
                                        break;
                                    case CommandsInterface.CF_REASON_NO_REPLY:
                                        s = getContext().getText(R.string.disable_cfnry_forbidden);
                                        break;
                                    default: // not reachable
                                        s = getContext().getText(R.string.disable_cfnrc_forbidden);
                                }
                                AlertDialog.Builder builder =
                                        FrameworksUtils.makeAlertDialogBuilder(getContext());
                                builder.setNeutralButton(R.string.close_dialog, null);
                                builder.setTitle(getContext()
                                        .getText(R.string.error_updating_title));
                                builder.setMessage(s);
                                builder.setCancelable(true);
                                builder.create().show();
                            } else if (msg.arg2 == MESSAGE_SET_CF &&
                                    msg.arg1 == CommandsInterface.CF_ACTION_REGISTRATION &&
                                    info.status == 0) {
                                // Handle the fail-to-enable case.
                                CharSequence s = getContext()
                                    .getText(R.string.registration_cf_forbidden);
                                AlertDialog.Builder builder =
                                        FrameworksUtils.makeAlertDialogBuilder(getContext());
                                builder.setNeutralButton(R.string.close_dialog, null);
                                builder.setTitle(getContext()
                                        .getText(R.string.error_updating_title));
                                builder.setMessage(s);
                                builder.setCancelable(true);
                                builder.create().show();
                            }
                        }
                    }
                }
            }

            // Now whether or not we got a new number, reset our enabled
            // summary text since it may have been replaced by an empty
            // placeholder.
            // for CDMA, doesn't display summary.
            if (summaryOff) {
                setSummaryOff("");
            } else {
                // Now whether or not we got a new number, reset our enabled
                // summary text since it may have been replaced by an empty
                // placeholder.
                updateSummaryText();
            }
        }

        private void handleSetCFResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception != null) {
                Log.d(LOG_TAG, "handleSetCFResponse: ar.exception=" + ar.exception);
                // setEnabled(false);
            }

            if (ar.result != null) {
                int arr = (int)ar.result;
                if (arr == CommandsInterface.SS_STATUS_UNKNOWN) {
                    Log.d(LOG_TAG, "handleSetCFResponse: no need to re get in CDMA");
                    mTcpListener.onFinished(CallForwardEditPreference.this, false);
                    return;
                }
            }

            Log.d(LOG_TAG, "handleSetCFResponse: re get");
            if (!mCallForwardByUssd) {
                if (isUtUnavailableForVideoCallForward()) {
                    Log.d(LOG_TAG, "handleSetCFResponse: skip CS retry for video CF");
                    mTcpListener.onFinished(CallForwardEditPreference.this, false);
                    return;
                }

                mPhone.getCallForwardingOption(reason, mServiceClass,
                        obtainMessage(MESSAGE_GET_CF, msg.arg1, MESSAGE_SET_CF, ar.exception));
            } else {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(mHandler.MESSAGE_GET_CF_USSD,
                        msg.arg1, MyHandler.MESSAGE_SET_CF, ar.exception),
                        mDelayMillisAfterUssdSet);
            }
        }

        private void prepareUssdCommand(Message msg,
                CarrierXmlParser.SsEntry.SSAction inputSsAction) {
            mAction = msg.arg1;
            mPreviousCommand = msg.arg2;
            mCommandException = msg.obj;
            mSsAction = inputSsAction;

            if (mSsAction != CarrierXmlParser.SsEntry.SSAction.QUERY) {
                if (mAction == CommandsInterface.CF_ACTION_REGISTRATION) {
                    mSsAction = CarrierXmlParser.SsEntry.SSAction.UPDATE_ACTIVATE;
                } else {
                    mSsAction = CarrierXmlParser.SsEntry.SSAction.UPDATE_DEACTIVATE;
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendUssdCommand(mUssdCallback, mSsAction, mCfInfo.isEmpty() ? null : mCfInfo);
                }
            }).start();
        }

        private void sendUssdCommand(TelephonyManager.UssdResponseCallback inputCallback,
                CarrierXmlParser.SsEntry.SSAction inputAction,
                HashMap<String, String> inputCfInfo) {
            String newUssdCommand = mCarrierXmlParser.getFeature(
                    CarrierXmlParser.FEATURE_CALL_FORWARDING)
                    .makeCommand(inputAction, inputCfInfo);
            TelephonyManager telephonyManager =
                    (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.sendUssdRequest(newUssdCommand, inputCallback, mHandler);
        }

        private Message makeGetCfMessage(int inputMsgWhat, int inputMsgArg2, Object inputMsgObj) {
            return mHandler.obtainMessage(inputMsgWhat,
                    mAction,
                    inputMsgArg2,
                    inputMsgObj);
        }

        private Message makeSetCfMessage(int inputMsgWhat, int inputMsgArg2) {
            return mHandler.obtainMessage(inputMsgWhat,
                    mAction,
                    inputMsgArg2);
        }

        private void sendCfMessage(Object inputArObj, Throwable inputThrowableException) {
            Message message;
            if (mSsAction == CarrierXmlParser.SsEntry.SSAction.UNKNOWN) {
                return;
            }
            if (mSsAction == CarrierXmlParser.SsEntry.SSAction.QUERY) {
                message = makeGetCfMessage(MyHandler.MESSAGE_GET_CF, mPreviousCommand,
                        mCommandException);
            } else {
                message = makeSetCfMessage(MyHandler.MESSAGE_SET_CF, MyHandler.MESSAGE_SET_CF);
            }
            AsyncResult.forMessage(message, inputArObj, inputThrowableException);
            message.sendToTarget();
        }

        private CallForwardInfo[] makeCallForwardInfo(HashMap<String, String> inputInfo) {
            int tmpStatus = 0;
            String tmpNumberStr = "";
            int tmpTime = 0;
            if (inputInfo != null && inputInfo.size() != 0) {
                String tmpStatusStr = inputInfo.get(CarrierXmlParser.TAG_RESPONSE_STATUS);

                String tmpTimeStr = inputInfo.get(CarrierXmlParser.TAG_RESPONSE_TIME);
                if (!TextUtils.isEmpty(tmpStatusStr)) {
                    if (tmpStatusStr.equals(
                            CarrierXmlParser.TAG_COMMAND_RESULT_DEFINITION_ACTIVATE)) {
                        tmpStatus = 1;
                    } else if (tmpStatusStr.equals(
                            CarrierXmlParser.TAG_COMMAND_RESULT_DEFINITION_DEACTIVATE)
                            || tmpStatusStr.equals(
                            CarrierXmlParser.TAG_COMMAND_RESULT_DEFINITION_UNREGISTER)) {
                        tmpStatus = 0;
                    }
                }

                tmpNumberStr = inputInfo.get(CarrierXmlParser.TAG_RESPONSE_NUMBER);
                if (!TextUtils.isEmpty(tmpTimeStr)) {
                    tmpTime = Integer.valueOf(inputInfo.get(CarrierXmlParser.TAG_RESPONSE_TIME));
                }
            }

            CallForwardInfo[] newCallForwardInfo = new CallForwardInfo[1];
            newCallForwardInfo[0] = new CallForwardInfo();
            newCallForwardInfo[0].status = tmpStatus;
            newCallForwardInfo[0].reason = reason;
            newCallForwardInfo[0].serviceClass = mServiceClass;
            newCallForwardInfo[0].number = tmpNumberStr;
            newCallForwardInfo[0].timeSeconds = tmpTime;
            return newCallForwardInfo;
        }
    }

    /*
     * Get the config of whether skip showing CF fail-to-disable dialog
     * from carrier config manager.
     *
     * @return boolean value of the config
     */
    private boolean isSkipCFFailToDisableDialog() {
        PersistableBundle carrierConfig =
                PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());
        if (carrierConfig != null) {
            return carrierConfig.getBoolean(
                    CarrierConfigManager.KEY_SKIP_CF_FAIL_TO_DISABLE_DIALOG_BOOL);
        } else {
            // by default we should not skip
            return false;
        }
    }
}
