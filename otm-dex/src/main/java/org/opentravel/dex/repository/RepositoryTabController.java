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

package org.opentravel.dex.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabController;

import javafx.fxml.FXML;

/**
 * Manage the repository tab.
 * 
 * @author dmh
 *
 */
public class RepositoryTabController implements DexTabController {
    private static Log log = LogFactory.getLog( RepositoryTabController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private RepositoryNamespacesTreeController repositoryNamespacesTreeController;
    @FXML
    private NamespaceLibrariesTreeTableController namespaceLibrariesTreeTableController;
    @FXML
    private RepositoryItemCommitHistoriesController repositoryItemCommitHistoriesController;
    @FXML
    private RepositorySelectionController repositorySelectionController;

    public RepositoryTabController() {
        log.debug( "Repository Tab Controller constructed." );
    }

    @Override
    @FXML
    public void initialize() {
        // no-op
    }

    /**
     * @param primaryStage
     */
    @Override
    public void configure(DexMainController parent) {

        // Set up the repository selection
        parent.addIncludedController( repositorySelectionController );

        // Set up repository namespaces tree
        parent.addIncludedController( repositoryNamespacesTreeController );

        // Set up the libraries in a namespace table
        parent.addIncludedController( namespaceLibrariesTreeTableController );

        // No set up needed, but add to list
        parent.addIncludedController( repositoryItemCommitHistoriesController );

        log.debug( "Repository Tab Stage set." );
    }
}
