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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.OtmEventUser;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.dex.events.DexEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;

/**
 * Abstract base controller for included controllers.
 * <p>
 * This controller exposes the collection members of the posted business object. The generic type is the Collection
 * containing business data object used when "posting" to this controller.
 * 
 * @author dmh
 *
 */
public abstract class DexIncludedControllerBase<C> implements DexIncludedController<C>, OtmEventUser {
    private static Log log = LogFactory.getLog( DexIncludedControllerBase.class );

    protected DexMainController mainController;
    protected C postedData;

    // Lists to copy static arrays declared in the sub-types
    protected List<EventType<? extends AbstractOtmEvent>> publishedEventTypes = null;
    protected List<EventType<? extends AbstractOtmEvent>> subscribedEventTypes = null;
    // source FX Node for events fired from this controller. None fired if null.
    protected Node eventPublisherNode = null;

    public DexIncludedControllerBase() {
        // log.debug( "Constructing included controller." );
    }

    @Override
    public DexMainController getMainController() {
        return mainController;
    }

    /**
     * @see org.opentravel.dex.controllers.DexIncludedController#getSelection()
     */
    @Override
    public DexDAO<?> getSelection() {
        return null; // MUST OVERRIDE!
    }

    public DexIncludedControllerBase(EventType<? extends AbstractOtmEvent>[] subscribed) {
        // log.debug( "Constructing included controller with subscribed event types." );
        if (subscribed != null && subscribed.length > 0)
            subscribedEventTypes = Collections.unmodifiableList( Arrays.asList( subscribed ) );
    }

    /**
     * Constructor for controller included by FXML into another controller.
     * 
     * @param subscribed event types handled by this controller. Used by main controller to register handlers.
     * @param published event types fired by this controller. Used by main controller to register handlers.
     */
    public DexIncludedControllerBase(EventType<? extends AbstractOtmEvent>[] subscribed,
        EventType<? extends AbstractOtmEvent>[] published) {
        this( subscribed );
        // log.debug( "Constructing included controller with subscribed and published event types." );
        if (published != null && published.length > 0)
            publishedEventTypes = Collections.unmodifiableList( Arrays.asList( published ) );
    }

    @Override
    public List<EventType<? extends AbstractOtmEvent>> getPublishedEventTypes() {
        return publishedEventTypes != null ? publishedEventTypes : Collections.emptyList();
    }

    @Override
    public List<EventType<? extends AbstractOtmEvent>> getSubscribedEventTypes() {
        return subscribedEventTypes != null ? subscribedEventTypes : Collections.emptyList();
    }

    @Override
    public void handleEvent(AbstractOtmEvent e) {
        // override
    }

    @Override
    public void setEventHandler(EventType<? extends AbstractOtmEvent> type, EventHandler<AbstractOtmEvent> handler) {
        if (type == null || handler == null)
            return; // Error
        if (eventPublisherNode != null && publishedEventTypes.contains( type )) {
            eventPublisherNode.addEventHandler( type, handler );
            // log.debug( "Event handler set: " + type.getName() + " " + handler.getClass().getName() );
        } else
            log.warn( "Publisher node not set or unhandled event type attempted to have handler set." );
    }

    @Override
    public void clear() {}

    /**
     * {@inheritDoc} check nodes and set main controller
     * 
     * @see org.opentravel.dex.controllers.DexIncludedController#configure(org.opentravel.dex.controllers.DexMainController)
     */
    @Override
    public void configure(DexMainController parent) {
        checkNodes();
        this.mainController = parent;
        // log.debug( "Main controller set." );
    }

    // Only use to fire events from Undo actions
    public Node getEventPublisherNode() {
        return eventPublisherNode;
    }

    /**
     * If event is not fired, check:
     * <ul>
     * <li>Constructor invokes super constructor with subscribed and published events
     * <li>eventPublisherNode is set
     * </ul>
     * 
     * @see org.opentravel.dex.controllers.DexIncludedController#fireEvent(org.opentravel.dex.events.DexEvent)
     */
    @Override
    public void fireEvent(DexEvent event) {
        if (event != null) {
            if (eventPublisherNode != null && publishedEventTypes != null
                && publishedEventTypes.contains( event.getEventType() ))
                eventPublisherNode.fireEvent( event );
            else
                log.warn( event.getEventType() + " event not fired." );
        }
    }

    @FXML
    @Override
    public void initialize() {
        // log.debug( "Initializing controller." );
    }

    @Override
    public void post(C businessData) {
        clear(); // Clear the view
        postedData = businessData; // Hold onto data
    }

    @Override
    public void publishEvent(DexEvent event) {
        eventPublisherNode.fireEvent( event );
    }

    @Override
    public void refresh() {
        post( postedData );
    }

    @Override
    public void select(Object selector) {
        // Override if supported.
    }

    /**
     * Utility to set table column properties.
     */
    protected void setColumnProps(TableColumn<?,?> c, boolean visable, boolean editable, boolean sortable, int width) {
        c.setVisible( visable );
        c.setEditable( editable );
        c.setSortable( sortable );
        if (width > 0)
            c.setPrefWidth( width );
    }

    /**
     * Utility to set tree table column properties.
     */
    protected void setColumnProps(TreeTableColumn<?,?> c, boolean visable, boolean editable, boolean sortable,
        int width) {
        c.setVisible( visable );
        c.setEditable( editable );
        c.setSortable( sortable );
        if (width > 0)
            c.setPrefWidth( width );
    }



    // /**
    // * TODO
    // */
    // protected void setWidths(TableView table) {
    // // Give all left over space to the last column
    // // double width = fileCol.widthProperty().get();
    // // width += versionCol.widthProperty().get();
    // // width += statusCol.widthProperty().get();
    // // width += lockedCol.widthProperty().get();
    // // remarkCol.prefWidthProperty().bind(table.widthProperty().subtract(width));
    // }

}
