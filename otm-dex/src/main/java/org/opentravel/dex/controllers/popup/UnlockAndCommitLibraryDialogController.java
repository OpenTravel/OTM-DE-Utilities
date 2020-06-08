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
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for unlock and commit library dialog box pop-up menu.
 * <p>
 * This controller replaces the unlockLibraryDialogController
 * 
 * @author dmh
 *
 */
public class UnlockAndCommitLibraryDialogController extends DexPopupControllerBase {
    // private static Log log = LogFactory.getLog( UnlockAndCommitLibraryDialogController.class );

    public enum TaskRequested {
        Cancel, UnlockOnly, CommitOnly, CommitAndUnlock
    }

    private TaskRequested task = TaskRequested.Cancel;

    public TaskRequested getTask() {
        return task;
    }

    public static final String LAYOUT_FILE = "/Dialogs/UnlockAndCommitDialog.fxml";

    @FXML
    BorderPane dialogBox;
    @FXML
    Label dialogTitleLabel;
    @FXML
    TextFlow dialogHelp;
    @FXML
    TextArea dialogText;
    @FXML
    Button dialogButtonCancel;
    @FXML
    Button dialogButtonOK;
    @FXML
    Button unlockOnlyButton;
    @FXML
    Button commitOnlyButton;

    protected static Stage dialogStage;

    private static String helpText =
        "Commit all changes and unlock the library in the repository using the current credentials. "
            + "\n\n'Unlock Only' will only unlock the library. All changes in the library's Work-In-Progress will be discarded."
            + "\n'Commit Only' will only commit changes with the remarks. The library will remain locked.";
    private static String dialogTitle = "Commit and Unlock Dialog";

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );

        if (dialogBox == null || dialogTitleLabel == null || dialogHelp == null || dialogText == null
            || unlockOnlyButton == null || dialogButtonCancel == null || dialogButtonOK == null)
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
    public static UnlockAndCommitLibraryDialogController init() {
        FXMLLoader loader = new FXMLLoader( UnlockAndCommitLibraryDialogController.class.getResource( LAYOUT_FILE ) );
        UnlockAndCommitLibraryDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof UnlockAndCommitLibraryDialogController))
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
        // checkNodes();

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );

        unlockOnlyButton.setOnAction( e -> doUnlockOnly() );
        commitOnlyButton.setOnAction( e -> doCommitOnly() );

        postHelp( helpText, dialogHelp );
        dialogText.setText( message );

        setButtons();
        dialogText.setOnKeyTyped( e -> setButtons() );
    }

    private void setButtons() {
        boolean notReady = false;
        if (dialogText.getText() == null || dialogText.getText().isEmpty())
            notReady = true;
        dialogButtonOK.setDisable( notReady );
        commitOnlyButton.setDisable( notReady );
    }

    public void doCancel() {
        task = TaskRequested.Cancel;
        super.doCancel();
    }

    public void doOK() {
        task = TaskRequested.CommitAndUnlock;
        super.doOK();
    }

    public void doUnlockOnly() {
        task = TaskRequested.UnlockOnly;
        super.doOK();
    }

    public void doCommitOnly() {
        task = TaskRequested.CommitOnly;
        super.doOK();
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

    public String getCommitRemarks() {
        return dialogText.getText();
    }

    /**
     * Use getTask() to know which button was used.
     * 
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#getResult()
     */
    @Override
    public Results getResult() {
        return result;
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }
}
