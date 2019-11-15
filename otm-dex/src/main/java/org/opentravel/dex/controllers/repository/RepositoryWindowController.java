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

package org.opentravel.dex.controllers.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
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
 * Manage the stand-alone repository window.
 * 
 * @author dmh
 *
 */
public class RepositoryWindowController extends DexPopupControllerBase {
    private static Log log = LogFactory.getLog( RepositoryWindowController.class );

    public static final String LAYOUT_FILE = "/RepositoryViews/RepositoryWindow.fxml";
    protected static String dialogTitle = "Repository";
    protected static Stage dialogStage;

    @FXML
    private RepositoryNamespacesTreeController repositoryNamespacesTreeController;
    @FXML
    private NamespaceLibrariesTreeTableController namespaceLibrariesTreeTableController;
    @FXML
    private RepositoryItemCommitHistoriesController repositoryItemCommitHistoriesController;
    @FXML
    private RepositorySelectionController repositorySelectionController;
    @FXML
    private RepositoryItemWebViewController repositoryItemWebViewController;

    /**
     * Initialize this controller using the passed FXML loader.
     * <p>
     * Note: This approach using a static stage and main controller hides the complexity from calling controller.
     * Otherwise, this code must migrate into the calling controller.
     * 
     */
    protected static RepositoryWindowController init() {
        FXMLLoader loader = new FXMLLoader( RepositoryWindowController.class.getResource( LAYOUT_FILE ) );
        RepositoryWindowController controller = null;
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
            if (!(controller instanceof RepositoryWindowController))
                throw new IllegalStateException( "Error creating resources window controller." );
        } catch (IOException e1) {
            throw new IllegalStateException(
                "Error loading search window. " + e1.getLocalizedMessage() + "\n" + e1.getCause().toString() );
        }

        positionStage( dialogStage );
        return controller;
    }


    public RepositoryWindowController() {
        log.debug( "Repository Window Controller constructed." );
    }

    @Override
    public void checkNodes() {}

    public void configure(DexMainController parent, MenuItem menuItem) {
        configure( parent );
        launchedFromMenuItem = menuItem; // Remember so it can be enabled on close
    }

    public void configure(DexMainController mc) {
        if (mc == null) {
            log.debug( "Null main controller when configuring resources window" );
            return;
        }
        OtmEventSubscriptionManager eventManager = mc.getEventSubscriptionManager();
        // Set up the repository selection
        mc.addIncludedController( repositorySelectionController, eventManager );

        // Set up repository namespaces tree
        mc.addIncludedController( repositoryNamespacesTreeController, eventManager );

        // Set up the libraries in a namespace table
        mc.addIncludedController( namespaceLibrariesTreeTableController, eventManager );

        // No set up needed, but add to list
        mc.addIncludedController( repositoryItemCommitHistoriesController, eventManager );

        mc.addIncludedController( repositoryItemWebViewController, eventManager );

        eventManager.configureEventHandlers();
        log.debug( "Member Details window configured." );
    }


    @Override
    @FXML
    public void initialize() {
        // no-op
        // checkNodes();
    }


    /**
     * @see org.opentravel.dex.controllers.popup.DexPopupControllerBase#setup(java.lang.String)
     */
    @Override
    public void setup(String message) {
        super.setStage( dialogTitle, dialogStage );
    }
}
