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
import org.opentravel.common.DexProjectHandler;
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
    private static String helpText = "Create new Library.";
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
    TextField nsField;
    @FXML
    TextField projectField;
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

    @FXML
    void doOpenProject(ActionEvent e) {
        DexProjectHandler handler = new DexProjectHandler();
        selectedProject = handler.selectProject( modelMgr );
        if (selectedProject != null)
            projectField.setText( selectedProject.getName() );
        // FIXME - also implement open the project
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
        // FIXME
        // 1. Make sure there is a project selected
        // 2. Name, Namespace, Comments, Project
        // When they enter a name or fileName copy to other field
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
        if (nsField.getText().isEmpty())
            nsField.setText( "http://opentravel.org/Sandbox" );

        //
        // final File file = new File(metaData.getPath());
        // final URL fileURL = URLUtils.toURL(file);
        // final TLLibrary tlLib = new TLLibrary();
        // tlLib.setStatus(TLLibraryStatus.DRAFT);
        // tlLib.setLibraryUrl(fileURL);

        // tlLib.setPrefix(metaData.getNsPrefix());
        // tlLib.setName(metaData.getName());
        // tlLib.setComments(metaData.getComments());
        // tlLib.setNamespace(metaData.getNamespace());


        // If the project file has not been selected, try to create one using the name
        libraryFile = new File( getFileName() );
        try {
            if (!libraryFile.createNewFile()) {
                postResults( "Could not create new library file: " + libraryFile.getPath() + " already exists." );
                return;
            }
        } catch (SecurityException se) {
            log.error( "Security error creating library file: " + se.getLocalizedMessage() );
            postResults( "Access denied while creating library file: " + se.getLocalizedMessage() );
            return;
        } catch (IOException e1) {
            log.error( "IO error creating library file: " + e1.getLocalizedMessage() );
            postResults( "Error creating library file: " + e1.getLocalizedMessage() );
            return;
        }
        if (!libraryFile.canWrite()) {
            postResults( "Newly created file can not be written to. " + libraryFile.getAbsolutePath() );
            return;
        }

        if (modelMgr != null)
            try {
                log.debug( "TODO - create library in model manager: " + libraryFile.getName() );
                AbstractLibrary tlLib = DexFileHandler.createLibrary( libraryFile );
                tlLib.setOwningModel( modelMgr.getTlModel() );
                tlLib.setName( nameField.getText() );
                tlLib.setPrefix( "pf1" );
                tlLib.setNamespace( nsField.getText() );
                // TODO - refactor how lib added to project. see DexProjectHandler
                ProjectItem pi = selectedProject.getTL().getProjectManager().addUnmanagedProjectItem( tlLib,
                    selectedProject.getTL() );
                if (pi == null) {
                    postResults( "Error adding new library to project." );
                    // Files.delete( libraryFile.toPath() );
                    libraryFile.delete();
                    return;
                }
                if (tlLib instanceof TLLibrary)
                    ((TLLibrary) tlLib).setComments( "" );
                // modelMgr.newProject( libraryFile, nameField.getText(), contextIdField.getText(), idField.getText(),
                // descriptionField.getText() );
                otmLibrary = modelMgr.addUnmanaged( tlLib );
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

        log.debug( "Created library: " + libraryFile.getAbsolutePath() );
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
        // TODO - the settings should be abstracted for Dex applications
        this.modelMgr = manager;
        this.userSettings = settings;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        postHelp( helpText, dialogHelp );

        // Initial settings
        //
        projectField.setEditable( false );
        projectField.setDisable( true ); // Grey it out
        if (!modelMgr.getOtmProjectManager().hasProjects()) {
            postResults( "Must have a project for the new library." );
            dialogButtonOK.setDisable( true );
        }
        if (userSettings != null)
            directoryField.setText( userSettings.getLastProjectFolder().getPath() );
        else
            directoryField.setText( DexFileHandler.getUserHome() );

    }
}
