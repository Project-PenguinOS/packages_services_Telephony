/*
 * Copyright (C) 2008 The Android Open Source Project
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
 */

package com.android.phone;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
import android.os.RemoteException;
import android.os.ServiceManager;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
import android.telephony.TelephonyManager;
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.Phone;
// QTI_BEGIN: 2020-05-04: Telephony: 1.5 HAL version check support for sim-deperso
import com.android.internal.telephony.RIL;
// QTI_END: 2020-05-04: Telephony: 1.5 HAL version check support for sim-deperso
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization

// QTI_BEGIN: 2021-02-10: Telephony: Move IExtTelephony to IExtPhone
import com.qti.extphone.IDepersoResCallback;
// QTI_END: 2021-02-10: Telephony: Move IExtTelephony to IExtPhone
/**
 * "SIM network unlock" PIN entry screen.
 *
 * @see PhoneGlobals.EVENT_SIM_NETWORK_LOCKED
 *
 * TODO: This UI should be part of the lock screen, not the
 * phone app (see bug 1804111).
 */
public class IccNetworkDepersonalizationPanel extends IccPanel {

    /**
     * Tracks whether there is an instance of the network depersonalization dialog showing or not.
     * Ensures only a single instance of the dialog is visible.
     */
    private static boolean [] sShowingDialog =
            new boolean[TelephonyManager.getDefault().getSupportedModemCount()];

    //debug constants
    private static final boolean DBG = false;

    //events
    private static final int EVENT_ICC_NTWRK_DEPERSONALIZATION_RESULT = 100;

// QTI_BEGIN: 2020-05-29: Telephony: Add HAL version check in Handler
    //this enum value should match with error value being propagated from vendor
    private int ERROR = 1;
// QTI_END: 2020-05-29: Telephony: Add HAL version check in Handler
    private Phone mPhone;
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    private int mPersoSubtype;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
    private static IccNetworkDepersonalizationPanel [] sNdpPanel =
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
            new IccNetworkDepersonalizationPanel[
                    TelephonyManager.getDefault().getSupportedModemCount()];
    private SubscriptionInfo mSir;

    //UI elements
    private EditText     mPinEntry;
    private LinearLayout mEntryPanel;
    private LinearLayout mStatusPanel;
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    private TextView     mPersoSubtypeText;
    private PersoSubState mPersoSubState;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    private TextView     mPhoneIdText;
    private TextView     mStatusText;

    private Button       mUnlockButton;
    private Button       mDismissButton;

// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    enum statusType {
        ENTRY,
        IN_PROGRESS,
        ERROR,
        SUCCESS
    }

// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    /**
     * Shows the network depersonalization dialog, but only if it is not already visible.
     */
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
    public static void showDialog(Phone phone, int subType) {
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
        int phoneId = phone == null ? 0: phone.getPhoneId();
        if (phoneId >= sShowingDialog.length) {
            Log.e(TAG, "[IccNetworkDepersonalizationPanel] showDialog; invalid phoneId" + phoneId);
            return;
        }
        if (sShowingDialog[phoneId]) {
            Log.i(TAG, "[IccNetworkDepersonalizationPanel] - showDialog; skipped already shown.");
            return;
        }
        Log.i(TAG, "[IccNetworkDepersonalizationPanel] - showDialog; showing dialog.");
        sShowingDialog[phoneId] = true;
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
        sNdpPanel[phoneId] = new IccNetworkDepersonalizationPanel(PhoneGlobals.getInstance(),
                phone, subType);
        sNdpPanel[phoneId].show();
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    }

// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
    public static void dialogDismiss(int phoneId) {
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
        if (phoneId >= sShowingDialog.length || !SubscriptionManager.isValidPhoneId(phoneId)) {
            Log.e(TAG, "[IccNetworkDepersonalizationPanel] - dismiss; invalid phoneId " + phoneId);
            return;
        }
        if (sNdpPanel[phoneId] != null && sShowingDialog[phoneId]) {
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
            sNdpPanel[phoneId].dismiss();
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        }
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    }

    //private textwatcher to control text entry.
    private TextWatcher mPinEntryWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence buffer, int start, int olen, int nlen) {
        }

        public void onTextChanged(CharSequence buffer, int start, int olen, int nlen) {
        }

        public void afterTextChanged(Editable buffer) {
            if (SpecialCharSequenceMgr.handleChars(
                    getContext(), buffer.toString())) {
                mPinEntry.getText().clear();
            }
        }
    };

    //handler for unlock function results
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == EVENT_ICC_NTWRK_DEPERSONALIZATION_RESULT) {
// QTI_BEGIN: 2020-05-29: Telephony: Add HAL version check in Handler
                if (mPhone.getHalVersion().greaterOrEqual(RIL.RADIO_HAL_VERSION_1_5)) {
                    AsyncResult res = (AsyncResult) msg.obj;
                        if (res.exception != null) {
                            if (DBG) log("network depersonalization request failure.");
                            displayStatus(statusType.ERROR.name());
                            postDelayed(new Runnable() {
                                public void run() {
                                    hideAlert();
                                    mPinEntry.getText().clear();
                                    mPinEntry.requestFocus();
                                }
                            }, 3000);
                        } else {
                            if (DBG) log("network depersonalization success.");
                            displayStatus(statusType.SUCCESS.name());
                            postDelayed(new Runnable() {
                                public void run() {
                                    dismiss();
                                }
                            }, 3000);
                        }
                } else {
                    //DepersoResult received ERROR/SUCCESS from vendor side
                    if (msg.arg1 == ERROR) {
// QTI_END: 2020-05-29: Telephony: Add HAL version check in Handler
// QTI_BEGIN: 2020-05-04: Telephony: 1.5 HAL version check support for sim-deperso
                        if (DBG) log("network depersonalization request failure.");
                        displayStatus(statusType.ERROR.name());
                        postDelayed(new Runnable() {
                            public void run() {
                                hideAlert();
                                mPinEntry.getText().clear();
                                mPinEntry.requestFocus();
                            }
                        }, 3000);
                    } else {
                        if (DBG) log("network depersonalization success.");
                        displayStatus(statusType.SUCCESS.name());
                        postDelayed(new Runnable() {
                            public void run() {
                                dismiss();
                            }
                        }, 3000);
                    }
// QTI_END: 2020-05-04: Telephony: 1.5 HAL version check support for sim-deperso
                }
            }
// QTI_BEGIN: 2020-05-29: Telephony: Add HAL version check in Handler
        }
// QTI_END: 2020-05-29: Telephony: Add HAL version check in Handler
    };

// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    private final IDepersoResCallback mCallback = new IDepersoResCallback.Stub() {
        public void onDepersoResult(int result, int phoneId) {
           log("on deperso result received: " +result);
           Message msg = mHandler.obtainMessage(EVENT_ICC_NTWRK_DEPERSONALIZATION_RESULT, result,
                   phoneId);
           msg.sendToTarget();
        }
    };

// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    //constructor
    public IccNetworkDepersonalizationPanel(Context context) {
        super(context);
        mPhone = PhoneGlobals.getPhone();
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPersoSubtype = PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal();
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mSir = SubscriptionManager.from(context)
                .getActiveSubscriptionInfoForSimSlotIndex(mPhone.getPhoneId());
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    }

    //constructor
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
// QTI_BEGIN: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
    public IccNetworkDepersonalizationPanel(Context context, Phone phone,
            int subtype) {
// QTI_END: 2018-03-16: Telephony: P PPR1.180311.001 merge a927ba8f56c95ca3e2f3dc9ca700ed1542737bc6 - conflicts
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        super(context);
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPhone = phone == null ? PhoneGlobals.getPhone() : phone;
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPersoSubtype = subtype;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mSir = SubscriptionManager.from(context)
                .getActiveSubscriptionInfoForSimSlotIndex(mPhone.getPhoneId());
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.sim_ndp);

        // PIN entry text field
        mPinEntry = (EditText) findViewById(R.id.pin_entry);
        mPinEntry.setKeyListener(DialerKeyListener.getInstance());
        mPinEntry.setOnClickListener(mUnlockListener);

        // Attach the textwatcher
        CharSequence text = mPinEntry.getText();
        Spannable span = (Spannable) text;
        span.setSpan(mPinEntryWatcher, 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        mEntryPanel = (LinearLayout) findViewById(R.id.entry_panel);
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPersoSubtypeText = (TextView) findViewById(R.id.perso_subtype_text);
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPhoneIdText = (TextView) findViewById(R.id.perso_phoneid_text);
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        displayStatus(statusType.ENTRY.name());
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization

        mUnlockButton = (Button) findViewById(R.id.ndp_unlock);
        mUnlockButton.setOnClickListener(mUnlockListener);

        // The "Dismiss" button is present in some (but not all) products,
        // based on the "KEY_SIM_NETWORK_UNLOCK_ALLOW_DISMISS_BOOL" variable.
        mDismissButton = (Button) findViewById(R.id.ndp_dismiss);
        PersistableBundle carrierConfig = PhoneGlobals.getInstance().getCarrierConfig();
        if (carrierConfig.getBoolean(
                CarrierConfigManager.KEY_SIM_NETWORK_UNLOCK_ALLOW_DISMISS_BOOL)) {
            if (DBG) log("Enabling 'Dismiss' button...");
            mDismissButton.setVisibility(View.VISIBLE);
            mDismissButton.setOnClickListener(mDismissListener);
        } else {
            if (DBG) log("Removing 'Dismiss' button...");
            mDismissButton.setVisibility(View.GONE);
        }

        //status panel is used since we're having problems with the alert dialog.
        mStatusPanel = (LinearLayout) findViewById(R.id.status_panel);
        mStatusText = (TextView) findViewById(R.id.status_text);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "[IccNetworkDepersonalizationPanel] - showDialog; hiding dialog.");
        int phoneId = mPhone == null ? 0 : mPhone.getPhoneId();
        if (phoneId >= sShowingDialog.length) {
            Log.e(TAG, "[IccNetworkDepersonalizationPanel] - onStop; invalid phoneId " + phoneId);
            return;
        }
        sShowingDialog[phoneId] = false;
    }

    //Mirrors IccPinUnlockPanel.onKeyDown().
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    View.OnClickListener mUnlockListener = new View.OnClickListener() {
        public void onClick(View v) {
            String pin = mPinEntry.getText().toString();

            if (TextUtils.isEmpty(pin)) {
                return;
            }

            log("Requesting De-Personalization for subtype " + mPersoSubtype);
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization

            try {
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
                mPhone.getIccCard().supplySimDepersonalization(mPersoSubState,pin,
                       Message.obtain(mHandler, EVENT_ICC_NTWRK_DEPERSONALIZATION_RESULT));
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
            } catch (NullPointerException ex) {
                log("NullPointerException @supplyIccDepersonalization" + ex);
            }
            displayStatus(statusType.IN_PROGRESS.name());
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        }
    };

// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    private void displayStatus(String type) {
        int label = 0;
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization

// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        mPersoSubState = PersoSubState.values()[mPersoSubtype];
        log("displayStatus mPersoSubState: " +mPersoSubState.name() +"type: " +type);

        label = getContext().getResources().getIdentifier(mPersoSubState.name()
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
                + "_" + type, "string", "android");
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        if (label == 0) {
            log ("Unable to get the PersoSubType string");
            return;
        }

// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        if(!PersoSubState.isPersoLocked(mPersoSubState)) {
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
            log ("Unsupported Perso Subtype :" + mPersoSubState.name());
            return;
        }
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        if(mSir != null) {
            CharSequence displayName = mSir.getDisplayName();
            log("Operator displayName is: " + displayName + "phoneId: " + mPhone.getPhoneId());

            if(displayName != null && displayName != "") {
                // Displaying Operator displayName  on UI
                String phoneIdText = getContext().getString(R.string.label_phoneid)
                        + ": " + displayName;
                mPhoneIdText.setText(phoneIdText);
            }
        }

       CharSequence displayName = mSir.getDisplayName();
       log("Operator displayName is: " + displayName + "phoneId: " + mPhone.getPhoneId());

        // Displaying Operator displayName  on UI
        String phoneIdText = getContext().getString(R.string.label_phoneid)
                + ": " + displayName;
        mPhoneIdText.setText(phoneIdText);

// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        if (type == statusType.ENTRY.name()) {
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
            String displayText = getContext().getString(label);
            mPersoSubtypeText.setText(displayText);
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        } else {
            mStatusText.setText(label);
            mEntryPanel.setVisibility(View.GONE);
            mStatusPanel.setVisibility(View.VISIBLE);
        }
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    }

    private void hideAlert() {
        mEntryPanel.setVisibility(View.VISIBLE);
        mStatusPanel.setVisibility(View.GONE);
    }

    View.OnClickListener mDismissListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (DBG) log("mDismissListener: skipping depersonalization...");
            dismiss();
        }
    };

    private void log(String msg) {
// QTI_BEGIN: 2018-02-26: Telephony: * Telephony: SIM De-personalization
        Log.d(TAG, "[IccNetworkDepersonalizationPanel] " + msg);
// QTI_END: 2018-02-26: Telephony: * Telephony: SIM De-personalization
    }
}
