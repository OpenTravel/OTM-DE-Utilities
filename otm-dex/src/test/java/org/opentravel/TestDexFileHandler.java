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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.common.DexFileHandler;
import org.opentravel.model.OtmModelManager;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;

/**
 * Verifies the functions of the <code>Dex File Handler</code>.
 */
public class TestDexFileHandler extends AbstractFxTest {
    private static Log log = LogFactory.getLog( TestDexFileHandler.class );

    public static final boolean RUN_HEADLESS = false;
    final int WATCH_TIME = 0; // How long to sleep so we can see what is happening. Can be 0.

    public final static String FXID_PROJECTCOMBO = "#projectCombo"; // if .projectCombo that would be css selector
    public final static String FILE_TESTOPENTRAVELREPO = "TestOpenTravelRepo.otp";
    public final static String FILE_TESTLOCAL = "TestLocalFiles.otp";
    public final static String FILE_TESTLOCALLIBRARY = "StandAloneLibrary.otm";
    public final static String FILE_TESTLOCALLIBRARYBASE = "base_library.otm";
    public final static String FILE_TESTLOCALLIBRARY1 = "facets1_library.otm";
    public final static String FILE_TESTLOCALLIBRARY2 = "facets2_library.otm";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestDexFileHandler.class );
        // startTestServer( "versions-repository", 9480, repositoryConfig, true, false, TestDexFileHandler.class );
        // repoManager = repositoryManager.get();
    }

    @Test
    public void testOpenOTAProject() throws Exception {
        // When the OpenTravel repository is used in the project
        OtmModelManager mgr = new OtmModelManager( null );
        ProjectManager pm = loadManagedProject( mgr.getTlModel() );
        // When the project is added to the model manager
        mgr.add( pm );

        // Then - Expect 4 libraries and 63 members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
    }

    @Test
    public void testOpenLocalProject() throws Exception {
        // When the local files are used in the project
        OtmModelManager mgr = new OtmModelManager( null );
        ProjectManager pm = loadUnmanagedProject( mgr.getTlModel() );
        mgr.add( pm );

        // Then - Expect libraries and members
        assertTrue( mgr.getLibraries().size() > 2 );
        assertTrue( !mgr.getMembers().isEmpty() );
        log.debug( "Read " + mgr.getMembers().size() + " members." );
    }

    public static void loadLocalLibrary(String path, TLModel tlModel) {
        File localLibrary = new File( wipFolder.get(), "/" + path );
        assertNotNull( localLibrary );
        assertTrue( localLibrary.exists() );

        int initialLibCount = tlModel.getAllLibraries().size();
        new DexFileHandler().openFile( localLibrary, tlModel );
        assertTrue( tlModel.getAllLibraries().size() > initialLibCount );
        log.debug( "Model now has " + tlModel.getAllLibraries().size() + " libraries." );
    }

    @Test
    public void testOpenLocalLibrary() throws Exception {
        OtmModelManager mgr = new OtmModelManager( null );

        loadLocalLibrary( FILE_TESTLOCALLIBRARY, mgr.getTlModel() );
        loadLocalLibrary( FILE_TESTLOCALLIBRARY1, mgr.getTlModel() );
        loadLocalLibrary( FILE_TESTLOCALLIBRARY2, mgr.getTlModel() );

    }
    // @AfterClass
    // public static void tearDownTests() throws Exception {
    // shutdownTestServer();
    // }


    /**
     * @return a project manager loaded from project that uses local library files
     */
    public static ProjectManager loadUnmanagedProject(TLModel tlModel) {
        DexFileHandler fileHandler = new DexFileHandler();
        File localProject = new File( wipFolder.get(), "/" + FILE_TESTLOCAL );
        assertNotNull( localProject );

        ProjectManager pm = fileHandler.openProject( localProject, tlModel, null );
        return pm;
    }

    /**
     * @return a project manager loaded from project that uses the OpenTravel repository
     */
    public static ProjectManager loadManagedProject(TLModel tlModel) {
        DexFileHandler fileHandler = new DexFileHandler();

        File repoProject = new File( wipFolder.get(), "/" + FILE_TESTOPENTRAVELREPO );
        assertNotNull( repoProject );
        ProjectManager pm = fileHandler.openProject( repoProject, tlModel, null );
        assertTrue( "Must have project items.", !pm.getAllProjectItems().isEmpty() );
        assertTrue( "Must have project items.", pm.getAllProjectItems().size() > 1 );
        return pm;
    }

    // x Add to the before class (change port number)
    // x Insert AfterClass from DiffUtil (for managed)
    // Put libraries/projects into resource/test-data
    // files copied to target/test-workspace/wip
    // TestDiffUnmanagedModels
    // see File oldLibraryFile = new ...
    // Mock nativeComponentBuilder

    @Test
    public void testFileChooser() throws Exception {
        // robot.clickOn( "Properties" );
        // robot.targetWindow( "Login Dialog" ).clickOn( "Cancel" );
    }

    @Test
    public void projectTest() throws Exception {
        // DexMainController controller = (DexMainController) application.getController();
        // int bgTasks = controller.getStatusController().getQueueSize();
        // assertTrue( bgTasks == 0 );
        //
        // robot.clickOn( FXID_PROJECTCOMBO );
        // selectFirstEntity( FXID_PROJECTCOMBO );
        // robot.sleep( 5 * WATCH_TIME );

        // try {
        // robot.clickOn( FILE_TESTPROJECT2 );
        // } catch (Exception e) {
        // // Expected if it could not find project
        // return; // all done
        // }

        // do {
        // robot.sleep( 100 );
        // bgTasks = controller.getStatusController().getQueueSize();
        // } while (bgTasks > 0);
        //
        // robot.sleep( 5 * WATCH_TIME );
        // WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        //
        // try {
        // Node price = robot.lookup( "Price" ).query();
        // WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        // assertNotNull( "Must find Price column.", price );
        // } catch (Exception e) {
        // // Expected if it could not find project
        // }

        // See TestDiffManagedModels
        // verifyThat( FXID_MEMBERTREE, (TreeTableView<MemberAndProvidersDAO> treeTableView) -> {
        // return true;
        // } );
        // robot.verifyThat( FXID_MEMBERTREE, NodeMatchers.hasChild( "Price" ) );

        // WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        // TreeTableView<MemberAndProvidersDAO> members = robot.lookup( FXID_MEMBERTREE ).query();
        // assertNotNull( members );
        // assertTrue( "Must have children in the members.", members.getRoot().getChildren().size() > 4 );
        // members.getSelectionModel().select( 1 );
        // robot.sleep( WATCH_TIME );
        // members.getSelectionModel().select( 4 );
        //
        // robot.sleep( WATCH_TIME );
        //
        // WaitForAsyncUtils.waitForFxEvents(); // make sure the event queue is empty
        // TreeTableView<PropertiesDAO> properties = robot.lookup( FXID_PROPERTIESTABLE ).query();
        // assertNotNull( properties );
        // assertTrue( "Must have children in the tree.", !properties.getRoot().getChildren().isEmpty() );
        // for (TreeItem<PropertiesDAO> c : properties.getRoot().getChildren()) {
        // properties.getSelectionModel().select( c );
        // robot.sleep( WATCH_TIME );
        // log.debug( properties.getSelectionModel().getSelectedItem().getValue().nameProperty().getValue() );
        // }
        // properties.getSelectionModel().select( 1 );
        // properties.getSelectionModel().select( 3 );
        // robot.sleep( WATCH_TIME );

        log.debug( "Done" );
    }

    // private void selectFirstEntity(String fxid) {
    // robot.clickOn( fxid );
    // robot.type( KeyCode.DOWN );
    // // robot.type( KeyCode.ENTER );
    // WaitForAsyncUtils.waitForFxEvents();
    // }

    // @Test
    // public void exitTest() throws Exception {
    // exit();
    // }
    //
    // public void exit() {
    // // Click on the file menu's exit entry
    // robot.clickOn( "File" ).clickOn( "Exit" );
    // // Click on popup dialog's exit button
    // robot.targetWindow( "Exit" ).clickOn( "Exit" );
    // }

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

    // Suggested by: https://www.youtube.com/watch?v=NG03nNpSmgU
    // Assumes class extends "ApplicationTest" private ApplicationTest at;
    //
    // @After
    // public void afterEachTest() throws TimeoutException {
    // FxToolkit.hideStage();
    //// release(new KeyCode[]{});
    //// release(new MouseButton[]{});
    // }
    // public <T extends Node> T find(final String query) {
    // return (T) lookup(query).queryAll().iterator().next();
    // }


    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }
}

