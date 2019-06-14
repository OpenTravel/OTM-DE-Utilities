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

package org.opentravel.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestUserSettings {

    @Test
    public void testDefaultSettings() throws Exception {
        UserSettings settings = UserSettings.getDefaultSettings();

        assertEquals( false, settings.isUseProxy() );
        assertEquals( "", settings.getProxyHost() );
        assertNull( settings.getProxyPort() );
        assertEquals( "", settings.getNonProxyHosts() );
    }

}
