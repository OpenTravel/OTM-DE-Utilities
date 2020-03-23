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
import org.opentravel.model.OtmObject;

import javafx.event.EventType;

/**
 * OTM DEX navigation event for signaling when a facet has been selected.
 * 
 * @author dmh
 *
 */
public class DexFacetSelectionEvent extends DexNavigationEvent {
    private static Log log = LogFactory.getLog( DexFacetSelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexFacetSelectionEvent> FACET_SELECTED = new EventType<>( DEX_ALL, "FACET_SELECTED" );

    // Could be contextual facet; contextual facets do not extend facet.
    protected OtmObject facet = null;

    public DexFacetSelectionEvent(OtmObject otm) {
        super( FACET_SELECTED );
        facet = otm;
    }

    public OtmObject get() {
        return facet;
    }
}
