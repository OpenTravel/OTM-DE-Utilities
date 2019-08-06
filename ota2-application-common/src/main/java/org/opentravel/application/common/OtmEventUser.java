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

package org.opentravel.application.common;

import org.opentravel.application.common.events.AbstractOtmEvent;

import java.util.List;

import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * Abstract interface for all controllers that publish or subscribe to OtmEvents.
 * 
 * @author dmh
 *
 */
public interface OtmEventUser {

    /**
     * @return list of Otm Events published by this event user
     */
    public List<EventType<? extends AbstractOtmEvent>> getPublishedEventTypes();

    /**
     * @return list of Otm Events subscribed to by this event user
     */
    public List<EventType<? extends AbstractOtmEvent>> getSubscribedEventTypes();

    /**
     * Event subscriber method that receives events.
     * <p>
     * <b>NOTE</b> because the handler is set before the actual event is fired and its type is known, implementations
     * may <b>NOT</b> use a sub-type of Event. They must perform instance of tests and either handle directly or call
     * appropriate method. It is encouraged to have additional handleEvent methods that have specific sub-types.
     * <p>
     * <b>Note</b> handlers must guard against firing events when setting controls in their controller. For example, if
     * a controller both publishes and subscribes to a library selection event, it must take care to not fire a library
     * selection event when handling a library selection event.
     * 
     * @param event
     */
    public void handleEvent(AbstractOtmEvent event);

    /**
     * Add to the event publisher's event dispatch chain the passed subscriber's event handler for the passed OtmEvent
     * type.
     * <p>
     * Does nothing if the handler is already set or this controller does not publish the event type.
     * <p>
     * Example: publisherNode.setEventHandler( new OtmEventX(data), subscriber::handleEvent );
     * 
     * @param type sub-class of AbstractOtmEvent
     * @param handler subscriber event handler to call
     */
    public void setEventHandler(EventType<? extends AbstractOtmEvent> type, EventHandler<AbstractOtmEvent> handler);


}
