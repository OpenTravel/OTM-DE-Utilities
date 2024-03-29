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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.application.common.events.AbstractOtmEvent;
import org.opentravel.common.ImageManager;
import org.opentravel.common.ImageManager.Icons;
import org.opentravel.dex.action.manager.DexFullActionManager;
import org.opentravel.dex.action.manager.DexMinorVersionActionManager;
import org.opentravel.dex.actions.DexActions;
import org.opentravel.dex.actions.string.DeprecationChangeAction;
import org.opentravel.dex.controllers.DexIncludedControllerBase;
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.dex.controllers.popup.TextAreaEditorContoller;
import org.opentravel.dex.events.DexEventLockEvent;
import org.opentravel.dex.events.DexMemberDeleteEvent;
import org.opentravel.dex.events.DexMemberSelectionEvent;
import org.opentravel.dex.events.DexModelChangeEvent;
import org.opentravel.dex.events.OtmObjectChangeEvent;
import org.opentravel.dex.events.OtmObjectModifiedEvent;
import org.opentravel.dex.events.OtmObjectReplacedEvent;
import org.opentravel.model.OtmTypeUser;
import org.opentravel.model.otmLibraryMembers.OtmContextualFacet;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * Controller for library member details controller.
 * 
 * @author dmh
 *
 */
public class MemberDetailsController extends DexIncludedControllerBase<Void> {
    private static Logger log = LogManager.getLogger( MemberDetailsController.class );

    public static final String TIP_FULLEDIT = "Full editing enabled for new objects in a library.";
    public static final String TIP_MINOREDIT =
        "Minor editing enabled for objects from previous versions of the object from libraries with the same major version number. Minor editing allows objects and optional properties to be added.";
    public static final String TIP_READONLYEDIT =
        "Read-only because repository managed library is not locked by the user.";


    /**
     * FXML Java FX Nodes this controller is dependent upon
     */
    @FXML
    private TitledPane memberDetails;
    @FXML
    private GridPane memberGridPane;
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
    private Label baseTypeLabel;
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
    @FXML
    private Button descriptionEditButton;
    @FXML
    private RadioButton editreadonly;
    @FXML
    private RadioButton editminor;
    @FXML
    private RadioButton editfull;
    @FXML
    private Button lockButton;


    // private OtmModelManager modelMgr;
    private OtmLibraryMember selectedMember;
    private boolean eventLock = false;
    private boolean ignoreClear = false;
    private Label deprecationLabel;
    private TextField deprecationField;
    private Button deprecationButton;

    // private int viewGroup = 1;

    // All event types fired by this controller.
    private static final EventType[] publishedEvents = {DexEventLockEvent.EVENT_LOCK};

    // All event types listened to by this controller's handlers
    private static final EventType[] subscribedEvents =
        {OtmObjectReplacedEvent.OBJECT_REPLACED, OtmObjectModifiedEvent.OBJECT_MODIFIED,
            OtmObjectChangeEvent.OBJECT_CHANGED, DexMemberDeleteEvent.MEMBER_DELETED,
            DexMemberSelectionEvent.MEMBER_SELECTED, DexMemberSelectionEvent.TYPE_USER_SELECTED,
            DexMemberSelectionEvent.TYPE_PROVIDER_SELECTED, DexModelChangeEvent.MODEL_CHANGED};

    public MemberDetailsController() {
        super( subscribedEvents, publishedEvents );
        // log.debug( "Member Details Controller constructor." );
    }

    @Override
    public void checkNodes() {
        if (!(memberDetails instanceof TitledPane))
            throw new IllegalStateException( "Member Details not injected by FXML." );
        if (!(memberGridPane instanceof GridPane))
            throw new IllegalStateException( "Member grid not injected by FXML." );
        if (objectLabel == null)
            throw new IllegalStateException( "Object label not injected by FXML." );
        if (!(libraryName instanceof TextField))
            throw new IllegalStateException( "Library  not injected by FXML." );
        if (!(memberName instanceof TextField))
            throw new IllegalStateException( "memberName not injected by FXML." );
        if (!(typeLabel instanceof Label))
            throw new IllegalStateException( "label not injected by FXML." );
        if (!(memberDescription instanceof TextField))
            throw new IllegalStateException( "member description not injected by FXML." );
        if (!(assignedTypeName instanceof TextField))
            throw new IllegalStateException( "member type name not injected by FXML." );
    }

