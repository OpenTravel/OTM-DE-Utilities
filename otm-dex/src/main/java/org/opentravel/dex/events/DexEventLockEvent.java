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

import javafx.event.EventType;

/**
 * OTM DEX navigation event for signaling when a event lock control has changed.
 * <p>
 * EventLock is a display control that when engaged prevent the displayed contents from changing. Any controller that
 * has a lock button should set its own control then fire this event. Other controllers that can be locked must
 * subscribe the event and determine if the event is relevant to them using the view group ID.
 * <p>
 * Controllers typically implement the lock by ignoring all new events (except this one and OtmObjectChangeEvent) until
 * unlocked.
 * 
 * @author dmh
 *
 */
public class DexEventLockEvent extends DexNavigationEvent {
    private static Logger log = LogManager.getLogger( DexEventLockEvent.class );
    private static final long serialVersionUID = 20200530L;

    public static final EventType<DexEventLockEvent> EVENT_LOCK = new EventType<>( DEX_ALL, "EVENT_LOCK" );

    protected boolean locked = false;
    protected int viewGroupId;

    /**
     * An event to notify controllers that an event lock has changed.
     * 
     * @param locked true if matching controllers should lock their displayed contents.
     * @param viewGroupId the tab/window group id of the controller that threw the event.
     */
    public DexEventLockEvent(boolean locked, int viewGroupId) {
        super( EVENT_LOCK );
        this.locked = locked;
        this.viewGroupId = viewGroupId;
    }

    public int getViewGroup() {
        return viewGroupId;
    }

    public boolean get() {
        return locked;
    }
}
