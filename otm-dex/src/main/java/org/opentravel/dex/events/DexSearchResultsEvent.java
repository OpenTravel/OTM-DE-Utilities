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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.search.SearchResultsDAO;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling new search results are available.
 * 
 * @author dmh
 *
 */
public class DexSearchResultsEvent extends DexEvent {
    private static Log log = LogFactory.getLog( DexSearchResultsEvent.class );
    private static final long serialVersionUID = 20191109L;

    public static final EventType<DexSearchResultsEvent> SEARCH_RESULTS = new EventType<>( DEX_ALL, "SEARCH_RESULTS" );

    private final SearchResultsDAO results;

    public SearchResultsDAO get() {
        return results;
    }

    public SearchResultsDAO getResults() {
        return get();
    }

    /**
     */
    public DexSearchResultsEvent(SearchResultsDAO results) {
        super( SEARCH_RESULTS );
        log.debug( "DexEvent source/target constructor ran." );
        this.results = results;
    }
}
