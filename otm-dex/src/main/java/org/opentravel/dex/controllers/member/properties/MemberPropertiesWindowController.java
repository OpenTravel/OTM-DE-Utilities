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

package org.opentravel.dex.controllers.member.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.member.MemberDetailsController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Manage the stand-alone resource window.
 * 
 * @author dmh
 *
 */
public class MemberPropertiesWindowController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( MemberPropertiesWindowController.class );

    public static final String LAYOUT_FILE = "/MemberViews/MemberPropertiesWindow.fxml";
    protected static String dialogTitle = "Member Properties";
    protected static Stage dialogStage;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    protected static MemberPropertiesWindowController init() {
        FXMLLoader loader = new FXMLLoader( MemberPropertiesWindowController.class.getResource( LAYOUT_FILE ) );
        MemberPropertiesWindowController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.NONE );
            dialogStage.getScene().getStylesheets().add( "DavesViper.css" );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof MemberPropertiesWindowController))
                throw new IllegalStateException( "Error creating resources window controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading search window. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        // FUTURE - could configure here since the controllers are instantiated
        positionStage( dialogStage );
        return controller;
    }

    /**
     * ********************************************************* FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private MemberDetailsController memberDetailsController;
    @FXML
    private MemberPropertiesTreeTableController memberPropertiesTreeTableController;


    public MemberPropertiesWindowController() {
        log.debug( "Member Properties Window Controller constructed." );
    }

    @Override
    public void checkNodes() {
        if (!(memberDetailsController instanceof MemberDetailsController))
            throw new IllegalStateException( "Member details controller not injected by FXML." );

        if (!(memberPropertiesTreeTableController instanceof MemberPropertiesTreeTableController))
            throw new IllegalStateException( "Member properties controller not injected by FXML." );
    }

    public void configure(DexMainController parent, MenuItem menuItem) {
        configure( parent );
        launchedFromMenuItem = menuItem; // Remember so it can be enabled on close
    }

    public void configure(DexMainController parent) {
        if (parent == null) {
            log.debug( "Null main controller when configuring resources window" );
            return;
        }
        OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();
        parent.addIncludedController( memberDetailsController, eventManager );
        parent.addIncludedController( memberPropertiesTreeTableController, eventManager );

        eventManager.configureEventHandlers();
        log.debug( "Member Details window configured." );
    }

    @Override
    @FXML
    public void initialize() {
        checkNodes();
    }

    @Override
    public void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
    }
}
