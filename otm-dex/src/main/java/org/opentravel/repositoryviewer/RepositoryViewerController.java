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

package org.opentravel.repositoryviewer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexMainControllerBase;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.controllers.MenuBarWithProjectController;
import org.opentravel.dex.controllers.repository.NamespaceLibrariesTreeTableController;
import org.opentravel.dex.controllers.repository.RepositoryItemCommitHistoriesController;
import org.opentravel.dex.controllers.repository.RepositoryNamespacesTreeController;
import org.opentravel.dex.controllers.repository.RepositorySelectionController;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Manage the repository viewer. Includes controllers for the trees and tree-tables.
 * 
 * @author dmh
 *
 */
public class RepositoryViewerController extends DexMainControllerBase implements DexMainController {
    private static Log log = LogFactory.getLog( RepositoryViewerController.class );

    // Let FXML inject controllers
    @FXML
    private DexStatusController dexStatusController;
    @FXML
    private RepositoryNamespacesTreeController repositoryNamespacesTreeController;
    @FXML
    private NamespaceLibrariesTreeTableController namespaceLibrariesTreeTableController;
    @FXML
    private RepositoryItemCommitHistoriesController repositoryItemCommitHistoriesController;
    @FXML
    private RepositorySelectionController repositorySelectionController;
    @FXML
    private MenuBarWithProjectController menuBarWithProjectController;


    @Override
    public void checkNodes() {
        if (!(repositoryItemCommitHistoriesController instanceof RepositoryItemCommitHistoriesController))
            throw new IllegalStateException( "Commit Histories controller not injected by FXML." );
        if (!(namespaceLibrariesTreeTableController instanceof NamespaceLibrariesTreeTableController))
            throw new IllegalStateException( "Libraries tree table controller not injected by FXML." );
        if (!(repositoryNamespacesTreeController instanceof RepositoryNamespacesTreeController))
            throw new IllegalStateException( "repository namespaces controller not injected by FXML." );
        if (!(repositorySelectionController instanceof RepositorySelectionController))
            throw new IllegalStateException( "repository selection controller not injected by FXML." );
        if (!(dexStatusController instanceof DexStatusController))
            throw new IllegalStateException( "Status controller not injected by FXML." );
        if (!(menuBarWithProjectController instanceof MenuBarWithProjectController))
            throw new IllegalStateException( "Menu bar not injected by FXML." );

        log.debug( "FXML Nodes checked OK." );
    }

    public RepositoryViewerController() {
        log.debug( "Starting constructor." );
    }

    @Override
    @FXML
    public void initialize() {
        log.debug( "Repository Viewer Controller initialized." );

        // // Get user settings / preferences
        // UserSettings settings = UserSettings.load();
    }

    /**
     * @param primaryStage
     */
    @Override
    public void setStage(Stage primaryStage) {
        // These may be needed by sub-controllers
        DexMainControllerBase.stage = primaryStage;
        imageMgr = new ImageManager( primaryStage );
        checkNodes();

        // Hide the project combo
        addIncludedController( menuBarWithProjectController, eventManager, 0 );
        menuBarWithProjectController.showCombo( false );
        menuBarController = menuBarWithProjectController; // Make available to base class

        addIncludedController( dexStatusController, eventManager, 0 );
        statusController = dexStatusController; // make available to base class

        // Set up the repository selection
        addIncludedController( repositorySelectionController, eventManager, 0 );
        addIncludedController( repositoryNamespacesTreeController, eventManager, 0 );
        addIncludedController( namespaceLibrariesTreeTableController, eventManager, 0 );
        // No set up needed, but add to list
        addIncludedController( repositoryItemCommitHistoriesController, eventManager, 0 );

        eventManager.configureEventHandlers();
        setMainController( this );

        log.debug( "Stage set." );
    }

    protected void postRepoError(Exception e) {
        postError( e, "Error accessing repository." );
    }


}
