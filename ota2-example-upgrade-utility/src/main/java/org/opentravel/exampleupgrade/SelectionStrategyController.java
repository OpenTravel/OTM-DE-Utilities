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

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

/**
 * Controller for the Selection Strategy dialog box.
 */
public class SelectionStrategyController {

    public static final String FXML_FILE = "/selection-strategy.fxml";

    @FXML
    private RadioButton baseFamilyRadio;
    @FXML
    private RadioButton exampleRadio;
    @FXML
    private RadioButton userRadio;
    @FXML
    private ChoiceBox<String> namespaceChoice;
    @FXML
    private Button okButton;

    private Stage dialogStage;
    private boolean okSelected = false;

    /**
     * Called when the user clicks the Ok button to confirm their updates.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void selectOk(ActionEvent event) {
        dialogStage.close();
        okSelected = true;
    }

    /**
     * Called when the user clicks the Ok button to cancel their updates.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void selectCancel(ActionEvent event) {
        dialogStage.close();
    }

    /**
     * Called when the user changes the selection strategy radio button choice.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void strategySelectionChanged(ActionEvent event) {
        namespaceChoice.disableProperty().set( !userRadio.isSelected() );
    }

    /**
     * Assigns the stage for the dialog.
     *
     * @param dialogStage the dialog stage to assign
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
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

    /**
     * Initializes the contents of the dialog using the information provided.
     * 
     * @param initialStrategy the initial selection strategy to use for configuring the visual controls
     * @param userNamespaces the list of namespaces from which the user can select
     */
    public void initialize(SelectionStrategy initialStrategy, List<String> userNamespaces) {
        Platform.runLater( () -> {
            String selectedNS = initialStrategy.getUserNamespace();

            if ((selectedNS == null) && !userNamespaces.isEmpty()) {
                selectedNS = userNamespaces.get( 0 );
            }

            switch (initialStrategy.getStrategyType()) {
                case BASE_FAMILY:
                    baseFamilyRadio.setSelected( true );
                    break;
                case EXAMPLE_NAMESPACE:
                    exampleRadio.setSelected( true );
                    break;
                case USER_NAMESPACE:
                    userRadio.setSelected( true );
                    break;
                default:
                    // No default action required
            }
            namespaceChoice.setItems( FXCollections.observableArrayList( userNamespaces ) );

            if (selectedNS != null) {
                namespaceChoice.setValue( selectedNS );
            }
            strategySelectionChanged( null );
        } );
    }

    /**
     * In the event of a successful (non-cancel) close of the dialog, this method will return the updated selection
     * strategy.
     * 
     * @return SelectionStrategy
     */
    public SelectionStrategy getStrategy() {
        SelectionStrategy.Type strategyType;

        if (baseFamilyRadio.isSelected()) {
            strategyType = SelectionStrategy.Type.BASE_FAMILY;

        } else if (exampleRadio.isSelected()) {
            strategyType = SelectionStrategy.Type.EXAMPLE_NAMESPACE;

        } else {
            strategyType = SelectionStrategy.Type.USER_NAMESPACE;
        }
        return new SelectionStrategy( strategyType, namespaceChoice.getValue() );
    }

}
