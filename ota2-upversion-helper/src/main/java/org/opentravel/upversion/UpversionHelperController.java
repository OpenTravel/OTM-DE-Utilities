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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.application.common.StatusType;
import org.opentravel.ns.ota2.project_v01_00.ManagedProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.project_v01_00.ProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryRefType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryReferencesType;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryAvailabilityChecker;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.ProjectFileUtils;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * JavaFX controller class for the OTM Upversion Helper application.
 */
public class UpversionHelperController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-upversion-helper.fxml";
	
	@FXML private MenuItem importMenu;
	@FXML private MenuItem exportMenu;
	@FXML private MenuItem upversionMenu;
	@FXML private MenuItem promoteOrDemoteMenu;
	@FXML private ChoiceBox<String> repositoryChoice;
	@FXML private ChoiceBox<String> namespaceChoice;
	@FXML private Button addButton;
	@FXML private Button removeButton;
	@FXML private TableView<RepositoryItemWrapper> candidateLibrariesTable;
	@FXML private TableColumn<RepositoryItemWrapper,String> candidateNameColumn;
	@FXML private TableColumn<RepositoryItemWrapper,String> candidateVersionColumn;
	@FXML private TableColumn<RepositoryItemWrapper,String> candidateStatusColumn;
	@FXML private TableView<RepositoryItemWrapper> selectedLibrariesTable;
	@FXML private TableColumn<RepositoryItemWrapper,String> selectedNameColumn;
	@FXML private TableColumn<RepositoryItemWrapper,String> selectedVersionColumn;
	@FXML private TableColumn<RepositoryItemWrapper,String> selectedStatusColumn;
	@FXML private Button upversionButton;
	@FXML private Button promoteOrDemoteButton;
	@FXML private ImageView statusBarIcon;
	@FXML private Label statusBarLabel;
	@FXML private ProgressBar upversionProgressBar;
	
	private RepositoryManager repositoryManager;
	private RepositoryAvailabilityChecker availabilityChecker;
	
	/**
	 * Default constructor.
	 */
	public UpversionHelperController() {
		try {
			repositoryManager = RepositoryManager.getDefault();
			availabilityChecker = RepositoryAvailabilityChecker.getInstance( repositoryManager );
			availabilityChecker.pingAllRepositories( true );
			
		} catch (RepositoryException e) {
			e.printStackTrace( System.out );
		}
	}

	/**
	 * Called when the user clicks the menu import selected libraries from
	 * an OTM project (OTP) file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void importFromOTP(ActionEvent event) {
		UserSettings userSettings = UserSettings.load();
		FileChooser chooser = newFileChooser( "Import from OTM Project",
				userSettings.getProjectFolder(),
				new String[] { "*.otp", "OTM Project Files" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Importing libraries...", StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						ProjectType otpContent = ProjectFileUtils.loadJaxbProjectFile( selectedFile, null );
						List<RepositoryItemWrapper> selectedItems = selectedLibrariesTable.getItems();
						ObservableList<RepositoryItemWrapper> candidateItems = FXCollections.observableArrayList();
						
						for (JAXBElement<? extends ProjectItemType> piElement : otpContent.getProjectItemBase()) {
							ProjectItemType piValue = piElement.getValue();
							
							if (piValue instanceof ManagedProjectItemType) {
								try {
									ManagedProjectItemType pi = (ManagedProjectItemType) piValue;
									Repository repository = repositoryManager.getRepository( pi.getRepository() );
									RepositoryItem item = repository.getRepositoryItem(
											pi.getBaseNamespace(), pi.getFilename(), pi.getVersion() );
									
									if ((item != null) && !selectedItems.contains( item )) {
										candidateItems.add( new RepositoryItemWrapper( item ) );
									}
									
								} catch (RepositoryException e) {
									// Ignore and proceed to next managed item
								}
							}
						}
						Collections.sort( candidateItems );
						
						Platform.runLater( () -> {
							namespaceChoice.getSelectionModel().clearSelection();
							candidateLibrariesTable.setItems( candidateItems );
							updateControlStates();
						});
						
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
	 * Called when the user clicks the menu export selected libraries to
	 * a new OTM project (OTP) file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void exportToOTP(ActionEvent event) {
		UserSettings userSettings = UserSettings.load();
		FileChooser chooser = newFileChooser( "Export to OTM Project",
				userSettings.getProjectFolder(),
				new String[] { "*.otp", "OTM Project Files" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showSaveDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Exporting libraries...", StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						String projectId = namespaceChoice.getSelectionModel().getSelectedItem();
						Map<String,Repository> repositoryMap = new HashMap<>();
						RepositoryReferencesType repoRefs = new RepositoryReferencesType();
						ProjectType otp = new ProjectType();
						
						otp.setName( selectedFile.getName().replace( ".otp", "" ) );
						otp.setProjectId( (projectId != null) ? projectId : "http://www.opentravel.org" );
						
						for (RepositoryItem item : selectedLibrariesTable.getItems()) {
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
						ProjectFileUtils.saveProjectFile( otp, selectedFile );
						
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
	 * Called when the user clicks the menu to add items to the list of
	 * selected libraries.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void addSelectedLibraries(ActionEvent event) {
		List<RepositoryItemWrapper> selectedItems = new ArrayList<>(
				candidateLibrariesTable.getSelectionModel().getSelectedItems() );
		List<RepositoryItemWrapper> candidateLibraries = candidateLibrariesTable.getItems();
		ObservableList<RepositoryItemWrapper> selectedLibraries =
				FXCollections.observableArrayList( selectedLibrariesTable.getItems() );
		
		candidateLibraries.removeAll( selectedItems );
		candidateLibrariesTable.getSelectionModel().clearSelection();
		selectedLibraries.addAll( selectedItems );
		Collections.sort( selectedLibraries );
		selectedLibrariesTable.setItems( selectedLibraries );
	}
	
	/**
	 * Called when the user clicks the menu to add items to the list of
	 * selected libraries.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void removeSelectedLibraries(ActionEvent event) {
		List<RepositoryItemWrapper> selectedItems = new ArrayList<>(
				selectedLibrariesTable.getSelectionModel().getSelectedItems() );
		List<RepositoryItemWrapper> selectedLibraries = selectedLibrariesTable.getItems();
		ObservableList<RepositoryItemWrapper> candidateLibraries =
				FXCollections.observableArrayList( candidateLibrariesTable.getItems() );
		
		selectedLibraries.removeAll( selectedItems );
		selectedLibrariesTable.getSelectionModel().clearSelection();
		candidateLibraries.addAll( selectedItems );
		Collections.sort( candidateLibraries );
		candidateLibrariesTable.setItems( candidateLibraries );
	}
	
	/**
	 * Called when the user clicks the menu to up-version each item in the
	 * list of selected libraries.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void upversionSelectedLibraries(ActionEvent event) {
		UserSettings userSettings = UserSettings.load();
		DirectoryChooser chooser = newDirectoryChooser( "Select Upversion Output Folder",
				userSettings.getOutputFolder() );
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
			confirmPurgeExistingFiles = (dialogResult.get() == ButtonType.YES);
		}
		
		if (confirmPurgeExistingFiles) {
			Runnable r = new BackgroundTask( "Upversioning selected files...", StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						List<RepositoryItem> selectedItems = new ArrayList<>( selectedLibrariesTable.getItems() );
						ProgressMonitor monitor = new ProgressMonitor( upversionProgressBar );
						UpversionOrchestrator orchestrator = new UpversionOrchestrator();
						String selectedNS = namespaceChoice.getSelectionModel().getSelectedItem();
						
						if (selectedNS == null) {
							selectedNS = "http://www.opentravel.org";
						}
						
						Platform.runLater( new Runnable() {
							public void run() {
								upversionProgressBar.setDisable( false );
							}
						});
						
						orchestrator
							.setRepositoryManager( repositoryManager )
							.setOldVersions( selectedItems )
							.setOutputFolder( selectedFolder )
							.setProjectId( "http://www.travelport.com" )
							.setProgressMonitor( monitor )
							.createNewVersions();
							
						Platform.runLater( new Runnable() {
							public void run() {
								upversionProgressBar.setDisable( true );
								upversionProgressBar.setProgress( 0.0 );
							}
						});
						
					} finally {
						userSettings.setOutputFolder( selectedFolder );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the menu to promote or demote each item
	 * in the list of selected libraries.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void promoteOrDemoteSelectedLibraries(ActionEvent event) {
		List<RepositoryItem> selectedItems = new ArrayList<>( selectedLibrariesTable.getItems() );
		PromoteDemoteDialogController.createDialog( selectedItems, getPrimaryStage() ).showAndWait();
		ObservableList<RepositoryItemWrapper> refreshedItems = FXCollections.observableArrayList();
		
		repositoryManager.resetDownloadCache();
		
		for (RepositoryItem item : selectedItems) {
			try {
				Repository itemRepo = item.getRepository();
				RepositoryItem rItem;
				
				if (itemRepo instanceof RemoteRepository) {
					((RemoteRepository) itemRepo).downloadContent( item, true );
				}
				rItem = repositoryManager.getRepositoryItem(
						item.getBaseNamespace(), item.getFilename(), item.getVersion() );
				refreshedItems.add( new RepositoryItemWrapper( rItem ) );
				
			} catch (RepositoryException e) {
				// Ignore and do not include this item in the refreshed list
			}
		}
		
		Platform.runLater( new Runnable() {
			public void run() {
				selectedLibrariesTable.setItems( refreshedItems );
				selectedLibrariesTable.refresh();
			}
		});
	}
	
	/**
	 * Called when the user clicks the menu to exit the application.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void exitApplication(ActionEvent event) {
		getPrimaryStage().close();
	}
	
	/**
	 * Called when the user clicks the menu to display the about-application
	 * dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void aboutApplication(ActionEvent event) {
		AboutDialogController.createAboutDialog( getPrimaryStage() ).showAndWait();
	}
	
	/**
	 * Called when the user modifies the selection of the 'repositoryChoice'
	 * control.
	 */
	private void repositorySelectionChanged() {
		Runnable r = new BackgroundTask( "Updating namespaces from remote repository...", StatusType.INFO ) {
			public void execute() throws Throwable {
				String rid = repositoryChoice.getSelectionModel().getSelectedItem();
				Repository repository = repositoryManager.getRepository( rid );
				List<String> baseNamespaces = repository.listBaseNamespaces();
				
				baseNamespaces.add( 0, null );
				Platform.runLater( () -> {
					namespaceChoice.setItems( FXCollections.observableList( baseNamespaces ) );
					namespaceChoice.getSelectionModel().select( 0 );
				});
			}
		};
		
		new Thread( r ).start();
	}
	
	/**
	 * Called when the user modifies the selection of the 'namespaceChoice'
	 * control.
	 */
	private void namespaceSelectionChanged() {
		String selectedNS = namespaceChoice.getSelectionModel().getSelectedItem();
		
		if (selectedNS != null) {
			Runnable r = new BackgroundTask( "Updating candidate libraries from remote repository...", StatusType.INFO ) {
				public void execute() throws Throwable {
					String rid = repositoryChoice.getSelectionModel().getSelectedItem();
					Repository repository = repositoryManager.getRepository( rid );
					List<RepositoryItemWrapper> selectedItems = selectedLibrariesTable.getItems();
					List<String> candidateNamespaces = getCandidateNamespaces();
					List<RepositoryItemWrapper> candidateItems = new ArrayList<>();
					
					for (String candidateNS : candidateNamespaces) {
						List<RepositoryItem> items = repository.listItems( candidateNS, null, true );
						
						for (RepositoryItem item : items) {
							if (!selectedItems.contains( item )) {
								candidateItems.add( new RepositoryItemWrapper( item ) );
							}
						}
					}
					Collections.sort( candidateItems );
					
					Platform.runLater( () -> {
						candidateLibrariesTable.setItems( FXCollections.observableList( candidateItems ) );
					});
				}
			};
			
			new Thread( r ).start();
			
		} else {
			Platform.runLater( () -> {
				candidateLibrariesTable.setItems( FXCollections.emptyObservableList() );
				updateControlStates();
			});
		}
	}
	
	/**
	 * Returns the list of candidate namespaces that are either equal to
	 * or sub-namespaces of the currently selected namespace.
	 * 
	 * @return List<String>
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
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( () -> {
			statusBarLabel.setText( message );
			statusBarIcon.setImage( (statusType == null) ? null : statusType.getIcon() );
			
			if (disableControls) {
				List<MenuItem> menus = Arrays.asList( importMenu, exportMenu, upversionMenu, promoteOrDemoteMenu );
				List<Control> controls = Arrays.asList( repositoryChoice, namespaceChoice,
						addButton, removeButton, candidateLibrariesTable,
						selectedLibrariesTable, upversionButton, promoteOrDemoteButton );
				
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
			boolean selectedItemsEmpty = selectedLibrariesTable.getItems().isEmpty();
			boolean candidateItemSelected = !candidateLibrariesTable.getSelectionModel().getSelectedItems().isEmpty();
			boolean selectedItemSelected = !selectedLibrariesTable.getSelectionModel().getSelectedItems().isEmpty();
			
			importMenu.setDisable( false );
			exportMenu.setDisable( selectedItemsEmpty );
			upversionMenu.setDisable( selectedItemsEmpty );
			promoteOrDemoteMenu.setDisable( selectedItemsEmpty );
			
			repositoryChoice.setDisable( false );
			namespaceChoice.setDisable( false );
			candidateLibrariesTable.setDisable( false );
			selectedLibrariesTable.setDisable( false );
			addButton.setDisable( !candidateItemSelected );
			removeButton.setDisable( !selectedItemSelected );
			upversionButton.setDisable( selectedItemsEmpty );
			promoteOrDemoteButton.setDisable( selectedItemsEmpty );
		} );
	}

	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#initialize(javafx.stage.Stage)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void initialize(Stage primaryStage) {
		super.initialize(primaryStage);
		
		// Configure listeners for choice boxes
		repositoryChoice.valueProperty().addListener( (observable, oldValue, newValue) -> {
			repositorySelectionChanged();
		} );
		namespaceChoice.valueProperty().addListener( (observable, oldValue, newValue) -> {
			namespaceSelectionChanged();
		} );
		
		// Configure cell values and tooltips for table views
		Callback<CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>> nameColumnValueFactory =
				new Callback<TableColumn.CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<RepositoryItemWrapper, String> nodeFeatures) {
						RepositoryItem item = nodeFeatures.getValue();
						return new ReadOnlyStringWrapper( (item == null) ? "" : item.getLibraryName() );
					}
				};
		Callback<CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>> versionColumnValueFactory =
				new Callback<TableColumn.CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<RepositoryItemWrapper, String> nodeFeatures) {
						RepositoryItem item = nodeFeatures.getValue();
						return new ReadOnlyStringWrapper( (item == null) ? "" : item.getVersion() );
					}
				};
		Callback<CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>> statusColumnValueFactory =
				new Callback<TableColumn.CellDataFeatures<RepositoryItemWrapper,String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<RepositoryItemWrapper, String> nodeFeatures) {
						RepositoryItem item = nodeFeatures.getValue();
						return new ReadOnlyStringWrapper( (item == null) ? "" : MessageBuilder.formatMessage( item.getStatus().toString() ) );
					}
				};
		Callback<TableView<RepositoryItemWrapper>,TableRow<RepositoryItemWrapper>> rowFactory =
				new Callback<TableView<RepositoryItemWrapper>,TableRow<RepositoryItemWrapper>>() {
					public TableRow<RepositoryItemWrapper> call(final TableView<RepositoryItemWrapper> tv) {
						return new TableRow<RepositoryItemWrapper>() {
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
					}
				};
				
		candidateNameColumn.setCellValueFactory( nameColumnValueFactory );
		candidateVersionColumn.setCellValueFactory( versionColumnValueFactory );
		candidateStatusColumn.setCellValueFactory( statusColumnValueFactory );
		candidateLibrariesTable.setRowFactory( rowFactory );
		candidateLibrariesTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> { updateControlStates(); } );
		selectedNameColumn.setCellValueFactory( nameColumnValueFactory );
		selectedVersionColumn.setCellValueFactory( versionColumnValueFactory );
		selectedStatusColumn.setCellValueFactory( statusColumnValueFactory );
		selectedLibrariesTable.setRowFactory( rowFactory );
		selectedLibrariesTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> { updateControlStates(); } );
		
		// Configure multiselect and placeholder labels for empty tables
		candidateLibrariesTable.setPlaceholder( new Label("") );
		candidateLibrariesTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
		selectedLibrariesTable.setPlaceholder( new Label("") );
		selectedLibrariesTable.getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
		
		// Assign proportional column widths and diable resizing/reordering
		List<TableColumn<?,?>> columns = Arrays.asList( candidateNameColumn, candidateStatusColumn,
				candidateVersionColumn, selectedNameColumn, selectedStatusColumn, selectedVersionColumn );
		
		for (TableColumn<?,?> column : columns) {
			column.setResizable( false );
			column.impl_setReorderable( false );
		}
		candidateNameColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply(0.6) );
		candidateVersionColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply(0.2) );
		candidateStatusColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply(0.2) );
		selectedNameColumn.prefWidthProperty().bind( selectedLibrariesTable.widthProperty().multiply(0.6) );
		selectedVersionColumn.prefWidthProperty().bind( candidateLibrariesTable.widthProperty().multiply(0.2) );
		selectedStatusColumn.prefWidthProperty().bind( selectedLibrariesTable.widthProperty().multiply(0.2) );
		
		// Initialize data and control states
		candidateLibrariesTable.setItems( FXCollections.emptyObservableList() );
		selectedLibrariesTable.setItems( FXCollections.emptyObservableList() );
		updateControlStates();
		
		primaryStage.showingProperty().addListener( (observable, oldValue, newValue) -> {
			ObservableList<String> repositoryIds = FXCollections.observableArrayList();
			
			repositoryManager.listRemoteRepositories().forEach( r -> repositoryIds.add( r.getId() ) );
			repositoryChoice.setItems( repositoryIds );
			repositoryChoice.getSelectionModel().select( 0 );
		} );
	}
	
}
