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

package org.opentravel.dex.controllers.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.popup.DexPopupControllerBase;
import org.opentravel.dex.repository.RepositorySearchController;
import org.opentravel.dex.repository.RepositorySelectionController;

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
public class SearchWindowController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( SearchWindowController.class );

    public static final String LAYOUT_FILE = "/SearchViews/SearchWindow.fxml";

    private static String dialogTitle = "Search";

    protected static Stage dialogStage;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    protected static SearchWindowController init() {
        FXMLLoader loader = new FXMLLoader( SearchWindowController.class.getResource( LAYOUT_FILE ) );
        SearchWindowController controller = null;
        try {
            // Load the fxml file initialize controller it declares.
            Pane pane = loader.load();
            // Create scene and stage
            dialogStage = new Stage();
            dialogStage.setScene( new Scene( pane ) );
            dialogStage.initModality( Modality.NONE );

            // get the controller from loader.
            controller = loader.getController();
            if (!(controller instanceof SearchWindowController))
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
    private RepositorySearchController repositorySearchController;
    @FXML
    private RepositorySelectionController repositorySelectionController;


    public SearchWindowController() {
        log.debug( "Search Window Controller constructed." );
    }

    @Override
    public void checkNodes() {
        if (!(repositorySearchController instanceof RepositorySearchController))
            throw new IllegalStateException( "Search controller not injected by FXML." );

        if (!(repositorySelectionController instanceof RepositorySelectionController))
            throw new IllegalStateException( "Selection controller not injected by FXML." );

        // FUTURE - could configure here since the controllers are instantiated
    }

    @Override
    @FXML
    public void initialize() {
        // no-op
        checkNodes();
    }

    // /**
    // * @param main controller needed to register controllers for event passing
    // */
    // // @Override
    // public void configure(DexMainController parent) {
    // if (parent == null) {
    // log.debug( "Null main controller when configuring resources window" );
    // return;
    // }
    //
    // OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();
    // parent.addIncludedController( repositorySearchController, eventManager );
    // parent.addIncludedController( repositorySelectionController, eventManager );
    //
    // // FIXME - Only do for dialog window, not tab
    // eventManager.configureEventHandlers();
    // log.debug( "Search window configured." );
    // }


    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#setup(java.lang.String)
     */
    @Override
    public void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
        // log.debug( "MUST configure with DexMainController" );
    }
}
