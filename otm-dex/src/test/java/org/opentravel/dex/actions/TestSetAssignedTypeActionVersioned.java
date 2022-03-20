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

package org.opentravel.dex.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.otmContainers.TestOtmVersionChain;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

/**
 * Verifies the functions of the <code>TYPECHANGE</code> action.
 */
public class TestSetAssignedTypeActionVersioned extends AbstractFxTest {
    private static Logger log = LogManager.getLogger( TestSetAssignedTypeActionVersioned.class );


    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    // final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    // final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    // final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmVersionChain.class );
        repoManager = repositoryManager.get();

        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
    }


    // private static OtmModelManager staticModelManager = null;
    // static OtmLibrary lib = null;
    // static OtmBusinessObject globalBO = null;
    //
    // @BeforeClass
    // public static void beforeClass() {
    // staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
    // lib = staticModelManager.add( new TLLibrary() );
    // assertTrue( lib.isEditable() );
    // assertTrue( lib.getActionManager() instanceof DexFullActionManager );
    //
    // globalBO = TestBusiness.buildOtm( lib, "GlobalBO" );
    //
    // // Tested in buildOtm()
    // // assertTrue( globalBO != null );
    // // assertTrue( globalBO.getLibrary() == lib );
    // // assertTrue( globalBO.isEditable() );
    // // assertTrue( globalBO.getActionManager() == lib.getActionManager() );
    // // assertTrue( staticModelManager.getMembers().contains( globalBO ) );
    // }

    // @Test
    // public void testisEnabled() throws VersionSchemeException, InterruptedException {
    // OtmModelManager mgr = TestOtmModelManager.build();
    //
    // // Load project and get latest library
    // if (!TestDexFileHandler.loadVersionProject( mgr ))
    // return; // No editable libraries
    // OtmLibrary latestLib = TestVersionChain.getMinorInChain( mgr );
    //
    // log.debug( "TODO" );
    // // New property to pre-existing object
    // // New object
    // // Resource subject
    // }

    @Test
    public void testAssignToNewProperty() {}

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
