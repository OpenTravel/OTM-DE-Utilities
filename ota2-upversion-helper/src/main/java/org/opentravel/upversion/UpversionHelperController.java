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

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.DirectoryChooserDelegate;
import org.opentravel.application.common.FileChooserDelegate;
import org.opentravel.application.common.OtmApplicationException;
import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.application.common.RepositoryItemWrapper;
import org.opentravel.application.common.StatusType;
import org.opentravel.ns.ota2.project_v01_00.ManagedProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.project_v01_00.ProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryRefType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryReferencesType;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryAvailabilityChecker;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * JavaFX controller class for the OTM Upversion Helper application.
 */
public class UpversionHelperController extends AbstractMainWindowController {

    public static final String FXML_FILE = "/ota2-upversion-helper.fxml";

    @FXML
    private MenuItem importMenu;
    @FXML
    private MenuItem exportMenu;
    @FXML
    private MenuItem exitMenu;
    @FXML
    private MenuItem upversionMenu;
    @FXML
    private MenuItem promoteOrDemoteMenu;
    @FXML
    private MenuItem validationMenu;
    @FXML
    private MenuItem aboutMenu;
    @FXML
    private ChoiceBox<String> repositoryChoice;
    @FXML
    private ChoiceBox<String> namespaceChoice;
    @FXML
    private CheckBox latestVersionsCheckbox;
    @FXML
    private Label versionFilterLabel;
    @FXML
    private TextField versionFilterText;
    @FXML
    private ChoiceBox<StatusChoice> statusFilterChoice;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private TableView<RepositoryItemWrapper> candidateLibrariesTable;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> candidateNameColumn;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> candidateVersionColumn;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> candidateStatusColumn;
    @FXML
    private TableView<RepositoryItemWrapper> selectedLibrariesTable;
    @FXML
    private TableColumn<RepositoryItemWrapper,Boolean> selectedCheckboxColumn;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> selectedNameColumn;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> selectedVersionColumn;
    @FXML
    private TableColumn<RepositoryItemWrapper,String> selectedStatusColumn;
    @FXML
    private TableView<ValidationFinding> validationTable;
    @FXML
    private TableColumn<ValidationFinding,ImageView> validationLevelColumn;
    @FXML
    private TableColumn<ValidationFinding,String> validationComponentColumn;
    @FXML
    private TableColumn<ValidationFinding,String> validationDescriptionColumn;
    @FXML
    private Button upversionButton;
    @FXML
    private Button promoteOrDemoteButton;
    @FXML
    private Hyperlink validationLink;
    @FXML
    private ImageView statusBarIcon;
    @FXML
    private Label statusBarLabel;
    @FXML
    private ProgressBar upversionProgressBar;

    private List<RepositoryItem> selectedNamespaceItems = new ArrayList<>();
    private List<RepositoryItem> selectedNamespaceLatestVersions = new ArrayList<>();
    private Map<String,Pattern> versionFilterPatterns = new HashMap<>();
    private boolean validationDirty = false;

