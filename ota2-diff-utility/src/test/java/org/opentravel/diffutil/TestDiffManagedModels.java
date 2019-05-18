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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;

import javafx.scene.input.KeyCode;
import javafx.scene.web.WebView;

/**
 * Verifies the operations of the Diff-Utility for models that are managed in an OTM repository.
 */
public class TestDiffManagedModels extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestDiffManagedModels.class );
        startTestServer( "versions-repository", 9480, repositoryConfig, true, false, TestDiffManagedModels.class );
        repoManager = repositoryManager.get();
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testCompareManagedLibraries() throws Exception {
        robot.clickOn( "Compare Libraries" );

        robot.clickOn( "#oldLibraryRepoButton" );
        robot.targetWindow( "Select Old Library Version" ).selectTreeItem( "#repositoryTreeView", "OTM Repositories",
            "OTA2.0 Test Repository", "http://www.OpenTravel.org", "/ns/OTA2/SchemaCompiler/version-test",
            "Version_Test_1_0_0.otm (1.0.0)" );
        robot.targetWindow( "Select Old Library Version" ).clickOn( "#okButton" );

        robot.clickOn( "#oldCommitChoice" );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );

        robot.clickOn( "#newLibraryRepoButton" );
        robot.targetWindow( "Select New Library Version" ).selectTreeItem( "#repositoryTreeView", "OTM Repositories",
            "OTA2.0 Test Repository", "http://www.OpenTravel.org", "/ns/OTA2/SchemaCompiler/version-test",
            "Version_Test_1_1_0.otm (1.1.0)" );
        robot.targetWindow( "Select New Library Version" ).clickOn( "#okButton" );

        robot.clickOn( "#newCommitChoice" );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        robot.waitForBackgroundTask( "#runLibraryButton" );

        robot.clickOn( "#runLibraryButton" );
        robot.waitForBackgroundTask( "#runLibraryButton" );
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertEquals( "OTM Model Comparison Report", reportViewer.getEngine().getTitle() );
    }

    @Test
    public void testCompareManagedReleases() throws Exception {
        robot.clickOn( "#oldReleaseFileButton" );
        robot.targetWindow( "Select Old Release Version" ).selectTreeItem( "#repositoryTreeView", "OTM Repositories",
            "OTA2.0 Test Repository", "http://www.OpenTravel.org", "/ns/OTA2/SchemaCompiler/version-test",
            "Version_Release_1_0_0.otr (1.0.0)" );
        robot.targetWindow( "Select Old Release Version" ).clickOn( "#okButton" );

        robot.clickOn( "#newReleaseFileButton" );
        robot.targetWindow( "Select New Release Version" ).selectTreeItem( "#repositoryTreeView", "OTM Repositories",
            "OTA2.0 Test Repository", "http://www.OpenTravel.org", "/ns/OTA2/SchemaCompiler/version-test",
            "Version_Release_1_1_0.otr (1.1.0)" );
        robot.targetWindow( "Select New Release Version" ).clickOn( "#okButton" );

        robot.clickOn( "#runProjectButton" );
        robot.waitForBackgroundTask();
        WebView reportViewer = (WebView) robot.lookup( "#reportViewer" ).query();
        assertEquals( "OTM Model Comparison Report", reportViewer.getEngine().getTitle() );
    }

    @Test
    public void testCompareUnmanagedReleases() throws Exception {
        File oldProjectFile = new File( wipFolder.get(), "/test-release-old.otr" );
        File newProjectFile = new File( wipFolder.get(), "/test-release-new.otr" );

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
