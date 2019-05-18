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

package org.opentravel.diffutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;

import javafx.scene.input.KeyCode;
import javafx.scene.web.WebView;

/**
 * Verifies the operations of the Diff-Utility for unmanaged models that reside on the local file system.
 */
public class TestDiffUnmanagedModels extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestDiffUnmanagedModels.class );
    }

    @Test
    public void testCompareUnmanagedLibraries() throws Exception {
        File oldLibraryFile = new File( wipFolder.get(), "/test-model-old.otm" );
        File newLibraryFile = new File( wipFolder.get(), "/test-model-new.otm" );
        File saveFile = new File( wipFolder.get(), "/diff-report.otm" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( oldLibraryFile, newLibraryFile );
        when( mockFileChooser.showSaveDialog( any() ) ).thenReturn( saveFile );

        robot.clickOn( "Compare Libraries" );

        robot.clickOn( "#oldLibraryFileButton" );
        verifyThat( "#oldLibraryFilename", hasText( oldLibraryFile.getName() ) );

        robot.clickOn( "#newLibraryFileButton" );
        verifyThat( "#newLibraryFilename", hasText( newLibraryFile.getName() ) );

        robot.clickOn( "#runLibraryButton" );
        robot.waitForBackgroundTask( "#runLibraryButton" );
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertEquals( "OTM Model Comparison Report", reportViewer.getEngine().getTitle() );

        // Save the report to our test WIP folder
        robot.clickOn( "#saveReportButton" );
        Assert.assertTrue( saveFile.exists() );

        // After creating the report, click a link and test the forward/back navigation buttons
        robot.interact(
            () -> reportViewer.getEngine().executeScript( "document.getElementsByTagName('a')[0].click()" ) );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( "#backButton" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( "#forwardButton" );
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testCompareLibraryEntities() throws Exception {
        File oldLibraryFile = new File( wipFolder.get(), "/test-model-old.otm" );
        File newLibraryFile = new File( wipFolder.get(), "/test-model-new.otm" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( oldLibraryFile, newLibraryFile );

        robot.clickOn( "Compare Libraries" );

        robot.clickOn( "#oldLibraryFileButton" );
        verifyThat( "#oldLibraryFilename", hasText( oldLibraryFile.getName() ) );

        robot.clickOn( "#newLibraryFileButton" );
        verifyThat( "#newLibraryFilename", hasText( newLibraryFile.getName() ) );

        robot.clickOn( "#oldEntityChoice" );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );

        robot.clickOn( "#newEntityChoice" );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );

        robot.clickOn( "#runLibraryButton" );
        robot.waitForBackgroundTask( "#runLibraryButton" );
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertEquals( "OTM Model Comparison Report", reportViewer.getEngine().getTitle() );
    }

    @Test
    public void testCompareProjects() throws Exception {
        File oldProjectFile = new File( wipFolder.get(), "/test-project-old.otp" );
        File newProjectFile = new File( wipFolder.get(), "/test-project-new.otp" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( oldProjectFile, newProjectFile );

        robot.clickOn( "#oldProjectFileButton" );
        verifyThat( "#oldProjectFilename", hasText( oldProjectFile.getName() ) );

        robot.clickOn( "#newProjectFileButton" );
        verifyThat( "#newProjectFilename", hasText( newProjectFile.getName() ) );

        robot.clickOn( "#runProjectButton" );
        robot.waitForBackgroundTask();
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertEquals( "OTM Model Comparison Report", reportViewer.getEngine().getTitle() );
    }

    @Test
    public void testCompareProjects_validationError() throws Exception {
        File oldProjectFile = new File( wipFolder.get(), "/test-project-old.otp" );
        File newProjectFile = new File( wipFolder.get(), "/test-project-error.otp" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( oldProjectFile, newProjectFile );

        robot.clickOn( "#oldProjectFileButton" );
        verifyThat( "#oldProjectFilename", hasText( oldProjectFile.getName() ) );

        robot.clickOn( "#newProjectFileButton" );
        verifyThat( "#newProjectFilename", hasText( newProjectFile.getName() ) );

        robot.clickOn( "#runProjectButton" );
        robot.waitForBackgroundTask();
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertNull( reportViewer.getEngine().getTitle() );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return OTMDiffApplication.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#runProjectButton";
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
