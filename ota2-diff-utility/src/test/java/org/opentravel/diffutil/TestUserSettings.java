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

package org.opentravel.diffutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestUserSettings {

    @Test
    public void testDefaultSettings() throws Exception {
        UserSettings settings = UserSettings.getDefaultSettings();
        String userHome = System.getProperty( "user.home" );

        assertEquals( userHome, settings.getOldProjectFolder().getAbsolutePath() );
        assertEquals( userHome, settings.getNewProjectFolder().getAbsolutePath() );
        assertEquals( userHome, settings.getOldLibraryFolder().getAbsolutePath() );
        assertEquals( userHome, settings.getNewLibraryFolder().getAbsolutePath() );
        assertEquals( userHome, settings.getReportFolder().getAbsolutePath() );
        assertNotNull( settings.getCompareOptions() );
        assertFalse( settings.getCompareOptions().isSuppressFieldVersionChanges() );
        assertFalse( settings.getCompareOptions().isSuppressLibraryPropertyChanges() );
        assertFalse( settings.getCompareOptions().isSuppressDocumentationChanges() );
    }

}