    /**
     * Called when the user clicks the menu import selected libraries from an OTM project (OTP) file.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void importFromOTP(ActionEvent event) {
        UserSettings userSettings = UserSettings.load();
        FileChooserDelegate chooser = newFileChooser( "Import from OTM Project", userSettings.getProjectFolder(),
            OTP_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
        File selectedFile = chooser.showOpenDialog( getPrimaryStage() );

        if (selectedFile != null) {
            Runnable r = new BackgroundTask( "Importing libraries...", StatusType.INFO ) {
                public void execute() throws OtmApplicationException {
                    try {
                        ProjectType otpContent = new ProjectFileUtils().loadJaxbProjectFile( selectedFile, null );
                        ObservableList<RepositoryItemWrapper> candidateItems =
                            getCandidateRepositoryItems( otpContent );

                        Platform.runLater( () -> {
                            namespaceChoice.getSelectionModel().clearSelection();
                            candidateLibrariesTable.setItems( candidateItems );
                            updateControlStates();
                        } );

                    } catch (Exception e) {
                        throw new OtmApplicationException( e );

                    } finally {
                        userSettings.setProjectFolder( selectedFile.getParentFile() );
                        userSettings.save();
                    }
                }

            };

            new Thread( r ).start();
        }
    }

    /**
     * Returns a list of candidate repository items from the OTM project.
     * 
     * @param otpContent the OTM project from which to return managed repository items
     * @return ObservableList&lt;RepositoryItemWrapper&gt;
     */
    private ObservableList<RepositoryItemWrapper> getCandidateRepositoryItems(ProjectType otpContent) {
        List<RepositoryItemWrapper> selectedItems = selectedLibrariesTable.getItems();
        ObservableList<RepositoryItemWrapper> candidateItems = FXCollections.observableArrayList();

        for (JAXBElement<? extends ProjectItemType> piElement : otpContent.getProjectItemBase()) {
            ProjectItemType piValue = piElement.getValue();

            if (piValue instanceof ManagedProjectItemType) {
                try {
                    ManagedProjectItemType pi = (ManagedProjectItemType) piValue;
                    Repository repository = getRepositoryManager().getRepository( pi.getRepository() );
                    RepositoryItem item =
                        repository.getRepositoryItem( pi.getBaseNamespace(), pi.getFilename(), pi.getVersion() );

                    if ((item != null) && !selectedItems.contains( item )) {
                        candidateItems.add( new RepositoryItemWrapper( item ) );
                    }

                } catch (RepositoryException e) {
                    // Ignore and proceed to next managed item
                }
            }
        }
        Collections.sort( candidateItems );
        return candidateItems;
    }

    /**
     * Called when the user clicks the menu export selected libraries to a new OTM project (OTP) file.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void exportToOTP(ActionEvent event) {
        UserSettings userSettings = UserSettings.load();
        FileChooserDelegate chooser = newFileChooser( "Export to OTM Project", userSettings.getProjectFolder(),
            OTP_EXTENSION_FILTER, ALL_EXTENSION_FILTER );
        File selectedFile = chooser.showSaveDialog( getPrimaryStage() );

        if (selectedFile != null) {
            Runnable r = new BackgroundTask( "Exporting libraries...", StatusType.INFO ) {
                public void execute() throws OtmApplicationException {
                    exportLibraries( selectedFile, userSettings );
                }
            };

            new Thread( r ).start();
        }
    }

    /**
     * Exports the current list of selected libraries to the specified OTP file.
     * 
     * @param otpFile the project file to which the libraries will be exported
     * @param userSettings the user's preference settings
     * @throws OtmApplicationException thrown if an error occurs while saving the project file
     */
    private void exportLibraries(File otpFile, UserSettings userSettings) throws OtmApplicationException {
        try {
            String projectId = namespaceChoice.getSelectionModel().getSelectedItem();
            Map<String,Repository> repositoryMap = new HashMap<>();
            RepositoryReferencesType repoRefs = new RepositoryReferencesType();
            ProjectType otp = new ProjectType();

            otp.setName( otpFile.getName().replace( ".otp", "" ) );
            otp.setProjectId( (projectId != null) ? projectId : "http://www.opentravel.org" );

            for (RepositoryItem item : getSelectedItems()) {
                ManagedProjectItemType pi = new ManagedProjectItemType();
                Repository itemRepo = item.getRepository();
                ObjectFactory objFactory = new ObjectFactory();

                if (!repositoryMap.containsKey( itemRepo.getId() )) {
                    repositoryMap.put( itemRepo.getId(), itemRepo );
                }
                pi.setRepository( itemRepo.getId() );
                pi.setBaseNamespace( item.getBaseNamespace() );
                pi.setFilename( item.getFilename() );
                pi.setVersion( item.getVersion() );
                otp.getProjectItemBase().add( objFactory.createManagedProjectItem( pi ) );
            }

            for (Entry<String,Repository> entry : repositoryMap.entrySet()) {
                RepositoryRefType repoRef = new RepositoryRefType();

                repoRef.setRepositoryId( entry.getKey() );
                repoRef.setValue( ((RemoteRepository) entry.getValue()).getEndpointUrl() );
                repoRefs.getRepositoryRef().add( repoRef );
            }
            otp.setRepositoryReferences( repoRefs );
            new ProjectFileUtils().saveProjectFile( otp, otpFile );

        } catch (Exception e) {
            throw new OtmApplicationException( e );

        } finally {
            userSettings.setProjectFolder( otpFile.getParentFile() );
            userSettings.save();
        }
    }

