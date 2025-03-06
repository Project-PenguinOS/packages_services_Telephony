package com.android.phone;

import android.app.ActionBar;
import android.app.Dialog;
// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.content.Context;
import android.content.Intent;
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.os.Bundle;
// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.os.PersistableBundle;
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.preference.Preference;
import android.preference.PreferenceScreen;
// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.telephony.CarrierConfigManager;
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
import android.util.Log;
import android.view.MenuItem;

import com.android.internal.telephony.Phone;
import com.android.phone.settings.SuppServicesUiUtil;

import java.util.ArrayList;

public class GsmUmtsAdditionalCallOptions extends TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "GsmUmtsAdditionalCallOptions";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    public static final String BUTTON_CLIR_KEY  = "button_clir_key";
    public static final String BUTTON_CW_KEY    = "button_cw_key";

    private static final int CW_WARNING_DIALOG = 201;
    private static final int CALLER_ID_WARNING_DIALOG = 202;

// QTI_BEGIN: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
    private static final String KEY_IS_CW_ENABLED = "is_cw_enabled";
    private static final String KEY_IS_CLIR_ENABLED = "is_clir_enabled";
    private static final String KEY_CLIR_ARRAY = "clir_array";
    private static final String KEY_CLIR_SUMMARY = "clir_summary";