    @Override
    public void initialize() {
        // log.debug( "Member Filter Controller - Initialize" );
        checkNodes();
    }

    @Override
    public void configure(DexMainController mainController, int viewGroupId) {
        super.configure( mainController, viewGroupId );
        eventPublisherNode = memberDetails;

        editfull.setTooltip( new Tooltip( TIP_FULLEDIT ) );
        editminor.setTooltip( new Tooltip( TIP_MINOREDIT ) );
        editreadonly.setTooltip( new Tooltip( TIP_READONLYEDIT ) );
        postEventLock();
        lockButton.setOnAction( e -> changeEventLock() );
    }

    @Override
    public void handleEvent(AbstractOtmEvent event) {
        if (eventLock)
            return;
        // log.debug( "Received event: " + event.getClass().getSimpleName() + ":" + event.getEventType() );
        if (event instanceof DexMemberSelectionEvent)
            handleEvent( (DexMemberSelectionEvent) event );
        if (event instanceof DexModelChangeEvent)
            handleEvent( (DexModelChangeEvent) event );
        else if (event instanceof OtmObjectChangeEvent)
            handleEvent( (OtmObjectChangeEvent) event );
        else if (event instanceof OtmObjectModifiedEvent)
            handleEvent( (OtmObjectModifiedEvent) event );
        else if (event instanceof OtmObjectReplacedEvent)
            handleEvent( (OtmObjectReplacedEvent) event );
        else if (event instanceof DexMemberDeleteEvent)
            handleEvent( (DexMemberDeleteEvent) event );
    }

    public void handleEvent(OtmObjectChangeEvent event) {
        if (event != null && event.get() instanceof OtmLibraryMember)
            post( (OtmLibraryMember) event.get() );
        else
            refresh();
    }

    public void handleEvent(OtmObjectReplacedEvent event) {
        post( event.get().getOwningMember() );
    }

