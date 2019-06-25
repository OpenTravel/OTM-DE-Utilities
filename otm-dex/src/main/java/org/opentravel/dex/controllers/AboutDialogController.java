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

package org.opentravel.dex.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the About application dialog.
 */
public class AboutDialogController {

    public static final String FXML_FILE = "/rv-about-dialog.fxml";

    private Stage dialogStage;

    @FXML
    private Label buildNumberLabel;

    /**
     * Initializes the dialog stage and controller used to display the application- about page.
     * 
     * @param stage the stage that will own the new dialog
     * @return AboutDialogController
     */
    public static AboutDialogController createAboutDialog(Stage stage) {
        AboutDialogController controller = null;
        try {
            FXMLLoader loader = new FXMLLoader( AboutDialogController.class.getResource( FXML_FILE ) );
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            Scene scene = new Scene( page );

            dialogStage.setTitle( "About" );
            dialogStage.initModality( Modality.WINDOW_MODAL );
            dialogStage.initOwner( stage );
            dialogStage.setScene( scene );

            controller = loader.getController();
            controller.setDialogStage( dialogStage );

        } catch (IOException e) {
            e.printStackTrace( System.out );
        }
        return controller;
    }

    /**
     * Called when the user clicks the close button of the dialog.
     * 
     * @param event the action event that triggered this method call
     */
    @FXML
    public void closeSelected(ActionEvent event) {
        dialogStage.close();
    }

    /**
     * Assigns the stage for the dialog.
     *
     * @param dialogStage the dialog stage to assign
     */
    public void setDialogStage(Stage dialogStage) {
        buildNumberLabel.setText( "TODO - get BUILD_NUMBER via properties." );
        // buildNumberLabel.setText(MessageBuilder.formatMessage("BUILD_NUMBER"));
        this.dialogStage = dialogStage;
    }

    /**
     * @see javafx.stage.Stage#showAndWait()
     */
    public void showAndWait() {
        dialogStage.showAndWait();
    }

}
