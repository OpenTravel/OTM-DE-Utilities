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
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexSearchResultsEvent;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Manage the search results display
 * 
 * @author dmh
 */
public class SearchResultsController extends DexIncludedControllerBase<SearchResultsDAO> {
    private static Log log = LogFactory.getLog( SearchResultsController.class );

    @FXML
    private TreeView<SearchResultItemDAO> resultsTreeView;

    private static final EventType[] subscribedEvents = {DexSearchResultsEvent.SEARCH_RESULTS};
    private static final EventType[] publishedEvents = {};

    @Override
    public void checkNodes() {
        if (resultsTreeView == null)
            throw new IllegalStateException( "Null results tree view in search controller." );
    }

    public SearchResultsController() {
        super( subscribedEvents );
    }

    @Override
    @FXML
    public void initialize() {
        log.debug( "Search Results Controller initialized." );
    }

    @Override
    public void post(SearchResultsDAO results) throws Exception {
        super.post( results ); // Clear tree and save results
        log.debug( "Posting search results." );
        results.createTreeItems( resultsTreeView.getRoot() );
    }

    @Override
    public void configure(DexMainController parent) {
        super.configure( parent );
        eventPublisherNode = resultsTreeView;

        resultsTreeView.setRoot( new TreeItem<SearchResultItemDAO>() );
        resultsTreeView.setShowRoot( false );

        log.debug( "Search results Stage set." );
    }


    private void handleEvent(DexSearchResultsEvent event) {
        try {
            post( event.get() );
        } catch (Exception e) {
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        log.debug( event.getEventType() + " event received. " );
        if (event instanceof DexSearchResultsEvent)
            handleEvent( (DexSearchResultsEvent) event );
        else
            refresh();
    }
}
