/*
 * Copyright 2020 The Android Open Source Project
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

package com.android.phone.testapps.gbatestapp;

// QTI_BEGIN: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
import android.app.Activity;

// QTI_END: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
import android.os.Bundle;

// QTI_BEGIN: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
import androidx.annotation.NonNull;
// QTI_END: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
import androidx.appcompat.app.AppCompatActivity;
// QTI_BEGIN: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// QTI_END: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7

import com.android.phone.testapps.gbatestapp.ui.main.MainFragment;

/** main activity of the gba test app */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// QTI_BEGIN: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
        setupEdgeToEdge(this);
// QTI_END: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }
// QTI_BEGIN: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7

    protected void setupEdgeToEdge(@NonNull Activity activity) {
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(android.R.id.content),
                (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime()
                                    | WindowInsetsCompat.Type.displayCutout());
                    // Apply the insets paddings to the view.
                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                    // Return CONSUMED if you don't want the window insets to keep being
                    // passed down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
    }
// QTI_END: 2025-08-13: Telephony: Handle Edge-to-Edge UI for GbaTestApp am: a25f20e5f7 am: a25f20e5f7
}
