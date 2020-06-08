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
import org.opentravel.dex.controllers.member.MemberDetailsController;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller for type selection dialog pop-up menu.
 * 
 * @author dmh
 *
 */
public class MemberDetailsPopupController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( MemberDetailsPopupController.class );

    public static final String LAYOUT_FILE = "/Dialogs/MemberDetailsPopup.fxml";

    private static String dialogTitle = "Member Details Dialog";
    private static String helpText = "Enter member details.";

    protected static Stage dialogStage;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    public static MemberDetailsPopupController init() {
        FXMLLoader loader = new FXMLLoader( MemberDetailsPopupController.class.getResource( LAYOUT_FILE ) );
        MemberDetailsPopupController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );
            dialogStage.getScene().getStylesheets().add( "DavesViper.css" );

            // get the controller from loader.
            controller = loader.getController();
            // controller.popupStage = dialogStage;

            if (!(controller instanceof MemberDetailsPopupController))
                throw new IllegalStateException( "Error creating member details dialog controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading dialog box. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }

        positionStage( dialogStage );
        return controller;
    }

    @FXML
    private Button cancelButton;
    @FXML
    private Button selectButton;
    @FXML
    private TextFlow dialogHelp;
    @FXML
    private MemberDetailsController memberDetailsController;

    private OtmLibraryMember currentMember;

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
        if (cancelButton == null || selectButton == null)
            throw new IllegalStateException( "Null FXML injected node." );
        if (memberDetailsController == null)
            throw new IllegalStateException( "Null FXML injected member details controller." );
    }

    @Override
    public void doOK() {
        memberDetailsController.commitChanges();
        super.doOK();
    }

    public void mouseClick(MouseEvent event) {
        // this fires after the member selection listener
        log.debug( "Double click selection" );
        if (event.getButton().equals( MouseButton.PRIMARY ) && event.getClickCount() == 2) {
            doOK();
        }
    }

    @Override
    public void refresh() {
        memberDetailsController.refresh();
    }

    public void setMember(OtmLibraryMember model) {
        this.currentMember = model;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        postHelp( helpText, dialogHelp );
        cancelButton.setOnAction( e -> doCancel() );

        selectButton.setText( "OK" );
        selectButton.setOnAction( e -> doOK() );
        //
        memberDetailsController.configure( null, 0 );
        memberDetailsController.post( currentMember );
    }

}
