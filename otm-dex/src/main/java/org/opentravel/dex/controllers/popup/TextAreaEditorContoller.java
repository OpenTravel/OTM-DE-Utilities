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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.objecteditor.UserSettings;

import java.io.IOException;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for MODAL dialog box pop-up menu for editing a block of text in a FXML TextArea.
 * <p>
 * 
 * @author dmh
 *
 */
public class TextAreaEditorContoller extends DexPopupControllerBase {
    private static Logger log = LogManager.getLogger( TextAreaEditorContoller.class );

    public static final String LAYOUT_FILE = "/Dialogs/TextAreaEditor.fxml";

    @FXML
    BorderPane dialogBox;
    @FXML
    TextArea editingArea;
    @FXML
    TextFlow dialogTitle;
    @FXML
    Button dialogButtonClose;
    @FXML
    Button dialogButtonOK;
    @FXML
    Label dialogTitleLabel;
    @FXML
    CheckBox hideDialog;

    private UserSettings userSettings;

    private StringProperty property;

    private static Stage dialogStage;

    /**
     * Is run when the associated .fxml file is loaded.
     */
    @Override
    @FXML
    public void initialize() {
        // log.debug( "Initialize injection point." );
    }

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
        if (dialogButtonClose == null)
            throw new IllegalStateException( "Missing close." );
        if (dialogTitle == null)
            throw new IllegalStateException( "Missing title." );
        if (editingArea == null)
            throw new IllegalStateException( "Missing dialog text." );
    }

    /**
     * Initialize this MODAL controller using FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     * @param loader FXML loaded for DialogBox.fxml
     * @param mainController
     * @return dialog box controller or null
     */
    public static TextAreaEditorContoller init() {
        FXMLLoader loader = new FXMLLoader( TextAreaEditorContoller.class.getResource( LAYOUT_FILE ) );
        TextAreaEditorContoller controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from it.
            controller = loader.getController();
            if (!(controller instanceof TextAreaEditorContoller)) {
                throw new IllegalStateException( "Error creating dialog box controller." );
            }
        } catch (IOException e1) {
            log.error( "Error loading dialog box: " + e1.getLocalizedMessage() );
            throw new IllegalStateException( "Error creating dialog box controller." );
        }

        positionStage( dialogStage );
        return controller;
    }

    @Override
    protected void setup(String message) {
        super.setStage( getTitle(), dialogStage );
    }

    /**
     * Show the title and message in a pop-up dialog window.
     * 
     * @param title
     * @param message
     */
    public void show(String title, String message) {
        setTitle( title );
        show( message );
    }

    @Override
    public void show(String message) {
        super.show( message );

        dialogButtonClose.setOnAction( e -> close() );
        if (userSettings != null) {
            hideDialog.setOnAction( this::setHideDialog );
            hideDialog.setSelected( userSettings.getHideOpenProjectDialog() );
        } else {
            dialogTitleLabel.setVisible( false );
            hideDialog.setVisible( false );
        }

        if (dialogButtonOK != null) {
            dialogButtonOK.setVisible( true );
            dialogButtonOK.setDisable( true ); // Wait for changes
        }

        dialogTitle.getChildren().clear();
        dialogTitle.getChildren().add( new Text( title ) );

        editingArea.setText( message );
        editingArea.setEditable( true );
    }

    /**
     * Show the title and message in a pop-up dialog window.
     * 
     * @param title
     * @param message
     */
    public void showAndWait(String title, String message) {
        setTitle( title );
        showAndWait( message );
    }

    public String getText() {
        return editingArea.getText();
    }

    /**
     * Show text area dialog with OK and Close buttons. Wait for an answer. If OK and the text has changed, save the
     * text into the property.
     */
    public Results showAndWait(StringProperty property) {
        this.property = property;
        return showAndWait( property.get() );
    }

    @Override
    public void doOK() {
        if (property != null)
            property.set( editingArea.getText() );
        super.doOK();
    }

    @Override
    public Results showAndWait(String text) {

        dialogButtonClose.setOnAction( e -> close() );

        if (dialogButtonOK != null) {
            dialogButtonOK.setVisible( true );
            dialogButtonOK.setOnAction( e -> doOK() );
        }

        dialogTitle.getChildren().clear();
        dialogTitle.getChildren().add( new Text( title ) );

        editingArea.setText( text );
        editingArea.setEditable( true );
        editingArea.setWrapText( true );

        return super.showAndWait( text );
    }

    /**
     * Add the message to the displayed text
     * 
     * @param message
     */
    public void add(String message) {
        if (editingArea != null)
            editingArea.setText( message );
    }

    public void setHideDialog(ActionEvent e) {
        if (userSettings != null)
            userSettings.setHideOpenProjectDialog( hideDialog.isSelected() );
    }

    public void close() {
        super.doCancel();
        // clear();
        // dialogStage.close();
    }

    @Override
    public void clear() {
        dialogTitle.getChildren().clear();
    }

    /**
     * @param userSettings
     */
    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

}
