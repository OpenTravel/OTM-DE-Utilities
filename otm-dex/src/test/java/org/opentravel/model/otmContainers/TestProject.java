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

package org.opentravel.model.otmContainers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileHandler;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestProject extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestProject.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    final static String FILE_TESTLOCAL = "TestLocalFiles.otp";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestProject.class );
        repoManager = repositoryManager.get();
    }

    /** ******************************************************************* **/
    // // Test MOVED to TestVersionChain
    // //@Test
    // public void testAddingVersionedProject() throws Exception {
    //
    // // Given a project that uses the OpenTravel repository
    // OtmModelManager mgr = new OtmModelManager( new DexFullActionManager( null ), repoManager );
    // boolean editable = TestDexFileHandler.loadVersionProject( mgr );
    // assertNotNull( mgr.getActionManager( true ) );
    //
    // // String BASENS0 = "http://www.opentravel.org/Sandbox/Test/VersionTest_Unmanaged";
    // // String BASENS1 = "http://www.opentravel.org/Sandbox/Test/v1";
    // //
    // OtmLibrary latestLib = null;
    // int highestMajor = 0;
    // for (OtmLibrary lib : mgr.getLibraries()) {
    // if (lib.isBuiltIn())
    // continue;
    // // log.debug( "Library " + lib + " opened." );
    // // log.debug( "Is latest? " + lib.isLatestVersion() );
    // // log.debug( "Is minor? " + lib.isMinorVersion() );
    // // log.debug( "Version number " + lib.getMajorVersion() + " " + lib.getMinorVersion() );
    // // log.debug( "Is editable? " + lib.isEditable() );
    // // // DexActionManager am = lib.getActionManager();
    // // log.debug( "What action manager? " + lib.getActionManager().getClass().getSimpleName() );
    // //
    // // // List<OtmLibrary> chain = mgr.getVersionChain( lib );
    // // log.debug( "Version chain contains " + mgr.getVersionChain( lib ).size() + " libraries" );
    // // log.debug( "" );
    //
    // if (lib.getMajorVersion() > highestMajor)
    // highestMajor = lib.getMajorVersion();
    // if (lib.isLatestVersion())
    // latestLib = lib;
    // }
    //
    // //
    // // Test adding properties to object in latest major
    // //
    // // Get the latest library and make sure we can add properties to the objects
    // if (editable) {
    // assertTrue( "Given: Library in repository must be editable.", latestLib.isEditable() );
    // OtmLibraryMember vlm = null;
    // for (OtmLibraryMember member : mgr.getMembers( latestLib.getVersionChain().getMajor() )) {
    // assertTrue( "This must be chain editable: ", member.getLibrary().isChainEditable() );
    // vlm = member.createMinorVersion( latestLib );
    // log.debug( "Created minor version of " + member );
    // // Services are not versioned
    // if (vlm == null)
    // assertTrue( !(member.getTL() instanceof Versioned) );
    // else {
    // // Post Checks
    // assertTrue( vlm != null );
    // if (!(vlm instanceof OtmValueWithAttributes) && !(vlm instanceof OtmSimpleObject)) // FIXME
    // assertTrue( vlm.getBaseType() == member );
    // assertTrue( vlm.getName().equals( member.getName() ) );
    // assertTrue( ((LibraryMember) vlm.getTL()).getOwningLibrary() == latestLib.getTL() );
    // assertTrue( vlm.getLibrary() == latestLib );
    // }
    // }
    // } else
    // log.warn( "No editable libraries - could not test adding properties." );
    // }


    @Test
    public void testNewProject() throws Exception {
        DexMainController controller = (DexMainController) application.getController();
        repoManager = controller.getRepositoryManager();

        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );

        File projectFile = null;
        String name = "testProj";
        String defaultContextId = "testContext";
        String projectId = "testProjId";
        String description = "some description";
        try {
            OtmProject p = mgr.newProject( projectFile, name, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Caught the expected exception." );
        }

        // Get a temporary file
        try {
            projectFile = TestDexFileHandler.getTempFile( "tProj.otp", "testNewProject" );
        } catch (Exception e) {
            log.debug( "Error creating temp project file." );
        }
        // if (!projFile.createNewFile()) {
        // log.error( "Error creating temporary file." );
        // }
        // log.debug( "Created Temporary File: " + projFile.getCanonicalPath() );
        OtmProject p = null;
        try {
            p = mgr.newProject( projectFile, name, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Error creating project." );
        }
        assertTrue( p != null );

        // Closing then opening project fixes adding items problem
        // TODO - i am guessing that the version chain is a problem.
        // Find out and fix instead of using this patch.
        // When fixed, update the NewProjectDialogController
        //
        // Close the project
        p.close();
        // Re-open
        new DexFileHandler().openProject( projectFile, mgr, null );
        p = mgr.getProject( name );

        // Get a repo item to open
        // TODO - make this into a static utility
        assertTrue( repoManager != null );
        String remoteRepoID = "Opentravel";
        String remoteRepoEndpoint = "http://opentravelmodel.net";
        List<RemoteRepository> repos = repoManager.listRemoteRepositories();
        assertTrue( !repos.isEmpty() );
        RemoteRepositoryClient rrc = (RemoteRepositoryClient) repoManager.getRepository( remoteRepoID );
        assertTrue( rrc != null );

        List<RepositoryItem> locked = rrc.getLockedItems();
        assertTrue( !locked.isEmpty() );
        List<String> baseNSList = rrc.listBaseNamespaces();
        List<RepositoryItem> items = new ArrayList<>();
        for (String baseNS : baseNSList) {
            items.addAll( rrc.listItems( baseNS, false, true ) );
        }
        log.debug( items.size() + " items read from repository." );
        RepositoryItem repoItem = locked.get( 0 );

        ProjectItem pi = mgr.getProjectManager().addManagedProjectItem( repoItem, p.getTL() );
        mgr.addProjects();
        mgr.add( pi.getContent() );

        //
        Collection<OtmProject> projects = mgr.getProjects();
        List<OtmLibrary> mgrLibs = new ArrayList<>( mgr.getLibraries() );
        assertTrue( !projects.isEmpty() );
        assertTrue( !mgrLibs.isEmpty() );

        // Base namespaces used by library tree table controller
        List<OtmLibrary> libs = new ArrayList<>();
        Set<String> bnList = mgr.getBaseNamespaces();
        for (String bn : bnList) {
            libs.addAll( mgr.getLibraryChain( bn ) );
        }
        assertTrue( !libs.isEmpty() );
        for (OtmLibrary lib : libs) {
            assertTrue( lib != null );
            assertTrue( lib.getTL() != null );
        }
        // Done by main controller on save all
        String results = DexFileHandler.saveLibraries( mgr.getEditableLibraries() );

        // Manually check the contents of the project file ...
        log.debug( "Will the file be deleted on exit? Yes, but not its .bak file" );

        // Now, adding libraries via the repository controller does not work
        //
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

