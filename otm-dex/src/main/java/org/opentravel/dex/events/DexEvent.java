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
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.actions.DexAction;
import org.opentravel.dex.controllers.member.MemberFilterController;

import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * DEX Event extends Abstract OTM event.
 * <p>
 * DexEvents leverage and extend JavaFX event architecture to provide the ability to loosely couple the interaction
 * between two or more controllers. Controllers can declare on initialization the events they throw and listen to.
 * Controllers then throw events to indicate they have changed the application state. <b>No</b> controller should ever
 * directly call other controllers--they use events to pass control and data.
 * <p>
 * Events are can be thrown when any application state changes. Events are not thrown for specific changes to model
 * objects (see {@link DexAction} ) but may be thrown to indicate an object has changed, been added or deleted.
 * <p>
 * Implementation steps (event publisher):
 * <ol>
 * <li>Create sub-type of DexEvent (if needed)
 * <li>Add event type to published event list
 * <li>Ensure <i>eventPublisherNode</i> in base controller is set to the fx:node used to broadcast events.
 * <li>Add <i>eventPublisherNode.fireEvent(new event)</i> in event provider controllers where needed.
 * </ol>
 * Implementation steps (event consumer(s)):
 * <ol>
 * <li>Override <i>handleEvent</i> method in consumer controller.
 * <li>Add event specific <i>handleEvent(SpecificEvent e)</i> business logic handler methods.
 * </ul>
 * <p>
 * 
 * @see MemberFilterController MemberFilterController for example of a provider.
 *      <p>
 * @see https://stackoverflow.com/questions/27416758/how-to-emit-and-handle-custom-events
 * @author dmh
 *
 */
public abstract class DexEvent extends AbstractOtmEvent {
    private static Logger log = LogManager.getLogger( DexEvent.class );
    private static final long serialVersionUID = 20190406L;
    public static final EventType<DexEvent> DEX_ALL = new EventType<>( "DEX_ALL" );

    @SuppressWarnings("unchecked")
    @Override
    public EventType<? extends DexEvent> getEventType() {
        return (EventType<? extends DexEvent>) super.getEventType();
    }

    /**
     * Filter change event with no subject.
     */
    public DexEvent() {
        super( DEX_ALL );
    }

    /**
     * @param eventType
     */
    public DexEvent(EventType<? extends DexEvent> eventType) {
        super( eventType );
    }

    public DexEvent(Object source, EventTarget target, EventType<? extends DexEvent> eventType) {
        super( source, target, eventType );
        // log.debug("DexEvent source/target/type constructor ran for " + getClass().getSimpleName());
        // If there is data, extract it from source or target here
    }

    public DexEvent(Object source, EventTarget target) {
        super( source, target, DEX_ALL );
        // log.debug("DexEvent source/target/type constructor ran for " + getClass().getSimpleName());
        // If there is data, extract it from source or target here
    }
}
