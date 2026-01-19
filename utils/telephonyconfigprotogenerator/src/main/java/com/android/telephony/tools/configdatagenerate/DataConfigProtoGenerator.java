/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.telephony.tools.configdatagenerate;

import com.android.internal.telephony.TelephonyConfigData;

import org.w3c.dom.Document;

/**
 * Generator for Data Config Protobuf.
 *
 * <p>This class is responsible for parsing data configuration from an XML document and building
 * the corresponding {@link TelephonyConfigData.DataConfigProto}. It handles connection capability
 * configs and metered capability configs for both home and roaming scenarios.
 */
public class DataConfigProtoGenerator extends BaseConfigGenerator {

    @Override
    public void parse(Document doc) {
        // TODO: Implement parsing logic
    }

    @Override
    public void build(TelephonyConfigData.TelephonyConfigProto.Builder builder) {
        // TODO: Implement build logic
    }
}
