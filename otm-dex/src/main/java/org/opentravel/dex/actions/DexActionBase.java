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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.dex.actions.string.NameChangeAction;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmObject;

public abstract class DexActionBase {
    private static Logger log = LogManager.getLogger( DexActionBase.class );

    protected DexActions actionType = null; // Which enumeration does this action implement
    protected OtmObject otm; // Actions are performed on the subject OtmObject
    protected boolean ignore; // If true, other actions are events are not invoked

    public DexActionBase() {}


    public DexActions getType() {
        return actionType;
    }

    public void setType(DexActions type) {
        this.actionType = type;
    }

    /**
     * Post a warning dialog to inform the user of consequences of their action.
     * 
     * @param title
     * @param reason
     */
    protected void postWarning(String reason) {
        getSubject().getActionManager().postWarning( reason );
    }

    // Override if the action has specific sub-type of OtmObject as its subject.
    public OtmObject getSubject() {
        return otm;
    }

    /**
     * Validate the otm and its owning member.
     * <p>
     * Override if there are veto keys needed to select relevant findings. See {@link NameChangeAction#isValid()} for an
     * selection example.
     */
    public boolean isValid() {
        if (otm == null)
            return false;

        // Validate the parent - naming could change validation status
        if (otm.getOwningMember() != null)
            otm.getOwningMember().isValid( true );
        return otm.isValid( true );
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
