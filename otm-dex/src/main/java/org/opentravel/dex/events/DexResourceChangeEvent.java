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
 * OTM DEX event for signaling when a model managed has a significant change to the model. This event signals that the
 * old model is invalid and users should reload from the manager.
 * 
 * @author dmh
 *
 */
public class DexResourceChangeEvent extends DexChangeEvent {
    private static Logger log = LogManager.getLogger( DexResourceChangeEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexResourceChangeEvent> RESOURCE_CHANGED =
        new EventType<>( DEX_ALL, "RESOURCE_CHANGED" );


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
    public DexResourceChangeEvent() {
        super( RESOURCE_CHANGED );
    }

    /**
     * @param otmLibrary
     */
    public DexResourceChangeEvent(OtmResource resource) {
        super( RESOURCE_CHANGED, resource );
    }

}
