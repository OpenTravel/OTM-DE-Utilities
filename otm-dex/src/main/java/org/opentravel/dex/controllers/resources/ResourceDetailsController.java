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

package org.opentravel.dex.controllers.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChangeEvent;
import org.opentravel.dex.events.DexResourceChildModifiedEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.dex.events.DexResourceModifiedEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * Controller for resource details.
 * 
 * @author dmh
 *
 */
public class ResourceDetailsController extends DexIncludedControllerBase<Void> {
    private static Log log = LogFactory.getLog( ResourceDetailsController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private TitledPane resourceDetailsPane;
    @FXML
    private GridPane propertyGrid;
    @FXML
    private TextField memberName;
    @FXML
    private Label memberNameLabel;
    @FXML
    private ImageView objectImageView;

    @FXML
    private TextField memberDescription;

    private int rowIndex = 0; // last row populated with data

    private boolean ignoreClear = false;

    private OtmObject postedObject; // Can't use PostedData

    // private OtmEventSubscriptionManager eventManager;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexResourceChangeEvent.RESOURCE_CHANGED};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexResourceModifiedEvent.RESOURCE_MODIFIED,
        DexResourceChildModifiedEvent.RESOURCE_CHILD_MODIFIED, DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexResourceChangeEvent.RESOURCE_CHANGED,
        DexMemberSelectionEvent.RESOURCE_SELECTED, OtmObjectModifiedEvent.OBJECT_MODIFIED,
        DexModelChangeEvent.MODEL_CHANGED, OtmObjectChangeEvent.OBJECT_CHANGED};

    public ResourceDetailsController() {
        super( subscribedEvents, publishedEvents );
        // log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(resourceDetailsPane instanceof TitledPane))
            throw new IllegalStateException( "Member Details not injected by FXML." );
        if (!(memberName instanceof TextField))
            throw new IllegalStateException( "memberName not injected by FXML." );
        if (!(memberDescription instanceof TextField))
            throw new IllegalStateException( "member description not injected by FXML." );
    }

    @Override
    public void initialize() {
        // log.debug( "Member Filter Controller - Initialize" );
        checkNodes();

    }

    @Override
    public void configure(DexMainController mainController, int viewGroupId) {
        super.configure( mainController, viewGroupId );
        eventPublisherNode = resourceDetailsPane;
        // eventManager = mainController.getEventSubscriptionManager();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        // log.debug( "Event received: " + event.getClass().getSimpleName() );
        if (event instanceof DexMemberSelectionEvent)
            handleEvent( (DexMemberSelectionEvent) event );
        else if (event instanceof DexResourceChildSelectionEvent)
            handleEvent( (DexResourceChildSelectionEvent) event );
        else if (event instanceof DexResourceChildModifiedEvent)
            handleEvent( (DexResourceChildModifiedEvent) event );
        else if (event instanceof DexResourceModifiedEvent)
            handleEvent( (DexResourceModifiedEvent) event );
        else if (event instanceof DexResourceChangeEvent)
            handleEvent( (DexResourceChangeEvent) event );
        else if (event instanceof DexModelChangeEvent)
            handleEvent( (DexModelChangeEvent) event );
        else if (event instanceof OtmObjectModifiedEvent)
            handleEvent( (OtmObjectModifiedEvent) event );
        else if (event instanceof OtmObjectChangeEvent)
            handleEvent( (OtmObjectChangeEvent) event );
    }

    public void handleEvent(DexResourceChildModifiedEvent event) {
        if (event.get() == postedObject)
            refresh();
    }

    public void handleEvent(OtmObjectModifiedEvent event) {
        if (event.get() == postedObject)
            refresh();
    }

    public void handleEvent(OtmObjectChangeEvent event) {
        if (event.get() == postedObject)
            refresh();
    }

    public void handleEvent(DexResourceModifiedEvent event) {
        refresh();
    }

    public void handleEvent(DexResourceChildSelectionEvent event) {
        post( event.get() );
    }

