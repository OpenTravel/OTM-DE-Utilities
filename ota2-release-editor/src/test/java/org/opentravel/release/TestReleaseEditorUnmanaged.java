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

package org.opentravel.release;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import org.opentravel.utilities.testutil.TestFxUtils;
import org.testfx.api.FxRobot;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.input.KeyCode;

/**
 * Verifies the functions of the <code>ExampleHelper</code> application that require artifacts managed by a remote OTM
 * repository.
 */
public class TestReleaseEditorUnmanaged extends AbstractFxTest {

    public static final boolean RUN_HEADLESS = false;

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestReleaseEditorUnmanaged.class );
        startTestServer( "versions-repository", 9483, repositoryConfig, true, false, TestReleaseEditorUnmanaged.class );
        repoManager = repositoryManager.get();
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
    }

    @Test
    public void testCreateUnmanagedRelease() throws Exception {
        File saveFile = new File( wipFolder.get(), "/TestCreateRelease_1_0_0.otr" );
        FxRobot dialogRobot;

        when( mockDirectoryChooser.showDialog( any() ) ).thenReturn( saveFile.getParentFile() );

        // Create the new release
        robot.clickOn( "File" ).clickOn( "New..." );

        dialogRobot = robot.targetWindow( "New Release" );
        dialogRobot.clickOn( "#newReleaseDirectoryButton" );
        TestFxUtils.typeText( dialogRobot, "#newReleaseName", "TestCreateRelease" );
        TestFxUtils.typeText( dialogRobot, "#newReleaseBaseNamespace",
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/release-test" );
        dialogRobot.clickOn( "#okButton" );
        WaitForAsyncUtils.waitForFxEvents();

        // Add a principle library
        robot.clickOn( "#addLibraryButton" );
        TestFxUtils.selectTreeItem( robot.targetWindow( "Add Principle Library" ), "#repositoryTreeView",
            "OTM Repositories", "OTA2.0 Test Repository", "http://www.OpenTravel.org",
            "/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_1_0.otm (1.1.0)" );
        robot.targetWindow( "Add Principle Library" ).clickOn( "#okButton" );
        WaitForAsyncUtils.waitForFxEvents();

        // Add another principle library, then remove it again
        robot.clickOn( "#addLibraryButton" );
        TestFxUtils.selectTreeItem( robot.targetWindow( "Add Principle Library" ), "#repositoryTreeView",
            "OTM Repositories", "OTA2.0 Test Repository", "http://www.OpenTravel.org",
            "/ns/OTA2/SchemaCompiler/version-test", "Version_Test_1_1_1.otm (1.1.1)" );
        robot.targetWindow( "Add Principle Library" ).clickOn( "#okButton" );
        WaitForAsyncUtils.waitForFxEvents();

        robot.clickOn( "#principalTableView" ).type( KeyCode.DOWN );
        robot.clickOn( "#removeLibraryButton" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.targetWindow( "Remove Principal Library" ).clickOn( "Yes" );
        Thread.sleep( 5000 );

        // Assign a default effective date
        TestFxUtils.typeText( dialogRobot, "#defaultEffectiveDate", "May 15, 2019 4:00:00 PM" );
        robot.clickOn( "#applyToAllButton" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( "#reloadModelButton" );
        WaitForAsyncUtils.waitForFxEvents();

        // Save the release
        robot.clickOn( "File" ).clickOn( "Save" );
        assertTrue( saveFile.exists() );
    }

    @Test
    public void testPublishAndUnpublishRelease() throws Exception {
        File releaseFile = new File( wipFolder.get(), "/test-release.otr" );

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( releaseFile );
        when( mockDirectoryChooser.showDialog( any() ) ).thenReturn( wipFolder.get() );

        // Open an existing library
        robot.clickOn( "#releaseFileButton" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#releaseFileButton", 10 );
        verifyThat( "#releaseFilename", hasText( releaseFile.getName() ) );

        // Navigate the tree that displays the model contents
        TestFxUtils.navigateTreeView( robot, "#libraryTreeView" );

        // Publish the release to an OTM repository
        robot.clickOn( "Repository" ).clickOn( "#publishReleaseMenu" ).clickOn( "OTA2.0 Test Repository" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#releaseFilename", 10 );

        // Unpublish the release and save it back to the local file system
        robot.clickOn( "Repository" ).clickOn( "#unpublishReleaseMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#releaseFilename", 10 );
    }

    @Test
    public void testNewReleaseVersion() throws Exception {
        // Open a managed release
        robot.clickOn( "Repository" ).clickOn( "#openManagedMenu" );
        TestFxUtils.selectTreeItem( robot.targetWindow( "Open Managed Release" ), "#repositoryTreeView",
            "OTM Repositories", "OTA2.0 Test Repository", "http://www.OpenTravel.org",
            "/ns/OTA2/SchemaCompiler/version-test", "Version_Release_1_1_0.otr (1.1.0)" );
        robot.targetWindow( "Open Managed Release" ).clickOn( "#okButton" );
        WaitForAsyncUtils.waitForFxEvents();

        // Create a new (unmanaged) version of the release
        when( mockDirectoryChooser.showDialog( any() ) ).thenReturn( wipFolder.get() );
        robot.clickOn( "Repository" ).clickOn( "#newReleaseVersionMenu" );

        // Change the release version
        TestFxUtils.typeText( robot, "#releaseVersion", "2.0.1" );

        // Attempt to close the release (cancel to avoid losing changes)
        robot.clickOn( "File" ).clickOn( "#closeMenu" );
        robot.targetWindow( "Close Release" ).clickOn( "Cancel" );

        // Close the release (confirm loss of changes)
        robot.clickOn( "File" ).clickOn( "#closeMenu" );
        robot.targetWindow( "Close Release" ).clickOn( "No" );
    }

    @Test
    public void testImportReleaseFromProject() throws Exception {
        File projectFile = new File( wipFolder.get(), "/test-project.otp" );

        // Import a new release from an OTM project (.otp) file
        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( projectFile );
        robot.clickOn( "File" ).clickOn( "#importMenu" );

        // Modify the compile options of the release
        robot.clickOn( "Compiler Options" );
        Thread.sleep( 250 ); // Wait for accordion to expand
        robot.clickOn( "#bindingStyleChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.ENTER );

        for (int i = 0; i < 2; i++) {
            robot.clickOn( "#compileXmlSchemasCheckbox" );
            robot.clickOn( "#compileServicesCheckbox" );
            robot.clickOn( "#compileJsonSchemasCheckbox" );
            robot.clickOn( "#compileSwaggerCheckbox" );
            robot.clickOn( "#compileDocumentationCheckbox" );
        }
        TestFxUtils.setScrollPosition( robot, "#optionsScrollPane", 0.5 );
        TestFxUtils.typeText( robot, "#serviceEndpointUrl", "http://soap.opentravel.org" );
        TestFxUtils.typeText( robot, "#baseResourceUrl", "http://rest.opentravel.org" );
        robot.clickOn( "#suppressExtensionsCheckbox" );
        robot.clickOn( "#generateExamplesCheckbox" ).clickOn( "#generateExamplesCheckbox" );
        TestFxUtils.setScrollPosition( robot, "#optionsScrollPane", 1.0 );
        robot.clickOn( "#exampleMaxDetailCheckbox" );
        ((Spinner<?>) robot.lookup( "#maxRepeatSpinner" ).query()).decrement();
        ((Spinner<?>) robot.lookup( "#maxRecursionDepthSpinner" ).query()).decrement();
        robot.clickOn( "#suppressOptionalFieldsCheckbox" );

        // Modify the facet selections of the release
        ChoiceBoxTableCell<?,?> facetChoiceCell;

        robot.clickOn( "Substitution Groups (Example Facet Selections)" );
        Thread.sleep( 250 ); // Wait for accordion to expand
        facetChoiceCell = (ChoiceBoxTableCell<?,?>) robot.lookup( "Substitution Group" ).query();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );

        // Modify the library commit selections
        robot.clickOn( "Release Members" );
        Thread.sleep( 250 ); // Wait for accordion to expand
        facetChoiceCell = (ChoiceBoxTableCell<?,?>) robot.lookup( "Latest Commit" ).query();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( facetChoiceCell );
        WaitForAsyncUtils.waitForFxEvents();
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.DOWN );
        robot.type( KeyCode.ENTER );

        // Save the release to a new file (save-as)
        when( mockDirectoryChooser.showDialog( any() ) ).thenReturn( wipFolder.get() );
        robot.clickOn( "File" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.clickOn( "#saveAsMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.targetWindow( "Confirm Overwrite" ).clickOn( "Yes" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#releaseName", 10 );

        // Compile the release
        robot.clickOn( "File" ).clickOn( "#compileMenu" );
        TestFxUtils.waitUntilEnabled( robot, "#releaseName", 10 );
    }

    @Test
    public void testUndoRedo() throws Exception {
        File releaseFile = new File( wipFolder.get(), "/test-release.otr" );
        String originalValue, updatedValue;

        when( mockFileChooser.showOpenDialog( any() ) ).thenReturn( releaseFile );

        // Open an unmanaged release file
        robot.clickOn( "#releaseFileButton" );
        WaitForAsyncUtils.waitForFxEvents();
        TestFxUtils.waitUntilEnabled( robot, "#releaseFileButton", 10 );

        // Modify binding style and confirm undo/redo
        ChoiceBox<?> choiceBox;

        robot.clickOn( "Compiler Options" );
        Thread.sleep( 250 ); // Wait for accordion to expand
        choiceBox = robot.lookup( "#bindingStyleChoice" ).query();
        originalValue = choiceBox.getSelectionModel().getSelectedItem().toString();
        robot.clickOn( "#bindingStyleChoice" );
        robot.type( KeyCode.DOWN ).type( KeyCode.ENTER );
        updatedValue = choiceBox.getSelectionModel().getSelectedItem().toString();

        robot.clickOn( "Edit" ).clickOn( "#undoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( originalValue, choiceBox.getSelectionModel().getSelectedItem().toString() );
        robot.clickOn( "Edit" ).clickOn( "#redoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( updatedValue, choiceBox.getSelectionModel().getSelectedItem().toString() );

        // Modify the release version and confirm undo/redo
        TextField textField;

        textField = robot.lookup( "#releaseVersion" ).query();
        originalValue = textField.getText();
        TestFxUtils.typeText( robot, "#releaseVersion", "5.0.0" );
        updatedValue = textField.getText();

        robot.clickOn( "Edit" ).clickOn( "#undoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( originalValue, textField.getText() );
        robot.clickOn( "Edit" ).clickOn( "#redoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( updatedValue, textField.getText() );

        // Modify max repeat and confirm undo/redo
        Spinner<?> spinner;

        robot.clickOn( "Compiler Options" );
        TestFxUtils.setScrollPosition( robot, "#optionsScrollPane", 1.0 );
        Thread.sleep( 250 ); // Wait for accordion to expand
        spinner = robot.lookup( "#maxRepeatSpinner" ).query();
        originalValue = spinner.getValue().toString();
        spinner.decrement();
        updatedValue = spinner.getValue().toString();

        robot.clickOn( "Edit" ).clickOn( "#undoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( originalValue, spinner.getValue().toString() );
        robot.clickOn( "Edit" ).clickOn( "#redoMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals( updatedValue, spinner.getValue().toString() );
    }

    @Test
    public void testAboutDialog() throws Exception {
        // Open the application's About dialog
        robot.clickOn( "Help" ).clickOn( "#aboutMenu" );
        WaitForAsyncUtils.waitForFxEvents();
        robot.targetWindow( "About" ).clickOn( "Close" );

        // Exit the application using the File->Exit menu selection
        robot.clickOn( "File" ).clickOn( "#exitMenu" );
    }

    /**
     * @see org.opentravel.utilities.testutil.AbstractFxTest#getApplicationClass()
     */
    @Override
    protected Class<? extends AbstractOTMApplication> getApplicationClass() {
        return OTMReleaseApplication.class;
    }

    /**
     * Configure headless/normal mode for TestFX execution.
     */
    static {
        TestFxMode.setHeadless( RUN_HEADLESS );
    }

}