    /**
     * Called when the user clicks the menu to add items to the list of selected libraries.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void addSelectedLibraries(ActionEvent event) {
        List<RepositoryItemWrapper> selectedItems =
            new ArrayList<>( candidateLibrariesTable.getSelectionModel().getSelectedItems() );
        List<RepositoryItemWrapper> candidateLibraries = candidateLibrariesTable.getItems();
        ObservableList<RepositoryItemWrapper> selectedLibraries =
            FXCollections.observableArrayList( selectedLibrariesTable.getItems() );

        validationDirty = true;
        candidateLibraries.removeAll( selectedItems );
        candidateLibrariesTable.getSelectionModel().clearSelection();
        selectedItems.forEach( w -> w.setSelected( true ) );
        selectedLibraries.addAll( selectedItems );
        Collections.sort( selectedLibraries );
        selectedLibrariesTable.setItems( selectedLibraries );
    }

    /**
     * Called when the user clicks the menu to add items to the list of selected libraries.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void removeSelectedLibraries(ActionEvent event) {
        List<RepositoryItemWrapper> selectedItems =
            new ArrayList<>( selectedLibrariesTable.getSelectionModel().getSelectedItems() );
        List<RepositoryItemWrapper> selectedLibraries = selectedLibrariesTable.getItems();
        ObservableList<RepositoryItemWrapper> candidateLibraries =
            FXCollections.observableArrayList( candidateLibrariesTable.getItems() );

        validationDirty = true;
        selectedLibraries.removeAll( selectedItems );
        selectedLibrariesTable.getSelectionModel().clearSelection();
        candidateLibraries.addAll( selectedItems );
        Collections.sort( candidateLibraries );
        candidateLibrariesTable.setItems( candidateLibraries );

        if (selectedLibrariesTable.getItems().isEmpty()) {
            displayValidationFindings( null );
        }
    }

    /**
     * Called when the user clicks the menu to up-version each item in the list of selected libraries.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void upversionSelectedLibraries(ActionEvent event) {
        UserSettings userSettings = UserSettings.load();
        DirectoryChooserDelegate chooser =
            newDirectoryChooser( "Select Upversion Output Folder", userSettings.getOutputFolder() );
        File selectedFolder = chooser.showDialog( getPrimaryStage() );
        boolean confirmPurgeExistingFiles = !UpversionOrchestrator.hasExistingFiles( selectedFolder );

        if (!confirmPurgeExistingFiles) {
            Alert alert = new Alert( AlertType.CONFIRMATION );
            Optional<ButtonType> dialogResult;

            alert.setTitle( "Directory Not Empty" );
            alert.setHeaderText( null );
            alert.setContentText( "The directory contains existing files that will be deleted.  Continue?" );

            alert.getButtonTypes().setAll( ButtonType.YES, ButtonType.NO );
            dialogResult = alert.showAndWait();
            confirmPurgeExistingFiles = dialogResult.isPresent() && (dialogResult.get() == ButtonType.YES);
        }

        if (confirmPurgeExistingFiles) {
            Runnable r = new BackgroundTask( "Upversioning selected files...", StatusType.INFO ) {
                public void execute() throws OtmApplicationException {
                    upversionSelectedLibraries( selectedFolder, userSettings );
                }
            };

            new Thread( r ).start();
        }
    }

    /**
     * Performs the up-versioning of all libraries selected by the user.
     * 
     * @param selectedFolder the folder where the up-versioned libraries will be saved
     * @param userSettings the user's current preference settings
     */
    private void upversionSelectedLibraries(File selectedFolder, UserSettings userSettings) {
        String errorMessage = null;

        try {
            ProgressMonitor monitor = new ProgressMonitor( upversionProgressBar );
            UpversionOrchestrator orchestrator = new UpversionOrchestrator();
            List<RepositoryItem> upversionLibraries = new ArrayList<>();
            List<RepositoryItem> supportingLibraries = new ArrayList<>();

            for (RepositoryItemWrapper item : selectedLibrariesTable.getItems()) {
                if (item.isSelected()) {
                    upversionLibraries.add( item.getItem() );

                } else {
                    supportingLibraries.add( item.getItem() );
                }
            }

            Platform.runLater( () -> upversionProgressBar.setDisable( false ) );

            orchestrator.setRepositoryManager( getRepositoryManager() ).setOldVersions( upversionLibraries )
                .setSupportingLibraries( supportingLibraries ).setOutputFolder( selectedFolder )
                .setProjectId( "http://www.travelport.com" ).setProgressMonitor( monitor ).createNewVersions();

        } catch (ValidationException e) {
            errorMessage = "The model contains one or more validation errors.";
            displayValidationFindings( e.getFindings() );

        } catch (Exception e) {
            errorMessage =
                String.format( "An error occurred during the up-version processing:%n%n%s", getErrorMessage( e ) );

        } finally {
            Platform.runLater( () -> {
                upversionProgressBar.setDisable( true );
                upversionProgressBar.setProgress( 0.0 );
            } );
            userSettings.setOutputFolder( selectedFolder );
            userSettings.save();

            if (errorMessage != null) {
                showErrorDialog( "Up-Versioning Error", errorMessage );
            }
        }
    }

