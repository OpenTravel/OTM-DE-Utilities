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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opentravel.application.common.ProgressMonitor;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.upversion.LibraryStatusOrchestrator.StatusAction;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for the promote/demote dialog used to change the state of
 * selected libraries.
 */
public class PromoteDemoteDialogController {
	
	public static final String FXML_FILE = "/promote-demote-dialog.fxml";
	
	@FXML HBox filterPanel;
	@FXML RadioButton promoteRadio;
	@FXML RadioButton demoteRadio;
	@FXML ChoiceBox<EnumWrapper<TLLibraryStatus>> fromStatusChoice;
	@FXML Label toStatusLabel;
	@FXML Label affectedCountLabel;
	@FXML Label totalCountLabel;
	@FXML ProgressIndicator progressInd;
	@FXML Button goCloseButton;
	@FXML Button cancelButton;
	
	private LibraryStatusOrchestrator orchestrator = new LibraryStatusOrchestrator();
	private boolean processingComplete = false;
	private Stage dialogStage;
	
	/**
	 * Initializes the dialog stage and controller used to display the promote/demote
	 * libraries dialog.
	 * 
	 * @param selectedLibraries  the list of selected libraries to be processed
	 * @param stage  the stage that will own the new dialog
	 * @return PromoteDemoteDialogController
	 */
	public static PromoteDemoteDialogController createDialog(List<RepositoryItem> selectedLibraries, Stage stage) {
		PromoteDemoteDialogController controller = null;
		try {
			FXMLLoader loader = new FXMLLoader( PromoteDemoteDialogController.class.getResource( FXML_FILE ) );
			Parent page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( "Promote/Demote Libraries" );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.setResizable( false );
			dialogStage.initOwner( stage );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.orchestrator.setRepositoryManager( RepositoryManager.getDefault() );
			controller.setSelectedLibraries( selectedLibraries );
			controller.setDialogStage( dialogStage );
			
		} catch (IOException | RepositoryException e) {
			e.printStackTrace( System.out );
		}
		return controller;
	}
	
	/**
	 * Assigns the list of selected libraries to be processed.
	 * 
	 * @param selectedLibraries  the list of selected libraries to be processed
	 */
	public void setSelectedLibraries(List<RepositoryItem> selectedLibraries) {
		orchestrator.setLibraryVersions( selectedLibraries );
		updateStatusChoices();
		fromStatusChanged();
	}
	
	/**
	 * Called when the user clicks the promote button of the dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void promoteSelected(ActionEvent event) {
		updateStatusChoices();
	}
	
	/**
	 * Called when the user clicks the demote button of the dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void demoteSelected(ActionEvent event) {
		updateStatusChoices();
	}
	
	/**
	 * Called when the user clicks the Go/Close button of the dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void goCloseSelected(ActionEvent event) {
		if (!processingComplete) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						ProgressMonitor monitor = new ProgressMonitor( progressInd );
						
						Platform.runLater( new Runnable() {
							public void run() {
								filterPanel.setDisable( true );
								goCloseButton.setDisable( true );
								cancelButton.setDisable( true );
								progressInd.setDisable( false );
							}
						});
						
						orchestrator
							.setProgressMonitor( monitor )
							.updateStatus();
						
						Platform.runLater( new Runnable() {
							public void run() {
								goCloseButton.setDisable( false );
								goCloseButton.setText( "Close" );
								processingComplete = true;
							}
						});
						
					} catch (Throwable t) {
						t.printStackTrace( System.out );
					}
				}
			};
			
			new Thread( r ).start();
			
		} else {
			cancelSelected( event );
		}
	}
	
	/**
	 * Called when the user clicks the cancel button of the dialog.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void cancelSelected(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * Assigns the stage for the dialog.
	 *
	 * @param dialogStage  the dialog stage to assign
	 */
	public void setDialogStage(Stage dialogStage) {
		ToggleGroup tGroup = new ToggleGroup();
		
		promoteRadio.setToggleGroup( tGroup );
		demoteRadio.setToggleGroup( tGroup );
		promoteRadio.selectedProperty().set( true );
		
		fromStatusChoice.getSelectionModel().selectedItemProperty().addListener(
				( observable, oldValue, newValue ) -> fromStatusChanged() );
		
		this.dialogStage = dialogStage;
	}
	
	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}
	
	/**
	 * Updates the contents of the status choice box based upon the selection
	 * of the promote/demote radio buttons.
	 */
	private void updateStatusChoices() {
		Platform.runLater( new Runnable() {
			public void run() {
				ObservableList<EnumWrapper<TLLibraryStatus>> statusChoices = FXCollections.observableArrayList();
				EnumWrapper<TLLibraryStatus> selectedStatus = fromStatusChoice.getValue();
				List<TLLibraryStatus> statusList = new ArrayList<>();
				
				if (promoteRadio.isSelected()) {
					orchestrator.setStatusAction( StatusAction.PROMOTE );
					statusList.addAll( Arrays.asList( TLLibraryStatus.DRAFT,
							TLLibraryStatus.UNDER_REVIEW, TLLibraryStatus.FINAL ) );
					
				} else {
					orchestrator.setStatusAction( StatusAction.DEMOTE );
					statusList.addAll( Arrays.asList( TLLibraryStatus.UNDER_REVIEW,
							TLLibraryStatus.FINAL, TLLibraryStatus.OBSOLETE ) );
				}
				statusList.forEach( status -> statusChoices.add( new EnumWrapper<TLLibraryStatus>( status ) ) );
				
				if ((selectedStatus == null) || !statusChoices.contains( selectedStatus )) {
					selectedStatus = statusChoices.get( 0 );
				}
				fromStatusChoice.setItems( statusChoices );
				
				if (selectedStatus != null) {
					fromStatusChoice.getSelectionModel().select( selectedStatus );
				}
			}
		});
	}
	
	/**
	 * Called when the user changes the selection of the 'fromStatus' choice box.
	 */
	private void fromStatusChanged() {
		Platform.runLater( new Runnable() {
			public void run() {
				EnumWrapper<TLLibraryStatus> selectedStatus = fromStatusChoice.getValue();
				TLLibraryStatus toStatus = orchestrator.setFromStatus( selectedStatus.getValue() ).getToStatus();
				
				if (toStatus != null) {
					toStatusLabel.setText( MessageBuilder.formatMessage( toStatus.toString() ) );
				} else {
					toStatusLabel.setText( "UNKNOWN" );
				}
				affectedCountLabel.setText( orchestrator.getAffectedLibraryCount() + "" );
				totalCountLabel.setText( orchestrator.getTotalLibraryCount() + "" );
			}
		});
	}
	
}
