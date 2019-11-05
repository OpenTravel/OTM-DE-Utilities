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

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for unlock library dialog box pop-up menu.
 * 
 * @author dmh
 *
 */
public class UnlockLibraryDialogContoller extends DexPopupControllerBase {
    // private static Log log = LogFactory.getLog( UnlockLibraryDialogContoller.class );

    public static final String LAYOUT_FILE = "/UnlockLibraryDialog.fxml";

    @FXML
    BorderPane dialogBox;
    @FXML
    Label dialogTitleLabel;
    @FXML
    TextFlow dialogHelp;
    @FXML
    TextArea dialogText;
    @FXML
    RadioButton ulCommitButton;
    @FXML
    Button dialogButtonCancel;
    @FXML
    Button dialogButtonOK;

    protected static Stage dialogStage;

    private static String helpText = "Unlock in the repository using the current credentials. "
        + "If the 'commit' is selected, the Work-In-Process will be "
        + "committed with the remarks to the remote repository before the existing lock is released. "
        + "If not selected, any changes in the library's Work-In-Progress will be discarded.";
    private static String dialogTitle = "Unlock Dialog";

    @Override
    public void checkNodes() {
        if (dialogBox == null || dialogTitleLabel == null || dialogHelp == null || dialogText == null
            || ulCommitButton == null || dialogButtonCancel == null || dialogButtonOK == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @param loader FXML loaded for DialogBox.fxml
     * @param mainController
     * @return dialog box controller or null
     */
    public static UnlockLibraryDialogContoller init() {
        FXMLLoader loader = new FXMLLoader( UnlockLibraryDialogContoller.class.getResource( LAYOUT_FILE ) );
        UnlockLibraryDialogContoller controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof UnlockLibraryDialogContoller))
                throw new IllegalStateException( "Error creating unlock dialog controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );
        return controller;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        ulCommitButton.setOnAction( e -> doCommitButton() );

        postHelp( helpText, dialogHelp );

        dialogText.setText( message );
    }

    /**
     * Add the message to the displayed text
     * 
     * @param message
     */
    public void add(String message) {
        if (dialogText != null)
            dialogText.setText( message );
    }

    public void doCommitButton() {
        // Hide the text if not selected
        dialogText.setVisible( ulCommitButton.isSelected() );
    }

    public boolean getCommitState() {
        return ulCommitButton.isSelected();
    }

    public String getCommitRemarks() {
        return dialogText.getText();
    }

    @Override
    public Results getResult() {
        return result;
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }
}
