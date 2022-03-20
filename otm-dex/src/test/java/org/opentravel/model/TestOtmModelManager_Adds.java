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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.common.DexFileHandler;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.model.otmContainers.TestLibraryUtils;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// DONE - Convert to DEX
// DONE - Test new addManaged and addUnmanaged
// remove deprecated adds

// @Ignore
public class TestOtmModelManager_Adds extends AbstractDexTest {
    private static Logger log = LogManager.getLogger( TestOtmModelManager_Adds.class );

    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestOtmModelManager_Adds.class );
    }

    /** Method Tests **/
    @Test
    public void testAdd_PI() {
        TLModel tlModel = getModelManager().getTlModel();

        // TODO - move this set up to BuildManagedPI()
        // Create a managed library
        OtmProject oProject = TestOtmProjectManager.buildProject( getModelManager() );
        OtmManagedLibrary mLib = buildMajor( "TestManaged1", oProject );
        assertTrue( "Given: ", mLib instanceof OtmMajorLibrary );

        ProjectItem pi = mLib.getProjectItem();
        assertTrue( "Given: ", pi instanceof RepositoryItem );

        // Close (clear) the model
        getModelManager().clear();
        assertTrue( "Given: There must still be a TLModel.", tlModel == getModelManager().getTlModel() );
        assertTrue( "Given: The TLModel must not contain user libraries.",
            tlModel.getUserDefinedLibraries().isEmpty() );

        // Load PI from the repo into the TL Model see: NamespaceLibrariesRowFactory#addToProject()
        ProjectItem newPI = null;
        try {
            newPI = getModelManager().getProjectManager().addManagedProjectItem( pi, oProject.getTL() );
        } catch (LibraryLoaderException | RepositoryException e) {
            assertTrue( "Exception: " + e.getLocalizedMessage(), false );
        }

        // Pre-check
        assertTrue( "Given: ", tlModel == getModelManager().getTlModel() );
        assertTrue( "Given: ", newPI != null );
        assertTrue( "Given: ", !newPI.getBaseNamespace().isEmpty() );

        //
        // When - new PI is managed.
        //
        OtmLibrary lib = getModelManager().addLibrary( newPI );
        //
        // Then
        //
        assertTrue( mLib instanceof OtmManagedLibrary );
        mLib = (OtmManagedLibrary) lib;

        List<OtmLibrary> afterLibs = getModelManager().getUserLibraries();
        assertTrue( "Then: ", !afterLibs.isEmpty() );
        assertTrue( "Then: ", afterLibs.contains( mLib ) );
        assertTrue( "Then: ", getModelManager().getVersionChain( mLib ) != null );
        check( mLib );
    }

    @Test
    public void testAdd_AbsLib() {
        TLLibrary tlLib = TestLibraryUtils.buildTL( "http://example.com/ns1", "p", "TestLib1" );
        tlLib.setOwningModel( getModelManager().getTlModel() );

        // Before Tests
        List<OtmLibrary> beforeLibs = getModelManager().getUserLibraries();
        assertTrue( "Given: ", beforeLibs.isEmpty() );
        assertTrue( "Given: Must not have associated PI.", getModelManager().getProjectItem( tlLib ) == null );

        // When
        OtmLibrary lib = getModelManager().addLibrary( tlLib );
        // Then
        assertTrue( "Then: must create local lib from unmanaged TLLib.", lib instanceof OtmLocalLibrary );
        OtmLocalLibrary localLib = (OtmLocalLibrary) lib;
        assertTrue( localLib instanceof OtmLocalLibrary );
        List<OtmLibrary> afterLibs = getModelManager().getUserLibraries();
        assertTrue( "Then: ", !afterLibs.isEmpty() );
        assertTrue( "Then: ", afterLibs.contains( localLib ) );
        check( localLib );

        // When - added again
        OtmLibrary secondLib = getModelManager().addLibrary( tlLib );
        assertTrue( "Then: found library must be returned.", secondLib == localLib );

        // When - null passed
        secondLib = getModelManager().addLibrary( (AbstractLibrary) null );
        assertTrue( "Then: null must be returned.", secondLib == null );
    }

    @Test
    public void TestRemoveFromMaps() {
        // Givens - libraries added
        OtmModelManager mgr = getModelManager();
        OtmProject proj = TestOtmProjectManager.buildProject( mgr );
        String ns1 = "http://example.com/ns1/chaintest";
        String ns2 = "http://example.com/ns2/chaintest";

        OtmLibrary localLib = TestLibrary.buildOtm( mgr );
        OtmLibrary majorLib = buildMajor( "TMM1", proj, ns1 );
        OtmLibrary majorLibA = buildMajor( "TMM1a", proj, ns1 );
        OtmLibrary minor1 = buildMinor( majorLib );
        OtmLibrary minor2 = buildMinor( minor1 );
        OtmLibrary majorLib2 = buildMajor( "TMM2", proj, ns2 );

        // Givens check
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            checkMaps( lib, true );
        }

        // Tests - libraries are always removed
        for (OtmLibrary lib : mgr.getUserLibraries()) {
            // When
            mgr.removeFromMaps( lib );
            checkMaps( lib, false );
        }
    }

    private void checkMaps(OtmLibrary lib, boolean shouldBeIn) {
        log.debug( "Checking " + lib + "  " + shouldBeIn );
        OtmModelManager mgr = lib.getModelManager();
        if (shouldBeIn) {
            assertTrue( "Check: libraries map.", mgr.get( lib.getTL() ) != null );
            assertTrue( "Check: chains map.", mgr.getVersionChain( lib ) != null );
            assertTrue( "Check: chains map.", mgr.getVersionChain( lib ).contains( lib ) );
            assertTrue( "Check: namespace map.", mgr.getBaseNamespaces().contains( lib.getBaseNS() ) );
            assertTrue( "Check: domain map.", mgr.getDomain( lib.getBaseNS() ) != null );
        } else {
            assertTrue( "Check: libraries map.", mgr.get( lib.getTL() ) == null );
            // Chains, namespaces and domains are unique based on baseNS not library.
            if (mgr.getVersionChain( lib ) != null)
                assertTrue( "Check: chains map.", !mgr.getVersionChain( lib ).contains( lib ) );
            // TODO? if ( mgr.getBaseNamespaces().contains( lib.getBaseNS() ))
            // assertTrue( "Check: namespace map.", !mgr.getBaseNamespaces().contains( lib.getBaseNS() ) );
            // TODO assertTrue( "Check: domain map.", mgr.getDomain( lib.getBaseNS() ) != null );

        }
    }

    /** Functional Tests **/

    /**
     * Test libraries in the manger to assure they have TL libraries and managing projects.
     * 
     * @param mgr
     */
    private static void mapTests(OtmModelManager mgr) {

        // Then - assure each library maps to the same TL as the otmLibrary's tlObject
        for (OtmLibrary otmLibrary : mgr.getLibraries()) {
            assertNotNull( mgr.get( otmLibrary ) );
            assertTrue( otmLibrary == mgr.get( mgr.get( otmLibrary ) ) );
            assertTrue( otmLibrary == mgr.get( otmLibrary.getTL() ) );
        }

        // Then - there are projects and each library is managed by a project
        // FIXME -
        // Collection<OtmProject> projects = mgr.getProjects();
        // assertTrue( !projects.isEmpty() );
        // for (OtmLibrary lib : mgr.getUserLibraries()) {
        // assertTrue( projects.contains( mgr.getManagingProject( lib ) ) );
        // }
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
            OtmLibrary newLib = mgr.addLibrary( tlLib );
            // OtmLibrary newLib = mgr.addOLD( tlLib );
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

        checkManaged( mgr );
    }

    @Test
    public void testAddingMangedProjectNew() {
        // Mimics code from DexFileHandler openProject without adding to model manager.
        OtmModelManager mgr = getModelManager();
        assertTrue( "Givens: ", mgr != null && mgr.getProjectManager() != null );

        File selectedProjectFile = new File( wipFolder.get(), "/" + TestDexFileHandler.FILE_TESTOPENTRAVELREPO );
        // File selectedProjectFile = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertTrue( "Missing project file parameter.", selectedProjectFile != null );
        assertTrue( "Invalid project file name: ",
            selectedProjectFile.getName().endsWith( DexFileHandler.PROJECT_FILE_EXTENSION ) );
        assertTrue( selectedProjectFile.canRead() );

        ValidationFindings findings = null;
        try {
            mgr.getProjectManager().loadProject( selectedProjectFile, findings, null );
        } catch (Exception e) {
            mgr.getProjectManager().closeAll();
            assertTrue( "Error Opening Project: " + e.getLocalizedMessage(), false );
        }

        mgr.addProjects();
        checkUnmanagedProject( mgr );
    }


    private static void checkManaged(OtmModelManager mgr) {
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
        mgr.getBaseNamespaces().forEach( b -> assertTrue( !mgr.getBaseNSLibraries( b ).isEmpty() ) );

        mapTests( mgr );
    }

    @Test
    public void testAddingProjectLibrariesToModel() {
        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );

        // Open the project
        ValidationFindings findings = null;
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );
        // TestDexFileHandler.loadUnmanagedProject( mgr );

        try {
            mgr.getProjectManager().loadProject( localProject, findings, null );
        } catch (LibraryLoaderException e) {
            assertTrue( "Must not throw exception: " + e.getLocalizedMessage(), true );
        } catch (RepositoryException e) {
            assertTrue( "Must not throw exception: " + e.getLocalizedMessage(), true );
        }

        int initialMemberCount = mgr.getMembers().size();
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLOCALLIBRARY, mgr );

        // int initialMemberCount = mgr.getMembers().size();
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.addLibrary( tlLib ) );
        // mgr.getTlModel().getUserDefinedLibraries().forEach( tlLib -> mgr.addOLD( tlLib ) );
        log.debug( "Model size is now: " + mgr.getLibraries().size() + " libraries and " + mgr.getMembers().size()
            + " members." );
        assertTrue( mgr.getMembers().size() > initialMemberCount );
    }

    @Test
    public void testAddProjects() {
        TestDexFileHandler.loadAndAddUnmanagedProject( getModelManager() );
    }

    @Test
    public void testAddingUnmangedProject() throws Exception {

        OtmModelManager mgr =
            new OtmModelManager( null, repoManager, TestOtmModelManager.getUserSettings( application ) );
        TLModel tlModel = mgr.getTlModel();
        assertNotNull( tlModel );

        // When a project opened that uses local library files
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        // Then
        checkUnmanagedProject( mgr );
    }

    @Test
    public void testAddingUnmangedProjectNew() {
        // Mimics code from DexFileHandler openProject without adding to model manager.
        OtmModelManager mgr = getModelManager();
        assertTrue( "Givens: ", mgr != null && mgr.getProjectManager() != null );

        File selectedProjectFile = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertTrue( "Missing project file parameter.", selectedProjectFile != null );
        assertTrue( "Invalid project file name: ",
            selectedProjectFile.getName().endsWith( DexFileHandler.PROJECT_FILE_EXTENSION ) );
        assertTrue( selectedProjectFile.canRead() );

        ValidationFindings findings = null;
        try {
            mgr.getProjectManager().loadProject( selectedProjectFile, findings, null );
        } catch (Exception e) {
            mgr.getProjectManager().closeAll();
            assertTrue( "Error Opening Project: " + e.getLocalizedMessage(), false );
        }

        mgr.addProjects();
        checkUnmanagedProject( mgr );
    }

    private static void checkUnmanagedProject(OtmModelManager mgr) {
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
        assertTrue( mgr.getBaseNamespaces() != null );
        assertTrue( !mgr.getBaseNamespaces().isEmpty() ); // There should be base namespaces
        // Then - library row factor needs lists per namespaces
        List<String> bnsList = mgr.getBaseNamespaces();
        for (String bns : bnsList)
            assertTrue( !mgr.getBaseNSLibraries( bns ).isEmpty() );

        mapTests( mgr );
    }

}

