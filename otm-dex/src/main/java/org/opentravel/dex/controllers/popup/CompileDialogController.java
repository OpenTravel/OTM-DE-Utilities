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
import org.opentravel.application.common.StatusType;
import org.opentravel.dex.tasks.model.CompileProjectTask;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.objecteditor.UserSettings;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
 * Dialog controller for creating a library.
 * <p>
 * Create the controller using the static {@link CompileDialogController#init() } method. If the model manager is set
 * before showing, the library will be created.
 * 
 * @author dmh
 *
 */
public class CompileDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( CompileDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/CompileDialog.fxml";

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

    // @FXML
    // BorderPane newLibraryDialog;
    // @FXML
    // Label dialogTitleLabel;
    // @FXML
    // TextFlow dialogHelp;
    @FXML
    Button closeButton;
    @FXML
    Button compileButton;
    // @FXML
    // Button validateButton;
    // @FXML
    // TextField nameField;
    // @FXML
    // TextField nsField;
    // @FXML
    // TextField projectField;
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

    private File libraryFile = null;
    private OtmModelManager modelMgr;
    // private OtmLibrary otmLibrary = null;
    private OtmProject selectedProject = null;

    private String resultText;

    private UserSettings userSettings;

    public String getResultText() {
        return resultText;
    }

    @Override
    public void checkNodes() {
        if (projectChoiceBox == null || targetDirectoryField == null || compileButton == null)
            // || dialogButtonOK == null || resultsArea == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        // dialogHelp.getChildren().clear();
    }

    public void disableControls() {
        // newMenu.setDisable( true );
        // openMenu.setDisable( true );
        // saveMenu.setDisable( true );
        // saveAsMenu.setDisable( true );
        // compileMenu.setDisable( true );
        // closeMenu.setDisable( true );
        // exitMenu.setDisable( true );
        // undoMenu.setDisable( true );
        // redoMenu.setDisable( true );
        // addLibraryMenu.setDisable( true );
        // reloadModelMenu.setDisable( true );
        // openManagedMenu.setDisable( true );
        // publishReleaseMenu.setDisable( true );
        // newReleaseVersionMenu.setDisable( true );
        // unpublishReleaseMenu.setDisable( true );
        // aboutMenu.setDisable( true );
        // releaseFileButton.setDisable( true );
        // releaseFilename.setDisable( true );
        // releaseName.setDisable( true );
        // releaseBaseNamespace.setDisable( true );
        // releaseStatus.setDisable( true );
        // releaseVersion.setDisable( true );
        // defaultEffectiveDate.setDisable( true );
        // timeZoneLabel.setDisable( true );
        // applyToAllButton.setDisable( true );
        // releaseDescription.setDisable( true );
        // addLibraryButton.setDisable( true );
        // removeLibraryButton.setDisable( true );
        // reloadModelButton.setDisable( true );
        // principalTableView.setDisable( true );
        // referencedTableView.setDisable( true );
        bindingStyleChoice.setDisable( true );
        compileXmlSchemasCheckbox.setDisable( true );
        compileServicesCheckbox.setDisable( true );
        compileJsonSchemasCheckbox.setDisable( true );
        compileSwaggerCheckbox.setDisable( true );
        compileDocumentationCheckbox.setDisable( true );
        serviceEndpointUrl.setDisable( true );
        baseResourceUrl.setDisable( true );
        suppressExtensionsCheckbox.setDisable( true );
        generateExamplesCheckbox.setDisable( true );
        exampleMaxDetailCheckbox.setDisable( true );
        maxRepeatSpinner.setDisable( true );
        maxRecursionDepthSpinner.setDisable( true );
        suppressExtensionsCheckbox.setDisable( true );
        // facetSelectionTableView.setDisable( true );
        // validationTableView.setDisable( true );
        // libraryTreeView.setDisable( true );
    }


    @FXML
    public void doCompile(ActionEvent e) {
        log.debug( "TODO - Do compile." );
        if (selectedProject == null)
            return;

        // Get the target folder as a file
        String folderName = targetDirectoryField.getText();
        if (folderName == null)
            return;
        File targetFile = new File( folderName );
        CompileProjectTask.createCompileDirectory( targetFile );
        // FIXME - error handling

        //
        updateCompileOptions();

        // Run the compile
        ValidationFindings findings = CompileProjectTask.compile( targetFile, selectedProject, userSettings );

        resultsTableView.getItems().clear();
        post( findings );
    }

    public void updateCompileOptions() {
        CompilerExtensionRegistry.setActiveExtension( bindingStyleChoice.getValue() );
        userSettings.setCompileSchemas( compileXmlSchemasCheckbox.isSelected() );
        userSettings.setCompileServices( compileServicesCheckbox.isSelected() );
        userSettings.setCompileJsonSchemas( compileJsonSchemasCheckbox.isSelected() );
        userSettings.setCompileSwagger( compileSwaggerCheckbox.isSelected() );
        userSettings.setCompileHtml( compileDocumentationCheckbox.isSelected() );
        userSettings.setServiceEndpointUrl( serviceEndpointUrl.textProperty().getValue() );
        userSettings.setResourceBaseUrl( baseResourceUrl.textProperty().getValue() );
        userSettings.setSuppressOtmExtensions( suppressExtensionsCheckbox.isSelected() );
        userSettings.setGenerateExamples( generateExamplesCheckbox.isSelected() );
        userSettings.setGenerateMaxDetailsForExamples( exampleMaxDetailCheckbox.isSelected() );
        userSettings.setExampleMaxRepeat( maxRepeatSpinner.getValue() );
        userSettings.setExampleMaxDepth( maxRecursionDepthSpinner.getValue() );
        userSettings.setSuppressOptionalFields( suppressOptionalFieldsCheckbox.isSelected() );
    }

    public void post(UserSettings userSettings) {
        // FIXME
        // options.setBindingStyle( bindingStyleChoice.getValue() );
        // CompilerExtensionRegistry.getActiveExtension();
        // CompilerExtensionRegistry.getAvailableExtensionIds();
        // CompilerExtensionRegistry.setActiveExtension( bindingStyleChoice.getValue() );
        ObservableList<String> exIds =
            FXCollections.observableList( CompilerExtensionRegistry.getAvailableExtensionIds() );
        bindingStyleChoice.setItems( exIds );

        compileXmlSchemasCheckbox.setSelected( userSettings.isCompileSchemas() );
        compileServicesCheckbox.setSelected( userSettings.isCompileServices() );
        compileJsonSchemasCheckbox.setSelected( userSettings.isCompileJsonSchemas() );
        compileSwaggerCheckbox.setSelected( userSettings.isCompileSwagger() );
        compileDocumentationCheckbox.setSelected( userSettings.isCompileHtml() );

        serviceEndpointUrl.textProperty().setValue( userSettings.getServiceEndpointUrl() );
        baseResourceUrl.textProperty().setValue( userSettings.getResourceBaseUrl() );
        suppressExtensionsCheckbox.setSelected( userSettings.isSuppressOtmExtensions() );
        generateExamplesCheckbox.setSelected( userSettings.isGenerateExamples() );
        exampleMaxDetailCheckbox.setSelected( userSettings.isGenerateMaxDetailsForExamples() );

        Integer maxRepeat = userSettings.getExampleMaxRepeat();
        if (maxRepeatSpinner.getValueFactory() != null)
            maxRepeatSpinner.getValueFactory().setValue( (maxRepeat == null) ? 3 : maxRepeat );
        Integer maxDepth = userSettings.getExampleMaxDepth();
        if (maxRecursionDepthSpinner.getValueFactory() != null)
            maxRecursionDepthSpinner.getValueFactory().setValue( maxDepth == null ? 3 : maxDepth );
        log.debug( " value factory: " + maxRepeatSpinner.getValueFactory() + " Tried to post " + maxRepeat + " got "
            + maxRepeatSpinner.getValue() );

        suppressOptionalFieldsCheckbox.setSelected( userSettings.isSuppressOptionalFields() );
    }

    @FXML
    public void doValidation(ActionEvent e) {
        log.debug( "TODO - Do validate." );
    }

    @FXML
    public void selectTargetDirectory(ActionEvent e) {
        log.debug( "TODO - File selection dialog." );
    }

    @FXML
    public void selectProject(ActionEvent e) {
        log.debug( "TODO - project selection." );
        if (e != null && e.getTarget() instanceof ChoiceBox) {
            selectedProject = ((ChoiceBox<OtmProject>) e.getTarget()).getValue();
            post( selectedProject );
        }
    }

    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#doOK()
     */
    @Override
    public void doOK() {

        super.doOK(); // all OK - close window
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

    private void post(File targetDirectory) {
        if (targetDirectory != null)
            targetDirectoryField.setText( targetDirectory.getPath() );
        else
            targetDirectoryField.setText( "" );
        targetDirectoryButton.setDisable( true ); // TODO
    }

    private void post(OtmModelManager mgr) {
        if (mgr != null) {
            ObservableList<OtmProject> projList = FXCollections.observableList( mgr.getUserProjects() );
            projectChoiceBox.setItems( projList );
            if (!projList.isEmpty())
                projectChoiceBox.getSelectionModel().select( 0 );
            selectedProject = projectChoiceBox.getSelectionModel().getSelectedItem();
            post( CompileProjectTask.getCompileDirectory( selectedProject ) );
        }
    }

    private void post(ValidationFindings findings) {
        resultsPane.setExpanded( true );
        resultsTableView.setItems( FXCollections.observableList( findings.getAllFindingsAsList() ) );
        // resultsTableView
        // for (ValidationFinding f : findings.getAllFindingsAsList()) {
        // // Type == source.getName == MessageOnly
        // Validatable source = f.getSource();
        // String key = f.getMessageKey();
        // String type = f.getType().getDisplayName();
        // String bare = f.getFormattedMessage( FindingMessageFormat.BARE_FORMAT );
        // String m = f.getFormattedMessage( FindingMessageFormat.MESSAGE_ONLY_FORMAT );
        // String d = f.getFormattedMessage( FindingMessageFormat.DEFAULT );
        // log.debug( "Do do do" );
        // }
    }

    private void post(OtmProject project) {
        post( CompileProjectTask.getCompileDirectory( project ) );
        // TODO - descriptionField.setText( project.getDescription() );
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        checkNodes();

        // Get the projects from project manager
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

    // compileXmlSchemasCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( compileXmlSchemasCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // compileServicesCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( compileServicesCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // compileJsonSchemasCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( compileJsonSchemasCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // compileSwaggerCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( compileSwaggerCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // compileDocumentationCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( compileDocumentationCheckbox.selectedProperty(),
    // oldValue, undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // serviceEndpointUrl.textProperty().addListener(
    // (observable, oldValue, newValue) -> new WritableValueUndoableAction<>( serviceEndpointUrl.textProperty(),
    // oldValue, undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // baseResourceUrl.textProperty().addListener(
    // (observable, oldValue, newValue) -> new WritableValueUndoableAction<>( baseResourceUrl.textProperty(),
    // oldValue, undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // suppressExtensionsCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( suppressExtensionsCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // generateExamplesCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( generateExamplesCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // exampleMaxDetailCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( exampleMaxDetailCheckbox.selectedProperty(), oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // maxRepeatSpinner.valueProperty()
    // .addListener( (observable, oldValue, newValue) -> new SpinnerUndoableAction<>( maxRepeatSpinner, oldValue,
    // undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // maxRecursionDepthSpinner.valueProperty()
    // .addListener( (observable, oldValue, newValue) -> new SpinnerUndoableAction<>( maxRecursionDepthSpinner,
    // oldValue, undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
    // suppressOptionalFieldsCheckbox.selectedProperty()
    // .addListener( (observable, oldValue,
    // newValue) -> new WritableValueUndoableAction<>( suppressOptionalFieldsCheckbox.selectedProperty(),
    // oldValue, undoManager, OTMReleaseController.this::handleCompileOptionModified ).submit() );
}
