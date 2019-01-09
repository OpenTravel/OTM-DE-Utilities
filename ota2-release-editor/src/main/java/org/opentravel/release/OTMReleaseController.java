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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.BrowseRepositoryDialogController;
import org.opentravel.application.common.StatusType;
import org.opentravel.release.NewReleaseDialogController.NewReleaseInfo;
import org.opentravel.release.navigate.TreeNode;
import org.opentravel.release.navigate.TreeNodeFactory;
import org.opentravel.release.undo.SpinnerUndoableAction;
import org.opentravel.release.undo.TextInputUndoableAction;
import org.opentravel.release.undo.UndoManager;
import org.opentravel.release.undo.UndoableAction;
import org.opentravel.release.undo.WritableValueUndoableAction;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseCompileOptions;
import org.opentravel.schemacompiler.repository.ReleaseItem;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryAvailabilityChecker;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalDateTimeTextField;

/**
 * JavaFX controller class for the OTM-Diff application.
 */
public class OTMReleaseController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-release-editor.fxml";
	
	private static final DateFormat dateTimeFormat = new SimpleDateFormat( "d MMM yyyy h:mm:ssa" );
	private static final String SUBSTITUTION_GROUP_CHOICE = "Substitution Group";
	private static final String BLANK_DATE_VALUE = "Latest Commit";
	private static final String DEFAULT_SUFFIX = " (Release Default)";
	
	@FXML private MenuItem newMenu;
	@FXML private MenuItem openMenu;
	@FXML private MenuItem importMenu;
	@FXML private MenuItem saveMenu;
	@FXML private MenuItem saveAsMenu;
	@FXML private MenuItem compileMenu;
	@FXML private MenuItem closeMenu;
	@FXML private MenuItem exitMenu;
	@FXML private MenuItem undoMenu;
	@FXML private MenuItem redoMenu;
	@FXML private MenuItem addLibraryMenu;
	@FXML private MenuItem reloadModelMenu;
	@FXML private MenuItem openManagedMenu;
	@FXML private Menu publishReleaseMenu;
	@FXML private MenuItem newReleaseVersionMenu;
	@FXML private MenuItem unpublishReleaseMenu;
	@FXML private MenuItem aboutMenu;
	@FXML private TextField releaseFilename;
	@FXML private TextField releaseName;
	@FXML private TextField releaseBaseNamespace;
	@FXML private Label releaseStatus;
	@FXML private TextField releaseVersion;
	@FXML private HBox effectiveDateHBox;
	@FXML private TextArea releaseDescription;
	@FXML private Button releaseFileButton;
	@FXML private Button reloadModelButton;
	@FXML private Button addLibraryButton;
	@FXML private Button removeLibraryButton;
	@FXML private Accordion releaseAccordion;
	@FXML private TitledPane releaseMembersPane;
	@FXML private TableView<ReleaseMember> principalTableView;
	@FXML private TableColumn<ReleaseMember,String> principalNameColumn;
	@FXML private TableColumn<ReleaseMember,String> principalStatusColumn;
	@FXML private TableColumn<ReleaseMember,String> principalDateColumn;
	@FXML private TableView<ReleaseMember> referencedTableView;
	@FXML private TableColumn<ReleaseMember,String> referencedNameColumn;
	@FXML private TableColumn<ReleaseMember,String> referencedStatusColumn;
	@FXML private TableColumn<ReleaseMember,String> referencedDateColumn;
	@FXML private TableView<FacetSelection> facetSelectionTableView;
	@FXML private TableColumn<FacetSelection,String> facetOwnerColumn;
	@FXML private TableColumn<FacetSelection,String> facetSelectionColumn;
	@FXML private ScrollPane optionsScrollPane;
	@FXML private ChoiceBox<String> bindingStyleChoice;
	@FXML private CheckBox compileXmlSchemasCheckbox;
	@FXML private CheckBox compileServicesCheckbox;
	@FXML private CheckBox compileJsonSchemasCheckbox;
	@FXML private CheckBox compileSwaggerCheckbox;
	@FXML private CheckBox compileDocumentationCheckbox;
	@FXML private TextField serviceEndpointUrl;
	@FXML private TextField baseResourceUrl;
	@FXML private CheckBox suppressExtensionsCheckbox;
	@FXML private CheckBox generateExamplesCheckbox;
	@FXML private CheckBox exampleMaxDetailCheckbox;
	@FXML private Spinner<Integer> maxRepeatSpinner;
	@FXML private Spinner<Integer> maxRecursionDepthSpinner;
	@FXML private CheckBox suppressOptionalFieldsCheckbox;
	@FXML private TableView<ValidationFinding> validationTableView;
	@FXML private TableColumn<ValidationFinding,ImageView> validationLevelColumn;
	@FXML private TableColumn<ValidationFinding,String> validationComponentColumn;
	@FXML private TableColumn<ValidationFinding,String> validationDescriptionColumn;
	@FXML private TreeView<TreeNode<?>> libraryTreeView;
	@FXML private TableView<NodeProperty> propertyTableView;
	@FXML private TableColumn<NodeProperty,String> propertyNameColumn;
	@FXML private TableColumn<NodeProperty,String> propertyValueColumn;
	@FXML private ImageView statusBarIcon;
	@FXML private Label statusBarLabel;
	
	private LocalDateTimeTextField defaultEffectiveDate;
	private Label timeZoneLabel;
	private Button applyToAllButton;
	
	private RepositoryManager repositoryManager;
	private RepositoryAvailabilityChecker availabilityChecker;
	private UndoManager undoManager = new UndoManager();
	private boolean managedRelease = false;
	private boolean releaseDirty = false;
	private boolean modelDirty = false;
	private boolean hasError = false;
	
	private File releaseFile;
	private ReleaseManager releaseManager;
	private ValidationFindings validationFindings;
	private Map<ReleaseMember,List<RepositoryItemCommit>> commitHistoryMap = new HashMap<>();
	private UserSettings userSettings;
	
	/**
	 * Default constructor.
	 */
	public OTMReleaseController() {
		try {
			repositoryManager = RepositoryManager.getDefault();
			availabilityChecker = RepositoryAvailabilityChecker.getInstance( repositoryManager );
			availabilityChecker.pingAllRepositories( true );
			
		} catch (RepositoryException e) {
			e.printStackTrace( System.out );
		}
	}
	
	/**
	 * Called when the user clicks the button to undo a previous action.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void undoAction(ActionEvent event) {
		undoManager.executeUndo();
	}
	
	/**
	 * Called when the user clicks the button to redo a previous undo action.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void redoAction(ActionEvent event) {
		undoManager.executeRedo();
	}
	
	/**
	 * Called when the user clicks the button to create a new release file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void newReleaseFile(ActionEvent event) {
		try {
			NewReleaseDialogController controller = NewReleaseDialogController.createNewReleaseDialog(
					userSettings.getReleaseFolder(), getPrimaryStage() );
			NewReleaseInfo releaseInfo = controller.showDialog();
			
			if (releaseInfo != null) {
				ReleaseManager newReleaseManager = new ReleaseManager( repositoryManager );
				File newReleaseFile = newReleaseManager.getNewReleaseFile(
						releaseInfo.getReleaseName(), releaseInfo.getReleaseDirectory() );
				
				if (!newReleaseFile.exists() || confirmOverwriteFile( newReleaseFile )) {
					Runnable r = new BackgroundTask( "Creating New Release...", StatusType.INFO ) {
						public void execute() throws Throwable {
							try {
								newReleaseManager.createNewRelease( releaseInfo.getReleaseBaseNamespace(),
										releaseInfo.getReleaseName(), releaseInfo.getReleaseDirectory() );
								OTMReleaseController.this.releaseManager = newReleaseManager;
								OTMReleaseController.this.releaseFile = URLUtils.toFile( newReleaseManager.getRelease().getReleaseUrl() );
								OTMReleaseController.this.validationFindings = new ValidationFindings();
								OTMReleaseController.this.managedRelease = false;
								updateControlsForNewRelease();
								
							} finally {
								userSettings.setReleaseFolder( releaseInfo.getReleaseDirectory() );
								userSettings.save();
							}
						}
					};
					
					new Thread( r ).start();
				}
			}
			
		} catch (RepositoryException | LibrarySaveException e) {
			Runnable r = new BackgroundTask( "Error creating the new release", StatusType.ERROR ) {
				public void execute() throws Throwable {
					Thread.sleep( 5000 );
				}
			};
			e.printStackTrace( System.out );
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button to open the release file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void openReleaseFile(ActionEvent event) {
		if (confirmCloseRelease()) {
			closeRelease();
		}
		FileChooser chooser = newFileChooser( "Open",
				userSettings.getReleaseFolder(),
				new String[] { "*.otr", "OTM Release Files" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if ((selectedFile != null) && (selectedFile != releaseFile)) {
			Runnable r = new BackgroundTask( "Loading Release: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						ReleaseManager manager = new ReleaseManager( repositoryManager );
						
						validationFindings = new ValidationFindings();
						manager.loadRelease( selectedFile, validationFindings );
						OTMReleaseController.this.releaseManager = manager;
						OTMReleaseController.this.releaseFile = selectedFile;
						OTMReleaseController.this.managedRelease = false;
						updateControlsForNewRelease();
						
					} finally {
						userSettings.setReleaseFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button to open a managed release from
	 * the OTM repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void openManagedRelease(ActionEvent event) {
		if (confirmCloseRelease()) {
			closeRelease();
		}
		if (availabilityChecker.pingAllRepositories( false )) {
			BrowseRepositoryDialogController controller =
					BrowseRepositoryDialogController.createDialog(
							"Open Managed Release", RepositoryItemType.RELEASE, getPrimaryStage() );
			
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				RepositoryItem selectedItem = controller.getSelectedRepositoryItem();
				
				if (selectedItem != null) {
					Runnable r = new BackgroundTask( "Loading Managed Release...", StatusType.INFO ) {
						public void execute() throws Throwable {
							ReleaseManager manager = new ReleaseManager( repositoryManager );
							
							validationFindings = new ValidationFindings();
							manager.loadRelease( selectedItem, validationFindings );
							OTMReleaseController.this.releaseManager = manager;
							OTMReleaseController.this.releaseFile = URLUtils.toFile(
									releaseManager.getRelease().getReleaseUrl() );
							OTMReleaseController.this.managedRelease = true;
							updateControlsForNewRelease();
						}
					};
					
					new Thread( r ).start();
				}
			}
		}
	}
	
	/**
	 * Called when the user clicks the button to import an OTM release from
	 * an existing OTM project (.otp) file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void importFromProject(ActionEvent event) {
		if (confirmCloseRelease()) {
			closeRelease();
		}
		FileChooser chooser = newFileChooser( "Import from OTP",
				userSettings.getReleaseFolder(),
				new String[] { "*.otp", "OTM Project Files (*.otp)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if ((selectedFile != null) && (selectedFile != releaseFile)) {
			Runnable r = new BackgroundTask( "Importing Release: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						ValidationFindings findings = new ValidationFindings();
						ReleaseManager newReleaseManager = new ReleaseManager( repositoryManager );
						
						newReleaseManager.importFromProject( selectedFile, findings );
						OTMReleaseController.this.releaseManager = newReleaseManager;
						OTMReleaseController.this.releaseFile = URLUtils.toFile( newReleaseManager.getRelease().getReleaseUrl() );
						OTMReleaseController.this.validationFindings = new ValidationFindings();
						OTMReleaseController.this.managedRelease = false;
						updateControlsForNewRelease();
						
					} finally {
						userSettings.setReleaseFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button to save the release file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveReleaseFile(ActionEvent event) {
		if (releaseManager != null) {
			Runnable r = new BackgroundTask( "Saving Release: " + releaseFile.getName(), StatusType.INFO ) {
				public void execute() throws Throwable {
					releaseManager.saveRelease();
					releaseDirty = false;
					updateControlStates();
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button to save-as the release file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveReleaseFileAs(ActionEvent event) {
		if ((releaseManager != null) && confirmCloseRelease()) {
			DirectoryChooser chooser = newDirectoryChooser( "Save As Folder", userSettings.getReleaseFolder() );
			File selectedFolder = chooser.showDialog( getPrimaryStage() );
			
			if (selectedFolder != null) {
				File saveAsFile = releaseManager.getSaveAsFile( selectedFolder );
				
				if (!saveAsFile.exists() || confirmOverwriteFile( saveAsFile )) {
					Runnable r = new BackgroundTask( "Saving Release As...", StatusType.INFO ) {
						public void execute() throws Throwable {
							try {
								releaseManager.saveReleaseAs( selectedFolder );
								releaseFile = URLUtils.toFile( releaseManager.getRelease().getReleaseUrl() );
								releaseDirty = false;
								updateControlStates();
								
							} finally {
								userSettings.setReleaseFolder( selectedFolder );
								userSettings.save();
							}
						}
					};
					
					new Thread( r ).start();
				}
			}
		}
	}
	
	/**
	 * Called when the user clicks the menu to compile the current release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void compileRelease(ActionEvent event) {
		if ((releaseManager != null) && !validationFindings.hasFinding( FindingType.ERROR )) {
			Runnable r = new BackgroundTask( "Compiling Release...", StatusType.INFO ) {
				public void execute() throws Throwable {
					CompileAllCompilerTask task = new CompileAllCompilerTask();
					String releaseFolderName = releaseFile.getName().replaceAll("\\.otr", "");
					File outputFolder = new File( releaseFile.getParentFile(), releaseFolderName );
					
					task.setOutputFolder( outputFolder.getAbsolutePath() );
					task.compileOutput( releaseManager );
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button to close the release file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void closeReleaseFile(ActionEvent event) {
		if (confirmCloseRelease()) {
			closeRelease();
		}
	}
	
	/**
	 * Called when the user clicks the button to apply the current effective
	 * date to all release members.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void applyEffectiveDateToAllMembers(ActionEvent event) {
		if (releaseManager != null) {
			new UndoableAction( undoManager ) {
				
				private Map<RepositoryItem,Date> oldEffectiveDates = new HashMap<>();
				private Map<RepositoryItem,Date> newEffectiveDates = new HashMap<>();
				
				/** Capture the old/new effective dates before updating. */
				public boolean doExecute() {
					Release release = releaseManager.getRelease();
					Date defaultEffectiveDate = release.getDefaultEffectiveDate();
					
					for (ReleaseMember member : release.getAllMembers()) {
						List<RepositoryItemCommit> commitHistory = commitHistoryMap.get( member );
						Date latestCommit = Utils.getLatestCommitDate( commitHistory );
						RepositoryItem item = member.getRepositoryItem();
						
						oldEffectiveDates.put( item, member.getEffectiveDate() );
						
						if (defaultEffectiveDate == null) {
							newEffectiveDates.put( item, null );
							
						} else if (defaultEffectiveDate.after( latestCommit )) {
							newEffectiveDates.put( item, latestCommit );
							
						} else {
							newEffectiveDates.put( item, defaultEffectiveDate );
						}
					}
					return updateEffectiveDates( newEffectiveDates );
				}
				
				/** Assigns the old effective dates. */
				public boolean doExecuteUndo() {
					return updateEffectiveDates( oldEffectiveDates );
				}

				/** Reassigns the new effective dates. */
				public boolean doExecuteRedo() {
					return updateEffectiveDates( newEffectiveDates );
				}
				
				/** Assign the effective dates specified in the map provided. */
				private boolean updateEffectiveDates(Map<RepositoryItem,Date> effectiveDates) {
					boolean result = true;
					
					for (RepositoryItem item : effectiveDates.keySet()) {
						ReleaseMember member = releaseManager.getPrincipalMember( item );
						
						if (member == null) {
							member = releaseManager.getReferencedMember( item );
						}
						if (member == null) {
							result = false; break;
						}
						member.setEffectiveDate( effectiveDates.get( item ) );
					}
					modelDirty = true;
					releaseDirty = true;
					principalTableView.refresh();
					referencedTableView.refresh();
					updateControlStates();
					return result;
				}
				
			}.submit();
		}
	}
	
	/**
	 * Called when the user clicks the button to add a principal library
	 * to the release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void addPrincipalLibrary(ActionEvent event) {
		if (releaseManager != null) {
			BrowseRepositoryDialogController controller =
					BrowseRepositoryDialogController.createDialog(
							"Add Principle Library", RepositoryItemType.LIBRARY, getPrimaryStage() );
			
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				RepositoryItem selectedItem = controller.getSelectedRepositoryItem();
				
				if (selectedItem != null) {
					new UndoableAction( undoManager ) {
						
						private RepositoryItem libraryItem = selectedItem;
						
						public boolean doExecute() {
							Runnable r = new PrincipalLibraryBackgroundTask( libraryItem, true );
							new Thread( r ).start();
							return true;
						}

						public boolean doExecuteUndo() {
							Runnable r = new PrincipalLibraryBackgroundTask( libraryItem, false );
							new Thread( r ).start();
							return true;
						}

						public boolean doExecuteRedo() {
							return doExecute();
						}
						
					}.submit();
				}
			}
		}
	}
	
	/**
	 * Called when the user clicks the button to remove a principal library
	 * from the release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void removePrincipalLibrary(ActionEvent event) {
		if (releaseManager != null) {
			ReleaseMember selectedMember = principalTableView.getSelectionModel().getSelectedItem();
			
			if (selectedMember != null) {
				Alert alert = new Alert( AlertType.CONFIRMATION );
				Optional<ButtonType> dialogResult;
				
				alert.setTitle( "Remove Principal Library" );
				alert.setHeaderText( null );
				alert.setContentText( "Remove library " + selectedMember.getRepositoryItem().getFilename() + ".  Are you sure?" );

				alert.getButtonTypes().setAll( ButtonType.YES, ButtonType.NO );
				dialogResult = alert.showAndWait();
				
				if (dialogResult.get() == ButtonType.YES) {
					new UndoableAction( undoManager ) {
						
						private RepositoryItem libraryItem = selectedMember.getRepositoryItem();
						
						public boolean doExecute() {
							Runnable r = new PrincipalLibraryBackgroundTask( libraryItem, false );
							new Thread( r ).start();
							return true;
						}

						public boolean doExecuteUndo() {
							Runnable r = new PrincipalLibraryBackgroundTask( libraryItem, true );
							new Thread( r ).start();
							return true;
						}

						public boolean doExecuteRedo() {
							return doExecute();
						}
						
					}.submit();
				}
			}
		}
	}
	
	/**
	 * Reusable background task that adds or removes an OTM library to/from the
	 * principal members list of the current release.
	 */
	private class PrincipalLibraryBackgroundTask extends BackgroundTask {
		
		private RepositoryItem libraryItem;
		private boolean addLibrary;
		
		/**
		 * Constructor that specifies the library to be added or removed.
		 * 
		 * @param libraryItem  the repository item for the library to be added or removed
		 * @param addLibrary  flag indicating whether to add or remove the library (false = remove)
		 */
		public PrincipalLibraryBackgroundTask(RepositoryItem libraryItem, boolean addLibrary) {
			super( "Updating Release Model...", StatusType.INFO );
			this.libraryItem = libraryItem;
			this.addLibrary = addLibrary;
		}
		
		/**
		 * @see org.opentravel.release.OTMReleaseController.BackgroundTask#execute()
		 */
		@Override
		protected void execute() throws Throwable {
			if (addLibrary) {
				releaseManager.addPrincipalMember( libraryItem );
				
			} else {
				releaseManager.removePrincipalMember( libraryItem );
			}
			releaseManager.loadReleaseModel( validationFindings = new ValidationFindings() );
			updateCommitHistories();
			principalTableView.setItems(
					FXCollections.observableList( releaseManager.getRelease().getPrincipalMembers() ) );
			referencedTableView.setItems(
					FXCollections.observableList( releaseManager.getRelease().getReferencedMembers() ) );
			releaseDirty = true;
			modelDirty = false;
			updateControlStates();
			updateModelTreeView();
		}
		
	}
	
	/**
	 * Called when the user clicks the button to reload the OTM model.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void reloadModel(ActionEvent event) {
		if (releaseManager != null) {
			Runnable r = new BackgroundTask( "Reloading Model...", StatusType.INFO ) {
				public void execute() throws Throwable {
					validationFindings = new ValidationFindings();
					releaseManager.loadReleaseModel( validationFindings );
					validationTableView.setItems(
							FXCollections.observableList( validationFindings.getAllFindingsAsList() ) );
					referencedTableView.setItems(
							FXCollections.observableList( releaseManager.getRelease().getReferencedMembers() ) );
					modelDirty = false;
					principalTableView.refresh();
					referencedTableView.refresh();
					updateControlStates();
					updateModelTreeView();
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the menu to publish the release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void publishRelease(MenuItem repoMenu) {
		if ((repoMenu != null) && (releaseManager != null) && !managedRelease) {
			RemoteRepository repository = (RemoteRepository) repoMenu.getProperties().get( "repository" );
			Runnable r = new BackgroundTask( "Publishing Release...", StatusType.INFO ) {
				public void execute() throws Throwable {
					ReleaseItem releaseItem = releaseManager.publishRelease( repository );
					ReleaseManager manager = new ReleaseManager( repositoryManager );
					
					validationFindings = new ValidationFindings();
					manager.loadRelease( releaseItem, validationFindings );
					OTMReleaseController.this.releaseManager = manager;
					OTMReleaseController.this.releaseFile = URLUtils.toFile(
							releaseManager.getRelease().getReleaseUrl() );
					OTMReleaseController.this.managedRelease = true;
					updateControlsForNewRelease();
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the menu to create a new version
	 * of a managed release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void newReleaseVersion(ActionEvent event) {
		if ((releaseManager != null) && managedRelease) {
			DirectoryChooser chooser = newDirectoryChooser( "New Version Folder", userSettings.getReleaseFolder() );
			File selectedFolder = chooser.showDialog( getPrimaryStage() );
			
			if (selectedFolder != null) {
				File newVersionFile = releaseManager.getNewVersionFile( selectedFolder );
				
				if (!newVersionFile.exists() || confirmOverwriteFile( newVersionFile )) {
					Runnable r = new BackgroundTask( "Creating New Version...", StatusType.INFO ) {
						public void execute() throws Throwable {
							try {
								validationFindings = new ValidationFindings();
								ReleaseManager manager = releaseManager.newVersion( selectedFolder, validationFindings );
								
								OTMReleaseController.this.releaseManager = manager;
								OTMReleaseController.this.releaseFile = URLUtils.toFile(
										releaseManager.getRelease().getReleaseUrl() );
								OTMReleaseController.this.managedRelease = false;
								updateControlsForNewRelease();
								
							} finally {
								userSettings.setReleaseFolder( selectedFolder );
								userSettings.save();
							}
						}
					};
					
					new Thread( r ).start();
				}
			}
		}
	}
	
	/**
	 * Called when the user clicks the menu to unpublish the release.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void unpublishRelease(ActionEvent event) {
		if ((releaseManager != null) && managedRelease) {
			DirectoryChooser chooser = newDirectoryChooser( "Unpublish to Local Folder", userSettings.getReleaseFolder() );
			File selectedFolder = chooser.showDialog( getPrimaryStage() );
			
			if (selectedFolder != null) {
				File saveAsFile = releaseManager.getSaveAsFile( selectedFolder );
				
				if (!saveAsFile.exists() || confirmOverwriteFile( saveAsFile )) {
					Runnable r = new BackgroundTask( "Unpublishing Release...", StatusType.INFO ) {
						public void execute() throws Throwable {
							try {
								validationFindings = new ValidationFindings();
								ReleaseManager manager = releaseManager.unpublishRelease( selectedFolder );
								
								manager.loadReleaseModel( validationFindings );
								OTMReleaseController.this.releaseManager = manager;
								OTMReleaseController.this.releaseFile = URLUtils.toFile(
										releaseManager.getRelease().getReleaseUrl() );
								OTMReleaseController.this.managedRelease = false;
								updateControlsForNewRelease();
								
							} finally {
								userSettings.setReleaseFolder( selectedFolder );
								userSettings.save();
							}
						}
					};
					
					new Thread( r ).start();
				}
			}
		}
	}
	
	/**
	 * Called when the user clicks the menu to exit the application.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void exitApplication(ActionEvent event) {
		closeApplication();
	}
	
	/**
	 * Prompts the user to confirm the overwrite of the specified file.
	 * 
	 * @param overwriteFile  the file that will be overwritten if the user confirms
	 * @return boolean
	 */
	private boolean confirmOverwriteFile(File overwriteFile) {
		Alert alert = new Alert( AlertType.CONFIRMATION );
		Optional<ButtonType> dialogResult;
		
		alert.setTitle( "Confirm Overwrite" );
		alert.setHeaderText( null );
		alert.setContentText( "A file named '" + overwriteFile.getName() +
				" already exists in the selected folder.  Overwrite?" );

		alert.getButtonTypes().setAll( ButtonType.YES, ButtonType.NO );
		dialogResult = alert.showAndWait();
		return (dialogResult.get() == ButtonType.YES);
	}
	
	/**
	 * If a release is loaded and has been modified, the user is prompted to confirm
	 * the close and allowed to save the release if desired.
	 * 
	 * @return boolean
	 */
	private boolean confirmCloseRelease() {
		boolean confirmClose = true;
		
		if ((releaseManager != null) && !managedRelease && releaseDirty) {
			Alert alert = new Alert( AlertType.CONFIRMATION );
			Optional<ButtonType> dialogResult;
			
			alert.setTitle( "Exit Application" );
			alert.setHeaderText( null );
			alert.setContentText( "The current release has been modified.  Save before exiting?" );

			alert.getButtonTypes().setAll( ButtonType.YES, ButtonType.NO, ButtonType.CANCEL );
			dialogResult = alert.showAndWait();
			
			if (dialogResult.get() == ButtonType.YES) {
				saveReleaseFile( null );
				
			} else if (dialogResult.get() == ButtonType.CANCEL) {
				confirmClose = false;
			}
		}
		return confirmClose;
	}
	
	/**
	 * Closes the current release and updates the state of all visual controls.
	 */
	private void closeRelease() {
		if (releaseManager != null) {
			undoManager.purge();
			releaseFile = null;
			releaseManager = null;
			managedRelease = false;
			releaseDirty = false;
			modelDirty = false;
			
			Platform.runLater( () -> {
				try {
					undoManager.disableActionExecution();
					updateCommitHistories();
					updateReleaseFields();
					updateCompilerOptions();
					updateFacetSelections();
					validateFields();
					updateControlStates();
					
				} finally {
					undoManager.enableActionExecution();
				}
			});
		}
	}
	
	/**
	 * Closes that application after confirming a save of the current release (if
	 * necessary).  Returns true if the application can be closed or false if the user
	 * cancelled the operation.
	 * 
	 * @return boolean
	 */
	public boolean closeApplication() {
		boolean confirmClose = confirmCloseRelease();
		
		if (confirmClose) {
			UserSettings settings = UserSettings.load();
			Scene scene = getPrimaryStage().getScene();
			
			settings.setWindowPosition( new Point(
					getPrimaryStage().xProperty().intValue(), getPrimaryStage().yProperty().intValue() ) );
			settings.setWindowSize( new Dimension(
					scene.widthProperty().intValue(), scene.heightProperty().intValue() ) );
			settings.save();
			getPrimaryStage().close();
		}
		return confirmClose;
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
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( () -> {
			statusBarLabel.setText( message );
			statusBarIcon.setImage( (statusType == null) ? null : statusType.getIcon() );
			
			if (disableControls) {
				newMenu.setDisable( true );
				openMenu.setDisable( true );
				saveMenu.setDisable( true );
				saveAsMenu.setDisable( true );
				compileMenu.setDisable( true );
				closeMenu.setDisable( true );
				exitMenu.setDisable( true );
				undoMenu.setDisable( true );
				redoMenu.setDisable( true );
				addLibraryMenu.setDisable( true );
				reloadModelMenu.setDisable( true );
				openManagedMenu.setDisable( true );
				publishReleaseMenu.setDisable( true );
				newReleaseVersionMenu.setDisable( true );
				unpublishReleaseMenu.setDisable( true );
				aboutMenu.setDisable( true );
				releaseFileButton.setDisable( true );
				releaseFilename.setDisable( true );
				releaseName.setDisable( true );
				releaseBaseNamespace.setDisable( true );
				releaseStatus.setDisable( true );
				releaseVersion.setDisable( true );
				defaultEffectiveDate.setDisable( true );
				timeZoneLabel.setDisable( true );
				applyToAllButton.setDisable( true );
				releaseDescription.setDisable( true );
				addLibraryButton.setDisable( true );
				removeLibraryButton.setDisable( true );
				reloadModelButton.setDisable( true );
				principalTableView.setDisable( true );
				referencedTableView.setDisable( true );
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
				facetSelectionTableView.setDisable( true );
				validationTableView.setDisable( true );
				libraryTreeView.setDisable( true );
				propertyTableView.setDisable( true );
				
			} else {
				updateControlStates();
			}
		} );
	}
	
	/**
	 * Marks the current release as dirty, performs validation, and updates all
	 * visual control states if necessary.
	 */
	private void markReleaseDirty() {
		boolean oldDirty = releaseDirty;
		
		releaseDirty = true;
		if (!oldDirty) updateControlStates();
		validateFields();
	}
	
	/**
	 * Called when a release field has been modified.
	 * 
	 * @param refreshActiveRepositories  flag indicating whether the list of
	 *									 active repositories should be refreshed
	 * @param oldValue  the original value of the property before it was modified
	 */
	private void handleReleaseFieldModified(boolean refreshActiveRepositories) {
		Release release = getRelease();
		
		if (release != null) {
			LocalDateTime effectiveDate = defaultEffectiveDate.getLocalDateTime();
			
			release.setName( releaseName.textProperty().getValue() );
			release.setBaseNamespace( releaseBaseNamespace.textProperty().getValue() );
			release.setVersion( releaseVersion.textProperty().getValue() );
			release.setDescription( releaseDescription.textProperty().getValue() );
			
			if (effectiveDate != null) {
				release.setDefaultEffectiveDate(
						Date.from( effectiveDate.atZone( ZoneId.systemDefault() ).toInstant() ) );
				
			} else {
				release.setDefaultEffectiveDate( null );
			}
			if (refreshActiveRepositories) {
				updateActiveRepositories();
			}
			markReleaseDirty();
		}
	}
	
	/**
	 * Called when a compiler option has been modified.
	 */
	private <T> void handleCompileOptionModified() {
		ReleaseCompileOptions options = getCompileOptions();
		
		if (options != null) {
			boolean generateExamplesChanged =
					(options.isGenerateExamples() != generateExamplesCheckbox.isSelected());
			
			options.setBindingStyle( bindingStyleChoice.getValue() );
			options.setCompileSchemas( compileXmlSchemasCheckbox.isSelected() );
			options.setCompileServices( compileServicesCheckbox.isSelected() );
			options.setCompileJsonSchemas( compileJsonSchemasCheckbox.isSelected() );
			options.setCompileSwagger( compileSwaggerCheckbox.isSelected() );
			options.setCompileHtml( compileDocumentationCheckbox.isSelected() );
			options.setServiceEndpointUrl( serviceEndpointUrl.textProperty().getValue() );
			options.setResourceBaseUrl( baseResourceUrl.textProperty().getValue() );
			options.setSuppressOtmExtensions( suppressExtensionsCheckbox.isSelected() );
			options.setGenerateExamples( generateExamplesCheckbox.isSelected() );
			options.setGenerateMaxDetailsForExamples( exampleMaxDetailCheckbox.isSelected() );
			options.setExampleMaxRepeat( maxRepeatSpinner.getValue() );
			options.setExampleMaxDepth( maxRecursionDepthSpinner.getValue() );
			options.setSuppressOptionalFields( suppressOptionalFieldsCheckbox.isSelected() );
			
			if (generateExamplesChanged) {
				updateControlStates();
			}
			markReleaseDirty();
		}
	}
	
	/**
	 * Called when a facet selection has been modified.
	 */
	private void handleFacetSelectionsModified() {
		Release release = getRelease();
		
		if (release != null) {
			List<FacetSelection> facetSelections = facetSelectionTableView.getItems();
			Map<QName,QName> selectionMap = FacetSelection.buildFacetSelectionMap( facetSelections );
			
			releaseDirty = true;
			release.setPreferredFacets( selectionMap );
			updateControlStates();
		}
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
	 */
	@Override
	protected void updateControlStates() {
		Platform.runLater( () -> {
			boolean isReleaseLoaded = (releaseManager != null);
			boolean isGenerateExamples = (isReleaseLoaded && generateExamplesCheckbox.isSelected());
			boolean isSaveEnabled = (isReleaseLoaded && !managedRelease && releaseDirty && !hasError);
			boolean isSaveAsEnabled = (isReleaseLoaded && !managedRelease && !hasError);
			boolean isReloadModelEnabled = (isReleaseLoaded && !managedRelease && modelDirty && !hasError);
			boolean isCompileEnabled = (isReleaseLoaded && !hasError
					&& !validationFindings.hasFinding( FindingType.ERROR ));
			boolean isPublishEnabled = (isReleaseLoaded && !releaseDirty && !managedRelease && !hasError
					&& !validationFindings.hasFinding( FindingType.ERROR ));
			boolean isUnpublishEnabled = (isReleaseLoaded && !releaseDirty && managedRelease && !hasError
					&& !validationFindings.hasFinding( FindingType.ERROR ));
			boolean isRemoveEnabled = (isReleaseLoaded && !managedRelease &&
						!principalTableView.getSelectionModel().getSelectedItems().isEmpty());
			
			// Set the enable/disable status of all controls
			newMenu.setDisable( false );
			openMenu.setDisable( false );
			importMenu.setDisable( false );
			saveMenu.setDisable( !isSaveEnabled );
			saveAsMenu.setDisable( !isSaveAsEnabled );
			compileMenu.setDisable( !isCompileEnabled );
			closeMenu.setDisable( !isReleaseLoaded );
			exitMenu.setDisable( false );
			undoMenu.setDisable( !undoManager.canUndo() );
			redoMenu.setDisable( !undoManager.canRedo() );
			addLibraryMenu.setDisable( !(isReleaseLoaded && !managedRelease) );
			reloadModelMenu.setDisable( !isReloadModelEnabled );
			openManagedMenu.setDisable( false );
			publishReleaseMenu.setDisable( !isPublishEnabled );
			newReleaseVersionMenu.setDisable( !isUnpublishEnabled );
			unpublishReleaseMenu.setDisable( !isUnpublishEnabled );
			aboutMenu.setDisable( false );
			releaseFileButton.setDisable( false );
			releaseFilename.setDisable( !isReleaseLoaded );
			releaseName.setDisable( !isReleaseLoaded );
			releaseBaseNamespace.setDisable( !isReleaseLoaded );
			releaseStatus.setDisable( !isReleaseLoaded );
			releaseVersion.setDisable( !isReleaseLoaded );
			defaultEffectiveDate.setDisable( !(isReleaseLoaded && !managedRelease) );
			timeZoneLabel.setDisable( !isReleaseLoaded );
			applyToAllButton.setDisable( !(isReleaseLoaded && !managedRelease) );
			releaseDescription.setDisable( !isReleaseLoaded );
			addLibraryButton.setDisable( !(isReleaseLoaded && !managedRelease) );
			removeLibraryButton.setDisable( !isRemoveEnabled );
			reloadModelButton.setDisable( !isReloadModelEnabled );
			principalTableView.setDisable( !isReleaseLoaded );
			referencedTableView.setDisable( !isReleaseLoaded );
			bindingStyleChoice.setDisable( !(isReleaseLoaded && !managedRelease) );
			compileXmlSchemasCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			compileServicesCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			compileJsonSchemasCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			compileSwaggerCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			compileDocumentationCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			serviceEndpointUrl.setDisable( !isReleaseLoaded );
			baseResourceUrl.setDisable( !isReleaseLoaded );
			suppressExtensionsCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			generateExamplesCheckbox.setDisable( !(isReleaseLoaded && !managedRelease) );
			exampleMaxDetailCheckbox.setDisable( !(isGenerateExamples && !managedRelease) );
			maxRepeatSpinner.setDisable( !isGenerateExamples );
			maxRecursionDepthSpinner.setDisable( !isGenerateExamples );
			suppressOptionalFieldsCheckbox.setDisable( !(isGenerateExamples && !managedRelease) );
			facetSelectionTableView.setDisable( !isReleaseLoaded );
			validationTableView.setDisable( !isReleaseLoaded );
			libraryTreeView.setDisable( !isReleaseLoaded );
			propertyTableView.setDisable( !isReleaseLoaded );
			
			// Set the editable status of all interactive controls
			releaseName.setEditable( !managedRelease );
			releaseBaseNamespace.setEditable( !managedRelease );
			releaseVersion.setEditable( !managedRelease );
			releaseDescription.setEditable( !managedRelease );
			serviceEndpointUrl.setEditable( !managedRelease );
			baseResourceUrl.setEditable( !managedRelease );
			maxRepeatSpinner.setEditable( !managedRelease );
			maxRecursionDepthSpinner.setEditable( !managedRelease );
			principalTableView.setEditable( isReleaseLoaded && !managedRelease );
			referencedTableView.setEditable( isReleaseLoaded && !managedRelease );
			facetSelectionTableView.setEditable( isReleaseLoaded && !managedRelease );
			
			if (!isReleaseLoaded) {
				principalTableView.setItems( null );
				referencedTableView.setItems( null );
				facetSelectionTableView.setItems( null );
				validationTableView.setItems( null );
				libraryTreeView.setRoot( null );
				propertyTableView.setItems( null );
			}
		});
	}
	
	/**
	 * Updates all visual controls for a newly-loaded or newly-created release.
	 */
	private void updateControlsForNewRelease() {
		Platform.runLater( () -> {
			try {
				undoManager.disableActionExecution();
				updateReleaseFields();
				updateCompilerOptions();
				updateFacetSelections();
				updateCommitHistories();
				updateModelTreeView();
				updateActiveRepositories();
				
				principalTableView.setItems(
						FXCollections.observableList( releaseManager.getRelease().getPrincipalMembers() ) );
				referencedTableView.setItems(
						FXCollections.observableList( releaseManager.getRelease().getReferencedMembers() ) );
				validationTableView.setItems(
						FXCollections.observableList( validationFindings.getAllFindingsAsList() ) );
				
				undoManager.purge();
				releaseDirty = false;
				modelDirty = false;
				validateFields();
			
			} finally {
				undoManager.enableActionExecution();
			}
		});
	}
	
	/**
	 * Updates the visual fields related to the release's identity and DESCRIPTION.  This
	 * method must be called from within the UI thread.
	 */
	private void updateReleaseFields() {
		Release release = getRelease();
		Date effDate = (release == null) ? null : release.getDefaultEffectiveDate();
		LocalDateTime effectiveDate = (effDate == null) ?
				null : LocalDateTime.ofInstant( effDate.toInstant(), ZoneId.systemDefault() );
		
		releaseFilename.setText( ((releaseFile == null) ? "" : releaseFile.getName()) +
				(managedRelease ? " (Managed / Read-Only)" : "") );
		releaseName.setText( (release == null) ? "" : release.getName() );
		releaseBaseNamespace.setText( (release == null) ? "" : release.getBaseNamespace() );
		releaseStatus.setText( (release == null) ? "" : MessageBuilder.formatMessage( release.getStatus().toString() ) );
		releaseVersion.setText( (release == null) ? "" : release.getVersion() );
		releaseDescription.setText( (release == null) ? "" : release.getDescription() );
		defaultEffectiveDate.setLocalDateTime( effectiveDate );
	}
	
	/**
	 * Updates the visual fields related to the release's compiler options.  This
	 * method must be called from within the UI thread.
	 */
	private void updateCompilerOptions() {
		Release release = getRelease();
		
		if (release != null) {
			ReleaseCompileOptions options = release.getCompileOptions();
			String endpointUrl = options.getServiceEndpointUrl();
			String resourceUrl = options.getResourceBaseUrl();
			Integer maxRepeat = options.getExampleMaxRepeat();
			Integer maxDepth = options.getExampleMaxDepth();
			
			bindingStyleChoice.setValue( options.getBindingStyle() );
			compileXmlSchemasCheckbox.setSelected( options.isCompileSchemas() );
			compileServicesCheckbox.setSelected( options.isCompileServices() );
			compileJsonSchemasCheckbox.setSelected( options.isCompileJsonSchemas() );
			compileSwaggerCheckbox.setSelected( options.isCompileSwagger() );
			compileDocumentationCheckbox.setSelected( options.isCompileHtml() );
			serviceEndpointUrl.setText( (endpointUrl == null) ? "" : endpointUrl );
			baseResourceUrl.setText( (resourceUrl == null) ? "" : resourceUrl );
			suppressExtensionsCheckbox.setSelected( options.isSuppressOtmExtensions() );
			generateExamplesCheckbox.setSelected( options.isGenerateExamples() );
			exampleMaxDetailCheckbox.setSelected( options.isGenerateMaxDetailsForExamples() );
			maxRepeatSpinner.getValueFactory().setValue( (maxRepeat == null) ? 3 : maxRepeat );
			maxRecursionDepthSpinner.getValueFactory().setValue( (maxDepth == null) ? 3 : maxDepth );
			suppressOptionalFieldsCheckbox.setSelected( options.isSuppressOptionalFields() );
			
		} else {
			bindingStyleChoice.setValue( CompilerExtensionRegistry.getActiveExtension() );
			compileXmlSchemasCheckbox.setSelected( false );
			compileServicesCheckbox.setSelected( false );
			compileJsonSchemasCheckbox.setSelected( false );
			compileSwaggerCheckbox.setSelected( false );
			compileDocumentationCheckbox.setSelected( false );
			serviceEndpointUrl.setText( "" );
			baseResourceUrl.setText( "" );
			suppressExtensionsCheckbox.setSelected( false );
			generateExamplesCheckbox.setSelected( false );
			exampleMaxDetailCheckbox.setSelected( false );
			maxRepeatSpinner.getValueFactory().setValue( 3 );
			maxRecursionDepthSpinner.getValueFactory().setValue( 3 );
			suppressOptionalFieldsCheckbox.setSelected( false );
		}
	}
	
	/**
	 * Populates the contents of the facet selection table using selections from the current
	 * OTM release.
	 */
	private void updateFacetSelections() {
		Release release = getRelease();
		List<FacetSelection> selectionList = null;
		
		if (release != null) {
			selectionList = FacetSelection.buildFacetSelections( release.getPreferredFacets(), releaseManager.getModel() );
		}
		facetSelectionTableView.setItems(
				(selectionList == null) ? null : FXCollections.observableList( selectionList ) );
	}
	
	/**
	 * Updates the tree view from which the user can visually browse the
	 * OTM model.
	 */
	private void updateModelTreeView() {
		Platform.runLater( () -> {
			TreeItem<TreeNode<?>> rootItem = null;
			
			if (releaseManager != null) {
				TreeNodeFactory nodeFactory = new TreeNodeFactory();
				rootItem = new TreeItem<>();
				
				for (TLLibrary library : releaseManager.getModel().getUserDefinedLibraries()) {
					rootItem.getChildren().add( nodeFactory.newTree( library ) );
				}
				Collections.sort( rootItem.getChildren(), nodeFactory.getComparator() );
			}
			libraryTreeView.setRoot( rootItem );
		});
	}
	
	/**
	 * Populates the map of commit history dates for each release member.
	 */
	private void updateCommitHistories() {
		Release release = getRelease();
		
		if (release != null) {
			for (ReleaseMember member : release.getAllMembers()) {
				if (commitHistoryMap.containsKey( member )) {
					continue;
				}
				try {
					commitHistoryMap.put( member, releaseManager.getCommitHistory( member ) );
					
				} catch (RepositoryException e) {
					// Warn and proceed without error
				}
				
			}
		} else {
			commitHistoryMap.clear();
		}
	}
	
	/**
	 * Updates the enabled state of the repository menu items based on the release's
	 * base namespace value.
	 */
	@SuppressWarnings("unchecked")
	private void updateActiveRepositories() {
		String baseNS = releaseBaseNamespace.textProperty().getValue();
		
		for (MenuItem menuItem : publishReleaseMenu.getItems()) {
			List<String> rootNamespaces = (List<String>)
					menuItem.getProperties().get( "rootNamespaces" );
			boolean isValidNS = false;
			
			for (String rootNS : rootNamespaces) {
				if (baseNS.equals( rootNS ) || baseNS.startsWith( rootNS + "/" )) {
					isValidNS = true;
					break;
				}
			}
			menuItem.setDisable( !isValidNS );
		}
	}
	
	/**
	 * Validates the field values of all visual fields.  This method must be called
	 * from within the UI thread.
	 */
	private void validateFields() {
		if (releaseManager != null) {
			boolean oldHasError = hasError;
			boolean isValid = true;
			
			isValid &= Validator.validateTextField( releaseName, "release name", 256, true );
			isValid &= Validator.validateURLTextField( releaseBaseNamespace, "base namespace", true );
			isValid &= Validator.validateVersionTextField( releaseVersion, "release version", true );
			isValid &= Validator.validateURLTextField( serviceEndpointUrl, "service endpoint URL", false );
			isValid &= Validator.validateURLTextField( baseResourceUrl, "base resource URL", false );
			hasError = !isValid;
			
			if (hasError != oldHasError) {
				if (hasError) {
					setStatusMessage("Error(s) exist in form fields", StatusType.ERROR, false );
				} else {
					setStatusMessage( null, null, false );
				}
			}
			updateControlStates();
			
		} else {
			Validator.clearErrorMessage( releaseName );
			Validator.clearErrorMessage( releaseBaseNamespace );
			Validator.clearErrorMessage( releaseVersion );
			Validator.clearErrorMessage( serviceEndpointUrl );
			Validator.clearErrorMessage( baseResourceUrl );
		}
	}
	
	/**
	 * Converts the given date to a parseable string.
	 * 
	 * @param date  the date to be formatted
	 * @return String
	 */
	private String toDateString(Date date) {
		String dateStr = null;
		
		if (date == null) {
			dateStr = BLANK_DATE_VALUE;
			
		} else {
			Release release = getRelease();
			Date defaultDate = (release == null) ? null : release.getDefaultEffectiveDate();
			
			dateStr = dateTimeFormat.format( date );
			
			if ((defaultDate != null) && defaultDate.equals( date )) {
				dateStr += DEFAULT_SUFFIX;
			}
		}
		return dateStr;
	}
	
	/**
	 * Converts the given repository item commit to a parseable date string.
	 * 
	 * @param commit  the repository item commit to be formatted as a date
	 * @return String
	 */
	private String toDateString(RepositoryItemCommit commit) {
		StringBuilder dateStr = new StringBuilder();
		String remarks = commit.getRemarks();
		
		dateStr.append( dateTimeFormat.format( commit.getEffectiveOn() ) );
		
		if ((remarks != null) && (remarks.trim().length() > 0)) {
			dateStr.append(" (").append( remarks.trim() ).append(")");
		}
		return dateStr.toString();
	}
	
	/**
	 * Parses an effective date from the string provided.
	 * 
	 * @param dateStr  the date string to parse
	 * @return Date
	 */
	private Date parseEffectiveDate(String dateStr) {
		Date date = null;
		
		try {
			if (dateStr != null) {
				int suffixIdx = dateStr.indexOf(" (");
				
				if (suffixIdx >= 0) {
					dateStr = dateStr.substring( 0, suffixIdx );
				}
			}
			date = ((dateStr == null) || dateStr.equals( BLANK_DATE_VALUE ))
					? null : dateTimeFormat.parse( dateStr );
			
		} catch (ParseException e) {
			// Ignore error and return null
		}
		return date;
	}
	
	/**
	 * Returns the release or null if one is not currently open in the editor.
	 * 
	 * @return Release
	 */
	private Release getRelease() {
		return (releaseManager == null) ? null : releaseManager.getRelease();
	}
	
	/**
	 * Returns the compiler options for the release or null if a release is not currently open.
	 * 
	 * @return ReleaseCompileOptions
	 */
	private ReleaseCompileOptions getCompileOptions() {
		Release release = getRelease();
		return (release == null) ? null : release.getCompileOptions();
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller
	 * and initializes all visual controls.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	@Override
	public void initialize(Stage primaryStage) {
		List<String> bindingStyles = CompilerExtensionRegistry.getAvailableExtensionIds();
		String defaultStyle = CompilerExtensionRegistry.getActiveExtension();
		UserSettings userSettings = UserSettings.load();
		
		super.initialize( primaryStage );
		
		// Initialize the possible values in choice groups and spinners
		bindingStyleChoice.setItems( FXCollections.observableArrayList( bindingStyles ) );
		bindingStyleChoice.setValue( defaultStyle );
		maxRepeatSpinner.setValueFactory( new IntegerSpinnerValueFactory( 1, 3, 3, 1 ) );
		maxRecursionDepthSpinner.setValueFactory( new IntegerSpinnerValueFactory( 1, 3, 3, 1 ) );
		
		// Initialize the list of repository menu items
		List<RemoteRepository> repositories = repositoryManager.listRemoteRepositories();
		
		for (RemoteRepository repository : repositories) {
			try {
				MenuItem repoMenu = new MenuItem();
				
				repoMenu.setText( repository.getDisplayName() );
				repoMenu.getProperties().put( "repository", repository );
				repoMenu.getProperties().put( "rootNamespaces", repository.listRootNamespaces() );
				repoMenu.setOnAction( event -> {
					publishRelease( repoMenu );
				});
				publishReleaseMenu.getItems().add( repoMenu );
				
			} catch (RepositoryException e) {
				// No error - skip and move on
			}
		}
		
		// Initialize the default effective date controls
		defaultEffectiveDate = new LocalDateTimeTextField();
		defaultEffectiveDate.setPrefWidth( 180.0D );
		defaultEffectiveDate.setPadding( new Insets( 3, 0, 0, 0 ) );
		timeZoneLabel = new Label( ZoneId.systemDefault().getDisplayName( TextStyle.SHORT, Locale.getDefault() )
				+ " (" + ZoneId.systemDefault().toString() + ")");
		timeZoneLabel.setPadding( new Insets( 8, 5, 5, 5 ) );
		applyToAllButton = new Button( "Apply to all members..." );
		effectiveDateHBox.getChildren().add( defaultEffectiveDate );
		effectiveDateHBox.getChildren().add( timeZoneLabel );
		effectiveDateHBox.getChildren().add( applyToAllButton );
		HBox.setMargin( defaultEffectiveDate, new Insets( 4, 0, 2, 0 ) );
		HBox.setMargin( timeZoneLabel, new Insets( 4, 0, 2, 0 ) );
		HBox.setMargin( applyToAllButton, new Insets( 7, 5, 7, 5 ) );
		
		// Initialize value change listeners for all controls
		undoManager.addListener( manager -> {
			Platform.runLater( () -> {
				undoMenu.setDisable( !manager.canUndo() );
				redoMenu.setDisable( !manager.canRedo() );
			});
		});
		
		releaseName.textProperty().addListener( (observable, oldValue, newValue) -> {
			new TextInputUndoableAction( releaseName, oldValue, undoManager, () -> {
				handleReleaseFieldModified( false );
			} ).submit();
		} );
		releaseBaseNamespace.textProperty().addListener( (observable, oldValue, newValue) -> {
			new TextInputUndoableAction( releaseBaseNamespace, oldValue, undoManager, () -> {
				handleReleaseFieldModified( true );
			} ).submit();
		} );
		releaseVersion.textProperty().addListener( (observable, oldValue, newValue) -> {
			new TextInputUndoableAction( releaseVersion, oldValue, undoManager, () -> {
				handleReleaseFieldModified( false );
			} ).submit();
		} );
		defaultEffectiveDate.localDateTimeProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( defaultEffectiveDate.localDateTimeProperty(), oldValue, undoManager, () -> {
				handleReleaseFieldModified( false );
			} ).submit();
		} );
		applyToAllButton.setOnAction( event -> {
			applyEffectiveDateToAllMembers( event );
		});
		releaseDescription.textProperty().addListener( (observable, oldValue, newValue) -> {
			new TextInputUndoableAction( releaseDescription, oldValue, undoManager, () -> {
				handleReleaseFieldModified( false );
			} ).submit();
		} );
		principalTableView.getSelectionModel().selectedItemProperty().addListener(
			(obs, oldSelection, newSelection) -> { updateControlStates(); }
		);
		bindingStyleChoice.valueProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( bindingStyleChoice.valueProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		compileXmlSchemasCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( compileXmlSchemasCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		compileServicesCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( compileServicesCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		compileJsonSchemasCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( compileJsonSchemasCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		compileSwaggerCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( compileSwaggerCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		compileDocumentationCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( compileDocumentationCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		serviceEndpointUrl.textProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( serviceEndpointUrl.textProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		baseResourceUrl.textProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( baseResourceUrl.textProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		suppressExtensionsCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( suppressExtensionsCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		generateExamplesCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( generateExamplesCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		exampleMaxDetailCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( exampleMaxDetailCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		maxRepeatSpinner.valueProperty().addListener( (observable, oldValue, newValue) -> {
			new SpinnerUndoableAction<>( maxRepeatSpinner, oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		maxRecursionDepthSpinner.valueProperty().addListener( (observable, oldValue, newValue) -> {
			new SpinnerUndoableAction<>( maxRecursionDepthSpinner, oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		suppressOptionalFieldsCheckbox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
			new WritableValueUndoableAction<>( suppressOptionalFieldsCheckbox.selectedProperty(), oldValue, undoManager, () -> {
				handleCompileOptionModified();
			} ).submit();
		} );
		libraryTreeView.getSelectionModel().selectedItemProperty().addListener( event -> {
			TreeItem<TreeNode<?>> treeItem = libraryTreeView.getSelectionModel().getSelectedItem();
			List<NodeProperty> nodeProps = (treeItem == null) ? Collections.emptyList() : treeItem.getValue().getProperties();
			
			propertyTableView.setItems( FXCollections.observableList( nodeProps ) );
		});
		
		// Configure the display options for all tables
		principalTableView.setEditable( true );
		principalDateColumn.setEditable( true );
		principalTableView.setRowFactory(tv -> new TableRow<ReleaseMember>() {
            private Tooltip tooltip = new Tooltip();
            public void updateItem(ReleaseMember member, boolean empty) {
                super.updateItem( member, empty );
                if (member == null) {
                    setTooltip( null );
                } else {
                    tooltip.setText( member.getRepositoryItem().getNamespace() );
                    setTooltip(tooltip);
                }
            }
        });
		principalNameColumn.setCellValueFactory( nodeFeatures -> {
			AbstractLibrary library = releaseManager.getLibrary( nodeFeatures.getValue() );
			StringBuilder nameValue = new StringBuilder( library.getName() );
			
			nameValue.append(" (").append( library.getVersion() ).append(")");
			return new ReadOnlyStringWrapper( nameValue.toString() );
		});
		principalStatusColumn.setCellValueFactory( nodeFeatures -> {
			AbstractLibrary library = releaseManager.getLibrary( nodeFeatures.getValue() );
			TLLibraryStatus status = (library instanceof TLLibrary) ? ((TLLibrary) library).getStatus() : TLLibraryStatus.FINAL;
			
			return new ReadOnlyStringWrapper( MessageBuilder.formatMessage( status.toString() ) );
		});
		principalDateColumn.setCellValueFactory( nodeFeatures -> {
			return new ReadOnlyObjectWrapper<String>( toDateString( nodeFeatures.getValue().getEffectiveDate() ) );
		});
		principalDateColumn.setCellFactory( nodeFeatures -> {
			return new EffectiveDateCell();
		});
		
		referencedTableView.setEditable( true );
		referencedDateColumn.setEditable( true );
		referencedTableView.setRowFactory(tv -> new TableRow<ReleaseMember>() {
            private Tooltip tooltip = new Tooltip();
            public void updateItem(ReleaseMember member, boolean empty) {
                super.updateItem( member, empty );
                if (member == null) {
                    setTooltip( null );
                } else {
                    tooltip.setText( member.getRepositoryItem().getNamespace() );
                    setTooltip(tooltip);
                }
            }
        });
		referencedNameColumn.setCellValueFactory( nodeFeatures -> {
			RepositoryItem item = nodeFeatures.getValue().getRepositoryItem();
			StringBuilder nameValue = new StringBuilder( item.getLibraryName() );
			
			nameValue.append(" (").append( item.getVersion() ).append(")");
			return new ReadOnlyStringWrapper( nameValue.toString() );
		});
		referencedStatusColumn.setCellValueFactory( nodeFeatures -> {
			AbstractLibrary library = releaseManager.getLibrary( nodeFeatures.getValue() );
			TLLibraryStatus status = (library instanceof TLLibrary) ? ((TLLibrary) library).getStatus() : TLLibraryStatus.FINAL;
			
			return new ReadOnlyStringWrapper( MessageBuilder.formatMessage( status.toString() ) );
		});
		referencedDateColumn.setCellValueFactory( nodeFeatures -> {
			return new ReadOnlyObjectWrapper<String>( toDateString( nodeFeatures.getValue().getEffectiveDate() ) );
		});
		referencedDateColumn.setCellFactory( nodeFeatures -> {
			return new EffectiveDateCell();
		});
		
		facetSelectionTableView.setEditable( true );
		facetSelectionColumn.setEditable( true );
		facetOwnerColumn.setCellValueFactory( nodeFeatures -> {
			TLFacetOwner facetOwner = nodeFeatures.getValue().getFacetOwner();
			String prefix = facetOwner.getOwningLibrary().getPrefix();
			
			return new ReadOnlyStringWrapper( prefix + ":" + facetOwner.getLocalName() );
		});
		facetSelectionColumn.setCellValueFactory( nodeFeatures -> {
			String selectedFacet = nodeFeatures.getValue().getSelectedFacetName();
			return new ReadOnlyStringWrapper( (selectedFacet == null) ? SUBSTITUTION_GROUP_CHOICE : selectedFacet );
		});
		facetSelectionColumn.setCellFactory( nodeFeatures -> {
			return new FacetSelectCell();
		});
		
		validationLevelColumn.setCellValueFactory( nodeFeatures -> {
			FindingType findingType = nodeFeatures.getValue().getType();
			Image image = (findingType == FindingType.WARNING) ?
					StatusType.WARNING.getIcon() : StatusType.ERROR.getIcon();
			
			return new SimpleObjectProperty<ImageView>(new ImageView(image));
		});
		validationComponentColumn.setCellValueFactory( nodeFeatures -> {
			return new ReadOnlyStringWrapper( nodeFeatures.getValue().getSource().getValidationIdentity() );
		});
		validationDescriptionColumn.setCellValueFactory( nodeFeatures -> {
				return new ReadOnlyStringWrapper( nodeFeatures.getValue().getFormattedMessage( FindingMessageFormat.BARE_FORMAT ) );
		});
		
		propertyNameColumn.setCellValueFactory( nodeFeatures -> {
			return new ReadOnlyStringWrapper( nodeFeatures.getValue().getName() );
		});
		propertyValueColumn.setCellValueFactory( nodeFeatures -> {
			return new ReadOnlyStringWrapper( nodeFeatures.getValue().getValue() );
		});
		
		// Configure placeholder labels for empty tables
		principalTableView.setPlaceholder( new Label("") );
		referencedTableView.setPlaceholder( new Label("") );
		facetSelectionTableView.setPlaceholder( new Label("") );
		validationTableView.setPlaceholder( new Label("") );
		propertyTableView.setPlaceholder( new Label("") );
		
		// Complete initialization and update the control states
		this.releaseAccordion.setExpandedPane( this.releaseMembersPane );
		this.libraryTreeView.setShowRoot( false );
		this.userSettings = userSettings;
		
		updateControlStates();
	}
	
	/**
	 * Table cell that allows users to choose an effective date for an OTM release
	 * member.
	 */
	private class EffectiveDateCell extends ChoiceBoxTableCell<ReleaseMember,String> {
		
		/**
		 * Default constructor.
		 */
		public EffectiveDateCell() {
			super( FXCollections.observableList( new ArrayList<>() ) );
			setOnMouseEntered( new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			    	boolean isEditable = EffectiveDateCell.this.isEditable();
			    	EffectiveDateCell.this.getScene().setCursor( isEditable ? Cursor.HAND : Cursor.DEFAULT );
			    }
			} );
			setOnMouseExited( new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			    	EffectiveDateCell.this.getScene().setCursor( Cursor.DEFAULT );
			    }
			} );
		}
		
		/**
		 * @see javafx.scene.control.cell.ChoiceBoxTableCell#updateItem(java.lang.Object, boolean)
		 */
		@Override
		public void updateItem(String value, boolean empty) {
			List<String> itemList = null;
			
			getItems().clear();
			
			if (!empty) {
				Release release = getRelease();
				List<ReleaseMember> memberList = getTableView().getItems();
				ReleaseMember member = memberList.get( getTableRow().getIndex() );
				Date defaultDate = (release == null) ? null : release.getDefaultEffectiveDate();
				List<RepositoryItemCommit> commitHistory = commitHistoryMap.get( member );
				
				itemList = new ArrayList<>();
				itemList.add( BLANK_DATE_VALUE );
				
				if ((defaultDate != null) && !Utils.isAfterLatestCommit( defaultDate, commitHistory )) {
					itemList.add( toDateString( defaultDate ) );
				}
				if (commitHistory != null) {
					for (RepositoryItemCommit commit : commitHistory) {
						itemList.add( toDateString( commit ) );
					}
				}
				getItems().addAll( FXCollections.observableArrayList( itemList ) );
			}
			setEditable( !empty );
			super.updateItem( value, empty );
		}

		/**
		 * @see javafx.scene.control.TableCell#commitEdit(java.lang.Object)
		 */
		@Override
		public void commitEdit(String newValue) {
			List<ReleaseMember> memberList = getTableView().getItems();
			int tableRow = getTableRow().getIndex();
			ReleaseMember _member = (tableRow < 0) ? null : memberList.get( tableRow );
			
			if (_member != null) {
				new UndoableAction( undoManager ) {
					
					private ReleaseMember member = _member;
					private Date oldEffectiveDate = (member == null) ? null : member.getEffectiveDate();
					private Date newEffectiveDate = parseEffectiveDate( newValue );
					
					public boolean doExecute() {
						updateCellValue( newEffectiveDate );
						return true;
					}

					public boolean doExecuteUndo() {
						updateCellValue( oldEffectiveDate );
						return true;
					}

					public boolean doExecuteRedo() {
						return doExecute();
					}
					
					private void updateCellValue(Date effectiveDate) {
						String dateStr = toDateString( effectiveDate );
						
						member.setEffectiveDate( effectiveDate );
						setItem( dateStr );
						releaseDirty = true;
						modelDirty = true;
						getTableView().refresh();
						updateControlStates();
					}
					
				}.submit();
			}
		}

	}
	
	/**
	 * Table cell that allows the user to select a facet from the available list
	 * for substitutable OTM entities.
	 */
	private class FacetSelectCell extends ChoiceBoxTableCell<FacetSelection,String> {
		
		/**
		 * Default constructor.
		 */
		public FacetSelectCell() {
			super( FXCollections.observableList( new ArrayList<>() ) );
			setOnMouseEntered( new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			    	boolean isEditable = FacetSelectCell.this.isEditable();
			    	FacetSelectCell.this.getScene().setCursor( isEditable ? Cursor.HAND : Cursor.DEFAULT );
			    }
			} );
			setOnMouseExited( new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
			    	FacetSelectCell.this.getScene().setCursor( Cursor.DEFAULT );
			    }
			} );
		}
		
		/**
		 * @see javafx.scene.control.cell.ChoiceBoxTableCell#updateItem(java.lang.Object, boolean)
		 */
		@Override
		public void updateItem(String value, boolean empty) {
			List<String> itemList = null;
			boolean editable = false;
			
			if (!empty) {
				List<FacetSelection> facetSelections = getTableView().getItems();
				FacetSelection facetSelection = facetSelections.get( getTableRow().getIndex() );
				itemList = new ArrayList<>();
				
				itemList.add( SUBSTITUTION_GROUP_CHOICE );
				itemList.addAll( facetSelection.getFacetNames() );
				editable = (itemList != null) && (itemList.size() > 1);
				if (!editable) itemList = null;
			}
			setEditable( editable );
			getItems().clear();
			
			if (editable) {
				getItems().addAll( FXCollections.observableArrayList( itemList ) );
			}
			super.updateItem( value, empty );
		}

		/**
		 * @see javafx.scene.control.TableCell#commitEdit(java.lang.Object)
		 */
		@Override
		public void commitEdit(String newValue) {
			List<FacetSelection> facetSelections = getTableView().getItems();
			int tableRow = getTableRow().getIndex();
			FacetSelection _facetSelection = (tableRow < 0) ? null : facetSelections.get( tableRow );
			String _adjustedValue, adjustedValue = newValue;
			
			if ((adjustedValue != null) && adjustedValue.equals( SUBSTITUTION_GROUP_CHOICE )) {
				adjustedValue = null;
			}
			_adjustedValue = adjustedValue;
			
			if (_facetSelection != null) {
				new UndoableAction( undoManager ) {
					
					private FacetSelection facetSelection = _facetSelection;
					private String oldValue = facetSelection.getSelectedFacetName();
					private String newValue = _adjustedValue;
					
					public boolean doExecute() {
						updateCellValue( newValue );
						return true;
					}

					public boolean doExecuteUndo() {
						updateCellValue( oldValue );
						return true;
					}

					public boolean doExecuteRedo() {
						return doExecute();
					}
					
					private void updateCellValue(String value) {
						facetSelection.setSelectedFacet( value );
						handleFacetSelectionsModified();
						setItem( value );
						getTableView().refresh();
					}
					
				}.submit();
			}
		}

	}
	
}
