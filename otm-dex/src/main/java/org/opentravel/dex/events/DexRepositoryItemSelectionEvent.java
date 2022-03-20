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

package org.opentravel.dex.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.controllers.repository.RepoItemDAO;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a library member has been selected.
 * 
 * @author dmh
 *
 */
public class DexRepositoryItemSelectionEvent extends DexEvent {
    private static Logger log = LogManager.getLogger( DexRepositoryItemSelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexRepositoryItemSelectionEvent> REPOSITORY_ITEM_SELECTED =
        new EventType<>( DEX_ALL, "REPOSITORY_ITEM_SELECTED" );

    private final RepoItemDAO repoItem;

    public RepoItemDAO getValue() {
        return repoItem;
    }

    /**
     * Filter change event with no subject.
     */
    public DexRepositoryItemSelectionEvent() {
        super( REPOSITORY_ITEM_SELECTED );
        repoItem = null;
    }

    // /**
    // * A library member selection event.
    // *
    // * @param source
    // * is the controller that created the event
    // * @param target
    // * the tree item that was selected
    // */
    public DexRepositoryItemSelectionEvent(Object source, RepoItemDAO item) {
        super( source, null, REPOSITORY_ITEM_SELECTED );
        log.debug( "DexEvent source/target constructor ran." );

        this.repoItem = item;
    }

    // /**
    // */
    // public DexRepositoryNamespaceSelectionEvent(Repository repository) {
    // super(REPOSITORY_ITEM_SELECTED);
    // log.debug("DexEvent source/target constructor ran.");
    // this.repository = repository;
    // }

}
