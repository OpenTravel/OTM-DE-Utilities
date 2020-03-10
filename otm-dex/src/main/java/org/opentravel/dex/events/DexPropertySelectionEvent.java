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
import org.opentravel.model.otmProperties.OtmProperty;

import javafx.event.EventType;

/**
 * OTM DEX navigation event for signaling when a property has been selected.
 * 
 * @author dmh
 *
 */
public class DexPropertySelectionEvent extends DexNavigationEvent {
    private static Log log = LogFactory.getLog( DexPropertySelectionEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexPropertySelectionEvent> PROPERTY_SELECTED =
        new EventType<>( DEX_ALL, "PROPERTY_SELECTED" );

    public DexPropertySelectionEvent(OtmProperty otm) {
        super( PROPERTY_SELECTED );
        property = otm;
    }

    public OtmProperty get() {
        return property;
    }
}
