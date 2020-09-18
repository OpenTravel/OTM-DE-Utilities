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

package org.opentravel.model;

import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager_Finds extends AbstractFxTest {
    // public class TestOtmModelManager_Gets extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager_Finds.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager_Finds.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testFindSubtypesOf() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null, null );
        mgr.addBuiltInLibraries( new TLModel() );

        // TODO
    }


    /**
     * get(abstractLibrary) get(otmLibrary) get(String) get(TLLibrary)
     */
    @Test
    public void testFindUsersOf() {
        // Given a project that uses local library files
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // Then
        // TODO

        log.debug( "Tested findUsersOf() on " + mgr.getLibraries().size() + " libraries." );
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

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }
}

