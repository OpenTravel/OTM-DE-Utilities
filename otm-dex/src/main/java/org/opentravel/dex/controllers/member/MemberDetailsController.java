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

package org.opentravel.dex.controllers.member;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller for library member details controller.
 * 
 * @author dmh
 *
 */
public class MemberDetailsController extends DexIncludedControllerBase<Void> {
    private static Log log = LogFactory.getLog( MemberDetailsController.class );

    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private VBox memberDetails;
    @FXML
    private TextField memberName;
    @FXML
    private Label objectLabel;
    @FXML
    private ImageView objectImageView;
    @FXML
    private TextField libraryName;
    @FXML
    private Button changeLibraryButton;
    @FXML
    private Label typeLabel;
    @FXML
    private TextField baseTypeName;
    @FXML
    private Button changeBaseButton;
    @FXML
    private TextField assignedTypeName;
    @FXML
    private Button changeTypeButton;
    @FXML
    private TextField memberDescription;

    // private OtmModelManager modelMgr;
    private OtmLibraryMember selectedMember;

    private boolean ignoreClear = false;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {OtmObjectModifiedEvent.OBJECT_MODIFIED, OtmObjectChangeEvent.OBJECT_CHANGED,
            DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    public MemberDetailsController() {
        super( subscribedEvents, publishedEvents );
        log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(memberDetails instanceof VBox))
            throw new IllegalStateException( "Member Details not injected by FXML." );
        if (objectLabel == null)
            throw new IllegalStateException( "Object label not injected by FXML." );
        if (!(libraryName instanceof TextField))
            throw new IllegalStateException( "Library  not injected by FXML." );
        if (!(memberName instanceof TextField))
            throw new IllegalStateException( "memberName not injected by FXML." );
        if (!(typeLabel instanceof Label))
            throw new IllegalStateException( "label not injected by FXML." );
        // if (!(deleteButton instanceof Button))
        // throw new IllegalStateException( "delete button not injected by FXML." );
        if (!(memberDescription instanceof TextField))
            throw new IllegalStateException( "member description not injected by FXML." );
        if (!(assignedTypeName instanceof TextField))
            throw new IllegalStateException( "member type name not injected by FXML." );
    }

    @Override
    public void initialize() {
        log.debug( "Member Filter Controller - Initialize" );
        checkNodes();

    }

    @Override
    public void configure(DexMainController mainController) {
        super.configure( mainController );
        eventPublisherNode = memberDetails;

        // Button actions set in post() method
        // // moveButton.setOnAction(e -> postNotImplemented());
        // changeBaseButton.setOnAction( e -> postNotImplemented() );
        // changeTypeButton.setOnAction( e -> postNotImplemented() );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        log.debug( "Received event: " + event.getClass().getSimpleName() );
        if (event instanceof DexMemberSelectionEvent)
            memberSelectionHandler( (DexMemberSelectionEvent) event );
        if (event instanceof DexModelChangeEvent)
            handleEvent( (DexModelChangeEvent) event );
        else if (event instanceof OtmObjectChangeEvent)
            handleEvent( (OtmObjectChangeEvent) event );
        else if (event instanceof OtmObjectModifiedEvent)
            handleEvent( (OtmObjectModifiedEvent) event );
    }

    public void handleEvent(OtmObjectChangeEvent event) {
        if (event != null && event.get() == selectedMember)
            refresh();
    }

    public void handleEvent(OtmObjectModifiedEvent event) {
        if (event != null && event.get() == selectedMember)
            refresh();
    }

    public void handleEvent(DexModelChangeEvent event) {
        if (event.get() instanceof OtmLibraryMember)
            post( (OtmLibraryMember) event.get() );
        else
            clear();
    }

    public void memberSelectionHandler(DexMemberSelectionEvent event) {
        if (event != null && event.getMember() != null) {
            selectedMember = event.getMember();
            post( selectedMember );
        }
    }

    private void postNotImplemented() {
        DialogBoxContoller.init().show( "Not Implemented", "Work in progress." );
    }

