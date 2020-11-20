/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.modelcheck;

import org.opentravel.application.common.OTA2ApplicationProvider;
import org.opentravel.application.common.OTA2ApplicationSpec;
import org.opentravel.application.common.OTA2LauncherTabSpec;

/**
 * Application provider for the Model Check application.
 */
public class ModelCheckApplicationProvider implements OTA2ApplicationProvider {

    private static final OTA2ApplicationSpec spec = new OTA2ApplicationSpec( "Model Check",
        "Verifies conformance to rules and best practices for OTM model development", 30,
        OTA2LauncherTabSpec.EXPERIMENTAL_TAB, Images.launcherIcon, ModelCheckApplication.class.getName(), null, null,
        false, true );

    /**
     * @see org.opentravel.application.common.OTA2ApplicationProvider#getApplicationSpec()
     */
    @Override
    public OTA2ApplicationSpec getApplicationSpec() {
        return spec;
    }

}
