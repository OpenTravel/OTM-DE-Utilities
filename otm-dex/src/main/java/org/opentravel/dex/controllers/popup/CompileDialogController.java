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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.StatusType;
import org.opentravel.common.DexFileHandler;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.dex.tasks.model.CompileProjectTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserCompilerSettings;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog controller for compiling the model.
 * <p>
 * Create the controller using the static {@link CompileDialogController#init() } method.
 * 
 * @author dmh
 *
 */
public class CompileDialogController extends DexPopupControllerBase implements TaskResultHandlerI {
    private static Logger log = LogManager.getLogger( CompileDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/CompileDialog.fxml";
    public static final String DIALOG_SETTING_LABEL = "compile_dialog";

    protected static Stage dialogStage;
    private static String helpText = "Compile Project.";
    private static String dialogTitle = "Compile Project";

    @FXML
    private TextField targetDirectoryField;
    @FXML
    private Button targetDirectoryButton;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static CompileDialogController init() {
        FXMLLoader loader = new FXMLLoader( CompileDialogController.class.getResource( LAYOUT_FILE ) );
        CompileDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            // dialogStage.initModality( Modality.APPLICATION_MODAL );
            dialogStage.initModality( Modality.NONE );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof CompileDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );

        return controller;
    }

    @FXML
    private Button saveButton;
    @FXML
    Button closeButton;
    @FXML
    Button compileButton;
    @FXML
    TextField descriptionField;
    @FXML
    ChoiceBox<OtmProject> projectChoiceBox;
    @FXML
    private ChoiceBox<String> bindingStyleChoice;
    @FXML
    private CheckBox compileXmlSchemasCheckbox;
    @FXML
    private CheckBox compileServicesCheckbox;
    @FXML
    private CheckBox compileJsonSchemasCheckbox;
    @FXML
    private CheckBox compileSwaggerCheckbox;
    @FXML
    private CheckBox compileOpenApiCheckbox;
    @FXML
    private CheckBox compileDocumentationCheckbox;
    @FXML
    private TextField serviceEndpointUrl;
    @FXML
    private TextField baseResourceUrl;
    @FXML
    private CheckBox suppressExtensionsCheckbox;
    @FXML
    private CheckBox generateExamplesCheckbox;
    @FXML
    private CheckBox exampleMaxDetailCheckbox;
    @FXML
    private Spinner<Integer> maxRepeatSpinner;
    @FXML
    private Spinner<Integer> maxRecursionDepthSpinner;
    @FXML
    private CheckBox suppressOptionalFieldsCheckbox;
    @FXML
    private TitledPane resultsPane;
    //
    @FXML
    private TableView<ValidationFinding> resultsTableView;
    @FXML
    private TableColumn<ValidationFinding,ImageView> validationLevelColumn;
    @FXML
    private TableColumn<ValidationFinding,String> validationComponentColumn;
    @FXML
    private TableColumn<ValidationFinding,String> validationDescriptionColumn;
    // @FXML
    // TextField contextIdField;
    // @FXML
    // TextField fileNameField;
    // @FXML
    // TextField directoryField;
    // @FXML
    // TextArea resultsArea;
    // @FXML
    // private Button openProjectButton;

    // private File libraryFile = null;
    private OtmModelManager modelMgr;
    // private OtmLibrary otmLibrary = null;
    private OtmProject selectedProject = null;

    private String resultText;
    private UserSettings userSettings;
    private DexStatusController statusController;

    public String getResultText() {
        return resultText;
    }

    @Override
    public void checkNodes() {
        if (saveButton == null || projectChoiceBox == null || targetDirectoryField == null || compileButton == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        // dialogHelp.getChildren().clear();
    }

    public void disableControls() {
        bindingStyleChoice.setDisable( true );
        compileXmlSchemasCheckbox.setDisable( true );
        compileServicesCheckbox.setDisable( true );
        compileJsonSchemasCheckbox.setDisable( true );
        compileSwaggerCheckbox.setDisable( true );
        compileOpenApiCheckbox.setDisable( true );
        compileDocumentationCheckbox.setDisable( true );
        serviceEndpointUrl.setDisable( true );
        baseResourceUrl.setDisable( true );
        suppressExtensionsCheckbox.setDisable( true );
        generateExamplesCheckbox.setDisable( true );
        exampleMaxDetailCheckbox.setDisable( true );
        maxRepeatSpinner.setDisable( true );
        maxRecursionDepthSpinner.setDisable( true );
        suppressExtensionsCheckbox.setDisable( true );
    }


    @FXML
    public void doCompile(ActionEvent e) {
        // log.debug( "Do compile." );

        if (selectedProject == null)
            return;
        // Get the target folder as a file
        String folderName = targetDirectoryField.getText();
        if (folderName == null)
            return;

        // Deactivate button
        resultsPane.setExpanded( false );
        compileButton.setDisable( true );
        resultsTableView.setDisable( true );
        resultsTableView.setItems( FXCollections.observableList( Collections.emptyList() ) );

        //
        updateCompileOptions();

        CompileProjectTask task =
            new CompileProjectTask( selectedProject, this, statusController, folderName, userSettings );
        task.go();
        // On completion, the handleTaskComplete method will run
    }

    @Override
    public void doOK() {
        updateCompileOptions();
        super.doOK();
    }

    public void updateCompileOptions() {
        UserCompilerSettings compilerSettings = userSettings.getCompilerSettings();

        compilerSettings.setBindingStyle( bindingStyleChoice.getSelectionModel().getSelectedItem() );
        compilerSettings.setCompileSchemas( compileXmlSchemasCheckbox.isSelected() );
        compilerSettings.setCompileServices( compileServicesCheckbox.isSelected() );
        compilerSettings.setCompileJsonSchemas( compileJsonSchemasCheckbox.isSelected() );
        compilerSettings.setCompileSwagger( compileSwaggerCheckbox.isSelected() );
        compilerSettings.setCompileOpenApi( compileOpenApiCheckbox.isSelected() );
        compilerSettings.setCompileHtml( compileDocumentationCheckbox.isSelected() );
        compilerSettings.setServiceEndpointUrl( serviceEndpointUrl.textProperty().getValue() );
        compilerSettings.setResourceBaseUrl( baseResourceUrl.textProperty().getValue() );
        compilerSettings.setSuppressOtmExtensions( suppressExtensionsCheckbox.isSelected() );
        compilerSettings.setGenerateExamples( generateExamplesCheckbox.isSelected() );
        compilerSettings.setGenerateMaxDetailsForExamples( exampleMaxDetailCheckbox.isSelected() );
        compilerSettings.setExampleMaxRepeat( maxRepeatSpinner.getValue() );
        compilerSettings.setExampleMaxDepth( maxRecursionDepthSpinner.getValue() );
        compilerSettings.setSuppressOptionalFields( suppressOptionalFieldsCheckbox.isSelected() );

        userSettings.setDimensions( DIALOG_SETTING_LABEL,
            new Dimension2D( dialogStage.getWidth(), dialogStage.getHeight() ) );

        userSettings.save();
        // log.debug( "Updated compiler user settings." );
    }

    public void post(UserSettings userSettings) {
        UserCompilerSettings compilerSettings = userSettings.getCompilerSettings();

        ObservableList<String> exIds =
            FXCollections.observableList( CompilerExtensionRegistry.getAvailableExtensionIds() );
        bindingStyleChoice.setItems( exIds );
        if (compilerSettings.getBindingStyle() != null)
            bindingStyleChoice.getSelectionModel().select( compilerSettings.getBindingStyle() );

        compileXmlSchemasCheckbox.setSelected( compilerSettings.isCompileSchemas() );
        compileServicesCheckbox.setSelected( compilerSettings.isCompileServices() );
        compileJsonSchemasCheckbox.setSelected( compilerSettings.isCompileJsonSchemas() );
        compileSwaggerCheckbox.setSelected( compilerSettings.isCompileSwagger() );
        compileOpenApiCheckbox.setSelected( compilerSettings.isCompileOpenApi() );
        compileDocumentationCheckbox.setSelected( compilerSettings.isCompileHtml() );

        serviceEndpointUrl.textProperty().setValue( compilerSettings.getServiceEndpointUrl() );
        baseResourceUrl.textProperty().setValue( compilerSettings.getResourceBaseUrl() );
        suppressExtensionsCheckbox.setSelected( compilerSettings.isSuppressOtmExtensions() );
        generateExamplesCheckbox.setSelected( compilerSettings.isGenerateExamples() );
        exampleMaxDetailCheckbox.setSelected( compilerSettings.isGenerateMaxDetailsForExamples() );

        Integer maxRepeat = compilerSettings.getExampleMaxRepeat();
        if (maxRepeatSpinner.getValueFactory() != null)
            maxRepeatSpinner.getValueFactory().setValue( (maxRepeat == null) ? 3 : maxRepeat );
        Integer maxDepth = compilerSettings.getExampleMaxDepth();
        if (maxRecursionDepthSpinner.getValueFactory() != null)
            maxRecursionDepthSpinner.getValueFactory().setValue( maxDepth == null ? 3 : maxDepth );

        suppressOptionalFieldsCheckbox.setSelected( compilerSettings.isSuppressOptionalFields() );
    }


    @FXML
    public void selectTargetDirectory(ActionEvent e) {
        // log.debug( "File selection dialog." );
        // Let user choose
        String initialDir = CompileProjectTask.getCompileDirectoryPath( selectedProject );
        DexFileHandler fh = new DexFileHandler();
        File selectedFile = fh.directoryChooser( popupStage, title, initialDir );
        post( selectedFile );
    }

    @FXML
    public void selectProject(ActionEvent e) {
        // log.debug( "TODO - project selection." );
        if (e != null && e.getTarget() instanceof ChoiceBox) {
            selectedProject = ((ChoiceBox<OtmProject>) e.getTarget()).getValue();
            post( selectedProject );
        }
    }

    // /**
    // * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#doOK()
    // */
    // @Override
    // public void doOK() {
    // super.doOK(); // all OK - close window
    // }

    @FXML
    public void doSave() {
        // log.debug( "Run save task" );
        DialogBoxContoller dialogBox = DialogBoxContoller.init();
        String results = DexFileHandler.saveLibraries( modelMgr.getEditableLibraries() );
        dialogBox.showAndWait( "Save Results", results );
    }

    /**
     * 
     * @param manager used to create project
     * @param initialProjectFolder used in user file selection dialog
     */
    public void configure(OtmModelManager manager, UserSettings settings, DexStatusController statusController) {
        this.modelMgr = manager;
        this.userSettings = settings;
        this.statusController = statusController;

        // Restore user window size settings
        Dimension size = userSettings.getWindowSize();
        dialogStage.setHeight( size.height );
        dialogStage.setWidth( size.width );
    }

    private void post(File targetDirectory) {
        if (targetDirectory != null)
            targetDirectoryField.setText( targetDirectory.getPath() );
        else
            targetDirectoryField.setText( "" );
        targetDirectoryButton.setDisable( false );
    }

    private void post(OtmModelManager mgr) {
        if (mgr != null) {
            ObservableList<OtmProject> projList = FXCollections.observableList( mgr.getProjects() );
            projectChoiceBox.setItems( projList );
            if (!projList.isEmpty())
                projectChoiceBox.getSelectionModel().select( 0 );
            selectedProject = projectChoiceBox.getSelectionModel().getSelectedItem();
            post( CompileProjectTask.getCompileDirectory( selectedProject ) );

            // Enable save button if there are changes in the queue
            if (mgr.getActionManager( true ) != null && mgr.getActionManager( true ).getQueueSize() > 0) {
                saveButton.setDisable( false );
            }
        }

    }

    private void post(ValidationFindings findings) {
        // log.debug( "Posting findings: " + findings );
        resultsPane.setExpanded( true );
        if (findings == null) {
            ValidationFinding ok = new ValidationFinding( selectedProject.getTL().getModel(), FindingType.WARNING,
                "Completed with no errors.", null );
            findings = new ValidationFindings();
            findings.addFinding( ok );
        }

        // if (findings != null)
        resultsTableView.setItems( FXCollections.observableList( findings.getAllFindingsAsList() ) );
        // else
        // resultsTableView.setItems( FXCollections.observableList( Collections.emptyList() ) );
    }

    private void post(OtmProject project) {
        post( CompileProjectTask.getCompileDirectory( project ) );
        descriptionField.setText( project.getDescription() );
    }

    @Override
    protected void setup(String message) {
        // 600w x 450h is minimum for this dialog
        // Dimension2D dimensions = new Dimension2D( 600, 500 );
        Dimension2D dimensions = null;
        if (userSettings != null)
            dimensions = userSettings.getDimensions( DIALOG_SETTING_LABEL );

        super.setStage( dialogTitle, dialogStage, dimensions );
        checkNodes();

        // Get the projects from project manager
        saveButton.setDisable( true );
        post( modelMgr );

        maxRepeatSpinner.setValueFactory( new IntegerSpinnerValueFactory( 1, 3, 3, 1 ) );
        maxRecursionDepthSpinner.setValueFactory( new IntegerSpinnerValueFactory( 1, 3, 3, 1 ) );
        maxRepeatSpinner.setEditable( true );
        maxRepeatSpinner.setDisable( false );
        post( userSettings );

        validationLevelColumn.setCellValueFactory( nodeFeatures -> {
            FindingType findingType = nodeFeatures.getValue().getType();
            Image image =
                (findingType == FindingType.WARNING) ? StatusType.WARNING.getIcon() : StatusType.ERROR.getIcon();

            return new SimpleObjectProperty<ImageView>( new ImageView( image ) );
        } );
        validationComponentColumn.setCellValueFactory(
            nodeFeatures -> new ReadOnlyStringWrapper( nodeFeatures.getValue().getSource().getValidationIdentity() ) );
        validationDescriptionColumn.setCellValueFactory( nodeFeatures -> new ReadOnlyStringWrapper(
            nodeFeatures.getValue().getFormattedMessage( FindingMessageFormat.BARE_FORMAT ) ) );
    }

    /**
     * @see org.opentravel.dex.tasks.TaskResultHandlerI#handleTaskComplete(javafx.concurrent.WorkerStateEvent)
     */
    @Override
    public void handleTaskComplete(WorkerStateEvent event) {
        ValidationFindings findings = null;
        if (event.getSource() instanceof CompileProjectTask) {
            String err = ((CompileProjectTask) event.getSource()).getErrorMsg();
            if (err != null && !err.isEmpty()) {
                ValidationFinding error =
                    new ValidationFinding( selectedProject.getTL().getModel(), FindingType.ERROR, err, null );
                findings = new ValidationFindings();
                findings.addFinding( error );
                // log.debug( err );
            } else
                findings = ((CompileProjectTask) event.getSource()).getFindings();
        }
        resultsTableView.getItems().clear();

        post( findings );
        resultsPane.setExpanded( true );
        compileButton.setDisable( false );
        resultsTableView.setDisable( false );
        // log.debug( "Compile task complete." );
    }
}