    /**
     * Called when the user clicks the menu to promote or demote each item in the list of selected libraries.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void promoteOrDemoteSelectedLibraries(ActionEvent event) {
        List<RepositoryItem> selectedItems = new ArrayList<>( selectedLibrariesTable.getItems() );
        PromoteDemoteDialogController.createDialog( selectedItems, getPrimaryStage(), getRepositoryManager() )
            .showAndWait();
        ObservableList<RepositoryItemWrapper> refreshedItems = FXCollections.observableArrayList();

        getRepositoryManager().resetDownloadCache();

        for (RepositoryItem item : selectedItems) {
            try {
                Repository itemRepo = item.getRepository();
                RepositoryItem rItem;

                if (itemRepo instanceof RemoteRepository) {
                    ((RemoteRepository) itemRepo).downloadContent( item, true );
                }
                rItem = getRepositoryManager().getRepositoryItem( item.getBaseNamespace(), item.getFilename(),
                    item.getVersion() );
                refreshedItems.add( new RepositoryItemWrapper( rItem ) );

            } catch (RepositoryException e) {
                // Ignore and do not include this item in the refreshed list
            }
        }

        Platform.runLater( () -> {
            selectedLibrariesTable.setItems( refreshedItems );
            selectedLibrariesTable.refresh();
        } );
    }

    /**
     * Called when the user changes the checkbox state of the 'Latest Versions' control.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void latestVersionFilterChanged(ActionEvent event) {
        updateControlStates();
        applyCandidateItemFilters();
    }

    /**
     * Called when the user clicks the link to validate the selected libraries.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void validationLinkClicked(ActionEvent event) {
        Runnable r = new BackgroundTask( "Validating selected libraries...", StatusType.INFO ) {
            public void execute() throws OtmApplicationException {
                try {
                    List<RepositoryItem> itemList = new ArrayList<>( selectedLibrariesTable.getItems() );
                    ValidationFindings findings = null;

                    Platform.runLater( () -> upversionProgressBar.setDisable( false ) );

                    if (!itemList.isEmpty()) {
                        findings = new ValidationOrchestrator().setRepositoryManager( getRepositoryManager() )
                            .setRepositoryItems( itemList )
                            .setProgressMonitor( new ProgressMonitor( upversionProgressBar ) ).runValidation();
                    }
                    displayValidationFindings( findings );

                } catch (SchemaCompilerException e) {
                    showErrorDialog( "Unexpected Error",
                        "An error occurred during model validation: " + getErrorMessage( e ) );

                } finally {
                    Platform.runLater( () -> {
                        upversionProgressBar.setDisable( true );
                        upversionProgressBar.setProgress( 0.0 );
                    } );
                }
            }
        };

        new Thread( r ).start();
    }

    /**
     * Displays the given list of validation findings to the user.
     * 
     * @param findings the validation findings to display
     */
    private void displayValidationFindings(ValidationFindings findings) {
        if (findings != null) {
            validationTable.setItems( FXCollections.observableArrayList( findings.getAllFindingsAsList() ) );

        } else {
            validationTable.setItems( FXCollections.emptyObservableList() );
        }
        validationDirty = false;
        updateControlStates();
    }

