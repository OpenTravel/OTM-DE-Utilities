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
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a resource has been modified. Modifications must not change the structure of the
 * resource. For structural changes, use {@link DexResourceChangeEvent}. This event signals that a field has changed and
 * other resource components may need to refresh.
 * 
 * @author dmh
 *
 */
public class DexResourceModifiedEvent extends DexChangeEvent {
    private static Logger log = LogManager.getLogger( DexResourceModifiedEvent.class );
    private static final long serialVersionUID = 20190909L;

    public static final EventType<DexResourceModifiedEvent> RESOURCE_MODIFIED =
        new EventType<>( DEX_ALL, "RESOURCE_MODIFIED" );

    // private final transient OtmResource resource;

    public OtmResource getResource() {
        if (otmObject instanceof OtmResource)
            return (OtmResource) otmObject;
        if (otmObject instanceof OtmResourceChild)
            return (OtmResource) otmObject.getOwningMember();
        return null;
    }

    /**
     * Filter change event with no subject.
     */
    public DexResourceModifiedEvent() {
        super( RESOURCE_MODIFIED );
    }

    /**
     * @param otmLibrary
     */
    public DexResourceModifiedEvent(OtmResource resource) {
        super( RESOURCE_MODIFIED, resource );
        // log.debug("DexEvent model manager constructor ran.");
    }

}
