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

package org.opentravel.dex.controllers;

import org.opentravel.dex.events.DexEvent;

import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * Abstract interface for all included OTM-DE FX view controllers. These controllers must be able to "Post" a view of
 * the object type declared as the generic variable.
 * <p>
 * <ul>
 * <li>The FXML file for this controller must be included into another FXML file.
 * <li>The controller must have the same name as the FXML file with "Controller" as a suffix.
 * <li>The controller must be declared with an @FXML in the containing controller.
 * </ul>
 * 
 * @author dmh
 *
 */
public interface DexIncludedController<T> extends DexController {

    /**
     * Do any post-initialization configuration needed by this controller now that it has full access to program
     * resource. Is invoked when the included controller is
     * {@link DexMainControllerBase#addIncludedController(DexIncludedController)} is called.
     * <p>
     * Set the main parent controller. Included controllers will not have access to the parent controller until this
     * method is called. An illegalState exception should be thrown if the parent controller is needed for posting data
     * into the view before the parent is set.
     * <p>
     * This method should retrieve all of the resources it needs from the parent such as the stage or image and model
     * managers.
     * 
     * @param parent
     */
    public void configure(DexMainController parent);

    /**
     * @return
     */
    public List<EventType> getPublishedEventTypes();

    /**
     * @return
     */
    public List<EventType> getSubscribedEventTypes();

    /**
     * Get the parent main controller.
     * 
     * @return
     */
    public DexMainController getMainController();

    /**
     * Method that receives events.
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
    public void handleEvent(Event event);

    // May do nothing if this controller does not publish the event type
    public void setEventHandler(EventType<? extends DexEvent> type, EventHandler<DexEvent> handler);

    /**
     * Initialize is called by the FXML loader when the FXML file is loaded. These methods must make the controller
     * ready to "Post" to their view components. Trees must be initialized, table columns set, etc.
     * <p>
     * This method should verify that all views and fields have been injected correctly and throw
     * illegalArgumentException if not.
     * <p>
     * Note: parent controller is not known and business data will not be available when this is called.
     */
    @Override
    public void initialize();

    /**
     * Post the business data into this controller's view(s). Save the data in <i>postedData</i> and clear()s. This
     * method is expected to be extended to handle forward/back navigation.
     * <p>
     * This method is expected to be overridden. Implementations <b>must</b> call super.post(businessData) first.
     * 
     * @param businessData
     * @throws Exception if business logic throws exceptions or parent controller is needed and not set.
     */
    public void post(T businessData) throws Exception;

    /**
     * Inform application that an event has occurred.
     * <p>
     * <b>Caution</b> if the controller also listens to this event, there could be a loop
     * 
     * @param event DexEvent to publish
     */
    public void publishEvent(DexEvent event);

    /**
     * Attempt to select a member of the collection using the passed object.
     * 
     * @param selector is the data to attempt to match to a valid selection
     */
    void select(Object selector);

    /**
     * Fire an event from this controllers eventPublisherNode. Only event types advertised as being published by this
     * controller are fired, other requests are silently ignored.
     * 
     * @param event
     */
    void fireEvent(DexEvent event);

    /**
     * 
     */

}