    public void handleEvent(DexResourceChangeEvent event) {
        OtmObject obj = event.get();
        log.debug( obj );
        // post( event.get() );
    }

    public void handleEvent(DexMemberSelectionEvent event) {
        if (event.getMember() instanceof OtmResource)
            post( (OtmResource) event.getMember() );
    }

    public void handleEvent(DexModelChangeEvent event) {
        clear();
    }

    public void memberSelectionHandler(DexMemberSelectionEvent event) {
        if (event != null && event.getMember() instanceof OtmResourceChild) {
            post( (OtmResourceChild) event.getMember() );
        }
    }

    public void post(OtmResource resource) {
        // log.debug( "Posting " + resource );
        if (resource == null) {
            clear();
            return;
        }
        propertyGrid.getChildren().clear();
        postedObject = resource;

        postTitle( resource );
        postName( resource );
        postDescription( resource );
        resource.getFields().forEach( f -> postField( f, resource ) );
    }


    private void postField(DexEditField field, OtmObject obj) {
        int column = field.column;
        if (field.label != null) {
            Label label = new Label( field.label );
            label.setTooltip( field.tooltip );
            propertyGrid.add( label, field.column, field.row + rowIndex );
            column += 1;
            label.setDisable( !obj.isEditable() );
        }
        if (field.fxNode != null) {
            if (field.fxNode instanceof Control) {
                ((Control) field.fxNode).setTooltip( field.tooltip );
            }
            propertyGrid.add( field.fxNode, column, field.row + rowIndex );
        }
    }

    private void postTitle(OtmObject obj) {
        if (obj instanceof OtmResource) {

            String name = obj.getObjectTypeName();
            if (obj.getOwningMember() != null)
                name = obj.getOwningMember().getName();

            String library = " (missing library) ";
            if (obj.getLibrary() != null)
                library = " in " + obj.getLibrary().getFullName();

            objectImageView.setImage( ImageManager.getImage( obj.getIconType() ) );
            resourceDetailsPane.setText( name + library );
        }
    }

    private void postName(OtmObject obj) {
        rowIndex = 0;
        Label label = new Label( "Name" );
        label.setTooltip( obj.getTooltip() );
        propertyGrid.add( label, 0, rowIndex );

        memberName.setDisable( obj.nameProperty() instanceof ReadOnlyStringWrapper );
        memberName.setEditable( !(obj.nameProperty() instanceof ReadOnlyStringWrapper) );
        memberName.setText( obj.nameProperty().get() );
        // Changing the property will trigger the NAMECHANGE action
        memberName.setOnAction( e -> obj.nameProperty().set( memberName.getText() ) );
        propertyGrid.add( memberName, 1, rowIndex++ );
    }

    private void postDescription(OtmObject obj) {
        memberDescription.setEditable( obj.isEditable() );
        memberDescription.setText( obj.descriptionProperty().get() );
        // ??Will Changing the property will trigger the action
        memberDescription.setOnAction( e -> obj.descriptionProperty().set( memberDescription.getText() ) );
        propertyGrid.add( new Label( "Description" ), 0, rowIndex );
        propertyGrid.add( memberDescription, 1, rowIndex++ );
    }

    public void post(OtmResourceChild resourceChild) {
        clear();
        if (resourceChild == null)
            return;

        postedObject = resourceChild;

        postTitle( resourceChild );
        postName( resourceChild );
        postDescription( resourceChild );

        resourceChild.getFields().forEach( f -> postField( f, resourceChild ) );
    }


    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            postedObject = null;
            memberName.setText( "" );
            memberDescription.setText( "" );
            objectImageView.setImage( null );
            resourceDetailsPane.setText( "" );
            propertyGrid.getChildren().clear();
        }
    }

    @Override
    public void refresh() {
        if (postedObject instanceof OtmResource)
            post( (OtmResource) postedObject );
        else if (postedObject instanceof OtmResourceChild)
            post( (OtmResourceChild) postedObject );
    }

}
