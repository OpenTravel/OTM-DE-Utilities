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

package org.opentravel.dex.controllers.popup;

import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.TestDexFileHandler;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.TestOtmModelManager;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

/**
 * Verifies the functions of the <code>SelectProjectDialog</code>
 */
public class TestSelectProjectDialog extends AbstractFxTest {

    private static Log log = LogFactory.getLog( TestOtmModelManager.class );

    public static final boolean RUN_HEADLESS = false;
    final int WATCH_TIME = 5000; // How long to sleep so we can see what is happening. Can be 0.

    final String FXID_PROJECTLIST = "#projectList";
    final String FXID_LIBTREETABLE = "#librariesTreeTable";

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestSelectProjectDialog.class );
        repoManager = repositoryManager.get();
    }

    @Test
    public void testSelectProjectSetup() {
        testSetup();
    }

    public OtmModelManager testSetup() {
        OtmModelManager mgr = new OtmModelManager( null, repoManager );
        // Givens - 2 projects and library that does not belong to project
        // Load first and second projects
        TestDexFileHandler.loadAndAddManagedProject( mgr );
        TestDexFileHandler.loadAndAddUnmanagedProject( mgr );

        assertTrue( mgr.getUserProjects().size() == 2 );
        int libraryCount = mgr.getLibraries().size();
        assertTrue( libraryCount > 0 );

        // Library
        TestDexFileHandler.loadLocalLibrary( TestDexFileHandler.FILE_TESTLIBRARYNOTINPROJECT, mgr.getTlModel() );
        mgr.add();
        assertTrue( mgr.getLibraries().size() > libraryCount );
        libraryCount = mgr.getLibraries().size();
        return mgr;
    }

    @Test
    public void testCancelButton() {
        // Given projects and libraries
        // OtmModelManager mgr = testSetup();
        // assertTrue( !mgr.getLibraries().isEmpty() );
        //
        // robot.clickOn( "Libraries" );
        //
        // robot.sleep( 5 * WATCH_TIME );
        // WaitForAsyncUtils.waitForFxEvents();
        // robot.clickOn( FXID_LIBTREETABLE );
        //
        // // Select first row
        // robot.type( KeyCode.DOWN );
        //
        // robot.sleep( 2 * WATCH_TIME );

        // robot.targetWindow( "Exit" ).clickOn( "Exit" );
        // robot.clickOn( FXID_PROJECTLIST );

    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return ObjectEditorApp.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#libraryText";
    }


    // @Test
    // public void testBuildExamplesFromLibrary() throws Exception {
    // File libraryFile = new File(wipFolder.get(), "/test-model.otm");
    // File saveFile = new File(wipFolder.get(), "/test-model-message.xml");
    //
    // when(mockFileChooser.showOpenDialog(any())).thenReturn(libraryFile);
    // when(mockFileChooser.showSaveDialog(any())).thenReturn(saveFile);
    //
    // robot.clickOn("#libraryFileButton");
    // robot.waitForBackgroundTask();
    //
    // exerciseExampleConfiguration();
    // }
    //
    // @Test
    // public void testBuildExamplesFromLibrary_validationError() throws Exception {
    // File libraryFile = new File(wipFolder.get(), "/test-model-error.otm");
    //
    // when(mockFileChooser.showOpenDialog(any())).thenReturn(libraryFile);
    //
    // robot.clickOn("#libraryFileButton");
    // verifyThat("#libraryText", hasText(""));
    // }
    //
    // @Test
    // public void testBuildExamplesFromProject() throws Exception {
    // File projectFile = new File(wipFolder.get(), "/test-project.otp");
    // File saveFile = new File(wipFolder.get(), "/test-project-message.xml");
    //
    // when(mockFileChooser.showOpenDialog(any())).thenReturn(projectFile);
    // when(mockFileChooser.showSaveDialog(any())).thenReturn(saveFile);
    //
    // robot.clickOn("#libraryFileButton");
    // robot.waitForBackgroundTask();
    //
    // exerciseExampleConfiguration();
    // }
    //
    // /**
    // * Performs the UI functions to adjust the configuration of an example message for a model that has been
    // pre-loaded
    // * into the Example Helper application.
    // */
    // private void exerciseExampleConfiguration() {
    // ChoiceBoxTreeTableCell<?, ?> facetChoiceCell;
    //
    // robot.clickOn("#entityChoice");
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.ENTER);
    // WaitForAsyncUtils.waitForFxEvents();
    //
    // facetChoiceCell = (ChoiceBoxTreeTableCell<?, ?>) robot.lookup("SampleBusinessObjectSummary").query();
    // robot.clickOn(facetChoiceCell);
    // robot.clickOn(facetChoiceCell);
    // robot.clickOn(facetChoiceCell);
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.ENTER);
    // WaitForAsyncUtils.waitForFxEvents();
    //
    // robot.clickOn("#bindingStyleChoice");
    // robot.type(KeyCode.DOWN);
    // robot.type(KeyCode.ENTER);
    // WaitForAsyncUtils.waitForFxEvents();
    //
    // robot.clickOn("#jsonRadio");
    // robot.clickOn("#xmlRadio");
    //
    // robot.clickOn("#repeatCountSpinner");
    // robot.type(KeyCode.UP);
    // WaitForAsyncUtils.waitForFxEvents();
    // robot.type(KeyCode.DOWN);
    // WaitForAsyncUtils.waitForFxEvents();
    //
    // robot.clickOn("#suppressOptionalFields");
    // robot.clickOn("#suppressOptionalFields");
    //
    // robot.clickOn("#saveButton");
    // robot.waitForBackgroundTask();
    // }
    //

}
