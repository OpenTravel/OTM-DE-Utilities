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

package org.opentravel.exampleupgrade;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the Legend dialog.
 */
public class LegendController {
	
	public static final String FXML_FILE = "/legend.fxml";
	
	@FXML private WebView webView;
	
	private Stage dialogStage;
	
	/**
	 * Called when the user clicks the close button.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleCloseButton(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}
	
	/**
	 * Initializes the dialog by assigning the HTML page to be displayed
	 * in the web view.
	 * 
	 * @param dialogStage  the stage used to display the dialog
	 * @param legendUrl  the URL of the HTML legend document
	 */
	public void initialize(Stage dialogStage, String legendUrl) {
		this.dialogStage = dialogStage;
		webView.getEngine().load( legendUrl );
	}
	
}