    public void post(OtmLibraryMember member) {
        if (member == null) {
            clear();
            return;
        }
        selectedMember = member;

        objectLabel.setTooltip( new Tooltip( member.getObjectTypeName() ) );
        objectImageView.setImage( ImageManager.getImage( member.getIconType() ) );

        // Member name
        StringProperty property = member.nameProperty();
        memberName.setEditable( !(property instanceof ReadOnlyStringWrapper) );
        memberName.setText( property.get() );
        memberName.setOnAction( e -> {
            property.set( memberName.getText() );
            memberName.setText( property.get() );
        } );

        // Set library
        libraryName.setEditable( false );
        libraryName.setText( member.libraryProperty().get() );
        changeLibraryButton.setDisable( !member.getActionManager().isEnabled( DexActions.SETMEMBERLIBRARY, member ) );
        changeLibraryButton.setOnAction( e -> {
            member.getActionManager().run( DexActions.SETMEMBERLIBRARY, member );
            member.libraryProperty().set( member.getLibraryName() );
            libraryName.setText( member.getLibraryName() );
        } );

        // Description
        memberDescription.setEditable( member.isEditable() );
        memberDescription.setText( member.descriptionProperty().get() );
        memberDescription.setOnAction( e -> member.descriptionProperty().set( memberDescription.getText() ) );

        // Base type
        changeBaseButton.setDisable( !member.isEditable() );
        baseTypeName.setText( member.baseTypeProperty().get() );
        changeBaseButton.setOnAction( e -> {
            member.getActionManager().run( DexActions.BASETYPECHANGE, member );
            member.baseTypeProperty().set( member.getBaseTypeName() );
            baseTypeName.setText( member.getBaseTypeName() );
        } );
        // TESTME - add action

        // Assigned type label
        final String TYPELABEL = "Assigned Type";
        final String TYPELABELVWA = "Value Type";
        final String TYPELABELCORE = "Simple Type";
        final String TYPELABELRESOURCE = "Exposed Object";
        // OtmTypeProvider provider = null;
        typeLabel.setDisable( !(member instanceof OtmTypeUser) );
        typeLabel.setText( TYPELABEL );
        if (member instanceof OtmCore)
            typeLabel.setText( TYPELABELCORE );
        else if (member instanceof OtmValueWithAttributes)
            typeLabel.setText( TYPELABELVWA );
        else if (member instanceof OtmResource)
            typeLabel.setText( TYPELABELRESOURCE );

        // Assigned Type field
        if (member.getActionManager().isEnabled( DexActions.TYPECHANGE, member )) {
            changeTypeButton.setDisable( false );
            changeTypeButton.setOnAction( e -> setAssignedType() );
        } else {
            changeTypeButton.setDisable( true );
            changeTypeButton.setOnAction( null );
        }
        assignedTypeName.setDisable( (!(member instanceof OtmTypeUser)) );
        assignedTypeName.setEditable( false );
        if (member instanceof OtmTypeUser && ((OtmTypeUser) member).getAssignedType() != null) {
            assignedTypeName.setTooltip( new Tooltip( ((OtmTypeUser) member).getAssignedType().getDescription() ) );
            assignedTypeName.setText( ((OtmTypeUser) member).assignedTypeProperty().get() );
        } else {
            assignedTypeName.setTooltip( null );
            assignedTypeName.setText( "" );
        }

        // Action Buttons
        // deleteButton.setDisable( true );
        // addButton.setDisable( true );
        // This approach will work. Need to make sure not to re-add nodes
        // if (member instanceof OtmEnumeration) {
        // CheckBox openBox = new CheckBox( "Open" );
        // CheckBox closeBox = new CheckBox( "Close" );
        // // addButton.setVisible( false );
        // // deleteButton.setVisible( false );
        // Parent parent = deleteButton.getParent();
        // ((VBox) parent).getChildren().remove( addButton );
        // ((VBox) parent).getChildren().remove( deleteButton );
        // ((VBox) parent).getChildren().add( openBox );
        // openBox.setSelected( member instanceof OtmEnumerationOpen );
        // ((VBox) parent).getChildren().add( closeBox );
        // openBox.setSelected( member instanceof OtmEnumerationClosed );
        // } else {
        // Parent parent = deleteButton.getParent();
        // ((VBox) parent).getChildren().add( addButton );
        // ((VBox) parent).getChildren().add( deleteButton );
        // // ((VBox) parent).getChildren().add( openBox );
        // addButton.setVisible( true );
        // deleteButton.setVisible( true );
        // }
    }

    // Called when button is pressed
    private void setAssignedType() {
        log.debug( "Set assigned type event." );
        if (selectedMember instanceof OtmTypeUser)
            selectedMember.getActionManager().run( DexActions.TYPECHANGE, selectedMember );
        refresh();
    }

    @Override
    public void clear() {
        // When posting updated filter results, do not clear the filters.
        if (!ignoreClear) {
            selectedMember = null;
            assignedTypeName.setText( "" );
            memberName.setText( "" );
            libraryName.setText( "" );
            baseTypeName.setText( "" );
            memberDescription.setText( "" );
            objectImageView.setImage( null );
        }
    }

    @Override
    public void refresh() {
        post( selectedMember );
    }

    /**
     * Scan text fields name and description and force changes to be saved
     */
    public void commitChanges() {
        selectedMember.setName( memberName.getText() );
        selectedMember.nameProperty().set( memberName.getText() );
        //
        selectedMember.setDescription( memberDescription.getText() );
        selectedMember.descriptionProperty().set( memberDescription.getText() );
    }

}
