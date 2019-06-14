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

package org.opentravel.upversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.application.common.AbstractOTMApplication;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.utilities.testutil.AbstractFxTest;
import org.opentravel.utilities.testutil.OtmFxRobot;
import org.opentravel.utilities.testutil.TestFxMode;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

/**
 * Verifies the functions of the <code>ExampleHelper</code> application that require artifacts managed by a remote OTM
 * repository.
 */
public class TestUpversionHelper extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestUpversionHelper.class );
        startTestServer( "versions-repository", 9482, repositoryConfig, true, false, TestUpversionHelper.class );
        repoManager = repositoryManager.get();
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testSelectionFilters() throws Exception {
        // Select a namespace that contains all of our candidates
        robot.clickOn( "#namespaceChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.DOWN ).type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        robot.waitForBackgroundTask();
        validateCandidateLibraries( "LibraryA_2_0_0.otm-DRAFT", "LibraryB_2_0_0.otm-DRAFT" );

        // Turn off the latest-versions filter
        robot.clickOn( "#latestVersionsCheckbox" );
        validateCandidateLibraries( "LibraryA_1_0_0.otm-FINAL", "LibraryB_1_0_0.otm-UNDER_REVIEW",
            "LibraryA_2_0_0.otm-DRAFT", "LibraryB_2_0_0.otm-DRAFT" );

        // Specify a version identifier filter
        robot.clickOn( "#versionFilterText" ).write( "1" );
        waitForFxEvents();
        validateCandidateLibraries( "LibraryA_1_0_0.otm-FINAL", "LibraryB_1_0_0.otm-UNDER_REVIEW" );

        // Specify a status filter
        robot.clickOn( "#statusFilterChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        waitForFxEvents();
        validateCandidateLibraries( "LibraryB_1_0_0.otm-UNDER_REVIEW" );

        // Add the library to the selected items and then remove it
        robot.clickOn( "LibraryB (test-repository)" ).clickOn( "#addButton" );
        robot.clickOn( "LibraryB (test-repository)" ).clickOn( "#removeButton" );
    }

    @Test
    public void testUpversionLibraries() throws Exception {
        // Select a namespace that contains the libraries we want to upversion
        robot.clickOn( "#namespaceChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.DOWN ).type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        robot.waitForBackgroundTask();

        // Add both candidates to the list of selected libraries
        robot.clickOn( "LibraryA (test-repository)" ).clickOn( "#addButton" );
        robot.clickOn( "LibraryB (test-repository)" ).clickOn( "#addButton" );

        // Upversion the libraries and validate that they have been created on the local file system
        File upversionFolder = new File( wipFolder.get(), "/upversion-output" );
        File projectFile = new File( upversionFolder, "/new-versions.otp" );

        upversionFolder.mkdirs();
        when( mockDirectoryChooser.showDialog( any() ) ).thenReturn( upversionFolder );
        robot.clickOn( "#upversionButton" );
        robot.waitForBackgroundTask();
        assertTrue( new File( upversionFolder, "/a-0300-LibraryA_3_0_0.otm" ).exists() );
        assertTrue( new File( upversionFolder, "/b-0300-LibraryB_3_0_0.otm" ).exists() );
        assertTrue( projectFile.exists() );

        // Load the project (outside of the application) and make sure none of the prior managed versions are pulled
        // into the model
        ProjectManager projectManager = new ProjectManager( new TLModel(), false, repositoryManager.get() );
        Project upversionProject = projectManager.loadProject( projectFile );

        for (ProjectItem item : upversionProject.getProjectItems()) {
            assertEquals( "3.0.0", item.getVersion() );
        }

        // Upversion again (this time we need to confirm the overwrite of existing files)
        robot.clickOn( "#upversionButton" );
        robot.targetWindow( "Directory Not Empty" ).clickOn( "Yes" );
        robot.waitForBackgroundTask();
    }

    @Test
    public void testPromoteDemoteLibraries() throws Exception {
        OtmFxRobot dialogRobot;

        // Select a namespace that contains the libraries we want to upversion
        robot.clickOn( "#namespaceChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.DOWN ).type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        robot.waitForBackgroundTask();

        // Add both candidates to the list of selected libraries
        robot.clickOn( "LibraryA (test-repository)" ).clickOn( "#addButton" );
        robot.clickOn( "LibraryB (test-repository)" ).clickOn( "#addButton" );

        // Promote both of the selected libraries
        robot.clickOn( "#promoteOrDemoteButton" );
        dialogRobot = robot.targetWindow( "Promote/Demote Libraries" );
        dialogRobot.clickOn( "#goCloseButton" );
        dialogRobot.waitForBackgroundTask( "#goCloseButton", 5 );
        dialogRobot.clickOn( "#goCloseButton" );

        // Demote both of the selected libraries
        robot.clickOn( "#promoteOrDemoteButton" );
        dialogRobot = robot.targetWindow( "Promote/Demote Libraries" );
        dialogRobot.clickOn( "#demoteRadio" );
        dialogRobot.clickOn( "#goCloseButton" );
        dialogRobot.waitForBackgroundTask( "#goCloseButton", 5 );
        dialogRobot.clickOn( "#goCloseButton" );
    }

    @Test
    public void testImportExportProjects() throws Exception {
        File importProject = new File( wipFolder.get(), "/test-project.otp" );
        File exportProject = new File( wipFolder.get(), "/export-project.otp" );

        // Import candidate libraries from a OTM project file
        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( importProject );
        when( mockFileChooser.showSaveDialog( any() ) ).thenReturn( exportProject );
        robot.clickOn( "File" ).clickOn( "#importMenu" );

        // Add the imported items to the selected libraries
        robot.clickOn( "LibraryA (test-repository)" ).clickOn( "#addButton" );
        robot.clickOn( "LibraryB (test-repository)" ).clickOn( "#addButton" );

        robot.clickOn( "File" ).clickOn( "#exportMenu" );
        assertTrue( exportProject.exists() );
    }

    @Test
    public void testValidateSelectedLibraries() throws Exception {
        // Select a namespace that contains all of our candidates
        robot.clickOn( "#namespaceChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.DOWN ).type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );
        robot.waitForBackgroundTask();

        // Select and validate a library that contains an error
        robot.clickOn( "#latestVersionsCheckbox" );
        robot.clickOn( "#versionFilterText" ).write( "1" );
        waitForFxEvents();
        robot.clickOn( "ErrorLibrary (test-repository)" ).clickOn( "#addButton" );
        robot.clickOn( "#validationLink" );

        // Verifiy that the validation table contains a single error
        TableView<ValidationFinding> validationTable = robot.lookup( "#validationTable" ).query();
        List<ValidationFinding> findingList = validationTable.getItems();

        assertEquals( 1, findingList.size() );
        assertEquals( FindingType.ERROR, findingList.get( 0 ).getType() );
    }

    @Test
    public void testAboutDialog() throws Exception {
        // Open the application's About dialog
        robot.clickOn( "Help" ).clickOn( "#aboutMenu" );
        robot.targetWindow( "About" ).clickOn( "Close" );

        // Exit the application using the File->Exit menu selection
        robot.clickOn( "File" ).clickOn( "#exitMenu" );
    }

    private void validateCandidateLibraries(String... librarySpecs) throws Exception {
        TableView<RepositoryItemWrapper> candidateTable = robot.lookup( "#candidateLibrariesTable" ).query();
        Set<String> candidates = new HashSet<>();

        for (RepositoryItemWrapper item : candidateTable.getItems()) {
            candidates.add( item.getFilename() + "-" + item.getStatus() );
        }

        for (String librarySpec : librarySpecs) {
            if (!candidates.contains( librarySpec )) {
                fail( "Missing candidate library: " + librarySpec );
            }
            candidates.remove( librarySpec );
        }

        if (librarySpecs.length < candidates.size()) {
            StringBuilder cList = new StringBuilder();

            candidates.forEach( c -> cList.append( c ).append( " " ) );
            fail( "Unexpected candidate libraries: " + cList );
        }
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return UpversionHelperApplication.class;
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getBackgroundTaskNodeQuery()
     */
    @Override
    protected String getBackgroundTaskNodeQuery() {
        return "#repositoryChoice";
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
