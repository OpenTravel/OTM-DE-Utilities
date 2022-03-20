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
import org.opentravel.model.OtmResourceChild;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a resource has been modified. Modifications must not change the structure of the
 * resource. For structural changes, use {@link DexResourceChangeEvent}. This event signals that a field has changed and
 * other resource components may need to refresh.
 * 
 * @author dmh
 *
 */
public class DexResourceChildModifiedEvent extends DexChangeEvent {
    private static Logger log = LogManager.getLogger( DexResourceChildModifiedEvent.class );
    private static final long serialVersionUID = 20190829L;

    public static final EventType<DexResourceChildModifiedEvent> RESOURCE_CHILD_MODIFIED =
        new EventType<>( DEX_ALL, "RESOURCE_CHILD_MODIFIED" );

    // private final transient OtmResourceChild resourceChild;

    // public OtmResourceChild getResource() {
    // return resourceChild;
    // }

    /**
     * Filter change event with no subject.
     */
    public DexResourceChildModifiedEvent() {
        super( RESOURCE_CHILD_MODIFIED );
        // resourceChild = null;
    }

    /**
     * @param otmLibrary
     */
    public DexResourceChildModifiedEvent(OtmResourceChild resourceChild) {
        super( RESOURCE_CHILD_MODIFIED );
        // log.debug("DexEvent model manager constructor ran.");
        this.otmObject = resourceChild;
    }

}
