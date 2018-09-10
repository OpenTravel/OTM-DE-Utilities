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

package org.opentravel.diffutil;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.BrowseRepositoryDialogController;
import org.opentravel.application.common.StatusType;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.ReleaseManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;
import org.opentravel.schemacompiler.repository.RepositoryItemType;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ModelComparator;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the OTM-Diff application.
 */
public class DiffUtilityController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-diff-util.fxml";
	
	@FXML private TextField oldProjectFilename;
	@FXML private TextField newProjectFilename;
	@FXML private TextField oldLibraryFilename;
	@FXML private TextField newLibraryFilename;
	@FXML private ChoiceBox<ChoiceItem> oldEntityChoice;
	@FXML private ChoiceBox<ChoiceItem> newEntityChoice;
	@FXML private Button oldProjectFileButton;
	@FXML private Button newProjectFileButton;
	@FXML private Button oldReleaseFileButton;
	@FXML private Button newReleaseFileButton;
	@FXML private Button oldLibraryFileButton;
	@FXML private Button newLibraryFileButton;
	@FXML private Button oldLibraryRepoButton;
	@FXML private Button newLibraryRepoButton;
	@FXML private Label oldCommitLabel;
	@FXML private Label newCommitLabel;
	@FXML private ChoiceBox<CommitChoiceItem> oldCommitChoice;
	@FXML private ChoiceBox<CommitChoiceItem> newCommitChoice;
	@FXML private Button runProjectButton;
	@FXML private Button runLibraryButton;
	@FXML private WebView reportViewer;
	@FXML private Button backButton;
	@FXML private Button forwardButton;
	@FXML private Button saveReportButton;
	@FXML private Label statusBarLabel;
	
	private File oldProjectOrReleaseFile;
	private File newProjectOrReleaseFile;
	private File oldLibraryFile;
	private File newLibraryFile;
	private RepositoryItem oldLibraryRepoItem;
	private RepositoryItem newLibraryRepoItem;
	private RepositoryItemCommit oldLibraryCommit;
	private RepositoryItemCommit newLibraryCommit;
	private RepositoryItem oldReleaseRepoItem;
	private RepositoryItem newReleaseRepoItem;
	
	private ProjectManager oldProjectManager = new ProjectManager( new TLModel(), false, null );
	private ProjectManager newProjectManager = new ProjectManager( new TLModel(), false, null );
	private UserSettings userSettings;
	
	/**
	 * Called when the user clicks the button select the file for the old version
	 * of an OTM project.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOldProject(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select Old Project or Release Version",
				userSettings.getOldProjectFolder(),
				new String[] { "*.otp", "OTM Project Files (*.otp)" },
				new String[] { "*.otr", "OTM Release Files (*.otr)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			oldReleaseRepoItem = null;
			oldProjectOrReleaseFile = selectedFile;
			oldProjectFilename.setText( selectedFile.getName() );
			updateControlStates();
			userSettings.setOldProjectFolder( selectedFile.getParentFile() );
			userSettings.save();
		}
	}
	
	/**
	 * Called when the user clicks the button select the file for the new version
	 * of an OTM project.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectNewProject(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select New Project or Release Version",
				userSettings.getNewProjectFolder(),
				new String[] { "*.otp", "OTM Project Files (*.otp)" },
				new String[] { "*.otr", "OTM Release Files (*.otr)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			newReleaseRepoItem = null;
			newProjectOrReleaseFile = selectedFile;
			newProjectFilename.setText( selectedFile.getName() );
			updateControlStates();
			userSettings.setNewProjectFolder( selectedFile.getParentFile() );
			userSettings.save();
		}
	}
	
	/**
	 * Called when the user clicks the button select the old version of an OTM
	 * release from a remote repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOldRelease(ActionEvent event) {
		BrowseRepositoryDialogController controller = BrowseRepositoryDialogController.createDialog(
				"Select Old Release Version", RepositoryItemType.RELEASE, getPrimaryStage() );
		
		if (controller != null) {
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				oldProjectOrReleaseFile = null;
				oldReleaseRepoItem = controller.getSelectedRepositoryItem();
				oldProjectFilename.setText( oldReleaseRepoItem.getFilename() );
				updateControlStates();
			}
		}
	}
	
	/**
	 * Called when the user clicks the button select the new version of an OTM
	 * release from a remote repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectNewRelease(ActionEvent event) {
		BrowseRepositoryDialogController controller = BrowseRepositoryDialogController.createDialog(
				"Select New Release Version", RepositoryItemType.RELEASE, getPrimaryStage() );
		
		if (controller != null) {
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				newProjectOrReleaseFile = null;
				newReleaseRepoItem = controller.getSelectedRepositoryItem();
				newProjectFilename.setText( newReleaseRepoItem.getFilename() );
				updateControlStates();
			}
		}
	}
	
	/**
	 * Called when the user clicks the button select the old version library from a
	 * file on the local file system.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOldLibraryFromFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select Old Library Version",
				userSettings.getOldLibraryFolder(),
				new String[] { "*.otm", "OTM Library Files (*.otm)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Loading Library: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						TLLibrary library;
						
						oldLibraryRepoItem = null;
						oldLibraryCommit = null;
						oldLibraryFile = selectedFile;
						setFilenameText( selectedFile.getName(), oldLibraryFilename );
						library = loadLibrary( selectedFile, oldProjectManager );
						updateEntityList( oldEntityChoice, library );
						updateCommitList( oldCommitChoice, null );
						
					} finally {
						closeAllProjects( oldProjectManager );
						userSettings.setOldLibraryFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the old version library from a
	 * remote repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOldLibraryFromRepo(ActionEvent event) {
		BrowseRepositoryDialogController controller = BrowseRepositoryDialogController.createDialog(
				"Select Old Library Version", RepositoryItemType.LIBRARY, getPrimaryStage() );
		
		if (controller != null) {
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				final RepositoryItem selectedItem = controller.getSelectedRepositoryItem();
				Runnable r = new BackgroundTask( "Loading Library: " + selectedItem.getFilename(), StatusType.INFO ) {
					public void execute() throws Throwable {
						try {
							TLLibrary library;
							
							oldLibraryFile = null;
							oldLibraryCommit = null;
							oldLibraryRepoItem = selectedItem;
							setFilenameText( selectedItem.getFilename(), oldLibraryFilename );
							library = loadLibrary( selectedItem, null, oldProjectManager );
							updateEntityList( oldEntityChoice, library );
							updateCommitList( oldCommitChoice, oldLibraryRepoItem );
							
						} finally {
							closeAllProjects( oldProjectManager );
						}
					}
				};
				
				new Thread( r ).start();
			}
		}
	}
	
	/**
	 * Called when the user clicks the button select the new version library from a
	 * file on the local file system.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectNewLibraryFromFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select New Library Version",
				userSettings.getNewLibraryFolder(),
				new String[] { "*.otm", "OTM Library Files (*.otm)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File selectedFile = chooser.showOpenDialog( getPrimaryStage() );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Loading Library: " + selectedFile.getName(), StatusType.INFO ) {
				public void execute() throws Throwable {
					try {
						TLLibrary library;
						
						newLibraryRepoItem = null;
						newLibraryCommit = null;
						newLibraryFile = selectedFile;
						setFilenameText( selectedFile.getName(), newLibraryFilename );
						library = loadLibrary( selectedFile, newProjectManager );
						updateEntityList( newEntityChoice, library );
						updateCommitList( newCommitChoice, null );
						
					} finally {
						closeAllProjects( newProjectManager );
						userSettings.setNewLibraryFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the new version library from a
	 * remote repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectNewLibraryFromRepo(ActionEvent event) {
		BrowseRepositoryDialogController controller = BrowseRepositoryDialogController.createDialog(
				"Select New Library Version", RepositoryItemType.LIBRARY, getPrimaryStage() );
		
		if (controller != null) {
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				RepositoryItem selectedItem = controller.getSelectedRepositoryItem();
				Runnable r = new BackgroundTask( "Loading Library: " + selectedItem.getFilename(), StatusType.INFO ) {
					public void execute() throws Throwable {
						try {
							TLLibrary library;
							
							newLibraryFile = null;
							newLibraryCommit = null;
							newLibraryRepoItem = selectedItem;
							setFilenameText( selectedItem.getFilename(), newLibraryFilename );
							library = loadLibrary( selectedItem, null, newProjectManager );
							updateEntityList( newEntityChoice, library );
							updateCommitList( newCommitChoice, newLibraryRepoItem );
							
						} catch (Throwable t) {
							t.printStackTrace( System.out );
							
						} finally {
							setStatusMessage( null, null, false );
							updateControlStates();
							closeAllProjects( newProjectManager );
						}
					}
				};
				
				new Thread( r ).start();
			}
		}
	}
	
	/**
	 * Called when the user clicks the button to save the document currently displayed
	 * in the report viewer.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveReport(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Save Report",
				userSettings.getReportFolder(),
				new String[] { "*.html", "HTML Files (*.html)" },
				new String[] { "*.*", "All Files (*.*)" } );
		File targetFile = chooser.showSaveDialog( getPrimaryStage() );
		
		if (targetFile != null) {
			Runnable r = new BackgroundTask( "Saving Report", StatusType.INFO ) {
				protected void execute() throws Throwable {
					try {
						URL reportUrl = new URL( reportViewer.getEngine().getLocation() );
						File reportFile = new File( reportUrl.toURI() );
						
						try (InputStream in = new FileInputStream( reportFile )) {
							try (OutputStream out = new FileOutputStream( targetFile )) {
								byte[] buffer = new byte[1024];
								int count;
								
								while ((count = in.read( buffer, 0, buffer.length )) >= 0) {
									out.write( buffer, 0, count );
								}
							}
						}
						
					} finally {
						userSettings.setReportFolder( targetFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the back button for the report viewer browser.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void browserBack(ActionEvent event) {
		reportViewer.getEngine().getHistory().go( -1 );
		updateControlStates();
	}
	
	/**
	 * Called when the user clicks the forward button for the report viewer browser.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void browserForward(ActionEvent event) {
		reportViewer.getEngine().getHistory().go( 1 );
		updateControlStates();
	}
	
	/**
	 * Called when the user clicks the back button for the report viewer browser.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void editSettings(ActionEvent event) {
		OptionsDialogController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader( DiffUtilityController.class.getResource(
					OptionsDialogController.FXML_FILE ) );
			BorderPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( "Model Comparison Options" );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( getPrimaryStage() );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.setDialogStage( dialogStage );
			controller.setCompareOptions( userSettings.getCompareOptions() );
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				userSettings.save();
			}
			
		} catch (IOException e) {
			e.printStackTrace( System.out );
		}
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#updateControlStates()
	 */
	@Override
	protected void updateControlStates() {
		Platform.runLater( new Runnable() {
			public void run() {
				String reportLocation = reportViewer.getEngine().getLocation();
				int historyIdx = reportViewer.getEngine().getHistory().getCurrentIndex();
				int historySize = reportViewer.getEngine().getHistory().getEntries().size();
				boolean reportDisplayed = (reportLocation != null) && reportLocation.startsWith( "file:" );
				boolean canBrowseBack = historyIdx > 0;
				boolean canBrowseForward = historyIdx < (historySize - 1);
				boolean runProjectEnabled = (oldProjectOrReleaseFile != null) && isProjectFile( oldProjectOrReleaseFile ) && oldProjectOrReleaseFile.exists()
						&& (newProjectOrReleaseFile != null) && isProjectFile( newProjectOrReleaseFile ) && newProjectOrReleaseFile.exists();
				boolean runReleaseEnabled = ((oldReleaseRepoItem != null) || ((oldProjectOrReleaseFile != null) && isReleaseFile( oldProjectOrReleaseFile ) && oldProjectOrReleaseFile.exists()))
						&& ((newReleaseRepoItem != null) || ((newProjectOrReleaseFile != null) && isReleaseFile( newProjectOrReleaseFile ) && newProjectOrReleaseFile.exists()));
				boolean oldLibrarySelected = (((oldLibraryFile != null) && oldLibraryFile.exists()) || (oldLibraryRepoItem != null));
				boolean newLibrarySelected = (((newLibraryFile != null) && newLibraryFile.exists()) || (newLibraryRepoItem != null));
				boolean nullOldEntitySelected = (oldEntityChoice.getValue() != null) && (oldEntityChoice.getValue().value == null);
				boolean nullNewEntitySelected = (newEntityChoice.getValue() != null) && (newEntityChoice.getValue().value == null);
				boolean resourceOldEntitySelected = (oldEntityChoice.getValue() != null) && (oldEntityChoice.getValue().label.startsWith("Resource:"));
				boolean resourceNewEntitySelected = (newEntityChoice.getValue() != null) && (newEntityChoice.getValue().label.startsWith("Resource:"));
				boolean runLibraryEnabled = oldLibrarySelected && newLibrarySelected
						&& ( (nullOldEntitySelected && (nullOldEntitySelected == nullNewEntitySelected))
								|| (!nullOldEntitySelected && (resourceOldEntitySelected == resourceNewEntitySelected)) );
				boolean oldLibraryManaged = (oldLibraryRepoItem != null);
				boolean newLibraryManaged = (newLibraryRepoItem != null);
				
				runProjectButton.disableProperty().set( !(runProjectEnabled || runReleaseEnabled) );
				runLibraryButton.disableProperty().set( !runLibraryEnabled );
				saveReportButton.disableProperty().set( !reportDisplayed );
				backButton.disableProperty().set( !canBrowseBack );
				forwardButton.disableProperty().set( !canBrowseForward );
				oldCommitLabel.disableProperty().set( !oldLibraryManaged );
				oldCommitChoice.disableProperty().set( !oldLibraryManaged );
				newCommitLabel.disableProperty().set( !newLibraryManaged );
				newCommitChoice.disableProperty().set( !newLibraryManaged );
			}
		});
	}
	
	/**
	 * Updates the value of the specified filename text field.
	 * 
	 * @param filenameValue  the value to assign to the filename text field
	 * @param textField  the text field to which the value will be assigned
	 */
	private void setFilenameText(String filenameValue, TextField textField) {
		Platform.runLater( new Runnable() {
			public void run() {
				textField.setText( filenameValue );
			}
		});
	}
	
	/**
	 * Displays the contents of the specified file in the HTML report viewer.
	 * 
	 * @param reportFile  the HTML report file to display
	 */
	private void showReport(final File reportFile) {
		Platform.runLater( new Runnable() {
			public void run() {
				if (reportFile != null) {
					reportViewer.getEngine().getHistory().setMaxSize( 0 );
					reportViewer.getEngine().getHistory().setMaxSize( 100 );
					reportViewer.getEngine().load( reportFile.toURI().toString() );
					
				} else {
					reportViewer.getEngine().loadContent( "" );
				}
			}
		});
	}
	
	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#setStatusMessage(java.lang.String, org.opentravel.application.common.StatusType, boolean)
	 */
	@Override
	protected void setStatusMessage(String message, StatusType statusType, boolean disableControls) {
		Platform.runLater( new Runnable() {
			public void run() {
				statusBarLabel.setText( message );
				oldEntityChoice.disableProperty().set( disableControls );
				newEntityChoice.disableProperty().set( disableControls );
				oldProjectFileButton.disableProperty().set( disableControls );
				newProjectFileButton.disableProperty().set( disableControls );
				oldLibraryFileButton.disableProperty().set( disableControls );
				newLibraryFileButton.disableProperty().set( disableControls );
				oldLibraryRepoButton.disableProperty().set( disableControls );
				newLibraryRepoButton.disableProperty().set( disableControls );
				oldCommitLabel.disableProperty().set( disableControls );
				oldCommitChoice.disableProperty().set( disableControls );
				newCommitLabel.disableProperty().set( disableControls );
				newCommitChoice.disableProperty().set( disableControls );
				runProjectButton.disableProperty().set( disableControls );
				runLibraryButton.disableProperty().set( disableControls );
				saveReportButton.disableProperty().set( disableControls );
				backButton.disableProperty().set( disableControls );
				forwardButton.disableProperty().set( disableControls );
			}
		});
	}
	
	/**
	 * Updates the given choice box with the list of entity names from the given library.
	 * 
	 * @param entityChoice  the choice box to update
	 * @param library  the library from which to obtain the list of entity names
	 */
	private void updateEntityList(ChoiceBox<ChoiceItem> entityChoice, TLLibrary library) {
		ObservableList<ChoiceItem> itemList = entityChoice.itemsProperty().get();
		ChoiceItem defaultItem = new ChoiceItem( "< All Entities >", null );
		final List<ChoiceItem> newItems = new ArrayList<>();
		
		newItems.add( defaultItem );
		
		for (NamedEntity entity : library.getNamedMembers()) {
			if ((entity instanceof TLContextualFacet)
					&& ((TLContextualFacet) entity).isLocalFacet()) {
				continue; // skip local contextual facets
			}
			if (entity instanceof TLService) {
				for (TLOperation operation : ((TLService) entity).getOperations()) {
					newItems.add( new ChoiceItem("Operation: " + operation.getName() ) );
				}
				
			} else if (entity instanceof TLResource) {
				newItems.add( new ChoiceItem( "Resource: " + entity.getLocalName(), entity.getLocalName() ) );
				
			} else {
				newItems.add( new ChoiceItem( entity.getLocalName() ) );
			}
		}
		
		Platform.runLater( new Runnable() {
			public void run() {
				itemList.clear();
				itemList.addAll( newItems );
				entityChoice.setValue( defaultItem );
			}
		});
	}
	
	/**
	 * Updates the given choice box with items from the commit history of the given repository item.
	 * 
	 * @param commitChoice  the commit choice box that will allow selection of a commit
	 * @param libraryRepoItem  the repository item for the library whose commit history will be selectable
	 */
	private void updateCommitList(ChoiceBox<CommitChoiceItem> commitChoice, RepositoryItem libraryRepoItem) {
		if (libraryRepoItem != null) {
			try {
				List<RepositoryItemCommit> commitHistory =
						libraryRepoItem.getRepository().getHistory( libraryRepoItem ).getCommitHistory();
				List<CommitChoiceItem> choiceItems = new ArrayList<>();
				
				if (!commitHistory.isEmpty()) {
					commitHistory.forEach( commit -> choiceItems.add( new CommitChoiceItem( commit ) ) );
					choiceItems.get( 0 ).value = null; // first item is the latest commit, so set to null
				}
				
				if (!choiceItems.isEmpty()) {
					Platform.runLater( () -> {
						commitChoice.setItems( FXCollections.observableList( choiceItems ) );
						commitChoice.getSelectionModel().select( 0 );
					} );
				}
				
			} catch (RepositoryException e) {
				Platform.runLater( () -> {
					commitChoice.setItems( FXCollections.emptyObservableList() );
				} );
				e.printStackTrace( System.out );
			}
		} else {
			Platform.runLater( () -> {
				commitChoice.setItems( FXCollections.emptyObservableList() );
			} );
		}
	}
	
	/**
	 * Called when the user modifies the old commit selection.
	 */
	private void oldCommitSelectionChanged() {
		CommitChoiceItem selectedItem = oldCommitChoice.getValue();
		RepositoryItemCommit selectedCommit = (selectedItem == null) ? null : selectedItem.value;
		boolean selectionChanged;
		
		if (selectedCommit == null) {
			selectionChanged = (oldLibraryCommit != null);
		} else {
			selectionChanged = (oldLibraryCommit == null) || (selectedCommit.getCommitNumber() != oldLibraryCommit.getCommitNumber());
		}
		
		if (selectionChanged) {
			Runnable r = new BackgroundTask( "Reloading Library: " +
					oldLibraryRepoItem.getFilename(), StatusType.INFO ) {
				protected void execute() throws Throwable {
					TLLibrary library = loadLibrary( oldLibraryRepoItem,
							oldLibraryCommit, oldProjectManager );
					updateEntityList( oldEntityChoice, library );
				}
			};
			
			oldLibraryCommit = selectedCommit;
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user modifies the new commit selection.
	 */
	private void newCommitSelectionChanged() {
		CommitChoiceItem selectedItem = newCommitChoice.getValue();
		RepositoryItemCommit selectedCommit = (selectedItem == null) ? null : selectedItem.value;
		boolean selectionChanged;
		
		if (selectedCommit == null) {
			selectionChanged = (newLibraryCommit != null);
		} else {
			selectionChanged = (newLibraryCommit == null) || (selectedCommit.getCommitNumber() != newLibraryCommit.getCommitNumber());
		}
		
		if (selectionChanged) {
			Runnable r = new BackgroundTask( "Reloading Library: " +
					newLibraryRepoItem.getFilename(), StatusType.INFO ) {
				protected void execute() throws Throwable {
					TLLibrary library = loadLibrary( newLibraryRepoItem,
							newLibraryCommit, newProjectManager );
					updateEntityList( newEntityChoice, library );
				}
			};
			
			newLibraryCommit = selectedCommit;
			new Thread( r ).start();
		}
	}
	
	/**
	 * Loads and returns an OTM library from the specified file.
	 * 
	 * @param libraryFile  the library file to load
	 * @param projectManager  the project manager to use when processing the load
	 * @return TLLibrary
	 * @throws SchemaCompilerException  thrown if an error occurs during the library loading process
	 */
	private TLLibrary loadLibrary(File libraryFile, ProjectManager projectManager)
			throws SchemaCompilerException {
		Project tempProject;
		ProjectItem item;
		
		projectManager.closeAll();
		tempProject = newTempProject( projectManager );
		item = projectManager.addUnmanagedProjectItem( libraryFile, tempProject );
		return (TLLibrary) item.getContent();
	}
	
	/**
	 * Loads and returns an OTM library from a remote repository.
	 * 
	 * @param libraryItem  the library repository item to load
	 * @param itemCommit  the commit of the managed library to be loaded
	 * @param projectManager  the project manager to use when processing the load
	 * @return TLLibrary
	 * @throws SchemaCompilerException  thrown if an error occurs during the library loading process
	 */
	private TLLibrary loadLibrary(RepositoryItem libraryItem, RepositoryItemCommit itemCommit,
			ProjectManager projectManager) throws SchemaCompilerException {
		/*
		Project tempProject;
		ProjectItem item;
		
		projectManager.closeAll();
		tempProject = newTempProject( projectManager );
		item = projectManager.addManagedProjectItem( libraryItem, tempProject );
		return (TLLibrary) item.getContent();
		*/
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        LibraryInputSource<InputStream> libraryInput;
		
		if (itemCommit == null) {
			RepositoryManager repositoryManager = RepositoryManager.getDefault();
			URL libraryUrl = repositoryManager.getContentLocation( libraryItem );
			
			libraryInput = new LibraryStreamInputSource( libraryUrl );
			
		} else {
			libraryInput = libraryItem.getRepository().getHistoricalContentSource(
					libraryItem, itemCommit.getEffectiveOn() );
		}
		modelLoader.loadLibraryModel( libraryInput );
		
		return (TLLibrary) modelLoader.getLibraryModel().getLibrary(
				libraryItem.getNamespace(), libraryItem.getLibraryName() );
	}
	
	/**
	 * Creates a temporary project under which a library may be loaded.
	 * 
	 * @param projectManager  the project manager in which the new project should be created
	 * @return Project
	 * @throws LibrarySaveException  thrown if the new project cannot be created
	 */
	private Project newTempProject(ProjectManager projectManager) throws LibrarySaveException {
		try {
			return projectManager.newProject( File.createTempFile( "tempProject", ".otp" ),
					"http://diff-util.com/project/temp", "Temp Project", null );
			
		} catch (IOException e) {
			throw new LibrarySaveException("Unable to create temporary project file.", e);
		}
	}
	
	/**
	 * Closes all projects contained within the given project manager.
	 * 
	 * @param projectManager  the project manager for which to close all projects
	 */
	private void closeAllProjects(ProjectManager projectManager) {
		List<File> tempFiles = new ArrayList<>();
		
		for (Project p : projectManager.getAllProjects()) {
			if (p.getProjectId().equals( "http://diff-util.com/project/temp" )) {
				tempFiles.add( p.getProjectFile() );
			}
		}
		projectManager.closeAll();
		
		for (File tempFile : tempFiles) {
			tempFile.delete();
		}
	}
	
	/**
	 * Opens the given URL using the default system web browser.
	 * 
	 * @param url  the URL to browse
	 */
	private void navigateExternalLink(String url) {
		try {
		    Desktop.getDesktop().browse( new URL( url ).toURI() );
		    
		} catch (Exception e) {
			// Ignore error
		}
	}
	
	/**
	 * Returns true if the given file is an OTM project (.otp) file.
	 * 
	 * @param f  the file to check
	 * @return boolean
	 */
	private boolean isProjectFile(File f) {
		return f.getName().toLowerCase().endsWith(".otp");
	}
	
	/**
	 * Returns true if the given file is an OTM release (.otr) file.
	 * 
	 * @param f  the file to check
	 * @return boolean
	 */
	private boolean isReleaseFile(File f) {
		return f.getName().toLowerCase().endsWith(".otr");
	}
	
	/**
	 * Called when the user clicks the 'Run Comparison' button to compare two
	 * OTM projects.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void runProjectComparison(ActionEvent event) {
		Runnable r = new BackgroundTask( "Running comparison...", StatusType.INFO ) {
			public void execute() throws Throwable {
				ValidationFindings oldFindings = new ValidationFindings();
				ValidationFindings newFindings = new ValidationFindings();
				boolean logFindings = false;
				
				showReport( null );
				
				if ((oldProjectOrReleaseFile != null) && (newProjectOrReleaseFile != null)
						&& isProjectFile( oldProjectOrReleaseFile ) && isProjectFile( newProjectOrReleaseFile )) {
					try {
						Project oldProject = oldProjectManager.loadProject( oldProjectOrReleaseFile, oldFindings );
						Project newProject = newProjectManager.loadProject( newProjectOrReleaseFile, newFindings );
						
						if (!oldFindings.hasFinding( FindingType.ERROR ) && !newFindings.hasFinding( FindingType.ERROR )) {
							File reportFile = File.createTempFile( "otmDiff", ".html" );
							
							try (OutputStream out = new FileOutputStream( reportFile )) {
								new ModelComparator( userSettings.getCompareOptions() )
										.compareProjects( oldProject, newProject, out );
							}
							showReport( reportFile );
							reportFile.deleteOnExit();
							
						} else {
							logFindings = true;
						}
						
					} finally {
						closeAllProjects( oldProjectManager );
						closeAllProjects( newProjectManager );
					}
					
				} else { // release comparison
					ReleaseManager oldReleaseManager = new ReleaseManager();
					ReleaseManager newReleaseManager = new ReleaseManager();
					
					if (oldProjectOrReleaseFile != null) {
						oldReleaseManager.loadRelease( oldProjectOrReleaseFile, oldFindings );
					} else {
						oldReleaseManager.loadRelease( oldReleaseRepoItem, oldFindings );
					}
					
					if (newProjectOrReleaseFile != null) {
						newReleaseManager.loadRelease( newProjectOrReleaseFile, newFindings );
					} else {
						newReleaseManager.loadRelease( newReleaseRepoItem, newFindings );
					}
					
					if (!oldFindings.hasFinding( FindingType.ERROR ) && !newFindings.hasFinding( FindingType.ERROR )) {
						File reportFile = File.createTempFile( "otmDiff", ".html" );
						
						try (OutputStream out = new FileOutputStream( reportFile )) {
							new ModelComparator( userSettings.getCompareOptions() )
									.compareReleases( oldReleaseManager, newReleaseManager, out );
						}
						showReport( reportFile );
						reportFile.deleteOnExit();
						
					} else {
						logFindings = true;
					}
				}
				
				if (logFindings) {
					if (oldFindings.hasFinding()) {
						System.out.println("\nErrors/Warnings: " + oldProjectOrReleaseFile.getName());
						for (String message : oldFindings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT )) {
							System.out.println("  " + message);
						}
					}
					if (newFindings.hasFinding()) {
						System.out.println("\nErrors/Warnings: " + newProjectOrReleaseFile.getName());
						for (String message : newFindings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT )) {
							System.out.println("  " + message);
						}
					}
					throw new RuntimeException("Validation error(s) detected in one or both projects.");
				}
			}
		};
		
		new Thread( r ).start();
	}
	
	/**
	 * Called when the user clicks the 'Run Comparison' button to compare two
	 * OTM libraries or entities.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void runLibraryComparison(ActionEvent event) {
		Runnable r = new BackgroundTask( "Running comparison...", StatusType.INFO ) {
			public void execute() throws Throwable {
				try {
					File reportFile = File.createTempFile( "otmDiff", ".html" );
					TLLibrary oldLibrary, newLibrary;
					
					showReport( null );
					
					if (oldLibraryFile != null) {
						oldLibrary = loadLibrary( oldLibraryFile, oldProjectManager );
					} else if (oldLibraryRepoItem != null) {
						oldLibrary = loadLibrary( oldLibraryRepoItem, oldLibraryCommit, oldProjectManager );
					} else {
						throw new IllegalStateException("Old library version not accessible.");
					}
					
					if (newLibraryFile != null) {
						newLibrary = loadLibrary( newLibraryFile, newProjectManager );
					} else if (newLibraryRepoItem != null) {
						newLibrary = loadLibrary( newLibraryRepoItem, newLibraryCommit, newProjectManager );
					} else {
						throw new IllegalStateException("New library version not accessible.");
					}
					
					if (oldEntityChoice.getValue().value == null) { // compare libraries
						
						try (OutputStream out = new FileOutputStream( reportFile )) {
							new ModelComparator( userSettings.getCompareOptions() )
									.compareLibraries( oldLibrary, newLibrary, out );
						}
						
					} else { // compare entities
						NamedEntity oldEntity = oldLibrary.getNamedMember( oldEntityChoice.getValue().value );
						NamedEntity newEntity = newLibrary.getNamedMember( newEntityChoice.getValue().value );
						
						if ((oldEntity == null) || (newEntity == null)) {
							throw new IllegalStateException("Selected entities are not accessible.");
						}
						
						if ((oldEntity instanceof TLResource) && (newEntity instanceof TLResource)) {
							try (OutputStream out = new FileOutputStream( reportFile )) {
								new ModelComparator( userSettings.getCompareOptions() )
										.compareResources( (TLResource) oldEntity, (TLResource) newEntity, out );
							}
							
						} else {
							try (OutputStream out = new FileOutputStream( reportFile )) {
								new ModelComparator( userSettings.getCompareOptions() )
										.compareEntities( oldEntity, newEntity, out );
							}
						}
					}
					showReport( reportFile );
					reportFile.deleteOnExit();
					
				} finally {
					closeAllProjects( oldProjectManager );
					closeAllProjects( newProjectManager );
				}
			}
		};
		
		new Thread( r ).start();
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	@Override
	protected void initialize(Stage primaryStage) {
		
		super.initialize( primaryStage );
		this.userSettings = UserSettings.load();
		
		oldEntityChoice.valueProperty().addListener( (observable, oldValue, newValue) -> updateControlStates() );
		newEntityChoice.valueProperty().addListener( (observable, oldValue, newValue) -> updateControlStates() );
		oldCommitChoice.valueProperty().addListener(
				(observable, oldValue, newValue) -> oldCommitSelectionChanged() );
		newCommitChoice.valueProperty().addListener(
				(observable, oldValue, newValue) -> newCommitSelectionChanged() );
		reportViewer.getEngine().getHistory().currentIndexProperty().addListener(
				(observable, oldValue, newValue) -> updateControlStates() );
		reportViewer.getEngine().getLoadWorker().stateProperty().addListener(
				(observable, oldState, newState) -> {
			        if (newState == Worker.State.SUCCEEDED) {
			            EventListener listener = new EventListener() {
			                public void handleEvent(Event ev) {
			                	String href = ((Element)ev.getTarget()).getAttribute("href");
			                	
			                	if ((href != null) && !href.startsWith("#")) {
				                	ev.preventDefault();
				                	navigateExternalLink( href );
			                	}
			                }
			            };

			            Document doc = reportViewer.getEngine().getDocument();
			            NodeList lista = doc.getElementsByTagName("a");
			            
			            for (int i=0; i<lista.getLength(); i++) {
			                ((EventTarget)lista.item(i)).addEventListener("click", listener, false);
			            }
			        }
				});
		updateControlStates();
	}
	
	/**
	 * Encapsulates a single selectable item that may be included in a choice box.
	 */
	private static class ChoiceItem {
		
		public String label;
		public String value;
		
		/**
		 * Constructor that creates an item with the same value for its label and value.
		 * 
		 * @param value  the choice value to assign
		 */
		public ChoiceItem(String value) {
			this.label = this.value = value;
		}
		
		/**
		 * Constructor that creates an item with different values for its label and value.
		 * 
		 * @param label  the display label for the item
		 * @param value  the choice value to assign
		 */
		public ChoiceItem(String label, String value) {
			this.label = label;
			this.value = value;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return label;
		}
		
	}
	
	/**
	 * Encapsulates a single selectable item for a library's commit date and comments.
	 */
	private static class CommitChoiceItem {
		
		private static final DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
		public String label;
		public RepositoryItemCommit value;
		
		/**
		 * Constructor that initializes the value and display label for the item.
		 * 
		 * @param commit  the commit record for a repository item
		 */
		public CommitChoiceItem(RepositoryItemCommit commit) {
			StringBuilder text = new StringBuilder();
			
			text.append( dateFormat.format( commit.getEffectiveOn() ) );
			text.append(" [").append( commit.getUser() ).append("]: ");
			text.append( commit.getRemarks() );
			this.value = commit;
			this.label = text.toString();
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
