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

package org.opentravel.dex.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmObject;

public abstract class DexActionBase {
    private static Log log = LogFactory.getLog( DexActionBase.class );

    protected DexActions actionType = null; // Which enumeration does this action implement
    protected OtmObject otm; // Actions are performed on the subject OtmObject
    protected boolean ignore; // If true, other actions are events are not invoked

    public DexActionBase() {}


    public DexActions getType() {
        return actionType;
    }

    // Override if the action has specific sub-type of OtmObject as its subject.
    public OtmObject getSubject() {
        return otm;
    }

    /**
     * Get the event and set its subject associated with this action's actionType.
     * 
     * @return
     */
    public DexChangeEvent getEvent() {
        DexChangeEvent event = null;
        try {
            event = DexActions.getEvent( actionType );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.warn( "Failed to get event handler: " + e.getLocalizedMessage() );
            return null;
        }
        if (event != null)
            event.set( otm );
        return event;
    }


}
