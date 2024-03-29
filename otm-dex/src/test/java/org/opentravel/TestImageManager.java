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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmLibraryMembers.OtmEnumerationClosed;
import org.opentravel.model.otmLibraryMembers.TestEnumerationClosed;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Verifies the functions of the <code>UserSettings</code> class.
 */
public class TestImageManager extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = true;

    private static Logger log = LogManager.getLogger( TestImageManager.class );

    private static OtmModelManager staticModelManager = null;

    @BeforeClass
    public static void beforeClass() {
        staticModelManager = new OtmModelManager( null, null, null ); // no action manager

        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
    }

    @Test
    public void testStatic() {
        // Given the model manager and an OtmObject
        OtmEnumerationClosed oec = TestEnumerationClosed.buildOtm( staticModelManager );

        // Then image/imageview is returned.
        ImageView iv1 = ImageManager.get( oec );
        ImageView iv2 = ImageManager.get( Icons.LIBRARY );
        Image im = ImageManager.getImage( Icons.ERROR );
        assertNotNull( iv1 );
        assertNotNull( iv2 );
        assertNotNull( im );
        assertNotNull( iv1.getImage() );

        log.debug( "Done." );
    }


    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }

}
