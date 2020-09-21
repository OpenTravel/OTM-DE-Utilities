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
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.Collection;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager_Adds extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager_Adds.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager_Adds.class );
        repoManager = repositoryManager.get();
    }

    /**
     * Test libraries in the manger to assure they have TL libraries and managing projects.
     * 
     * @param mgr
     */
    private void mapTests(OtmModelManager mgr) {

        // Then - assure each library maps to the same TL as the otmLibrary's tlObject
        for (OtmLibrary otmLibrary : mgr.getLibraries()) {
            assertNotNull( mgr.get( otmLibrary ) );
            assertTrue( otmLibrary == mgr.get( mgr.get( otmLibrary ) ) );
            assertTrue( otmLibrary == mgr.get( otmLibrary.getTL() ) );
        }

        // Then - there are projects and each library is managed by a project
        Collection<OtmProject> projects = mgr.getProjects();
        assertTrue( !projects.isEmpty() );
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            assertTrue( projects.contains( mgr.getManagingProject( lib ) ) );
        }
    }


    @Test
    public void testAddingLibrariesToEmptyModel() {
        // Given a model manager and TL Model with library loaded
        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr );
        assertTrue( "Given: no user libraries in model manager yet.", mgr.getUserLibraries().isEmpty() );

        // When - libraries are added to model manager
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> {
            OtmLibrary newLib = mgr.add( tlLib );
            assertTrue( newLib != null );
        } );

        // Then - there will be user libraries
        assertTrue( !mgr.getUserLibraries().isEmpty() );
    }

    @Test
    public void testAddingManagedProject() throws Exception {

        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        //
        for (OtmLibrary lib : mgr.getLibraries())
            log.debug( "Library " + lib + " opened." );

        // Then - Expect 4 libraries and 63 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
        for (OtmLibraryMember m : mgr.getMembers()) {
            assertTrue( m.getTL().getOwningModel() == mgr.getTlModel() );
            // if (m instanceof OtmContextualFacet)
            // TestContextualFacet.testContributedFacet( ((OtmContextualFacet) m).getWhereContributed(),
            // (OtmContextualFacet) m, ((OtmContextualFacet) m).getContributedObject() );
        }
        // Then - assure each base namespace has an non-empty chain
        assertNotNull( mgr.getBaseNamespaces() );
        assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
        mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );

        mapTests( mgr );
    }

    @Test
    public void testAddingLibrariesToModel() {
        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );
        TestDexFileHandler.loadUnmanagedProject( mgr );

        int initialMemberCount = mgr.getMembers().size();
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr );

        // int initialMemberCount = mgr.getMembers().size();
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        assertTrue( mgr.getMembers().size() > initialMemberCount );
    }


    @Test
    public void testAddingUnmangedProject() throws Exception {

        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // Given a project that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );

        // Then - expect at least one project
        Collection<OtmProject> p = mgr.getProjects();
        assertTrue( mgr.getOtmProjectManager().getUserProjects().size() == 1 );
        assertTrue( mgr.getProjects().size() == 1 );

        // Then - Expect 6 libraries and 70 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );

        // Then - assure each base namespace has an non-empty set. Library view lists libraries by baseNS.
        assertNotNull( mgr.getBaseNamespaces() );
        assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
        mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );

        mapTests( mgr );
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