    /**
     * Called when the user clicks the menu to exit the application.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void exitApplication(ActionEvent event) {
        getPrimaryStage().close();
    }

    /**
     * Called when the user clicks the menu to display the about-application dialog.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void aboutApplication(ActionEvent event) {
        AboutDialogController.createAboutDialog( getPrimaryStage() ).showAndWait();
    }

    /**
     * Called when the user modifies the selection of the 'repositoryChoice' control.
     */
    private void repositorySelectionChanged() {
        Runnable r = new BackgroundTask( "Updating namespaces from remote repository...", StatusType.INFO ) {
            public void execute() throws OtmApplicationException {
                try {
                    String rid = repositoryChoice.getSelectionModel().getSelectedItem();
                    Repository repository = getRepositoryManager().getRepository( rid );
                    List<String> baseNamespaces = repository.listBaseNamespaces();

                    baseNamespaces.add( 0, null );
                    Platform.runLater( () -> {
                        namespaceChoice.setItems( FXCollections.observableList( baseNamespaces ) );
                        namespaceChoice.getSelectionModel().select( 0 );
                    } );

                } catch (Exception e) {
                    throw new OtmApplicationException( e );
                }
            }
        };

        new Thread( r ).start();
    }

    /**
     * Called when the user modifies the selection of the 'namespaceChoice' control.
     */
    private void namespaceSelectionChanged() {
        String selectedNS = namespaceChoice.getSelectionModel().getSelectedItem();

        if (selectedNS != null) {
            Runnable r =
                new BackgroundTask( "Updating candidate libraries from remote repository...", StatusType.INFO ) {
                    public void execute() throws OtmApplicationException {
                        try {
                            String rid = repositoryChoice.getSelectionModel().getSelectedItem();
                            Repository repository = getRepositoryManager().getRepository( rid );
                            List<String> candidateNamespaces = getCandidateNamespaces();

                            loadSelectedNamespaceItems( repository, candidateNamespaces );
                            applyCandidateItemFilters();

                        } catch (Exception e) {
                            throw new OtmApplicationException( e );
                        }
                    }

                };

            new Thread( r ).start();

        } else {
            Platform.runLater( () -> {
                candidateLibrariesTable.setItems( FXCollections.emptyObservableList() );
                updateControlStates();
            } );
        }
    }

    /**
     * Returns the list of candidate namespaces that are either equal to or sub-namespaces of the currently selected
     * namespace.
     * 
     * @return List&lt;String&gt;
     */
    private List<String> getCandidateNamespaces() {
        List<String> candidateNamespaces = new ArrayList<>();
        String selectedNS = namespaceChoice.getSelectionModel().getSelectedItem();
        String nsPrefix = selectedNS + "/";

        candidateNamespaces.add( selectedNS );

        for (String ns : namespaceChoice.getItems()) {
            if ((ns != null) && ns.startsWith( nsPrefix )) {
                candidateNamespaces.add( ns );
            }
        }
        return candidateNamespaces;
    }

    /**
     * Retrieves all candidate repository items from the selected namespaces of the given repository.
     * 
     * @param repository the repository from which to retrieve candidate repository items
     * @param candidateNamespaces the list of candidate namespaces from the user's current selection
     * @throws RepositoryException thrown if an error occurs while accessing the remote repository
     */
    private void loadSelectedNamespaceItems(Repository repository, List<String> candidateNamespaces)
        throws RepositoryException {
        List<RepositoryItem> latestItems = new ArrayList<>();
        List<RepositoryItem> nsItems = new ArrayList<>();

        for (String candidateNS : candidateNamespaces) {
            List<RepositoryItem> itemList = repository.listItems( candidateNS, null, false );
            Set<String> libraryNames = new HashSet<>();

            for (RepositoryItem item : itemList) {
                if (!libraryNames.contains( item.getLibraryName() )) {
                    libraryNames.add( item.getLibraryName() );
                    latestItems.add( item );
                }
                nsItems.add( item );
            }
        }
        selectedNamespaceLatestVersions = latestItems;
        selectedNamespaceItems = nsItems;
    }

