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

package org.opentravel.application.common.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.OtmEventUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.event.EventType;

/**
 * Manage publishing and subscribing to OTM Event streams. Controllers must register with this manager to participate in
 * event chains.
 * <p>
 * OTM and DEX events are fired by publishing controllers. Subscribers receive these events in their eventHandler(). The
 * runtime events are first examined by the DEX/OTM event dispatcher then passed onto the FX event dispatch chain.
 * 
 */
public class OtmEventSubscriptionManager {
    private static Logger log = LogManager.getLogger( OtmEventSubscriptionManager.class );

    // Map of event types to list of publishers of that event type
    private Map<EventType<? extends AbstractOtmEvent>,List<OtmEventUser>> publishedEvents = new HashMap<>();

    // Map of event types to list of subscribers of that event type
    private Map<EventType<? extends AbstractOtmEvent>,List<OtmEventUser>> subscribedEvents = new HashMap<>();

    // Flag to minimize number of times handlers are set onto the publisher
    private boolean dirtyFlag = false;

    /**
     * Default constructor.
     */
    public OtmEventSubscriptionManager() {
        // No action needed in default constructor.
    }

    /**
     * Add this controller's events to the maps of published and subscribed events.
     * <p>
     * <b>Note:</b> caller must {@link #configureEventHandlers()} after all the controllers are registered.
     * 
     * @param controller
     */
    public void register(OtmEventUser controller) {
        if (!controller.getPublishedEventTypes().isEmpty() || !controller.getSubscribedEventTypes().isEmpty()) {
            // log.debug( "Registering events for " + controller.getClass().getSimpleName() );
            dirtyFlag = true; // there are events

            // Record any published event types
            for (EventType<? extends AbstractOtmEvent> et : controller.getPublishedEventTypes())
                addController( publishedEvents, et, controller );

            // Record any subscribed event types
            for (EventType<? extends AbstractOtmEvent> et : controller.getSubscribedEventTypes()) {
                addController( subscribedEvents, et, controller );
            }
        }
    }

    /**
     * Un-register the controller and remove handlers from the publishers.
     * <p>
     * <b>Partially done</b> - handlers are not removed.
     * 
     * @param controller
     */
    public void remove(OtmEventUser controller) {
        // Unregister any published event types
        for (EventType<?> et : controller.getPublishedEventTypes())
            if (publishedEvents.containsKey( et )) {
                publishedEvents.get( et ).remove( controller );
                // TODO - remove handler
            }

        // Unregister any subscribed event types
        for (EventType<?> et : controller.getSubscribedEventTypes())
            if (subscribedEvents.containsKey( et )) {
                subscribedEvents.get( et ).remove( controller );
                // TODO - remove handler
                // removeEventHandler(event, handler);
            }

    }

    private void addController(Map<EventType<? extends AbstractOtmEvent>,List<OtmEventUser>> map,
        EventType<? extends AbstractOtmEvent> et, OtmEventUser controller) {
        if (map.containsKey( et )) {
            if (!map.get( et ).contains( controller ))
                map.get( et ).add( controller );
        } else {
            ArrayList<OtmEventUser> list = new ArrayList<>();
            list.add( controller );
            map.put( et, list );
        }
    }

    /**
     * Configure all event publishers to throw events to the subscribers.
     * <p>
     * Main controllers should {@link #register(OtmEventUser)} all their sub-controllers first, <i>then</i> configure
     * the handlers.
     */
    public void configureEventHandlers() {
        // log.debug( "Configuring event handlers." );
        if (hasNewUsers()) {
            // For each published event type
            for (Entry<EventType<? extends AbstractOtmEvent>,List<OtmEventUser>> set : publishedEvents.entrySet()) {
                // For each publisher of that event type
                for (OtmEventUser publisher : set.getValue()) {
                    setSubscribers( publisher, set.getKey() );
                }
            }
            dirtyFlag = false; // No subscribers or publisher registered
        }
    }

    private boolean hasNewUsers() {
        if (subscribedEvents == null || subscribedEvents.isEmpty() || publishedEvents == null
            || publishedEvents.isEmpty())
            dirtyFlag = false; // No subscribers or publisher registered
        return dirtyFlag;
    }

    private void setSubscribers(OtmEventUser publisher, EventType<? extends AbstractOtmEvent> publishedEvent) {
        // Set event handler for all subscribers
        // log.debug(
        // "Setting subscribers for: " + publishedEvent.toString() + " from " + publisher.getClass().getSimpleName() );
        if (subscribedEvents.containsKey( publishedEvent )) {
            for (OtmEventUser subscriber : subscribedEvents.get( publishedEvent )) {
                publisher.setEventHandler( publishedEvent, subscriber::handleEvent );
                // log.debug(
                // "Set Subscriber to " + publishedEvent.getName() + " for " + subscriber.getClass().getSimpleName() );
            }
        }
    }

}

