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
 */

// QTI_BEGIN: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
/**
* Changes from Qualcomm Innovation Center, Inc. are provided under the following license:
* Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
* SPDX-License-Identifier: BSD-3-Clause-Clear
*/

// QTI_END: 2024-06-13: Telephony: Fix call forwarding alert issue in airplane mode
package com.android.phone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
// QTI_BEGIN: 2018-02-23: Telephony: Enable proprietary MobileNetworkSettings
import android.os.ServiceManager;
// QTI_END: 2018-02-23: Telephony: Enable proprietary MobileNetworkSettings
import android.os.SystemProperties;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
import android.telecom.TelecomManager;
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
// QTI_BEGIN: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
// QTI_END: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.
import android.telephony.SubscriptionManager;
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
import android.telephony.TelephonyManager;
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.settings.SuppServicesUiUtil;
// QTI_BEGIN: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.
import com.android.services.telephony.TelecomAccountRegistry;
// QTI_END: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.
import com.android.telephony.Rlog;

import java.io.IOException;
import java.util.List;

// QTI_BEGIN: 2018-02-23: Telephony: Enable proprietary MobileNetworkSettings
import org.codeaurora.internal.IExtTelephony;

// QTI_END: 2018-02-23: Telephony: Enable proprietary MobileNetworkSettings
/**
 * Misc utilities for the Phone app.
 */
public class PhoneUtils {
    public static final String EMERGENCY_ACCOUNT_HANDLE_ID = "E";
    private static final String LOG_TAG = "PhoneUtils";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    // Do not check in with VDBG = true, since that may write PII to the system log.
    private static final boolean VDBG = false;

    // USSD string length for MMI operations
    static final int MIN_USSD_LEN = 1;
    static final int MAX_USSD_LEN = 160;

    /** Define for default vibrate pattern if res cannot be found */
    private static final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};

    /**
     * Theme to use for dialogs displayed by utility methods in this class. This is needed
     * because these dialogs are displayed using the application context, which does not resolve
     * the dialog theme correctly.
     */
    private static final int THEME = com.android.internal.R.style.Theme_DeviceDefault_Dialog_Alert;

// QTI_BEGIN: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.
    /** Extra key to identify the service class voice or video */
    public static final String SERVICE_CLASS = "service_class";
// QTI_END: 2018-03-30: Telephony: IMS: Add UT interface to query CF setting for service class.

// QTI_BEGIN: 2018-06-13: Telephony: MSIM: Emergency account handle support
    private static final int PRIMARY_STACK_MODEM_ID = 0;

