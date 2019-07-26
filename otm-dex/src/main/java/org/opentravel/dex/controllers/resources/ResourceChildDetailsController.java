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
import org.opentravel.common.DexEditField;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.DexResourceChildSelectionEvent;
import org.opentravel.model.OtmObject;
import org.opentravel.model.OtmResourceChild;
import org.opentravel.model.otmLibraryMembers.OtmResource;

import javafx.event.Event;
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
 * Controller for resource details controller.
 * 
 * @author dmh
 *
 */
public class ResourceChildDetailsController extends DexIncludedControllerBase<Void> {
    private static Log log = LogFactory.getLog( ResourceChildDetailsController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private VBox resourceChildDetails;
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

    // private OtmModelManager modelMgr;
    private OtmResourceChild selectedChild;
    private int rowIndex = 0; // last row populated with data

    private boolean ignoreClear = false;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents = {DexResourceChildSelectionEvent.RESOURCE_CHILD_SELECTED,
        DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    public ResourceChildDetailsController() {
        super( subscribedEvents, publishedEvents );
        log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(resourceChildDetails instanceof VBox))
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
        eventPublisherNode = resourceChildDetails;

        // moveButton.setOnAction(e -> postNotImplemented());
    }

    @Override
    public void handleEvent(Event event) {
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

    private void postNotImplemented() {
        DialogBoxContoller.init().show( "Not Implemented", "Work in progress." );
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
        Label label = new Label( field.label );
        label.setTooltip( field.tooltip );
        propertyGrid.add( label, field.column, field.row + rowIndex );

        if (field.fxNode instanceof Control)
            ((Control) field.fxNode).setTooltip( field.tooltip );
        if (obj.isEditable())
            propertyGrid.add( field.fxNode, field.column + 1, field.row + rowIndex );
        else {
            TextField txt = new TextField( "some value" );
            txt.setEditable( false );
            txt.setDisable( true );
            propertyGrid.add( txt, field.column + 1, field.row + rowIndex );
        }
        // TODO - update row index
    }

    private void postTitle(OtmObject obj) {
        objectImageView.setImage( ImageManager.getImage( obj.getIconType() ) );
        if (obj instanceof OtmResource)
            childObjectType.setText( obj.getOwningMember().getName() );
        else
            childObjectType.setText( obj.getObjectTypeName() + "   in " + obj.getOwningMember().getName() );
        // childObjectType.setText( obj.getOwningMember().getName() + ": " + obj.getObjectTypeName() );
        // childObjectType.setTooltip( obj.getTooltip() );
    }

    private void postName(OtmObject obj) {
        rowIndex = 0;
        Label label = new Label( "Name" );
        // Label label = new Label( obj.getObjectTypeName() );
        // label.setImage( ImageManager.getImage( obj.getIconType() ) );
        label.setTooltip( obj.getTooltip() );
        // propertyGrid.add( ImageManager.get( obj ), 2, rowIndex );
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
        // propertyGrid.add( memberDescription, 1, rowIndex++, 2, 1 ); // SPAN Columns!
    }

    public void post(OtmResourceChild resourceChild) {
        if (resourceChild == null) {
            clear();
            return;
        }
        // TODO - how to do a DE like getting of fields to be posted?
        // GridPane gridpane = new GridPane();
        // gridpane.add(new Button(), 1, 0); // column=1 row=0
        // gridpane.add(new Label(), 2, 0); // column=2 row=0

        selectedChild = resourceChild;

        // objectImageView.setImage( ImageManager.getImage( resourceChild.getIconType() ) );
        // childObjectType.setText( selectedChild.getObjectTypeName() + " - " + selectedChild.getName() );
        postTitle( resourceChild );

        propertyGrid.getChildren().clear();

        // Name
        // memberName.setEditable( resourceChild.isEditable() );
        // memberName.setText( resourceChild.nameProperty().get() );
        // memberName.setOnAction( e -> resourceChild.nameProperty().set( memberName.getText() ) );
        // propertyGrid.add( new Label( "Name" ), 0, 0 );
        // propertyGrid.add( memberName, 1, 0 );

        postName( resourceChild );
        postDescription( resourceChild );

        resourceChild.getFields().forEach( f -> postField( f, resourceChild ) );
        // int columnIndex = 0;
        // int rowIndex = 1; // 0 is for name
        // for (DexEditField field : resourceChild.getFields()) {
        // propertyGrid.add( new Label( field.label ), columnIndex, rowIndex );
        // if (resourceChild.isEditable())
        // propertyGrid.add( field.fxNode, columnIndex + 1, rowIndex++ );
        // else {
        // TextField txt = new TextField( "some value" );
        // txt.setEditable( false );
        // txt.setDisable( true );
        // propertyGrid.add( txt, columnIndex + 1, rowIndex++ );
        // }
        // }

        // Description
        // memberDescription.setEditable( resourceChild.isEditable() );
        // memberDescription.setText( resourceChild.descriptionProperty().get() );
        // memberDescription.setOnAction( e -> resourceChild.descriptionProperty().set( memberDescription.getText() ) );
        // propertyGrid.add( new Label( "Description" ), 0, rowIndex );
        // propertyGrid.add( memberDescription, 1, rowIndex, 2, 1 );
    }

    // // Called when button is pressed
    // private void setAssignedType() {
    // log.debug( "Set assigned type event." );
    // if (selectedChild instanceof OtmTypeUser)
    // selectedChild.getActionManager().run( DexActions.TYPECHANGE, selectedChild, null );
    // refresh();
    // }

    // /**
    // * Make and fire a filter event. Set ignore clear in case event handler tries to clear() this controller.
    // */
    // private void fireFilterChangeEvent() {
    // if (eventPublisherNode != null) {
    // ignoreClear = true; // Set just in case event handler does a clear
    // log.debug("Ready to fire controller level Filter Change event.");
    // eventPublisherNode.fireEvent(new DexFilterChangeEvent(this, memberDetails));
    // ignoreClear = false;
    // } else if (popupController != null) {
    // popupController.refresh();
    // }
    // }

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
