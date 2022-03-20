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
 * OTM DEX event for signaling when an Otm Object has been replaced by another. Controllers that are posting the
 * original object should post the replacement.
 * <p>
 * Does <b>not</b> indicate the original object was deleted.
 * 
 * @author dmh
 *
 */
public class OtmObjectReplacedEvent extends DexChangeEvent {
    // private static Logger log = LogManager.getLogger( OtmObjectChangeEvent.class );
    private static final long serialVersionUID = 20190909L;

    public static final EventType<OtmObjectReplacedEvent> OBJECT_REPLACED =
        new EventType<>( DEX_ALL, "OBJECT_REPLACED" );

    private final transient OtmObject originalObject;

    public OtmObject getOrginalObject() {
        return originalObject;
    }

    public OtmObject getReplacementObject() {
        return get();
    }

    /**
     * {@inheritDoc} get the replacement object
     * 
     * @see org.opentravel.dex.events.DexChangeEvent#get()
     */
    @Override
    public OtmObject get() {
        return otmObject;
    }

    // public OtmObjectReplacedEvent() {
    // super( OBJECT_REPLACED );
    // otmObject = null;
    // }

    public OtmObjectReplacedEvent(OtmObject replacement, OtmObject orginal) {
        super( OBJECT_REPLACED );
        this.otmObject = replacement;
        this.originalObject = orginal;
    }

}
