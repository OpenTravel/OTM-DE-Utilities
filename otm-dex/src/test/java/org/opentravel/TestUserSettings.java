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

package org.opentravel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.objecteditor.UserSettings;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestUserSettings {
    // private static Log log = LogFactory.getLog(TestUserSettings.class);
    // static final Logger log = LoggerFactory.getLogger(TestUserSettings.class);

    @Test
    public void testDefaultSettings() throws Exception {
        UserSettings settings = UserSettings.getDefaultSettings();
        String userHome = System.getProperty( "user.home" );

        assertNotNull( settings.getDefaultWindowSize() );
        assertNotNull( settings.getLastProjectFolder() );
        assertTrue( settings.getLastProjectFolder().getPath().startsWith( userHome ) );
        assertNotNull( settings.getWindowPosition() );
        assertNotNull( settings.getWindowSize() );

        // This works:
        // assertFalse("TESTING", settings.getLastProjectFolder().getPath().startsWith(userHome));

        System.out.println( "Well...at least this works." );
        // log.debug("Done getting current settings.");
        // log.debug("TODO - test setting values.");
        // TODO - test setting values
    }

}
