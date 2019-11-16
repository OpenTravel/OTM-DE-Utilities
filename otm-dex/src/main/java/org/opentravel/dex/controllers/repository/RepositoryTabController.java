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

import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabControllerBase;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Manage the repository tab.
 * 
 * @author dmh
 *
 */
public class RepositoryTabController extends DexTabControllerBase {

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
    @FXML
    private RepositoryItemWebViewController repositoryItemWebViewController;

    public RepositoryTabController() {
        // no-op
    }

    /**
     * @param primaryStage
     */
    @Override
    public void configure(DexMainController mc) {
        includedControllers.add( repositorySelectionController );
        includedControllers.add( repositoryNamespacesTreeController );
        includedControllers.add( namespaceLibrariesTreeTableController );
        includedControllers.add( repositoryItemCommitHistoriesController );
        includedControllers.add( repositoryItemWebViewController );
        super.configure( mc );
    }

    @Override
    public String getDialogTitle() {
        return RepositoryWindowController.DIALOG_TITLE;
    }

    public void launchWindow(ActionEvent e) {
        RepositoryWindowController w = RepositoryWindowController.init();
        super.launchWindow( e, w );
    }

}