// QTI_END: 2018-06-13: Telephony: MSIM: Emergency account handle support
    /** USSD information used to aggregate all USSD messages */
    private static StringBuilder sUssdMsg = new StringBuilder();

    private static final ComponentName PSTN_CONNECTION_SERVICE_COMPONENT =
            new ComponentName("com.android.phone",
                    "com.android.services.telephony.TelephonyConnectionService");

    /** This class is never instantiated. */
    private PhoneUtils() {
    }

    /**
     * Handle the MMIInitiate message and put up an alert that lets
     * the user cancel the operation, if applicable.
     *
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
     * @param phone the Phone object.
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
     * @param context context to get strings.
     * @param mmiCode the MmiCode object being started.
     * @param buttonCallbackMessage message to post when button is clicked.
     * @param previousAlert a previous alert used in this activity.
     * @return the dialog handle
     */
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
    static Dialog displayMMIInitiate(Phone phone,
                                          Context context,
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
                                          MmiCode mmiCode,
                                          Message buttonCallbackMessage,
                                          Dialog previousAlert) {
        log("displayMMIInitiate: " + Rlog.pii(LOG_TAG, mmiCode.toString()));
        if (previousAlert != null) {
            previousAlert.dismiss();
        }

        // The UI paradigm we are using now requests that all dialogs have
        // user interaction, and that any other messages to the user should
        // be by way of Toasts.
        //
        // In adhering to this request, all MMI initiating "OK" dialogs
        // (non-cancelable MMIs) that end up being closed when the MMI
        // completes (thereby showing a completion dialog) are being
        // replaced with Toasts.
        //
        // As a side effect, moving to Toasts for the non-cancelable MMIs
        // also means that buttonCallbackMessage (which was tied into "OK")
        // is no longer invokable for these dialogs.  This is not a problem
        // since the only callback messages we supported were for cancelable
        // MMIs anyway.
        //
        // A cancelable MMI is really just a USSD request. The term
        // "cancelable" here means that we can cancel the request when the
        // system prompts us for a response, NOT while the network is
        // processing the MMI request.  Any request to cancel a USSD while
        // the network is NOT ready for a response may be ignored.
        //
        // With this in mind, we replace the cancelable alert dialog with
        // a progress dialog, displayed until we receive a request from
        // the the network.  For more information, please see the comments
        // in the displayMMIComplete() method below.
        //
        // Anything that is NOT a USSD request is a normal MMI request,
        // which will bring up a toast (desribed above).

        boolean isCancelable = (mmiCode != null) && mmiCode.isCancelable();

        if (!isCancelable) {
            log("displayMMIInitiate: not a USSD code, displaying status toast.");
            CharSequence text = context.getText(R.string.mmiStarted);
            Toast.makeText(context, text, Toast.LENGTH_SHORT)
                .show();
            return null;
        } else {
            log("displayMMIInitiate: running USSD code, displaying intermediate progress.");

            // create the indeterminate progress dialog and display it.
            ProgressDialog pd = new ProgressDialog(context, THEME);
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
            if (QtiPhoneUtilsHelper.isMultiSimMode() && phone != null) {
                pd.setTitle(context.getText(R.string.ussdinitiated_title));
                PhoneAccount account = QtiPhoneUtilsHelper.getPhoneAccount(phone.getSubId());
                if (account != null && account.getIcon() != null) {
                    pd.setIcon(account.getIcon().loadDrawable(context));
                }
            }
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
            pd.setMessage(context.getText(R.string.ussdRunning));
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            pd.show();

            return pd;
        }

    }

    /**
     * Handle the MMIComplete message and fire off an intent to display
     * the message.
     *
     * @param context context to get strings.
     * @param mmiCode MMI result.
     * @param previousAlert a previous alert used in this activity.
     */
    static void displayMMIComplete(final Phone phone, Context context, final MmiCode mmiCode,
            Message dismissCallbackMessage,
            AlertDialog previousAlert) {
        final PhoneGlobals app = PhoneGlobals.getInstance();
        CharSequence text;
        int title = 0;  // title for the progress dialog, if needed.
        MmiCode.State state = mmiCode.getState();

        log("displayMMIComplete: state=" + state);

        switch (state) {
            case PENDING:
                // USSD code asking for feedback from user.
                text = mmiCode.getMessage();
                log("displayMMIComplete: using text from PENDING MMI message: '" + text + "'");
                break;
            case CANCELLED:
                text = null;
                break;
            case COMPLETE:
                PersistableBundle b = null;
                if (SubscriptionManager.isValidSubscriptionId(phone.getSubId())) {
                    b = app.getCarrierConfigForSubId(
                            phone.getSubId());
                } else {
                    b = app.getCarrierConfig();
                }

                if (b.getBoolean(CarrierConfigManager.KEY_USE_CALLER_ID_USSD_BOOL)) {
                    text = SuppServicesUiUtil.handleCallerIdUssdResponse(app, context, phone,
                            mmiCode);
                    if (mmiCode.getMessage() != null && !text.equals(mmiCode.getMessage())) {
                        break;
                    }
                }

                if (app.getPUKEntryActivity() != null) {
                    // if an attempt to unPUK the device was made, we specify
                    // the title and the message here.
                    title = com.android.internal.R.string.PinMmi;
                    text = context.getText(R.string.puk_unlocked);
                    break;
                }
                // All other conditions for the COMPLETE mmi state will cause
                // the case to fall through to message logic in common with
                // the FAILED case.

            case FAILED:
                text = mmiCode.getMessage();
                log("displayMMIComplete (failed): using text from MMI message: '" + text + "'");
                break;
            default:
                throw new IllegalStateException("Unexpected MmiCode state: " + state);
        }

        if (previousAlert != null) {
            previousAlert.dismiss();
        }

        // Check to see if a UI exists for the PUK activation.  If it does
        // exist, then it indicates that we're trying to unblock the PUK.
        if ((app.getPUKEntryActivity() != null) && (state == MmiCode.State.COMPLETE)) {
            log("displaying PUK unblocking progress dialog.");

            // create the progress dialog, make sure the flags and type are
            // set correctly.
            ProgressDialog pd = new ProgressDialog(app, THEME);
            pd.setTitle(title);
            pd.setMessage(text);
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            pd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            // display the dialog
            pd.show();

            // indicate to the Phone app that the progress dialog has
            // been assigned for the PUK unlock / SIM READY process.
            app.setPukEntryProgressDialog(pd);

        } else if ((app.getPUKEntryActivity() != null) && (state == MmiCode.State.FAILED)) {
            createUssdDialog(app, context, text, phone,
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            // In case of failure to unlock, we'll need to reset the
            // PUK unlock activity, so that the user may try again.
            app.setPukEntryActivity(null);
        } else {
            // In case of failure to unlock, we'll need to reset the
            // PUK unlock activity, so that the user may try again.
            if (app.getPUKEntryActivity() != null) {
                app.setPukEntryActivity(null);
            }

            // A USSD in a pending state means that it is still
            // interacting with the user.
            if (state != MmiCode.State.PENDING) {
                createUssdDialog(app, context, text, phone,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            } else {
                log("displayMMIComplete: USSD code has requested user input. Constructing input "
                        + "dialog.");

                // USSD MMI code that is interacting with the user.  The
                // basic set of steps is this:
                //   1. User enters a USSD request
                //   2. We recognize the request and displayMMIInitiate
                //      (above) creates a progress dialog.
                //   3. Request returns and we get a PENDING or COMPLETE
                //      message.
                //   4. These MMI messages are caught in the PhoneApp
                //      (onMMIComplete) and the InCallScreen
                //      (mHandler.handleMessage) which bring up this dialog
                //      and closes the original progress dialog,
                //      respectively.
                //   5. If the message is anything other than PENDING,
                //      we are done, and the alert dialog (directly above)
                //      displays the outcome.
                //   6. If the network is requesting more information from
                //      the user, the MMI will be in a PENDING state, and
                //      we display this dialog with the message.
                //   7. User input, or cancel requests result in a return
                //      to step 1.  Keep in mind that this is the only
                //      time that a USSD should be canceled.

                // inflate the layout with the scrolling text area for the dialog.
                ContextThemeWrapper contextThemeWrapper =
                        new ContextThemeWrapper(context, R.style.DialerAlertDialogTheme);
                LayoutInflater inflater = (LayoutInflater) contextThemeWrapper.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_ussd_response, null);

                // get the input field.
                final EditText inputText = (EditText) dialogView.findViewById(R.id.input_field);

                // specify the dialog's click listener, with SEND and CANCEL logic.
                final DialogInterface.OnClickListener mUSSDDialogListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            switch (whichButton) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // As per spec 24.080, valid length of ussd string
                                    // is 1 - 160. If length is out of the range then
                                    // display toast message & Cancel MMI operation.
                                    if (inputText.length() < MIN_USSD_LEN
                                            || inputText.length() > MAX_USSD_LEN) {
                                        Toast.makeText(app,
                                                app.getResources().getString(R.string.enter_input,
                                                MIN_USSD_LEN, MAX_USSD_LEN),
                                                Toast.LENGTH_LONG).show();
                                        if (mmiCode.isCancelable()) {
                                            mmiCode.cancel();
                                        }
                                    } else {
                                        phone.sendUssdResponse(inputText.getText().toString());
                                    }
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    if (mmiCode.isCancelable()) {
                                        mmiCode.cancel();
                                    }
                                    break;
                            }
                        }
                    };

                // build the dialog
                final AlertDialog newDialog =
                        new AlertDialog.Builder(contextThemeWrapper)
                        .setMessage(text)
                        .setView(dialogView)
                        .setPositiveButton(R.string.send_button, mUSSDDialogListener)
                        .setNegativeButton(R.string.cancel, mUSSDDialogListener)
                        .setCancelable(false)
                        .create();

// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
                if (QtiPhoneUtilsHelper.isMultiSimMode() && phone != null) {
                    PhoneAccount account = QtiPhoneUtilsHelper.getPhoneAccount(phone.getSubId());
                    if (account != null && account.getIcon() != null) {
                        newDialog.setIcon(account.getIcon().loadDrawable(context));
                    }
                    if (phone.getCarrierName() != null) {
                        newDialog.setTitle(app.getResources().getString(
                                R.string.carrier_mmi_msg_title, phone.getCarrierName()));
                    } else {
                        newDialog.setTitle(app.getResources().getString(
                               R.string.default_carrier_mmi_msg_title));
                    }
                }

// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
                // attach the key listener to the dialog's input field and make
                // sure focus is set.
                final View.OnKeyListener mUSSDDialogInputListener =
                    new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_CALL:
                                case KeyEvent.KEYCODE_ENTER:
                                    if(event.getAction() == KeyEvent.ACTION_DOWN) {
                                        phone.sendUssdResponse(inputText.getText().toString());
                                        newDialog.dismiss();
                                    }
                                    return true;
                            }
                            return false;
                        }
                    };
                inputText.setOnKeyListener(mUSSDDialogInputListener);
                inputText.requestFocus();

                // set the window properties of the dialog
                newDialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                newDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                // now show the dialog!
                newDialog.show();

                newDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextColor(context.getResources().getColor(R.color.dialer_theme_color));
                newDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setTextColor(context.getResources().getColor(R.color.dialer_theme_color));
            }

            if (mmiCode.isNetworkInitiatedUssd()) {
                playSound(context);
            }
        }
    }

    private static void playSound(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int callsRingerMode = audioManager.getRingerMode();

        if (callsRingerMode == AudioManager.RINGER_MODE_NORMAL) {
            log("playSound : RINGER_MODE_NORMAL");
            try {
                Uri notificationUri = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, notificationUri);
                AudioAttributes aa = new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(aa);
                mediaPlayer.setLooping(false);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                log("playSound exception : " + e);
            }
        } else if (callsRingerMode == AudioManager.RINGER_MODE_VIBRATE) {
            log("playSound : RINGER_MODE_VIBRATE");
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Use NotificationManagerService#DEFAULT_VIBRATE_PATTERN if
            // R.array.config_defaultNotificationVibePattern is not defined.
            long[] pattern = getLongArray(context.getResources(),
                    R.array.config_defaultNotificationVibePattern, DEFAULT_VIBRATE_PATTERN);
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1),
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .build());
        }
    }

    private static long[] getLongArray(Resources r, int resid, long[] def) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return def;
        }
        final int len = ar.length;
        long[] out = new long[len];
        for (int i = 0; i < len; i++) {
            out[i] = ar[i];
        }
        return out;
    }

    /**
     * It displays the message dialog for user about the mmi code result message.
     *
     * @param app This is {@link PhoneGlobals}
     * @param context Context to get strings.
     * @param text This is message's result.
     * @param phone This is phone to create sssd dialog.
     * @param windowType The new window type. {@link WindowManager.LayoutParams}.
     */
    public static void createUssdDialog(PhoneGlobals app, Context context, CharSequence text,
            Phone phone, int windowType) {
        log("displayMMIComplete: MMI code has finished running.");

        log("displayMMIComplete: Extended NW displayMMIInitiate (" + text + ")");
        if (text == null || text.length() == 0) {
            return;
        }

        // displaying system alert dialog on the screen instead of
        // using another activity to display the message.  This
        // places the message at the forefront of the UI.
        AlertDialog ussdDialog = new AlertDialog.Builder(context, THEME)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sUssdMsg.setLength(0);
                    }
                })
                .create();

        ussdDialog.getWindow().setType(windowType);
        ussdDialog.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (sUssdMsg.length() != 0) {
            sUssdMsg.insert(0, "\n")
                    .insert(0, app.getResources().getString(R.string.ussd_dialog_sep))
                    .insert(0, "\n");
        }
        if (phone != null && phone.getCarrierName() != null) {
            ussdDialog.setTitle(app.getResources().getString(R.string.carrier_mmi_msg_title,
                    phone.getCarrierName()));
        } else {
            ussdDialog
                    .setTitle(app.getResources().getString(R.string.default_carrier_mmi_msg_title));
        }
