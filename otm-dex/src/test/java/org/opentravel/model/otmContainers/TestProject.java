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

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.common.DexProjectException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;

import java.io.File;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestProject extends TestProjectUtils {
    private static Log log = LogFactory.getLog( TestProject.class );

    @Test
    public void testConstructor() {
        // TODO
    }

    @Test
    public void testAdd_LocalLibrary() throws DexProjectException {
        // Given a local library
        OtmLocalLibrary library = TestLibrary.buildOtm( getModelManager() );
        // Given a project
        OtmProject proj = build( getModelManager() );

        // When
        proj.add( library );

        // Then
        check( library, proj );

        // Debugging post checks
        assertTrue( "Then: library must be added to TL project manager.",
            proj.getTL().getProjectManager().getProjectItem( library.getTL() ) != null );
        assertTrue( "Then: Project must find abstractLibrary.", proj.getProjectItem( library.getTL() ) != null );
        assertTrue( "Then: Project must find Otm Library.", proj.getProjectItem( library ) != null );
        // assertTrue( "Then: Project must be managing project.",
        // getModelManager().getManagingProject( library ) != null );
    }

    @Test(expected = DexProjectException.class)
    public void testAdd_LocalLibrary_null() throws DexProjectException {
        // Given a local library
        OtmLocalLibrary library = null;
        // Given a project
        OtmProject proj = build( getModelManager() );

        // When
        proj.add( library );
        assertTrue( "Must not reach here.", true );
    }

    @Test(expected = DexProjectException.class)
    public void testAdd_LocalLibrary_NotInTLModel() throws DexProjectException {
        // Given a local library NOT in the model
        OtmLocalLibrary library = TestLibrary.buildOtm( getModelManager() );
        getModelManager().getTlModel().removeLibrary( library.getTL() );

        // Given a project
        OtmProject proj = build( getModelManager() );

        // When
        proj.add( library );
        assertTrue( "Must not reach here.", true );
    }

    @Test
    public void testAddManaged_PI() throws DexProjectException {
        // Given - a PI
        OtmProject managingProject = TestOtmProjectManager.buildProject( getModelManager() );
        OtmManagedLibrary mLib = buildMajor( "TestProject_Lib1", managingProject );
        ProjectItem mPI = mLib.getProjectItem();
        AbstractLibrary mTllib = mLib.getTL();
        ProjectManager pm = mPI.getProjectManager();

        assertTrue( "Given:", mPI != null );
        assertTrue( "Given: ", managingProject != null );
        assertTrue( "Given: ", mPI.getContent() == mTllib );

        // Remove the library
        managingProject.remove( mLib ); // Note dependency
        assertTrue( "Given: ", pm == mPI.getProjectManager() ); // Still has one!
        assertTrue( "Given: The Project to be added to must NOT contain lib.",
            !managingProject.contains( mLib.getTL() ) );

        // When
        assertTrue( "Given", mPI instanceof RepositoryItem );
        OtmManagedLibrary newLib = managingProject.addManaged( mPI );

        // Then
        check( newLib, managingProject );
        assertTrue( "Given: The Project added to must contains lib.", managingProject.contains( newLib.getTL() ) );

        // Then - make sure the TLLibrary is new
        assertTrue( "Then: New Otm library must NOT be facade for same TL library.", mLib.getTL() != newLib.getTL() );
        // Then - make sure the managing project is the same
        // assertTrue( "Then: New managing project must be the same as the old one.",
        // getModelManager().getManagingProject( newLib ) == managingProject );
    }


    @Test
    public void testClose() {
        // When
        // Then
    }

    @Test
    public void testRemove() {
        // Given - a PI
        OtmProject managingProject = TestOtmProjectManager.buildProject( getModelManager() );
        OtmManagedLibrary mLib = buildMajor( "TestProject1", managingProject );
        ProjectItem mPI = mLib.getProjectItem();
        assertTrue( "Given:", mPI != null );
        // OtmProject managingProject = getModelManager().getManagingProject( mLib );
        // assertTrue( "Given: ", managingProject != null );

        // Remove the library
        managingProject.remove( mLib );
        // assertTrue( getModelManager().getManagingProject( mLib ) == null );
        // assertTrue( managingProject.getProjectItem( mLib ) == null );

        // Note - Still has project manager!
        // FIXME - do some testing
        assertTrue( mPI.getProjectManager() != null );
    }

    @Test
    public void testGetProjectItem_AbstractLibrary() {
        // When
        // Then
    }

    @Test
    public void testGetProjectItem_OtmLibrary() {
        // When
        // Then
    }

    // TODO - old test, review and update
    @Test
    public void testNewProject() throws Exception {
        OtmModelManager mgr = getModelManager();
        OtmProjectManager otpm = mgr.getOtmProjectManager();
        TestOtmProjectManager.checkProjectManagers( mgr );

        // Given - the call parameters
        File projectFile = null;
        String projName = "testProj";
        String defaultContextId = "testContext";
        String projectId = "testProjId";
        String description = "some description";
        OtmProject p = null;

        // When - missing project file
        try {
            p = otpm.newProject( projectFile, projName, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Caught the expected exception. Project file must not be null." );
        }

        // When - temporary project file supplied
        try {
            projectFile = TestDexFileHandler.getTempFile( "tProj.otp", "testNewProject" );
        } catch (Exception e) {
            log.debug( "Error creating temp project file." );
            assertTrue( "Exception: " + e.getLocalizedMessage(), true );
        }
        try {
            p = otpm.newProject( projectFile, projName, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Unexpected error in newProject." );
            assertTrue( "Exception: " + e.getLocalizedMessage(), true );
        }
        assertTrue( "Then: newProject must return a project.", p != null );
        assertTrue( "Then: Project manager must contain new project.", otpm.getProjects().contains( p ) );
        assertTrue( "Then: Project manager must find project by name.", otpm.get( projName ) == p );
        check( p );

        // Then - test adding Libraries
        //
        OtmLocalLibrary pLib = TestLibrary.buildOtm( getModelManager() );
        // When added
        ProjectItem ret = p.add( pLib );
        // Then
        check( pLib, p );

        // Add second library
        pLib = TestLibrary.buildOtm( getModelManager() );
        // When added
        ret = p.add( pLib );
        // Then
        check( pLib, p );

        // Test - adding managed libraries
        OtmManagedLibrary mLib = buildMajor( "TestProject_Lib1", p );
        ProjectItem mPI = mLib.getProjectItem();

        // FIXME
        // 6/17/2021 - this will fail because mPI is already in project.
        // // When
        // assertTrue( mPI instanceof RepositoryItem );
        // OtmManagedLibrary newLib = p.addManaged( mPI );
        // // Then
        // // OtmProject nMP = getModelManager().getManagingProject( newLib );
        // // assertTrue( "FIXME", getModelManager().getManagingProject( newLib ) == p );
        // check( newLib, p );

        // Closing then opening project fixes adding items problem
        // TODO - i am guessing that the version chain is a problem.
        // Find out and fix instead of using this patch.
        // When fixed, update the NewProjectDialogController
        //
        // Close the project
        p.close();

        // //
        // // FIXME
        // //
        // // Re-open
        // DexFileHandler.openProject( projectFile, mgr, null );
        //// new DexFileHandler().openProject( projectFile, mgr, null );
        // p = mgr.getOtmProjectManager().getProject( projName );

    }

    // private void junk() {
    // // Was at end of testNewProject...
    // //
    // // DexMainController controller = (DexMainController) application.getController();
    // DexMainController controller = getMainController();
    // repoManager = controller.getRepositoryManager();
    // // Given a project that uses the OpenTravel repository
    // // OtmModelManager mgr = controller.getModelManager();
    // // OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
    //
    // // Get a repo item to open
    // // TODO - make this into a static utility
    // assertTrue( repoManager != null );
    // String remoteRepoID = "Opentravel";
    // String remoteRepoEndpoint = "http://opentravelmodel.net";
    // List<RemoteRepository> repos = repoManager.listRemoteRepositories();
    // assertTrue( !repos.isEmpty() );
    // RemoteRepositoryClient rrc = (RemoteRepositoryClient) repoManager.getRepository( remoteRepoID );
    // assertTrue( rrc != null );
    //
    // // List<RepositoryItem> locked = rrc.getLockedItems();
    // // assertTrue( !locked.isEmpty() );
    // List<String> baseNSList = rrc.listBaseNamespaces();
    // List<RepositoryItem> items = new ArrayList<>();
    // for (String baseNS : baseNSList) {
    // items.addAll( rrc.listItems( baseNS, false, true ) );
    // }
    // log.debug( items.size() + " items read from repository." );
    // // RepositoryItem repoItem = locked.get( 0 );
    //
    // // ProjectItem pi = mgr.getProjectManager().addManagedProjectItem( repoItem, p.getTL() );
    // // mgr.addProjects();
    // // mgr.add( pi.getContent() );
    // //
    // // //
    // // Collection<OtmProject> projects = mgr.getProjects();
    // // List<OtmLibrary> mgrLibs = new ArrayList<>( mgr.getLibraries() );
    // // assertTrue( !projects.isEmpty() );
    // // assertTrue( !mgrLibs.isEmpty() );
    // //
    // // // Base namespaces used by library tree table controller
    // // List<OtmLibrary> libs = new ArrayList<>();
    // // Set<String> bnList = mgr.getBaseNamespaces();
    // // for (String bn : bnList) {
    // // libs.addAll( mgr.getLibraryChain( bn ) );
    // // }
    // // assertTrue( !libs.isEmpty() );
    // // for (OtmLibrary lib : libs) {
    // // assertTrue( lib != null );
    // // assertTrue( lib.getTL() != null );
    // // }
    // // // Done by main controller on save all
    // // String results = DexFileHandler.saveLibraries( mgr.getEditableLibraries() );
    // //
    // // // Manually check the contents of the project file ...
    // // log.debug( "Will the file be deleted on exit? Yes, but not its .bak file" );
    // //
    // // // Now, adding libraries via the repository controller does not work
    // // //
    // }


}

