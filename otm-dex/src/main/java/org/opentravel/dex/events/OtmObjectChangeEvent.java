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

import org.opentravel.model.OtmObject;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when an Otm Object structure has changed.
 * 
 * @author dmh
 *
 */
public class OtmObjectChangeEvent extends DexChangeEvent {
    // private static Logger log = LogManager.getLogger( OtmObjectChangeEvent.class );
    private static final long serialVersionUID = 20190909L;

    public static final EventType<OtmObjectChangeEvent> OBJECT_CHANGED = new EventType<>( DEX_ALL, "OBJECT_CHANGED" );

    // private final transient OtmObject object;

    public OtmObject getObject() {
        return get();
    }

    @Override
    public OtmObject get() {
        return otmObject;
    }

    public OtmObjectChangeEvent() {
        super( OBJECT_CHANGED );
        otmObject = null;
    }

    /**
     * @param otmLibrary
     */
    public OtmObjectChangeEvent(OtmObject object) {
        super( OBJECT_CHANGED );
        this.otmObject = object;
    }

}
