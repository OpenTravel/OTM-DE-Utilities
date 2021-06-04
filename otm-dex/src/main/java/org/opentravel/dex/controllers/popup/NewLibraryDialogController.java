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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexFileException;
import org.opentravel.common.DexFileHandler;
import org.opentravel.common.DexProjectException;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmLocalLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.repository.ProjectItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog controller for creating a library.
 * <p>
 * Create the controller using the static {@link NewLibraryDialogController#init() } method. If the model manager is set
 * before showing, the library will be created.
 * 
 * @author dmh
 *
 */
public class NewLibraryDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( NewLibraryDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/NewLibraryDialog.fxml";

    protected static Stage dialogStage;
    private static String helpText = "Create a new library.";
    private static String dialogTitle = "Create Library Dialog";

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static NewLibraryDialogController init() {
        FXMLLoader loader = new FXMLLoader( NewLibraryDialogController.class.getResource( LAYOUT_FILE ) );
        NewLibraryDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof NewLibraryDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );

        return controller;
    }

    @FXML
    BorderPane newLibraryDialog;
    @FXML
    Label dialogTitleLabel;
    @FXML
    TextFlow dialogHelp;
    @FXML
    Button dialogButtonCancel;
    @FXML
    Button dialogButtonOK;
    @FXML
    TextField nameField;
    @FXML
    ComboBox<String> nsCombo;
    @FXML
    ComboBox<String> projectCombo;
    @FXML
    TextField descriptionField;
    @FXML
    TextField contextIdField;
    @FXML
    TextField fileNameField;
    @FXML
    TextField directoryField;
    @FXML
    TextArea resultsArea;
    @FXML
    private Button openProjectButton;

    private File libraryFile = null;
    private OtmModelManager modelMgr;
    private OtmLocalLibrary otmLibrary = null;
    private OtmProject selectedProject = null;
    private String resultText;
    private UserSettings userSettings;
    private Map<String,OtmProject> projectFileMap;
    // private OtmModelNamespaceManager nsHandler;

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
        if (newLibraryDialog == null || dialogTitleLabel == null || dialogHelp == null || dialogButtonCancel == null
            || dialogButtonOK == null || resultsArea == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    private void cleanUp(Path path) {
        try {
            Files.delete( path );
        } catch (IOException e) {
            postResults( "Could not delete " + path + " because " + e.getLocalizedMessage() );
        }
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }

    /**
     * 
     * @param manager used to create project
     * @param initialProjectFolder used in user file selection dialog
     */
    public void configure(OtmModelManager manager, UserSettings settings) {
        this.modelMgr = manager;
        this.userSettings = settings;
        // this.nsHandler = new OtmModelNamespaceManager( manager );
    }

    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#doOK()
     */
    @Override
    public void doOK() {
        if (selectedProject == null) {
            postResults( "Must select a project for the new library. " );
            return;
        }
        if (fileNameField.getText().isEmpty()) {
            postResults( "You must provide a file name for the new library. " );
            return;
        }
        if (directoryField.getText().isEmpty()) {
            postResults( "You must select a directory for the new library. " );
            return;
        }
        if (nameField.getText().isEmpty())
            nameField.setText( fileNameField.getText() );
        if (nsCombo.getValue() == null || nsCombo.getValue().isEmpty())
            nsCombo.setValue( "http://opentravel.org/Sandbox" );

        try {
            otmLibrary =
                DexFileHandler.createLibrary( getFileName(), nsCombo.getValue(), nameField.getText(), modelMgr );
        } catch (DexFileException e) {
            postResults( e.getLocalizedMessage() );
            return;
        }

        if (otmLibrary != null)
            try {
                ProjectItem pi = selectedProject.add( otmLibrary );
                if (pi == null) {
                    cleanUp( libraryFile.toPath() );
                    postResults( "Error adding new library to project." );
                    return;
                }
                otmLibrary.save();
            } catch (IllegalArgumentException er) {
                cleanUp( libraryFile.toPath() );
                postResults( "Could not create new library in model: " + er.getLocalizedMessage() );
                otmLibrary = null;
                return;
            } catch (DexProjectException e) {
                cleanUp( libraryFile.toPath() );
                postResults( "Could not add library to project: " + e.getLocalizedMessage() );
                otmLibrary = null;
                return;
            }

        log.debug( "Created library: " + otmLibrary );
        super.doOK(); // all OK - close window
    }

    /**
     * Get the newly created file or null.
     * 
     * @return
     */
    public OtmLibrary get() {
        return otmLibrary;
    }

    /**
     * Combine the directory and fileName field text
     * 
     */
    private String getFileName() {
        String fileName = directoryField.getText() + DexFileHandler.FILE_SEPARATOR + fileNameField.getText();

        if (!fileNameField.getText().endsWith( DexFileHandler.LIBRARY_FILE_EXTENSION ))
            fileName += DexFileHandler.LIBRARY_FILE_EXTENSION;

        return fileName;
    }

    public String getResultText() {
        return resultText;
    }

    private void postResults(String text) {
        resultsArea.setWrapText( true );
        if (libraryFile != null)
            resultsArea.setText( libraryFile.getAbsolutePath() + "\n" + text );
        resultsArea.setText( text );
    }

    /**
     * Event handler invoked by fxml when the selection button is pushed.
     * 
     * @param e
     */
    @FXML
    public void selectFile(ActionEvent e) {
        // log.debug( "Button: " + e.toString() );
        DexFileHandler fileHandler = new DexFileHandler();
        libraryFile = fileHandler.directoryChooser( dialogStage, "Select Library Directory", userSettings );
        if (libraryFile != null)
            directoryField.setText( libraryFile.getPath() );
    }

    private void setSelectedProject(ActionEvent e) {
        selectedProject = projectFileMap.get( projectCombo.getValue() );
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        postHelp( helpText, dialogHelp );

        setupNS();
        setupProject();

        directoryField.setText( DexFileHandler.getDefaultProjectFolder( userSettings ) );
    }

    private void setupNS() {
        // Get the namespaces
        modelMgr.getBaseNamespaces().forEach( ns -> nsCombo.getItems().add( ns ) );
        // nsHandler.getBaseNamespaces().forEach( ns -> nsCombo.getItems().add( ns ) );
        nsCombo.setEditable( true );
    }

    private void setupProject() {
        projectCombo.getItems().clear();
        projectFileMap = modelMgr.getOtmProjectManager().getOpenFileMap();
        ObservableList<String> projectList = FXCollections.observableArrayList( projectFileMap.keySet() );
        projectCombo.setEditable( false );
        projectCombo.setItems( projectList );
        if (!projectList.isEmpty()) {
            projectCombo.getSelectionModel().select( 0 );
            selectedProject = projectFileMap.get( projectCombo.getValue() );
        }
        projectCombo.setOnAction( this::setSelectedProject );
    }
}
