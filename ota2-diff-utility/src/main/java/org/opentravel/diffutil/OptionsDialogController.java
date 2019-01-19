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

import org.opentravel.schemacompiler.diff.ModelCompareOptions;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;

/**
 * Controller used to select the option settings to be used when comparing OTM models,
 * libraries, and entities.
 */
public class OptionsDialogController {
	
	public static final String FXML_FILE = "/options.fxml";
	
	@FXML private CheckBox suppressFieldVersionChangesCB;
	@FXML private CheckBox suppressLibraryPropertyChangesCB;
	@FXML private CheckBox suppressDocumentationChangesCB;
	
	private Stage dialogStage;
	private ModelCompareOptions compareOptions;
	private boolean okSelected = false;
	
	/**
	 * Called when the user clicks the Ok button to confirm their updates.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectOk(ActionEvent event) {
		compareOptions.setSuppressFieldVersionChanges( suppressFieldVersionChangesCB.isSelected() );
		compareOptions.setSuppressLibraryPropertyChanges( suppressLibraryPropertyChangesCB.isSelected() );
		compareOptions.setSuppressDocumentationChanges( suppressDocumentationChangesCB.isSelected() );
		dialogStage.close();
		okSelected = true;
	}
	
	/**
	 * Called when the user clicks the Ok button to cancel their updates.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectCancel(ActionEvent event) {
		dialogStage.close();
	}
	
	/**
	 * Assigns the stage for the dialog.
	 *
	 * @param dialogStage  the dialog stage to assign
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	/**
	 * Assigns the <code>ModelCompareOptions</code> instance that will be updated
	 * by this dialog.
	 *
	 * @param compareOptions  the options instance to assign
	 */
	public void setCompareOptions(ModelCompareOptions compareOptions) {
		this.compareOptions = compareOptions;
		suppressFieldVersionChangesCB.setSelected( compareOptions.isSuppressFieldVersionChanges() );
		suppressLibraryPropertyChangesCB.setSelected( compareOptions.isSuppressLibraryPropertyChanges() );
		suppressDocumentationChangesCB.setSelected( compareOptions.isSuppressDocumentationChanges() );
	}
	
	/**
	 * Returns true if the user clicked the Ok button to close the dialog.
	 *
	 * @return boolean
	 */
	public boolean isOkSelected() {
		return okSelected;
	}

	/**
	 * @see javafx.stage.Stage#showAndWait()
	 */
	public void showAndWait() {
		dialogStage.showAndWait();
	}

}
