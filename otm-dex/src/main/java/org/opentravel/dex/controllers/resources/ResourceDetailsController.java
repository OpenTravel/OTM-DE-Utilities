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
import org.opentravel.application.common.events.OtmEventSubscriptionManager;
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
    private VBox resourceDetails;
    @FXML
    private TitledPane childObjectType;
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

    private OtmResourceChild selectedChild;
    private int rowIndex = 0; // last row populated with data

    private boolean ignoreClear = false;

    private OtmEventSubscriptionManager eventManager;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED, DexMemberSelectionEvent.MEMBER_SELECTED,
            DexMemberSelectionEvent.RESOURCE_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    public ResourceDetailsController() {
        super( subscribedEvents, publishedEvents );
        log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(resourceDetails instanceof VBox))
            throw new IllegalStateException( "Member Details not injected by FXML." );
        if (!(memberName instanceof TextField))
            throw new IllegalStateException( "memberName not injected by FXML." );
        if (!(memberDescription instanceof TextField))
            throw new IllegalStateException( "member description not injected by FXML." );
    }

    @Override
    public void initialize() {
        log.debug( "Member Filter Controller - Initialize" );
        checkNodes();

    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        eventPublisherNode = resourceDetails;
        eventManager = mainController.getEventSubscriptionManager();
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexMemberSelectionEvent)
            handleEvent( (DexMemberSelectionEvent) event );
        else if (event instanceof DexResourceChildSelectionEvent)
            handleEvent( (DexResourceChildSelectionEvent) event );
        else if (event instanceof DexModelChangeEvent)
            handleEvent( (DexModelChangeEvent) event );
    }

    public void handleEvent(DexResourceChildSelectionEvent event) {
        post( event.get() );
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
        if (resource == null) {
            clear();
            return;
        }
        propertyGrid.getChildren().clear();


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
        }
        if (field.fxNode != null) {
            if (field.fxNode instanceof Control)
                ((Control) field.fxNode).setTooltip( field.tooltip );
            if (obj.isEditable())
                propertyGrid.add( field.fxNode, column, field.row + rowIndex );
            else {
                TextField txt = new TextField( "some value" );
                txt.setEditable( false );
                txt.setDisable( true );
                propertyGrid.add( txt, column, field.row + rowIndex );
            }
        }
    }

    private void postTitle(OtmObject obj) {
        objectImageView.setImage( ImageManager.getImage( obj.getIconType() ) );
        if (obj instanceof OtmResource)
            childObjectType.setText( obj.getOwningMember().getName() );
        else
            childObjectType.setText( obj.getObjectTypeName() + "   in " + obj.getOwningMember().getName() );
    }

    private void postName(OtmObject obj) {
        rowIndex = 0;
        Label label = new Label( "Name" );
        label.setTooltip( obj.getTooltip() );
        propertyGrid.add( label, 0, rowIndex );

        memberName.setEditable( obj.isEditable() );
        memberName.setText( obj.nameProperty().get() );
        memberName.setOnAction( e -> obj.nameProperty().set( memberName.getText() ) );
        propertyGrid.add( memberName, 1, rowIndex++ );
    }

    private void postDescription(OtmObject obj) {
        memberDescription.setEditable( obj.isEditable() );
        memberDescription.setText( obj.descriptionProperty().get() );
        memberDescription.setOnAction( e -> obj.descriptionProperty().set( memberDescription.getText() ) );
        propertyGrid.add( new Label( "Description" ), 0, rowIndex );
        propertyGrid.add( memberDescription, 1, rowIndex++ );
    }

    public void post(OtmResourceChild resourceChild) {
        clear();
        if (resourceChild == null)
            return;

        selectedChild = resourceChild;

        postTitle( resourceChild );
        postName( resourceChild );
        postDescription( resourceChild );

        resourceChild.getFields().forEach( f -> postField( f, resourceChild ) );
    }


    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            selectedChild = null;
            memberName.setText( "" );
            memberDescription.setText( "" );
            objectImageView.setImage( null );
            childObjectType.setText( "" );
            propertyGrid.getChildren().clear();
        }
    }

    @Override
    public void refresh() {
        post( selectedChild );
    }

}
