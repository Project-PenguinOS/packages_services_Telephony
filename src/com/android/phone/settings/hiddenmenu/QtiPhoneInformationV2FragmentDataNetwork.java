/*
 * Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */
package com.android.phone.settings.hiddenmenu;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.phone.R;

public class QtiPhoneInformationV2FragmentDataNetwork extends PhoneInformationV2FragmentDataNetwork{
    private static final String TAG = "QtiPhoneInfoV2DataNetwork";

    private Switch mEnableVoLteSwitch;
    private Switch mEnableVoNrSwitch;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEnableVoLteSwitch = (Switch) view.findViewById(R.id.enable_volte_switch);
        mEnableVoNrSwitch = (Switch) view.findViewById(R.id.enable_vonr_switch);
        if (!isImsSupportedOnDevice()) {
            mEnableVoLteSwitch.setVisibility(View.GONE);
            mEnableVoNrSwitch.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateAllFields() {
        super.updateAllFields();

        updateVoLteState();
        updateVoNrState();
    }

    private void updateVoLteState() {
        if (mEnableVoLteSwitch == null || mImsManager == null) {
            return;
        }

        if (!SubscriptionManager.isValidSubscriptionId(mSubId)) {
            mEnableVoLteSwitch.setEnabled(false);
            mEnableVoLteSwitch.setChecked(false);
            return;
        }

        int voLteSetting = SubscriptionManager.getIntegerSubscriptionProperty(
                mSubId, SubscriptionManager.ENHANCED_4G_MODE_ENABLED, -1, mContext);
        boolean voLteEnabled = (voLteSetting != 0) ? true : false;
        ImsMmTelManager imsMmTelManager = mImsManager.getImsMmTelManager(mSubId);
        mEnableVoLteSwitch.setChecked(voLteEnabled);
        mEnableVoLteSwitch.setEnabled(true);
        mEnableVoLteSwitch.setOnCheckedChangeListener(mVoLteOnChangeListener);
    }

    private void updateVoNrState() {
        if (mEnableVoNrSwitch == null || mQueuedWork == null || mImsManager == null) {
            return;
        }

        if (!SubscriptionManager.isValidSubscriptionId(mSubId)) {
            mEnableVoNrSwitch.setEnabled(false);
            mEnableVoNrSwitch.setChecked(false);
            return;
        }
        final int subId = mSubId;
        mQueuedWork.execute(new Runnable() {
            public void run() {
                if (subId != mSubId) {
                    return;
                }
                ImsMmTelManager imsMmTelManager = mImsManager.getImsMmTelManager(subId);
                int voNRSetting = SubscriptionManager.getIntegerSubscriptionProperty(
                        subId, SubscriptionManager.NR_ADVANCED_CALLING_ENABLED, -1, mContext);
                boolean voNrEnabled = (voNRSetting != 0) ? true : false;

                int voLteSetting = SubscriptionManager.getIntegerSubscriptionProperty(
                        subId, SubscriptionManager.ENHANCED_4G_MODE_ENABLED, -1, mContext);
                boolean voLteEnabled = (voLteSetting != 0) ? true : false;

                getActivity().runOnUiThread(() -> {
                    mEnableVoNrSwitch.setChecked(voNrEnabled);
                    // Disable VoNr option if VoLte is disabled
                    if (!voLteEnabled) {
                        mEnableVoNrSwitch.setEnabled(false);
                    }
                });
            }
        });
        mEnableVoNrSwitch.setOnCheckedChangeListener(mVoNrOnChangeListener);
    }

    OnCheckedChangeListener mVoLteOnChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "VoLte button onCheckedChanged " + isChecked + " on subId=" + mSubId);
            setVoLteEnabled(isChecked);
        }
    };

    OnCheckedChangeListener mVoNrOnChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(TAG, "VoNr button onCheckedChanged " + isChecked + " on subId=" + mSubId);
            setVoNrEnabled(isChecked);
        }
    };

    private void setVoLteEnabled(boolean isChecked) {
        if (!SubscriptionManager.isValidSubscriptionId(mSubId) || (mTelephonyManager == null)) {
            Log.e(TAG, " setVoLteEnabled with invalid subId:  " + mSubId + " or TM is null");
            return;
        }

        final int subId = mSubId;
        final int phoneId = mPhoneId;
        mQueuedWork.execute(new Runnable() {
            public void run() {
                if (subId != mSubId) {
                    return;
                }
                ImsMmTelManager imsMmTelManager = mImsManager.getImsMmTelManager(subId);
                try {
                    if (isChecked != QtiPhoneInformationUtil.isVolteEnabled(imsMmTelManager)) {
                        Log.d(TAG, "setVoLteEnabled: " + isChecked + " on subId=" + subId +
                                " mPhoneId:" + mPhoneId);
                        if (isChecked) {
                            mTelephonyManager.enableIms(phoneId);
                        } else {
                            mTelephonyManager.disableIms(phoneId);
                        }
                    }

                    //Update Enhanced 4G LTE Mode Settings
                    SubscriptionManager.setSubscriptionProperty(
                            subId, SubscriptionManager.ENHANCED_4G_MODE_ENABLED,
                            (isChecked ? "1" : "0"));

                    PersistableBundle b = PhoneInformationUtil.getCarrierConfig(mContext).
                            getConfigForSubId(subId);
                    //If Enhanced 4G LTE Mode is uneditable, hidden and VoLTE is disabled we will
                    //enable VoIMS opt-in to allow the user to change the IMS enabled setting, this
                    //is to adapt to the logic in ImsManager.java
                    boolean isUiUnEditable = !b.getBoolean(CarrierConfigManager.
                            KEY_EDITABLE_ENHANCED_4G_LTE_BOOL, false) || b.getBoolean
                            (CarrierConfigManager.KEY_HIDE_ENHANCED_4G_LTE_BOOL, false);
                    if (isUiUnEditable) {
                        SubscriptionManager.setSubscriptionProperty(subId,
                                SubscriptionManager.VOIMS_OPT_IN_STATUS, (isChecked ?"0" : "1"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "fail to set VoLTE=" + isChecked + ". subId=" + subId, e);
                }

                getActivity().runOnUiThread(() -> {
                    /**
                     * 1. VoNr option is disabled if VoLte option is disabled
                     * 2. VoNr option is enabled if VoLte option is enabled
                     */
                    if (!isChecked) {
                        if (mEnableVoNrSwitch != null) {
                            mEnableVoNrSwitch.setChecked(false);
                            mEnableVoNrSwitch.setEnabled(false);
                        }
                    } else {
                        if (mEnableVoNrSwitch != null) {
                            mEnableVoNrSwitch.setEnabled(true);
                        }
                    }
                });
            }
        });
    }

    private void setVoNrEnabled(boolean isChecked) {
        if (!SubscriptionManager.isValidSubscriptionId(mSubId)
                || (mTelephonyManager == null)) {
            Log.e(TAG, " setVoNrEnabled with invalid subId:  " + mSubId + " or TM is null");
            return;
        }
        final int subId = mSubId;
        mQueuedWork.execute(new Runnable() {
            public void run() {
                try {
                    boolean isVoNrEnabled =
                            QtiPhoneInformationUtil.isVoNrEnabled(mTelephonyManager);
                    if (isVoNrEnabled != isChecked) {
                        mTelephonyManager.setVoNrEnabled(isChecked);
                        Log.d(TAG, "set VoNR state to " + isChecked + " on subId=" + subId);

                        //Update VoNR Settings
                        SubscriptionManager.setSubscriptionProperty(
                                subId, SubscriptionManager.NR_ADVANCED_CALLING_ENABLED,
                                (isChecked ? "1" : "0"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "fail to set VoNr=" + isChecked + ". subId=" + subId, e);
                }
            }
        });
    }
}