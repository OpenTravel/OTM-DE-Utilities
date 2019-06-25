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

import static org.junit.Assert.assertNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestEnumerations;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;

import javafx.stage.Stage;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestImageManager {
    private static Log log = LogFactory.getLog( TestImageManager.class );

    private static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null ); // no action manager
    }

    @Test
    public void testConstructor() {

        // When stage is null
        Stage stage = null;
        // Then - errors logged but no npe
        ImageManager imageMgr = new ImageManager( stage );

        log.debug( "Done." );
    }

    @Test
    @Ignore
    // FIXME Test fails if not run in the correct order
    public void testStatic() {
        // Given the model manager and an OtmObject
        OtmEnumerationClosed oec = TestEnumerations.buildOtmEnumerationClosed( staticModelManager );

        // When run before initialize
        // ImageManager imageMgr = new ImageManager();

        // Then null is returned.
        assertNull( ImageManager.get( Icons.ATTRIBUTE ) );
        assertNull( ImageManager.get( oec ) );

        log.debug( "Done." );
    }

    @Test
    @Ignore
    // FIXME Test fails if not run in the correct order
    public void testNotInitialized() {
        // Given the model manager and an OtmObject
        OtmEnumerationClosed oec = TestEnumerations.buildOtmEnumerationClosed( staticModelManager );

        // When run before initialize
        ImageManager imageMgr = new ImageManager();

        // Then null is returned.
        assertNull( imageMgr.get_OLD( Icons.ATTRIBUTE ) );
        assertNull( imageMgr.get_OLD( oec ) );

        log.debug( "Done." );
    }

}