    /**
     * Applies the current version and status filters to the list of repository items for the currently selected
     * namespace. All items that match the filter criteria will be populated in the <code>candidateLibrariesTable</code>
     * (except for those items that have already been moved to the <code>selectedLibrariesTable</code>.
     */
    private void applyCandidateItemFilters() {
        List<RepositoryItemWrapper> selectedItems = selectedLibrariesTable.getItems();
        List<RepositoryItemWrapper> candidateItems = new ArrayList<>();

        for (RepositoryItem item : selectedNamespaceItems) {
            RepositoryItemWrapper itemWrapper = new RepositoryItemWrapper( item );

            if (!selectedItems.contains( itemWrapper ) && isFilterMatch( itemWrapper.getItem() )) {
                candidateItems.add( itemWrapper );
            }
        }
        Collections.sort( candidateItems );

        Platform.runLater( () -> candidateLibrariesTable.setItems( FXCollections.observableList( candidateItems ) ) );
    }

    /**
     * Returns true if the given repository item matches the filter criteria specified by the user.
     * 
     * @param item the repository item to evaluate
     * @return boolean
     */
    private boolean isFilterMatch(RepositoryItem item) {
        TLLibraryStatus statusFilter = statusFilterChoice.getSelectionModel().getSelectedItem().getStatus();
        boolean filterMatch = (statusFilter == null) || (statusFilter == item.getStatus());

        if (filterMatch) {
            if (latestVersionsCheckbox.isSelected()) {
                filterMatch = selectedNamespaceLatestVersions.contains( item );

            } else {
                Pattern versionFilter = getVersionFilterPattern();

                filterMatch = (versionFilter != null) && versionFilter.matcher( item.getVersion() ).matches();
            }
        }
        return filterMatch;
    }

    /**
     * Returns the regular expression pattern to use for verifying a filter match against a user-specified pattern.
     * 
     * @return Pattern
     */
    private Pattern getVersionFilterPattern() {
        String filterText = versionFilterText.getText() + "*";
        String filterRegex = filterText.replace( ".", "\\." ).replace( "*", ".*?" );

        if (!versionFilterPatterns.containsKey( filterRegex )) {
            try {
                versionFilterPatterns.put( filterRegex, Pattern.compile( filterRegex ) );

            } catch (Exception e) {
                versionFilterPatterns.put( filterRegex, null );
            }
        }
        return versionFilterPatterns.get( filterRegex );
    }

    /**
     * Returns the list of repository items from the selected-libraries table that have been selected/checked by the
     * user.
     * 
     * @return List&lt;RepositoryItem&gt;
     */
    private List<RepositoryItem> getSelectedItems() {
        List<RepositoryItem> itemList = new ArrayList<>();

        for (RepositoryItemWrapper item : selectedLibrariesTable.getItems()) {
            if (item.isSelected()) {
                itemList.add( item.getItem() );
            }
        }
        return itemList;
    }

