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
import org.opentravel.model.OtmModelManager;

import javafx.event.EventType;

/**
 * OTM DEX event for signaling when a model managed has a significant change to the model. This event signals that the
 * old model is invalid and users should reload from the manager.
 * 
 * @author dmh
 *
 */
public class DexModelChangeEvent extends DexChangeEvent {
    private static Logger log = LogManager.getLogger( DexModelChangeEvent.class );
    private static final long serialVersionUID = 20190409L;

    public static final EventType<DexModelChangeEvent> MODEL_CHANGED = new EventType<>( DEX_ALL, "MODEL_CHANGED" );

    private final transient OtmModelManager modelManager;

    public OtmModelManager getModelManager() {
        return modelManager;
    }

    /**
     * Filter change event with no subject.
     */
    public DexModelChangeEvent() {
        super( MODEL_CHANGED );
        modelManager = null;
    }

    /**
     * @param otmLibrary
     */
    public DexModelChangeEvent(OtmModelManager manager) {
        super( MODEL_CHANGED );
        // log.debug("DexEvent model manager constructor ran.");
        modelManager = manager;
    }

}
