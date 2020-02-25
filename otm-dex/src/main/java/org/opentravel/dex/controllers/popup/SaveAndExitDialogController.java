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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.DexFileHandler;
import org.opentravel.dex.action.manager.DexActionManager;
import org.opentravel.model.OtmModelManager;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog controller for saving libraries.
 * <p>
 * Create the controller using the static {@link SaveAndExitDialogController#init() } method. If the model manager's ->
 * action manager -> event queue is not empty before showing, the dialog will show giving the user the option to save
 * the libraries before continuing.
 * 
 * @author dmh
 *
 */
public class SaveAndExitDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( SaveAndExitDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/SaveAndExitDialog.fxml";

    protected static Stage dialogStage;
    private static String dialogTitle = "Save And Exit";

    @FXML
    private Label savePrompt;
    @FXML
    private Label changeNotice;
    @FXML
    private Button saveButton;
    @FXML
    private Button exitButton;
    @FXML
    private Button cancelButton;

    private OtmModelManager modelMgr;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static SaveAndExitDialogController init() {
        FXMLLoader loader = new FXMLLoader( SaveAndExitDialogController.class.getResource( LAYOUT_FILE ) );
        SaveAndExitDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            // dialogStage.initModality( Modality.APPLICATION_MODAL );
            dialogStage.initModality( Modality.NONE );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof SaveAndExitDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );

        return controller;
    }

    @Override
    public void checkNodes() {
        if (saveButton == null || exitButton == null || cancelButton == null || savePrompt == null
            || changeNotice == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        // dialogHelp.getChildren().clear();
    }



    @FXML
    public void doSave() {
        // log.debug( "Run save task" );
        DialogBoxContoller dialogBox = DialogBoxContoller.init();
        String results = DexFileHandler.saveLibraries( modelMgr.getEditableLibraries() );
        dialogBox.showAndWait( "Save Results", results );

        super.doOK();
    }

    @FXML
    public void doNoSave() {
        // log.debug( "Close without saving." );
        super.doOK();
    }

    @FXML
    public void doCancelExit() {
        // log.debug( "do Cancel." );
        super.doCancel();
    }



    /**
     * If there have been changes to the model, prompt the user to save them.
     * 
     * @param mgr
     */
    private void post(OtmModelManager mgr) {
        changeNotice.setVisible( false );
        savePrompt.setVisible( false );

        if (mgr == null)
            return;
        DexActionManager actionMgr = modelMgr.getActionManager( true );
        if (actionMgr == null)
            return;

        if (actionMgr.getQueueSize() > 0) {
            changeNotice.setVisible( true );
            savePrompt.setVisible( true );
        }
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        checkNodes();
    }

    /**
     * If set, the dialog will prompt the user to save their work before exiting.
     * 
     * @param manager
     */
    public void setModelManager(OtmModelManager manager) {
        this.modelMgr = manager;
        post( modelMgr );
    }

}
