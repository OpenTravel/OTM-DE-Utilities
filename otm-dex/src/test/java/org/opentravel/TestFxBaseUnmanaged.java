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

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.objecteditor.ObjectEditorApp;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

/**
 * Verifies the functions of the <code>ExampleHelper</code> application when working with unmanaged OTM libraries and
 * projects.
 */
public class TestFxBaseUnmanaged extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        // setupWorkInProcessArea(TestFxBaseUnmanaged.class);
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

    @Test
    public void basicTest() throws Exception {
        System.out.println( "Testing base class worked." );
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
