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
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileHandler;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
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

        // Prevent java.nio.BufferOverflowException
        System.setProperty( "headless.geometry", "2600x2200-32" );
    }

    /** ******************************************************************* **/

    /**
     * Class for testing using otm projects.
     * <p>
     * TestProject.ProjectTestSet pts = new TestProject().new ProjectTestSet( application );
     * <p>
     * OtmProject px = new TestProject().new ProjectTestSet( application ).create();
     */
    public class ProjectTestSet {
        AbstractOTMApplication application = null;
        public DexMainController controller;
        public RepositoryManager repoMgr;
        public OtmModelManager modelMgr;
        public OtmProjectManager projMgr;

        public RemoteRepository remoteRepo = null;
        public String remoteRepoId;
        public String localRepoID = null;
        public String localRepoName = null;

        // the project creation call parameters
        public String name = "testProj";
        public String defaultContextId = "testContext";
        public String projectId = "http://opentravel.org/temp/projectNS1/v1";
        public String description = "some description";

        public File projectFile = null;
        public OtmProject p = null;

        public ProjectTestSet(AbstractOTMApplication application) {
            this.application = application;

            if (application == null)
                throw new IllegalArgumentException( "Must have application as argument." );
            if (!(application.getController() instanceof DexMainController))
                throw new IllegalArgumentException( "Must have access to Dex Main Controller." );

            controller = (DexMainController) application.getController();
            repoMgr = controller.getRepositoryManager();
            modelMgr = new OtmModelManager( new DexFullActionManager( controller ), repoManager, null );
            projMgr = modelMgr.getOtmProjectManager();

            localRepoID = repoMgr.getLocalRepositoryId();
            localRepoName = repoMgr.getLocalRepositoryDisplayName();

            List<RemoteRepository> repos = repoMgr.listRemoteRepositories();
            remoteRepo = repos.get( 0 );
            assertTrue( remoteRepo != null );
            remoteRepoId = remoteRepo.getId();

            // Not needed - creates exception about directory already exists
            // try {
            // repoMgr.createLocalRepository( localRepoID, localRepoName );
            // } catch (RepositoryException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            if (controller == null)
                throw new IllegalArgumentException( "Must have controller." );
            if (repoMgr == null)
                throw new IllegalArgumentException( "Must have application repository manager." );
            if (modelMgr == null)
                throw new IllegalArgumentException( "Must have application model manager." );
            if (projMgr == null)
                throw new IllegalArgumentException( "Must have application project manager." );

        }

        /**
         * Create a library and manage in the project
         * 
         * @return
         * @throws RepositoryException
         */
        public OtmLibrary createLibrary() throws RepositoryException {
            // Create major library and manage in project
            OtmLibrary ml = TestLibrary.buildOtm( modelMgr );
            ml.getTL().setNamespace( p.getTL().getProjectId() );
            p.add( ml );

            assertTrue( modelMgr.getManagingProject( ml ) == p );
            return ml;
        }

        /**
         * Create a library and manage in this project and in the repository
         * 
         * @return
         * @throws RepositoryException
         * @throws DexTaskException
         */
        public OtmLibrary createManagedLibrary() throws RepositoryException {
            OtmLibrary major = createLibrary();
            assertTrue( ManageLibraryTask.isEnabled( major ) );
            //
            // ManageLibraryTask manageTask = new ManageLibraryTask( repo.getId(), major, null, controller );
            OtmProject proj = modelMgr.getManagingProject( major );
            ProjectManager pm = proj.getTL().getProjectManager();
            ProjectItem item = major.getProjectItem();
            try {
                pm.publish( item, remoteRepo );
            } catch (IllegalArgumentException | RepositoryException | PublishWithLocalDependenciesException e) {
                log.debug( e.getCause() );
            }

            // try {
            // manageTask.doIT();
            // } catch (DexTaskException e) {
            // log.debug( manageTask.getErrorMsg() );
            // log.debug( e.getCause() );
            // assertTrue( false );
            // }
            // assertTrue( manageTask.getErrorMsg() == null );

            assertTrue( !major.isUnmanaged() );
            assertTrue( major.isMajorVersion() );
            return major;
        }

        /**
         * @param application the abstract application from setup
         */
        public OtmProject create() {


            // Get temporary project file
            try {
                projectFile = TestDexFileHandler.getTempFile( "tProj.otp", "testNewProject" );
            } catch (Exception e) {
                log.debug( "Error creating temp project file." );
            }
            // Create project
            try {
                p = projMgr.newProject( projectFile, name, defaultContextId, projectId, description );
            } catch (Exception e) {
                log.debug( "Unexpected error creating project." );
            }

            assertTrue( p != null );
            assertTrue( modelMgr.getProjects().contains( p ) );
            return p;
        }

        public OtmProject loadVersionedProject() throws InterruptedException {
            p = null;
            List<OtmProject> initialProjects = modelMgr.getProjects();

            // Load versioned project
            boolean result = TestDexFileHandler.loadVersionProject( modelMgr );
            if (result) {
                List<OtmProject> newProjects = new ArrayList<>();
                modelMgr.getProjects().forEach( p -> {
                    if (!initialProjects.contains( p ))
                        newProjects.add( p );
                } );
                log.debug( newProjects.size() + " projects loaded." );
                assertTrue( newProjects.size() == 1 );
                p = newProjects.get( 0 );
            }
            return p;
        }

        public OtmProject loadVersionedProjectWithResource() throws InterruptedException {
            p = null;
            List<OtmProject> initialProjects = modelMgr.getProjects();

            // Load versioned project
            boolean result = TestDexFileHandler.loadVersionProjectWithResource( modelMgr );
            if (result) {
                List<OtmProject> newProjects = new ArrayList<>();
                modelMgr.getProjects().forEach( p -> {
                    if (!initialProjects.contains( p ))
                        newProjects.add( p );
                } );
                log.debug( newProjects.size() + " projects loaded." );
                assertTrue( newProjects.size() == 1 );
                p = newProjects.get( 0 );
            }
            return p;
        }

    }

    /** ******************************************* TESTS ********************************** */
    @Test
    public void testTestClass() throws Exception {
        OtmProject project = new ProjectTestSet( application ).create();
        assertTrue( project != null );
    }

    @Test
    public void testNewProject() throws Exception {
        DexMainController controller = (DexMainController) application.getController();
        repoManager = controller.getRepositoryManager();
        // Given a project that uses the OpenTravel repository
        OtmModelManager mgr = new OtmModelManager( null, repoManager, null );
        OtmProjectManager projMgr = mgr.getOtmProjectManager();

        // Given - the call parameters
        File projectFile = null;
        String name = "testProj";
        String defaultContextId = "testContext";
        String projectId = "testProjId";
        String description = "some description";
        OtmProject p = null;

        // When - missing project file
        try {
            p = projMgr.newProject( projectFile, name, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Caught the expected exception. Project file must not be null." );
        }

        // When - temporary project file supplied
        try {
            projectFile = TestDexFileHandler.getTempFile( "tProj.otp", "testNewProject" );
        } catch (Exception e) {
            log.debug( "Error creating temp project file." );
        }
        try {
            p = projMgr.newProject( projectFile, name, defaultContextId, projectId, description );
        } catch (Exception e) {
            log.debug( "Unexpected error creating project." );
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
        p = mgr.getOtmProjectManager().getProject( name );

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

    // // Exact duplicate of TestOtmModelManager. No callers.
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
    // }


    /** ************************************************************************************ **/
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

