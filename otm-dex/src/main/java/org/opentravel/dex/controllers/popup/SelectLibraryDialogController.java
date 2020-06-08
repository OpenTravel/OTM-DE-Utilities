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
import org.opentravel.dex.controllers.library.LibrariesTreeTableController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for creating a library selection dialog pop-up menu.
 * <p>
 * Create the controller using the static {@link SelectLibraryDialogController#init() } method. If the model manager is
 * set before showing, the project will be created.
 * 
 * @author dmh
 *
 */
public class SelectLibraryDialogController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( SelectLibraryDialogController.class );

    public static final String LAYOUT_FILE = "/Dialogs/SelectLibraryDialog.fxml";

    protected static Stage dialogStage;
    private static String helpText = "Select the library.";
    private static String dialogTitle = "Library Selection Dialog";

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @return dialog box controller or null
     */
    public static SelectLibraryDialogController init() {
        FXMLLoader loader = new FXMLLoader( SelectLibraryDialogController.class.getResource( LAYOUT_FILE ) );
        SelectLibraryDialogController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof SelectLibraryDialogController))
                throw new IllegalStateException( "Error creating controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        positionStage( dialogStage );
        return controller;
    }


    @FXML
    private LibrariesTreeTableController librariesTreeTableController;
    @FXML
    BorderPane selectLibraryDialog;
    @FXML
    Label dialogTitleLabel;
    @FXML
    TextFlow dialogHelp;
    @FXML
    Button dialogButtonCancel;
    @FXML
    Button dialogButtonOK;

    private OtmModelManager modelMgr;
    // private String resultText;
    // private UserSettings userSettings;

    public OtmLibrary getSelected() {
        return librariesTreeTableController != null ? librariesTreeTableController.getSelectedLibrary() : null;
    }

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );

        if (librariesTreeTableController == null || dialogTitleLabel == null || dialogHelp == null
            || dialogButtonCancel == null || dialogButtonOK == null)
            throw new IllegalStateException( "Missing injected field." );
    }

    @Override
    public void clear() {
        dialogHelp.getChildren().clear();
    }

    public void mouseClick(MouseEvent event) {
        // this fires after the member selection listener
        log.debug( "Double click selection" );
        if (event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2) {
            doOK();
        }
    }

    // /**
    // * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#doOK()
    // */
    // @Override
    // public void doOK() {
    //
    // if (getSelected() == null || !getSelected().isEditable()) {
    // // postResults( "No Editable library selected.");
    // super.doCancel();
    // }
    // log.debug( "Selected: " + getSelected() );
    // super.doOK(); // all OK - close window
    // }


    // /**
    // *
    // * @param manager used to create project
    // * @param initialProjectFolder used in user file selection dialog
    // */
    // public void configure(OtmModelManager manager, UserSettings settings) {
    // // TODO - the settings should be abstracted for Dex applications
    // this.modelMgr = manager;
    // this.userSettings = settings;
    // }

    public void setModelManager(OtmModelManager mgr) {
        this.modelMgr = mgr;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // checkNodes();
        librariesTreeTableController.configure( modelMgr, true );
        librariesTreeTableController.setOnMouseClicked( this::mouseClick );

        dialogButtonCancel.setOnAction( e -> doCancel() );
        dialogButtonOK.setOnAction( e -> doOK() );
        dialogButtonOK.setText( "Select" );
        postHelp( helpText, dialogHelp );

        librariesTreeTableController.post( modelMgr );
    }
}
