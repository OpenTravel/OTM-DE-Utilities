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
import org.opentravel.AbstractDexTest;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexProjectException;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmProjectManager;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.PublishWithLocalDependenciesException;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the functions of the <code>Otm Model Manager</code>.
 */
// @Ignore
public class TestProjectUtils extends AbstractDexTest {
    private static Log log = LogFactory.getLog( TestProjectUtils.class );

    @BeforeClass
    public static void setupTests() throws Exception {
        beforeClassSetup( TestProjectUtils.class );
    }

    public static final String PTMP = "TempProject";
    public static final String PFNAME = "tProj.otp";
    public static final String PNAME = "testProj";
    public static final String CID = "testContext";
    public static final String PID = "http://opentravel.org/temp/projectNS1/v1";
    public static final String DES = "some description";

    /** ******************************************* TESTS ********************************** */
    @Test
    public void testTestClass() throws Exception {
        OtmProject project = new ProjectTestSet( application ).create();
        assertTrue( project != null );
    }

    /** ******************************************* Utilities ********************************** */

    /**
     * Build an TL project using a temp file from TestDexFileHandler
     */
    public static Project buildTL(OtmModelManager modelMgr) {
        ProjectManager tlProjectManager = modelMgr.getProjectManager();
        assertTrue( "Builder given: ", tlProjectManager != null );
        List<Project> initialProjects = tlProjectManager.getAllProjects();
        List<OtmProject> initialOtmProjects = modelMgr.getProjects();

        // Get temporary project file
        File projectFile = null;
        try {
            projectFile = TestDexFileHandler.getTempFile( PFNAME, PTMP );
            // path = projectFile.getCanonicalPath();
        } catch (Exception e) {
            log.debug( "Error creating temp project file." );
            assertTrue( "Exception: error creating temp project file: " + e.getLocalizedMessage(), false );
        }

        Project p = null;
        try {
            p = tlProjectManager.newProject( projectFile, PID, PNAME, DES );
            // p = new Project( tlProjectManager );
        } catch (LibrarySaveException e) {
            log.debug( "NewProject exception: " + e.getLocalizedMessage() );
        }

        List<Project> endingProjects = tlProjectManager.getAllProjects();
        List<OtmProject> endingOtmProjects = modelMgr.getProjects();

        assertTrue( "Builder: ", p != null );
        assertTrue( "Builder: ", tlProjectManager.getAllProjects().contains( p ) );
        return p;
    }

    /**
     * @deprecated use {@link TestOtmProjectManager#buildProject(OtmModelManager)} Build an otm project using a temp
     *             file from TestDexFileHandler
     */
    @Deprecated
    public static OtmProject build(OtmModelManager modelMgr) {
        return TestOtmProjectManager.buildProject( modelMgr );

        // OtmProjectManager projMgr = modelMgr.getOtmProjectManager();
        // assertTrue( "Builder: ", modelMgr != null );
        // assertTrue( "Builder: ", projMgr != null );
        // List<Project> tlProjListBefore = projMgr.getTLProjectManager().getAllProjects();
        // List<OtmProject> otmProjListBefore = modelMgr.getProjects();
        // TestOtmProjectManager.checkProjectManagers( modelMgr );
        //
        // OtmProject otmProject = null;
        // String path = "";
        //
        // // Get temporary project file
        // File projectFile = null;
        // try {
        // projectFile = TestDexFileHandler.getTempFile( PFNAME, PTMP );
        // path = projectFile.getCanonicalPath();
        // } catch (Exception e) {
        // log.debug( "Error creating temp project file." );
        // assertTrue( "Exception: error creating temp project file: " + e.getLocalizedMessage(), false );
        // }
        //
        // // Create project
        // try {
        // otmProject = projMgr.newProject( projectFile, PNAME, CID, PID, DES );
        // } catch (Exception e) {
        // assertTrue( "Exception: error creating project: " + e.getLocalizedMessage(), false );
        // }
        //
        // List<Project> tlProjListAfter = projMgr.getTLProjectManager().getAllProjects();
        // List<OtmProject> otmProjectsAfter = modelMgr.getProjects();
        // TestOtmProjectManager.checkProjectManagers( modelMgr );
        //
        // assertTrue( "Builder: ", otmProject != null );
        // assertTrue( "Builder: ", modelMgr.getProjects() != null );
        // assertTrue( "Builder: ", modelMgr.getProjects().contains( otmProject ) );
        // assertTrue( "TL Project manager must contain new project.", tlProjListAfter.contains( otmProject.getTL() ) );
        //
        // log.debug( "Built new project in file: " + path );
        // return otmProject;
    }

