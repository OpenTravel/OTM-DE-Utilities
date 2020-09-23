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

import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * DEX Change Event extends DexEvent and Abstract OTM event.
 * <p>
 * Change events occur when the OTM/TL model has been modified.
 * 
 * @author dmh
 *
 */
public abstract class DexChangeEvent extends DexEvent {
    // private static Log log = LogFactory.getLog( DexChangeEvent.class );
    private static final long serialVersionUID = 20190826L;

    protected transient OtmObject otmObject = null;

    public OtmObject getOtmObject() {
        return otmObject;
    }

    /**
     * Filter change event with no subject.
     */
    public DexChangeEvent() {
        super( DEX_ALL );
    }

    /**
     * @param eventType
     */
    public DexChangeEvent(EventType<? extends DexChangeEvent> eventType) {
        super( eventType );
    }

    public DexChangeEvent(EventType<? extends DexChangeEvent> eventType, OtmObject otm) {
        super( eventType );
        otmObject = otm;
    }

    public DexChangeEvent(Object source, EventTarget target) {
        super( source, target, DEX_ALL );
    }

    public void set(OtmObject otm) {
        otmObject = otm;
    }

    public OtmObject get() {
        return otmObject;
    }

}