    public void handleEvent(DexMemberDeleteEvent event) {
        if (event.getAlternateMember() != null)
            post( event.getAlternateMember() );
        else
            clear();
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

    public void handleEvent(DexMemberSelectionEvent event) {
        if (event != null && event.getMember() != null) {
            selectedMember = event.getMember();
            post( selectedMember );
        }
    }

    public void post(OtmLibraryMember member) {
        if (member == null) {
            clear();
            return;
        }
        selectedMember = member;
        objectLabel.setText( member.getObjectTypeName() );
        objectLabel.setTooltip( new Tooltip( member.getObjectTypeName() ) );
        objectImageView.setImage( ImageManager.getImage( member.getIconType() ) );

        // Member name
        // StringProperty property = member.nameProperty();
        StringProperty property = member.nameEditingProperty();
        memberName.setEditable( !(property instanceof ReadOnlyStringWrapper) );
        memberName.setText( property.get() );
        memberName.setOnAction( e -> {
            property.set( memberName.getText() );
            memberName.setText( property.get() );
        } );
        // Set library
        libraryName.setEditable( false );
        libraryName.setText( member.libraryProperty().get() );
        changeLibraryButton.setDisable( !member.getActionManager().isEnabled( DexActions.SETLIBRARY, member ) );
        changeLibraryButton.setOnAction( e -> {
            commitChanges();
            member.getActionManager().run( DexActions.SETLIBRARY, member );
            member.libraryProperty().set( member.getLibraryName() );
            libraryName.setText( member.getLibraryName() );
            refresh();
        } );

        // Description
        memberDescription.setEditable( member.isEditable() );
        memberDescription.setText( member.descriptionProperty().get() );
        memberDescription.setOnAction( e -> member.descriptionProperty().set( memberDescription.getText() ) );
        descriptionEditButton.setDisable( !member.isEditable() );
        descriptionEditButton
            .setOnAction( ae -> TextAreaEditorContoller.init().showAndWait( member.descriptionProperty() ) );

        // Base type
        final String BASETYPELABEL = "Base Type";
        final String BASETYPELABELCF = "Contributed To";
        if (member instanceof OtmContextualFacet)
            baseTypeLabel.setText( BASETYPELABELCF );
        else
            baseTypeLabel.setText( BASETYPELABEL );

        changeBaseButton.setDisable( !member.getActionManager().isEnabled( DexActions.BASETYPECHANGE, member ) );
        baseTypeName.setText( member.baseTypeProperty().get() );
        changeBaseButton.setOnAction( e -> {
            member.getActionManager().run( DexActions.BASETYPECHANGE, member );
            member.baseTypeProperty().set( member.getBaseTypeName() );
            baseTypeName.setText( member.getBaseTypeName() );
        } );

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


        postEventLock();

        editfull.setDisable( true );
        editreadonly.setDisable( true );
        editminor.setDisable( true );
        if (member.getActionManager() instanceof DexFullActionManager)
            post( editfull );
        else if (member.getActionManager() instanceof DexMinorVersionActionManager)
            post( editminor );
        else
            post( editreadonly );

        // If deprecated, add a row. Otherwise, remove it.
        postDeprecation( member );
    }

    private void postEventLock() {
        if (eventLock) {
            lockButton.setGraphic( new ImageView( ImageManager.getImage( Icons.UNLOCK ) ) );
            lockButton.setText( "Unlock" );
            lockButton.setTooltip( new Tooltip( "UnLock to allow display to change." ) );
        } else {
            lockButton.setGraphic( new ImageView( ImageManager.getImage( Icons.LOCK ) ) );
            lockButton.setText( "Lock" );
            lockButton.setTooltip( new Tooltip( "Lock to prevent display from changing." ) );
        }
    }

    private void changeEventLock() {
        if (eventLock)
            eventLock = false;
        else
            eventLock = true;
        postEventLock();
        fireEvent( new DexEventLockEvent( eventLock, getViewGroupId() ) );
    }

    private void postDeprecation(OtmLibraryMember member) {
        if (member.isDeprecated()) {
            int row = 4;
            if (deprecationLabel == null) {
                deprecationLabel = new Label( "Deprecation" );
                memberGridPane.add( deprecationLabel, 0, row );
            }
            if (deprecationField == null) {
                deprecationField = new TextField( "" );
                GridPane.setConstraints( deprecationField, 1, row, 3, 1 );
                memberGridPane.add( deprecationField, 1, row );
            }
            deprecationField.setEditable( DeprecationChangeAction.isEnabled( member ) );
            deprecationField.setOnAction( e -> member.deprecationProperty().set( deprecationField.getText() ) );
            deprecationField.setText( member.getDeprecation() );

            if (deprecationButton == null) {
                deprecationButton = new Button( "Edit" );
                memberGridPane.add( deprecationButton, 4, row );
            }
            deprecationButton.setDisable( !DeprecationChangeAction.isEnabled( member ) );
            deprecationButton
                .setOnAction( ae -> TextAreaEditorContoller.init().showAndWait( member.deprecationProperty() ) );
        } else {
            memberGridPane.getChildren().remove( deprecationLabel );
            memberGridPane.getChildren().remove( deprecationField );
            memberGridPane.getChildren().remove( deprecationButton );
            deprecationLabel = null;
            deprecationButton = null;
            deprecationField = null;
        }
    }

    private void post(RadioButton button) {
        button.setSelected( true );
        button.setDisable( false );
    }

    // Called when button is pressed
    private void setAssignedType() {
        // log.debug( "Set assigned type event." );
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
        log.debug( "Committing changes." );
        selectedMember.setName( memberName.getText() );
        selectedMember.nameProperty().set( memberName.getText() );
        //
        selectedMember.setDescription( memberDescription.getText() );
        selectedMember.descriptionProperty().set( memberDescription.getText() );
    }

}
