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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestVersionChain;
import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.TestBusiness;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.IOException;

/**
 * Verifies the functions of the <code>delete property action</code> class on versioned objects.
 */
public class TestDeleteLibraryMemberActionVersioned extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestDeleteLibraryMemberActionVersioned.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    private static OtmModelManager staticModelManager = null;
    static OtmLibrary lib = null;
    static OtmBusinessObject globalBO = null;

    @BeforeClass
    public static void beforeClass() throws IOException {
        setupWorkInProcessArea( TestDeleteLibraryMemberActionVersioned.class );
        repoManager = repositoryManager.get();

        staticModelManager = new OtmModelManager( new DexFullActionManager( null ), null, null );
        lib = staticModelManager.add( new TLLibrary() );
        assertTrue( lib.isEditable() );
        assertTrue( lib.getActionManager() instanceof DexFullActionManager );

        globalBO = TestBusiness.buildOtm( lib, "GlobalBO" ); // Tested in buildOtm()

        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
    }

    /**
     * Test cases:
     * <ol>
     * <li>New Member in major - all types
     * <li>New Member in minor - all types
     * </ol>
     * 
     * @throws VersionSchemeException
     * @throws InterruptedException
     */
    @Test
    public void testIsEnabled1() throws VersionSchemeException, InterruptedException {

        // Given - case 1 - all the types of members
        TestLibrary.addOneOfEach( lib );
        assertTrue( "Given: ", !staticModelManager.getMembers( lib ).isEmpty() );
        DexActionManager fullAM = staticModelManager.getActionManager( true );
        DexActionManager roAM = staticModelManager.getActionManager( false );
        DexActionManager minorAM = new DexMinorVersionActionManager( fullAM );

        // Read-only case
        for (OtmLibraryMember member : staticModelManager.getMembers( lib )) {
            log.debug( "Testing " + member + " in a major library." );
            assertTrue( fullAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
            // assertTrue( "This would be an error.", !minorAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
            assertTrue( !roAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
        }
    }

    @Test
    public void testIsEnabled2()
        throws VersionSchemeException, InterruptedException, RepositoryException, DexTaskException {
        // Given - test set of libraries in a model with action managers
        ActionsTestSet ts = new ActionsTestSet( application );
        // VersionTestSet ts = new VersionTestSet();

        // Given - minor has minor members for all major members
        TestVersionChain.createMinorMembers( ts.major );

        // Given - minor library has new minor members of all types
        // Note - do after creating minors or else the createMinorMembers will fail due to invalid objects
        TestLibrary.addOneOfEach( ts.minor );

        // When - isEnabled asked for all minor members
        for (OtmLibraryMember member : ts.mgr.getMembers( ts.minor )) {
            // boolean enabled = ts.minorAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member );
            // log.debug( "Testing " + member + " is enabled for delete: " + enabled );

            // Then - minor must be true, Read-only must be false
            assertTrue( ts.minorAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
            assertTrue( !ts.roAM.isEnabled( DexActions.DELETELIBRARYMEMBER, member ) );
        }
    }

    @Test
    public void testDeleteMemberAction() {
        log.debug( "TODO" );
        // Given -

    }

    // @Test
    // public void testDeleteMemberAction_MinorQueryFacet()
    // throws VersionSchemeException, InterruptedException, DexTaskException {
    // // Given - test set of libraries in a model with action managers
    // VersionTestSet ts = new VersionTestSet();
    //
    // // Given - a minor query facet
    // OtmBusinessObject minorBO = ts.addBO();
    // OtmQueryFacet minorQF = ts.addQuery();
    // assertTrue( minorQF.getContributedObject() == minorBO );
    //
    // }

    /* ********************************************************************** */
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