    /**
     * Check the project's TL and project managers.
     * 
     * @param otmProject
     */
    public static void check(OtmProject otmProject) {
        Project tlProject = otmProject.getTL();
        assertTrue( "Check: project must have TL project.", tlProject != null );

        // Check access to managers
        OtmModelManager modelMgr = otmProject.getModelManager();
        assertTrue( "Check: must have model manager.", modelMgr != null );
        ProjectManager tlProjMgr = modelMgr.getProjectManager();
        OtmProjectManager otmPM = modelMgr.getOtmProjectManager();
        assertTrue( "Check: must have TL project manager.", tlProjMgr != null );
        assertTrue( "Check: must have OTM project manager.", otmPM != null );
        TestOtmProjectManager.checkProjectManagers( modelMgr );

        // Assure manager have the project
        assertTrue( "Check: Otm model manger must contain project.", modelMgr.getProjects().contains( otmProject ) );
        assertTrue( "Check: TL project manager must contain project.",
            tlProjMgr.getAllProjects().contains( tlProject ) );
    }


    /**
     * Check that the library is managed correctly by the project.
     * 
     * @param lib
     * @param otmProject
     */
    public static void check(OtmLibrary lib, OtmProject otmProject) {
        assertTrue( "Check: null parameter.", lib != null );
        assertTrue( "Check: null parameter.", otmProject != null );
        //
        // Make sure managers have this project
        check( otmProject );
        Project tlProject = otmProject.getTL();
        OtmModelManager modelMgr = otmProject.getModelManager();
        OtmProjectManager otmPM = modelMgr.getOtmProjectManager();
        ProjectManager tlPM = modelMgr.getProjectManager();

        AbstractLibrary tlLib = lib.getTL();
        assertTrue( "Check: ", tlLib != null );

        // Check the PI
        ProjectItem libPI = lib.getProjectItem();
        ProjectItem tlPI = tlPM.getProjectItem( lib.getTL() );
        assertTrue( "Check: library PI must equal TL Project Manager's PI", libPI == tlPI );
        assertTrue( "Check: library must have project item.", libPI != null );
        assertTrue( "Check: project item must have same TLLib as OTM library. ", libPI.getContent() == tlLib );

        // Check the model managers
        assertTrue( "Check: OtmModelManager must find the library.", modelMgr.get( tlLib ) == lib );

        // Check the Projects
        assertTrue( "Check: Project must contain the abstract library.", otmProject.contains( lib.getTL() ) );
        assertTrue( "Check: The TL Project must contains lib's PI.", tlProject.getProjectItems().contains( libPI ) );
    }

    /** ******************************************************************* **/

    /**
     * Class for testing using otm projects.
     * <p>
     * TestProject.ProjectTestSet pts = new TestProject().new ProjectTestSet( application );
     * <p>
     * OtmProject px = new TestProject().new ProjectTestSet( application ).create();
     * <p>
     * 
     * <b>Note: </b> this creates its own model manager <br>
     * ProjectTestSet pts = new TestProject().new ProjectTestSet( application ); <br>
     * OtmProject proj = pts.create(); <br>
     * OtmModelManager modelMgr = pts.modelMgr;
     * 
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
        public File localRepoFile;

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
            // Works - controller is initialized with a model manager
            modelMgr = controller.getModelManager();
            // rm = repositoryManager;

            repoMgr = controller.getRepositoryManager();
            // modelMgr = new OtmModelManager( new DexFullActionManager( controller ), repoManager, null );
            projMgr = modelMgr.getOtmProjectManager();

            localRepoID = repoMgr.getLocalRepositoryId();
            localRepoName = repoMgr.getLocalRepositoryDisplayName();
            localRepoFile = repoMgr.getRepositoryLocation();

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
         * @throws DexProjectException
         */
        public OtmLibrary createLibrary() throws DexProjectException {
            // Create major library and manage in project
            OtmLocalLibrary ml = TestLibrary.buildOtm( modelMgr );
            ml.getTL().setNamespace( p.getTL().getProjectId() );
            p.add( ml );

            // FIXME assertTrue( modelMgr.getManagingProject( ml ) == p );
            return ml;
        }

        /**
         * Create a library and manage in this project and in the repository
         * 
         * @return
         * @throws DexProjectException
         */
        public OtmLibrary createManagedLibrary() throws DexProjectException {
            OtmLibrary major = createLibrary();
            assertTrue( ManageLibraryTask.isEnabled( major ) );
            //
            // FIXME - use project or task
            // ManageLibraryTask manageTask = new ManageLibraryTask( repo.getId(), major, null, controller );
            // FIXME OtmProject proj = modelMgr.getManagingProject( major );
            // ProjectManager pm = proj.getTL().getProjectManager();
            ProjectManager pm = major.getModelManager().getProjectManager();
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

            // assertTrue( !major.isUnmanaged() );
            // assertTrue( major.isMajorVersion() );
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
            assertTrue( modelMgr != null );
            assertTrue( modelMgr.getProjects() != null );
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

}

