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
import org.opentravel.schemacompiler.repository.RepositoryItem;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a repository item has been replaced by a different repository item. This can happen
 * when libraries are locked.
 * 
 * @author dmh
 *
 */
public class DexRepositoryItemReplacedEvent extends DexEvent {
    private static Logger log = LogManager.getLogger( DexRepositoryItemReplacedEvent.class );
    private static final long serialVersionUID = 20190606L;

    public static final EventType<DexRepositoryItemReplacedEvent> REPOSITORY_ITEM_REPLACED =
        new EventType<>( DEX_ALL, "REPOSITORY_ITEM_REPLACED" );

    private final RepositoryItem oldItem;
    private final RepositoryItem newItem;

    public RepositoryItem getValue() {
        return newItem;
    }

    public RepositoryItem getNewItem() {
        return newItem;
    }

    public RepositoryItem getOldItem() {
        return oldItem;
    }

    // /**
    // * Filter change event with no subject.
    // */
    // public DexRepositoryItemReplacedEvent() {
    // super(REPOSITORY_ITEM_REPLACED);
    // repoItem = null;
    // }

    public DexRepositoryItemReplacedEvent(Object source, RepositoryItem oldItem, RepositoryItem newItem) {
        super( source, null, REPOSITORY_ITEM_REPLACED );
        log.debug( "DexEvent source/target constructor ran." );

        this.oldItem = oldItem;
        this.newItem = newItem;
    }
}
