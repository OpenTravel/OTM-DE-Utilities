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

package org.opentravel.modelcheck;

import java.util.Arrays;
import java.util.List;

import org.opentravel.application.common.AbstractMainWindowController;
import org.opentravel.application.common.StatusType;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the Model Check application.
 */
public class ModelCheckController extends AbstractMainWindowController {
	
	public static final String FXML_FILE = "/ota2-model-check.fxml";

	@FXML private TextField filenameText;
	@FXML private Button localFileButton;
	@FXML private Button managedReleaseButton;
	@FXML private Button optionsButton;
	@FXML private Button navBackButton;
	@FXML private Button navForwardButton;
	@FXML private Button saveReportButton;
	@FXML private WebView reportViewer;
	@FXML private ImageView statusBarIcon;
	@FXML private Label statusBarLabel;
	
	private UserSettings userSettings;
	
	/**
	 * Called when the user clicks the button to select a release or project file from
	 * the local file system.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectLocalFile(ActionEvent event) {
		System.out.println("selectLocalFile()");
	}
	
	/**
	 * Called when the user clicks the button to select a managed release from a remote
	 * repository.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectManagedRelease(ActionEvent event) {
		System.out.println("selectManagedRelease()");
	}
	
	/**
	 * Called when the user clicks the button to edit the model check options.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void editOptions(ActionEvent event) {
		System.out.println("editOptions()");
	}
	
	/**
	 * Called when the user clicks the button to navigate backwards in the web-view
	 * history.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void navigateBack(ActionEvent event) {
		System.out.println("navigateBack()");
	}
	
	/**
	 * Called when the user clicks the button to navigate forward in the web-view
	 * history.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void navigateForward(ActionEvent event) {
		System.out.println("navigateForward()");
	}
	
	/**
	 * Called when the user clicks the button to save the current HTML report to
	 * the local file system.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveReport(ActionEvent event) {
		System.out.println("saveReport()");
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
				List<Node> controls = Arrays.asList( filenameText, localFileButton,
						managedReleaseButton, optionsButton, navBackButton, navForwardButton,
						reportViewer );
				
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
			String reportLocation = reportViewer.getEngine().getLocation();
			int historyIdx = reportViewer.getEngine().getHistory().getCurrentIndex();
			int historySize = reportViewer.getEngine().getHistory().getEntries().size();
			boolean reportDisplayed = (reportLocation != null) && reportLocation.startsWith( "file:" );
			boolean canBrowseBack = historyIdx > 0;
			boolean canBrowseForward = historyIdx < (historySize - 1);
			
			saveReportButton.disableProperty().set( !reportDisplayed );
			navBackButton.disableProperty().set( !canBrowseBack );
			navForwardButton.disableProperty().set( !canBrowseForward );
		} );
	}

	/**
	 * @see org.opentravel.application.common.AbstractMainWindowController#initialize(javafx.stage.Stage)
	 */
	@Override
	protected void initialize(Stage primaryStage) {
		super.initialize( primaryStage );
		this.userSettings = UserSettings.load();
		updateControlStates();
	}
	
}