// QTI_BEGIN: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
        if (QtiPhoneUtilsHelper.isMultiSimMode() && phone != null) {
            PhoneAccount account = QtiPhoneUtilsHelper.getPhoneAccount(phone.getSubId());
            if (account != null && account.getIcon() != null) {
                ussdDialog.setIcon(account.getIcon().loadDrawable(context));
            }
        }
// QTI_END: 2021-11-03: Telephony: Add SIM info to USSD sessions on UI
        sUssdMsg.insert(0, text);
        ussdDialog.setMessage(sUssdMsg.toString());
        ussdDialog.show();
    }

    /**
     * Cancels the current pending MMI operation, if applicable.
     * @return true if we canceled an MMI operation, or false
     *         if the current pending MMI wasn't cancelable
     *         or if there was no current pending MMI at all.
     *
     * @see #displayMMIInitiate(Context, MmiCode, Message, Dialog)
     */
    static boolean cancelMmiCode(Phone phone) {
        List<? extends MmiCode> pendingMmis = phone.getPendingMmiCodes();
        int count = pendingMmis.size();
        if (DBG) log("cancelMmiCode: num pending MMIs = " + count);

        boolean canceled = false;
        if (count > 0) {
            // assume that we only have one pending MMI operation active at a time.
            // I don't think it's possible to enter multiple MMI codes concurrently
            // in the phone UI, because during the MMI operation, an Alert panel
            // is displayed, which prevents more MMI code from being entered.
            MmiCode mmiCode = pendingMmis.get(0);
            if (mmiCode.isCancelable()) {
                mmiCode.cancel();
                canceled = true;
            }
        }
        return canceled;
    }

    //
    // Misc UI policy helper functions
    //

    /**
     * Returns true when the given call is in INCOMING state and there's no foreground phone call,
     * meaning the call is the first real incoming call the phone is having.
     */
    public static boolean isRealIncomingCall(Call.State state) {
        return (state == Call.State.INCOMING && !PhoneGlobals.getInstance().mCM.hasActiveFgCall());
    }

    //
    // General phone and call state debugging/testing code
    //

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    public static PhoneAccountHandle makePstnPhoneAccountHandle(Phone phone) {
        if (phone == null) {
            return null;
        } else {
            return makePstnPhoneAccountHandleWithPrefix(phone, "",
                    false, phone.getUserHandle());
        }
    }

    public static PhoneAccountHandle makePstnPhoneAccountHandleWithPrefix(
            Phone phone, String prefix, boolean isEmergency, UserHandle userHandle) {
// QTI_BEGIN: 2023-02-22: Telephony: IMS : Fix for phone process in guest mode
        // To determine this as an emergency only, we are checking the simless case
        // and updating the id based on that.

        TelecomAccountRegistry telecomAccountRegistry =
                TelecomAccountRegistry.getInstance(null);
        List<SubscriptionInfo> subList = null;
        if (telecomAccountRegistry != null) {
            subList = telecomAccountRegistry.getActiveSubscriptionInfoList();
        }
// QTI_END: 2023-02-22: Telephony: IMS : Fix for phone process in guest mode
// QTI_BEGIN: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.

        boolean isEmergencyOnlyAccount = subList != null && subList.size() == 0;
        String id = (isEmergency || isEmergencyOnlyAccount) ? EMERGENCY_ACCOUNT_HANDLE_ID : prefix +
// QTI_END: 2023-01-18: Telephony: IMS : Move RTT downgrade and upgrade logic completely to AOSP.
                String.valueOf((phone != null) ? phone.getSubId() : null);
        return makePstnPhoneAccountHandleWithId(id, userHandle);
    }

    public static PhoneAccountHandle makePstnPhoneAccountHandleWithId(
            String id, UserHandle userHandle) {
        ComponentName pstnConnectionServiceName = getPstnConnectionServiceName();
        // If user handle is null, resort to default constructor to use phone process's
        // user handle
        return userHandle == null
                ? new PhoneAccountHandle(pstnConnectionServiceName, id)
                : new PhoneAccountHandle(pstnConnectionServiceName, id, userHandle);
    }

    public static int getSubIdForPhoneAccount(PhoneAccount phoneAccount) {
        if (phoneAccount != null
                && phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION)) {
            return getSubIdForPhoneAccountHandle(phoneAccount.getAccountHandle());
        }
        return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    public static int getSubIdForPhoneAccountHandle(PhoneAccountHandle handle) {
        Phone phone = getPhoneForPhoneAccountHandle(handle);
        if (phone != null) {
            return phone.getSubId();
        }
        return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    public static Phone getPhoneForPhoneAccountHandle(PhoneAccountHandle handle) {
        if (handle != null && handle.getComponentName().equals(getPstnConnectionServiceName())) {
            return getPhoneFromSubId(handle.getId());
        }
        return null;
    }

// QTI_BEGIN: 2018-06-13: Telephony: MSIM: Emergency account handle support
    public static boolean isValidPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle) {
        return phoneAccountHandle != null && !TextUtils.isEmpty(phoneAccountHandle.getId())
                && !phoneAccountHandle.getId().equals("null");
    }

