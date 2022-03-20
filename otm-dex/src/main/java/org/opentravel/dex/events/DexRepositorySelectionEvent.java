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
import org.opentravel.schemacompiler.repository.Repository;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling.
 * 
 * @author dmh
 *
 */
public class DexRepositorySelectionEvent extends DexEvent {
    private static Logger log = LogManager.getLogger( DexRepositorySelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexRepositorySelectionEvent> REPOSITORY_SELECTED =
        new EventType<>( DEX_ALL, "REPOSITORY_SELECTED" );

    private final Repository repository;

    public Repository getRepository() {
        return repository;
    }

    /**
     * Filter change event with no subject.
     */
    public DexRepositorySelectionEvent() {
        super( REPOSITORY_SELECTED );
        repository = null;
    }

    /**
     */
    public DexRepositorySelectionEvent(Repository repository) {
        super( REPOSITORY_SELECTED );
        log.debug( "DexEvent source/target constructor ran." );
        this.repository = repository;
    }
}
