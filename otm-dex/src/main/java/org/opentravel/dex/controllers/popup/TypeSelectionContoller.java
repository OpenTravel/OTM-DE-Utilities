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
import org.opentravel.dex.controllers.member.MemberAndProvidersDAO;
import org.opentravel.dex.controllers.member.MemberFilterController;
import org.opentravel.dex.controllers.member.MemberTreeTableController;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.OtmTypeProvider;

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
public class TypeSelectionContoller extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( TypeSelectionContoller.class );

    public static final String LAYOUT_FILE = "/TypeSelectionDialog.fxml";

    private static String dialogTitle = "Type Selection Dialog";
    private static String helpText = "Select a type.";

    protected static Stage dialogStage;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    public static TypeSelectionContoller init() {
        FXMLLoader loader = new FXMLLoader( TypeSelectionContoller.class.getResource( LAYOUT_FILE ) );
        TypeSelectionContoller controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.APPLICATION_MODAL );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof TypeSelectionContoller))
                throw new IllegalStateException( "Error creating type selection dialog controller." );
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
    private MemberTreeTableController memberTreeTableController;
    @FXML
    private MemberFilterController memberFilterController;

    public MemberFilterController getMemberFilterController() {
        return memberFilterController;
    }

    private OtmModelManager modelManager;

    // private ImageManager imageMgr;

    @Override
    public void checkNodes() {
        if (dialogStage == null)
            throw new IllegalStateException( "Missing stage." );
        if (cancelButton == null || selectButton == null)
            throw new IllegalStateException( "Null FXML injected node." );
    }

    public MemberAndProvidersDAO getSelected() {
        return memberTreeTableController.getSelected();
    }

    /**
     * @return selection if type provider, otherwise null
     */
    public OtmTypeProvider getSelectedProvider() {
        if (memberTreeTableController.getSelected() != null
            && memberTreeTableController.getSelected().getValue() != null
            && memberTreeTableController.getSelected().getValue() instanceof OtmTypeProvider)
            return (OtmTypeProvider) memberTreeTableController.getSelected().getValue();
        return null;
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
        memberTreeTableController.refresh();
    }

    public void setManager(OtmModelManager model) {
        this.modelManager = model;
    }

    @Override
    protected void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        postHelp( helpText, dialogHelp );
        cancelButton.setOnAction( e -> doCancel() );
        selectButton.setOnAction( e -> doOK() );

        memberTreeTableController.configure( modelManager, false );
        memberFilterController.configure( modelManager, this );
        memberFilterController.setBuiltIns( true ); // always start with built-ins showing
        memberTreeTableController.setFilter( memberFilterController );

        memberTreeTableController.post( modelManager );
        memberTreeTableController.setOnMouseClicked( this::mouseClick );

    }

}