// QTI_END: 2018-06-13: Telephony: MSIM: Emergency account handle support
    /**
     * Determine if a given phone account corresponds to an active SIM
     *
     * @param sm An instance of the subscription manager so it is not recreated for each calling of
     * this method.
     * @param handle The handle for the phone account to check
     * @return {@code true} If there is an active SIM for this phone account,
     * {@code false} otherwise.
     */
    public static boolean isPhoneAccountActive(SubscriptionManager sm, PhoneAccountHandle handle) {
        return sm.getActiveSubscriptionInfo(Integer.parseInt(handle.getId())) != null;
    }

    private static ComponentName getPstnConnectionServiceName() {
        return PSTN_CONNECTION_SERVICE_COMPONENT;
    }

    private static Phone getPhoneFromSubId(String subId) {
        if (!TextUtils.isEmpty(subId)) {
            for (Phone phone : PhoneFactory.getPhones()) {
                String phoneSubId = Integer.toString(phone.getSubId());
                if (subId.equals(phoneSubId)) {
                    return phone;
                }
            }
        }
        return null;
    }

    /**
     * Register ICC status for all phones.
     */
    static final void registerIccStatus(Handler handler, int event) {
        for (Phone phone : PhoneFactory.getPhones()) {
            IccCard sim = phone.getIccCard();
            if (sim != null) {
                if (VDBG) Log.v(LOG_TAG, "register for ICC status, phone " + phone.getPhoneId());
                sim.registerForNetworkLocked(handler, event, phone);
            }
        }
    }

    /**
     * Register ICC status for all phones.
     */
    static final void registerIccStatus(Handler handler, int event, int phoneId) {
        Phone[] phones = PhoneFactory.getPhones();
        IccCard sim = phones[phoneId].getIccCard();
        if (sim != null) {
            if (VDBG) {
                Log.v(LOG_TAG, "register for ICC status, phone " + phones[phoneId].getPhoneId());
            }
            sim.registerForNetworkLocked(handler, event, phones[phoneId]);
        }
    }

    /**
     * Unregister ICC status for a specific phone.
     */
    static final void unregisterIccStatus(Handler handler, int phoneId) {
        Phone[] phones = PhoneFactory.getPhones();
        IccCard sim = phones[phoneId].getIccCard();
        if (sim != null) {
            if (VDBG) {
                Log.v(LOG_TAG, "unregister for ICC status, phone " + phones[phoneId].getPhoneId());
            }
            sim.unregisterForNetworkLocked(handler);
        }
    }

    /**
     * Set the radio power on/off state for all phones.
     *
     * @param enabled true means on, false means off.
     */
    static final void setRadioPower(boolean enabled) {
        for (Phone phone : PhoneFactory.getPhones()) {
            phone.setRadioPower(enabled);
        }
    }

