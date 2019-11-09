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

package org.opentravel.dex.controllers.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Manage the stand-alone resource window.
 * 
 * @author dmh
 *
 */
public class ResourcesWindowController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( ResourcesWindowController.class );

    public static final String LAYOUT_FILE = "/ResourceViews/ResourcesWindow.fxml";

    private static String dialogTitle = "Resources";

    protected static Stage dialogStage;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    public static ResourcesWindowController init() {
        FXMLLoader loader = new FXMLLoader( ResourcesWindowController.class.getResource( LAYOUT_FILE ) );
        ResourcesWindowController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.NONE );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof ResourcesWindowController))
                throw new IllegalStateException( "Error creating resources window controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading resources window. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }
        return controller;
    }

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private ResourcesTreeTableController resourcesTreeTableController;
    @FXML
    private ResourceDetailsController resourceDetailsController;
    @FXML
    private ResourceActionsTreeTableController resourceActionsTreeTableController;
    @FXML
    private ResourceErrorsTreeTableController resourceErrorsTreeTableController;

    public ResourcesWindowController() {
        log.debug( "Resource Tab Controller constructed." );
    }

    @Override
    public void checkNodes() {
        if (!(resourcesTreeTableController instanceof ResourcesTreeTableController))
            throw new IllegalStateException( "Resource tree table controller not injected by FXML." );

        if (!(resourceDetailsController instanceof ResourceDetailsController))
            throw new IllegalStateException( "Resource child details controller not injected by FXML." );

        if (!(resourceActionsTreeTableController instanceof ResourceActionsTreeTableController))
            throw new IllegalStateException( "Resource Actions controller not injected by FXML." );

        if (!(resourceErrorsTreeTableController instanceof ResourceErrorsTreeTableController))
            throw new IllegalStateException( "Resource Errors controller not injected by FXML." );

    }

    @Override
    @FXML
    public void initialize() {
        // no-op
        checkNodes();
    }

    /**
     * @param primaryStage
     */
    // @Override
    public void configure(DexMainController parent) {
        if (parent == null) {
            log.debug( "Null main controller when configuring resources window" );
            return;
        }

        OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();
        parent.addIncludedController( resourcesTreeTableController, eventManager );
        parent.addIncludedController( resourceDetailsController, eventManager );
        parent.addIncludedController( resourceActionsTreeTableController, eventManager );
        parent.addIncludedController( resourceErrorsTreeTableController, eventManager );

        eventManager.configureEventHandlers();
        log.debug( "Repository window configured." );
    }

    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#setup(java.lang.String)
     */
    @Override
    public void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // log.debug( "MUST configure with DexMainController" );
    }
}