// QTI_END: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
    private CLIRListPreference mCLIRButton;
    private CallWaitingSwitchPreference mCWButton;

    private final ArrayList<Preference> mPreferences = new ArrayList<Preference>();
    private int mInitIndex = 0;
    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;

    private boolean mShowCLIRButton = true;
    private boolean mShowCWButton = true;
    private boolean mCLIROverUtPrecautions = false;
    private boolean mCWOverUtPrecautions = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        getWindow().addSystemFlags(
                android.view.WindowManager.LayoutParams
                        .SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);

        addPreferencesFromResource(R.xml.gsm_umts_additional_options);

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.additional_gsm_call_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();

        PreferenceScreen prefSet = getPreferenceScreen();
        mCLIRButton = (CLIRListPreference) prefSet.findPreference(BUTTON_CLIR_KEY);
        mCWButton = (CallWaitingSwitchPreference) prefSet.findPreference(BUTTON_CW_KEY);

        PersistableBundle b = null;
        if (mSubscriptionInfoHelper.hasSubId()) {
            b = PhoneGlobals.getInstance().getCarrierConfigForSubId(
                    mSubscriptionInfoHelper.getSubId());
        } else {
            b = PhoneGlobals.getInstance().getCarrierConfig();
        }

        if (b != null) {
            mShowCLIRButton = b.getBoolean(
                    CarrierConfigManager.KEY_ADDITIONAL_SETTINGS_CALLER_ID_VISIBILITY_BOOL);
            mShowCWButton = b.getBoolean(
                    CarrierConfigManager.KEY_ADDITIONAL_SETTINGS_CALL_WAITING_VISIBILITY_BOOL);
            mCLIROverUtPrecautions = mShowCLIRButton && b.getBoolean(
                    CarrierConfigManager.KEY_CALLER_ID_OVER_UT_WARNING_BOOL);
            mCWOverUtPrecautions = mShowCWButton && b.getBoolean(
                    CarrierConfigManager.KEY_CALL_WAITING_OVER_UT_WARNING_BOOL);
            if (DBG) {
                Log.d(LOG_TAG, "mCLIROverUtPrecautions:" + mCLIROverUtPrecautions
                        + ",mCWOverUtPrecautions:" + mCWOverUtPrecautions);
            }
        }

        boolean isSsOverUtPrecautions = SuppServicesUiUtil.isSsOverUtPrecautions(this, mPhone);

        if (mCLIRButton != null) {
            if (mShowCLIRButton) {
                if (mCLIROverUtPrecautions && isSsOverUtPrecautions) {
                    mCLIRButton.setEnabled(false);
                } else {
                    mPreferences.add(mCLIRButton);
                }
// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
            } else {
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
                prefSet.removePreference(mCLIRButton);
// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
            }
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
        }

        if (mCWButton != null) {
            if (mShowCWButton) {
                if (mCWOverUtPrecautions && isSsOverUtPrecautions) {
                    mCWButton.setEnabled(false);
                } else {
                    mPreferences.add(mCWButton);
                }
            } else {
                prefSet.removePreference(mCWButton);
            }
        }

        if (mPreferences.size() != 0) {
            if (icicle == null) {
                if (DBG) Log.d(LOG_TAG, "start to init ");
                doPreferenceInit(mInitIndex);
            } else {
                if (DBG) Log.d(LOG_TAG, "restore stored states");
                mInitIndex = mPreferences.size();
// QTI_BEGIN: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
                Bundle bundle;
                for (Preference pref : mPreferences) {
                    bundle = icicle.getParcelable(pref.getKey());
                    if (bundle == null) continue;
                    if ((pref instanceof CallWaitingSwitchPreference) && mShowCWButton) {
                        ((CallWaitingSwitchPreference) pref).init(this, true, mPhone);
                        ((CallWaitingSwitchPreference) pref).restoreCallWaitingInfo(
                                bundle.getBoolean(KEY_IS_CW_ENABLED));
                    } else if ((pref instanceof CLIRListPreference) && mShowCLIRButton) {
                        ((CLIRListPreference) pref).init(this, true, mPhone);
                        ((CLIRListPreference) pref).restoreClirInfo(bundle.getBoolean(
                                KEY_IS_CLIR_ENABLED),
                                bundle.getCharSequence(KEY_CLIR_SUMMARY));
                        int[] clirArray = bundle.getIntArray(KEY_CLIR_ARRAY);
                        if (clirArray != null) {
                            if (DBG) {
                                Log.d(LOG_TAG, "onCreate:  clirArray[0]="
                                        + clirArray[0] + ", clirArray[1]=" + clirArray[1]);
                            }
                            ((CLIRListPreference) pref).handleGetCLIRResult(clirArray);
// QTI_END: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
                        } else {
// QTI_BEGIN: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
                            if (isUtEnabledToDisableClir()) {
                                ((CLIRListPreference) pref).setSummary(
                                        R.string.sum_default_caller_id);
                                mCWButton.init(this, false, mPhone);
                            } else {
                                ((CLIRListPreference) pref).init(this, false, mPhone);
                            }
// QTI_END: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
                        }
                    }
                }
            }
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

// QTI_BEGIN: 2018-07-06: Telephony: Skip reading the CLIR over Ut
    private boolean isUtEnabledToDisableClir() {
        boolean skipClir = false;
        CarrierConfigManager configManager = (CarrierConfigManager)
            getSystemService(Context.CARRIER_CONFIG_SERVICE);
        PersistableBundle pb = configManager.getConfigForSubId(mPhone.getSubId());
        if (pb != null) {
            skipClir = pb.getBoolean("config_disable_clir_over_ut");
        }
        return mPhone.isUtEnabled() && skipClir;
    }
// QTI_END: 2018-07-06: Telephony: Skip reading the CLIR over Ut
    @Override
    public void onResume() {
        super.onResume();
        int indexOfStartInit = mPreferences.size();
        boolean isPrecaution = SuppServicesUiUtil.isSsOverUtPrecautions(this, mPhone);
        dismissWarningDialog();

        if (mShowCLIRButton && mCLIROverUtPrecautions && mCLIRButton != null) {
            if (isPrecaution) {
                showWarningDialog(CW_WARNING_DIALOG);
                if (mCLIRButton.isEnabled()) {
                    if (mPreferences.contains(mCLIRButton)) {
                        mPreferences.remove(mCLIRButton);
                    }
                    mCLIRButton.setEnabled(false);
                }
            } else {
                if (!mPreferences.contains(mCLIRButton)) {
                    mCLIRButton.setEnabled(true);
                    mPreferences.add(mCLIRButton);
                }
            }
        }
        if (mShowCWButton && mCWOverUtPrecautions && mCWButton != null) {
            if (isPrecaution) {
                showWarningDialog(CALLER_ID_WARNING_DIALOG);
                if (mCWButton.isEnabled()) {
                    if (mPreferences.contains(mCWButton)) {
                        mPreferences.remove(mCWButton);
                    }
                    mCWButton.setEnabled(false);
                }
            } else {
                if (!mPreferences.contains(mCWButton)) {
                    mCWButton.setEnabled(true);
                    mPreferences.add(mCWButton);
                }
            }
        }

        if (indexOfStartInit < mPreferences.size()) {
            mInitIndex = indexOfStartInit;
            doPreferenceInit(indexOfStartInit);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

// QTI_BEGIN: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
        for (Preference pref : mPreferences) {
            Bundle bundle = new Bundle();
            if (mShowCWButton && pref instanceof CallWaitingSwitchPreference) {
                bundle.putBoolean(KEY_IS_CW_ENABLED, pref.isEnabled());
            } else if (mShowCLIRButton && pref instanceof CLIRListPreference)  {
                bundle.putBoolean(KEY_IS_CLIR_ENABLED, pref.isEnabled());
                bundle.putCharSequence(KEY_CLIR_SUMMARY, pref.getSummary());
                if (((CLIRListPreference) pref).clirArray != null) {
                    bundle.putIntArray(KEY_CLIR_ARRAY, ((CLIRListPreference) pref).clirArray);
                }
            }
            outState.putParcelable(pref.getKey(), bundle);
// QTI_END: 2024-07-09: Telephony: IMS: Save/restore InstanceStates for CW and CLIR during language changed
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
// QTI_BEGIN: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
        doNextPreferenceInit();
// QTI_END: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
        super.onFinished(preference, reading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            CallFeaturesSetting.goUpToTopLevelSetting(this, mSubscriptionInfoHelper);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

// QTI_BEGIN: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
    private void doNextPreferenceInit() {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            doPreferenceInit(mInitIndex);
        }
    }

// QTI_END: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
    private void doPreferenceInit(int index) {
        if (mPreferences.size() > index) {
            Preference pref = mPreferences.get(index);
            if (pref instanceof CallWaitingSwitchPreference) {
                ((CallWaitingSwitchPreference) pref).init(this, false, mPhone);
            } else if (pref instanceof CLIRListPreference) {
// QTI_BEGIN: 2019-02-18: Telephony: Avoid sending two CLIR requests while enter additional settings
                if (isUtEnabledToDisableClir()) {
                  ((CLIRListPreference) pref).setSummary(R.string.sum_default_caller_id);
// QTI_END: 2019-02-18: Telephony: Avoid sending two CLIR requests while enter additional settings
// QTI_BEGIN: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
                  doNextPreferenceInit();
// QTI_END: 2019-05-07: Telephony: IMS: Query call waiting after CLIR grey out
// QTI_BEGIN: 2019-02-18: Telephony: Avoid sending two CLIR requests while enter additional settings
                } else {
                  ((CLIRListPreference) pref).init(this, false, mPhone);
                }
// QTI_END: 2019-02-18: Telephony: Avoid sending two CLIR requests while enter additional settings
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CW_WARNING_DIALOG) {
            return SuppServicesUiUtil.showBlockingSuppServicesDialog(this, mPhone, BUTTON_CW_KEY);
        } else if (id == CALLER_ID_WARNING_DIALOG) {
            return SuppServicesUiUtil.showBlockingSuppServicesDialog(this, mPhone, BUTTON_CLIR_KEY);
        }
        return super.onCreateDialog(id);
    }

    private void showWarningDialog(int id) {
        showDialog(id);
    }

    private void dismissWarningDialog() {
        dismissDialogSafely(CW_WARNING_DIALOG);
        dismissDialogSafely(CALLER_ID_WARNING_DIALOG);
    }
}
