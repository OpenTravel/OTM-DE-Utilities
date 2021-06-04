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

package org.opentravel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileException;
import org.opentravel.common.DexFileHandler;
import org.opentravel.common.DexProjectException;
import org.opentravel.common.ValidationUtils;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.MenuBarWithProjectController;
import org.opentravel.dex.tasks.DexTask;
import org.opentravel.dex.tasks.DexTaskException;
import org.opentravel.dex.tasks.repository.LockLibraryTask;
import org.opentravel.dex.tasks.repository.ManageLibraryTask;
import org.opentravel.dex.tasks.repository.PromoteLibraryTask;
import org.opentravel.dex.tasks.repository.TestManageLibraryTask;
import org.opentravel.dex.tasks.repository.TestVersionLibraryTask;
import org.opentravel.dex.tasks.repository.UnlockLibraryTask;
import org.opentravel.dex.tasks.repository.VersionLibraryTask;
import org.opentravel.dex.tasks.repository.VersionLibraryTask.VersionType;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmObject;
import org.opentravel.model.TestOtmProjectManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmMajorLibrary;
import org.opentravel.model.otmContainers.OtmManagedLibrary;
import org.opentravel.model.otmContainers.OtmMinorLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.model.otmContainers.OtmVersionChain;
import org.opentravel.model.otmContainers.TestLibrary;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.DefaultRepositoryFileManager;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for Dex tests requiring a repository allowing testing of major and minor versions.
 * <p>
 * SubTypes must run {@link AbstractDexTest#beforeClassSetup(Class)} in their @BeforeClass to set up the work area and
 * set headless geometry to prevent buffer overflow.
 * <p>
 * This @Before each method
 * <ul>
 * <li>Creates new repository in local temporary directory.
 * <li>Mimics the {@link MenuBarWithProjectController#doCloseHandler(javafx.event.ActionEvent)} behavior by:
 * <ul>
 * <li>Clearing ModelManager, ProjectManager, TLModel.
 * <li>Clearing action queue.
 * </ul>
 * </ul>
 * <p>
 * Repository setup follows example in: org.opentravel.schemacompiler.repository.TestRepositoryManager
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractDexTest extends AbstractFxTest {
    private static Log log = LogFactory.getLog( AbstractDexTest.class );

    public static final boolean RUN_HEADLESS = true;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    protected static File repositoryConfig =
        new File( System.getProperty( "user.dir" ) + "/src/test/resources/ota2-repository-config.xml" );

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private RepositoryManager repositoryManager;
    private RepositoryFileManager mockFileManager;

    private static DexMainController mainController;
    private static OtmModelManager modelManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks( this );
        mockFileManager = spy( new DefaultRepositoryFileManager( folder.getRoot() ) );

        // This repository manager has temporary file for local repository and no remote repositories
        repositoryManager = new RepositoryManager( mockFileManager );
        // Add known remote repositories to the controller's repository manager
        for (RemoteRepository r : RepositoryManager.getDefault().listRemoteRepositories())
            repositoryManager.addRemoteRepository( r.getEndpointUrl() );
        application.getController().setRepositoryManager( repositoryManager );

        // Application setup creates model manager which creates a project manager with the wrong repository manager.
        getModelManager().updateProjectManager( repositoryManager );
        TestOtmProjectManager.checkProjectManagers( getModelManager() );

        // Assure the model (manager, project, TLModel) and action queue are clear
        DexMainController controller = getMainController();
        if (controller != null) {
            OtmModelManager modelMgr = controller.getModelManager();
            if (modelMgr != null) {
                modelMgr.clear();
                DexActionManager actionMgr = modelMgr.getActionManager( true );
                if (actionMgr != null)
                    actionMgr.clearQueue();
            }
        }

        log.debug( "Before cleared model and created new repository at "
            + repositoryManager.getFileManager().getRepositoryLocation().getPath() );
    }

    // @BeforeClass
    public static void beforeClassSetup(Class<?> testClass) throws Exception {
        // Prevent java.nio.BufferOverflowException - default is 1280x800
        // System.setProperty( "headless.geometry", "1600x1200-32" );
        System.setProperty( "headless.geometry", "2600x2200-32" );
        // GC Exception:
        // java -Xloggc:gc.log GarbageCollector

        if (testClass == null)
            testClass = AbstractDexTest.class;
        setupWorkInProcessArea( testClass );

        log.debug( "BeforeClass setup work area." );
    }

    //
    // FIXME - consider the difference between Before and BeforeClass and After and AfterClass
    // too much setup/tear-down happening
    //
    // From TestUpversionOrchestrator
    // @BeforeClass
    // public static void setupTests() throws Exception {
    // setupWorkInProcessArea( TestUpversionOrchestrator.class );
    // startTestServer( "versions-repository", 9492, repositoryConfig, true, false, TestUpversionOrchestrator.class );
    // repoManager = repositoryManager.get();
    // }
    //
    // @AfterClass
    // public static void tearDownTests() throws Exception {
    // shutdownTestServer();
    // }


    /**
     * 
     * @param subDirectory added to wipFolder
     * @return
     * @throws IOException
     */
    public static File getTempDir(String subDirectory) throws IOException {
        assertTrue( "Given: must have subDirectory.", subDirectory != null );
        File outputFolder = new File( wipFolder.get(), subDirectory );
        outputFolder.mkdir();
        outputFolder.deleteOnExit();
        assertTrue( "Must be writable folder.", outputFolder.canWrite() );
        return outputFolder;
    }

    /**
     * Create an .otm file and OTMLibrary. Uses
     * {@link DexFileHandler#createLibrary(String, String, String, OtmModelManager)} to create library and manage with
     * model manager.
     * 
     * @param ns namespace, defaults if null
     * @param subDir added to {@link #getTempDir(String)} defaults if null
     * @param name defaults if null
     * @return
     */
    public OtmLocalLibrary buildTempLibrary(String ns, String subDir, String name) {
        if (ns == null)
            ns = "http://example.com/test";
        if (subDir == null)
            subDir = "TempTest";
        String fileName = "tlib1";
        String libName = "TestLib";
        if (name != null) {
            fileName = name;
            libName = name;
        }

        // Get a temp directory for the new .otm file
        File dir = null;
        try {
            dir = getTempDir( subDir );
        } catch (IOException e) {
            assertTrue( "TempDir exception: " + e.getLocalizedMessage(), false );
        }

        // Create OTM library in model manager from file path
        String path = dir.getPath() + File.separator + fileName;
        OtmLocalLibrary otmLibrary = null;
        try {
            otmLibrary = DexFileHandler.createLibrary( path, ns, libName, getModelManager() );
        } catch (DexFileException e) {
            assertTrue( "CreateLibrary exception: " + e.getLocalizedMessage(), false );
        }

        // Post-checks
        assertTrue( "Builder: new library must be found by model manager.",
            getModelManager().get( otmLibrary ) == otmLibrary.getTL() );
        TestLibrary.checkLibrary( otmLibrary );

        log.debug( "Created temp library " + path );
        return otmLibrary;
    }

    public RepositoryManager getRepository() {
        // 5/26 - this is failing
        // try {
        // assertTrue( "Given: ", repositoryManager == RepositoryManager.getDefault() );
        // } catch (RepositoryException e) {
        // assertTrue( "Repository Exception: " + e.getLocalizedMessage(), false );
        // }
        return repositoryManager;
    }

    public String getRepoId() {
        String repoID = "";
        try {
            repoID = RepositoryManager.getDefault().getLocalRepositoryId();
        } catch (RepositoryException e) {
            assertTrue( "Exception: " + e.getLocalizedMessage(), false );
        }
        return repoID;
    }

    public DexMainController getMainController() {
        DexMainController mc = null;
        if (application != null && application.getController() instanceof DexMainController)
            mc = (DexMainController) application.getController();
        assertTrue( "Given: must have main controller.", mc != null );
        return mc;
    }

    public OtmModelManager getModelManager() {
        OtmModelManager mgr = null;
        if (application != null && application.getController() instanceof DexMainController)
            mgr = ((DexMainController) application.getController()).getModelManager();
        assertTrue( "Given: must have model manager.", mgr != null );
        return mgr;
    }

    /** *********************** (RTU) Repository Test Utilities ********************************** */

    /**
     * Lock the library.
     */
    public void rtuLock(OtmManagedLibrary lib) {
        assertTrue( "Util pre-condition: ", lib != null );
        assertTrue( "Util pre-condition: ", lib.getState() != null );
        assertTrue( "Util pre-condition: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        try {
            new LockLibraryTask( lib, null, null, null, getModelManager() ).doIT();
        } catch (RepositoryException e) {
            assertTrue( "Lock library exception: " + e.getLocalizedMessage(), false );
        }
        assertTrue( "Util: must be locked.", lib.getState() == RepositoryItemState.MANAGED_WIP );
    }

    /**
     * Use PromoteLibraryTask to reach endStatus. Unlock if MANAGED_WIP.
     * 
     * @param lib
     * @param endStatus
     */
    public void rtuPromoteUntil(OtmManagedLibrary lib, TLLibraryStatus endStatus) {
        if (lib.getState() == RepositoryItemState.MANAGED_WIP)
            rtuUnLock( lib );

        while (lib.getStatus() != endStatus) {
            TLLibraryStatus targetStatus = lib.getProjectItem().getStatus().nextStatus();
            if (!PromoteLibraryTask.isEnabled( lib, targetStatus )) {
                log.debug( "Can't promote: " + PromoteLibraryTask.getReason( lib, targetStatus ) );
                return;
            }
            try {
                new PromoteLibraryTask( lib, null, null, null, getModelManager() ).doIT();
            } catch (RepositoryException e) {
                assertTrue( "Promote Exception: " + e.getLocalizedMessage(), false );
            }
        }
    }

    public OtmManagedLibrary rtuVersion(VersionType type, OtmManagedLibrary mLib) throws DexTaskException {
        rtuPromoteUntil( mLib, TLLibraryStatus.FINAL );
        List<OtmLibrary> orginalLibs = getModelManager().getUserLibraries();

        // Given a task
        VersionLibraryTask task = new VersionLibraryTask( type, mLib, null, getMainController().getStatusController(),
            null, getModelManager() );

        if (!VersionLibraryTask.isEnabled( mLib ))
            log.debug( "Given error: " + VersionLibraryTask.getReason( mLib ) );
        assertTrue( "Given: task is enabled for library. ", VersionLibraryTask.isEnabled( mLib ) );

        // Model Manager checks
        assertTrue( getModelManager().contains( mLib.getTL() ) );

        // When run
        task.doIT();

        // Get the Original and New libraries.
        OtmManagedLibrary vLib = null;
        for (OtmLibrary uLib : getModelManager().getUserLibraries())
            if (uLib instanceof OtmManagedLibrary && !orginalLibs.contains( uLib ))
                vLib = (OtmManagedLibrary) uLib;

        TestVersionLibraryTask.check( mLib, vLib );
        return vLib;
    }

    /**
     * Unlock the library.
     */
    public void rtuUnLock(OtmManagedLibrary lib) {
        assertTrue( "Util pre-condition: ", lib.getState() == RepositoryItemState.MANAGED_WIP );
        try {
            new UnlockLibraryTask( lib, false, "", null, null ).doIT();
        } catch (RepositoryException e) {
            assertTrue( "Unlock library exception: " + e.getLocalizedMessage(), false );
        }
        assertTrue( "Util: ", lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
    }

    /**
     * Build and add 4 chains containing 1 local, 3 major and 2 minor libraries.
     */
    public Map<OtmVersionChain,List<OtmLibrary>> buildLibraryTestSet() {
        OtmModelManager mgr = getModelManager();
        mgr.clear();
        OtmProject proj = TestOtmProjectManager.buildProject( mgr );
        String ns1 = "http://example.com/ns1/chaintest";
        String ns2 = "http://example.com/ns2/chaintest";

        OtmLibrary localLib = TestLibrary.buildOtm( mgr );
        OtmLibrary majorLib = buildMajor( "TMM1", proj, ns1 );
        OtmLibrary majorLibA = buildMajor( "TMM1a", proj, ns1 );
        OtmLibrary minor1 = buildMinor( majorLib );
        OtmLibrary minor2 = buildMinor( minor1 );
        OtmLibrary majorLib2 = buildMajor( "TMM2", proj, ns2 );

        Map<OtmVersionChain,List<OtmLibrary>> setMap = new HashMap<>();
        put( setMap, localLib );
        put( setMap, majorLib );
        put( setMap, majorLibA );
        put( setMap, minor1 );
        put( setMap, minor2 );
        put( setMap, majorLib2 );
        return setMap;
    }

    private void put(Map<OtmVersionChain,List<OtmLibrary>> map, OtmLibrary lib) {
        if (map.containsKey( lib.getVersionChain() ))
            map.get( lib.getVersionChain() ).add( lib );
        else {
            List<OtmLibrary> list = new ArrayList<>();
            list.add( lib );
            map.put( lib.getVersionChain(), list );
        }
    }

    /**
     * Build a major library using temp file in a new project and publish to repository.
     * 
     * @return the published, draft, unlocked library.
     */
    public OtmMajorLibrary buildMajor(String name) {
        return buildMajor( name, null, null );
    }

    public OtmMajorLibrary buildMajor(String name, OtmProject project) {
        return buildMajor( name, project, null );
    }

    /**
     * Build a major library using temp file in a new project and publish to repository.
     * <p>
     * To get just a PI, get the ProjectItem then clear the model manager
     * 
     * @param name library name
     * @param proj project to add library to, if null a new project is created.
     *        {@link TestOtmProjectManager#buildProject(OtmModelManager)}
     * @return managed, published major library
     */
    public OtmMajorLibrary buildMajor(String name, OtmProject proj, String ns) {
        OtmLocalLibrary newLib = buildTempLibrary( ns, null, name );
        assertTrue( "Builder: ", getModelManager().getUserLibraries().contains( newLib ) );

        if (proj == null)
            proj = TestOtmProjectManager.buildProject( getModelManager() );

        try {
            proj.add( newLib );
        } catch (DexProjectException e) {
            assertTrue( "Exception building major library: " + e.getLocalizedMessage(), false );
        }

        // Publish creates and tests a new published library
        OtmMajorLibrary major = publishLibrary( newLib );

        assertTrue( "Builder: ", !getModelManager().getUserLibraries().contains( newLib ) );
        assertTrue( "Builder: ", getModelManager().getUserLibraries().contains( major ) );
        assertTrue( "Builder: ", !major.getProjectItems().isEmpty() );
        assertTrue( "Builder: ", major.getStatus() == TLLibraryStatus.DRAFT );
        assertTrue( "Builder: ", major.getState() == RepositoryItemState.MANAGED_UNLOCKED );

        return major;
    }

    /**
     * Build a minor library from the passed library.
     * <p>
     * If needed, {@link DexTask}s will be used to:
     * <ul>
     * <li>published
     * <li>unlocked
     * <li>promoted to final
     * </ul>
     * 
     * @param lib
     */
    public OtmMinorLibrary buildMinor(OtmLibrary lib) {
        // FIXME - use version task
        List<OtmLibrary> beforeLibs = getModelManager().getUserLibraries();
        if (!lib.isValid())
            log.debug( ValidationUtils.getMessagesAsString( lib.getFindings() ) );
        assertTrue( "PreCondition: library must be valid.", lib.isValid() );

        // Publish if needed
        if (lib instanceof OtmLocalLibrary)
            lib = publishLibrary( lib );
        assertTrue( lib instanceof OtmManagedLibrary );

        // Unlock if needed
        if (lib.getState() == RepositoryItemState.MANAGED_WIP)
            rtuUnLock( (OtmManagedLibrary) lib );
        assertTrue( lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );

        // Promote until FINAL
        rtuPromoteUntil( (OtmManagedLibrary) lib, TLLibraryStatus.FINAL );

        // Create a minor version
        VersionLibraryTask vlt = new VersionLibraryTask( VersionType.MINOR, lib, null, null, null, getModelManager() );
        if (!VersionLibraryTask.isEnabled( lib ))
            log.debug( "Version Task not enabled because: " + VersionLibraryTask.getReason( lib ) );
        try {
            vlt.doIT();
        } catch (DexTaskException e) {
            log.debug( "VersionLibraryTask exception: " + e.getLocalizedMessage() );
        }

        List<OtmLibrary> afterLibs = getModelManager().getUserLibraries();
        assertTrue( "Model must have a new library.", afterLibs.size() > beforeLibs.size() );

        // Find the newly created minor library
        OtmMinorLibrary minor = null;
        for (OtmLibrary l : afterLibs)
            if (!beforeLibs.contains( l ) && l instanceof OtmMinorLibrary)
                minor = (OtmMinorLibrary) l;
        assertTrue( "Then: new minor library was not found.", minor != null );

        return minor;
    }

    /**
     * based on: ManageLibraryTask manageTask = new ManageLibraryTask( repo.getId(), major, null, controller );
     * 
     * @param lib
     * @return
     */
    public OtmMajorLibrary publishLibrary(OtmLibrary lib) {
        // check( lib );
        // RepositoryManager r = getRepository();
        OtmMajorLibrary newLib = TestManageLibraryTask.publish( repositoryManager, lib, getMainController() );
        // OtmLibrary newLib = TestManageLibraryTask.publish( getRepoId(), lib, getMainController() );

        log.debug( "Published library " + lib + " to repo " + getRepoId() );
        return newLib;
    }


    /**
     * TODO - build this out to be the one entry point for standard checks.
     * 
     * @param lib
     */
    public void check(OtmObject obj) {
        // if (obj instanceof OtmBusinessObject)
        // TestBusiness.check((OtmBusinessObject)obj);
    }

    public void check(OtmLibrary obj) {
        if (obj instanceof OtmLibrary)
            TestLibrary.checkLibrary( (OtmLibrary) obj );

        if (obj instanceof OtmManagedLibrary)
            TestManageLibraryTask.check( (OtmManagedLibrary) obj, getModelManager() );
    }

    // TODO - move and use all these tests into some sort of check
    public void ToDo(OtmLibrary lib) {
        String reason = "";
        if (!ManageLibraryTask.isEnabled( lib ))
            reason = ManageLibraryTask.getReason( lib );
        assertTrue( "Given: manage library task must be enabled for library; " + reason,
            ManageLibraryTask.isEnabled( lib ) );

        // Get the repository
        RepositoryManager repository = application.getController().getRepositoryManager();
        assertTrue( "Given - before class setup was not run properly.", repositoryManager != null );
        assertTrue( "Given", repository != null );
        assertTrue( repository == repositoryManager );
        String repoPath = repository.getFileManager().getRepositoryLocation().getPath();

        // Get and check the model manager
        OtmModelManager modelMgr = getModelManager();
        assertTrue( "Given", modelMgr != null );
        RepositoryManager mmrm = modelMgr.getProjectManager().getRepositoryManager();
        assertTrue( "Given: project and application must have same repository manager.", mmrm == repositoryManager );

        // // Get a project that manages the library
        // OtmProject proj = modelMgr.getManagingProject( lib );
        // assertTrue( "Given: library must have a managing project.", proj != null );

        // Get and check TL project manager
        //// ProjectManager pm = proj.getTL().getProjectManager();
        // assertTrue( "Given: project must have a TL project.", pm != null );
        // String pmRepoPath = pm.getRepositoryManager().getRepositoryLocation().getPath();
        // assertTrue( "pm Repo is right repo.", pm.getRepositoryManager() == repository );
        // assertTrue( "Repo and project must have same path.", repoPath.equals( pmRepoPath ) );

        // Get the project item
        ProjectItem item = lib.getProjectItem();
        assertTrue( "Given: library must have a project item.", item != null );

        // // Do It
        // try {
        // pm.publish( item, repository );
        // } catch (IllegalArgumentException | RepositoryException | PublishWithLocalDependenciesException e) {
        // log.debug( "Exception publishing item: " + e.getLocalizedMessage() );
        // log.debug( "Exception publishing item: " + e.getCause() );
        // }
        //
        // assertTrue( lib.getTL().getLibraryUrl().toString().contains( "junit" ) );
        // assertTrue( !lib.isUnmanaged() );
        // assertTrue( lib.getState() == RepositoryItemState.MANAGED_UNLOCKED );
        // assertTrue( lib.getStatus() == TLLibraryStatus.DRAFT );
        //
        // return lib;
    }

    /** *********************** AbstractFxText required methods ********************************** */
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