// QTI_BEGIN: 2018-03-07: Telephony: Emergency Number Implementation for SS & DS
    private static IExtTelephony getIExtTelephony() {
// QTI_END: 2018-03-07: Telephony: Emergency Number Implementation for SS & DS
// QTI_BEGIN: 2020-03-18: Telephony: SEPolicy: Update telephony SELinux policies to avoid name collision.
        return IExtTelephony.Stub.asInterface(ServiceManager.getService("qti.radio.extphone"));
// QTI_END: 2020-03-18: Telephony: SEPolicy: Update telephony SELinux policies to avoid name collision.
// QTI_BEGIN: 2018-03-07: Telephony: Emergency Number Implementation for SS & DS
    }

    public static int getPhoneIdForECall() {
        int phoneId = 0;
        try {
            phoneId = getIExtTelephony().getPhoneIdForECall();
        } catch (RemoteException ex) {
            Log.e("TelephonyConnectionService", "Exceptions : " + ex);
        } catch (NullPointerException ex) {
            Log.e("TelephonyConnectionService", "Exception : " + ex);
        }
        return phoneId;
    }
// QTI_END: 2018-03-07: Telephony: Emergency Number Implementation for SS & DS
// QTI_BEGIN: 2018-06-13: Telephony: MSIM: Emergency account handle support

    public static int getPrimaryStackPhoneId() {
        String modemUuId = null;
        int primayStackPhoneId = SubscriptionManager.INVALID_PHONE_INDEX;

        for (Phone phone : PhoneFactory.getPhones()) {
            if (phone == null) continue;

            Log.d(LOG_TAG, "Logical Modem id: " + phone.getModemUuId()
                    + " phoneId: " + phone.getPhoneId());
            modemUuId = phone.getModemUuId();
            if ((modemUuId == null) || (modemUuId.length() <= 0) ||
                    modemUuId.isEmpty()) {
                continue;
            }
            // Select the phone id based on modemUuid
            // if modemUuid is 0 for any phone instance, primary stack is mapped
            // to it so return the phone id as the primary stack phone id.
// QTI_END: 2018-06-13: Telephony: MSIM: Emergency account handle support
            int modemUuIdValue = PRIMARY_STACK_MODEM_ID;
            try {
                modemUuIdValue = Integer.parseInt(modemUuId);
            } catch (NumberFormatException e) {
                Log.w(LOG_TAG, "modemUuId is not an integer: " + modemUuId);
            }
            if (modemUuIdValue == PRIMARY_STACK_MODEM_ID) {
// QTI_BEGIN: 2018-06-13: Telephony: MSIM: Emergency account handle support
                primayStackPhoneId = phone.getPhoneId();
                Log.d(LOG_TAG, "Primay Stack phone id: " + primayStackPhoneId + " selected");
                break;
            }
        }

        // If phone id is invalid return default phone id
        if (primayStackPhoneId == SubscriptionManager.INVALID_PHONE_INDEX) {
            Log.d(LOG_TAG, "Returning default phone id");
            primayStackPhoneId = 0;
        }

        return primayStackPhoneId;
    }
}
