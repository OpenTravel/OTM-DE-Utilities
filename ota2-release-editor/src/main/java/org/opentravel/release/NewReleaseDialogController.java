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

import org.opentravel.application.common.DirectoryChooserDelegate;
import org.opentravel.application.common.NativeComponentBuilder;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the About appication dialog.
 */
public class NewReleaseDialogController {

    public static final String FXML_FILE = "/new-release-dialog.fxml";

    private static final Logger log = LoggerFactory.getLogger( NewReleaseDialogController.class );

    private Stage dialogStage;

    @FXML
    private TextField newReleaseDirectory;
    @FXML
    private TextField newReleaseName;
    @FXML
    private TextField newReleaseBaseNamespace;
    @FXML
    private Button newReleaseDirectoryButton;
    @FXML
    private Button okButton;

    private NativeComponentBuilder nativeComponentBuilder;
    private File releaseFolder;
    private boolean okSelected = false;

    /**
     * Initializes the dialog stage and controller used to create a new OTM release.
     * 
     * @param initialDirectory the initial directory for the release (may be null)
     * @param stage the stage that will own the new dialog
     * @param nativeComponentBuilder the native component builder to use for when creating dialog's file chooser
     * @return NewReleaseDialogController
     */
    public static NewReleaseDialogController createNewReleaseDialog(File initialDirectory, Stage stage,
        NativeComponentBuilder nativeComponentBuilder) {
        NewReleaseDialogController controller = null;
        try {
            FXMLLoader loader = new FXMLLoader( NewReleaseDialogController.class.getResource( FXML_FILE ) );
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            Scene scene = new Scene( page );

            dialogStage.setTitle( "New Release" );
            dialogStage.initModality( Modality.WINDOW_MODAL );
            dialogStage.initOwner( stage );
            dialogStage.setScene( scene );

            controller = loader.getController();
            controller.nativeComponentBuilder = nativeComponentBuilder;
            controller.initialize( dialogStage, initialDirectory );

        } catch (IOException e) {
            log.error( "Error creating new-release dialog.", e );
        }
        return controller;
    }

    /**
     * Called when the user clicks the button to select a directory for the new release.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void selectReleaseDirectory(ActionEvent event) {
        DirectoryChooserDelegate chooser;
        File selectedFolder;

        // Make sure the initial directory for the chooser exists
        while ((releaseFolder != null) && !releaseFolder.exists()) {
            releaseFolder = releaseFolder.getParentFile();
        }
        if (releaseFolder == null) {
            releaseFolder = new File( System.getProperty( "user.home" ) );
        }
        chooser = nativeComponentBuilder.newDirectoryChooser( "Select Release Directory", releaseFolder );
        selectedFolder = chooser.showDialog( dialogStage );

        if (selectedFolder != null) {
            newReleaseDirectory.setText( selectedFolder.getAbsolutePath() );
            releaseFolder = selectedFolder;
        }
    }

    /**
     * Called when the user clicks the Ok button of the dialog.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void okSelected(ActionEvent event) {
        okSelected = true;
        dialogStage.close();
    }

    /**
     * Called when the user clicks the Ok button of the dialog.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void cancelSelected(ActionEvent event) {
        dialogStage.close();
    }

    /**
     * Validates the field values of all visual fields. This method must be called from within the UI thread.
     */
    private void validateFields() {
        boolean isValid = true;

        isValid &= Validator.validateTextField( newReleaseDirectory, "release directory", -1, true );
        isValid &= Validator.validateTextField( newReleaseName, "release name", 256, true );
        isValid &= Validator.validateURLTextField( newReleaseBaseNamespace, "base namespace", true );
        okButton.setDisable( !isValid );
    }

    /**
     * Assigns the stage for the dialog.
     *
     * @param dialogStage the dialog stage to assign
     * @param initialDirectory the initial directory for the release (may be null)
     */
    private void initialize(Stage dialogStage, File initialDirectory) {
        if (initialDirectory != null) {
            newReleaseDirectory.setText( initialDirectory.getAbsolutePath() );
            this.releaseFolder = initialDirectory;
        }
        newReleaseDirectory.textProperty().addListener( (observable, oldValue, newValue) -> validateFields() );
        newReleaseName.textProperty().addListener( (observable, oldValue, newValue) -> validateFields() );
        newReleaseBaseNamespace.textProperty().addListener( (observable, oldValue, newValue) -> validateFields() );
        okButton.setDisable( true );
        this.dialogStage = dialogStage;
    }

    /**
     * Displays the dialog and returns a <code>NewReleaseInfo</code> instance if the user selects Ok.
     * 
     * @return NewReleaseInfo
     * @throws RepositoryException thrown if the remote repository cannot be accessed
     * @throws LibrarySaveException thrown if the new release cannot be saved to the local file system
     */
    public NewReleaseInfo showDialog() throws RepositoryException, LibrarySaveException {
        NewReleaseInfo releaseInfo = null;

        dialogStage.showAndWait();

        if (okSelected) {
            releaseInfo =
                new NewReleaseInfo( releaseFolder, newReleaseName.getText(), newReleaseBaseNamespace.getText() );
        }
        return releaseInfo;
    }

    /**
     * Encapsulates all of the fields required to create a new OTM release instance.
     */
    public static class NewReleaseInfo {

        private File releaseDirectory;
        private String releaseName;
        private String releaseBaseNamespace;

        /**
         * Full constructor.
         * 
         * @param releaseDirectory the directory where the new release will be created
         * @param releaseName the name of the new release
         * @param releaseBaseNamespace the base namespace of the new release
         */
        public NewReleaseInfo(File releaseDirectory, String releaseName, String releaseBaseNamespace) {
            this.releaseDirectory = releaseDirectory;
            this.releaseName = releaseName;
            this.releaseBaseNamespace = releaseBaseNamespace;
        }

        /**
         * Returns the the directory where the new release will be created.
         *
         * @return File
         */
        public File getReleaseDirectory() {
            return releaseDirectory;
        }

        /**
         * Returns the name of the new release.
         *
         * @return String
         */
        public String getReleaseName() {
            return releaseName;
        }

        /**
         * Returns the base namespace of the new release.
         *
         * @return String
         */
        public String getReleaseBaseNamespace() {
            return releaseBaseNamespace;
        }

    }

}