    /**
     * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String,
     *      org.opentravel.application.common.StatusType, boolean)
     */
    @Override
    protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
        Platform.runLater( () -> {
            statusBarLabel.setText( message );
            statusBarIcon.setImage( (statusType == null) ? null : statusType.getIcon() );

            if (disableControls) {
                List<MenuItem> menus = Arrays.asList( importMenu, exportMenu, upversionMenu, promoteOrDemoteMenu );
                List<Control> controls = Arrays.asList( repositoryChoice, namespaceChoice, addButton, removeButton,
                    candidateLibrariesTable, selectedLibrariesTable, upversionButton, promoteOrDemoteButton,
                    latestVersionsCheckbox, versionFilterText, statusFilterChoice );

                menus.forEach( m -> m.setDisable( true ) );
                controls.forEach( c -> c.setDisable( true ) );

            } else {
                updateControlStates();
            }
        } );
    }

    /**
     * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
     */
    @Override
    protected void updateControlStates() {
        Platform.runLater( () -> {
            boolean selectedItemsEmpty = getSelectedItems().isEmpty();
            boolean allowValidation = validationDirty && !selectedLibrariesTable.getItems().isEmpty();
            boolean candidateItemSelected = !candidateLibrariesTable.getSelectionModel().getSelectedItems().isEmpty();
            boolean selectedItemSelected = !selectedLibrariesTable.getSelectionModel().getSelectedItems().isEmpty();
            boolean latestVersionsSelected = latestVersionsCheckbox.isSelected();

            importMenu.setDisable( false );
            exportMenu.setDisable( selectedItemsEmpty );
            upversionMenu.setDisable( selectedItemsEmpty );
            promoteOrDemoteMenu.setDisable( selectedItemsEmpty );
            validationMenu.setDisable( !allowValidation );

            repositoryChoice.setDisable( false );
            namespaceChoice.setDisable( false );
            candidateLibrariesTable.setDisable( false );
            selectedLibrariesTable.setDisable( false );
            addButton.setDisable( !candidateItemSelected );
            removeButton.setDisable( !selectedItemSelected );
            validationLink.setDisable( !allowValidation );
            upversionButton.setDisable( selectedItemsEmpty );
            promoteOrDemoteButton.setDisable( selectedItemsEmpty );
            versionFilterLabel.setDisable( latestVersionsSelected );
            versionFilterText.setDisable( latestVersionsSelected );
            latestVersionsCheckbox.setDisable( false );
            statusFilterChoice.setDisable( false );
        } );
    }

    /**
     * @see org.opentravel.application.common.AbstractMainWindowController#initialize(javafx.stage.Stage)
     */
    @Override
    @SuppressWarnings("squid:MaximumInheritanceDepth") // Unavoidable since the base class is from core JavaFXx
    protected void initialize(Stage primaryStage) {
        RepositoryAvailabilityChecker availabilityChecker =
            RepositoryAvailabilityChecker.getInstance( getRepositoryManager() );

        availabilityChecker.pingAllRepositories( false );
        super.initialize( primaryStage );

        // Configure listeners for choice/text boxes
        repositoryChoice.valueProperty()
            .addListener( (observable, oldValue, newValue) -> repositorySelectionChanged() );
        namespaceChoice.valueProperty().addListener( (observable, oldValue, newValue) -> namespaceSelectionChanged() );
        versionFilterText.textProperty().addListener( (observable, oldValue, newValue) -> applyCandidateItemFilters() );
        statusFilterChoice.valueProperty()
            .addListener( (observable, oldValue, newValue) -> applyCandidateItemFilters() );

        // Configure cell values and tooltips for table views
        Callback<CellDataFeatures<RepositoryItemWrapper,String>,ObservableValue<String>> nameColumnValueFactory =
            nodeFeatures -> {
                RepositoryItem item = nodeFeatures.getValue();
                return new ReadOnlyStringWrapper(
                    (item == null) ? "" : (item.getLibraryName() + " (" + item.getRepository().getId() + ")") );
            };
        Callback<CellDataFeatures<RepositoryItemWrapper,String>,ObservableValue<String>> versionColumnValueFactory =
            nodeFeatures -> {
                RepositoryItem item = nodeFeatures.getValue();
                return new ReadOnlyStringWrapper( (item == null) ? "" : item.getVersion() );
            };
        Callback<CellDataFeatures<RepositoryItemWrapper,String>,ObservableValue<String>> statusColumnValueFactory =
            nodeFeatures -> {
                RepositoryItem item = nodeFeatures.getValue();
                return new ReadOnlyStringWrapper(
                    (item == null) ? "" : MessageBuilder.formatMessage( item.getStatus().toString() ) );
            };
        Callback<TableView<RepositoryItemWrapper>,TableRow<RepositoryItemWrapper>> rowFactory =
            tv -> new TableRow<RepositoryItemWrapper>() {
                public void updateItem(RepositoryItemWrapper item, boolean empty) {
                    super.updateItem( item, empty );
                    if (item == null) {
                        setTooltip( null );
                    } else {
                        Tooltip tooltip = new Tooltip();
                        tooltip.setText( getItem().getNamespace() );
                        setTooltip( tooltip );
                    }
                }
            };

        candidateNameColumn.setCellValueFactory( nameColumnValueFactory );
        candidateVersionColumn.setCellValueFactory( versionColumnValueFactory );
        candidateStatusColumn.setCellValueFactory( statusColumnValueFactory );
        candidateLibrariesTable.setRowFactory( rowFactory );
        candidateLibrariesTable.getSelectionModel().selectedItemProperty()
            .addListener( (observable, oldValue, newValue) -> updateControlStates() );

        selectedCheckboxColumn.setCellFactory( CheckBoxTableCell.forTableColumn( i -> {
            ObservableBooleanValue selectedProperty = selectedLibrariesTable.getItems().get( i ).selectedProperty();
            selectedProperty.addListener( l -> updateControlStates() );
            return selectedProperty;
        } ) );
        selectedNameColumn.setCellValueFactory( nameColumnValueFactory );
        selectedVersionColumn.setCellValueFactory( versionColumnValueFactory );
        selectedStatusColumn.setCellValueFactory( statusColumnValueFactory );
        selectedLibrariesTable.setRowFactory( rowFactory );
        selectedLibrariesTable.getSelectionModel().selectedItemProperty()
            .addListener( (observable, oldValue, newValue) -> updateControlStates() );

        // Configure multiselect and placeholder labels for empty tables
        candidateLibrariesTable.setPlaceholder( new Label( "" ) );
        candidateLibrariesTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        selectedLibrariesTable.setPlaceholder( new Label( "" ) );
        selectedLibrariesTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
        validationTable.setPlaceholder( new Label( "" ) );
        validationTable.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );

        // Assign proportional column widths and diable resizing/reordering
        List<TableColumn<?,?>> columns = Arrays.asList( candidateNameColumn, candidateStatusColumn,
            candidateVersionColumn, selectedNameColumn, selectedStatusColumn, selectedVersionColumn );

        for (TableColumn<?,?> column : columns) {
            column.setResizable( false );
            column.setSortable( false );
        }
        candidateNameColumn.prefWidthProperty()
            .bind( candidateLibrariesTable.widthProperty().multiply( 0.6 ).subtract( 1 ) );
        candidateVersionColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply( 0.2 ) );
        candidateStatusColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply( 0.2 ) );
        selectedNameColumn.prefWidthProperty()
            .bind( selectedLibrariesTable.widthProperty().multiply( 0.6 ).subtract( 14 ) );
        selectedVersionColumn.prefWidthProperty()
            .bind( selectedLibrariesTable.widthProperty().multiply( 0.2 ).subtract( 6 ) );
        selectedStatusColumn.prefWidthProperty()
            .bind( selectedLibrariesTable.widthProperty().multiply( 0.2 ).subtract( 6 ) );

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
        validationComponentColumn.prefWidthProperty().bind( validationTable.widthProperty().multiply( 0.4 ) );
        validationDescriptionColumn.prefWidthProperty()
            .bind( validationTable.widthProperty().multiply( 0.6 ).subtract( 20 ) );

        // Initialize data and control states
        ObservableList<StatusChoice> statusChoices = FXCollections.observableArrayList();

        for (TLLibraryStatus status : TLLibraryStatus.values()) {
            statusChoices.add( new StatusChoice( status ) );
        }
        statusChoices.add( 0, new StatusChoice( null ) );
        statusFilterChoice.setItems( statusChoices );
        statusFilterChoice.getSelectionModel().select( 0 );
        latestVersionsCheckbox.setSelected( true );
        candidateLibrariesTable.setItems( FXCollections.emptyObservableList() );
        selectedLibrariesTable.setItems( FXCollections.emptyObservableList() );

        updateControlStates();
        initRepositoryChoice();
    }

    /**
     * Initializes the list of repositories that are selectable by the user.
     */
    private void initRepositoryChoice() {
        Runnable r = new BackgroundTask( "Initializing from remote repositories...", StatusType.INFO ) {
            public void execute() throws OtmApplicationException {
                try {
                    ObservableList<String> repositoryIds = FXCollections.observableArrayList();

                    getRepositoryManager().listRemoteRepositories().forEach( r -> repositoryIds.add( r.getId() ) );
                    repositoryChoice.setItems( repositoryIds );
                    repositoryChoice.getSelectionModel().select( 0 );

                } catch (Exception e) {
                    throw new OtmApplicationException( e );
                }
            }
        };

        new Thread( r ).start();
    }

    /**
     * Provides a selectable value and display label for library statuses.
     */
    private static class StatusChoice {

        private TLLibraryStatus status;
        private String label;

        /**
         * Constructor that specifies the library status that can be selected.
         * 
         * @param status the library status value
         */
        public StatusChoice(TLLibraryStatus status) {
            String labelKey = (status == null) ? "ANY_STATUS" : status.toString();

            this.status = status;
            this.label = MessageBuilder.formatMessage( labelKey );
        }

        /**
         * Returns library status value.
         *
         * @return TLLibraryStatus
         */
        public TLLibraryStatus getStatus() {
            return status;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return label;
        }

    }

}
