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
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.DexTabController;
import org.opentravel.dex.repository.RepositorySelectionController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

/**
 * Manage the properties tab. Just register the included controllers.
 * 
 * @author dmh
 *
 */
public class SearchTabController implements DexTabController {
    private static Log log = LogFactory.getLog( SearchTabController.class );
    private static String dialogTitle = "Search";

    /**
     * ********************************************************* FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private RepositorySelectionController repositorySelectionController;
    @FXML
    private SearchQueryController searchQueryController;
    @FXML
    private SearchResultsController searchResultsController;


    // Available but not used
    @FXML
    private Tab searchTab;
    // @FXML
    // private VBox whereUsedTabVbox;

    public SearchTabController() {
        log.debug( "Search Tab Controller constructed." );
    }

    @Override
    public void checkNodes() {
        if (!(repositorySelectionController instanceof RepositorySelectionController))
            throw new IllegalStateException( "Selection controller not injected by FXML." );
    }

    @FXML
    @Override
    public void initialize() {
        // searchTab.setText( dialogTitle );
    }

    @Override
    public void configure(DexMainController parent) {
        OtmEventSubscriptionManager eventManager = parent.getEventSubscriptionManager();
        parent.addIncludedController( repositorySelectionController, eventManager );
        parent.addIncludedController( searchQueryController, eventManager );
        parent.addIncludedController( searchResultsController, eventManager );
    }

    @Override
    public String getDialogTitle() {
        return dialogTitle;
    }

    public void launchWindow(ActionEvent e) {
        SearchWindowController w = SearchWindowController.init();
        w.show( dialogTitle );
        // return w;
    }
}
