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
import org.opentravel.dex.actions.DexActionManager.DexActions;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.DialogBoxContoller;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmCore;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;
import org.opentravel.model.otmLibraryMembers.OtmResource;
import org.opentravel.model.otmLibraryMembers.OtmValueWithAttributes;

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
    private Button deleteButton;
    @FXML
    private Button addButton;
    @FXML
    private TextField memberDescription;

    // private OtmModelManager modelMgr;
    private OtmLibraryMember selectedMember;

    private boolean ignoreClear = false;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {DexMemberSelectionEvent.MEMBER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

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
        if (!(deleteButton instanceof Button))
            throw new IllegalStateException( "delete button not injected by FXML." );
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

        // moveButton.setOnAction(e -> postNotImplemented());
        changeBaseButton.setOnAction( e -> postNotImplemented() );
        changeTypeButton.setOnAction( e -> postNotImplemented() );
        addButton.setOnAction( e -> postNotImplemented() );
        deleteButton.setOnAction( e -> postNotImplemented() );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (event instanceof DexMemberSelectionEvent)
            memberSelectionHandler( (DexMemberSelectionEvent) event );
        if (event instanceof DexModelChangeEvent)
            modelChangeEvent( (DexModelChangeEvent) event );
    }

    public void modelChangeEvent(DexModelChangeEvent event) {
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
        // Collection<OtmTypeUser> u = member.getDescendantsTypeUsers();
        // Collection<OtmTypeProvider> p = member.getDescendantsTypeProviders();
        // Collection<OtmTypeProvider> c = member.getChildrenTypeProviders();

        objectLabel.setTooltip( new Tooltip( member.getObjectTypeName() ) );
        // if (imageMgr != null)
        // objectImageView.setImage( imageMgr.get_OLD( member.getIconType() ) );
        objectImageView.setImage( ImageManager.getImage( member.getIconType() ) );
        memberName.setEditable( member.isEditable() );
        memberName.setEditable( member.isEditable() );
        // memberName.textProperty().bind(member.nameProperty());
        memberName.setText( member.nameProperty().get() );
        memberName.setOnAction( e -> member.nameProperty().set( memberName.getText() ) );

        // Set library
        libraryName.setEditable( false );
        libraryName.setText( member.libraryProperty().get() );
        changeLibraryButton.setDisable( !member.isEditable() );
        changeLibraryButton.setDisable( true ); // TEMP

        // Description
        memberDescription.setEditable( member.isEditable() );
        memberDescription.setText( member.descriptionProperty().get() );
        memberDescription.setOnAction( e -> member.descriptionProperty().set( memberDescription.getText() ) );

        // Base type
        changeBaseButton.setDisable( true ); // TEMP
        baseTypeName.setText( member.baseTypeProperty().get() );

        // Assigned type
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

        // icon?
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
    }

    // Called when button is pressed
    private void setAssignedType() {
        log.debug( "Set assigned type event." );
        if (selectedMember instanceof OtmTypeUser)
            selectedMember.getActionManager().run( DexActions.TYPECHANGE, selectedMember, null );
        refresh();
    }

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
            // if (mainController != null)
            // modelMgr = mainController.getModelManager();
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

}
