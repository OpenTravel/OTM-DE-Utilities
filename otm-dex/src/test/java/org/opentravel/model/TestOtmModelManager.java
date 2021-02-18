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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.objecteditor.ObjectEditorController;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestOtmModelManager extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestOtmModelManager.class );
        repoManager = repositoryManager.get();
    }

    /**
     * Build a model manager with full action manager, built in libraries and repository manager.
     * 
     * @param actionManager action manager to assign as full action manager. can be null.
     * @return
     */
    public static OtmModelManager build() {
        DexFullActionManager fullMgr = new DexFullActionManager( null );
        OtmModelManager mgr = new OtmModelManager( fullMgr, repoManager, null );
        mgr.addBuiltInLibraries( new TLModel() );
        return mgr;
    }

    /**
     * Build a model manager with built in libraries and repository manager.
     * 
     * @param actionManager action manager to assign as full action manager. can be null.
     * @return
     */
    public static OtmModelManager buildModelManager(DexActionManager actionManager) {
        OtmModelManager mgr = new OtmModelManager( actionManager, repoManager, null );
        mgr.addBuiltInLibraries( new TLModel() );
        return mgr;
    }

    /**
     * Assign type to ALL type users in the model. Note, this will likely make them invalid due to UPA and name
     * collisions.
     * 
     * @param assignedType
     * @param mgr
     * @return users that were successfully set are returned
     */
    public static List<OtmTypeUser> assignTypeToEveryUser(OtmTypeProvider assignedType, OtmModelManager mgr) {
        // Assign everything to assignedType
        List<OtmTypeUser> users = new ArrayList<>();
        for (OtmLibraryMember lm : mgr.getMembers())
            for (OtmTypeUser user : lm.getDescendantsTypeUsers()) {
                OtmTypeProvider u = user.setAssignedType( assignedType );
                if (u == assignedType)
                    users.add( user );
            }
        return users;
    }


    // @Test
    // public void testAddingManagedProject() throws Exception {
    //
    // // Given a project that uses the OpenTravel repository
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // TestDexFileHandler.loadAndAddManagedProject( mgr );
    //
    // //
    // for (OtmLibrary lib : mgr.getLibraries())
    // log.debug( "Library " + lib + " opened." );
    //
    // // Then - Expect 4 libraries and 63 members
    // assertTrue( mgr.getLibraries().size() > 2 );
    // assertTrue( !mgr.getMembers().isEmpty() );
    // log.debug( "Read " + mgr.getMembers().size() + " members." );
    // for (OtmLibraryMember m : mgr.getMembers()) {
    // assertTrue( m.getTL().getOwningModel() == mgr.getTlModel() );
    // // if (m instanceof OtmContextualFacet)
    // // TestContextualFacet.testContributedFacet( ((OtmContextualFacet) m).getWhereContributed(),
    // // (OtmContextualFacet) m, ((OtmContextualFacet) m).getContributedObject() );
    // }
    // // Then - assure each base namespace has an non-empty chain
    // assertNotNull( mgr.getBaseNamespaces() );
    // assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
    // mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );
    //
    // mapTests( mgr );
    // }

    @Test
    public void testClose() throws Exception {
        // Given - project added to the model manager
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        // mapTests( mgr );

        // Given assertions
        assertFalse( mgr.getBaseNamespaces().isEmpty() );
        for (String baseNS : mgr.getBaseNamespaces()) {
            assertFalse( mgr.getLibraryChain( baseNS ).isEmpty() );
        }
        int baseNSCount = mgr.getBaseNamespaces().size();
        // OtmModelManager mgr = new OtmModelManager( null, null );
        // mgr.addBuiltInLibraries( new TLModel() );
        assertNotNull( mgr.getIdType() );
        assertNotNull( mgr.getEmptyType() );

        // When - cleared
        mgr.clear();

        // Then
        assertTrue( mgr.getBaseNamespaces().isEmpty() );
        assertTrue( mgr.getProjects().isEmpty() );
        assertTrue( mgr.getProjects().isEmpty() );
        for (OtmLibraryMember mbr : mgr.getMembers())
            assertTrue( mbr.getLibrary().isBuiltIn() );
        // assertTrue( mgr.getMembers().isEmpty() );
        for (OtmLibrary lib : mgr.getLibraries())
            assertTrue( lib.isBuiltIn() );
        // assertTrue( mgr.getLibraries().isEmpty() );
        //
        assertNotNull( mgr.getIdType() );
        assertNotNull( mgr.getEmptyType() );

        // When - loaded again
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        // mgr.addProjects();
        assertTrue( mgr.getBaseNamespaces().size() == baseNSCount );
    }

    @Test
    public void testContains() {
        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );

        // When the project is loaded and added to the model manager
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then - model manager contains the libraries and members
        checkContains( mgr.getProjectManager(), mgr );

        // When second project is added to the model manager
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        // Then - model manager contains both libraries and all their members
        checkContains( mgr.getProjectManager(), mgr );
    }

    // @Test
    // public void testAddingLibrariesToEmptyModel() {
    // // Given a model manager and TL Model with library loaded
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr );
    // assertTrue( "Given: no user libraries in model manager yet.", mgr.getUserLibraries().isEmpty() );
    //
    // // When - libraries are added to model manager
    // mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> {
    // OtmLibrary newLib = mgr.add( tlLib );
    // assertTrue( newLib != null );
    // } );
    //
    // // Then - there will be user libraries
    // assertTrue( !mgr.getUserLibraries().isEmpty() );
    // }
    //
    // @Test
    // public void testAddingLibrariesToModel() {
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // TestDexFileHandler.loadUnmanagedProject( mgr );
    //
    // int initialMemberCount = mgr.getMembers().size();
    // TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr );
    //
    // // int initialMemberCount = mgr.getMembers().size();
    // log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
    // + " members." );
    // mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
    // log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
    // + " members." );
    // assertTrue( mgr.getMembers().size() > initialMemberCount );
    // }
    //
    private void checkContains(ProjectManager pm, OtmModelManager mgr) {
        for (ProjectItem pi : pm.getAllProjectItems()) {
            assertTrue( "Must contain the tlLibrary in project item.", mgr.contains( pi.getContent() ) );
            for (LibraryMember lm : pi.getContent().getNamedMembers()) {
                // log.debug( "Testing " + lm.getLocalName() );

                OtmLibraryMember otmL = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) lm );
                if (otmL == null)
                    log.debug( "ERROR - missing otm from listener on a " + lm.getClass().getSimpleName() );
                OtmLibraryMember otm = (OtmLibraryMember) OtmModelElement.get( (TLModelElement) lm );
                assertTrue( "Must have Otm object from listener.", otm != null );
                assertTrue( "Must contain Otm object from named member.", mgr.contains( otm ) );

                if (!mgr.contains( lm ))
                    log.warn( "Error detected" );
                assertTrue( "Must contain each named member.", mgr.contains( lm ) );
            }
        }
    }

    // /**
    // * Test libraries in the manger to assure they have TL libraries and managing projects.
    // *
    // * @param mgr
    // */
    // private void mapTests(OtmModelManager mgr) {
    //
    // // Then - assure each library maps to the same TL as the otmLibrary's tlObject
    // for (OtmLibrary otmLibrary : mgr.getLibraries()) {
    // assertNotNull( mgr.get( otmLibrary ) );
    // assertTrue( otmLibrary == mgr.get( mgr.get( otmLibrary ) ) );
    // assertTrue( otmLibrary == mgr.get( otmLibrary.getTL() ) );
    // }
    //
    // // Then - there are projects and each library is managed by a project
    // Collection<OtmProject> projects = mgr.getProjects();
    // assertTrue( !projects.isEmpty() );
    // mgr.getLibraries().forEach( l -> assertTrue( projects.contains( mgr.getManagingProject( l ) ) ) );
    // }

    // // Moved to TestOtmModelManager_Adds
    // @Test
    // public void testAddingUnmangedProject() throws Exception {
    //
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // TLModel tlModel = mgr.getTlModel();
    // assertNotNull( tlModel );
    //
    // // Given a project that uses local library files
    // TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
    // assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );
    //
    // // Then - expect at least one project
    // Collection<OtmProject> p = mgr.getProjects();
    // assertTrue( mgr.getUserProjects().size() == 1 );
    // assertTrue( mgr.getProjects().size() == 2 );
    //
    // // Then - Expect 6 libraries and 70 members
    // assertTrue( mgr.getLibraries().size() > 2 );
    // assertTrue( !mgr.getMembers().isEmpty() );
    // log.debug( "Read " + mgr.getMembers().size() + " members." );
    //
    // // Then - assure each base namespace has an non-empty set. Library view lists libraries by baseNS.
    // assertNotNull( mgr.getBaseNamespaces() );
    // assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
    // mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getLibraryChain( b ).isEmpty() ) );
    //
    //
    // mapTests( mgr );
    // }

    // /**
    // * moved to {@link TestContextualFacet}
    // */
    // @Test
    // public void testOpenedContextualFacets() {
    // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    // // Given a project that uses local library files
    // TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
    // for (OtmLibrary lib : mgr.getLibraries())
    // log.debug( "Library " + lib + " opened." );
    // assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );
    //
    // for (OtmLibraryMember m : mgr.getMembers()) {
    // if (m instanceof OtmContextualFacet) {
    // OtmContributedFacet contrib = ((OtmContextualFacet) m).getWhereContributed();
    // OtmLibraryMember base = ((OtmContextualFacet) m).getContributedObject();
    // if (contrib != null && base != null)
    // TestContextualFacet.testContributedFacet( contrib, (OtmContextualFacet) m, base );
    // else {
    // String oeName = ((TLContextualFacet) m.getTL()).getOwningEntityName();
    // log.debug( "Bad contextual facet: " + m + " Entity name = " + oeName );
    // for (OtmLibraryMember candidate : mgr.getMembers()) {
    // if (candidate.getNameWithPrefix().equals( oeName ))
    // log.debug( "Name Match Found " );
    // }
    // }
    // }
    // }
    // }

    /**
     * Test hasEditableLibraries() hasEditableLibraries(library) hasProjects()
     */
    @Test
    public void testHas() {
        // Given an empty model manager
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        assertTrue( mgr.getOtmProjectManager().hasProjects() == false );
        assertTrue( mgr.hasEditableLibraries() == false );

        // Given a project that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        assertTrue( mgr.getOtmProjectManager().hasProjects() == true );
        assertTrue( mgr.hasEditableLibraries() == true );
        for (OtmLibrary lib : mgr.getLibraries()) {
            mgr.hasEditableLibraries( lib ); // NPE check
            mgr.isLatest( lib ); // NPE check
        }
    }

    @Test
    public void testUserSettings() {
        // Given
        UserSettings settings = getUserSettings( application );
        assertNotNull( settings );
        OtmModelManager mgr = new OtmModelManager( null, repoManager, settings );
        OtmProjectManager pMgr = mgr.getOtmProjectManager();
        assertNotNull( pMgr );

        // there may be pre-existing projects
        if (!settings.getRecentProjects().isEmpty())
            log.debug(
                "Settings are loaded from your .ota2 directory. Save the file and remove recent projects to run this test." );
        else {
            assertTrue( "Given: ", settings.getRecentProjects().isEmpty() );

            // Test recent projects in user settings
            //
            // When 1 project added
            TestDexFileHandler.loadAndAddManagedProject( mgr );
            // Then
            assertTrue( settings.getRecentProjects().size() == 1 );
            // When second project added
            TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
            // Then
            assertTrue( settings.getRecentProjects().size() == 2 );
            // When - projects are closed
            mgr.clear();
            // Then - still 2 projects remembered
            assertTrue( settings.getRecentProjects().size() == 2 );
            // Then - project manager also reports projects
            assertTrue( pMgr.getRecentProjects().size() == 2 );
        }

    }

    /**
     * Get the settings from the test's application.
     * 
     * @param application
     * @return settings or null
     */
    public static UserSettings getUserSettings(AbstractOTMApplication application) {
        if (application == null)
            return null;
        UserSettings settings = null;
        AbstractMainWindowController controller = application.getController();
        if (controller instanceof ObjectEditorController) {
            settings = ((ObjectEditorController) controller).getUserSettings();
            assertNotNull( settings );
        }

        return settings;
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

