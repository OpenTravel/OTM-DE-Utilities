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
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.events.DexRepositoryItemSelectionEvent;
import org.opentravel.dex.events.DexRepositoryNamespaceSelectionEvent;
import org.opentravel.dex.events.DexRepositorySelectionEvent;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

/**
 * Controller for opening browser to selected repository item.
 * 
 * @author dmh
 *
 */
public class RepositoryItemWebViewController extends DexIncludedControllerBase<RepoItemDAO> {
    private static Log log = LogFactory.getLog( RepositoryItemWebViewController.class );

    private static final EventType[] subscribedEvents = {DexRepositoryItemSelectionEvent.REPOSITORY_ITEM_SELECTED,
        DexRepositoryNamespaceSelectionEvent.REPOSITORY_NS_SELECTED, DexRepositorySelectionEvent.REPOSITORY_SELECTED};

    @FXML
    public WebView webView;
    @FXML
    public TextField webURL;

    public RepositoryItemWebViewController() {
        super( subscribedEvents );
    }

    @Override
    public void checkNodes() {
        if (!(webView instanceof WebView))
            throw new IllegalStateException( "Web View not injected by FXML." );
    }

    @Override
    public void clear() {
        // historyTable.getItems().clear();
    }

    public void handleEvent(DexRepositoryItemSelectionEvent event) {
        try {
            post( event.getValue() );
        } catch (Exception e) {
            mainController.postError( e, "Error displaying repository item history" );
        }
    }

    @Override
    public void handleEvent(AbstractOtmEvent e) {
        if (e instanceof DexRepositoryItemSelectionEvent)
            handleEvent( (DexRepositoryItemSelectionEvent) e );
        else if (e instanceof DexRepositoryNamespaceSelectionEvent)
            clear();
        else if (e instanceof DexRepositorySelectionEvent)
            clear();
    }

    @Override
    public void initialize() {
        // log.debug( "Initializing repository library table view." );
    }

    @Override
    public void post(RepoItemDAO repoItem) {
        super.post( repoItem );


        if (repoItem != null) {
            // Authentication ???
            webView.getEngine().load( repoItem.getRepositoryURL() );
            webView.getEngine().setUserAgent( "Dex 1.0 - JavaFX 8.0" );
            log.debug( "Posting: " + repoItem.getRepositoryURL() );
            if (webURL != null) {
                webURL.setText( repoItem.getRepositoryURL() );
                webURL.setEditable( false );
            }
        }
    }

    @Override
    public void refresh() {
        try {
            post( postedData );
        } catch (Exception e) {
            log.error( "Unhandled error refreshing repository item commit history: " + e.getLocalizedMessage() );
        }
    }
}
