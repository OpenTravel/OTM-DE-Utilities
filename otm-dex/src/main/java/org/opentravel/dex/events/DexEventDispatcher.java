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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexIncludedController;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;

/**
 *  OTM DEX event dispatcher.
 * @author dmh
 *
 */
/**
 * Useful for debugging, this is a Dispatcher that simply logs when an DexEvent is received. All events are then passed
 * to the original dispatcher.
 * 
 * @author dmh
 *
 */
public class DexEventDispatcher implements EventDispatcher {
    private static Log log = LogFactory.getLog( DexEventDispatcher.class );
    private final EventDispatcher originalDispatcher;
    private final Deque<DexNavigationEvent> navQueue = new ArrayDeque<>();
    private final Deque<DexNavigationEvent> navUndoneQueue = new ArrayDeque<>();
    private boolean ignoreNext = false;

    public DexEventDispatcher(EventDispatcher originalDispatcher) {
        this.originalDispatcher = originalDispatcher;
    }

    public boolean canGoBack() {
        return !navQueue.isEmpty();
    }

    public void goBack(DexIncludedController<?> eventNode) {
        if (navQueue.isEmpty())
            return;
        // DexNavigationEvent oldEvent = navQueue.pop();
        DexNavigationEvent oldEvent = navQueue.removeLast();
        if (oldEvent != null)
            navUndoneQueue.addLast( oldEvent );
        DexNavigationEvent event = navQueue.peekLast();
        if (event != null) {
            ignoreNext = true;
            eventNode.fireEvent( event );
            // log.debug(
            // "Fired " + event.getMember() + " from nav queue. Popped " + oldEvent.getMember() + " from queue." );
        }
    }

    public boolean canGoForward() {
        return !navUndoneQueue.isEmpty();
    }

    public void goForward(DexIncludedController<?> eventNode) {
        if (navUndoneQueue.isEmpty())
            return;
        DexNavigationEvent event = navUndoneQueue.removeLast();
        navQueue.add( event );
        ignoreNext = true;
        eventNode.fireEvent( event );
    }

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if (event instanceof DexEvent) {
            // log.debug( "Using my dispatcher on my event: " + event.getClass().getSimpleName() );
            if (event instanceof DexNavigationEvent && !ignoreNext) {
                navQueue.add( (DexNavigationEvent) event );
                navUndoneQueue.clear();
                // log.debug( "Pushed " + ((DexNavigationEvent) event).getMember() + " onto nav queue." );
            }
            ignoreNext = false;
            // Add code here if the event is to be handled outside of the dispatch chain
            // event.consume();
            // some event filter and business logic ...
            // return event;
        }
        return originalDispatcher != null ? originalDispatcher.dispatchEvent( event, tail ) : event;
    }
}
