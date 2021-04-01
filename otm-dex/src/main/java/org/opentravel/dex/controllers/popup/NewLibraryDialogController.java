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
import org.opentravel.common.DexFileHandler;
import org.opentravel.common.DexNamespaceHandler;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;

import java.io.File;
import java.io.IOException;
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
    private OtmLibrary otmLibrary = null;
    private OtmProject selectedProject = null;
    private String resultText;
    private UserSettings userSettings;
    private Map<String,OtmProject> projectFileMap;

    private DexNamespaceHandler nsHandler;

    public String getResultText() {
        return resultText;
    }

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
        if (newLibraryDialog == null || dialogTitleLabel == null || dialogHelp == null || dialogButtonCancel == null
            || dialogButtonOK == null || resultsArea == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }

    /**
     * Event handler invoked by fxml when the selection button is pushed.
     * 
     * @param e
     */
    @FXML
    public void selectFile(ActionEvent e) {
        log.debug( "Button: " + e.toString() );
        DexFileHandler fileHandler = new DexFileHandler();
        libraryFile = fileHandler.directoryChooser( dialogStage, "Select Library Directory", userSettings );
        if (libraryFile != null)
            directoryField.setText( libraryFile.getPath() );
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
        if (nsCombo.getValue().isEmpty())
            nsCombo.setValue( "http://opentravel.org/Sandbox" );

        DexFileHandler fileHandler = new DexFileHandler();
        libraryFile = fileHandler.createLibraryFile( getFileName() );
        if (libraryFile == null) {
            postResults( fileHandler.getErrorMessage() );
            return;
        }

        if (modelMgr != null)
            try {
                AbstractLibrary tlLib = DexFileHandler.createLibrary( libraryFile );
                String namespace = DexNamespaceHandler.fixNamespaceVersion( nsCombo.getValue() );
                String prefix = nsHandler.getPrefix( namespace );

                tlLib.setOwningModel( modelMgr.getTlModel() );
                tlLib.setName( nameField.getText() );
                tlLib.setNamespace( namespace );
                tlLib.setPrefix( prefix );

                // TODO - refactor how lib added to project. see DexProjectHandler
                ProjectItem pi = selectedProject.getTL().getProjectManager().addUnmanagedProjectItem( tlLib,
                    selectedProject.getTL() );
                if (pi == null) {
                    postResults( "Error adding new library to project." );
                    libraryFile.delete();
                    return;
                }
                if (tlLib instanceof TLLibrary)
                    ((TLLibrary) tlLib).setComments( "" );
                otmLibrary = modelMgr.addUnmanaged( tlLib );
                otmLibrary.add( pi );
                otmLibrary.save();
            } catch (IllegalArgumentException er) {
                postResults( "Could not create new library in model. " + er.getLocalizedMessage() );
                libraryFile.delete();
                otmLibrary = null;
                return;
            } catch (RepositoryException e) {
                postResults( "Could not add library to project." + e.getLocalizedMessage() );
                libraryFile.delete();
                otmLibrary = null;
                return;
            }

        // log.debug( "Created library: " + libraryFile.getAbsolutePath() );
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

    private void postResults(String text) {
        resultsArea.setWrapText( true );
        if (libraryFile != null)
            resultsArea.setText( libraryFile.getAbsolutePath() + "\n" + text );
        resultsArea.setText( text );
    }

    /**
     * 
     * @param manager used to create project
     * @param initialProjectFolder used in user file selection dialog
     */
    public void configure(OtmModelManager manager, UserSettings settings) {
        this.modelMgr = manager;
        this.userSettings = settings;
        this.nsHandler = new DexNamespaceHandler( manager );
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

    private void setSelectedProject(ActionEvent e) {
        selectedProject = projectFileMap.get( projectCombo.getValue() );
    }

    private void setupNS() {
        // Get the namespaces
        nsHandler.getBaseNamespaces().forEach( ns -> nsCombo.getItems().add( ns ) );
        nsCombo.setEditable( true );
    }

    // private String checkNS(String ns) {
    // String suffix = ns;
    // if (ns.lastIndexOf( '/' ) > 0)
    // suffix = ns.substring( ns.lastIndexOf( '/' ) );
    // if (!suffix.matches( "/v[0-9].*" ))
    // ns += "/v1";
    //
    // log.debug( "NS check: " + ns );
    // return ns;
    // }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        postHelp( helpText, dialogHelp );

        setupNS();
        setupProject();

        if (userSettings != null)
            directoryField.setText( userSettings.getLastProjectFolder().getPath() );
        else
            directoryField.setText( DexFileHandler.getUserHome() );

    }
}
