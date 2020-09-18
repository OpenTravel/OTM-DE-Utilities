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
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for creating a project dialog box pop-up menu.
 * <p>
 * Create the controller using the static {@link NewProjectDialogController#init() } method. If the model manager is set
 * before showing, the project will be created.
 * 
 * @author dmh
 *
 */
public class NewProjectDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( NewProjectDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/NewProjectDialog.fxml";

    protected static Stage dialogStage;
    private static String helpText = "Select the project.";
    private static String dialogTitle = "Project Selection Dialog";

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static NewProjectDialogController init() {
        FXMLLoader loader = new FXMLLoader( NewProjectDialogController.class.getResource( LAYOUT_FILE ) );
        NewProjectDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );
            dialogStage.getScene().getStylesheets().add( "DavesViper.css" );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof NewProjectDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );

        return controller;
    }

    @FXML
    BorderPane newProjectDialog;
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
    TextField idField;
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

    private File projFile = null;
    private OtmModelManager modelMgr;

    private String resultText;


    private UserSettings userSettings;

    public String getResultText() {
        return resultText;
    }

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );

        if (newProjectDialog == null || dialogTitleLabel == null || dialogHelp == null || dialogButtonCancel == null
            || dialogButtonOK == null || resultsArea == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }


    /**
     * Select the directory button action event handler.
     * 
     * @param e
     */
    @FXML
    public void selectFile(ActionEvent e) {
        log.debug( "Button: " + e.toString() );
        DexFileHandler fileHandler = new DexFileHandler();
        projFile = fileHandler.directoryChooser( dialogStage, "Select Project Directory", userSettings );
        if (projFile != null)
            directoryField.setText( projFile.getPath() );
    }

    private boolean canExit() {
        if (!checkFileName())
            return false;
        if (modelMgr == null) {
            postResults( "Internal error. Please cancel." );
            return false;
        }
        if (contextIdField.getText().isEmpty()) {
            postResults( "Missing Context ID." );
            return false;
        }
        if (idField.getText().isEmpty()) {
            postResults( "Missing ID." );
            return false;
        }
        return true;
    }

    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#doOK()
     */
    @Override
    public void doOK() {
        if (!canExit())
            return;

        // try to create project file using the names
        projFile = new File( getFileName() );
        try {
            if (!projFile.createNewFile()) {
                postResults( "Could not create new project file: " + projFile.getPath() + " already exists." );
                return;
            }
        } catch (SecurityException se) {
            log.error( "Security error creating project file: " + se.getLocalizedMessage() );
            postResults( "Access denied while creating project file: " + se.getLocalizedMessage() );
            return;
        } catch (IOException e1) {
            log.error( "IO error creating project file: " + e1.getLocalizedMessage() );
            postResults( "Error creating project file: " + e1.getLocalizedMessage() );
            return;
        }
        if (!projFile.canWrite()) {
            postResults( "Newly created file can not be written to. " + projFile.getAbsolutePath() );
            return;
        }

        // Use model manager to create the project using the file and data.
        OtmProject newProject = null;
        try {
            newProject = modelMgr.newProject( projFile, nameField.getText(), contextIdField.getText(),
                idField.getText(), descriptionField.getText() );
        } catch (Exception er) {
            postResults( "Could not create new project in model. " + er.getLocalizedMessage() );
            projFile.delete();
            return;
        }

        // Now, close and reopen. See TestProject for details on why this patch is needed.
        if (newProject != null && newProject.getTL() != null) {
            newProject.close();
            new DexFileHandler().openProject( projFile, modelMgr, null );
        }

        log.debug( "Created project: " + projFile.getAbsolutePath() );
        super.doOK(); // all OK - close window
    }

    /**
     * Combine the directory and fileName field text
     * 
     */
    private String getFileName() {
        String fileName = directoryField.getText() + DexFileHandler.FILE_SEPARATOR + fileNameField.getText();

        if (!fileNameField.getText().endsWith( DexFileHandler.PROJECT_FILE_EXTENSION ))
            fileName += DexFileHandler.PROJECT_FILE_EXTENSION;

        return fileName;
    }

    private void postResults(String text) {
        resultsArea.setWrapText( true );
        if (projFile != null)
            resultsArea.setText( projFile.getAbsolutePath() + "\n" + text );
        resultsArea.setText( text );
    }

    /**
     * 
     * @param manager used to create project
     * @param initialProjectFolder used in user file selection dialog
     */
    public void configure(OtmModelManager manager, UserSettings settings) {
        // TODO - the settings should be abstracted for Dex applications
        this.modelMgr = manager;
        this.userSettings = settings;
    }

    public boolean checkFileName() {
        if (checkDirectory()) {
            if (fileNameField.getText().isEmpty())
                return false;
            File tmpFile = new File( getFileName() );
            if (tmpFile.exists()) {
                postResults( "Project already exists." );
                return false;
            }
            postResults( "File name OK." );
        }

        if (nameField.getText().isEmpty())
            nameField.setText( fileNameField.getText() );
        return true;
    }

    /**
     * Check directory and post any errors found
     * 
     * @return true if directory is writable
     */
    public boolean checkDirectory() {
        if (!directoryField.getText().isEmpty()) {
            File tmpDir = new File( directoryField.getText() );
            String cPath = "";
            try {
                cPath = tmpDir.getCanonicalPath();
            } catch (IOException e) {
                postResults( "Error accessing directory: " + getFileName() + "\n" + e.getLocalizedMessage() );
                return false;
            }
            if (!tmpDir.isDirectory())
                postResults( "Invalid directory: " + cPath );
            else if (!tmpDir.canWrite())
                postResults( "Can't create file in the directory: " + cPath );
            else
                return true;
        }
        return false;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );

        fileNameField.setOnAction( e -> checkFileName() );
        directoryField.setOnAction( e -> checkFileName() );

        postHelp( helpText, dialogHelp );

        if (userSettings != null)
            directoryField.setText( userSettings.getLastProjectFolder().getPath() );
        else
            directoryField.setText( DexFileHandler.getUserHome() );
    }
}
