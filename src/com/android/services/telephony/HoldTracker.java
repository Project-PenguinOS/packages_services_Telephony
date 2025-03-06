/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.services.telephony;

// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
import java.util.HashSet;
import java.util.Set;
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""

/**
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
 * Tracks and updates the hold capability of every call or conference across PhoneAccountHandles.
 *
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
 * @hide
 */
public class HoldTracker {
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
    private final Set<Holdable> mHoldables;
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""

// QTI_BEGIN: 2025-01-30: Telephony: Revert "DSDA: Handle transition to DSDS"
    public HoldTracker() {
// QTI_END: 2025-01-30: Telephony: Revert "DSDA: Handle transition to DSDS"
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        mHoldables = new HashSet<>();
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
    }

    /**
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     * Adds the holdable, and updates the hold capability for all holdables.
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     */
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
    public void addHoldable(Holdable holdable) {
        if (!mHoldables.contains(holdable)) {
            mHoldables.add(holdable);
            updateHoldCapability();
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        }
    }

    /**
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     * Removes the holdable, and updates the hold capability for all holdable.
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     */
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
    public void removeHoldable(Holdable holdable) {
        if (mHoldables.remove(holdable)) {
            updateHoldCapability();
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        }
    }

    /**
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     * Updates the hold capability for all tracked holdables.
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
     */
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
    public void updateHoldCapability() {
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        int topHoldableCount = 0;
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        for (Holdable holdable : mHoldables) {
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
            if (!holdable.isChildHoldable()) {
                ++topHoldableCount;
            }
        }

// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        Log.d(this, "updateHoldCapability(): topHoldableCount = "
                + topHoldableCount);
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
// QTI_BEGIN: 2025-01-30: Telephony: Revert "IMS: Allow two HELD calls on same SUB"
        boolean isHoldable = topHoldableCount < 2;
// QTI_END: 2025-01-30: Telephony: Revert "IMS: Allow two HELD calls on same SUB"
// QTI_BEGIN: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
        for (Holdable holdable : mHoldables) {
// QTI_END: 2025-01-30: Telephony: Revert "Revert "DSDA: Update hold capability across subscriptions.""
            holdable.setHoldable(holdable.isChildHoldable() ? false : isHoldable);
        }
    }
}
