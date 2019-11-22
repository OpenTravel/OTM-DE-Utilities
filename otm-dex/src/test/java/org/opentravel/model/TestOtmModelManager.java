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
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmFacets.OtmContributedFacet;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmSimpleObject;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;
import org.opentravel.model.otmLibraryMembers.OtmXsdSimple;
import org.opentravel.model.otmLibraryMembers.TestContextualFacet;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.util.ArrayList;
import java.util.Collection;
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
     * Build a model manager with built in libraries and repository manager.
     * 
     * @param actionManager action manager to assign as full action manager. can be null.
     * @return
     */
    public static OtmModelManager buildModelManager(DexActionManager actionManager) {
        OtmModelManager mgr = new OtmModelManager( actionManager, repoManager );
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

    /** ******************************************************************* **/
    @Test
    public void testAddingVersionedProject() throws Exception {

        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager );
        TestDexFileHandler.loadVersionProject( mgr );
        assertNotNull( mgr.getActionManager( true ) );

        String BASENS0 = "http://www.opentravel.org/Sandbox/Test/VersionTest_Unmanaged";
        // String BASENS1 = "http://www.opentravel.org/Sandbox/Test/v1";
        //
        OtmLibrary latestLib = null;
        int highestMajor = 0;
        for (OtmLibrary lib : mgr.getLibraries()) {
            if (lib.isBuiltIn())
                continue;
            log.debug( "Library " + lib + " opened." );
            log.debug( "Is latest? " + lib.isLatestVersion() );
            log.debug( "Is minor? " + lib.isMinorVersion() );
            log.debug( "Version number " + lib.getMajorVersion() + " " + lib.getMinorVersion() );
            log.debug( "Is editable? " + lib.isEditable() );
            // DexActionManager am = lib.getActionManager();
            log.debug( "What action manager? " + lib.getActionManager().getClass().getSimpleName() );

            // List<OtmLibrary> chain = mgr.getVersionChain( lib );
            log.debug( "Version chain contains " + mgr.getVersionChain( lib ).size() + " libraries" );
            log.debug( "" );

            if (lib.getMajorVersion() > highestMajor)
                highestMajor = lib.getMajorVersion();
            if (lib.isLatestVersion())
                latestLib = lib;
        }

        //
        // Test adding properties to object in latest major
        //
        // Get the latest library and make sure we can add properties to the objects
        assertTrue( "Given: Library in repository must be editable.", latestLib.isEditable() );
        OtmLibraryMember vlm = null;
        for (OtmLibraryMember member : mgr.getMembers( latestLib.getVersionChain().getMajor() )) {
            assertTrue( "This must be chain editable: ", member.getLibrary().isChainEditable() );
            vlm = member.createMinorVersion( latestLib );
            log.debug( "Created minor version of " + member );
            // Services are not versioned
            if (vlm == null)
                assertTrue( !(member.getTL() instanceof Versioned) );
            else {
                // Post Checks
                assertTrue( vlm != null );
                if (!(vlm instanceof OtmValueWithAttributes) && !(vlm instanceof OtmSimpleObject)) // FIXME
                    assertTrue( vlm.getBaseType() == member );
                assertTrue( vlm.getName().equals( member.getName() ) );
                assertTrue( ((LibraryMember) vlm.getTL()).getOwningLibrary() == latestLib.getTL() );
                assertTrue( vlm.getLibrary() == latestLib );
            }

        }
    }

    @Test
    public void testAddingManagedProject() throws Exception {

        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        TestDexFileHandler.loadManagedProject( mgr );

        // When the project is added to the model manager
        mgr.addProjects();

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
    public void testClose() throws Exception {
        // Given - project added to the model manager
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        TestDexFileHandler.loadManagedProject( mgr );
        mgr.addProjects();
        mapTests( mgr );

        // Given assertions
        assertFalse( mgr.getBaseNamespaces().isEmpty() );
        for (String baseNS : mgr.getBaseNamespaces()) {
            assertFalse( mgr.getLibraryChain( baseNS ).isEmpty() );
        }
        int baseNSCount = mgr.getBaseNamespaces().size();

        // When - cleared
        mgr.clear();

        // Then
        assertTrue( mgr.getBaseNamespaces().isEmpty() );
        assertTrue( mgr.getUserProjects().isEmpty() );
        assertTrue( mgr.getProjects().isEmpty() );
        assertTrue( mgr.getMembers().isEmpty() );
        assertTrue( mgr.getLibraries().isEmpty() );

        // When - loaded again
        TestDexFileHandler.loadManagedProject( mgr );
        mgr.addProjects();
        assertTrue( mgr.getBaseNamespaces().size() == baseNSCount );
    }

    @Test
    public void testContains() {
        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager );

        // When the project is loaded and added to the model manager
        TestDexFileHandler.loadAndAddManagedProject( mgr );

        // Then - model manager contains the libraries and members
        checkContains( mgr.getProjectManager(), mgr );

        // When second project is added to the model manager
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        // Then - model manager contains both libraries and all their members
        checkContains( mgr.getProjectManager(), mgr );
    }

    @Test
    public void testGetPredefinedTypes() {
        // Given
        OtmModelManager mgr = new OtmModelManager( null, null );
        mgr.addBuiltInLibraries( new TLModel() );
        OtmXsdSimple id = mgr.getIdType();
        assertNotNull( id );
        OtmXsdSimple empty = mgr.getEmptyType();
        assertNotNull( empty );
    }

    @Test
    public void testAddingLibrariesToEmptyModel() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr.getTlModel() );

        // int initialMemberCount = mgr.getMembers().size();
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        assertTrue( !mgr.getLibraries().isEmpty() );
    }

    @Test
    public void testAddingLibrariesModel() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        TestDexFileHandler.loadUnmanagedProject( mgr );
        int initialMemberCount = mgr.getMembers().size();
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr.getTlModel() );

        // int initialMemberCount = mgr.getMembers().size();
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.add( tlLib ) );
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        assertTrue( mgr.getMembers().size() > initialMemberCount );
    }

    private void checkContains(ProjectManager pm, OtmModelManager mgr) {
        pm.getAllProjectItems().forEach( pi -> {
            assertTrue( "Must contain the tlLibrary in project item.", mgr.contains( pi.getContent() ) );
            pi.getContent().getNamedMembers().forEach( lm -> {
                assertTrue( "Must contain each named member.", mgr.contains( lm ) );
                assertTrue( "Must contain Otm object from named member.",
                    mgr.contains( (OtmLibraryMember) OtmModelElement.get( (TLModelElement) lm ) ) );
            } );
        } );

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
        mgr.getLibraries().forEach( l -> assertTrue( projects.contains( mgr.getManagingProject( l ) ) ) );
    }

    @Test
    public void testAddingUnmangedProject() throws Exception {

        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // Given a project that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );

        // Then - expect at least one project
        Collection<OtmProject> p = mgr.getProjects();
        assertTrue( mgr.getUserProjects().size() == 1 );
        assertTrue( mgr.getProjects().size() == 2 );

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

    // ?? Where do file open, object specific tests belong?
    @Test
    public void testOpenedContextualFacets() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        // Given a project that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );
        for (OtmLibrary lib : mgr.getLibraries())
            log.debug( "Library " + lib + " opened." );
        assertTrue( "Must have project items.", !mgr.getProjectManager().getAllProjectItems().isEmpty() );

        for (OtmLibraryMember m : mgr.getMembers()) {
            if (m instanceof OtmContextualFacet) {
                OtmContributedFacet contrib = ((OtmContextualFacet) m).getWhereContributed();
                OtmLibraryMember base = ((OtmContextualFacet) m).getContributedObject();
                if (contrib != null && base != null)
                    TestContextualFacet.testContributedFacet( contrib, (OtmContextualFacet) m, base );
                else {
                    String oeName = ((TLContextualFacet) m.getTL()).getOwningEntityName();
                    log.debug( "Bad contextual facet: " + m + " Entity name = " + oeName );
                    for (OtmLibraryMember candidate : mgr.getMembers()) {
                        if (candidate.getNameWithPrefix().equals( oeName ))
                            log.debug( "Name Match Found " );
                    }
                }
            }
        }
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

